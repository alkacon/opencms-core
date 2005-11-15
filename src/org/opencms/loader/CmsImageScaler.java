
package org.opencms.loader;

import com.alkacon.simapi.RenderSettings;
import com.alkacon.simapi.Simapi;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
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

    /** The name of the transparent color (for the backgound image). */
    public static final String COLOR_TRANSPARENT = "transparent";

    /** The (optional) parameter used for sending the scale information of an image in the http request. */
    public static final String PARAM_SCALE = "__scale";

    /** The scaler parameter to indicate the requested image background color (if required). */
    public static final String SCALE_PARAM_COLOR = "c";

    /** The scaler parameter to indicate the requested image height. */
    public static final String SCALE_PARAM_HEIGHT = "h";

    /** The scaler parameter to indicate the requested image position (if required). */
    public static final String SCALE_PARAM_POS = "p";

    /** The scaler parameter to indicate to requested image save quality in percent (if applicable, for example used with JPEG images). */
    public static final String SCALE_PARAM_QUALITY = "q";

    /** The scaler parameter to indicate to requested <code>{@link java.awt.RenderingHints}</code> settings. */
    public static final String SCALE_PARAM_RENDERMODE = "r";

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

    /** The target image save quality (if applicable, for example used with JPEG images) (optional). */
    private int m_quality;

    /** The image processing renderings hints constant mode indicator (optional). */
    private int m_renderMode;

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
            BufferedImage image = Simapi.read(content);
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
     * Returns the image type from the given file name based on the file suffix (extension)
     * and the available image writers.<p>
     * 
     * For example, for the file name "opencms.gif" the type is GIF, for 
     * "opencms.jpg" is is "JPEG" etc.<p> 
     * 
     * In case the input filename has no suffix, or there is no known image writer for the format defined
     * by the suffix, <code>null</code> is returned.<p>
     * 
     * Any non-null result can be used if an image type input value is required.<p>
     * 
     * @param filename the file name to get the type for
     *  
     * @return the image type from the given file name based on the suffix and the available image writers, 
     *      or null if no image writer is available for the format 
     */
    public String getImageType(String filename) {

        return Simapi.getImageType(filename);
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
     * Returns the image saving quality in percent (0 - 100).<p>
     * 
     * This is used oly if applicable, for example when saving JPEG images.<p>
     *
     * @return the image saving quality in percent
     */
    public int getQuality() {

        return m_quality;
    }

    /**
     * Returns the image rendering mode constant.<p>
     *
     * Possible values are:<dl>
     * <dt>{@link Simapi#RENDER_QUALITY} (default)</dt>
     * <dd>Use best possible image processing - this may be slow sometimes.</dd>
     * 
     * <dt>{@link Simapi#RENDER_SPEED}</dt>
     * <dd>Fastest image processing but worse results - use this for thumbnails or where speed is more important then quality.</dd>
     * 
     * <dt>{@link Simapi#RENDER_MEDIUM}</dt>
     * <dd>Use default rendering hints from JVM - not recommended since it's almost as slow as the {@link Simapi#RENDER_QUALITY} mode.</dd></dl>
     *
     * @return the image rendering mode constant
     */
    public int getRenderMode() {

        return m_renderMode;
    }

    /**
     * Returns the type.<p>
     * 
     * Possible values are:<dl>
     * 
     * <dt>0 (default): Scale to exact target size with background padding</dt><dd><ul>
     * <li>enlarge image to fit in target size (if required)
     * <li>reduce image to fit in target size (if required)
     * <li>keep image aspect ratio / propotions intact
     * <li>fill up with bgcolor to reach exact target size
     * <li>fit full image inside target size (only applies if reduced)</ul></dd>
     *
     * <dt>1: Thumbnail generation mode (like 0 but no image enlargement)</dt><dd><ul>
     * <li>dont't enlarge image
     * <li>reduce image to fit in target size (if required)
     * <li>keep image aspect ratio / propotions intact
     * <li>fill up with bgcolor to reach exact target size
     * <li>fit full image inside target size (only applies if reduced)</ul></dd>
     *
     * <dt>2: Scale to exact target size, crop what does not fit</dt><dd><ul>
     * <li>enlarge image to fit in target size (if required)
     * <li>reduce image to fit in target size (if required)
     * <li>keep image aspect ratio / propotions intact
     * <li>fit full image inside target size (crop what does not fit)</ul></dd>
     *
     * <dt>3: Scale and keep image propotions, target size variable</dt><dd><ul>
     * <li>enlarge image to fit in target size (if required)
     * <li>reduce image to fit in target size (if required)
     * <li>keep image aspect ratio / propotions intact
     * <li>scaled image will not be padded or cropped, so target size is likley not the exact requested size</ul></dd>
     *
     * <dt>4: Don't keep image propotions, use exact target size</dt><dd><ul>
     * <li>enlarge image to fit in target size (if required)
     * <li>reduce image to fit in target size (if required)
     * <li>don't keep image aspect ratio / propotions intact
     * <li>the image will be scaled exactly to the given target size and likley will be loose proportions</ul></dd>
     * </dl>
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

        Simapi scaler;
        if ((m_renderMode == 0) && (m_quality == 0)) {
            // use default render mode and quality
            scaler = new Simapi();
        } else {
            // use special render mode and/or quality
            RenderSettings renderSettings = new RenderSettings(m_renderMode);
            if (m_quality != 0) {
                renderSettings.setCompressionQuality(m_quality / 100f);
            }
            scaler = new Simapi(renderSettings);
        }
        // calculate a valid image type supported by the imaging libary (e.g. "JPEG", "GIF")
        String imageType = Simapi.getImageType(file.getRootPath());
        if (imageType == null) {
            // no type given, maybe the name got mixed up
            String mimeType = OpenCms.getResourceManager().getMimeType(file.getName(), null, null);
            // check if this is another known mime type, if so DONT use it (images should not be named *.pdf)
            if (mimeType == null) {
                // no mime type found, use JPEG format to write images to the cache         
                imageType = Simapi.TYPE_JPEG;
            }
        }
        if (imageType == null) {
            // unknown type, unable to scale the image
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.ERR_UNABLE_TO_SCALE_IMAGE_2, file.getRootPath(), toString()));
            }
            return result;
        }
        try {
            BufferedImage image = Simapi.read(file.getContents());
            switch (getType()) {
                // select the "right" method of scaling according to the "t" parameter
                case 1:
                    // thumbnail generation mode (like 0 but no image enlargement)
                    image = scaler.resize(image, getWidth(), getHeight(), getColor(), getPosition(), false);
                    break;
                case 2:
                    // scale to exact target size, crop what does not fit
                    image = scaler.resize(image, getWidth(), getHeight(), getPosition());
                    break;
                case 3:
                    // scale and keep image propotions, target size variable
                    image = scaler.resize(image, getWidth(), getHeight(), true);
                    break;
                case 4:
                    // don't keep image propotions, use exact target size
                    image = scaler.resize(image, getWidth(), getHeight(), false);
                    break;
                default:
                    // scale to exact target size with background padding
                    image = scaler.resize(image, getWidth(), getHeight(), getColor(), getPosition(), true);
            }
            // get the byte result for the scaled image
            result = scaler.getBytes(image, imageType);
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
     * Sets the image saving quality in percent.<p>
     *
     * @param quality the image saving quality (in percent) to set
     */
    public void setQuality(int quality) {

        if (quality < 0) {
            m_quality = 0;
        } else if (quality > 100) {
            m_quality = 100;
        } else {
            m_quality = quality;
        }
    }

    /**
     * Sets the image rendering mode constant.<p>
     *
     * @param renderMode the image rendering mode to set
     * 
     * @see #getRenderMode() for a list of allowed values for the rendering mode
     */
    public void setRenderMode(int renderMode) {

        if ((renderMode < Simapi.RENDER_QUALITY) || (renderMode > Simapi.RENDER_SPEED)) {
            renderMode = Simapi.RENDER_QUALITY;
        }
        m_renderMode = renderMode;
    }

    /**
     * Sets the scale type.<p>
     *
     * @param type the scale type to set
     * 
     * @see #getType() for a detailed description of the possible values for the type
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
            if (m_color == Simapi.COLOR_TRANSPARENT) {
                result.append(COLOR_TRANSPARENT);
            } else {
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
        }
        if (m_quality > 0) {
            result.append(',');
            result.append(CmsImageScaler.SCALE_PARAM_QUALITY);
            result.append(':');
            result.append(m_quality);
        }
        if (m_renderMode > 0) {
            result.append(',');
            result.append(CmsImageScaler.SCALE_PARAM_RENDERMODE);
            result.append(':');
            result.append(m_renderMode);
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
                    if (COLOR_TRANSPARENT.indexOf(v) == 0) {
                        m_color = Simapi.COLOR_TRANSPARENT;
                    } else {
                        m_color = CmsStringUtil.getColorValue(v, Color.WHITE, k);
                    }
                } else if (CmsImageScaler.SCALE_PARAM_POS.equals(k)) {
                    // image position (depends on scale type)
                    m_position = getParamPosition(v);
                } else if (CmsImageScaler.SCALE_PARAM_QUALITY.equals(k)) {
                    // image position (depends on scale type)
                    setQuality(CmsStringUtil.getIntValue(v, 0, k));
                } else if (CmsImageScaler.SCALE_PARAM_RENDERMODE.equals(k)) {
                    // image position (depends on scale type)
                    setRenderMode(CmsStringUtil.getIntValue(v, 0, k));
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