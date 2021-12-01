package org.semanticweb.owlapi.util;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;

import com.google.common.collect.Iterators;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.semanticweb.owlapi.model.MIMETypeAware;
import org.semanticweb.owlapi.model.PriorityCollectionSorting;

/**
 * A collection that is sorted by HasPriority annotation on its members.
 * 
 * @author ignazio
 * @param <T>
 *        type of the collection
 * @since 4.0.0
 */
public class PriorityCollection<T extends Serializable> implements Iterable<T>, Serializable {

    private final List<T> delegate = Collections.synchronizedList(new ArrayList<T>());
    private final PriorityCollectionSorting configurationHolder;

    /**
     * @param sorting
     *        the configuration holder for sort settings.
     */
    public PriorityCollection(PriorityCollectionSorting sorting) {
        this.configurationHolder = sorting;
    }

    /**
     * @return true if the collection is empty
     */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * @return size of the collection
     */
    public int size() {
        return delegate.size();
    }

    private void sort() {
        configurationHolder.sort(delegate);
    }

    private void sortSet() {
        configurationHolder.sortInputSet(delegate);
    }

    /**
     * @param c
     *        collection of elements to set. Existing elements will be removed,
     *        and the priority collection will be sorted according to the
     *        PriorityCollectionSorting value for the manager configuration.
     */
    public void set(Iterable<T> c) {
        clear();
        add(c);
    }

    /**
     * @param c
     *        collection of elements to set. Existing elements will be removed,
     *        and the priority collection will be sorted according to the
     *        PriorityCollectionSorting value for the manager configuration.
     */
    public void set(Set<T> c) {
        clear();
        for (T t : c) {
            delegate.add(t);
        }
        sortSet();
    }

    /**
     * Remove all elements, replace with the arguments and sort according to
     * priority.
     * 
     * @param c
     *        list of elements to set
     */
    public void set(@SuppressWarnings("unchecked") T... c) {
        clear();
        add(c);
    }

    /**
     * Add the arguments and sort according to the PriorityCollectionSorting
     * value for the manager configuration.
     * 
     * @param c
     *        list of elements to add
     */
    public void add(@SuppressWarnings("unchecked") T... c) {
        int i = 0;
        for (T t : c) {
            delegate.add(i++, t);
        }
        sort();
    }

    /**
     * Add the argument and sort according to the PriorityCollectionSorting
     * value for the manager configuration.
     * 
     * @param t
     *        element to add
     */
    public void add(T t) {
        delegate.add(0, t);
        sort();
    }

    /**
     * Add the arguments and sort according to the PriorityCollectionSorting
     * value for the manager configuration.
     * 
     * @param c
     *        list of elements to add
     */
    public void add(Iterable<T> c) {
        int i = 0;
        for (T t : c) {
            delegate.add(i++, t);
        }
        sort();
    }

    /**
     * Remove the arguments.
     * 
     * @param c
     *        list of elements to remove
     */
    public void remove(@SuppressWarnings("unchecked") T... c) {
        for (T t : c) {
            delegate.remove(t);
        }
    }

    /**
     * Remove the argument.
     * 
     * @param c
     *        element to remove
     */
    public void remove(T c) {
        delegate.remove(c);
    }

    /**
     * Rmove all elements from the collection.
     */
    public void clear() {
        delegate.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.unmodifiableIterator(delegate.iterator());
    }

    /**
     * Returns the first item matching the mime type<br>
     * NOTE: The order in which the services are loaded an examined is not
     * deterministic so this method may return different results if the
     * MIME-Type matches more than one item. However, if the default MIME-Types
     * are always unique, the correct item will always be chosen
     * 
     * @param mimeType
     *        A MIME type to use for choosing an item
     * @return An item matching the given mime type or null if none were found.
     */
    public PriorityCollection<T> getByMIMEType(String mimeType) {
        checkNotNull(mimeType, "MIME-Type cannot be null");
        PriorityCollection<T> pc = new PriorityCollection<>(configurationHolder);
        // adding directly to the delegate. No need to order because insertion
        // will be ordered as in this PriorityCollection
        for (T t : delegate) {
            // if the instance has MIME types associated
            if (t instanceof MIMETypeAware) {
                MIMETypeAware mimeTypeAware = (MIMETypeAware) t;
                if (mimeType.equals(mimeTypeAware.getDefaultMIMEType())) {
                    pc.add(t);
                } else {
                    if (mimeTypeAware.getMIMETypes().contains(mimeType)) {
                        pc.add(t);
                    }
                }
            }
        }
        return pc;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
