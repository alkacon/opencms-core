/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsLocalizedKeyResolver.java,v $
 * Date   : $Date: 2005/03/20 23:44:28 $
 * Version: $Revision: 1.2 $
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

package org.opencms.workplace;

import org.opencms.util.CmsMacroResolver;
import org.opencms.util.I_CmsMacroResolver;

/** 
 * Used to resolve localized key macros for the Workplace.<p> 
 * 
 * This macro resolver can handler the following macros:<ul>
 * <li><code>${key.<i>NAME</i>}</code>: The localized message key for <code><i>NAME</i></CODE> is returned.</li>
 * </ul>
 */
class CmsLocalizedKeyResolver implements I_CmsMacroResolver {

    /** Key prefix used to specify the value of a localized key as macro value. */
    public static final String MACRO_KEY = "key.";

    /** The workplace settings to use for the localization.<p> */
    private CmsWorkplaceSettings m_workplaceSettings;

    /**
     * Factory method to create a new {@link CmsLocalizedKeyResolver} instance.<p>
     * 
     * @return a new instance of a {@link CmsLocalizedKeyResolver}
     */
    public static CmsLocalizedKeyResolver newInstance() {

        return new CmsLocalizedKeyResolver();
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#getMacroValue(java.lang.String)
     */
    public String getMacroValue(String key) {

        if (m_workplaceSettings != null) {
            if (key.startsWith(MACRO_KEY)) {
                return m_workplaceSettings.getMessages().key(key.substring(MACRO_KEY.length()));
            }
        }
        return null;
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#isKeepEmptyMacros()
     */
    public boolean isKeepEmptyMacros() {

        return false;
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#resolveMacros(java.lang.String)
     */
    public String resolveMacros(String input) {

        return CmsMacroResolver.resolveMacros(input, this);
    }

    /**
     * Provides the current users workplace settings to this macro resolver.<p>
     * 
     * @param workplaceSettings the current users workplace settings
     * 
     * @return this instance
     */
    public CmsLocalizedKeyResolver setWorkplaceSettings(CmsWorkplaceSettings workplaceSettings) {

        m_workplaceSettings = workplaceSettings;
        return this;
    }
}