/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.editors.messagebundle;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsUIServlet;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.CmsEditor;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.editors.I_CmsEditor;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorModel.ConfigurableMessages;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.BundleType;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.EditMode;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.TableProperty;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.TranslateTableFieldFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.tepi.filtertable.FilterTable;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

/**
 * Controller for the VAADIN UI of the Message Bundle Editor.
 */
@Theme("opencms")
public class CmsMessageBundleEditor implements I_CmsEditor, I_CmsWindowCloseListener, ViewChangeListener {

    /** Used to implement {@link java.io.Serializable}. */
    private static final long serialVersionUID = 5366955716462191580L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMessageBundleEditor.class);

    /** Messages used by the GUI. */
    static CmsMessages m_messages;

    /** Configurable Messages. */
    ConfigurableMessages m_configurableMessages;

    /** The field factories for the different modes. */
    private final Map<CmsMessageBundleEditorTypes.EditMode, CmsMessageBundleEditorTypes.TranslateTableFieldFactory> m_fieldFactories = new HashMap<CmsMessageBundleEditorTypes.EditMode, CmsMessageBundleEditorTypes.TranslateTableFieldFactory>(
        2);
    /** The cell style generators for the different modes. */
    private final Map<CmsMessageBundleEditorTypes.EditMode, CmsMessageBundleEditorTypes.TranslateTableCellStyleGenerator> m_styleGenerators = new HashMap<CmsMessageBundleEditorTypes.EditMode, CmsMessageBundleEditorTypes.TranslateTableCellStyleGenerator>(
        2);
    /** The model behind the UI. */
    CmsMessageBundleEditorModel m_model;
    /** CmsObject for read / write actions. */
    CmsObject m_cms;
    /** The resource that was opened with the editor. */
    CmsResource m_resource;
    /** The table component that is shown. */
    FilterTable m_table;

    /** The options column, optionally shown in the table. */
    CmsMessageBundleEditorTypes.OptionColumnGenerator m_optionsColumn;

    /** Panel that where the tables short cut handler is attached. */
    private Panel m_navigator;

    /** The place where to go when the editor is closed. */
    private String m_backLink;

    /** Add key button - created in the beginning, but only shown in special settings. */
    private Component m_addKeyButton;

    /** The app's info component. */
    private HorizontalLayout m_appInfo;

    /** The right half of the app info component. */
    private HorizontalLayout m_rightAppInfo;

    /** The text field displaying the name of the currently edited file. */
    private TextField m_fileName;

    /**
     * @see com.vaadin.navigator.ViewChangeListener#afterViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void afterViewChange(ViewChangeEvent event) {

        // do nothing

    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#beforeViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public boolean beforeViewChange(ViewChangeEvent event) {

        cleanUpAction();
        return true;
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#getPriority()
     */
    public int getPriority() {

        return 200;
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#initUI(org.opencms.ui.apps.I_CmsAppUIContext, org.opencms.file.CmsResource, java.lang.String)
     */
    public void initUI(I_CmsAppUIContext context, CmsResource resource, String backLink) {

        m_cms = ((CmsUIServlet)VaadinServlet.getCurrent()).getCmsObject();
        m_messages = Messages.get().getBundle(UI.getCurrent().getLocale());
        m_resource = resource;
        m_backLink = backLink;

        try {
            m_model = new CmsMessageBundleEditorModel(m_cms, m_resource);
            m_configurableMessages = m_model.getConfigurableMessages(m_messages, UI.getCurrent().getLocale());

            fillToolBar(context);
            fillAppInfo(context);

            Component main = createMainComponent();

            if (m_model.hasMasterMode()) {
                m_fieldFactories.put(
                    CmsMessageBundleEditorTypes.EditMode.MASTER,
                    new CmsMessageBundleEditorTypes.TranslateTableFieldFactory(
                        m_table,
                        m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.MASTER)));
                m_styleGenerators.put(
                    CmsMessageBundleEditorTypes.EditMode.MASTER,
                    new CmsMessageBundleEditorTypes.TranslateTableCellStyleGenerator(
                        m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.MASTER)));
            }
            m_fieldFactories.put(
                CmsMessageBundleEditorTypes.EditMode.DEFAULT,
                new CmsMessageBundleEditorTypes.TranslateTableFieldFactory(
                    m_table,
                    m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.DEFAULT)));
            m_styleGenerators.put(
                CmsMessageBundleEditorTypes.EditMode.DEFAULT,
                new CmsMessageBundleEditorTypes.TranslateTableCellStyleGenerator(
                    m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.DEFAULT)));

            m_table.setTableFieldFactory(m_fieldFactories.get(m_model.getEditMode()));
            m_table.setCellStyleGenerator(m_styleGenerators.get(m_model.getEditMode()));

            adjustVisibleColumns();

            context.setAppContent(main);

            adjustFocus();

            if (m_model.getSwitchedLocaleOnOpening()) {
                String caption = m_messages.key(
                    Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_SWITCHED_LOCALE_CAPTION_0);
                String description = m_messages.key(
                    Messages.GUI_NOTIFICATION_MESSAGEBUNDLEEDITOR_SWITCHED_LOCALE_DESCRIPTION_0);
                Notification warningSwitchedLocale = new Notification(caption, description, Type.WARNING_MESSAGE, true);
                warningSwitchedLocale.setDelayMsec(-1);
                warningSwitchedLocale.show(UI.getCurrent().getPage());
            }

        } catch (IOException | CmsException e) {
            LOG.error(m_messages.key(Messages.ERR_LOADING_RESOURCES_0), e);
            Notification.show(m_messages.key(Messages.ERR_LOADING_RESOURCES_0), Type.ERROR_MESSAGE);
            closeAction();
        }

    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#matchesResource(org.opencms.file.CmsResource, boolean)
     */
    public boolean matchesResource(CmsResource resource, boolean plainText) {

        if (plainText) {
            return false;
        }

        String resourceTypeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        if (CmsMessageBundleEditorTypes.BundleType.toBundleType(resourceTypeName) != null) {
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#newInstance()
     */
    public I_CmsEditor newInstance() {

        return new CmsMessageBundleEditor();
    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    public void onWindowClose() {

        cleanUpAction();

    }

    /**
     * Unlocks all resources. Call when closing the editor.
     */
    void closeAction() {

        CmsEditor.openBackLink(m_backLink);
    }

    Map<Object, Object> getFilters() {

        Map<Object, Object> result = new HashMap<Object, Object>(4);
        result.put(TableProperty.KEY, m_table.getFilterFieldValue(TableProperty.KEY));
        result.put(TableProperty.DEFAULT, m_table.getFilterFieldValue(TableProperty.DEFAULT));
        result.put(TableProperty.DESCRIPTION, m_table.getFilterFieldValue(TableProperty.DESCRIPTION));
        result.put(TableProperty.TRANSLATION, m_table.getFilterFieldValue(TableProperty.TRANSLATION));
        return result;
    }

    /**
     * Save the changes.
     */
    void saveAction() {

        try {

            Map<TableProperty, Object> filters = new HashMap<TableProperty, Object>(4);
            filters.put(TableProperty.DEFAULT, m_table.getFilterFieldValue(TableProperty.DEFAULT));
            filters.put(TableProperty.DESCRIPTION, m_table.getFilterFieldValue(TableProperty.DESCRIPTION));
            filters.put(TableProperty.KEY, m_table.getFilterFieldValue(TableProperty.KEY));
            filters.put(TableProperty.TRANSLATION, m_table.getFilterFieldValue(TableProperty.TRANSLATION));

            m_table.clearFilters();

            m_model.save();

            for (TableProperty propertyId : filters.keySet()) {
                m_table.setFilterFieldValue(propertyId, filters.get(propertyId));
            }

        } catch (CmsException e) {
            LOG.error(m_messages.key(Messages.ERR_SAVING_CHANGES_0), e);
        }

    }

    /**
     * Set the edit mode.
     * @param newMode the edit mode to set.
     * @return Flag, indicating if mode switching was successful.
     */
    boolean setEditMode(CmsMessageBundleEditorTypes.EditMode newMode) {

        CmsMessageBundleEditorTypes.EditMode oldMode = m_model.getEditMode();
        boolean success = false;
        if (!newMode.equals(oldMode)) {
            m_table.clearFilters();
            if (m_model.setEditMode(newMode)) {
                m_table.setTableFieldFactory(m_fieldFactories.get(newMode));
                m_table.setCellStyleGenerator(m_styleGenerators.get(newMode));
                adjustOptionsColumn(oldMode, newMode);
                if (newMode.equals(CmsMessageBundleEditorTypes.EditMode.MASTER)) {
                    addAddKeyButton();
                    if (m_table.getItemIds().isEmpty()) {
                        m_table.addItem();
                    }
                } else {
                    removeAddKeyButton();
                }
                success = true;
            } else {
                Notification.show(m_messages.key(Messages.ERR_MODE_CHANGE_NOT_POSSIBLE_0), Type.ERROR_MESSAGE);

            }
            adjustFocus();
        }
        return success;

    }

    void setFilters(Map<Object, Object> filters) {

        for (Object column : filters.keySet()) {
            Object filterValue = filters.get(column);
            if ((filterValue != null) && !filterValue.toString().isEmpty() && !m_table.isColumnCollapsed(column)) {
                m_table.setFilterFieldValue(column, filterValue);
            }
        }
    }

    /**
     * Add the "Add key" button to the app info section.
     */
    private void addAddKeyButton() {

        if (null == m_addKeyButton) {
            m_addKeyButton = createAddKeyButton();
        }
        m_rightAppInfo.addComponent(m_addKeyButton);
        m_rightAppInfo.setComponentAlignment(m_addKeyButton, Alignment.MIDDLE_RIGHT);

    }

    /**
     * Sets the focus to the first editable field of the table.
     */
    private void adjustFocus() {

        Map<Integer, AbstractTextField> firstEditableCol = m_fieldFactories.get(
            m_model.getEditMode()).getValueFields().get(Integer.valueOf(1));
        if (null != firstEditableCol) {
            AbstractTextField firstTextField = firstEditableCol.get(Integer.valueOf(1));
            if (null != firstTextField) {
                firstTextField.focus();
            }
        }

    }

    /**
     * Show or hide the options column dependent on the provided edit mode.
     * @param oldMode the old edit mode
     * @param newMode the edit mode for which the options column's visibility should be adjusted.
     */
    private void adjustOptionsColumn(
        CmsMessageBundleEditorTypes.EditMode oldMode,
        CmsMessageBundleEditorTypes.EditMode newMode) {

        if (m_model.isShowOptionsColumn(oldMode) != m_model.isShowOptionsColumn(newMode)) {
            m_table.removeGeneratedColumn(TableProperty.OPTIONS);
            if (m_model.isShowOptionsColumn(newMode)) {
                m_table.addGeneratedColumn(TableProperty.OPTIONS, m_optionsColumn);
            }
        }
    }

    /**
     * Adjust the visible columns.
     */
    private void adjustVisibleColumns() {

        if (m_table.isColumnCollapsingAllowed()) {
            if ((m_model.hasDefaultValues()) || m_model.getBundleType().equals(BundleType.DESCRIPTOR)) {
                m_table.setColumnCollapsed(TableProperty.DEFAULT, false);
            } else {
                m_table.setColumnCollapsed(TableProperty.DEFAULT, true);
            }

            if (((m_model.getEditMode().equals(EditMode.MASTER) || m_model.hasDescriptionValues()))
                || m_model.getBundleType().equals(BundleType.DESCRIPTOR)) {
                m_table.setColumnCollapsed(TableProperty.DESCRIPTION, false);
            } else {
                m_table.setColumnCollapsed(TableProperty.DESCRIPTION, true);
            }
        }
    }

    /**
     * Unlock all edited resources.
     */
    private void cleanUpAction() {

        try {
            m_model.deleteDescriptorIfNecessary();
        } catch (CmsException e) {
            LOG.error(m_messages.key(Messages.ERR_DELETING_DESCRIPTOR_0), e);
        }
        // unlock resource
        try {
            m_model.unlock();
        } catch (CmsException e) {
            LOG.error(m_messages.key(Messages.ERR_UNLOCKING_RESOURCES_0), e);
        }

    }

    /**
     * Returns a button component. On click, it triggers adding a bundle descriptor.
     * @return a button for adding a descriptor to a bundle.
     */
    @SuppressWarnings("serial")
    private Component createAddDescriptorButton() {

        Button addDescriptorButton = CmsToolBar.createButton(
            FontOpenCms.COPY_LOCALE,
            m_messages.key(Messages.GUI_ADD_DESCRIPTOR_0));

        addDescriptorButton.setDisableOnClick(true);

        addDescriptorButton.addClickListener(new ClickListener() {

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                if (!m_model.addDescriptor()) {
                    CmsVaadinUtils.showAlert(
                        m_messages.key(Messages.ERR_BUNDLE_DESCRIPTOR_CREATION_FAILED_0),
                        m_messages.key(Messages.ERR_BUNDLE_DESCRIPTOR_CREATION_FAILED_DESCRIPTION_0),
                        null);
                } else {
                    try {
                        m_table.setContainerDataSource(m_model.getContainerForCurrentLocale());
                    } catch (IOException | CmsException e) {
                        // Can never appear here, since container is created by addDescriptor already.
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    m_fieldFactories.put(
                        CmsMessageBundleEditorTypes.EditMode.DEFAULT,
                        new CmsMessageBundleEditorTypes.TranslateTableFieldFactory(
                            m_table,
                            m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.DEFAULT)));
                    m_styleGenerators.put(
                        CmsMessageBundleEditorTypes.EditMode.DEFAULT,
                        new CmsMessageBundleEditorTypes.TranslateTableCellStyleGenerator(
                            m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.DEFAULT)));
                    m_fieldFactories.put(
                        CmsMessageBundleEditorTypes.EditMode.MASTER,
                        new CmsMessageBundleEditorTypes.TranslateTableFieldFactory(
                            m_table,
                            m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.MASTER)));
                    m_styleGenerators.put(
                        CmsMessageBundleEditorTypes.EditMode.MASTER,
                        new CmsMessageBundleEditorTypes.TranslateTableCellStyleGenerator(
                            m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.MASTER)));
                    setEditMode(EditMode.MASTER);
                    m_table.setColumnCollapsingAllowed(true);
                    adjustVisibleColumns();
                    // fillAppInfo(m_context);
                    // m_context.removeToolbarButton(event.getComponent());
                }
            }
        });
        return addDescriptorButton;
    }

    /**
     * Returns a button component. On click, it triggers adding a new line to the table.
     * @return a button for adding new lines to the table.
     */
    @SuppressWarnings("serial")
    private Component createAddKeyButton() {

        Button add = new Button();
        add.setDescription(m_messages.key(Messages.GUI_ADD_ROW_0));
        add.setIcon(FontOpenCms.CIRCLE_PLUS, m_messages.key(Messages.GUI_ADD_ROW_0));
        add.addStyleName("icon-only");
        add.addStyleName("borderless-colored");
        add.addStyleName("friendly");
        add.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                m_table.addItem();

            }
        });
        return add;
    }

    /**
     * Create the close button UI Component.
     * @return the close button.
     */
    @SuppressWarnings("serial")
    private Component createCloseButton() {

        Button closeBtn = CmsToolBar.createButton(
            FontOpenCms.CIRCLE_INV_CANCEL,
            m_messages.key(Messages.GUI_BUTTON_CANCEL_0));
        closeBtn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                closeAction();
            }

        });
        return closeBtn;
    }

    /**
     * Create the display for the file path.
     * @return the display for the file path.
     */
    private Component createFilePathDisplay() {

        FormLayout fileNameDisplay = new FormLayout();
        fileNameDisplay.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        fileNameDisplay.setSizeFull();
        m_fileName = new TextField();
        m_fileName.setWidth("100%");
        m_fileName.setValue(m_model.getEditedFilePath());
        m_fileName.setEnabled(true);
        m_fileName.setReadOnly(true);
        m_fileName.setCaption(m_messages.key(Messages.GUI_FILENAME_LABEL_0));
        fileNameDisplay.addComponent(m_fileName);
        fileNameDisplay.setSpacing(true);
        return fileNameDisplay;
    }

    /**
     * Creates the switcher component for the key sets.
     *
     * @return the switcher component.
     */
    @SuppressWarnings("serial")
    private Component createKeysetSwitcher() {

        FormLayout allkeys = new FormLayout();
        allkeys.setHeight("100%");
        allkeys.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        allkeys.setSpacing(true);
        ComboBox allkeysSelect = new ComboBox();
        allkeysSelect.setCaption(m_messages.key(Messages.GUI_KEYSET_SWITCHER_LABEL_0));
        allkeysSelect.setNullSelectionAllowed(false);

        allkeysSelect.addItem(CmsMessageBundleEditorTypes.KeySetMode.ALL);
        allkeysSelect.setItemCaption(
            CmsMessageBundleEditorTypes.KeySetMode.ALL,
            m_messages.key(Messages.GUI_KEYSET_SWITCHER_MODE_ALL_0));
        allkeysSelect.addItem(CmsMessageBundleEditorTypes.KeySetMode.USED_ONLY);
        allkeysSelect.setItemCaption(
            CmsMessageBundleEditorTypes.KeySetMode.USED_ONLY,
            m_messages.key(Messages.GUI_KEYSET_SWITCHER_MODE_ONLY_USED_0));
        allkeysSelect.setValue(m_model.getKeySetMode());
        allkeysSelect.setNewItemsAllowed(false);
        allkeysSelect.setTextInputAllowed(false);

        allkeysSelect.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                Object sortProperty = m_table.getSortContainerPropertyId();
                boolean isAcending = m_table.isSortAscending();
                Map<Object, Object> filters = getFilters();
                m_table.clearFilters();
                m_model.setKeySetMode((CmsMessageBundleEditorTypes.KeySetMode)event.getProperty().getValue());
                m_table.sort(new Object[] {sortProperty}, new boolean[] {isAcending});
                m_table.select(m_table.getCurrentPageFirstItemId());
                setFilters(filters);
            }

        });
        allkeys.addComponent(allkeysSelect);
        return allkeys;
    }

    /**
     * Creates the language switcher UI Component.
     * @return the language switcher.
     */
    @SuppressWarnings("serial")
    private Component createLanguageSwitcher() {

        FormLayout languages = new FormLayout();
        languages.setHeight("100%");
        languages.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        ComboBox languageSelect = new ComboBox();
        languageSelect.setCaption(m_messages.key(Messages.GUI_LANGUAGE_SWITCHER_LABEL_0));
        languageSelect.setNullSelectionAllowed(false);

        // set Locales
        for (Locale locale : m_model.getLocales()) {
            languageSelect.addItem(locale);
            String caption = locale.getDisplayName(UI.getCurrent().getLocale());
            if (CmsLocaleManager.getDefaultLocale().equals(locale)) {
                caption += " ("
                    + Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_DEFAULT_LOCALE_0)
                    + ")";
            }
            languageSelect.setItemCaption(locale, caption);
        }
        languageSelect.setValue(m_model.getLocale());
        languageSelect.setNewItemsAllowed(false);
        languageSelect.setTextInputAllowed(false);

        if (m_model.getLocales().size() > 1) {
            languageSelect.addValueChangeListener(new ValueChangeListener() {

                @SuppressWarnings("synthetic-access")
                public void valueChange(ValueChangeEvent event) {

                    try {
                        Object sortProperty = m_table.getSortContainerPropertyId();
                        boolean isAcending = m_table.isSortAscending();
                        Map<Object, Object> filters = getFilters();
                        m_table.clearFilters();
                        m_model.setLocale((Locale)event.getProperty().getValue());
                        m_fileName.setReadOnly(false);
                        m_fileName.setValue(m_model.getEditedFilePath());
                        m_fileName.setReadOnly(true);
                        m_table.sort(new Object[] {sortProperty}, new boolean[] {isAcending});
                        m_table.select(m_table.getCurrentPageFirstItemId());
                        setFilters(filters);
                    } catch (IOException | CmsException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });

        } else {
            languageSelect.setEnabled(false);
        }
        languages.addComponent(languageSelect);
        return languages;
    }

    /**
     * Creates the main component of the editor with all sub-components.
     * @return the completely filled main component of the editor.
     * @throws IOException thrown if setting the table's content data source fails.
     * @throws CmsException thrown if setting the table's content data source fails.
     */
    private Component createMainComponent() throws IOException, CmsException {

        m_table = createTable();
        m_navigator = new Panel();
        m_navigator.setSizeFull();
        m_navigator.setContent(m_table);
        m_navigator.addActionHandler(new CmsMessageBundleEditorTypes.TableKeyboardHandler(m_table));
        m_navigator.addStyleName("o-message-bundle-editor");

        return m_navigator;
    }

    /** Creates the save button UI Component.
     * @return the save button.
     */
    @SuppressWarnings("serial")
    private Component createSaveButton() {

        Button saveBtn = CmsToolBar.createButton(FontOpenCms.SAVE, m_messages.key(Messages.GUI_BUTTON_SAVE_0));
        saveBtn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                saveAction();
            }

        });
        return saveBtn;
    }

    /** Creates the save and exit button UI Component.
     * @return the save and exit button.
     */
    @SuppressWarnings("serial")
    private Component createSaveExitButton() {

        Button saveExitBtn = CmsToolBar.createButton(
            FontOpenCms.SAVE_EXIT,
            m_messages.key(Messages.GUI_BUTTON_SAVE_AND_EXIT_0));
        saveExitBtn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                saveAction();
                closeAction();

            }
        });
        return saveExitBtn;
    }

    /** Creates the (filled) table UI component.
     * @return the (filled) table
     * @throws IOException thrown if reading the properties file fails.
     * @throws CmsException thrown if some read action for getting the table contentFilter fails.
     */
    private FilterTable createTable() throws IOException, CmsException {

        final FilterTable table = new FilterTable();
        table.setSizeFull();

        table.setContainerDataSource(m_model.getContainerForCurrentLocale());
        if (table.getItemIds().isEmpty() && !m_model.hasDescriptor()) {
            table.addItem();
        }

        table.setColumnHeader(TableProperty.KEY, m_configurableMessages.getColumnHeader(TableProperty.KEY));
        table.setColumnCollapsible(TableProperty.KEY, false);

        table.setColumnHeader(TableProperty.DEFAULT, m_configurableMessages.getColumnHeader(TableProperty.DEFAULT));
        table.setColumnCollapsible(TableProperty.DEFAULT, true);

        table.setColumnHeader(
            TableProperty.DESCRIPTION,
            m_configurableMessages.getColumnHeader(TableProperty.DESCRIPTION));
        table.setColumnCollapsible(TableProperty.DESCRIPTION, true);

        table.setColumnHeader(
            TableProperty.TRANSLATION,
            m_configurableMessages.getColumnHeader(TableProperty.TRANSLATION));
        table.setColumnCollapsible(TableProperty.TRANSLATION, false);

        table.setColumnHeader(TableProperty.OPTIONS, m_configurableMessages.getColumnHeader(TableProperty.OPTIONS));
        table.setFilterDecorator(new CmsMessageBundleEditorFilterDecorator());

        table.setFilterBarVisible(true);
        table.setFilterFieldVisible(TableProperty.OPTIONS, false);
        table.setColumnCollapsible(TableProperty.OPTIONS, false);

        table.setSortEnabled(true);
        table.setEditable(true);

        table.setSelectable(true);
        table.setImmediate(true);
        table.setMultiSelect(false);

        table.setColumnCollapsingAllowed(m_model.hasDescriptor());

        table.setColumnReorderingAllowed(false);

        m_optionsColumn = generateOptionsColumn(table);

        if (m_model.isShowOptionsColumn(m_model.getEditMode())) {
            table.addGeneratedColumn(TableProperty.OPTIONS, m_optionsColumn);
        }
        table.setColumnWidth(TableProperty.OPTIONS, 42);
        table.setColumnExpandRatio(TableProperty.KEY, 1f);
        table.setColumnExpandRatio(TableProperty.DESCRIPTION, 1f);
        table.setColumnExpandRatio(TableProperty.DEFAULT, 1f);
        table.setColumnExpandRatio(TableProperty.TRANSLATION, 1f);

        table.setPageLength(30);
        table.setCacheRate(1);
        table.sort(new Object[] {TableProperty.KEY}, new boolean[] {true});
        table.addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 5418404788437252894L;

            public void itemClick(ItemClickEvent event) {

                Object propertyId = event.getPropertyId();
                Object itemId = event.getItemId();
                int col = m_model.getEditableColumns().indexOf(propertyId);
                if (col >= 0) {
                    AbstractTextField newTF = ((TranslateTableFieldFactory)m_table.getTableFieldFactory()).getValueFields().get(
                        Integer.valueOf(col + 1)).get(itemId);
                    if (newTF != null) {
                        newTF.focus();
                    }
                }

            }
        });
        table.setNullSelectionAllowed(false);
        table.select(table.getCurrentPageFirstItemId());
        return table;
    }

    /** Creates the view switcher UI component.
     * @return the view switcher.
     */
    @SuppressWarnings("serial")
    private Component createViewSwitcher() {

        FormLayout views = new FormLayout();
        views.setHeight("100%");
        views.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

        final ComboBox viewSelect = new ComboBox();
        viewSelect.setCaption(m_messages.key(Messages.GUI_VIEW_SWITCHER_LABEL_0));

        // add Modes
        viewSelect.addItem(CmsMessageBundleEditorTypes.EditMode.DEFAULT);
        viewSelect.setItemCaption(
            CmsMessageBundleEditorTypes.EditMode.DEFAULT,
            m_messages.key(Messages.GUI_VIEW_SWITCHER_EDITMODE_DEFAULT_0));
        viewSelect.addItem(CmsMessageBundleEditorTypes.EditMode.MASTER);
        viewSelect.setItemCaption(
            CmsMessageBundleEditorTypes.EditMode.MASTER,
            m_messages.key(Messages.GUI_VIEW_SWITCHER_EDITMODE_MASTER_0));

        // set current mode as selected
        viewSelect.setValue(m_model.getEditMode());

        viewSelect.setNewItemsAllowed(false);
        viewSelect.setTextInputAllowed(false);
        viewSelect.setNullSelectionAllowed(false);

        viewSelect.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                Object sortProperty = m_table.getSortContainerPropertyId();
                boolean isAcending = m_table.isSortAscending();
                Map<Object, Object> filters = getFilters();
                if (!setEditMode((CmsMessageBundleEditorTypes.EditMode)event.getProperty().getValue())) {
                    viewSelect.setValue(m_model.getEditMode());
                } else {
                    m_table.sort(new Object[] {sortProperty}, new boolean[] {isAcending});
                    m_table.select(m_table.getCurrentPageFirstItemId());
                    setFilters(filters);
                }
            }
        });
        views.addComponent(viewSelect);
        return views;
    }

    /**
     * Adds the compontents to the app info bar.
     * @param context the app UI context.
     */
    private void fillAppInfo(I_CmsAppUIContext context) {

        m_appInfo = new HorizontalLayout();
        m_appInfo.setSizeFull();
        m_appInfo.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        m_appInfo.setSpacing(true);

        HorizontalLayout left = new HorizontalLayout();
        left.setHeight("100%");
        left.setSpacing(true);

        m_rightAppInfo = new HorizontalLayout();
        m_rightAppInfo.setSizeFull();
        m_rightAppInfo.setSpacing(true);

        m_appInfo.addComponent(left);
        m_appInfo.addComponent(m_rightAppInfo);
        m_appInfo.setExpandRatio(m_rightAppInfo, 1f);

        Component languages = createLanguageSwitcher();
        left.addComponent(languages);
        if (!(m_model.hasDescriptor()
            || m_model.getBundleType().equals(CmsMessageBundleEditorTypes.BundleType.DESCRIPTOR))) {
            Component keysetSwitcher = createKeysetSwitcher();
            left.addComponent(keysetSwitcher);
        }
        if (m_model.hasMasterMode()) {
            Component viewSwitcher = createViewSwitcher();
            left.addComponent(viewSwitcher);
        }

        Component fileNameDisplay = createFilePathDisplay();
        m_rightAppInfo.addComponent(fileNameDisplay);
        m_rightAppInfo.setExpandRatio(fileNameDisplay, 2f);

        if (!m_model.hasDescriptor()) {
            addAddKeyButton();
        }
        context.setAppInfo(m_appInfo);
    }

    /** Adds Editor specific UI components to the toolbar.
     * @param context The context that provides access to the toolbar.
     */
    private void fillToolBar(final I_CmsAppUIContext context) {

        context.setAppTitle(m_messages.key(Messages.GUI_APP_TITLE_0));

        // create components
        Component saveBtn = createSaveButton();
        Component saveExitBtn = createSaveExitButton();
        Component closeBtn = createCloseButton();

        context.enableDefaultToolbarButtons(false);
        context.addToolbarButtonRight(closeBtn);
        context.addToolbarButton(saveExitBtn);
        context.addToolbarButton(saveBtn);

        Component addDescriptorBtn = createAddDescriptorButton();
        if (m_model.hasDescriptor() || m_model.getBundleType().equals(BundleType.DESCRIPTOR)) {
            addDescriptorBtn.setEnabled(false);
        }
        context.addToolbarButton(addDescriptorBtn);
    }

    /** Generates the options column for the table.
     * @param table table instance passed to the option column generator
     * @return the options column
     */
    private CmsMessageBundleEditorTypes.OptionColumnGenerator generateOptionsColumn(CustomTable table) {

        return new CmsMessageBundleEditorTypes.OptionColumnGenerator(table);
    }

    /**
     * Removes the "Add key" button from the app info section.
     */
    private void removeAddKeyButton() {

        m_rightAppInfo.removeComponent(m_addKeyButton);
    }
}
