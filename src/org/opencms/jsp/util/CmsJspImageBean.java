
package org.opencms.jsp.util;

import org.opencms.loader.CmsImageScaler;

/**
 * Bean containing image information for the use in JSP (e.g. formatters)
 */
public class CmsJspImageBean {

    /** The image URL */
    private String m_srcUrl;

    /** The image's width in pixels */
    private int m_width;

    /** The image's height in pixels */
    private int m_height;

    /** The CmsImageScaler that was used to create this image */
    private CmsImageScaler m_scaler;

    /**
     * Returns the image's height
     * @return height in pixels
     */
    public int getHeight() {

        return m_height;
    }

    /**
     * Returns the image scaler that was used to create this image. May be used to access image scaler properties in JSP.
     * @return the image scaler that was used to create this image
     */
    public CmsImageScaler getScaler() {

        return m_scaler;
    }

    /**
     * Returns the image URL that may be used in img or picture tags
     * @return the image URL
     */
    public String getSrcUrl() {

        return m_srcUrl;
    }

    /**
     * Returns the image's width
     * @return width in pixels
     */
    public int getWidth() {

        return m_width;
    }

    /**
     * Sets the image's height
     * @param height  the image's width in pixels
     */
    public void setHeight(int height) {

        m_height = height;
    }

    /**
     * Sets the image scaler that was used to create this image.
     * @param scaler the image scaler that was used to create this image.
     */
    public void setScaler(CmsImageScaler scaler) {

        m_scaler = scaler;
    }

    /**
     * Sets the image URL
     * @param srcUrl the image URL
     */
    public void setSrcUrl(String srcUrl) {

        m_srcUrl = srcUrl;
    }

    /**
     * Sets the image's width
     * @param width  the image's width in pixels
     */
    public void setWidth(int width) {

        m_width = width;
    }

}
