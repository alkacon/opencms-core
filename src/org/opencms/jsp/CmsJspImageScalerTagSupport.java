
package org.opencms.jsp;

import org.opencms.loader.CmsImageScaler;
import org.opencms.util.CmsStringUtil;

import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Abstract parent for all JSP tags dealing with image scaling, defines some common image scaler
 * properties and corresponding getters/setters that may be used by extending classes.
 */
public abstract class CmsJspImageScalerTagSupport extends BodyTagSupport {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 1303030767942208144L;

    /** Required image scaler attributes constant. */
    protected static final String SCALE_ATTR_HEIGHT = "height";

    /** Required image scaler attributes constant. */
    protected static final String SCALE_ATTR_MAXHEIGHT = "maxHeight";

    /** Required image scaler attributes constant. */
    protected static final String SCALE_ATTR_MAXWIDTH = "maxWidth";

    /** Required image scaler attributes constant. */
    protected static final String SCALE_ATTR_POSITION = "scaleposition";

    /** Required image scaler attributes constant. */
    protected static final String SCALE_ATTR_QUALITY = "scalequality";

    /** Required image scaler attributes constant. */
    protected static final String SCALE_ATTR_RENDERMODE = "scalerendermode";

    /** Required image scaler attributes constant. */
    protected static final String SCALE_ATTR_TYPE = "scaletype";

    /** Required image scaler attributes constant. */
    protected static final String SCALE_ATTR_WIDTH = "width";

    /** The given image scaler parameters. */
    protected transient CmsImageScaler m_scaler;

    /** The image source. */
    protected String m_src;

    /**
     * Initializes a CmsImageScaler to be used by derived classes. The CmsImageScaler is recreated
     * every time {@link #release()} is called.
     */
    public CmsJspImageScalerTagSupport() {
        // initialize the image scaler parameter container
        m_scaler = new CmsImageScaler();
    }

    /**
     * Returns the scaling height for the image.<p>
     *
     * @return the scaling height for the image
     */
    public String getHeight() {

        return String.valueOf(m_scaler.getHeight());
    }

    /**
     * Returns the maximum scaling height for the image, only needed if scale type is 5.<p>
     *
     * @return the maximum scaling height for the image
     */
    public String getMaxHeight() {

        return String.valueOf(m_scaler.getMaxHeight());
    }

    /**
     * Returns the maximum scaling width for the image, only needed if scale type is 5.<p>
     *
     * @return the maximum scaling width for the image
     */
    public String getMaxWidth() {

        return String.valueOf(m_scaler.getMaxWidth());
    }

    /**
     * Returns the background color used by the image scaler.<p>
     *
     * @return the background color used by the image scaler
     */
    public String getScaleColor() {

        return m_scaler.getColorString();
    }

    /**
     * Returns the filter list used by the image scaler.<p>
     *
     * @return the filter list used by the image scaler
     */
    public String getScaleFilter() {

        return m_scaler.getFiltersString();
    }

    /**
     * Returns the position used by the image scaler.<p>
     *
     * @return the position used by the image scaler
     */
    public String getScalePosition() {

        return String.valueOf(m_scaler.getPosition());
    }

    /**
     * Returns the quality used by the image scaler.<p>
     *
     * @return the quality used by the image scaler
     */
    public String getScaleQuality() {

        return String.valueOf(m_scaler.getQuality());
    }

    /**
     * Returns the render mode used by the image scaler.<p>
     *
     * @return the render mode used by the image scaler
     */
    public String getScaleRendermode() {

        return String.valueOf(m_scaler.getRenderMode());
    }

    /**
     * Returns the scaling type for the image.<p>
     *
     * @return the scaling type for the image
     */
    public String getScaleType() {

        return String.valueOf(m_scaler.getType());
    }

    /**
     * Returns the source of the image to scale,
     * which will have the OpenCms webapp / servlet prefix added.<p>
     *
     * @return the source of the image to scale
     */
    public String getSrc() {

        return m_src;
    }

    /**
     * Returns the scaling width for the image.<p>
     *
     * @return the scaling width for the image
     */
    public String getWidth() {

        return String.valueOf(m_scaler.getWidth());
    }

    /**
     * Does some cleanup and creates a new ImageScaler before the tag is released to the tag pool.
     *
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        m_scaler = new CmsImageScaler();
        m_src = null;
        super.release();
    }

    /**
     * Sets the scaling height for the image.<p>
     *
     * If no valid integer is given, then "0" is used as value.<p>
     *
     * @param value the scaling height for the image to set
     */
    public void setHeight(String value) {

        m_scaler.setHeight(CmsStringUtil.getIntValueRounded(value, 0, SCALE_ATTR_HEIGHT));
    }

    /**
     * Sets the maximum scaling height for the image, only needed if scale type is 5.<p>
     *
     * If no valid integer is given, then the value of {@link #getHeight()} is used as value.<p>
     *
     * @param value the maximum scaling height for the image to set
     */
    public void setMaxHeight(String value) {

        m_scaler.setMaxHeight(CmsStringUtil.getIntValueRounded(value, -1, SCALE_ATTR_MAXHEIGHT));
    }

    /**
     * Sets the maximum scaling width for the image, only needed if scale type is 5.<p>
     *
     * If no valid integer is given, then the value of {@link #getWidth()} is used as value.<p>
     *
     * @param value the maximum scaling width for the image to set
     */
    public void setMaxWidth(String value) {

        m_scaler.setMaxWidth(CmsStringUtil.getIntValueRounded(value, -1, SCALE_ATTR_MAXWIDTH));
    }

    /**
     * Sets the background color used by the image scaler.<p>
     *
     * @param value the background color to set
     */
    public void setScaleColor(String value) {

        m_scaler.setColor(value);
    }

    /**
     * Sets the filter(s) used by the image scaler.<p>
     *
     * @param value the filter(s) to set
     */
    public void setScaleFilter(String value) {

        m_scaler.setFilters(value);
    }

    /**
     * Sets the position used by the image scaler.<p>
     *
     * @param value the position to set
     */
    public void setScalePosition(String value) {

        m_scaler.setPosition(CmsStringUtil.getIntValue(value, 0, SCALE_ATTR_POSITION));
    }

    /**
     * Sets the quality used by the image scaler.<p>
     *
     * @param value the quality to set
     */
    public void setScaleQuality(String value) {

        m_scaler.setQuality(CmsStringUtil.getIntValue(value, 0, SCALE_ATTR_QUALITY));
    }

    /**
     * Sets the render mode used by the image scaler.<p>
     *
     * @param value the render mode to set
     */
    public void setScaleRendermode(String value) {

        m_scaler.setRenderMode(CmsStringUtil.getIntValue(value, 0, SCALE_ATTR_RENDERMODE));
    }

    /**
     * Sets the scaling type for the image.<p>
     *
     * If no valid integer is given, then "0" is used as value.<p>
     *
     * @param value the scaling type for the image to set
     */
    public void setScaleType(String value) {

        m_scaler.setType(CmsStringUtil.getIntValue(value, 0, SCALE_ATTR_TYPE));
    }

    /**
     * Sets the source of the image.<p>
     *
     * The source must be an absolute path in the current users OpenCms site, without any
     * webapp or servlet prefix.<p>
     *
     * @param value the image source to set
     */
    public void setSrc(String value) {

        m_src = value;
    }

    /**
     * Sets the scaling width for the image.<p>
     *
     * If no valid integer is given, then "0" is used as value.<p>
     *
     * @param value the scaling width for the image to set
     */
    public void setWidth(String value) {

        m_scaler.setWidth(CmsStringUtil.getIntValueRounded(value, 0, SCALE_ATTR_WIDTH));
    }
}
