package com.piksel.sequoia.clientsdk.criteria;

/*-
 * #%L
 * Sequoia Java Client SDK
 * %%
 * Copyright (C) 2018 Piksel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static com.google.api.client.util.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.piksel.sequoia.annotations.PublicEvolving;

import lombok.EqualsAndHashCode;

/**
 * Specify a linked resource is included on the returned resource.
 */
@PublicEvolving
@EqualsAndHashCode
public class Inclusion {

    private final String resourceName;
    private final List<FieldSelector> fieldsEntries;
    private final List<Order> ordersEntries;
    private int currentOrderIndex = -1; // used for order clause building

    private Inclusion(String resourceName) {
        this.resourceName = resourceName;
        this.fieldsEntries = newArrayList();
        this.ordersEntries = newArrayList();
    }

    public static Inclusion resource(String resourceName) {
        ArgumentValidation.checkResourceName(resourceName);
        return new Inclusion(resourceName);
    }

    public String toString() {
        return this.resourceName;
    }

    /**
     * Create an inclusion from the provided field names.
     */
    public Inclusion fields(String... fieldNames) {
        for (String fieldName : fieldNames) {
            add(new FieldSelector(fieldName));
        }
        return this;
    }
    
    /**
     * Create an inclusion from the provided order.
     */
    public Inclusion orderBy(String fieldName) {
        Order order = Order.asc(fieldName);
        return add(order);
    }

    /**
     * Create an inclusion from the provided field names,
     * 
     * @deprecated {@link #fields} will be used for this functionality.
     */
    @Deprecated
    public Inclusion include(String... fieldNames) {
        return fields(fieldNames);
    }

    /**
     * Provide all of the linked fields to be included.
     */
    public List<String> getFieldEntries() {
        List<String> list = new ArrayList<>();
        fieldsEntries.forEach(field -> list.add(field.toString()));
        return list;
    }
    
    /**
     * Provide all of the linked sort entries to be included.
     */
    public List<String> getOrderEntries() {
        List<String> list = new ArrayList<>();
        ordersEntries.forEach(order -> list.add(order.toString()));
        return list;
    }
    
    /**
     * Allows to specify the ascending order of the last field added to the order
     */
    public Inclusion asc() throws CriteriaException {
        return orderDirection(true);
    }

    /**
     * Allows to specify the descending order of the last field added to the order
     */
    public Inclusion desc() throws CriteriaException {
        return orderDirection(false);
    }
    
    private Inclusion orderDirection(boolean ascending) throws CriteriaException {
        int i = ensureOrderIndex();
        Order order = this.ordersEntries.get(i);
        if (order.isAscending() != ascending) {
            String name = order.getPropertyName();
            Order newOrder = Order.desc(name);
            this.ordersEntries.set(i, newOrder);
        }
        return this;
    }

    private void add(FieldSelector fieldSelector) {
        if (fieldsEntries.indexOf(fieldSelector) == -1) {
            fieldsEntries.add(fieldSelector);
        }
    }
    
    private Inclusion add(Order order) {
        int indexOrder = this.ordersEntries.indexOf(order);
        if (indexOrder > -1) {
            this.ordersEntries.set(indexOrder, order);
        } else {
            this.ordersEntries.add(order);
        }
        this.currentOrderIndex = this.ordersEntries.size() - 1;
        return this;
    }
    
    private int ensureOrderIndex() throws CriteriaException {
        int size = this.ordersEntries.size();
        try {
            Preconditions.checkPositionIndex(this.currentOrderIndex, size);
        } catch (IndexOutOfBoundsException e) {
            throw new UndefinedOrderDirectionException(e);
        } catch (Exception e) {
            throw new CriteriaException(e);
        }
        return this.currentOrderIndex;
    }
    

}
