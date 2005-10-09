
package org.opencms.loader;

import com.alkacon.simapi.Simapi;
import com.alkacon.simapi.SimapiFactory;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Contains the parameters for the image scaler.<p>
 */
public class CmsImageScaler {

    /** The (optional) parameter used for sending the scale information of an image in the http request. */
    public static final String PARAM_SCALE = "__scale";

    /** The scaler parameter to indicate the requested image background color (if required). */
    public static final String SCALE_PARAM_COLOR = "c";

    /** The scaler parameter to indicate the requested image height. */
    public static final String SCALE_PARAM_HEIGHT = "h";

    /** The scaler parameter to indicate the requested image positio (if required). */
    public static final String SCALE_PARAM_POS = "p";

    /** The scaler parameter to indicate the requested scale type. */
    public static final String SCALE_PARAM_TYPE = "t";

    /** The scaler parameter to indicate the requested image width. */
    public static final String SCALE_PARAM_WIDTH = "w";

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsImageScaler.class);

    /** The target background color (optional). */
    private Color m_color;

    /** The target height (required). */
    private int m_height;

    /** The target position (optional). */
    private int m_position;

    /** The final (parsed and corrected) scale parameters. */
    private String m_scaleParameters;

    /** The target scale type (optional). */
    private int m_type;

    /** The target width (required). */
    private int m_width;

    /**
     * Creates a new image scaler for the given image contained in the byte array.<p>
     * 
     * <b>Please note:</b>The image itself is not stored in the scaler, only the width and
     * height dimensions of the image. To actually scale an image, you need to use
     * <code>{@link #scaleImage(CmsFile)}</code>. This constructor is commonly used only 
     * to extract the image dimensions, for example when creating a String value for
     * the <code>{@link CmsPropertyDefinition#PROPERTY_IMAGE_SIZE}</code> property.<p>
     * 
     * In case the byte array can not be decoded to an image, or in case of other errors,
     * <code>{@link #isValid()}</code> will return <code>false</code>.<p>
     * 
     * @param content the image to calculate the dimensions for
     * @param rootPath the root path of the resource (for error logging)
     */
    public CmsImageScaler(byte[] content, String rootPath) {

        init();
        try {
            // read the scaled image
            BufferedImage image = SimapiFactory.getInstace().read(content);
            m_height = image.getHeight();
            m_width = image.getWidth();
        } catch (Exception e) {
            // nothing we can do about this, keep the original properties            
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.ERR_UNABLE_TO_EXTRACT_IMAGE_SIZE_1, rootPath), e);
            }
            // set height / width to default of -1
            init();
        }
    }

    /**
     * Creates a new image scaler by reading the property <code>{@link CmsPropertyDefinition#PROPERTY_IMAGE_SIZE}</code>
     * from the given resource.<p>
     * 
     * In case of any errors reading or parsing the property,
     * <code>{@link #isValid()}</code> will return <code>false</code>.<p>
     * 
     * @param cms the OpenCms user context to use when reading the property
     * @param res the resource to read the property from
     */
    public CmsImageScaler(CmsObject cms, CmsResource res) {

        init();
        String sizeValue = null;
        if ((cms != null) && (res != null)) {
            try {
                CmsProperty sizeProp = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_IMAGE_SIZE, false);
                if (!sizeProp.isNullProperty()) {
                    // parse property value using standard procedures
                    sizeValue = sizeProp.getValue();
                }
            } catch (Exception e) {
                // ignore
            }
        }
        if (CmsStringUtil.isNotEmpty(sizeValue)) {
            parseParameters(sizeValue);
        }
    }

    /**
     * Creates a new image scaler based on the given String.<p>
     * 
     * @param parameters the scale parameters to use
     */
    public CmsImageScaler(String parameters) {

        init();
        if (CmsStringUtil.isNotEmpty(parameters)) {
            parseParameters(parameters);
        }
    }

    /**
     * Returns the color.<p>
     *
     * @return the color
     */
    public Color getColor() {

        return m_color;
    }

    /**
     * Returns the height.<p>
     *
     * @return the height
     */
    public int getHeight() {

        return m_height;
    }

    /**
     * Returns the position.<p>
     *
     * @return the position
     */
    public int getPosition() {

        return m_position;
    }

    /**
     * Returns the type.<p>
     *
     * Possible values are:<ul>
     * <li>0: scale to exact size best fit [default] (req. position, color)</li>
     * <li>1: scale to exact size crop (req. position)</li>
     * <li>2: scale to best fit, size not fixed, keep aspect ratio</li>
     * <li>3: scale to exact fit, don't keep aspect ratio</li>
     * <li>4: crop only (req. position)</li>
     * </ul>
     *
     * @return the type
     */
    public int getType() {

        return m_type;
    }

    /**
     * Returns the width.<p>
     *
     * @return the width
     */
    public int getWidth() {

        return m_width;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return toString().hashCode();
    }

    /**
     * Returns <code>true</code> if all required parameters are available.<p>
     * 
     * Required parameters are "h" (height), and "w" (width).<p>
     * 
     * @return <code>true</code> if all required parameters are available
     */
    public boolean isValid() {

        return (m_width > 0) && (m_height > 0);
    }

    /**
     * Returns a scaled version of the given image file according this image scalers parameters.<p>
     *  
     * @param file the image file to scale
     * 
     * @return a scaled version of the given image file according to the provided scaler parameters
     */
    public byte[] scaleImage(CmsFile file) {

        byte[] result = file.getContents();

        Simapi scaler = SimapiFactory.getInstace();
        // calculate a valid image type supported by the imaging libary (e.g. "JPEG", "GIF")
        String type = scaler.getImageType(file.getRootPath());
        try {
            BufferedImage image = scaler.read(file.getContents());
            switch (getType()) {
                // select the "right" method of scaling according to the "t" parameter
                case 1:
                    image = scaler.resize(image, getWidth(), getHeight(), getPosition());
                    break;
                case 2:
                    image = scaler.resize(image, getWidth(), getHeight(), true);
                    break;
                case 3:
                    image = scaler.resize(image, getWidth(), getHeight(), false);
                    break;
                case 4:
                    image = scaler.crop(image, getWidth(), getHeight(), getPosition());
                    break;
                default:
                    image = scaler.resize(image, getWidth(), getHeight(), getColor(), getPosition());
            }
            // get the byte result for the scaled image
            result = scaler.getBytes(image, type);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.ERR_UNABLE_TO_SCALE_IMAGE_2, file.getRootPath(), toString()), e);
            }
        }
        return result;
    }

    /**
     * Sets the color.<p>
     *
     * @param color the color to set
     */
    public void setColor(Color color) {

        m_color = color;
    }

    /**
     * Sets the height.<p>
     *
     * @param height the height to set
     */
    public void setHeight(int height) {

        m_height = height;
    }

    /**
     * Sets the scale position.<p>
     *
     * @param position the position to set
     */
    public void setPosition(int position) {

        m_position = position;
    }

    /**
     * Sets the scale type.<p>
     *
     * @param type the scale type to set
     * 
     * @see #getType()
     */
    public void setType(int type) {

        m_type = type;
    }

    /**
     * Sets the width.<p>
     *
     * @param width the width to set
     */
    public void setWidth(int width) {

        m_width = width;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        if (m_scaleParameters != null) {
            return m_scaleParameters;
        }

        StringBuffer result = new StringBuffer(64);
        result.append(CmsImageScaler.SCALE_PARAM_WIDTH);
        result.append(':');
        result.append(m_width);
        result.append(',');
        result.append(CmsImageScaler.SCALE_PARAM_HEIGHT);
        result.append(':');
        result.append(m_height);
        if (m_type > 0) {
            result.append(',');
            result.append(CmsImageScaler.SCALE_PARAM_TYPE);
            result.append(':');
            result.append(m_type);
        }
        if (m_position > 0) {
            result.append(',');
            result.append(CmsImageScaler.SCALE_PARAM_POS);
            result.append(':');
            result.append(m_position);
        }
        if (m_color != Color.WHITE) {
            result.append(',');
            result.append(CmsImageScaler.SCALE_PARAM_COLOR);
            result.append(':');
            if (m_color.getRed() < 16) {
                result.append('0');
            }
            result.append(Integer.toString(m_color.getRed(), 16));
            if (m_color.getGreen() < 16) {
                result.append('0');
            }
            result.append(Integer.toString(m_color.getGreen(), 16));
            if (m_color.getBlue() < 16) {
                result.append('0');
            }
            result.append(Integer.toString(m_color.getBlue(), 16));
        }
        m_scaleParameters = result.toString();
        return m_scaleParameters;
    }

    /**
     * Returns a valid value for the "p" (position) parameter.<p> 
     * 
     * @param value the value to parse as position
     * 
     * @return a valid value for the "p" (position) parameter
     */
    private int getParamPosition(String value) {

        int pos = CmsStringUtil.getIntValue(value, -1, CmsImageScaler.SCALE_PARAM_POS);
        switch (pos) {
            case Simapi.POS_DOWN_LEFT:
            case Simapi.POS_DOWN_RIGHT:
            case Simapi.POS_STRAIGHT_DOWN:
            case Simapi.POS_STRAIGHT_LEFT:
            case Simapi.POS_STRAIGHT_RIGHT:
            case Simapi.POS_STRAIGHT_UP:
            case Simapi.POS_UP_LEFT:
            case Simapi.POS_UP_RIGHT:
                // pos is fine
                break;
            default:
                pos = Simapi.POS_CENTER;
        }
        return pos;
    }

    /**
     * Returns a valid value for the "t" (type) parameter.<p> 
     * 
     * Possible values are:<ul>
     * <li>0: scale to exact size best fit [default] (req. position, color)</li>
     * <li>1: scale to exact size crop (req. position)</li>
     * <li>2: scale to best fit, size not fixed, keep aspect ratio</li>
     * <li>3: scale to exact fit, don't keep aspect ratio</li>
     * <li>4: crop only (req. position)</li>
     * </ul>
     * 
     * @param value the value to parse as type
     * 
     * @return a valid value for the "t" (type) parameter
     */
    private int getParamType(String value) {

        int type = CmsStringUtil.getIntValue(value, -1, CmsImageScaler.SCALE_PARAM_TYPE);
        if ((type < 0) || (type > 4)) {
            type = 0;
        }
        return type;
    }

    /**
     * Initializes the members with the default values.<p>
     */
    private void init() {

        m_height = -1;
        m_width = -1;
        m_type = 0;
        m_position = 0;
        m_color = Color.WHITE;
    }

    /**
     * Parses the scaler parameters.<p>
     * 
     * @param parameters the parameters to parse
     */
    private void parseParameters(String parameters) {

        m_width = -1;
        m_height = -1;
        m_position = 0;
        m_type = 0;
        m_color = Color.WHITE;

        List tokens = CmsStringUtil.splitAsList(parameters, ',');
        Iterator it = tokens.iterator();
        String k;
        String v;
        while (it.hasNext()) {
            String t = (String)it.next();
            // extract key and value
            k = null;
            v = null;
            int idx = t.indexOf(':');
            if (idx >= 0) {
                k = t.substring(0, idx).trim();
                if (t.length() > idx) {
                    v = t.substring(idx + 1).trim();
                }
            }
            if (CmsStringUtil.isNotEmpty(k) && CmsStringUtil.isNotEmpty(v)) {
                // key and value are available
                if (CmsImageScaler.SCALE_PARAM_HEIGHT.equals(k)) {
                    // image height
                    m_height = CmsStringUtil.getIntValue(v, Integer.MIN_VALUE, k);
                } else if (CmsImageScaler.SCALE_PARAM_WIDTH.equals(k)) {
                    // image width
                    m_width = CmsStringUtil.getIntValue(v, Integer.MIN_VALUE, k);
                } else if (CmsImageScaler.SCALE_PARAM_TYPE.equals(k)) {
                    // scaling type
                    m_type = getParamType(v);
                } else if (CmsImageScaler.SCALE_PARAM_COLOR.equals(k)) {
                    // image background color
                    m_color = CmsStringUtil.getColorValue("#" + v, Color.WHITE, k);
                } else if (CmsImageScaler.SCALE_PARAM_POS.equals(k)) {
                    // image position (depends on scale type)
                    m_position = getParamPosition(v);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().key(Messages.ERR_INVALID_IMAGE_SCALE_PARAMS_2, k, v));
                    }
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.ERR_INVALID_IMAGE_SCALE_PARAMS_2, k, v));
                }
            }
        }
    }
}