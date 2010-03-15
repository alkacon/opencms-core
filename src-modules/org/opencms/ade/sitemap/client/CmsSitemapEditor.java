/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapEditor.java,v $
 * Date   : $Date: 2010/03/15 15:12:54 $
 * Version: $Revision: 1.1 $
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
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.sitemap.client.util.CmsSitemapProvider;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsHeader;
import org.opencms.gwt.client.ui.CmsImageButton;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.CmsToolbarPlaceHolder;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.lazytree.A_CmsLazyOpenHandler;
import org.opencms.gwt.client.ui.lazytree.CmsLazyTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Sitemap editor.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapEditor extends A_CmsEntryPoint {

    /** The sitemap service instance. */
    private I_CmsSitemapServiceAsync m_sitemapSvc;

    /**
     * Returns the sitemap service instance.<p>
     * 
     * @return the sitemap service instance
     */
    protected I_CmsSitemapServiceAsync getSitemapService() {

        if (m_sitemapSvc == null) {
            m_sitemapSvc = GWT.create(I_CmsSitemapService.class);
        }
        return m_sitemapSvc;
    }

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();

        I_CmsLayoutBundle.INSTANCE.rootCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.pageCss().ensureInjected();

        RootPanel.getBodyElement().addClassName(I_CmsLayoutBundle.INSTANCE.rootCss().root());

        CmsToolbar toolBar = new CmsToolbar();
        toolBar.addLeft(new CmsImageButton(I_CmsImageBundle.INSTANCE.style().editorIcon(), true));
        toolBar.addRight(new CmsImageButton(I_CmsImageBundle.INSTANCE.style().deleteIcon(), true));
        RootPanel.get().add(toolBar);

        RootPanel.get().add(new CmsToolbarPlaceHolder());
        CmsHeader title = new CmsHeader(Messages.get().key(
            Messages.GUI_EDITOR_TITLE_1,
            CmsSitemapProvider.get().getUri()));
        title.addStyleName(I_CmsLayoutBundle.INSTANCE.rootCss().pageCenter());
        RootPanel.get().add(title);

        final CmsPage page = new CmsPage();
        RootPanel.get().add(page);

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
                getSitemapService().getSitemapEntry("/demo_t3/", this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsClientSitemapEntry root) {

                root.setName("demo_t3");
                page.remove(loadingLabel);

                CmsLazyTree<CmsSitemapTreeItem> tree = new CmsLazyTree<CmsSitemapTreeItem>(
                    new A_CmsLazyOpenHandler<CmsSitemapTreeItem>() {

                        /**
                         * @see org.opencms.gwt.client.ui.lazytree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.lazytree.CmsLazyTreeItem)
                         */
                        public void load(final CmsSitemapTreeItem target) {

                            CmsRpcAction<CmsClientSitemapEntry[]> getChildrenAction = new CmsRpcAction<CmsClientSitemapEntry[]>() {

                                /**
                                * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                                */
                                @Override
                                public void execute() {

                                    // Make the call to the sitemap service
                                    start(1000);
                                    getSitemapService().getSitemapChildren(target.getEntry().getSitePath(), this);
                                }

                                /**
                                * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                                */
                                @Override
                                public void onResponse(CmsClientSitemapEntry[] result) {

                                    target.removeItems();
                                    for (CmsClientSitemapEntry entry : result) {
                                        target.addItem(entry);
                                    }
                                    target.setState(true);
                                    stop();
                                }
                            };
                            getChildrenAction.execute();
                        }

                    });
                tree.addItem(new CmsSitemapTreeItem(root));
                page.add(tree);
                stop();
            }

        };
        getRootAction.execute();
    }
}
