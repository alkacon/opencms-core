/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapBean.java,v $
 * Date   : $Date: 2009/11/03 13:30:42 $
 * Version: $Revision: 1.1 $
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
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.6
 */
public class CmsSitemapBean {

    /** The site entries. */
    private final List<CmsSiteEntryBean> m_siteEntries;

    /** The locale. */
    private final Locale m_locale;

    /** 
     * Creates a new sitemap bean.<p> 
     * 
     * @param locale the locale
     * @param siteEntries the site entries
     **/
    public CmsSitemapBean(Locale locale, List<CmsSiteEntryBean> siteEntries) {

        m_locale = locale;
        m_siteEntries = Collections.unmodifiableList(siteEntries);
    }

    /**
     * Returns the site entries.<p>
     *
     * @return the site entries
     */
    public List<CmsSiteEntryBean> getSiteEntries() {

        return m_siteEntries;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }
}
