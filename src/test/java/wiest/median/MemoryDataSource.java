package wiest.median;

import java.util.List;

public class MemoryDataSource implements DataSource {

    private final List<Double> nums;
    private int index = 0;

    public MemoryDataSource(List<Double> nums) {
        this.nums = nums;
    }

    @Override
    public boolean hasNext() {
        return index < nums.size();
    }

    @Override
    public double getNext() {
        return nums.get(index++);
    }

    @Override
    public void close() {

    }
}
