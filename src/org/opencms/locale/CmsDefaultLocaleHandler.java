/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/locale/Attic/CmsDefaultLocaleHandler.java,v $
 * Date   : $Date: 2004/01/22 10:39:35 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.locale;

import org.opencms.main.OpenCms;

import com.opencms.file.CmsRequestContext;

import java.util.HashSet;
import java.util.Set;

/**
 * @version $Revision: 1.1 $ $Date: 2004/01/22 10:39:35 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsDefaultLocaleHandler implements I_CmsLocaleHandler {

    /**
     * @see org.opencms.locale.I_CmsLocaleHandler#getLocaleName(com.opencms.file.CmsRequestContext, java.lang.String, java.lang.String[], java.lang.String[])
     */
    public String getLocaleName(
        CmsRequestContext context,
        String requestedLocaleName,
        String availableLocaleNames[],
        String defaultLocaleNames[]) {
        
        CmsLocaleManager localeManager = OpenCms.getLocaleManager();
        
        // initialize locale names if not initialized resource-specific
        if (availableLocaleNames == null) {
            availableLocaleNames = localeManager.getAvailableLocaleNames();
        }
        if (defaultLocaleNames == null) {
            defaultLocaleNames = localeManager.getDefaultLocaleNames();
        }
        
        // get the available locales, i.e. filter the available locale names against the requested locale name if defined
        HashSet filter = null;
        if (requestedLocaleName != null) {
            filter = new HashSet();
            filter.add(requestedLocaleName);
        }
        Set available = localeManager.getMatchingLocales(availableLocaleNames, filter);
        
        // get the best matching locale, i.e. filter the requested locale name and the default locale names against the available locale names
        String result = localeManager.getBestMatchingLocaleName(requestedLocaleName, defaultLocaleNames, available);
        
        // if there is no matching locale at all, assume the default locale of the system
        if (result == null) {
            result = localeManager.getDefaultLocaleName();
        }
        
        return result;
    }
}
