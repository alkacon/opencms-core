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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.tree.A_CmsLazyOpenHandler;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsVfsEntryBean;

import java.util.IdentityHashMap;
import java.util.List;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;

/**
 * The vfs-selector popup.<p>
 * 
 * @since 8.0.0
 */
public class CmsVfsSelector extends CmsPopup {

    /**
     * The select click handler.<p>
     */
    private class SelectClickHandler implements ClickHandler {

        /** The select path. */
        private String m_path;

        /**
         * Constructor.<p>
         * 
         * @param path the path
         */
        protected SelectClickHandler(String path) {

            m_path = path;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            onSelect(m_path);

        }
    }

    /** A map from tree items to the corresponding data beans. */
    protected IdentityHashMap<CmsLazyTreeItem, CmsVfsEntryBean> m_entryMap;

    /** The callback to execute on select. */
    protected I_CmsSimpleCallback<String> m_selectCallback;

    /** The vfs-tree. */
    protected CmsLazyTree<CmsLazyTreeItem> m_tree;

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#center()
     */
    @Override
    public void center() {

        if (m_tree == null) {
            initContent();
        }
        adjustMaxHeight();
        super.center();
    }

    /**
     * Sets the select callback.<p>
     * 
     * @param callback the callback to set
     */
    public void setSelectCallback(I_CmsSimpleCallback<String> callback) {

        m_selectCallback = callback;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        if (m_tree == null) {
            initContent();
        }
        super.show();
    }

    /**
     * Creates a vfs-tree entry widget.<p>
     * 
     * @param entry the entry bean
     * 
     * @return the entry widget
     */
    protected CmsLazyTreeItem createItem(CmsVfsEntryBean entry) {

        String iconStyle = CmsIconUtil.getResourceIconClasses(entry.getResourceType(), entry.getName(), true);
        HTML itemWidget = new HTML("<div class=\""
            + iconStyle
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().inlineBlock()
            + "\"></div>"
            + entry.getName());
        itemWidget.setStyleName(I_CmsLayoutBundle.INSTANCE.singleLineItemCss().itemFace());
        itemWidget.addClickHandler(new SelectClickHandler(entry.getPath()));
        CmsLazyTreeItem result = new CmsLazyTreeItem(itemWidget, false);
        m_entryMap.put(result, entry);
        result.setLeafStyle(!entry.isFolder());
        result.addStyleName(I_CmsLayoutBundle.INSTANCE.singleLineItemCss().singleLineItem());
        return result;
    }

    /**
     * Execute on select.<p>
     * 
     * @param path the selected path
     */
    protected void onSelect(String path) {

        m_selectCallback.execute(path);
    }

    /**
     * Clears the contents of the tab and resets the mapping from tree items to VFS beans.<p>
     */
    void clearTree() {

        if (m_tree != null) {
            m_tree.clear();
            m_entryMap = new IdentityHashMap<CmsLazyTreeItem, CmsVfsEntryBean>();
        }
    }

    /**
     * Adjusts the max-height of the tree.<p>
     */
    private void adjustMaxHeight() {

        if (m_tree != null) {
            int maxHeight = Window.getClientHeight() - 60;
            m_tree.getElement().getStyle().setPropertyPx("maxHeight", maxHeight);
        }

    }

    /**
     * Initializes the content.<p>
     */
    private void initContent() {

        m_entryMap = new IdentityHashMap<CmsLazyTreeItem, CmsVfsEntryBean>();
        m_tree = new CmsLazyTree<CmsLazyTreeItem>(new A_CmsLazyOpenHandler<CmsLazyTreeItem>() {

            /**
             * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
             */
            public void load(final CmsLazyTreeItem target) {

                CmsVfsEntryBean entry = m_entryMap.get(target);
                final String path = entry.getPath();
                CmsRpcAction<List<CmsVfsEntryBean>> action = new CmsRpcAction<List<CmsVfsEntryBean>>() {

                    @Override
                    public void execute() {

                        start(0, false);
                        CmsCoreProvider.getVfsService().getChildren(path, this);
                    }

                    @Override
                    protected void onResponse(List<CmsVfsEntryBean> result) {

                        stop(false);
                        for (CmsVfsEntryBean childEntry : result) {
                            CmsLazyTreeItem item = createItem(childEntry);
                            target.addChild(item);
                        }
                        target.onFinishLoading();
                        center();
                    }

                };
                action.execute();
            }
        });
        m_tree.getElement().getStyle().setOverflow(Overflow.AUTO);
        add(m_tree);
        CmsRpcAction<List<CmsVfsEntryBean>> action = new CmsRpcAction<List<CmsVfsEntryBean>>() {

            @Override
            public void execute() {

                start(0, false);
                CmsCoreProvider.getVfsService().getRootEntries(this);
            }

            @Override
            protected void onResponse(List<CmsVfsEntryBean> result) {

                stop(false);
                for (CmsVfsEntryBean entry : result) {
                    CmsLazyTreeItem item = createItem(entry);
                    m_tree.add(item);
                }
                center();
            }

        };
        action.execute();

    }
}
