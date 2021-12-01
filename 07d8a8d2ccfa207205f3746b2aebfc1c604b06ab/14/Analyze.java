/*
 * Copyright 2004-2020 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.command.ddl;

import org.h2.command.CommandInterface;
import org.h2.engine.Constants;
import org.h2.engine.Database;
import org.h2.engine.Right;
import org.h2.engine.Session;
import org.h2.index.Cursor;
import org.h2.result.Row;
import org.h2.table.Column;
import org.h2.table.Table;
import org.h2.table.TableType;
import org.h2.util.IntIntHashMap;
import org.h2.value.DataType;
import org.h2.value.Value;

/**
 * This class represents the statements
 * ANALYZE and ANALYZE TABLE
 */
public class Analyze extends DefineCommand {

    private static final class SelectivityData {

        private long count, distinctCount;
        private final IntIntHashMap distinctHashes;

        SelectivityData() {
            distinctHashes = new IntIntHashMap(false);
        }

        void add(Value v) {
            count++;
            int size = distinctHashes.size();
            if (size >= Constants.SELECTIVITY_DISTINCT_COUNT) {
                distinctHashes.clear();
                distinctCount += size;
            }
            // the value -1 is not supported
            distinctHashes.put(v.hashCode(), 1);
        }

        int getSelectivity() {
            int s;
            if (count == 0) {
                s = 0;
            } else {
                s = (int) (100 * (distinctCount + distinctHashes.size()) / count);
                if (s <= 0) {
                    s = 1;
                }
            }
            return s;
        }

    }

    /**
     * The sample size.
     */
    private int sampleRows;
    /**
     * used in ANALYZE TABLE...
     */
    private Table table;

    public Analyze(Session session) {
        super(session);
        sampleRows = session.getDatabase().getSettings().analyzeSample;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    @Override
    public int update() {
        session.commit(true);
        session.getUser().checkAdmin();
        Database db = session.getDatabase();
        if (table != null) {
            analyzeTable(session, table, sampleRows, true);
        } else {
            for (Table table : db.getAllTablesAndViews(false)) {
                analyzeTable(session, table, sampleRows, true);
            }
        }
        return 0;
    }

    /**
     * Analyze this table.
     *
     * @param session the session
     * @param table the table
     * @param sample the number of sample rows
     * @param manual whether the command was called by the user
     */
    public static void analyzeTable(Session session, Table table, int sample, boolean manual) {
        if (table.getTableType() != TableType.TABLE //
                || table.isHidden() //
                || session == null //
                || !manual && (session.getDatabase().isSysTableLocked() || table.hasSelectTrigger()) //
                || table.isTemporary() && !table.isGlobalTemporary() //
                        && session.findLocalTempTable(table.getName()) == null //
                || table.isLockedExclusively() && !table.isLockedExclusivelyBy(session)
                || !session.getUser().hasRight(table, Right.SELECT) //
                // if the connection is closed and there is something to undo
                || session.getCancel() != 0) {
            return;
        }
        table.lock(session, false, false);
        Column[] columns = table.getColumns();
        int columnCount = columns.length;
        if (columnCount == 0) {
            return;
        }
        Cursor cursor = table.getScanIndex(session).find(session, null, null);
        if (cursor.next()) {
            SelectivityData[] array = new SelectivityData[columnCount];
            for (int i = 0; i < columnCount; i++) {
                Column col = columns[i];
                if (!DataType.isLargeObject(col.getType().getValueType())) {
                    array[i] = new SelectivityData();
                }
            }
            int rowNumber = 0;
            do {
                Row row = cursor.get();
                for (int i = 0; i < columnCount; i++) {
                    SelectivityData selectivity = array[i];
                    if (selectivity != null) {
                        selectivity.add(row.getValue(i));
                    }
                }
            } while ((sample <= 0 || ++rowNumber < sample) && cursor.next());
            for (int i = 0; i < columnCount; i++) {
                SelectivityData selectivity = array[i];
                if (selectivity != null) {
                    columns[i].setSelectivity(selectivity.getSelectivity());
                }
            }
//<<<<<<< HEAD
//        }
//        buff.append(" FROM ");
//        table.getSQL(buff, true);
//        if (sample > 0) {
//            buff.append(" FETCH FIRST ROW ONLY SAMPLE_SIZE ? ");
//        }
//        //如: SELECT SELECTIVITY(ID), SELECTIVITY(NAME), SELECTIVITY(B) FROM PUBLIC.REGULARTABLETEST LIMIT 1 SAMPLE_SIZE 10000
//        String sql = buff.toString();
//        Prepared command = session.prepare(sql);
//        if (sample > 0) {
//            ArrayList<Parameter> params = command.getParameters();
//            params.get(0).setValue(ValueInt.get(sample));
//        }
//        ResultInterface result = command.query(0);
//        result.next();
//        for (int j = 0; j < columns.length; j++) {
//            Value v = result.currentRow()[j];
//            if (v != ValueNull.INSTANCE) {
//                int selectivity = v.getInt();
//                columns[j].setSelectivity(selectivity);
//=======
        } else {
            for (int i = 0; i < columnCount; i++) {
                columns[i].setSelectivity(0);
            }
        }
        session.getDatabase().updateMeta(session, table);
    }

    public void setTop(int top) {
        this.sampleRows = top;
    }

    @Override
    public int getType() {
        return CommandInterface.ANALYZE;
    }

}
