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

    private Node root;
    private int count = 0;

    public boolean insert(K key, V value) {
        return false;
    }

    public V find(K key) {
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