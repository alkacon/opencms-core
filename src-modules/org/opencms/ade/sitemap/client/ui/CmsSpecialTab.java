/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/Attic/CmsSpecialTab.java,v $
 * Date   : $Date: 2011/02/02 07:37:52 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.ui;

import org.opencms.ade.galleries.client.A_CmsTabHandler;
import org.opencms.ade.galleries.client.ui.A_CmsListTab;
import org.opencms.ade.galleries.client.ui.CmsSearchParamPanel;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapItemCss;
import org.opencms.ade.sitemap.shared.CmsResourceTypeInfo;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsPair;

import java.util.List;

/**
 * Tab containing sitemap-specific special items for drag-and-drop.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsSpecialTab extends A_CmsListTab {

    /** Sitemap item css. */
    public static final I_CmsSitemapItemCss CSS = I_CmsLayoutBundle.INSTANCE.sitemapItemCss();

    /** The id of this tab. */
    public static final String TAB_ID = "special";

    /** The tag for identifying redirect items. */
    public static final String TAG_REDIRECT = "redirect";

    /** The tag for identifying items from the special tab. */
    public static final String TAG_SPECIAL = "special";

    /** The drag-and-drop handler. */
    protected CmsDNDHandler m_dndHandler;

    /** The tab handler. */
    private CmsSpecialTabHandler m_tabHandler;

    /** 
     * Creates the special tab.<p>
     * 
     * @param tabHandler the tab handler 
     * @param dnd the drag-and-drop handler
     * @param canEditDetailPages true if the items for new detail pages should be shown in the tab  
     */
    public CmsSpecialTab(CmsSpecialTabHandler tabHandler, CmsDNDHandler dnd, boolean canEditDetailPages) {

        super(TAB_ID);
        m_dndHandler = dnd;
        m_tabHandler = tabHandler;
        addWidgetToList(makeRedirectItem());
        if (canEditDetailPages) {
            for (CmsResourceTypeInfo typeInfo : CmsSitemapView.getInstance().getController().getData().getResourceTypeInfos()) {
                CmsDetailPageListItem item = makeDetailPageItem(typeInfo);
                addWidgetToList(item);
            }
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanel(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public CmsSearchParamPanel getParamPanel(CmsGallerySearchBean searchObj) {

        // search parameter display not available
        return null;
    }

    /**
     * Creates a list item representing a detail page to be created.<p>
     * 
     * @param typeInfo the bean for the type for which the detail page item should be created
     *  
     * @return the detail page list item  
     */
    public CmsDetailPageListItem makeDetailPageItem(CmsResourceTypeInfo typeInfo) {

        CmsListInfoBean info = new CmsListInfoBean();
        String subtitle = typeInfo.getName();
        String title = "Detail page for [" + typeInfo.getTitle() + "]";
        info.setTitle(title);
        info.setSubTitle(subtitle);
        CmsListItemWidget widget = new CmsListItemWidget(info);
        widget.setIcon(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().normal());
        CmsDetailPageListItem listItem = new CmsDetailPageListItem(widget, typeInfo);
        listItem.addTag(TAG_SPECIAL);
        listItem.initMoveHandle(m_dndHandler);
        return listItem;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected List<CmsPair<String, String>> getSortList() {

        // sorting not available 
        return null;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getTabHandler()
     */
    @Override
    protected A_CmsTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * Creates a list item representing a redirect.<p>#
     * 
     * @return the new list item 
     */
    protected CmsListItem makeRedirectItem() {

        CmsListInfoBean info = new CmsListInfoBean();
        String title = Messages.get().key(Messages.GUI_REDIRECT_TITLE_0);
        String subtitle = Messages.get().key(Messages.GUI_REDIRECT_SUBTITLE_0);
        info.setTitle(title);
        info.setSubTitle(subtitle);
        CmsListItemWidget widget = new CmsListItemWidget(info);
        widget.setIcon(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().redirect());
        CmsListItem item = new CmsListItem(widget);
        item.addTag(TAG_REDIRECT);
        item.addTag(TAG_SPECIAL);

        item.initMoveHandle(m_dndHandler);
        return item;
    }

}
