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

import com.ait.lienzo.client.core.event.NodeDragEndEvent;
import com.ait.lienzo.client.core.event.NodeDragEndHandler;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.wires.event.WiresResizeEndEvent;
import com.ait.lienzo.client.core.shape.wires.event.WiresResizeEndHandler;
import com.ait.lienzo.client.core.shape.wires.handlers.AlignAndDistributeControl;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresConnectorControl;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresConnectorHandler;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresControl;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresControlFactory;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresHandlerFactory;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresShapeControl;
import com.ait.lienzo.client.core.shape.wires.handlers.impl.WiresControlFactoryImpl;
import com.ait.lienzo.client.core.shape.wires.handlers.impl.WiresHandlerFactoryImpl;
import com.ait.lienzo.client.core.shape.wires.handlers.impl.WiresShapeHandler;
import com.ait.lienzo.client.core.types.OnLayerBeforeDraw;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.widget.DragConstraintEnforcer;
import com.ait.lienzo.client.widget.DragContext;
import com.ait.tooling.nativetools.client.collection.NFastArrayList;
import com.ait.tooling.nativetools.client.collection.NFastStringMap;
import com.ait.tooling.nativetools.client.event.HandlerRegistrationManager;

public final class WiresManager
{
    private static final NFastStringMap<WiresManager>        MANAGER_MAP           = new NFastStringMap<>();

    private final MagnetManager                              m_magnetManager       = new MagnetManager();

    private SelectionManager                                 m_selectionManager;

    private WiresDragHandler                                 m_handler;

    private final AlignAndDistribute                         m_index;

    private final NFastStringMap<WiresShape>                 m_shapesMap           = new NFastStringMap<>();

    private final NFastStringMap<HandlerRegistrationManager> m_shapeHandlersMap    = new NFastStringMap<>();

    private final NFastArrayList<WiresConnector>             m_connectorList       = new NFastArrayList<>();

    private final WiresLayer                                 m_layer;

    private WiresControlFactory                              m_controlFactory;

    private WiresHandlerFactory                              m_wiresHandlerFactory;

    private ILocationAcceptor                                m_locationAcceptor    = ILocationAcceptor.ALL;

    private IConnectionAcceptor                              m_connectionAcceptor  = IConnectionAcceptor.ALL;

    private IContainmentAcceptor                             m_containmentAcceptor = IContainmentAcceptor.ALL;

    private IDockingAcceptor                                 m_dockingAcceptor     = IDockingAcceptor.NONE;

    private boolean                                          m_spliceEnabled;

    public static final WiresManager get(final Layer layer)
    {
        final String uuid = layer.uuid();

        WiresManager manager = MANAGER_MAP.get(uuid);

        if (null != manager)
        {
            return manager;
        }
        manager = new WiresManager(layer);

        MANAGER_MAP.put(uuid, manager);

        return manager;
    }

    private WiresManager(final Layer layer)
    {
        m_layer = new WiresLayer(layer);

        m_layer.setWiresManager(this);

        layer.setOnLayerBeforeDraw(new LinePreparer(this));

        m_index = new AlignAndDistribute(layer);

        m_handler = null;
        m_wiresHandlerFactory = new WiresHandlerFactoryImpl();
    }

    public void enableSelectionManager()
    {
        if (m_selectionManager == null)
        {
            m_selectionManager = new SelectionManager(this);
        }
    }

    public boolean isSpliceEnabled()
    {
        return m_spliceEnabled;
    }

    public void setSpliceEnabled(final boolean spliceEnabled)
    {
        m_spliceEnabled = spliceEnabled;
    }

    public static class LinePreparer implements OnLayerBeforeDraw
    {
        private final WiresManager m_wiresManager;

        public LinePreparer(final WiresManager wiresManager)
        {
            m_wiresManager = wiresManager;
        }

        @Override
        public boolean onLayerBeforeDraw(final Layer layer)
        {
            // this is necessary as the line decorator cannot be determined until line parse has been attempted
            // as this is expensive it's delayed until the last minute before draw. As drawing order is not guaranteed
            // this method is used to force a parse on any line that has been refreshed. Refreshed means it's points where
            // changed and thus will be reparsed.
            for (final WiresConnector c : m_wiresManager.getConnectorList())
            {
                if (WiresConnector.updateHeadTailForRefreshedConnector(c))
                {
                    return false;
                }
            }
            return true;
        }
    }

    public MagnetManager getMagnetManager()
    {
        return m_magnetManager;
    }

    public SelectionManager getSelectionManager()
    {
        return m_selectionManager;
    }

    public WiresShapeControl register(final WiresShape shape)
    {
        return register(shape, true);
    }

    public WiresShapeControl register(final WiresShape shape, final boolean addIntoIndex)
    {
        shape.setWiresManager(this);


        final WiresShapeHandler handler = getWiresHandlerFactory().newShapeHandler(getControlFactory().newShapeControl(shape, this),
                                                                                   getControlFactory().newShapeHighlight(this),
                                                                                   this);

        if (addIntoIndex)
        {
            // Shapes added to the align and distribute index.
            handler.getControl().setAlignAndDistributeControl(addToIndex(shape));

            shape.addWiresResizeEndHandler(new WiresResizeEndHandler()
            {
                @Override
                public void onShapeResizeEnd(final WiresResizeEndEvent event)
                {
                    removeFromIndex(shape);

                    handler.getControl().setAlignAndDistributeControl(addToIndex(shape));
                }
            });
        }
        final HandlerRegistrationManager registrationManager = createHandlerRegistrationManager();

        setWiresShapeHandler(shape, registrationManager, handler);

        // Shapes added to the canvas layer by default.
        getLayer().add(shape);

        final String uuid = shape.uuid();

        m_shapesMap.put(uuid, shape);

        m_shapeHandlersMap.put(uuid, registrationManager);

        return handler.getControl();
    }

    public static void setWiresShapeHandler(final WiresShape shape, final HandlerRegistrationManager registrationManager, final WiresShapeHandler handler)
    {
        final Group group = shape.getGroup();

        registrationManager.register(group.addNodeMouseClickHandler(handler));

        registrationManager.register(group.addNodeMouseDownHandler(handler));

        registrationManager.register(group.addNodeMouseUpHandler(handler));

        registrationManager.register(group.addNodeDragEndHandler(handler));

        group.setDragConstraints(handler);

        shape.setWiresShapeControl(handler.getControl());
    }

    public void deregister(final WiresShape shape)
    {
        final String uuid = shape.uuid();

        removeHandlers(uuid);

        removeFromIndex(shape);

        shape.destroy();

        getLayer().remove(shape);

        m_shapesMap.remove(uuid);
    }

    public WiresConnectorControl register(final WiresConnector connector)
    {
        connector.setConnectionAcceptor(m_connectionAcceptor);

        final String uuid = connector.uuid();

        final HandlerRegistrationManager m_registrationManager = createHandlerRegistrationManager();

        final WiresConnectorHandler handler = getWiresHandlerFactory().newConnectorHandler(connector, this);

        connector.setWiresConnectorHandler(m_registrationManager, handler);

        getConnectorList().add(connector);

        m_shapeHandlersMap.put(uuid, m_registrationManager);

        connector.addToLayer(getLayer().getLayer());

        return handler.getControl();
    }

    public void deregister(final WiresConnector connector)
    {
        connector.removeFromLayer();

        final String uuid = connector.uuid();

        removeHandlers(uuid);

        connector.destroy();

        getConnectorList().remove(connector);
    }

    public void resetContext()
    {
        if (null != m_handler)
        {
            m_handler.reset();

            m_handler = null;
        }
    }

    public WiresLayer getLayer()
    {
        return m_layer;
    }

    public WiresShape getShape(final String uuid)
    {
        return m_shapesMap.get(uuid);
    }

    private AlignAndDistributeControl addToIndex(final WiresShape shape)
    {
        return m_index.addShape(shape.getGroup());
    }

    private void removeFromIndex(final WiresShape shape)
    {
        m_index.removeShape(shape.getGroup());
    }

    public AlignAndDistribute getAlignAndDistribute()
    {
        return m_index;
    }

    public void setWiresControlFactory(final WiresControlFactory factory)
    {
        this.m_controlFactory = factory;
    }

    public void setWiresHandlerFactory(WiresHandlerFactory wiresHandlerFactory) {
        this.m_wiresHandlerFactory = wiresHandlerFactory;
    }

    public WiresControlFactory getControlFactory()
    {
        if (null == m_controlFactory)
        {
            m_controlFactory = new WiresControlFactoryImpl();
        }
        return m_controlFactory;
    }

    public WiresHandlerFactory getWiresHandlerFactory() {
        return m_wiresHandlerFactory;
    }

    public IConnectionAcceptor getConnectionAcceptor()
    {
        return m_connectionAcceptor;
    }

    public void setConnectionAcceptor(final IConnectionAcceptor connectionAcceptor)
    {
        m_connectionAcceptor = connectionAcceptor;
    }

    public IContainmentAcceptor getContainmentAcceptor()
    {
        return m_containmentAcceptor;
    }

    public IDockingAcceptor getDockingAcceptor()
    {
        return m_dockingAcceptor;
    }

    public void setContainmentAcceptor(final IContainmentAcceptor containmentAcceptor)
    {
        if (containmentAcceptor == null)
        {
            throw new IllegalArgumentException("ContainmentAcceptor cannot be null");
        }
        m_containmentAcceptor = containmentAcceptor;
    }

    public void setDockingAcceptor(final IDockingAcceptor dockingAcceptor)
    {
        if (dockingAcceptor == null)
        {
            throw new IllegalArgumentException("DockingAcceptor cannot be null");
        }
        m_dockingAcceptor = dockingAcceptor;
    }

    public void setLocationAcceptor(final ILocationAcceptor locationAcceptor)
    {
        if (locationAcceptor == null)
        {
            throw new IllegalArgumentException("LocationAcceptor cannot be null");
        }
        m_locationAcceptor = locationAcceptor;
    }

    public ILocationAcceptor getLocationAcceptor()
    {
        return m_locationAcceptor;
    }

    private void removeHandlers(final String uuid)
    {
        final HandlerRegistrationManager m_registrationManager = m_shapeHandlersMap.get(uuid);

        if (null != m_registrationManager)
        {
            m_registrationManager.removeHandler();
        }
    }

    public NFastArrayList<WiresConnector> getConnectorList()
    {
        return m_connectorList;
    }

    public NFastStringMap<WiresShape> getShapesMap()
    {
        return m_shapesMap;
    }

    HandlerRegistrationManager createHandlerRegistrationManager()
    {
        return new HandlerRegistrationManager();
    }

    public static abstract class WiresDragHandler implements DragConstraintEnforcer, NodeDragEndHandler
    {
        private final WiresManager wiresManager;

        private DragContext        dragContext;

        protected WiresDragHandler(final WiresManager wiresManager)
        {
            this.wiresManager = wiresManager;
        }

        public abstract WiresControl getControl();

        protected abstract boolean doAdjust(Point2D dxy);

        protected abstract void doOnNodeDragEnd(NodeDragEndEvent event);

        @Override
        public void startDrag(final DragContext dragContext)
        {
            this.dragContext = dragContext;

            wiresManager.m_handler = this;
        }

        @Override
        public boolean adjust(final Point2D dxy)
        {
            if (null == dragContext)
            {
                dxy.setX(0);

                dxy.setY(0);

                return true;
            }
            return doAdjust(dxy);
        }

        @Override
        public void onNodeDragEnd(final NodeDragEndEvent event)
        {
            if (null != dragContext)
            {
                doOnNodeDragEnd(event);

                this.dragContext = null;

                wiresManager.m_handler = null;
            }
        }

        public void reset()
        {
            if (null != dragContext)
            {
                doReset();
            }
        }

        protected void doReset()
        {
            dragContext.reset();

            dragContext = null;

            getControl().reset();
        }

        protected WiresManager getWiresManager()
        {
            return wiresManager;
        }
    }
}
