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

import org.opencms.gwt.shared.sort.I_CmsHasPath;
import org.opencms.gwt.shared.sort.I_CmsHasTitle;
import org.opencms.relations.CmsCategory;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A specific bean holding all info to be displayed in the categories tab.<p>
 *
 * @since 8.0.0
 */
public class CmsCategoryBean implements I_CmsHasTitle, I_CmsHasPath, IsSerializable {

    /** The category's base path. */
    private String m_basePath;

    /** The category description. */
    private String m_description;

    /** The category id. */
    private CmsUUID m_id;

    /** The category path. */
    private String m_path;

    /** The category's root path. */
    private String m_rootPath;

    /** The category's site path. */
    private String m_sitePath;

    /** The category title. */
    private String m_title;

    /**
     * Constructor.<p>
     *
     * @param category the server-side category
     */
    public CmsCategoryBean(CmsCategory category) {

        this(
            category.getId(),
            category.getTitle(),
            category.getDescription(),
            category.getPath(),
            category.getBasePath(),
            category.getRootPath());
    }

    /**
     * Constructor.<p>
     *
     * @param categoryTreeEntry the category tree entry to copy
     */
    public CmsCategoryBean(CmsCategoryTreeEntry categoryTreeEntry) {

        this(
            categoryTreeEntry.getId(),
            categoryTreeEntry.getTitle(),
            categoryTreeEntry.getDescription(),
            categoryTreeEntry.getPath(),
            categoryTreeEntry.getBasePath(),
            categoryTreeEntry.getRootPath());
        m_sitePath = categoryTreeEntry.getSitePath();
    }

    /**
     * The constructor.<p>
     *
     * @param id the category id
     * @param title the title to set
     * @param description the subtitle to set
     * @param path the category path
     * @param basePath the category base path
     * @param rootPath the category root path
     */
    public CmsCategoryBean(
        CmsUUID id,
        String title,
        String description,
        String path,
        String basePath,
        String rootPath) {

        m_id = id;
        m_title = title;
        m_description = description;
        m_path = path;
        m_basePath = basePath;
        m_rootPath = rootPath;
    }

    /**
     * Constructor for serialization only.<p>
     */
    protected CmsCategoryBean() {

        // noting to do
    }

    /**
     * Returns the base path.<p>
     *
     * @return the base path
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

        return m_id;
    }

    /**
     * Returns the category path.<p>
     *
     * @return the category path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the root path.<p>
     *
     * @return the root path
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Returns the category site path.<p>
     *
     * @return the category site path
     */
    public String getSitePath() {

        return m_sitePath;
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
     * Returns if the category matches the given filter.<p>
     *
     * @param filter the filter to match
     *
     * @return <code>true</code> if the gallery matches the given filter.<p>
     */
    public boolean matchesFilter(String filter) {

        filter = filter.toLowerCase();
        return m_title.toLowerCase().contains(filter) || m_path.toLowerCase().contains(filter);
    }

    /**
     * Sets the description.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the category path.<p>
     *
     * @param path the category path to set
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * Sets the category site path.<p>
     *
     * @param sitePath category site path
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }
}