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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUriSplitter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

/**
 * Creates HTML code for &lt;img src&gt; tags that use the OpenCms image scaling capabilities.<p>
 *
 * @since 6.2.0
 */
public class CmsJspTagImage extends CmsJspImageScalerTagSupport implements I_CmsJspTagParamParent {

    /** Optional HTML attribute constant. */
    private static final String ATTR_ALIGN = "align";

    /** Optional HTML attribute constant. */
    private static final String ATTR_ALT = "alt";

    /** Optional HTML attribute constant. */
    private static final String ATTR_BORDER = "border";

    /** Optional HTML attribute constant. */
    private static final String ATTR_CLASS = "class";

    /** Optional HTML attribute constant. */
    private static final String ATTR_HSPACE = "hspace";

    /** Optional HTML attribute constant. */
    private static final String ATTR_ID = "id";

    /** Optional HTML attribute constant. */
    private static final String ATTR_LONGDESC = "longdesc";

    /** Optional HTML attribute constant. */
    private static final String ATTR_NAME = "name";

    /** Optional HTML attribute constant. */
    private static final String ATTR_STYLE = "style";

    /** Optional HTML attribute constant. */
    private static final String ATTR_TITLE = "title";

    /** Optional HTML attribute constant. */
    private static final String ATTR_USEMAP = "usemap";

    /** Optional HTML attribute constant. */
    private static final String ATTR_VSPACE = "vspace";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagImage.class);

    /** Required image scaler attributes constant. */
    private static final String SCALE_ATTR_COLOR = "scalecolor";

    /** Required image scaler attributes constant. */
    private static final String SCALE_ATTR_FILTER = "scalefilter";

    /** Required image scaler attributes constant. */
    private static final String SCALE_ATTR_NODIM = "nodim";

    /** Required image scaler attributes constant. */
    private static final String SCALE_ATTR_PARTIALTAG = "partialtag";

    /** Required image scaler attributes constant. */
    private static final String SCALE_ATTR_SRC = "src";

    /** Lists for fast lookup. */
    private static final String[] SCALER_ATTRS = {
        SCALE_ATTR_COLOR,
        SCALE_ATTR_FILTER,
        SCALE_ATTR_HEIGHT,
        SCALE_ATTR_PARTIALTAG,
        SCALE_ATTR_POSITION,
        SCALE_ATTR_QUALITY,
        SCALE_ATTR_RENDERMODE,
        SCALE_ATTR_SRC,
        SCALE_ATTR_TYPE,
        SCALE_ATTR_WIDTH,
        SCALE_ATTR_MAXHEIGHT,
        SCALE_ATTR_MAXWIDTH,
        SCALE_ATTR_NODIM};

    /** Image scaler attribute list. */
    private static final List<String> SCALER_ATTRS_LIST = Arrays.asList(SCALER_ATTRS);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 6513320107441256414L;

    /** Map with additionally set image attributes not needed by the image scaler. */
    private Map<String, String> m_attributes;

    /** Controls if the created HTML image tag contains height and width attributes. */
    private boolean m_noDim;

    /** Controls if the created HTML image tag is a full or partial tag. */
    private boolean m_partialTag;

    /**
     * Creates a new image scaling tag instance.<p>
     */
    public CmsJspTagImage() {
        super();
    }

    /**
     * Creates the images scaler used by this image tag.<p>
     *
     * @param scaler the scaler created from this tags parameters
     * @param original a scaler that contains the original image dimensions
     * @param scaleParam optional scaler parameters for cropping
     *
     * @return the images scaler used by this image tag
     */
    public static CmsImageScaler getScaler(CmsImageScaler scaler, CmsImageScaler original, String scaleParam) {

        if (scaleParam != null) {
            CmsImageScaler cropScaler = null;
            // use cropped image as a base for scaling
            cropScaler = new CmsImageScaler(scaleParam);
            if (scaler.getType() == 5) {
                // must reset height / width parameters in crop scaler for type 5
                cropScaler.setWidth(cropScaler.getCropWidth());
                cropScaler.setHeight(cropScaler.getCropHeight());
            }
            scaler = cropScaler.getCropScaler(scaler);
        }
        // calculate target scale dimensions (if required)
        if (((scaler.getHeight() <= 0) || (scaler.getWidth() <= 0))
            || ((scaler.getType() == 5) && scaler.isValid() && !scaler.isCropping())) {
            // read the image properties for the selected resource
            if (original.isValid()) {
                scaler = original.getReScaler(scaler);
            }
        }
        return scaler;
    }

    /**
     * Internal action method to create the tag content.<p>
     *
     * @param src the image source
     * @param scaler the image scaleing parameters
     * @param attributes the additional image HTML attributes
     * @param partialTag if <code>true</code>, the opening <code>&lt;img</code> and closing <code> /&gt;</code> is omitted
     * @param noDim if <code>true</code>, the <code>height</code> and <code>width</code> attributes are omitted
     * @param req the current request
     *
     * @return the created &lt;img src&gt; tag content
     *
     * @throws CmsException in case something goes wrong
     */
    public static String imageTagAction(
        String src,
        CmsImageScaler scaler,
        Map<String, String> attributes,
        boolean partialTag,
        boolean noDim,
        ServletRequest req)
    throws CmsException {

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();

        // resolve possible relative URI
        src = CmsLinkManager.getAbsoluteUri(src, controller.getCurrentRequest().getElementUri());
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
        CmsImageScaler original = new CmsImageScaler(cms, imageRes);
        scaler = getScaler(scaler, original, scaleParam);

        StringBuffer result = new StringBuffer(128);
        if (!partialTag) {
            // open tag if not a partial tag
            result.append("<img");
        }

        // append the image source
        result.append(" src=\"");

        String imageLink = cms.getSitePath(imageRes);
        if (scaler.isValid()) {
            // now append the scaler parameters
            imageLink += scaler.toRequestParam();
        }
        result.append(OpenCms.getLinkManager().substituteLink(cms, imageLink));
        result.append("\"");

        if (!noDim && scaler.isValid()) {
            // append image width and height
            result.append(" width=\"");
            result.append(scaler.getWidth());
            result.append("\"");
            result.append(" height=\"");
            result.append(scaler.getHeight());
            result.append("\"");
        }

        if (attributes != null) {
            // append the HTML attributes
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                String attr = entry.getKey();
                String value = entry.getValue();
                result.append(" ");
                result.append(attr);
                result.append("=\"");
                result.append(CmsEncoder.escapeXml(value));
                result.append("\"");
            }
        }

        if (!partialTag) {
            // close tag if not a partial tag
            result.append(" />");
        }
        return result.toString();
    }

    /**
     * Internal action method to create the tag content.<p>
     *
     * @param src the image source
     * @param scaler the image scaleing parameters
     * @param attributes the additional image HTML attributes
     * @param partialTag if <code>true</code>, the opening <code>&lt;img</code> and closing <code> /&gt;</code> is omitted
     * @param req the current request
     *
     * @return the created &lt;img src&gt; tag content
     *
     * @throws CmsException in case something goes wrong
     */
    public static String imageTagAction(
        String src,
        CmsImageScaler scaler,
        Map<String, String> attributes,
        boolean partialTag,
        ServletRequest req)
    throws CmsException {

        return imageTagAction(src, scaler, attributes, partialTag, false, req);
    }

    /**
     * @see org.opencms.jsp.I_CmsJspTagParamParent#addParameter(java.lang.String, java.lang.String)
     */
    public void addParameter(String name, String value) {

        String key = name.trim().toLowerCase();
        switch (SCALER_ATTRS_LIST.indexOf(key)) {
            case 0: // scaleColor
                setScaleColor(value);
                break;
            case 1: // scaleFilter
                setScaleFilter(value);
                break;
            case 2: // height
                setHeight(value);
                break;
            case 3: // partialTag
                setPartialTag(value);
                break;
            case 4: // scalePosition
                setScalePosition(value);
                break;
            case 5: // scaleQuality
                setScaleQuality(value);
                break;
            case 6: // scaleRendermode
                setScaleRendermode(value);
                break;
            case 7: // src
                setSrc(value);
                break;
            case 8: // scaleType
                setScaleType(value);
                break;
            case 9: // width
                setWidth(value);
                break;
            case 10: // maxHeight
                setMaxHeight(value);
                break;
            case 11: // maxWidth
                setMaxWidth(value);
                break;
            case 12: // noDim
                setNoDim(value);
                break;
            default: // not a value used by the image scaler, treat as HTML attribute
                setAttribute(key, value);
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {

        ServletRequest req = pageContext.getRequest();

        // this will always be true if the page is called through OpenCms
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                // create the HTML image tag
                String imageTag = null;
                try {
                    imageTag = imageTagAction(m_src, m_scaler, m_attributes, m_partialTag, m_noDim, req);
                } catch (CmsException e) {
                    // any issue accessing the VFS - just return an empty string
                    // otherwise template layout will get mixed up with nasty exception messages
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().getBundle().key(Messages.ERR_IMAGE_TAG_VFS_ACCESS_1, m_src), e);
                    }
                }
                // make sure that no null String is returned
                pageContext.getOut().print(imageTag == null ? "" : imageTag);

            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "image"), ex);
                }
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        release();
        return EVAL_PAGE;
    }

    /**
     * Returns <code>{@link #EVAL_BODY_BUFFERED}</code>.<p>
     *
     * @return <code>{@link #EVAL_BODY_BUFFERED}</code>
     *
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() {

        return EVAL_BODY_BUFFERED;
    }

    /**
     * Returns the value of the HTML "align" attribute.<p>
     *
     * @return the value of the HTML "align" attribute
     */
    public String getAlign() {

        return getAttribute(ATTR_ALIGN);
    }

    /**
     * Returns the value of the HTML "alt" attribute.<p>
     *
     * @return the value of the HTML "alt" attribute
     */
    public String getAlt() {

        return getAttribute(ATTR_ALT);
    }

    /**
     * Returns the value of the HTML "border" attribute.<p>
     *
     * @return the value of the HTML "border" attribute
     */
    public String getBorder() {

        return getAttribute(ATTR_BORDER);
    }

    /**
     * Returns the value of the HTML "class" attribute.<p>
     *
     * @return the value of the HTML "class" attribute
     */
    public String getCssclass() {

        return getAttribute(ATTR_CLASS);
    }

    /**
     * Returns the value of the HTML "hspace" attribute.<p>
     *
     * @return the value of the HTML "hspace" attribute
     */
    public String getHspace() {

        return getAttribute(ATTR_HSPACE);
    }

    /**
     * Returns the value of the HTML "id" attribute.<p>
     *
     * @return the value of the HTML "id" attribute
     */
    @Override
    public String getId() {

        return getAttribute(ATTR_ID);
    }

    /**
     * Returns the value of the HTML "longdesc" attribute.<p>
     *
     * @return the value of the HTML "longdesc" attribute
     */
    public String getLongdesc() {

        return getAttribute(ATTR_LONGDESC);
    }

    /**
     * Returns the value of the HTML "name" attribute.<p>
     *
     * @return the value of the HTML "name" attribute
     */
    public String getName() {

        return getAttribute(ATTR_NAME);
    }

    /**
     * Returns <code>"true"</code> if the created HTML image tag does not contain height and width attributes.<p>
     *
     * @return <code>"true"</code> if the created HTML image tag does not contain height and width attributes
     */

    public String getNoDim() {

        return String.valueOf(m_noDim);
    }

    /**
     * Returns the value of the HTML "style" attribute.<p>
     *
     * @return the value of the HTML "style" attribute
     */
    public String getStyle() {

        return getAttribute(ATTR_STYLE);
    }

    /**
     * Returns the value of the HTML "title" attribute.<p>
     *
     * @return the value of the HTML "title" attribute
     */
    public String getTitle() {

        return getAttribute(ATTR_TITLE);
    }

    /**
     * Returns the value of the HTML "usemap" attribute.<p>
     *
     * @return the value of the HTML "usemap" attribute
     */
    public String getUsemap() {

        return getAttribute(ATTR_USEMAP);
    }

    /**
     * Returns the value of the HTML "vspace" attribute.<p>
     *
     * @return the value of the HTML "vspace" attribute
     */
    public String getVspace() {

        return getAttribute(ATTR_VSPACE);
    }

    /**
     * Returns <code>"true"</code> if the HTML tag should only be created as partial tag.<p>
     *
     * @return <code>"true"</code> if the HTML tag should only be created as partial tag
     */
    public String isPartialTag() {

        return String.valueOf(m_partialTag);
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        m_attributes = null;
        m_partialTag = false;
        m_noDim = false;
        super.release();
    }

    /**
     * Sets the value of the HTML "align" attribute.<p>
     *
     * @param value the value of the HTML "align" attribute to set
     */
    public void setAlign(String value) {

        setAttribute(ATTR_ALIGN, value);
    }

    /**
     * Sets the value of the HTML "alt" attribute.<p>
     *
     * @param value the value of the HTML "alt" attribute to set
     */
    public void setAlt(String value) {

        setAttribute(ATTR_ALT, value, true);

    }

    /**
     * Sets the value of the HTML "border" attribute.<p>
     *
     * @param value the value of the HTML "border" attribute to set
     */
    public void setBorder(String value) {

        setAttribute(ATTR_BORDER, value);

    }

    /**
     * Sets the value of the HTML "class" attribute.<p>
     *
     * @param value the value of the HTML "class" attribute to set
     */
    public void setCssclass(String value) {

        setAttribute(ATTR_CLASS, value);

    }

    /**
     * Sets the value of the HTML "hspace" attribute.<p>
     *
     * @param value the value of the HTML "hspace" attribute to set
     */
    public void setHspace(String value) {

        setAttribute(ATTR_HSPACE, value);

    }

    /**
     * Sets the value of the HTML "id" attribute.<p>
     *
     * @param value the value of the HTML "id" attribute to set
     */
    @Override
    public void setId(String value) {

        setAttribute(ATTR_ID, value);

    }

    /**
     * Sets the value of the HTML "longdesc" attribute.<p>
     *
     * @param value the value of the HTML "longdesc" attribute to set
     */
    public void setLongdesc(String value) {

        setAttribute(ATTR_LONGDESC, value);

    }

    /**
     * Sets the value of the HTML "name" attribute.<p>
     *
     * @param value the value of the HTML "name" attribute to set
     */
    public void setName(String value) {

        setAttribute(ATTR_NAME, value);

    }

    /**
     * Controls if the created HTML image tag contains height and width attributes.<p>
     *
     * @param noDim the value to set
     */
    public void setNoDim(String noDim) {

        m_noDim = Boolean.valueOf(noDim).booleanValue();
    }

    /**
     * Controls if the created HTML image tag is a full or partial tag.<p>
     *
     * @param partialTag the value to set
     */
    public void setPartialTag(String partialTag) {

        m_partialTag = Boolean.valueOf(partialTag).booleanValue();
    }

    /**
     * Sets the value of the HTML "style" attribute.<p>
     *
     * @param value the value of the HTML "style" attribute to set
     */
    public void setStyle(String value) {

        setAttribute(ATTR_STYLE, value);
    }

    /**
     * Sets the value of the HTML "title" attribute.<p>
     *
     * @param value the value of the HTML "title" attribute to set
     */
    public void setTitle(String value) {

        setAttribute(ATTR_TITLE, value);
    }

    /**
     * Sets the value of the HTML "usemap" attribute.<p>
     *
     * @param value the value of the HTML "usemap" attribute to set
     */
    public void setUsemap(String value) {

        setAttribute(ATTR_USEMAP, value);
    }

    /**
     * Sets the value of the HTML "vspace" attribute.<p>
     *
     * @param value the value of the HTML "vspace" attribute to set
     */
    public void setVspace(String value) {

        setAttribute(ATTR_VSPACE, value);

    }

    /**
     * Returns the given keys attribute value from the attribute map.<p>
     *
     * @param key the attribute to read from the map
     * @return the given keys attribute value from the attribute map
     */
    private String getAttribute(String key) {

        if (m_attributes != null) {
            return m_attributes.get(key);
        }
        return null;
    }

    /**
     * Sets the given key with the given value in the attribute map.<p>
     *
     * @param key the key to set
     * @param value the value to set
     */
    private void setAttribute(String key, String value) {

        setAttribute(key, value, false);
    }

    /**
     * Sets the given key with the given value in the attribute map.<p>
     *
     * @param key the key to set
     * @param value the value to set
     * @param allowEmptyValue flag to determine if an empty value (not <code>null</code>!) should be set
     */
    private void setAttribute(String key, String value, boolean allowEmptyValue) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value) || (allowEmptyValue && (value != null))) {
            if (m_attributes == null) {
                m_attributes = new HashMap<String, String>();
            }
            m_attributes.put(key, value);
        }
    }
}
