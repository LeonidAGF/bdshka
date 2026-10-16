package bdshka;

import java.util.ArrayList;
import java.util.List;

final class BTreeNode<K, V> {
    static final class Entry<K, V> {
        final K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    final List<Entry<K, V>> entries;
    final List<BTreeNode<K, V>> children;

    BTreeNode(int t) {
        entries = new ArrayList<>(2 * t - 1);
        children = new ArrayList<>(2 * t);
    }

    boolean isLeaf() {
        return children.isEmpty();
    }
}
