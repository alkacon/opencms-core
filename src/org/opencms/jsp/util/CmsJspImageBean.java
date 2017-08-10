
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

import java.util.HashMap;
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
                    int originalWidth = getWidth();
                    int originalHeight = getHeight();

                    if (wd == hd) {
                        // ratio is 1-1 or 2-2, meaning "square"
                        if (originalHeight < originalWidth) {
                            // set square based on height
                            newWidth = originalHeight;
                            newHeight = originalHeight;
                        } else if (originalWidth < originalHeight) {
                            // set square based on width
                            newWidth = originalWidth;
                            newHeight = originalWidth;
                        }
                    } else {
                        // target size based on width
                        double ratiod = originalWidth / wd;
                        newWidth = originalWidth;
                        newHeight = (int)Math.round(hd * ratiod);
                        if (newHeight > originalHeight) {
                            // height is too big, base target size on height
                            ratiod = originalHeight / hd;
                            newWidth = (int)Math.round(wd * ratiod);
                            newHeight = originalHeight;
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
            } catch (NumberFormatException e) {
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

    /** The CmsImageScaler that was used to create a scaled version of this image. */
    private CmsImageScaler m_scaler;

    /** The CmsImageScaler that describes the original proportions of this image. */
    private CmsImageScaler m_originalScaler;

    /** The current OpenCms user context. */
    private CmsObject m_cms = null;

    /** Lazy initialized map of ratio scaled versions of this image. */
    Map<String, CmsJspImageBean> m_scaleRatio = null;

    /**
     * Map used to store hi-DPI variants of the image.
     * <ul>
     *   <li>key: the variant multiplier, e.g. "2x" (the common retina multiplier)</li>
     *   <li>value: a CmsJspImageBean representing the hi-DPI variant</li>
     * </ul>
     */
    private Map<String, CmsJspImageBean> m_hiDpiImages = null;

    /** The CmsResource for this image. */
    CmsResource m_resource = null;

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
        // set original scaler
        setOriginal(originalScaler);

        // set target scaler
        CmsImageScaler targetScaler = originalScaler;
        if (scaleParam != null) {
            // scale parameters have been set
            targetScaler = new CmsImageScaler(scaleParam);
        }
        setScaler(targetScaler);
    }

    /**
     * Initializes a new image bean based on a VFS input string and applies additional re-scaling.<p>
     *
     * The input string is is used to read the images from the VFS.
     * It can contain scaling parameters.
     * The additional re-scaling is then applied to the image that has been read.<p>
     *
     * @param cms the current uses OpenCms context
     * @param imageUri the URI to read the image from in the OpenCms VFS, may also contain scaling parameters
     * @param targetScaler the additional re-scaling to apply to the image
     *
     * @throws CmsException in case of problems reading the image from the VFS
     */
    public CmsJspImageBean(CmsObject cms, String imageUri, CmsImageScaler targetScaler)
    throws CmsException {

        this(cms, imageUri);

        if ((targetScaler.getHeight() > 0) || (targetScaler.getWidth() > 0)) {
            // NOT the same as targetScaler.isValid()
            // one provided dimension is enough here

            String scaleParams = getScaler().getRequestParam();

            if ((scaleParams != null) && !"undefined".equals(scaleParams)) {
                CmsImageScaler cropScaler = null;
                // use cropped image as a base for scaling
                cropScaler = new CmsImageScaler(scaleParams);
                if (targetScaler.getType() == 5) {
                    // must reset height / width parameters in crop scaler for type 5
                    cropScaler.setWidth(cropScaler.getCropWidth());
                    cropScaler.setHeight(cropScaler.getCropHeight());
                }
                targetScaler = cropScaler.getCropScaler(targetScaler);
            }

            int width = targetScaler.getWidth();
            int height = targetScaler.getHeight();

            // If either width or height is not set, the CmsImageScaler will have a problem. So the
            // missing dimension is calculated with the given dimension and the original image's
            // aspect ratio (or the respective crop aspect ratio).
            if ((width <= 0) || (height <= 0)) {
                float ratio;
                // use the original width/height or the crop with/height for aspect ratio calculation
                if (!targetScaler.isCropping()) {
                    ratio = (float)getWidth() / (float)getHeight();
                } else {
                    ratio = (float)targetScaler.getCropWidth() / (float)targetScaler.getCropHeight();
                }
                if (width <= 0) {
                    // width is not set, calculate it with the given height and the original/crop aspect ratio
                    width = Math.round(height * ratio);
                    targetScaler.setWidth(width);
                } else if (height <= 0) {
                    // height is not set, calculate it with the given width and the original/crop aspect ratio
                    height = Math.round(width / ratio);
                    targetScaler.setHeight(height);
                }
            }

            // calculate target scale dimensions (if required)
            if (((targetScaler.getHeight() <= 0) || (targetScaler.getWidth() <= 0))
                || ((targetScaler.getType() == 5) && targetScaler.isValid() && !targetScaler.isCropping())) {
                // read the image properties for the selected resource
                targetScaler = getOriginal().getReScaler(targetScaler);
            }

            setScaler(targetScaler);
        }
    }

    /**
     * Initializes a new empty image bean.<p>
     *
     * All values must be set with setters later.<p>
     */
    protected CmsJspImageBean() {
        // all values must be set with setters later
    }

    /**
     * adds a CmsJspImageBean as hi-DPI variant to this image
     * @param factor the variant multiplier, e.g. "2x" (the common retina multiplier)
     * @param image the image to be used for this variant
     */
    public void addHiDpiImage(String factor, CmsJspImageBean image) {

        if (m_hiDpiImages == null) {
            m_hiDpiImages = new HashMap<>();
        }
        m_hiDpiImages.put(factor, image);
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
     * Returns the original (unscaled) height of the image.<p>
     *
     * @return the original (unscaled) height of the image
     */
    public int getHeight() {

        return m_originalScaler.getHeight();
    }

    /**
     * Returns the map containing all hi-DPI variants of this image.
     * @return Map containing the hi-DPI variants of the image.
     * <ul>
     *   <li>key: the variant multiplier, e.g. "2x" (the common retina multiplier)</li>
     *   <li>value: a CmsJspImageBean representing the hi-DPI variant</li>
     * </ul>
     */
    public Map<String, CmsJspImageBean> getHiDpiImages() {

        return m_hiDpiImages;
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
     * Returns the image scaler that describes the original proportions of this image.<p>
     *
     * @return the image scaler that describes the original proportions of this image
     */
    public CmsImageScaler getOriginal() {

        return m_originalScaler;
    }

    /**
     * Returns the image scaler that is used for the scaled version of this image.<p>
     *
     * @return the image scaler that is used for the scaled version of this image
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
     * Returns a variation of the current image scaled with the given scaler.<p>
     *
     * It is always the original image which is used as a base, never a scaled version.
     * So for example if the image has been cropped by the user, the cropping are is ignored.
     *
     * @param scaler contains the information about how to scale the image
     *
     * @return a variation of the current image scaled with the given scaler
     */
    public CmsJspImageBean getVariation(CmsImageScaler scaler) {

        CmsJspImageBean result = getClone();
        if (scaler.isValid()) {
            result.setScaler(scaler);
        }
        return result;
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
     * Returns the original (unscaled) width of the image.<p>
     *
     * @return the original (unscaled) width of the image
     */
    public int getWidth() {

        return m_originalScaler.getWidth();
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
     * Sets the scaler that describes the original proportions of this image.<p>
     *
     * @param originalScaler the scaler that describes the original proportions of this image
     */
    public void setOriginal(CmsImageScaler originalScaler) {

        m_originalScaler = originalScaler;
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
        result.setOriginal(getOriginal());
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