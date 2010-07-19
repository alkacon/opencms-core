/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/CmsCroppingParamBean.java,v $
 * Date   : $Date: 2010/07/19 07:45:28 $
 * Version: $Revision: 1.1 $
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

import org.opencms.util.CmsStringUtil;

/**
 * Scale parameter data bean.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsCroppingParamBean {

    /** Scale parameter name. */
    private static final String SCALE_PARAM_CROP_HEIGHT = "ch";

    /** Scale parameter name. */
    private static final String SCALE_PARAM_CROP_WIDTH = "cw";

    /** Scale parameter name. */
    private static final String SCALE_PARAM_CROP_X = "cx";

    /** Scale parameter name. */
    private static final String SCALE_PARAM_CROP_Y = "cy";

    /** The scale parameter delimiter. */
    private static final String SCALE_PARAM_DELIMITER = ",";

    /** The scale parameter equal. */
    private static final String SCALE_PARAM_EQ = "=";

    /** The scale parameter colon. */
    private static final String SCALE_PARAM_COLON = ":";

    /** Scale parameter name. */
    private static final String SCALE_PARAM_NAME = "__scale";

    /** Scale parameter name. */
    private static final String SCALE_PARAM_TARGETHEIGHT = "h";

    /** Scale parameter name. */
    private static final String SCALE_PARAM_TARGETWIDTH = "w";

    /** The cropping height parameter. */
    private int m_cropHeight = -1;

    /** The cropping width parameter. */
    private int m_cropWidth = -1;

    /** The cropping X parameter. */
    private int m_cropX = -1;

    /** The cropping Y parameter. */
    private int m_cropY = -1;

    /** The target height. */
    private int m_targetHeight = -1;

    /** The target width. */
    private int m_targetWidth = -1;

    /**
     * Parses an image scale parameter and returns the parsed data.<p>
     * 
     * @param selectedPath the image path including the scale parameter
     * 
     * @return the cropping data
     */
    public static CmsCroppingParamBean parseImagePath(String selectedPath) {

        if (selectedPath.contains(SCALE_PARAM_NAME + SCALE_PARAM_EQ)) {
            // removing string part before the scaling parameter
            String param = selectedPath.substring(SCALE_PARAM_NAME.length() + SCALE_PARAM_EQ.length());

            // removing string part after the scaling parameter
            if (param.contains("&")) {
                param = param.substring(0, param.indexOf("&"));
            }
            return parseScaleParam(param);
        }
        return new CmsCroppingParamBean();
    }

    /**
     * Parses an image scale parameter and returns the parsed data.<p>
     * 
     * @param param the image path including the scale parameter
     * 
     * @return the cropping data
     */
    public static CmsCroppingParamBean parseScaleParam(String param) {

        CmsCroppingParamBean result = new CmsCroppingParamBean();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(param)) {
            return result;
        }
        String[] parameters = param.split(SCALE_PARAM_DELIMITER);
        for (int i = 0; i < parameters.length; i++) {
            String scaleParam = parameters[i].trim();
            if (scaleParam.startsWith(SCALE_PARAM_TARGETHEIGHT + SCALE_PARAM_COLON)) {
                result.setTargetHeight(parseValue(SCALE_PARAM_TARGETHEIGHT, scaleParam));
                continue;
            }
            if (scaleParam.startsWith(SCALE_PARAM_TARGETWIDTH + SCALE_PARAM_COLON)) {
                result.setTargetWidth(parseValue(SCALE_PARAM_TARGETWIDTH, scaleParam));
                continue;
            }
            if (scaleParam.startsWith(SCALE_PARAM_CROP_X + SCALE_PARAM_COLON)) {
                result.setCropX(parseValue(SCALE_PARAM_CROP_X, scaleParam));
                continue;
            }
            if (scaleParam.startsWith(SCALE_PARAM_CROP_Y + SCALE_PARAM_COLON)) {
                result.setCropY(parseValue(SCALE_PARAM_CROP_Y, scaleParam));
                continue;
            }
            if (scaleParam.startsWith(SCALE_PARAM_CROP_HEIGHT + SCALE_PARAM_COLON)) {
                result.setCropHeight(parseValue(SCALE_PARAM_CROP_HEIGHT, scaleParam));
                continue;
            }
            if (scaleParam.startsWith(SCALE_PARAM_CROP_WIDTH + SCALE_PARAM_COLON)) {
                result.setCropWidth(parseValue(SCALE_PARAM_CROP_WIDTH, scaleParam));
                continue;
            }
        }
        return result;
    }

    /**
     * Parses a single scale value. Returning <code>-1</code> for invalid parameters.<p>
     * 
     * @param paramName the parameter name
     * @param param the parameter
     * 
     * @return the value
     */
    private static native int parseValue(String paramName, String param)/*-{
        param=param.substr(paramName.length+2);
        var result=parseInt(param);
        if (isNaN(result)){
        return -1;
        }
        return result;
    }-*/;

    /**
     * Returns the cropping height parameter.<p>
     *
     * @return the cropping height parameter
     */
    public int getCropHeight() {

        return m_cropHeight;
    }

    /**
     * Returns the cropping width parameter.<p>
     *
     * @return the cropping width parameter
     */
    public int getCropWidth() {

        return m_cropWidth;
    }

    /**
     * Returns the cropping X parameter.<p>
     *
     * @return the cropping X parameter
     */
    public int getCropX() {

        return m_cropX;
    }

    /**
     * Returns the cropping Y parameter.<p>
     *
     * @return the cropping Y parameter
     */
    public int getCropY() {

        return m_cropY;
    }

    /**
     * Returns the target height.<p>
     *
     * @return the target height
     */
    public int getTargetHeight() {

        return m_targetHeight;
    }

    /**
     * Returns the target width.<p>
     *
     * @return the target width
     */
    public int getTargetWidth() {

        return m_targetWidth;
    }

    /**
     * Returns if contained parameters indicate a cropped image.<p>
     * 
     * @return <code>true</code> if contained parameters indicate a cropped image
     */
    public boolean isCropped() {

        return m_cropX > -1;
    }

    /**
     * Sets the cropping height parameter.<p>
     *
     * @param cropHeight the cropping height parameter to set
     */
    public void setCropHeight(int cropHeight) {

        m_cropHeight = cropHeight;
    }

    /**
     * Sets the cropping width parameter.<p>
     *
     * @param cropWidth the cropping width parameter to set
     */
    public void setCropWidth(int cropWidth) {

        m_cropWidth = cropWidth;
    }

    /**
     * Sets the cropping X parameter.<p>
     *
     * @param cropX the cropping X parameter to set
     */
    public void setCropX(int cropX) {

        m_cropX = cropX;
    }

    /**
     * Sets the cropping Y parameter.<p>
     *
     * @param cropY the cropping Y parameter to set
     */
    public void setCropY(int cropY) {

        m_cropY = cropY;
    }

    /**
     * Sets the target height.<p>
     *
     * @param targetHeight the target height to set
     */
    public void setTargetHeight(int targetHeight) {

        m_targetHeight = targetHeight;
    }

    /**
     * Sets the target width.<p>
     *
     * @param targetWidth the target width to set
     */
    public void setTargetWidth(int targetWidth) {

        m_targetWidth = targetWidth;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer();
        if (m_targetHeight > -1) {
            result.append(SCALE_PARAM_TARGETHEIGHT).append(SCALE_PARAM_COLON).append(m_targetHeight).append(
                SCALE_PARAM_DELIMITER);
        }
        if (m_targetWidth > -1) {
            result.append(SCALE_PARAM_TARGETWIDTH).append(SCALE_PARAM_COLON).append(m_targetWidth).append(
                SCALE_PARAM_DELIMITER);
        }
        if (m_cropX > -1) {
            result.append(SCALE_PARAM_CROP_X).append(SCALE_PARAM_COLON).append(m_cropX).append(SCALE_PARAM_DELIMITER);
        }
        if (m_cropY > -1) {
            result.append(SCALE_PARAM_CROP_Y).append(SCALE_PARAM_COLON).append(m_cropY).append(SCALE_PARAM_DELIMITER);
        }
        if (m_cropHeight > -1) {
            result.append(SCALE_PARAM_CROP_HEIGHT).append(SCALE_PARAM_COLON).append(m_cropHeight).append(
                SCALE_PARAM_DELIMITER);
        }
        if (m_cropWidth > -1) {
            result.append(SCALE_PARAM_CROP_WIDTH).append(SCALE_PARAM_COLON).append(m_cropWidth).append(
                SCALE_PARAM_DELIMITER);
        }
        if (result.length() > 0) {
            result.insert(0, SCALE_PARAM_EQ).insert(0, SCALE_PARAM_NAME);
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

}
