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
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.ade.contenteditor.client.CmsContentEditor;
import org.opencms.ade.contenteditor.client.I_CmsEntityChangeListener;
import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsFilterSelectBox;
import org.opencms.util.CmsPair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Select widget for display types.
 * In case the widget is configured to match display types, only formatters of a specific type may be selected at the same time.<p>
 */
public class CmsDisplayTypeSelectWidget extends Composite implements I_CmsEditWidget, I_CmsHasDisplayDirection {

    /** The no filter string. */
    private static final String NO_FILTER = "###no-filter###";

    /** The global select box. */
    protected CmsFilterSelectBox m_selectBox = new CmsFilterSelectBox();

    /** Flag indicating the widget is configured to match types. */
    boolean m_matchTypes;

    /** The available select options, un-filtered. */
    Map<String, CmsPair<String, String>> m_options;

    /** Value of the activation. */
    private boolean m_active = true;

    /** The current filter type. */
    private String m_filterType;

    /** Path components of the path used to select the option value. */
    private String[] m_valuePath;

    /** The empty option label. */
    private String m_emptyLabel;

    /**
     * Creates a new widget instance.<p>
     *
     * @param configuration the widget configuration
     */
    public CmsDisplayTypeSelectWidget(String configuration) {

        JSONObject config = (JSONObject)JSONParser.parseStrict(configuration);
        String path = ((JSONString)config.get("valuePath")).stringValue();
        m_valuePath = splitPath(path);
        m_matchTypes = ((JSONBoolean)config.get("matchTypes")).booleanValue();
        m_emptyLabel = ((JSONString)config.get("emptyLabel")).stringValue();
        JSONArray opts = (JSONArray)config.get("options");
        m_options = new LinkedHashMap<String, CmsPair<String, String>>();
        for (int i = 0; i < opts.size(); i++) {
            JSONObject opt = (JSONObject)opts.get(i);
            String value = ((JSONString)opt.get("value")).stringValue();
            String label = ((JSONString)opt.get("label")).stringValue();
            String displayType = ((JSONString)opt.get("displayType")).stringValue();
            m_options.put(value, new CmsPair<String, String>(label, displayType));
        }

        // Place the check above the box using a vertical panel.
        m_selectBox.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxPanel());
        m_selectBox.setPopupResize(false);
        // add some styles to parts of the selectbox.
        m_selectBox.getOpener().addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_selectBox.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_selectBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeEvent();

            }

        });

        update(CmsContentEditor.getEntity());
        initWidget(m_selectBox);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     * Please edit the blog entry text.
     */
    public void fireChangeEvent() {

        ValueChangeEvent.fire(this, m_selectBox.getFormValueAsString());

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsHasDisplayDirection#getDisplayingDirection()
     */
    public Direction getDisplayingDirection() {

        return m_selectBox.displayingAbove() ? Direction.above : Direction.below;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_selectBox.getFormValueAsString();
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
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    public void onLoad() {

        if (m_matchTypes) {
            update(CmsContentEditor.getEntity());

            CmsContentEditor.addEntityChangeListener(new I_CmsEntityChangeListener() {

                public void onEntityChange(CmsEntity entity) {

                    boolean attached = RootPanel.getBodyElement().isOrHasChild(getElement());
                    if (attached) {
                        update(CmsContentEditor.getEntity());
                    }
                }
            }, null);
        }
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#owns(com.google.gwt.dom.client.Element)
     */
    public boolean owns(Element element) {

        return getElement().isOrHasChild(element);

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        // check if value change. If not do nothing.
        if (m_active == active) {
            return;
        }
        // set new value.
        m_active = active;
        // set the new value to the selectbox.
        m_selectBox.setEnabled(active);
        // fire change event if necessary.
        if (active) {
            fireChangeEvent();
        }

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        // no input field so nothing to do

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, false);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        m_selectBox.setFormValueAsString(value);
        if (fireEvents) {
            fireChangeEvent();
        }

    }

    /**
     * Updates the select options from the given entity.<p>
     *
     * @param entity a top-level content entity
     */
    public void update(CmsEntity entity) {

        String filterType = NO_FILTER;
        if (m_matchTypes) {
            List<Object> values = CmsEntity.getValuesForPath(entity, m_valuePath);
            if (values.size() > 1) {
                String firstValue = (String)values.get(0);
                CmsPair<String, String> val = m_options.get(firstValue);
                if (val != null) {
                    filterType = val.getSecond();
                }
            }
        }

        if (!filterType.equals(m_filterType)) {
            boolean noFilter = NO_FILTER.equals(filterType);
            Map<String, String> items = new LinkedHashMap<String, String>();
            // add empty option
            items.put("", m_emptyLabel);
            for (Entry<String, CmsPair<String, String>> optEntry : m_options.entrySet()) {
                if (noFilter || filterType.equals(optEntry.getValue().getSecond())) {
                    items.put(optEntry.getKey(), optEntry.getValue().getFirst());
                }
            }
            replaceItems(items);
        }
        m_filterType = filterType;
    }

    /**
     * Replaces the select items with the given items.<p>
     *
     * @param items the select items
     */
    private void replaceItems(Map<String, String> items) {

        String oldValue = m_selectBox.getFormValueAsString();
        //set value and option to the combo box.
        m_selectBox.setItems(items);
        if (items.containsKey(oldValue)) {
            m_selectBox.setFormValueAsString(oldValue);
        }
    }

    /**
     * Splits a path into components.<p>
     *
     * @param path the path to split
     * @return the path components
     */
    private String[] splitPath(String path) {

        path = path.replaceAll("^/", "").replaceAll("/$", "");
        return path.split("/");
    }

}
