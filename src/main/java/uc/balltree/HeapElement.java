package uc.balltree;

/**
 * Created by keltp on 2017-06-05.
 */
public class HeapElement {
    protected int index;
    protected double distance;

    public HeapElement(int index, double distance) {
        this.index = index;
        this.distance = distance;
    }
}
