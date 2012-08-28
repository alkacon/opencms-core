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

import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.input.CmsCategoryField;
import org.opencms.gwt.client.ui.input.category.CmsCategoryTree;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.relations.CmsCategory;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;

/**
 * Provides a standard HTML form category widget, for use on a widget dialog.<p>
 **/
public class CmsCategoryWidget extends Composite implements I_EditWidget {

    /**
     * Drag and drop event preview handler.<p>
     * 
     * To be used while dragging.<p>
     */
    protected class CloseEventPreviewHandler implements NativePreviewHandler {

        /**
         * @see com.google.gwt.user.client.Event.NativePreviewHandler#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
         */
        public void onPreviewNativeEvent(NativePreviewEvent event) {

            Event nativeEvent = Event.as(event.getNativeEvent());
            switch (DOM.eventGetType(nativeEvent)) {
                case Event.ONMOUSEMOVE:
                    break;
                case Event.ONMOUSEUP:

                    int x_coord = nativeEvent.getClientX();
                    int y_coord = (nativeEvent.getClientY() + Window.getScrollTop());

                    if (((x_coord > (m_xcoordspopup + 605)) || (x_coord < (m_xcoordspopup)))
                        || ((y_coord > ((m_ycoordspopup + 390))) || (y_coord < ((m_ycoordspopup))))) {
                        closePopup();
                    }
                    break;
                case Event.ONKEYDOWN:
                    break;
                case Event.ONMOUSEWHEEL:
                    int x_coords = nativeEvent.getClientX();
                    int y_coords = (nativeEvent.getClientY() + Window.getScrollTop());

                    if (((x_coords > (m_xcoordspopup + 605)) || (x_coords < (m_xcoordspopup)))
                        || ((y_coords > ((m_ycoordspopup + 390))) || (y_coords < ((m_ycoordspopup))))) {
                        closePopup();
                    }
                    break;
                default:
                    // do nothing
            }
        }

    }

    /** Configuration parameter to set the category to display. */
    private static final String CONFIGURATION_CATEGORY = "category";

    /** Configuration parameter to set the 'only leaf' flag parameter. */
    private static final String CONFIGURATION_ONLYLEAFS = "onlyleafs";

    /** Configuration parameter to set the 'property' parameter. */
    private static final String CONFIGURATION_PROPERTY = "property";

    /** Configuration parameter to set the 'selection type' parameter. */
    private static final String CONFIGURATION_SELECTIONTYPE = "selectiontype";

    /** Configuration parameter to set the default height. */
    private static final int DEFAULT_HEIGHT = 122;

    /** Configuration parameter to set the maximal height. */
    private static final int MAX_HEIGHT = 242;

    /** Category widget. */
    CmsCategoryField m_categoryField = new CmsCategoryField();

    /***/
    protected HandlerRegistration m_previewHandlerRegistration;

    /***/
    CmsCategoryTree m_cmsCategoryTree;

    /***/
    CmsPopup m_cmsPopup;

    /** The x-coords of the popup. */
    protected int m_xcoordspopup;

    /** The y-coords of the popup. */
    protected int m_ycoordspopup;

    /** List of all selected categories. */
    ArrayList<String> m_selected = new ArrayList<String>();

    /** Map of selected Values in relation to the select level. */
    String m_selectedValue;

    /** Value of the activation. */
    private boolean m_active = true;

    /** String of the configured category folder. */
    private String m_categoryFolder = "/sites/default/_categories/";

    /** List off all categories. */
    private List<CmsCategoryTreeEntry> m_results = new ArrayList<CmsCategoryTreeEntry>();
    /***/
    private String[] m_selectedArray;

    /** Height of the display field. */
    int m_height = DEFAULT_HEIGHT;

    /** The selection type parsed from configuration string. */
    private String m_selectiontype = "multi";

    /** Is true if only one value is set in xml. */
    private boolean m_isSingelValue;

    /**
     * Constructs an CmsComboWidget with the in XSD schema declared configuration.<p>
     * @param config The configuration string given from OpenCms XSD
     * @param isSingelValue Is true if only one value is set in xml
     */
    public CmsCategoryWidget(String config) {

        //merge configuration string
        parseConfiguration(config);

        if (m_selectiontype.equals("multi")) {
            m_isSingelValue = false;
        } else {
            m_isSingelValue = true;
        }

        m_categoryField.getScrollPanel().addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().categoryPanel());

        m_categoryField.addDomHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                if ((m_cmsPopup == null) || !m_cmsPopup.isShowing()) {
                    openPopup();
                } else {
                    closePopup();
                }

            }
        }, ClickEvent.getType());
        initWidget(m_categoryField);
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

        ValueChangeEvent.fire(this, getValue());

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        Iterator<String> i = m_selected.iterator();
        String result = "";
        int y = 0;
        if (m_isSingelValue) {
            int max_length = 0;
            while (i.hasNext()) {
                String value = i.next();
                if (value.split("/").length > max_length) {
                    max_length = value.split("/").length;
                    result = m_categoryFolder + value;
                }

            }

        } else {
            while (i.hasNext()) {
                if (y != 0) {
                    result += ",";

                }
                result += m_categoryFolder + i.next();
                y++;
            }
        }
        return result;
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

        if (m_active == active) {
            return;
        }

        m_active = active;
        if (m_active) {
            setValue(m_selectedValue);
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

        String singelValue = "";
        m_selected.clear();
        if (m_isSingelValue && !value.isEmpty() && !value.replace(m_categoryFolder, "").isEmpty()) {
            value = value.replace(",", "");
            String childValue = value.replace(m_categoryFolder, "");
            singelValue = childValue;
            String helpValue = childValue.substring(0, childValue.lastIndexOf("/"));
            while (helpValue.indexOf("/") != -1) {
                helpValue = helpValue.substring(0, helpValue.lastIndexOf("/") + 1);
                value += "," + m_categoryFolder + helpValue;
                helpValue = helpValue.substring(0, helpValue.lastIndexOf("/"));
            }
        }

        String shortvalue = value.replaceAll(m_categoryFolder, "");
        m_selectedValue = shortvalue;
        String[] selected = shortvalue.split(",");
        m_selectedArray = selected;
        ArrayList<String> test = new ArrayList<String>();
        for (int i = 0; i < selected.length; i++) {
            m_selected.add(selected[i].replace(m_categoryFolder, ""));
            test.add(selected[i].replace(m_categoryFolder, ""));
        }

        if (!selected[0].isEmpty()) {
            m_results = new ArrayList<CmsCategoryTreeEntry>();
            parseValues(test.toArray(new String[0]), null, 1);
            m_categoryField.buildCategoryTree(m_results, m_selected);
        } else {
            m_results = new ArrayList<CmsCategoryTreeEntry>();
            m_categoryField.buildCategoryTree(m_results, m_selected);
        }

        if (fireEvents) {
            fireChangeEvent();
        }
        if (m_isSingelValue) {
            m_selected.clear();
            m_selected.add(singelValue);
        }
        if (!selected[0].isEmpty()) {
            int elementheight = (selected.length * 24) + 2;
            if (elementheight < DEFAULT_HEIGHT) {
                m_height = elementheight;
            } else {
                m_height = DEFAULT_HEIGHT;
            }

            if (m_height > MAX_HEIGHT) {
                m_height = MAX_HEIGHT;
            }
        } else {
            m_height = 26;
        }
        m_categoryField.setHeight(m_height);
    }

    /**
     * 
     */
    protected void closePopup() {

        m_previewHandlerRegistration.removeHandler();
        m_previewHandlerRegistration = null;
        String selected = "";
        if (m_isSingelValue) {
            selected = m_cmsCategoryTree.getSelected();
        } else {
            for (String s : m_cmsCategoryTree.getAllSelected()) {
                if (!s.isEmpty()) {
                    selected += m_categoryFolder + s + ",";
                }
            }
        }
        setValue(selected);
        m_cmsPopup.hide();

    }

    /**
     * 
     */
    protected void openPopup() {

        if (m_cmsPopup == null) {

            m_cmsPopup = new CmsPopup("Category");
            m_cmsPopup.setWidth(600);
            m_cmsPopup.setHeight(386);
            m_cmsCategoryTree = new CmsCategoryTree(m_selected, 300, m_isSingelValue, m_categoryFolder);
            m_cmsPopup.add(m_cmsCategoryTree);
            m_cmsPopup.setModal(false);
            m_cmsPopup.addDialogClose(new Command() {

                public void execute() {

                    closePopup();
                }
            });
        }
        if (m_previewHandlerRegistration != null) {
            m_previewHandlerRegistration.removeHandler();
        }
        m_previewHandlerRegistration = Event.addNativePreviewHandler(new CloseEventPreviewHandler());
        List<String> selected = new ArrayList<String>();
        for (int i = 0; i < m_selectedArray.length; i++) {
            selected.add(m_selectedArray[i].toLowerCase());
        }
        m_cmsPopup.showRelativeTo(m_categoryField);
        m_xcoordspopup = m_cmsPopup.getPopupLeft();
        m_ycoordspopup = m_cmsPopup.getPopupTop();
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
                m_categoryFolder = category;
            }
            int selectiontypeIndex = configuration.indexOf(CONFIGURATION_SELECTIONTYPE);
            if (selectiontypeIndex != -1) {
                // selection type is given
                String selectiontype = configuration.substring(selectiontypeIndex
                    + CONFIGURATION_SELECTIONTYPE.length()
                    + 1);
                if (selectiontype.indexOf("|") != -1) {
                    // cut eventual following configuration values
                    selectiontype = selectiontype.substring(0, selectiontype.indexOf("|"));
                }
                m_selectiontype = selectiontype;
            }

        }
    }

    /**
     * @param values 
     * @param parent 
     * @param level 
     */
    private void parseValues(String[] values, CmsCategoryTreeEntry parent, int level) {

        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if ((value.indexOf("/") == (value.length() - 1)) && (parent == null)) {
                try {
                    String name = value.replace("/", "");

                    CmsCategory category = new CmsCategory(null, m_categoryFolder + value, name, null, m_categoryFolder);
                    CmsCategoryTreeEntry categoryEntry = new CmsCategoryTreeEntry(category);
                    m_results.add(categoryEntry);
                    List<String> test = new ArrayList<String>();
                    for (int y = 0; y < values.length; y++) {
                        if (!values[i].equals(values[y])) {
                            test.add(values[y]);
                        }
                    }
                    parseValues(test.toArray(new String[0]), categoryEntry, level + 1);
                } catch (Exception e) {
                    //TODO nothing
                }
            } else if (parent != null) {

                if (value.contains(parent.getPath()) && (value.split("/").length == level)) {
                    CmsCategory category;
                    try {
                        String name = value.replace(parent.getPath(), "").replace("/", "");
                        category = new CmsCategory(null, m_categoryFolder + value, name, null, m_categoryFolder);
                        CmsCategoryTreeEntry categoryEntry = new CmsCategoryTreeEntry(category);
                        parent.addChild(categoryEntry);
                        List<String> test = new ArrayList<String>();
                        for (int y = 0; y < values.length; y++) {
                            if (!values[i].equals(values[y])) {
                                test.add(values[y]);
                            }
                        }
                        parseValues(test.toArray(new String[0]), categoryEntry, level + 1);
                    } catch (Exception e) {
                        //TODO nothing
                    }

                }
            }
        }
    }
}