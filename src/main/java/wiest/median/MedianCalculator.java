package wiest.median;

public interface MedianCalculator {

    void loadSource(DataSource source);

    double calculateMedian();

    void destroy();

}
