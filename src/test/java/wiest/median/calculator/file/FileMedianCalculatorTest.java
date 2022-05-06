package wiest.median.calculator.file;

import org.junit.jupiter.api.Test;
import wiest.median.MedianCalculator;
import wiest.median.MemoryDataSource;
import wiest.median.filereader.NumberFileReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileMedianCalculatorTest extends LocalFileTest {


    @Test
    void testEmpty() {
        var file = new MemoryDataSource(List.of());
        MedianCalculator calc = new FileMedianCalculator(TEST_DATA_DIR,1);

        calc.loadSource(file);

        assertEquals(0, calc.calculateMedian());
    }

    @Test
    void testSingleNumber() {
        var file = new MemoryDataSource(List.of(1.));
        MedianCalculator calc = new FileMedianCalculator(TEST_DATA_DIR,1);

        calc.loadSource(file);

        assertEquals(1., calc.calculateMedian());
    }

    @Test
    void testGetMedianEven() {
        var file = new MemoryDataSource(List.of(1.,2.,3.,4.,5., 6.));
        MedianCalculator calc = new FileMedianCalculator(TEST_DATA_DIR,1);

        calc.loadSource(file);

        assertEquals(3.5, calc.calculateMedian());
    }

    @Test
    void testGetMedianUneven() {
        var file = new MemoryDataSource(List.of(1.,2.,3.,4.,5.));
        MedianCalculator calc = new FileMedianCalculator(TEST_DATA_DIR,1);

        calc.loadSource(file);

        assertEquals(3, calc.calculateMedian());
    }

    @Test
    void testMedianWithFileWriting() {
        var file = new MemoryDataSource(IntStream.range(0, 1001).asDoubleStream().boxed().toList());
        MedianCalculator calc = new FileMedianCalculator(TEST_DATA_DIR,1);

        calc.loadSource(file);

        assertEquals(500, calc.calculateMedian());
    }

    @Test
    void testMedianWithFileWritingRandomNumbers() {
        var numbers = IntStream.range(0, 1001).asDoubleStream().boxed().collect(Collectors.toList());
        Collections.shuffle(numbers);
        var file = new MemoryDataSource(numbers);
        MedianCalculator calc = new FileMedianCalculator(TEST_DATA_DIR,1);

        calc.loadSource(file);

        assertEquals(500, calc.calculateMedian());
    }

    @Test
    void testMedianWithLargeInput() {
        // About 78 times the data that can be cached in memory before file access
        var numbers = IntStream.range(0, 500_000 + 1)
                .asDoubleStream().boxed().collect(Collectors.toList());
        Collections.shuffle(numbers);
        var file = new MemoryDataSource(numbers);
        MedianCalculator calc = new FileMedianCalculator(TEST_DATA_DIR,100);

        calc.loadSource(file);

        assertEquals(250_000, calc.calculateMedian());
    }

    @Test
    void testMedianWithNegativeNumbers() {
        var numbers = IntStream.range(-10, 11).asDoubleStream().boxed().collect(Collectors.toList());
        Collections.shuffle(numbers);
        var file = new MemoryDataSource(numbers);
        MedianCalculator calc = new FileMedianCalculator(TEST_DATA_DIR,1);

        calc.loadSource(file);

        assertEquals(0, calc.calculateMedian());
    }

    @Test
    void testRandomFile() {
        var file = new NumberFileReader("src/test/resources/random_numbers.txt");
        MedianCalculator calc = new FileMedianCalculator(TEST_DATA_DIR,1);

        calc.loadSource(file);

        assertEquals(6.1234, calc.calculateMedian());
    }

    @Test
    void testMedianSimilarNumbers() {
        List<Double> dataList = new ArrayList<>();
        dataList.addAll(Collections.nCopies(1000, 1.));
        dataList.addAll(Collections.nCopies(2000, 2.));
        dataList.addAll(Collections.nCopies(1000, 3.));
        dataList.addAll(IntStream.range(-100, 110).asDoubleStream().boxed().toList());

        var file = new MemoryDataSource(dataList);
        MedianCalculator calc = new FileMedianCalculator(TEST_DATA_DIR,1);

        calc.loadSource(file);

        assertEquals(2, calc.calculateMedian());
    }

}