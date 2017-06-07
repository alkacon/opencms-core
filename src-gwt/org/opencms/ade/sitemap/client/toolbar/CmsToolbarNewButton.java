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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.CmsCreatableListItem;
import org.opencms.ade.sitemap.client.ui.CmsCreatableListItem.NewEntryType;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsNewResourceInfo;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Sitemap toolbar new menu button.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarNewButton extends A_CmsToolbarListMenuButton {

    /** The tag for identifying redirect items. */
    public static final String TAG_REDIRECT = "redirect";

    /** The tag for identifying items from the special tab. */
    public static final String TAG_SPECIAL = "special";

    /** The function element list. */
    private CmsList<I_CmsListItem> m_functionList;

    /** The new-elements list. */
    private CmsList<I_CmsListItem> m_newElementsList;

    /** The special elements list. */
    private CmsList<I_CmsListItem> m_detailList;

    /**
     * Constructor.<p>
     *
     * @param toolbar the toolbar instance
     * @param controller the sitemap controller
     */
    public CmsToolbarNewButton(CmsSitemapToolbar toolbar, CmsSitemapController controller) {

        super(
            Messages.get().key(Messages.GUI_TOOLBAR_NEW_BUTTON_TITLE_0),
            I_CmsButton.ButtonData.WAND_BUTTON.getIconClass(),
            toolbar,
            controller);
    }

    /**
     * @see org.opencms.ade.sitemap.client.toolbar.A_CmsToolbarListMenuButton#initContent()
     */
    @Override
    protected boolean initContent() {

        while (m_tabs.getTabCount() > 0) {
            m_tabs.removeTab(0);
        }
        boolean hasTabs = false;
        m_newElementsList = new CmsList<I_CmsListItem>();
        for (CmsNewResourceInfo info : getController().getData().getNewElementInfos()) {
            m_newElementsList.add(makeNewElementItem(info));
        }
        if (m_newElementsList.getWidgetCount() > 0) {
            hasTabs = true;
            addTab(createTab(m_newElementsList), Messages.get().key(Messages.GUI_NEW_PAGES_TAB_TITLE_0));
        }
        m_detailList = new CmsList<I_CmsListItem>();
        if (getController().getData().canEditDetailPages()) {
            for (CmsNewResourceInfo typeInfo : getController().getData().getResourceTypeInfos()) {
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(typeInfo.getCreateParameter())) {
                    CmsCreatableListItem item = makeDetailPageItem(typeInfo);
                    m_detailList.add(item);
                }
            }
        }

        for (CmsNewResourceInfo typeInfo : getController().getData().getResourceTypeInfos()) {
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(typeInfo.getCreateParameter())) {
                CmsCreatableListItem item = makeDetailPageItem(typeInfo);
                m_detailList.add(item);
            }
        }
        if (m_detailList.getWidgetCount() > 0) {
            hasTabs = true;
            addTab(createTab(m_detailList), Messages.get().key(Messages.GUI_SPECIAL_TAB_TITLE_0));
        }

        m_functionList = new CmsList<I_CmsListItem>();
        m_functionList.add(makeRedirectItem());
        m_functionList.add(makeNavigationLevelItem());

        if (m_functionList.getWidgetCount() > 0) {
            hasTabs = true;
            String tabLabel = Messages.get().key(Messages.GUI_FUNCTION_TAB_TITLE_0);
            addTab(createTab(m_functionList), tabLabel);
        }

        if (!hasTabs) {
            // no new elements available, show appropriate message

            //TODO: improve styling, add localization
            Label messageLabel = new Label(Messages.get().key(Messages.GUI_NO_CREATABLE_ELEMENTS_0));
            messageLabel.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.clipboardCss().menuTabContainer());
            SimplePanel content = new SimplePanel();
            content.setWidget(messageLabel);
            setMenuWidget(messageLabel);
        }
        return false;
    }

    /**
     * Creates a list item representing a detail page to be created.<p>
     *
     * @param typeInfo the bean for the type for which the detail page item should be created
     *
     * @return the detail page list item
     */
    private CmsCreatableListItem makeDetailPageItem(CmsNewResourceInfo typeInfo) {

        CmsListInfoBean info = new CmsListInfoBean();
        String subtitle = typeInfo.getSubTitle();
        String title = Messages.get().key(
            typeInfo.isFunction() ? Messages.GUI_FUNCTION_PAGE_TITLE_1 : Messages.GUI_DETAIL_PAGE_TITLE_1,
            typeInfo.getTitle());

        info.setTitle(title);
        info.setSubTitle(subtitle);
        CmsListItemWidget widget = new CmsListItemWidget(info);
        String iconTypeName = typeInfo.getTypeName();
        if (iconTypeName.startsWith(CmsDetailPageInfo.FUNCTION_PREFIX)) {
            iconTypeName = "function";
        }
        widget.setIcon(CmsIconUtil.getResourceIconClasses(iconTypeName, false));
        CmsCreatableListItem listItem = new CmsCreatableListItem(widget, typeInfo, NewEntryType.detailpage);
        listItem.initMoveHandle(CmsSitemapView.getInstance().getTree().getDnDHandler());
        return listItem;
    }

    /**
     * Creates a list item representing a redirect.<p>
     *
     * @return the new list item
     */
    private CmsCreatableListItem makeNavigationLevelItem() {

        CmsNewResourceInfo typeInfo = getController().getData().getNewNavigationLevelElementInfo();
        CmsListInfoBean info = new CmsListInfoBean();
        info.setTitle(typeInfo.getTitle());
        info.setSubTitle(typeInfo.getSubTitle());
        CmsListItemWidget widget = new CmsListItemWidget(info);
        widget.setIcon(CmsIconUtil.getResourceIconClasses(CmsGwtConstants.TYPE_NAVLEVEL, false));
        CmsCreatableListItem listItem = new CmsCreatableListItem(widget, typeInfo, NewEntryType.regular);
        listItem.initMoveHandle(CmsSitemapView.getInstance().getTree().getDnDHandler());
        return listItem;
    }

    /**
     * Create a new-element list item.<p>
     *
     * @param typeInfo the new-element info
     *
     * @return the list item
     */
    private CmsCreatableListItem makeNewElementItem(final CmsNewResourceInfo typeInfo) {

        CmsListInfoBean info = new CmsListInfoBean();
        info.setTitle(typeInfo.getTitle());
        info.setSubTitle(typeInfo.getSubTitle());
        if ((typeInfo.getDescription() != null) && (typeInfo.getDescription().trim().length() > 0)) {
            info.addAdditionalInfo(Messages.get().key(Messages.GUI_LABEL_DESCRIPTION_0), typeInfo.getDescription());
        }
        if (typeInfo.getVfsPath() != null) {
            info.addAdditionalInfo(Messages.get().key(Messages.GUI_LABEL_VFSPATH_0), typeInfo.getVfsPath());
        }
        if (typeInfo.getDate() != null) {
            info.addAdditionalInfo(Messages.get().key(Messages.GUI_LABEL_DATE_0), typeInfo.getDate());
        }
        CmsListItemWidget widget = new CmsListItemWidget(info);
        widget.setIcon(CmsIconUtil.getResourceIconClasses(CmsGwtConstants.TYPE_CONTAINERPAGE, false));
        CmsCreatableListItem listItem = new CmsCreatableListItem(widget, typeInfo, NewEntryType.regular);
        listItem.initMoveHandle(CmsSitemapView.getInstance().getTree().getDnDHandler(), true);
        return listItem;
    }

    /**
     * Creates a list item representing a redirect.<p>
     *
     * @return the new list item
     */
    private CmsCreatableListItem makeRedirectItem() {

        CmsNewResourceInfo typeInfo = getController().getData().getNewRedirectElementInfo();
        CmsListInfoBean info = new CmsListInfoBean();
        info.setTitle(typeInfo.getTitle());
        info.setSubTitle(Messages.get().key(Messages.GUI_REDIRECT_SUBTITLE_0));
        CmsListItemWidget widget = new CmsListItemWidget(info);
        widget.setIcon(CmsIconUtil.getResourceIconClasses("htmlredirect", false));
        CmsCreatableListItem listItem = new CmsCreatableListItem(widget, typeInfo, NewEntryType.redirect);
        listItem.initMoveHandle(CmsSitemapView.getInstance().getTree().getDnDHandler());
        return listItem;
    }
}
