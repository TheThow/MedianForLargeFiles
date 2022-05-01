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
                    \tDue to lack of optimization the actual memory allocated by the jvm
                    \tcould be significantly more in some cases
                    """);
            System.exit(1);
        }

        String filename = args[0];
        int memory = args.length > 1 ? Integer.parseInt(args[1]) : 8192;

        var dataSource = new NumberFileReader(filename);
        MedianCalculator calc = new FileMedianCalculator("./storagedata", memory * 1024);
        calc.loadSource(dataSource);

        System.out.printf("Median of file %s is %f\n", filename, calc.calculateMedian());
        calc.destroy();
    }
}
