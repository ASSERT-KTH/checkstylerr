package com.piksel.sequoia.clientsdk.criteria;

import com.piksel.sequoia.annotations.Internal;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * An expression {@link Criterion} used during selection.
 * 
 * <p>This expression is <em>simple</em> because it produces
 * a basic equality criteria, as opposed to other more intricate
 * expressions such as range expressions or text search 
 * expressions.
 */
@Internal
@Getter
@EqualsAndHashCode
public class SimpleExpression implements Criterion {

    private final String propertyName;
    private final Object value;
    private final Operator op;

    public SimpleExpression(String propertyName, Object value, Operator op) {
        this.propertyName = propertyName;
        this.value = value;
        this.op = op;
    }

    public void applyExpression(DocumentName documentName, QueryString qs) {
        qs.put(documentName.get().concat(this.propertyName), String.valueOf(this.value));
    }

}