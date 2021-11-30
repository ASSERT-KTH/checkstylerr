package com.piksel.sequoia.clientsdk.criteria;

import java.util.List;
import java.util.Optional;

import com.piksel.sequoia.annotations.PublicEvolving;
import com.piksel.sequoia.clientsdk.resource.DirectRelationship;
import com.piksel.sequoia.clientsdk.resource.IndirectRelationship;
import com.piksel.sequoia.clientsdk.resource.ResourceIterable;

/**
 * Specifies a collection of various {@link Criterion} which together form components of a resourceful query. The interface has a <em>self-referential
 * </em> generic type which allows subclasses to fluently return itself.
 * 
 * @param <T>
 *            self referencing generic type for fluent API
 */
@PublicEvolving
public interface Criteria<T extends Criteria<T>> {

    /**
     * Add a criterion to the criteria list.
     * 
     * @param criterion
     *            criterion to add to the list of criteria
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T add(Criterion criterion) throws CriteriaException;

    /**
     * Add an order criterion to the list of order criteria
     * 
     * @param order
     *            ordering criterion to add to the list of order criteria
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T add(Order order);

    /**
     * Add an inclusion criterion to the list of order criteria
     * 
     * @param inclusion
     *            the inclusion criterion to add to the list of inclusion criteria
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T add(Inclusion inclusion);

    /**
     * Provide a fluent interface method to add a criterion to the criteria list.
     * 
     * @param criterion
     *            criterion to add to the list of criteria
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T and(Criterion criterion) throws CriteriaException;

    /**
     * Return true if the list of all criteria is empty
     * 
     * @since 1.0.0
     */
    boolean isEmpty();

    /**
     * Return a list of {@link Criterion}.
     * 
     * @since 1.0.0
     */
    List<Criterion> getCriterionEntries();

    /**
     * Return a list of Order criterion
     * 
     * @since 1.0.0
     */
    List<Order> getOrderEntries();

    /**
     * Return a list of Fields criterion
     * 
     * @since 1.0.0
     */
    List<FieldSelector> getFieldsEntries();

    /**
     * Return the perPage value
     * 
     * @since 1.0.0
     */
    PerPage getPerPage();

    /**
     * Return the page value
     * 
     * @since 1.0.0
     */
    Page getPage();

    /**
     * Return the count value
     * 
     * @since 1.0.0
     */
    Count getCount();

    /**
     * Return the count type value
     * 
     * @since 1.0.1
     */
    FacetCount getFacetCount();

    /**
     * Return the skipResources value.
     * 
     * @since 1.0.0
     */
    ResourceSettings resourceSettings();

    /**
     * Return the language value.
     * 
     * @since 1.0.0
     */
    Language getLang();

    /**
     * Allows to specify the ascending order of the last field added to the order criteria
     * 
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T asc() throws CriteriaException;

    /**
     * Allows to specify the descending order of the last field added to the order criteria
     * 
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T desc() throws CriteriaException;

    /**
     * Allows results to be sorted via the given field and on its natural ordering.
     * 
     * @param fieldName
     *            field name to be used to create an order criterion
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T orderBy(String fieldName);

    /**
     * Convenience method for creating an ordering over the resource owner.
     * 
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T orderByOwner();

    /**
     * Convenience method for creating an ordering over the resource name.
     * 
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T orderByName();

    /**
     * Convenience method for creating an ordering over the createdAt timestamp.
     * 
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T orderByCreatedAt();

    /**
     * Convenience method for creating an ordering over the createdBy field.
     * 
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T orderByCreatedBy();

    /**
     * Convenience method for creating an ordering over the updatedBy field.
     * 
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T orderByUpdatedBy();

    /**
     * Convenience method for creating an ordering over updatedAt field.
     * 
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T orderByUpdatedAt();

    /**
     * Request that pagination only contains the provided number of items.
     * 
     * @param numItemsPerPage
     *            num items per page.
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T perPage(int numItemsPerPage);

    /**
     * Request a specific page number.
     * 
     * @param numPage
     *            the number of the page to be retrieved
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T page(int numPage);

    /**
     * Add the count to be retrieved as totalCount in {@link ResourceIterable}.
     */
    T count();

    /**
     * Request the fields count to be retrieved as facetCount in {@link ResourceIterable}.
     * 
     * @param fields
     *            for performing the facetCount.
     * @since 1.0.1
     */
    T count(String fields);

    /**
     * Request that the result does not contain resources when used in combination with the {@link #count()} method.
     */
    T skipResources();

    /**
     * Return a list of Inclusion criterion.
     * 
     * @since 1.0.0
     */
    List<Inclusion> getInclusionEntries();

    /**
     * Requests that a specific set of fields are included in the response.
     * 
     * <p>
     * Allows a so-called <em>sparse fieldset</em> response to be returned to the user.
     * 
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T fields(String... fieldNames);

    /**
     * Include linked resources through the {@link Inclusion} object.
     * 
     * <p>
     * Inclusion allows a resource to be requested with its associated linked resources available directly on the resource.
     * 
     * @param inclusions
     *            the set of inclusions to add to the criteria
     * @return a reference to the current instance of this object
     * @since 1.0.0
     * @see DirectRelationship
     * @see IndirectRelationship
     */
    T include(Inclusion... inclusions);

    /**
     * Allow to specify the language for retrieving the fields.
     * 
     * @param language
     *            the language to select
     * @return a reference to the current instance of this object
     * @since 1.0.0
     */
    T lang(String language);

    /**
     * Allow to set the active filter to some boolean value
     *
     * @param value
     *            the value of active filter
     * @return a reference to the current instance of this object
     * @since 1.5.0
     */
    T active (boolean value);

    /**
     * Return the value of active flag (true, false or absent)
     *
     * @since 1.5.0
     */
    Optional<Boolean> getActive ();

    /**
     * Allow to set the available filter to some boolean value
     *
     * @param value
     *            the value of available filter
     * @return a reference to the current instance of this object
     * @since 1.5.0
     */
    T available (boolean value);

    /**
     * Return the value of available flag (true, false or absent)
     *
     * @since 1.5.0
     */
    Optional<Boolean> getAvailable ();

    /**
     * Return the name of the document to apply the criteria
     *
     * @since 1.5.0
     */
    DocumentName getDocumentName();

    /**
     * Set the name of the document to apply the criteria
     * @param name
     *              the value of the name of the document
     * @since 1.5.0
     */
    T setDocumentName (String name);

    /**
     * Allow to set a criteria for a intersected document
     *
     * @param resourceName
     *          the name of the resource to interset and apply the criteria
     * @param criteria
     *          the set of criterias to filter intersected document
     * @since 1.5.0
     */
    T intersect(String resourceName, T criteria);

    List<T> getIntersectCriterias();

}