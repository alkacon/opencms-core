/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/client/Attic/SitemapView.java,v $
 * Date   : $Date: 2010/03/11 11:26:12 $
 * Version: $Revision: 1.3 $
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

package org.opencms.ade.client;

import org.opencms.ade.shared.CmsClientSitemapEntry;
import org.opencms.ade.shared.rpc.I_CmsSitemapService;
import org.opencms.ade.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.lazytree.A_CmsLazyOpenHandler;
import org.opencms.gwt.client.ui.lazytree.CmsLazyTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Testing the sitemap.<p>
 */
public class SitemapView extends A_CmsEntryPoint {

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
        RootPanel.get().add(new Label(Messages.get().key(Messages.GUI_LOADING_0)));

        CmsRpcAction<CmsClientSitemapEntry> getRootAction = new CmsRpcAction<CmsClientSitemapEntry>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                start(1000);
                getSitemapService().getSitemapEntry("/demo_t3/", this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsClientSitemapEntry root) {

                root.setName("demo_t3");
                RootPanel.get().clear();

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
                RootPanel.get().add(tree);
                stop();
            }

        };
        getRootAction.execute();
    }
}
