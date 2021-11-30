package com.piksel.sequoia.clientsdk.criteria;

import com.piksel.sequoia.annotations.Internal;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * An expression {@link Criterion} used during selection.
 */
@Getter
@EqualsAndHashCode
@Internal
public class RangeExpression implements Criterion {

    private final String propertyName;
    private final Object rangeValueA;
    private final Object rangeValueB;
    private final RangeLocation rangeLocation;

    public RangeExpression(String propertyName, Object rangeValueA,
            Object rangeValueB, RangeLocation rangeLocation) {
        this.propertyName = propertyName;
        this.rangeValueA = rangeValueA;
        this.rangeValueB = rangeValueB;
        this.rangeLocation = rangeLocation;
    }

    public RangeExpression(String propertyName, Object rangeValueA,
            RangeLocation rangeLocation) {
        this.propertyName = propertyName;
        this.rangeValueA = rangeValueA;
        this.rangeValueB = null;
        this.rangeLocation = rangeLocation;
    }

    public void applyExpression(DocumentName documentName, QueryString qs) {
        String rva = String.valueOf(this.rangeValueA);
        if (this.rangeValueB != null) {
            String rvb = String.valueOf(this.rangeValueB);
            qs.put(documentName.get().concat(this.propertyName),
                    this.rangeLocation.toMatchString(rva, rvb));
        } else {
            qs.put(documentName.get().concat(this.propertyName), this.rangeLocation.toMatchString(rva));
        }
    }

}