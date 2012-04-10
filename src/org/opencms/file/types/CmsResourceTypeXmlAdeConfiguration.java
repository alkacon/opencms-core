/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file.types;

import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.i18n.CmsLocaleManager;

import java.util.List;
import java.util.Locale;

/**
 * Custom resource type for ADE configurations.<p>
 */
public class CmsResourceTypeXmlAdeConfiguration extends CmsResourceTypeXmlContent {

    /**
     * This overridden method ensures that configurations will always be created with an english locale node.<p>
     *  
     * @see org.opencms.file.types.CmsResourceTypeXmlContent#getLocaleForNewContent(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, java.lang.String, java.util.List)
     */
    @Override
    protected Locale getLocaleForNewContent(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        List<CmsProperty> properties) {

        return CmsLocaleManager.getLocale("en");
    }

}
