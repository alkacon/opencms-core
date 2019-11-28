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

package org.opencms.ui.components.editablegroup;

import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.ErrorEvent;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.Component.Listener;
import com.vaadin.ui.Layout;
import com.vaadin.v7.ui.Label;

/**
 * Manages a group of widgets used as a multivalue input.<p>
 *
 * This class is not itself a widget, it just coordinates the other widgets actually used to display the multivalue widget group.
 */
public class CmsEditableGroup {

    /**
     * Empty handler which shows or hides an 'Add' button to add new rows, depending on whether the group is empty.
     */
    public static class AddButtonEmptyHandler implements CmsEditableGroup.I_EmptyHandler {

        /** The 'Add' button. */
        private Button m_addButton;

        /** The group. */
        private CmsEditableGroup m_group;

        /**
         * Creates a new instance.
         *
         * @param addButtonText the text for the Add button
         */
        public AddButtonEmptyHandler(String addButtonText) {

            m_addButton = new Button(addButtonText);
            m_addButton.addClickListener(evt -> {
                Component component = m_group.getNewComponentFactory().get();
                m_group.addRow(component);
            });
        }

        /**
         * @see org.opencms.ui.components.editablegroup.CmsEditableGroup.I_EmptyHandler#init(org.opencms.ui.components.editablegroup.CmsEditableGroup)
         */
        public void init(CmsEditableGroup group) {

            m_group = group;
        }

        /**
         * @see org.opencms.ui.components.editablegroup.CmsEditableGroup.I_EmptyHandler#setEmpty(boolean)
         */
        public void setEmpty(boolean empty) {

            if (empty) {
                m_group.getContainer().addComponent(m_addButton);
            } else {
                m_group.getContainer().removeComponent(m_addButton);
            }
        }

    }

    /**
     * Default implementation for row builder.
     */
    public static class DefaultRowBuilder implements CmsEditableGroup.I_RowBuilder {

        public I_CmsEditableGroupRow buildRow(CmsEditableGroup group, Component component) {

            if (component instanceof Layout.MarginHandler) {
                // Since the row is a HorizontalLayout with the edit buttons positioned next to the original
                // widget, a margin on the widget causes it to be vertically offset from the buttons too much
                Layout.MarginHandler marginHandler = (Layout.MarginHandler)component;
                marginHandler.setMargin(false);
            }
            if (component instanceof AbstractComponent) {
                component.addListener(group.getErrorListener());
            }
            I_CmsEditableGroupRow row = new CmsEditableGroupRow(group, component);
            if (group.getRowCaption() != null) {
                row.setCaption(group.getRowCaption());
            }
            return row;
        }
    }

    /**
     * Handles state changes when the group becomes empty/not empty.
     */
    public interface I_EmptyHandler {

        /**
         * Needs to be called initially with the group for which this is used.
         *
         * @param group the group
         */
        public void init(CmsEditableGroup group);

        /**
         * Called when the group changes from empty to not empty, or vice versa.
         *
         * @param empty true if the group is empty
         */
        public void setEmpty(boolean empty);
    }

    /**
     * Interface for group row components that can have errors.
     */
    public interface I_HasError {

        /**
         * Check if there is an error.
         *
         * @return true if there is an error
         */
        public boolean hasEditableGroupError();
    }

    /**
     * Builds editable group rows by wrapping other components.
     */
    public interface I_RowBuilder {

        /**
         * Builds a row for the given group by wrapping the given component.
         *
         * @param group the group
         * @param component the component
         * @return the new row
         */
        public I_CmsEditableGroupRow buildRow(CmsEditableGroup group, Component component);
    }

    /** The container in which to render the individual rows of the multivalue widget group. */
    private AbstractOrderedLayout m_container;

    /** Flag which controls whether the edit option is enabled. */
    private boolean m_editEnabled;

    private I_EmptyHandler m_emptyHandler;

    /** The error label. */
    private Label m_errorLabel = new Label();

    /** The error listener. */
    private Listener m_errorListener;

    /** The error message. */
    private String m_errorMessage;

    /**Should the add option be hidden?*/
    private boolean m_hideAdd;

    /** Factory for creating new input fields. */
    private Supplier<Component> m_newComponentFactory;

    /** The builder to use for creating new rows. */
    private I_RowBuilder m_rowBuilder = new DefaultRowBuilder();

    /** The row caption. */
    private String m_rowCaption;

    /**
     * Creates a new instance.<p>
     *
     * @param container the container in which to render the individual rows
     * @param componentFactory the factory used to create new input fields
     * @param placeholder the placeholder to display when there are no rows
     */
    public CmsEditableGroup(
        AbstractOrderedLayout container,
        Supplier<Component> componentFactory,
        I_EmptyHandler emptyHandler) {

        m_hideAdd = false;
        m_emptyHandler = emptyHandler;
        m_container = container;
        m_newComponentFactory = componentFactory;
        m_emptyHandler = emptyHandler;
        m_emptyHandler.init(this);
        m_errorListener = new Listener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void componentEvent(Event event) {

                if (event instanceof ErrorEvent) {
                    updateGroupValidation();
                }
            }
        };
        m_errorLabel.setValue(m_errorMessage);
        m_errorLabel.addStyleName("o-editablegroup-errorlabel");
        setErrorVisible(false);
    }

    /**
     * Creates a new instance.<p>
     *
     * @param container the container in which to render the individual rows
     * @param componentFactory the factory used to create new input fields
     * @param addButtonCaption the caption for the button which is used to add a new row to an empty list
     */
    public CmsEditableGroup(
        AbstractOrderedLayout container,
        Supplier<Component> componentFactory,
        String addButtonCaption) {

        this(container, componentFactory, new AddButtonEmptyHandler(addButtonCaption));
    }

    /**
     * Adds a row for the given component at the end of the group.
     *
     * @param component the component to wrap in the row to be added
     */
    public void addRow(Component component) {

        Component actualComponent = component == null ? m_newComponentFactory.get() : component;
        I_CmsEditableGroupRow row = m_rowBuilder.buildRow(this, actualComponent);
        m_container.addComponent(row);
        updatePlaceholder();
        updateButtonBars();
        updateGroupValidation();
    }

    /**
     * Adds a new row after the given one.
     *
     * @param row the row after which a new one should be added
     */
    public void addRowAfter(I_CmsEditableGroupRow row) {

        int index = m_container.getComponentIndex(row);
        if (index >= 0) {
            Component component = m_newComponentFactory.get();
            I_CmsEditableGroupRow newRow = m_rowBuilder.buildRow(this, component);
            m_container.addComponent(newRow, index + 1);
        }
        updatePlaceholder();
        updateButtonBars();
        updateGroupValidation();
    }

    /**
     * Gets the row container.
     *
     * @return the row container
     */
    public AbstractOrderedLayout getContainer() {

        return m_container;
    }

    /**
     * Gets the error listener.
     *
     * @return t
     */
    public Listener getErrorListener() {

        return m_errorListener;
    }

    /**
     * Gets the factory used for creating new components.
     *
     * @return the factory used for creating new components
     */
    public Supplier<Component> getNewComponentFactory() {

        return m_newComponentFactory;
    }

    /**
     * Returns the row caption.<p>
     *
     * @return the row caption
     */
    public String getRowCaption() {

        return m_rowCaption;
    }

    /**
     * Gets all rows.
     *
     * @return the list of all rows
     */
    public List<I_CmsEditableGroupRow> getRows() {

        List<I_CmsEditableGroupRow> result = Lists.newArrayList();
        for (Component component : m_container) {
            if (component instanceof I_CmsEditableGroupRow) {
                result.add((I_CmsEditableGroupRow)component);
            }
        }
        return result;
    }

    /**
     * Initializes the multivalue group.<p>
     */
    public void init() {

        m_container.removeAllComponents();
        m_container.addComponent(m_errorLabel);
        updatePlaceholder();
    }

    /**
     * Moves the given row down.
     *
     * @param row the row to move
     */
    public void moveDown(I_CmsEditableGroupRow row) {

        int index = m_container.getComponentIndex(row);
        if ((index >= 0) && (index < (m_container.getComponentCount() - 1))) {
            m_container.removeComponent(row);
            m_container.addComponent(row, index + 1);
        }
        updateButtonBars();
    }

    /**
     * Moves the given row up.
     *
     * @param row the row to move
     */
    public void moveUp(I_CmsEditableGroupRow row) {

        int index = m_container.getComponentIndex(row);
        if (index > 0) {
            m_container.removeComponent(row);
            m_container.addComponent(row, index - 1);
        }
        updateButtonBars();
    }

    public void onEdit(I_CmsEditableGroupRow row) {

    }

    /**
     * Removes the given row.
     *
     * @param row the row to remove
     */
    public void remove(I_CmsEditableGroupRow row) {

        int index = m_container.getComponentIndex(row);
        if (index >= 0) {
            m_container.removeComponent(row);
        }
        updatePlaceholder();
        updateButtonBars();
        updateGroupValidation();
    }

    public void removeAll() {

        m_container.removeAllComponents();
        updatePlaceholder();
        updateButtonBars();
        updateGroupValidation();

    }

    /**
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroup#setAddButtonVisible(boolean)
     */
    public void setAddButtonVisible(boolean visible) {

        m_hideAdd = !visible;

    }

    /**
     * Enables / disables edit button.
     *
     * @param enabled true if edit button should be enabled
     */
    public void setEditEnabled(boolean enabled) {

        m_editEnabled = enabled;
    }

    /**
     * Sets the error message.<p>
     *
     * @param errorMessage the error message
     */
    public void setErrorMessage(String errorMessage) {

        m_errorMessage = errorMessage;
        m_errorLabel.setValue(errorMessage != null ? errorMessage : "");
    }

    /**
     * Sets the row builder.
     *
     * @param rowBuilder the row builder
     */
    public void setRowBuilder(I_RowBuilder rowBuilder) {

        m_rowBuilder = rowBuilder;
    }

    /**
     * Sets the row caption.<p>
     *
     * @param rowCaption the row caption to set
     */
    public void setRowCaption(String rowCaption) {

        m_rowCaption = rowCaption;
    }

    /**
     * Checks if the given group component has an error.<p>
     *
     * @param component the component to check
     * @return true if the component has an error
     */
    protected boolean hasError(Component component) {

        if (component instanceof AbstractComponent) {
            if (((AbstractComponent)component).getComponentError() != null) {
                return true;
            }
        }
        if (component instanceof I_HasError) {
            if (((I_HasError)component).hasEditableGroupError()) {
                return true;
            }

        }
        return false;
    }

    /**
     * Shows or hides the error label.<p>
     *
     * @param hasError true if we have an error
     */
    private void setErrorVisible(boolean hasError) {

        m_errorLabel.setVisible(hasError && (m_errorMessage != null));
    }

    /**
     * Updates the button bars.<p>
     */
    private void updateButtonBars() {

        List<I_CmsEditableGroupRow> rows = getRows();
        int i = 0;
        for (I_CmsEditableGroupRow row : rows) {
            boolean first = i == 0;
            boolean last = i == (rows.size() - 1);
            row.getButtonBar().setFirstLast(first, last, m_hideAdd);
            row.getButtonBar().getState().setEditEnabled(m_editEnabled);
            i += 1;
        }
    }

    /**
     * Updates the visibility of the error label based on errors in the group components.<p>
     */
    private void updateGroupValidation() {

        boolean hasError = false;
        for (I_CmsEditableGroupRow row : getRows()) {
            if (hasError(row.getComponent())) {
                hasError = true;
                break;
            }
        }
        setErrorVisible(hasError);
    }

    /**
     * Updates the button visibility.<p>
     */
    private void updatePlaceholder() {

        boolean empty = getRows().size() == 0;
        m_emptyHandler.setEmpty(empty);

    }
}
