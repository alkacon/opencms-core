/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapView.java,v $
 * Date   : $Date: 2010/05/20 09:17:29 $
 * Version: $Revision: 1.16 $
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

import org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange;
import org.opencms.ade.sitemap.client.ui.CmsPage;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsHeader;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsToolbarPlaceHolder;
import org.opencms.gwt.client.ui.tree.A_CmsDeepLazyOpenHandler;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
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
 * @version $Revision: 1.16 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapView extends A_CmsEntryPoint implements I_CmsSitemapControllerHandler, NativePreviewHandler {

    /** Text metrics key. */
    private static final String TM_SITEMAP = "Sitemap";

    /** The controller. */
    protected CmsSitemapController m_controller;

    /** The hover bar. */
    protected CmsSitemapHoverbar m_hoverbar;

    /** The sitemap toolbar. */
    private CmsSitemapToolbar m_toolbar;

    /** The displayed sitemap tree. */
    private CmsLazyTree<CmsSitemapTreeItem> m_tree;

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
        CmsSitemapTreeItem treeItem = new CmsSitemapTreeItem(itemWidget, originalPath);
        treeItem.updateSitePath(entry.getSitePath());
        if (m_controller.isEditable()) {
            if (m_hoverbar == null) {
                CmsSitemapHoverbarHandler handler = new CmsSitemapHoverbarHandler(m_controller);
                m_hoverbar = new CmsSitemapHoverbar(handler);
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
        CmsTreeItem ti = item.getParentItem();
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

        String[] names = CmsStringUtil.splitAsArray(path, "/");
        CmsSitemapTreeItem result = null;
        for (String name : names) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                // in case of leading slash
                continue;
            }
            if (result != null) {
                result = (CmsSitemapTreeItem)result.getChild(name);
            } else {
                // match the root node
                result = m_tree.getItem(name);
            }
            if (result == null) {
                // not found
                break;
            }
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.client.I_CmsSitemapControllerHandler#onChange(org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange)
     */
    public void onChange(I_CmsClientSitemapChange change) {

        change.applyToView(this);
    }

    /**
     * @see org.opencms.ade.sitemap.client.I_CmsSitemapControllerHandler#onClearUndo()
     */
    public void onClearUndo() {

        m_toolbar.getRedoButton().disable(Messages.get().key(Messages.GUI_DISABLED_REDO_0));
    }

    /**
     * @see org.opencms.ade.sitemap.client.I_CmsSitemapControllerHandler#onFirstUndo()
     */
    public void onFirstUndo() {

        m_toolbar.getRedoButton().enable();
    }

    /**
     * @see org.opencms.ade.sitemap.client.I_CmsSitemapControllerHandler#onGetChildren(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry, String)
     */
    public void onGetChildren(CmsClientSitemapEntry entry, String originalPath) {

        CmsSitemapTreeItem target = getTreeItem(entry.getSitePath());
        target.getTree().setAnimationEnabled(false);
        target.clearChildren();
        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            target.addChild(create(child, originalPath + child.getName() + "/"));
        }
        target.onFinishLoading();
        target.getTree().setAnimationEnabled(true);
    }

    /**
     * @see org.opencms.ade.sitemap.client.I_CmsSitemapControllerHandler#onLastRedo()
     */
    public void onLastRedo() {

        m_toolbar.getRedoButton().disable(Messages.get().key(Messages.GUI_DISABLED_REDO_0));
    }

    /**
     * @see org.opencms.ade.sitemap.client.I_CmsSitemapControllerHandler#onLastUndo()
     */
    public void onLastUndo() {

        m_toolbar.getSaveButton().disable(Messages.get().key(Messages.GUI_DISABLED_SAVE_0));
        m_toolbar.getResetButton().disable(Messages.get().key(Messages.GUI_DISABLED_RESET_0));
        m_toolbar.getUndoButton().disable(Messages.get().key(Messages.GUI_DISABLED_UNDO_0));
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
        I_CmsImageBundle.INSTANCE.buttonCss().ensureInjected();

        RootPanel.getBodyElement().addClassName(I_CmsLayoutBundle.INSTANCE.rootCss().root());

        // controller & tree-item-factory & tool-bar
        m_controller = new CmsSitemapController(this);
        m_toolbar = new CmsSitemapToolbar(m_controller);
        m_toolbar.setHandler(new CmsSitemapToolbarHandler(m_controller));

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
        m_tree = new CmsLazyTree<CmsSitemapTreeItem>(new A_CmsDeepLazyOpenHandler<CmsSitemapTreeItem>() {

            /**
             * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
             */
            public void load(final CmsSitemapTreeItem target) {

                m_controller.getChildren(target.getOriginalPath(), target.getSitePath());
            }
        });
        m_tree.addItem(rootItem);
        m_tree.truncate(TM_SITEMAP, 920);
        m_tree.setAnimationEnabled(true);

        // paint
        page.remove(loadingLabel);
        page.add(m_tree);

        // key events handling
        Event.addNativePreviewHandler(this);

        // unload event handling
        Window.addWindowClosingHandler(new ClosingHandler() {

            /**
             * @see com.google.gwt.user.client.Window.ClosingHandler#onWindowClosing(com.google.gwt.user.client.Window.ClosingEvent)
             */
            public void onWindowClosing(ClosingEvent event) {

                if (!m_controller.isDirty()) {
                    return;
                }
                boolean savePage = Window.confirm(Messages.get().key(Messages.GUI_CONFIRM_DIRTY_LEAVING_0));
                if (savePage) {
                    m_controller.commit(true);
                }
            }
        });
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
     * Will be triggered on reset.<p>
     */
    public void onReset() {

        m_toolbar.getSaveButton().disable(Messages.get().key(Messages.GUI_DISABLED_SAVE_0));
        m_toolbar.getResetButton().disable(Messages.get().key(Messages.GUI_DISABLED_RESET_0));
        m_toolbar.getUndoButton().disable(Messages.get().key(Messages.GUI_DISABLED_UNDO_0));
    }

    /**
     * Will be triggered when the sitemap is changed in anyway for the first time.<p> 
     */
    public void onStartEdit() {

        m_toolbar.getSaveButton().enable();
        m_toolbar.getResetButton().enable();
        m_toolbar.getUndoButton().enable();
    }
}
