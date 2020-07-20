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

import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.shared.categorizedselect.I_CmsCategorizedSelectData;
import org.opencms.gwt.shared.categorizedselect.I_CmsCategorizedSelectData.Option;
import org.opencms.gwt.shared.categorizedselect.I_CmsCategorizedSelectDataFactory;

import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Client-side implementation of the categorized select widget.
 *
 * <p>Provides two select fields, one for filter categories and one for the actual selection to be made.
 * No RPC is used, all data for the filter categories and options has to be supplied initially.
 */
public class CmsCategorizedSelectWidget extends Composite implements I_CmsEditWidget {

    /**
     * The Interface I_CmsWidgetUiBinder.
     */
    interface I_CmsWidgetUiBinder extends UiBinder<Widget, CmsCategorizedSelectWidget> {
        // uibinder interface
    }

    /** The config factory. */
    public static I_CmsCategorizedSelectDataFactory configFactory = GWT.create(I_CmsCategorizedSelectDataFactory.class);

    /** The Constant NO_FILTER. */
    public static final String NO_FILTER = "--";

    /** The Constant uibinder. */
    private static final I_CmsWidgetUiBinder uibinder = GWT.create(I_CmsWidgetUiBinder.class);

    /** The category select. */
    @UiField
    protected CmsSelectBox m_categorySelect;

    /** The filter label. */
    @UiField
    protected Label m_filterLabel;

    /** The main select. */
    @UiField
    protected CmsSelectBox m_mainSelect;

    /** The active. */
    private boolean m_active = true;

    /** The config. */
    private I_CmsCategorizedSelectData m_config;

    /** The container. */
    private FlowPanel m_container = new FlowPanel();

    /**
     * Instantiates a new cms categorized select widget.
     *
     * @param config the config
     */
    public CmsCategorizedSelectWidget(I_CmsCategorizedSelectData config) {

        m_config = config;
        m_container = (FlowPanel)uibinder.createAndBindUi(this);
        m_filterLabel.setText(config.getFilterLabel());
        LinkedHashMap<String, String> options = new LinkedHashMap<>();

        for (Option option : config.getOptions()) {
            options.put(option.getKey(), option.getLabel());
        }
        m_mainSelect.setItems(options);

        LinkedHashMap<String, String> categoryOptions = new LinkedHashMap<>();
        categoryOptions.put(NO_FILTER, NO_FILTER);
        for (I_CmsCategorizedSelectData.Category category : config.getCategories()) {
            categoryOptions.put(category.getKey(), category.getLabel());
        }
        m_categorySelect.setItems(categoryOptions);
        m_categorySelect.addValueChangeHandler(new ValueChangeHandler<String>() {

            @SuppressWarnings("synthetic-access")
            public void onValueChange(ValueChangeEvent<String> event) {

                updateCategory(event.getValue());
            }
        });

        initWidget(m_container);
        m_mainSelect.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                CmsCategorizedSelectWidget.this.fireChangeEvent();
            }
        });

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

        return m_mainSelect.getFormValueAsString();
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

        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        if (active == m_active) {
            return;
        }
        m_active = active;
        m_mainSelect.setEnabled(active);
        m_categorySelect.setEnabled(active);
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
     * Sets the value.
     *
     * @param value the new value
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, false);

    }

    /**
     * Sets the value.
     *
     * @param value the value
     * @param fireEvent the fire event
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setValue(java.lang.String, boolean)
     */
    public void setValue(String value, boolean fireEvent) {

        m_mainSelect.setFormValueAsString(value);
        if (fireEvent) {
            fireChangeEvent();
        }

    }

    /**
     * Update category.
     *
     * @param category the category
     */
    private void updateCategory(String category) {

        LinkedHashMap<String, String> options = new LinkedHashMap<>();
        boolean noFilter = NO_FILTER.equals(category);
        for (Option option : m_config.getOptions()) {
            List<String> categories = option.getCategories();
            if (noFilter || categories.contains(category)) {
                options.put(option.getKey(), option.getLabel());
            }
        }
        m_mainSelect.setItems(options);
        if (options.size() > 0) {
            String nextValue = options.keySet().iterator().next();
            m_mainSelect.setFormValue(nextValue, true);
            fireChangeEvent();
        }
    }
}
