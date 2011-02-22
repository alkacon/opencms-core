/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapView.java,v $
 * Date   : $Date: 2011/02/22 09:22:40 $
 * Version: $Revision: 1.58 $
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

import org.opencms.ade.sitemap.client.control.CmsSitemapChangeEvent;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.control.CmsSitemapDNDController;
import org.opencms.ade.sitemap.client.control.CmsSitemapLoadEvent;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapChangeHandler;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapLoadHandler;
import org.opencms.ade.sitemap.client.hoverbar.CmsSitemapHoverbar;
import org.opencms.ade.sitemap.client.toolbar.CmsSitemapToolbar;
import org.opencms.ade.sitemap.client.ui.CmsPage;
import org.opencms.ade.sitemap.client.ui.CmsStatusIconUpdateHandler;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsHeader;
import org.opencms.gwt.client.ui.CmsInfoLoadingListItemWidget;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsToolbarPlaceHolder;
import org.opencms.gwt.client.ui.CmsListItemWidget.AdditionalInfoItem;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsResourceStateUtil;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.client.util.I_CmsAdditionalInfoLoader;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Sitemap editor.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.58 $ 
 * 
 * @since 8.0.0
 */
public final class CmsSitemapView extends A_CmsEntryPoint implements I_CmsSitemapChangeHandler, I_CmsSitemapLoadHandler {

    /** The sitemap editor modes. */
    public enum EditorMode {
        /** The navigation mode. */
        navigation,
        /** The VFS mode. */
        vfs
    }

    /** The singleton instance. */
    private static CmsSitemapView m_instance;

    /** Text metrics key. */
    private static final String TM_SITEMAP = "Sitemap";

    /** The controller. */
    protected CmsSitemapController m_controller;

    /** The displayed sitemap tree. */
    protected CmsLazyTree<CmsSitemapTreeItem> m_tree;

    /** The current sitemap editor mode. */
    private EditorMode m_editorMode;

    private CmsStyleVariable m_inNavigationStyle;

    /** The sitemap toolbar. */
    private CmsSitemapToolbar m_toolbar;

    /**
     * Returns the instance.<p>
     *
     * @return the instance
     */
    public static CmsSitemapView getInstance() {

        return m_instance;
    }

    /**
     * Creates a new tree item from the given sitemap entry.<p>
     * 
     * @param entry the sitemap entry
     * 
     * @return the new created (still orphan) tree item 
     */
    public CmsSitemapTreeItem create(final CmsClientSitemapEntry entry) {

        CmsListInfoBean infoBean = new CmsListInfoBean();
        infoBean.setTitle(entry.getTitle());
        infoBean.setSubTitle(entry.getSitePath());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_NAME_0), entry.getName());
        String shownPath = entry.getVfsPath();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(shownPath)) {
            shownPath = "-";
        }
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_VFS_PATH_0), shownPath);

        CmsInfoLoadingListItemWidget itemWidget = new CmsInfoLoadingListItemWidget(infoBean);
        final CmsSitemapTreeItem treeItem = new CmsSitemapTreeItem(itemWidget, entry);
        itemWidget.setAdditionalInfoLoader(new I_CmsAdditionalInfoLoader() {

            public void load(final AsyncCallback<List<AdditionalInfoItem>> callback) {

                if (entry.getVfsPath() == null) {
                    List<AdditionalInfoItem> infoItems = new ArrayList<AdditionalInfoItem>();
                    AdditionalInfoItem item = createResourceStateInfo(CmsResourceState.STATE_NEW);
                    infoItems.add(item);
                    //                    if (entry.getOwnProperty(CmsSitemapManager.Property.isRedirect.getName()) != null) {
                    //                        CmsLinkBean link = entry.getRedirectInfo();
                    //                        String target = "-";
                    //                        if (link != null) {
                    //                            target = link.getLink();
                    //                        }
                    //                        String title = Messages.get().key(Messages.GUI_ADDINFO_REDIRECT_0);
                    //                        AdditionalInfoItem redirectInfo = new AdditionalInfoItem(title, target, null);
                    //                        infoItems.add(redirectInfo);
                    //                    }
                    callback.onSuccess(infoItems);
                } else {
                    CmsCoreProvider.get().getResourceState(entry.getVfsPath(), new AsyncCallback<CmsResourceState>() {

                        /**
                         * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
                         */
                        public void onFailure(Throwable caught) {

                            // do nothing

                        }

                        /**
                         * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(Object o)
                         */
                        public void onSuccess(CmsResourceState result) {

                            AdditionalInfoItem item = createResourceStateInfo(result);
                            callback.onSuccess(Collections.singletonList(item));
                        }
                    });

                }

            }

            /**
             * Helper method for creating an additional info item from a resource state.<p>
             * 
             * @param state the resource state for creating the additional info item 
             * 
             * @return the additional info item 
             */
            protected AdditionalInfoItem createResourceStateInfo(CmsResourceState state) {

                final String label = org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.GUI_RESOURCE_STATE_0);
                AdditionalInfoItem item = new AdditionalInfoItem(
                    label,
                    CmsResourceStateUtil.getStateName(state),
                    CmsResourceStateUtil.getStateStyle(state));
                return item;

            }
        });

        CmsSitemapHoverbar.installOn(m_controller, treeItem);

        return treeItem;
    }

    /**
     * Creates a sitemap tree item from a client sitemap entry.<p>
     * 
     * @param entry the entry from which the sitemap tree item should be created 
     * 
     * @return the new sitemap tree item 
     */
    public CmsSitemapTreeItem createSitemapItem(CmsClientSitemapEntry entry) {

        CmsSitemapTreeItem result = create(entry);
        result.clearChildren();
        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            CmsSitemapTreeItem childItem = createSitemapItem(child);
            result.addChild(childItem);
        }
        if (entry.getChildrenLoadedInitially()) {
            result.onFinishLoading();
        }
        return result;
    }

    /**
     * Ensures the given item is visible in the viewport.<p>
     * 
     * @param item the item to see
     */
    public void ensureVisible(CmsSitemapTreeItem item) {

        // open the tree
        CmsTreeItem ti = item.getParentItem();
        while (ti != null) {
            ti.setOpen(true);
            ti = ti.getParentItem();
        }
        // scroll
        CmsDomUtil.ensureVisible(RootPanel.getBodyElement(), item.getElement(), 200);
    }

    /**
     * Returns the controller.<p>
     *
     * @return the controller
     */
    public CmsSitemapController getController() {

        return m_controller;
    }

    /**
     * Returns the editor mode.<p>
     *
     * @return the editor mode
     */
    public EditorMode getEditorMode() {

        return m_editorMode;
    }

    /**
     * Gets the list of descendants of a path and splits it into two lists, one containing the sitemap entries whose children have 
     * already been loaded, and those whose children haven't been loaded.<p>
     * 
     * @param path the path for which the open and closed descendants should be returned 
     * 
     * @return a pair whose first and second components are lists of open and closed descendant entries of the path, respectively 
     */
    public CmsPair<List<CmsClientSitemapEntry>, List<CmsClientSitemapEntry>> getOpenAndClosedDescendants(String path) {

        List<CmsClientSitemapEntry> descendants = m_controller.getLoadedDescendants(path);
        List<CmsClientSitemapEntry> openDescendants = new ArrayList<CmsClientSitemapEntry>();
        List<CmsClientSitemapEntry> closedDescendants = new ArrayList<CmsClientSitemapEntry>();
        for (CmsClientSitemapEntry entry : descendants) {
            CmsSitemapTreeItem treeItem = getTreeItem(entry.getSitePath());
            List<CmsClientSitemapEntry> listToAddTo = treeItem.isLoaded() ? openDescendants : closedDescendants;
            listToAddTo.add(entry);
        }
        return new CmsPair<List<CmsClientSitemapEntry>, List<CmsClientSitemapEntry>>(openDescendants, closedDescendants);

    }

    /**
     * Returns the tree.<p>
     * 
     * @return the tree
     */
    public CmsLazyTree<CmsSitemapTreeItem> getTree() {

        return m_tree;
    }

    /**
     * Returns the tree entry with the given path.<p>
     * 
     * @param path the path to look for
     * 
     * @return the tree entry with the given path, or <code>null</code> if not found
     */
    public CmsSitemapTreeItem getTreeItem(String path) {

        CmsSitemapData data = m_controller.getData();
        CmsClientSitemapEntry root = data.getRoot();
        String rootSitePath = root.getSitePath();
        String remainingPath = path.substring(rootSitePath.length());

        CmsSitemapTreeItem result = getRootItem();

        String[] names = CmsStringUtil.splitAsArray(remainingPath, "/");
        for (String name : names) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                continue;
            }

            result = (CmsSitemapTreeItem)result.getChild(name);
        }
        return result;

    }

    /**
     * Highlights the sitemap entry with the given path.<p>
     * 
     * @param sitePath the sitemap path of the entry to highlight
     */
    public void highlightPath(String sitePath) {

        openItemsOnPath(sitePath);
        CmsSitemapTreeItem item = getTreeItem(sitePath);
        if (item != null) {
            item.highlightTemporarily(1500);
        }
    }

    /**
     * Returns if the current sitemap editor mode is navigation.<p>
     * 
     * @return <code>true</code> if the current sitemap editor mode is navigation
     */
    public boolean isNavigationMode() {

        return EditorMode.navigation == m_editorMode;
    }

    /**
     * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapChangeHandler#onChange(org.opencms.ade.sitemap.client.control.CmsSitemapChangeEvent)
     */
    public void onChange(CmsSitemapChangeEvent changeEvent) {

        changeEvent.getChange().applyToView(this);
    }

    /**
     * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapLoadHandler#onLoad(org.opencms.ade.sitemap.client.control.CmsSitemapLoadEvent)
     */
    public void onLoad(CmsSitemapLoadEvent event) {

        CmsSitemapTreeItem target = getTreeItem(event.getEntry().getSitePath());
        target.getTree().setAnimationEnabled(false);
        target.clearChildren();
        for (CmsClientSitemapEntry child : event.getEntry().getSubEntries()) {
            target.addChild(create(child));
        }
        target.onFinishLoading();
        target.getTree().setAnimationEnabled(true);
        if (event.isSetOpen()) {
            target.setOpen(true);
        }
        m_controller.recomputeProperties();
    }

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        m_instance = this;
        RootPanel rootPanel = RootPanel.get();
        m_inNavigationStyle = new CmsStyleVariable(rootPanel);
        m_editorMode = EditorMode.navigation;
        // init
        I_CmsLayoutBundle.INSTANCE.rootCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.pageCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.clipboardCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.sitemapItemCss().ensureInjected();
        I_CmsImageBundle.INSTANCE.buttonCss().ensureInjected();

        rootPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.rootCss().root());

        // controller 
        m_controller = new CmsSitemapController();
        m_controller.addChangeHandler(this);
        m_controller.addLoadHandler(this);

        // toolbar
        m_toolbar = new CmsSitemapToolbar(m_controller);
        rootPanel.add(m_toolbar);
        rootPanel.add(new CmsToolbarPlaceHolder());

        // hoverbar
        //  m_hoverbar = new CmsSitemapHoverbar(m_controller);

        // title
        CmsHeader title = new CmsHeader(Messages.get().key(Messages.GUI_EDITOR_TITLE_0), CmsCoreProvider.get().getUri());
        title.addStyleName(I_CmsLayoutBundle.INSTANCE.rootCss().pageCenter());
        rootPanel.add(title);

        // content page
        final CmsPage page = new CmsPage();
        rootPanel.add(page);

        // initial content
        final Label loadingLabel = new Label(org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_LOADING_0));
        page.add(loadingLabel);

        // read pre-fetched data
        CmsClientSitemapEntry root = m_controller.getData().getRoot();
        CmsSitemapTreeItem rootItem = createSitemapItem(root);
        rootItem.onFinishLoading();
        rootItem.setOpen(true);

        // starting rendering
        m_tree = new CmsLazyTree<CmsSitemapTreeItem>(new I_CmsLazyOpenHandler<CmsSitemapTreeItem>() {

            /**
             * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
             */
            public void load(final CmsSitemapTreeItem target) {

                m_controller.getChildren(target.getSitePath(), true);
            }

            /**
             * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#onOpen(com.google.gwt.event.logical.shared.OpenEvent)
             */
            public void onOpen(OpenEvent<CmsSitemapTreeItem> event) {

                CmsSitemapTreeItem target = event.getTarget();
                if (target.getLoadState() != CmsLazyTreeItem.LoadState.UNLOADED) {
                    return;
                }
                target.onStartLoading();
                target.setOpen(false);
                load(target);
            }
        });
        if (m_controller.isEditable()) {
            // enable drag'n drop 
            CmsDNDHandler dndHandler = new CmsDNDHandler(new CmsSitemapDNDController(m_controller, m_toolbar));
            dndHandler.addTarget(m_tree);
            m_tree.setDNDHandler(dndHandler);
            m_tree.setDropEnabled(true);
            m_tree.setDNDTakeAll(true);
        }
        m_tree.truncate(TM_SITEMAP, 920);
        m_tree.setAnimationEnabled(true);
        m_tree.addItem(rootItem);

        // paint
        page.remove(loadingLabel);
        page.add(m_tree);

        m_controller.addPropertyUpdateHandler(new CmsStatusIconUpdateHandler());
        m_controller.recomputeProperties();
        rootItem.updateSitePath();

        // check if editable
        if (!m_controller.isEditable()) {
            // notify user
            CmsNotification.get().sendSticky(
                CmsNotification.Type.WARNING,
                Messages.get().key(Messages.GUI_NO_EDIT_NOTIFICATION_1, m_controller.getData().getNoEditReason()));
            return;
        }
        String openPath = m_controller.getData().getOpenPath();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(openPath)) {
            highlightPath(CmsResource.getFolderPath(openPath));
        }

    }

    /**
     * Sets the editor mode.<p>
     *
     * @param editorMode the editor mode to set
     */
    public void setEditorMode(EditorMode editorMode) {

        m_editorMode = editorMode;
        if (m_editorMode == EditorMode.vfs) {
            m_toolbar.setNewEnabled(false, "The NEW function is only available in navigation view.");
            m_inNavigationStyle.setValue(I_CmsLayoutBundle.INSTANCE.sitemapItemCss().vfsMode());
        } else {

            m_toolbar.setNewEnabled(true, null);
            m_inNavigationStyle.setValue(I_CmsLayoutBundle.INSTANCE.sitemapItemCss().navMode());
        }
    }

    /**
     * Updates the detail page view for a given changed entry.<p>
     * 
     * @param entry the entry which was changed 
     */
    public void updateDetailPageView(CmsClientSitemapEntry entry) {

        CmsDetailPageTable detailPageTable = m_controller.getDetailPageTable();
        List<CmsUUID> idsToUpdate = new ArrayList<CmsUUID>();
        if (entry.isDetailPage()) {
            idsToUpdate.add(entry.getId());
            idsToUpdate.addAll(detailPageTable.getAllIds());
        }
        updateEntriesById(idsToUpdate);
    }

    /**
     * Updates the entries whose id is in the given list of ids.<p>
     * 
     * @param ids a list of sitemap entry ids 
     */
    public void updateEntriesById(Collection<CmsUUID> ids) {

        Map<CmsUUID, CmsClientSitemapEntry> entries = m_controller.getEntriesById(ids);
        for (CmsClientSitemapEntry entry : entries.values()) {
            CmsSitemapTreeItem item = CmsSitemapTreeItem.getItemById(entry.getId());
            item.updateEntry(entry);
        }
    }

    /**
     * Gets the sitemap tree item widget which represents the root of the current sitemap.<p>
     * 
     * @return the root sitemap tree item widget 
     */
    protected CmsSitemapTreeItem getRootItem() {

        return (CmsSitemapTreeItem)(m_tree.getWidget(0));
    }

    /**
     * Helper method to get all sitemap tree items from the root to a given path.<p>
     * 
     * For example, if the root item has the site path '/root/', and the value of path is
     * '/root/a/b/', the sitemap tree items corresponding to '/root/', '/root/a/' and '/root/a/b'
     * will be returned (in that order).<p>
     * 
     * @param path the path for which the sitemap tree items should be returned 
     *  
     * @return the sitemap tree items on the path
     */
    private List<CmsSitemapTreeItem> getItemsOnPath(String path) {

        List<CmsSitemapTreeItem> result = new ArrayList<CmsSitemapTreeItem>();

        CmsSitemapData data = m_controller.getData();
        CmsClientSitemapEntry root = data.getRoot();
        String rootSitePath = root.getSitePath();
        String remainingPath = path.substring(rootSitePath.length());

        CmsSitemapTreeItem currentItem = getRootItem();
        result.add(currentItem);

        String[] names = CmsStringUtil.splitAsArray(remainingPath, "/");
        for (String name : names) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                continue;
            }
            currentItem = (CmsSitemapTreeItem)currentItem.getChild(name);
            result.add(currentItem);
        }
        return result;
    }

    /**
     * Opens all sitemap tree items on a path, except the last one.<p>
     * 
     * @param path the path for which all intermediate sitemap items should be opened 
     */
    private void openItemsOnPath(String path) {

        List<CmsSitemapTreeItem> itemsOnPath = getItemsOnPath(path);
        // the last item on the path shouldn't be opened 
        itemsOnPath.remove(itemsOnPath.size() - 1);
        for (CmsSitemapTreeItem item : itemsOnPath) {
            item.setOpen(true);
        }
    }
}
