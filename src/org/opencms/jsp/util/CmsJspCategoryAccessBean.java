/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

/** Bean for easy access to categories in JSPs. */
public class CmsJspCategoryAccessBean {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspCategoryAccessBean.class);

    /** CmsObject - basically needed for reading categories and the leaf check. */
    CmsObject m_cms;
    /** The wrapped list of categories. */
    List<CmsCategory> m_categories;
    /** The path of the main category. All categories of {@link #m_categories} are sub-categories of the main category. */
    String m_mainCategoryPath;
    /** Map from the path of a main category to wrappers that hold only the sub-categories of that main category. */
    Map<String, CmsJspCategoryAccessBean> m_subCategories;

    /**
     * Default constructor.
     *
     * @param cms the current {@link CmsObject}.
     * @param resource the resource for which the categories should be read.
     */
    public CmsJspCategoryAccessBean(CmsObject cms, CmsResource resource) {
        this(cms, getCategories(cms, resource), "");
    }

    /**
     * Constructor for wrapping a category list.<p>
     * NOTE: The list must always be a tree starting at the root, i.e., for each category also it's parents must be included.
     *
     * @param cms the current {@link CmsObject}.
     * @param categories the the categories that should be wrapped.
     */
    public CmsJspCategoryAccessBean(CmsObject cms, List<CmsCategory> categories) {
        this(cms, categories, "");
    }

    /**
     * Internal constructor for creating wrappers with a subset of the categories.
     *
     * @param cms the current {@link CmsObject}.
     * @param categories the original categories.
     * @param mainCategoryPath path of the main category for which only sub-categories should be wrapped.
     */
    private CmsJspCategoryAccessBean(CmsObject cms, List<CmsCategory> categories, String mainCategoryPath) {
        m_cms = cms;
        m_mainCategoryPath = mainCategoryPath.isEmpty() || mainCategoryPath.endsWith("/")
        ? mainCategoryPath
        : mainCategoryPath + "/";

        if (m_mainCategoryPath.isEmpty()) {
            m_categories = categories;
        } else {
            List<CmsCategory> filteredCategories = new ArrayList<CmsCategory>();
            for (CmsCategory category : categories) {
                if (category.getPath().startsWith(m_mainCategoryPath)
                    && !category.getPath().equals(m_mainCategoryPath)) {
                    filteredCategories.add(category);
                }
            }
            m_categories = filteredCategories;
        }

    }

    /**
     * Reads the categories for the given resource.
     *
     * @param cms the {@link CmsObject} used for reading the categories.
     * @param resource the resource for which the categories should be read.
     * @return the categories assigned to the given resource.
     */
    private static List<CmsCategory> getCategories(CmsObject cms, CmsResource resource) {

        if ((null != resource) && (null != cms)) {
            try {
                return CmsCategoryService.getInstance().readResourceCategories(cms, resource);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return new ArrayList<CmsCategory>(0);
    }

    /**
     * Returns all wrapped categories.
     *
     * @return all wrapped categories.
     */
    public List<CmsCategory> getAllItems() {

        return m_categories;
    }

    /**
     * Returns <code>true</code> if there is no category wrapped, otherwise <code>false</code>.
     *
     * @return <code>true</code> if there is no category wrapped, otherwise <code>false</code>.
     */
    public boolean getIsEmpty() {

        return m_categories.isEmpty();
    }

    /**
     * Returns only the leaf categories of the wrapped categories.
     *
     * @return only the leaf categories of the wrapped categories.
     */
    public List<CmsCategory> getLeafItems() {

        List<CmsCategory> result = new ArrayList<CmsCategory>();
        for (CmsCategory category : m_categories) {
            try {
                if (isLeaf(category)) {
                    result.add(category);
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Returns a map from a category path to the wrapper of all the sub-categories of the category with the path given as key.
     *
     * @return a map from a category path to all sub-categories of the path's category.
     */
    public Map<String, CmsJspCategoryAccessBean> getSubCategories() {

        if (m_subCategories == null) {
            m_subCategories = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @SuppressWarnings("synthetic-access")
                public Object transform(Object pathPrefix) {

                    return new CmsJspCategoryAccessBean(m_cms, m_categories, (String)pathPrefix);
                }

            });
        }
        return m_subCategories;
    }

    /**
     * Returns all categories that are direct children of the current main category.
     *
     * @return all categories that are direct children of the current main category.
     */
    public List<CmsCategory> getTopItems() {

        List<CmsCategory> categories = new ArrayList<CmsCategory>();
        String matcher = Pattern.quote(m_mainCategoryPath) + "[^/]*/";
        for (CmsCategory category : m_categories) {
            if (category.getPath().matches(matcher)) {
                categories.add(category);
            }
        }
        return categories;
    }

    /**
     * Checks if a category is a leaf category (according to the current context).
     *
     * @param category the category to check.
     * @return <code>true</code> if the category is a leaf, <code>false</code> otherwise.
     * @throws CmsException thrown if the leaf check fails.
     */
    private boolean isLeaf(CmsCategory category) throws CmsException {

        return CmsCategoryService.getInstance().readCategories(
            m_cms,
            category.getPath(),
            false,
            m_cms.getRequestContext().getUri()).isEmpty();
    }
}
