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

package org.opencms.ade.galleries;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavBuilder.Visibility;
import org.opencms.jsp.CmsJspNavElement;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Helper class for building a filtered sitemap tree for the gallery dialog's 'Sitemap' tab.<p>
 *
 * Objects of this class are single-use, which means they should only be used for a single navigation tree filtering operation.
 */
public class CmsGalleryFilteredNavTreeBuilder {

    /**
     * A tree node representing a navigation entry.<p>
     */
    public class NavigationNode {

        /** The child nodes. */
        private List<NavigationNode> m_children = Lists.newArrayList();

        /** True if this is a leaf node in the original, unfiltered navigation tree. */
        private boolean m_isLeaf;

        /** True if this node has matched a filter string. */
        private boolean m_isMatch;

        /** True if this node should not be thrown away. */
        private boolean m_keep;

        /** The navigation element for this node. */
        private CmsJspNavElement m_navElement;

        /** The parent node. */
        private NavigationNode m_parent;

        /**
         * Creates a new node.<p>
         *
         * @param navElement the navigation element
         */
        NavigationNode(CmsJspNavElement navElement) {
            m_navElement = navElement;
        }

        /**
         * Gets the children of this node.<p>
         *
         * @return the list of child nodes
         */
        public List<NavigationNode> getChildren() {

            return m_children;
        }

        /**
         * Gets the navigation element for this node.<p>
         *
         * @return the navigation element
         */
        public CmsJspNavElement getNavElement() {

            return m_navElement;
        }

        /**
         * Returns true if this is a leaf in the original unfiltered navigation tree.<p>
         *
         * @return true if this is a leaf
         */
        public boolean isLeaf() {

            return m_isLeaf;
        }

        /**
         * Returns true if this node has previously matched the filter string.<P>
         *
         * @return true if this has matched the filter string
         */
        public boolean isMatch() {

            return m_isMatch;
        }

        /**
         * Removes the node from its parent.<p>
         */
        public void removeFromParent() {

            if (m_parent != null) {
                m_parent.m_children.remove(this);
                m_parent = null;
            }
        }

        /**
         * Marks this node as a leaf.<p>
         *
         * @param isLeaf true if this should be marked as a leaf
         */
        public void setIsLeaf(boolean isLeaf) {

            m_isLeaf = isLeaf;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return m_navElement == null ? super.toString() : m_navElement.getResource().getRootPath();
        }

        /**
         * Collects all tree leaves.<p>
         *
         * @param results the list used to collect the leaves
         */
        void collectLeaves(List<NavigationNode> results) {

            if (m_children.isEmpty()) {
                results.add(this);
            } else {
                for (NavigationNode child : m_children) {
                    child.collectLeaves(results);

                }
            }
        }

        /**
         * Creates children for this node by loading child navigation entries for this node's navigation entry.<p<
         */
        @SuppressWarnings("synthetic-access")
        void expand() {

            for (CmsJspNavElement navElement : m_navBuilder.getNavigationForFolder(
                m_navElement.getResource().getRootPath(),
                Visibility.all,
                CmsResourceFilter.ONLY_VISIBLE)) {
                if ((navElement != null) && navElement.isInNavigation()) {
                    NavigationNode child = new NavigationNode(navElement);
                    addChild(child);
                }
            }

        }

        /**
         * Recursively loads children for all nodes.<p>
         */
        void expandAll() {

            expand();
            for (NavigationNode child : m_children) {
                child.expandAll();
            }
        }

        /**
         * Gets the leaves of this tree.<p<
         *
         * @return the leaves of the tree
         */
        List<NavigationNode> getLeaves() {

            List<NavigationNode> result = Lists.newArrayList();
            collectLeaves(result);
            return result;
        }

        /**
         * Gets the parent node.<p>
         *
         * @return the parent node
         */
        NavigationNode getParent() {

            return m_parent;
        }

        /**
         * Marks the node to not be removed during the pruning step.<p>
         */
        void markAsKeep() {

            m_keep = true;
        }

        /**
         * Matches all nodes of the tree against the filter.<p>
         *
         * @param filter the filter string
         */
        @SuppressWarnings("synthetic-access")
        void matchAll(String filter) {

            m_isMatch = matches(filter);
            CmsGalleryFilteredNavTreeBuilder.this.m_hasMatches |= m_isMatch;
            for (NavigationNode child : m_children) {
                child.matchAll(filter);
            }

        }

        /**
         * Checks if the node matches the given filter string.<p>
         *
         * @param filter a filter string
         *
         * @return true if the node matches the filter string
         */
        boolean matches(String filter) {

            for (String matchText : new String[] {
                m_navElement.getNavText(),
                m_navElement.getTitle(),
                m_navElement.getDescription()}) {
                if (matchText != null) {
                    return matchText.toLowerCase().contains(filter.toLowerCase());
                }
            }
            return true;
        }

        /**
         * Prunes nodes which should not be kept after filtering.<p>
         */
        void removeUnmarkedNodes() {

            if (!m_keep) {
                removeFromParent();
            } else {
                for (NavigationNode child : Lists.newArrayList(m_children)) {
                    child.removeUnmarkedNodes();
                }
            }
        }

        /**
         * Adds a child node.<p>
         *
         * @param child the child node
         */
        private void addChild(NavigationNode child) {

            if (child.m_parent != null) {
                throw new IllegalArgumentException();
            }
            m_children.add(child);
            child.m_parent = this;
        }
    }

    /** True if any matches have been found. */
    private boolean m_hasMatches;

    /** The navigation builder used to process the navigation entries. */
    private CmsJspNavBuilder m_navBuilder;

    /** The root node. */
    private NavigationNode m_root;

    /**
     * Creates a new navigation tree.<p>
     *
     * @param cms the CMS context
     * @param rootPath the root path
     */
    public CmsGalleryFilteredNavTreeBuilder(CmsObject cms, String rootPath) {
        CmsJspNavBuilder navBuilder = new CmsJspNavBuilder(cms);
        m_navBuilder = navBuilder;
        CmsJspNavElement rootNav = navBuilder.getNavigationForResource(
            rootPath,
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        m_root = new NavigationNode(rootNav);
    }

    /**
     * Gets the root node.<p>
     *
     * @return the root node
     */
    public NavigationNode getRoot() {

        return m_root;
    }

    /**
     * Returns true if any matching tree nodes have been found.<p>
     *
     * @return tree if any matching tree nodes have been found
     */
    public boolean hasMatches() {

        return m_hasMatches;
    }

    /**
     * Constructs the filtered navigation tree.<p>
     *
     * @param filter the filter string
     */
    public void initTree(String filter) {

        m_root.expandAll();
        m_root.matchAll(filter);
        List<NavigationNode> leaves = m_root.getLeaves();
        m_root.markAsKeep();
        for (NavigationNode leaf : leaves) {
            leaf.setIsLeaf(true);
            NavigationNode current = leaf;
            while ((current != null) && (current != m_root) && !current.isMatch()) {
                current = current.getParent();
            }
            while (current != null) {
                current.markAsKeep();
                current = current.getParent();
            }

        }
        m_root.removeUnmarkedNodes();
    }
}
