package ru.curs.celesta.dbutils.adaptors;

import java.util.List;

//TODO: Javadoc
public interface StaticDataAdaptor {

    /**
     * Selects list of static strings in specified order.
     *
     * @param data List of String to select
     * @param columnName name of result column
     * @param orderBy expression to concatenate after "ORDER BY"
     * @return {@link List}
     */
    List<String> selectStaticStrings(
            List<String> data, String columnName, String orderBy
    );

    /**
     * Compares strings by DB.
     *
     * @param left  left string
     * @param right  right string
     * @return {@code -1 if left < right, 0 if left == right, 1 if left > right}
     */
    int compareStrings(String left, String right);
}
