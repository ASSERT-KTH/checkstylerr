/*******************************************************************************
 * Copyright (c) 2014 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.rc.javafx.tester.adapter;

import java.util.List;
import java.util.concurrent.Callable;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.layout.VBox;

import org.eclipse.jubula.rc.common.driver.ClickOptions;
import org.eclipse.jubula.rc.common.driver.IRobot;
import org.eclipse.jubula.rc.common.exception.StepExecutionException;
import org.eclipse.jubula.rc.common.listener.EventLock;
import org.eclipse.jubula.rc.common.tester.adapter.interfaces.IMenuComponent;
import org.eclipse.jubula.rc.common.tester.adapter.interfaces.IMenuItemComponent;
import org.eclipse.jubula.rc.javafx.driver.EventThreadQueuerJavaFXImpl;
import org.eclipse.jubula.tools.internal.constants.TimeoutConstants;
import org.eclipse.jubula.tools.internal.objects.event.EventFactory;
import org.eclipse.jubula.tools.internal.objects.event.TestErrorEvent;

/**
 * Adapter for a MenuItem. The actual values are gained via the Node behind a
 * MenuItem.
 * 
 * @author BREDEX GmbH
 * @param <M>
 * @created 10.2.2014
 */
public class MenuItemAdapter<M extends MenuItem> 
        extends AbstractMenuAdapter<M>
        implements IMenuItemComponent {
    /**
     * Creates an adapter for a MenuItem.
     * 
     * @param objectToAdapt
     *            the object which needs to be adapted
     */
    public MenuItemAdapter(M objectToAdapt) {
        super(objectToAdapt);
    }

    @Override
    public boolean isSelected() {
        return EventThreadQueuerJavaFXImpl.invokeAndWait("isSelected", //$NON-NLS-1$
                new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        MenuItem item = getRealComponent();
                        if (item instanceof Toggle) {
                            return ((Toggle) item).isSelected();
                        } else if (item instanceof CheckMenuItem) {
                            return ((CheckMenuItem) item).isSelected();
                        }
                        return false;
                    }
                });

    }

    @Override
    public String getText() {
        return EventThreadQueuerJavaFXImpl.invokeAndWait("getText", //$NON-NLS-1$
                new Callable<String>() {

                    @Override
                    public String call() throws Exception {
                        return getRealComponent().getText();
                    }

                });
    }

    @Override
    public boolean isEnabled() {
        return EventThreadQueuerJavaFXImpl.invokeAndWait("isEnabled", //$NON-NLS-1$
                new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        return !getRealComponent().isDisable();
                    }
                });
    }

    @Override
    public boolean isExisting() {
        return getRealComponent() != null;
    }

    @Override
    public boolean isShowing() {
        return EventThreadQueuerJavaFXImpl.invokeAndWait("isShowing", //$NON-NLS-1$
                new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        return getRealComponent().isVisible();
                    }
                });
    }

    @Override
    public boolean hasSubMenu() {
        final MenuItem item = getRealComponent();
        return EventThreadQueuerJavaFXImpl.invokeAndWait("hasSubMenu", //$NON-NLS-1$
                new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        if (item instanceof Menu) {
                            return ((Menu) item).getItems().size() > 0;
                        }
                        return false;
                    }
                });
    }

    @Override
    public boolean isSeparator() {
        final MenuItem item = getRealComponent();
        return EventThreadQueuerJavaFXImpl.invokeAndWait("hasSubMenu", //$NON-NLS-1$
                new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        return item instanceof SeparatorMenuItem;
                    }
                });
    }

    @Override
    public void selectMenuItem() {
        clickMenuItem();
    }

    @Override
    public IMenuComponent openSubMenu() {
        final MenuItem item = getRealComponent();
        if (!(item instanceof Menu)) {
            throw new StepExecutionException("unexpected item found", //$NON-NLS-1$
                    EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
        }
        clickMenuItem();
        if (!waitforSubmenuToOpen((Menu) getRealComponent())) {
            throw new StepExecutionException("submenu could not be opened", //$NON-NLS-1$
                    EventFactory.createActionError(TestErrorEvent.
                            POPUP_NOT_FOUND));
        }        
        return new MenuAdapter((Menu) getRealComponent());
    }

    /**
     * This method realizes the waiting for the submenu to open
     * 
     * @param menu
     *            the menu on whose submenu should be waited for it to open
     * @return true if the submenu opened successfully, false if not
     */
    protected boolean waitforSubmenuToOpen(final Menu menu) {
        final EventLock eventLock = new EventLock();
        final EventHandler<Event> shownHandler = 
                new EventHandler<Event>() {

                @Override
                public void handle(Event event) {
                    synchronized (eventLock) {
                        eventLock.notifyAll();
                    }
                }
            };
        EventThreadQueuerJavaFXImpl
                .invokeAndWait("addShownListener", new Callable<Void>() { //$NON-NLS-1$

                    @Override
                    public Void call() throws Exception {
                        menu.addEventHandler(Menu.ON_SHOWN, shownHandler);
                        return null;
                    }
                });
        boolean result = false;
        
        try {
            if (!menu.isShowing()) {
                synchronized (eventLock) {
                    eventLock.wait(TimeoutConstants.
                            SERVER_TIMEOUT_WAIT_FOR_POPUP);
                }
            }
        } catch (InterruptedException e) {
            // ignore
        } finally {
            result = EventThreadQueuerJavaFXImpl.
                    invokeAndWait("openSubMenu", //$NON-NLS-1$
                    new Callable<Boolean>() {

                        @Override
                        public Boolean call() throws Exception {
                            menu.removeEventHandler(Menu.ON_SHOWN,
                                    shownHandler);
                            return menu.isShowing();
                        }
                    });
        }
        return result;
    }

    /**
     * Clicks on a menu item
     */
    protected void clickMenuItem() {
        final IRobot robot = getRobot();
        final MenuItem item = getRealComponent();
        Node[] nodes = EventThreadQueuerJavaFXImpl.invokeAndWait(
                "clickMenuItem", new Callable<Node[]>() { //$NON-NLS-1$

                    @Override
                    public Node[] call() throws Exception {
                        if (item.isDisable()) {
                            throw new StepExecutionException(
                                    "menu item not enabled", //$NON-NLS-1$
                                    EventFactory
                                            .createActionError(TestErrorEvent.
                                                    MENU_ITEM_NOT_ENABLED));
                        }
                        Parent p = (Parent) item.getParentPopup().getSkin()
                                .getNode();
                        List<Node> ctxtMCont = p.getChildrenUnmodifiable();
                        VBox mBox = null;
                        for (Node node : ctxtMCont) {
                            if (node instanceof VBox) {
                                mBox = (VBox) node;
                                break;
                            }
                        }
                        if (mBox == null) {
                            throw new StepExecutionException("No Menu was found", //$NON-NLS-1$
                                    EventFactory
                                            .createActionError(TestErrorEvent.
                                                    POPUP_NOT_FOUND));
                        }
                        int itemIndex = item.getParentPopup().getItems().
                                indexOf(item);
                         /* item.getParentMenu().getItems()
                                .indexOf(item);*/
                        
                        Node itemNode = mBox.getChildrenUnmodifiable().get(
                                itemIndex);

                        Node itemOwner = item.getParentPopup().getOwnerNode();

                        return new Node[] { itemNode, itemOwner };
                    }
                });
        // if the owner of this Menu is an instance of MenuButton, it could be
        // part of a MenuBar. Therefore, we have to move vertical at first.
        if (nodes[1] != null && nodes[1] instanceof MenuButton) {
            robot.click(
                    nodes[0],
                    null,
                    ClickOptions.create()
                            .setFirstHorizontal(false));
        } else {
            robot.click(
                    nodes[0],
                    null);
        }
    }

}
