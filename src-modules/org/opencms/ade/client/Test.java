/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/client/Attic/Test.java,v $
 * Date   : $Date: 2010/03/03 15:33:13 $
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

package org.opencms.ade.client;

import org.opencms.gwt.client.rpc.CmsRpcAction;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

/**
 *
 */
public class Test implements EntryPoint {

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    public void onModuleLoad() {

        RootPanel.get().add(new Label(Messages.get().key(Messages.GUI_LOADING_0)));

        // Set up the callback object.
        CmsRpcAction<CmsClientSitemapEntry> getSitemapAction = new CmsRpcAction<CmsClientSitemapEntry>() {

            /** The sitemap service instance. */
            private I_CmsSitemapServiceAsync m_sitemapSvc;

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                start(0);
                getSitemapService().getSitemap(this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsClientSitemapEntry result) {

                RootPanel.get().clear();
                Tree tree = new Tree();
                addEntry(tree, null, result);
                RootPanel.get().add(tree);
                stop();
            }

            /**
             * Adds recursively the given sitemap entry to the given tree widget.<p>
             * 
             * @param tree the tree
             * @param parent the parent node, initially <code>null</code>
             * @param entry the sitemap entry to add
             */
            private void addEntry(Tree tree, TreeItem parent, CmsClientSitemapEntry entry) {

                TreeItem treeItem;
                if (parent == null) {
                    treeItem = tree.addItem(Messages.get().key(Messages.GUI_SITEMAP_0));
                } else {
                    treeItem = parent.addItem(entry.getName());
                }
                treeItem.setState(true);
                for (CmsClientSitemapEntry entries : entry.getSubEntries()) {
                    addEntry(tree, treeItem, entries);
                }
            }

            /**
             * Returns the sitemap service instance.<p>
             * 
             * @return the sitemap service instance
             */
            private I_CmsSitemapServiceAsync getSitemapService() {

                if (m_sitemapSvc == null) {
                    m_sitemapSvc = GWT.create(I_CmsSitemapService.class);
                }
                return m_sitemapSvc;
            }
        };
        getSitemapAction.execute();
    }
}
