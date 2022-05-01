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


public class FileDataSetContainer {

    private static final Logger LOG = LoggerFactory.getLogger(FileDataSetContainer.class);

    private final double inclusiveMin;
    private final double inclusiveMax;
    private final String dir;

    private final DoubleArrayList memoryCache = new DoubleArrayList();
    private final File storageFile;

    private int fileNumberCount = 0;

    public FileDataSetContainer(String dir, double inclusiveMin, double inclusiveMax) {
        this(dir, inclusiveMin, inclusiveMax, new DoubleArrayList());
    }

    public FileDataSetContainer(String dir, double inclusiveMin, double inclusiveMax, DoubleList initData) {
        this.inclusiveMin = inclusiveMin;
        this.inclusiveMax = inclusiveMax;
        this.memoryCache.addAll(initData);
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
    }

    public int getTotalEntryCount() {
        return memoryCache.size() + fileNumberCount;
    }

    public int getCacheNumberCount() {
        return memoryCache.size();
    }

    public double getInclusiveMin() {
        return inclusiveMin;
    }

    public double getInclusiveMax() {
        return inclusiveMax;
    }

    public void add(double number) {
        if (number < inclusiveMin || number > inclusiveMax) {
            throw new FileDataSetException(String.format(
                    "Trying to add a number outside of the datasets bounds! %f in [%f, %f]",
                    number, inclusiveMin, inclusiveMax));
        }
        memoryCache.add(number);
    }

    public void writeToFile() {
        try {
            LOG.debug("Writing to file: {}", this);
            // TODO: Compress this file to save disk space as numbers can be compressed quite well usually
            var numbersSorted = getAllEntriesSorted();
            fileNumberCount = numbersSorted.size();
            memoryCache.clear();
            memoryCache.trim();
            BinIO.storeDoubles(numbersSorted.iterator(), storageFile);
        } catch (IOException e) {
            throw new FileDataSetException("Error during writing dataset to file", e);
        }
    }

    public DoubleList getAllEntriesSorted() {
        try {
            // TODO: since fileList is already sorted one could easily merge memoryCache into fileList in one iteration
            var fileList = new DoubleArrayList(BinIO.loadDoubles(storageFile));
            fileList.addAll(memoryCache);
            fileList.sort(DoubleComparators.asDoubleComparator(Double::compareTo));
            return fileList;
        } catch (IOException e) {
            throw new FileDataSetException("Error during loading dataset from file", e);
        }
    }


    public ContainerSplitResult splitInHalf() {
        var totalEntries = getAllEntriesSorted();
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
        var success = storageFile.delete();
        if (!success) {
            LOG.error("Could not delete storage file! Name: " + storageFile.getName());
        }
    }

    @Override
    public String toString() {
        return "DataSetPart{" +
                "inclusiveMin=" + inclusiveMin +
                ", exclusiveMax=" + inclusiveMax +
                ", cacheNumberCount=" + memoryCache.size() +
                ", fileNumberCount=" + fileNumberCount +
                '}';
    }

    public record ContainerSplitResult(FileDataSetContainer lowerBound, FileDataSetContainer upperBound) {

        public FileDataSetContainer getLowerContainer() {
                return lowerBound;
            }

        public FileDataSetContainer getUpperContainer() {
                return upperBound;
            }
    }
}
