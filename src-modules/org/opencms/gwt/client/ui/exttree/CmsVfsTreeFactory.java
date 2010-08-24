/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/exttree/Attic/CmsVfsTreeFactory.java,v $
 * Date   : $Date: 2010/08/24 15:15:14 $
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

package org.opencms.gwt.client.ui.exttree;

import org.opencms.gwt.shared.CmsVfsEntryBean;

import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.TreeBeanModelReader;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreeStyle;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Utility class for creating GXT trees for selecting VFS resources.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public final class CmsVfsTreeFactory {

    /** 
     * Hidden default constructor.<p>
     */
    private CmsVfsTreeFactory() {

        // will never be called 
    }

    /**
     * Creates a tree widget for browsing the VFS tree.<p>
     * 
     * @param selectionHandler the tree selection handler 
     * 
     * @return a tree widget 
     */
    public static TreePanel<BeanModel> createVfsTree(SelectionChangedListener<BeanModel> selectionHandler) {

        CmsVfsTreeProxy proxy = new CmsVfsTreeProxy();
        TreeBeanModelReader reader = new TreeBeanModelReader();
        TreeLoader<BeanModel> loader = new BaseTreeLoader<BeanModel>(proxy, reader) {

            /**
             * @see com.extjs.gxt.ui.client.data.BaseTreeLoader#hasChildren(com.extjs.gxt.ui.client.data.ModelData)
             */
            @Override
            public boolean hasChildren(BeanModel parent) {

                CmsVfsEntryBean model = parent.getBean();
                return model.hasChildren();
            }
        };
        TreeStore<BeanModel> store = new TreeStore<BeanModel>(loader);
        store.setKeyProvider(new ModelKeyProvider<BeanModel>() {

            /**
             * @see ModelKeyProvider#getKey(com.extjs.gxt.ui.client.data.ModelData model)
             */
            public String getKey(BeanModel model) {

                CmsVfsEntryBean entry = model.getBean();
                return entry.getPath();
            }
        });
        final TreePanel<BeanModel> tree = new TreePanel<BeanModel>(store);
        tree.setDisplayProperty("name");
        final TreeStyle treeStyle = tree.getStyle();
        tree.setIconProvider(new ModelIconProvider<BeanModel>() {

            /**
             * @see ModelIconProvider#getIcon(com.extjs.gxt.ui.client.data.ModelData)
             */
            public AbstractImagePrototype getIcon(BeanModel model) {

                CmsVfsEntryBean entry = model.getBean();
                boolean expanded = tree.isExpanded(model);
                if (entry.isFolder()) {
                    // we never want to display the leaf icon on folders, even if they have no children 
                    return expanded ? treeStyle.getNodeOpenIcon() : treeStyle.getNodeCloseIcon();
                }
                return treeStyle.getLeafIcon();
            }
        });
        if (selectionHandler != null) {
            tree.getSelectionModel().addSelectionChangedListener(selectionHandler);
        }
        return tree;
    }
}
