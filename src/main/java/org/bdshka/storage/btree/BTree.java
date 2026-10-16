package org.bdshka.storage.btree;

import java.util.Comparator;
import java.util.Objects;

public final class BTree<K, V> {
    private final int t;
    private final Comparator<? super K> comparator;
    private BTreeNode<K, V> root;
    private int size;

    public BTree(int t, Comparator<? super K> comparator) {
        if (t < 2 || t > Integer.MAX_VALUE / 2) {
            throw new IllegalArgumentException("Invalid minimum degree: " + t);
        }

        this.t = t;
        this.comparator = Objects.requireNonNull(comparator);
        this.root = new BTreeNode<>(t);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public V get(K key) {
        BTreeNode.Entry<K, V> entry = findEntry(key);
        return entry == null ? null : entry.value;
    }

    public boolean containsKey(K key) {
        return findEntry(key) != null;
    }

    public V put(K key, V value) {
        Objects.requireNonNull(value);
        BTreeNode.Entry<K, V> existing = findEntry(key);

        if (existing != null) {
            V previous = existing.value;
            existing.value = value;
            return previous;
        }

        if (root.entries.size() == 2 * t - 1) {
            BTreeNode<K, V> newRoot = new BTreeNode<>(t);
            newRoot.children.add(root);
            splitChild(newRoot, 0);
            root = newRoot;
        }

        insertNonFull(root, new BTreeNode.Entry<>(key, value));
        size++;
        return null;
    }

    public V remove(K key) {
        BTreeNode.Entry<K, V> existing = findEntry(key);

        if (existing == null) {
            return null;
        }

        V previous = existing.value;
        delete(root, key);

        if (root.entries.isEmpty() && !root.isLeaf()) {
            root = root.children.get(0);
        }

        size--;
        return previous;
    }

    private int lowerBound(BTreeNode<K, V> node, K key) {
        int left = 0;
        int right = node.entries.size();

        while (left < right) {
            int middle = (left + right) >>> 1;

            if (comparator.compare(node.entries.get(middle).key, key) < 0) {
                left = middle + 1;
            } else {
                right = middle;
            }
        }

        return left;
    }

    private BTreeNode.Entry<K, V> findEntry(K key) {
        Objects.requireNonNull(key);
        BTreeNode<K, V> node = root;

        while (true) {
            int index = lowerBound(node, key);

            if (index < node.entries.size()) {
                BTreeNode.Entry<K, V> entry = node.entries.get(index);

                if (comparator.compare(entry.key, key) == 0) {
                    return entry;
                }
            }

            if (node.isLeaf()) {
                return null;
            }

            node = node.children.get(index);
        }
    }

    private void insertNonFull(BTreeNode<K, V> node, BTreeNode.Entry<K, V> entry) {
        int index = lowerBound(node, entry.key);

        if (node.isLeaf()) {
            node.entries.add(index, entry);
            return;
        }

        if (node.children.get(index).entries.size() == 2 * t - 1) {
            splitChild(node, index);

            if (comparator.compare(entry.key, node.entries.get(index).key) > 0) {
                index++;
            }
        }

        insertNonFull(node.children.get(index), entry);
    }

    private void splitChild(BTreeNode<K, V> parent, int index) {
        BTreeNode<K, V> left = parent.children.get(index);
        BTreeNode<K, V> right = new BTreeNode<>(t);
        BTreeNode.Entry<K, V> median = left.entries.get(t - 1);

        right.entries.addAll(left.entries.subList(t, left.entries.size()));
        left.entries.subList(t - 1, left.entries.size()).clear();

        if (!left.isLeaf()) {
            right.children.addAll(left.children.subList(t, left.children.size()));
            left.children.subList(t, left.children.size()).clear();
        }

        parent.entries.add(index, median);
        parent.children.add(index + 1, right);
    }

    private void delete(BTreeNode<K, V> node, K key) {
        int index = lowerBound(node, key);

        if (index < node.entries.size() && comparator.compare(node.entries.get(index).key, key) == 0) {
            if (node.isLeaf()) {
                node.entries.remove(index);
            } else {
                deleteFromInternal(node, index);
            }
            return;
        }

        if (node.isLeaf()) {
            return;
        }

        if (node.children.get(index).entries.size() == t - 1) {
            prepareChild(node, index);

            if (index > node.entries.size()) {
                index--;
            }
        }

        delete(node.children.get(index), key);
    }

    private void deleteFromInternal(BTreeNode<K, V> node, int index) {
        BTreeNode.Entry<K, V> entry = node.entries.get(index);
        BTreeNode<K, V> left = node.children.get(index);
        BTreeNode<K, V> right = node.children.get(index + 1);

        if (left.entries.size() >= t) {
            BTreeNode.Entry<K, V> predecessor = maximum(left);
            node.entries.set(index, predecessor);
            delete(left, predecessor.key);
        } else if (right.entries.size() >= t) {
            BTreeNode.Entry<K, V> successor = minimum(right);
            node.entries.set(index, successor);
            delete(right, successor.key);
        } else {
            mergeChildren(node, index);
            delete(left, entry.key);
        }
    }

    private BTreeNode.Entry<K, V> minimum(BTreeNode<K, V> node) {
        while (!node.isLeaf()) {
            node = node.children.get(0);
        }

        return node.entries.get(0);
    }

    private BTreeNode.Entry<K, V> maximum(BTreeNode<K, V> node) {
        while (!node.isLeaf()) {
            node = node.children.get(node.children.size() - 1);
        }

        return node.entries.get(node.entries.size() - 1);
    }

    private void prepareChild(BTreeNode<K, V> parent, int index) {
        if (index > 0 && parent.children.get(index - 1).entries.size() >= t) {
            borrowFromLeft(parent, index);
        } else if (index < parent.entries.size() && parent.children.get(index + 1).entries.size() >= t) {
            borrowFromRight(parent, index);
        } else if (index < parent.entries.size()) {
            mergeChildren(parent, index);
        } else {
            mergeChildren(parent, index - 1);
        }
    }

    private void borrowFromLeft(BTreeNode<K, V> parent, int index) {
        BTreeNode<K, V> child = parent.children.get(index);
        BTreeNode<K, V> sibling = parent.children.get(index - 1);

        child.entries.add(0, parent.entries.get(index - 1));
        parent.entries.set(index - 1, sibling.entries.remove(sibling.entries.size() - 1));

        if (!sibling.isLeaf()) {
            child.children.add(0, sibling.children.remove(sibling.children.size() - 1));
        }
    }

    private void borrowFromRight(BTreeNode<K, V> parent, int index) {
        BTreeNode<K, V> child = parent.children.get(index);
        BTreeNode<K, V> sibling = parent.children.get(index + 1);

        child.entries.add(parent.entries.get(index));
        parent.entries.set(index, sibling.entries.remove(0));

        if (!sibling.isLeaf()) {
            child.children.add(sibling.children.remove(0));
        }
    }

    private void mergeChildren(BTreeNode<K, V> parent, int index) {
        BTreeNode<K, V> left = parent.children.get(index);
        BTreeNode<K, V> right = parent.children.remove(index + 1);

        left.entries.add(parent.entries.remove(index));
        left.entries.addAll(right.entries);
        left.children.addAll(right.children);
    }
}
