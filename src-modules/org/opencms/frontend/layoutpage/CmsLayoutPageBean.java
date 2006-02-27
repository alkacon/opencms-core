/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/layoutpage/CmsLayoutPageBean.java,v $
 * Date   : $Date: 2006/02/27 13:18:53 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.frontend.layoutpage;

import com.alkacon.simapi.Simapi;

import org.opencms.file.CmsFile;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides special methods to build the predefined layout page output HTML.<p>
 * 
 * Please read the package information for details about modifying layouts or adding new layouts.<p>
 * 
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.1.9 
 */
public class CmsLayoutPageBean extends CmsJspActionElement {

    /** Default align type: paragraph with image on bottom, text above. */
    public static final String ALIGN_TYPE_BOTTOM = "imagebottom";

    /** Default align type: paragraph with image to the left. */
    public static final String ALIGN_TYPE_IMAGE_LEFT = "imageleft";

    /** Default align type: paragraph with image left, text to the right. */
    public static final String ALIGN_TYPE_IMAGE_LEFT_TEXT_RIGHT = "imageleft_textright";

    /** Default align type: paragraph with image to the right. */
    public static final String ALIGN_TYPE_IMAGE_RIGHT = "imageright";

    /** Default align type: paragraph with image right, text to the left. */
    public static final String ALIGN_TYPE_IMAGE_RIGHT_TEXT_LEFT = "imageright_textleft";

    /** Default align type: paragraph without image. */
    public static final String ALIGN_TYPE_TEXT_ONLY = "textonly";

    /** Align type: paragraph with image on top, text below. */
    public static final String ALIGN_TYPE_TOP = "imagetop";

    /** Default width for large images. */
    public static final int IMG_WIDTH_LARGE = 737;

    /** Default width for medium images. */
    public static final int IMG_WIDTH_MEDIUM = 200;

    /** Default width for small images. */
    public static final int IMG_WIDTH_SMALL = 100;

    /** Macro name for the image description macro. */
    public static final String MACRO_DESCRIPTION = "layout.description";

    /** Macro name for the headline macro. */
    public static final String MACRO_HEADLINE = "layout.headline";

    /** Macro name for the image macro. */
    public static final String MACRO_IMAGE = "layout.image";

    /** Macro name for the text macro. */
    public static final String MACRO_TEXT = "layout.text";

    /** Name of the frontend module in OpenCms. */
    public static final String MODULE_NAME = "org.opencms.frontend.layoutpage";

    /** Name of the align node. */
    public static final String NODE_ALIGN = "Align";

    /** Name of the description node. */
    public static final String NODE_DESCRIPTION = "Description";

    /** Name of the headline node. */
    public static final String NODE_HEADLINE = "Headline";

    /** Name of the Image node. */
    public static final String NODE_IMAGE = "Image";

    /** Name of the paragraph node. */
    public static final String NODE_PARAGRAPH = "Paragraph";

    /** Name of the text node. */
    public static final String NODE_TEXT = "Text";

    /** Default VFS path to the html snippet files to include to render the layout paragraphs. */
    public static final String VFS_PATH_LAYOUTELEMENTS = CmsWorkplace.VFS_PATH_MODULES + MODULE_NAME + "/layouts/";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLayoutPageBean.class);

    /** The XML content that stores the layout. */
    private CmsXmlContent m_content;

    /** Stores the possible layout patterns. */
    private Map m_layoutPatterns;

    /** The VFS path to the html snippet files.  */
    private String m_pathLayoutElements;

    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsLayoutPageBean() {

        super();
    }

    /**
     * Constructor, with parameters.<p>
     * 
     * Use this constructor for the template.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsLayoutPageBean(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super();
        init(context, req, res);
    }

    /**
     * Adds a pattern with the provided name and image width to the possible patterns to show.<p>
     * 
     * @param patternName the name of the pattern layout
     * @param imgWidth the image width to use
     */
    public void addLayoutPattern(String patternName, int imgWidth) {

        m_layoutPatterns.put(patternName, new Integer(imgWidth));
    }

    /**
     * Returns the HTML for the paragraphs to display.<p>
     * 
     * @return the HTML for the paragraphs to display
     */
    public String buildHtmlParagraphs() {

        StringBuffer result = new StringBuffer(16384);
        Locale locale = getRequestContext().getLocale();
        Iterator i = m_content.getValues(NODE_PARAGRAPH, locale).iterator();
        while (i.hasNext()) {
            // loop all paragraph nodes
            I_CmsXmlContentValue value = (I_CmsXmlContentValue)i.next();
            String xPath = value.getPath() + "/";

            // get the optional headline
            String headline = "";
            if (m_content.hasValue(xPath + NODE_HEADLINE, locale)) {
                headline = m_content.getStringValue(getCmsObject(), xPath + NODE_HEADLINE, locale);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(headline)) {
                    headline = "<h2 class=\"lp_headline\">" + headline + "</h2>\n";
                }
            }

            // get the text value
            String textValue = m_content.getStringValue(getCmsObject(), xPath + NODE_TEXT, locale);

            // process optional image
            xPath += NODE_IMAGE + "/";
            String imgDesc = "";
            String imgAlign = "";
            String imgTag = "";
            if (m_content.hasValue(xPath, locale)) {
                // image node found, check VFS presence
                String imgUri = m_content.getStringValue(getCmsObject(), xPath + NODE_IMAGE, locale);
                if (getCmsObject().existsResource(imgUri)) {
                    // image exists, create image tag to show
                    if (m_content.hasValue(xPath + NODE_DESCRIPTION, locale)) {
                        // get image description
                        imgDesc = m_content.getStringValue(getCmsObject(), xPath + NODE_DESCRIPTION, locale);
                    }
                    imgAlign = m_content.getStringValue(getCmsObject(), xPath + NODE_ALIGN, locale);
                    CmsImageScaler scaler = getImageScaler(imgAlign);
                    // create image tag with additional "alt" and "title" attributes
                    Map attrs = new HashMap(5);
                    attrs.put("alt", imgDesc);
                    attrs.put("title", imgDesc);
                    imgTag = img(imgUri, scaler, attrs);
                }
            }

            if (CmsStringUtil.isEmpty(imgAlign)) {
                // set default display type (in case no image was found)
                imgAlign = ALIGN_TYPE_TEXT_ONLY;
            }

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(imgDesc)) {
                // format image description string
                imgDesc = "<p class=\"lp_imgdesc\">" + imgDesc + "</p>";
            }

            // get the HTML layout containing macros to use for this paragraph
            String elementContent = getContent(m_pathLayoutElements + imgAlign);
            CmsMacroResolver resolver = CmsMacroResolver.newInstance();
            // fill in macro values
            resolver.addMacro(MACRO_HEADLINE, headline);
            resolver.addMacro(MACRO_TEXT, textValue);
            resolver.addMacro(MACRO_IMAGE, imgTag);
            resolver.addMacro(MACRO_DESCRIPTION, imgDesc);

            // add resolved layout element to result
            result.append(resolver.resolveMacros(elementContent));
        }

        return result.toString();
    }

    /**
     * Fills the default layout patterns to show.<p>
     */
    public void createDefaultLayoutPatterns() {

        m_layoutPatterns.put(ALIGN_TYPE_BOTTOM, new Integer(IMG_WIDTH_LARGE));
        m_layoutPatterns.put(ALIGN_TYPE_TOP, new Integer(IMG_WIDTH_LARGE));
        m_layoutPatterns.put(ALIGN_TYPE_IMAGE_LEFT, new Integer(IMG_WIDTH_MEDIUM));
        m_layoutPatterns.put(ALIGN_TYPE_IMAGE_RIGHT, new Integer(IMG_WIDTH_MEDIUM));
        m_layoutPatterns.put(ALIGN_TYPE_IMAGE_LEFT_TEXT_RIGHT, new Integer(IMG_WIDTH_SMALL));
        m_layoutPatterns.put(ALIGN_TYPE_IMAGE_RIGHT_TEXT_LEFT, new Integer(IMG_WIDTH_SMALL));
    }

    /**
     * Returns the layout patterns with the layout name as key, the value is the corresponding image width.<p>
     * 
     * @return the layout patterns with the layout name as key, the value is the corresponding image width
     */
    public Map getLayoutPatterns() {

        return m_layoutPatterns;
    }

    /**
     * Returns the VFS path to the html snippet files to include to render the layout paragraphs.<p>
     * 
     * @return the VFS path to the html snippet files to include to render the layout paragraphs
     */
    public String getPathLayoutElements() {

        return m_pathLayoutElements;
    }

    /**
     * Initialize this bean with the current page context, request and response.<p>
     * 
     * It is required to call one of the init() methods before you can use the 
     * instance of this bean.
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public void init(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super.init(context, req, res);
        // set default path to html snippet files to use
        m_pathLayoutElements = VFS_PATH_LAYOUTELEMENTS;
        // read layout configuration XML content file
        try {
            CmsFile file = getCmsObject().readFile(getRequestContext().getUri());
            m_content = CmsXmlContentFactory.unmarshal(getCmsObject(), file);
        } catch (CmsException e) {
            // log error if reading resource fails
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(Messages.LOG_ERR_VFS_RESOURCE_1, getRequestContext().getUri()));
            }
        }
        // initialize pattern Map
        m_layoutPatterns = new HashMap(10);
    }

    /**
     * Sets the layout patterns with the layout name as key, the value is the corresponding image width.<p>
     * 
     * @param layoutPatterns the layout patterns with the layout name as key, the value is the corresponding image width
     */
    public void setLayoutPatterns(Map layoutPatterns) {

        m_layoutPatterns = layoutPatterns;
    }

    /**
     * Sets the VFS path to the html snippet files to include to render the layout paragraphs.<p>
     * 
     * @param pathLayoutElements the VFS path to the html snippet files to include to render the layout paragraphs
     */
    public void setPathLayoutElements(String pathLayoutElements) {

        m_pathLayoutElements = pathLayoutElements;
    }

    /**
     * Returns an initialized image scaler depending on the image align to use.<p>
     * 
     * @param imgAlign the image align to use for a paragraph
     * @return an initialized image scaler depending on the image align to use
     */
    protected CmsImageScaler getImageScaler(String imgAlign) {

        // create scaler instance
        CmsImageScaler scaler = new CmsImageScaler();
        scaler.setType(1);
        scaler.setPosition(Simapi.POS_DOWN_LEFT);

        // get the image width from layout pattern Map
        Integer imgWidth = (Integer)getLayoutPatterns().get(imgAlign);
        if (imgWidth != null) {
            // found a width value
            scaler.setWidth(imgWidth.intValue());
        } else {
            // did not find a value, provide a default width
            scaler.setWidth(IMG_WIDTH_MEDIUM);
        }

        return scaler;
    }

}
