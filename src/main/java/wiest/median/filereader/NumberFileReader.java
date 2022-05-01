package wiest.median.filereader;

import wiest.median.DataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Scanner;

public class NumberFileReader implements DataSource {
    private static final NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
    private final Scanner file;

    public NumberFileReader(String filename) {
        try {
            file = new Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            throw new FileReaderException("File does not exist!", e);
        }
    }

    public boolean hasNext() {
        return file.hasNext();
    }

    public double getNext() {
        if (hasNext()) {
            var numStr = file.nextLine().replace(".", ",");
            try {
                return nf.parse(numStr).doubleValue();
            } catch (ParseException e) {
                throw new FileReaderException(String.format("Error reading value %s", numStr), e);
            }
        } else {
            throw new FileReaderException("End of file reached");
        }
    }

    public void close() {
        file.close();
    }
}
