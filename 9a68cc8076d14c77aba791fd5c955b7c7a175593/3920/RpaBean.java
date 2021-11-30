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

/*
 * ShoppingEJB.java
 *
 * Created on May 15, 2003, 5:16 PM
 */

package shopping;

import jakarta.ejb.SessionContext;
import jakarta.ejb.SessionBean;
import jakarta.ejb.EJBException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author  Harpreet
 * @version
 */

public class RpaBean implements SessionBean {

    private String shopper = "anonymous";
    private String principal = "j2ee/shingwai";
    private int totalPrice = 0;

    private int totalItems = 0;

    private List<String> items;

    private List<Integer> itemPrice;

    private SessionContext sc = null;

    /** Creates a new instance of ShoppingEJB */
    public void ejbCreate(String shopperName) {
        shopper = shopperName;
        items = new ArrayList<String>();
        itemPrice = new ArrayList<Integer>();
    }

    public void addItem(String item, int price) throws EJBException,
        RemoteException{
        checkCallerPrincipal();
        items.add(item);
        itemPrice.add(Integer.valueOf(price));
        totalItems++;
        totalPrice += price;
        System.out.println(" Shopping Cart: Shopper "+ shopper +" has bought "
            + item +" for price ="+ price +" .Total Items = "+totalItems +
            " .TotalPrice = " + totalPrice);
    }

    public void deleteItem(String item) throws EJBException,
        RemoteException{
        checkCallerPrincipal();
        int index = items.indexOf(item);
        items.remove(item);
        Integer price = itemPrice.get(index);
        System.out.println("Shopping Cart: Removing item "+ item +" @price "+
            price.intValue());
        totalPrice -= price.shortValue();
        itemPrice.remove(index);
        System.out.println(" Shopping Cart: Shopper "+ shopper +"  .Total Items = "+totalItems +
            " .TotalPrice = " + totalPrice);
    }

    public double getTotalCost() throws EJBException{
        checkCallerPrincipal();
        return totalPrice;
    }

    public String[] getItems() throws EJBException{
        checkCallerPrincipal();
        String[] itemNames = items.toArray(new String[0]);
        return itemNames;
    }

    public void ejbActivate() {
        System.out.println("In Rpa ejbActivate");
    }


    public void ejbPassivate() {
        System.out.println("In Rpa ejbPassivate");
    }


    public void ejbRemove()  {
        System.out.println("In Rpa ejbRemove");
    }


    public void setSessionContext(jakarta.ejb.SessionContext sessionContext) {
        sc = sessionContext;
    }

    private void checkCallerPrincipal() throws EJBException {
        System.out.println("Caller Principal = "+sc.getCallerPrincipal() +
                  " comparing with " + principal);

        if (!sc.isCallerInRole("STAFF") || !sc.isCallerInRole("MGR") ||
                sc.isCallerInRole("ADMIN")) {
            throw new EJBException("Principal should be a Employee, MGR and not ADMIN.");
        }
        if (!sc.getCallerPrincipal().getName().equals(principal)) {
            throw new EJBException("Wrong Principal. Principal should be = "
            + principal);
        }
    }
}
