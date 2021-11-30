package com.piksel.sequoia.clientsdk.criteria;

import static java.util.Arrays.copyOf;

import com.piksel.sequoia.annotations.Internal;
import com.piksel.sequoia.annotations.PublicEvolving;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@PublicEvolving
@Internal
public class LogicalExpression implements Criterion {

    private final String propertyName;
    private final Object[] values;
    private final LogicalLocation logicalLocation;

    public LogicalExpression(String propertyName, Object[] values,
            LogicalLocation logicalLocation) {
        this.propertyName = propertyName;
        this.values = copyOf(values, values.length);
        this.logicalLocation = logicalLocation;
    }

    public Object[] getValues() {
        return copyOf(values, values.length);
    }

    public void applyExpression(DocumentName documentName, QueryString qs) {
        qs.put(documentName.get().concat(propertyName),
                logicalLocation.toMatchString((String[]) getValues()));
    }

}
