/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.shared;

import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains information about which folders should restrict uploads.
 */
public class CmsUploadRestrictionInfo implements IsSerializable {

    /**
     * Helper class for building a new CmsUploadRestrictionInfo object.
     */
    public static class Builder {

        /** The tree node corresponding to the root directory. */
        private Node m_root = new Node();

        /** Map used to 'uniquify' equivalent node data objects. */
        private Map<NodeData, NodeData> m_nodeDataCache = new HashMap<>();

        /**
         * Adds a new entry with the given options for the given path.
         *
         * @param path the path
         * @param enabled upload enabled (TRUE / FALSE / null)
         * @param extensions (set of file extensions or null)
         *
         * @return the builder instance
         */
        public Builder add(String path, Boolean enabled, Set<String> extensions) {

            Node node = findOrCreateNode(m_root, path);
            NodeData data = new NodeData();
            data.setEnabled(enabled);
            data.setExtensions(extensions);
            data = m_nodeDataCache.computeIfAbsent(data, dataParam -> dataParam);
            node.setData(data);
            return this;
        }

        /**
         * Adds a new entry.
         *
         * <p>The info string has the format 'key1:value1|key2:value2|....'. Currently the keys 'types' and 'enabled' are supported;
         * 'enabled' (format: 'enabled:true') enables/disables uploads, and 'types' (format: 'types:jpg,png' sets the allowed file extensions.
         *
         * @param path the path
         * @param info the upload info entry
         * @return the builder instance
         */
        public Builder add(String path, String info) {

            Node node = findOrCreateNode(m_root, path);
            NodeData data = new NodeData();
            data.parse(info);
            data = m_nodeDataCache.computeIfAbsent(data, dataParam -> dataParam);
            node.setData(data);
            return this;

        }

        /**
         * Creates a new upload restriction info object.
         *
         * @return the new object
         */
        @SuppressWarnings("synthetic-access")
        public CmsUploadRestrictionInfo build() {

            CmsUploadRestrictionInfo result = new CmsUploadRestrictionInfo();
            result.m_root = m_root;
            return result;
        }

    }

    /**
     * Tree node that stores the settings for a single folder.
     */
    public static class Node implements IsSerializable {

        /** Map of child nodes by name. */
        private Map<String, Node> m_children = new HashMap<>();

        /** The stored node data (may be null). */
        private NodeData m_data;

        /**
         * Creates a new instance.
         */
        public Node() {}

        /**
         * Gets the children.
         *
         * @return the children by name
         */
        public Map<String, Node> getChildren() {

            return m_children;
        }

        /**
         * Gets the node data
         *
         * @return the node data
         */
        public NodeData getData() {

            return m_data;
        }

        /**
         * Sets the node data
         *
         * @param data the node data
         */
        public void setData(NodeData data) {

            m_data = data;
        }
    }

    /**
     * The data for a single node.
     *
     * <p>Contains information on which file extensions are uploadable and whether uploads are enabled at all.
     */
    public static class NodeData implements IsSerializable {

        /** True if upload enabled (may be null). */
        private Boolean m_enabled;

        /** Set of allowed extensions (without leading '.'). May be null. */
        private Set<String> m_extensions;

        /** Creates a new instance. */
        public NodeData() {}

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object other) {

            if (!(other instanceof NodeData)) {
                return false;
            }
            NodeData data = (NodeData)other;
            return Objects.equals(m_enabled, data.getEnabled()) && Objects.equals(m_extensions, data.getExtensions());

        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return Objects.hash(m_enabled, m_extensions);
        }

        /**
         * Merges this node with a child node, where if the child node has attributes set, they override the corresponding attributes of this node.
         *
         * @param child the child node
         * @return the merged node
         */
        public NodeData merge(NodeData child) {

            if (child == null) {
                return this;
            }
            if ((child.getExtensions() != null) && (child.getEnabled() != null)) {
                // everything is overwritten, so we don't need a merged version
                return child;
            }
            NodeData result = new NodeData();
            if (child.getExtensions() != null) {
                result.setExtensions(child.getExtensions());
            } else {
                result.setExtensions(getExtensions());
            }
            if (child.getEnabled() != null) {
                result.setEnabled(child.getEnabled());
            } else {
                result.setEnabled(getEnabled());
            }
            return result;
        }

        public void parse(String info) {

            Map<String, String> parsedInfo = CmsStringUtil.splitAsMap(info, "|", ":");
            String enabledStr = parsedInfo.get(KEY_ENABLED);
            if (enabledStr != null) {
                setEnabled(Boolean.valueOf(enabledStr));
            }
            String typesStr = parsedInfo.get(KEY_TYPES);
            if (typesStr != null) {
                Set<String> types = new HashSet<>();
                for (String type : typesStr.split(",")) {
                    type = type.trim().toLowerCase();
                    if (type.startsWith(".")) {
                        type = type.substring(1);
                    }
                    if (type.length() > 0) {
                        types.add(type);
                    }
                }
                setExtensions(types);
            }
        }

        /**
         * Gets the 'upload enabled' status (may be null).
         *
         * @return the 'upload enabled' status
         */
        private Boolean getEnabled() {

            return m_enabled;
        }

        /**
         * Gets the allowed file extensions for uploading (may be null).
         *
         * @return the set of allowed file extensions for uploads
         */
        private Set<String> getExtensions() {

            return m_extensions;
        }

        /**
         * Sets the 'upload enabled' status.
         *
         * @param enabled the 'upload enabled' status
         */
        private void setEnabled(Boolean enabled) {

            m_enabled = enabled;
        }

        /**
         * Sets the allowed file extensions.
         *
         * @param extensions the set of allowed file extensions
         */
        private void setExtensions(Set<String> extensions) {

            m_extensions = extensions;
        }

    }

    /** The 'enabled' key. */
    public static final String KEY_ENABLED = "enabled";

    /** The 'types' key. */
    public static final String KEY_TYPES = "types";

    /** The default upload restriction that allows everything. */
    public static final String UNRESTRICTED_UPLOADS = "enabled:true|types:*";

    protected Node m_root;

    /**
     * Creates a new instance.
     */
    protected CmsUploadRestrictionInfo() {}

    /**
     * Helper method for collecting and merging the node data valid for a particular path.
     *
     * @param root the root node
     * @param path the path along which to collect and merge the node data
     *
     * @return the merged node data
     */
    public static NodeData collectNodeData(Node root, String path) {

        NodeData empty = new NodeData();
        NodeData currentData = empty.merge(root.getData()); // root.getData() may be null, so we merge it with an empty NodeData instance
        Node current = root;
        List<String> pathComponents = Arrays.asList(path.split("/"));
        for (String part : pathComponents) {
            if ("".equals(part)) {
                continue;
            }
            Node child = current.getChildren().get(part);
            if (child != null) {
                currentData = currentData.merge(child.getData());
                current = child;
            } else {
                break;
            }
        }
        return currentData;

    }

    /**
     * Finds or creates the node corresponding to a given path from a root node (also creating any required intermediate nodes).
     *
     * @param root the root node of the tree
     * @param path the path
     * @return the node for the given path
     */
    public static Node findOrCreateNode(Node root, String path) {

        Node current = root;
        if ("/".equals(path) || "".equals(path)) {
            // empty list of path components is OK
        } else {
            List<String> pathComponents = Arrays.asList(path.split("/"));
            for (String part : pathComponents) {
                if ("".equals(part)) {
                    // trailing or duplicate slashes
                    continue;
                }
                Node child = current.getChildren().computeIfAbsent(part, k -> new Node());
                current = child;
            }
        }
        return current;
    }

    /**
     * Normalizes a path.
     *
     * @param path the path
     * @return the normalized path
     */
    static String normalizePath(String path) {

        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return path;
    }

    /**
     * Check if a given file extension is allowed for the given upload path.
     *
     * @param path the root path of the upload folder
     * @param extension the file extension to check
     * @return true if the file extension is valid for uploads to the folder
     */
    public boolean checkTypeAllowed(String path, String extension) {

        Set<String> types = getTypes(path);
        if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        extension = extension.toLowerCase();
        boolean result = types.contains(extension) || types.contains("*");
        return result;
    }

    /**
     * Gets the 'accept' attribute to use for the file input element for the given upload folder.
     *
     * @param path the upload folder root path
     * @return the 'accept' attribute that should be used for the file input
     */
    public String getAcceptAttribute(String path) {

        Set<String> types = getTypes(path);
        List<String> suffixes = new ArrayList<>();
        for (String type : types) {
            if ("*".equals(type)) {
                return "";
            } else {
                suffixes.add("." + type);
            }
        }
        String result = Joiner.on(",").join(suffixes);
        return result;
    }

    /**
     * Checks if the upload should be enabled for the given upload path.
     *
     * @param originalPath the upload root path
     * @return true if the upload is enabled
     */
    public boolean isUploadEnabled(String originalPath) {

        NodeData data = collectNodeData(m_root, originalPath);
        return (data.getEnabled() == null) || data.getEnabled().booleanValue();
    }

    /**
     * Gets the valid extensions for uploads to a given upload folder
     *
     * @param path the upload folder root path
     * @return the valid extensions
     */
    protected Set<String> getTypes(String path) {

        NodeData data = collectNodeData(m_root, path);
        if (data.getExtensions() == null) {
            return Collections.emptySet();
        } else {
            return data.getExtensions();
        }
    }

}
