package wiest.median.calculator.file;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.io.BinIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.DoubleStream;


public class FileDataSetContainer {

    private static final Logger LOG = LoggerFactory.getLogger(FileDataSetContainer.class);

    private final double inclusiveMin;
    private final double inclusiveMax;
    private final String dir;

    private int memoryCacheCount = 0;
    private final File storageFile;

    private int fileNumberCount = 0;

    public FileDataSetContainer(String dir, double inclusiveMin, double inclusiveMax) {
        this(dir, inclusiveMin, inclusiveMax, new DoubleArrayList());
    }

    public FileDataSetContainer(String dir, double inclusiveMin, double inclusiveMax, DoubleList initData) {
        this.inclusiveMin = inclusiveMin;
        this.inclusiveMax = inclusiveMax;
        this.dir = dir;

        storageFile = new File(dir + "/Median_" + UUID.randomUUID());
        try {
            boolean success = storageFile.createNewFile();
            if (!success) {
                throw new FileDataSetException("File creation not successful");
            }
        } catch (IOException e) {
            throw new FileDataSetException("Cannot create local storage file", e);
        }
        mergeAndWriteToFile(initData);
    }

    public int getTotalEntryCount() {
        return memoryCacheCount + fileNumberCount;
    }

    public int getCacheNumberCount() {
        return memoryCacheCount;
    }

    public double getInclusiveMin() {
        return inclusiveMin;
    }

    public double getInclusiveMax() {
        return inclusiveMax;
    }

    public void increaseCacheEntryCount() {
        memoryCacheCount++;
    }

    public void mergeAndWriteToFile(DoubleList data) {
        try {
            LOG.debug("Writing to file: {}", this);

            // TODO: Compress this file to save disk space as numbers can be compressed quite well usually
            var storedData = getMergedStorageData(data);
            fileNumberCount = storedData.size();
            BinIO.storeDoubles(storedData.iterator(), storageFile);

            memoryCacheCount = 0;

        } catch (IOException e) {
            throw new FileDataSetException("Error during writing dataset to file", e);
        }
    }

    public DoubleList getMergedStorageData(DoubleList dataToMerge) {
        return DoubleArrayList.wrap(
                DoubleStream.concat(getStoredData().doubleStream(), dataToMerge.doubleStream())
                .sorted().toArray()
        );
    }

    private DoubleList getStoredData() {
        try {
            return DoubleArrayList.wrap(BinIO.loadDoubles(storageFile));
        } catch (IOException e) {
            throw new FileDataSetException("Error during loading dataset from file", e);
        }
    }

    public ContainerSplitResult splitInHalf() {
        if (memoryCacheCount > 0) {
            throw new FileDataSetException("Cannot split file with content in memory");
        }

        var totalEntries = getStoredData();
        if (totalEntries.size() < 2) {
            throw new FileDataSetException("Cannot split file with less than two elements in half");
        }

        var splitIndex = totalEntries.size() / 2;
        var splitElement = totalEntries.getDouble(splitIndex);

        var lowerContainer = new FileDataSetContainer(
                this.dir,
                this.getInclusiveMin(),
                splitElement,
                totalEntries.subList(0, splitIndex));
        var upperContainer = new FileDataSetContainer(
                this.dir,
                splitElement,
                this.getInclusiveMax(),
                totalEntries.subList(splitIndex, totalEntries.size()));

        return new ContainerSplitResult(lowerContainer, upperContainer);
    }

    public void deleteLocalStorage() {
        if (!storageFile.delete()) {
            LOG.error("Could not delete storage file! Name: " + storageFile.getName());
        }
    }

    @Override
    public String toString() {
        return "DataSetPart{" +
                "inclusiveMin=" + inclusiveMin +
                ", exclusiveMax=" + inclusiveMax +
                ", cacheNumberCount=" + memoryCacheCount +
                ", fileNumberCount=" + fileNumberCount +
                '}';
    }

    public record ContainerSplitResult(FileDataSetContainer lowerContainer, FileDataSetContainer upperContainer) {

        public FileDataSetContainer getLowerContainer() {
            return lowerContainer;
        }

        public FileDataSetContainer getUpperContainer() {
            return upperContainer;
        }
    }
}
