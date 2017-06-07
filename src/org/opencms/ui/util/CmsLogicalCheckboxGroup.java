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

package org.opencms.ui.util;

import java.util.IdentityHashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;

/**
 * Ensures that among a set of check boxes, at most one of them is checked, without imposing constraints on the location of the checkboxes
 * in the UI.
 */
public class CmsLogicalCheckboxGroup {

    /**
     * Listener interface.<p>
     */
    public static interface I_ChangeListener {

        /**
         * Gets called when the selected check box changes.<p>
         *
         * @param box the selected check box, or null if deselected
         */
        void onSelect(CheckBox box);
    }

    /** The check boxes of this group. */
    private List<CheckBox> m_checkboxes = Lists.newArrayList();

    /** The change listener. */
    private I_ChangeListener m_listener;

    /** Value change listeners for the check boxes. */
    private IdentityHashMap<Component, ValueChangeListener> m_listeners = Maps.newIdentityHashMap();

    /** True if we are currently processing an event, used to prevent infinite recursion. */
    private boolean m_runningEvent;

    /** The currently selected check box, or null if none is selected. */
    private CheckBox m_selected;

    /**
     * Adds a check box to the group.<p>
     *
     * @param checkBox the check box to add
     */
    public void add(final CheckBox checkBox) {

        checkBox.setValue(Boolean.FALSE);
        m_checkboxes.add(checkBox);
        ValueChangeListener listener = new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void valueChange(ValueChangeEvent event) {

                if (m_runningEvent) {
                    return;
                } else {
                    try {
                        m_runningEvent = true;
                        if (((Boolean)event.getProperty().getValue()).booleanValue()) {
                            if ((m_selected != null) && (m_selected != checkBox)) {
                                m_selected.setValue(Boolean.FALSE);
                            }
                            setActiveCheckBox(checkBox);
                        } else {
                            setActiveCheckBox(null);
                            m_selected = null;
                        }

                        // TODO Auto-generated method stub
                    } finally {
                        m_runningEvent = false;

                    }
                }

            }
        };
        checkBox.addValueChangeListener(listener);
        m_listeners.put(checkBox, listener);
    }

    /**
     * Gets the currently selected check box.<p>
     *
     * @return the check box
     */
    public CheckBox getSelected() {

        return m_selected;
    }

    /**
     * Removes a check box from the group.<p>
     *
     * @param checkBox the check box
     */
    public void remove(CheckBox checkBox) {

        m_checkboxes.remove(checkBox);
        if (m_selected == checkBox) {
            m_selected = null;
        }
        ValueChangeListener listener = m_listeners.get(checkBox);
        if (listener != null) {
            checkBox.removeValueChangeListener(m_listeners.get(checkBox));
        }
    }

    /**
     * Sets the change listener.<p>
     *
     * @param listener the change listener
     */
    public void setChangeListener(I_ChangeListener listener) {

        m_listener = listener;
    }

    /**
     * Sets the active check box.<p>
     *
     * @param box the active check box
     */
    protected void setActiveCheckBox(CheckBox box) {

        m_selected = box;
        if (m_listener != null) {
            m_listener.onSelect(box);
        }
    }

}
