package wiest.median.calculator.file;

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

 Since we are using ArrayLists it might come to the worst case scenario that every ArrayList expands
 to way more size than actually needed, resulting in memory wasted for empty elements
 Furthermore, in {@link FileDataSetContainer#getAllEntriesSorted()} the sorting likely also uses additional memory.

 One solution to the ArrayList problem would be having a single global fixed-size list
 which would then need some more logic to be matched to a corresponding file.

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

    private int totalCacheEntryCount = 0;

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
    }

    public void addNumber(double number) {
        LOG.trace("Adding number: {}", number);
        if (totalCacheEntryCount >= maxCacheOrFileEntryCount) {
            LOG.debug("Max Cache Count hit - Total entry count: {}", getTotalSize());
            storeLargestContainer();
        }

        var matchingContainer = getMatchingContainer(number);
        matchingContainer.add(number);
        totalCacheEntryCount++;
    }

    private FileDataSetContainer getMatchingContainer(double number) {
        return containers.stream()
                .filter(ds -> ds.getInclusiveMin() <= number && number <= ds.getInclusiveMax())
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("No dataset exists for value %f", number)));
    }

    private void storeLargestContainer() {
        var maxDataContainer = getLargestContainer();
        totalCacheEntryCount -= maxDataContainer.getCacheNumberCount();

        if (maxDataContainer.getTotalEntryCount() <= maxCacheOrFileEntryCount) {
            maxDataContainer.writeToFile();
        } else {
            splitContainer(maxDataContainer);
        }

        LOG.debug("Reduced cache to: {} entries", totalCacheEntryCount);
    }

    private FileDataSetContainer getLargestContainer() {
        return containers.stream()
                .max(Comparator.comparing(FileDataSetContainer::getCacheNumberCount))
                .orElseThrow(() -> new IllegalStateException("Cache not initialized"));
    }

    private void splitContainer(FileDataSetContainer targetContainer) {
        var splitResult = targetContainer.splitInHalf();
        var containerIndex = containers.indexOf(targetContainer);

        containers.add(containerIndex, splitResult.getUpperContainer());
        containers.add(containerIndex, splitResult.getLowerContainer());

        splitResult.getUpperContainer().writeToFile();
        splitResult.getLowerContainer().writeToFile();

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
                    return container.getAllEntriesSorted().getDouble(index - entryCounter);
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
        totalCacheEntryCount = 0;
        containers.clear();
    }
}
