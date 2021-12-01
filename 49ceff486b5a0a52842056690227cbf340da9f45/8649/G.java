/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package soot;
import soot.coffi.*;

import java.io.PrintStream;
import java.util.*;
import soot.jimple.toolkits.pointer.util.NativeHelper;
import soot.jimple.toolkits.annotation.arraycheck.Array2ndDimensionSymbol;
import soot.jimple.toolkits.pointer.UnionFactory;
import soot.jimple.toolkits.typing.ClassHierarchy;

/** A class to group together all the global variables in Soot. */
public class G extends Singletons 
{
    private static G instance = new G();
    public static G v() { return instance; }
    public static void reset() { instance = new G(); }

    public PrintStream out = System.out;

    public class Global {
    }

    public long coffi_BasicBlock_ids = 0;
    public Utf8_Enumeration coffi_CONSTANT_Utf8_info_e1 = new Utf8_Enumeration();
    public Utf8_Enumeration coffi_CONSTANT_Utf8_info_e2 = new Utf8_Enumeration();
    public int SETNodeLabel_uniqueId = 0;
    public boolean ASTAnalysis_modified;
    public NativeHelper NativeHelper_helper = null;
    public HashMap Parm_pairToElement = new HashMap();
    public int SparkNativeHelper_tempVar = 0;
    public int PaddleNativeHelper_tempVar = 0;
    public boolean PointsToSetInternal_warnedAlready = false;
    public Set MethodRWSet_allGlobals = new HashSet();
    public Set MethodRWSet_allFields = new HashSet();
    public int GeneralConstObject_counter = 0;
    public UnionFactory Union_factory = null;
    public HashMap<Object, Array2ndDimensionSymbol> Array2ndDimensionSymbol_pool = new HashMap<Object, Array2ndDimensionSymbol>();
    public Map AbstractUnit_allMapToUnnamed = Collections.unmodifiableMap(new AbstractUnitAllMapTo("<unnamed>"));
    public List<Timer> Timer_outstandingTimers = new ArrayList<Timer>();
    public boolean Timer_isGarbageCollecting;
    public Timer Timer_forcedGarbageCollectionTimer = new Timer("gc");
    public int Timer_count;
    public final Map<Scene, ClassHierarchy> ClassHierarchy_classHierarchyMap = new HashMap<Scene, ClassHierarchy>();
    public final Map<MethodContext, MethodContext> MethodContext_map = new HashMap<MethodContext, MethodContext>();


    
    public boolean ASTTransformations_modified;
    
    /*
     * 16th Feb 2006 Nomair
     * The AST transformations are unfortunately non-monotonic.
     * Infact one transformation on each iteration simply reverses the bodies of an if-else
     * To make the remaining transformations monotonic this transformation is handled with
     * a separate flag...clumsy but works
     */
    public boolean ASTIfElseFlipped;
    
    
    
}

