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

package org.opencms.ui.components;

import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;

import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Component state bean.
 */
public class CmsComponentState {

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsComponentState.class);

    /** Hash parameter. */
    private static final String SITE = "s";

    /** Hash parameter. */
    private static final String FOLDER = "f";

    /** Hash parameter. */
    private static final String RESOURCE_TYPE = "t";

    /** Hash parameter. */
    private static final String DATE_FROM = "d1";

    /** Hash parameter. */
    private static final String DATE_TO = "d2";

    /** Hash parameter. */
    private static final String AVAILABILITY = "a";

    /** Hash parameter. */
    private static final String LOCALE = "l";

    /** Hash parameter. */
    private static final String QUERY = "q";

    /** Hash parameter. */
    private static final String CATEGORY = "c";

    /** The site. */
    String m_site;

    /** The folder. */
    String m_folder;

    /** The resource type. */
    I_CmsResourceType m_resourceType;

    /** The date from. */
    Date m_dateFrom;

    /** The date to. */
    Date m_dateTo;

    /** The availability. */
    String m_availability;

    /** The locale. */
    Locale m_locale;

    /** The text search query. */
    String m_query;

    /** The category. */
    String m_category;

    /**
     * Creates a new state bean.
     */
    public CmsComponentState() {

    }

    /**
     * Creates a new state bean for a given state string.
     * @param state the state string
     */
    public CmsComponentState(String state) {

        parseStateString(state);
    }

    /**
     * Generates a state string for this state bean.
     * @return the state string
     */
    public String generateStateString() {

        String state = "";
        if (m_site != null) {
            state = A_CmsWorkplaceApp.addParamToState(state, SITE, m_site);
        }
        if (m_folder != null) {
            state = A_CmsWorkplaceApp.addParamToState(state, FOLDER, m_folder);
        }
        if (m_resourceType != null) {
            state = A_CmsWorkplaceApp.addParamToState(state, RESOURCE_TYPE, m_resourceType.getTypeName());
        }
        if (m_dateFrom != null) {
            state = A_CmsWorkplaceApp.addParamToState(state, DATE_FROM, String.valueOf(m_dateFrom.getTime()));
        }
        if (m_dateTo != null) {
            state = A_CmsWorkplaceApp.addParamToState(state, DATE_TO, String.valueOf(m_dateTo.getTime()));
        }
        if (m_availability != null) {
            state = A_CmsWorkplaceApp.addParamToState(state, AVAILABILITY, m_availability);
        }
        if (m_locale != null) {
            state = A_CmsWorkplaceApp.addParamToState(state, LOCALE, m_locale.toString());
        }
        if (m_query != null) {
            state = A_CmsWorkplaceApp.addParamToState(state, QUERY, m_query);
        }
        if (m_category != null) {
            state = A_CmsWorkplaceApp.addParamToState(state, CATEGORY, m_category);
        }
        return state;
    }

    /**
     * Returns the availability.
     * @return the availability
     */
    public String getAvailability() {

        return m_availability;
    }

    /**
     * Returns the category.
     * @return the category
     */
    public String getCategory() {

        return m_category;
    }

    /**
     * Returns the date from.
     * @return the date from
     */
    public Date getDateFrom() {

        return m_dateFrom;
    }

    /**
     * Returns the date to.
     * @return the date to
     */
    public Date getDateTo() {

        return m_dateTo;
    }

    /**
     * Returns the folder.
     * @return the folder
     */
    public String getFolder() {

        return m_folder;
    }

    /**
     * Returns the locale.
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the query.
     * @return the query
     */
    public String getQuery() {

        return m_query;
    }

    /**
     * Returns the resource type.
     * @return the resource type
     */
    public I_CmsResourceType getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the site.
     * @return the site
     */
    public String getSite() {

        return m_site;
    }

    /**
     * Parses a state string.
     * @param state the state string
     */
    public void parseStateString(String state) {

        if (state != null) {
            String site = A_CmsWorkplaceApp.getParamFromState(state, SITE);
            if (site != null) {
                m_site = site.replace("%2F", "/");
            }
            String folder = A_CmsWorkplaceApp.getParamFromState(state, FOLDER);
            if (folder != null) {
                m_folder = folder.replace("%2F", "/");
            }
            try {
                String typeName = A_CmsWorkplaceApp.getParamFromState(state, RESOURCE_TYPE);
                if (typeName != null) {
                    I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(typeName);
                    m_resourceType = type;
                }
            } catch (CmsLoaderException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            String dateFrom = A_CmsWorkplaceApp.getParamFromState(state, DATE_FROM);
            if (dateFrom != null) {
                try {
                    m_dateFrom = new Date(Long.parseLong(dateFrom));
                } catch (NumberFormatException e) {
                    m_dateFrom = null;
                }
            }
            String dateTo = A_CmsWorkplaceApp.getParamFromState(state, DATE_TO);
            if (dateTo != null) {
                try {
                    m_dateTo = new Date(Long.parseLong(dateTo));
                } catch (NumberFormatException e) {
                    m_dateTo = null;
                }
            }
            String availability = A_CmsWorkplaceApp.getParamFromState(state, AVAILABILITY);
            if (availability != null) {
                m_availability = availability;
            }
            String locale = A_CmsWorkplaceApp.getParamFromState(state, LOCALE);
            if (locale != null) {
                m_locale = CmsLocaleManager.getLocale(locale);
            }
            String query = A_CmsWorkplaceApp.getParamFromState(state, QUERY);
            if (query != null) {
                m_query = query.replace("%2F", "/");
            }
            String category = A_CmsWorkplaceApp.getParamFromState(state, CATEGORY);
            if (category != null) {
                m_category = category.replace("%2F", "/");
            }
        }
    }

    /**
     * Sets the availability.
     * @param availability the availability
     */
    public void setAvailability(String availability) {

        m_availability = availability;
    }

    /**
     * Sets the category.
     * @param category the category
     */
    public void setCategory(String category) {

        m_category = category;
    }

    /**
     * Sets the from date.
     * @param dateFrom the dateFrom
     */
    public void setDateFrom(Date dateFrom) {

        m_dateFrom = dateFrom;
    }

    /**
     * Sets the to date.
     * @param dateTo the dateTo
     */
    public void setDateTo(Date dateTo) {

        m_dateTo = dateTo;
    }

    /**
     * Sets the folder.
     * @param folder the folder
     */
    public void setFolder(String folder) {

        m_folder = folder;
    }

    /**
     * Sets the locale.
     * @param locale the locale
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the query.
     * @param query the query
     */
    public void setQuery(String query) {

        m_query = query;
    }

    /**
     * Sets the resource type.
     * @param resourceType the resource type
     */
    public void setResourceType(I_CmsResourceType resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * Sets the site.
     * @param site the site
     */
    public void setSite(String site) {

        m_site = site;
    }
}
