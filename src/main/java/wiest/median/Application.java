package wiest.median;

import wiest.median.calculator.file.FileMedianCalculator;
import wiest.median.filereader.NumberFileReader;

public class Application {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println(
                    """
                    Run with arguments [filename] [memory]
                    [memory] = max amount of input data stored in memory in megabytes - default 8192
                    NOTE: \tThere will also be some overhead - a few bytes * (file size / memory size)
                    \tThis should be at most half of your available memory
                    """);
            System.exit(1);
        }

        String filename = args[0];
        int memory = args.length > 1 ? Integer.parseInt(args[1]) : 8192;

        MedianCalculator calc = new FileMedianCalculator("./storageData", memory * 1024);

        try {
            var dataSource = new NumberFileReader(filename);
            calc.loadSource(dataSource);
            System.out.printf("Median of file %s is %f\n", filename, calc.calculateMedian());
        } finally {
            calc.destroy();
        }
    }
}
