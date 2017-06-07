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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsTypesTabHandler;
import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean.TypeVisibility;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides the widget for the types tab.<p>
 *
 * It displays the available types in the given sort order.
 *
 * @since 8.0.
 */
public class CmsTypesTab extends A_CmsListTab {

    /**
     * Handles the change of the item selection.<p>
     */
    private class SelectionHandler extends A_SelectionHandler {

        /** The resource type (name/id?) as id for the selected type. */
        private String m_resourceType;

        /**
         * Constructor.<p>
         *
         * @param resourceType as id(name) for the selected type
         * @param checkBox the reference to the checkbox
         */
        public SelectionHandler(String resourceType, CmsCheckBox checkBox) {

            super(checkBox);
            m_resourceType = resourceType;
            m_selectionHandlers.add(this);
        }

        /**
         * @see org.opencms.ade.galleries.client.ui.A_CmsListTab.A_SelectionHandler#onSelectionChange()
         */
        @Override
        protected void onSelectionChange() {

            if (getCheckBox().isChecked()) {
                getTabHandler().selectType(m_resourceType);
            } else {
                getTabHandler().deselectType(m_resourceType);
            }
        }

        /**
         * @see org.opencms.ade.galleries.client.ui.A_CmsListTab.A_SelectionHandler#selectBeforeGoingToResultTab()
         */
        @Override
        protected void selectBeforeGoingToResultTab() {

            for (SelectionHandler otherHandler : m_selectionHandlers) {
                if ((otherHandler != this)
                    && (otherHandler.getCheckBox() != null)
                    && otherHandler.getCheckBox().isChecked()) {
                    otherHandler.getCheckBox().setChecked(false);
                    otherHandler.onSelectionChange();
                }
            }
            getCheckBox().setChecked(true);
            onSelectionChange();
        }
    }

    /** The list of selection handlers. */
    List<SelectionHandler> m_selectionHandlers = Lists.newArrayList();

    /** Style variable to control whether the full type list should be shown. */
    CmsStyleVariable m_typeListMode;

    /** Custom widget to display in the top bar of the types tab. */
    private Widget m_additionalTypeTabControl;

    /** The reference to the drag handler for the list elements. */
    private CmsDNDHandler m_dndHandler;

    /** The reference to the handler of this tab. */
    private CmsTypesTabHandler m_tabHandler;

    /** The switch to enable/disable the full type list. */
    private Widget m_typeModeToggle;

    /** Map of type beans to type name. */
    private Map<String, CmsResourceTypeBean> m_types;

    /**
     * Constructor.<p>
     *
     * @param tabHandler the tab handler
     * @param dndHandler the drag and drop handler
     * @param additionalControl an additional custom widget that can be displayed in the top bar
     */
    public CmsTypesTab(CmsTypesTabHandler tabHandler, CmsDNDHandler dndHandler, Widget additionalControl) {

        super(GalleryTabId.cms_tab_types);
        m_tabHandler = tabHandler;
        m_dndHandler = dndHandler;
        m_additionalTypeTabControl = additionalControl;
        init();
    }

    /**
     * Fill the content of the types tab panel.<p>
     *
     * @param typeInfos the type info beans
     * @param selectedTypes the list of types to select
     */
    public void fillContent(List<CmsResourceTypeBean> typeInfos, List<String> selectedTypes) {

        if (m_types == null) {
            m_types = new HashMap<String, CmsResourceTypeBean>();
        }
        clearList();
        m_types.clear();
        for (CmsResourceTypeBean typeBean : typeInfos) {
            m_types.put(typeBean.getType(), typeBean);
            CmsListItemWidget listItemWidget;
            CmsListInfoBean infoBean = new CmsListInfoBean(typeBean.getTitle(), typeBean.getDescription(), null);
            listItemWidget = new CmsListItemWidget(infoBean);
            listItemWidget.setIcon(CmsIconUtil.getResourceIconClasses(typeBean.getType(), false));
            listItemWidget.setUnselectable();
            CmsCheckBox checkBox = new CmsCheckBox();
            CmsListItem listItem = new CmsListItem(checkBox, listItemWidget);
            if (typeBean.isDeactivated()) {
                checkBox.disable("");
                listItem.addStyleName(
                    org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().expired());
            } else {
                SelectionHandler selectionHandler = new SelectionHandler(typeBean.getType(), checkBox);
                checkBox.addClickHandler(selectionHandler);
                listItemWidget.addClickHandler(selectionHandler);
                if ((selectedTypes != null) && selectedTypes.contains(typeBean.getType())) {
                    checkBox.setChecked(true);
                }
                listItemWidget.addButton(createSelectButton(selectionHandler));

                if (typeBean.isCreatableType() && (m_dndHandler != null)) {
                    listItem.initMoveHandle(m_dndHandler, true);
                    listItem.getMoveHandle().setTitle(Messages.get().key(Messages.GUI_TAB_TYPES_CREATE_NEW_0));
                }
            }
            listItem.setId(typeBean.getType());

            if (typeBean.getVisibility() == TypeVisibility.showOptional) {
                listItem.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().shouldOnlyShowInFullTypeList());
            }
            addWidgetToList(listItem);
        }
        updateTypeModeToggle();
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanels(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public List<CmsSearchParamPanel> getParamPanels(CmsGallerySearchBean searchObj) {

        List<CmsSearchParamPanel> result = new ArrayList<CmsSearchParamPanel>();
        for (String type : searchObj.getTypes()) {
            CmsResourceTypeBean typeBean = m_types.get(type);
            String title = type;
            if (typeBean != null) {
                title = typeBean.getTitle();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                    title = typeBean.getType();
                }
            }
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_PARAMS_LABEL_TYPES_0),
                this);
            panel.setContent(title, type);
            result.add(panel);
        }
        return result;
    }

    /**
     * Deselect the types  in the types list.<p>
     *
     * @param types the categories to deselect
     */
    public void uncheckTypes(Collection<String> types) {

        for (String type : types) {
            CmsListItem item = (CmsListItem)m_scrollList.getItem(type);
            if (item != null) {
                item.getCheckBox().setChecked(false);
            }
        }
    }

    /**
     * Updates the types list.<p>
     *
     * @param types the new types list
     * @param selectedTypes the list of types to select
     */
    public void updateContent(List<CmsResourceTypeBean> types, List<String> selectedTypes) {

        clearList();
        fillContent(types, selectedTypes);
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected LinkedHashMap<String, String> getSortList() {

        return null;

    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getTabHandler()
     */
    @Override
    protected CmsTypesTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#hasQuickSearch()
     */
    @Override
    protected boolean hasQuickSearch() {

        // quick filter not available for this tab
        return true;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#init()
     */
    @Override
    protected void init() {

        super.init();

        m_typeListMode = new CmsStyleVariable(this);
        m_typeListMode.setValue(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().typesImportant());
        final CmsCheckBox typeToggle = new CmsCheckBox(Messages.get().key(Messages.GUI_SHOW_SYSTEM_TYPES_0));
        FlowPanel toggleContainer = new FlowPanel();
        toggleContainer.add(typeToggle);
        toggleContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().typeModeSwitch());
        m_options.add(toggleContainer);
        if (m_additionalTypeTabControl != null) {
            m_options.add(m_additionalTypeTabControl);
        }
        typeToggle.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            public void onValueChange(ValueChangeEvent<Boolean> event) {

                boolean value = event.getValue().booleanValue();
                setShowAllTypes(value);
                m_list.onResizeDescendant();
            }
        });
        m_typeModeToggle = toggleContainer;
        updateTypeModeToggle();
    }

    /**
     * Enables/disables full type list mode.<p>
     *
     * @param showAllTypes true if all types should be shown
     */
    protected void setShowAllTypes(boolean showAllTypes) {

        m_typeListMode.setValue(showAllTypes ? null : I_CmsLayoutBundle.INSTANCE.galleryDialogCss().typesImportant());
        m_tabHandler.updateSize();
    }

    /**
     * Updates the type mode switch.<p>
     */
    protected void updateTypeModeToggle() {

        m_typeModeToggle.setVisible(false);
        if (m_types != null) {
            for (CmsResourceTypeBean type : m_types.values()) {
                if (type.getVisibility() == TypeVisibility.showOptional) {
                    m_typeModeToggle.setVisible(true);
                    return;

                }
            }
        }
    }
}