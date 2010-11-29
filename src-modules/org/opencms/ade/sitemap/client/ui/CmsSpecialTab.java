/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/Attic/CmsSpecialTab.java,v $
 * Date   : $Date: 2010/11/29 10:33:35 $
 * Version: $Revision: 1.1 $
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
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapItemCss;
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
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSpecialTab extends A_CmsListTab {

    /** The drag-and-drop handler. */
    protected CmsDNDHandler m_dndHandler;

    /** The tab handler. */
    private CmsSpecialTabHandler m_tabHandler;

    /** The tag for identifying redirect items. */
    public static final String TAG_REDIRECT = "redirect";

    /** The tag for identifying items from the special tab. */
    public static final String TAG_SPECIAL = "special";

    /** The id of this tab. */
    public static final String TAB_ID = "special";

    /** Sitemap item css. */
    public static final I_CmsSitemapItemCss CSS = I_CmsLayoutBundle.INSTANCE.sitemapItemCss();

    /** 
     * Creates the special tab.<p>
     * 
     * @param tabHandler the tab handler 
     * @param dnd the drag-and-drop handler 
     */
    public CmsSpecialTab(CmsSpecialTabHandler tabHandler, CmsDNDHandler dnd) {

        super(TAB_ID);
        m_dndHandler = dnd;
        m_tabHandler = tabHandler;
        addWidgetToList(makeRedirectItem());
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
        widget.setIcon(CSS.redirect());
        CmsListItem item = new CmsListItem(widget);
        item.addTag(TAG_REDIRECT);
        item.addTag(TAG_SPECIAL);
        item.initMoveHandle(m_dndHandler);
        return item;
    }

}
