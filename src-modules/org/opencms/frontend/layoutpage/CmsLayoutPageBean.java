/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/layoutpage/CmsLayoutPageBean.java,v $
 * Date   : $Date: 2011/03/23 14:50:01 $
 * Version: $Revision: 1.8 $
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

package org.opencms.frontend.layoutpage;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;

/**
 * Provides special methods to build the predefined layout page output HTML.<p>
 * 
 * Please read the package information for details about modifying layouts or adding new layouts.<p>
 * 
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.1.9 
 */
public class CmsLayoutPageBean {

    /** Columns layout: one column. */
    public static final String COLUMNS_LAYOUT_1 = "1col";

    /** Columns layout: two columns. */
    public static final String COLUMNS_LAYOUT_2 = "2col";

    /** Image Link variant: provide link to larger version. */
    public static final String IMG_LINK_LARGER = "linklarger";

    /** Image Link variant: no link to larger version. */
    public static final String IMG_LINK_NONE = "nolink";

    /** Image width variant name for large images. */
    public static final String IMG_WIDTH_LARGE = "large";

    /** Image width variant name for medium images. */
    public static final String IMG_WIDTH_MEDIUM = "medium";

    /** Image width variant name for small images. */
    public static final String IMG_WIDTH_SMALL = "small";

    /** Macro name for the image description macro. */
    public static final String MACRO_DESCRIPTION = "description";

    /** Macro name for the headline macro. */
    public static final String MACRO_HEADLINE = "headline";

    /** Macro name for the image macro. */
    public static final String MACRO_IMAGE = "image";

    /** Macro name for the image width macro. */
    public static final String MACRO_IMAGE_WIDTH = "imagewidth";

    /** Macro name for the target macro. */
    public static final String MACRO_TARGET = "target";

    /** Macro name for the text macro. */
    public static final String MACRO_TEXT = "text";

    /** Macro name for the title macro. */
    public static final String MACRO_TITLE = "title";

    /** The name of the module. */
    public static final String MODULE_NAME = "org.opencms.frontend.layoutpage";

    /** Paragraph type: paragraph with image on bottom, text above. */
    public static final String PARAGRAPH_TYPE_BOTTOM = "imagebottom";

    /** Paragraph type: paragraph with image on bottom and description, text above. */
    public static final String PARAGRAPH_TYPE_BOTTOM_DESCRIPTION = "imagebottom_desc";

    /** Paragraph type: paragraph with image to the left. */
    public static final String PARAGRAPH_TYPE_IMAGE_LEFT = "imageleft";

    /** Paragraph type: paragraph with image to the left and image description. */
    public static final String PARAGRAPH_TYPE_IMAGE_LEFT_DESCRIPTION = "imageleft_desc";

    /** Paragraph type: paragraph with image left, text to the right. */
    public static final String PARAGRAPH_TYPE_IMAGE_LEFT_TEXT_RIGHT = "imageleft_textright";

    /** Paragraph type: paragraph with image left, text to the right and image description. */
    public static final String PARAGRAPH_TYPE_IMAGE_LEFT_TEXT_RIGHT_DESCRIPTION = "imageleft_textright_desc";

    /** Paragraph type: paragraph with image to the right. */
    public static final String PARAGRAPH_TYPE_IMAGE_RIGHT = "imageright";

    /** Paragraph type: paragraph with image to the right and image description. */
    public static final String PARAGRAPH_TYPE_IMAGE_RIGHT_DESCRIPTION = "imageright_desc";

    /** Paragraph type: paragraph with image right, text to the left. */
    public static final String PARAGRAPH_TYPE_IMAGE_RIGHT_TEXT_LEFT = "imageright_textleft";

    /** Paragraph type: paragraph with image right, text to the left and image description. */
    public static final String PARAGRAPH_TYPE_IMAGE_RIGHT_TEXT_LEFT_DESCRIPTION = "imageright_textleft_desc";

    /** Paragraph type: paragraph without image. */
    public static final String PARAGRAPH_TYPE_TEXT_ONLY = "textonly";

    /** Paragraph type: paragraph with image on top, text below. */
    public static final String PARAGRAPH_TYPE_TOP = "imagetop";

    /** Paragraph type: paragraph with image on top and description, text below. */
    public static final String PARAGRAPH_TYPE_TOP_DESCRIPTION = "imagetop_desc";

    /** Default VFS path to the html snippet files to include to render the layout paragraphs. */
    public static final String VFS_PATH_LAYOUTELEMENTS = CmsWorkplace.VFS_PATH_MODULES + MODULE_NAME + "/layouts/";

    /** Name of the align node. */
    protected static final String NODE_ALIGN = "Align";

    /** Name of the columns layout node. */
    protected static final String NODE_COLUMNS_LAYOUT = "ColumnsLayout";

    /** Name of the description node. */
    protected static final String NODE_DESCRIPTION = "Description";

    /** Name of the headline node. */
    protected static final String NODE_HEADLINE = "Headline";

    /** Name of the Image node. */
    protected static final String NODE_IMAGE = "Image";

    /** Name of the ImageOptions node. */
    protected static final String NODE_IMAGEOPTIONS = "ImageOptions";

    /** Name of the paragraph node. */
    protected static final String NODE_PARAGRAPH = "Paragraph";

    /** Name of the text node. */
    protected static final String NODE_TEXT = "Text";

    /** Name of the file link node. */
    protected static final String NODE_FILELINK = "Filelink";

    /** Name of the file link node. */
    protected static final String KEY_HEADLINE = "headline";

    /** Name of the file link node. */
    protected static final String KEY_TEXTVALUE = "textValue";

    /** Name of the file link node. */
    protected static final String KEY_IMGURI = "imgUri";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLayoutPageBean.class);

    /** The width of the content area of the template. */
    private int m_bodyWdith;

    /** The padding of the columns in the content area. */
    private int m_colPadding;

    /** The spacing of the columns in the content area. */
    private int m_colSpacing;

    /** The column layout variant of the parapgraphs. */
    private String m_columnLayout;

    /** The calculated width of a single paragraph column. */
    private int m_columnWidth;

    /** The XML content that configures the layout. */
    private CmsXmlContent m_content;

    /** Indicates if the layout images should be fixed or calculated from the content area width. */
    private boolean m_fixedImageSize;

    /** The image width to use for large images in fixed image size mode. */
    private int m_imgWidthLarge;

    /** The image width to use for medium images in fixed image size mode. */
    private int m_imgWidthMedium;

    /** The image width to use for small images in fixed image size mode. */
    private int m_imgWidthSmall;

    /** The JSP action element to get access to the OpenCms API. */
    private CmsJspActionElement m_jspActionElement;

    /** The possible patterns to show with corresponding image widths to use. */
    private Map m_layoutPatterns;

    /** The VFS path to the html snippet files.  */
    private String m_pathLayoutElements;

    /** The map to store the information of the .properties files for each integrated xml content type.*/
    private Map m_typeMappings;

    /** The layout variant to show, e.g. "common", "print" or "accessibe". */
    private String m_variant;

    /**
     * Empty constructor, required for every JavaBean.<p>
     * 
     * It is required to call either the init() method or set the members manually before you can use the 
     * instance of this bean.
     */
    public CmsLayoutPageBean() {

        super();
    }

    /**
     * Constructor, with parameters.<p>
     * 
     * Use this constructor for the template.<p>
     * 
     * @param jsp the current JSP action element
     * @param content the XML content that configures the layout
     * @param variant the layout variant to show, e.g. "common", "print" or "accessibe"
     * @param bodyWith the width of the content area of the template
     * @param colPadding the padding of the columns in the content area
     * @param colSpacing the spacing of the columns in the content area
     */
    public CmsLayoutPageBean(
        CmsJspActionElement jsp,
        CmsXmlContent content,
        String variant,
        int bodyWith,
        int colPadding,
        int colSpacing) {

        super();
        init(jsp, content, variant, bodyWith, colPadding, colSpacing);
    }

    /**
     * Returns the HTML for the large image to show in a popup window with a desired width of 600px.<p>
     * 
     * @param cms the initialized JSP action element
     * @param imgUri the URI of the image to link to
     * @param imgSize the image size property value containing the original image information
     * @return the HTML for the large image to show in a popup window
     */
    public static String buildLargeImageTag(CmsJspActionElement cms, String imgUri, String imgSize) {

        // create scaler instance of original image
        CmsImageScaler origImage = new CmsImageScaler(imgSize);
        // create scaler with desired image width
        CmsImageScaler scaler = new CmsImageScaler();
        scaler.setWidth(600);
        // return scaler with result image width
        CmsImageScaler resultScaler = origImage.getWidthScaler(scaler);
        return cms.img(imgUri, resultScaler, null);
    }

    /**
     * Adds a paragraph layout pattern with the provided name and image width variant name to the possible patterns to show.<p>
     * 
     * Possible image width variants are:<p>
     * <ul>
     * <li>{@link #IMG_WIDTH_SMALL}: small image</li>
     * <li>{@link #IMG_WIDTH_MEDIUM}: medium image</li>
     * <li>{@link #IMG_WIDTH_LARGE}: large image</li>
     * </ul>
     * 
     * @param patternName the name of the pattern layout
     * @param imgWidthVariant the image width variant name to use
     */
    public void addLayoutPattern(String patternName, String imgWidthVariant) {

        m_layoutPatterns.put(patternName, imgWidthVariant);
    }

    /**
     * Returns the HTML for the paragraphs to display.<p>
     * 
     * @return the HTML for the paragraphs to display
     */
    public String buildHtmlParagraphs() {

        StringBuffer result = new StringBuffer(16384);
        Locale locale = getCmsObject().getRequestContext().getLocale();
        
        m_typeMappings = new HashMap();

        // first calculate the column width
        calculateColumnWidth();

        // get the macros
        I_CmsMacroWrapper macros = null;
        try {
            macros = getMacroWrapper();
        } catch (Exception e) {
            // log error and stop output
            if (!(e instanceof CmsException)) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage());
                }
            }
            return "";
        }

        // determine if two columns should be shown
        boolean showTwoCols = getColumnLayout().indexOf(COLUMNS_LAYOUT_2) != -1;
        // determine if image links to larger image version should be shown
        boolean showImgLinks = IMG_LINK_LARGER.equals(m_content.getStringValue(
            getCmsObject(),
            NODE_IMAGEOPTIONS,
            locale));

        // determine localized image link title
        String imgLinkTitleLocalized = "";
        CmsMessages messages = getCmsJspActionElement().getMessages(
            "org/opencms/frontend/layoutpage/frontendmessages",
            locale);
        if (showImgLinks) {
            imgLinkTitleLocalized = messages.keyDefault("link.image.original", "");
        }

        // get the paragraph nodes from the XML content
        Iterator i = m_content.getValues(NODE_PARAGRAPH, locale).iterator();

        // open column table
        result.append(macros.getResult("content_start"));

        // set variables needed in loop to determine correct column to show
        boolean firstInRow = true;

        while (i.hasNext()) {
            // loop all paragraph nodes
            I_CmsXmlContentValue value = (I_CmsXmlContentValue)i.next();
            String xPath = value.getPath() + "/";

            // check if a file has to be integrated
            boolean hasFileLink = m_content.hasValue(xPath + NODE_FILELINK, locale);
            
            ExtendedProperties xmlElementsProperties = null;
            CmsFile linkToFile = null;
            CmsXmlContent xmlContentFileLink = null;
            if (hasFileLink) {
                try {
                    // read the integrated file, get the xml content of it and recieve the properties for
                    // the type of this resource
                    linkToFile = getCmsObject().readFile(
                        m_content.getStringValue(getCmsObject(), xPath + NODE_FILELINK, locale));
                    xmlContentFileLink = CmsXmlContentFactory.unmarshal(getCmsObject(), linkToFile);
                    xmlElementsProperties = getXmlElementsProperties(linkToFile);
                } catch (Exception e) {
                    // if reading of external file fails the external file will be ignored
                    hasFileLink = false;
                }
            }
            
            // get the optional headline
            String headline = "";
            if (m_content.hasValue(xPath + NODE_HEADLINE, locale)) {
                headline = m_content.getStringValue(getCmsObject(), xPath + NODE_HEADLINE, locale);
            }
            // if headline is empty try to get it from the integrated xml content
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(headline)) {
                if (hasFileLink) {
                    String titleValue = this.getPropertiesValue(
                        xmlElementsProperties,
                        KEY_HEADLINE,
                        xmlContentFileLink,
                        locale);
                    if (!CmsStringUtil.isEmptyOrWhitespaceOnly(titleValue)) {
                        // if titleValue is not empty set the headline to this value and build a link
                        // to the integrated file around it 
                        headline = "<a title=\""
                            + titleValue
                            + "\" href=\""
                            + m_jspActionElement.link(m_content.getStringValue(
                                getCmsObject(),
                                xPath + NODE_FILELINK,
                                locale))
                            + "\" target=\"_self\">"
                            + titleValue
                            + "</a>";
                    } else {
                        headline = "";
                    }
                } else {
                    headline = "";
                }
            }

            // get the paragraph text value
            String textValue = m_content.getStringValue(getCmsObject(), xPath + NODE_TEXT, locale);
            
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(textValue)) {
                if (hasFileLink) {
                    String text = getPropertiesValue(xmlElementsProperties, KEY_TEXTVALUE, xmlContentFileLink, locale);
                    if (!CmsStringUtil.isEmptyOrWhitespaceOnly(text)) {
                        // if text is not empty set textValue to it, trim the size and
                        // append a link to the integrated file
                        textValue = text;
                        textValue = CmsStringUtil.trimToSize(textValue, 250);
                        textValue += "<a href=\""
                            + m_jspActionElement.link(m_content.getStringValue(
                                getCmsObject(),
                                xPath + NODE_FILELINK,
                                locale))
                            + "\" target=\"_self\">&gt; "
                            + messages.keyDefault("link.more", "")
                            + "</a>";
                    } else {
                        textValue = "";
                    }
                } else {
                    textValue = "";
                }
            }

            // process optional image
            xPath += NODE_IMAGE + "/";
            String imgDesc = "";
            String paragraphType = "";
            String imgTag = "";
            String imgUrl = "";
            int imgWidth = getColumnWidth();
            boolean imagePresent = false;

            if (m_content.hasValue(xPath, locale)) {
                // image node found, check VFS presence by reading image size property
                String imgUri = m_content.getStringValue(getCmsObject(), xPath + NODE_IMAGE, locale);
                
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(imgUri)) {
                    if (hasFileLink) {
                        String imgValue = getPropertiesValue(
                            xmlElementsProperties,
                            KEY_IMGURI,
                            xmlContentFileLink,
                            locale);
                        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(imgValue)) {
                            imgUri = imgValue;
                        } else {
                            imgUri = "";
                        }
                    } else {
                        imgUri = "";
                    }
                }
                
                String imgSize = null;
                try {
                    imgSize = getCmsObject().readPropertyObject(
                        imgUri,
                        CmsPropertyDefinition.PROPERTY_IMAGE_SIZE,
                        false).getValue();
                } catch (CmsException e) {
                    // file property not found, ignore
                }
                if (imgSize != null) {
                    // image exists, create image tag to show
                    imagePresent = true;
                    if (m_content.hasValue(xPath + NODE_DESCRIPTION, locale)) {
                        // get image description
                        imgDesc = m_content.getStringValue(getCmsObject(), xPath + NODE_DESCRIPTION, locale);
                    }
                    paragraphType = m_content.getStringValue(getCmsObject(), xPath + NODE_ALIGN, locale);
                    // get initialized image scaler for the image                   
                    CmsImageScaler scaler = getImageScaler(paragraphType, imgSize);
                    imgWidth = scaler.getWidth();

                    // determine image description String to show
                    String imgTitle = imgDesc;
                    if (showImgLinks) {
                        // append localized note for large image link to description
                        StringBuffer tempImgTitle = new StringBuffer(128);
                        tempImgTitle.append(imgDesc);
                        if (CmsStringUtil.isNotEmpty(imgDesc)) {
                            tempImgTitle.append(" ");
                        }
                        tempImgTitle.append(imgLinkTitleLocalized);
                        imgTitle = tempImgTitle.toString();
                    }

                    // create image tag with additional "alt", "title" and "border" attributes
                    Map attrs = new HashMap(4);
                    attrs.put("alt", imgTitle);
                    attrs.put("title", imgTitle);
                    attrs.put("border", "0");
                    imgTag = getCmsJspActionElement().img(imgUri, scaler, attrs);

                    // create link around image if configured
                    macros.putContextVariable(MACRO_IMAGE, imgTag);
                    if (showImgLinks) {
                        // use macro with link
                        imgUrl = getLinkToLargeImage(imgUri, imgSize);
                        macros.putContextVariable(MACRO_TITLE, imgTitle);
                        macros.putContextVariable(MACRO_TARGET, imgUrl);
                        imgTag = macros.getResult("image_with_link");
                    } else {
                        // use macro without link
                        imgTag = macros.getResult("image_without_link");
                    }

                }
            }

            macros.putContextVariable(MACRO_DESCRIPTION, imgDesc);
            if (imagePresent && showImgLinks) {
                // use description with link to image
                macros.putContextVariable(MACRO_TITLE, imgLinkTitleLocalized);
                macros.putContextVariable(MACRO_TARGET, imgUrl);
                imgDesc = macros.getResult("description_with_link");
            } else {
                // use description without link to image
                imgDesc = macros.getResult("description_without_link");
            }

            if (CmsStringUtil.isEmpty(paragraphType)) {
                // set default paragraph display type (in case no image was found)
                paragraphType = PARAGRAPH_TYPE_TEXT_ONLY;
            }

            // create paragraph output

            if (firstInRow || !showTwoCols) {
                // open row (tr) 
                result.append(macros.getResult("row_start"));
            }

            // open td
            result.append(macros.getResult("element_start"));

            // put macro variables in context to use for this paragraph
            macros.putContextVariable(MACRO_HEADLINE, headline);
            macros.putContextVariable(MACRO_TEXT, textValue);
            macros.putContextVariable(MACRO_IMAGE, imgTag);
            macros.putContextVariable(MACRO_IMAGE_WIDTH, new Integer(imgWidth));
            macros.putContextVariable(MACRO_DESCRIPTION, imgDesc);
            // add resolved macro layout paragraph to result
            result.append(macros.getResult(paragraphType));

            // close td
            result.append(macros.getResult("element_end"));

            if (!showTwoCols || !firstInRow || (firstInRow && !i.hasNext())) {
                if (showTwoCols && firstInRow && !i.hasNext()) {
                    // append additional empty dummy element in two column mode
                    result.append(macros.getResult("element_start"));
                    result.append(macros.getResult("element_end"));
                }
                // close row (tr)
                result.append(macros.getResult("row_end"));
            }

            firstInRow = !firstInRow;
        }

        // close column table
        result.append(macros.getResult("content_end"));

        return result.toString();
    }

    /**
     * Fills the default paragraph layout patterns to show.<p>
     */
    public void createDefaultLayoutPatterns() {

        m_layoutPatterns.put(PARAGRAPH_TYPE_BOTTOM, IMG_WIDTH_LARGE);
        m_layoutPatterns.put(PARAGRAPH_TYPE_BOTTOM_DESCRIPTION, IMG_WIDTH_LARGE);
        m_layoutPatterns.put(PARAGRAPH_TYPE_TOP, IMG_WIDTH_LARGE);
        m_layoutPatterns.put(PARAGRAPH_TYPE_TOP_DESCRIPTION, IMG_WIDTH_LARGE);
        m_layoutPatterns.put(PARAGRAPH_TYPE_IMAGE_LEFT, IMG_WIDTH_MEDIUM);
        m_layoutPatterns.put(PARAGRAPH_TYPE_IMAGE_LEFT_DESCRIPTION, IMG_WIDTH_MEDIUM);
        m_layoutPatterns.put(PARAGRAPH_TYPE_IMAGE_RIGHT, IMG_WIDTH_MEDIUM);
        m_layoutPatterns.put(PARAGRAPH_TYPE_IMAGE_RIGHT_DESCRIPTION, IMG_WIDTH_MEDIUM);
        m_layoutPatterns.put(PARAGRAPH_TYPE_IMAGE_LEFT_TEXT_RIGHT, IMG_WIDTH_SMALL);
        m_layoutPatterns.put(PARAGRAPH_TYPE_IMAGE_LEFT_TEXT_RIGHT_DESCRIPTION, IMG_WIDTH_SMALL);
        m_layoutPatterns.put(PARAGRAPH_TYPE_IMAGE_RIGHT_TEXT_LEFT, IMG_WIDTH_SMALL);
        m_layoutPatterns.put(PARAGRAPH_TYPE_IMAGE_RIGHT_TEXT_LEFT_DESCRIPTION, IMG_WIDTH_SMALL);
    }

    /**
     * Returns the width of the content area of the template.<p>
     *
     * @return the width of the content area of the template
     */
    public int getBodyWidth() {

        return m_bodyWdith;
    }

    /**
     * Returns the padding of the columns in the content area.<p>
     *
     * @return the padding of the columns in the content area
     */
    public int getColPadding() {

        return m_colPadding;
    }

    /**
     * Returns the spacing of the columns in the content area.<p>
     *
     * @return the spacing of the columns in the content area
     */
    public int getColSpacing() {

        return m_colSpacing;
    }

    /**
     * Returns the column layout of the parapgraphs in the content area.<p>
     *
     * @return the column layout of the parapgraphs in the content area
     */
    public String getColumnLayout() {

        return m_columnLayout;
    }

    /**
     * Returns the calculated width of a single paragraph column.<p>
     *
     * @return the calculated width of a single paragraph column
     */
    public int getColumnWidth() {

        return m_columnWidth;
    }

    /**
     * Returns the XML content that stores the layout.<p>
     *
     * @return the XML content that stores the layout
     */
    public CmsXmlContent getContent() {

        return m_content;
    }

    /**
     * Returns the image width to use for large images in fixed image size mode.<p>
     *
     * @return the image width to use for large images in fixed image size mode
     */
    public int getImgWidthLarge() {

        return m_imgWidthLarge;
    }

    /**
     * Returns the image width to use for medium images in fixed image size mode.<p>
     *
     * @return the image width to use for medium images in fixed image size mode
     */
    public int getImgWidthMedium() {

        return m_imgWidthMedium;
    }

    /**
     * Returns the image width to use for small images in fixed image size mode.<p>
     *
     * @return the image width to use for small images in fixed image size mode
     */
    public int getImgWidthSmall() {

        return m_imgWidthSmall;
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
     * Returns the layout variant to show, e.g. "common", "print" or "accessibe".<p>
     *
     * @return the layout variant to show, e.g. "common", "print" or "accessibe"
     */
    public String getVariant() {

        return m_variant;
    }

    /**
     * Initialize this bean with the OpenCms user context, the XML content, the layout variant and width information.<p>
     * 
     * It is required to call either the init() method or set the members manually before you can use the 
     * instance of this bean.
     * 
     * @param jsp the current JSP action element
     * @param content the XML content that configures the layout
     * @param variant the layout variant to show, e.g. "common", "print" or "accessibe"
     * @param bodyWith the width of the content area of the template
     * @param colPadding the padding of the columns in the content area
     * @param colSpacing the spacing of the columns in the content area
     */
    public void init(
        CmsJspActionElement jsp,
        CmsXmlContent content,
        String variant,
        int bodyWith,
        int colPadding,
        int colSpacing) {

        // set default path to html snippet files to use
        setPathLayoutElements(VFS_PATH_LAYOUTELEMENTS);
        // set CmsObject
        setCmsJspActionElement(jsp);
        // set layout configuration XML content file
        setContent(content);
        // set the variant to display
        setVariant(variant);

        // set column layout defined in content
        String layout = m_content.getStringValue(
            getCmsObject(),
            NODE_COLUMNS_LAYOUT,
            jsp.getRequestContext().getLocale());
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(layout)) {
            layout = COLUMNS_LAYOUT_1;
        }
        setColumnLayout(layout);

        // set the size information values
        setColPadding(colPadding);
        setColSpacing(colSpacing);
        setBodyWidth(bodyWith);

        // initialize pattern Map
        setLayoutPatterns(new HashMap(16));
    }

    /**
     * Returns if the layout images should be fixed or calculated from the content area width.<p>
     *
     * @return if the layout images should be fixed or calculated from the content area width
     */
    public boolean isFixedImageSize() {

        return m_fixedImageSize;
    }

    /**
     * Sets the width of the content area of the template.<p>
     *
     * @param bodyWidth the width of the content area of the template
     */
    public void setBodyWidth(int bodyWidth) {

        m_bodyWdith = bodyWidth;
    }

    /**
     * Sets the JSP action element to get access to the OpenCms API.<p>
     *
     * @param jsp the JSP action element to get access to the OpenCms API
     */
    public void setCmsJspActionElement(CmsJspActionElement jsp) {

        m_jspActionElement = jsp;
    }

    /**
     * Sets the padding of the columns in the content area.<p>
     *
     * @param colPadding the padding of the columns in the content area
     */
    public void setColPadding(int colPadding) {

        m_colPadding = colPadding;
    }

    /**
     * Sets the spacing of the columns in the content area.<p>
     *
     * @param colSpacing the spacing of the columns in the content area
     */
    public void setColSpacing(int colSpacing) {

        m_colSpacing = colSpacing;
    }

    /**
     * Sets the column layout of the parapgraphs in the content area.<p>
     *
     * @param colLayout the column layout of the parapgraphs in the content area
     */
    public void setColumnLayout(String colLayout) {

        m_columnLayout = colLayout;
    }

    /**
     * Sets the XML content that stores the layout.<p>
     *
     * @param content the XML content that stores the layout
     */
    public void setContent(CmsXmlContent content) {

        m_content = content;
    }

    /**
     * Sets if the layout images should be fixed or calculated from the content area width.<p>
     *
     * @param fixedImageSize if the layout images should be fixed or calculated from the content area width
     */
    public void setFixedImageSize(boolean fixedImageSize) {

        m_fixedImageSize = fixedImageSize;
    }

    /**
     * Sets the image width to use for large images in fixed image size mode.<p>
     *
     * @param largeImgWidth the image width to use for large images in fixed image size mode
     */
    public void setImgWidthLarge(int largeImgWidth) {

        m_imgWidthLarge = largeImgWidth;
    }

    /**
     * Sets the image width to use for medium images in fixed image size mode.<p>
     *
     * @param mediumImgWidth the image width to use for medium images in fixed image size mode
     */
    public void setImgWidthMedium(int mediumImgWidth) {

        m_imgWidthMedium = mediumImgWidth;
    }

    /**
     * Sets the image width to use for small images in fixed image size mode.<p>
     *
     * @param smallImgWidth the image width to use for small images in fixed image size mode
     */
    public void setImgWidthSmall(int smallImgWidth) {

        m_imgWidthSmall = smallImgWidth;
    }

    /**
     * Sets the paragraph layout patterns with the layout name as key, the value is the corresponding image width.<p>
     * 
     * @param layoutPatterns the paragraph layout patterns with the layout name as key, the value is the corresponding image width
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
     * Sets the layout variant to show, e.g. "common", "print" or "accessibe".<p>
     *
     * @param variant the layout variant to show, e.g. "common", "print" or "accessibe"
     */
    public void setVariant(String variant) {

        m_variant = variant;
    }

    /**
     * Calculates the actual column width to show depending on the chosen layout.<p>
     */
    protected void calculateColumnWidth() {

        // calculate the actual column width to show depending on the layout
        if (getColumnLayout().indexOf(COLUMNS_LAYOUT_2) != -1) {
            // 2 column layout
            setColumnWidth(((getBodyWidth() - getColSpacing()) / 2) - (2 * getColPadding()));
        } else {
            // 1 colummn layout
            setColumnWidth(getBodyWidth() - (2 * getColPadding()));
        }
    }

    /**
     * Returns the desired image width depending on the given image variant and the image mode (fixed or not).<p>
     * 
     * @param imgWidthVariant the image width variant to use from the paragraph layout pattern Map
     * @return the desired image width depending on the given image variant and the image mode (fixed or not)
     */
    protected int calculateImageWidth(String imgWidthVariant) {

        if (imgWidthVariant.equals(IMG_WIDTH_LARGE)) {
            // large image
            if (isFixedImageSize() && getImgWidthLarge() > 0) {
                return getImgWidthLarge();
            }
            return getColumnWidth();
        } else if (imgWidthVariant.equals(IMG_WIDTH_SMALL)) {
            // small image
            if (isFixedImageSize() && getImgWidthSmall() > 0) {
                return getImgWidthSmall();
            }
            return (getColumnWidth() / 4);
        } else {
            // medium image
            if (isFixedImageSize() && getImgWidthMedium() > 0) {
                return getImgWidthMedium();
            }
            return (int)Math.round(getColumnWidth() / 2.3);
        }
    }

    /**
     * Returns the JSP action element to get access to the OpenCms API.<p>
     *
     * @return the JSP action element to get access to the OpenCms API
     */
    protected CmsJspActionElement getCmsJspActionElement() {

        return m_jspActionElement;
    }

    /**
     * Returns the OpenCms user context to use.<p>
     *
     * @return the OpenCms user context to use
     */
    protected CmsObject getCmsObject() {

        return m_jspActionElement.getCmsObject();
    }

    /**
     * Returns an initialized image scaler depending on the image align to use.<p>
     * 
     * @param paragraphType the paragraph type to show
     * @param imgSize the image size property value containing the original image information
     * @return an initialized image scaler depending on the image align to use
     */
    protected CmsImageScaler getImageScaler(String paragraphType, String imgSize) {

        // get the image width variant to use from the paragraph layout pattern Map
        String imgWidthVariant = (String)getLayoutPatterns().get(paragraphType);
        if (CmsStringUtil.isEmpty(imgWidthVariant)) {
            // did not find a value, provide a default width
            imgWidthVariant = IMG_WIDTH_MEDIUM;
        }

        // calculate image width to use depending on the column width
        int imgWidth = calculateImageWidth(imgWidthVariant);

        // create scaler instance of original image
        CmsImageScaler origImage = new CmsImageScaler(imgSize);
        // create scaler with desired image width
        CmsImageScaler scaler = new CmsImageScaler();
        scaler.setWidth(imgWidth);
        // return scaler with result image width
        return origImage.getWidthScaler(scaler);
    }

    /**
     * Creates a valid JavaScript link to open a larger image version in a new popup window.<p>
     * 
     * @param imgUri the URI of the image to link to
     * @param imgSize the image size property value containing the original image information
     * @return a valid JavaScript link to open a larger image version in a new popup window
     */
    protected String getLinkToLargeImage(String imgUri, String imgSize) {

        StringBuffer elementLink = new StringBuffer(128);
        elementLink.append(CmsWorkplace.VFS_PATH_MODULES);
        elementLink.append(MODULE_NAME);
        elementLink.append("/elements/popup-image.html?uri=");
        elementLink.append(imgUri);
        elementLink.append("&imgsize=");
        elementLink.append(imgSize);
        elementLink.append("&");
        elementLink.append(CmsLocaleManager.PARAMETER_LOCALE);
        elementLink.append("=");
        elementLink.append(getCmsObject().getRequestContext().getLocale());
        StringBuffer tempLink = new StringBuffer(256);
        tempLink.append("javascript:window.open('");
        tempLink.append(getCmsJspActionElement().link(elementLink.toString()));
        tempLink.append("', 'largeImage', ");
        tempLink.append("'width=620,height=400,location=no,menubar=no,toolbar=no,status=no,scrollbars=yes,resizable=yes');");
        return tempLink.toString();
    }

    /**
     * Returns an initialized macro wrapper that can be used for the paragraph output.<p>
     * 
     * @return an initialized macro wrapper that can be used for the paragraph output
     * @throws Exception if the initialization of the wrapper fails
     */
    protected I_CmsMacroWrapper getMacroWrapper() throws Exception {

        // create path to macro file to use
        StringBuffer macroFile = new StringBuffer(256);
        macroFile.append(getPathLayoutElements());
        macroFile.append(getColumnLayout());
        macroFile.append("_");
        macroFile.append(getVariant());
        macroFile.append(".");
        macroFile.append(CmsMacroWrapperFreeMarker.FILE_SUFFIX);
        String fileName = macroFile.toString();
        if (!getCmsObject().existsResource(fileName)) {
            // macro file not found, log error and throw exception
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_VFS_RESOURCE_1, fileName));
            }
            throw new CmsException(Messages.get().container(Messages.LOG_ERR_VFS_RESOURCE_1, fileName));
        }
        return new CmsMacroWrapperFreeMarker(getCmsObject(), fileName);
    }
    
    /**
     * Returns the String value of the xml content defined by the value(s) inside the given extended properties.<p>
     * 
     * @param xmlElements the instance of ExtendedProperties for the integrated resource type, e.g. news.
     * @param key key is used to identify the value inside the given map.
     * @param xmlContentFileLink the xml content of the integrated file.
     * @param locale the locale object.
     * @return the String value of the xml content defined by the value(s) inside the given map.
     */
    protected String getPropertiesValue(
        ExtendedProperties xmlElements,
        String key,
        CmsXmlContent xmlContentFileLink,
        Locale locale) {

        Object value = xmlElements.get(key);
        String result = "";
        if (value != null) {
            if (value instanceof String) {
                // if value is a String object get the string value from the xml content
                result = xmlContentFileLink.getStringValue(getCmsObject(), (String)value, locale);
            } else if (value instanceof Vector) {
                // if value is a vector iterate over it
                Iterator it_title = ((Vector)value).iterator();
                while (it_title.hasNext()) {
                    String next = (String)it_title.next();
                    if (!CmsStringUtil.isEmptyOrWhitespaceOnly(xmlContentFileLink.getStringValue(
                        getCmsObject(),
                        next,
                        locale))
                        && !xmlContentFileLink.getStringValue(getCmsObject(), next, locale).equals("(none)")) {
                        if (result.length() > 1 && result.lastIndexOf(",") != result.length()) {
                            // only append ',' if the result String is already in use
                            result += ",";
                        }
                        // add the String value from the xml content for the given String of the vector
                        result += xmlContentFileLink.getStringValue(getCmsObject(), next, locale);
                    }
                }
            }
        } else {
            result = "";
        }
        return result;
    }

    /**
     * Returns an instance of ExtendedProperties with key-values pairs which can be used to build e.g. the headline.<p>
     * 
     * @param linkToFile xml content file which is integrated inside the layout page
     * @return an instance of ExtendedProperties with key-value pairs which define the elements to use inside
     *         an integrated xml content, e.g. to build the headline
     */
    protected ExtendedProperties getXmlElementsProperties(CmsFile linkToFile) {

        ExtendedProperties properties = new ExtendedProperties();
        try {
            // get the type name for the integrated file
            // type name is used as key for m_typeMappings
            String typeName = OpenCms.getResourceManager().getResourceType(linkToFile.getTypeId()).getTypeName();
            if (m_typeMappings.get(typeName) == null) {
                // get key/value from the .properties file and store it in properties and m_typeMappings
                properties.load(new ByteArrayInputStream(
                    getCmsObject().readFile(
                        CmsWorkplace.VFS_PATH_MODULES + MODULE_NAME + "/mappings/" + typeName + ".properties").getContents()));
                m_typeMappings.put(typeName, properties);
            } else {
                // if typeName is already used inside m_typeProperties get properties from this map
                properties = (ExtendedProperties)m_typeMappings.get(typeName);
            }
        } catch (Exception e) {
            // ignore
        }
        return properties;
    }

    /**
     * Sets the calculated width of a single paragraph column.<p>
     *
     * @param columnWidth the calculated width of a single paragraph column
     */
    protected void setColumnWidth(int columnWidth) {

        m_columnWidth = columnWidth;
    }

}
