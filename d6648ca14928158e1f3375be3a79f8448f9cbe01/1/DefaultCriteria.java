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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.piksel.sequoia.annotations.Internal;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds collections of criteria to be used in queries.
 * 
 * @param <T>
 *            used to allow subtypes to define the fluent return type
 */
@Internal
@Slf4j
public class DefaultCriteria<T extends Criteria<T>> implements Criteria<T> {

    private DocumentName documentName;
    private final List<Criterion> criterionEntries;
    private final List<Order> orderEntries;
    private final List<Inclusion> inclusionEntries;
    private final List<FieldSelector> fieldsEntries;
    private Boolean active;
    private Boolean available;
    private PerPage perPage;
    private Count count;
    private FacetCount facetCount;
    private Language lang;
    private ResourceSettings resourceSettings;

    private int currentOrderIndex = -1; // used for order clause building

    private List<T> intersectCriterias;

    protected DefaultCriteria() {
        this.documentName = new DocumentName();
        this.criterionEntries = new ArrayList<>();
        this.orderEntries = new ArrayList<>();
        this.inclusionEntries = new ArrayList<>();
        this.fieldsEntries = new ArrayList<>();
        this.active = null;
        this.available = null;
        this.intersectCriterias = new ArrayList<>();
    }

    protected DefaultCriteria(String documentName) {
        this.documentName = new DocumentName(documentName);
        this.criterionEntries = new ArrayList<>();
        this.orderEntries = new ArrayList<>();
        this.inclusionEntries = new ArrayList<>();
        this.fieldsEntries = new ArrayList<>();
        this.active = null;
        this.available = null;
        this.intersectCriterias = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T add(Criterion criterion) throws CriteriaException {
        try {
            Preconditions.checkNotNull(criterion);
        } catch (NullPointerException e) {
            throw new UndefinedCriterionException(e);
        } catch (Exception e) {
            throw new CriteriaException(e);
        }
        this.criterionEntries.add(criterion);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T add(Order order) {
        int indexOrder = this.orderEntries.indexOf(order);
        if (indexOrder > -1) {
            this.orderEntries.set(indexOrder, order);
        } else {
            this.orderEntries.add(order);
        }
        this.currentOrderIndex = this.orderEntries.size() - 1;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T add(Inclusion inclusion) {
        int indexInclusion = this.inclusionEntries.indexOf(inclusion);
        if (indexInclusion == -1) {
            this.inclusionEntries.add(inclusion);
        }
        return (T) this;
    }

    private void add(FieldSelector fieldSelector) {
        if (fieldsEntries.indexOf(fieldSelector) == -1) {
            fieldsEntries.add(fieldSelector);
        }
    }

    @Override
    public T and(Criterion criterion) throws CriteriaException {
        return add(criterion);
    }

    @Override
    public T orderBy(String fieldName) {
        Order order = Order.asc(fieldName);
        return add(order);
    }

    /**
     * Allows to specify the ascending order of the last field added to the order criteria
     * 
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    public T asc() throws CriteriaException {
        return orderDirection(true);
    }

    /**
     * Allows to specify the descending order of the last field added to the order criteria
     * 
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    public T desc() throws CriteriaException {
        return orderDirection(false);
    }

    /**
     * Ensure that the currentOrderIndex is in bound in the list of order criteria
     * 
     * @return the currentOrderIndex
     * @since 1.0.0
     */
    private int ensureOrderIndex() throws CriteriaException {
        int size = this.orderEntries.size();
        try {
            Preconditions.checkPositionIndex(this.currentOrderIndex, size);
        } catch (IndexOutOfBoundsException e) {
            throw new UndefinedOrderDirectionException(e);
        } catch (Exception e) {
            throw new CriteriaException(e);
        }
        return this.currentOrderIndex;
    }

    /**
     * Swap out the Order statement if it is different
     * 
     * @param ascending
     *            boolean indicating whether the order direction is ascending or not
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    private T orderDirection(boolean ascending) throws CriteriaException {
        int i = ensureOrderIndex();
        Order order = this.orderEntries.get(i);
        if (order.isAscending() != ascending) {
            String name = order.getPropertyName();
            Order newOrder = Order.desc(name);
            this.orderEntries.set(i, newOrder);
        }
        return (T) this;
    }

    /**
     * Return true if criterionEntries and orderEntries are empty
     * 
     * @since 1.0.0
     */
    @Override
    public boolean isEmpty() {
        return criterionEntries.isEmpty() && orderEntries.isEmpty() && Objects.isNull(perPage) && Objects.isNull(count)
                && Objects.isNull(facetCount) && Objects.isNull(resourceSettings) && inclusionEntries.isEmpty() && fieldsEntries.isEmpty()
                && Objects.isNull(lang)
                && !this.getActive().isPresent()
                && !this.getAvailable().isPresent()
                && this.intersectCriterias.isEmpty();
    }

    @Override
    public List<Criterion> getCriterionEntries() {
        return ImmutableList.copyOf(criterionEntries);
    }

    @Override
    public List<Order> getOrderEntries() {
        return ImmutableList.copyOf(orderEntries);
    }

    @Override
    public List<Inclusion> getInclusionEntries() {
        return ImmutableList.copyOf(inclusionEntries);
    }

    @Override
    public List<FieldSelector> getFieldsEntries() {
        return ImmutableList.copyOf(fieldsEntries);
    }

    @Override
    public Count getCount() {
        return count;
    }

    @Override
    public FacetCount getFacetCount() {
        return facetCount;
    }

    @Override
    public Language getLang() {
        return lang;
    }

    @Override
    public PerPage getPerPage() {
        return perPage;
    }

    @Override
    public List<T> getIntersectCriterias () { return intersectCriterias; }

    @Override
    public DocumentName getDocumentName() { return documentName; }

    @SuppressWarnings("unchecked")
    @Override
    public T setDocumentName (String name) {
        this.documentName = new DocumentName(name);
        return (T) this;
    }

    @Override
    public ResourceSettings resourceSettings() {
        return resourceSettings;
    }

    @Override
    public T orderByOwner() {
        return orderBy("owner");
    }

    @Override
    public T orderByName() {
        return orderBy("name");
    }

    @Override
    public T orderByCreatedAt() {
        return orderBy("createdAt");
    }

    @Override
    public T orderByCreatedBy() {
        return orderBy("createdBy");
    }

    @Override
    public T orderByUpdatedAt() {
        return orderBy("updatedAt");
    }

    @Override
    public T orderByUpdatedBy() {
        return orderBy("updatedBy");
    }



    @SuppressWarnings("unchecked")
    @Override
    public T perPage(int numItemsPerPage) {
        checkHigherThanZero(numItemsPerPage);
        this.perPage = PerPage.builder().perPage(numItemsPerPage).build();
        return (T) this;
    }


    private void checkHigherThanZero(int numPage) {
        Preconditions.checkArgument(numPage > 0, "it must be higher than 0");
    }

    @SuppressWarnings("unchecked")
    @Override
    public T count() {
        this.count = Count.builder().count(true).build();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T count(String fields) {
        this.facetCount = FacetCount.builder().fields(fields).build();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T skipResources() {
        this.resourceSettings = ResourceSettings.builder().skipResources(true).build();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T lang(String lang) {
        this.lang = Language.builder().lang(lang).build();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T active(boolean value) {
        this.active = value;
        return (T) this;
    }

    @Override public Optional<Boolean> getActive() {
        return Optional.ofNullable(this.active);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T available(boolean value) {
        this.available = value;
        return (T) this;
    }

    @Override public Optional<Boolean> getAvailable() {
        return Optional.ofNullable(this.available);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T include(Inclusion... inclusions) {
        for (Inclusion inclusion : inclusions) {
            add(inclusion);
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T fields(String... fieldNames) {
        for (String fieldName : fieldNames) {
            add(new FieldSelector(fieldName));
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T intersect(String resourceName, T criteria) {
        try {
            Preconditions.checkNotNull(criteria, "Intersect criteria is mandatory");
        } catch (NullPointerException e) {
            throw new CriteriaException(e);
        }

        try {
            Preconditions.checkNotNull(resourceName, "Resource name is mandatory");
            Preconditions.checkArgument(!StringUtils.isBlank(resourceName), "Resource name should contain some value");
            Preconditions.checkArgument(!criteria.isEmpty(), "Intersect criteria should contain any criteria");
        } catch (NullPointerException e) {
            throw new InvalidResourceNameException(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new CriteriaException(e);
        }
        if (!Objects.isNull(this.facetCount)) {
            log.warn("Facet count incompatible with intersections");
            this.facetCount = null;
        }

        this.intersectCriterias.add(criteria.setDocumentName(resourceName));
        return (T) this;
    }
    
}
