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

package org.opencms.ui.apps;

import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Helper class for building a tree of categories/apps which should be displayed from the list of available apps and categories.<p>
 */
public class CmsAppHierarchyBuilder {

    /** The logger instance for this tree. */
    private static final Log LOG = CmsLog.getLog(CmsAppHierarchyBuilder.class);

    /** The list of available categories. */
    private List<I_CmsAppCategory> m_appCategoryList = Lists.newArrayList();

    /** The list of available app configurations. */
    private List<I_CmsWorkplaceAppConfiguration> m_appConfigs = Lists.newArrayList();

    /** The working set of category tree nodes. */
    private Map<String, CmsAppCategoryNode> m_nodes = Maps.newHashMap();

    /** The root tree node. */
    private CmsAppCategoryNode m_rootNode = new CmsAppCategoryNode();

    /**
     * Adds an app configuration.<p>
     *
     * @param appConfig the app configuration to add
     */
    public void addAppConfiguration(I_CmsWorkplaceAppConfiguration appConfig) {

        m_appConfigs.add(appConfig);
    }

    /**
     * Adds an app category.<p>
     *
     * @param category the app category to add
     */
    public void addCategory(I_CmsAppCategory category) {

        m_appCategoryList.add(category);
    }

    /**
     * Builds the tree of categories and apps.<p>
     *
     * This tree will only include those categories which are reachable by following the parent chain of
     * an available app configuration up to the root category (null).
     *
     * @return the root node of the tree
     */
    public CmsAppCategoryNode buildHierarchy() {

        // STEP 0: Initialize everything and sort categories by priority

        Collections.sort(m_appCategoryList, new Comparator<I_CmsAppCategory>() {

            public int compare(I_CmsAppCategory cat1, I_CmsAppCategory cat2) {

                return ComparisonChain.start().compare(cat1.getPriority(), cat2.getPriority()).result();
            }
        });
        m_rootNode = new CmsAppCategoryNode();
        m_nodes.clear();
        m_nodes.put(null, m_rootNode);

        // STEP 1: Create a node for each category

        for (I_CmsAppCategory category : m_appCategoryList) {
            m_nodes.put(category.getId(), new CmsAppCategoryNode(category));
        }

        // STEP 2: Assign category nodes to nodes for their parent category

        for (CmsAppCategoryNode node : m_nodes.values()) {
            if (node != m_rootNode) {
                addNodeToItsParent(node);
            }
        }

        // STEP 3: Assign app configs to category nodes

        for (I_CmsWorkplaceAppConfiguration appConfig : m_appConfigs) {
            addAppConfigToCategory(appConfig);
        }

        // STEP 4: Validate whether there are unused categories / apps

        Set<String> usedNodes = findReachableNodes(m_rootNode, new HashSet<String>());
        if (usedNodes.size() < m_nodes.size()) {
            LOG.warn("Unused app categories: " + Sets.difference(m_nodes.keySet(), usedNodes));
        }
        Set<String> unusedApps = Sets.newHashSet();
        for (I_CmsWorkplaceAppConfiguration appConfig : m_appConfigs) {
            if (!usedNodes.contains(appConfig.getAppCategory())) {
                unusedApps.add(appConfig.getId());
            }
        }
        if (unusedApps.size() > 0) {
            LOG.warn("Unused apps: " + unusedApps);
        }

        // STEP 5: Remove parts of the hierarchy which don't contain any apps
        m_rootNode.removeApplessSubtrees();

        // STEP 6: Sort all categories and app configurations for each node
        m_rootNode.sortRecursively();

        return m_rootNode;
    }

    /**
     * Gets the root node.<p>
     *
     * @return the root node
     */
    public CmsAppCategoryNode getRootNode() {

        return m_rootNode;
    }

    /**
     * Adds an app configuration to the node belonging to its parent category id.<p>
     *
     * @param appConfig the app configuration to add to its parent node
     */
    protected void addAppConfigToCategory(I_CmsWorkplaceAppConfiguration appConfig) {

        CmsAppCategoryNode node = m_nodes.get(appConfig.getAppCategory());
        if (node == null) {
            LOG.info(
                "Missing parent ["
                    + appConfig.getAppCategory()
                    + "] for "
                    + appConfig.getId()
                    + " / "
                    + appConfig.getClass().getName());
        } else {
            node.addAppConfiguration(appConfig);
        }
    }

    /**
     * Adds a category node to the category node belonging to its parent id.<p>
     *
     * @param node the node which should be attached to its parent
     */
    protected void addNodeToItsParent(CmsAppCategoryNode node) {

        String parentId = node.getCategory().getParentId();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(parentId)) {
            parentId = null;
        }
        CmsAppCategoryNode parentNode = m_nodes.get(parentId);
        if (parentNode == null) {
            LOG.error(
                "Missing parent [" + node.getCategory().getParentId() + "] for [" + node.getCategory().getId() + "]");
        } else {
            parentNode.addChild(node);
        }
    }

    /**
     * Finds the category nodes reachable from a node.<p>
     *
     * @param rootNode the root node
     * @param reachableNodes set used for collecting the reachable nodes
     *
     * @return the set of reachable node ids
     */
    private Set<String> findReachableNodes(CmsAppCategoryNode rootNode, HashSet<String> reachableNodes) {

        reachableNodes.add(rootNode.getCategory().getId());
        for (CmsAppCategoryNode child : rootNode.getChildren()) {
            findReachableNodes(child, reachableNodes);
        }
        return reachableNodes;
    }

}
