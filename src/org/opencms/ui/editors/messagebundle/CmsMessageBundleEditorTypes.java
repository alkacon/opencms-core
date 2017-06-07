/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchException;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.components.extensions.CmsAutoGrowingTextArea;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.tepi.filtertable.FilterTable;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.CustomTable.CellStyleGenerator;
import com.vaadin.ui.CustomTable.ColumnGenerator;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

/** Types and helper classes used by the message bundle editor. */
public final class CmsMessageBundleEditorTypes {

    /** Types of bundles editable by the Editor. */
    public enum BundleType {
        /** A bundle of type propertyvfsbundle. */
        PROPERTY,
        /** A bundle of type xmlvfsbundle. */
        XML,
        /** A bundle descriptor. */
        DESCRIPTOR;

        /**
         * An adjusted version of what is typically Enum.valueOf().
         * @param value the resource type name that should be transformed into BundleType
         * @return The bundle type for the resource type name, or null, if the resource has no bundle type.
         */
        public static BundleType toBundleType(String value) {

            if (null == value) {
                return null;
            }
            if (value.equals(PROPERTY.toString())) {
                return PROPERTY;
            }
            if (value.equals(XML.toString())) {
                return XML;
            }
            if (value.equals(DESCRIPTOR.toString())) {
                return DESCRIPTOR;
            }

            return null;
        }

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {

            switch (this) {
                case PROPERTY:
                    return "propertyvfsbundle";
                case XML:
                    return "xmlvfsbundle";
                case DESCRIPTOR:
                    return "bundledescriptor";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    /** Helper for accessing Bundle descriptor XML contents. */
    public static final class Descriptor {

        /** Message node. */
        public static final String N_MESSAGE = "Message";
        /** Key node. */
        public static final String N_KEY = "Key";
        /** Description node. */
        public static final String N_DESCRIPTION = "Description";
        /** Default node. */
        public static final String N_DEFAULT = "Default";
        /** Locale in which the content is available. */
        public static final Locale LOCALE = new Locale("en");
        /** The mandatory postfix of a bundle descriptor. */
        public static final String POSTFIX = "_desc";

    }

    /** The propertyIds of the table columns. */
    public enum TableProperty {
        /** Table column with the message key. */
        KEY,
        /** Table column with the message description. */
        DESCRIPTION,
        /** Table column with the message's default value. */
        DEFAULT,
        /** Table column with the current (language specific) translation of the message. */
        TRANSLATION,
        /** Table column with the options (add, delete). */
        OPTIONS
    }

    /**
     * Data stored for each editable field in the message table.
     */
    static class ComponentData implements Serializable {

        /** Serialization id. */
        private static final long serialVersionUID = 1L;

        /** Id of the editable column. */
        private int m_editableId;
        /** Id of the table row. */
        private Object m_itemId;
        /** The value in the field when it gets the focus, i.e., before a current edit operation. */
        private String m_lastValue;

        /**
         * Default constructor.
         *
         * @param editableId id of the editable column.
         * @param itemId id of the table row.
         * @param lastValue the value in the field when it gets the focus, i.e., before a current edit operation.
         */
        public ComponentData(int editableId, Object itemId, String lastValue) {
            m_editableId = editableId;
            m_itemId = itemId;
            m_lastValue = lastValue;
        }

        /**
         * Returns the editable column id.
         * @return the editable column id.
         */
        public int getEditableColumnId() {

            return m_editableId;
        }

        /**
         * Returns the id of the table row.
         * @return the id of the table row.
         */
        public Object getItemId() {

            return m_itemId;
        }

        /**
         * Returns the last value in the field (before the current edit operation).
         * @return the last value in the field (before the current edit operation).
         */
        public String getLastValue() {

            return m_lastValue;
        }

        /**
         * Set the last value in the field. Do this when the field is focused.
         * @param lastValue the last value in the field.
         */
        public void setLastValue(String lastValue) {

            m_lastValue = lastValue;
        }
    }

    /** The different edit modes. */
    enum EditMode {
        /** Editing the messages and the descriptor. */
        MASTER,
        /** Only editing messages. */
        DEFAULT
    }

    /**
     * The editor state holds the information on what columns of the editors table
     * should be editable and if the options column should be shown.
     * The state depends on the loaded bundle and the edit mode.
     */
    static class EditorState {

        /** The editable columns (from left to right).*/
        private List<TableProperty> m_editableColumns;
        /** Flag, indicating if the options column should be shown. */
        private boolean m_showOptions;

        /** Constructor, setting all the state information directly.
         * @param editableColumns the property ids of the editable columns (from left to right)
         * @param showOptions flag, indicating if the options column should be shown.
         */
        public EditorState(List<TableProperty> editableColumns, boolean showOptions) {
            m_editableColumns = editableColumns;
            m_showOptions = showOptions;
        }

        /** Returns the editable columns from left to right (as there property ids).
         * @return the editable columns from left to right (as there property ids).
         */
        public List<TableProperty> getEditableColumns() {

            return m_editableColumns;
        }

        /** Returns a flag, indicating if the options column should be shown.
         * @return a flag, indicating if the options column should be shown.
         */
        public boolean isShowOptions() {

            return m_showOptions;
        }
    }

    /** Key change event. */
    static class EntryChangeEvent {

        /** The field via which the key was edited. */
        private AbstractTextField m_source;
        /** The item id of the table row in which the key was edited. */
        private Object m_itemId;
        /** The property id the table column in which the value was edited. */
        private Object m_propertyId;
        /** The value before it was edited. */
        private String m_oldValue;
        /** The value after it was edited. */
        private String m_newValue;

        /** Default constructor.
         * @param source the field via which the entry was edited.
         * @param itemId the item id of the table row in which the entry was edited.
         * @param propertyId the property id of the table column in which the entry was edited
         * @param oldKey the key before it was edited.
         * @param newKey the key after it was edited.
         */
        public EntryChangeEvent(
            AbstractTextField source,
            Object itemId,
            Object propertyId,
            String oldKey,
            String newKey) {
            m_source = source;
            m_itemId = itemId;
            m_propertyId = propertyId;
            m_oldValue = oldKey;
            m_newValue = newKey;
        }

        /**
         * Returns the item id of the table row in which the entry was edited.
         * @return the item id of the table row in which the entry was edited.
         */
        public Object getItemId() {

            return m_itemId;
        }

        /**
         * Returns the value after it was edited.
         * @return the value after it was edited.
         */
        public String getNewValue() {

            return m_newValue;
        }

        /**
         * Returns the value before it was edited.
         * @return the value before it was edited.
         */
        public String getOldValue() {

            return m_oldValue;
        }

        /**
         * Returns the property id of the table column in which the entry was edited.
         * @return the property id of the table column in which the entry was edited.
         */
        public Object getPropertyId() {

            return m_propertyId;
        }

        /**
         * Returns the field via which the entry was edited.
         * @return the field via which the entry was edited.
         */
        public AbstractTextField getSource() {

            return m_source;
        }
    }

    /** Interface for a entry change handler. */
    static interface I_EntryChangeListener {

        /**
         * Called when a entry change event is fired.
         * @param event the entry change event.
         */
        void handleEntryChange(EntryChangeEvent event);
    }

    /** Interface for a item deletion listener. */
    static interface I_ItemDeletionListener {

        /**
         * Called when an item deletion event is fired.
         * @param e the event
         * @return <code>true</code> if deletion handling was successful, <code>false</code> otherwise.
         */
        boolean handleItemDeletion(ItemDeletionEvent e);
    }

    /** Interface for an Listener for changes in the options. */
    static interface I_OptionListener {

        /**
         * Handles adding a key.
         * @param key the key to add.
         * @return flag, indicating if the key was added. If not, it was already present.
         */
        boolean handleAddKey(String key);

        /**
         * Handles a language change.
         * @param language the newly selected language.
         */
        void handleLanguageChange(Locale language);

        /**
         * Handles the change of the edit mode.
         * @param mode the newly selected edit mode.
         */
        void handleModeChange(EditMode mode);

    }

    /** Item deletion event. */
    static class ItemDeletionEvent {

        /** The id of the deleted item. */
        private Object m_itemId;

        /** Default constructor.
         * @param itemId the id of the deleted item.
         */
        public ItemDeletionEvent(Object itemId) {
            m_itemId = itemId;
        }

        /**
         * Returns the id of the deleted item.
         * @return the id of the deleted item.
         */
        public Object getItemId() {

            return m_itemId;
        }

    }

    /** Manages the keys used in at least one locale. */
    static final class KeySet {

        /** Map from keys to the number of locales they are present. */
        Map<Object, Integer> m_keyset;

        /** Default constructor. */
        public KeySet() {
            m_keyset = new HashMap<Object, Integer>();
        }

        /**
         * Returns the current key set.
         * @return the current key set.
         */
        public Set<Object> getKeySet() {

            return new HashSet<Object>(m_keyset.keySet());
        }

        /**
         * Removes the given key.
         * @param key the key to be removed.
         */
        public void removeKey(final String key) {

            m_keyset.remove(key);
        }

        /**
         * Rename a key.
         * @param oldKey the current key name.
         * @param newKey the substitution for the key name.
         */
        public void renameKey(String oldKey, String newKey) {

            if (m_keyset.containsKey(oldKey) && !m_keyset.containsKey(newKey)) {
                Integer count = m_keyset.get(oldKey);
                m_keyset.remove(oldKey);
                m_keyset.put(newKey, count);
            } else {
                //TODO: should never be the case, but handle it anyway?
            }

        }

        /**
         * Updates the set with all keys that are used in at least one language.
         * @param oldKeys keys of a locale as registered before
         * @param newKeys keys of the locale now
         */
        public void updateKeySet(Set<Object> oldKeys, Set<Object> newKeys) {

            // Remove keys that are not present anymore
            if (null != oldKeys) {
                Set<Object> removedKeys = new HashSet<Object>(oldKeys);
                if (null != newKeys) {
                    removedKeys.removeAll(newKeys);
                }
                for (Object key : removedKeys) {
                    Integer i = m_keyset.get(key);
                    int uses = null != i ? i.intValue() : 0;
                    if (uses > 1) {
                        m_keyset.put(key, Integer.valueOf(uses - 1));
                    } else if (uses == 1) {
                        m_keyset.remove(key);
                    }
                }
            }

            // Add keys that are new
            if (null != newKeys) {
                Set<Object> addedKeys = new HashSet<Object>(newKeys);
                if (null != oldKeys) {
                    addedKeys.removeAll(oldKeys);
                }
                for (Object key : addedKeys) {
                    if (m_keyset.containsKey(key)) {
                        m_keyset.put(key, Integer.valueOf(m_keyset.get(key).intValue() + 1));
                    } else {
                        m_keyset.put(key, Integer.valueOf(1));
                    }
                }
            }

        }

    }

    /** Validates keys. */
    @SuppressWarnings("serial")
    static class KeyValidator extends AbstractStringValidator {

        /**
         * Default constructor.
         */
        public KeyValidator() {
            super(Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_INVALID_KEY_0));

        }

        /**
         * @see com.vaadin.data.validator.AbstractValidator#isValidValue(java.lang.Object)
         */
        @Override
        protected boolean isValidValue(String value) {

            if (null == value) {
                return true;
            }
            return !value.matches(".*\\p{IsWhite_Space}.*");
        }

    }

    /** A column generator that additionally adjusts the appearance of the options buttons to selection changes on the table. */
    @SuppressWarnings("serial")
    static class OptionColumnGenerator implements ColumnGenerator {

        /** Map from itemId (row) -> option buttons in the row. */
        Map<Object, Collection<Component>> m_buttons;
        /** The id of the currently selected item (row). */
        Object m_selectedItem;
        /** The table, the column is generated for. */
        CustomTable m_table;
        /** The key deletion listeners. */
        I_ItemDeletionListener m_listener;

        /**
         * Default constructor.
         *
         * @param table the table, for which the column is generated for.
         */
        public OptionColumnGenerator(final CustomTable table) {
            m_buttons = new HashMap<Object, Collection<Component>>();
            m_table = table;
            m_table.addValueChangeListener(new Property.ValueChangeListener() {

                public void valueChange(ValueChangeEvent event) {

                    selectItem(m_table.getValue());
                }
            });

        }

        /**
         * @see com.vaadin.ui.CustomTable.ColumnGenerator#generateCell(com.vaadin.ui.CustomTable, java.lang.Object, java.lang.Object)
         */
        public Object generateCell(final CustomTable source, final Object itemId, final Object columnId) {

            CmsMessages messages = Messages.get().getBundle(UI.getCurrent().getLocale());
            HorizontalLayout options = new HorizontalLayout();
            Button delete = new Button();
            delete.addStyleName("icon-only");
            delete.addStyleName("borderless-colored");
            delete.setDescription(messages.key(Messages.GUI_REMOVE_ROW_0));
            delete.setIcon(FontOpenCms.CIRCLE_MINUS, messages.key(Messages.GUI_REMOVE_ROW_0));
            delete.addClickListener(new ClickListener() {

                public void buttonClick(ClickEvent event) {

                    ItemDeletionEvent e = new ItemDeletionEvent(itemId);
                    if ((null == m_listener) || m_listener.handleItemDeletion(e)) {
                        m_table.removeItem(itemId);
                    }
                }
            });

            options.addComponent(delete);

            Collection<Component> buttons = new ArrayList<Component>(1);
            buttons.add(delete);
            m_buttons.put(itemId, buttons);

            if (source.isSelected(itemId)) {
                selectItem(itemId);
            }

            return options;
        }

        /**
         * Registers an item deletion listener. Only one listener can be registered.
         * Registering a new listener will automatically unregister the previous one.
         *
         * @param listener the listener to register.
         */
        void registerItemDeletionListener(final I_ItemDeletionListener listener) {

            m_listener = listener;
        }

        /**
         * Call this method, when a new item is selected. It will adjust the style of the option buttons, thus that they stay visible.
         *
         * @param itemId the id of the newly selected item (row).
         */
        void selectItem(final Object itemId) {

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

    /** Handler to improve the keyboard navigation in the table. */
    @SuppressWarnings("serial")
    static class TableKeyboardHandler implements Handler {

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
        public TableKeyboardHandler(final FilterTable table) {
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
            List<TableProperty> editableColums = fieldFactory.getEditableColumns();

            if (target instanceof AbstractTextField) {
                // Move according to keypress
                ComponentData data = (ComponentData)(((AbstractTextField)target).getData());
                // Abort if no data attribute found
                if (null == data) {
                    return;
                }
                int colId = data.getEditableColumnId();
                Integer rowIdInteger = (Integer)data.getItemId();
                @SuppressWarnings("boxing") // rowIdInteger should never be null
                int rowId = Integer.valueOf(rowIdInteger);

                // TODO: Find a better solution?
                // NOTE: A collection is returned, but actually it's a linked list.
                // It's a hack, but actually I don't know how to do better here.
                @SuppressWarnings("unchecked")
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
                    int nextColId = getNextColId(editableColums, colId);
                    if (colId >= nextColId) {
                        int currentRow = visibleItemIds.indexOf(Integer.valueOf(rowId));
                        rowId = visibleItemIds.get((currentRow + 1) % visibleItemIds.size()).intValue();
                    }
                    colId = nextColId;
                } else if (action == m_tabPrev) {
                    int previousColId = getPreviousColId(editableColums, colId);
                    if (colId <= previousColId) {
                        int currentRow = visibleItemIds.indexOf(Integer.valueOf(rowId));
                        rowId = visibleItemIds.get(
                            ((currentRow + visibleItemIds.size()) - 1) % visibleItemIds.size()).intValue();
                    }
                    colId = previousColId;
                }

                AbstractTextField newTF = fieldFactory.getValueFields().get(Integer.valueOf(colId)).get(
                    Integer.valueOf(rowId));
                if (newTF != null) {
                    newTF.focus();
                }
            }
        }

        /**
         * Calculates the id of the next editable column.
         * @param editableColumns all editable columns
         * @param colId id (index in <code>editableColumns</code> plus 1) of the current column.
         * @return id of the next editable column.
         */
        private int getNextColId(List<TableProperty> editableColumns, int colId) {

            for (int i = colId % editableColumns.size(); i != (colId - 1); i = (i + 1) % editableColumns.size()) {
                if (!m_table.isColumnCollapsed(editableColumns.get(i))) {
                    return i + 1;
                }
            }
            return colId;
        }

        /**
         * Calculates the id of the previous editable column.
         * @param editableColumns all editable columns
         * @param colId id (index in <code>editableColumns</code> plus 1) of the current column.
         * @return id of the previous editable column.
         */
        private int getPreviousColId(List<TableProperty> editableColumns, int colId) {

            // use +4 instead of -1 to prevent negativ numbers
            for (int i = ((colId + editableColumns.size()) - 2) % editableColumns.size(); i != (colId
                - 1); i = ((i + editableColumns.size()) - 1) % editableColumns.size()) {
                if (!m_table.isColumnCollapsed(editableColumns.get(i))) {
                    return i + 1;
                }
            }
            return colId;
        }
    }

    /** Custom cell style generator to allow different style for editable columns. */
    @SuppressWarnings("serial")
    static class TranslateTableCellStyleGenerator implements CellStyleGenerator {

        /** The editable columns. */
        private List<TableProperty> m_editableColums;

        /**
         * Default constructor, taking the list of editable columns.
         *
         * @param editableColumns the list of editable columns.
         */
        public TranslateTableCellStyleGenerator(List<TableProperty> editableColumns) {
            m_editableColums = editableColumns;
            if (null == m_editableColums) {
                m_editableColums = new ArrayList<TableProperty>();
            }
        }

        /**
         * @see com.vaadin.ui.CustomTable.CellStyleGenerator#getStyle(com.vaadin.ui.CustomTable, java.lang.Object, java.lang.Object)
         */
        public String getStyle(CustomTable source, Object itemId, Object propertyId) {

            String result = TableProperty.KEY.equals(propertyId) ? "key-" : "";
            result += m_editableColums.contains(propertyId) ? "editable" : "fix";
            return result;
        }

    }

    /** TableFieldFactory for making only some columns editable and to support enhanced navigation. */
    @SuppressWarnings("serial")
    static class TranslateTableFieldFactory extends DefaultFieldFactory {

        /** Mapping from column -> row -> AbstractTextField. */
        private final Map<Integer, Map<Integer, AbstractTextField>> m_valueFields;
        /** The editable columns. */
        private final List<TableProperty> m_editableColumns;
        /** Reference to the table, the factory is used for. */
        final CustomTable m_table;
        /** Registered key change listeners. */
        private final Set<I_EntryChangeListener> m_keyChangeListeners = new HashSet<I_EntryChangeListener>();

        /**
         * Default constructor.
         * @param table The table, the factory is used for.
         * @param editableColumns the property names of the editable columns of the table.
         */
        public TranslateTableFieldFactory(CustomTable table, List<TableProperty> editableColumns) {
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

            final TableProperty pid = (TableProperty)propertyId;

            for (int i = 1; i <= m_editableColumns.size(); i++) {
                if (pid.equals(m_editableColumns.get(i - 1))) {

                    AbstractTextField tf;
                    if (pid.equals(TableProperty.KEY)) {
                        tf = new TextField();
                        tf.addValidator(new KeyValidator());
                    } else {
                        TextArea atf = new TextArea();
                        atf.setRows(1);
                        CmsAutoGrowingTextArea.addTo(atf, 20);
                        tf = atf;
                    }
                    tf.setWidth("100%");
                    tf.setResponsive(true);

                    tf.setInputPrompt(
                        Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_PLEASE_ADD_VALUE_0));
                    tf.setData(new ComponentData(i, itemId, ""));
                    if (!m_valueFields.containsKey(Integer.valueOf(i))) {
                        m_valueFields.put(Integer.valueOf(i), new HashMap<Integer, AbstractTextField>());
                    }
                    m_valueFields.get(Integer.valueOf(i)).put((Integer)itemId, tf);
                    tf.addFocusListener(new FocusListener() {

                        public void focus(FocusEvent event) {

                            if (!m_table.isSelected(itemId)) {
                                m_table.select(itemId);
                            }
                            AbstractTextField field = (AbstractTextField)event.getComponent();
                            // Update last value
                            ComponentData data = (ComponentData)field.getData();
                            data.setLastValue(field.getValue());
                            field.setData(data);
                        }

                    });
                    tf.addBlurListener(new BlurListener() {

                        public void blur(BlurEvent event) {

                            AbstractTextField field = (AbstractTextField)event.getComponent();
                            ComponentData data = (ComponentData)field.getData();
                            if (!data.getLastValue().equals(field.getValue())) {
                                EntryChangeEvent ev = new EntryChangeEvent(
                                    field,
                                    data.getItemId(),
                                    pid,
                                    data.getLastValue(),
                                    field.getValue());
                                fireKeyChangeEvent(ev);
                            }
                        }
                    });
                    return tf;
                }
            }
            return null;

        }

        /**
         * Returns the editable columns.
         * @return the editable columns.
         */
        public List<TableProperty> getEditableColumns() {

            return m_editableColumns;
        }

        /**
         * Returns the mapping from the position in the table to the TextField.
         * @return the mapping from the position in the table to the TextField.
         */
        public Map<Integer, Map<Integer, AbstractTextField>> getValueFields() {

            return m_valueFields;
        }

        /**
         * Register a key change listener.
         * @param listener the listener to register.
         */
        public void registerKeyChangeListener(final I_EntryChangeListener listener) {

            m_keyChangeListeners.add(listener);
        }

        /**
         * Called to fire a key change event.
         * @param ev the event to fire.
         */
        void fireKeyChangeEvent(final EntryChangeEvent ev) {

            for (I_EntryChangeListener listener : m_keyChangeListeners) {
                listener.handleEntryChange(ev);
            }
        }
    }

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsMessageBundleEditorTypes.class);

    /** The width of the options column in pixel. */
    public static final int OPTION_COLUMN_WIDTH = 42;

    /** The width of the options column in pixel. */
    public static final String OPTION_COLUMN_WIDTH_PX = OPTION_COLUMN_WIDTH + "px";

    /** Hide default constructor. */
    private CmsMessageBundleEditorTypes() {
        //noop
    }

    /**
     * Returns the bundle descriptor for the bundle with the provided base name.
     * @param cms {@link CmsObject} used for searching.
     * @param basename the bundle base name, for which the descriptor is searched.
     * @return the bundle descriptor, or <code>null</code> if it does not exist or searching fails.
     */
    public static CmsResource getDescriptor(CmsObject cms, String basename) {

        CmsSolrQuery query = new CmsSolrQuery();
        query.setResourceTypes(CmsMessageBundleEditorTypes.BundleType.DESCRIPTOR.toString());
        query.setFilterQueries("filename:\"" + basename + CmsMessageBundleEditorTypes.Descriptor.POSTFIX + "\"");
        query.add("fl", "path");
        CmsSolrResultList results;
        try {
            boolean isOnlineProject = cms.getRequestContext().getCurrentProject().isOnlineProject();
            String indexName = isOnlineProject
            ? CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE
            : CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE;
            results = OpenCms.getSearchManager().getIndexSolr(indexName).search(cms, query, true, null, true, null);
        } catch (CmsSearchException e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_BUNDLE_DESCRIPTOR_SEARCH_ERROR_0), e);
            return null;
        }

        switch (results.size()) {
            case 0:
                return null;
            case 1:
                return results.get(0);
            default:
                String files = "";
                for (CmsResource res : results) {
                    files += " " + res.getRootPath();
                }
                LOG.warn(Messages.get().getBundle().key(Messages.ERR_BUNDLE_DESCRIPTOR_NOT_UNIQUE_1, files));
                return results.get(0);
        }
    }

    /**
     * Displays a localized warning.
     * @param caption the caption of the warning.
     * @param description the description of the warning.
     */
    static void showWarning(final String caption, final String description) {

        Notification warning = new Notification(caption, description, Type.WARNING_MESSAGE, true);
        warning.setDelayMsec(-1);
        warning.show(UI.getCurrent().getPage());

    }
}
