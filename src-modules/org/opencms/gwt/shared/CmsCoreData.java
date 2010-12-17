/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/shared/Attic/CmsCoreData.java,v $
 * Date   : $Date: 2010/12/17 08:45:30 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Runtime data bean for prefetching.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public class CmsCoreData implements IsSerializable {

    /** A enumeration for the ADE context. */
    public enum AdeContext {

        /** Context for container page. */
        containerpage,

        /** Context for sitemap. */
        sitemap
    }

    /** Name of the used js variable. */
    public static final String DICT_NAME = "org_opencms_gwt";

    /** The time sent from the server when loading the data. */
    protected long m_serverTime;

    /** The OpenCms context. */
    private String m_context;

    /** The current request locale. */
    private String m_locale;

    /** The current navigation URI. */
    private String m_navigationUri;

    /** The current site root. */
    private String m_siteRoot;

    /** The current uri. */
    private String m_uri;

    /** The current workplace locale. */
    private String m_wpLocale;

    /**
     * Constructor.<p>
     */
    public CmsCoreData() {

        // empty
    }

    /**
     * Clone constructor.<p>
     * 
     * @param clone the instance to clone 
     */
    public CmsCoreData(CmsCoreData clone) {

        this(
            clone.getContext(),
            clone.getSiteRoot(),
            clone.getLocale(),
            clone.getWpLocale(),
            clone.getUri(),
            clone.getNavigationUri(),
            clone.getServerTime());
    }

    /**
     * Constructor.<p>
     * 
     * @param context the OpenCms context
     * @param siteRoot the current site root
     * @param locale the current request locale
     * @param wpLocale the workplace locale
     * @param uri the current uri
     * @param navigationUri the current navigation URI
     * @param serverTime the current time  
     */
    public CmsCoreData(
        String context,
        String siteRoot,
        String locale,
        String wpLocale,
        String uri,
        String navigationUri,
        long serverTime) {

        m_context = context;
        m_siteRoot = siteRoot;
        m_locale = locale;
        m_wpLocale = wpLocale;
        m_uri = uri;
        m_navigationUri = navigationUri;
        m_serverTime = serverTime;
    }

    /**
     * Returns the OpenCms context.<p>
     *
     * @return the OpenCms context
     */
    public String getContext() {

        return m_context;
    }

    /**
     * Returns the current request locale.<p>
     *
     * @return the current request locale
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Returns the current navigation (sitemap) URI.<p>
     *  
     * @return the current navigation URI 
     */
    public String getNavigationUri() {

        return m_navigationUri;
    }

    /**
     * Returns the time of the server when the data was loaded.<p>
     * 
     * @return the time of the server when the data was loaded 
     */
    public long getServerTime() {

        return m_serverTime;
    }

    /**
     * Returns the current site root.<p>
     *
     * @return the current site root
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Returns the current uri.<p>
     *
     * @return the current uri
     */
    public String getUri() {

        return m_uri;
    }

    /**
     * Returns the current workplace locale.<p>
     *
     * @return the current workplace locale
     */
    public String getWpLocale() {

        return m_wpLocale;
    }

}