/*******************************************************************************
 * Copyright (c) 2004, 2010 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.ui.wizards.pages;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jubula.client.core.persistence.DatabaseConnectionInfo;
import org.eclipse.jubula.client.core.preferences.database.AbstractHostBasedConnectionInfo;
import org.eclipse.jubula.client.core.preferences.database.DatabaseConnection;
import org.eclipse.jubula.client.core.preferences.database.H2ConnectionInfo;
import org.eclipse.jubula.client.core.preferences.database.MySQLConnectionInfo;
import org.eclipse.jubula.client.core.preferences.database.OracleConnectionInfo;
import org.eclipse.jubula.client.core.preferences.database.PostGreSQLConnectionInfo;
import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
import org.eclipse.jubula.client.ui.databinding.SimpleIntegerToStringConverter;
import org.eclipse.jubula.client.ui.databinding.SimpleStringToIntegerConverter;
import org.eclipse.jubula.client.ui.databinding.validators.StringToPortValidator;
import org.eclipse.jubula.client.ui.i18n.Messages;
import org.eclipse.jubula.client.ui.utils.DialogUtils;
import org.eclipse.jubula.client.ui.widgets.UIComponentHelper;
import org.eclipse.jubula.tools.internal.constants.StringConstants;
import org.eclipse.jubula.tools.internal.i18n.I18n;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


/**
 * This wizard page consists of the following for configuring a database 
 * connection:<ul>
 * <li>a general area for configuring the name and type</li>
 * <li>a detail area for configuring specific properties (the contents of 
 *     which depend on the selected type)</li>
 * <li>a concatenated connection URL display area that summarizes the 
 *     information provided in the other areas</li>
 * </ul>
 * @author BREDEX GmbH
 * @created May 19, 2010
 */
@SuppressWarnings("unchecked")
public class DatabaseConnectionWizardPage extends WizardPage {

    /**
     * Responsible for creating the detail area.
     * 
     * @author BREDEX GmbH
     */
    private static interface IDetailAreaBuilder {
        
        /**
         * Creates a detail area for the given parameters.
         * 
         * @param parent The parent that will contain the detail area.
         * @param dbc The data binding context for the detail area.
         */
        public void createDetailArea(Composite parent, 
                DataBindingContext dbc);

        /**
         * 
         * @return a String-representation of the receiver that can be shown 
         *         to the user. 
         */
        public String getTypeName();
        
        /**
         * Attempts to initialize the receiver with the given parameter. If the
         * provided information is not valid for the receiver, then this method
         * will do nothing.
         * 
         * @param sourceInfo The info to use for initialization.
         */
        public void initializeInfo(DatabaseConnectionInfo sourceInfo);
        
        /**
         * 
         * @return the connection info managed by the receiver.
         */
        public DatabaseConnectionInfo getConnectionInfo();
    }

    /**
     * Creates detail area for, and manages, an {@link H2ConnectionInfo}.
     * 
     * @author BREDEX GmbH
     */
    private static final class H2DetailBuilder 
        implements IDetailAreaBuilder {

        /** the managed connection info */
        private H2ConnectionInfo m_connInfo = new H2ConnectionInfo();

        /**
         * 
         * {@inheritDoc}
         */
        public void initializeInfo(DatabaseConnectionInfo sourceInfo) {
            if (sourceInfo instanceof H2ConnectionInfo) {
                m_connInfo = (H2ConnectionInfo)sourceInfo;
            }
        }
        
        /**
         * 
         * {@inheritDoc}
         */
        public void createDetailArea(Composite parent, 
                DataBindingContext dbc) {

            UIComponentHelper.createLabel(
                    parent, I18n.getString("DatabaseConnection.H2.Location"), SWT.NONE); //$NON-NLS-1$
            final Text locationText = createDetailText(parent);
            DialogUtils.setWidgetName(locationText, "H2.Location"); //$NON-NLS-1$
            dbc.bindValue(
                    WidgetProperties.text(SWT.Modify).observe(locationText),
                    BeanProperties.value(H2ConnectionInfo.PROP_NAME_LOCATION)
                    .observe(m_connInfo),
                        new UpdateValueStrategy()
                        .setAfterGetValidator(new IValidator() {
                            public IStatus validate(Object value) {
                                if (StringUtils.isEmpty((String)value)) {
                                    return ValidationStatus.error(
                        Messages.DatabaseConnectionPreferencePageLocationEmpty);
                                }
                                return ValidationStatus.ok();
                            }
                        }), new UpdateValueStrategy());
        }

        /**
         * 
         * {@inheritDoc}
         */
        public String getTypeName() {
            return "H2"; //$NON-NLS-1$
        }

        /**
         * 
         * {@inheritDoc}
         */
        public DatabaseConnectionInfo getConnectionInfo() {
            return m_connInfo;
        }
    
    }

    /**
     * Creates detail area for, and manages, an {@link OracleConnectionInfo}.
     * 
     * @author BREDEX GmbH
     */
    private static final class OracleDetailBuilder 
        implements IDetailAreaBuilder {

        /** the managed connection info */
        private OracleConnectionInfo m_connInfo = new OracleConnectionInfo();
        
        /**
         * 
         * {@inheritDoc}
         */
        
        public void createDetailArea(Composite parent, 
                DataBindingContext dbc) {
            UIComponentHelper.createLabel(parent, 
                    I18n.getString("DatabaseConnection.Oracle.Service"), SWT.NONE); //$NON-NLS-1$
            final Button isServiceCheckBox = new Button(parent, SWT.CHECK);
            ISWTObservableValue selection =
                    WidgetProperties.selection().observe(isServiceCheckBox);
            dbc.bindValue(selection,
                    BeanProperties.value(OracleConnectionInfo.PROP_NAME_SERVICE)
                    .observe(m_connInfo));
            GridDataFactory.fillDefaults().grab(true, false)
                .align(SWT.FILL, SWT.CENTER).applyTo(isServiceCheckBox);
            UIComponentHelper.createLabel(parent, 
                    I18n.getString("DatabaseConnection.HostBased.Hostname"), SWT.NONE); //$NON-NLS-1$
            final Text hostnameText = createDetailText(parent);
            DialogUtils.setWidgetName(hostnameText, "Oracle.Hostname"); //$NON-NLS-1$
            dbc.bindValue(
                    WidgetProperties.text(SWT.Modify).observe(hostnameText),
                    BeanProperties.value(
                            AbstractHostBasedConnectionInfo.PROP_NAME_HOSTNAME)
                    .observe(m_connInfo),
                        new UpdateValueStrategy()
                        .setAfterGetValidator(new IValidator() {
                            public IStatus validate(Object value) {
                                if (StringUtils.isEmpty((String)value)) {
                                    return ValidationStatus.error(
                        Messages.DatabaseConnectionPreferencePageHostnameEmpty);
                                }
                                return ValidationStatus.ok();
                            }
                        }), new UpdateValueStrategy());
            UIComponentHelper.createLabel(parent, 
                    I18n.getString("DatabaseConnection.HostBased.Port"), SWT.NONE); //$NON-NLS-1$
            final Text portText = createDetailText(parent);
            DialogUtils.setWidgetName(portText, "Oracle.Port"); //$NON-NLS-1$
            UpdateValueStrategy portTargetToModelUpdateStrategy =
                new UpdateValueStrategy();
            portTargetToModelUpdateStrategy
                .setConverter(new SimpleStringToIntegerConverter())
                .setAfterGetValidator(new StringToPortValidator(
                        I18n.getString("DatabaseConnection.HostBased.Port"))); //$NON-NLS-1$
            dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(portText),
                    BeanProperties.value(
                            AbstractHostBasedConnectionInfo.PROP_NAME_PORT)
                    .observe(m_connInfo),
                    portTargetToModelUpdateStrategy,
                    new UpdateValueStrategy().setConverter(
                            new SimpleIntegerToStringConverter()));
            String sidLabelText = I18n.getString("DatabaseConnection.Oracle.SID"); //$NON-NLS-1$
            String serviceLabelText = I18n.getString("DatabaseConnection.Oracle.Service"); //$NON-NLS-1$
            Label label = UIComponentHelper.createLabel(parent,
                    m_connInfo.isService()
                        ? serviceLabelText : sidLabelText, SWT.NONE);
            isServiceCheckBox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (m_connInfo.isService()) {
                        label.setText(
                                serviceLabelText + StringConstants.COLON);
                    } else {
                        label.setText(sidLabelText + StringConstants.COLON);
                    }
                    parent.layout();
                }
            });
            final Text schemaText = createDetailText(parent);
            DialogUtils.setWidgetName(schemaText, "Oracle.SID"); //$NON-NLS-1$
            dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(schemaText),
                    BeanProperties.value(
                            AbstractHostBasedConnectionInfo.PROP_NAME_DB_NAME)
                    .observe(m_connInfo),
                        new UpdateValueStrategy()
                        .setAfterGetValidator(new IValidator() {
                            public IStatus validate(Object value) {
                                if (StringUtils.isEmpty((String)value)) {
                                    return ValidationStatus.error(
                        Messages.DatabaseConnectionPreferencePageSIDEmpty);
                                }
                                return ValidationStatus.ok();
                            }
                        }), new UpdateValueStrategy());
        }

        /**
         * 
         * {@inheritDoc}
         */
        public String getTypeName() {
            return "Oracle"; //$NON-NLS-1$
        }

        /**
         * 
         * {@inheritDoc}
         */
        public void initializeInfo(DatabaseConnectionInfo sourceInfo) {
            if (sourceInfo instanceof OracleConnectionInfo) {
                m_connInfo = (OracleConnectionInfo)sourceInfo;
            }
        }

        /**
         * 
         * {@inheritDoc}
         */
        public DatabaseConnectionInfo getConnectionInfo() {
            return m_connInfo;
        }
        
    }

    /**
     * Creates detail area for, and manages, a {@link PostGreSQLConnectionInfo}.
     * 
     * @author BREDEX GmbH
     */
    private static final class PostGreSQLDetailBuilder 
        implements IDetailAreaBuilder {

        /** the managed connection info */
        private PostGreSQLConnectionInfo m_connInfo = 
            new PostGreSQLConnectionInfo();
        
        /**
         * 
         * {@inheritDoc}
         */
        public void createDetailArea(Composite parent, 
                DataBindingContext dbc) {

            UIComponentHelper.createLabel(parent, 
                    I18n.getString("DatabaseConnection.HostBased.Hostname"), SWT.NONE); //$NON-NLS-1$
            final Text hostnameText = createDetailText(parent);
            DialogUtils.setWidgetName(hostnameText, "PostGreSQL.Hostname"); //$NON-NLS-1$
            dbc.bindValue(
                    WidgetProperties.text(SWT.Modify).observe(hostnameText),
                    BeanProperties.value(
                            AbstractHostBasedConnectionInfo.PROP_NAME_HOSTNAME)
                    .observe(m_connInfo),
                    new UpdateValueStrategy()
                    .setAfterGetValidator(new IValidator() {
                        public IStatus validate(Object value) {
                            if (StringUtils.isEmpty((String)value)) {
                                return ValidationStatus.error(
                    Messages.DatabaseConnectionPreferencePageHostnameEmpty);
                            }
                            return ValidationStatus.ok();
                        }
                    }), new UpdateValueStrategy());

            UIComponentHelper.createLabel(parent, 
                    I18n.getString("DatabaseConnection.HostBased.Port"), SWT.NONE); //$NON-NLS-1$
            final Text portText = createDetailText(parent);
            DialogUtils.setWidgetName(portText, "PostGreSQL.Port"); //$NON-NLS-1$
            UpdateValueStrategy portTargetToModelUpdateStrategy =
                new UpdateValueStrategy();
            portTargetToModelUpdateStrategy
                .setConverter(new SimpleStringToIntegerConverter())
                .setAfterGetValidator(new StringToPortValidator(
                        I18n.getString("DatabaseConnection.HostBased.Port"))); //$NON-NLS-1$
            dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(portText),
                    BeanProperties.value(
                            AbstractHostBasedConnectionInfo.PROP_NAME_PORT)
                    .observe(m_connInfo),
                    portTargetToModelUpdateStrategy,
                    new UpdateValueStrategy().setConverter(
                            new SimpleIntegerToStringConverter()));
            
            UIComponentHelper.createLabel(parent, 
                    I18n.getString("DatabaseConnection.PostGreSQL.Database"), SWT.NONE); //$NON-NLS-1$
            final Text schemaText = createDetailText(parent);
            DialogUtils.setWidgetName(schemaText, "PostGreSQL.Database"); //$NON-NLS-1$
            dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(schemaText),
                    BeanProperties.value(
                            AbstractHostBasedConnectionInfo.PROP_NAME_DB_NAME)
                    .observe(m_connInfo),
                        new UpdateValueStrategy()
                        .setAfterGetValidator(new IValidator() {
                            public IStatus validate(Object value) {
                                if (StringUtils.isEmpty((String)value)) {
                                    return ValidationStatus.error(
                        Messages.DatabaseConnectionPreferencePageDatabaseEmpty);
                                }
                                return ValidationStatus.ok();
                            }
                        }), new UpdateValueStrategy());
        }

        /**
         * 
         * {@inheritDoc}
         */
        public String getTypeName() {
            return "(unsupported) PostGreSQL"; //$NON-NLS-1$
        }

        /**
         * 
         * {@inheritDoc}
         */
        public void initializeInfo(DatabaseConnectionInfo sourceInfo) {
            if (sourceInfo instanceof PostGreSQLConnectionInfo) {
                m_connInfo = (PostGreSQLConnectionInfo)sourceInfo;
            }
        }

        /**
         * 
         * {@inheritDoc}
         */
        public DatabaseConnectionInfo getConnectionInfo() {
            return m_connInfo;
        }
        
    }

    /**
     * Creates detail area for, and manages, a {@link PostGreSQLConnectionInfo}.
     * 
     * @author BREDEX GmbH
     */
    private static final class MySQLDetailBuilder 
        implements IDetailAreaBuilder {

        /** the managed connection info */
        private MySQLConnectionInfo m_connInfo = 
            new MySQLConnectionInfo();
        
        /**
         * 
         * {@inheritDoc}
         */
        public void createDetailArea(Composite parent, 
                DataBindingContext dbc) {

            UIComponentHelper.createLabel(parent, 
                    I18n.getString("DatabaseConnection.HostBased.Hostname"), SWT.NONE); //$NON-NLS-1$
            final Text hostnameText = createDetailText(parent);
            DialogUtils.setWidgetName(hostnameText, "MySQL.Hostname"); //$NON-NLS-1$
            dbc.bindValue(
                    WidgetProperties.text(SWT.Modify).observe(hostnameText),
                    BeanProperties.value(
                            AbstractHostBasedConnectionInfo.PROP_NAME_HOSTNAME)
                    .observe(m_connInfo),
                    new UpdateValueStrategy()
                        .setAfterGetValidator(new IValidator() {
                            public IStatus validate(Object value) {
                                if (StringUtils.isEmpty((String)value)) {
                                    return ValidationStatus.error(
                        Messages.DatabaseConnectionPreferencePageHostnameEmpty);
                                }
                                return ValidationStatus.ok();
                            }
                        }), new UpdateValueStrategy());

            
            UIComponentHelper.createLabel(parent, 
                    I18n.getString("DatabaseConnection.HostBased.Port"), SWT.NONE); //$NON-NLS-1$
            
            final Text portText = createDetailText(parent);
            
            DialogUtils.setWidgetName(portText, "MySQL.Port"); //$NON-NLS-1$
            
            UpdateValueStrategy portTargetToModelUpdateStrategy =
                new UpdateValueStrategy();
            
            portTargetToModelUpdateStrategy
                .setConverter(new SimpleStringToIntegerConverter())
                .setAfterGetValidator(new StringToPortValidator(
                        I18n.getString("DatabaseConnection.HostBased.Port"))); //$NON-NLS-1$
            
            dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(portText),
                    BeanProperties.value(
                            AbstractHostBasedConnectionInfo.PROP_NAME_PORT)
                    .observe(m_connInfo),
                    portTargetToModelUpdateStrategy,
                    new UpdateValueStrategy().setConverter(
                            new SimpleIntegerToStringConverter()));
            
            
            UIComponentHelper.createLabel(parent, 
                    I18n.getString("DatabaseConnection.MySQL.Database"), SWT.NONE); //$NON-NLS-1$
            final Text schemaText = createDetailText(parent);
            DialogUtils.setWidgetName(schemaText, "MySQL.Database"); //$NON-NLS-1$
            dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(schemaText),
                    BeanProperties.value(
                            AbstractHostBasedConnectionInfo.PROP_NAME_DB_NAME)
                    .observe(m_connInfo),
                new UpdateValueStrategy().setAfterGetValidator(
                    new IValidator() {
                        public IStatus validate(Object value) {
                            if (StringUtils.isEmpty((String)value)) {
                                return ValidationStatus.error(
                        Messages.DatabaseConnectionPreferencePageDatabaseEmpty);
                            }
                            return ValidationStatus.ok();
                        }
                    }), new UpdateValueStrategy());
            
        }

        /**
         * 
         * {@inheritDoc}
         */
        public String getTypeName() {
            return "(unsupported) MySQL"; //$NON-NLS-1$
        }

        /**
         * 
         * {@inheritDoc}
         */
        public void initializeInfo(DatabaseConnectionInfo sourceInfo) {
            if (sourceInfo instanceof MySQLConnectionInfo) {
                m_connInfo = (MySQLConnectionInfo)sourceInfo;
            }
        }

        /**
         * 
         * {@inheritDoc}
         */
        public DatabaseConnectionInfo getConnectionInfo() {
            return m_connInfo;
        }
        
    }

    /** all available detail area builders */
    private IDetailAreaBuilder[] m_detailAreaBuilders = 
            new IDetailAreaBuilder [] {
                new H2DetailBuilder(),
                new OracleDetailBuilder(),
                new PostGreSQLDetailBuilder(),
                new MySQLDetailBuilder()
            };

    /** the connection to edit within this page */
    private DatabaseConnection m_connectionToEdit;
    
    /**
     * Constructor
     * 
     * @param pageName The name of the page.
     * @param connectionToEdit The object that will be modified based on the
     *                         data entered on this page.
     */
    public DatabaseConnectionWizardPage(String pageName, 
            DatabaseConnection connectionToEdit) {
        
        super(pageName);
        m_connectionToEdit = connectionToEdit;
        for (IDetailAreaBuilder builder : m_detailAreaBuilders) {
            builder.initializeInfo(m_connectionToEdit.getConnectionInfo());
        }
    }

    
    
    /**
     * 
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        setTitle(I18n.getString("DatabaseConnectionWizardPage.title")); //$NON-NLS-1$
        setDescription(I18n.getString("DatabaseConnectionWizardPage.description")); //$NON-NLS-1$
        final DataBindingContext dbc = new DataBindingContext();
        WizardPageSupport.create(this, dbc);
        GridDataFactory textGridDataFactory =
                GridDataFactory.fillDefaults().grab(true, false);
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(
                GridLayoutFactory.fillDefaults().numColumns(2).create());
        setControl(composite);
        UIComponentHelper.createLabel(composite, I18n.getString("DatabaseConnection.Name"), SWT.NONE); //$NON-NLS-1$
        Text nameText = new Text(composite, SWT.BORDER);
        DialogUtils.setWidgetName(nameText, "DatabaseConnection.Name"); //$NON-NLS-1$
        nameText.setLayoutData(textGridDataFactory.create());
        dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(nameText),
            BeanProperties.value(
                    DatabaseConnection.PROP_NAME_NAME)
            .observe(m_connectionToEdit),
            new UpdateValueStrategy()
                .setAfterGetValidator(new IValidator() {
                    public IStatus validate(Object value) {
                        if (StringUtils.isEmpty((String)value)) {
                            return ValidationStatus.error(
                                    I18n.getString("DatabaseConnectionWizardPage.Error.emptyName")); //$NON-NLS-1$
                        }
                        return ValidationStatus.ok();
                    } }), new UpdateValueStrategy());
        nameText.setFocus();
        nameText.selectAll();
        
        UIComponentHelper.createLabel(composite, I18n.getString("DatabaseConnection.Type"), SWT.NONE); //$NON-NLS-1$
        ComboViewer typeComboViewer = new ComboViewer(composite);
        DialogUtils.setWidgetName(typeComboViewer.getControl(), "DatabaseConnection.Type"); //$NON-NLS-1$
        typeComboViewer.setContentProvider(new ArrayContentProvider());
        typeComboViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((IDetailAreaBuilder)element).getTypeName();
            } });
        typeComboViewer.setInput(m_detailAreaBuilders);
        typeComboViewer.getControl().setLayoutData(
                textGridDataFactory.create());
        final Composite detailArea = createDetailArea(composite, 
                nameText.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        DialogUtils.setWidgetName(detailArea, "DatabaseConnection.DetailArea"); //$NON-NLS-1$
        IObservableValue<DatabaseConnection> connectionInfoObservable = 
                BeanProperties.value(
                        DatabaseConnection.PROP_NAME_CONN_INFO)
                .observe(m_connectionToEdit);
        bindComboViewer(dbc, typeComboViewer, detailArea,
                connectionInfoObservable);
        
        Text url = new Text(composite, SWT.BORDER);
        DialogUtils.setWidgetName(url, "DatabaseConnection.URL"); //$NON-NLS-1$
        url.setEditable(false);
        url.setBackground(composite.getBackground());
        url.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
                    .span(2, 1).create());
        dbc.bindValue(WidgetProperties.text().observe(url),
            BeanProperties.value(
                    DatabaseConnectionInfo.PROP_NAME_CONN_URL)
            .observeDetail(connectionInfoObservable),
                new UpdateValueStrategy().setAfterGetValidator(
                    new IValidator() {
                        public IStatus validate(Object value) {
                            if (StringUtils.isEmpty((String)value)) {
                                return ValidationStatus.error(
                                    I18n.getString("DatabaseConnectionWizardPage.Error.emptyName")); //$NON-NLS-1$
                                }
                            return ValidationStatus.ok();
                        } }),  new UpdateValueStrategy());
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, 
                ContextHelpIds.DATABASE_CONNECTION_CONFIGURATION_DIALOG);
    }

    /**
     * {@inheritDoc}
     */
    public void performHelp() {
        PlatformUI.getWorkbench().getHelpSystem().displayHelp(
                ContextHelpIds.DATABASE_CONNECTION_CONFIGURATION_DIALOG);
    }

    /**
     * 
     * @param dbc The data binding context.
     * @param typeComboViewer The combo viewer.
     * @param detailArea The detail area.
     * @param connectionInfoObservable The connection info observable.
     */
    private void bindComboViewer(final DataBindingContext dbc,
            ComboViewer typeComboViewer, final Composite detailArea,
            IObservableValue<DatabaseConnection> connectionInfoObservable) {
        dbc.bindValue(
                ViewersObservables.observeSingleSelection(typeComboViewer), 
                connectionInfoObservable, 
                new UpdateValueStrategy().setConverter(new IConverter() {
                    public Object getToType() {
                        return DatabaseConnectionInfo.class;
                    }
                    public Object getFromType() {
                        return IDetailAreaBuilder.class;
                    }
                    public Object convert(Object fromObject) {
                        for (Control child : detailArea.getChildren()) {
                            child.dispose();
                        }
                        IDetailAreaBuilder fromBuilder = 
                            (IDetailAreaBuilder)fromObject;
                        fromBuilder.createDetailArea(detailArea, dbc);
                        detailArea.layout();
                        ((Composite)getControl()).layout();
                        return fromBuilder.getConnectionInfo();
                    }
                }), new UpdateValueStrategy().setConverter(new IConverter() {
                    
                    public Object getToType() {
                        return IDetailAreaBuilder.class;
                    }
                    
                    public Object getFromType() {
                        return DatabaseConnectionInfo.class;
                    }
                    
                    public Object convert(Object fromObject) {
                        DatabaseConnectionInfo fromInfo =
                            (DatabaseConnectionInfo)fromObject;
                        for (IDetailAreaBuilder builder 
                                : m_detailAreaBuilders) {
                            if (builder.getConnectionInfo() == fromInfo) {
                                for (Control child : detailArea.getChildren()) {
                                    child.dispose();
                                }
                                
                                builder.createDetailArea(detailArea, dbc);
                                
                                detailArea.layout();
                                ((Composite)getControl()).layout();
                                return builder;
                            }
                        }

                        return null;
                    }
                }));
    }

    /**
     * Creates and returns a text field with 
     * {@link org.eclipse.swt.layout.GridData} suitable for
     * sharing a row in a {@link org.eclipse.swt.layout.GridLayout} with a label.
     * 
     * @param parent The parent for the created text field.
     * @return the created text field.
     */
    private static Text createDetailText(Composite parent) {
        final Text detailText = new Text(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false)
            .align(SWT.FILL, SWT.CENTER).applyTo(detailText);
        return detailText;
    }
    
    /**
     * Creates and returns a Composite for containing detailed Database 
     * Connection information.
     * 
     * @param parent The parent of the detail area.
     * @param fieldHeight The height of a text field. Used for layout.
     * @return the created detail area.
     */
    private static Composite createDetailArea(
            Composite parent, int fieldHeight) {
        final GridLayout detailAreaLayout = new GridLayout(2, false);
        final Group detailArea = new Group(parent, SWT.NONE);
        final int numberOfDetailFields = 4;
        final int totalFieldHeight = fieldHeight * numberOfDetailFields;
        final int totalVerticalSpacing = 
            detailAreaLayout.verticalSpacing * (numberOfDetailFields - 1);
        final int totalMarginHeight = (detailAreaLayout.marginHeight * 2) 
            + detailAreaLayout.marginBottom + detailAreaLayout.marginTop;
        final int detailAreaVerticalHint = 
            totalFieldHeight + totalVerticalSpacing + totalMarginHeight;

        detailArea.setText(I18n.getString("DatabaseConnectionWizardPage.DetailArea.title")); //$NON-NLS-1$
        detailArea.setLayoutData(
                GridDataFactory.fillDefaults().grab(true, true)
                    .span(2, 1).hint(SWT.DEFAULT, detailAreaVerticalHint)
                    .create());
        detailArea.setLayout(detailAreaLayout);
        
        return detailArea;
    }
    
}
