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

/**
 * Utility class for the resource icon CSS.<p>
 *
 * @since 8.0.0
 */
public class CmsIconUtil {

    /** The suffix for the CSS classes for small icons. */
    public static final String SMALL_SUFFIX = "_small";

    /** The resource icon CSS class prefix. */
    public static final String TYPE_ICON_CLASS = "cms_type_icon";

    /** Type for resource not found. */
    public static final String TYPE_RESOURCE_NOT_FOUND = "cms_resource_not_found";

    /**
     * Constructor.<p>
     */
    protected CmsIconUtil() {

        // empty
    }

    /**
     * Returns the CSS classes of the resource icon for the given resource type name.<p>
     *
     * Use this function, if the resource type is known, but not the filename. If the filename is available use {@link CmsIconUtil#getResourceIconClasses(String, String, boolean)}<p>
     *
     * @param resourceTypeName the resource type name
     * @param small if true, get the icon classes for the small icon, else for the biggest one available
     *
     * @return the CSS classes
     */
    public static String getResourceIconClasses(String resourceTypeName, boolean small) {

        StringBuffer sb = new StringBuffer(TYPE_ICON_CLASS);
        sb.append(" ").append(getResourceTypeIconClass(resourceTypeName, small));
        return sb.toString();
    }

    /**
     * Returns the CSS classes of the resource icon for the given resource type and filename.<p>
     *
     * Use this the resource type and filename is known. Otherwise use {@link CmsIconUtil#getResourceIconClasses(String,boolean)}<p>
     *
     * @param resourceTypeName the resource type name
     * @param fileName the filename
     * @param small if true, get the icon classes for the small icon, else for the biggest one available
     *
     * @return the CSS classes
     */
    public static String getResourceIconClasses(String resourceTypeName, String fileName, boolean small) {

        StringBuffer sb = new StringBuffer(TYPE_ICON_CLASS);
        sb.append(" ").append(getResourceTypeIconClass(resourceTypeName, small)).append(" ").append(
            getFileTypeIconClass(resourceTypeName, fileName, small));
        return sb.toString();
    }

    /**
     * Returns the CSS class for a given resource type name and file name extension.<p>
     *
     * @param resourceTypeName the resource type name
     * @param suffix the file name extension
     * @param small if true, get the icon class for the small icon, else for the biggest one available
     *
     * @return the CSS class for the type and extension
     */
    public static String getResourceSubTypeIconClass(String resourceTypeName, String suffix, boolean small) {

        StringBuffer buffer = new StringBuffer(TYPE_ICON_CLASS).append("_").append(resourceTypeName.hashCode()).append(
            "_").append(suffix);
        if (small) {
            buffer.append(SMALL_SUFFIX);
        }
        return buffer.toString();
    }

    /**
     * Returns the CSS class for the given resource type.<p>
     *
     * @param resourceTypeName the resource type name
     * @param small if true, get the icon class for the small icon, else for the biggest one available
     *
     * @return the CSS class
     */
    public static String getResourceTypeIconClass(String resourceTypeName, boolean small) {

        StringBuffer sb = new StringBuffer(TYPE_ICON_CLASS);
        sb.append("_").append(resourceTypeName.hashCode());
        if (small) {
            sb.append(SMALL_SUFFIX);
        }
        return sb.toString();
    }

    /**
     * Returns the CSS class for the given filename.<p>
     *
     * @param resourceTypeName the resource type name
     * @param fileName the filename
     * @param small if true, get the CSS class for the small icon, else for the biggest one available
     *
     * @return the CSS class
     */
    protected static String getFileTypeIconClass(String resourceTypeName, String fileName, boolean small) {

        if ((fileName != null) && fileName.contains(".")) {
            int last = fileName.lastIndexOf(".");
            if (fileName.length() > (last + 1)) {
                String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                return getResourceSubTypeIconClass(resourceTypeName, suffix, small);
            }
        }
        return "";

    }
}
