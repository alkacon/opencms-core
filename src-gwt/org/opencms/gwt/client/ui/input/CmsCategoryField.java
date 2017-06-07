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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.category.CmsDataValue;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * Basic category widget for forms.<p>
 *
 * @since 8.0.0
 *
 */
public class CmsCategoryField extends Composite implements I_CmsFormWidget, I_CmsHasInit {

    /** Selection handler to handle check box click events and double clicks on the list items. */
    protected abstract class A_SelectionHandler implements ClickHandler, DoubleClickHandler {

        /** The reference to the checkbox. */
        private CmsCheckBox m_checkBox;

        /** The the select button, can be used instead of a double click to select and search. */
        private CmsPushButton m_selectButton;

        /**
         * Constructor.<p>
         *
         * @param checkBox the item check box
         */
        protected A_SelectionHandler(CmsCheckBox checkBox) {

            m_checkBox = checkBox;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            if (event.getSource().equals(m_selectButton)) {
                m_checkBox.setChecked(true);
                onSelectionChange();
            } else if (event.getSource().equals(m_checkBox)) {
                onSelectionChange();
            }
        }

        /**
         * @see com.google.gwt.event.dom.client.DoubleClickHandler#onDoubleClick(com.google.gwt.event.dom.client.DoubleClickEvent)
         */
        public void onDoubleClick(DoubleClickEvent event) {

            m_checkBox.setChecked(true);
            onSelectionChange();
            event.stopPropagation();
            event.preventDefault();
        }

        /**
         * Sets the select button, can be used instead of a double click to select and search.<p>
         *
         * @param button the select button
         */
        public void setSelectButton(CmsPushButton button) {

            m_selectButton = button;
        }

        /**
         * Returns the check box.<p>
         *
         * @return the check box
         */
        protected CmsCheckBox getCheckBox() {

            return m_checkBox;
        }

        /**
         * Executed on selection change. Either when the check box was clicked or on double click on a list item.<p>
         */
        protected abstract void onSelectionChange();
    }

    /** The widget type identifier for this widget. */
    private static final String WIDGET_TYPE = "categoryField";

    /** The panel contains all the categories. */
    FlowPanel m_categories = new FlowPanel();

    /** The default rows set. */
    int m_defaultHeight;

    /** The root panel containing the other components of this widget. */
    Panel m_panel = new FlowPanel();

    /** The container for the text area. */
    CmsScrollPanel m_scrollPanel = GWT.create(CmsScrollPanel.class);

    /** The sife pathes of all added categories. */
    private List<String> m_allSidePath = new ArrayList<String>();

    /** The error display for this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The value if the parent should be selected with the children. */
    private boolean m_selectParent;

    /** The side path of the last added category. */
    private String m_singelSidePath = "";

    /** Count the numbers of values shown. */
    private int m_valuesSet;

    /**
     * Category field widgets for ADE forms.<p>
     */
    public CmsCategoryField() {

        super();
        initWidget(m_panel);
        m_panel.add(m_scrollPanel);
        m_scrollPanel.getElement().getStyle().setHeight(50, Unit.PX);
        m_scrollPanel.add(m_categories);

        m_panel.add(m_error);
        m_scrollPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
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

                return new CmsCategoryField();
            }
        });
    }

    /**
     * Builds and shows the category tree.<p>
     *
     * @param treeEntries List of category entries
     * @param selectedCategories a list of all selected categories
     */
    public void buildCategoryTree(List<CmsCategoryTreeEntry> treeEntries, Collection<String> selectedCategories) {

        m_valuesSet = 0;
        m_allSidePath.clear();
        m_categories.removeFromParent();
        m_categories.clear();

        if ((treeEntries != null) && !treeEntries.isEmpty()) {
            // add the first level and children
            for (CmsCategoryTreeEntry category : treeEntries) {
                // set the category tree item and add to list
                CmsTreeItem treeItem;
                if (m_selectParent || !hasSelectedChildren(category.getChildren(), selectedCategories)) {
                    treeItem = buildTreeItem(category, selectedCategories, false);
                    if (treeItem.isOpen()) {
                        m_allSidePath.add(category.getSitePath());
                    }
                } else {
                    treeItem = buildTreeItem(category, selectedCategories, true);
                }
                if (treeItem.isOpen()) {
                    m_singelSidePath = category.getSitePath();

                    m_valuesSet++;
                    addChildren(treeItem, category.getChildren(), selectedCategories);
                }

            }
        }
        m_scrollPanel.add(m_categories);
        m_scrollPanel.onResizeDescendant();

    }

    /**
     * Returns the site path of all shown categories.<p>
     *
     * @return the site path of all shown categories
     */
    public List<String> getAllSitePath() {

        return m_allSidePath;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return I_CmsFormWidget.FieldType.STRING;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * Returns the scroll panel of this widget.<p>
     *
     * @return the scroll panel
     */
    public CmsScrollPanel getScrollPanel() {

        return m_scrollPanel;
    }

    /**
     * Returns the site path of the last category.<p>
     *
     * @return the site path of the last category
     */
    public String getSingelSitePath() {

        return m_singelSidePath;
    }

    /**
     * Returns the count of values set to show.<p>
     * @return the count of values set to show
     */
    public int getValuesSet() {

        return m_valuesSet;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return false;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        //TODO implement reset();
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

        //TODO implement setEnabled;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_error.setText(errorMessage);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String value) {

        // TODO: Auto-generated method stub

    }

    /**
     * Sets the height of this category field.<p>
     *
     * @param height the height of this category field
     */
    public void setHeight(int height) {

        m_defaultHeight = height;
        m_scrollPanel.setHeight(m_defaultHeight + "px");
        m_scrollPanel.setDefaultHeight(m_defaultHeight);
        m_scrollPanel.onResizeDescendant();
    }

    /**
     * Sets if the parent category should be selected with the child or not.
     *
     * @param value if the parent categories should be selected or not
     * */
    public void setParentSelection(boolean value) {

        m_selectParent = value;
    }

    /**
     * Sets the value of the widget.<p>
     *
     * @param value the new value
     */
    public void setSelected(Object value) {

        // nothing to do
    }

    /**
     * Set the selected categories.<p>
     *
     * @param newValue String of selected categories separated by '|'
     */
    public void setSelectedAsString(String newValue) {

        setSelected(newValue);
    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
    }

    /**
     * Adds children item to the category tree and select the categories.<p>
     *
     * @param parent the parent item
     * @param children the list of children
     * @param selectedCategories the list of categories to select
     */
    private void addChildren(
        CmsTreeItem parent,
        List<CmsCategoryTreeEntry> children,
        Collection<String> selectedCategories) {

        if (children != null) {
            for (CmsCategoryTreeEntry child : children) {
                // set the category tree item and add to parent tree item
                CmsTreeItem treeItem;
                boolean isPartofPath = false;
                Iterator<String> it = selectedCategories.iterator();
                while (it.hasNext()) {
                    String path = it.next();
                    if (path.contains(child.getPath())) {
                        isPartofPath = true;
                    }
                }
                if (isPartofPath) {
                    m_singelSidePath = child.getSitePath();
                    m_valuesSet++;
                    if (m_selectParent || !hasSelectedChildren(child.getChildren(), selectedCategories)) {
                        m_allSidePath.add(child.getSitePath());
                        treeItem = buildTreeItem(child, selectedCategories, false);
                    } else {
                        treeItem = buildTreeItem(child, selectedCategories, true);
                    }
                    addChildren(treeItem, child.getChildren(), selectedCategories);
                    parent.addChild(treeItem);
                }

            }
        }
    }

    /**
     * Builds a tree item for the given category.<p>
     *
     * @param category the category
     * @param selectedCategories the selected categories
     * @param inactive true if the value should be displayed inactive
     *
     * @return the tree item widget
     */
    private CmsTreeItem buildTreeItem(
        CmsCategoryTreeEntry category,
        Collection<String> selectedCategories,
        boolean inactive) {

        CmsListInfoBean categoryBean = new CmsListInfoBean(
            category.getTitle(),
            CmsStringUtil.isNotEmptyOrWhitespaceOnly(category.getDescription())
            ? category.getDescription()
            : category.getPath(),
            null);
        // set the list item widget

        CmsDataValue categoryTreeItem = new CmsDataValue(
            500,
            4,
            CmsIconUtil.getResourceIconClasses("category", true),
            categoryBean.getTitle(),
            categoryBean.getSubTitle());
        if (inactive) {
            categoryTreeItem.setInactive();
        }
        categoryTreeItem.setTitle(categoryBean.getSubTitle());
        CmsTreeItem treeItem = new CmsTreeItem(false, categoryTreeItem);
        treeItem.setId(category.getPath());
        boolean isPartofPath = false;
        Iterator<String> it = selectedCategories.iterator();
        while (it.hasNext()) {
            String path = it.next();
            if (path.contains(category.getPath())) {
                isPartofPath = true;
            }
        }
        if (isPartofPath) {
            m_categories.add(treeItem);
            treeItem.setOpen(true);
        }
        return treeItem;
    }

    /**
     * Checks if it has selected children.<p>
     *
     * @param children the children to check
     * @param selectedCategories list of all selected categories
     *
     * @return true if it has selected children
     * */
    private boolean hasSelectedChildren(List<CmsCategoryTreeEntry> children, Collection<String> selectedCategories) {

        boolean result = false;
        if (children == null) {
            return false;
        }
        for (CmsCategoryTreeEntry child : children) {
            result = selectedCategories.contains(child.getSitePath());
            if (result) {
                return true;
            }
        }

        return result;
    }
}
