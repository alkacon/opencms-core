/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsLocalizedKeyMapper.java,v $
 * Date   : $Date: 2005/02/17 12:44:35 $
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

import org.opencms.util.I_CmsStringMapper;

/** 
 * The string mapper is used to resolve localized key macros for the Workplace.<p> 
 */
class CmsLocalizedKeyMapper implements I_CmsStringMapper {

    /** The workplace settings to use for the localization.<p> */
    private CmsWorkplaceSettings m_settings;

    /**
     * Creates a new localized key mapper.<p>
     * 
     * @param settings the current users OpenCms Workplace settings
     */
    CmsLocalizedKeyMapper(CmsWorkplaceSettings settings) {

        m_settings = settings;
    }

    /**
     * Maps a given key to a localized value.<p>
     * 
     * @param key the key starting with "key:"
     * 
     * @return the localized value
     */
    public String getValue(String key) {

        if (key.startsWith(I_CmsStringMapper.KEY_LOCALIZED_PREFIX)) {
            return m_settings.getMessages().key(key.substring(I_CmsStringMapper.KEY_LOCALIZED_PREFIX.length()));
        } else {
            return null;
        }
    }
}