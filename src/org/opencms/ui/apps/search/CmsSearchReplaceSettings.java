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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.apps.search;

import org.opencms.ui.apps.search.CmsSourceSearchForm.SearchType;
import org.opencms.util.CmsStringUtil;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Settings bean for the dialog.
 * <p>
 */
public class CmsSearchReplaceSettings implements Serializable {

    /** The serial version id. */
    private static final long serialVersionUID = 1122133222446934991L;

    /** The force replacement flag. */
    private boolean m_forceReplace;

    /** When replacing XML content, replace-operation only applies to this locale. */
    private String m_locale;

    /** Display message. */
    private String m_message;

    /** Flag indicating if only content values should be searched and replaced. */
    private boolean m_onlyContentValues;

    /** The paths to collect resources. */
    private List<String> m_paths = new LinkedList<String>();

    /** The project to use. */
    private String m_project;

    /** The search query to filter matching resources. */
    private String m_query;

    /** The replace pattern. */
    private String m_replacepattern;

    /** The list of resource paths to process: all should be files. */
    private String[] m_resources;

    /** The search pattern. */
    private String m_searchpattern;

    /** The source to retrive the resources from. */
    private String m_source;

    /** The search type. */
    private SearchType m_type;

    /** The resource type to use for replacement. */
    private String[] m_types;

    /** The Xpath to perform the replacement. */
    private String m_xpath;

    /**
     * Bean constructor with cms object for path validation.<p>
     */
    public CmsSearchReplaceSettings() {

        super();
        m_paths.add("/");
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * @return the message
     */
    public String getMessage() {

        return m_message;
    }

    /**
     * @return the paths
     */
    public List<String> getPaths() {

        return m_paths;
    }

    /**
     * @return the project
     */
    public String getProject() {

        return m_project;
    }

    /**
     * Returns the query.<p>
     *
     * @return the query
     */
    public String getQuery() {

        return m_query;
    }

    /**
     * @return the replace pattern
     */
    public String getReplacepattern() {

        return m_replacepattern;
    }

    /**
     * @return the resources
     */
    public String getResources() {

        return CmsStringUtil.arrayAsString(m_resources, ",");
    }

    /**
     * Returns the resources paths in an array.<p>
     *
     * @return the resources paths in an array.
     */
    public String[] getResourcesArray() {

        return m_resources;
    }

    /**
     * @return the search pattern
     */
    public String getSearchpattern() {

        return m_searchpattern;
    }

    /**
     * Returns the source.<p>
     *
     * @return the source
     */
    public String getSource() {

        return m_source;
    }

    /**
     * Returns the search type.<p>
     *
     * @return the search type
     */
    public SearchType getType() {

        return m_type;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public String getTypes() {

        return m_types != null ? CmsStringUtil.arrayAsString(m_types, ",") : "";
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public String[] getTypesArray() {

        return m_types;
    }

    /**
     * Returns the xpath.<p>
     *
     * @return the xpath
     */
    public String getXpath() {

        return m_xpath;
    }

    /**
     * Returns the force replace flag, if <code>true</code> the replacement
     * will also be performed if the replacement String is empty.<p>
     *
     * @return the force replace flag
     */
    public boolean isForceReplace() {

        return m_forceReplace;
    }

    /**
     * Returns if only content values should be searched and replaced.<p>
     *
     * @return if only content values should be searched and replaced
     */
    public boolean isOnlyContentValues() {

        return m_onlyContentValues;
    }

    /**
     * Returns <code>true</code> if Solr index is selected and a query was entered.<p>
     *
     * @return <code>true</code> if Solr index is selected and a query was entered
     */
    public boolean isSolrSearch() {

        return (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_source)
            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_query));
    }

    /**
     * Sets the force replace flag.<p>
     *
     * @param forceReplace the force replace flag to set
     */
    public void setForceReplace(boolean forceReplace) {

        m_forceReplace = forceReplace;
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(String locale) {

        m_locale = locale;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(final String message) {

        // nop, this is hardcoded... just has to be here for "bean - convention".
    }

    /**
     * Sets if only content values should be searched and replaced.<p>
     *
     * @param onlyContentValue if only content values should be searched and replaced
     */
    public void setOnlyContentValues(boolean onlyContentValue) {

        m_onlyContentValues = onlyContentValue;
    }

    /**
     * Sets the paths.<p>
     *
     * @param paths the paths to set
     */
    public void setPaths(final List<String> paths) {

        m_paths = paths;
    }

    /**
     * @param project the project to work in
     */
    public void setProject(String project) {

        m_project = project;
    }

    /**
     * Sets the query.<p>
     *
     * @param query the query to set
     */
    public void setQuery(String query) {

        m_query = query;
    }

    /**
     * Sets the replace pattern.<p>
     *
     * @param replacepattern the replace pattern
     */
    public void setReplacepattern(String replacepattern) {

        m_replacepattern = replacepattern;
    }

    /**
     * @param resources
     *            the resources to set
     */
    public void setResources(final String resources) {

        m_resources = CmsStringUtil.splitAsArray(resources, ",");

    }

    /**
     * Sets the search pattern.<p>
     *
     * @param searchpattern the search pattern
     */
    public void setSearchpattern(String searchpattern) {

        m_searchpattern = searchpattern;
    }

    /**
     * Sets the source.<p>
     *
     * @param source the source to set
     */
    public void setSource(String source) {

        m_source = source;
    }

    /**
     * Sets the search type.<p>
     *
     * @param type the search type to set
     */
    public void setType(SearchType type) {

        m_type = type;
    }

    /**
     * Sets the type.<p>
     *
     * @param types the type to set
     */
    public void setTypes(String types) {

        m_types = CmsStringUtil.splitAsArray(types, ",");
    }

    /**
     * Sets the xpath.<p>
     *
     * @param xpath the xpath to set
     */
    public void setXpath(String xpath) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(xpath)) {
            xpath = xpath.trim();
            if (xpath.startsWith("/")) {
                xpath = xpath.substring(1);
            }
            if (xpath.endsWith("/")) {
                xpath = xpath.substring(0, xpath.length() - 1);
            }
        }
        m_xpath = xpath;
    }
}
