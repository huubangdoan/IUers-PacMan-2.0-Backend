package com.Iuer.Pacman.index;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe self-balancing AVL Tree index for fast in-memory lookups.
 */
public class TreeIndex<K extends Comparable<K>, V> {

    private class Node {
        K key;
        V value;
        Node left, right;
        int height = 1;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node root;
    private int count = 0;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private int height(Node n) {
        return n == null ? 0 : n.height;
    }

    private int getBalance(Node n) {
        return n == null ? 0 : height(n.left) - height(n.right);
    }

    private void updateHeight(Node n) {
        if (n != null) {
            n.height = 1 + Math.max(height(n.left), height(n.right));
        }
    }

    private Node rotateRight(Node y) {
        Node x = y.left;
        Node t2 = x.right;
        x.right = y;
        y.left = t2;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node t2 = y.left;
        y.left = x;
        x.right = t2;
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    private Node balance(Node n) {
        updateHeight(n);
        int balance = getBalance(n);

        // Left Left Case
        if (balance > 1 && getBalance(n.left) >= 0) {
            return rotateRight(n);
        }

        // Left Right Case
        if (balance > 1 && getBalance(n.left) < 0) {
            n.left = rotateLeft(n.left);
            return rotateRight(n);
        }

        // Right Right Case
        if (balance < -1 && getBalance(n.right) <= 0) {
            return rotateLeft(n);
        }

        // Right Left Case
        if (balance < -1 && getBalance(n.right) > 0) {
            n.right = rotateRight(n.right);
            return rotateLeft(n);
        }

        return n;
    }

    public boolean insert(K key, V value) {
        rwLock.writeLock().lock();
        try {
            boolean[] inserted = new boolean[1];
            root = insert(root, key, value, inserted);
            if (inserted[0]) {
                count++;
            }
            return inserted[0];
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private Node insert(Node n, K key, V value, boolean[] inserted) {
        if (n == null) {
            inserted[0] = true;
            return new Node(key, value);
        }
        int c = key.compareTo(n.key);
        if (c < 0) {
            n.left = insert(n.left, key, value, inserted);
        } else if (c > 0) {
            n.right = insert(n.right, key, value, inserted);
        } else {
            n.value = value;
            return n;
        }
        return balance(n);
    }

    public V find(K key) {
        rwLock.readLock().lock();
        try {
            Node cur = root;
            while (cur != null) {
                int c = key.compareTo(cur.key);
                if (c < 0) {
                    cur = cur.left;
                } else if (c > 0) {
                    cur = cur.right;
                } else {
                    return cur.value;
                }
            }
            return null;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public boolean erase(K key) {
        rwLock.writeLock().lock();
        try {
            boolean[] erased = new boolean[1];
            root = erase(root, key, erased);
            if (erased[0]) {
                count--;
            }
            return erased[0];
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private Node erase(Node n, K key, boolean[] erased) {
        if (n == null) return null;

        int c = key.compareTo(n.key);
        if (c < 0) {
            n.left = erase(n.left, key, erased);
        } else if (c > 0) {
            n.right = erase(n.right, key, erased);
        } else {
            erased[0] = true;
            if (n.left == null || n.right == null) {
                n = (n.left != null) ? n.left : n.right;
            } else {
                Node min = minNode(n.right);
                n.key = min.key;
                n.value = min.value;
                boolean[] dummy = new boolean[1];
                n.right = erase(n.right, min.key, dummy);
            }
        }
        if (n == null) return null;
        return balance(n);
    }

    private Node minNode(Node n) {
        while (n.left != null) n = n.left;
        return n;
    }

    public int size() {
        rwLock.readLock().lock();
        try {
            return count;
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
