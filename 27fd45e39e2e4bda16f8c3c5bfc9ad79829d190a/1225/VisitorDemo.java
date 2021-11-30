package com.sourcemaking.visitor.fourth_example;

import java.util.*;

interface Component {
    void traverse();
}

class Leaf implements Component {
    private int number;

    public Leaf(int value) {
        this.number = value;
    }

    public void traverse() {
        System.out.print(number + " ");
    }
}

class Composite implements Component {
    private static char next = 'a';
    private List children = new ArrayList();
    private char letter = next++;

    public void add(Component c) {
        children.add(c);
    }

    public void traverse() {
        System.out.print(letter + " ");
        for (Object aChildren : children) {
            ((Component) aChildren).traverse();
        }
    }
}

public class VisitorDemo {
    public static void main( String[] args ) {
        Composite[] containers = new Composite[3];
        for (int i=0; i < containers.length; i++) {
            containers[i] = new Composite();
            for (int j=1; j < 4; j++) {
                containers[i].add(new Leaf(i * containers.length + j));
            }
        }
        for (int i=1; i < containers.length; i++) {
            containers[0].add(containers[i]);
        }
        containers[0].traverse();
        System.out.println();
    }
}
