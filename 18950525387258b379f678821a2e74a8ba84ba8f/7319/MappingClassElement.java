/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * MappingClassElement.java
 *
 * Created on March 3, 2000, 1:11 PM
 */

package com.sun.jdo.api.persistence.model.mapping;

import java.util.List;
import java.util.ArrayList;

import org.netbeans.modules.dbschema.TableElement;
import org.netbeans.modules.dbschema.SchemaElement;

import com.sun.jdo.api.persistence.model.ModelException;

/**
 *
 * @author raccah
 * @version %I%
 */
public interface MappingClassElement extends MappingElement
{
    /** Constant representing mapping file extension. */
    public static final String MAPPING_EXTENSION = "mapping";        // NOI18N

    /** Constant representing Consistency level.
     * NONE_CONSISTENCY implies that no consistency semantics are enforced.
     */
    public static final int NONE_CONSISTENCY = 0x0;

    /** Constant representing Consistency level.
     * CHECK_MODIFIED_AT_COMMIT_CONSISTENCY implies that at commit,
     * consistency check is enforced for all fetched fields of modified
     * objects.
     */
    public static final int CHECK_MODIFIED_AT_COMMIT_CONSISTENCY  = 0x1;

    /** Constant representing Consistency level.
     * CHECK_ALL_AT_COMMIT_CONSISTENCY implies that at commit, consistency
     * check is enforced for all the fields of objects at this consistency
     * level.
     * Please note that this level is not supported in the current release.
     */
    public static final int CHECK_ALL_AT_COMMIT_CONSISTENCY  = 0x2;

    /** Constant representing Consistency level.
     * LOCK_WHEN_MODIFIED_CONSISTENCY implies exclusive lock is obtained for
     * data corresponding to this object when an attempt to modify the object
     * is made.
     * Please note that this level is not supported in the current release.
     */
    public static final int LOCK_WHEN_MODIFIED_CONSISTENCY   = 0x4;

    /** Constant representing Consistency level.
     * LOCK_WHEN_MODIFIED_CHECK_ALL_AT_COMMIT_CONSISTENCY implies exclusive
     * lock is obtained for data corresponding to this object when an attempt
     * to modify the object is made.  Also at commit, consistency check is
     * enforced for all the fields of objects at this consistency level.
     * Please note that this level is not supported in the current release.
     */
    public static final int LOCK_WHEN_MODIFIED_CHECK_ALL_AT_COMMIT_CONSISTENCY =
        CHECK_ALL_AT_COMMIT_CONSISTENCY | LOCK_WHEN_MODIFIED_CONSISTENCY;

    /** Constant representing Consistency level.
     * LOCK_WHEN_LOADED_CONSISTENCY implies that exclusive lock is
     * obtained for data corresponding to this object before accessing it.
     */
    public static final int LOCK_WHEN_LOADED_CONSISTENCY    = 0x8;

    /** Constant representing Consistency level.
     * VERSION_CONSISTENCY implies that no lock is obtained for data
     * corresponding to this object until it will be updated.
     */
    public static final int VERSION_CONSISTENCY    = 0x10;

    // TBD
// clears out fields -- equiv to new, can take it out
//    public void clear ();
//    public boolean mapFieldToTable (MappingFieldElement field,
//        TableElement table);

    /** Returns the version number of this MappingClassElement object.
     * Please note, the returned version number reflects the version number at
     * the last save, NOT the version number of the memory representation.
     * @return version number
     */
    public int getVersionNumber ();

    /** Returns true if the version number of this MappingClassElement object
     * is older than the current version number of the archiving scheme.
     * @see #getVersionNumber
     * @return true if it is in need of updating, false otherwise
     */
    public boolean hasOldVersionNumber ();

    /** This method is called after a MappingClassElement is unarchived
     * from a .mapping file.  This method provides a hook to do any checking
     * (version number checking) and conversion after unarchiving.
     * @exception ModelException if impossible
     */
    public void postUnarchive () throws ModelException;

    /** This method is called prior to storing a MappingClassElement in a
     * .mapping file.  This method provides a hook to do any conversion
     * before archiving.
     * @exception ModelException if impossible
     */
    public void preArchive () throws ModelException;

    /** Gets the modified flag for this mapping class.
     * @return <code>true</code> if there have been (property) changes to this
     * class, <code>false</code> otherwise.
     */
    public boolean isModified ();

    /** Set the modified flag for this mapping class to flag.  This is usually
     * set to <code>true</code> by property changes and <code>false</code>
     * after a save.
     * @param flag if <code>true</code>, this class is marked as modified;
     * if <code>false</code>, it is marked as unmodified.
     */
    public void setModified (boolean flag);

    /** Gets the consistency level of this mapping class.
     * @return the consistency level, one of {@link #NONE_CONSISTENCY},
     * {@link #CHECK_MODIFIED_AT_COMMIT_CONSISTENCY},
     * {@link #CHECK_ALL_AT_COMMIT_CONSISTENCY},
     * {@link #LOCK_WHEN_MODIFIED_CONSISTENCY},
     * {@link #LOCK_WHEN_MODIFIED_CHECK_ALL_AT_COMMIT_CONSISTENCY},
     * {@link #LOCK_WHEN_LOADED_CONSISTENCY}, or
     * {@link #VERSION_CONSISTENCY}.
     */
    public int getConsistencyLevel ();

    /** Set the consistency level of this mapping class.
     * @param level an integer indicating the consistency level, one of:
     * {@link #NONE_CONSISTENCY},{@link #CHECK_MODIFIED_AT_COMMIT_CONSISTENCY},
     * {@link #CHECK_ALL_AT_COMMIT_CONSISTENCY},
     * {@link #LOCK_WHEN_MODIFIED_CONSISTENCY},
     * {@link #LOCK_WHEN_MODIFIED_CHECK_ALL_AT_COMMIT_CONSISTENCY},
     * {@link #LOCK_WHEN_LOADED_CONSISTENCY}, or
     * {@link #VERSION_CONSISTENCY}.
     * @exception ModelException if impossible.
     */
    public void setConsistencyLevel (int level)  throws ModelException;

    //======================= schema handling ===========================

    /** Returns the name of the SchemaElement which represents the
     * database used by the tables mapped to this mapping class element.
     * @return the name of the database root for this mapping class
     */
    public String getDatabaseRoot ();

    /** Set the database root for this MappingClassElement.
     * The root represents the database used by the tables mapped to
     * this mapping class.
     * @param root the new database root
     * @exception ModelException if impossible
     */
    public void setDatabaseRoot (SchemaElement root) throws ModelException;

    //======================= table handling ===========================

    /** Returns the list of tables (MappingTableElements) used by this mapping
     * class.
     * @return the meta data tables for this mapping class
     */
    public ArrayList getTables ();

    /** Scans through this mapping class looking for a table whose
     * name matches the name passed in.
     * @param name name of the table to find.
     * @return the meta data table whose name matches the name parameter
     */
    public MappingTableElement getTable (String name);

    /** Convenience method which accepts a table element and attempts to add
     * it as either a primary or secondary table depending on the existing list
     * of tables and the foreign keys for the table.
     * @param table table element to be added as either a primary or secondary
     * table.
     * @exception ModelException if impossible
     */
    public void addTable (TableElement table) throws ModelException;

    /** Set the primary table for this mapping class to the supplied table.
     * @param table table element to be used as the primary table.
     * @exception ModelException if impossible
     */
    public void setPrimaryTable (TableElement table) throws ModelException;

    /** Adds a reference to the supplied table as a secondary table for this
     * mapping class.  It creates a MappingReferenceKeyElement for the supplied
     * primary/secondary table pair.
     * @param parentTable mapping table element which should also be the primary
     * table.
     * @param table table element to be used as a secondary table.
     * @exception ModelException if impossible
     */
    public MappingReferenceKeyElement addSecondaryTable (MappingTableElement
        parentTable, TableElement table) throws ModelException;

    /** Removes the reference to the supplied table as a mapped table for this
     * mapping class.  This works whether the table is the primary table or a
     * secondary table.
     * @param table mapping table element to be removed from this mapping class.
     * @exception ModelException if impossible
     */
    public void removeTable (MappingTableElement table) throws ModelException;

    //======================= field handling ===========================

    /** Returns the list of fields (MappingFieldElements) in this mapping
     * class.  This list includes both local and relationship fields.
     * @return the mapping fields in this mapping class
     */
    public ArrayList getFields ();

    /** Scans through this mapping class looking for a field whose
     * name matches the name passed in.
     * @param name name of the field to find.
     * @return the mapping field whose name matches the name parameter
     */
    public MappingFieldElement getField (String name);

    /** Adds a field to the list of fields in this mapping class.
     * @param field field element to be added
     * @exception ModelException if impossible
     */
    public void addField (MappingFieldElement field) throws ModelException;

    /** Removes a field from the list of fields in this mapping class.
     * @param field field element to be removed
     * @exception ModelException if impossible
     */
    public void removeField (MappingFieldElement field) throws ModelException;

    /** Returns the list of version fields (MappingFieldElements) in this
     * mapping class.  This list only includes fields if the consistency
     * level is {@link #VERSION_CONSISTENCY}.
     * @return the version fields in this mapping class
     */
    public List getVersionFields ();

    /** Gets the navigable flag for this mapping class.
     * @return <code>true</code> if lazy initialization will be used,
     * <code>false</code> if access to a non-fetched field will result in an
     * exception.  The default is <code>true</code>.
     */
    public boolean isNavigable ();

    /** Set the navigable flag for this mapping class to flag.
     * @param flag if <code>true</code>, lazy initialization will be used;
     * if <code>false</code>, access to a non-fetched field will result in an
     * exception.
     * @exception ModelException if impossible
     */
    public void setNavigable (boolean flag) throws ModelException;
}
