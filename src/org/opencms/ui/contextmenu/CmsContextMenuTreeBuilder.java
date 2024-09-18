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

import org.opencms.main.CmsLog;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.actions.CmsContextMenuActionItem;
import org.opencms.ui.actions.I_CmsDefaultAction;
import org.opencms.util.CmsTreeNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Helper class for building context menus from the list of available context menu items.<p>
 */
public class CmsContextMenuTreeBuilder {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContextMenuTreeBuilder.class);

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The default action item. */
    private I_CmsContextMenuItem m_defaultActionItem;

    /** Cached visibilities for context menu entries. */
    private IdentityHashMap<I_CmsContextMenuItem, CmsMenuItemVisibilityMode> m_visiblities = new IdentityHashMap<I_CmsContextMenuItem, CmsMenuItemVisibilityMode>();

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsContextMenuTreeBuilder(I_CmsDialogContext context) {

        m_context = context;
    }

    /**
     * Builds the complete context menu from the given available items.<p>
     *
     * @param availableItems the available items
     *
     * @return the complete context menu
     */
    public CmsTreeNode<I_CmsContextMenuItem> buildAll(List<I_CmsContextMenuItem> availableItems) {

        CmsTreeNode<I_CmsContextMenuItem> result = buildTree(availableItems);
        removeEmptySubtrees(result);
        return result;

    }

    /**
     * Builds a tree from a list of available context menu items.<p>
     *
     * The root node of the returned tree has no useful data, its child nodes correspond to the top-level
     * entries of the ccontext menu.
     *
     * @param items the available context menu items
     * @return the context menu item tree
     */
    public CmsTreeNode<I_CmsContextMenuItem> buildTree(List<I_CmsContextMenuItem> items) {

        items = new ArrayList<I_CmsContextMenuItem>(items);

        Map<String, List<I_CmsContextMenuItem>> itemsById = items.stream().collect(
            Collectors.groupingBy(item -> item.getId()));
        List<I_CmsContextMenuItem> uniqueItems = itemsById.values().stream().map(itemsForCurrentId -> {
            Collections.sort(itemsForCurrentId, Comparator.comparing(item -> -item.getPriority())); // highest priority first
            int i;
            for (i = 0; i < (itemsForCurrentId.size() - 1); i++) { // the last entry is *not* part of the iteration
                CmsMenuItemVisibilityMode visibility = getVisibility(itemsForCurrentId.get(i));
                if (!visibility.isUseNext()) {
                    break;
                }
            }
            return itemsForCurrentId.get(i); // i is the first index that didn't return USE_NEXT (it may be the index of the last item, which we didn't ask for its visibility)
        }).filter(item -> !getVisibility(item).isInVisible()).collect(Collectors.toList());

        if (m_context.getResources().size() == 1) {
            m_defaultActionItem = findDefaultAction(uniqueItems);
        }

        // Now sort by order. Since all children of a node should be processed in one iteration of the following loop,
        // this order also applies to the child order of each tree node built in the next step

        Collections.sort(uniqueItems, new Comparator<I_CmsContextMenuItem>() {

            public int compare(I_CmsContextMenuItem a, I_CmsContextMenuItem b) {

                return Float.compare(a.getOrder(), b.getOrder());
            }
        });
        Set<String> processedIds = Sets.newHashSet();
        boolean changed = true;
        Map<String, CmsTreeNode<I_CmsContextMenuItem>> treesById = Maps.newHashMap();

        // Create childless tree node for each item
        for (I_CmsContextMenuItem item : uniqueItems) {
            CmsTreeNode<I_CmsContextMenuItem> node = new CmsTreeNode<I_CmsContextMenuItem>();
            node.setData(item);
            treesById.put(item.getId(), node);
        }
        CmsTreeNode<I_CmsContextMenuItem> root = new CmsTreeNode<I_CmsContextMenuItem>();

        // Use null as the root node, which does not have any useful data
        treesById.put(null, root);

        // Iterate through list multiple times, each time only processing those items whose parents
        // we have encountered in a previous iteration (actually, in the last iteration). We do this so that the resulting
        // tree is actually a tree and contains no cycles, even if there is a reference cycle between the context menu items via their parent ids.
        // (Items which form such a cycle will never be reached.)
        while (changed) {
            changed = false;
            Iterator<I_CmsContextMenuItem> iterator = uniqueItems.iterator();
            Set<String> currentLevel = Sets.newHashSet();
            while (iterator.hasNext()) {
                I_CmsContextMenuItem currentItem = iterator.next();
                String parentId = currentItem.getParentId();
                if ((parentId == null) || processedIds.contains(parentId)) {
                    changed = true;
                    iterator.remove();
                    currentLevel.add(currentItem.getId());
                    treesById.get(parentId).addChild(treesById.get(currentItem.getId()));
                }
            }
            processedIds.addAll(currentLevel);
        }
        return root;
    }

    /**
     * Returns the default action item if available.<p>
     * Only available once {@link #buildTree(List)} or {@link #buildAll(List)} has been executed.<p>
     *
     * @return the default action item
     */
    public I_CmsContextMenuItem getDefaultActionItem() {

        return m_defaultActionItem;
    }

    /**
     * Gets the visibility for a given item (cached, if possible).<p>
     *
     * @param item the item
     * @return the visibility of that item
     */
    public CmsMenuItemVisibilityMode getVisibility(I_CmsContextMenuItem item) {

        CmsMenuItemVisibilityMode result = m_visiblities.get(item);
        if (result == null) {
            result = item.getVisibility(m_context);
            m_visiblities.put(item, result);
        }
        return result;
    }

    /**
     * Recursively remove subtrees (destructively!) which do not contain any 'leaf' context menu items.<p>
     *
     * @param root the root of the tree to process
     */
    public void removeEmptySubtrees(CmsTreeNode<I_CmsContextMenuItem> root) {

        List<CmsTreeNode<I_CmsContextMenuItem>> children = root.getChildren();
        if ((root.getData() != null) && root.getData().isLeafItem()) {
            children.clear();
        } else {
            Iterator<CmsTreeNode<I_CmsContextMenuItem>> iter = children.iterator();
            while (iter.hasNext()) {
                CmsTreeNode<I_CmsContextMenuItem> node = iter.next();
                removeEmptySubtrees(node);
                if ((node.getData() != null) && !node.getData().isLeafItem() && (node.getChildren().size() == 0)) {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Evaluates the default action if any for highlighting within the menu.<p>
     *
     * @param items the menu items
     *
     * @return the default action if available
     */
    private I_CmsContextMenuItem findDefaultAction(Collection<I_CmsContextMenuItem> items) {

        I_CmsContextMenuItem result = null;
        int resultRank = -1;
        for (I_CmsContextMenuItem menuItem : items) {
            if ((menuItem instanceof CmsContextMenuActionItem)
                && (((CmsContextMenuActionItem)menuItem).getWorkplaceAction() instanceof I_CmsDefaultAction)) {
                I_CmsDefaultAction action = (I_CmsDefaultAction)((CmsContextMenuActionItem)menuItem).getWorkplaceAction();
                if (getVisibility(menuItem).isActive()) {
                    if (result == null) {
                        result = menuItem;
                        resultRank = action.getDefaultActionRank(m_context);
                    } else {
                        int rank = action.getDefaultActionRank(m_context);
                        if (rank > resultRank) {
                            result = menuItem;
                            resultRank = rank;
                        }
                    }
                }
            }
        }
        return result;
    }

}
