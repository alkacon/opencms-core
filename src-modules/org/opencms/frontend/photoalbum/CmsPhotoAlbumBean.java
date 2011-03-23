/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/photoalbum/CmsPhotoAlbumBean.java,v $
 * Date   : $Date: 2011/03/23 14:52:28 $
 * Version: $Revision: 1.9 $
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

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods to generate frontend views of a photo album using a XML Content configuration file.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.1.3 
 */
public class CmsPhotoAlbumBean extends CmsJspActionElement {

    /** Request parameter value for the album action: show detail view. */
    public static final int ACTION_DETAIL = 1;

    /** Request parameter value for the album action: show original image. */
    public static final int ACTION_ORIGINAL = 2;

    /** Request parameter value for the album action: show thumbnail view. */
    public static final int ACTION_THUMBNAIL = 0;

    /** Request parameter name for the action parameter. */
    public static final String PARAM_ACTION = "action";

    /** Request parameter name for the image parameter. */
    public static final String PARAM_IMAGE = "image";

    /** Request parameter name for the album page parameter. */
    public static final String PARAM_PAGE = "thumbpage";

    /** Request parameter value for the album action: show detail view. */
    public static final String VALUE_ACTION_DETAIL = "detail";

    /** Request parameter value for the album action: show original image. */
    public static final String VALUE_ACTION_ORIGINAL = "original";

    /** Request parameter value for the album action: show thumbnail view. */
    public static final String VALUE_ACTION_THUMBNAIL = "thumbnail";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPhotoAlbumBean.class);

    /** The list of all photos to display in the photo album. */
    private List m_albumPhotos;

    /** Holds possible configuration error messages. */
    private List m_configErrors;

    /** The photo album configuration. */
    private CmsPhotoAlbumConfiguration m_configuration;

    /** The current page to display for the thumbnail view. */
    private int m_currentPage;

    /** The display action to determine the view to generate. */
    private int m_displayAction;

    /** The messages to use. */
    private CmsMessages m_messages;

    /** The number of pages to display for the thumbnail view. */
    private int m_pageCount;

    /** The number of photos to display on a single thumbnail overview page. */
    private int m_photosPerPage;

    /** The CSS style object that is used to format the photo album output. */
    private CmsPhotoAlbumStyle m_style;

    /**
     * Constructor, creates the necessary photo album configuration objects.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsPhotoAlbumBean(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(context, req, res, null);
    }

    /**
     * Constructor, creates the necessary photo album configuration objects using a given configuration file URI.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     * @param configUri URI of the photo album configuration file, if not provided, current URI is used for configuration
     */
    public CmsPhotoAlbumBean(PageContext context, HttpServletRequest req, HttpServletResponse res, String configUri) {

        super(context, req, res);
        init(configUri);
    }

    /**
     * Builds the HTML to display the photo album.<p>
     * 
     * @return the HTML to display the photo album
     * @throws IOException if writing the output fails
     */
    public String displayAlbum() throws IOException {

        // show eventual configuration errors
        buildHtmlConfigurationErrors();
        // show selected album view
        switch (getDisplayAction()) {
            case ACTION_DETAIL:
                return buildHtmlViewDetail();
            case ACTION_ORIGINAL:
                return "";
            case ACTION_THUMBNAIL:
            default:
                return buildHtmlViewThumbNail();
        }
    }

    /**
     * Returns the list of all photos to display in the photo album.<p>
     * 
     * @return the list of all photos to display in the photo album
     */
    public List getAlbumPhotos() {

        if (m_albumPhotos == null) {
            CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(CmsResourceTypeImage.getStaticTypeId());
            try {
                m_albumPhotos = getCmsObject().readResources(getConfiguration().getVfsPathGallery(), filter, false);
            } catch (CmsException e) {
                // create empty photo list
                m_albumPhotos = new ArrayList(0);
                // log error
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_ERR_IMAGEFOLDER_NOT_FOUND_1,
                        getConfiguration().getVfsPathGallery()));
                }
                addConfigError(m_messages.key(
                    Messages.LOG_ERR_IMAGEFOLDER_NOT_FOUND_1,
                    getConfiguration().getVfsPathGallery()));
            }
        }
        return m_albumPhotos;
    }

    /**
     * Returns the photo album configuration.<p>
     * 
     * @return the photo album configuration
     */
    public CmsPhotoAlbumConfiguration getConfiguration() {

        return m_configuration;
    }

    /**
     * Returns the current page to display for the thumbnail view.<p>
     * 
     * @return the current page to display for the thumbnail view
     */
    public int getCurrentPage() {

        return m_currentPage;
    }

    /**
     * Returns the display action to determine the view to generate.<p>
     * 
     * @return the display action to determine the view to generate
     */
    public int getDisplayAction() {

        return m_displayAction;
    }

    /**
     * Returns the number of pages to display for the thumbnail view.<p>
     *  
     * @return the number of pages to display for the thumbnail view
     */
    public int getPageCount() {

        return m_pageCount;
    }

    /**
     * Returns the number of photos to display on a single thumbnail overview page.<p>
     * 
     * @return the number of photos to display on a single thumbnail overview page
     */
    public int getPhotosPerPage() {

        return m_photosPerPage;
    }

    /**
     * Returns the CSS style object that is used to format the photo album output.<p>
     * 
     * @return the CSS style object that is used to format the photo album output
     */
    public CmsPhotoAlbumStyle getStyle() {

        return m_style;
    }

    /**
     * Initializes the photo album configuration and determines the display action.<p>
     * 
     * @param configUri URI of the photo album configuration file, if not provided, current URI is used for configuration
     */
    public void init(String configUri) {

        // set messages
        m_messages = Messages.get().getBundle(getRequestContext().getLocale());
        // initialize empty list of configuration errors
        setConfigErrors(new ArrayList());
        // initialize the photo album CSS styles
        setStyle(new CmsPhotoAlbumStyle());
        // initialize the photo album configuration
        try {
            setConfiguration(new CmsPhotoAlbumConfiguration(this, configUri));
        } catch (Exception e) {
            // set empty configuration
            setConfiguration(new CmsPhotoAlbumConfiguration());
            if (e instanceof CmsException) {
                // for Cms exceptions, show detailed error message
                addConfigError(((CmsException)e).getLocalizedMessage(getRequestContext().getLocale()));
            } else {
                addConfigError(e.getLocalizedMessage());
            }
        }
        // determine the album view to display depending on request parameter
        String action = getRequest().getParameter(PARAM_ACTION);
        if (VALUE_ACTION_DETAIL.equals(action)) {
            // show the detail view
            setDisplayAction(ACTION_DETAIL);
        } else if (VALUE_ACTION_ORIGINAL.equals(action)) {
            // show the original image
            setDisplayAction(ACTION_ORIGINAL);
        } else {
            // default action: show the thumbnail overview
            setDisplayAction(ACTION_THUMBNAIL);
        }
        // determine the necessary page data to build navigation elements
        calculatePageData();
    }

    /**
     * Sets the photo album configuration.<p>
     * 
     * @param configuration the photo album configuration
     */
    public void setConfiguration(CmsPhotoAlbumConfiguration configuration) {

        m_configuration = configuration;
    }

    /**
     * Sets the CSS style object that is used to format the photo album output.<p>
     * 
     * @param style the CSS style object that is used to format the photo album output
     */
    public void setStyle(CmsPhotoAlbumStyle style) {

        m_style = style;
    }

    /**
     * Adds an error to the list of configuration errors.<p>
     * 
     * @param configError error to add to the list of configuration errors
     */
    protected void addConfigError(String configError) {

        m_configErrors.add(configError);
    }

    /**
     * Returns the HTML for the photo album title.<p>
     * 
     * @return the HTML for the photo album title
     */
    protected String buildHtmlAlbumTitle() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getConfiguration().getAlbumTitle())) {
            // show the title
            StringBuffer result = new StringBuffer(128);
            result.append("<h1");
            result.append(getStyle().getClassPageTitle());
            result.append(">");
            result.append(getConfiguration().getAlbumTitle());
            result.append("</h1>\n");
            return result.toString();
        }
        return "";
    }

    /**
     * Writes the HTML for the configuration error output.<p>
     * 
     * Writes the found configuration errors directly to the JSP writer.<p>
     * 
     * @throws IOException if writing the output fails
     */
    protected void buildHtmlConfigurationErrors() throws IOException {

        if (!getRequestContext().currentProject().isOnlineProject() && getConfigErrors().size() > 0) {
            // configuration error(s) found, show them in offline projects
            getJspContext().getOut().print("<h1>");
            getJspContext().getOut().print(m_messages.key(Messages.GUI_CONFIG_ERRORS_HEADLINE_0));
            getJspContext().getOut().print("</h1>");
            getJspContext().getOut().print("<p>");
            // loop error messages
            for (int i = 0; i < getConfigErrors().size(); i++) {
                if (i > 0) {
                    getJspContext().getOut().println("<br />");
                }
                getJspContext().getOut().print(getConfigErrors().get(i));
            }
            getJspContext().getOut().print("</p>");
        }
    }

    /**
     * Returns the HTML for the image navigation on the photo album detail page.<p>
     * 
     * @param currentNavigationPosition the current navigation position to display the navigation
     * @param photoIndex the index of the photo to display
     * @param photo the photo to display as CmsResource object
     * @return the HTML for the image navigation on the photo album detail page
     */
    protected String buildHtmlImageNavigation(String currentNavigationPosition, int photoIndex, CmsResource photo) {

        if (!checkNavigationPosition(currentNavigationPosition)) {
            // wrong position to insert the navigation elements, do not show navigation at this position
            return "";
        }

        StringBuffer result = new StringBuffer(1024);
        StringBuffer link = new StringBuffer(256);
        result.append("<tr>\n\t<td");
        result.append(getStyle().getClassNavigation());
        result.append(getConfiguration().getStyleAlignAttribute(getConfiguration().getAlignNavigation()));
        result.append(">");
        if (photoIndex > 0) {
            // build the "Back" link
            result.append("<a");
            result.append(getStyle().getClassLink());
            result.append(" href=\"");
            link.append(getRequestContext().getUri());
            link.append("?");
            link.append(PARAM_ACTION).append("=").append(VALUE_ACTION_DETAIL);
            link.append("&amp;");
            link.append(PARAM_IMAGE).append("=").append(photoIndex - 1);
            result.append(link(link.toString()));
            result.append("\">");
            result.append(m_messages.key(Messages.GUI_NAVIGATION_BACK_0));
            result.append("</a>");
            result.append(" - ");
        } else {
            result.append(fillNavSpaces(m_messages.key(Messages.GUI_NAVIGATION_BACK_0) + " - "));
        }
        // build the image index information
        Object[] args = new Object[] {new Integer(photoIndex + 1), new Integer(getAlbumPhotos().size())};
        result.append(m_messages.key(Messages.GUI_DETAIL_IMAGEINFO_2, args));
        if (photoIndex < (getAlbumPhotos().size() - 1)) {
            // build the "Next" link
            result.append(" - ");
            result.append("<a");
            result.append(getStyle().getClassLink());
            result.append(" href=\"");
            link = new StringBuffer(256);
            link.append(getRequestContext().getUri());
            link.append("?");
            link.append(PARAM_ACTION).append("=").append(VALUE_ACTION_DETAIL);
            link.append("&amp;");
            link.append(PARAM_IMAGE).append("=").append(photoIndex + 1);
            result.append(link(link.toString()));
            result.append("\">");
            result.append(m_messages.key(Messages.GUI_NAVIGATION_NEXT_0));
            result.append("</a>");
        } else {
            result.append(fillNavSpaces(" - " + m_messages.key(Messages.GUI_NAVIGATION_NEXT_0)));
        }
        result.append("<br />");

        // build the link to the thumbnail overview
        int thumbPage = 1;
        if (getConfiguration().showPageNavigation()) {
            // calculate the page to show
            thumbPage = (photoIndex / getPhotosPerPage()) + 1;
        }
        result.append("<a");
        result.append(getStyle().getClassLink());
        result.append(" href=\"");
        link = new StringBuffer(256);
        link.append(getRequestContext().getUri());
        link.append("?");
        link.append(PARAM_ACTION).append("=").append(VALUE_ACTION_THUMBNAIL);
        link.append("&amp;");
        link.append(PARAM_PAGE).append("=").append(thumbPage);
        result.append(link(link.toString()));
        result.append("\">");
        result.append(m_messages.key(Messages.GUI_NAVIGATION_OVERVIEW_0));
        result.append("</a>");

        // build the link to the original image if configured
        if (getConfiguration().showDetailOriginalLink()) {
            result.append(" - <a");
            result.append(getStyle().getClassLink());
            result.append(" href=\"");
            result.append(link(getCmsObject().getSitePath(photo)));
            result.append("\" target=\"originalphoto\">");
            result.append(m_messages.key(Messages.GUI_NAVIGATION_ORIGINAL_0));
            result.append("</a>");
        }
        result.append("</td>\n</tr>\n");

        return result.toString();
    }

    /**
     * Returns the HTML for the page navigation on the thumbnail overview pages.<p>
     * 
     * @param currentNavigationPosition the current navigation position to display the navigation
     * @return the HTML for the page navigation on the thumbnail overview pages
     */
    protected String buildHtmlPageNavigation(String currentNavigationPosition) {

        if (!checkNavigationPosition(currentNavigationPosition)) {
            // wrong position to insert the navigation elements, do not show navigation at this position
            return "";
        }

        StringBuffer result = new StringBuffer(1024);

        if (getConfiguration().showPageNavigation() && getPageCount() > 1) {
            // show navigation and number of pages greater than 1
            result.append("<tr>\n\t<td colspan=\"");
            result.append(getConfiguration().getThumbCols());
            result.append("\"");
            result.append(getStyle().getClassNavigation());
            result.append(getConfiguration().getStyleAlignAttribute(getConfiguration().getAlignNavigation()));
            result.append(">");
            StringBuffer link = new StringBuffer(256);
            if (getCurrentPage() > 1) {
                // build the "Back" link
                result.append("<a");
                result.append(getStyle().getClassLink());
                result.append(" href=\"");
                link.append(getRequestContext().getUri());
                link.append("?");
                link.append(PARAM_ACTION).append("=").append(VALUE_ACTION_THUMBNAIL);
                link.append("&amp;");
                link.append(PARAM_PAGE).append("=").append(getCurrentPage() - 1);
                result.append(link(link.toString()));
                result.append("\">");
                result.append(m_messages.key(Messages.GUI_NAVIGATION_BACK_0));
                result.append("</a>");
                result.append(" - ");
            } else {
                result.append(fillNavSpaces(m_messages.key(Messages.GUI_NAVIGATION_BACK_0) + " - "));
            }
            // build the page index information
            result.append(m_messages.key(Messages.GUI_THUMB_PAGEINFO_2, new Integer(getCurrentPage()), new Integer(
                getPageCount())));
            if (getCurrentPage() < getPageCount()) {
                // build the "Next" link
                result.append(" - ");
                result.append("<a class=\"");
                result.append(getStyle().getClassLink());
                result.append("\" href=\"");
                link = new StringBuffer(256);
                link.append(getRequestContext().getUri());
                link.append("?");
                link.append(PARAM_ACTION).append("=").append(VALUE_ACTION_THUMBNAIL);
                link.append("&amp;");
                link.append(PARAM_PAGE).append("=").append(getCurrentPage() + 1);
                result.append(link(link.toString()));
                result.append("\">");
                result.append(m_messages.key(Messages.GUI_NAVIGATION_NEXT_0));
                result.append("</a>");
            } else {
                result.append(fillNavSpaces(" - " + m_messages.key(Messages.GUI_NAVIGATION_NEXT_0)));
            }
            result.append("</td>\n</tr>\n");
        }
        return result.toString();
    }

    /**
     * Returns the HTML for a text row on the thumbnail overview page of the photo album.<p>
     * 
     * @param text the text to display in the row
     * @return the HTML for a text row on the thumbnail overview page of the photo album
     */
    protected String buildHtmlThumbTextRow(String text) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(text)) {
            StringBuffer result = new StringBuffer(2048);
            result.append("<tr>\n\t<td colspan=\"");
            result.append(getConfiguration().getThumbCols());
            result.append("\"");
            result.append(getStyle().getClassThumbText());
            result.append(">");
            result.append(text);
            result.append("</td>\n</tr>\n");
            return result.toString();
        }
        return "";
    }

    /**
     * Returns the HTML to build the detail view of a selected photo album image.<p>
     * 
     * @return the HTML to build the detail view of a selected photo album image
     */
    protected String buildHtmlViewDetail() {

        StringBuffer result = new StringBuffer(4096);

        //show the photo gallery title
        result.append(buildHtmlAlbumTitle());

        // get the photo index number to show
        String indexParam = getRequest().getParameter(PARAM_IMAGE);
        int photoIndex = 0;
        if (CmsStringUtil.isNotEmpty(indexParam)) {
            // check the index parameter and set it to valid value if necessary
            photoIndex = Integer.parseInt(indexParam);
            if (photoIndex > (getAlbumPhotos().size() - 1)) {
                photoIndex = getAlbumPhotos().size() - 1;
            }
        }

        // get the photo to show
        CmsResource photo = (CmsResource)getAlbumPhotos().get(photoIndex);
        String resourceName = getCmsObject().getSitePath(photo);

        // determine the photo title
        String title = "";
        if (getConfiguration().showResourceNameAsTitle()) {
            title = CmsResource.getName(resourceName);
        }
        title = property(CmsPropertyDefinition.PROPERTY_TITLE, resourceName, title);
        title = CmsEncoder.escapeXml(title);

        result.append("<table border=\"0\"");
        result.append(getStyle().getClassThumbTable());
        result.append(" width=\"");
        result.append(getConfiguration().getDetailImageScaler().getWidth());
        result.append("\">\n");

        // show the navigation if configured position is top above text
        result.append(buildHtmlImageNavigation(CmsPhotoAlbumConfiguration.NAVPOS_TOP_ABOVE, photoIndex, photo));

        // show the image title if configured
        if (getConfiguration().showDetailTitle() && CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
            result.append("<tr>\n\t<td");
            result.append(getStyle().getClassDetailImageTitle());
            result.append(getConfiguration().getStyleAlignAttribute(getConfiguration().getDetailAlignTitle()));
            result.append(">");
            result.append(title);
            result.append("</td>\n</tr>\n");

        }

        // show the navigation if configured position is top below text
        result.append(buildHtmlImageNavigation(CmsPhotoAlbumConfiguration.NAVPOS_TOP_BELOW, photoIndex, photo));

        // show image row
        result.append("<tr>\n\t<td>");
        // create the image
        result.append("<img src=\"");
        StringBuffer link = new StringBuffer(256);
        link.append(resourceName);
        link.append(getConfiguration().getDetailImageScaler().toRequestParam());
        result.append(link(link.toString()));
        result.append("\" border=\"0\" width=\"");
        result.append(getConfiguration().getDetailImageScaler().getWidth());
        result.append("\" height=\"");
        result.append(getConfiguration().getDetailImageScaler().getHeight());
        result.append("\" alt=\"");
        result.append(title);
        result.append("\" title=\"");
        result.append(title);
        result.append("\" />");
        result.append("</td>\n</tr>\n");

        // show the navigation if configured position is bottom above text
        result.append(buildHtmlImageNavigation(CmsPhotoAlbumConfiguration.NAVPOS_BOTTOM_ABOVE, photoIndex, photo));

        // show the image description if configured and present
        if (getConfiguration().showDetailDescription()) {
            String description = property(CmsPropertyDefinition.PROPERTY_DESCRIPTION, resourceName, "");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(description)) {
                result.append("<tr>\n\t<td");
                result.append(getStyle().getClassDetailImageDescription());
                result.append(getConfiguration().getStyleAlignAttribute(getConfiguration().getDetailAlignTitle()));
                result.append(">");
                result.append(description);
                result.append("</td>\n</tr>\n");
            }
        }

        // show the navigation if configured position is bottom below text
        result.append(buildHtmlImageNavigation(CmsPhotoAlbumConfiguration.NAVPOS_BOTTOM_BELOW, photoIndex, photo));

        result.append("</table>");
        return result.toString();
    }

    /**
     * Returns the HTML to build a thumbnail overview page of the photo album.<p>
     * 
     * @return the HTML to build a thumbnail overview page of the photo album
     */
    protected String buildHtmlViewThumbNail() {

        StringBuffer result = new StringBuffer(4096);

        // determine photo indizes to display and the number of thumb rows
        int startIndex = (getCurrentPage() - 1) * getPhotosPerPage();
        int endIndex = 0;
        int rowCount = getConfiguration().getThumbRows();
        if (getConfiguration().showPageNavigation()) {
            // navigation is shown, calculate end index
            endIndex = (getCurrentPage() * getPhotosPerPage()) - 1;
            if (endIndex > (getAlbumPhotos().size() - 1)) {
                endIndex = getAlbumPhotos().size() - 1;
            }
        } else {
            // all photos on one page
            endIndex = getAlbumPhotos().size() - 1;
        }
        int photoCount = endIndex - startIndex + 1;
        rowCount = photoCount / getConfiguration().getThumbCols();
        if ((photoCount % getConfiguration().getThumbCols()) > 0) {
            rowCount += 1;
        }

        // show the photo album title
        result.append(buildHtmlAlbumTitle());

        result.append("<table border=\"0\"");
        result.append(getStyle().getClassThumbTable());
        result.append(" width=\"");
        result.append(getConfiguration().getThumbCols() * getConfiguration().getThumbNailScaler().getWidth());
        result.append("\">\n");

        // show the navigation if configured position is top above text
        result.append(buildHtmlPageNavigation(CmsPhotoAlbumConfiguration.NAVPOS_TOP_ABOVE));

        // show the top text if present
        result.append(buildHtmlThumbTextRow(getConfiguration().getThumbTextTop()));

        // show the navigation if configured position is top below text
        result.append(buildHtmlPageNavigation(CmsPhotoAlbumConfiguration.NAVPOS_TOP_BELOW));

        String styleAttr = getConfiguration().getStyleAlignAttribute(getConfiguration().getThumbAlignTitle());

        int photoIndex = startIndex;
        for (int i = 1; i <= rowCount; i++) {
            // build the table thumbnail rows
            result.append("<tr>\n");
            for (int k = 1; k <= getConfiguration().getThumbCols(); k++) {
                // build the tumbnail table data cell
                result.append("\t<td width=\"");
                result.append(getConfiguration().getThumbNailScaler().getWidth());
                result.append("\"");
                result.append(getStyle().getClassThumbImageTitle());
                result.append(styleAttr);
                result.append(">");
                if (photoIndex <= endIndex) {
                    // current photo is in list range, show it
                    CmsResource photo = (CmsResource)getAlbumPhotos().get(photoIndex);
                    String resourceName = getCmsObject().getSitePath(photo);
                    String title = "";
                    if (getConfiguration().showResourceNameAsTitle()) {
                        title = CmsResource.getName(resourceName);
                    }
                    title = property(CmsPropertyDefinition.PROPERTY_TITLE, resourceName, title);
                    title = CmsEncoder.escapeXml(title);
                    // create the link to the detail view
                    result.append("<a href=\"");
                    StringBuffer link = new StringBuffer(256);
                    link.append(getRequestContext().getUri());
                    link.append("?");
                    link.append(PARAM_ACTION).append("=").append(VALUE_ACTION_DETAIL);
                    link.append("&amp;").append(PARAM_IMAGE).append("=").append(photoIndex);
                    result.append(link(link.toString()));
                    result.append("\">");
                    // create the scaled thumbnail
                    result.append("<img src=\"");
                    link = new StringBuffer(256);
                    link.append(resourceName);
                    link.append(getConfiguration().getThumbNailScaler().toRequestParam());
                    result.append(link(link.toString()));
                    result.append("\" border=\"0\" width=\"");
                    result.append(getConfiguration().getThumbNailScaler().getWidth());
                    result.append("\" height=\"");
                    result.append(getConfiguration().getThumbNailScaler().getHeight());
                    result.append("\" alt=\"");
                    result.append(title);
                    result.append("\" title=\"");
                    result.append(title);
                    result.append("\" />");
                    result.append("</a>");
                    if (getConfiguration().showThumbTitle() && CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
                        // show title below the thumbnail
                        result.append("<br clear=\"all\" /><span");
                        result.append(getStyle().getClassThumbImageTitle());
                        result.append(">");
                        result.append(title);
                        result.append("</span>");
                    }
                    photoIndex++;
                }
                result.append("</td>\n");
            }
            result.append("</tr>\n");
        }

        // show the navigation if configured position is bottom above text
        result.append(buildHtmlPageNavigation(CmsPhotoAlbumConfiguration.NAVPOS_BOTTOM_ABOVE));

        // show the bottom text if present
        result.append(buildHtmlThumbTextRow(getConfiguration().getThumbTextBottom()));

        // show the navigation if configured position is bottom below text
        result.append(buildHtmlPageNavigation(CmsPhotoAlbumConfiguration.NAVPOS_BOTTOM_BELOW));

        result.append("</table>");
        return result.toString();
    }

    /**
     * Determines the necessary page information to build the navigation elements for the album pages.<p>
     * 
     * Calculates the following values and stores them in members:
     * <ul>
     * <li>the number of photos to display on one overview page</li>
     * <li>the current thumbnail page to show</li>
     * <li>the number of pages to create to show all images of the selected gallery</li>
     * </ul>
     */
    protected void calculatePageData() {

        if (getConfiguration().showPageNavigation()) {
            // show page navigation, do calculations
            setPhotosPerPage(getConfiguration().getThumbCols() * getConfiguration().getThumbRows());
            int pageCount = getAlbumPhotos().size() / getPhotosPerPage();
            if ((getAlbumPhotos().size() % getPhotosPerPage()) != 0) {
                pageCount++;
            }
            setPageCount(pageCount);
            // determine page to show
            String page = getRequest().getParameter(PARAM_PAGE);
            if (CmsStringUtil.isNotEmpty(page) && getPageCount() > 1) {
                int currentPage = Integer.parseInt(page);
                if (currentPage > getPageCount()) {
                    currentPage = getPageCount();
                }
                setCurrentPage(currentPage);
            } else {
                setCurrentPage(1);
            }
        } else {
            // no navigation shown, set to default values
            setPhotosPerPage(0);
            setPageCount(1);
            setCurrentPage(1);
        }
    }

    /**
     * Returns if the current navigation position to check is the configured position.<p>
     * 
     * @param currentPosition the current navigation position to check
     * @return true if the current navigation position to check is the configured position, otherwise false
     */
    protected boolean checkNavigationPosition(String currentPosition) {

        return getConfiguration().getNavigationPosition().indexOf(currentPosition) > -1;
    }

    /**
     * Returns non breakable spaces (<code>&amp;nbsp;</code>) as replacement for the given replace value.<p>
     * 
     * @param replaceValue the value to replace with spaces
     * @return non breakable spaces as replacement
     */
    protected String fillNavSpaces(String replaceValue) {

        int centerIndex = getConfiguration().getAlignNavigation().indexOf("center");
        if (centerIndex > -1 && CmsStringUtil.isNotEmpty(replaceValue)) {
            int length = replaceValue.length();
            StringBuffer result = new StringBuffer(6 * length);
            for (int i = 0; i < length; i++) {
                result.append("&nbsp;");
            }
            return result.toString();
        }
        return "";
    }

    /**
     * Returns the configuration errors that occurred.<p>
     * 
     * @return the configuration errors that occurred
     */
    protected List getConfigErrors() {

        return m_configErrors;
    }

    /**
     * Sets the configuration errors that occurred.<p>
     * 
     * @param configErrors the configuration errors that occurred
     */
    protected void setConfigErrors(List configErrors) {

        m_configErrors = configErrors;
    }

    /**
     * Sets the current page to display for the thumbnail view.<p>
     * 
     * @param currentPage the current page to display for the thumbnail view
     */
    protected void setCurrentPage(int currentPage) {

        m_currentPage = currentPage;
    }

    /**
     * Sets the display action to determine the view to generate.<p>
     * 
     * @param displayAction the display action to determine the view to generate
     */
    protected void setDisplayAction(int displayAction) {

        m_displayAction = displayAction;
    }

    /**
     * Sets the number of pages to display for the thumbnail view.<p>
     * 
     * @param pageCount the number of pages to display for the thumbnail view
     */
    protected void setPageCount(int pageCount) {

        m_pageCount = pageCount;
    }

    /**
     * Sets the number of photos to display on a single thumbnail overview page.<p>
     * 
     * @param photosPerPage the number of photos to display on a single thumbnail overview page
     */
    protected void setPhotosPerPage(int photosPerPage) {

        m_photosPerPage = photosPerPage;
    }

}
