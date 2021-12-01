/*
 * Copyright (c) 2018 Ahome' Innovation Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ait.lienzo.client.core.shape.wires.handlers.impl;

import static com.ait.lienzo.client.core.AttributeOp.any;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ait.lienzo.client.core.Attribute;
import com.ait.lienzo.client.core.event.AttributesChangedEvent;
import com.ait.lienzo.client.core.event.AttributesChangedHandler;
import com.ait.lienzo.client.core.shape.Attributes;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IDrawable;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.wires.AlignAndDistribute;
import com.ait.lienzo.client.core.shape.wires.handlers.AlignAndDistributeControl;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.tooling.common.api.flow.Flows;
import com.ait.tooling.nativetools.client.collection.NFastStringSet;
import com.ait.tooling.nativetools.client.event.HandlerRegistrationManager;
import com.google.gwt.event.shared.HandlerRegistration;

public class AlignAndDistributeControlImpl implements AlignAndDistributeControl
{
    protected AlignAndDistribute                                   m_alignAndDistribute;

    protected IPrimitive<?>                                        m_group;

    protected BoundingBox                                          m_box;

    protected boolean                                              m_isDraggable;

    protected boolean                                              m_isDragging;

    protected HandlerRegistrationManager                           m_attrHandlerRegs;

    protected HandlerRegistration                                  m_dragEndHandlerReg;

    protected AlignAndDistribute.AlignAndDistributeMatchesCallback m_alignAndDistributeMatchesCallback;

    protected double                                               m_startLeft;

    protected double                                               m_startTop;

    protected double                                               m_left;

    protected double                                               m_hCenter;

    protected double                                               m_right;

    protected double                                               m_top;

    protected double                                               m_vCenter;

    protected double                                               m_bottom;

    protected Set<AlignAndDistribute.DistributionEntry>            m_horizontalDistEntries;

    protected Set<AlignAndDistribute.DistributionEntry>            m_verticalDistEntries;

    private boolean                                                indexed;

    private final Flows.BooleanOp                                  m_bboxOp;

    private final Flows.BooleanOp                                  m_tranOp;

    public AlignAndDistributeControlImpl(final IPrimitive<?> group, final AlignAndDistribute alignAndDistribute, final AlignAndDistribute.AlignAndDistributeMatchesCallback alignAndDistributeMatchesCallback, final List<Attribute> attributes)
    {
        m_group = group;

        m_alignAndDistribute = alignAndDistribute;

        m_alignAndDistributeMatchesCallback = alignAndDistributeMatchesCallback;

        // circles xy are in centre, where as others are top left.
        // For this reason we must use getBoundingBox, which uses BoundingPoints underneath, when ensures the shape x/y is now top left.
        m_box = AlignAndDistribute.getBoundingBox(group);

        final double left = m_box.getMinX();
        final double right = m_box.getMaxX();
        final double top = m_box.getMinY();
        final double bottom = m_box.getMaxY();

        captureHorizontalPositions(left, right);
        captureVerticalPositions(top, bottom);

        m_alignAndDistribute.indexOn(this);

        if (m_group.isDraggable())
        {
            dragOn();
        }
        m_attrHandlerRegs = new HandlerRegistrationManager();

        final ArrayList<Attribute> temp = new ArrayList<>(attributes);

        temp.add(Attribute.X);

        temp.add(Attribute.Y);

        final NFastStringSet seen = new NFastStringSet();

        final ArrayList<Attribute> list = new ArrayList<>();

        for (final Attribute attribute : temp)
        {
            if (null != attribute)
            {
                if (false == seen.contains(attribute.getProperty()))
                {
                    list.add(attribute);

                    seen.add(attribute.getProperty());
                }
            }
        }
        m_bboxOp = any(list);

        addHandlers(m_group, list);

        m_tranOp = any(Attribute.ROTATION, Attribute.SCALE, Attribute.SHEAR);
    }

    private final AttributesChangedHandler ShapeAttributesChangedHandler = new AttributesChangedHandler()
    {
        @Override
        public void onAttributesChanged(final AttributesChangedEvent event)
        {
            refresh(event.evaluate(m_tranOp), event.evaluate(m_bboxOp));
        }
    };

    public void addHandlers(final IDrawable<?> drawable, final ArrayList<Attribute> list)
    {
        for (final Attribute attribute : list)
        {
            m_attrHandlerRegs.register(drawable.addAttributesChangedHandler(attribute, ShapeAttributesChangedHandler));
        }
        m_attrHandlerRegs.register(drawable.addAttributesChangedHandler(Attribute.ROTATION, ShapeAttributesChangedHandler));
        m_attrHandlerRegs.register(drawable.addAttributesChangedHandler(Attribute.SCALE, ShapeAttributesChangedHandler));
        m_attrHandlerRegs.register(drawable.addAttributesChangedHandler(Attribute.SHEAR, ShapeAttributesChangedHandler));
    }

    @Override
    public boolean isIndexed()
    {
        return indexed;
    }

    @Override
    public void setIndexed(final boolean indexed)
    {
        this.indexed = indexed;
    }

    @Override
    public Set<AlignAndDistribute.DistributionEntry> getHorizontalDistributionEntries()
    {
        if (m_horizontalDistEntries == null)
        {
            m_horizontalDistEntries = new HashSet<>();
        }
        return m_horizontalDistEntries;
    }

    @Override
    public Set<AlignAndDistribute.DistributionEntry> getVerticalDistributionEntries()
    {
        if (m_verticalDistEntries == null)
        {
            m_verticalDistEntries = new HashSet<>();
        }
        return m_verticalDistEntries;
    }

    public IPrimitive<?> getShape()
    {
        return m_group;
    }

    /**
     * This is a cached BoundingBox
     * @return
     */
    public BoundingBox getBoundingBox()
    {
        return m_box;
    }

    @Override
    public double getLeft()
    {
        return m_left;
    }

    @Override
    public double getHorizontalCenter()
    {
        return m_hCenter;
    }

    @Override
    public double getRight()
    {
        return m_right;
    }

    @Override
    public double getTop()
    {
        return m_top;
    }

    @Override
    public double getVerticalCenter()
    {
        return m_vCenter;
    }

    @Override
    public double getBottom()
    {
        return m_bottom;
    }

    public void capturePositions(final double left, final double right, final double top, final double bottom)
    {
        if ((left != m_left) || (right != m_right))
        {
            captureHorizontalPositions(left, right);
        }
        if ((top != m_top) || (bottom != m_bottom))
        {
            captureVerticalPositions(top, bottom);
        }
    }

    public void captureHorizontalPositions(final double left, final double right)
    {
        final double width = m_box.getWidth();
        m_left = left;
        m_hCenter = m_left + (width / 2);
        m_right = right;
    }

    public void captureVerticalPositions(final double top, final double bottom)
    {
        final double height = m_box.getHeight();
        m_top = top;
        m_vCenter = (m_top + (height / 2));
        m_bottom = bottom;
    }

    @Override
    public void updateIndex()
    {
        // circles xy are in centre, where as others are top left.
        // For this reason we must use getBoundingBox, which uses BoundingPoints underneath, when ensures the shape x/y is now top left.
        m_box = AlignAndDistribute.getBoundingBox(m_group);

        final double left = m_box.getMinX();
        final double right = m_box.getMaxX();
        final double top = m_box.getMinY();
        final double bottom = m_box.getMaxY();

        final boolean leftChanged = left != m_left;
        final boolean rightChanged = right != m_right;
        final boolean topChanged = top != m_top;
        final boolean bottomChanged = bottom != m_bottom;

        if (!leftChanged && !rightChanged && !topChanged && !bottomChanged)
        {
            // this can happen when the event batching triggers after a drag has stopped, but the event change was due to the dragging.
            // @dean REVIEW
            return;
        }

        //BoundingBox box = AlignAndDistribute.getBoundingBox(m_group);
        updateIndex(leftChanged, rightChanged, topChanged, bottomChanged, left, right, top, bottom);
    }

    public void updateIndex(final boolean leftChanged, final boolean rightChanged, final boolean topChanged, final boolean bottomChanged, final double left, final double right, final double top, final double bottom)
    {
        if (leftChanged || rightChanged)
        {
            m_alignAndDistribute.removeHorizontalDistIndex(this);

            final boolean hCenterChanged = ((left + (m_box.getWidth() / 2)) != m_hCenter);

            if (leftChanged)
            {
                m_alignAndDistribute.removeLeftAlignIndexEntry(this, m_left);
            }
            if (hCenterChanged)
            {
                m_alignAndDistribute.removeHCenterAlignIndexEntry(this, m_hCenter);
            }
            if (rightChanged)
            {
                m_alignAndDistribute.removeRightAlignIndexEntry(this, m_right);
            }
            captureHorizontalPositions(left, right);

            if (leftChanged)
            {
                m_alignAndDistribute.addLeftAlignIndexEntry(this, m_left);
            }
            if (hCenterChanged)
            {
                m_alignAndDistribute.addHCenterAlignIndexEntry(this, m_hCenter);
            }
            if (rightChanged)
            {
                m_alignAndDistribute.addRightAlignIndexEntry(this, m_right);
            }
            m_alignAndDistribute.buildHorizontalDistIndex(this);
        }
        if (topChanged || bottomChanged)
        {
            m_alignAndDistribute.removeVerticalDistIndex(this);

            final boolean vCenterChanged = ((top + (m_box.getHeight() / 2)) != m_vCenter);

            if (topChanged)
            {
                m_alignAndDistribute.removeTopAlignIndexEntry(this, m_top);
            }
            if (vCenterChanged)
            {
                m_alignAndDistribute.removeVCenterAlignIndexEntry(this, m_vCenter);
            }
            if (bottomChanged)
            {
                m_alignAndDistribute.removeBottomAlignIndexEntry(this, m_bottom);
            }
            captureVerticalPositions(top, bottom);

            if (topChanged)
            {
                m_alignAndDistribute.addTopAlignIndexEntry(this, m_top);
            }
            if (vCenterChanged)
            {
                m_alignAndDistribute.addVCenterAlignIndexEntry(this, m_vCenter);
            }
            if (bottomChanged)
            {
                m_alignAndDistribute.addBottomAlignIndexEntry(this, m_bottom);
            }
            m_alignAndDistribute.buildVerticalDistIndex(this);
        }
    }

    public void dragOn()
    {
        m_isDraggable = true;
    }

    public void draggOff()
    {
        m_isDraggable = false;
    }

    @Override
    public boolean isDraggable()
    {
        return m_isDraggable;
    }

    private final boolean hasComplexTransformAttributes()
    {
        final Attributes attr = AlignAndDistribute.getAttributes(m_group);

        if (attr.hasComplexTransformAttributes())
        {
            final double r = attr.getRotation();

            if (r != 0)
            {
                return true;
            }
            final Point2D scale = attr.getScale();

            if (null != scale)
            {
                if ((scale.getX() != 1) || (scale.getY() != 1))
                {
                    return true;
                }
            }
            final Point2D shear = attr.getShear();

            if (null != shear)
            {
                if ((shear.getX() != 0) || (shear.getY() != 0))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void refresh()
    {
        refresh(true, true);
    }

    @Override
    public void refresh(final boolean transforms, final boolean attributes)
    {
        if (m_isDragging)
        {
            // ignore attribute changes while dragging
            return;
        }
        if (transforms)
        {
            final boolean hasTransformations = hasComplexTransformAttributes();

            if (indexed && hasTransformations)
            {
                // Indexing cannot be done on transformed shapes
                // it's cheaper to just check if the attributes exist on the shape, than it is to test for attributes on the event
                m_alignAndDistribute.indexOff(this);
            }
            else if (!indexed && !hasTransformations)
            {
                // Indexing was turned off, but there are no more transformations, so turn it back on again
                m_alignAndDistribute.indexOn(this);
            }
        }
        final boolean isDraggable = m_group.isDraggable();

        if (!m_isDraggable && isDraggable)
        {
            // was off, now on
            dragOn();
        }
        else if (m_isDraggable && !isDraggable)
        {
            // was on, now on off
            draggOff();
        }
        if (indexed && attributes)
        {
            updateIndex();
        }
    }

    @Override
    public void dragStart()
    {
        // shapes being dragged must be removed from the index, so that they don't snap to themselves
        // Also removes all nested shapes.
        m_startLeft = m_left;
        m_startTop = m_top;

        m_isDragging = true;
        iterateAndRemoveIndex(m_group);
    }

    @Override
    public void remove()
    {
        this.removeHandlerRegistrations();
    }

    public void iterateAndRemoveIndex(final IPrimitive<?> prim)
    {
        indexOff(prim);

        if (prim instanceof Group)
        {
            for (final IPrimitive<?> child : prim.asGroup().getChildNodes())
            {
                if (child instanceof Group)
                {
                    iterateAndRemoveIndex(child.asGroup());
                }
                else
                {
                    indexOff(child);
                }
            }
        }
    }

    public void indexOff(final IPrimitive<?> child)
    {
        final AlignAndDistributeControl handler = m_alignAndDistribute.getControlForShape(child.uuid());

        if ((handler != null) && handler.isIndexed())
        {
            m_alignAndDistribute.indexOffWithoutChangingStatus(handler);
        }
    }

    public static class ShapePair
    {
        private final Group         parent;

        private final IPrimitive<?> child;

        AlignAndDistributeControl   handler;

        public ShapePair(final Group group, final IPrimitive<?> child, final AlignAndDistributeControl handler)
        {
            this.parent = group;
            this.child = child;
            this.handler = handler;
        }
    }

    public void removeChildrenIfIndexed(final IPrimitive<?> prim, final List<ShapePair> pairs)
    {
        for (final IPrimitive<?> child : prim.asGroup().getChildNodes())
        {
            final AlignAndDistributeControl handler = m_alignAndDistribute.getControlForShape(child.uuid());

            if (handler != null)
            {
                final ShapePair pair = new ShapePair(prim.asGroup(), child, handler);
                pairs.add(pair);
                prim.asGroup().remove(child);
            }
            if (child instanceof Group)
            {
                removeChildrenIfIndexed(child.asGroup(), pairs);
            }
        }
    }

    private void indexOn(final IPrimitive<?> shape)
    {
        final AlignAndDistributeControl handler = m_alignAndDistribute.getControlForShape(shape.uuid());

        indexOn(handler);
    }

    private void indexOn(final AlignAndDistributeControl handler)
    {
        if ((handler != null) && handler.isIndexed())
        {
            m_alignAndDistribute.indexOnWithoutChangingStatus(handler);

            handler.updateIndex();
        }
    }

    @Override
    public boolean dragAdjust(final Point2D dxy)
    {
        if (!indexed)
        {
            // ignore adjustment if indexing is off
            return false;
        }

        double left = m_startLeft + dxy.getX();
        double top = m_startTop + dxy.getY();
        double width = m_box.getWidth();
        double height = m_box.getHeight();
        capturePositions(left, left + width, top, top + height);

        final AlignAndDistribute.AlignAndDistributeMatches matches = m_alignAndDistribute.findNearestMatches(this, m_left, m_hCenter, m_right, m_top, m_vCenter, m_bottom);

        boolean recapture = false;

        if (m_alignAndDistribute.isSnap())
        {
            final double xOffset = m_startLeft;
            final double yOffset = m_startTop;

            // Adjust horizontal
            if (matches.getLeftList() != null)
            {
                dxy.setX(matches.getLeftPos() - xOffset);
                recapture = true;
            }
            else if (matches.getHorizontalCenterList() != null)
            {
                dxy.setX((matches.getHorizontalCenterPos() - (width / 2)) - xOffset);
                recapture = true;
            }
            else if (matches.getRightList() != null)
            {
                dxy.setX((matches.getRightPos() - width) - xOffset);
                recapture = true;
            }
            // Adjust Vertical
            if (matches.getTopList() != null)
            {
                dxy.setY(matches.getTopPos() - yOffset);
                recapture = true;
            }
            else if (matches.getVerticalCenterList() != null)
            {
                dxy.setY((matches.getVerticalCenterPos() - (height / 2)) - yOffset);
                recapture = true;
            }
            else if (matches.getBottomList() != null)
            {
                dxy.setY((matches.getBottomPos() - height) - yOffset);
                recapture = true;
            }
            // Adjust horizontal distribution
            if (matches.getLeftDistList() != null)
            {
                dxy.setX(matches.getLeftDistList().getFirst().getPoint() - width - xOffset);
                recapture = true;
            }
            else if (matches.getRightDistList() != null)
            {
                dxy.setX(matches.getRightDistList().getFirst().getPoint() - xOffset);
                recapture = true;
            }
            else if (matches.getHorizontalCenterDistList() != null)
            {
                dxy.setX(matches.getHorizontalCenterDistList().getFirst().getPoint() - (width / 2) - xOffset);
                recapture = true;
            }
            // Adjust vertical distribution
            if (matches.getTopDistList() != null)
            {
                dxy.setY(matches.getTopDistList().getFirst().getPoint() - height - yOffset);
                recapture = true;
            }
            else if (matches.getBottomDistList() != null)
            {
                dxy.setY(matches.getBottomDistList().getFirst().getPoint() - yOffset);
                recapture = true;
            }
            else if (matches.getVerticalCenterDistList() != null)
            {
                dxy.setY(matches.getVerticalCenterDistList().getFirst().getPoint() - (height / 2) - yOffset);
                recapture = true;
            }
            // it was adjusted, so recapture points
            if (recapture)
            {
                // can't use the original left and top vars, as they are before adjustment snap
                left = m_startLeft + dxy.getX();
                top = m_startTop + dxy.getY();
                width = m_box.getWidth();
                height = m_box.getHeight();
                capturePositions(left, left + width, top, top + height);
            }
        }
        if (m_alignAndDistribute.isDrawGuideLines())
        {
            m_alignAndDistributeMatchesCallback.call(matches);
        }
        return recapture;
    }

    @Override
    public void dragEnd()
    {
        if (m_isDragging)
        {
            m_isDragging = false;

            m_alignAndDistributeMatchesCallback.dragEnd();

            // We do not want the nested indexed shapes to impact the bounding box
            // so remove them, they will be added once the index has been made.
            final List<ShapePair> pairs = new ArrayList<>();
            removeChildrenIfIndexed(m_group, pairs);

            indexOn(m_group);

            // re-add the children, index before it adds the next nested child
            for (final ShapePair pair : pairs)
            {
                pair.parent.add(pair.child);

                indexOn(pair.handler);
            }
        }
    }

    private void removeDragHandlerRegistrations()
    {
        if (null != m_dragEndHandlerReg)
        {
            m_dragEndHandlerReg.removeHandler();

            m_dragEndHandlerReg = null;
        }
    }

    public void removeHandlerRegistrations()
    {
        if (null != m_attrHandlerRegs)
        {
            m_attrHandlerRegs.destroy();

            m_attrHandlerRegs = null;
        }
        removeDragHandlerRegistrations();
    }
}
