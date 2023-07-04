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

package org.opencms.acacia.client.widgets;

import org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.shared.attributeselect.I_CmsAttributeSelectData;
import org.opencms.gwt.shared.attributeselect.I_CmsAttributeSelectData.AttributeDefinition;
import org.opencms.gwt.shared.attributeselect.I_CmsAttributeSelectData.Option;
import org.opencms.gwt.shared.attributeselect.I_CmsAttributeSelectData.OptionWithAttributes;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.HashMultimap;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * An attribute select widget acts as a select widget and consists of several attribute filter select boxes and one main select box, such
 * that choosing values from the attribute filters restricts the available options in the main select box to those which
 * have a matching value for every filter attribute.
 *
 * <p>All data related to the options and filter attributes must be passed into the constructor, this widget does not use any RPC calls.
 */
public class CmsAttributeSelectWidget extends Composite implements I_CmsEditWidget {

    /**
     * Class representing a pair of an attribute name and value, for use as a key in the option index.
     */
    class IndexKey {

        /** The attribute name. */
        private String m_name;

        /** The attribute value. */
        private String m_value;

        /**
         * Creates a new instance.
         *
         * @param name the attribute name
         * @param value  the attribute value
         */
        public IndexKey(String name, String value) {

            m_name = name;
            m_value = value;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {

            if (!(o instanceof IndexKey)) {
                return false;
            }
            IndexKey other = (IndexKey)o;
            return other.m_name.equals(m_name) && other.m_value.equals(m_value);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return (31 * m_name.hashCode()) + m_value.hashCode();
        }
    }

    /** The panel containing everything else. */
    protected FlowPanel m_root = new FlowPanel();

    /** Tracks if the widget is active. */
    private boolean m_active = true;

    /** Map of attribute definitions by name. */
    private Map<String, AttributeDefinition> m_attributeDefinitions = new HashMap<>();

    /** Map of attribute select boxes by attribute name. */
    private Map<String, CmsSelectBox> m_attributeSelects = new HashMap<>();

    /** Value set from the outside. */
    private String m_externalValue;

    /** The main select box for actually choosing the widget value. */
    private CmsSelectBox m_mainSelect;

    /** An index for quickly locating all options with a given attribute value. */
    private HashMultimap<IndexKey, String> m_optionIndex = HashMultimap.create();

    /** Map of all options. */
    private LinkedHashMap<String, OptionWithAttributes> m_options = new LinkedHashMap<>();

    /**
     * Creates a new instance.
     *
     * @param data the widget data
     */
    public CmsAttributeSelectWidget(I_CmsAttributeSelectData data) {

        initWidget(m_root);
        for (AttributeDefinition attrDef : data.getAttributeDefinitions()) {
            m_attributeDefinitions.put(attrDef.getName(), attrDef);
            CmsSelectBox selectBox = new CmsSelectBox();
            LinkedHashMap<String, String> selectBoxOptions = new LinkedHashMap<>();
            for (Option option : attrDef.getOptions()) {
                selectBoxOptions.put(option.getValue(), option.getLabel());
                if (option.getHelpText() != null) {
                    selectBox.setTitle(option.getValue(), option.getHelpText());
                }
            }
            selectBox.setItems(selectBoxOptions);
            // set the value before adding the change handler, since we already call handleFilterChange() below to initialize the main select box
            selectBox.setFormValueAsString(getDefaultOption(attrDef));
            addFilterLine(attrDef.getLabel(), selectBox);
            m_attributeSelects.put(attrDef.getName(), selectBox);
            selectBox.addValueChangeHandler(event -> handleFilterChange());
        }

        m_mainSelect = new CmsSelectBox();
        m_root.add(m_mainSelect);
        LinkedHashMap<String, String> mainOptions = new LinkedHashMap<>();
        for (OptionWithAttributes option : data.getOptions()) {
            if (option.getHelpText() != null) {
                // we only need to set all the help texts once, they will be preserved during option changes
                m_mainSelect.setTitle(option.getValue(), option.getHelpText());
            }
            m_options.put(option.getValue(), option);
            mainOptions.put(option.getValue(), option.getLabel());
            for (String attribute : option.getAttributes().keySet()) {
                for (String attrValue : option.getAttributes().get(attribute)) {
                    m_optionIndex.put(new IndexKey(attribute, attrValue), option.getValue());
                }
            }
        }
        m_mainSelect.addValueChangeHandler(event -> fireChangeEvent());
        handleFilterChange();
    }

    /**
     * Adds the focus handler.
     *
     * @param handler the handler
     * @return the handler registration
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
    }

    /**
     * Adds the value change handler.
     *
     * @param handler the handler
     * @return the handler registration
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     *
     */
    public void fireChangeEvent() {

        String val = m_mainSelect.getFormValueAsString();
        if (val != null) {
            ValueChangeEvent.fire(this, val);
        }

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        String value = m_mainSelect.getFormValueAsString();
        return value;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        super.onAttach();

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#owns(com.google.gwt.dom.client.Element)
     */
    public boolean owns(Element element) {

        return false;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        if (active == m_active) {
            // Trying to set one value while initializing the widget can result in a different value being set.
            // But at that time the event handler for the widget may not yet be set up correctly, so fireChangeEvent
            // does nothing. So we have to fire the event here in setActive (which is called during widget
            // initialization, after the change handler is set up).
            if (active && !Objects.equals(getValue(), m_externalValue)) {
                fireChangeEvent();
            }
            return;
        }
        m_active = active;
        m_mainSelect.setEnabled(active);
        for (CmsSelectBox attrSelect : m_attributeSelects.values()) {
            attrSelect.setEnabled(active);
        }
        if (active) {
            fireChangeEvent();
        }
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        // do nothing

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, false);

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setValue(java.lang.String, boolean)
     */
    public void setValue(String value, boolean fireEvent) {

        m_externalValue = value;
        OptionWithAttributes option = m_options.get(value);
        if (option != null) {
            for (String optionAttribute : option.getAttributes().keySet()) {
                m_attributeSelects.get(optionAttribute).setFormValue(
                    option.getAttributes().get(optionAttribute).get(0),
                    false);
            }
            handleFilterChange();
            m_mainSelect.setFormValue(value, false);
        } else {
            for (AttributeDefinition attrDef : m_attributeDefinitions.values()) {
                CmsSelectBox select = m_attributeSelects.get(attrDef.getName());
                // the editor initializes new widgets with the empty value, so we want to use the default option instead
                // the neutral option for the attribute in that case.
                String attrSelectValue = CmsStringUtil.isEmpty(value)
                ? getDefaultOption(attrDef)
                : getNeutralOption(attrDef);
                select.setFormValue(attrSelectValue, false);
            }
            handleFilterChange();
            m_mainSelect.setFormValue(value, false);
        }
        if (fireEvent) {
            fireChangeEvent();
        }
    }

    /**
     * Gets the currently selected attribute filters.
     *
     * @return a map with attribute names as its keys and attribute values as its values
     */
    protected Map<String, String> getFilterAttributes() {

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, CmsSelectBox> entry : m_attributeSelects.entrySet()) {
            String filterValue = entry.getValue().getFormValueAsString();
            result.put(entry.getKey(), filterValue);
        }
        return result;
    }

    /**
     * Changes the set of available options in the main select box to those which match the currently
     * selected attribute filters.
     */
    protected void handleFilterChange() {

        Map<String, String> attributes = getFilterAttributes();
        Map<String, String> newMainOptions = new LinkedHashMap<>();

        // first generate a map of all options, then reduce the keys for each chosen filter using the option index

        for (Map.Entry<String, OptionWithAttributes> option : m_options.entrySet()) {
            newMainOptions.put(option.getKey(), option.getValue().getLabel());
        }
        for (Map.Entry<String, String> filterEntry : attributes.entrySet()) {
            newMainOptions.keySet().retainAll(
                m_optionIndex.get(new IndexKey(filterEntry.getKey(), filterEntry.getValue())));
        }
        m_mainSelect.setItems(newMainOptions);
        fireChangeEvent();
    }

    /**
     * Adds a new line with an attribute filter select box and a label.
     *
     * @param label the label
     * @param selectBox the select box
     */
    private void addFilterLine(String label, CmsSelectBox selectBox) {

        FlowPanel filterLine = new FlowPanel();
        filterLine.add(new Label(label));
        filterLine.add(selectBox);
        filterLine.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().attributeFilterLine());
        m_root.add(filterLine);

    }

    /**
     * Gets the default option for an attribute.
     * @param attrDef the attribute
     * @return the default option
     */
    private String getDefaultOption(AttributeDefinition attrDef) {

        if (attrDef.getDefaultOption() != null) {
            return attrDef.getDefaultOption();
        }

        return attrDef.getOptions().get(0).getValue();

    }

    /**
     * Gets the filter attribute value to use if no other filter attribute value can be used.
     *
     * @param attrDef the attribute definition
     *
     * @return the neutral option
     */
    private String getNeutralOption(AttributeDefinition attrDef) {

        if (attrDef.getNeutralOption() != null) {
            return attrDef.getNeutralOption();
        }
        return attrDef.getOptions().get(0).getValue();
    }

}
