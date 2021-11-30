/*******************************************************************************
 * Copyright (c) 2018 BREDEX GmbH. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: BREDEX GmbH - initial API and implementation and/or initial
 * documentation
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.jubula.client.ui.rcp.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jubula.client.core.businessprocess.IComponentNameCache;
import org.eclipse.jubula.client.core.model.IAUTMainPO;
import org.eclipse.jubula.client.core.model.IObjectMappingCategoryPO;
import org.eclipse.jubula.client.core.model.IProjectPO;
import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
import org.eclipse.jubula.client.ui.constants.IconConstants;
import org.eclipse.jubula.client.ui.rcp.Plugin;
import org.eclipse.jubula.client.ui.rcp.filter.JBFilteredTree;
import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
import org.eclipse.jubula.client.ui.rcp.provider.contentprovider.objectmapping.OMEditorTreeContentProvider;
import org.eclipse.jubula.client.ui.rcp.provider.labelprovider.OMEditorTreeLabelProvider;
import org.eclipse.jubula.client.ui.utils.LayoutUtil;
import org.eclipse.jubula.tools.internal.constants.StringConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * @author BREDEX GmbH
 *
 */
public class AssocOMtoSpecTCDialog extends TitleAreaDialog {
    /** the button to shift a selection from used list to available tree */
    private Button m_selectionAvailableToUsedButton;
    /** the button to shift all from available tree to used list */
    private Button m_allAvailableToUsedButton;

    /** the button to shift a selection from used list to available tree */
    private Button m_selectionUsedToAvailableButton;
    /** the button to shift all from available tree to used list */
    private Button m_allUsedToAvailableButton;
    /** the selected {@link IAUTMainPO} */
    private IAUTMainPO m_autMain;
    /** the {@link Combo} for selecting the {@link IAUTMainPO} **/
    private Combo m_autSelectorCombo;
    /** list of all {@link IAUTMainPO} for this project */
    private List<IAUTMainPO> m_auts = new ArrayList<>();
    /** the list field for the available items */
    private TreeViewer m_availableTree;

    /** the list field for the available items */
    private TreeViewer m_usedTree;
    /** images for add/remove buttons*/
    private Image[] m_activeButtonContents =
            new Image[] { IconConstants.RIGHT_ARROW_IMAGE,
                IconConstants.DOUBLE_RIGHT_ARROW_IMAGE,
                IconConstants.LEFT_ARROW_IMAGE,
                IconConstants.DOUBLE_LEFT_ARROW_IMAGE };
    /** disabled images for add/remove buttons*/
    private Image[] m_disabledButtonContents =
            new Image[] { IconConstants.RIGHT_ARROW_DIS_IMAGE,
                IconConstants.DOUBLE_RIGHT_ARROW_DIS_IMAGE,
                IconConstants.LEFT_ARROW_DIS_IMAGE,
                IconConstants.DOUBLE_LEFT_ARROW_DIS_IMAGE };
    
    /** selection handler */
    private SelectionListener m_selectionListener =
            new WidgetSelectionListener();
    /** selected categories*/
    private Set<IObjectMappingCategoryPO> m_selectedCat = new HashSet<>();

    /**
     * This private inner class contains a new SelectionListener.
     * 
     * @author BREDEX GmbH
     * @created 10.02.2005
     */
    private class WidgetSelectionListener implements SelectionListener {

        /** {@inheritDoc} */
        public void widgetSelected(SelectionEvent e) {
            Object source = e.getSource();
            if (source.equals(m_availableTree.getTree())
                    || source.equals(m_usedTree.getTree())) {
                checkButtons();
            } else if (source.equals(m_selectionAvailableToUsedButton)) {
                handleSelectionAvailableToUsedButtonEvent();
            } else if (source.equals(m_selectionUsedToAvailableButton)) {
                handleSelectionUsedToAvailableButtonEvent();
            } else if (source.equals(m_allAvailableToUsedButton)) {
                handleSelectionAllAvailableToUsedButtonEvent();
            } else if (source.equals(m_allUsedToAvailableButton)) {
                handleSelectionAllUsedToAvailableButtonEvent();
            } else if (source.equals(m_autSelectorCombo)) {
                handleSelectionAutSelectorEvent();
            }
            checkButtons();

        }

        /** {@inheritDoc} */
        public void widgetDefaultSelected(SelectionEvent e) {
            // do nothing
        }

    }

    /**
     * @param parentShell the parent {@link Shell}
     * @param specTC the {@link ISpecTestCasePO} to change the OMAssoc
     */
    public AssocOMtoSpecTCDialog(Shell parentShell,
            ISpecTestCasePO specTC) {
        super(parentShell);
        List<IObjectMappingCategoryPO> omCategoryAssoc = 
                specTC.getOmCategoryAssoc();
        m_selectedCat.addAll(omCategoryAssoc);
        if (omCategoryAssoc.size() > 0) {
            m_autMain = omCategoryAssoc.get(0).getAutMainParent();
        } else {
            IProjectPO project = GeneralStorage.getInstance().getProject();
            Set<IAUTMainPO> autMainList = project.getAutMainList();
            if (autMainList.size() > 0) {
                m_autMain = autMainList.iterator().next();
            }
        }
        m_auts.addAll(
                GeneralStorage.getInstance().getProject().getAutMainList());

    }

    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea(Composite parent) {
        setTitle(Messages.AssocOMtoSpecTCDialogTitle);
        getShell().setText(Messages.AssocOMtoSpecTCDialogTitle);
        setMessage(Messages.AssocOMtoSpecTCDialogMessage);
        Composite composite = new Composite(parent, SWT.FILL);
        GridLayout parentLayout = new GridLayout(1, false);
        parent.setLayout(parentLayout);
        GridLayout compositeLayout = new GridLayout(1, false);
        composite.setLayout(compositeLayout);
        composite.setLayoutData(createGrabAllGridData());
        new Label(composite, SWT.FILL);
        LayoutUtil.createSeparator(composite);
        m_autSelectorCombo = new Combo(composite, SWT.READ_ONLY);
        m_autSelectorCombo.addSelectionListener(m_selectionListener);
        
        m_autSelectorCombo.setItems(m_auts.stream().map(IAUTMainPO::getName)
                .toArray(String[]::new));
        m_autSelectorCombo.setBounds(50, 50, 150, 65);
        m_autSelectorCombo.select(m_auts.indexOf(m_autMain));
        new Label(composite, SWT.FILL);
        createLayout(composite);
        m_availableTree.setContentProvider(new OMEditorTreeContentProvider(
                Plugin.getActiveCompCache(), m_selectedCat));
        m_availableTree.setInput(m_autMain.getObjMap().getMappedCategory());
        m_usedTree.setInput(m_selectedCat);
        checkButtons();
        Plugin.getHelpSystem().setHelp(parent,
                ContextHelpIds.ASSIGN_OM_CAT_SPEC);
        setHelpAvailable(true);
        return parent;
    }

    /**
     * {@inheritDoc}
     */
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(newShellStyle | SWT.RESIZE);
    }

    /**
     * {@inheritDoc}
     */
    protected Point getInitialSize() {
        final Point initialSize = super.getInitialSize();
        initialSize.x = 800;
        initialSize.y = 600;
        return initialSize;
    }

    /**
     * Creates the layout with Vertical alignment.
     * 
     * @param parent The parent composite.
     */
    private void createLayout(Composite parent) {
        Composite composite = new Composite(parent, SWT.FILL);
        GridLayout compositeLayout = new GridLayout();
        compositeLayout.numColumns = 3;
        compositeLayout.marginHeight = 0;
        compositeLayout.marginWidth = 0;
        composite.setLayout(compositeLayout);
        composite.setLayoutData(createGrabAllGridData());
        Composite compositeLeft =
                createComposite(composite, 1, GridData.FILL, true);

        Composite compositeMiddle =
                createComposite(composite, 1, GridData.FILL, false);

        Composite compositeRight =
                createComposite(composite, 1, GridData.FILL, true);

        m_availableTree = createTree(compositeLeft,
                Messages.AssocOMtoSpecTCDialogAvailableCat, 10);
        m_availableTree.getTree().addSelectionListener(m_selectionListener);

        createShiftButtons(compositeMiddle);

        m_usedTree = createTree(compositeRight,
                Messages.AssocOMtoSpecTCDialogSelectedCat, 10);
        m_usedTree.getTree().addSelectionListener(m_selectionListener);
    }

    /**
     * Creates a new composite.
     * 
     * @param parent The parent composite.
     * @param numColumns the number of columns for this composite.
     * @param alignment The horizontalAlignment.
     * @param horizontalSpace The horizontalSpace.
     * @return The new composite.
     */
    private Composite createComposite(Composite parent, int numColumns,
            int alignment, boolean horizontalSpace) {

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout compositeLayout = new GridLayout();
        compositeLayout.numColumns = numColumns;
        compositeLayout.marginHeight = 0;
        compositeLayout.marginWidth = 0;
        composite.setLayout(compositeLayout);
        GridData compositeData = new GridData();
        compositeData.horizontalAlignment = alignment;
        compositeData.grabExcessHorizontalSpace = horizontalSpace;
        composite.setLayoutData(compositeData);
        return composite;
    }

    /**
     * Creates the 4 arrow buttons
     * 
     * @param parent The parent composite.
     */
    private void createShiftButtons(Composite parent) {
        createLabel(parent, StringConstants.EMPTY);
        m_selectionAvailableToUsedButton = new Button(parent, SWT.PUSH);
        m_selectionAvailableToUsedButton
                .addSelectionListener(m_selectionListener);
        m_selectionAvailableToUsedButton.setLayoutData(createButtonGridData());
        
        m_allAvailableToUsedButton = new Button(parent, SWT.PUSH);
        m_allAvailableToUsedButton.addSelectionListener(m_selectionListener);
        createLabel(parent, StringConstants.EMPTY);
        m_allAvailableToUsedButton.setLayoutData(createButtonGridData());
        
        m_selectionUsedToAvailableButton = new Button(parent, SWT.PUSH);
        m_selectionUsedToAvailableButton
                .addSelectionListener(m_selectionListener);
        m_selectionUsedToAvailableButton.setLayoutData(createButtonGridData());
        
        m_allUsedToAvailableButton = new Button(parent, SWT.PUSH);
        m_allUsedToAvailableButton.addSelectionListener(m_selectionListener);
        m_allUsedToAvailableButton.setLayoutData(createButtonGridData());
    }
    
    /**
     * Creates a label for this composite.
     * 
     * @param text The label text to set.
     * @param parent The composite.
     * @return a new label
     */
    private Label createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        GridData labelGrid = new GridData(GridData.BEGINNING, GridData.CENTER,
                false, false, 1, 1);
        label.setLayoutData(labelGrid);
        return label;
    }

    /**
     * Creates a new tree
     * 
     * @param composite The parent composite.
     * @param labelText The text for the label.
     * @param lines The quantity of lines of this list.
     * @return The new tree.
     */
    private TreeViewer createTree(Composite composite, String labelText,
            int lines) {

        Composite firstComposite =
                createComposite(composite, 2, GridData.BEGINNING, false);
        Composite secondComposite =
                createComposite(composite, 1, GridData.FILL, true);
        Label label = createLabel(firstComposite, labelText);
        secondComposite.setLayoutData(createGrabAllGridData());
        composite.setLayoutData(createGrabAllGridData());
        FilteredTree filtered = new JBFilteredTree(secondComposite,
                LayoutUtil.MULTI_TEXT_STYLE, new PatternFilter(), true);
        TreeViewer tree = filtered.getViewer();
        tree.setAutoExpandLevel(3);
        tree.setData("Label", label); //$NON-NLS-1$
        GridData treeGridData = createGrabAllGridData();
        treeGridData.widthHint = 300;
        IComponentNameCache activeCompCache = Plugin.getActiveCompCache();
        tree.setLabelProvider(
                new OMEditorTreeLabelProvider(activeCompCache, true));
        tree.setContentProvider(
                new OMEditorTreeContentProvider(activeCompCache));
        tree.getTree().setLayoutData(treeGridData);
        return tree;
    }
    
    /**
     * @return  a new {@link GridData} with grabbing and filling horizontal space
     */
    private GridData createButtonGridData() {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        return gridData;
    }
    
    /**
     * @return a new {@link GridData} which grabs and fills horizontal and vertical
     */
    private GridData createGrabAllGridData() {
        return new GridData(GridData.FILL, GridData.FILL, true, true);
    }
    
    /**
     * checks and sets the switch button status
     */
    private void checkButtons() {
        ITreeSelection selection = m_availableTree.getStructuredSelection();
        enableAvailableToUsedButton(!selection.isEmpty());
        for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            Object type = iterator.next();
            if (!(type instanceof IObjectMappingCategoryPO)) {
                enableAvailableToUsedButton(false);
                break;
            }
        }
        ITreeSelection usedSelection = m_usedTree.getStructuredSelection();
        Object firstElement = usedSelection.getFirstElement();
        enableRemoveUsedButton(!usedSelection.isEmpty()
                && m_selectedCat.contains(firstElement));
        for (Iterator<?> iterator = usedSelection.iterator(); iterator
                .hasNext();) {
            Object type = iterator.next();
            if (!(type instanceof IObjectMappingCategoryPO)) {
                enableRemoveUsedButton(false);
                break;
            }
        }
        enableRemoveAllUsedButton(m_selectedCat.size() > 0);
        enableAllAvailableToUsedButton(m_availableTree
                .getVisibleExpandedElements().length != m_availableTree
                        .getTree().getItemCount());
    }
    
    /**
     * enable or disable(and change the icon) of the {@link Button} to move from {@link #m_availableTree} to {@link #m_usedTree}
     * @param enable enable
     */
    private void enableAvailableToUsedButton(boolean enable) {
        enableButton(m_selectionAvailableToUsedButton, enable,
                m_activeButtonContents[0], m_disabledButtonContents[0]);
    }

    /**
     * enable or disable(and change the icon) of the {@link Button} to move all from {@link #m_availableTree} to {@link #m_usedTree}
     * @param enable enable
     */
    private void enableAllAvailableToUsedButton(boolean enable) {
        enableButton(m_allAvailableToUsedButton, enable,
                m_activeButtonContents[1], m_disabledButtonContents[1]);
    }

    /**
     * enable or disable(and change the icon) of the {@link Button} to move from {@link #m_usedTree} to {@link #m_availableTree}
     * @param enable enable
     */
    private void enableRemoveUsedButton(boolean enable) {
        enableButton(m_selectionUsedToAvailableButton, enable,
                m_activeButtonContents[2], m_disabledButtonContents[2]);
    }

    /**
     * enable or disable(and change the icon) of the {@link Button} to move all from {@link #m_usedTree} to {@link #m_availableTree}
     * @param enable enable
     */
    private void enableRemoveAllUsedButton(boolean enable) {
        enableButton(m_allUsedToAvailableButton, enable,
                m_activeButtonContents[3], m_disabledButtonContents[3]);
    }

    /**
     * 
     * @param button the {@link Button}
     * @param enable enable
     * @param activeImage the active {@link Image}
     * @param disabledImage the disabeld {@link Image}
     */
    private void enableButton(Button button, boolean enable, Image activeImage,
            Image disabledImage) {
        button.setEnabled(enable);
        if (enable) {
            button.setImage(activeImage);
        } else {
            button.setImage(disabledImage);
        }
    }
    
    /**
     * 
     */
    private void handleSelectionAutSelectorEvent() {
        IAUTMainPO autMain = m_auts.get(m_autSelectorCombo.getSelectionIndex());
        if (!autMain.equals(m_autMain)) {
            m_autMain = autMain;
            m_selectedCat.clear();
            m_availableTree.setInput(m_autMain.getObjMap().getMappedCategory());
            m_availableTree.refresh();
            m_usedTree.refresh();
            checkButtons();
        }
    }
    /**
     * Handles the selectionEvent for {@link #m_selectionAvailableToUsedButton}
     */
    protected void handleSelectionAvailableToUsedButtonEvent() {
        if (m_selectionAvailableToUsedButton.isEnabled()) {
            IStructuredSelection selection =
                    m_availableTree.getStructuredSelection();
            for (Iterator<?> selIterator = selection.iterator(); selIterator
                    .hasNext();) {
                IObjectMappingCategoryPO iObjectMappingCategoryPO =
                        (IObjectMappingCategoryPO) selIterator.next();
                List<IObjectMappingCategoryPO> toRemove = new ArrayList<>();
                for (Iterator<?> usedIterator = m_selectedCat.iterator();
                        usedIterator.hasNext();) {
                    IObjectMappingCategoryPO selecteCat =
                            (IObjectMappingCategoryPO) usedIterator.next();
                    IObjectMappingCategoryPO parent = selecteCat.getParent();
                    while (parent != null) {
                        if (iObjectMappingCategoryPO.equals(parent)) {
                            usedIterator.remove();
                        }
                        parent = parent.getParent();
                    }
                    
                }
                m_selectedCat.add(iObjectMappingCategoryPO);
            }
            m_usedTree.refresh();
            m_availableTree.refresh();
        }
    }

    /**
     * Handles the selectionEvent for {@link #m_selectionUsedToAvailableButton}
     */
    protected void handleSelectionUsedToAvailableButtonEvent() {
        if (m_selectionUsedToAvailableButton.isEnabled()) {
            IStructuredSelection selection =
                    m_usedTree.getStructuredSelection();
            for (Iterator<?> iterator = selection.iterator(); iterator
                    .hasNext();) {
                IObjectMappingCategoryPO iObjectMappingCategoryPO =
                        (IObjectMappingCategoryPO) iterator.next();
                m_selectedCat.remove(iObjectMappingCategoryPO);
            }
            m_usedTree.refresh();
            m_availableTree.refresh();
        }
    }
    
    /**
     * Handles the selectionEvent for {@link #m_allUsedToAvailableButton}
     */
    private void handleSelectionAllUsedToAvailableButtonEvent() {
        if (m_allUsedToAvailableButton.isEnabled()) {
            m_selectedCat.clear();
            m_usedTree.refresh();
            m_availableTree.refresh();
        }
    }

    /**
     * Handles the selectionEvent for {@link #m_allAvailableToUsedButton}
     */
    private void handleSelectionAllAvailableToUsedButtonEvent() {
        if (m_allAvailableToUsedButton.isEnabled()) {
            m_selectedCat.clear();
            m_selectedCat.addAll(m_autMain.getObjMap().getMappedCategory()
                    .getUnmodifiableCategoryList());
            m_usedTree.refresh();
            m_availableTree.refresh();
        }
    }
    /**
     * @return the {@link IObjectMappingCategoryPO} which should be associated with the {@link ISpecTestCasePO}
     */
    public Collection<IObjectMappingCategoryPO> getSelectedItems() {
        return m_selectedCat;
    }
}
