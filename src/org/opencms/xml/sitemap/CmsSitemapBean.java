/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapBean.java,v $
 * Date   : $Date: 2010/02/03 15:10:53 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.sitemap;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Describes one locale of a sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 7.6
 */
public class CmsSitemapBean {

    /** The entry point, as root path. */
    private final String m_entryPoint;

    /** The locale. */
    private final Locale m_locale;

    /** The site entries. */
    private final List<CmsSitemapEntry> m_siteEntries;

    /** 
     * Creates a new sitemap bean.<p> 
     * 
     * @param locale the locale
     * @param entryPoint the entry point
     * @param siteEntries the site entries
     **/
    public CmsSitemapBean(Locale locale, String entryPoint, List<CmsSitemapEntry> siteEntries) {

        m_locale = locale;
        m_entryPoint = entryPoint;
        m_siteEntries = Collections.unmodifiableList(siteEntries);
    }

    /**
     * Returns the entry point, as root path.<p>
     * 
     * @return the entry point
     */
    public String getEntryPoint() {

        return m_entryPoint;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the site entries.<p>
     *
     * @return the site entries
     */
    public List<CmsSitemapEntry> getSiteEntries() {

        return m_siteEntries;
    }
}
