/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapView.java,v $
 * Date   : $Date: 2010/05/18 12:58:17 $
 * Version: $Revision: 1.14 $
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
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Sitemap editor.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapView extends A_CmsEntryPoint implements I_CmsSitemapControllerHandler {

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
     * 
     * @return the new created (still orphan) tree item 
     */
    public CmsSitemapTreeItem create(CmsClientSitemapEntry entry) {

        CmsListInfoBean infoBean = new CmsListInfoBean();
        infoBean.setTitle(entry.getTitle());
        infoBean.setSubTitle(entry.getSitePath());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_NAME_0), entry.getName());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_VFS_PATH_0), entry.getVfsPath());
        CmsListItemWidget itemWidget = new CmsListItemWidget(infoBean);
        CmsSitemapTreeItem treeItem = new CmsSitemapTreeItem(itemWidget, entry.getSitePath());
        treeItem.setId(entry.getName());
        if (m_controller.isEditable()) {
            if (m_hoverbar == null) {
                CmsSitemapHoverbarHandler handler = new CmsSitemapHoverbarHandler(m_controller);
                m_hoverbar = new CmsSitemapHoverbar(handler);
            }
            m_hoverbar.installOn(m_controller, itemWidget, entry.getSitePath());
        }
        return treeItem;
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
     * @see org.opencms.ade.sitemap.client.I_CmsSitemapControllerHandler#onGetChildren(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
     */
    public void onGetChildren(CmsClientSitemapEntry entry) {

        CmsSitemapTreeItem target = getTreeItem(entry.getSitePath());
        target.clearChildren();
        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            target.addChild(create(child));
        }
        target.onFinishLoading();
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
        CmsSitemapTreeItem rootItem = create(root);
        rootItem.clearChildren();
        for (CmsClientSitemapEntry child : root.getSubEntries()) {
            CmsSitemapTreeItem childItem = create(child);
            rootItem.addChild(childItem);
            childItem.clearChildren();
            for (CmsClientSitemapEntry grandchild : child.getSubEntries()) {
                childItem.addChild(create(grandchild));
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

                m_controller.getChildren(target.getSitePath());
            }
        });
        m_tree.addItem(rootItem);
        m_tree.truncate(TM_SITEMAP, 920);

        // paint
        page.remove(loadingLabel);
        page.add(m_tree);
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
