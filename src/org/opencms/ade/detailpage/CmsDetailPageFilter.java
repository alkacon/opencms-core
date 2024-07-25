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

package org.opencms.ade.detailpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.util.CmsPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;

/**
 * Filters and sorts a list of detail pages based on whether they are suitable detail pages for a fixed detail content.
 *
 * <p>Note: Filtering on detail page types is already handled elsewhere, so this class only handles detail pages for a single type.
 */
public class CmsDetailPageFilter {

    /** Prefix for the category qualifier in a detail page entry. */
    public static final String PREFIX_CATEGORY = "category:";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDetailPageFilter.class);

    /** The CMS context. */
    private CmsObject m_cms;

    /** The detail resource (either this or m_path is null). */
    private CmsResource m_resource;

    /** The categories of the detail resource (lazily initialized). */
    private Set<CmsPath> m_categories;

    /** The detail root path (either this or m_resource is null). */
    private String m_path;

    /**
     * Creates a new instance based on an actual detail resource.
     *
     * @param cms the CMS context
     * @param resource the detail resource
     */
    public CmsDetailPageFilter(CmsObject cms, CmsResource resource) {

        try {
            m_cms = OpenCms.initCmsObject(cms);
            m_cms.getRequestContext().setSiteRoot("");
            m_resource = resource;
        } catch (CmsException e) {
            // shouldn't happen - initCmsObject doesn't *actually* throw exceptions
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /**
     * Creates a new instance based on the root path of a detail resource.
     *
     * @param cms the CMS context
     * @param rootPath the detail resource root path
     */
    public CmsDetailPageFilter(CmsObject cms, String rootPath) {

        try {
            m_cms = OpenCms.initCmsObject(cms);
            m_cms.getRequestContext().setSiteRoot("");
            m_path = rootPath;
        } catch (CmsException e) {
            // shouldn't happen
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /**
     *
     * Produces a filtered and sorted result stream of candidate detail pages for the detail resource.
     *
     * @param infos the base list of detail page beans to filter/sort
     * @return the result stream
     */
    public Stream<CmsDetailPageInfo> filterDetailPages(List<CmsDetailPageInfo> infos) {

        List<CmsDetailPageInfo> infos2 = new ArrayList<>(infos);

        // the sort method uses a stable sort, so detail page order will be preserved for those entries with the same sort key
        Collections.sort(infos2, new Comparator<CmsDetailPageInfo>() {

            public int compare(CmsDetailPageInfo a, CmsDetailPageInfo b) {

                return Integer.compare(getSortKey(a), getSortKey(b));
            }

            private int getSortKey(CmsDetailPageInfo info) {

                // sort order:
                // 0 - qualified non-default detail pages
                // 1 - unqualified non-default detail pages
                // 2 - qualified default detail pages
                // 3 - unqualified default detail pages
                boolean defaultDetailFlag = info.getType().equals(CmsGwtConstants.DEFAULT_DETAILPAGE_TYPE);
                boolean qualifierFlag = info.getQualifier() != null;
                if (!defaultDetailFlag) {
                    if (qualifierFlag) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    if (qualifierFlag) {
                        return 2;
                    } else {
                        return 3;
                    }
                }
            }
        });
        // qualified pages shouldn't work without an unqualified fallback
        if (!infos2.stream().anyMatch(detailPage -> detailPage.getQualifier() == null)) {
            if ((infos2.size() != 0) && LOG.isWarnEnabled()) {
                LOG.warn(
                    "No unqualified detail page entries found in list - probably a configuration error: " + infos2);
            }
            return Collections.<CmsDetailPageInfo> emptyList().stream();
        }
        return infos2.stream().filter(info -> (info.getQualifier() == null) || checkQualifier(info.getQualifier()));
    }

    /**
     * Checks that a detail page qualifier matches the detail resource.
     *
     * @param qualifier the qualifier to check
     * @return true if the qualifier matches the detail resource
     */
    protected boolean checkQualifier(String qualifier) {

        // shouldn't happen, test anyway
        if (qualifier == null) {
            return true;
        }
        qualifier = qualifier.trim();
        if (qualifier.startsWith(PREFIX_CATEGORY)) {
            String categoryStr = qualifier.substring(PREFIX_CATEGORY.length());
            // use CmsPath to normalize leading/trailing slashes
            CmsPath categoryPath = new CmsPath(categoryStr);
            return getCategories().contains(categoryPath);
        }
        LOG.error("Invalid detail page qualifier: " + qualifier);
        return false;
    }

    /**
     * Gets the category paths, lazily initializing them first if necessary.
     *
     * @return the set of category paths
     */
    protected Set<CmsPath> getCategories() {

        if (m_categories == null) {
            m_categories = readCategories();
        }
        return m_categories;
    }

    /**
     * Reads the categories for the resource.
     *
     * @return the categories for the resource
     */
    protected Set<CmsPath> readCategories() {

        try {
            List<CmsCategory> categories;

            if (m_resource != null) {
                categories = CmsCategoryService.getInstance().readResourceCategories(m_cms, m_resource);
            } else {
                CmsResource resource = m_cms.readResource(m_path, CmsResourceFilter.IGNORE_EXPIRATION);
                categories = CmsCategoryService.getInstance().readResourceCategories(m_cms, resource);
            }
            Set<CmsPath> result = new HashSet<>();
            for (CmsCategory category : categories) {
                result.add(new CmsPath(category.getPath()));
            }
            return result;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            // empty result is cached, so we do not retry
            return Collections.emptySet();
        }

    }

}
