/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapTreeItem.java,v $
 * Date   : $Date: 2010/03/31 12:19:02 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.ui.CmsImageButton;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.client.util.CmsCoreProvider;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;

/**
 * Sitemap entry tree item implementation.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.client.ui.tree.CmsLazyTreeItem
 * @see org.opencms.ade.sitemap.shared.CmsClientSitemapEntry
 */
public class CmsSitemapTreeItem extends CmsLazyTreeItem {

    /** Internal entry reference. */
    private CmsClientSitemapEntry m_entry;

    /**
     * Default constructor.<p>
     * 
     * @param entry the sitemap entry to use
     */
    public CmsSitemapTreeItem(final CmsClientSitemapEntry entry) {

        super(createWidget(entry));
        m_entry = entry;
    }

    /**
     * Creates the item widget for the given entry.<p>
     * 
     * @param entry the sitemap entry to create the widget for
     *  
     * @return the widget for the given entry
     */
    private static CmsListItemWidget createWidget(final CmsClientSitemapEntry entry) {

        CmsListInfoBean infoBean = new CmsListInfoBean();
        infoBean.setTitle(entry.getTitle());
        infoBean.setSubTitle(entry.getSitePath());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_NAME_0), entry.getName());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_VFS_PATH_0), entry.getVfsPath());
        CmsListItemWidget itemWidget = new CmsListItemWidget(infoBean);
        Image icon = new Image(I_CmsImageBundle.INSTANCE.magnifierIconActive());
        icon.addStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        itemWidget.setIcon(icon);
        CmsImageButton linkIcon = new CmsImageButton(I_CmsImageBundle.INSTANCE.style().magnifierIcon(), false);
        linkIcon.setTitle("Go to page");
        linkIcon.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                Window.Location.replace(CmsCoreProvider.get().link(entry.getSitePath()));
            }
        });
        itemWidget.addButton(linkIcon);
        return itemWidget;
    }

    /**
     * Adds a child for the given entry.<p>
     * 
     * @param entry the child entry to add
     */
    public void addChild(CmsClientSitemapEntry entry) {

        addChild(new CmsSitemapTreeItem(entry));
    }

    /**
     * Returns the entry.<p>
     *
     * @return the entry
     */
    public CmsClientSitemapEntry getEntry() {

        return m_entry;
    }
}
