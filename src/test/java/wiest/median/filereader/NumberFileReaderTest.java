package wiest.median.filereader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberFileReaderTest {

    @Test
    void testOpenValidFile() {
        assertDoesNotThrow(
                () -> new NumberFileReader("src/test/resources/random_numbers.txt")
        );
    }

    @Test
    void testMissingFile() {
        assertThrows(FileReaderException.class,
                () -> new NumberFileReader("does_not_exist.txt")
        );
    }

    @Test
    void testReadValidFile() {
        NumberFileReader fileReader = new NumberFileReader("src/test/resources/random_numbers.txt");
        assertDoesNotThrow(
                () -> {
                    while (fileReader.hasNext()) {
                        fileReader.getNext();
                    }
                }
        );
    }

    @Test
    void testNextElementDoesNotExist() {
        NumberFileReader fileReader = new NumberFileReader("src/test/resources/random_numbers.txt");
        while (fileReader.hasNext()) {
            fileReader.getNext();
        }

        assertThrows(FileReaderException.class, fileReader::getNext);
    }

    @Test
    void testGetNext() {
        NumberFileReader fileReader = new NumberFileReader("src/test/resources/defined_numbers.txt");

        assertEquals(1, fileReader.getNext());
        assertEquals(2, fileReader.getNext());
        assertEquals(3, fileReader.getNext());
    }

    @Test
    void testHasNextElement() {
        NumberFileReader fileReader = new NumberFileReader("src/test/resources/defined_numbers.txt");

        assertTrue(fileReader.hasNext());
    }

    @Test
    void testNoNextElement() {
        NumberFileReader fileReader = new NumberFileReader("src/test/resources/defined_numbers.txt");

        fileReader.getNext();
        fileReader.getNext();
        fileReader.getNext();

        assertFalse(fileReader.hasNext());
    }

    @Test
    void testReadInvalidFile() {
        NumberFileReader fileReader = new NumberFileReader("src/test/resources/invalid_input.txt");
        assertThrows(FileReaderException.class,
                () -> {
                    while (fileReader.hasNext()) {
                        fileReader.getNext();
                    }
                }
        );
    }

}