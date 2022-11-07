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

package org.opencms.jsp.search.config.parser.simplesearch;

import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigurationBean.CombinationMode;

import java.util.Collections;
import java.util.List;

/** Wrapper for a combined category and folder restriction. */
public class CmsCategoryFolderRestrictionBean {

    /** The categories to restrict the search to. */
    private List<String> m_categories;

    /** The folders to restrict the search to. */
    private List<String> m_folders;

    /** The category combination mode, i.e., "AND" or "OR". */
    private CombinationMode m_categoryMode;

    /**
     * Constructor for the wrapper.
     * @param categories the categories to filter
     * @param folders the folders to filter
     * @param categoryMode the combination mode for categories
     */
    public CmsCategoryFolderRestrictionBean(List<String> categories, List<String> folders, CombinationMode categoryMode) {

        m_categories = categories == null ? Collections.<String> emptyList() : categories;
        m_folders = folders == null ? Collections.<String> emptyList() : folders;
        m_categoryMode = categoryMode == null ? CombinationMode.OR : categoryMode;
    }

    /**
     * Outputs the restriction as Solr filter query.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        if (m_categories.isEmpty() && m_folders.isEmpty()) {
            return "";
        }
        String result = "(";
        if (!m_categories.isEmpty()) {
            result += "category_exact:(";
            if (m_categories.size() > 1) {
                result += m_categories.stream().reduce(
                    (cat1, cat2) -> "\"" + cat1 + "\" " + m_categoryMode + " \"" + cat2 + "\"").get();
            } else {
                result += "\"" + m_categories.get(0) + "\"";
            }
            result += ")";
        }
        if (!m_folders.isEmpty()) {
            if (!m_categories.isEmpty()) {
                result += " AND ";
            }
            result += "parent-folders:(";
            if (m_folders.size() > 1) {
                result += m_folders.stream().reduce((f1, f2) -> "\"" + f1 + "\" OR \"" + f2 + "\"").get();
            } else {
                result += "\"" + m_folders.get(0) + "\"";
            }
            result += ")";
        }
        result += ")";
        return result;
    }
}