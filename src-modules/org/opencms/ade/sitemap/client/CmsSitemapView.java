/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapView.java,v $
 * Date   : $Date: 2010/06/09 13:19:35 $
 * Version: $Revision: 1.22 $
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
import org.opencms.ade.sitemap.client.control.CmsSitemapLoadEvent;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapChangeHandler;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapLoadHandler;
import org.opencms.ade.sitemap.client.hoverbar.CmsSitemapHoverbar;
import org.opencms.ade.sitemap.client.toolbar.CmsSitemapToolbar;
import org.opencms.ade.sitemap.client.ui.CmsPage;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsHeader;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.CmsToolbarPlaceHolder;
import org.opencms.gwt.client.ui.tree.A_CmsDnDDeepLazyOpenHandler;
import org.opencms.gwt.client.ui.tree.CmsDnDLazyTree;
import org.opencms.gwt.client.ui.tree.CmsDnDTreeDropEvent;
import org.opencms.gwt.client.ui.tree.CmsDnDTreeHandler;
import org.opencms.gwt.client.ui.tree.CmsDnDTreeItem;
import org.opencms.gwt.client.ui.tree.I_CmsDnDTreeDropHandler;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Sitemap editor.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.22 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapView extends A_CmsEntryPoint
implements I_CmsSitemapChangeHandler, I_CmsSitemapLoadHandler, NativePreviewHandler, ClosingHandler {

    /** Text metrics key. */
    private static final String TM_SITEMAP = "Sitemap";

    /** The controller. */
    protected CmsSitemapController m_controller;

    /** The hover bar. */
    protected CmsSitemapHoverbar m_hoverbar;

    /** The sitemap toolbar. */
    private CmsToolbar m_toolbar;

    /** The displayed sitemap tree. */
    private CmsDnDLazyTree<CmsSitemapTreeItem> m_tree;

    /**
     * Creates a new tree item from the given sitemap entry.<p>
     * 
     * @param entry the sitemap entry
     * @param originalPath the original path in case it was renamed or moved
     * 
     * @return the new created (still orphan) tree item 
     */
    public CmsSitemapTreeItem create(CmsClientSitemapEntry entry, String originalPath) {

        CmsListInfoBean infoBean = new CmsListInfoBean();
        infoBean.setTitle(entry.getTitle());
        infoBean.setSubTitle(entry.getSitePath());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_NAME_0), entry.getName());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_VFS_PATH_0), entry.getVfsPath());
        CmsListItemWidget itemWidget = new CmsListItemWidget(infoBean);
        CmsSitemapTreeItem treeItem = new CmsSitemapTreeItem(itemWidget, entry, originalPath);
        if (m_controller.isEditable()) {
            if (m_hoverbar == null) {
                m_hoverbar = new CmsSitemapHoverbar(m_controller);
            }
            m_hoverbar.installOn(m_controller, treeItem);
        }
        return treeItem;
    }

    /**
     * Ensures the given item is visible in the viewport.<p>
     * 
     * @param item the item to see
     */
    public void ensureVisible(CmsSitemapTreeItem item) {

        // open the tree
        CmsDnDTreeItem ti = item.getParentItem();
        while (ti != null) {
            ti.setOpen(true);
            ti = ti.getParentItem();
        }
        // scroll
        CmsDomUtil.ensureVisible(RootPanel.getBodyElement(), item.getElement(), 200);
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
            target.addChild(create(child, event.getOriginalPath() + child.getName() + "/"));
        }
        target.onFinishLoading();
        target.getTree().setAnimationEnabled(true);
    }

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        // init
        super.onModuleLoad();
        I_CmsLayoutBundle.INSTANCE.rootCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.pageCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.sitemapItemCss().ensureInjected();
        I_CmsImageBundle.INSTANCE.buttonCss().ensureInjected();

        RootPanel.getBodyElement().addClassName(I_CmsLayoutBundle.INSTANCE.rootCss().root());

        // controller & tree-item-factory & tool-bar

        m_controller = new CmsSitemapController();
        m_controller.addChangeHandler(this);
        m_controller.addLoadHandler(this);
        m_toolbar = new CmsSitemapToolbar(m_controller);

        RootPanel.get().add(m_toolbar);
        RootPanel.get().add(new CmsToolbarPlaceHolder());

        // title
        CmsHeader title = new CmsHeader(Messages.get().key(Messages.GUI_EDITOR_TITLE_0), CmsCoreProvider.get().getUri());
        title.addStyleName(I_CmsLayoutBundle.INSTANCE.rootCss().pageCenter());
        RootPanel.get().add(title);

        // content page
        final CmsPage page = new CmsPage();
        RootPanel.get().add(page);

        // initial content
        final Label loadingLabel = new Label(org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_LOADING_0));
        page.add(loadingLabel);

        // read pre-fetched data
        CmsClientSitemapEntry root = m_controller.getData().getRoot();
        CmsSitemapTreeItem rootItem = create(root, root.getSitePath());
        rootItem.clearChildren();
        for (CmsClientSitemapEntry child : root.getSubEntries()) {
            CmsSitemapTreeItem childItem = create(child, child.getSitePath());
            rootItem.addChild(childItem);
            childItem.clearChildren();
            for (CmsClientSitemapEntry grandchild : child.getSubEntries()) {
                childItem.addChild(create(grandchild, grandchild.getSitePath()));
            }
            childItem.onFinishLoading();
        }
        rootItem.onFinishLoading();
        rootItem.setOpen(true);

        // starting rendering
        m_tree = new CmsDnDLazyTree<CmsSitemapTreeItem>(new A_CmsDnDDeepLazyOpenHandler<CmsSitemapTreeItem>() {

            /**
             * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
             */
            public void load(final CmsSitemapTreeItem target) {

                m_controller.getChildren(target.getOriginalPath(), target.getSitePath());
            }
        });
        m_tree.addTreeDropHandler(new I_CmsDnDTreeDropHandler() {

            /**
            * @see org.opencms.gwt.client.ui.tree.I_CmsDnDTreeDropHandler#onDrop(org.opencms.gwt.client.ui.tree.CmsDnDTreeDropEvent)
            */
            public void onDrop(CmsDnDTreeDropEvent e) {

                CmsDnDTreeItem item = e.getDestTree().getItemByPath(e.getDestPath());
                m_controller.moveDnd(
                    m_controller.getEntry(e.getSrcPath()),
                    item.getPath(),
                    item.getParentItem().getItemPosition(item));
            }
        });
        m_tree.setDnDHandler(new CmsDnDTreeHandler() {

            /**
             * @see org.opencms.gwt.client.ui.CmsDnDListHandler#canDropNow()
             */
            @Override
            protected boolean canDropNow() {

                boolean cancel = !m_controller.isDirty();
                cancel &= !CmsCoreProvider.get().lockAndCheckModification(
                    CmsCoreProvider.get().getUri(),
                    m_controller.getData().getTimestamp());
                return !cancel;
            }
        });
        m_tree.setDnDEnabled(true);
        m_tree.truncate(TM_SITEMAP, 920);
        m_tree.setAnimationEnabled(true);
        m_tree.addItem(rootItem);
        // prevent drop on root level
        m_tree.enableDropTarget(false);

        // paint
        page.remove(loadingLabel);
        page.add(m_tree);

        // key events handling
        Event.addNativePreviewHandler(this);

        // unload event handling
        Window.addWindowClosingHandler(this);

        // check if editable
        if (!m_controller.isEditable()) {
            // notify user
            CmsNotification.get().sendSticky(
                CmsNotification.Type.WARNING,
                Messages.get().key(Messages.GUI_NO_EDIT_NOTIFICATION_1, m_controller.getData().getNoEditReason()));
        }
    }

    /**
     * @see com.google.gwt.user.client.Event.NativePreviewHandler#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
     */
    public void onPreviewNativeEvent(NativePreviewEvent event) {

        Event nativeEvent;
        try {
            nativeEvent = Event.as(event.getNativeEvent());
        } catch (Exception e) {
            // sometimes in dev mode, and only in dev mode, we get
            // "Found interface com.google.gwt.user.client.Event, but class was expected"
            return;
        }
        if (event.getTypeInt() != Event.ONKEYUP) {
            return;
        }
        if ((nativeEvent.getKeyCode() == 'z') || (nativeEvent.getKeyCode() == 'Z')) {
            m_controller.undo();
        }
        if ((nativeEvent.getKeyCode() == 'r') || (nativeEvent.getKeyCode() == 'R')) {
            m_controller.redo();
        }
    }

    /** 
     * @see com.google.gwt.user.client.Window.ClosingHandler#onWindowClosing(com.google.gwt.user.client.Window.ClosingEvent)
     */
    public void onWindowClosing(ClosingEvent event) {

        // unload event handling
        if (!m_controller.isDirty()) {
            return;
        }
        boolean savePage = Window.confirm(Messages.get().key(Messages.GUI_CONFIRM_DIRTY_LEAVING_0));
        if (savePage) {
            m_controller.commit(true);
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
}
