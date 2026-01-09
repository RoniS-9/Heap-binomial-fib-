import java.util.ArrayList;

/**
 * Heap
 *
 * An implementation of Fibonacci heap over positive integers 
 * with the possibility of not performing lazy melds and 
 * the possibility of not performing lazy decrease keys.
 *
 */
public class Heap {
    public final boolean lazyMelds;
    public final boolean lazyDecreaseKeys;
    public HeapItem min;
    public int size;
    public int totalLinks;
    public int totalCuts;
    public int totalHeapifyCosts;
    public int numTrees;
    public int numMarkedNodes;

    //*************************** HELPER METHODS AND FIELDS ******************************//
    public void updateMin() {
        if (this.min == null) {
            return; // Heap is empty
        }

        HeapNode current = this.min.node;
        HeapNode start = current;
        HeapItem newMin = this.min;

        do {
            if (current.item.key < newMin.key) {
                newMin = current.item;
            }
            current = current.next;
        } while (current != start);

        this.min = newMin;
        consolidate();
    }

    public void consolidate() {
        int maxRank = (int) Math.floor(Math.log(this.size) / Math.log(2)) + 1;
        HeapNode[] rankTable = new HeapNode[maxRank];

        // Collect trees of same rank
        // ArrayList<HeapNode> roots = new ArrayList<>();
        HeapNode current = this.min.node;
        HeapNode start = current;
        /*do {
            roots.add(current);
            current = current.next;
        }
        while (current != start);

        for (HeapNode node : roots) {
            int rank = node.rank;
            HeapNode linked = node;

            while (rankTable[rank] != null) {
                HeapNode occupant = rankTable[rank];
                linked = link(occupant, linked);
                rankTable[rank] = null;
                rank = linked.rank;
            }
            rankTable[rank] = linked;
        }*/

        do {
            int rank = current.rank;
            HeapNode next = current.next;
            HeapNode linked = current;
            while (rankTable[rank] != null) {
                HeapNode occupant = rankTable[rank];
                linked = link(occupant, current);
                rankTable[rank] = null;
                rank = linked.rank;
            }
            rankTable[rank] = linked;
            current = next;
        }
        while (current != start);

        // Rebuild root list and find new min
        this.min = null;
        this.numTrees = 0;
        for (HeapNode node : rankTable) {
            if (node != null) {
                // Add node to root list
                if (this.min == null) {
                    this.min = node.item;
                    node.next = node.prev = node;
                }
                else {
                    HeapNode minNode = this.min.node;
                    HeapNode afterMin = minNode.next;
                    minNode.next = node;
                    node.prev = minNode;
                    node.next = afterMin;
                    afterMin.prev = node;

                    // Update min if necessary
                    if (node.item.key < this.min.key) {
                        this.min = node.item;
                    }
                }
                this.numTrees++;
            }
        }
    }

    public void lazyDecreaseKey(HeapItem x) {
        HeapNode parent = x.node.parent;
        if (parent != null && x.key < parent.item.key) {
            cut(x.node);
            if (parent.marked) {
                cut(parent);
            }
            else {
                parent.marked = true;
                numMarkedNodes++;
            }
            if (x.node.marked) {
                numMarkedNodes = Math.max(0, numMarkedNodes - 1);
            }
            x.node.marked = false;
        }
    }

    public void nonLazyDecreaseKey(HeapItem x) {
        HeapNode parent = x.node.parent;
        while (parent != null && x.key < parent.item.key) {
            heapifyUp(x.node);
            parent = x.node.parent;
        }
    }

    public void heapifyUp(HeapNode startNode) {
        HeapNode parent = startNode.parent;
        if (parent == null) {
            return; // startNode is already a root
        }
        HeapItem temp = startNode.item;
        startNode.item = parent.item;
        parent.item = temp;
        startNode.item.node = startNode;
        parent.item.node = parent;
    }

    public HeapNode link(HeapNode x, HeapNode y) {
        if (x == null || y == null) {
            return null;
        }

        if (x.item.key > y.item.key) {
            HeapNode temp = x;
            x = y;
            y = temp;
        }

        // Remove y from root list
        y.prev.next = y.next;
        y.next.prev = y.prev;
        y.next = y.prev = y;

        // Attach y to x's child list
        HeapNode child = x.child;
        if (child == null) {
            x.child = y;
        }
        else {
            HeapNode afterChild = child.next;
            child.next = y;
            y.prev = child;
            y.next = afterChild;
            afterChild.prev = y;
        }

        // Make y a child of x
        y.parent = x;
        x.rank++;

        // Update counts
        this.totalLinks++;
        this.numTrees--;

        return x;
    }

    public void cut(HeapNode x) {
        HeapNode parent = x.parent;
        if (parent == null) {
            return; // x is already a root
        }
        // x is the first child
        if (x == parent.child) {
            // x is not the only child
            if (x.next != x) {
                parent.child = x.next;
                x.prev.next = x.next;
                x.next.prev = x.prev;
            }
            // x is the only child
            else {
                parent.child = null;
            }
        }
        // x is not the first child
        else {
            x.prev.next = x.next;
            x.next.prev = x.prev;
        }
        parent.rank--;
        x.parent = null;
        x.prev = x.next = x;

        // Add x to root list
        HeapNode minNode = this.min.node;
        HeapNode afterMin = minNode.next;
        minNode.next = x;
        x.prev = minNode;
        x.next = afterMin;
        afterMin.prev = x;

        // Update min if necessary
        if (x.item.key < this.min.key) {
            this.min = x.item;
        }

        // Update tree and cut counts
        this.numTrees++;
        this.totalCuts++;
    }

    //*************************** MAIN METHODS ******************************//

    /**
     *
     * Constructor to initialize an empty heap.
     *
     */
    public Heap(boolean lazyMelds, boolean lazyDecreaseKeys) {
        this.lazyMelds = lazyMelds;
        this.lazyDecreaseKeys = lazyDecreaseKeys;
        // student code can be added here
        this.min = null;
        this.size = 0;
        this.totalLinks = 0;
        this.totalCuts = 0;
        this.totalHeapifyCosts = 0;
        this.numTrees = 0;
        this.numMarkedNodes = 0;
    }

    /**
     *
     * pre: key > 0
     * <p>
     * Insert (key,info) into the heap and return the newly generated HeapNode.
     *
     */
    public HeapItem insert(int key, String info) {
        HeapNode newNode = new HeapNode();
        HeapItem newItem = new HeapItem(key, info);
        newNode.item = newItem;
        newItem.node = newNode;

        // initialize new node fields
        newNode.child = null;
        newNode.parent = null;
        newNode.rank = 0;
        newNode.next = newNode.prev = newNode;

        // If the current heap is empty, initialize it with the new node
        if (this.min == null) {
            this.min = newItem;
            this.min.node.next = this.min.node.prev = this.min.node;
            this.size = 1;
            return newItem;
        }

        // Insert the new node into the root list
        HeapNode minNode = this.min.node;
        HeapNode afterNew = minNode.next;
        minNode.next = newNode;
        newNode.prev = minNode;
        newNode.next = afterNew;
        afterNew.prev = newNode;

        // update min if necessary
        if (key < this.min.key) {
            this.min = newItem;
        }


        // melding and size update
        if (!this.lazyMelds) {
            Heap singletonHeap = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
            singletonHeap.min = newItem;
            singletonHeap.size = 1;
            meld(singletonHeap);
        }
        else {
            this.size++;
            this.numTrees++;
        }

        return newItem;
    }

    /**
     *
     * Return the minimal HeapNode, null if empty.
     *
     */
    public HeapItem findMin() {
        return this.min;
    }

    /**
     *
     * Delete the minimal item.
     *
     */
    public void deleteMin() {
        if (this.min == null || this.size == 0) return; // Heap is empty

        HeapNode minNode = this.min.node;
        if (minNode == null) return; // Heap is empty

        HeapNode minPrev = minNode.prev;
        HeapNode minNext = minNode.next;

        // Add children to root list
        HeapNode child = minNode.child;
        int childCount = 0;
        if (child != null) {
            HeapNode curr = child;
            do {
                childCount++;
                child.parent = null; // Remove parent reference
                if (curr.marked) {
                    numMarkedNodes--;
                    curr.marked = false;
                }
                curr = curr.next;
            }
            while (curr != child);
        }

        // Remove min from root list
        if (minNode.next == minNode) { // min is the only root
            if (child == null) {
                this.size--;
                this.min = null;
                this.numTrees = 0; // CHANGED: no trees remain
                return;
            }
            else {
                this.size--;
                this.min = child.item;
                this.numTrees = childCount; //new number of trees equals number of children
                minNode.child = null;
                minNode.next = minNode.prev = minNode.parent = null;
                updateMin();
                return;
            }
        }
        else {
            minPrev.next = minNext;
            minNext.prev = minPrev;

            // Splice children list into root list
            if (child != null) {
                HeapNode last = child.prev;

                minPrev.next = child;
                child.prev = minPrev;
                last.next = minNext;
                minNext.prev = last;

                // update tree count: remove min, add each child as a tree
                this.numTrees = this.numTrees - 1 + childCount;
            }
            else {
                this.numTrees--; // only min was removed
            }

            this.size--;
            this.min = minNext.item; // Temporarily set min to next node
            updateMin();
            return;
        }
    }

    /**
     *
     * pre: 0<=diff<=x.key
     * <p>
     * Decrease the key of x by diff and fix the heap.
     *
     */
    public void decreaseKey(HeapItem x, int diff) {
        x.key -= diff;
        if (x.key < this.min.key) { this.min = x; }
        if (this.lazyDecreaseKeys) {
            lazyDecreaseKey(x);
        } else {
            nonLazyDecreaseKey(x);
        }
    }

    /**
     *
     * Delete the x from the heap.
     *
     */
    public void delete(HeapItem x) {
        HeapNode node = x.node;
        decreaseKey(x, x.key + 1); // decrease key to -1
        deleteMin();
    }

    /**
     *
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2) {
        HeapNode node2min = heap2.min.node;
        if (node2min == null) {
            return; // heap2 is empty, nothing to meld
        }
        if (this.min == null) {
            this.min = heap2.min;
            this.size = heap2.size;
            this.numTrees = heap2.numTrees;
            this.numMarkedNodes = heap2.numMarkedNodes;
            this.totalCuts = heap2.totalCuts;
            this.totalLinks = heap2.totalLinks;
            this.totalHeapifyCosts = heap2.totalHeapifyCosts;
        }
        this.size += heap2.size;
        this.numTrees += heap2.numTrees;
        this.numMarkedNodes += heap2.numMarkedNodes;
        this.totalCuts += heap2.totalCuts;
        this.totalLinks += heap2.totalLinks;
        this.totalHeapifyCosts += heap2.totalHeapifyCosts;

        // Update min if necessary
        if (heap2.min.key < this.min.key) {
            this.min = heap2.min;
        }

        if (!this.lazyMelds) {
            consolidate();
        }
    }

    /**
     *
     * Return the number of elements in the heap
     *
     */
    public int size() {
        return this.size; // should be replaced by student code
    }

    /**
     *
     * Return the number of trees in the heap.
     *
     */
    public int numTrees() {
        return numTrees; // should be replaced by student code
    }

    /**
     *
     * Return the number of marked nodes in the heap.
     *
     */
    public int numMarkedNodes() {
        return numMarkedNodes; // should be replaced by student code
    }

    /**
     *
     * Return the total number of links.
     *
     */
    public int totalLinks() {
        return totalLinks; // should be replaced by student code
    }

    /**
     *
     * Return the total number of cuts.
     *
     */
    public int totalCuts() {
        return totalCuts; // should be replaced by student code
    }

    /**
     *
     * Return the total heapify costs.
     *
     */
    public int totalHeapifyCosts() {
        return totalHeapifyCosts; // should be replaced by student code
    }


    /**
     * Class implementing a node in a Heap.
     *
     */
    public static class HeapNode {
        public HeapItem item;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int rank;
        public boolean marked;
    }

    /**
     * Class implementing an item in a Heap.
     *
     */
    public static class HeapItem {
        public HeapNode node;
        public int key;
        public String info;

        public HeapItem(int key, String info) {
            this.node = null;
            this.key = key;
            this.info = info;
        }
    }
}
