/*
 * Copyright 2004-2020 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.expression.function;

import org.h2.engine.Session;
import org.h2.expression.ExpressionVisitor;
import org.h2.expression.Operation0;
import org.h2.value.TypeInfo;
import org.h2.value.Value;
import org.h2.value.ValueTime;
import org.h2.value.ValueTimestamp;

/**
 * Current datetime value function.
 */
public final class CurrentDateTimeValueFunction extends Operation0 {

    public static final int CURRENT_DATE = 0, CURRENT_TIME = 1, LOCALTIME = 2, CURRENT_TIMESTAMP = 3,
            LOCALTIMESTAMP = 4;

    private static final int[] TYPES = { Value.DATE, Value.TIME_TZ, Value.TIME, Value.TIMESTAMP_TZ, Value.TIMESTAMP };

    private static final String[] NAMES = { "CURRENT_DATE", "CURRENT_TIME", "LOCALTIME", "CURRENT_TIMESTAMP",
            "LOCALTIMESTAMP" };

    public static String getName(int function) {
        return NAMES[function];
    }

    private final int function, scale;

    private final TypeInfo type;

    public CurrentDateTimeValueFunction(int function, int scale) {
        this.function = function;
        this.scale = scale;
        if (scale < 0) {
            scale = function >= CURRENT_TIMESTAMP ? ValueTimestamp.DEFAULT_SCALE : ValueTime.DEFAULT_SCALE;
        }
        type = TypeInfo.getTypeInfo(TYPES[function], 0L, scale, null);
    }

    @Override
    public Value getValue(Session session) {
        return session.currentTimestamp().castTo(type, session);
    }

    @Override
    public StringBuilder getSQL(StringBuilder builder, int sqlFlags) {
        builder.append(NAMES[function]);
        if (scale >= 0) {
            builder.append('(').append(scale).append(')');
        }
        return builder;
    }

    @Override
    public boolean isEverything(ExpressionVisitor visitor) {
        switch (visitor.getType()) {
        case ExpressionVisitor.DETERMINISTIC:
        case ExpressionVisitor.READONLY:
            return false;
        }
        return true;
    }

    @Override
    public TypeInfo getType() {
        return type;
    }

    @Override
    public int getCost() {
        return 1;
    }

}
