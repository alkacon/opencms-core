
package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUriSplitter;

import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

/**
 * Bean containing image information for the use in JSP (for example formatters).
 */
public class CmsJspImageBean {

    /**
     * Provides a Map to access to ratio scaled of the current image bean.<p>
     */
    public class CmsScaleRatioTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        @Override
        public Object transform(Object input) {

            String ratio = (String)input;
            CmsJspImageBean result = null;

            try {
                int i = ratio.indexOf('-');
                if (i > 0) {
                    ratio = ratio.replace(',', '.');

                    double wd = Double.valueOf(ratio.substring(0, i)).doubleValue();
                    double hd = Double.valueOf(ratio.substring(i + 1)).doubleValue();

                    int newWidth = -1;
                    int newHeight = -1;
                    int imageWidth = getWidth();
                    int imageHeight = getHeight();

                    if (wd == hd) {
                        // ratio is 1-1 or 2-2, meaning "square"
                        if (imageHeight < imageWidth) {
                            newWidth = imageHeight;
                            newHeight = imageHeight;
                        } else if (imageWidth < imageHeight) {
                            newWidth = imageWidth;
                            newHeight = imageWidth;
                        }
                    } else {
                        double ratiod = imageWidth / wd;
                        newWidth = imageWidth;
                        newHeight = (int)Math.round(hd * ratiod);
                        if (newHeight > imageHeight) {
                            // height is too big, use width instead
                            ratiod = imageHeight / hd;
                            newWidth = (int)Math.round(wd * ratiod);
                            newHeight = imageHeight;
                        }
                    }

                    result = getClone();

                    if (newHeight > -1) {
                        // may be -1 if image is square and square ratio like '1-1' is requested
                        // in this case the image already square and not scaled
                        CmsImageScaler scaler = new CmsImageScaler();
                        scaler.setHeight(newHeight);
                        scaler.setWidth(newWidth);
                        scaler.setType(2);
                        result.setScaler(scaler);
                    }
                }
            } catch (Exception e) {
                LOG.info(e.getLocalizedMessage(), e);
            }

            return result;
        }
    }

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsJspImageBean.class);

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

    /** The current OpenCms user context. */
    private CmsObject m_cms = null;

    /** Lazy initialized map of ratio scaled versions of this image. */
    Map<String, CmsJspImageBean> m_scaleRatio = null;

    /** The CmsResource for this image. */
    CmsResource m_resource = null;

    /**
     * Initializes a new empty image bean.<p>
     *
     * All values must be set with setters later.<p>
     */
    public CmsJspImageBean() {
        // all values must be set with setters later
    }

    /**
     * Initializes a new image bean based on a VFS input string.<p>
     *
     * The input string is is used to read the images from the VFS.
     * It can contain scaling parameters.<p>
     *
     * @param cms the current uses OpenCms context
     * @param imageUri the URI to read the image from in the OpenCms VFS, may also contain scaling parameters
     *
     * @throws CmsException in case of problems reading the image from the VFS
     */
    public CmsJspImageBean(CmsObject cms, String imageUri)
    throws CmsException {

        setCmsObject(cms);
        // split the given image URI to see if there are scaling parameters attached
        CmsUriSplitter splitSrc = new CmsUriSplitter(imageUri);
        String scaleParam = null;
        if (splitSrc.getQuery() != null) {
            // check if the original URI already has parameters, this is true if original has been cropped
            String[] scaleStr = CmsRequestUtil.createParameterMap(splitSrc.getQuery()).get(CmsImageScaler.PARAM_SCALE);
            if (scaleStr != null) {
                scaleParam = scaleStr[0];
            }
        }

        // set VFS URI without scaling parameters
        setVfsUri(splitSrc.getPrefix());
        setResource(cms.readResource(getVfsUri()));

        // the originalScaler reads the image dimensions from the VFS properties
        CmsImageScaler originalScaler = new CmsImageScaler(cms, getResource());
        // set original height and width
        setHeight(originalScaler.getHeight());
        setWidth(originalScaler.getWidth());

        // set target scaler
        CmsImageScaler targetScaler = originalScaler;
        if (scaleParam != null) {
            // scale parameters have been set
            targetScaler = new CmsImageScaler(scaleParam);
        }
        setScaler(targetScaler);
    }

    /**
     * Returns the current OpenCms user context.<p>
     *
     * @return the current OpenCms user context
     */
    public CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Returns the image's original height.<p>
     *
     * To get the scaled height of the image,
     * use {@link #getScaler()}.getHeight().<p>
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
        result.append(m_scaler.getWidth());
        result.append("\"");
        result.append(" height=\"");
        result.append(m_scaler.getHeight());
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
     * Returns a lazy initialized Map that provides access to ratio scaled instances of the current image bean.<p>
     *
     * @return a lazy initialized Map that provides access to ratio scaled instances of the current image bean
     */
    public Map<String, CmsJspImageBean> getScaleRatio() {

        if (m_scaleRatio == null) {
            m_scaleRatio = CmsCollectionsGenericWrapper.createLazyMap(new CmsScaleRatioTransformer());
        }
        return m_scaleRatio;
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
     * use {@link #getScaler()}.getWidth().<p>
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
     * Sets the current OpenCms user context.<p>
     *
     * @param cms the current OpenCms user context to set
     */
    public void setCmsObject(CmsObject cms) {

        m_cms = cms;
    }

    /**
     *
     * Sets the image's height.<p>
     *
     * @param height the image's height in pixels
     */
    public void setHeight(int height) {

        m_height = height;
    }

    /**
     * Sets the CmsResource for this image.<p>
     *
     * @param resource the CmsResource for this image
     */
    public void setResource(CmsResource resource) {

        m_resource = resource;
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
        String imageSrc = getCmsObject().getSitePath(getResource());
        if (m_scaler.isValid() && !m_scaler.isOriginalScaler()) {
            // now append the scaler parameters if required
            imageSrc += m_scaler.toRequestParam();
        }
        setSrcUrl(OpenCms.getLinkManager().substituteLink(getCmsObject(), imageSrc));
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
     * Sets the image's width.<p>
     *
     * @param width the image's width in pixels
     */
    public void setWidth(int width) {

        m_width = width;
    }

    /**
     * Returns a clone of this image scaled bean.<p>
     *
     * @return a clone of this image scaled bean
     *
     * @see java.lang.Object#clone()
     */
    protected CmsJspImageBean getClone() {

        CmsJspImageBean result = new CmsJspImageBean();
        result.setCmsObject(getCmsObject());
        result.setResource(getResource());
        result.setHeight(getHeight());
        result.setWidth(getWidth());
        result.setVfsUri(getVfsUri());
        result.setScaler(getScaler());
        return result;
    }

    /**
     * Returns the CmsResource for this image.<p>
     *
     * @return the CmsResource for this image
     */
    protected CmsResource getResource() {

        return m_resource;
    }
}