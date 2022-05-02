package wiest.median.calculator.file;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 In an optimal implementation we would allocate our memory only for the in-memory cache and the file size:
 We need to be able to load one full file into memory in addition to the full cache.
 Plus we have some small overhead from the {@link FileDataSet#containers} list and
 the {@link FileDataSetContainer} class.
 For simplicity, we divide the memory evenly between the file size and the cache size.

 In the worst case scenario the copying from cache to file could increase memory consumption by 100%

 Given more time one could certainly optimize some things here :)
 */
public class FileDataSet {

    /**
     * For simplicity, we just take half of the allowed memory for files and for the in-memory cache
     */
    private static final int MAGIC_MEMORY_SPLIT_FACTOR = 2;

    private static final Logger LOG = LoggerFactory.getLogger(FileDataSet.class);

    private final int maxCacheOrFileEntryCount;
    private final List<FileDataSetContainer> containers = new ArrayList<>();
    private final DoubleList memoryCache;

    public FileDataSet(String fileDir, int maxDataInMemoryKb) {
        File dir = new File(fileDir);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new FileDataSetException("Cannot create data dir");
            }
        }

        long entryCount = maxDataInMemoryKb * 1024L / Double.BYTES;
        if (entryCount > Integer.MAX_VALUE) {
            throw new FileDataSetException("Cannot store more entries than an Integer can index for now");
        }

        maxCacheOrFileEntryCount = (int) entryCount / MAGIC_MEMORY_SPLIT_FACTOR;
        containers.add(new FileDataSetContainer(fileDir, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        LOG.info("Creating dataset that can cache {} doubles in memory", maxCacheOrFileEntryCount);

        memoryCache = new DoubleArrayList(maxCacheOrFileEntryCount);
    }

    public void addNumber(double number) {
        LOG.trace("Adding number: {}", number);
        if (memoryCache.size() >= maxCacheOrFileEntryCount-1) {
            LOG.debug("Max Cache Count hit - Total entry count: {}", getTotalSize());
            storeLargestContainer();
        }

        var matchingContainer = getMatchingContainer(number);
        matchingContainer.increaseCacheEntryCount();
        memoryCache.add(number);
    }

    private FileDataSetContainer getMatchingContainer(double number) {
        return containers.stream()
                .filter(ds -> ds.getInclusiveMin() <= number && number <= ds.getInclusiveMax())
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("No dataset exists for value %f", number)));
    }

    private void storeLargestContainer() {
        var maxDataContainer = getLargestContainer();
        var cacheData = getCacheEntriesForContainer(maxDataContainer);
        memoryCache.removeAll(cacheData);

        if (maxDataContainer.getTotalEntryCount() > maxCacheOrFileEntryCount) {
            splitAndStoreContainer(maxDataContainer, cacheData);
        } else {
            maxDataContainer.mergeAndWriteToFile(cacheData);
        }

        LOG.debug("Reduced cache to: {} entries", memoryCache.size());
    }

    private FileDataSetContainer getLargestContainer() {
        return containers.stream()
                .max(Comparator.comparing(FileDataSetContainer::getCacheNumberCount))
                .orElseThrow(() -> new IllegalStateException("Cache not initialized"));
    }

    private DoubleList getCacheEntriesForContainer(FileDataSetContainer container) {
        return new DoubleArrayList(memoryCache.doubleStream()
                .filter(d -> d >= container.getInclusiveMin() && d <= container.getInclusiveMax())
                .iterator());
    }

    private void splitAndStoreContainer(FileDataSetContainer targetContainer, DoubleList cacheData) {
        var splitResult = targetContainer.splitInHalf(cacheData);
        var containerIndex = containers.indexOf(targetContainer);

        containers.add(containerIndex, splitResult.getUpperContainer());
        containers.add(containerIndex, splitResult.getLowerContainer());

        LOG.debug("Split data file {} into {} and {}", targetContainer, splitResult.getLowerContainer(), splitResult.getUpperContainer());

        targetContainer.deleteLocalStorage();
        containers.remove(targetContainer);
    }

    public int getTotalSize() {
        return containers.stream()
                .reduce(0, (sum, container) -> sum + container.getTotalEntryCount(), Integer::sum);
    }

    public double getEntryAtIndex(int index) {
        if (index >= 0) {
            int entryCounter = 0;
            for (var container : containers) {
                if (entryCounter + container.getTotalEntryCount() > index) {
                    var cacheData = getCacheEntriesForContainer(container);
                    var mergedData = container.getMergedStorageData(cacheData);
                    return mergedData.getDouble(index - entryCounter);
                }
                entryCounter += container.getTotalEntryCount();
            }
        }

        throw new IllegalArgumentException("Index out of bounds");
    }

    public void destroy() {
        for (var container : containers) {
            container.deleteLocalStorage();
        }
        containers.clear();
    }
}
