package wiest.median.calculator.file;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.junit.jupiter.api.Test;
import wiest.median.calculator.file.FileDataSetContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileDataSetContainerTest extends LocalFileTest {


    @Test
    void trackingCacheData() {
        FileDataSetContainer container = new FileDataSetContainer(TEST_DATA_DIR, -10, 10);
        container.increaseCacheEntryCount();

        assertEquals(1, container.getCacheNumberCount());
    }

    @Test
    void testInitStoreDataInFiles() {
        FileDataSetContainer container = new FileDataSetContainer(TEST_DATA_DIR, -10, 10, DoubleList.of(5));

        assertEquals(List.of(5.), container.getMergedStorageData(DoubleList.of()));
    }

    @Test
    void testStoreDataInFiles() {
        FileDataSetContainer container = new FileDataSetContainer(TEST_DATA_DIR, -10, 10);
        container.mergeAndWriteToFile(DoubleList.of(5.));

        assertEquals(List.of(5.), container.getMergedStorageData(DoubleList.of()));
    }

    @Test
    void getMergedData() {
        FileDataSetContainer container = new FileDataSetContainer(TEST_DATA_DIR, -10, 10);
        container.mergeAndWriteToFile(DoubleList.of(5.));

        assertEquals(List.of(5., 10.), container.getMergedStorageData(DoubleList.of(10.)));
    }

    @Test
    void testSplitContainerUnevenSize() {
        FileDataSetContainer container = new FileDataSetContainer(TEST_DATA_DIR, -20, 20);
        container.mergeAndWriteToFile(DoubleList.of(1., 2., 3.));


        var result = container.splitInHalf();
        var firstContainer = result.getLowerContainer();
        var secondContainer = result.getUpperContainer();

        assertEquals(-20, firstContainer.getInclusiveMin());
        assertEquals(2, firstContainer.getInclusiveMax());
        assertEquals(1, firstContainer.getTotalEntryCount());

        assertEquals(2, secondContainer.getInclusiveMin());
        assertEquals(20, secondContainer.getInclusiveMax());
        assertEquals(2, secondContainer.getTotalEntryCount());
    }

    @Test
    void testSplitContainerEvenSize() {
        FileDataSetContainer container = new FileDataSetContainer(TEST_DATA_DIR, -20, 20);
        container.mergeAndWriteToFile(DoubleList.of(1., 2., 3., 4.));

        var result = container.splitInHalf();
        var firstContainer = result.getLowerContainer();
        var secondContainer = result.getUpperContainer();

        assertEquals(-20, firstContainer.getInclusiveMin());
        assertEquals(3, firstContainer.getInclusiveMax());
        assertEquals(2, firstContainer.getTotalEntryCount());

        assertEquals(3, secondContainer.getInclusiveMin());
        assertEquals(20, secondContainer.getInclusiveMax());
        assertEquals(2, secondContainer.getTotalEntryCount());
    }

    @Test
    void testSplitEqualNumbers() {
        FileDataSetContainer container = new FileDataSetContainer(TEST_DATA_DIR, -20, 20);
        container.mergeAndWriteToFile(DoubleList.of(1., 2., 3., 3., 3., 4., 5.));

        var result = container.splitInHalf();
        var firstContainer = result.getLowerContainer();
        var secondContainer = result.getUpperContainer();

        assertEquals(-20, firstContainer.getInclusiveMin());
        assertEquals(3, firstContainer.getInclusiveMax());
        assertEquals(3, firstContainer.getTotalEntryCount());

        assertEquals(3, secondContainer.getInclusiveMin());
        assertEquals(20, secondContainer.getInclusiveMax());
        assertEquals(4, secondContainer.getTotalEntryCount());
    }

}