/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ui.contextmenu;

import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.contextmenu.CmsContextMenu.ContextMenuItem;
import org.opencms.ui.contextmenu.CmsContextMenu.ContextMenuItemClickEvent;
import org.opencms.ui.contextmenu.CmsContextMenu.ContextMenuItemClickListener;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsTreeNode;

import com.vaadin.ui.themes.ValoTheme;

/**
 * Context menu builder for resource items.<p>
 */
public class CmsResourceContextMenuBuilder implements I_CmsContextMenuBuilder {

    /** Tree builder used to build the tree of menu items. */
    private CmsContextMenuTreeBuilder m_treeBuilder;

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuBuilder#buildContextMenu(org.opencms.ui.I_CmsDialogContext, org.opencms.ui.contextmenu.CmsContextMenu)
     */
    public void buildContextMenu(I_CmsDialogContext context, CmsContextMenu menu) {

        CmsContextMenuTreeBuilder treeBuilder = new CmsContextMenuTreeBuilder(context);
        m_treeBuilder = treeBuilder;
        CmsTreeNode<I_CmsContextMenuItem> tree = treeBuilder.buildAll(
            OpenCms.getWorkplaceAppManager().getMenuItemProvider().getMenuItems());
        I_CmsContextMenuItem defaultActionItem = treeBuilder.getDefaultActionItem();
        for (CmsTreeNode<I_CmsContextMenuItem> node : tree.getChildren()) {
            createItem(menu, node, context, defaultActionItem);
        }
    }

    /**
     * Gets the localized title for the context menu item by resolving any message key macros in the raw title using the current locale.<p>
     *
     * @param item the unlocalized title
     * @return the localized title
     */
    String getTitle(I_CmsContextMenuItem item) {

        return CmsVaadinUtils.localizeString(item.getTitle(A_CmsUI.get().getLocale()));
    }

    /**
     * Creates a context menu item.<p>
     *
     * @param parent the parent (either the context menu itself, or a parent item)
     * @param node the node which should be added as a context menu item
     * @param context the dialog context
     * @param defaultAction the default action item if available
     *
     * @return the created item
     */
    private ContextMenuItem createItem(
        Object parent,
        CmsTreeNode<I_CmsContextMenuItem> node,
        final I_CmsDialogContext context,
        I_CmsContextMenuItem defaultAction) {

        final I_CmsContextMenuItem data = node.getData();
        ContextMenuItem guiMenuItem = null;
        if (parent instanceof CmsContextMenu) {
            guiMenuItem = ((CmsContextMenu)parent).addItem(getTitle(data));
        } else {
            guiMenuItem = ((ContextMenuItem)parent).addItem(getTitle(data));
        }
        if (m_treeBuilder.getVisibility(data).isInActive()) {
            guiMenuItem.setEnabled(false);
            String key = m_treeBuilder.getVisibility(data).getMessageKey();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(key)) {
                guiMenuItem.setDescription(CmsVaadinUtils.getMessageText(key));
            }
        }
        if (node.getChildren().size() > 0) {
            for (CmsTreeNode<I_CmsContextMenuItem> childNode : node.getChildren()) {
                createItem(guiMenuItem, childNode, context, defaultAction);
            }
        } else {
            guiMenuItem.addItemClickListener(new ContextMenuItemClickListener() {

                public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                    data.executeAction(context);
                }
            });

        }
        // highlight the default action
        if (data.equals(defaultAction)) {
            guiMenuItem.addStyleName(ValoTheme.LABEL_BOLD);
        }
        return guiMenuItem;
    }
}
