
package org.opencms.jsp.util;

import org.opencms.loader.CmsImageScaler;

/**
 * Bean containing image information for the use in JSP (for example formatters).
 */
public class CmsJspImageBean {

    /** The image URL. */
    private String m_srcUrl;

    /** The image VFS path. */
    private String m_vfsUri;

    /** The image's width in pixels. */
    private int m_width;

    /** The image's height in pixels. */
    private int m_height;

    /** The CmsImageScaler that was used to create this image. */
    private CmsImageScaler m_scaler;

    /**
     * Returns the image's height.<p>
     *
     * @return height in pixels
     */
    public int getHeight() {

        return m_height;
    }

    /**
     * Returns the basic source parameters for this image.<p>
     *
     * In case the image was cropped or otherwise manipulated,
     * the values are created for the manipulated version.<p>
     *
     * The return form is "src='(srcUrl)' height='(h)' width='(w)'".<p>
     *
     * @return the basic source parameters for this image
     */
    public String getImgSrc() {

        StringBuffer result = new StringBuffer(128);

        // append the image source
        result.append("src=\"");
        result.append(getSrcUrl());
        result.append("\"");
        // append image width and height
        result.append(" width=\"");
        result.append(m_scaler.getHeight());
        result.append("\"");
        result.append(" height=\"");
        result.append(m_scaler.getWidth());
        result.append("\"");

        return result.toString();
    }

    /**
     * Returns the image scaler that was used to create this image.<p>
     *
     * May be used to access image scaler properties in JSP.
     *
     * @return the image scaler that was used to create this image
     */
    public CmsImageScaler getScaler() {

        return m_scaler;
    }

    /**
     * Returns the image URL that may be used in img or picture tags.<p>
     *
     * @return the image URL
     */
    public String getSrcUrl() {

        return m_srcUrl;
    }

    /**
     * Returns the URI of the image in the OpenCms VFS.<p>
     *
     * @return the URI of the image in the OpenCms VFS
     */
    public String getVfsUri() {

        return m_vfsUri;
    }

    /**
     * Returns the image's original width.<p>
     *
     * To get the scaled height of the image,
     * use {@link #getScaler()}.getHeight().<p>
     *
     * @return width in pixels
     */
    public int getWidth() {

        return m_width;
    }

    /**
     * Returns <code>true</code> if the image has been scaled or otherwise processed.<p>
     *
     * @return the isScaled
     */
    public boolean isScaled() {

        return !m_scaler.isOriginalScaler();
    }

    /**
     * Sets the image's original height.<p>
     *
     * @param height  the image's width in pixels
     */
    public void setHeight(int height) {

        m_height = height;
    }

    /**
     * Sets the image scaler that was used to create this image.<p>
     *
     * @param scaler the image scaler that was used to create this image.
     */
    public void setScaler(CmsImageScaler scaler) {

        m_scaler = scaler;
        if (m_height <= 0) {
            setHeight(m_scaler.getHeight());
        }
        if (m_width <= 0) {
            setWidth(m_scaler.getWidth());
        }
    }

    /**
     * Sets the image URL.<p>
     *
     * @param srcUrl the image URL
     */
    public void setSrcUrl(String srcUrl) {

        m_srcUrl = srcUrl;
    }

    /**
     * Sets the URI of the image in the OpenCms VFS.<p>
     *
     * @param vfsUri the URI of the image in the OpenCms VFS to set
     */
    public void setVfsUri(String vfsUri) {

        m_vfsUri = vfsUri;
    }

    /**
     * Sets the image's original width.<p>
     *
     * To get the scaled width of the image,
     * use {@link #getScaler()}.getWidth().<p>
     *
     * @param width  the image's width in pixels
     */
    public void setWidth(int width) {

        m_width = width;
    }
}
