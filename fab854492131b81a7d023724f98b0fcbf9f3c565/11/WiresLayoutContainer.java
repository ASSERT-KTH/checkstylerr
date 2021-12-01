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

package com.ait.lienzo.client.core.shape.wires;

import java.util.HashMap;

import com.ait.lienzo.client.core.Attribute;
import com.ait.lienzo.client.core.event.AttributesChangedEvent;
import com.ait.lienzo.client.core.event.AttributesChangedHandler;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.tooling.common.api.java.util.UUID;
import com.ait.tooling.nativetools.client.collection.NFastArrayList;
import com.ait.tooling.nativetools.client.event.HandlerRegistrationManager;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Basic layout container implementation.
 * It hold in memory the required child attributes that are added into layouts to perform the further coordinate calculations.
 * Next -> Use an incremental/differential approach to avoid keeping in memory children attributes.
 */
public class WiresLayoutContainer implements LayoutContainer
{
    private static final LayoutBuilder                      CENTER_LAYOUT                 = new CenterLayoutBuilder();

    private static final LayoutBuilder                      TOP_LAYOUT                    = new TopLayoutBuilder();

    private static final LayoutBuilder                      BOTTOM_LAYOUT                 = new BottomLayoutBuilder();

    private static final LayoutBuilder                      LEFT_LAYOUT                   = new LeftLayoutBuilder();

    private static final LayoutBuilder                      RIGHT_LAYOUT                  = new RightLayoutBuilder();

    private final Group                                     group;

    private Point2D                                         offset;

    private double                                          width;

    private double                                          height;

    private final NFastArrayList<ChildEntry>                children;

    protected HandlerRegistrationManager                    attrHandlerRegs               = new HandlerRegistrationManager();

    protected HashMap<ObjectAttribute, HandlerRegistration> registrations                 = new HashMap<>();

    private final AttributesChangedHandler                  ShapeAttributesChangedHandler = new AttributesChangedHandler()
    {
        @Override
        public void onAttributesChanged(final AttributesChangedEvent event)
        {
            refresh();
        }
    };

    public WiresLayoutContainer()
    {
        this.group = new Group().setDraggable(false);
        this.offset = new Point2D(0, 0);
        this.width = 0;
        this.height = 0;
        this.children = new NFastArrayList<>();
    }

    @Override
    public LayoutContainer setOffset(final Point2D offset)
    {
        this.offset = offset;

        return this;
    }

    @Override
    public WiresLayoutContainer setSize(final double width, final double height)
    {
        this.width = width;

        this.height = height;

        return this;
    }

    public double getWidth()
    {
        return width;
    }

    public double getHeight()
    {
        return height;
    }

    public Point2D getOffset()
    {
        return offset;
    }

    @Override
    public WiresLayoutContainer add(final IPrimitive<?> child)
    {
        return add(child, null);
    }

    @Override
    public WiresLayoutContainer add(final IPrimitive<?> child, final LayoutContainer.Layout layout)
    {
        if (null == child)
        {
            throw new NullPointerException("Child cannot be null.");
        }
        if (null == child.getID())
        {
            child.setID(UUID.uuid());
        }
        addChild(child);

        if (null != layout)
        {
            final ChildEntry entry = new ChildEntry(child.getID(), layout);
            children.add(entry);
            for (final Attribute attribute : child.getTransformingAttributes())
            {
                final HandlerRegistration reg = child.addAttributesChangedHandler(attribute, ShapeAttributesChangedHandler);
                registrations.put(new ObjectAttribute(child, attribute), reg);
                attrHandlerRegs.register(reg);
            }
            doPositionChild(child, true);
        }
        return this;
    }

    protected void addChild(final IPrimitive<?> child)
    {
        group.add(child);
    }

    @Override
    public WiresLayoutContainer remove(final IPrimitive<?> child)
    {
        final ChildEntry entry = getChildEntry(child.getID());

        if (null != entry)
        {
            children.remove(entry);

            for (final Attribute attribute : child.getTransformingAttributes())
            {
                final ObjectAttribute key = new ObjectAttribute(child, attribute);

                attrHandlerRegs.deregister(registrations.remove(key));
            }
        }
        group.remove(child);

        return this;
    }

    @Override
    public LayoutContainer execute()
    {
        for (final IPrimitive<?> child : group.getChildNodes())
        {
            doPositionChild(child, false);
        }
        if (null != getGroup().getLayer())
        {
            getGroup().getLayer().batch();
        }
        return this;
    }

    @Override
    public LayoutContainer refresh()
    {
        for (final ChildEntry entry : children)
        {
            entry.refresh();
        }
        return this;
    }

    private WiresLayoutContainer clear()
    {
        children.clear();

        group.removeAll();

        registrations.clear();

        attrHandlerRegs.clear();

        return this;
    }

    @Override
    public void destroy()
    {
        clear();

        attrHandlerRegs.destroy();

        group.removeFromParent();
    }

    @Override
    public Group getGroup()
    {
        return group;
    }

    private ChildEntry getChildEntry(final String key)
    {
        for (final ChildEntry entry : children)
        {
            if (entry.uuid.equals(key))
            {
                return entry;
            }
        }
        return null;
    }

    private void doPositionChild(final IPrimitive<?> child, final boolean batch)
    {
        final String id = child.getID();

        final ChildEntry entry = getChildEntry(id);

        if (null != entry)
        {
            LayoutBuilder builder = null;

            final LayoutContainer.Layout childLayout = entry.layout;

            switch (childLayout)
            {
                case CENTER:
                    builder = CENTER_LAYOUT;
                    break;
                case TOP:
                    builder = TOP_LAYOUT;
                    break;
                case LEFT:
                    builder = LEFT_LAYOUT;
                    break;
                case BOTTOM:
                    builder = BOTTOM_LAYOUT;
                    break;
                case RIGHT:
                    builder = RIGHT_LAYOUT;
                    break;
            }
            if (null != builder)
            {
                final double[] initial = getInitialCoordinates(entry, child);
                final double c[] = builder.getCoordinates(entry, this);
                final double x = c[0] + initial[0] + offset.getX();
                final double y = c[1] + initial[1] + offset.getY();

                child.setX(x);
                child.setY(y);
                child.moveToTop();

                if (batch && (null != getGroup().getLayer()))
                {
                    getGroup().getLayer().batch();
                }
            }
        }
    }

    /* *******************************************************************
                                LAYOUTS
     ******************************************************************* */

    private interface LayoutBuilder
    {
        public double[] getCoordinates(ChildEntry entry, WiresLayoutContainer cont);
    }

    private static final class CenterLayoutBuilder implements LayoutBuilder
    {
        @Override
        public double[] getCoordinates(final ChildEntry entry, final WiresLayoutContainer cont)
        {
            final double x = cont.getWidth() / 2;

            final double y = cont.getHeight() / 2;

            return new double[] { x, y };
        }
    }

    private static final class TopLayoutBuilder implements LayoutBuilder
    {
        @Override
        public double[] getCoordinates(final ChildEntry entry, final WiresLayoutContainer cont)
        {
            final double x = cont.getWidth() / 2;

            final double bbh = entry.initial_size_attr;

            final double y = bbh / 2;

            return new double[] { x, y };
        }
    }

    private static final class BottomLayoutBuilder implements LayoutBuilder
    {
        @Override
        public double[] getCoordinates(final ChildEntry entry, final WiresLayoutContainer cont)
        {
            final double x = cont.getWidth() / 2;

            final double bbh = entry.initial_size_attr;

            final double y = cont.getHeight() - (bbh / 2);

            return new double[] { x, y };
        }
    }

    private static final class LeftLayoutBuilder implements LayoutBuilder
    {
        @Override
        public double[] getCoordinates(final ChildEntry entry, final WiresLayoutContainer cont)
        {
            final double y = cont.getHeight() / 2;

            final double bbw = entry.initial_size_attr;

            final double x = bbw / 2;

            return new double[] { x, y };
        }
    }

    private static final class RightLayoutBuilder implements LayoutBuilder
    {
        @Override
        public double[] getCoordinates(final ChildEntry entry, final WiresLayoutContainer cont)
        {
            final double y = cont.getHeight() / 2;

            final double bbw = entry.initial_size_attr;

            final double x = cont.getWidth() - (bbw / 2);

            return new double[] { x, y };
        }
    }

    private double[] getInitialCoordinates(final ChildEntry entry, final IPrimitive<?> child)
    {
        if (!entry.isReady())
        {
            initializeChild(entry, child);
        }
        return new double[] { entry.initial_coords.getX(), entry.initial_coords.getY() };
    }

    private void initializeChild(final ChildEntry entry, final IPrimitive<?> child)
    {
        // Initial coordinates.
        final BoundingBox bb = child.getBoundingBox();
        final double[] c = getChildRelativeCoordinates(bb);

        // Size relative attribute.
        final Layout layout = entry.layout;
        final double w = bb.getWidth();
        final double h = bb.getHeight();
        final double cs = (Layout.TOP.equals(layout) || Layout.BOTTOM.equals(layout)) ? h : w;

        entry.initial_coords = new Point2D(c[0], c[1]);
        entry.initial_size_attr = cs;
    }

    private double[] getChildRelativeCoordinates(final BoundingBox bb)
    {
        final double bbx = bb.getX();
        final double bby = bb.getY();
        final double bbw = bb.getWidth();
        final double bbh = bb.getHeight();

        final double x = -bbx - (bbw / 2);
        final double y = -bby - (bbh / 2);

        return new double[] { x, y };
    }

    private class ChildEntry
    {
        private final String                 uuid;

        private final LayoutContainer.Layout layout;

        private Point2D                      initial_coords;

        private Double                       initial_size_attr;

        private ChildEntry(final String uuid, final Layout layout)
        {
            this.uuid = uuid;
            this.layout = layout;
            this.initial_coords = null;
            this.initial_size_attr = null;
        }

        private void refresh()
        {
            this.initial_coords = null;

            this.initial_size_attr = null;
        }

        private boolean isReady()
        {
            return (null != initial_coords) && (null != initial_size_attr);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof ChildEntry))
            {
                return false;
            }
            final ChildEntry that = (ChildEntry) o;

            return uuid.equals(that.uuid);
        }
    }

    private final static class ObjectAttribute
    {
        private final Object    obj;

        private final Attribute attr;

        private ObjectAttribute(final Object obj, final Attribute attr)
        {
            this.obj = obj;

            this.attr = attr;
        }

        @Override
        public final int hashCode()
        {
            return obj.hashCode() ^ attr.hashCode();
        }

        @Override
        public final boolean equals(final Object o)
        {
            if (o instanceof ObjectAttribute)
            {
                final ObjectAttribute other = (ObjectAttribute) o;

                return obj.equals(other.obj) && attr.equals(other.attr);
            }
            return false;
        }
    }
}
