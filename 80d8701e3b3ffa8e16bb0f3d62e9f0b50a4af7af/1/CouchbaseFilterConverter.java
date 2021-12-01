/*
 /*
 * oxCore is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */

package org.gluu.persist.couchbase.impl;

import org.gluu.persist.exception.operation.SearchException;
import org.gluu.persist.ldap.impl.LdapFilterConverter;
import org.gluu.search.filter.Filter;
import org.gluu.search.filter.FilterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.java.query.dsl.Expression;

/**
 * Filter to Couchbase expressions convert
 *
 * @author Yuriy Movchan Date: 05/15/2018
 */
public class CouchbaseFilterConverter {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseFilterConverter.class);
    
    private LdapFilterConverter ldapFilterConverter = new LdapFilterConverter();

    public Expression convertToCouchbaseFilter(Filter genericFilter) throws SearchException {
        Filter currentGenericFilter = genericFilter;

        FilterType type = currentGenericFilter.getType();
        if (FilterType.RAW == type) {
        	LOG.warn("RAW Ldap filter to Couchbase convertion will be removed in new version!!!");
        	currentGenericFilter = ldapFilterConverter.convertRawLdapFilterToFilter(currentGenericFilter.getFilterString());
        	type = currentGenericFilter.getType();
        }

        if ((FilterType.NOT == type) || (FilterType.AND == type) || (FilterType.OR == type)) {
            Filter[] genericFilters = currentGenericFilter.getFilters();
            Expression[] expFilters = new Expression[genericFilters.length];

            if (genericFilters != null) {
                for (int i = 0; i < genericFilters.length; i++) {
                    expFilters[i] = convertToCouchbaseFilter(genericFilters[i]);
                }

                if (FilterType.NOT == type) {
                    return Expression.par(expFilters[0].not());
                } else if (FilterType.AND == type) {
                    Expression result = expFilters[0];
                    for (int i = 1; i < expFilters.length; i++) {
                        result = result.and(expFilters[i]);
                    }
                    return Expression.par(result);
                } else if (FilterType.OR == type) {
                    Expression result = expFilters[0];
                    for (int i = 1; i < expFilters.length; i++) {
                        result = result.or(expFilters[i]);
                    }
                    return Expression.par(result);
                }
            }
        }

        if (FilterType.EQUALITY == type) {
            if (currentGenericFilter.isArrayAttribute()) {
                return Expression.path(Expression.s(currentGenericFilter.getAssertionValue()).in(Expression.path(currentGenericFilter.getAttributeName())));
            } else {
                Expression exp1 = Expression
                        .par(Expression.path(Expression.path(currentGenericFilter.getAttributeName())).eq(Expression.s(currentGenericFilter.getAssertionValue())));
                Expression exp2 = Expression
                        .par(Expression.path(Expression.s(currentGenericFilter.getAssertionValue())).in(Expression.path(currentGenericFilter.getAttributeName())));
                return Expression.par(exp1.or(exp2));
            }
        }

        if (FilterType.LESS_OR_EQUAL == type) {
            return Expression.path(Expression.path(currentGenericFilter.getAttributeName())).lte(Expression.s(currentGenericFilter.getAssertionValue()));
        }

        if (FilterType.GREATER_OR_EQUAL == type) {
            return Expression.path(Expression.path(currentGenericFilter.getAttributeName())).gte(Expression.s(currentGenericFilter.getAssertionValue()));
        }

        if (FilterType.PRESENCE == type) {
            return Expression.path(Expression.path(currentGenericFilter.getAttributeName())).isNotMissing();
        }

        if (FilterType.APPROXIMATE_MATCH == type) {
            throw new SearchException("Convertion from APPROXIMATE_MATCH LDAP filter to Couchbase filter is not implemented");
        }

        if (FilterType.SUBSTRING == type) {
            StringBuilder like = new StringBuilder();
            if (currentGenericFilter.getSubInitial() != null) {
                like.append(currentGenericFilter.getSubInitial());
            }
            like.append("%");

            String[] subAny = currentGenericFilter.getSubAny();
            if ((subAny != null) && (subAny.length > 0)) {
                for (String any : subAny) {
                    like.append(any);
                    like.append("%");
                }
            }

            if (currentGenericFilter.getSubFinal() != null) {
                like.append(currentGenericFilter.getSubFinal());
            }
            return Expression.path(Expression.path(currentGenericFilter.getAttributeName()).like(Expression.s(like.toString())));
        }

        throw new SearchException(String.format("Unknown filter type '%s'", type));
    }

}
