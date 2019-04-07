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

package org.opencms.jsp.util;

import org.opencms.ade.galleries.shared.CmsPoint;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspResourceWrapper;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUriSplitter;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

/**
 * Bean containing image information for the use in JSP (for example formatters).
 */
public class CmsJspImageBean {

    /**
     * Provides a Map to access hi-DPI versions of the current image.<p>
     */
    public class CmsScaleHiDpiTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        @Override
        public Object transform(Object input) {

            return createHiDpiVariation(String.valueOf(input));
        }
    }

    /**
     * Provides a Map to access ratio scaled versions of the current image.<p>
     */
    public class CmsScaleRatioTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        @Override
        public Object transform(Object input) {

            return createRatioVariation(String.valueOf(input));
        }
    }

    /**
     * Provides a Map to access width scaled versions of the current image.<p>
     */
    public class CmsScaleWidthTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        @Override
        public Object transform(Object input) {

            return createWidthVariation(String.valueOf(input));
        }
    }

    /** The minimum dimension (width and height) a generated image must have. */
    public static int MIN_DIMENSION = 4;

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsJspImageBean.class);

    /** Size variations for source sets. */
    static final double[] m_sizeVariants = {1.000, 0.7500, 0.5000, 0.3750, 0.2500, 0.1250};

    /** The wrapped VFS resource for this image. */
    CmsJspResourceWrapper m_resource = null;

    /** Lazy initialized map of ratio scaled versions of this image. */
    Map<String, CmsJspImageBean> m_scaleRatio = null;

    /** Lazy initialized map of width scaled versions of this image. */
    Map<String, CmsJspImageBean> m_scaleWidth = null;

    /** Map used for creating a image source set. */
    TreeMap<Integer, CmsJspImageBean> m_srcSet = null;

    /** The CmsImageScaler that describes the basic adjustments (usually cropping) that have been set on the original image. */
    private CmsImageScaler m_baseScaler;

    /** The current OpenCms user context. */
    private CmsObject m_cms = null;

    /** The CmsImageScaler that is used to create a scaled version of this image. */
    private CmsImageScaler m_currentScaler;

    /**
     * Map used to store hi-DPI variants of the image.
     * <ul>
     *   <li>key: the variant multiplier, e.g. "2x" (the common retina multiplier)</li>
     *   <li>value: a CmsJspImageBean representing the hi-DPI variant</li>
     * </ul>
     */
    private Map<String, CmsJspImageBean> m_hiDpiImages = null;

    /** The CmsImageScaler that describes the original pixel proportions of this image. */
    private CmsImageScaler m_originalScaler;

    /** The image quality (for JPEG calculation). */
    private int m_quality = 0;

    /** The image VFS path. */
    private String m_vfsUri;

    /** The ratio of the image, width to height, for example '4-3' or '16-9'. */
    private String m_ratio;

    /** The height percentage of the image relative to the image width. */
    private String m_ratioHeightPercentage;

    /**
     * Initializes a new image bean based on a VFS resource and optional scaler parameters.<p>
     *
     * @param cms the current OpenCms user context
     * @param imageRes the VFS resource to read the image from
     * @param scaleParams optional scaler parameters to apply to the VFS resource
     */
    public CmsJspImageBean(CmsObject cms, CmsResource imageRes, String scaleParams) {

        init(cms, imageRes, scaleParams);
    }

    /**
     * Initializes a new image bean based on a string pointing to a VFS resource that may contain appended scaling parameters.<p>
     *
     * @param cms the current OpenCms user context
     * @param imageUri the URI to read the image from in the OpenCms VFS, may also contain appended scaling parameters
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

        init(cms, cms.readResource(splitSrc.getPrefix()), scaleParam);
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
     * @param initScaler the additional re-scaling to apply to the image
     *
     * @throws CmsException in case of problems reading the image from the VFS
     */
    public CmsJspImageBean(CmsObject cms, String imageUri, CmsImageScaler initScaler)
    throws CmsException {

        this(cms, imageUri);

        CmsImageScaler targetScaler = createVariation(
            getWidth(),
            getHeight(),
            getBaseScaler(),
            initScaler.getWidth(),
            initScaler.getHeight(),
            getQuality());

        if ((targetScaler != null) && targetScaler.isValid()) {
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
     * Create a variation scaler fir this image.<p>
     *
     * @param originalWidth the original image pixel width
     * @param originalHeight the original image pixel height
     * @param baseScaler the base scaler that may contain crop parameters
     * @param targetWidth the target image pixel width
     * @param targetHeight the target image pixel height
     * @param quality the compression quality factor to use for image generation
     *
     * @return the created variation scaler for this image
     */
    protected static CmsImageScaler createVariation(
        int originalWidth,
        int originalHeight,
        CmsImageScaler baseScaler,
        int targetWidth,
        int targetHeight,
        int quality) {

        CmsImageScaler result = null;

        if ((targetWidth <= 0) || (targetHeight <= 0)) {
            // not all dimensions have been given, calculate the missing

            double baseRatio;
            if (baseScaler.isCropping()) {
                // use the image crop with/height for aspect ratio calculation
                baseRatio = (double)baseScaler.getCropWidth() / (double)baseScaler.getCropHeight();
            } else {
                // use the image original pixel width/height for aspect ratio calculation
                baseRatio = (double)originalWidth / (double)originalHeight;
            }

            // one dimension is missing, calculate it from the image
            if (targetWidth <= 0) {
                // width is not set, calculate it with the given height and the aspect ratio
                targetWidth = (int)Math.round(targetHeight * baseRatio);
            } else if (targetHeight <= 0) {
                // height is not set, calculate it with the given width and the aspect ratio
                targetHeight = (int)Math.round(targetWidth / baseRatio);
            }
        }

        if ((targetWidth >= MIN_DIMENSION)
            && (targetHeight >= MIN_DIMENSION)
            && (originalWidth >= targetWidth)
            && (originalHeight >= targetHeight)) {

            // image original dimensions are large enough, generate result scaler
            result = new CmsImageScaler();

            result.setWidth(targetWidth);
            result.setHeight(targetHeight);

            if ((baseScaler.getFocalPoint() != null)
                && checkCropRegionContainsFocalPoint(baseScaler, baseScaler.getFocalPoint())) {
                result.setType(8);
                if (baseScaler.isCropping()) {
                    result.setCropArea(
                        baseScaler.getCropX(),
                        baseScaler.getCropY(),
                        baseScaler.getCropWidth(),
                        baseScaler.getCropHeight());
                } else {
                    result.setCropArea(0, 0, originalWidth, originalHeight);
                }
            } else {

                result.setType(2);
                if (baseScaler.isCropping()) {

                    double targetRatio = (double)baseScaler.getCropWidth() / (double)targetWidth;
                    int targetCropWidth = baseScaler.getCropWidth();
                    int targetCropHeight = (int)Math.round(targetHeight * targetRatio);

                    if (targetCropHeight > baseScaler.getCropHeight()) {
                        targetRatio = (double)baseScaler.getCropHeight() / (double)targetHeight;
                        targetCropWidth = (int)Math.round(targetWidth * targetRatio);
                        targetCropHeight = baseScaler.getCropHeight();
                    }

                    int targetX = baseScaler.getCropX();
                    int targetY = baseScaler.getCropY();

                    if (targetCropWidth != baseScaler.getCropWidth()) {
                        targetX = targetX + (int)Math.round((baseScaler.getCropWidth() - targetCropWidth) / 2.0);
                    }
                    if (targetCropHeight != baseScaler.getCropHeight()) {
                        targetY = targetY + (int)Math.round((baseScaler.getCropHeight() - targetCropHeight) / 2.0);
                    }

                    result.setCropArea(targetX, targetY, targetCropWidth, targetCropHeight);
                }
            }
        }

        if ((result != null) && (quality > 0)) {
            // apply compression quality setting
            result.setQuality(quality);
        }
        return result;
    }

    /**
     * Helper method to check whether the focal point in the scaler is contained in the scaler's crop region.<p>
     *
     * If the scaler has no crop region, true is returned.
     *
     * @param scaler the scaler
     * @param focalPoint the focal point to check
     * @return true if the scaler's crop region contains the focal point
     */
    private static boolean checkCropRegionContainsFocalPoint(CmsImageScaler scaler, CmsPoint focalPoint) {

        if (!scaler.isCropping()) {
            return true;
        }
        double x = focalPoint.getX();
        double y = focalPoint.getY();
        return (scaler.getCropX() <= x)
            && (x < (scaler.getCropX() + scaler.getCropWidth()))
            && (scaler.getCropY() <= y)
            && (y < (scaler.getCropY() + scaler.getCropHeight()));
    }

    /**
     * adds a CmsJspImageBean as hi-DPI variant to this image
     * @param factor the variant multiplier, e.g. "2x" (the common retina multiplier)
     * @param image the image to be used for this variant
     */
    public void addHiDpiImage(String factor, CmsJspImageBean image) {

        if (m_hiDpiImages == null) {
            m_hiDpiImages = CmsCollectionsGenericWrapper.createLazyMap(new CmsScaleHiDpiTransformer());
        }
        m_hiDpiImages.put(factor, image);
    }

    /**
     * Adds a number of size variations to the source set.<p>
     *
     * In case the screen size is not really known, it may be a good idea to add
     * some variations for large images to make sure there are some common options in case the basic
     * image is very large.<p>
     *
     * @param minWidth the minimum image width to add size variations for
     * @param maxWidth the maximum width size variation to create
     */
    public void addSrcSetWidthVariants(int minWidth, int maxWidth) {

        int imageWidth = getWidth();
        if (imageWidth > minWidth) {
            // only add variants in case the image is larger then the given minimum
            int srcSetMaxWidth = getSrcSetMaxWidth();
            for (double factor : m_sizeVariants) {
                long width = Math.round(imageWidth * factor);
                if (width > srcSetMaxWidth) {
                    if (width <= maxWidth) {
                        setSrcSets(createWidthVariation(String.valueOf(width)));
                    }
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Creates a hi-DPI scaled version of the current image.<p>
     *
     * @param hiDpiStr the hi-DPI variation to generate, for example "2.5x".<p>
     *
     * @return a hi-DPI scaled version of the current image
     */
    public CmsJspImageBean createHiDpiVariation(String hiDpiStr) {

        CmsJspImageBean result = null;
        if (hiDpiStr.matches("^[0-9]+(.[0-9]+)?x$")) {

            double multiplier = Double.valueOf(hiDpiStr.substring(0, hiDpiStr.length() - 1)).doubleValue();

            int targetWidth = (int)Math.round(getScaler().getWidth() * multiplier);
            int targetHeight = (int)Math.round(getScaler().getHeight() * multiplier);

            CmsImageScaler targetScaler = createVariation(
                getWidth(),
                getHeight(),
                getBaseScaler(),
                targetWidth,
                targetHeight,
                getQuality());

            if (targetScaler != null) {
                result = createVariation(targetScaler);
            }

        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("Illegal multiplier format: '%s' not usable for image scaling", hiDpiStr));
            }
        }
        return result;
    }

    /**
     * Creates a ratio scaled version of the current image.<p>
     *
     * @param ratioStr the rato variation to generate, for example "4-3" or "1-1".<p>
     *
     * @return a ratio scaled version of the current image
     */
    public CmsJspImageBean createRatioVariation(String ratioStr) {

        CmsJspImageBean result = null;

        try {
            int i = ratioStr.indexOf('-');
            if (i > 0) {
                ratioStr = ratioStr.replace(',', '.');

                double ratioW = Double.valueOf(ratioStr.substring(0, i)).doubleValue();
                double ratioH = Double.valueOf(ratioStr.substring(i + 1)).doubleValue();

                int targetWidth, targetHeight;

                double ratioFactorW = getScaler().getWidth() / ratioW;
                targetWidth = getScaler().getWidth();
                targetHeight = (int)Math.round(ratioH * ratioFactorW);

                if (targetHeight > getScaler().getHeight()) {
                    double ratioFactorH = getScaler().getHeight() / ratioH;
                    targetWidth = (int)Math.round(ratioW * ratioFactorH);
                    targetHeight = getScaler().getHeight();
                }

                CmsImageScaler targetScaler = createVariation(
                    getWidth(),
                    getHeight(),
                    getBaseScaler(),
                    targetWidth,
                    targetHeight,
                    getQuality());

                if (targetScaler != null) {
                    result = createVariation(targetScaler);
                    result.m_ratio = ratioStr;
                    result.m_ratioHeightPercentage = calcRatioHeightPercentage(ratioW, ratioH);
                }
            }
        } catch (NumberFormatException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("Illegal ratio format: '%s' not usable for image scaling", ratioStr));
            }
        }

        return result;
    }

    /**
     * Creates a width scaled version of the current image.<p>
     *
     * @param widthStr the with variation to generate, for example "1078" or "800".<p>
     *
     * @return a width scaled version of the current image
     */
    public CmsJspImageBean createWidthVariation(String widthStr) {

        CmsJspImageBean result = null;

        try {

            double baseRatio;
            if ((getOriginalScaler().getFocalPoint() != null)
                && checkCropRegionContainsFocalPoint(getScaler(), getOriginalScaler().getFocalPoint())) {
                // We use scaling mode 8 if there is a focal point, and in this case,
                // the correct aspect ratio is width x height, not cropWidth x cropHeight
                // even if cropping is set
                baseRatio = (double)getScaler().getWidth() / (double)getScaler().getHeight();
            } else if (getScaler().isCropping()) {
                // use the image crop with/height for aspect ratio calculation
                baseRatio = (double)getScaler().getCropWidth() / (double)getScaler().getCropHeight();
            } else {
                // use the image original pixel width/height for aspect ratio calculation
                baseRatio = (double)getScaler().getWidth() / (double)getScaler().getHeight();
            }

            // height is not set, calculate it with the given width and the aspect ratio
            int targetWidth = Integer.valueOf(widthStr).intValue();
            int targetHeight = (int)Math.round(targetWidth / baseRatio);

            CmsImageScaler targetScaler = createVariation(
                getWidth(),
                getHeight(),
                getBaseScaler(),
                targetWidth,
                targetHeight,
                getQuality());

            if (targetScaler != null) {
                result = createVariation(targetScaler);
            }

        } catch (NumberFormatException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("Illegal width format: '%s' not usable for image scaling", widthStr));
            }
        }

        return result;
    }

    /**
     * Returns the original pixel height of the image.<p>
     *
     * @return the original pixel height of the image
     */
    public int getHeight() {

        return m_originalScaler.getHeight();
    }

    /**
     * Returns a lazy initialized Map that provides access to ratio scaled instances of this image bean.<p>
     *
     * @return a lazy initialized Map that provides access to ratio scaled instances of this image bean
     *
     * @deprecated use {@link #getScaleHiDpi()} instead
     */
    @Deprecated
    public Map<String, CmsJspImageBean> getHiDpiImages() {

        return getScaleHiDpi();
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
        result.append(m_currentScaler.getWidth());
        result.append("\"");
        result.append(" height=\"");
        result.append(m_currentScaler.getHeight());
        result.append("\"");

        return result.toString();
    }

    /**
     * Returns the compression quality factor used for image generation.<p>
     *
     * @return the compression quality factor used for image generation
     */
    public int getQuality() {

        return m_quality;
    }

    /**
     * Returns the image ratio.<p>
     *
     * The ratio is in the form 'width-height', for example '4-3' or '16-9'.
     * In case no ratio was set, the pixel dimensions of the image are returned.<p>
     *
     *  @return the image ratio
     */
    public String getRatio() {

        if (m_ratio == null) {
            m_ratio = "" + getScaler().getWidth() + "-" + getScaler().getHeight();
        }
        return m_ratio;
    }

    /**
     * Returns the image height percentage relative to the image width as a String.<p>
     *
     * In case a ratio has been used to scale the image, the height percentage is
     * calculated based on the ratio, not on the actual image pixel size.
     * This is done to avoid rounding differences.<p>
     *
     *  @return the image height percentage relative to the image width
     */
    public String getRatioHeightPercentage() {

        if (m_ratioHeightPercentage == null) {

            m_ratioHeightPercentage = calcRatioHeightPercentage(getScaler().getWidth(), getScaler().getHeight());
        }
        return m_ratioHeightPercentage;
    }

    /**
     * Returns the JSP access wrapped VFS resource for this image.<p>
     *
     * @return the JSP access wrapped VFS resource for this image
     */
    public CmsJspResourceWrapper getResource() {

        return m_resource;
    }

    /**
     * Returns a lazy initialized Map that provides access to hi-DPI scaled instances of this image bean.<p>
     *
     * <ul>
     *   <li>key: the variant multiplier, e.g. "2x" (the common retina multiplier)</li>
     *   <li>value: a CmsJspImageBean representing the hi-DPI variant</li>
     * </ul>
     *
     * @return a lazy initialized Map that provides access to hi-DPI scaled instances of this image bean
     */
    public Map<String, CmsJspImageBean> getScaleHiDpi() {

        if (m_hiDpiImages == null) {
            m_hiDpiImages = CmsCollectionsGenericWrapper.createLazyMap(new CmsScaleHiDpiTransformer());
        }
        return m_hiDpiImages;
    }

    /**
     * Returns the image scaler that is used for the scaled version of this image bean.<p>
     *
     * @return the image scaler that is used for the scaled version of this image bean
     */
    public CmsImageScaler getScaler() {

        return m_currentScaler;
    }

    /**
     * Returns a lazy initialized Map that provides access to ratio scaled instances of this image bean.<p>
     *
     * @return a lazy initialized Map that provides access to ratio scaled instances of this image bean
     */
    public Map<String, CmsJspImageBean> getScaleRatio() {

        if (m_scaleRatio == null) {
            m_scaleRatio = CmsCollectionsGenericWrapper.createLazyMap(new CmsScaleRatioTransformer());
        }
        return m_scaleRatio;
    }

    /**
     * Returns a lazy initialized Map that provides access to width scaled instances of this image bean.<p>
     *
     * @return a lazy initialized Map that provides access to width scaled instances of this image bean
     */
    public Map<String, CmsJspImageBean> getScaleWidth() {

        if (m_scaleWidth == null) {
            m_scaleWidth = CmsCollectionsGenericWrapper.createLazyMap(new CmsScaleWidthTransformer());
        }
        return m_scaleWidth;
    }

    /**
     * Generates a srcset attribute parameter list from all images added to this image bean.<p>
     *
     * @return a srcset attribute parameter list from all images added to this image bean
     */
    public String getSrcSet() {

        StringBuffer result = new StringBuffer(128);
        if (m_srcSet != null) {
            int items = m_srcSet.size();
            for (Map.Entry<Integer, CmsJspImageBean> entry : m_srcSet.entrySet()) {
                CmsJspImageBean imageBean = entry.getValue();
                // append the image source
                result.append(imageBean.getSrcUrl());
                result.append(" ");
                // append width
                result.append(imageBean.getScaler().getWidth());
                result.append("w");
                if (--items > 0) {
                    result.append(", ");
                }
            }
        }
        return result.toString();
    }

    /**
     * Generates a srcset attribute parameter for this image bean.<p>
     *
     * @return a srcset attribute parameter for this image bean
     */
    public String getSrcSetEntry() {

        StringBuffer result = new StringBuffer(128);
        if (m_currentScaler.isValid()) {
            // append the image source
            result.append(getSrcUrl());
            result.append(" ");
            // append width
            result.append(m_currentScaler.getWidth());
            result.append("w");
        }
        return result.toString();
    }

    /**
     * Returns the source set map.<p>
     *
     * In case no source set entries have been added before, the map is not initialized and <code>null</code> is returned.
     *
     * @return the source set map
     */
    public Map<Integer, CmsJspImageBean> getSrcSetMap() {

        return m_srcSet;
    }

    /**
     * Returns the largest image from the generated source set.<p>
     *
     * In case the source set has not been initialized,
     * it returns the instance itself.
     *
     * @return the largest image from the generated source set
     */
    public CmsJspImageBean getSrcSetMaxImage() {

        CmsJspImageBean result = this;
        if (m_srcSet != null) {
            result = m_srcSet.lastEntry().getValue();
        }
        return result;
    }

    /**
     * Returns the largest width value form the source set.<p>
     *
     * In case no source set entries have been added before, the map is not initialized and <code>0</code> is returned.
     *
     * @return the largest width value form the source set
     */
    public int getSrcSetMaxWidth() {

        int result = 0;
        if ((m_srcSet != null) && (m_srcSet.size() > 0)) {

            result = m_srcSet.lastKey().intValue();
        }
        return result;
    }

    /**
     * Getter for {@link #setSrcSets(CmsJspImageBean)} which returns this image bean.<p>
     *
     * Exists to make sure {@link #setSrcSets(CmsJspImageBean)} is available as property on a JSP.<p>
     *
     * @return this image bean
     *
     * @see CmsJspImageBean#getSrcSet()
     * @see CmsJspImageBean#getSrcSetMap()
     */
    public CmsJspImageBean getSrcSets() {

        return this;
    }

    /**
     * Returns the image URL that may be used in img or picture tags.<p>
     *
     * @return the image URL
     */
    public String getSrcUrl() {

        String imageSrc = getCmsObject().getSitePath(getResource());
        if ((getScaler() != null) && getScaler().isValid()) {
            // now append the scaler parameters if required
            imageSrc += getScaler().toRequestParam();
        }
        return OpenCms.getLinkManager().substituteLink(getCmsObject(), imageSrc);
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
     * Returns <code>true</code> if this image bean has been correctly initialized with an image VFS resource.<p>
     *
     * @return <code>true</code> if this image bean has been correctly initialized with an image VFS resource
     */
    public boolean isImage() {

        return getOriginalScaler().isValid();
    }

    /**
     * Returns <code>true</code> if the image has been scaled or otherwise processed.<p>
     *
     * @return <code>true</code> if the image has been scaled or otherwise processed
     */
    public boolean isScaled() {

        return !m_currentScaler.isOriginalScaler();
    }

    /**
     * Sets the compression quality factor to use for image generation.<p>
     *
     * @param quality the compression quality factor to use for image generation
     */
    public void setQuality(int quality) {

        m_quality = quality;
        getScaler().setQuality(m_quality);
    }

    /**
     * Adjusts the quality settings for all image beans in the srcSet depending on the pixel count.<p>
     *
     * The idea is to make sure large pixel images use a higher JPEG compression in order to reduce the size.<p>
     *
     * The following quality settings are used depending on the image size:
     * <ul>
     * <li>larger then 1200 * 800: quality 75
     * <li>larger then 1024 * 768: quality 80
     * <li>otherwise: quality 85
     * </ul>
     *
     */
    public void setSrcSetQuality() {

        if (m_srcSet != null) {

            for (Map.Entry<Integer, CmsJspImageBean> entry : m_srcSet.entrySet()) {
                CmsJspImageBean imageBean = entry.getValue();
                int quality;
                long pixel = imageBean.getScaler().getWidth() * imageBean.getScaler().getWidth();
                if (pixel > 960000) {
                    // image size 1200 * 800
                    quality = 75;
                } else if (pixel > 786432) {
                    // image size 1024 * 768
                    quality = 80;
                } else {
                    quality = 85;
                }
                imageBean.setQuality(quality);
            }
        }
    }

    /**
     * Adds another image bean instance to the source set map of this bean.<p>
     *
     * @param imageBean the image bean to add
     */
    public void setSrcSets(CmsJspImageBean imageBean) {

        if (m_srcSet == null) {
            m_srcSet = new TreeMap<Integer, CmsJspImageBean>();
        }
        if ((imageBean != null) && imageBean.getScaler().isValid()) {
            m_srcSet.put(Integer.valueOf(imageBean.getScaler().getWidth()), imageBean);
        }
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
     * Returns the image source URL as String representation.<p>
     *
     * @return the image source URL
     *
     * @see #getSrcUrl()
     */
    @Override
    public String toString() {

        return getSrcUrl();
    }

    /**
     * Returns the ratio height percentage of an image based on width and height.<p>
     *
     * @param width width to calculate percentage from
     * @param height height to calculate percentage from
     *
     * @return the ratio height percentage of an image based on width and height
     */
    protected String calcRatioHeightPercentage(double width, double height) {

        double p = Math.round((height / width) * 10000000.0) / 100000.0;
        return String.valueOf(p) + "%";
    }

    /**
     * Returns a variation of the current image scaled with the given scaler.<p>
     *
     * It is always the original image which is used as a base, never a scaled version.
     * So for example if the image has been cropped by the user, the cropping are is ignored.<p>
     *
     * @param targetScaler contains the information about how to scale the image
     *
     * @return a variation of the current image scaled with the given scaler
     */
    protected CmsJspImageBean createVariation(CmsImageScaler targetScaler) {

        CmsJspImageBean result = new CmsJspImageBean();

        result.setCmsObject(getCmsObject());
        result.setResource(getCmsObject(), getResource());
        result.setOriginalScaler(getOriginalScaler());
        result.setBaseScaler(getBaseScaler());
        result.setVfsUri(getVfsUri());
        result.setScaler(targetScaler);
        result.setQuality(getQuality());

        return result;
    }

    /**
     * Sets the scaler that describes the basic adjustments (usually cropping) that have been set on the original image.<p>
     *
     * @return the scaler that describes the basic adjustments (usually cropping) that have been set on the original image
     */
    protected CmsImageScaler getBaseScaler() {

        return m_baseScaler;
    }

    /**
     * Returns the current OpenCms user context.<p>
     *
     * @return the current OpenCms user context
     */
    protected CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Returns the image scaler that describes the original proportions of this image.<p>
     *
     * @return the image scaler that describes the original proportions of this image
     */
    protected CmsImageScaler getOriginalScaler() {

        return m_originalScaler;
    }

    /**
     * Returns this instance bean, required for the transformers.<p>
     *
     * @return this instance bean
     */
    protected CmsJspImageBean getSelf() {

        return this;
    }

    /**
     * Initializes this new image bean based on a VFS resource and optional scaler parameters.<p>
     *
     * @param cms the current OpenCms user context
     * @param imageRes the VFS resource to read the image from
     * @param scaleParams optional scaler parameters to apply to the VFS resource
     */
    protected void init(CmsObject cms, CmsResource imageRes, String scaleParams) {

        setCmsObject(cms);

        // set VFS URI without scaling parameters
        setResource(cms, imageRes);
        setVfsUri(cms.getRequestContext().getSitePath(imageRes));

        // the originalScaler reads the image dimensions from the VFS properties
        CmsImageScaler originalScaler = new CmsImageScaler(cms, getResource());
        // set original scaler
        setOriginalScaler(originalScaler);

        // set base scaler
        CmsImageScaler baseScaler = originalScaler;
        if (scaleParams != null) {
            // scale parameters have been set
            baseScaler = new CmsImageScaler(scaleParams);
            baseScaler.setFocalPoint(originalScaler.getFocalPoint());
        }

        setBaseScaler(baseScaler);

        // set the current scaler to the base scaler
        setScaler(baseScaler);
    }

    /**
     * Returns the scaler that describes the basic adjustments (usually cropping) that have been set on the original image.<p>
     *
     * @param baseScaler the scaler that describes the basic adjustments (usually cropping) that have been set on the original image
     */
    protected void setBaseScaler(CmsImageScaler baseScaler) {

        m_baseScaler = baseScaler;
    }

    /**
     * Sets the current OpenCms user context.<p>
     *
     * @param cms the current OpenCms user context to set
     */
    protected void setCmsObject(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Sets the scaler that describes the original proportions of this image.<p>
     *
     * @param originalScaler the scaler that describes the original proportions of this image
     */
    protected void setOriginalScaler(CmsImageScaler originalScaler) {

        m_originalScaler = originalScaler;
    }

    /**
     * Sets the CmsResource for this image.<p>
     *
     * @param cms the current OpenCms user context, required for wrapping the resource
     * @param resource the VFS resource for this image
     */
    protected void setResource(CmsObject cms, CmsResource resource) {

        m_resource = CmsJspResourceWrapper.wrap(cms, resource);
    }

    /**
     * Sets the image scaler that was used to create this image.<p>
     *
     * @param scaler the image scaler that was used to create this image.
     */
    protected void setScaler(CmsImageScaler scaler) {

        m_currentScaler = scaler;
    }
}