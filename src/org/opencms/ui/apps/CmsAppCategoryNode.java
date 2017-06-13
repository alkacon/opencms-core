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

import org.opencms.ui.apps.CmsWorkplaceAppManager.ConfigurationComparator;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Tree node representing an app category.<p>
 *
 * Contains a list of child nodes and a list of apps, which are the leaves of the tree.
 * Note that the list of children is not initialized after construction, it has to be filled manually.
 */
public class CmsAppCategoryNode implements I_CmsHasOrder {

    /** The category data. */
    private I_CmsAppCategory m_data;

    /** The child nodes. */
    private List<CmsAppCategoryNode> m_children = Lists.newArrayList();

    /** The app configurations in the category belonging to this node. */
    private List<I_CmsWorkplaceAppConfiguration> m_appConfigurations = Lists.newArrayList();

    /** Used to count the apps in the subtree starting with this node. */
    private int m_appCount;

    /**
     * Creates a new category node for the given category.
     *
     * @param appCategory the category data
     */
    public CmsAppCategoryNode(I_CmsAppCategory appCategory) {

        m_data = appCategory;
    }

    /**
     * Creates a new root node.<p>
     */
    CmsAppCategoryNode() {

        m_data = new CmsAppCategory(null, null, 0, 0);
    }

    /**
     * Adds an app configuration.<p>
     *
     * @param appConfig the app configuration to add
     */
    public void addAppConfiguration(I_CmsWorkplaceAppConfiguration appConfig) {

        m_appConfigurations.add(appConfig);
    }

    /**
     * Adds a child node.<p>
     *
     * @param node the child node
     */
    public void addChild(CmsAppCategoryNode node) {

        m_children.add(node);

    }

    /**
     * Gets the app configurations for this category.<p>
     *
     * @return the app configurations
     */
    public List<I_CmsWorkplaceAppConfiguration> getAppConfigurations() {

        return m_appConfigurations;
    }

    /**
     * Gets the app category data for this node.<p>
     *
     * @return the app category data
     */
    public I_CmsAppCategory getCategory() {

        return m_data;
    }

    /**
     * Gets the child nodes of this node.<p>
     *
     * @return the child nodes
     */
    public List<CmsAppCategoryNode> getChildren() {

        return m_children;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsHasOrder#getOrder()
     */
    public int getOrder() {

        return getCategory().getOrder();
    }

    /**
     * Recursively removes subtrees containing no app configurations.<p>
     */
    public void removeApplessSubtrees() {

        internalRemoveApplessSubtrees(true);
    }

    /**
     * Sorts the child nodes *and* the apps of this node by their order.<p>
     */
    public void sort() {

        Collections.sort(m_appConfigurations, new ConfigurationComparator<I_CmsWorkplaceAppConfiguration>());
        Collections.sort(m_children, new ConfigurationComparator<CmsAppCategoryNode>());
    }

    /**
     * Recursively calls sort on all descendants of this node.<p>
     */
    public void sortRecursively() {

        sort();
        for (CmsAppCategoryNode child : m_children) {
            child.sortRecursively();
        }
    }

    /**
     * Recursively computes the app count for each descendant of this tree node.<p>
     */
    private void computeAppCount() {

        m_appCount = m_appConfigurations.size();
        for (CmsAppCategoryNode child : m_children) {
            child.computeAppCount();
            m_appCount += child.m_appCount;
        }
    }

    /**
     * Internal helper method used to remove subtrees containing no app configurations.<p>
     *
     * @param isRoot should be true if this is a root node
     */
    private void internalRemoveApplessSubtrees(boolean isRoot) {

        if (isRoot) {
            computeAppCount();
        }
        Iterator<CmsAppCategoryNode> iter = m_children.iterator();
        while (iter.hasNext()) {
            CmsAppCategoryNode child = iter.next();
            if (child.m_appCount == 0) {
                iter.remove();
            } else {
                child.internalRemoveApplessSubtrees(false);
            }
        }
    }

}
