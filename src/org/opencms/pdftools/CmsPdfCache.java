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

package org.opencms.pdftools;

import org.opencms.cache.CmsVfsNameBasedDiskCache;
import org.opencms.file.CmsResource;
import org.opencms.file.wrapper.CmsWrappedResource;
import org.opencms.main.OpenCms;

/**
 * Cache class for storing generated PDFs in the RFS.<p>
 *
 * This class stores all cached files in a single folder.
 */
public class CmsPdfCache extends CmsVfsNameBasedDiskCache {

    /** The folder name for the cache folder. */
    public static final String PDF_CACHE_FOLDER = "pdfcache";

    /**
     * Creates a new cache instance.<p>
     */
    public CmsPdfCache() {

        super(OpenCms.getSystemInfo().getWebInfRfsPath(), PDF_CACHE_FOLDER);
    }

    /**
     * @see org.opencms.cache.CmsVfsNameBasedDiskCache#getCacheName(org.opencms.file.CmsResource, java.lang.String)
     */
    @Override
    public String getCacheName(CmsResource resource, String parameters) {

        // we want a 'flat' folder structure, so we create a fake resource with a path in which the slashes
        // from the original path have been transformed to underscores
        CmsWrappedResource wrapper = new CmsWrappedResource(resource);
        String fakePath = "/" + resource.getRootPath().replaceAll("/", "_");
        // the extension doesn't really matter for the caching, but with an extension of PDF it's easier to look at the files in the cache folder with
        // file managers, so we replace the extension
        fakePath = fakePath.replaceFirst("\\.(?:html|xml)$", ".pdf");
        wrapper.setRootPath(fakePath);
        CmsResource fakeResource = wrapper.getResource();
        return super.getCacheName(fakeResource, parameters);
    }

}
