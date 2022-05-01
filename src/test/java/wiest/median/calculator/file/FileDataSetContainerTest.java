package wiest.median.calculator.file;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileDataSetContainerTest extends LocalFileTest {


    @Test
    void testAddData() {
        FileDataSetContainer container = new FileDataSetContainer(TEST_DATA_DIR, -10, 10);
        container.add(5);

        assertEquals(1, container.getCacheNumberCount());
        assertEquals(List.of(5.), container.getAllEntriesSorted());
    }

    @Test
    void testStoreDataInFiles() {
        FileDataSetContainer container = new FileDataSetContainer(TEST_DATA_DIR, -10, 10);
        container.add(5);
        container.writeToFile();

        assertEquals(List.of(5.), container.getAllEntriesSorted());
    }

    @Test
    void testCombinedDataFromMemoryAndFile() {
        FileDataSetContainer container = new FileDataSetContainer(TEST_DATA_DIR, -10, 10);
        container.add(5);
        container.writeToFile();
        container.add(2);

        assertEquals(1, container.getCacheNumberCount());
        assertEquals(2, container.getTotalEntryCount());
        assertEquals(List.of(2., 5.), container.getAllEntriesSorted());
    }

    @Test
    void testSplitContainerUnevenSize() {
        FileDataSetContainer container = new FileDataSetContainer(TEST_DATA_DIR, -20, 20);
        container.add(1);
        container.add(2);
        container.add(3);

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
        container.add(1);
        container.add(2);
        container.add(3);
        container.add(4);

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
        container.add(1);
        container.add(2);
        container.add(3);
        container.add(3);
        container.add(3);
        container.add(4);
        container.add(5);

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