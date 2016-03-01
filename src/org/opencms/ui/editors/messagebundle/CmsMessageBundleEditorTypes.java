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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.ui.FontOpenCms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tepi.filtertable.FilterTable;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.CustomTable.ColumnGenerator;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/** Types and helper classes used by the message bundle editor. */
public class CmsMessageBundleEditorTypes {

    /** Types of bundles editable by the Editor. */
    public enum BundleType {
        /** A bundle of type propertyvfsbundle. */
        PROPERTY, /** A bundle of type xmlvfsbundle. */
        XML, /** A bundle descriptor. */
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

    /** The different edit modes. */
    enum EditMode {
        /** Editing the messages and the descriptor. */
        MASTER, /** Only editing messages. */
        DEFAULT
    }

    /**
     * The editor state holds the information on what columns of the editors table
     * should be editable and if the options column should be shown.
     * The state depends on the loaded bundle and the edit mode.
     */
    static class EditorState {

        /** The editable columns (from left to right).*/
        private List<Object> m_editableColumns;
        /** Flag, indicating if the options column should be shown. */
        private boolean m_showOptions;

        /** Constructor, setting all the state information directly.
         * @param editableColumns the property ids of the editable columns (from left to right)
         * @param showOptions flag, indicating if the options column should be shown.
         */
        public EditorState(List<Object> editableColumns, boolean showOptions) {
            m_editableColumns = editableColumns;
            m_showOptions = showOptions;
        }

        /** Returns the editable columns from left to right (as there property ids).
         * @return the editable columns from left to right (as there property ids).
         */
        public List<Object> getEditableColumns() {

            return m_editableColumns;
        }

        /** Returns a flag, indicating if the options column should be shown.
         * @return a flag, indicating if the options column should be shown.
         */
        public boolean isShowOptions() {

            return m_showOptions;
        }
    }

    /** Manages the keys used in at least one locale. */
    static final class KeySet {

        /** Map from keys to the number of locales they are present. */
        Map<String, Integer> m_keyset;

        /** Default constructor. */
        public KeySet() {
            m_keyset = new HashMap<String, Integer>();
        }

        /**
         * Returns the current key set.
         * @return the current key set.
         */
        public Set<String> getKeySet() {

            return new HashSet<String>(m_keyset.keySet());
        }

        /**
         * Updates the set with all keys that are used in at least one language.
         * @param oldKeys keys of a locale as registered before
         * @param newKeys keys of the locale now
         */
        public void updateKeySet(Set<String> oldKeys, Set<String> newKeys) {

            // Remove keys that are not present anymore
            if (null != oldKeys) {
                Set<String> removedKeys = new HashSet<String>(oldKeys);
                if (null != newKeys) {
                    removedKeys.removeAll(newKeys);
                }
                for (String key : removedKeys) {
                    int uses = m_keyset.get(key).intValue();
                    if (uses > 1) {
                        m_keyset.put(key, Integer.valueOf(uses - 1));
                    } else {
                        m_keyset.remove(key);
                    }
                }
            }

            // Add keys that are new
            if (null != newKeys) {
                Set<String> addedKeys = new HashSet<String>(newKeys);
                if (null != oldKeys) {
                    addedKeys.removeAll(oldKeys);
                }
                for (String key : addedKeys) {
                    if (m_keyset.containsKey(key)) {
                        m_keyset.put(key, Integer.valueOf(m_keyset.get(key).intValue() + 1));
                    } else {
                        m_keyset.put(key, Integer.valueOf(1));
                    }
                }
            }

        }

    }

    /** The different ways the key set is shown. */
    enum KeySetMode {
        /** All keys used in any of the available languages. */
        ALL, /** Only keys used for the current language. */
        USED_ONLY;
    }

    /** Helper to handle the lock reports together with the files. */
    static final class LockedFile {

        /** The file that was read. */
        private CmsFile m_file;
        /** The lock action record from locking the file. */
        private CmsLockActionRecord m_lockRecord;
        /** Flag, indicating if the file was newly created. */
        private boolean m_new;

        /** Private constructor.
         * @param cms the cms user context.
         * @param resource the resource to lock and read.
         * @throws CmsException thrown if locking fails.
         */
        private LockedFile(CmsObject cms, CmsResource resource)
        throws CmsException {
            m_lockRecord = CmsLockUtil.ensureLock(cms, resource);
            m_file = cms.readFile(resource);
            m_new = false;
        }

        /**
         * Lock and read a file.
         * @param cms the cms user context.
         * @param resource the resource to lock and read.
         * @return the read file with the lock action record.
         * @throws CmsException thrown if locking fails
         */
        public static LockedFile lockResource(CmsObject cms, CmsResource resource) throws CmsException {

            return new LockedFile(cms, resource);
        }

        /** Returns the file.
         * @return the file.
         */
        public CmsFile getFile() {

            return m_file;
        }

        /** Returns the lock action record.
         * @return the lock action record.
         */
        public CmsLockActionRecord getLockActionRecord() {

            return m_lockRecord;
        }

        /**
         * Returns a flag, indicating if the file is newly created.
         * @return flag, indicating if the file is newly created.
         */
        public boolean isCreated() {

            return m_new;
        }

        /**
         * Set the flag, indicating if the file was newly created.
         * @param isNew flag, indicating if the file was newly created.
         */
        public void setCreated(boolean isNew) {

            m_new = isNew;

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

    /** The propertyIds of the table columns. */
    enum TableProperty {
        /** Table column with the message key. */
        KEY, /** Table column with the message description. */
        DESCRIPTION, /** Table column with the message's default value. */
        DEFAULT, /** Table column with the current (language specific) translation of the message. */
        TRANSLATION, /** Table column with the options (add, delete). */
        OPTIONS
    }

    /** TableFieldFactory for making only some columns editable and to support enhanced navigation. */
    @SuppressWarnings("serial")
    static class TranslateTableFieldFactory extends DefaultFieldFactory {

        /** Mapping from column -> row -> AbstractTextField. */
        private final Map<Integer, Map<Integer, AbstractTextField>> m_valueFields;
        /** The editable columns. */
        private final List<Object> m_editableColumns;
        /** Reference to the table, the factory is used for. */
        final CustomTable m_table;

        /**
         * Default constructor.
         * @param table The table, the factory is used for.
         * @param editableColumns the property names of the editable columns of the table.
         */
        public TranslateTableFieldFactory(CustomTable table, List<Object> editableColumns) {
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

            TableProperty pid = (TableProperty)propertyId;

            AbstractTextField tf;
            if (pid.equals(TableProperty.KEY)) {
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
                    tf.setInputPrompt(CmsMessageBundleEditor.m_messages.key(Messages.GUI_PLEASE_ADD_VALUE_0));
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

}
