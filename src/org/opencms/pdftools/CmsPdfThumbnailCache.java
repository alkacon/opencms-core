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
import org.opencms.util.CmsFileUtil;

/**
 * Cache for PDF thumbnails.<p>
 */
public class CmsPdfThumbnailCache extends CmsVfsNameBasedDiskCache {

    /** The folder name for the cache folder. */
    public static final String PDF_CACHE_FOLDER = "imagecache/pdfthumbnails";

    /**
     * Creates a new cache instance.<p>
     */
    public CmsPdfThumbnailCache() {

        super(OpenCms.getSystemInfo().getWebInfRfsPath(), PDF_CACHE_FOLDER);
    }

    /**
     * @see org.opencms.cache.CmsVfsNameBasedDiskCache#getCacheName(org.opencms.file.CmsResource, java.lang.String)
     */
    @Override
    public String getCacheName(CmsResource resource, String parameters) {

        String extension = CmsFileUtil.getExtension(resource.getRootPath());
        CmsWrappedResource wrapper = new CmsWrappedResource(resource);
        String fakePath = "/thumbnail_" + resource.getStructureId() + extension;
        wrapper.setRootPath(fakePath);
        CmsResource fakeResource = wrapper.getResource();
        return super.getCacheName(fakeResource, parameters);
    }

}
