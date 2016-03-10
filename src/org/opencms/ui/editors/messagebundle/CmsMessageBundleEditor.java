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
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsUIServlet;
import org.opencms.main.OpenCms;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.CmsEditor;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.editors.I_CmsEditor;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.TableProperty;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.tepi.filtertable.FilterTable;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;

/**
 * Controller for the VAADIN UI of the Message Bundle Editor.
 */
@Theme("opencms")
public class CmsMessageBundleEditor implements I_CmsEditor, I_CmsWindowCloseListener, ViewChangeListener {

    /** Used to implement {@link Serializable}. */
    private static final long serialVersionUID = 5366955716462191580L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMessageBundleEditor.class);

    /** Property name for the generated table column with the add/delete buttons. */
    // public static final String PROPERTY_ID_OPTIONS = "options";

    /** Messages used by the GUI. */
    static CmsMessages m_messages;

    /** Configurable Messages. */
    CmsMessages m_configurableMessages;

    /** The field factories for the different modes. */
    private final Map<CmsMessageBundleEditorTypes.EditMode, CmsMessageBundleEditorTypes.TranslateTableFieldFactory> m_fieldFactories = new HashMap<CmsMessageBundleEditorTypes.EditMode, CmsMessageBundleEditorTypes.TranslateTableFieldFactory>(
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

        unlockAction();
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
            }
            m_fieldFactories.put(
                CmsMessageBundleEditorTypes.EditMode.DEFAULT,
                new CmsMessageBundleEditorTypes.TranslateTableFieldFactory(
                    m_table,
                    m_model.getEditableColumns(CmsMessageBundleEditorTypes.EditMode.DEFAULT)));

            m_table.setTableFieldFactory(m_fieldFactories.get(m_model.getEditMode()));

            context.setAppContent(main);

            adjustFocus();

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

        unlockAction();

    }

    /**
     * Unlocks all resources. Call when closing the editor.
     */
    void closeAction() {

        CmsEditor.openBackLink(m_backLink);
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

    /**
     * Add the "Add key" button to the app info section.
     */
    private void addAddKeyButton() {

        if (null == m_addKeyButton) {
            m_addKeyButton = createAddKeyButton();
        }
        m_appInfo.addComponent(m_addKeyButton);
        m_appInfo.setComponentAlignment(m_addKeyButton, Alignment.MIDDLE_RIGHT);

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
            if (m_model.isShowOptionsColumn(newMode)) {
                m_table.addGeneratedColumn(TableProperty.OPTIONS, m_optionsColumn);

            } else {
                m_table.removeGeneratedColumn(TableProperty.OPTIONS);
            }
        }
    }

    /**
     * Returns a button component. On click, it triggers adding a new line to the table.
     * @return a button for adding new lines to the table.
     */
    @SuppressWarnings("serial")
    private Component createAddKeyButton() {

        Button addKeyButton = new Button();
        addKeyButton.setCaption(m_messages.key(Messages.GUI_ADD_KEY_0));
        // addKeyButton.setHeight("100%");
        addKeyButton.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                m_table.addItem();

            }
        });
        return addKeyButton;
    }

    /**
     * Create the close button UI Component.
     * @return the close button.
     */
    @SuppressWarnings("serial")
    private Component createCloseButton() {

        Button closeBtn = CmsToolBar.createButton(FontOpenCms.CIRCLE_INV_CANCEL, "Cancel");
        closeBtn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                closeAction();
            }

        });
        return closeBtn;
    }

    /**
     * Creates the switcher component for the key sets.
     *
     * @return the switcher component.
     */
    @SuppressWarnings("serial")
    private Component createKeysetSwitcher() {

        HorizontalLayout allkeys = new HorizontalLayout();
        allkeys.setHeight("100%");
        allkeys.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        allkeys.setSpacing(true);
        Label allkeysLabel = new Label(m_messages.key(Messages.GUI_KEYSET_SWITCHER_LABEL_0));
        ComboBox allkeysSelect = new ComboBox();
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

                m_table.clearFilters();
                m_model.setKeySetMode((CmsMessageBundleEditorTypes.KeySetMode)event.getProperty().getValue());
            }

        });
        allkeys.addComponent(allkeysLabel);
        allkeys.addComponent(allkeysSelect);
        return allkeys;
    }

    /**
     * Creates the language switcher UI Component.
     * @return the language switcher.
     */
    @SuppressWarnings("serial")
    private Component createLanguageSwitcher() {

        HorizontalLayout languages = new HorizontalLayout();
        languages.setHeight("100%");
        languages.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        languages.setSpacing(true);
        Label languageLabel = new Label(m_messages.key(Messages.GUI_LANGUAGE_SWITCHER_LABEL_0));
        ComboBox languageSelect = new ComboBox();
        languageSelect.setNullSelectionAllowed(false);

        // set Locales
        if (m_model.getBundleType().equals(CmsMessageBundleEditorTypes.BundleType.PROPERTY)) {
            languageSelect.addItem(CmsMessageBundleEditorTypes.DEFAULT_LOCALE);
            languageSelect.setItemCaption(
                CmsMessageBundleEditorTypes.DEFAULT_LOCALE,
                m_messages.key(Messages.GUI_DEFAULT_LOCALE_0));
        }
        for (Locale locale : m_model.getLocales()) {
            languageSelect.addItem(locale);
            languageSelect.setItemCaption(locale, locale.getDisplayName(UI.getCurrent().getLocale()));
        }
        languageSelect.setValue(m_model.getLocale());
        languageSelect.setNewItemsAllowed(false);
        languageSelect.setTextInputAllowed(false);

        languageSelect.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                try {
                    m_table.clearFilters();
                    m_model.setLocale((Locale)event.getProperty().getValue());
                } catch (IOException | CmsException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String locale = m_model.getLocale().equals(CmsMessageBundleEditorTypes.DEFAULT_LOCALE)
                ? m_messages.key(Messages.GUI_DEFAULT_LOCALE_0)
                : m_model.getLocale().getDisplayName(UI.getCurrent().getLocale());

                m_table.setColumnHeader(
                    TableProperty.TRANSLATION,
                    m_configurableMessages.key(Messages.GUI_COLUMN_HEADER_TRANSLATION_0) + " (" + locale + ")");
            }
        });
        languages.addComponent(languageLabel);
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

        return m_navigator;
    }

    /** Creates the save button UI Component.
     * @return the save button.
     */
    @SuppressWarnings("serial")
    private Component createSaveButton() {

        Button saveBtn = CmsToolBar.createButton(FontOpenCms.SAVE, "Save");
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

        Button saveExitBtn = CmsToolBar.createButton(FontOpenCms.SAVE_EXIT, "Save and Exit");
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
        table.addStyleName("v-table-wrap-lines");
        table.addStyleName("v-table-alert-empty");

        table.setContainerDataSource(m_model.getContainerForCurrentLocale());
        if (table.getItemIds().isEmpty() && !m_model.hasDescriptor()) {
            table.addItem();
        }
        table.setColumnHeader(TableProperty.KEY, m_configurableMessages.getString(Messages.GUI_COLUMN_HEADER_KEY_0));
        table.setColumnHeader(TableProperty.DEFAULT, m_configurableMessages.key(Messages.GUI_COLUMN_HEADER_DEFAULT_0));
        table.setColumnHeader(
            TableProperty.DESCRIPTION,
            m_configurableMessages.key(Messages.GUI_COLUMN_HEADER_DESCRIPTION_0));
        table.setColumnHeader(
            TableProperty.TRANSLATION,
            m_configurableMessages.key(Messages.GUI_COLUMN_HEADER_TRANSLATION_0)
                + " ("
                + m_model.getLocale().getDisplayName()
                + ")");
        table.setColumnHeader(TableProperty.OPTIONS, m_configurableMessages.key(Messages.GUI_COLUMN_HEADER_OPTIONS_0));

        table.setFilterBarVisible(true);
        table.setFilterFieldVisible(TableProperty.OPTIONS, false);
        table.setSortEnabled(true);
        table.setEditable(true);

        table.setSelectable(true);
        table.setImmediate(true);
        table.setMultiSelect(false);
        table.setColumnCollapsingAllowed(false);

        table.setColumnReorderingAllowed(false);

        m_optionsColumn = generateOptionsColumn(table);

        if (m_model.isShowOptionsColumn(m_model.getEditMode())) {
            table.addGeneratedColumn(TableProperty.OPTIONS, m_optionsColumn);
        }
        table.setColumnWidth(TableProperty.OPTIONS, 72);
        table.setColumnExpandRatio(TableProperty.KEY, 2f);
        table.setColumnExpandRatio(TableProperty.DESCRIPTION, 5f);
        table.setColumnExpandRatio(TableProperty.DEFAULT, 3f);
        table.setColumnExpandRatio(TableProperty.TRANSLATION, 3f);

        table.setPageLength(30);
        table.setCacheRate(1);
        table.sort(new Object[] {TableProperty.KEY}, new boolean[] {true});
        return table;
    }

    /** Creates the view switcher UI component.
     * @return the view switcher.
     */
    @SuppressWarnings("serial")
    private Component createViewSwitcher() {

        HorizontalLayout views = new HorizontalLayout();
        views.setHeight("100%");
        views.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        views.setSpacing(true);

        Label viewLabel = new Label(m_messages.key(Messages.GUI_VIEW_SWITCHER_LABEL_0));

        final ComboBox viewSelect = new ComboBox();

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

                if (!setEditMode((CmsMessageBundleEditorTypes.EditMode)event.getProperty().getValue())) {
                    viewSelect.setValue(m_model.getEditMode());
                }
            }
        });
        views.addComponent(viewLabel);
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
        if (m_model.getLocales().size() > 1) {
            Component languages = createLanguageSwitcher();
            m_appInfo.addComponent(languages);
        }
        if (!(m_model.hasDescriptor()
            || m_model.getBundleType().equals(CmsMessageBundleEditorTypes.BundleType.DESCRIPTOR))) {
            Component keysetSwitcher = createKeysetSwitcher();
            m_appInfo.addComponent(keysetSwitcher);
        }
        if (m_model.hasMasterMode()) {
            Component viewSwitcher = createViewSwitcher();
            m_appInfo.addComponent(viewSwitcher);
        }
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

        context.addToolbarButton(closeBtn);
        context.addToolbarButton(saveExitBtn);
        context.addToolbarButton(saveBtn);
    }

    /** Generates the options column for the table.
     * @return the options column
     */
    private CmsMessageBundleEditorTypes.OptionColumnGenerator generateOptionsColumn(CustomTable table) {

        return new CmsMessageBundleEditorTypes.OptionColumnGenerator(table);
    }

    /**
     * Removes the "Add key" button from the app info section.
     */
    private void removeAddKeyButton() {

        m_appInfo.removeComponent(m_addKeyButton);
    }

    /**
     * Unlock all edited resources.
     */
    private void unlockAction() {

        // unlock resource
        try {
            m_model.unlock();
        } catch (CmsException e) {
            LOG.error(m_messages.key(Messages.ERR_UNLOCKING_RESOURCES_0), e);
        }

    }
}
