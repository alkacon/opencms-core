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

package org.opencms.ui.apps.cacheadmin;

import com.alkacon.simapi.Simapi;

import org.opencms.file.CmsObject;
import org.opencms.loader.CmsImageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.util.CmsFileUtil;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

/**
 * Bean for Variations im image resources.<p>
 */
public class CmsVariationBean {

    /**Variation path.*/
    private String m_variationPath;

    /**Root CmsObject. */
    private CmsObject m_rootCms;

    /**
     * public constructor.<p>
     *
     * @param variation path to variation file to hold information for
     */
    public CmsVariationBean(String variation) {

        m_variationPath = variation;
        try {
            m_rootCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            m_rootCms.getRequestContext().setSiteRoot("");
        } catch (CmsException e) {
            //
        }

    }

    /**
     * Gets the dimensions of the current variation.<p>
     *
     * @return String representation of the dimensions
     */
    @SuppressWarnings("resource")
    public String getDimensions() {

        try {
            BufferedImage img = Simapi.read(IOUtils.toByteArray(new FileInputStream(m_variationPath)));
            return "" + img.getWidth() + " x " + img.getHeight() + "px";
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Get length of variation.<p>
     *
     * @return string representation of length
     */
    @SuppressWarnings("resource")
    public String getLength() {

        try {
            return CmsFileUtil.formatFilesize(
                IOUtils.toByteArray(new FileInputStream(m_variationPath)).length,
                A_CmsUI.get().getLocale());
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Gets path of variation.<p>
     *
     * @return path
     */
    public String getName() {

        return m_variationPath.substring(CmsImageLoader.getImageRepositoryPath().length());
    }
}
