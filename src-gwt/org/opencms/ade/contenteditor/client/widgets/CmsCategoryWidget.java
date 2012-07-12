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

package org.opencms.ade.contenteditor.client.widgets;

import com.alkacon.acacia.client.widgets.I_EditWidget;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a standard HTML form category widget, for use on a widget dialog.<p>
 **/
public class CmsCategoryWidget extends Composite implements I_EditWidget {

    /** Configuration parameter to set the category to display. */
    public static final String CONFIGURATION_CATEGORY = "category";

    /** Configuration parameter to set the 'only leaf' flag parameter. */
    public static final String CONFIGURATION_ONLYLEAFS = "onlyleafs";

    /** Configuration parameter to set the 'property' parameter. */
    public static final String CONFIGURATION_PROPERTY = "property";

    /** Map of selected Values in relation to the select level. */
    Map<Integer, String> m_selectedValue = new HashMap<Integer, String>();

    /** List off all categories. */
    List<CmsCategoryTreeEntry> m_results = new ArrayList<CmsCategoryTreeEntry>();

    /** Value of the activation. */
    private boolean m_active = true;

    /** String of the configured category folder. */
    private String m_categoryFolder = "/sites/default/_categories/";

    /** Horizontal panel. It holds all the select boxes. */
    private HorizontalPanel m_panel = new HorizontalPanel();

    /**
     * Constructs an CmsComboWidget with the in XSD schema declared configuration.<p>
     * @param config The configuration string given from OpenCms XSD.
     */
    public CmsCategoryWidget(String config) {

        //merge configuration string
        parseConfiguration(config);

        //generate the select boxes
        getCategories();
        m_panel.getElement().getStyle().setProperty("minHeight", "25px");
        // All composites must call initWidget() in their constructors.
        initWidget(m_panel);

    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return null;
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     */
    public void fireChangeEvent() {

        //ValueChangeEvent.fire(this, value);
        getSelections();

    }

    /**
     * Represents a value change event.<p>
     * @param value The selectbox how has fired the event.
     */
    public void fireChangeEvent(CmsSelectBox value) {

        Iterator<Widget> it = m_panel.iterator();
        int k = 0;
        while (it.hasNext()) {
            CmsSelectBox selectBox = (CmsSelectBox)it.next();
            if (selectBox.equals(value)) {
                m_selectedValue.put(Integer.valueOf(k), value.getFormValueAsString());
                break;
            }
            k++;
        }
        removeTillSelectBox(value);
        if (!value.getFormValueAsString().equals("Select value") && hasNext(value)) {
            m_panel.add(getNextSelectbox(value, k));
        }
        it = m_panel.iterator();
        String valuePath = m_categoryFolder + value.getFormValueAsString();
        ValueChangeEvent.fire(this, valuePath);

    }

    /**
     * Creates all the selectboxes and add them to the panel.<p>
     * */
    public void getSelections() {

        // remove all widgets from panel.
        m_panel.clear();
        // add the root selectbox to the panel.
        m_panel.add(getRootSelectBox());
        // check if there are more selected sub categories.  
        for (int i = 1; i < m_selectedValue.size(); i++) {
            // create and add the sub category to the panel.
            m_panel.add(getNextSelectbox((CmsSelectBox)m_panel.getWidget(i - 1), i));
        }

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        Iterator<Widget> it = m_panel.iterator();
        String selectedCategories = "";
        while (it.hasNext()) {
            CmsSelectBox selectBox = (CmsSelectBox)it.next();
            selectedCategories = selectBox.getFormValueAsString();
        }
        return selectedCategories;
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        super.onAttach();
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        m_active = active;
        Iterator<Widget> it = m_panel.iterator();
        while (it.hasNext()) {
            CmsSelectBox selectbox = (CmsSelectBox)it.next();
            selectbox.setEnabled(active);
        }
        if (active) {
            fireChangeEvent();
        }

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, true);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        value = value.replace(m_categoryFolder, "");
        String[] selectedValues = value.split("/");
        String prefix = "";
        for (int i = 0; i < selectedValues.length; i++) {
            prefix += selectedValues[i] + "/";
            m_selectedValue.put(Integer.valueOf(i), prefix);
        }
        if (fireEvents) {
            fireChangeEvent();
        }

    }

    /**
     * Help function to get all the categories from the vfs.<p>
     */
    private void getCategories() {

        // generate a list of all configured categories.
        final List<String> categories = new ArrayList<String>();
        categories.add(m_categoryFolder + "/");

        // start request 
        CmsRpcAction<List<CmsCategoryTreeEntry>> action = new CmsRpcAction<List<CmsCategoryTreeEntry>>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            public void execute() {

                CmsCoreProvider.getService().getCategories("/", true, categories, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(List<CmsCategoryTreeEntry> result) {

                // copy the result to the global variable. 
                m_results = result;
                // start to build the widget.
                getSelections();
            }

        };
        action.execute();
    }

    /**
     * Help function to get a List of all sub categories for given path.<p>
     * 
     * @param path Path of the category.
     * @return List of CategoryTreeEntries.
     * */
    private List<CmsCategoryTreeEntry> getCategoryTree(String path) {

        // split the path into the all parts. Eg.: color/red/ => color;red;
        String[] levels = path.split("/");
        // copy the complete category tree. 
        List<CmsCategoryTreeEntry> result = m_results;
        // do for all the parts of the path.
        for (int i = 0; i < levels.length; i++) {
            // create iterator of the first level of category tree.
            Iterator<CmsCategoryTreeEntry> it = result.iterator();
            while (it.hasNext()) {
                CmsCategoryTreeEntry next = it.next();
                // split the path into the all parts. Eg.: color/red/ => color;red;
                String[] nextPath = next.getPath().split("/");
                // check if both pathes are equal.
                if (nextPath[i].equalsIgnoreCase(levels[i].replace(" ", ""))) {
                    // if they are, set the children of this tree to the results. 
                    result = next.getChildren();
                }
            }
        }
        return result;
    }

    /**
     * Help function to get the sub select box.<p>
     * 
     * @param parent the parent select box.
     * @param level the level this select box stands.
     * @return a CmsSelectBox
     * */
    private CmsSelectBox getNextSelectbox(CmsSelectBox parent, int level) {

        // create new select box.
        CmsSelectBox child = new CmsSelectBox();
        child.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeEvent((CmsSelectBox)event.getSource());
            }

        });
        // get list of the children  from parent selctbox.
        List<CmsCategoryTreeEntry> childs = getCategoryTree(parent.getFormValueAsString());
        Map<String, String> items = new LinkedHashMap<String, String>();
        // add default value to the results.
        items.put("Select value", "Select value");
        // copy the list to the results.
        for (int y = 0; y < childs.size(); y++) {
            items.put(childs.get(y).getPath(), childs.get(y).getTitle());
        }
        // add the results to the select box.
        child.setItems(items);
        // check if the result has the selected value
        if (items.containsKey(m_selectedValue.get(Integer.valueOf(level)))) {
            // select the selected value.
            child.selectValue(m_selectedValue.get(Integer.valueOf(level)));
        } else {
            // select the default value.
            child.selectValue("Select value");
        }
        // return the generated select box.
        return child;
    }

    /**
     * Helper function to get the root select box.<p>
     * @return CmsSelectBox
     * */
    private CmsSelectBox getRootSelectBox() {

        // create new select box.
        CmsSelectBox child = new CmsSelectBox();
        child.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeEvent((CmsSelectBox)event.getSource());
            }

        });
        List<CmsCategoryTreeEntry> childs = m_results;
        Map<String, String> items = new LinkedHashMap<String, String>();
        // add default value to the results.
        items.put("Select value", "Select value");
        // copy the list to the results.
        for (int y = 0; y < childs.size(); y++) {
            items.put(childs.get(y).getPath(), childs.get(y).getTitle());
        }
        // add the results to the select box.
        child.setItems(items);
        // check if the result has the selected value
        if (items.containsKey(m_selectedValue.get(Integer.valueOf(0)))) {
            // select the selected value.
            child.selectValue(m_selectedValue.get(Integer.valueOf(0)));
        } else {
            // select the default value.
            child.selectValue("Select value");
        }
        // return the generated select box.
        return child;
    }

    /**
     * Helper function to check if the select box has children.<p>
     * 
     * @param parent the select box to be checked.
     * @return true if has next.
     * */
    private boolean hasNext(CmsSelectBox parent) {

        // try to get the children of the given parent.
        List<CmsCategoryTreeEntry> childs = getCategoryTree(parent.getFormValueAsString());
        // if there are no children return false.
        if (childs == null) {
            return false;
        }
        // if there are one or more children return true.      
        return !childs.isEmpty();
    }

    /**
     * Help function to parse the configuration.<p>
     * @param configuration the value to be parsed.
     *  
     * */
    private void parseConfiguration(String configuration) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            int categoryIndex = configuration.indexOf(CONFIGURATION_CATEGORY);
            if (categoryIndex != -1) {
                // category is given
                String category = configuration.substring(CONFIGURATION_CATEGORY.length() + 1);
                if (category.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    category = category.substring(0, category.indexOf('|'));
                }
                //m_categoryFolder = category;
            }
            // TODO: check if the following configuration is necessary. 

            /*int onlyLeafsIndex = configuration.indexOf(CONFIGURATION_ONLYLEAFS);
            if (onlyLeafsIndex != -1) {
                // only leafs is given
                String onlyLeafs = configuration.substring(onlyLeafsIndex + CONFIGURATION_ONLYLEAFS.length() + 1);
                if (onlyLeafs.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    onlyLeafs = onlyLeafs.substring(0, onlyLeafs.indexOf('|'));
                }
                m_onlyLeafs = onlyLeafs;
            }
            int propertyIndex = configuration.indexOf(CONFIGURATION_PROPERTY);
            if (propertyIndex != -1) {
                // property is given
                String property = configuration.substring(propertyIndex + CONFIGURATION_PROPERTY.length() + 1);
                if (property.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    property = property.substring(0, property.indexOf('|'));
                }
                m_property = property;
            }*/
        }
    }

    /**
     * Helper function to remove all select boxes beginning after the given select box.<p>
     * 
     * @param value the starting select box.
     * */
    private void removeTillSelectBox(CmsSelectBox value) {

        // iterate about all widgets of the panel.
        Iterator<Widget> it = m_panel.iterator();
        // marker if the widgets should be removed. 
        boolean remove = false;
        // counter to get the point the removing starts.
        int k = 0;
        int i = 0;
        // start to iterate 
        while (it.hasNext()) {
            CmsSelectBox selectbox = (CmsSelectBox)it.next();
            // check if this one is to remove.
            if (remove) {
                it.remove();
            }
            // check if this select box is like the given one.
            if (selectbox.equals(value) && !remove) {
                // set the marker to true.
                remove = true;
                // stop counting the widgets.
                k = i;
            }
            i++;
        }
        // remove all values from the map of selected values.
        for (i = 0; i < m_selectedValue.size(); i++) {
            // check if the counter has reached the widgetcounter.
            if (i > k) {
                m_selectedValue.remove(m_selectedValue.get(Integer.valueOf(i)));
            }
        }
    }
}