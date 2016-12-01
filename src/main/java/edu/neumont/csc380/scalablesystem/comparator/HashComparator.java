package edu.neumont.csc380.scalablesystem.comparator;

import java.io.Serializable;
import java.util.Comparator;

public class HashComparator<T> implements Comparator<T>, Serializable {
    @Override
    public int compare(Object o1, Object o2) {
        Integer h1 = o1.hashCode();
        Integer h2 = o2.hashCode();
        return h1.compareTo(h2);
    }
}
