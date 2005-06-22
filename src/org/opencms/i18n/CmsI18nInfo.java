/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/CmsI18nInfo.java,v $
 * Date   : $Date: 2005/06/22 10:38:11 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.i18n;

import java.util.Locale;

/**
 * Bundle of i18n setting to be used to setup a new request context.<p>
 *
 * @author Alexander Kandzior 
 * @since 5.3
 */
public class CmsI18nInfo {
    
    /** The locale to use. */
    private String m_encoding;
    
    /** The encoding to use. */
    private Locale m_locale;
    
    /**
     * Generates a new i18n info object.<p>
     * 
     * @param locale the locale to use
     * @param encoding the encoding to use
     */
    public CmsI18nInfo(Locale locale, String encoding) {
        m_encoding = encoding;
        m_locale = locale;
    }
    
    /**
     * Returns the encoding to use.<p>
     * 
     * @return the encoding to use
     */
    public String getEncoding() {
        return m_encoding;
    }
    
    /**
     * Returns the locale to use.<p>
     * 
     * @return the locale to use
     */
    public Locale getLocale() {
        return m_locale;
    }
    
}
