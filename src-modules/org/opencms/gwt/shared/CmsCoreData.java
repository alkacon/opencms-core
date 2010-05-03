/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/shared/Attic/CmsCoreData.java,v $
 * Date   : $Date: 2010/05/03 14:33:05 $
 * Version: $Revision: 1.2 $
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
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsCoreData implements IsSerializable {

    /** Name of the used js variable. */
    public static final String DICT_NAME = "org_opencms_gwt";

    /** The OpenCms context. */
    private String m_context;

    /** The current request locale. */
    private String m_locale;

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

        this(clone.getContext(), clone.getSiteRoot(), clone.getLocale(), clone.getWpLocale(), clone.getUri());
    }

    /**
     * Constructor.<p>
     * 
     * @param context the OpenCms context
     * @param siteRoot the current site root
     * @param locale the current request locale
     * @param wpLocale the workplace locale
     * @param uri the current uri
     */
    public CmsCoreData(String context, String siteRoot, String locale, String wpLocale, String uri) {

        m_context = context;
        m_siteRoot = siteRoot;
        m_locale = locale;
        m_wpLocale = wpLocale;
        m_uri = uri;
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