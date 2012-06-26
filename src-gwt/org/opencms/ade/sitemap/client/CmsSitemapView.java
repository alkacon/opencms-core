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
import org.opencms.ade.sitemap.client.ui.CmsSitemapHeader;
import org.opencms.ade.sitemap.client.ui.CmsStatusIconUpdateHandler;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsPingTimer;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsListItemWidget.Background;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Sitemap editor.<p>
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

    /**
     * The sitemap tree open handler.<p>
     */
    protected class TreeOpenHandler implements I_CmsLazyOpenHandler<CmsSitemapTreeItem> {

        /** Flag indicating the tree is initializing. */
        private boolean m_initializing;

        /**
         * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
         */
        public void load(final CmsSitemapTreeItem target) {

            // not used
        }

        /**
         * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#onOpen(com.google.gwt.event.logical.shared.OpenEvent)
         */
        public void onOpen(OpenEvent<CmsSitemapTreeItem> event) {

            CmsSitemapTreeItem target = event.getTarget();
            if ((target.getLoadState() == CmsLazyTreeItem.LoadState.UNLOADED)) {
                target.onStartLoading();
                target.setOpen(false);
                getController().getChildren(target.getEntryId(), true, null);
            } else if (!m_initializing
                && ((target.getChildren().getWidgetCount() > 0) && (((CmsSitemapTreeItem)target.getChild(0)).getLoadState() == CmsLazyTreeItem.LoadState.UNLOADED))) {
                // load grand children in advance
                getController().getChildren(target.getEntryId(), false, null);
            }
        }

        /** 
         * Sets the initializing flag.<p>
         * 
         * @param initializing the initializing flag
         */
        protected void setInitializing(boolean initializing) {

            m_initializing = initializing;
        }
    }

    /** The singleton instance. */
    private static CmsSitemapView m_instance;

    /** Text metrics key. */
    private static final String TM_SITEMAP = "Sitemap";

    /** The displayed sitemap tree. */
    protected CmsLazyTree<CmsSitemapTreeItem> m_tree;

    /** The controller. */
    private CmsSitemapController m_controller;

    /** The current sitemap editor mode. */
    private EditorMode m_editorMode;

    /** Style variable which keeps track of whether we are in VFS mode or navigation mode. */
    private CmsStyleVariable m_inNavigationStyle;

    /** The sitemap toolbar. */
    private CmsSitemapToolbar m_toolbar;

    /** The registered tree items. */
    private Map<CmsUUID, CmsSitemapTreeItem> m_treeItems;

    /** The tree open handler. */
    private TreeOpenHandler m_openHandler;

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
    public CmsSitemapTreeItem create(CmsClientSitemapEntry entry) {

        CmsSitemapTreeItem treeItem = new CmsSitemapTreeItem(entry);
        CmsSitemapHoverbar.installOn(m_controller, treeItem);
        // highlight the open path
        if (isLastPage(entry)) {
            treeItem.setBackgroundColor(Background.YELLOW);
        }
        m_treeItems.put(entry.getId(), treeItem);
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
     * Returns the icon class for the given entry depending on the editor mode.<p>
     * 
     * @param entry the entry to get the icon for
     * 
     * @return the icon CSS class
     */
    public String getIconForEntry(CmsClientSitemapEntry entry) {

        String iconClass = CmsIconUtil.getResourceIconClasses(entry.getResourceTypeName(), entry.getSitePath(), false);
        if (isNavigationMode()) {
            if (m_controller.isDetailPage(entry.getId())) {
                iconClass = CmsIconUtil.getResourceIconClasses(
                    m_controller.getDetailPageInfo(entry.getId()).getIconType(),
                    false);
            } else if (!entry.isSubSitemapType()
                && CmsStringUtil.isNotEmptyOrWhitespaceOnly(entry.getDefaultFileType())) {
                iconClass = CmsIconUtil.getResourceIconClasses(entry.getDefaultFileType(), false);
            }
        }
        return iconClass;
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
     * @param entryId the id of the sitemap entry
     * 
     * @return the tree entry with the given path, or <code>null</code> if not found
     */
    public CmsSitemapTreeItem getTreeItem(CmsUUID entryId) {

        return m_treeItems.get(entryId);
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
            if (result == null) {
                return null;
            }
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
            item.highlightTemporarily(1500, isLastPage(item.getSitemapEntry()) ? Background.YELLOW : Background.DEFAULT);
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

        CmsSitemapChange change = changeEvent.getChange();
        switch (change.getChangeType()) {
            case delete:
                CmsSitemapTreeItem item = getTreeItem(change.getEntryId());
                item.getParentItem().removeChild(item);
                break;
            case undelete:
            case create:
                CmsClientSitemapEntry newEntry = m_controller.getEntryById(change.getEntryId());
                CmsSitemapTreeItem newItem = createSitemapItem(newEntry);
                getTreeItem(change.getParentId()).insertChild(newItem, newEntry.getPosition());
                break;
            case bumpDetailPage:
                updateDetailPageView(m_controller.getEntryById(change.getEntryId()));
                updateAll(m_controller.getEntryById(change.getEntryId()));
                break;
            case modify:
                if (change.hasChangedPosition() || change.hasNewParent()) {
                    CmsClientSitemapEntry entry = m_controller.getEntryById(change.getEntryId());
                    CmsSitemapTreeItem moveEntry = getTreeItem(change.getEntryId());
                    CmsSitemapTreeItem sourceParent = (CmsSitemapTreeItem)moveEntry.getParentItem();
                    getTree().setAnimationEnabled(false);
                    sourceParent.removeChild(moveEntry);
                    CmsSitemapTreeItem destParent = change.hasNewParent()
                    ? getTreeItem(change.getParentId())
                    : sourceParent;
                    if (entry.getPosition() < destParent.getChildCount()) {
                        destParent.insertChild(moveEntry, entry.getPosition());
                    } else {
                        destParent.addChild(moveEntry);
                    }
                    updateAll(entry);
                    ensureVisible(moveEntry);
                    getTree().setAnimationEnabled(true);
                    break;
                }
                //$FALL-THROUGH$
            case remove:
                updateAll(m_controller.getEntryById(change.getEntryId()));
                break;
            default:
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapLoadHandler#onLoad(org.opencms.ade.sitemap.client.control.CmsSitemapLoadEvent)
     */
    public void onLoad(CmsSitemapLoadEvent event) {

        CmsSitemapTreeItem target = getTreeItem(event.getEntry().getId());
        target.getTree().setAnimationEnabled(false);
        target.clearChildren();
        for (CmsClientSitemapEntry child : event.getEntry().getSubEntries()) {
            CmsSitemapTreeItem childItem = createSitemapItem(child);
            target.addChild(childItem);
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
        if (!checkBuildId("org.opencms.ade.sitemap")) {
            return;
        }
        CmsPingTimer.start();
        m_instance = this;
        RootPanel rootPanel = RootPanel.get();
        m_editorMode = EditorMode.navigation;
        // init
        I_CmsSitemapLayoutBundle.INSTANCE.sitemapCss().ensureInjected();
        I_CmsSitemapLayoutBundle.INSTANCE.clipboardCss().ensureInjected();
        I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().ensureInjected();
        I_CmsSitemapLayoutBundle.INSTANCE.propertiesCss().ensureInjected();
        I_CmsImageBundle.INSTANCE.buttonCss().ensureInjected();

        rootPanel.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapCss().root());
        m_treeItems = new HashMap<CmsUUID, CmsSitemapTreeItem>();
        // controller 
        m_controller = new CmsSitemapController();
        m_controller.addChangeHandler(this);
        m_controller.addLoadHandler(this);

        // toolbar
        m_toolbar = new CmsSitemapToolbar(m_controller);
        rootPanel.add(m_toolbar);

        // header
        CmsSitemapHeader title = new CmsSitemapHeader(m_controller.getData().getSitemapInfo());
        title.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapCss().pageCenter());
        rootPanel.add(title);

        // content page
        final CmsPage page = new CmsPage();
        rootPanel.add(page);

        // initial content
        final Label loadingLabel = new Label(org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_LOADING_0));
        page.add(loadingLabel);

        // initialize the tree
        m_openHandler = new TreeOpenHandler();
        m_tree = new CmsLazyTree<CmsSitemapTreeItem>(m_openHandler);
        m_inNavigationStyle = new CmsStyleVariable(m_tree);

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
        page.add(m_tree);

        // draw tree items 
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                initiateTreeItems(page, loadingLabel);
            }
        });
    }

    /**
     * Builds the tree items initially.<p>
     * 
     * @param page the page
     * @param loadingLabel the loading label, will be removed when finished
     */
    void initiateTreeItems(CmsPage page, Label loadingLabel) {

        CmsClientSitemapEntry root = m_controller.getData().getRoot();
        CmsSitemapTreeItem rootItem = createSitemapItem(root);
        rootItem.onFinishLoading();
        rootItem.setOpen(true);
        m_tree.addItem(rootItem);
        setEditorMode(EditorMode.navigation);

        m_controller.addPropertyUpdateHandler(new CmsStatusIconUpdateHandler());
        m_controller.recomputeProperties();
        rootItem.updateSitePath();

        // check if editable
        if (!m_controller.isEditable()) {
            // notify user
            CmsNotification.get().sendSticky(
                CmsNotification.Type.WARNING,
                Messages.get().key(Messages.GUI_NO_EDIT_NOTIFICATION_1, m_controller.getData().getNoEditReason()));
        }
        String openPath = m_controller.getData().getOpenPath();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(openPath)) {
            m_openHandler.setInitializing(true);
            openItemsOnPath(openPath);
            m_openHandler.setInitializing(false);
        }

        page.remove(loadingLabel);
    }

    /**
     * Removes deleted entry widget reference.<p>
     * 
     * @param entry the entry being deleted
     */
    public void removeDeleted(CmsClientSitemapEntry entry) {

        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            removeDeleted(child);
        }
        m_treeItems.remove(entry.getId());
    }

    /**
     * Sets the editor mode.<p>
     *
     * @param editorMode the editor mode to set
     */
    public void setEditorMode(EditorMode editorMode) {

        m_editorMode = editorMode;
        if (m_editorMode == EditorMode.vfs) {
            m_toolbar.setNewEnabled(false, Messages.get().key(Messages.GUI_TOOLBAR_NEW_DISABLE_0));
            m_inNavigationStyle.setValue(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().vfsMode());
        } else {

            m_toolbar.setNewEnabled(true, null);
            m_inNavigationStyle.setValue(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().navMode());
        }
        getRootItem().updateEditorMode();
    }

    /**
     * Updates the detail page view for a given changed entry.<p>
     * 
     * @param entry the entry which was changed 
     */
    public void updateDetailPageView(CmsClientSitemapEntry entry) {

        CmsDetailPageTable detailPageTable = m_controller.getDetailPageTable();
        List<CmsUUID> idsToUpdate = new ArrayList<CmsUUID>();
        if (m_controller.isDetailPage(entry)) {
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
            if (currentItem == null) {
                break;
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                continue;
            }
            currentItem = (CmsSitemapTreeItem)currentItem.getChild(name);
            if (currentItem != null) {
                result.add(currentItem);
            }
        }
        return result;
    }

    /**
     * Checks if the given entry represents the last opened page.<p>
     * 
     * @param entry the entry to check
     * 
     * @return <code>true</code> if the given entry is the last opened page
     */
    private boolean isLastPage(CmsClientSitemapEntry entry) {

        return ((entry.isInNavigation() && (entry.getId().toString().equals(m_controller.getData().getReturnCode()))) || ((entry.getDefaultFileId() != null) && entry.getDefaultFileId().toString().equals(
            m_controller.getData().getReturnCode())));
    }

    /**
     * Opens all sitemap tree items on a path, except the last one.<p>
     * 
     * @param path the path for which all intermediate sitemap items should be opened 
     */
    private void openItemsOnPath(String path) {

        List<CmsSitemapTreeItem> itemsOnPath = getItemsOnPath(path);
        for (CmsSitemapTreeItem item : itemsOnPath) {
            item.setOpen(true);
        }
    }

    /**
     * Updates the entry and it's children's view.<p>
     * 
     * @param entry the entry to update
     */
    private void updateAll(CmsClientSitemapEntry entry) {

        CmsSitemapTreeItem item = getTreeItem(entry.getId());
        if (item != null) {
            item.updateEntry(entry);
            item.updateSitePath(entry.getSitePath());
            for (CmsClientSitemapEntry child : entry.getSubEntries()) {
                updateAll(child);
            }
        }
    }
}
