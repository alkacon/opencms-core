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

import org.opencms.ade.galleries.client.CmsSitemapTabHandler;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsSitemapEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.input.category.CmsDataValue;
import org.opencms.gwt.client.ui.tree.A_CmsLazyOpenHandler;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;

/**
 * The tab widget for selecting sitemap entries.<p>
 *
 * @since 8.5.0
 */
public class CmsSitemapTab extends A_CmsListTab {

    /** The tab handler. */
    CmsSitemapTabHandler m_handler;

    /** Flag to disable the fillDefault method (used when the tab is filled in some other way). */
    private boolean m_disableFillDefault;

    /** The initialized flag. */
    private boolean m_initialized;

    /** The list of sitemap tree items. */
    private List<CmsLazyTreeItem> m_items = new ArrayList<CmsLazyTreeItem>();

    /**
     * Constructor.<p>
     *
     * @param handler the tab handler
     */
    public CmsSitemapTab(CmsSitemapTabHandler handler) {

        super(GalleryTabId.cms_tab_sitemap);
        m_handler = handler;
        init();
    }

    /**
     * Sets the initial folders in the VFS tab.<p>
     *
     * @param entries the root folders to display
     */
    public void fill(List<CmsSitemapEntryBean> entries) {

        clear();
        for (CmsSitemapEntryBean entry : entries) {
            CmsLazyTreeItem item = createItem(entry);
            addWidgetToList(item);
        }
        m_initialized = true;
        onContentChange();
    }

    /**
     * Default way to fill the sitemap tab.<p>
     *
     * @param entries the entries to fill the tab with
     */
    public void fillDefault(List<CmsSitemapEntryBean> entries) {

        if (!m_disableFillDefault) {
            fill(entries);
            selectSite(m_handler.getDefaultSelectedSiteRoot());
        }
    }

    /**
     * Fills the sitemap tab with preloaded data.<p>
     *
     * @param entries the preloaded sitemap entries
     */
    public void fillWithPreloadInfo(List<CmsSitemapEntryBean> entries) {

        fill(entries);
        m_disableFillDefault = true;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanels(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public List<CmsSearchParamPanel> getParamPanels(CmsGallerySearchBean searchObj) {

        return Collections.emptyList();
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#hasQuickFilter()
     */
    @Override
    public boolean hasQuickFilter() {

        return true;
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
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    public void onLoad() {

        m_handler.initializeSitemapTab();

    }

    /**
     * Method which is called when the sitemap preload data is received.<p>
     *
     * @param sitemapPreloadData the sitemap tree's preloaded root entry
     */
    public void onReceiveSitemapPreloadData(CmsSitemapEntryBean sitemapPreloadData) {

        fillWithPreloadInfo(Collections.singletonList(sitemapPreloadData));
        String siteRoot = sitemapPreloadData.getSiteRoot();
        if (siteRoot != null) {
            selectSite(siteRoot);
        }
    }

    /**
     * Clears the contents of the tab and resets the mapping from tree items to VFS beans.<p>
     */
    protected void clear() {

        clearList();
        m_items.clear();
    }

    /**
     * Helper method for creating a VFS tree item widget from a VFS entry bean.<p>
     *
     * @param sitemapEntry the VFS entry bean
     *
     * @return the tree item widget
     */
    protected CmsLazyTreeItem createItem(final CmsSitemapEntryBean sitemapEntry) {

        CmsDataValue dataValue = new CmsDataValue(
            600,
            3,
            CmsIconUtil.getResourceIconClasses(sitemapEntry.getImageType(), true),
            sitemapEntry.getDisplayName());
        dataValue.setUnselectable();
        if (sitemapEntry.isHiddenEntry()) {
            dataValue.setColor("#aaaaaa");
        }
        dataValue.setSearchMatch(sitemapEntry.isSearchMatch());

        CmsLazyTreeItem result = new CmsLazyTreeItem(dataValue, true);
        result.setData(sitemapEntry);
        if (getTabHandler().hasSelectResource()) {
            dataValue.addButton(
                createSelectResourceButton(
                    m_handler.getSelectPath(sitemapEntry),
                    sitemapEntry.getStructureId(),
                    sitemapEntry.getDisplayName(),
                    sitemapEntry.getType()));
        }
        result.setLeafStyle(!sitemapEntry.isFolder());
        result.setSmallView(true);
        if (sitemapEntry.hasChildren()) {
            for (CmsSitemapEntryBean child : sitemapEntry.getChildren()) {
                result.addChild(createItem(child));
            }
            result.setOpen(true, false);
            result.onFinishLoading();
        }
        if ((sitemapEntry.getChildren() != null) && sitemapEntry.getChildren().isEmpty()) {
            result.setLeafStyle(true);
        }
        m_items.add(result);
        dataValue.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent e) {

                if (getTabHandler().hasSelectResource()) {
                    getTabHandler().selectResource(
                        m_handler.getSelectPath(sitemapEntry),
                        sitemapEntry.getStructureId(),
                        sitemapEntry.getDisplayName(),
                        sitemapEntry.getType());
                }
            }
        });
        return result;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#createScrollList()
     */
    @Override
    protected CmsList<? extends I_CmsListItem> createScrollList() {

        CmsLazyTree<CmsLazyTreeItem> result = new CmsLazyTree<CmsLazyTreeItem>(
            new A_CmsLazyOpenHandler<CmsLazyTreeItem>() {

                /**
                 * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
                 */
                public void load(final CmsLazyTreeItem target) {

                    CmsSitemapEntryBean entry = target.getData();
                    I_CmsSimpleCallback<List<CmsSitemapEntryBean>> callback = new I_CmsSimpleCallback<List<CmsSitemapEntryBean>>() {

                        public void execute(List<CmsSitemapEntryBean> loadedEntries) {

                            for (CmsSitemapEntryBean childEntry : loadedEntries) {
                                CmsLazyTreeItem item = createItem(childEntry);
                                target.addChild(item);
                            }
                            target.onFinishLoading();
                            onContentChange();
                        }
                    };

                    getTabHandler().getSubEntries(entry.getRootPath(), false, callback);
                }
            });
        result.addOpenHandler(new OpenHandler<CmsLazyTreeItem>() {

            public void onOpen(OpenEvent<CmsLazyTreeItem> event) {

                CmsLazyTreeItem target = event.getTarget();
                CmsSitemapEntryBean entry = target.getData();
                Set<CmsUUID> openItemIds = getOpenItemIds();
                openItemIds.add(entry.getStructureId());
                m_handler.onChangeTreeState(openItemIds);
                onContentChange();
            }

        });
        result.addCloseHandler(new CloseHandler<CmsLazyTreeItem>() {

            public void onClose(CloseEvent<CmsLazyTreeItem> event) {

                CmsLazyTreeItem target = event.getTarget();
                Set<CmsUUID> openItemIds = getOpenItemIds();
                CmsSitemapEntryBean entry = target.getData();
                openItemIds.remove(entry.getStructureId());
                m_handler.onChangeTreeState(openItemIds);
            }
        });
        return result;

    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected LinkedHashMap<String, String> getSortList() {

        return m_handler.getSortList();

    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getTabHandler()
     */
    @Override
    protected CmsSitemapTabHandler getTabHandler() {

        return m_handler;
    }

    /**
     * Collects the structure ids belonging to open tree entries.<p>
     *
     * @return the collected set of structure ids
     */
    Set<CmsUUID> getOpenItemIds() {

        Set<CmsUUID> result = new HashSet<CmsUUID>();
        for (CmsLazyTreeItem item : m_items) {
            CmsSitemapEntryBean entryBean = item.getData();
            if (item.isOpen()) {
                result.add(entryBean.getStructureId());
            }
        }
        return result;
    }

    /**
     * Selects a specific site root.<p>
     *
     * @param siteRoot the site root to select
     */
    private void selectSite(String siteRoot) {

        if (m_sortSelectBox != null) {
            Map<String, String> options = m_sortSelectBox.getItems();
            String option = null;
            for (Map.Entry<String, String> entry : options.entrySet()) {
                if (CmsStringUtil.comparePaths(entry.getKey(), siteRoot)) {
                    option = entry.getKey();
                    break;
                }
            }
            if (option != null) {
                m_sortSelectBox.setFormValue(option, false);
            }
        }
    }

}
