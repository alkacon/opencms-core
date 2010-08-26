/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/CmsImageFormatRestriction.java,v $
 * Date   : $Date: 2010/08/26 13:34:11 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries.client.preview;

import org.opencms.gwt.client.util.CmsClientStringUtil;

/**
 * Predefined image format restriction. To be used within the image format tab of the image preview.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsImageFormatRestriction implements I_CmsFormatRestriction {

    /** The height. */
    private int m_height;

    /** The label. */
    private String m_label;

    /** The width. */
    private int m_width;

    /**
     * Constructor.<p>
     * 
     * @param label the label
     * @param config the configuration
     */
    public CmsImageFormatRestriction(String label, String config) {

        m_label = label;
        parseConfig(config);
    }

    /**
     * Returns if given configuration string is valid.<p>
     * 
     * @param config configuration string
     * 
     * @return <code>true</code> if given configuration string is valid
     */
    public static native boolean isValidConfig(String config)/*-{
        var regex=/^(\?|\d+)x(\?|\d+)$/;
        return regex.test(config);
    }-*/;

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsFormatRestriction#getHeight(int, int)
     */
    public int getHeight(int orgHeight, int orgWidth) {

        if ((m_height == -1) && (m_width == -1)) {
            return orgHeight;
        }

        return (m_height == -1) ? (orgHeight * m_width / orgWidth) : m_height;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsFormatRestriction#getLabel()
     */
    public String getLabel() {

        return m_label;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsFormatRestriction#getWidth(int, int)
     */
    public int getWidth(int orgHeight, int orgWidth) {

        if ((m_height == -1) && (m_width == -1)) {
            return orgWidth;
        }

        return (m_width == -1) ? (orgWidth * m_height / orgHeight) : m_width;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsFormatRestriction#isCroppingEnabled()
     */
    public boolean isCroppingEnabled() {

        return true;
    }

    /**
    * @see org.opencms.ade.galleries.client.preview.I_CmsFormatRestriction#isFixedRatio()
    */
    public boolean isFixedRatio() {

        return true;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsFormatRestriction#isHeightEditable()
     */
    public boolean isHeightEditable() {

        return m_height == -1;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsFormatRestriction#isWidthEditable()
     */
    public boolean isWidthEditable() {

        return m_width == -1;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsFormatRestriction#matchesCroppingParam(org.opencms.ade.galleries.client.preview.CmsCroppingParamBean)
     */
    public boolean matchesCroppingParam(CmsCroppingParamBean croppingParam) {

        if (!isHeightEditable() && (m_height != croppingParam.getTargetHeight())) {
            return false;
        }

        if (!isWidthEditable() && (m_width != croppingParam.getTargetWidth())) {
            return false;
        }
        return true;
    }

    /**
     * Parses the the given configuration string.<p>
     * 
     * @param config the configuration
     */
    private void parseConfig(String config) {

        if (isValidConfig(config)) {
            String[] conf = config.split("x");
            if (conf[0].trim().equals("?")) {
                m_width = -1;
            } else {
                m_width = CmsClientStringUtil.parseInt(conf[0]);
                m_width = (m_width == 0) ? -1 : m_width;
            }
            if (conf[1].trim().equals("?")) {
                m_height = -1;
            } else {
                m_height = CmsClientStringUtil.parseInt(conf[0]);
                m_height = (m_height == 0) ? -1 : m_height;
            }
        }
    }
}
