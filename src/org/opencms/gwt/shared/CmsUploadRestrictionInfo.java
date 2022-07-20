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

package org.opencms.gwt.shared;

import org.opencms.file.CmsResource;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

        /** 'Enabled' status. */
        private Map<String, Boolean> m_tempUploadEnabledMap = new HashMap<>();

        /** 'Types' status. */
        private Map<String, Set<String>> uploadTypesMap = new HashMap<>();

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

            Map<String, String> parsedInfo = CmsStringUtil.splitAsMap(info, "|", ":");
            path = normalizePath(path);
            String enabledStr = parsedInfo.get(KEY_ENABLED);
            if (enabledStr != null) {
                m_tempUploadEnabledMap.put(path, Boolean.valueOf(enabledStr));
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
                uploadTypesMap.put(path, types);
            }
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
            result.m_uploadEnabledMap = m_tempUploadEnabledMap;
            result.m_uploadTypesMap = uploadTypesMap;
            return result;
        }

    }

    /** The 'enabled' key. */
    public static final String KEY_ENABLED = "enabled";

    /** The 'types' key. */
    public static final String KEY_TYPES = "types";

    /** The default upload restriction that allows everything. */
    public static final String UNRESTRICTED_UPLOADS = "enabled:true|types:*";

    /** Map to keep track of enabled/disabled status. */
    private Map<String, Boolean> m_uploadEnabledMap = new HashMap<>();

    /** Map to keep track of allowed file extensions. */
    private Map<String, Set<String>> m_uploadTypesMap = new HashMap<>();

    /**
     * Creates a new instance.
     */
    protected CmsUploadRestrictionInfo() {}

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
        return Joiner.on(",").join(suffixes);
    }

    /**
     * Checks if the upload should be enabled for the given upload path.
     *
     * @param originalPath the upload root path
     * @return true if the upload is enabled
     */
    public boolean isUploadEnabled(String originalPath) {

        String path = originalPath;

        while (path != null) {
            path = normalizePath(path);
            Boolean value = m_uploadEnabledMap.get(path);
            if (value != null) {
                return value.booleanValue();
            }
            path = CmsResource.getParentFolder(path);
        }
        return true;
    }

    /**
     * Gets the valid extensions for uploads to a given upload folder
     *
     * @param path the upload folder root path
     * @return the valid extensions
     */
    protected Set<String> getTypes(String path) {

        while (path != null) {
            path = normalizePath(path);
            Set<String> value = m_uploadTypesMap.get(path);
            if (value != null) {
                return value;

            }
            path = CmsResource.getParentFolder(path);
        }
        return Collections.emptySet();
    }

}
