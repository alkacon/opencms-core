/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagImage.java,v $
 * Date   : $Date: 2011/03/23 14:51:34 $
 * Version: $Revision: 1.14 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * Creates HTML code for &lt;img src&gt; tags that use the OpenCms image scaling capabilities.<p>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 6.2.0 
 */
public class CmsJspTagImage extends BodyTagSupport implements I_CmsJspTagParamParent {

    // optional HTML attribute constants
    private static final String ATTR_ALIGN = "align";
    private static final String ATTR_ALT = "alt";
    private static final String ATTR_BORDER = "border";
    private static final String ATTR_HSPACE = "hspace";
    private static final String ATTR_ID = "id";
    private static final String ATTR_LONGDESC = "longdesc";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_STYLE = "style";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_USEMAP = "usemap";
    private static final String ATTR_VSPACE = "vspace";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagImage.class);

    // image scaler required attribute constants
    private static final String SCALE_ATTR_COLOR = "scalecolor";
    private static final String SCALE_ATTR_FILTER = "scalefilter";
    private static final String SCALE_ATTR_HEIGHT = "height";
    private static final String SCALE_ATTR_PARTIALTAG = "partialtag";
    private static final String SCALE_ATTR_POSITION = "scaleposition";
    private static final String SCALE_ATTR_QUALITY = "scalequality";
    private static final String SCALE_ATTR_RENDERMODE = "scalerendermode";
    private static final String SCALE_ATTR_SRC = "src";
    private static final String SCALE_ATTR_TYPE = "scaletype";
    private static final String SCALE_ATTR_WIDTH = "width";

    // lists for fast lookup
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
        SCALE_ATTR_WIDTH};
    private static final List SCALER_ATTRS_LIST = Arrays.asList(SCALER_ATTRS);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 6513320107441256414L;

    /** Map with additionally set image attributes not needed by the image scaler. */
    private Map m_attributes;

    /** Controls if the created HTML image tag is a full or partial tag. */
    private boolean m_partialTag;

    /** The given image scaler parameters. */
    private transient CmsImageScaler m_scaler;

    /** The image source. */
    private String m_src;

    /**
     * Creates a new image scaling tag instance.<p>
     */
    public CmsJspTagImage() {

        // initialize the image scaler parameter container
        m_scaler = new CmsImageScaler();
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
        Map attributes,
        boolean partialTag,
        ServletRequest req) throws CmsException {

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();

        // resolve possible relative URI
        src = CmsLinkManager.getAbsoluteUri(src, controller.getCurrentRequest().getElementUri());
        CmsUriSplitter splitSrc = new CmsUriSplitter(src);

        CmsResource imageRes = cms.readResource(splitSrc.getPrefix());
        CmsImageScaler reScaler = null;
        if (splitSrc.getQuery() != null) {
            // check if the original URI already has parameters, this is true if original has been cropped
            String[] scaleStr = (String[])CmsRequestUtil.createParameterMap(splitSrc.getQuery()).get(
                CmsImageScaler.PARAM_SCALE);
            if (scaleStr != null) {
                // use cropped image as a base for scaling
                reScaler = new CmsImageScaler(scaleStr[0]);
                scaler = reScaler.getCropScaler(scaler);
            }
        }

        // calculate target scale dimensions (if required)  
        if ((scaler.getHeight() <= 0) || (scaler.getWidth() <= 0)) {
            // read the image properties for the selected resource
            CmsImageScaler original = new CmsImageScaler(cms, imageRes);
            if (original.isValid()) {
                scaler = original.getReScaler(scaler);
            }
        }

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

        if (scaler.isValid()) {
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
            Iterator i = attributes.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry)i.next();
                String attr = (String)entry.getKey();
                String value = (String)entry.getValue();
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
            default: // no a value used by the image scaler, treat as HTML attribute
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
                    imageTag = imageTagAction(m_src, m_scaler, m_attributes, m_partialTag, req);
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
        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
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
     * Returns the scaling height for the image.<p>
     * 
     * @return the scaling height for the image
     */
    public String getHeight() {

        return String.valueOf(m_scaler.getHeight());
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
     * Returns the scaling width for the image.<p>
     * 
     * @return the scaling width for the image
     */
    public String getWidth() {

        return String.valueOf(m_scaler.getWidth());
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
        m_scaler = new CmsImageScaler();
        m_partialTag = false;
        m_src = null;
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

        setAttribute(ATTR_ALT, value);

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
     * Sets the scaling height for the image.<p>
     * 
     * If no valid integer is given, then "0" is used as value.<p>
     * 
     * @param value the scaling height for the image to set
     */
    public void setHeight(String value) {

        m_scaler.setHeight(CmsStringUtil.getIntValue(value, 0, SCALE_ATTR_HEIGHT));
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
     * Controls if the created HTML image tag is a full or partial tag.<p>
     *
     * @param partialTag the value to set
     */
    public void setPartialTag(String partialTag) {

        m_partialTag = Boolean.valueOf(partialTag).booleanValue();
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
     * Sets the scaling width for the image.<p>
     * 
     * If no valid integer is given, then "0" is used as value.<p>
     * 
     * @param value the scaling width for the image to set
     */
    public void setWidth(String value) {

        m_scaler.setWidth(CmsStringUtil.getIntValue(value, 0, SCALE_ATTR_WIDTH));
    }

    /**
     * Returns the given keys attribute value from the attribute map.<p>
     * 
     * @param key the attribute to read from the map
     * @return the given keys attribute value from the attribute map
     */
    private String getAttribute(String key) {

        if (m_attributes != null) {
            return (String)m_attributes.get(key);
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

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
            if (m_attributes == null) {
                m_attributes = new HashMap();
            }
            m_attributes.put(key, value);
        }
    }
}