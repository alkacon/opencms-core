/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * @since 8.0.0
 */
public class CmsCroppingParamBean {

    private static final String SCALE_FORMAT_NAME_PARAM = "__formatName";

    /** The scale parameter colon. */
    private static final String SCALE_PARAM_COLON = ":";

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

    /** Scale parameter name. */
    private static final String SCALE_PARAM_NAME = "__scale";

    /** Scale parameter name. */
    private static final String SCALE_PARAM_TARGETHEIGHT = "h";

    /** Scale parameter name. */
    private static final String SCALE_PARAM_TARGETWIDTH = "w";

    /** The cropping height parameter. */
    private int m_cropHeight = I_CmsFormatRestriction.DIMENSION_NOT_SET;

    /** The cropping width parameter. */
    private int m_cropWidth = I_CmsFormatRestriction.DIMENSION_NOT_SET;

    /** The cropping X parameter. */
    private int m_cropX = I_CmsFormatRestriction.DIMENSION_NOT_SET;

    /** The cropping Y parameter. */
    private int m_cropY = I_CmsFormatRestriction.DIMENSION_NOT_SET;

    /** The used format name. */
    private String m_formatName;

    private int m_orgHeight = I_CmsFormatRestriction.DIMENSION_NOT_SET;

    private int m_orgWidth = I_CmsFormatRestriction.DIMENSION_NOT_SET;

    /** The target height. */
    private int m_targetHeight = I_CmsFormatRestriction.DIMENSION_NOT_SET;

    /** The target width. */
    private int m_targetWidth = I_CmsFormatRestriction.DIMENSION_NOT_SET;

    /**
     * Constructor.<p>
     */
    public CmsCroppingParamBean() {

        // nothing to do here
    }

    /**
     * Copy constructor.<p>
     * 
     * @param copy the copy values to use
     */
    public CmsCroppingParamBean(CmsCroppingParamBean copy) {

        this(copy.getOrgHeight(), copy.getOrgWidth());
        m_cropHeight = copy.getCropHeight();
        m_cropWidth = copy.getCropWidth();
        m_cropX = copy.getCropX();
        m_cropY = copy.getCropY();
        m_targetHeight = copy.getTargetHeight();
        m_targetWidth = copy.getTargetWidth();
    }

    /**
     * Constructor.<p>
     * 
     * @param orgHeight the original image height 
     * @param orgWidth the original image width
     */
    public CmsCroppingParamBean(int orgHeight, int orgWidth) {

        m_orgHeight = orgHeight;
        m_orgWidth = orgWidth;
    }

    /**
     * Parses an image scale parameter and returns the parsed data.<p>
     * 
     * @param selectedPath the image path including the scale parameter
     * 
     * @return the cropping data
     */
    public static CmsCroppingParamBean parseImagePath(String selectedPath) {

        CmsCroppingParamBean result = null;
        int pos = selectedPath.indexOf(SCALE_PARAM_NAME + SCALE_PARAM_EQ);
        if (pos > -1) {
            // removing string part before the scaling parameter
            String param = selectedPath.substring(pos + SCALE_PARAM_NAME.length() + SCALE_PARAM_EQ.length());

            // removing string part after the scaling parameter
            pos = param.indexOf("&");
            if (pos > -1) {
                param = param.substring(0, pos);
            }
            result = parseScaleParam(param);
        } else {
            result = new CmsCroppingParamBean();
        }
        // look up format name if available
        pos = selectedPath.indexOf(SCALE_FORMAT_NAME_PARAM + SCALE_PARAM_EQ);
        if (pos > -1) {
            String param = selectedPath.substring(pos + SCALE_FORMAT_NAME_PARAM.length() + SCALE_PARAM_EQ.length());

            // removing string part after the scaling parameter
            pos = param.indexOf("&");
            if (pos > -1) {
                param = param.substring(0, pos);
            }
            result.setFormatName(param);
        }
        return result;
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
     * Parses a single scale value. Returning <code>-1</code> --> {@link I_CmsFormatRestriction.DIMENSION_NOT_SET} invalid parameters.<p>
     * 
     * @param paramName the parameter name
     * @param param the parameter
     * 
     * @return the value
     */
    private static native int parseValue(String paramName, String param)/*-{
        param = param.substr(paramName.length + 1);
        var result = parseInt(param);
        if (isNaN(result)) {
            return I_CmsFormatRestriction.DIMENSION_NOT_SET;
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
     * Returns the used format name.<p>
     *
     * @return the used format name
     */
    public String getFormatName() {

        return m_formatName;
    }

    /**
     * Returns the original image height.<p>
     *
     * @return the original image height
     */
    public int getOrgHeight() {

        return m_orgHeight;
    }

    /**
     * Returns the original image width.<p>
     *
     * @return the original image width
     */
    public int getOrgWidth() {

        return m_orgWidth;
    }

    /**
     * Returns the resulting image ratio.<p>
     * 
     * @return the image ratio
     */
    public double getRatio() {

        double ratio = 1;
        if ((getTargetWidth() == -1) || (getTargetHeight() == -1)) {
            ratio = (double)getOrgWidth() / getOrgHeight();
        } else {
            ratio = (double)getTargetWidth() / getTargetHeight();
        }
        return ratio;
    }

    /**
     * Returns a cropping bean with a restricted maximum target size.<p>
     * 
     * @param maxHeight the max height
     * @param maxWidth the max width
     * 
     * @return the cropping bean
     */
    public CmsCroppingParamBean getRestrictedSizeParam(int maxHeight, int maxWidth) {

        CmsCroppingParamBean result = new CmsCroppingParamBean(this);
        if ((getTargetHeight() <= maxHeight) && (getTargetWidth() <= maxWidth)) {
            if ((getTargetHeight() == I_CmsFormatRestriction.DIMENSION_NOT_SET) && (getOrgHeight() > maxHeight)) {
                result.setTargetHeight(maxHeight);
            }
            if ((getTargetWidth() == I_CmsFormatRestriction.DIMENSION_NOT_SET) && (getOrgWidth() > maxWidth)) {
                result.setTargetWidth(maxWidth);
            }
            return result;
        }

        if (((1.00 * getTargetHeight()) / getTargetWidth()) > ((1.00 * maxHeight) / maxWidth)) {
            result.setTargetHeight(maxHeight);
            double width = (1.00 * getTargetWidth() * maxHeight) / getTargetHeight();
            result.setTargetWidth((int)Math.floor(width));
            return result;
        }
        double height = (1.00 * getTargetHeight() * maxWidth) / getTargetWidth();
        result.setTargetHeight((int)Math.floor(height));
        result.setTargetWidth(maxWidth);
        return result;
    }

    /**
     * Returns the scale parameter to this bean for a restricted maximum target size.<p>
     * 
     * @param maxHeight the max height
     * @param maxWidth the max width
     * 
     * @return the scale parameter
     */
    public String getRestrictedSizeScaleParam(int maxHeight, int maxWidth) {

        String result = toString();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
            return getRestrictedSizeParam(maxHeight, maxWidth).toString();
        }

        CmsCroppingParamBean restricted = new CmsCroppingParamBean();
        restricted.setTargetHeight(maxHeight);
        restricted.setTargetWidth(maxWidth);
        return restricted.toString();
    }

    /**
     * Returns the resulting height of the cropped image.<p>
     * 
     * @return the height
     */
    public int getResultingHeight() {

        int height = getResultingTargetHeight();
        if (height == -1) {
            if (isCropped()) {
                height = m_cropHeight;
            } else {
                height = m_orgHeight;
            }
        }
        return height;
    }

    /**
     * Returns the resulting width of the cropped image.<p>
     * 
     * @return the width
     */
    public int getResultingWidth() {

        int width = getResultingTargetWidth();
        if (width == -1) {
            if (isCropped()) {
                width = m_cropWidth;
            } else {
                width = m_orgWidth;
            }
        }
        return width;
    }

    /**
     * Returns the scale parameter.<p>
     * 
     * @return the scale parameter
     */
    public String getScaleParam() {

        if (!isScaled() && !isCropped()) {
            // the image is not cropped nor scaled, return an empty parameter
            return "";
        }
        StringBuffer result = new StringBuffer();
        if ((m_targetHeight > -1) || (m_targetWidth > -1)) {
            result.append(SCALE_PARAM_TARGETHEIGHT).append(SCALE_PARAM_COLON).append(getResultingTargetHeight()).append(
                SCALE_PARAM_DELIMITER);
            result.append(SCALE_PARAM_TARGETWIDTH).append(SCALE_PARAM_COLON).append(getResultingTargetWidth()).append(
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
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
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

        return m_cropX > I_CmsFormatRestriction.DIMENSION_NOT_SET;
    }

    /**
     * Returns if the given cropping parameters would scale the image.<p>
     *  
     * @return <code>true</code> if the image is scaled
     */
    public boolean isScaled() {

        return !(((m_targetHeight == m_orgHeight) || (m_targetHeight == -1)) && ((m_targetWidth == m_orgWidth) || (m_targetWidth == -1)));
    }

    /**
     * Resets the cropping parameters to no cropping.<p>
     */
    public void reset() {

        m_cropHeight = I_CmsFormatRestriction.DIMENSION_NOT_SET;
        m_cropWidth = I_CmsFormatRestriction.DIMENSION_NOT_SET;
        m_cropX = I_CmsFormatRestriction.DIMENSION_NOT_SET;
        m_cropY = I_CmsFormatRestriction.DIMENSION_NOT_SET;
        m_targetHeight = I_CmsFormatRestriction.DIMENSION_NOT_SET;
        m_targetWidth = I_CmsFormatRestriction.DIMENSION_NOT_SET;
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
     * Sets the used format name.<p>
     *
     * @param formatName the used format name to set
     */
    public void setFormatName(String formatName) {

        m_formatName = formatName;
    }

    /**
     * Sets the original image height.<p>
     *
     * @param orgHeight the original image height to set
     */
    public void setOrgHeight(int orgHeight) {

        m_orgHeight = orgHeight;
    }

    /**
     * Sets the original image width.<p>
     *
     * @param orgWidth the original image width to set
     */
    public void setOrgWidth(int orgWidth) {

        m_orgWidth = orgWidth;
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

        String result = getScaleParam();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
            result = SCALE_PARAM_NAME + SCALE_PARAM_EQ + result;
        }
        return result;
    }

    /**
     * Returns the resulting target height if set, otherwise '-1'.<p>
     * 
     * @return the height
     */
    private int getResultingTargetHeight() {

        int height = -1;
        if ((m_targetHeight > -1) || (m_targetWidth > -1)) {
            if (m_targetHeight > -1) {
                height = m_targetHeight;
            } else {
                height = (int)Math.floor(((1.00 * m_orgHeight) / m_orgWidth) * m_targetWidth);
            }
        }
        return height;
    }

    /**
     * Returns the resulting target width if set, otherwise '-1'.<p>
     * 
     * @return the width
     */
    private int getResultingTargetWidth() {

        int width = -1;
        if ((m_targetHeight > -1) || (m_targetWidth > -1)) {
            if (m_targetWidth > -1) {
                width = m_targetWidth;
            } else {
                width = (int)Math.floor(((1.00 * m_orgWidth) / m_orgHeight) * m_targetHeight);
            }
        }
        return width;
    }

}
