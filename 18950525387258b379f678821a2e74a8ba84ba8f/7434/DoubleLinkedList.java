/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * ConnectionImpl.java
 *
 * Create on March 3, 2000
 */

package com.sun.jdo.spi.persistence.utility;

/**
 * This class defines a thread-safe double linked-list.
 * The list is usable by any class that implements the
 * com.forte.util.Linkable interface.  This class allows
 * a linkable object it be inserted or removed from anywhere
 * in the list.
 *
 * RESTRICTION: An object can only be a member of 1 list
 * at a time.
 */
public class DoubleLinkedList
{

    // Instance variables.

    /**
     * Head of linked list.
     */
    public Linkable                        head;

    /**
     * Tail of linked list.
     */
    public Linkable                        tail;

    /**
     * Size of linked list.
     */
    public int                            size;

    /**
     * Default constructor.
     */
    public DoubleLinkedList()
    {
        this.head = null;
        this.size = 0;
        this.tail = null;
    }


    // Public Methods.

    /**
     * Return the object at the head of a linked list.
     *
     */
    public synchronized Linkable getHead()
    {
        return this.head;
    }

    /**
     * Return the object at the tail of a linked list.
     *
     */
    public synchronized Linkable getTail()
    {
        return this.tail;
    }


    /**
     * Return size of the linked list.
     *
     */
    public synchronized int getSize()
    {
        return this.size;
    }


    /**
     * Insert an object at the head of a linked list.
     *
     */
    public synchronized void insertAtHead(Linkable node)
    {
        if (node instanceof Linkable)
        {
            if (this.head == null)
            {
                node.setNext(null);                // Fixup node nextlink.
                node.setPrevious(null);            // Fixup node backlink.
                this.head = node;                // Insert node at head of list.
            }
            else
            {
                Linkable oldHead = this.head;    // Fixup current head node.
                oldHead.setPrevious(node);        // Set backlink to new node.
                node.setNext(oldHead);            // Fixup new node nextlink.
                node.setPrevious(null);            // Fixup new node backlink.
                this.head = node;                // Insert node at head of list.
            }
            if (this.tail == null)                // If list was empty,
            {
                this.tail = node;                // Insert node at tail of list.
            }
            this.size++;
        }
    }

    /**
     * Insert an object at the tail of a linked list.
     *
     */
    public synchronized void insertAtTail(Linkable node)
    {
        if (node instanceof Linkable)
        {
            if (this.tail == null)
            {
                node.setNext(null);                // Fixup node nextlink.
                node.setPrevious(null);            // Fixup node backlink.
                this.tail = node;                // Insert node at end of list.
            }
            else
            {
                Linkable oldTail = this.tail;    // Fixup current tail node.
                oldTail.setNext(node);            // Set backlink to new node.
                node.setNext(null) ;            // Fixup new node backlink.
                node.setPrevious(oldTail);        // Fixup new node nextlink.
                this.tail = node;                // Insert node at end of list.
            }
            if (this.head == null)                // If list was empty,
            {
                this.head = node;                // Insert node at head of list.
            }
            this.size++;
        }
    }



    /**
     * Remove and return an object from the head of a linked list.
     *
     */
    public synchronized Linkable removeFromHead()
    {
        Linkable node = this.head;
        if (node instanceof Linkable)
        {
            this.head = node.getNext();    // Set head to next node.
            if (this.head == null)        // If we emptied the list,
            {
                this.tail = null;        // Fixup the tail pointer.
            }
            else
            {
                this.head.setPrevious(null);// Clear head node backlink.
            }
            node.setNext(null);            // Clear removed node nextlink.
            node.setPrevious(null);        // Clear romoved node backlink.
            this.size--;
        }
        return node;
    }

    /**
     * Remove and return an object from the tail of a linked list.
     *
     */
    public synchronized Linkable removeFromTail()
    {
        Linkable node = this.tail;
        if (node instanceof Linkable)
        {
            this.tail = node.getPrevious();    // Set tail to previous node.
            if (this.tail == null)            // If we emptied the list,
            {
                this.head = null;            // Fixup the head pointer.
            }
            else
            {
                this.tail.setNext(null);    // Clear tail node nextlink.
            }
            node.setNext(null);                // Clear removed node nextlink.
            node.setPrevious(null);            // Clear removed node backlink.
            this.size--;
        }
        return node;
    }

    /**
     * Remove the specified object from anywhere in the linked list.
     * This method is usually used by the object to remove itself
     * from the list.
     *
     */
    public synchronized void removeFromList(Linkable node)
    {
        if ((this.size <= 0) || ((this.head == null) && (this.tail == null)))
        {
            return;
        }
        if (node instanceof Linkable)
        {
            Linkable p = node.getPrevious();    // Reference to previous node.
            Linkable n = node.getNext();        // Reference to next node.

            if (p == null)            // Is this the first (or only) node in the list?
            {
                this.head = n;        // Yes, set the head of the list to point to the next.
            }
            else
            {
                p.setNext(n);        // No, set the previous node to point to the next.
            }

            if (n == null)            // Is this the last (or only) node in the list?
            {
                this.tail = p;        // Yes, set the tail to point to the previous.
            }
            else
            {
                n.setPrevious(p);    // No, set the next node to point to the previous.
            }

            node.setNext(null);
            node.setPrevious(null);
            this.size--;
        }
    }


    /**
     * Insert an object anywhere into the linked list.
     *
     * @param afternode            the new node will be inserted after this node
     * @param newnode            the new node to be inserted
     */
    public synchronized void insertIntoList(Linkable afternode, Linkable newnode)
    {
        if ((newnode instanceof Linkable) && (afternode instanceof Linkable))
        {
            if (this.tail == afternode)            // If inserting at the tail,
            {
                this.insertAtTail(newnode);        // Use insertAtTail method.
            }
            else
            {
                Linkable nextnode = afternode.getNext();
                newnode.setNext(nextnode);        // Point to next node.
                newnode.setPrevious(afternode);    // Point to previous node.
                afternode.setNext(newnode);        // Fixup backlink in afternode.
                nextnode.setPrevious(newnode);    // Fixup nextlink in next node.
            }
            this.size++;
        }
    }

    /**
     * Return a string representation of this DoubleLinkedList object.
     * <p>
     * @return  String representation of this object.
     */
    public synchronized String toString()
    {
        /*        boolean dif = ThreadContext.lgr().test
            (    // Check for trace flag sp:1:1
                TraceLogger.CONFIGURATION,
                TraceLogger.SVC_SP,
                SPLogFlags.CFG_DIFFABLE_EXCEPTS,
                1
            );
        String buf = "DoubleLinkedList@\n";
        if(!dif)
        {
            buf = buf + "      head = " + this.head + "\n";
            buf = buf + "      tail = " + this.tail + "\n";
        }
        buf = buf + "      size = " + this.size + "\n";
        return buf;
        */

        return null;
    }
}

