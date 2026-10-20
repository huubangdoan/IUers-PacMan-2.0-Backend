package com.Iuer.Pacman.index;

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
    private Node insert(Node n, K key, V value) {
        if (n == null) {
            inserted = true;               
            return new Node(key, value);
        }
        int c = key.compareTo(n.key);
        if (c < 0) {
            n.left = insert(n.left, key, value);
        } else if (c > 0) {
            n.right = insert(n.right, key, value);
        }
    return n;
}

    private Node root;
    private int count = 0;
    private boolean inserted;

    public boolean insert(K key, V value) {
        inserted = false;
        root = insert(root, key, value);
        if (inserted) count++;
        return inserted;
}

    public V find(K key) {
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
}

    public boolean erase(K key) {
        return false;
    }

    public int size() {
        return count;
    }

    public void inorder() {
    }
}