/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapTreeItem.java,v $
 * Date   : $Date: 2010/03/15 15:12:54 $
 * Version: $Revision: 1.2 $
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
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItem.TagName;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.lazytree.CmsLazyTreeItem;
import org.opencms.gwt.client.util.CmsCoreProvider;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;

/**
 * Sitemap entry tree item implementation.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.client.ui.lazytree.CmsLazyTreeItem
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

        super();
        m_entry = entry;
        CmsListInfoBean infoBean = new CmsListInfoBean();
        infoBean.setTitle(m_entry.getTitle());
        infoBean.setSubTitle("blah");
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_NAME_0), m_entry.getName());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_URL_0), entry.getSitePath());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_VFS_PATH_0), entry.getResourceId());
        CmsListItem listItem = new CmsListItem(infoBean, TagName.DIV);
        listItem.setWidth("500px");
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
        linkIcon.setAccessKey('G');
        listItem.addButton(linkIcon);
        setWidget(listItem);
    }

    /**
     * Adds a child for the given entry.<p>
     * 
     * @param entry the child entry to add
     */
    public void addItem(CmsClientSitemapEntry entry) {

        addItem(new CmsSitemapTreeItem(entry));
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
