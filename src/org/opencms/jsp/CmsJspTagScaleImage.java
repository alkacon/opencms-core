
package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.util.CmsJspImageBean;
import org.opencms.jsp.util.CmsJspScaledImageBean;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUriSplitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;

/**
 * This tag allows using the OpenCms native image scaling mechanism within JSP.
 * It generates a ScaledImage bean that can be used to include the selected image, adding the
 * required image scaling parameters.<br/><br/>
 * The following image formats are supported: BMP, GIF, JPEG, PNG, PNM, TIFF.
 * <em>
 * Note: Picture scaling is by default only enabled for target size with width and height
 * &lt;=1500. The size can be changed in the image scaler configuration in the file
 * <code>opencms-vfs.xml</code> in the body of the tag <code>&lt;loader&gt;</code>. Also other
 * options for the image scaler are set there.
 * <br/><br/>
 * This tag is an alternative to the OpenCms standard tag cms:img, providing more flexibility by
 * not generating any output but providing a bean that may be used to generate any output needed.
 * This way you can use scaled images for
 * <ul>
 *   <li>The standard img-Tag</li>
 *   <li>
 *     The more modern picture-Tag with multiple sources (hi-DPI variants for retina displays)
 *     for responsive design
 *   </li>
 *   <li>Background-Image integration</li>
 * </ul>
 * </em>
 */
public class CmsJspTagScaleImage extends CmsJspImageScalerTagSupport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagScaleImage.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -6639978110802734737L;

    /** List of hi-DPI variant sizes to produce, e.g. 1.3x, 1.5x, 2x, 3x */
    private List<String> m_hiDpiVariantList;

    /** Name of the request attribute used to store the ScaledImageBean bean */
    private String m_var;

    /**
     * Creates a new image scaling tag instance.<p>
     */
    public CmsJspTagScaleImage() {
        super();
    }

    /**
     * Does some cleanup before returning EVAL_PAGE
     *
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @SuppressWarnings("unused")
    @Override
    public int doEndTag() throws JspException {

        release();
        return EVAL_PAGE;
    }

    /**
     * Handles the Start tag, checks some parameters, uses the CmsImageScaler to create a scaled
     * version of the image (and hi-DPI variants if necessary), stores all information in a
     * ScaledImageBean and stores it as a request attribute (the name for this attribute is given
     * with the tag attribute "var").
     *
     * @return EVAL_BODY_INCLUDE or SKIP_BODY in case of an unexpected Exception (please consult
     * the OpenCms log file if that happens)
     *
     * @throws JspException in case of invalid attributes (if neither width nor height is set)
     */
    @Override
    public int doStartTag() throws JspException {

        if ((m_scaler.getWidth() <= 0) && (m_scaler.getHeight() <= 0)) {
            throw new JspException("At least one of the attributes width or height has to be set");
        }

        ServletRequest req = pageContext.getRequest();

        // this will always be true if the page is called through OpenCms
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                CmsJspScaledImageBean scaledImage = null;
                try {
                    scaledImage = imageTagAction();
                } catch (CmsException e) {
                    // any issue accessing the VFS - just return SKIP_BODY
                    // otherwise template layout will get mixed up with nasty exception messages
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().getBundle().key(Messages.ERR_IMAGE_TAG_VFS_ACCESS_1, m_src), e);
                    }
                }
                pageContext.getRequest().setAttribute(m_var, scaledImage);
            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "scaleImage"), ex);
                }
                return SKIP_BODY;
            }
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Does some cleanup before the tag is released to the tag pool
     *
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        m_hiDpiVariantList = null;
        m_var = null;
        super.release();
    }

    /**
     * Sets the String containing a comma separated list of hi-DPI variants to produce, e.g.
     * "1.3x,1.5x,2x,3x". Currently in most cases "2x" should suffice to generate an additional
     * image for retina screens.
     *
     * @param value comma separated list of hi-DPI variants to produce, e.g. "1.3x,1.5x,2x,3x"
     */
    public void setHiDpiVariants(String value) {

        m_hiDpiVariantList = new ArrayList<>(4);
        String[] multipliers = StringUtils.split(value, ',');
        Collections.addAll(m_hiDpiVariantList, multipliers);
    }

    /**
     * Sets the name of the variable used for storing the resulting bean.
     *
     * @param value name of the resulting CmsJspScaledImage bean
     */
    public void setVar(String value) {

        m_var = value;
    }

    /**
     * Internal method to handle requested hi-DPI variants, adding ImageBeans for all hi-DPI
     * variants to <code>scaledImage</code>
     *
     * @param cms the current CmsObject
     * @param imageRes the CMS resource representing the image
     * @param scaler the CmsImageScaler for the scaled image, will be cloned for each hi-DPI variant
     * @param scaledImage the ScaledImage bean (scaled images will be added to this)
     * @param originalScaler the CmsImageScaler containing the information about the original image
     */
    private void handleHiDpiVariants(
        CmsObject cms,
        CmsResource imageRes,
        CmsImageScaler scaler,
        CmsJspScaledImageBean scaledImage,
        CmsImageScaler originalScaler) {

        int targetWidth = m_scaler.getWidth();
        int targetHeight = m_scaler.getHeight();
        int originalWidth = originalScaler.getWidth();
        int originalHeight = originalScaler.getHeight();

        for (String multiplierString : m_hiDpiVariantList) {

            if (!multiplierString.matches("^[0-9]+(.[0-9]+)?x$")) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        String.format("Illegal multiplier format: %s not usable for image scaling", multiplierString));
                }
                continue;
            }
            float multiplier = NumberUtils.createFloat(
                multiplierString.substring(0, multiplierString.length() - 1)).floatValue();
            int width = Math.round(targetWidth * multiplier);
            int height = Math.round(targetHeight * multiplier);

            if ((originalWidth >= width) && (originalHeight >= height)) {
                CmsImageScaler hiDpiScaler = (CmsImageScaler)scaler.clone();
                hiDpiScaler.setWidth(width);
                hiDpiScaler.setHeight(height);

                String imageSrc = cms.getSitePath(imageRes);
                if (hiDpiScaler.isValid()) {
                    // now append the scaler parameters
                    imageSrc += hiDpiScaler.toRequestParam();
                }
                CmsJspImageBean image = new CmsJspImageBean();
                image.setSrcUrl(OpenCms.getLinkManager().substituteLink(cms, imageSrc));
                image.setWidth(width);
                image.setHeight(height);
                image.setScaler(hiDpiScaler);
                scaledImage.addHiDpiImage(multiplierString, image);
            }
        }
    }

    /**
     * Internal action method to create the scaled image bean.
     *
     * @return the created ScaledImageBean bean
     *
     * @throws CmsException in case something goes wrong
     */
    private CmsJspScaledImageBean imageTagAction() throws CmsException {

        ServletRequest request = pageContext.getRequest();
        CmsFlexController controller = CmsFlexController.getController(request);
        CmsObject cms = controller.getCmsObject();

        // resolve possible relative URI
        String src = CmsLinkManager.getAbsoluteUri(m_src, controller.getCurrentRequest().getElementUri());
        CmsUriSplitter splitSrc = new CmsUriSplitter(src);

        String scaleParam = null;
        if (splitSrc.getQuery() != null) {
            // check if the original URI already has parameters, this is true if original has been cropped
            String[] scaleStr = CmsRequestUtil.createParameterMap(splitSrc.getQuery()).get(CmsImageScaler.PARAM_SCALE);
            if (scaleStr != null) {
                scaleParam = scaleStr[0];
            }
        }

        CmsResource imageRes = cms.readResource(splitSrc.getPrefix());
        CmsImageScaler originalScaler = new CmsImageScaler(cms, imageRes);
        initScaler(originalScaler, scaleParam);

        String imageSrc = cms.getSitePath(imageRes);
        if (m_scaler.isValid()) {
            // now append the scaler parameters
            imageSrc += m_scaler.toRequestParam();
        }

        CmsJspScaledImageBean scaledImage = new CmsJspScaledImageBean();
        scaledImage.setSrcUrl(OpenCms.getLinkManager().substituteLink(cms, imageSrc));
        scaledImage.setWidth(m_scaler.getWidth());
        scaledImage.setHeight(m_scaler.getHeight());
        scaledImage.setScaler(m_scaler);

        // now handle hi-DPI variants
        if ((m_hiDpiVariantList != null) && (m_hiDpiVariantList.size() > 0)) {
            handleHiDpiVariants(cms, imageRes, m_scaler, scaledImage, originalScaler);
        }
        return scaledImage;
    }

    /**
     * Initializes the images scaler used for creating the scaled image bean.<p>
     *
     * @param originalScaler a scaler that contains the original image dimensions
     * @param scaleParams optional scaler parameters for cropping
     */
    private void initScaler(CmsImageScaler originalScaler, String scaleParams) {

        int m_width = m_scaler.getWidth();
        int m_height = m_scaler.getHeight();

        if ((scaleParams != null) && !"undefined".equals(scaleParams)) {
            CmsImageScaler cropScaler = null;
            // use cropped image as a base for scaling
            cropScaler = new CmsImageScaler(scaleParams);
            if (m_scaler.getType() == 5) {
                // must reset height / width parameters in crop scaler for type 5
                cropScaler.setWidth(cropScaler.getCropWidth());
                cropScaler.setHeight(cropScaler.getCropHeight());
            }
            m_scaler = cropScaler.getCropScaler(m_scaler);
            m_width = m_scaler.getWidth();
            m_height = m_scaler.getHeight();
        }

        // If either width or height is not set, the CmsImageScaler will have a problem. So the
        // missing dimension is calculated with the given dimension and the original image's
        // aspect ratio (or the respective crop aspect ratio).
        if ((m_width <= 0) || (m_height <= 0)) {
            float ratio;
            // use the original width/height or the crop with/height for aspect ratio calculation
            if (!m_scaler.isCropping()) {
                ratio = (float)originalScaler.getWidth() / (float)originalScaler.getHeight();
            } else {
                ratio = (float)m_scaler.getCropWidth() / (float)m_scaler.getCropHeight();
            }
            if (m_width <= 0) {
                // width is not set, calculate it with the given height and the original/crop aspect ratio
                m_width = Math.round(m_height * ratio);
                m_scaler.setWidth(m_width);
            } else if (m_height <= 0) {
                // height is not set, calculate it with the given width and the original/crop aspect ratio
                m_height = Math.round(m_width / ratio);
                m_scaler.setHeight(m_height);
            }
        }

        // calculate target scale dimensions (if required)
        if (((m_scaler.getHeight() <= 0) || (m_scaler.getWidth() <= 0))
            || ((m_scaler.getType() == 5) && m_scaler.isValid() && !m_scaler.isCropping())) {
            // read the image properties for the selected resource
            if (originalScaler.isValid()) {
                m_scaler = originalScaler.getReScaler(m_scaler);
            }
        }
    }

}
