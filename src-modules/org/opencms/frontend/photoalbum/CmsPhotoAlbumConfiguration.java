/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/photoalbum/CmsPhotoAlbumConfiguration.java,v $
 * Date   : $Date: 2011/03/23 14:52:28 $
 * Version: $Revision: 1.7 $
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

package org.opencms.frontend.photoalbum;

import com.alkacon.simapi.Simapi;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlHtmlValue;

import java.awt.Color;
import java.util.List;
import java.util.Locale;

/**
 * Represents the frontend configuration of a photo album.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.1.3 
 */
public class CmsPhotoAlbumConfiguration {

    /** Image filter: delimiter character. */
    public static final String FILTER_DELIMITER = ".";
    
    /** Image filter: no filter selected. */
    public static final String FILTER_NONE = "none";
    
    /** Navigation element position: on the bottom, above the editable text element. */
    public static final String NAVPOS_BOTTOM_ABOVE = "b_a";

    /** Navigation element position: on the bottom, below the editable text element. */
    public static final String NAVPOS_BOTTOM_BELOW = "b_b";

    /** Navigation element position: on top, above the editable text element. */
    public static final String NAVPOS_TOP_ABOVE = "t_a";

    /** Navigation element position: on top, below the editable text element. */
    public static final String NAVPOS_TOP_BELOW = "t_b";

    /** Configuration node name for the align navigation node. */
    public static final String NODE_ALIGN_NAVIGATION = "AlignNavigation";

    /** Configuration node name for the align navigation node. */
    public static final String NODE_ALIGN_TITLE = "AlignTitle";

    /** Configuration node name for the background node. */
    public static final String NODE_BACKGROUND = "Background";

    /** Configuration node name for the columns node. */
    public static final String NODE_COLS = "Cols";

    /** Configuration node name for the detail node. */
    public static final String NODE_DETAIL = "Details";
    
    /** Configuration node name for the filter node. */
    public static final String NODE_FILTER = "Filter";

    /** Configuration node name for the high quality node. */
    public static final String NODE_HIGHQUALITY = "HighQuality";

    /** Configuration node name for the navigation position node. */
    public static final String NODE_NAVIGATION_POSITION = "NavigationPosition";

    /** Configuration node name for the rows node. */
    public static final String NODE_ROWS = "Rows";

    /** Configuration node name for the show description node. */
    public static final String NODE_SHOWDESCRIPTION = "ShowDescription";

    /** Configuration node name for the show original link node. */
    public static final String NODE_SHOWLINKORIGINAL = "ShowLinkOriginal";

    /** Configuration node name for the show resource name as title node. */
    public static final String NODE_SHOWRESOURCENAMEASTITLE = "ShowResourceNameAsTitle";

    /** Configuration node name for the show title node. */
    public static final String NODE_SHOWTITLE = "ShowTitle";

    /** Configuration node name for the size node. */
    public static final String NODE_SIZE = "Size";

    /** Configuration node name for the bottom text node. */
    public static final String NODE_TEXTBOTTOM = "TextBottom";

    /** Configuration node name for the top text node. */
    public static final String NODE_TEXTTOP = "TextTop";

    /** Configuration node name for the thumbs node. */
    public static final String NODE_THUMBS = "Thumbs";

    /** Configuration node name for the title node. */
    public static final String NODE_TITLE = "Title";

    /** Configuration node name for the vfs folder node. */
    public static final String NODE_VFSFOLDER = "VfsFolder";

    /** The image album title. */
    private String m_albumTitle;

    /** The alignment of the album navigation elements. */
    private String m_alignNavigation;

    /** The alignment of the image titles on the detail view. */
    private String m_detailAlignTitle;

    /** The image scaler for the detail image. */
    private CmsImageScaler m_detailImageScaler;

    /** The navigation element position on the pages. */
    private String m_navigationPosition;

    /** The flag if the image description is shown on the detail view. */
    private boolean m_showDetailDescription;

    /** The flag if the image original link is shown on the detail view. */
    private boolean m_showDetailOriginalLink;

    /** The flag if the image title is shown on the detail view. */
    private boolean m_showDetailTitle;

    /** The flag if the image resource name is shown as title if no title is found. */
    private boolean m_showResourceNameAsTitle;

    /** The flag if the image title is shown on the thumbnail overview. */
    private boolean m_showThumbTitle;

    /** The alignment of the image titles on the thumbnail overview. */
    private String m_thumbAlignTitle;

    /** The number of image columns on the thumbnail overview. */
    private int m_thumbCols;

    /** The image scaler for the thumbnails. */
    private CmsImageScaler m_thumbNailScaler;

    /** The number of image rows on the thumbnail overview. */
    private int m_thumbRows;

    /** The optional bottom text on the thumbnail overview. */
    private String m_thumbTextBottom;

    /** The optional top text on the thumbnail overview. */
    private String m_thumbTextTop;

    /** The OpenCms VFS path of the image gallery to use. */
    private String m_vfsPathGallery;

    /**
     * Empty constructor that does no initialization.<p>
     */
    public CmsPhotoAlbumConfiguration() {

        // initialize member objects
        initMembers();
    }

    /**
     * Constructor that initializes the configuration using the currently requested URI.<p>
     * 
     * @param jsp the initialized CmsJspActionElement to access the OpenCms API
     * @throws Exception if parsing the configuration fails
     */
    public CmsPhotoAlbumConfiguration(CmsJspActionElement jsp)
    throws Exception {

        init(jsp, null);
    }

    /**
     * Constructor that initializes the configuration from the given configuration URI.<p>
     * 
     * @param jsp the initialized CmsJspActionElement to access the OpenCms API
     * @param configUri URI of the configuration file, if not provided, current URI is used for configuration
     * @throws Exception if parsing the configuration fails
     */
    public CmsPhotoAlbumConfiguration(CmsJspActionElement jsp, String configUri)
    throws Exception {

        init(jsp, configUri);
    }

    /**
     * Returns the image album title.<p>
     * 
     * @return the image album title
     */
    public String getAlbumTitle() {

        return m_albumTitle;
    }

    /**
     * Returns the alignment of the album navigation elements.<p>
     * 
     * @return the alignment of the album navigation elements
     */
    public String getAlignNavigation() {

        return m_alignNavigation;
    }

    /**
     * Returns the alignment of the image titles on the detail view.<p>
     * 
     * @return the alignment of the image titles on the detail view
     */
    public String getDetailAlignTitle() {

        return m_detailAlignTitle;
    }

    /**
     * Returns the image scaler for the detail image.<p>
     * 
     * @return the image scaler for the detail image
     */
    public CmsImageScaler getDetailImageScaler() {

        return m_detailImageScaler;
    }

    /**
     * Returns the navigation element position on the pages.<p>
     * 
     * @return the navigation element position on the pages
     */
    public String getNavigationPosition() {

        return m_navigationPosition;
    }

    /**
     * Returns the alignment of the image titles on the thumbnail overview.<p>
     * 
     * @return the alignment of the image titles on the thumbnail overview
     */
    public String getThumbAlignTitle() {

        return m_thumbAlignTitle;
    }

    /**
     * Returns the number of image columns on the thumbnail overview.<p>
     * 
     * @return the number of image columns on the thumbnail overview
     */
    public int getThumbCols() {

        return m_thumbCols;
    }

    /**
     * Returns the image scaler for the thumbnails.<p>
     * 
     * @return the image scaler for the thumbnails
     */
    public CmsImageScaler getThumbNailScaler() {

        return m_thumbNailScaler;
    }

    /**
     * Returns the number of image rows on the thumbnail overview.<p>
     * 
     * @return the number of image rows on the thumbnail overview
     */
    public int getThumbRows() {

        return m_thumbRows;
    }

    /**
     * Returns the optional bottom text on the thumbnail overview.<p>
     * 
     * @return the optional bottom text on the thumbnail overview
     */
    public String getThumbTextBottom() {

        return m_thumbTextBottom;
    }

    /**
     * Returns the optional top text on the thumbnail overview.<p>
     * 
     * @return the optional top text on the thumbnail overview
     */
    public String getThumbTextTop() {

        return m_thumbTextTop;
    }

    /**
     * Returns the OpenCms VFS path of the image gallery to use.<p>
     * 
     * @return the OpenCms VFS path of the image gallery to use
     */
    public String getVfsPathGallery() {

        return m_vfsPathGallery;
    }

    /**
     * Initializes the album configuration.<p>
     * 
     * @param jsp the initialized CmsJspActionElement to access the OpenCms API
     * @param configUri URI of the form configuration file, if not provided, current URI is used for configuration
     * 
     * @throws Exception if parsing the configuration fails
     */
    public void init(CmsJspActionElement jsp, String configUri) throws Exception {

        // initialize member objects
        initMembers();

        // read the album configuration file from VFS
        if (CmsStringUtil.isEmpty(configUri)) {
            configUri = jsp.getRequestContext().getUri();
        }
        CmsFile file = jsp.getCmsObject().readFile(configUri);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(jsp.getCmsObject(), file);

        // get locale from request context
        Locale locale = jsp.getRequestContext().getLocale();
        // get the cms object
        CmsObject cms = jsp.getCmsObject();

        // get the album title
        String stringValue = content.getStringValue(cms, NODE_TITLE, locale);
        setAlbumTitle(getConfigurationValue(stringValue, ""));

        // get the gallery vfs folder
        stringValue = content.getStringValue(cms, NODE_VFSFOLDER, locale);
        setVfsPathGallery(getConfigurationValue(stringValue, ""));

        // get the alignment of navigation elements
        stringValue = content.getStringValue(cms, NODE_ALIGN_NAVIGATION, locale);
        setAlignNavigation(getConfigurationValue(stringValue, "left"));

        // get the position of navigation elements
        stringValue = content.getStringValue(cms, NODE_NAVIGATION_POSITION, locale);
        setNavigationPosition(getConfigurationValue(stringValue, NAVPOS_BOTTOM_BELOW));

        // get the show resource name as title flag
        stringValue = content.getStringValue(cms, NODE_SHOWRESOURCENAMEASTITLE, locale);
        setShowResourceNameAsTitle(Boolean.valueOf(stringValue).booleanValue());

        // get the thumbnail configuration parameters
        String nodePrefix = NODE_THUMBS + "/";

        // get the show thumbs title flag
        stringValue = content.getStringValue(cms, nodePrefix + NODE_SHOWTITLE, locale);
        setShowThumbTitle(Boolean.valueOf(stringValue).booleanValue());

        // get the alignment of thumbs title
        stringValue = content.getStringValue(cms, nodePrefix + NODE_ALIGN_TITLE, locale);
        setThumbAlignTitle(getConfigurationValue(stringValue, "left"));

        // get the number of displayed columns
        stringValue = content.getStringValue(cms, nodePrefix + NODE_COLS, locale);
        setThumbCols(Integer.parseInt(getConfigurationValue(stringValue, "1")));

        // get the number of rows per page
        stringValue = content.getStringValue(cms, nodePrefix + NODE_ROWS, locale);
        setThumbRows(Integer.parseInt(getConfigurationValue(stringValue, "-1")));

        // get the thumbnail background color
        stringValue = content.getStringValue(cms, nodePrefix + NODE_BACKGROUND, locale);
        stringValue = getConfigurationValue(stringValue, "#FFFFFF");
        Color color = Color.WHITE;
        try {
            color = Color.decode(stringValue);
        } catch (NumberFormatException e) {
            throw new CmsException(Messages.get().container(Messages.LOG_ERR_WRONG_THUMB_BGCOLOR_1, stringValue));
        }
        getThumbNailScaler().setColor(color);

        // get the thumbs high quality flag
        stringValue = content.getStringValue(cms, nodePrefix + NODE_HIGHQUALITY, locale);
        if (Boolean.valueOf(stringValue).booleanValue()) {
            // use high quality for thumbnails
            getThumbNailScaler().setQuality(85);
        } else {
            // use speed render settings for thumbnails
            getThumbNailScaler().setQuality(50);
            getThumbNailScaler().setRenderMode(Simapi.RENDER_SPEED);
        }

        // get the thumbnail size
        stringValue = content.getStringValue(cms, nodePrefix + NODE_SIZE, locale);
        setImageSize(stringValue, true);
        
        // get the thumbnail filter
        stringValue = content.getStringValue(cms, nodePrefix + NODE_FILTER, locale);
        setImageFilter(stringValue, true);

        // get the top and bottom texts for the thumbnail pages
        CmsXmlHtmlValue textValue = (CmsXmlHtmlValue)content.getValue(nodePrefix + NODE_TEXTTOP, locale);
        if (textValue != null) {
            // get the top text
            stringValue = textValue.getStringValue(cms);
            setThumbTextTop(getConfigurationValue(stringValue, ""));
        } else {
            setThumbTextTop("");
        }
        textValue = (CmsXmlHtmlValue)content.getValue(nodePrefix + NODE_TEXTBOTTOM, locale);
        if (textValue != null) {
            // get the top text
            stringValue = textValue.getStringValue(cms);
            setThumbTextBottom(getConfigurationValue(stringValue, ""));
        } else {
            setThumbTextBottom("");
        }

        // get the detail configuration parameters
        nodePrefix = NODE_DETAIL + "/";

        // get the show detail title flag
        stringValue = content.getStringValue(cms, nodePrefix + NODE_SHOWTITLE, locale);
        setShowDetailTitle(Boolean.valueOf(stringValue).booleanValue());

        // get the show detail description flag
        stringValue = content.getStringValue(cms, nodePrefix + NODE_SHOWDESCRIPTION, locale);
        setShowDetailDescription(Boolean.valueOf(stringValue).booleanValue());

        // get the alignment of detail title
        stringValue = content.getStringValue(cms, nodePrefix + NODE_ALIGN_TITLE, locale);
        setDetailAlignTitle(getConfigurationValue(stringValue, "left"));

        // get the show detail link to original flag
        stringValue = content.getStringValue(cms, nodePrefix + NODE_SHOWLINKORIGINAL, locale);
        setShowDetailOriginalLink(Boolean.valueOf(stringValue).booleanValue());

        // get the detail image size
        stringValue = content.getStringValue(cms, nodePrefix + NODE_SIZE, locale);
        setImageSize(stringValue, false);
        
        // get the detail image filter
        stringValue = content.getStringValue(cms, nodePrefix + NODE_FILTER, locale);
        setImageFilter(stringValue, false);

        // get the detail image background color
        stringValue = content.getStringValue(cms, nodePrefix + NODE_BACKGROUND, locale);
        stringValue = getConfigurationValue(stringValue, "#FFFFFF");
        color = Color.WHITE;
        try {
            color = Color.decode(stringValue);
        } catch (NumberFormatException e) {
            throw new CmsException(Messages.get().container(Messages.LOG_ERR_WRONG_DETAIL_BGCOLOR_1, stringValue));
        }
        getDetailImageScaler().setColor(color);
    }

    /**
     * Returns if the image description is shown on the detail view.<p>
     * 
     * @return true if the image description is shown on the detail view, otherwise false
     */
    public boolean showDetailDescription() {

        return m_showDetailDescription;
    }

    /**
     * Returns if the image original link is shown on the detail view.<p>
     * 
     * @return true if the image original link is shown on the detail view, otherwise false
     */
    public boolean showDetailOriginalLink() {

        return m_showDetailOriginalLink;
    }

    /**
     * Returns if the image title is shown on the detail view.<p>
     * 
     * @return true if the image title is shown on the detail view, otherwise false
     */
    public boolean showDetailTitle() {

        return m_showDetailTitle;
    }

    /**
     * Returns if a page navigation should be shown on the tumbnail overview.<p>
     * 
     * @return true if a page navigation should be shown on the tumbnail overview, otherwise false
     */
    public boolean showPageNavigation() {

        return getThumbRows() > 0;
    }

    /**
     * Returns if the image resource name is shown as title if no title is found.<p>
     * 
     * @return true if the image resource name is shown as title if no title is found, otherwise false
     */
    public boolean showResourceNameAsTitle() {

        return m_showResourceNameAsTitle;
    }

    /**
     * Returns if the optional bottom text on the thumbnail overview is shown.<p>
     * 
     * @return true if the optional bottom text on the thumbnail overview is shown, otherwise false
     */
    public boolean showThumbTextBottom() {

        return CmsStringUtil.isNotEmpty(getThumbTextBottom());
    }

    /**
     * Returns if the optional top text on the thumbnail overview is shown.<p>
     * 
     * @return true if the optional top text on the thumbnail overview is shown, otherwise false
     */
    public boolean showThumbTextTop() {

        return CmsStringUtil.isNotEmpty(getThumbTextTop());
    }

    /**
     * Returns if the image title is shown on the thumbnail overview.<p>
     * 
     * @return true if the image title is shown on the thumbnail overview, otherwise false
     */
    public boolean showThumbTitle() {

        return m_showThumbTitle;
    }

    /**
     * Returns the style attribute value to use for alignment configurations.<p>
     * 
     * @param alignment the alignment (left, center or right)
     * @return the style attribute value to use for alignment configurations
     */
    protected String getStyleAlignAttribute(String alignment) {

        StringBuffer result = new StringBuffer(32);
        result.append(" style=\"text-align: ");
        result.append(alignment);
        result.append(";\"");
        return result.toString();
    }

    /**
     * Initializes the members variables.<p>
     */
    protected void initMembers() {

        // initialize member image scaler objects
        setDetailImageScaler(new CmsImageScaler());
        setThumbNailScaler(new CmsImageScaler());

        // set defaults
        setVfsPathGallery("/");
        setThumbCols(3);
        setThumbRows(3);
        setNavigationPosition(NAVPOS_TOP_BELOW);

    }

    /**
     * Sets the image album title.<p>
     * 
     * @param albumTitle the image album title
     */
    protected void setAlbumTitle(String albumTitle) {

        m_albumTitle = albumTitle;
    }

    /**
     * Sets the alignment of the album navigation elements.<p>
     * 
     * @param alignNavigation the alignment of the album navigation elements
     */
    protected void setAlignNavigation(String alignNavigation) {

        m_alignNavigation = alignNavigation;
    }

    /**
     * Sets the alignment of the image titles on the detail view.<p>
     * 
     * @param detailAlignTitle the alignment of the image titles on the detail view
     */
    protected void setDetailAlignTitle(String detailAlignTitle) {

        m_detailAlignTitle = detailAlignTitle;
    }

    /**
     * Sets the image scaler for the detail image.<p>
     * 
     * @param detailImageScaler the image scaler for the detail image
     */
    protected void setDetailImageScaler(CmsImageScaler detailImageScaler) {

        m_detailImageScaler = detailImageScaler;
    }

    /**
     * Sets the navigation element position on the pages.<p>
     * 
     * @param navigationPosition the navigation element position on the pages
     */
    protected void setNavigationPosition(String navigationPosition) {

        m_navigationPosition = navigationPosition;
    }

    /**
     * Sets if the image description is shown on the detail view.<p>
     * 
     * @param showDetailDescription true if the image description is shown on the detail view, otherwise false
     */
    protected void setShowDetailDescription(boolean showDetailDescription) {

        m_showDetailDescription = showDetailDescription;
    }

    /**
     * Sets if the image original link is shown on the detail view.<p>
     * 
     * @param showDetailOriginalLink true if the image original link is shown on the detail view, otherwise false
     */
    protected void setShowDetailOriginalLink(boolean showDetailOriginalLink) {

        m_showDetailOriginalLink = showDetailOriginalLink;
    }

    /**
     * Sets if the image title is shown on the detail view.<p>
     * 
     * @param showDetailTitle true if the image title is shown on the detail view, otherwise false
     */
    protected void setShowDetailTitle(boolean showDetailTitle) {

        m_showDetailTitle = showDetailTitle;
    }

    /**
     * Sets if the image resource name is shown as title if no title is found.<p>
     * 
     * @param showResourceNameAsTitle true if the image resource name is shown as title if no title is found, otherwise false
     */
    protected void setShowResourceNameAsTitle(boolean showResourceNameAsTitle) {

        m_showResourceNameAsTitle = showResourceNameAsTitle;
    }

    /**
     * Sets if the image title is shown on the thumbnail overview.<p>
     * 
     * @param showThumbTitle true if the image title is shown on the thumbnail overview, otherwise false
     */
    protected void setShowThumbTitle(boolean showThumbTitle) {

        m_showThumbTitle = showThumbTitle;
    }

    /**
     * Sets the alignment of the image titles on the thumbnail overview.<p>
     * 
     * @param thumbAlignTitle the alignment of the image titles on the thumbnail overview
     */
    protected void setThumbAlignTitle(String thumbAlignTitle) {

        m_thumbAlignTitle = thumbAlignTitle;
    }

    /**
     * Sets the number of image columns on the thumbnail overview.<p>
     * 
     * @param thumbCols the number of image columns on the thumbnail overview
     */
    protected void setThumbCols(int thumbCols) {

        m_thumbCols = thumbCols;
    }

    /**
     * Sets the image scaler for the thumbnails.<p>
     * 
     * @param thumbNailScaler the image scaler for the thumbnails
     */
    protected void setThumbNailScaler(CmsImageScaler thumbNailScaler) {

        m_thumbNailScaler = thumbNailScaler;
    }

    /**
     * Sets the number of image rows on the thumbnail overview.<p>
     * 
     * @param thumbRows the number of image rows on the thumbnail overview
     */
    protected void setThumbRows(int thumbRows) {

        if (thumbRows < 1) {
            m_thumbRows = -1;
        } else {
            m_thumbRows = thumbRows;
        }
    }

    /**
     * Sets the optional bottom text on the thumbnail overview.<p>
     * 
     * @param thumbTextBottom the optional bottom text on the thumbnail overview
     */
    protected void setThumbTextBottom(String thumbTextBottom) {

        m_thumbTextBottom = thumbTextBottom;
    }

    /**
     * Sets the optional top text on the thumbnail overview.<p>
     * 
     * @param thumbTextTop the optional top text on the thumbnail overview
     */
    protected void setThumbTextTop(String thumbTextTop) {

        m_thumbTextTop = thumbTextTop;
    }

    /**
     * Sets the OpenCms VFS path of the image gallery to use.<p>
     * 
     * @param vfsPathGallery the OpenCms VFS path of the image gallery to use
     */
    protected void setVfsPathGallery(String vfsPathGallery) {

        m_vfsPathGallery = vfsPathGallery;
    }

    /**
     * Checks if the given value is empty and returns in that case the default value.<p>
     * 
     * @param value the configuration value to check
     * @param defaultValue the default value to return in case the value is empty
     * @return the checked value
     */
    private String getConfigurationValue(String value, String defaultValue) {

        if (CmsStringUtil.isNotEmpty(value)) {
            return value;
        }
        return defaultValue;
    }
    
    /**
     * Sets the image filter for thumbnails and the detail view.<p>
     * 
     * @param configValue the String value of the configuration file
     * @param isThumbNail flag indicating if the thumbnail or the detail view configuration is set
     * @throws Exception if parsing the configuration fails
     */
    private void setImageFilter(String configValue, boolean isThumbNail) throws Exception {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configValue) && !FILTER_NONE.equals(configValue)) {
            // filter value set, determine it
            List filter = CmsStringUtil.splitAsList(configValue, FILTER_DELIMITER);
            for (int i=0; i<filter.size(); i++) {
                String currentFilter = (String)filter.get(i);
                if (isThumbNail) {
                    // set thumbnail filter
                    getThumbNailScaler().addFilter(currentFilter);
                } else {
                    // set detail image filter
                    getDetailImageScaler().addFilter(currentFilter);
                }
            }
            
        }
    }

    /**
     * Sets the image size for thumbnails and the detail view.<p>
     * 
     * @param configValue the String value of the configuration file
     * @param isThumbNail flag indicating if the thumbnail or the detail view configuration is set
     * @throws Exception if parsing the configuration fails
     */
    private void setImageSize(String configValue, boolean isThumbNail) throws Exception {

        List sizes = CmsStringUtil.splitAsList(getConfigurationValue(configValue, "200x150"), 'x', true);
        int width = Integer.parseInt((String)sizes.get(0));
        int height = Integer.parseInt((String)sizes.get(1));
        if (isThumbNail) {
            getThumbNailScaler().setWidth(width);
            getThumbNailScaler().setHeight(height);
        } else {
            getDetailImageScaler().setWidth(width);
            getDetailImageScaler().setHeight(height);
        }
    }

}