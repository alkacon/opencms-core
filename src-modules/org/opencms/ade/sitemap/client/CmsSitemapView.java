/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapView.java,v $
 * Date   : $Date: 2010/04/21 07:40:21 $
 * Version: $Revision: 1.5 $
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

import org.opencms.ade.sitemap.client.ui.CmsPage;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapChange;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsHeader;
import org.opencms.gwt.client.ui.CmsToolbarPlaceHolder;
import org.opencms.gwt.client.ui.tree.A_CmsDeepLazyOpenHandler;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.util.CmsStringUtil;

import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Sitemap editor.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapView extends A_CmsEntryPoint {

    /** The sitemap controller. */
    protected CmsSitemapController m_controller;

    /** The display tree. */
    protected CmsLazyTree<CmsSitemapTreeItem> m_tree;

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();

        // init
        I_CmsLayoutBundle.INSTANCE.rootCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.pageCss().ensureInjected();
        I_CmsImageBundle.INSTANCE.buttonCss().ensureInjected();

        RootPanel.getBodyElement().addClassName(I_CmsLayoutBundle.INSTANCE.rootCss().root());

        // controller
        m_controller = new CmsSitemapController(this);

        // toolbar
        CmsSitemapToolbarHandler handler = new CmsSitemapToolbarHandler(m_controller);
        CmsSitemapToolbar toolbar = new CmsSitemapToolbar(handler);
        m_controller.setToolbar(toolbar);
        RootPanel.get().add(toolbar);
        RootPanel.get().add(new CmsToolbarPlaceHolder());

        // title
        CmsHeader title = new CmsHeader(
            Messages.get().key(Messages.GUI_EDITOR_TITLE_0),
            CmsSitemapProvider.get().getUri());
        title.addStyleName(I_CmsLayoutBundle.INSTANCE.rootCss().pageCenter());
        RootPanel.get().add(title);

        // content page
        final CmsPage page = new CmsPage();
        RootPanel.get().add(page);

        // initial content
        final Label loadingLabel = new Label(Messages.get().key(Messages.GUI_LOADING_0));
        page.add(loadingLabel);

        CmsRpcAction<CmsClientSitemapEntry> getRootAction = new CmsRpcAction<CmsClientSitemapEntry>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                start(500);
                CmsSitemapProvider.getService().getRoot(CmsSitemapProvider.get().getUri(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsClientSitemapEntry root) {

                page.remove(loadingLabel);
                m_controller.setRoot(root);

                m_tree = new CmsLazyTree<CmsSitemapTreeItem>(new A_CmsDeepLazyOpenHandler<CmsSitemapTreeItem>() {

                    /**
                     * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
                     */
                    public void load(final CmsSitemapTreeItem target) {

                        getChildren(target);
                    }

                });
                CmsSitemapTreeItem rootItem = new CmsSitemapTreeItem(root, m_controller);
                m_tree.addItem(rootItem);
                page.add(m_tree);
                getChildren(rootItem);
                rootItem.setOpen(true);
                stop();
            }
        };
        getRootAction.execute();
    }

    /**
     * Refreshes the displayed tree after a change.<p>
     * 
     * @param change the change to refresh
     */
    public void refresh(CmsClientSitemapChange change) {

        CmsClientSitemapEntry oldEntry = change.getOld();
        CmsClientSitemapEntry newEntry = change.getNew();
        switch (change.getType()) {
            case DELETE:
                CmsTreeItem deleteParent = getTreeItem(CmsResource.getParentFolder(oldEntry.getSitePath()));
                deleteParent.removeChild(oldEntry.getName());
                break;
            case EDIT:
                CmsSitemapTreeItem editEntry = (CmsSitemapTreeItem)getTreeItem(oldEntry.getSitePath());
                editEntry.setEntry(newEntry);
                break;
            case MOVE:
                CmsTreeItem sourceParent = getTreeItem(CmsResource.getParentFolder(oldEntry.getSitePath()));
                CmsTreeItem moved = sourceParent.removeChild(oldEntry.getName());
                CmsTreeItem destParent = getTreeItem(CmsResource.getParentFolder(newEntry.getSitePath()));
                destParent.insertChild(moved, change.getPosition());
                break;
            case NEW:
                CmsTreeItem newParent = getTreeItem(CmsResource.getParentFolder(newEntry.getSitePath()));
                newParent.addChild(new CmsSitemapTreeItem(newEntry, m_controller));
                break;
            default:
        }
    }

    /**
     * Returns the children entries of the given node.<p>
     * 
     * @param target the item to get the children for
     */
    protected void getChildren(final CmsSitemapTreeItem target) {

        CmsRpcAction<List<CmsClientSitemapEntry>> getChildrenAction = new CmsRpcAction<List<CmsClientSitemapEntry>>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                CmsSitemapProvider.getService().getChildren(target.getHandler().getEntry().getSitePath(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(List<CmsClientSitemapEntry> result) {

                target.clearChildren();
                m_controller.addData(target.getHandler().getEntry().getSitePath(), result);
                for (CmsClientSitemapEntry entry : result) {
                    target.addChild(new CmsSitemapTreeItem(entry, m_controller));
                }
                target.onFinishLoading();
            }
        };
        getChildrenAction.execute();
    }

    /**
     * Returns the tree entry with the given path.<p>
     * 
     * @param path the path to look for
     * 
     * @return the tree entry with the given path, or <code>null</code> if not found
     */
    private CmsTreeItem getTreeItem(String path) {

        String[] names = CmsStringUtil.splitAsArray(path, "/");
        CmsTreeItem result = null;
        for (String name : names) {
            if (result != null) {
                result = result.getChild(name);
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
}
