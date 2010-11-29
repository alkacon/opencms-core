/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsSitemapTab.java,v $
 * Date   : $Date: 2010/11/29 10:33:35 $
 * Version: $Revision: 1.6 $
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.A_CmsTabHandler;
import org.opencms.ade.galleries.client.CmsSitemapTabHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsSitemapEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.tree.A_CmsLazyOpenHandler;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsPair;

import java.util.IdentityHashMap;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The tab containing the sitemap tree.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapTab extends A_CmsListTab {

    /** The map from tree items to sitemap entry beans. */
    protected IdentityHashMap<CmsLazyTreeItem, CmsSitemapEntryBean> m_entryMap = new IdentityHashMap<CmsLazyTreeItem, CmsSitemapEntryBean>();

    /** This tab's handler. */
    CmsSitemapTabHandler m_tabHandler;

    /**
     * Constructor.<p>
     * 
     * @param handler the tab handler for the sitemap tab 
     */
    public CmsSitemapTab(CmsSitemapTabHandler handler) {

        super(GalleryTabId.cms_tab_sitemap);

        m_tabHandler = handler;
        addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().listOnlyTab());
    }

    /**
     * Sets the initial sitemap entries in the sitemap tab.<p>
     * 
     * @param entries the root sitemap entries to display 
     */
    public void fillInitially(List<CmsSitemapEntryBean> entries) {

        for (CmsSitemapEntryBean entry : entries) {
            CmsLazyTreeItem item = createItem(entry);
            addWidgetToList(item);
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanel(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public CmsSearchParamPanel getParamPanel(CmsGallerySearchBean searchObj) {

        // this tab provides no search parameters 
        return null;
    }

    /**
     * Creates a tree item widget based on a sitemap entry bean.<p>
     * 
     * @param sitemapEntry the sitemap entry bean 
     * 
     * @return a tree item displaying the info from the sitemap entry bean 
     */
    protected CmsLazyTreeItem createItem(final CmsSitemapEntryBean sitemapEntry) {

        CmsListInfoBean info = new CmsListInfoBean();
        info.setTitle(sitemapEntry.getTitle());
        info.setSubTitle(sitemapEntry.getSitePath());
        CmsListItemWidget liWidget = new CmsListItemWidget(info);
        liWidget.setIcon(CmsIconUtil.getResourceIconClasses("folder", false));

        CmsPushButton selectButton = new CmsPushButton();
        // TODO: use different icon
        selectButton.setImageClass(I_CmsImageBundle.INSTANCE.style().newIcon());
        selectButton.setShowBorder(false);
        selectButton.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());

        selectButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_tabHandler.selectResource(sitemapEntry.getSitePath(), sitemapEntry.getTitle(), "sitemap");
            }
        });
        liWidget.addButton(selectButton);
        CmsLazyTreeItem result = new CmsLazyTreeItem(liWidget);
        m_entryMap.put(result, sitemapEntry);
        return result;

    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#createScrollList()
     */
    @Override
    protected CmsList<? extends I_CmsListItem> createScrollList() {

        return new CmsLazyTree<CmsLazyTreeItem>(new A_CmsLazyOpenHandler<CmsLazyTreeItem>() {

            /**
             * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
             */
            public void load(final CmsLazyTreeItem target) {

                CmsSitemapEntryBean entry = m_entryMap.get(target);
                String path = entry.getSitePath();

                AsyncCallback<List<CmsSitemapEntryBean>> callback = new AsyncCallback<List<CmsSitemapEntryBean>>() {

                    /**
                     * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
                     */
                    public void onFailure(Throwable caught) {

                        // should never be called 

                    }

                    /**
                     * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
                     */
                    public void onSuccess(List<CmsSitemapEntryBean> result) {

                        for (CmsSitemapEntryBean childEntry : result) {
                            CmsLazyTreeItem item = createItem(childEntry);
                            target.addChild(item);
                        }
                        target.onFinishLoading();
                    }
                };

                m_tabHandler.getSubEntries(path, callback);

            }
        });
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected List<CmsPair<String, String>> getSortList() {

        // not sortable
        return null;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getTabHandler()
     */
    @Override
    protected A_CmsTabHandler getTabHandler() {

        return m_tabHandler;
    }

}
