/*
 * Copyright 2004-2020 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.h2.api.ErrorCode;
import org.h2.command.Parser;
import org.h2.command.dml.AllColumnsForPlan;
import org.h2.command.dml.Select;
import org.h2.engine.Database;
import org.h2.engine.Right;
import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.expression.condition.Comparison;
import org.h2.expression.condition.ConditionAndOr;
import org.h2.index.Index;
import org.h2.index.IndexCondition;
import org.h2.index.IndexCursor;
import org.h2.message.DbException;
import org.h2.result.Row;
import org.h2.result.SearchRow;
import org.h2.result.SortOrder;
import org.h2.util.StringUtils;
import org.h2.util.Utils;
import org.h2.value.Value;
import org.h2.value.ValueLong;
import org.h2.value.ValueNull;

/**
 * A table filter represents a table that is used in a query. There is one such
 * object whenever a table (or view) is used in a query. For example the
 * following query has 2 table filters: SELECT * FROM TEST T1, TEST T2.
 */
public class TableFilter implements ColumnResolver {

    private static final int BEFORE_FIRST = 0, FOUND = 1, AFTER_LAST = 2, NULL_ROW = 3;

    /**
     * Comparator that uses order in FROM clause as a sort key.
     */
    public static final Comparator<TableFilter> ORDER_IN_FROM_COMPARATOR =
            Comparator.comparing(TableFilter::getOrderInFrom);

    /**
     * A visitor that sets joinOuterIndirect to true.
     */
    private static final TableFilterVisitor JOI_VISITOR = f -> f.joinOuterIndirect = true;

    /**
     * Whether this is a direct or indirect (nested) outer join
     */
    protected boolean joinOuterIndirect;

    private Session session;

    private final Table table;
    private final Select select; //通常只有执行select语句时不为null，update、delete时为null
    private String alias;
    private Index index;
    private final IndexHints indexHints;
    private int[] masks;
    private int scanCount;
    private boolean evaluatable;

    /**
     * Indicates that this filter is used in the plan.
     */
    private boolean used;

    /**
     * The filter used to walk through the index.
     */
    private final IndexCursor cursor;

    /**
     * The index conditions used for direct index lookup (start or end).
     */
//<<<<<<< HEAD
//    //由where条件生成，见org.h2.command.dml.Select.prepare()的condition.createIndexConditions(session, f);
//    //索引条件是用来快速定位索引的开始和结束位置的，比如有一个id的索引字段，值从1到10，
//    //现在有一个where id>3 and id<7的条件，那么在查找前，索引就事先定位到3和7的位置了
//    //见org.h2.index.IndexCursor.find(Session, ArrayList<IndexCondition>)
//    //这8种类型的表达式能建立索引条件
//    //Comparison、CompareLike、ConditionIn、ConditionInSelect、ConditionInConstantSet、
//    //ConditionAndOr、ExpressionColumn、ValueExpression
//    private final ArrayList<IndexCondition> indexConditions = New.arrayList();
//=======
    private final ArrayList<IndexCondition> indexConditions = Utils.newSmallArrayList();

    /**
     * Whether new window conditions should not be accepted.
     */
    private boolean doneWithIndexConditions;

    /**
     * Additional conditions that can't be used for index lookup, but for row
     * filter for this table (ID=ID, NAME LIKE '%X%')
     */
    //如果是单表，那么跟fullCondition一样是整个where条件
    //join的情况下只包含属于本表的表达式
    private Expression filterCondition; //只在addFilterCondition方法中赋值

    /**
     * The complete join condition.
     */
    private Expression joinCondition; //on 条件, 只在addFilterCondition方法中赋值

    private SearchRow currentSearchRow;
    private Row current;
    private int state;

    /**
     * The joined table (if there is one).
     */
    private TableFilter join;

    /**
     * Whether this is an outer join.
     */
    //也就是此表的左边是不是outer join，比如t1 left outer join t2，那么t2的joinOuter是true，但是t1的joinOuter是false
    private boolean joinOuter;

    /**
     * The nested joined table (if there is one).
     */
    private TableFilter nestedJoin;

    /**
     * Map of common join columns, used for NATURAL joins and USING clause of
     * other joins. This map preserves original order of the columns.
     */
    private LinkedHashMap<Column, Column> commonJoinColumns;

    private TableFilter commonJoinColumnsFilter;
    private ArrayList<Column> commonJoinColumnsToExclude;
    private boolean foundOne;
    //在org.h2.command.dml.Select.preparePlan()中设，完整的where条件
    //这个字段只是在选择索引的过程中有用，filterCondition的值通过它传递
    //单表时fullCondition虽然不为null，但是没有用处，单表时filterCondition为null，
    //除非把EARLY_FILTER参数设为true，这样filterCondition就不为null了，在next中就过滤掉行，
    //如果filterCondition计算是true的话，在Select类的queryXXX方法中又计算一次condition
    private Expression fullCondition;
    private final int hashCode;
    private final int orderInFrom;

    /**
     * Map of derived column names. This map preserves original order of the
     * columns.
     */
    private LinkedHashMap<Column, String> derivedColumnMap;

    /**
     * Create a new table filter object.
     *
     * @param session the session
     * @param table the table from where to read data
     * @param alias the alias name
     * @param rightsChecked true if rights are already checked
     * @param select the select statement
     * @param orderInFrom original order number (index) of this table filter in
     * @param indexHints the index hints to be used by the query planner
     */
    public TableFilter(Session session, Table table, String alias,
            boolean rightsChecked, Select select, int orderInFrom, IndexHints indexHints) {
        this.session = session;
        this.table = table;
        this.alias = alias;
        this.select = select;
        this.cursor = new IndexCursor();
        if (!rightsChecked) {
            session.getUser().checkRight(table, Right.SELECT);
        }
        hashCode = session.nextObjectId();
        this.orderInFrom = orderInFrom;
        this.indexHints = indexHints;
    }

    /**
     * Get the order number (index) of this table filter in the "from" clause of
     * the query.
     *
     * @return the index (0, 1, 2,...)
     */
    public int getOrderInFrom() {
        return orderInFrom;
    }

    public IndexCursor getIndexCursor() {
        return cursor;
    }

    @Override
    public Select getSelect() {
        return select;
    }

    public Table getTable() {
        return table;
    }

    /**
     * Lock the table. This will also lock joined tables.
     *
     * @param s the session
     * @param exclusive true if an exclusive lock is required
     * @param forceLockEvenInMvcc lock even in the MVCC mode
     */
    public void lock(Session s, boolean exclusive, boolean forceLockEvenInMvcc) {
        table.lock(s, exclusive, forceLockEvenInMvcc);
        if (join != null) {
            join.lock(s, exclusive, forceLockEvenInMvcc);
        }
    }

    /**
     * Get the best plan item (index, cost) to use for the current join
     * order.
     *
     * @param s the session
     * @param filters all joined table filters
     * @param filter the current table filter index
     * @param allColumnsSet the set of all columns
     * @return the best plan item
     */
//<<<<<<< HEAD
//    //对于Delete、Update是在prepare()时直接进来，
//    //而Select要prepare()=>preparePlan()=>Optimizer.optimize()=>Plan.calculateCost(Session)
//    public PlanItem getBestPlanItem(Session s, int level) {
//        PlanItem item;
//        //没有索引条件时直接走扫描索引(RegularTable是PageDataIndex或ScanIndex(内存)，而MVTable是MVPrimaryIndex)
//        if (indexConditions.size() == 0) {
//            item = new PlanItem();
//            item.setIndex(table.getScanIndex(s));
//            item.cost = item.getIndex().getCost(s, null, null, null);
//        } else {
//            int len = table.getColumns().length;
//            int[] masks = new int[len]; //对应表的所有字段，只有其中的索引字段才有值，其他的不设置，默认为0
//            for (IndexCondition condition : indexConditions) {
//            	//如果IndexCondition是expression或expressionList，只有ExpressionColumn类型有可能返回false
//            	//如果IndexCondition是expressionQuery，expressionQuery是Select、SelectUnion类型有可能返回false
//            	//其他都返回true
//                if (condition.isEvaluatable()) {
//                    //H2数据库目前不支持在or表达式上面建立索引条件，例如id> 40 or name<'b3'，就算id和name字段各自有索引也不会选择它们
//                	//对于ConditionAndOr的场景才会出现indexConditions.size>1
//                	//而ConditionAndOr只处理“AND”的场景而不管"OR"的场景
//                	//所以当多个indexCondition通过AND组合时，只要其中一个是false，显然就没有必要再管其他的indexCondition
//                	//这时把masks设为null
//                    if (condition.isAlwaysFalse()) { //如where id>40 AND 3<2(在condition.optimize时被优化成false了)
//                        masks = null;
//                        break;
//                    }
//                    //condition.getColumn()不可能为null，因为目的是要选合适的索引，而索引建立在字段之上
//                    //所以IndexCondition中的column变量不可能是null
//                    int id = condition.getColumn().getColumnId();
//                    if (id >= 0) {
//                    	//多个IndexCondition可能是同一个字段
//                    	//如id>1 and id <10，这样masks[id]最后就变成IndexCondition.RANGE了
//                        masks[id] |= condition.getMask(indexConditions);
//                    }
//                }
//            }
//            SortOrder sortOrder = null;
//            if (select != null) {
//                sortOrder = select.getSortOrder();
//            }
//            item = table.getBestPlanItem(s, masks, this, sortOrder);
//            // The more index conditions, the earlier the table.
//            // This is to ensure joins without indexes run quickly:
//            // x (x.a=10); y (x.b=y.b) - see issue 113
//            //level越大，item.cost就减去一个越小的值，所以join的cost越大
//            //索引条件越多，cost越小
//            item.cost -= item.cost * indexConditions.size() / 100 / level;
//=======
    public PlanItem getBestPlanItem(Session s, TableFilter[] filters, int filter,
            AllColumnsForPlan allColumnsSet) {
        PlanItem item1 = null;
        SortOrder sortOrder = null;
        if (select != null) {
            sortOrder = select.getSortOrder();
        }
        if (indexConditions.isEmpty()) {
            item1 = new PlanItem();
            item1.setIndex(table.getScanIndex(s, null, filters, filter,
                    sortOrder, allColumnsSet));
            item1.cost = item1.getIndex().getCost(s, null, filters, filter,
                    sortOrder, allColumnsSet);
        }
        int len = table.getColumns().length;
        int[] masks = new int[len];
        for (IndexCondition condition : indexConditions) {
            if (condition.isEvaluatable()) {
                if (condition.isAlwaysFalse()) {
                    masks = null;
                    break;
                }
                int id = condition.getColumn().getColumnId();
                if (id >= 0) {
                    masks[id] |= condition.getMask(indexConditions);
                }
            }
        }
        PlanItem item = table.getBestPlanItem(s, masks, filters, filter, sortOrder, allColumnsSet);
        item.setMasks(masks);
        // The more index conditions, the earlier the table.
        // This is to ensure joins without indexes run quickly:
        // x (x.a=10); y (x.b=y.b) - see issue 113
        item.cost -= item.cost * indexConditions.size() / 100 / (filter + 1);

        if (item1 != null && item1.cost < item.cost) {
            item = item1;
        }

        if (nestedJoin != null) {
            setEvaluatable(true);
            item.setNestedJoinPlan(nestedJoin.getBestPlanItem(s, filters, filter, allColumnsSet));
            // TODO optimizer: calculate cost of a join: should use separate
            // expected row number and lookup cost
            item.cost += item.cost * item.getNestedJoinPlan().cost;
        }
        if (join != null) {
            setEvaluatable(true);
            do {
                filter++;
            } while (filters[filter] != join);
            item.setJoinPlan(join.getBestPlanItem(s, filters, filter, allColumnsSet));
            // TODO optimizer: calculate cost of a join: should use separate
            // expected row number and lookup cost
            item.cost += item.cost * item.getJoinPlan().cost;
        }
        return item;
    }

    /**
     * Set what plan item (index, cost, masks) to use.
     *
     * @param item the plan item
     */
    public void setPlanItem(PlanItem item) {
        if (item == null) {
            // invalid plan, most likely because a column wasn't found
            // this will result in an exception later on
            return;
        }
        setIndex(item.getIndex());
        masks = item.getMasks();
        if (nestedJoin != null) {
            if (item.getNestedJoinPlan() != null) {
                nestedJoin.setPlanItem(item.getNestedJoinPlan());
            } else {
                nestedJoin.setScanIndexes();
            }
        }
        if (join != null) {
            if (item.getJoinPlan() != null) {
                join.setPlanItem(item.getJoinPlan());
            } else {
                join.setScanIndexes();
            }
        }
    }

    /**
     * Set all missing indexes to scan indexes recursively.
     */
    private void setScanIndexes() {
        if (index == null) {
            setIndex(table.getScanIndex(session));
        }
        if (join != null) {
            join.setScanIndexes();
        }
        if (nestedJoin != null) {
            nestedJoin.setScanIndexes();
        }
    }

    /**
     * Prepare reading rows. This method will remove all index conditions that
     * can not be used, and optimize the conditions.
     */
    public void prepare() {
        // forget all unused index conditions
        // the indexConditions list may be modified here
    	//如
    	//create table IF NOT EXISTS DeleteTest(id int, name varchar(500), b boolean)
    	//delete from DeleteTest where b
    	//按字段b删除，实际上就是删除b=true的记录
    	//如果没有为字段b建立索引，就在这里删除这个无用条件
    	//这样在org.h2.index.IndexCursor.find(Session, ArrayList<IndexCondition>)中就不会计算无用的索引
        //另外，会删除那些与当些索引字段无关的indexCondition
        //例如，为id和name字段都建了索引，
        //如果条件是:where id>2 and name='a1'
        //此时因为等号的代价更低，所以选择name对应的索引，
        //indexConditions.size()就是2，但是会删除id>2这个indexCondition
        for (int i = 0; i < indexConditions.size(); i++) {
            IndexCondition condition = indexConditions.get(i);
            if (!condition.isAlwaysFalse()) {
                Column col = condition.getColumn();
                if (col.getColumnId() >= 0) {
                    if (index.getColumnIndex(col) < 0) {
                        indexConditions.remove(i);
                        i--;
                    }
                }
            }
        }
        if (nestedJoin != null) {
            if (nestedJoin == this) {
                DbException.throwInternalError("self join");
            }
            nestedJoin.prepare();
        }
        if (join != null) {
            if (join == this) {
                DbException.throwInternalError("self join");
            }
            join.prepare();
        }
        if (filterCondition != null) {
            filterCondition = filterCondition.optimize(session);
        }
        if (joinCondition != null) {
            joinCondition = joinCondition.optimize(session);
        }
    }

    /**
     * Start the query. This will reset the scan counts.
     *
     * @param s the session
     */
    public void startQuery(Session s) {
        this.session = s;
        scanCount = 0;
        if (nestedJoin != null) {
            nestedJoin.startQuery(s);
        }
        if (join != null) {
            join.startQuery(s);
        }
    }

    /**
     * Reset to the current position.
     */
    public void reset() {
        if (nestedJoin != null) {
            nestedJoin.reset();
        }
        if (join != null) {
            join.reset();
        }
        state = BEFORE_FIRST;
        foundOne = false;
    }

    /**
     * Check if there are more rows to read.
     *
     * @return true if there are
     */
    //在from或join后面加括号的都是nestedJoin
    //如SELECT * FROM (JoinTest1 LEFT OUTER JOIN (JoinTest2))

    //TableFilter(SYSTEM_JOIN_xxx).nestedJoin => TableFilter(JoinTest1)
    //TableFilter(SYSTEM_JOIN_xxx).join => null

    //TableFilter(JoinTest1).nestedJoin => null
    //TableFilter(JoinTest1).join => TableFilter(SYSTEM_JOIN_yyy)

    //TableFilter(SYSTEM_JOIN_yyy).nestedJoin => TableFilter(JoinTest2)
    //TableFilter(SYSTEM_JOIN_yyy).join => null

    //TableFilter(JoinTest2).nestedJoin => null
    //TableFilter(JoinTest2).join => null

    //同一个TableFilter的join和nestedJoin有可能会同时不为null，如下
    //SELECT rownum, * FROM (JoinTest1) LEFT OUTER JOIN JoinTest2 ON id>30
    //TableFilter(SYSTEM_JOIN_xxx).nestedJoin => TableFilter(JoinTest1)
    //TableFilter(SYSTEM_JOIN_xxx).join => TableFilter(JoinTest2)
    public boolean next() {
        if (state == AFTER_LAST) {
            return false;
        } else if (state == BEFORE_FIRST) {
            cursor.find(session, indexConditions);
            if (!cursor.isAlwaysFalse()) {
                if (nestedJoin != null) {
                    nestedJoin.reset();
                }
                if (join != null) {
                    join.reset();
                }
            }
        } else {
            // state == FOUND || NULL_ROW
            // the last row was ok - try next row of the join
            if (join != null && join.next()) { //join表移动，主表不动，如果join上次是NULL_ROW,那么主表要往下移
                return true;
            }
        }
        while (true) {
            // go to the next row
            if (state == NULL_ROW) {
                break;
            }
            if (cursor.isAlwaysFalse()) {
            	//当OPTIMIZE_IS_NULL设为false时，cursor.isAlwaysFalse()是true
                //对于这样的SELECT rownum, * FROM JoinTest1 LEFT OUTER JOIN JoinTest2 ON name2=null
                //还是会返回JoinTest1的所有记录，JoinTest2中的全为null
                state = AFTER_LAST;
            } else if (nestedJoin != null) {
                if (state == BEFORE_FIRST) {
                    state = FOUND;
                }
            } else {
                if ((++scanCount & 4095) == 0) {
                    checkTimeout();
                }
                if (cursor.next()) {
                    currentSearchRow = cursor.getSearchRow();
                    current = null;
                    state = FOUND;
                } else {
                    state = AFTER_LAST;
                }
            }
            //nestedJoin就是在表名前后加括号
            //如sql = "SELECT rownum, * FROM JoinTest1 LEFT OUTER JOIN (JoinTest2) ON id>30";
        	//nestedJoin是(JoinTest2)
            //如sql = "SELECT rownum, * FROM (JoinTest1) LEFT OUTER JOIN JoinTest2 ON id>30";
        	//nestedJoin是(JoinTest1)
            if (nestedJoin != null && state == FOUND) {
                if (!nestedJoin.next()) {
                    state = AFTER_LAST;
                    if (joinOuter && !foundOne) {
                        // possibly null row
                    	//如sql = "SELECT rownum, * FROM JoinTest1 LEFT OUTER JOIN (JoinTest2) ON id>30";
                    	//nestedJoin是(JoinTest2)，joinOuter是true
                    } else {
                    	//如sql = "SELECT rownum, * FROM (JoinTest1) LEFT OUTER JOIN JoinTest2 ON id>30";
                    	//nestedJoin是(JoinTest1)，joinOuter是false
                        continue;
                    }
                }
            }
            // if no more rows found, try the null row (for outer joins only)
            if (state == AFTER_LAST) {
            	//分两种情况:
            	//1. 正常情况结束， 上次没有找到，且是外部连接，此时是一个悬浮记录，把右边的字段都用null表示
            	//2. on条件总是false，使得还没有遍历表state就变成了AFTER_LAST
            	//当OPTIMIZE_IS_NULL设为false时，cursor.isAlwaysFalse()是true
                //对于这样的SELECT rownum, * FROM JoinTest1 LEFT OUTER JOIN JoinTest2 ON name2=null
                //还是会返回JoinTest1的所有记录，JoinTest2中的全为null
                if (joinOuter && !foundOne) {
                    setNullRow();
                } else {
                    break;
                }
            }
            if (!isOk(filterCondition)) {
                continue;
            }
            //对于SELECT rownum, * FROM JoinTest1 LEFT OUTER JOIN JoinTest2 ON id>30
            //id>30中的id虽然是JoinTest1的，但是这个joinCondition是加到JoinTest2对应的TableFilter中
            //所以可以做一个优化，当判断joinCondition中不包含JoinTest2的字段时，joinConditionOk肯定是false，
            //这样就不用一行行再去遍历JoinTest2表了，直接调用setNullRow()，然后退出循环
            boolean joinConditionOk = isOk(joinCondition);
            if (state == FOUND) {
                if (joinConditionOk) {
                    foundOne = true;
                } else {
                    continue;
                }
            }
            if (join != null) {
                join.reset();
                if (!join.next()) {
                    continue;
                }
            }
            // check if it's ok
            if (state == NULL_ROW || joinConditionOk) {
                return true;
            }
        }
        state = AFTER_LAST;
        return false;
    }

    /**
     * Set the state of this and all nested tables to the NULL row.
     */
    protected void setNullRow() {
        state = NULL_ROW;
        current = table.getNullRow();
        currentSearchRow = current;
        if (nestedJoin != null) {
            nestedJoin.visit(TableFilter::setNullRow);
        }
    }

    private void checkTimeout() {
        session.checkCanceled();
    }

    /**
     * Whether the current value of the condition is true, or there is no
     * condition.
     *
     * @param condition the condition (null for no condition)
     * @return true if yes
     */
    boolean isOk(Expression condition) {
        return condition == null || condition.getBooleanValue(session);
    }

    /**
     * Get the current row.
     *
     * @return the current row, or null
     */
    public Row get() {
        if (current == null && currentSearchRow != null) {
            current = cursor.get();
        }
        return current;
    }

    /**
     * Set the current row.
     *
     * @param current the current row
     */
    public void set(Row current) {
        this.current = current;
        this.currentSearchRow = current;
    }

    /**
     * Get the table alias name. If no alias is specified, the table name is
     * returned.
     *
     * @return the alias name
     */
    @Override
    public String getTableAlias() {
        if (alias != null) {
            return alias;
        }
        return table.getName();
    }

    /**
     * Add an index condition.
     *
     * @param condition the index condition
     */
    public void addIndexCondition(IndexCondition condition) {
        if (!doneWithIndexConditions) {
            indexConditions.add(condition);
        }
    }

    /**
     * Used to reject all additional index conditions.
     */
    public void doneWithIndexConditions() {
        this.doneWithIndexConditions = true;
    }

    /**
     * Add a filter condition.
     *
     * @param condition the condition
     * @param isJoin if this is in fact a join condition
     */
    public void addFilterCondition(Expression condition, boolean isJoin) {
        if (isJoin) {
            if (joinCondition == null) {
                joinCondition = condition;
            } else {
                joinCondition = new ConditionAndOr(ConditionAndOr.AND,
                        joinCondition, condition);
            }
        } else {
            if (filterCondition == null) {
                filterCondition = condition;
            } else {
                filterCondition = new ConditionAndOr(ConditionAndOr.AND,
                        filterCondition, condition);
            }
        }
    }

    /**
     * Add a joined table.
     *
     * @param filter the joined table filter
     * @param outer if this is an outer join
     * @param on the join condition
     */
//<<<<<<< HEAD
//    //没有发现outer、nested同时为true的
//    //on这个joinCondition是加到filter参数对应的TableFilter中，也就是右表，而不是左表
//    public void addJoin(TableFilter filter, boolean outer, boolean nested, final Expression on) {
//    	//给on中的ExpressionColumn设置columnResolver，
//    	//TableFilter实现了ColumnResolver接口，所以ExpressionColumn的columnResolver实际上就是TableFilter对象
//    	//另外，下面的两个visit能查出多个Table之间的列是否同名
//=======
    public void addJoin(TableFilter filter, boolean outer, Expression on) {
        if (on != null) {
            on.mapColumns(this, 0, Expression.MAP_INITIAL);
            TableFilterVisitor visitor = new MapColumnsVisitor(on);
            visit(visitor);
            filter.visit(visitor);
        }
//<<<<<<< HEAD
//        if (nested && session.getDatabase().getSettings().nestedJoins) {
//            if (nestedJoin != null) {
//                throw DbException.throwInternalError();
//            }
//            //很少有嵌套join，只在org.h2.command.Parser.getNested(TableFilter)看到有
//            //被一个DualTable(一个min和max都为1的RangeTable)嵌套
//            //还有一种情况是先LEFT OUTER JOIN再NATURAL JOIN
//            //如from JoinTest1 LEFT OUTER JOIN JoinTest3 NATURAL JOIN JoinTest2
//            nestedJoin = filter;
//=======
        if (join == null) {
            join = filter;
            filter.joinOuter = outer;
            if (outer) {
                filter.visit(JOI_VISITOR);
            }
            if (on != null) {
                filter.mapAndAddFilter(on);
            }
        } else {
//<<<<<<< HEAD
//            if (join == null) {
//                join = filter;
//                filter.joinOuter = outer;
//                if (session.getDatabase().getSettings().nestedJoins) {
//                    if (outer) {
//                    	//filter自身和filter的nestedJoin和join字段对应的filter的joinOuterIndirect都为true
//                    	//nestedJoin和join字段对应的filter继续递归所有的nestedJoin和join字段
//                        filter.visit(new TableFilterVisitor() {
//                            @Override
//                            public void accept(TableFilter f) {
//                                f.joinOuterIndirect = true;
//                            }
//                        });
//                    }
//                } else {
//                    if (outer) {
//                        //当nestedJoins为false时，nestedJoin字段不会有值，都是join字段有值，
//                        // convert all inner joins on the right hand side to outer joins
//                        TableFilter f = filter.join;
//                        while (f != null) {
//                            f.joinOuter = true;
//                            f = f.join;
//                        }
//                    }
//                }
//                if (on != null) {
//                    filter.mapAndAddFilter(on);
//                }
//            } else {
//                join.addJoin(filter, outer, nested, on);
//            }
//=======
            join.addJoin(filter, outer, on);
        }
    }

    /**
     * Set a nested joined table.
     *
     * @param filter the joined table filter
     */
    public void setNestedJoin(TableFilter filter) {
        nestedJoin = filter;
    }

    /**
     * Map the columns and add the join condition.
     *
     * @param on the condition
     */
    public void mapAndAddFilter(Expression on) {
        on.mapColumns(this, 0, Expression.MAP_INITIAL);
        addFilterCondition(on, true);
        if (nestedJoin != null) {
            on.mapColumns(nestedJoin, 0, Expression.MAP_INITIAL);
        }
        if (join != null) {
            join.mapAndAddFilter(on);
        }
    }

    /**
     * Create the index conditions for this filter if needed.
     */
    public void createIndexConditions() {
        if (joinCondition != null) {
            joinCondition = joinCondition.optimize(session);
            joinCondition.createIndexConditions(session, this);
            if (nestedJoin != null) {
                joinCondition.createIndexConditions(session, nestedJoin);
            }
        }
        if (join != null) {
            join.createIndexConditions();
        }
        if (nestedJoin != null) {
            nestedJoin.createIndexConditions();
        }
    }

    public TableFilter getJoin() {
        return join;
    }

    /**
     * Whether this is an outer joined table.
     *
     * @return true if it is
     */
    public boolean isJoinOuter() {
        return joinOuter;
    }

    /**
     * Whether this is indirectly an outer joined table (nested within an inner
     * join).
     *
     * @return true if it is
     */
    public boolean isJoinOuterIndirect() {
        return joinOuterIndirect;
    }

    /**
     * Get the query execution plan text to use for this table filter and append
     * it to the specified builder.
     *
     * @param builder string builder to append to
     * @param isJoin if this is a joined table
     * @param alwaysQuote quote all identifiers
     * @return the specified builder
     */
    public StringBuilder getPlanSQL(StringBuilder builder, boolean isJoin, boolean alwaysQuote) {
        if (isJoin) {
            if (joinOuter) {
                builder.append("LEFT OUTER JOIN ");
            } else {
                builder.append("INNER JOIN ");
            }
        }
        if (nestedJoin != null) {
            StringBuilder buffNested = new StringBuilder();
            TableFilter n = nestedJoin;
            do {
                n.getPlanSQL(buffNested, n != nestedJoin, alwaysQuote).append('\n');
                n = n.getJoin();
            } while (n != null);
            String nested = buffNested.toString();
            boolean enclose = !nested.startsWith("(");
            if (enclose) {
                builder.append("(\n");
            }
            StringUtils.indent(builder, nested, 4, false);
            if (enclose) {
                builder.append(')');
            }
            if (isJoin) {
                builder.append(" ON ");
                if (joinCondition == null) {
                    // need to have a ON expression,
                    // otherwise the nesting is unclear
                    builder.append("1=1");
                } else {
                    joinCondition.getUnenclosedSQL(builder, alwaysQuote);
                }
            }
            return builder;
        }
        if (table.isView() && ((TableView) table).isRecursive()) {
            table.getSchema().getSQL(builder, alwaysQuote).append('.');
            Parser.quoteIdentifier(builder, table.getName(), alwaysQuote);
        } else {
            table.getSQL(builder, alwaysQuote);
        }
        if (table.isView() && ((TableView) table).isInvalid()) {
            throw DbException.get(ErrorCode.VIEW_IS_INVALID_2, table.getName(), "not compiled");
        }
        if (alias != null) {
            builder.append(' ');
            Parser.quoteIdentifier(builder, alias, alwaysQuote);
            if (derivedColumnMap != null) {
                builder.append('(');
                boolean f = false;
                for (String name : derivedColumnMap.values()) {
                    if (f) {
                        builder.append(", ");
                    }
                    f = true;
                    Parser.quoteIdentifier(builder, name, alwaysQuote);
                }
                builder.append(')');
            }
        }
        if (indexHints != null) {
            builder.append(" USE INDEX (");
            boolean first = true;
            for (String index : indexHints.getAllowedIndexes()) {
                if (!first) {
                    builder.append(", ");
                } else {
                    first = false;
                }
                Parser.quoteIdentifier(builder, index, alwaysQuote);
            }
            builder.append(")");
        }
        if (index != null) {
            builder.append('\n');
            StringBuilder planBuilder = new StringBuilder();
            planBuilder.append(index.getPlanSQL());
            if (!indexConditions.isEmpty()) {
                planBuilder.append(": ");
                for (int i = 0, size = indexConditions.size(); i < size; i++) {
                    if (i > 0) {
                        planBuilder.append("\n    AND ");
                    }
                    planBuilder.append(indexConditions.get(i).getSQL(false));
                }
            }
            String plan = StringUtils.quoteRemarkSQL(planBuilder.toString());
            planBuilder.setLength(0);
            planBuilder.append("/* ").append(plan);
            if (plan.indexOf('\n') >= 0) {
                planBuilder.append('\n');
            }
            StringUtils.indent(builder, planBuilder.append(" */").toString(), 4, false);
        }
        if (isJoin) {
            builder.append("\n    ON ");
            if (joinCondition == null) {
                // need to have a ON expression, otherwise the nesting is
                // unclear
                builder.append("1=1");
            } else {
                joinCondition.getUnenclosedSQL(builder, alwaysQuote);
            }
        }
        if (filterCondition != null) {
            builder.append('\n');
            String condition = StringUtils.unEnclose(filterCondition.getSQL(false));
            condition = "/* WHERE " + StringUtils.quoteRemarkSQL(condition) + "\n*/";
            StringUtils.indent(builder, condition, 4, false);
        }
        if (scanCount > 0) {
            builder.append("\n    /* scanCount: ").append(scanCount).append(" */");
        }
        return builder;
    }

    /**
     * Remove all index conditions that are not used by the current index.
     */
    void removeUnusableIndexConditions() {
        // the indexConditions list may be modified here
        for (int i = 0; i < indexConditions.size(); i++) {
            IndexCondition cond = indexConditions.get(i);
            if (!cond.isEvaluatable()) {
                indexConditions.remove(i--);
            }
        }
    }

    public int[] getMasks() {
        return masks;
    }

    public ArrayList<IndexCondition> getIndexConditions() {
        return indexConditions;
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
        cursor.setIndex(index);
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isUsed() {
        return used;
    }

    /**
     * Set the session of this table filter.
     *
     * @param session the new session
     */
    void setSession(Session session) {
        this.session = session;
    }

    /**
     * Remove the joined table
     */
    public void removeJoin() {
        this.join = null;
    }

    public Expression getJoinCondition() {
        return joinCondition;
    }

    /**
     * Remove the join condition.
     */
    public void removeJoinCondition() {
        this.joinCondition = null;
    }

    public Expression getFilterCondition() {
        return filterCondition;
    }

    /**
     * Remove the filter condition.
     */
    public void removeFilterCondition() {
        this.filterCondition = null;
    }

    public void setFullCondition(Expression condition) {
        this.fullCondition = condition;
        if (join != null) {
            join.setFullCondition(condition);
        }
    }

    /**
     * Optimize the full condition. This will add the full condition to the
     * filter condition.
     */
    void optimizeFullCondition() {
        if (!joinOuter && fullCondition != null) {
            fullCondition.addFilterConditions(this);
            if (nestedJoin != null) {
                nestedJoin.optimizeFullCondition();
            }
            if (join != null) {
                join.optimizeFullCondition();
            }
        }
    }

    /**
     * Update the filter and join conditions of this and all joined tables with
     * the information that the given table filter and all nested filter can now
     * return rows or not.
     *
     * @param filter the table filter
     * @param b the new flag
     */
    public void setEvaluatable(TableFilter filter, boolean b) {
        filter.setEvaluatable(b);
        if (filterCondition != null) {
            filterCondition.setEvaluatable(filter, b);
        }
        if (joinCondition != null) {
            joinCondition.setEvaluatable(filter, b);
        }
        if (nestedJoin != null) {
            // don't enable / disable the nested join filters
            // if enabling a filter in a joined filter
            if (this == filter) {
                nestedJoin.setEvaluatable(nestedJoin, b);
            }
        }
        if (join != null) {
            join.setEvaluatable(filter, b);
        }
    }

    public void setEvaluatable(boolean evaluatable) {
        this.evaluatable = evaluatable;
    }

    @Override
    public String getSchemaName() {
        if (alias == null && !(table instanceof VirtualTable)) {
            return table.getSchema().getName();
        }
        return null;
    }

    @Override
    public Column[] getColumns() {
        return table.getColumns();
    }

    @Override
    public Column findColumn(String name) {
        HashMap<Column, String> map = derivedColumnMap;
        if (map != null) {
            Database db = session.getDatabase();
            for (Entry<Column, String> entry : derivedColumnMap.entrySet()) {
                if (db.equalsIdentifiers(entry.getValue(), name)) {
                    return entry.getKey();
                }
            }
            return null;
        }
        return table.findColumn(name);
    }

    @Override
    public String getColumnName(Column column) {
        HashMap<Column, String> map = derivedColumnMap;
        return map != null ? map.get(column) : column.getName();
    }

    @Override
    public boolean hasDerivedColumnList() {
        return derivedColumnMap != null;
    }

    /**
     * Get the column with the given name.
     *
     * @param columnName
     *            the column name
     * @param ifExists
     *            if (@code true) return {@code null} if column does not exist
     * @return the column
     * @throws DbException
     *             if the column was not found and {@code ifExists} is
     *             {@code false}
     */
    public Column getColumn(String columnName, boolean ifExists) {
        HashMap<Column, String> map = derivedColumnMap;
        if (map != null) {
            Database database = session.getDatabase();
            for (Entry<Column, String> entry : map.entrySet()) {
                if (database.equalsIdentifiers(columnName, entry.getValue())) {
                    return entry.getKey();
                }
            }
            if (ifExists) {
                return null;
            } else {
                throw DbException.get(ErrorCode.COLUMN_NOT_FOUND_1, columnName);
            }
        }
        return table.getColumn(columnName, ifExists);
    }

    /**
     * Get the system columns that this table understands. This is used for
     * compatibility with other databases. The columns are only returned if the
     * current mode supports system columns.
     *
     * @return the system columns
     */
    @Override
    public Column[] getSystemColumns() {
        if (!session.getDatabase().getMode().systemColumns) {
            return null;
        }
        Column[] sys = new Column[3];
        sys[0] = new Column("oid", Value.INT);
        sys[0].setTable(table, 0);
        sys[1] = new Column("ctid", Value.VARCHAR);
        sys[1].setTable(table, 0);
        sys[2] = new Column("CTID", Value.VARCHAR);
        sys[2].setTable(table, 0);
        return sys;
    }

    @Override
    public Column getRowIdColumn() {
        return table.getRowIdColumn();
    }

    @Override
    public Value getValue(Column column) {
        if (currentSearchRow == null) {
            return null;
        }
        int columnId = column.getColumnId();
        if (columnId == -1) {
            return ValueLong.get(currentSearchRow.getKey());
        }
        if (current == null) {
            Value v = currentSearchRow.getValue(columnId);
            if (v != null) {
                return v;
            }
            current = cursor.get();
            if (current == null) {
                return ValueNull.INSTANCE;
            }
        }
        return current.getValue(columnId);
    }

    @Override
    public TableFilter getTableFilter() {
        return this;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Set derived column list.
     *
     * @param derivedColumnNames names of derived columns
     */
    public void setDerivedColumns(ArrayList<String> derivedColumnNames) {
        Column[] columns = getColumns();
        int count = columns.length;
        if (count != derivedColumnNames.size()) {
            throw DbException.get(ErrorCode.COLUMN_COUNT_DOES_NOT_MATCH);
        }
        LinkedHashMap<Column, String> map = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            String alias = derivedColumnNames.get(i);
            for (int j = 0; j < i; j++) {
                if (alias.equals(derivedColumnNames.get(j))) {
                    throw DbException.get(ErrorCode.DUPLICATE_COLUMN_NAME_1, alias);
                }
            }
            map.put(columns[i], alias);
        }
        this.derivedColumnMap = map;
    }

    @Override
    public String toString() {
        //return alias != null ? alias : table.toString();
        
        StringBuilder s = new StringBuilder();
        toS(this, s, "\t");
        return s.toString();
    }

    private static void toS(TableFilter t, StringBuilder s, String tabs) {
        s.append("TableFilter[").append(t.alias != null ? t.alias : t.table.toString()).append("]");
        if (t.nestedJoin != null) {
            s.append("\n").append(tabs).append("=>nestedJoin=");
            toS(t.nestedJoin, s, tabs + "\t");
        }
        if (t.join != null) {
            s.append("\n").append(tabs).append("=>join=");
            toS(t.join, s, tabs + "\t");
        }
    }

    /**
     * Add a column to the common join column list for a left table filter.
     *
     * @param leftColumn
     *            the column on the left side
     * @param replacementColumn
     *            the column to use instead, may be the same as column on the
     *            left side
     * @param replacementFilter
     *            the table filter for replacement columns
     */
    public void addCommonJoinColumns(Column leftColumn, Column replacementColumn, TableFilter replacementFilter) {
        if (commonJoinColumns == null) {
            commonJoinColumns = new LinkedHashMap<>();
            commonJoinColumnsFilter = replacementFilter;
        } else {
            assert commonJoinColumnsFilter == replacementFilter;
        }
        commonJoinColumns.put(leftColumn, replacementColumn);
    }

    /**
     * Add an excluded column to the common join column list.
     *
     * @param columnToExclude
     *            the column to exclude
     */
    public void addCommonJoinColumnToExclude(Column columnToExclude) {
        if (commonJoinColumnsToExclude == null) {
            commonJoinColumnsToExclude = Utils.newSmallArrayList();
        }
        commonJoinColumnsToExclude.add(columnToExclude);
    }

    /**
     * Returns common join columns map.
     *
     * @return common join columns map, or {@code null}
     */
    public LinkedHashMap<Column, Column> getCommonJoinColumns() {
        return commonJoinColumns;
    }

    /**
     * Returns common join columns table filter.
     *
     * @return common join columns table filter, or {@code null}
     */
    public TableFilter getCommonJoinColumnsFilter() {
        return commonJoinColumnsFilter;
    }

    /**
     * Check if the given column is an excluded common join column.
     *
     * @param c
     *            the column to check
     * @return true if this is an excluded common join column
     */
    public boolean isCommonJoinColumnToExclude(Column c) {
        return commonJoinColumnsToExclude != null && commonJoinColumnsToExclude.contains(c);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Are there any index conditions that involve IN(...).
     *
     * @return whether there are IN(...) comparisons
     */
    public boolean hasInComparisons() {
        for (IndexCondition cond : indexConditions) {
            int compareType = cond.getCompareType();
            if (compareType == Comparison.IN_QUERY || compareType == Comparison.IN_LIST) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add the current row to the array, if there is a current row.
     *
     * @param rows the rows to lock
     */
    public void lockRowAdd(ArrayList<Row> rows) {
        if (state == FOUND) {
            rows.add(get());
        }
    }

    public TableFilter getNestedJoin() {
        return nestedJoin;
    }

    /**
     * Visit this and all joined or nested table filters.
     *
     * @param visitor the visitor
     */
    public void visit(TableFilterVisitor visitor) {
        TableFilter f = this;
        do {
            visitor.accept(f);
            TableFilter n = f.nestedJoin;
            if (n != null) {
                n.visit(visitor);
            }
            f = f.join;
        } while (f != null);
    }

    public boolean isEvaluatable() {
        return evaluatable;
    }

    public Session getSession() {
        return session;
    }

    public IndexHints getIndexHints() {
        return indexHints;
    }

    /**
     * Returns whether this is a table filter with implicit DUAL table for a
     * SELECT without a FROM clause.
     *
     * @return whether this is a table filter with implicit DUAL table
     */
    public boolean isNoFromClauseFilter() {
        return table instanceof DualTable && join == null && nestedJoin == null
                && joinCondition == null && filterCondition == null;
    }

    /**
     * A visitor for table filters.
     */
    public interface TableFilterVisitor {

        /**
         * This method is called for each nested or joined table filter.
         *
         * @param f the filter
         */
        void accept(TableFilter f);
    }

    /**
     * A visitor that maps columns.
     */
    private static final class MapColumnsVisitor implements TableFilterVisitor {
        private final Expression on;

        MapColumnsVisitor(Expression on) {
            this.on = on;
        }

        @Override
        public void accept(TableFilter f) {
            on.mapColumns(f, 0, Expression.MAP_INITIAL);
        }
    }

}
