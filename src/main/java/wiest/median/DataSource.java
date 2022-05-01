package wiest.median;

public interface DataSource {

    boolean hasNext();

    double getNext();

    void close();

}
