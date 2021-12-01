package org.apache.usergrid.persistence.graph.serialization.impl.shard.impl;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.usergrid.persistence.core.astyanax.ColumnNameIterator;
import org.apache.usergrid.persistence.core.astyanax.MultiKeyColumnNameIterator;
import org.apache.usergrid.persistence.core.astyanax.MultiTennantColumnFamily;
import org.apache.usergrid.persistence.core.astyanax.ScopedRowKey;
import org.apache.usergrid.persistence.core.scope.ApplicationScope;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.model.ConsistencyLevel;
import com.netflix.astyanax.query.RowQuery;
import com.netflix.astyanax.util.RangeBuilder;


/**
 * Internal iterator to iterate over multiple row keys
 *
 * @param <R> The row type
 * @param <C> The column type
 * @param <T> The parsed return type
 */
public class ShardRowIterator<R, C, T> implements Iterator<T> {

    private final EdgeSearcher<R, C, T> searcher;

    private final MultiTennantColumnFamily<ApplicationScope, R, C> cf;

    private Iterator<T> currentColumnIterator;

    private final Keyspace keyspace;

    private final int pageSize;

    private final ConsistencyLevel consistencyLevel;


    public ShardRowIterator( final EdgeSearcher<R, C, T> searcher,
                             final MultiTennantColumnFamily<ApplicationScope, R, C> cf, final Keyspace keyspace,
                             final ConsistencyLevel consistencyLevel, final int pageSize ) {
        this.searcher = searcher;
        this.cf = cf;
        this.keyspace = keyspace;
        this.pageSize = pageSize;
        this.consistencyLevel = consistencyLevel;
    }


    @Override
    public boolean hasNext() {
        //we have more columns to return
        if ( currentColumnIterator != null && currentColumnIterator.hasNext() ) {
            return true;
        }

        /**
         * We have another row key, advance to it and re-check
         */
        if ( searcher.hasNext() ) {
            advanceRow();
            return hasNext();
        }

        //we have no more columns, and no more row keys, we're done
        return false;
    }


    @Override
    public T next() {
        if ( !hasNext() ) {
            throw new NoSuchElementException( "There are no more rows or columns left to advance" );
        }

        return currentColumnIterator.next();
    }


    @Override
    public void remove() {
        throw new UnsupportedOperationException( "Remove is unsupported" );
    }


    /**
     * Advance our iterator to the next row (assumes the check for row keys is elsewhere)
     */
    private void advanceRow() {

        /**
         * If the edge is present, we need to being seeking from this
         */

        final RangeBuilder rangeBuilder = new RangeBuilder().setLimit( pageSize );


        //set the range into the search
        searcher.setRange( rangeBuilder );

        /**
         * Get our list of slices
         */
        final List<ScopedRowKey<ApplicationScope, R>> rowKeys = searcher.next();


        final List<ColumnNameIterator<C, T>> columnNameIterators = new ArrayList<>( rowKeys.size() );

        for(ScopedRowKey<ApplicationScope, R> rowKey: rowKeys){



           final  RowQuery<ScopedRowKey<ApplicationScope, R>, C> query =
                    keyspace.prepareQuery( cf ).setConsistencyLevel( consistencyLevel ).getKey( rowKey )
                            .autoPaginate( true ).withColumnRange( rangeBuilder.build() );


            final ColumnNameIterator<C, T> columnNameIterator = new ColumnNameIterator<>( query, searcher, searcher.hasPage() );

            columnNameIterators.add( columnNameIterator );

        }



        currentColumnIterator = new MultiKeyColumnNameIterator<>(columnNameIterators, searcher, pageSize);


    }
}
