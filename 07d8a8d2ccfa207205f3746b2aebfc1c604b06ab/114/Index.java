/*
 * Copyright 2004-2020 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.index;

import org.h2.command.dml.AllColumnsForPlan;
import org.h2.engine.Session;
import org.h2.message.DbException;
import org.h2.result.Row;
import org.h2.result.RowFactory;
import org.h2.result.SearchRow;
import org.h2.result.SortOrder;
import org.h2.schema.SchemaObject;
import org.h2.table.Column;
import org.h2.table.IndexColumn;
import org.h2.table.Table;
import org.h2.table.TableFilter;

/**
 * An index. Indexes are used to speed up searching data.
 */
//此类提供的查找方法并没有直接提供模糊查询之类的功能，只有范围查询，
//更高级的功能都是通过范围查询获得记录后再在Select那一层通过where条件一一比较和过滤
public interface Index extends SchemaObject {

    /**
     * Get the message to show in a EXPLAIN statement.
     *
     * @return the plan
     */
    //用于EXPLAIN语句，是"模式名.索引名"，跟getCreateSQL()不一样，getCreateSQL()是完整的"CREATE INDEX"
    default String getPlanSQL() {
        return getSQL(false);
    }

    /**
     * Close this index.
     *
     * @param session the session used to write data
     */
    void close(Session session); //在org.h2.store.PageStore.recover()中调用一次，然后在关闭数据库时又调用一次

    /**
     * Add a row to the index.
     *
     * @param session the session to use
     * @param row the row to add
     */
    void add(Session session, Row row);

    /**
     * Remove a row from the index.
     *
     * @param session the session
     * @param row the row
     */
    void remove(Session session, Row row); //删除单行

    /**
     * Update index after row change.
     *
     * @param session the session
     * @param oldRow row before the update
     * @param newRow row after the update
     */
    default void update(Session session, Row oldRow, Row newRow) {
        remove(session, oldRow);
        add(session, newRow);
    }

    /**
     * Returns {@code true} if {@code find()} implementation performs scan over all
     * index, {@code false} if {@code find()} performs the fast lookup.
     *
     * @return {@code true} if {@code find()} implementation performs scan over all
     *         index, {@code false} if {@code find()} performs the fast lookup
     */
    default boolean isFindUsingFullTableScan() {
        return false;
    }

    /**
     * Find a row or a list of rows and create a cursor to iterate over the
     * result.
     *
     * @param session the session
     * @param first the first row, or null for no limit
     * @param last the last row, or null for no limit
     * @return the cursor to iterate over the results
     */
    Cursor find(Session session, SearchRow first, SearchRow last);

    /**
     * Estimate the cost to search for rows given the search mask.
     * There is one element per column in the search mask.
     * For possible search masks, see IndexCondition.
     *
     * @param session the session
     * @param masks per-column comparison bit masks, null means 'always false',
     *              see constants in IndexCondition
     * @param filters all joined table filters
     * @param filter the current table filter index
     * @param sortOrder the sort order
     * @param allColumnsSet the set of all columns
     * @return the estimated cost
     */
    double getCost(Session session, int[] masks, TableFilter[] filters, int filter,
            SortOrder sortOrder, AllColumnsForPlan allColumnsSet);

    /**
     * Remove the index.
     *
     * @param session the session
     */
    void remove(Session session); //删除行并释放page

    /**
     * Remove all rows from the index.
     *
     * @param session the session
     */
    void truncate(Session session); //不释放page，只删除行

    /**
     * Check if the index can directly look up the lowest or highest value of a
     * column.
     *
     * @return true if it can
     */
    default boolean canGetFirstOrLast() {
        return false;
    }

    /**
     * Check if the index can get the next higher value.
     *
     * @return true if it can
     */
    default boolean canFindNext() { //只有PageBtreeIndex返回true
        return false;
    }

    /**
     * Find a row or a list of rows that is larger and create a cursor to
     * iterate over the result.
     *
     * @param session the session
     * @param higherThan the lower limit (excluding)
     * @param last the last row, or null for no limit
     * @return the cursor
     */
//<<<<<<< HEAD
//    //MVSecondaryIndex还不支持这个方法，所以在Select类中还无法支持DistinctQuery优化(2015-05-06更新: MVSecondaryIndex已支持)
//    //比如这样的SQL: select distinct name from test
//    //就算为name字段建立了索引也不行
//    Cursor findNext(Session session, SearchRow higherThan, SearchRow last); //只有org.h2.index.PageBtreeIndex实现了
//=======
    default Cursor findNext(Session session, SearchRow higherThan, SearchRow last) {
        throw DbException.throwInternalError(toString());
    }

    /**
     * Find the first (or last) value of this index. The cursor returned is
     * positioned on the correct row, or on null if no row has been found.
     *
     * @param session the session
     * @param first true if the first (lowest for ascending indexes) or last
     *            value should be returned
     * @return a cursor (never null)
     */
    default Cursor findFirstOrLast(Session session, boolean first) { //用于快束min、max聚合查询
        throw DbException.throwInternalError(toString());
    }

    /**
     * Check if the index needs to be rebuilt.
     * This method is called after opening an index.
     *
     * @return true if a rebuild is required.
     */
    boolean needRebuild(); //构建索引时调用

    /**
     * Get the row count of this table, for the given session.
     *
     * @param session the session
     * @return the row count
     */
    long getRowCount(Session session);

    /**
     * Get the approximated row count for this table.
     *
     * @return the approximated row count
     */
    long getRowCountApproximation();

    /**
     * Get the used disk space for this index.
     *
     * @return the estimated number of bytes
     */
    long getDiskSpaceUsed();

    /**
     * Compare two rows.
     *
     * @param rowData the first row
     * @param compare the second row
     * @return 0 if both rows are equal, -1 if the first row is smaller,
     *         otherwise 1
     */
    int compareRows(SearchRow rowData, SearchRow compare); //只比较索引字段，并不一定是所有字段

    /**
     * Get the index of a column in the list of index columns
     *
     * @param col the column
     * @return the index (0 meaning first column)
     */
    //并不是返回列id，而是索引字段列表中的位置，
    //PageDataIndex、MVPrimaryIndex直接返回-1，因为这两个类严格说不是索引，而是存放主表的原始记录的
    int getColumnIndex(Column col);

    /**
     * Check if the given column is the first for this index
     *
     * @param column the column
     * @return true if the given columns is the first
     */
    boolean isFirstColumn(Column column);

    /**
     * Get the indexed columns as index columns (with ordering information).
     *
     * @return the index columns
     */
    IndexColumn[] getIndexColumns();

    /**
     * Get the indexed columns.
     *
     * @return the columns
     */
    Column[] getColumns();

    /**
     * Get the index type.
     *
     * @return the index type
     */
    IndexType getIndexType();

    /**
     * Get the table on which this index is based.
     *
     * @return the table
     */
    Table getTable();

//    /**
//     * Commit the operation for a row. This is only important for multi-version
//     * indexes. The method is only called if multi-version is enabled.
//     *
//     * @param operation the operation type
//     * @param row the row
//     */
//    //如果使用了multiVersion，在提交时删除row
//    void commit(int operation, Row row); //PageDataIndex、ScanIndex、MultiVersionIndex有实现，其他子类什么都没做

    /**
     * Get the row with the given key.
     *
     * @param session the session
     * @param key the unique key
     * @return the row
     */
    //MVSecondaryIndex类没实现，而是在org.h2.mvstore.db.MVSecondaryIndex.MVStoreCursor.get()中调用MVPrimaryIndex的
    //按key获取主表的完整记录，PageDataIndex、MVPrimaryIndex才有效
    default Row getRow(Session session, long key) {
        throw DbException.getUnsupportedException(toString());
    }

    /**
     * Does this index support lookup by row id?
     *
     * @return true if it does
     */
    //按_ROWID_排序时，直接使用MVPrimaryIndex、PageDataIndex
    //只有org.h2.mvstore.db.MVPrimaryIndex和org.h2.index.PageDataIndex返回true
    default boolean isRowIdIndex() {
        return false;
    }

    /**
     * Can this index iterate over all rows?
     *
     * @return true if it can
     */
    //只看到org.h2.store.PageStore.compact(int)在用
    //HashIndex、NonUniqueHashIndex、FunctionIndex返回false
    default boolean canScan() {
        return true;
    }

    /**
     * Enable or disable the 'sorted insert' optimizations (rows are inserted in
     * ascending or descending order) if applicable for this index
     * implementation.
     *
     * @param sortedInsertMode the new value
     */
    //只在org.h2.index.PageDataLeaf中用到，
    //如insert into IndexTestTable(id, name, address) SORTED values(...)
    //见org.h2.index.PageDataLeaf.addRowTry(Row)，insert加了SORTED后，获取切换点的方式不一样。
    default void setSortedInsertMode(boolean sortedInsertMode) { //PageIndex有覆盖此方法
        // ignore
    }

    RowFactory getRowFactory();
}
