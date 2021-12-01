/*
 * Copyright (C) 2013-2015 RoboVM AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bugvm.apple.gameplaykit;

/*<imports>*/
import java.io.*;
import java.nio.*;
import java.util.*;
import com.bugvm.objc.*;
import com.bugvm.objc.annotation.*;
import com.bugvm.objc.block.*;
import com.bugvm.rt.*;
import com.bugvm.rt.annotation.*;
import com.bugvm.rt.bro.*;
import com.bugvm.rt.bro.annotation.*;
import com.bugvm.rt.bro.ptr.*;
import com.bugvm.apple.foundation.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*/@Library("GameplayKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/GKGraphNode/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class GKGraphNodePtr extends Ptr<GKGraphNode, GKGraphNodePtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(GKGraphNode.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public GKGraphNode() {}
    protected GKGraphNode(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "connectedNodes")
    public native NSArray<GKGraphNode> getConnectedNodes();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "addConnectionsToNodes:bidirectional:")
    public native void addConnectionsToNodes(NSArray<GKGraphNode> nodes, boolean bidirectional);
    @Method(selector = "removeConnectionsToNodes:bidirectional:")
    public native void removeConnectionsToNodes(NSArray<GKGraphNode> nodes, boolean bidirectional);
    @Method(selector = "estimatedCostToNode:")
    public native float getEstimatedCostToNode(GKGraphNode node);
    @Method(selector = "costToNode:")
    public native float getCostToNode(GKGraphNode node);
    @Method(selector = "findPathToNode:")
    public native NSArray<GKGraphNode> findPathToNode(GKGraphNode goalNode);
    @Method(selector = "findPathFromNode:")
    public native NSArray<GKGraphNode> findPathFromNode(GKGraphNode startNode);
    /*</methods>*/
}
