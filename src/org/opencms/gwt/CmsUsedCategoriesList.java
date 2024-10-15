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

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents a list of recently used categories for a given user.
 *
 * <p>You can add categories to this or truncate the list to the n most recently used categories,
 * as well as serialize it to or deserialize it from JSON.
 */
public class CmsUsedCategoriesList {

    /** Additional info key used to store used categories for users. */
    public static final String ADDINFO_USED_CATEGORIES = "USED_CATEGORIES";

    /** Configuration option used to change the maximum number of entries to keep per user. */
    public static final String CONF_USED_CATEGORIES_LIMIT = "usedCategoriesLimit";

    /** Default limit for storing used categories. */
    public static final int USED_CATEGORIES_LIMIT = 50;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUsedCategoriesList.class);

    /** The internal list of category paths (*not* site paths). */
    @JsonProperty("c")
    ArrayList<String> m_categories = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public CmsUsedCategoriesList() {

    }

    /**
     * Stores a used category for the current user.
     *
     * @param cms the current CMS context
     * @param category the category to store
     * @throws CmsException if something goes wrong
     */
    public static void addUsedCategoryForCurrentUser(CmsObject cms, String category) throws CmsException {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        String usedCategoriesInfo = (String)user.getAdditionalInfo(ADDINFO_USED_CATEGORIES);
        CmsUsedCategoriesList usedCategories = fromJson(usedCategoriesInfo);
        int limit = USED_CATEGORIES_LIMIT;
        Object configuredUsedCategoriesLimit = OpenCms.getRuntimeProperty(CONF_USED_CATEGORIES_LIMIT);
        if (configuredUsedCategoriesLimit != null) {
            try {
                limit = Integer.parseInt("" + configuredUsedCategoriesLimit);
            } catch (NumberFormatException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        usedCategories.add(category);
        usedCategories.limit(limit);
        user.setAdditionalInfo(ADDINFO_USED_CATEGORIES, usedCategories.toJson());
        cms.writeUser(user);
    }

    /**
     * Deserializes a used categories list from JSON.
     *
     * @param json the JSON representation
     *
     * @return the used categories list
     */
    public static CmsUsedCategoriesList fromJson(String json) {

        ObjectMapper mapper = new ObjectMapper();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(json)) {
            return new CmsUsedCategoriesList();
        }
        try {
            return mapper.readerFor(CmsUsedCategoriesList.class).readValue(json);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return new CmsUsedCategoriesList();
        }
    }

    /**
     * Adds a new category entry.
     *
     * <p>If the category is already part of this list, it is moved to the end of the list.
     *
     * @param category the category to add
     */
    public void add(String category) {

        m_categories.remove(category);
        m_categories.add(category);
    }

    /**
     * Gets the categories as a set.
     *
     * @return the categories
     */
    @JsonIgnore
    public Set<String> getCategories() {

        return new HashSet<>(m_categories);
    }

    /**
     * Removes all but the last 'maxEntries' entries if there are more entries than that.
     *
     * @param maxEntries the numer of entries to keep
     */
    public void limit(int maxEntries) {

        if (m_categories.size() > maxEntries) {
            m_categories.subList(0, m_categories.size() - maxEntries).clear();
        }
    }

    /**
     * Converts the list to JSON.
     *
     * @return the JSON representation for this list
     */
    public String toJson() {

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return "";
        }
    }

}
