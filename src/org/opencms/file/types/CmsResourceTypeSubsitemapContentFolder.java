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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationCopyResource;
import org.opencms.file.CmsObject;
import org.opencms.util.CmsMacroResolver;

import java.util.Arrays;
import java.util.List;

/**
 * Content type class for subsitemap content folders.
 *
 * <p>Has special handling for copy resources.
 */
public class CmsResourceTypeSubsitemapContentFolder extends CmsResourceTypeFolderExtended {

    /** Serial version id. */
    private static final long serialVersionUID = -4763516920316525304L;

    /** The default value for the config file copy source. */
    public static final String DEFAULT_CONFIG_SOURCE = "/system/modules/org.opencms.base/copyresources/sitemap.config";

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getCopyResources(org.opencms.file.CmsObject, java.lang.String, org.opencms.util.CmsMacroResolver)
     */
    @Override
    protected List<CmsConfigurationCopyResource> getCopyResources(
        CmsObject cms,
        String resourcename,
        CmsMacroResolver resolver) {

        String source = DEFAULT_CONFIG_SOURCE;
        CmsConfigurationCopyResource res = new CmsConfigurationCopyResource(
            source,
            "${resource.folder.path}/.config",
            "new");
        return Arrays.asList(res);
    }

}
