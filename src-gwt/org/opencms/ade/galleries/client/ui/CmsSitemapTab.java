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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsSitemapTabHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsSitemapEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.input.category.CmsDataValue;
import org.opencms.gwt.client.ui.tree.A_CmsLazyOpenHandler;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.shared.CmsIconUtil;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The tab widget for selecting sitemap entries.<p>
 * 
 * @since 8.5.0
 */
public class CmsSitemapTab extends A_CmsListTab {

    /** A map from tree items to the corresponding data beans. */
    protected IdentityHashMap<CmsLazyTreeItem, CmsSitemapEntryBean> m_entryMap = new IdentityHashMap<CmsLazyTreeItem, CmsSitemapEntryBean>();

    /** The tab handler. */
    private CmsSitemapTabHandler m_handler;

    /** The initialized flag. */
    private boolean m_initialized;

    /**
     * Constructor.<p>
     * 
     * @param handler the tab handler
     */
    public CmsSitemapTab(CmsSitemapTabHandler handler) {

        super(GalleryTabId.cms_tab_sitemap);
        m_scrollList.truncate("sitemap_tab", CmsGalleryDialog.DIALOG_WIDTH);
        m_handler = handler;
        addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().listOnlyTab());
        init();
    }

    /**
     * Sets the initial folders in the VFS tab.<p>
     * 
     * @param entries the root folders to display 
     */
    public void fillInitially(List<CmsSitemapEntryBean> entries) {

        clear();
        for (CmsSitemapEntryBean entry : entries) {
            CmsLazyTreeItem item = createItem(entry);
            addWidgetToList(item);
        }
        m_initialized = true;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanels(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public List<CmsSearchParamPanel> getParamPanels(CmsGallerySearchBean searchObj) {

        return Collections.emptyList();
    }

    /**
     * Returns if the tab content has been initialized.<p>
     * 
     * @return <code>true</code> if the tab content has been initialized
     */
    public boolean isInitialized() {

        return m_initialized;
    }

    /**
     * Clears the contents of the tab and resets the mapping from tree items to VFS beans.<p>
     */
    protected void clear() {

        clearList();
        m_entryMap = new IdentityHashMap<CmsLazyTreeItem, CmsSitemapEntryBean>();
    }

    /**
     * Helper method for creating a VFS tree item widget from a VFS entry bean.<p>
     * 
     * @param sitemapEntry the VFS entry bean 
     * 
     * @return the tree item widget
     */
    protected CmsLazyTreeItem createItem(final CmsSitemapEntryBean sitemapEntry) {

        String name = CmsResource.getName(sitemapEntry.getSitePath());
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        CmsDataValue dataValue = new CmsDataValue(600, 3, CmsIconUtil.getResourceIconClasses(
            sitemapEntry.getType(),
            true), sitemapEntry.getDisplayName());
        dataValue.setUnselectable();

        CmsLazyTreeItem result = new CmsLazyTreeItem(dataValue, true);

        //  dataValue.addButton(createSelectButton(selectionHandler));
        m_entryMap.put(result, sitemapEntry);
        //      m_itemsByPath.put(sitemapEntry.getSitePath(), result);
        result.setLeafStyle(false);
        result.setSmallView(true);
        if (sitemapEntry.hasChildren()) {
            for (CmsSitemapEntryBean child : sitemapEntry.getChildren()) {
                result.addChild(createItem(child));
            }
            result.onFinishLoading();
        }
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

                getTabHandler().getSubEntries(path, null, false, callback);

            }
        });
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected LinkedHashMap<String, String> getSortList() {

        return null;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getTabHandler()
     */
    @Override
    protected CmsSitemapTabHandler getTabHandler() {

        return m_handler;
    }

}
