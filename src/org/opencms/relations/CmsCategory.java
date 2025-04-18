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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.relations;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.Serializable;

import org.apache.commons.logging.Log;

/**
 * Represents a category, that is just a folder.<p>
 *
 * The category can be centralized under <code>/system/categories/</code>,
 * or decentralized in every folder.<p>
 *
 * For instance, you can have a category folder under <code>/sites/default/</code>
 * so, any file under <code>/sites/default/</code> could be assigned to any
 * category defined under <code>/system/categories/</code> or
 * <code>/sites/default/categories</code>.<p>
 *
 * But a file under <code>/sites/othersite/</code> will only be assignable to
 * categories defined in <code>/system/categories/</code>.<p>
 *
 * @since 6.9.2
 */
public class CmsCategory implements Comparable<CmsCategory>, Serializable {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCategory.class);

    /** The serialization id. */
    private static final long serialVersionUID = -6395887983124249138L;

    /** The category's base path. */
    private String m_basePath;

    /** The description of the category. */
    private String m_description;

    /** Background color to use for the category in the explorer's 'Categories'  column. */
    private String m_background;

    /** The path of the category. */
    private String m_path;

    /** The category's root path. */
    private String m_rootPath;

    /** The structure id of the resource that this category represents. */
    private CmsUUID m_structureId;

    /** The title of the category. */
    private String m_title;

    /**
     * Creates a new category based on another one, keeping everything except title and description, which are passed in separately.<p>
     *
     * @param other the other category to copy fields from
     * @param title the new title
     * @param description the new description
     */
    public CmsCategory(CmsCategory other, String title, String description) {

        m_basePath = other.m_basePath;

        m_path = other.m_path;
        m_rootPath = other.m_rootPath;
        m_structureId = other.m_structureId;
        m_description = description != null ? description : other.m_description;
        m_title = title != null ? title : other.m_title;
        m_background = other.m_background;
    }

    /**
     * Default constructor.<p>
     *
     * @param structureId the structure id of the resource that this category represents
     * @param rootPath the root path of the category folder
     * @param title the title of the category
     * @param description the description of the category
     * @param baseFolder the base categories folder
     * @param background the background color for displaying the category in the explorer file table (in 6 digit hexadecimal form, e.g. #aabbcc), or null
     *
     * @throws CmsException if the root path does not match the given base folder
     */
    public CmsCategory(
        CmsUUID structureId,
        String rootPath,
        String title,
        String description,
        String baseFolder,
        String background)
    throws CmsException {

        m_structureId = structureId;
        m_rootPath = rootPath;
        m_title = title;
        m_description = description;
        m_path = getCategoryPath(m_rootPath, baseFolder);
        m_basePath = m_rootPath.substring(0, m_rootPath.length() - m_path.length());
        m_background = background;
    }

    /**
     * Empty default constructor which is only used for serialization.<p>
     */
    protected CmsCategory() {

        // do nothing
    }

    /**
     * Returns the category path for the given root path.<p>
     *
     * @param rootPath the root path
     * @param baseFolder the categories base folder name
     *
     * @return the category path
     *
     * @throws CmsException if the root path does not match the given base folder
     */
    public static String getCategoryPath(String rootPath, String baseFolder) throws CmsException {

        String base;
        if (rootPath.startsWith(CmsCategoryService.CENTRALIZED_REPOSITORY)) {
            base = CmsCategoryService.CENTRALIZED_REPOSITORY;
        } else {
            base = baseFolder;
            if (!base.endsWith("/")) {
                base += "/";
            }
            if (!base.startsWith("/")) {
                base = "/" + base;
            }
            int pos = rootPath.indexOf(base);
            if (pos < 0) {
                throw new CmsDataAccessException(
                    Messages.get().container(Messages.ERR_CATEGORY_INVALID_LOCATION_1, rootPath));
            }
            base = rootPath.substring(0, pos + base.length());
        }
        return rootPath.substring(base.length());
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsCategory cat) {

        boolean thisGlobal = getBasePath().equals(CmsCategoryService.CENTRALIZED_REPOSITORY);
        boolean thatGlobal = cat.getBasePath().equals(CmsCategoryService.CENTRALIZED_REPOSITORY);
        if ((thisGlobal && thatGlobal) || (!thisGlobal && !thatGlobal)) {
            return getPath().compareTo(cat.getPath());
        }
        return thisGlobal ? -1 : 1;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof CmsCategory)) {
            return false;
        }
        CmsCategory compareCategory = (CmsCategory)obj;
        if (!compareCategory.getPath().equals(getPath())) {
            return false;
        }
        return true;
    }

    /**
     * Gets the configured background color for the category in the 'Categories' explorer column.
     *
     * <p>The color should consist of 6 hex digits preceded by a hash symbol (#aabbcc).
     *
     * @return the background color, or null if it's not set
     */
    public String getBackground() {

        return m_background;
    }

    /**
     * Returns the category's base path.<p>
     *
     * @return the category's base path
     */
    public String getBasePath() {

        return m_basePath;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public CmsUUID getId() {

        return m_structureId;
    }

    /**
     * Returns the mere category name without it's complete path and without the trailing folder - slash.<p>
     *
     * @return the mere category name without it's complete path and without the trailing folder - slash
     */
    public String getName() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_path)) {
            return "";
        }
        String result = CmsResource.getName(m_path);
        // remove trailing slash as categories are not displayed like folders
        if (CmsResource.isFolder(result)) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * Returns the path.<p>
     *
     * @return the path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the category's root path.<p>
     *
     * @return the category's root path
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return getPath().hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "[" + CmsCategory.class.getSimpleName() + "/" + System.identityHashCode(this) + ": " + m_rootPath + " ]";
    }

}