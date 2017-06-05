package uc.balltree;

/**
 * Created by keltp on 2017-06-05.
 */
public class Heap {
    HeapElement heap[] = null;

    public Heap(int maxSize) {
        if ((maxSize % 2) == 0) {
            maxSize++;
        }

        heap = new HeapElement[maxSize + 1];
        heap[0] = new HeapElement(0, 0);
    }

    public int size() {
        return heap[0].index;
    }

    public HeapElement peek() {
        return heap[1];
    }

    public HeapElement get() throws Exception {
        if (heap[0].index == 0) {
            throw new Exception("No elements present in the heap");
        }
        HeapElement r = heap[1];
        heap[1] = heap[heap[0].index];
        heap[0].index--;
        downheap();
        return r;
    }

    public void put(int i, double d) throws Exception {
        if ((heap[0].index + 1) > (heap.length - 1)) {
            throw new Exception("the number of elements cannot exceed the "
                    + "initially set maximum limit");
        }
        heap[0].index++;
        heap[heap[0].index] = new HeapElement(i, d);
        upheap();
    }

    /**
     * Puts an element by substituting it in place of the top most element.
     *
     * @param i the index
     * @param d the distance
     * @throws Exception if distance is smaller than that of the head element
     */
    public void putBySubstitute(int i, double d) throws Exception {
        HeapElement head = get();
        put(i, d);
        // System.out.println("previous: "+head.distance+" current: "+heap[1].distance);
        if (head.distance == heap[1].distance) { // Utils.eq(head.distance,
            // heap[1].distance)) {
            putKthNearest(head.index, head.distance);
        } else if (head.distance > heap[1].distance) { // Utils.gr(head.distance,
            // heap[1].distance)) {
            kThNearest = null;
            kThNearestSize = 0;
            initSize = 10;
        } else if (head.distance < heap[1].distance) {
            throw new Exception("The substituted element is smaller than the "
                    + "head element. put() should have been called "
                    + "in place of putBySubstitute()");
        }
    }

    /** the kth nearest ones. */
    HeapElement kThNearest[] = null;

    /** The number of kth nearest elements. */
    int kThNearestSize = 0;

    /** the initial size of the heap. */
    int initSize = 10;

    public int noOfKthNearest() {
        return kThNearestSize;
    }

    /**
     * Stores kth nearest elements (if there are more than one).
     *
     * @param i the index
     * @param d the distance
     */
    public void putKthNearest(int i, double d) {
        if (kThNearest == null) {
            kThNearest = new HeapElement[initSize];
        }
        if (kThNearestSize >= kThNearest.length) {
            initSize += initSize;
            HeapElement temp[] = new HeapElement[initSize];
            System.arraycopy(kThNearest, 0, temp, 0, kThNearest.length);
            kThNearest = temp;
        }
        kThNearest[kThNearestSize++] = new HeapElement(i, d);
    }

    /**
     * returns the kth nearest element or null if none there.
     *
     * @return the kth nearest element
     */
    public HeapElement getKthNearest() {
        if (kThNearestSize == 0) {
            return null;
        }
        kThNearestSize--;
        return kThNearest[kThNearestSize];
    }

    /**
     * performs upheap operation for the heap to maintian its properties.
     */
    protected void upheap() {
        int i = heap[0].index;
        HeapElement temp;
        while (i > 1 && heap[i].distance > heap[i / 2].distance) {
            temp = heap[i];
            heap[i] = heap[i / 2];
            i = i / 2;
            heap[i] = temp; // this is i/2 done here to avoid another division.
        }
    }

    /**
     * performs downheap operation for the heap to maintian its properties.
     */
    protected void downheap() {
        int i = 1;
        HeapElement temp;
        while (((2 * i) <= heap[0].index && heap[i].distance < heap[2 * i].distance)
                || ((2 * i + 1) <= heap[0].index && heap[i].distance < heap[2 * i + 1].distance)) {
            if ((2 * i + 1) <= heap[0].index) {
                if (heap[2 * i].distance > heap[2 * i + 1].distance) {
                    temp = heap[i];
                    heap[i] = heap[2 * i];
                    i = 2 * i;
                    heap[i] = temp;
                } else {
                    temp = heap[i];
                    heap[i] = heap[2 * i + 1];
                    i = 2 * i + 1;
                    heap[i] = temp;
                }
            } else {
                temp = heap[i];
                heap[i] = heap[2 * i];
                i = 2 * i;
                heap[i] = temp;
            }
        }
    }

    /**
     * returns the total size.
     *
     * @return the total size
     */
    public int totalSize() {
        return size() + noOfKthNearest();
    }
}
