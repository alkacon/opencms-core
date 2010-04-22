/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapView.java,v $
 * Date   : $Date: 2010/04/22 09:23:34 $
 * Version: $Revision: 1.8 $
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
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsHeader;
import org.opencms.gwt.client.ui.CmsToolbarPlaceHolder;
import org.opencms.gwt.client.ui.tree.A_CmsDeepLazyOpenHandler;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Sitemap editor.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapView extends A_CmsEntryPoint {

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
        final CmsSitemapController controller = new CmsSitemapController();
        final CmsSitemapTreeItemFactory factory = new CmsSitemapTreeItemFactory(controller);
        final CmsLazyTree<CmsSitemapTreeItem> tree = new CmsLazyTree<CmsSitemapTreeItem>(
            new A_CmsDeepLazyOpenHandler<CmsSitemapTreeItem>() {

                /**
                 * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
                 */
                public void load(final CmsSitemapTreeItem target) {

                    getChildren(target, factory);
                }
            });
        final CmsSitemapToolbar toolbar = new CmsSitemapToolbar();
        toolbar.setHandler(new CmsSitemapToolbarHandler(controller));
        controller.setHandler(new CmsSitemapControllerHandler(toolbar, tree, factory));

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

        CmsRpcAction<List<CmsClientSitemapEntry>> getRootsAction = new CmsRpcAction<List<CmsClientSitemapEntry>>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                start(500);
                CmsSitemapProvider.getService().getRoots(CmsSitemapProvider.get().getUri(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(List<CmsClientSitemapEntry> roots) {

                page.remove(loadingLabel);
                controller.setRoots(roots);

                List<CmsSitemapTreeItem> items = new ArrayList<CmsSitemapTreeItem>();
                for (CmsClientSitemapEntry root : roots) {
                    CmsSitemapTreeItem rootItem = factory.create(root);
                    rootItem.clearChildren();
                    for (CmsClientSitemapEntry entry : root.getChildren()) {
                        rootItem.addChild(factory.create(entry));
                    }
                    rootItem.onFinishLoading();
                    rootItem.setOpen(true);
                    items.add(rootItem);
                }
                // paint
                page.add(tree);
                for (CmsSitemapTreeItem item : items) {
                    tree.addItem(item);
                }
                stop();
            }
        };
        getRootsAction.execute();
    }

    /**
     * Returns the children entries of the given node.<p>
     * 
     * @param target the item to get the children for
     * @param factory the sitemap tree item factory
     */
    protected void getChildren(final CmsSitemapTreeItem target, final CmsSitemapTreeItemFactory factory) {

        CmsRpcAction<List<CmsClientSitemapEntry>> getChildrenAction = new CmsRpcAction<List<CmsClientSitemapEntry>>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                CmsSitemapProvider.getService().getChildren(target.getEntry().getSitePath(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(List<CmsClientSitemapEntry> result) {

                target.clearChildren();
                for (CmsClientSitemapEntry entry : result) {
                    target.addChild(factory.create(entry));
                }
                target.getEntry().setChildren(result);
                target.onFinishLoading();
            }
        };
        getChildrenAction.execute();
    }
}
