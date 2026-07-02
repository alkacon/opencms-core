/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.loader;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.util.CmsStringUtil;

import java.nio.file.Paths;

/**
 * File system based storage for generated image cache entries.<p>
 */
public class CmsFsImageCache extends CmsRfsImageCache {

    /** Configuration parameter for the image cache path. */
    public static final String PARAM_PATH = "path";

    /**
     * Creates a new uninitialized FS image cache.<p>
     */
    public CmsFsImageCache() {

        super();
    }

    /**
     * Creates a new FS image cache.<p>
     *
     * @param path the file system path
     * @throws Exception if the path can not be initialized
     */
    public CmsFsImageCache(String path)
    throws Exception {

        super(path);
    }

    /**
     * @see org.opencms.loader.CmsRfsImageCache#initConfiguration()
     */
    @Override
    public void initConfiguration() throws CmsConfigurationException {

        String path = getConfiguration().get(PARAM_PATH);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_IMAGE_CACHE_INIT_1, PARAM_PATH));
        }
        try {
            initRepository(Paths.get(path).toAbsolutePath().normalize());
        } catch (Exception e) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMAGE_CACHE_INIT_1, getClass().getName()),
                e);
        }
    }
}
