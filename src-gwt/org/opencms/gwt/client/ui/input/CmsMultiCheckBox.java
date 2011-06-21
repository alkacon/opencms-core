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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * A form widget consisting of a group of checkboxes.<p>
 * 
 * @since 8.0.0
 *  
 */
public class CmsMultiCheckBox extends Composite implements I_CmsFormWidget, I_CmsHasInit {

    /** The type string for this widget. */
    public static final String WIDGET_TYPE = "multiselect";

    /** The list of checkboxes. */
    private List<CmsCheckBox> m_checkboxes = new ArrayList<CmsCheckBox>();

    /** Error display for this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The select options of the multi check box. */
    private Map<String, String> m_items = new LinkedHashMap<String, String>();

    /** Panel which contains all the components of the widget. */
    private Panel m_panel = new FlowPanel();

    /**
     * Constructs a new checkbox group from a list of string pairs.<p>
     * 
     * The first string of every pair is the value of the checkbox, the second string is the label.
     * 
     * @param items a list of pairs of strings. 
     */
    public CmsMultiCheckBox(List<CmsPair<String, String>> items) {

        super();
        init(CmsPair.pairsToMap(items));
    }

    /**
     * Constructs a new checkbox group from a map from strings to strings.<p>
     * 
     * The keys of the map are used as the selection values of the checkboxes, while the value
     * for a given key in the map is used as the label for the checkbox which is displayed to the user. 
     * 
     * @param items the map of checkbox options 
     */
    public CmsMultiCheckBox(Map<String, String> items) {

        super();
        init(items);
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // registers a factory for creating new instances of this widget
        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams) {

                return new CmsMultiCheckBox(widgetParams);
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return I_CmsFormWidget.FieldType.STRING_LIST;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        return new ArrayList<String>(getSelected());
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        List<String> selected = new ArrayList<String>(getSelected());
        return CmsStringUtil.listAsString(selected, "|");

    }

    /**
     * Returns the set of values of the selected checkboxes.<p>
     * 
     * @return a set of strings
     */
    public Set<String> getSelected() {

        Set<String> result = new HashSet<String>();
        int i = 0;
        for (Map.Entry<String, String> entry : m_items.entrySet()) {
            String key = entry.getKey();
            CmsCheckBox checkBox = m_checkboxes.get(i);
            if (checkBox.isChecked()) {
                result.add(key);
            }
            i += 1;
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        boolean result = true;
        for (CmsCheckBox checkbox : m_checkboxes) {
            if (!checkbox.isEnabled()) {
                result = false;
            }
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        for (CmsCheckBox checkbox : m_checkboxes) {
            checkbox.setChecked(false);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        for (CmsCheckBox checkbox : m_checkboxes) {
            checkbox.setEnabled(enabled);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_error.setText(errorMessage);
    }

    /**
     * Sets the value of the widget.<p>
     * 
     * @param value the new value 
     */
    @SuppressWarnings("unchecked")
    public void setFormValue(Object value) {

        if (value instanceof List<?>) {
            List<String> keys = (List<String>)value;
            Set<String> keySet = new HashSet<String>(keys);
            int i = 0;
            for (Map.Entry<String, String> entry : m_items.entrySet()) {
                String key = entry.getKey();
                CmsCheckBox checkbox = m_checkboxes.get(i);
                checkbox.setChecked(keySet.contains(key));
                i += 1;
            }
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String formValue) {

        List<String> values = CmsStringUtil.splitAsList(formValue, "|");
        setFormValue(values);

    }

    /**
     * Initializes the widget given a map of select options.<p>
     * 
     * The keys of the map are the values of the select options, while the values of the map 
     * are the labels which should be used for the checkboxes.
     * 
     * @param items the map of select options
     */
    protected void init(Map<String, String> items) {

        initWidget(m_panel);
        m_items = new LinkedHashMap<String, String>(items);
        m_panel.setStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().multiCheckBox());
        m_panel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().textMedium());
        for (Map.Entry<String, String> entry : items.entrySet()) {
            String value = entry.getValue();
            CmsCheckBox checkbox = new CmsCheckBox(value);
            // wrap the check boxes in FlowPanels to arrange them vertically 
            FlowPanel checkboxWrapper = new FlowPanel();
            checkboxWrapper.add(checkbox);
            m_panel.add(checkboxWrapper);
        }
        m_panel.add(m_error);
    }

}
