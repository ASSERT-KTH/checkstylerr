/*
 /*
 * oxCore is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */

package org.gluu.persist.couchbase.impl;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.gluu.persist.annotation.AttributeEnum;
import org.gluu.persist.exception.operation.SearchException;
import org.gluu.persist.ldap.impl.LdapFilterConverter;
import org.gluu.persist.reflect.property.PropertyAnnotation;
import org.gluu.persist.reflect.util.ReflectHelper;
import org.gluu.search.filter.Filter;
import org.gluu.search.filter.FilterType;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.java.query.dsl.Expression;

/**
 * Filter to Couchbase expressions convert
 *
 * @author Yuriy Movchan Date: 05/15/2018
 */
@ApplicationScoped
public class CouchbaseFilterConverter {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseFilterConverter.class);
    
    private LdapFilterConverter ldapFilterConverter = new LdapFilterConverter();

    public Expression convertToCouchbaseFilter(Filter genericFilter, Map<String, PropertyAnnotation> propertiesAnnotationsMap) throws SearchException {
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
                    expFilters[i] = convertToCouchbaseFilter(genericFilters[i], propertiesAnnotationsMap);
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
        	Boolean isMultiValuedDetected = determineMultiValuedByType(currentGenericFilter.getAttributeName(), propertiesAnnotationsMap);
            if (currentGenericFilter.isMultiValued() || Boolean.TRUE.equals(isMultiValuedDetected)) {
                return Expression.path(buildTypedExpression(currentGenericFilter).in(Expression.path(currentGenericFilter.getAttributeName())));
            } else if (Boolean.FALSE.equals(isMultiValuedDetected)) {
            	return Expression.path(Expression.path(currentGenericFilter.getAttributeName())).eq(buildTypedExpression(currentGenericFilter));
            } else {
                Expression exp1 = Expression
                        .par(Expression.path(Expression.path(currentGenericFilter.getAttributeName())).eq(buildTypedExpression(currentGenericFilter)));
                Expression exp2 = Expression
                        .par(Expression.path(buildTypedExpression(currentGenericFilter)).in(Expression.path(currentGenericFilter.getAttributeName())));
                return Expression.par(exp1.or(exp2));
            }
        }

        if (FilterType.LESS_OR_EQUAL == type) {
            return Expression.path(Expression.path(currentGenericFilter.getAttributeName())).lte(buildTypedExpression(currentGenericFilter));
        }

        if (FilterType.GREATER_OR_EQUAL == type) {
            return Expression.path(Expression.path(currentGenericFilter.getAttributeName())).gte(buildTypedExpression(currentGenericFilter));
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

	private Expression buildTypedExpression(Filter currentGenericFilter) {
		if (currentGenericFilter.getAssertionValue() instanceof Boolean) {
			return Expression.x((Boolean) currentGenericFilter.getAssertionValue());
		} else if (currentGenericFilter.getAssertionValue() instanceof Integer) {
			return Expression.x((Integer) currentGenericFilter.getAssertionValue());
		} else if (currentGenericFilter.getAssertionValue() instanceof Long) {
			return Expression.x((Long) currentGenericFilter.getAssertionValue());
		}

		return Expression.s(StringHelper.escapeSql(currentGenericFilter.getAssertionValue()));
	}

	private Boolean determineMultiValuedByType(String attributeName, Map<String, PropertyAnnotation> propertiesAnnotationsMap) {
		if (propertiesAnnotationsMap == null) {
			return null;
		}

		PropertyAnnotation propertyAnnotation = propertiesAnnotationsMap.get(attributeName);
		if ((propertyAnnotation == null) || (propertyAnnotation.getParameterType() == null)) {
			return null;
		}

		Class<?> parameterType = propertyAnnotation.getParameterType();
		
		boolean isMultiValued = parameterType.equals(String[].class) || ReflectHelper.assignableFrom(parameterType, List.class) || ReflectHelper.assignableFrom(parameterType, AttributeEnum[].class); 
		
		return isMultiValued;
	}

}
