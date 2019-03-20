package Database;

public class BTree<Key extends Comparable<Key>, Value>  {
    // 每个B-树的node最多有M-1个节点
    // 但是不能少于2
    private static final int M = 4;

    private Node root;       // 根节点
    private int height;      // 高度
    private int n;           // 节点数

    // helper B-tree node data type
    private static final class Node {
        private int m;                             // number of children
        private Entry[] children = new Entry[M];   // the array of children

        // create a node with k children
        private Node(int k) {
            m = k;
        }
    }

    // 外部节点 只有key和next
    // 内部节点 只有key和value
    private static class Entry {
        private Comparable key;
        private final Object val;
        private Node next;     // helper field to iterate over array entries
        public Entry(Comparable key, Object val, Node next) {
            this.key  = key;
            this.val  = val;
            this.next = next;
        }
    }

    /**
     * Initializes an empty B-tree.
     */
    public BTree() {
        root = new Node(0);
    }

    /**
     * Returns true if this symbol table is empty.
     * @return {@code true} if this symbol table is empty; {@code false} otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the number of key-value pairs in this symbol table.
     * @return the number of key-value pairs in this symbol table
     */
    public int size() {
        return n;
    }

    /**
     * Returns the height of this B-tree (for debugging).
     *
     * @return the height of this B-tree
     */
    public int height() {
        return height;
    }


    /**
     * Returns the value associated with the given key.
     *
     * @param  key the key
     * @return the value associated with the given key if the key is in the symbol table
     *         and {@code null} if the key is not in the symbol table
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("argument to get() is null");
        // 传入参数 root 和 key 还有树的高度
        return search(root, key, height);
    }

    private Value search(Node x, Key key, int ht) {
        // 获得x 的所有儿子节点
        Entry[] children = x.children;

        // 外部节点
        if (ht == 0) {
            for (int j = 0; j < x.m; j++) {
                // 如果key 相等就直接返回数据
                if (eq(key, children[j].key))
                    return (Value) children[j].val;
            }
        }

        // 内部节点
        else {
            for (int j = 0; j < x.m; j++) {
                if (j+1 == x.m || less(key, children[j+1].key))
                    // 递归向下查找
                    return search(children[j].next, key, ht-1);
            }
        }
        return null;
    }


    /**
     * Inserts the key-value pair into the symbol table, overwriting the old value
     * with the new value if the key is already in the symbol table.
     * If the value is {@code null}, this effectively deletes the key from the symbol table.
     *
     * @param  key the key
     * @param  val the value
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("argument key to put() is null");
        Node u = insert(root, key, val, height);
        n++;
        if (u == null) return;

        // need to split root
        Node t = new Node(2);
        t.children[0] = new Entry(root.children[0].key, null, root);
        t.children[1] = new Entry(u.children[0].key, null, u);
        root = t;
        height++;
    }

    private Node insert(Node h, Key key, Value val, int ht) {
        int j;
        Entry t = new Entry(key, val, null);

        // 外部节点
        if (ht == 0) {
            for (j = 0; j < h.m; j++) {
                if (less(key, h.children[j].key)) break;
            }
        }

        // 内部节点
        else {
            for (j = 0; j < h.m; j++) {
                // 在两个之间就向下查找
                if ((j+1 == h.m) || less(key, h.children[j+1].key)) {
                    Node u = insert(h.children[j++].next, key, val, ht - 1);

                    if (u == null) return null;
                    // 如果返回的是切分的节点
                    t.key = u.children[0].key;
                    t.next = u;
                    break;
                }
            }
        }

        // 腾出t的空间
        for (int i = h.m; i > j; i--)
            h.children[i] = h.children[i-1];
        // 将t插入
        h.children[j] = t;
        // 更新数目
        h.m++;

        if (h.m < M) return null;
        else         return split(h);
    }

    // 切分节点
    private Node split(Node h) {
        // 新建一个节点
        Node t = new Node(M/2);
        // 将当前节点切半
        h.m = M/2;
        for (int j = 0; j < M/2; j++)
            // h的后半节点转移到t的上
            t.children[j] = h.children[M/2+j];
        return t;
    }

    /**
     * Returns a string representation of this B-tree (for debugging).
     *
     * @return a string representation of this B-tree.
     */
    public String toString() {
        return toString(root, height, "") + "\n";
    }

    private String toString(Node h, int ht, String indent) {
        StringBuilder s = new StringBuilder();
        Entry[] children = h.children;

        if (ht == 0) {
            for (int j = 0; j < h.m; j++) {
                s.append(indent + children[j].key + " " + children[j].val + "\n");
            }
        }
        else {
            for (int j = 0; j < h.m; j++) {
                if (j > 0) s.append(indent + "(" + children[j].key + ")\n");
                s.append(toString(children[j].next, ht-1, indent + "     "));
            }
        }
        return s.toString();
    }


    // comparison functions - make Comparable instead of Key to avoid casts
    private boolean less(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean eq(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }


    /**
     * Unit tests the {@code BTree} data type.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        BTree<String, String> st = new BTree<String, String>();

        st.put("www.cs.princeton.edu", "128.112.136.12");
        st.put("www.cs.princeton.edu", "128.112.136.11");
        st.put("www.princeton.edu",    "128.112.128.15");
        st.put("www.yale.edu",         "130.132.143.21");
        st.put("www.simpsons.com",     "209.052.165.60");
        st.put("www.apple.com",        "17.112.152.32");
        st.put("www.amazon.com",       "207.171.182.16");
        st.put("www.ebay.com",         "66.135.192.87");
        st.put("www.cnn.com",          "64.236.16.20");
        st.put("www.google.com",       "216.239.41.99");
        st.put("www.nytimes.com",      "199.239.136.200");
        st.put("www.microsoft.com",    "207.126.99.140");
        st.put("www.dell.com",         "143.166.224.230");
        st.put("www.slashdot.org",     "66.35.250.151");
        st.put("www.espn.com",         "199.181.135.201");
        st.put("www.weather.com",      "63.111.66.11");
        st.put("www.yahoo.com",        "216.109.118.65");


//        .println("cs.princeton.edu:  " + st.get("www.cs.princeton.edu"));
//        StdOut.println("hardvardsucks.com: " + st.get("www.harvardsucks.com"));
//        StdOut.println("simpsons.com:      " + st.get("www.simpsons.com"));
//        StdOut.println("apple.com:         " + st.get("www.apple.com"));
//        StdOut.println("ebay.com:          " + st.get("www.ebay.com"));
//        StdOut.println("dell.com:          " + st.get("www.dell.com"));
//        StdOut.println();
//
//        StdOut.println("size:    " + st.size());
//        StdOut.println("height:  " + st.height());
//        StdOut.println(st);
//        StdOut.println();
    }

}