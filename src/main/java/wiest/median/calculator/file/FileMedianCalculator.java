package wiest.median.calculator.file;

import wiest.median.DataSource;
import wiest.median.MedianCalculator;

public class FileMedianCalculator implements MedianCalculator {

    private final FileDataSet dataSet;

    public FileMedianCalculator(String fileDir, int maxDataInMemoryKb) {
        dataSet = new FileDataSet(fileDir, maxDataInMemoryKb);
    }

    @Override
    public void loadSource(DataSource source) {
        while (source.hasNext()) {
            dataSet.addNumber(source.getNext());
        }
        source.close();
    }

    @Override
    public double calculateMedian() {
        int size = dataSet.getTotalSize();

        if (size == 0) {
            return 0;
        }

        if (size % 2 == 1) {
            return dataSet.getEntryAtIndex(size/2);
        } else {
            return (dataSet.getEntryAtIndex(size/2-1) + dataSet.getEntryAtIndex(size/2)) / 2;
        }
    }

    @Override
    public void destroy() {
        dataSet.destroy();
    }

}
