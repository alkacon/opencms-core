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
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorModel.BundleType;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorModel.EditMode;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorModel.KeySetMode;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.tepi.filtertable.FilterTable;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction;
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
import com.vaadin.ui.CustomTable.ColumnGenerator;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

/**
 * Controller for the VAADIN UI of the Message Bundle Editor.
 */
@Theme("opencms")
public class CmsMessageBundleEditor implements I_CmsEditor, I_CmsWindowCloseListener, ViewChangeListener {

    /** Handler to improve the keyboard navigation in the table. */
    @SuppressWarnings("serial")
    static class KbdHandler implements Handler {

        /** The field factory keeps track of the editable rows and the row/col positions of the TextFields. */
        private FilterTable m_table;

        /** Tab was pressed. */
        private Action m_tabNext = new ShortcutAction("Tab", ShortcutAction.KeyCode.TAB, null);
        /** Tab+Shift was pressed. */
        private Action m_tabPrev = new ShortcutAction(
            "Shift+Tab",
            ShortcutAction.KeyCode.TAB,
            new int[] {ShortcutAction.ModifierKey.SHIFT});
        /** Down was pressed. */
        private Action m_curDown = new ShortcutAction("Down", ShortcutAction.KeyCode.ARROW_DOWN, null);
        /** Up was pressed. */
        private Action m_curUp = new ShortcutAction("Up", ShortcutAction.KeyCode.ARROW_UP, null);
        /** Enter was pressed. */
        private Action m_enter = new ShortcutAction("Enter", ShortcutAction.KeyCode.ENTER, null);

        /**
         * Shortcut-Handler to improve the navigation in the table component.
         *
         * @param table the table, the handler is attached to.
         */
        public KbdHandler(final FilterTable table) {
            m_table = table;
        }

        /**
         * @see com.vaadin.event.Action.Handler#getActions(java.lang.Object, java.lang.Object)
         */
        public Action[] getActions(Object target, Object sender) {

            return new Action[] {m_tabNext, m_tabPrev, m_curDown, m_curUp, m_enter};
        }

        /**
         * @see com.vaadin.event.Action.Handler#handleAction(com.vaadin.event.Action, java.lang.Object, java.lang.Object)
         */
        public void handleAction(Action action, Object sender, Object target) {

            TranslateTableFieldFactory fieldFactory = (TranslateTableFieldFactory)m_table.getTableFieldFactory();

            if (target instanceof AbstractTextField) {
                // Move according to keypress
                String data = (String)(((AbstractTextField)target).getData());
                // Abort if no data attribute found
                if (null == data) {
                    return;
                }
                String[] dataItems = data.split(":");
                int colId = Integer.parseInt(dataItems[0]);
                int rowId = Integer.parseInt(dataItems[1]);

                // NOTE: A collection is returned, but actually it's a linked list.
                // It's a hack, but actually I don't know how to do better here.
                List<Integer> visibleItemIds = (List<Integer>)m_table.getVisibleItemIds();

                if ((action == m_curDown) || (action == m_enter)) {
                    int currentRow = visibleItemIds.indexOf(Integer.valueOf(rowId));
                    if (currentRow < (visibleItemIds.size() - 1)) {
                        rowId = visibleItemIds.get(currentRow + 1).intValue();
                    }
                } else if (action == m_curUp) {
                    int currentRow = visibleItemIds.indexOf(Integer.valueOf(rowId));
                    if (currentRow > 0) {
                        rowId = visibleItemIds.get(currentRow - 1).intValue();
                    }
                } else if (action == m_tabNext) {
                    if (colId == fieldFactory.getEditableColumns()) {
                        colId = 1;
                        int currentRow = visibleItemIds.indexOf(Integer.valueOf(rowId));
                        rowId = visibleItemIds.get((currentRow + 1) % visibleItemIds.size()).intValue();
                    } else {
                        colId++;
                    }
                } else if (action == m_tabPrev) {
                    if (colId == 1) {
                        int currentRow = visibleItemIds.indexOf(Integer.valueOf(rowId));
                        colId = fieldFactory.getEditableColumns();
                        rowId = visibleItemIds.get((currentRow - 1) % visibleItemIds.size()).intValue();
                    } else {
                        colId--;
                    }
                }

                AbstractTextField newTF = fieldFactory.getValueFields().get(Integer.valueOf(colId)).get(
                    Integer.valueOf(rowId));
                if (newTF != null) {
                    newTF.focus();
                }
            }
        }
    }

    static class OptionColumnGenerator implements ColumnGenerator {

        Map<Object, Collection<Component>> m_buttons;
        Object m_selectedItem;
        CustomTable m_table;

        public OptionColumnGenerator(CustomTable table) {
            m_buttons = new HashMap<Object, Collection<Component>>();
            m_table = table;
        }

        @SuppressWarnings({"serial", "serial"})
        public Object generateCell(CustomTable source, final Object itemId, Object columnId) {

            HorizontalLayout options = new HorizontalLayout();
            Button delete = new Button();
            delete.addStyleName("icon-only");
            delete.addStyleName("borderless-colored");
            // delete.addStyleName("danger");
            delete.setDescription("Delete this row");
            delete.setIcon(FontOpenCms.CIRCLE_MINUS, "Delete");
            delete.addClickListener(new ClickListener() {

                public void buttonClick(ClickEvent event) {

                    m_table.removeItem(itemId);
                }
            });

            Button add = new Button();
            add.setDescription("Add new row below");
            add.setIcon(FontOpenCms.CIRCLE_PLUS, "Add");
            add.addStyleName("icon-only");
            add.addStyleName("borderless-colored");
            add.addStyleName("friendly");
            add.addClickListener(new ClickListener() {

                public void buttonClick(ClickEvent event) {

                    m_table.addItemAfter(itemId);
                }
            });
            options.addComponent(delete);
            options.addComponent(add);

            Collection<Component> buttons = new ArrayList<Component>(2);
            buttons.add(add);
            buttons.add(delete);
            m_buttons.put(itemId, buttons);

            return options;
        }

        public void selectItem(Object itemId) {

            if ((null != m_selectedItem) && (null != m_buttons.get(m_selectedItem))) {
                for (Component button : m_buttons.get(m_selectedItem)) {
                    button.removeStyleName("borderless");
                    button.addStyleName("borderless-colored");

                }
            }
            m_selectedItem = itemId;
            if ((null != m_selectedItem) && (null != m_buttons.get(m_selectedItem))) {
                for (Component button : m_buttons.get(m_selectedItem)) {
                    button.removeStyleName("borderless-colored");
                    button.addStyleName("borderless");
                }
            }
        }

    }

    /** TableFieldFactory for making only some columns editable and to support enhanced navigation. */
    @SuppressWarnings("serial")
    private static class TranslateTableFieldFactory extends DefaultFieldFactory {

        /** Mapping from column -> row -> AbstractTextField. */
        private final Map<Integer, Map<Integer, AbstractTextField>> m_valueFields;
        /** The editable columns. */
        private final List<String> m_editableColumns;
        /** Reference to the table, the factory is used for. */
        final CustomTable m_table;

        /**
         * Default constructor.
         * @param editableColumns the property names of the editable columns of the table.
         */
        public TranslateTableFieldFactory(CustomTable table, List<String> editableColumns) {
            m_table = table;
            m_valueFields = new HashMap<Integer, Map<Integer, AbstractTextField>>();
            m_editableColumns = editableColumns;
        }

        /**
         * @see com.vaadin.ui.TableFieldFactory#createField(com.vaadin.data.Container, java.lang.Object, java.lang.Object, com.vaadin.ui.Component)
         */
        @Override
        public Field<?> createField(
            final Container container,
            final Object itemId,
            final Object propertyId,
            Component uiContext) {

            String pid = (String)propertyId;

            AbstractTextField tf;
            if (pid.equals(CmsMessageBundleEditorModel.PROPERTY_ID_KEY)) {
                tf = new TextField();
            } else {
                TextArea atf = new TextArea();
                atf.setRows(2);
                tf = atf;
            }
            tf.setWidth("100%");
            tf.setResponsive(true);

            for (int i = 1; i <= m_editableColumns.size(); i++) {
                if (pid.equals(m_editableColumns.get(i - 1))) {
                    tf.setInputPrompt(m_messages.key(Messages.GUI_PLEASE_ADD_VALUE_0));
                    tf.setData(i + ":" + itemId);
                    if (!m_valueFields.containsKey(Integer.valueOf(i))) {
                        m_valueFields.put(Integer.valueOf(i), new HashMap<Integer, AbstractTextField>());
                    }
                    m_valueFields.get(Integer.valueOf(i)).put((Integer)itemId, tf);
                    tf.addFocusListener(new FocusListener() {

                        public void focus(FocusEvent event) {

                            m_table.select(itemId);
                        }

                    });
                    return tf;
                }
            }
            return null;

        }

        /**
         * Returns the number of editable columns.
         * @return the number of editable columns.
         */
        public int getEditableColumns() {

            return m_editableColumns.size();
        }

        /**
         * Returns the mapping from the position in the table to the TextField.
         * @return the mapping from the position in the table to the TextField.
         */
        public Map<Integer, Map<Integer, AbstractTextField>> getValueFields() {

            return m_valueFields;
        }
    }

    /** Used to implement {@link Serializable}. */
    private static final long serialVersionUID = 5366955716462191580L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMessageBundleEditor.class);

    /** Property name for the generated table column with the add/delete buttons. */
    public static final String PROPERTY_ID_OPTIONS = "options";

    /** Messages used by the GUI. */
    static CmsMessages m_messages;

    /** The field factories for the different modes. */
    private final Map<EditMode, TranslateTableFieldFactory> m_fieldFactories = new HashMap<EditMode, TranslateTableFieldFactory>(
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
    OptionColumnGenerator m_optionsColumn;

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

            fillToolBar(context);
            fillAppInfo(context);

            Component main = createMainComponent();

            if (m_model.hasMasterMode()) {
                m_fieldFactories.put(
                    EditMode.MASTER,
                    new TranslateTableFieldFactory(m_table, m_model.getEditableColumns(EditMode.MASTER)));
            }
            m_fieldFactories.put(
                EditMode.DEFAULT,
                new TranslateTableFieldFactory(m_table, m_model.getEditableColumns(EditMode.DEFAULT)));

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
        if (CmsMessageBundleEditorModel.BundleType.toBundleType(resourceTypeName) != null) {
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

        unlockAction();

        CmsEditor.openBackLink(m_backLink);
    }

    /**
     * Save the changes.
     */
    void saveAction() {

        try {

            Map<String, Object> filters = new HashMap<String, Object>(4);
            filters.put(
                CmsMessageBundleEditorModel.PROPERTY_ID_DEFAULT,
                m_table.getFilterFieldValue(CmsMessageBundleEditorModel.PROPERTY_ID_DEFAULT));
            filters.put(
                CmsMessageBundleEditorModel.PROPERTY_ID_DESC,
                m_table.getFilterFieldValue(CmsMessageBundleEditorModel.PROPERTY_ID_DESC));
            filters.put(
                CmsMessageBundleEditorModel.PROPERTY_ID_KEY,
                m_table.getFilterFieldValue(CmsMessageBundleEditorModel.PROPERTY_ID_KEY));
            filters.put(
                CmsMessageBundleEditorModel.PROPERTY_ID_TRANSLATION,
                m_table.getFilterFieldValue(CmsMessageBundleEditorModel.PROPERTY_ID_TRANSLATION));

            m_table.clearFilters();

            m_model.save();

            for (String propertyId : filters.keySet()) {
                m_table.setFilterFieldValue(propertyId, filters.get(propertyId));
            }

        } catch (CmsException e) {
            LOG.error(m_messages.key(Messages.ERR_SAVING_CHANGES_0), e);
        }

    }

    /**
     * Set the edit mode.
     * @param newMode the edit mode to set.
     */
    void setEditMode(EditMode newMode) {

        EditMode oldMode = m_model.getEditMode();
        if (!newMode.equals(oldMode)) {
            m_table.clearFilters();
            m_model.setEditMode(newMode);
            m_table.setTableFieldFactory(m_fieldFactories.get(newMode));
            adjustOptionsColumn(oldMode, newMode);
            if (newMode.equals(EditMode.MASTER)) {
                addAddKeyButton();
                if (m_table.getItemIds().isEmpty()) {
                    m_table.addItem();
                }
            } else {
                removeAddKeyButton();
            }
            adjustFocus();
        }

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
    private void adjustOptionsColumn(EditMode oldMode, EditMode newMode) {

        if (m_model.isShowOptionsColumn(oldMode) != m_model.isShowOptionsColumn(newMode)) {
            if (m_model.isShowOptionsColumn(newMode)) {
                m_table.addGeneratedColumn(PROPERTY_ID_OPTIONS, m_optionsColumn);

            } else {
                m_table.removeGeneratedColumn(PROPERTY_ID_OPTIONS);
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

        allkeysSelect.addItem(KeySetMode.ALL);
        allkeysSelect.setItemCaption(KeySetMode.ALL, m_messages.key(Messages.GUI_KEYSET_SWITCHER_MODE_ALL_0));
        allkeysSelect.addItem(KeySetMode.USED_ONLY);
        allkeysSelect.setItemCaption(
            KeySetMode.USED_ONLY,
            m_messages.key(Messages.GUI_KEYSET_SWITCHER_MODE_ONLY_USED_0));
        allkeysSelect.setValue(m_model.getKeySetMode());
        allkeysSelect.setNewItemsAllowed(false);
        allkeysSelect.setTextInputAllowed(false);

        allkeysSelect.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                m_table.clearFilters();
                m_model.setKeySetMode((KeySetMode)event.getProperty().getValue());
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
        if (m_model.getBundleType().equals(BundleType.PROPERTY)) {
            languageSelect.addItem(CmsMessageBundleEditorModel.DEFAULT_LOCALE);
            languageSelect.setItemCaption(
                CmsMessageBundleEditorModel.DEFAULT_LOCALE,
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

                String locale = m_model.getLocale().equals(CmsMessageBundleEditorModel.DEFAULT_LOCALE)
                ? m_messages.key(Messages.GUI_DEFAULT_LOCALE_0)
                : m_model.getLocale().getDisplayName(UI.getCurrent().getLocale());

                m_table.setColumnHeader(
                    CmsMessageBundleEditorModel.PROPERTY_ID_TRANSLATION,
                    m_messages.key(Messages.GUI_COLUMN_HEADER_TRANSLATION_0) + " (" + locale + ")");
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
        m_navigator.addActionHandler(new KbdHandler(m_table));

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
    @SuppressWarnings("serial")
    private FilterTable createTable() throws IOException, CmsException {

        final FilterTable table = new FilterTable();
        table.setSizeFull();
        table.addStyleName("v-table-wrap-lines");
        table.addStyleName("v-table-alert-empty");

        table.setContainerDataSource(m_model.getContainerForCurrentLocale());
        if (table.getItemIds().isEmpty() && !m_model.hasDescriptor()) {
            table.addItem();
        }
        table.setColumnHeader(
            CmsMessageBundleEditorModel.PROPERTY_ID_KEY,
            m_messages.key(Messages.GUI_COLUMN_HEADER_KEY_0));
        table.setColumnHeader(
            CmsMessageBundleEditorModel.PROPERTY_ID_DEFAULT,
            m_messages.key(Messages.GUI_COLUMN_HEADER_DEFAULT_0));
        table.setColumnHeader(
            CmsMessageBundleEditorModel.PROPERTY_ID_DESC,
            m_messages.key(Messages.GUI_COLUMN_HEADER_DESCRIPTION_0));
        table.setColumnHeader(
            CmsMessageBundleEditorModel.PROPERTY_ID_TRANSLATION,
            m_messages.key(Messages.GUI_COLUMN_HEADER_TRANSLATION_0)
                + " ("
                + m_model.getLocale().getDisplayName()
                + ")");
        table.setColumnHeader(PROPERTY_ID_OPTIONS, m_messages.key(Messages.GUI_COLUMN_HEADER_OPTIONS_0));

        table.setFilterBarVisible(true);
        table.setFilterFieldVisible(PROPERTY_ID_OPTIONS, false);
        table.setSortEnabled(true);
        table.setEditable(true);

        table.setSelectable(true);
        table.setImmediate(true);
        table.setMultiSelect(false);
        table.setColumnCollapsingAllowed(false);

        table.setColumnReorderingAllowed(false);

        m_optionsColumn = generateOptionsColumn(table);

        if (m_model.isShowOptionsColumn(m_model.getEditMode())) {
            table.addGeneratedColumn(PROPERTY_ID_OPTIONS, m_optionsColumn);
        }
        table.setColumnWidth(PROPERTY_ID_OPTIONS, 72);
        table.setColumnExpandRatio(CmsMessageBundleEditorModel.PROPERTY_ID_KEY, 2f);
        table.setColumnExpandRatio(CmsMessageBundleEditorModel.PROPERTY_ID_DESC, 5f);
        table.setColumnExpandRatio(CmsMessageBundleEditorModel.PROPERTY_ID_DEFAULT, 3f);
        table.setColumnExpandRatio(CmsMessageBundleEditorModel.PROPERTY_ID_TRANSLATION, 3f);

        table.addValueChangeListener(new Property.ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                m_optionsColumn.selectItem(table.getValue());
            }
        });

        table.setCacheRate(3);
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

        ComboBox viewSelect = new ComboBox();

        // add Modes
        viewSelect.addItem(EditMode.DEFAULT);
        viewSelect.setItemCaption(EditMode.DEFAULT, m_messages.key(Messages.GUI_VIEW_SWITCHER_EDITMODE_DEFAULT_0));
        viewSelect.addItem(EditMode.MASTER);
        viewSelect.setItemCaption(EditMode.MASTER, m_messages.key(Messages.GUI_VIEW_SWITCHER_EDITMODE_MASTER_0));

        // set current mode as selected
        viewSelect.setValue(m_model.getEditMode());

        viewSelect.setNewItemsAllowed(false);
        viewSelect.setTextInputAllowed(false);
        viewSelect.setNullSelectionAllowed(false);

        viewSelect.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                setEditMode((EditMode)event.getProperty().getValue());
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
        if (!(m_model.hasDescriptor() || m_model.getBundleType().equals(BundleType.DESCRIPTOR))) {
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
    private OptionColumnGenerator generateOptionsColumn(CustomTable table) {

        return new OptionColumnGenerator(table);
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
