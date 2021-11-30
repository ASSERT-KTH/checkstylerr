/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.unsynchpc_flush.ejb;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.TransactionRequiredException;


@Stateless
public class SLSBWithUnsynchPC implements Tester {
    @PersistenceContext(unitName="lib/unsynchpc_flush-par.jar#em",
            synchronization = SynchronizationType.UNSYNCHRONIZED)
    EntityManager em;

    @Override
    public boolean flushBeforeJoin() {
        System.out.println("I am in flushBeforeJoin");

        Person p = new Person("Tom");
        em.persist(p);

        try {
            System.out.println("is jonined before flush: " + em.isJoinedToTransaction());
            //flush before the unsynchronized PC join transaction
            em.flush();
            System.out.println("flushed with no exceptions thrown");
            return false;
        } catch (TransactionRequiredException tre) {
            /* SPEC: A persistence context of type SynchronizationType.UNSYNCHRONIZED must
             * not be flushed to the database unless it is joined to a transaction
             */
            //Expected exception thrown
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            //Unexpected exception type
            return false;
        }
    }

    @Override
    public boolean flushAfterJoin() {
        System.out.println("I am in flushAfterJoin");

        Person p = new Person("Tom2");
        em.persist(p);

        System.out.println("is jonined before joinTransaction: " + em.isJoinedToTransaction());
        //Join transaction
        em.joinTransaction();
        try {
            System.out.println("is jonined before flush: " + em.isJoinedToTransaction());
            /* SPEC: After the persistence context has been joined to a transaction,
             * changes in a persistence context can be flushed to the database explicitly
             * by the application
             */
            em.flush();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void autoFlushByProvider(String name) {
        System.out.println("I am in autoFlushByProvider");

        Person p = new Person(name);
        em.persist(p);

        System.out.println("is jonined before joinTransaction: " + em.isJoinedToTransaction());
        //Join transaction
        em.joinTransaction();
    }

    @Override
    public boolean isPersonFound(String name) {
        System.out.println("I am in isPersonFound");
        return em.find(Person.class, name) != null;
    }
}
