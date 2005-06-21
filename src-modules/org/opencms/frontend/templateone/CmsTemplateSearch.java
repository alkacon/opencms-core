/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateSearch.java,v $
 * Date   : $Date: 2005/06/21 15:49:58 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.frontend.templateone;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.Messages;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.I_CmsWpConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the search result JSP page.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.7 $
 */
public class CmsTemplateSearch extends CmsTemplateBean {

    /** Request parameter name for the search entire website flag.<p> */
    public static final String C_PARAM_SEARCHALL = "entire";

    /** Stores the URI of the page calling the search result page.<p> */
    private String m_pageUri;

    /** The search entire website flag.<p> */
    private boolean m_searchAll;

    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsTemplateSearch() {

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
    public CmsTemplateSearch(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super();
        init(context, req, res);
    }

    /**
     * Builds the html to display error messages on the search result page.<p>
     * 
     * If no error occurs, an empty String will be returned.<p>
     * 
     * @param search the search result object holding all necessary information
     * @param results the list of result objects to display
     * @return the html to display error messages
     */
    public String buildSearchErrorMessages(CmsSearch search, List results) {

        StringBuffer result = new StringBuffer(32);
        if (search.getLastException() != null) {
            // the search did not run properly, create error output
            String errorMessage = "";
            if (((CmsException)search.getLastException()).getMessageContainer().getKey().equals(
                Messages.ERR_QUERY_TOO_SHORT_1)) {
                // query String was too short
                errorMessage = key("search.error.wordlength");
            } else {
                // other error
                errorMessage = key("search.error.details");
            }
            result.append("<h3>");
            result.append(key("search.error"));
            result.append("</h3>\n");
            result.append("<p>");
            result.append(errorMessage);
            result.append("</p>\n");
            result.append("<!-- Exception message: ");
            result.append(search.getLastException().toString());
            result.append("// -->\n");
        } else if (results == null || results.size() == 0) {
            // no results were found for the current query
            result.append("<h3>");
            result.append(key("search.error.nomatch"));
            result.append("</h3>\n");
        }
        return result.toString();
    }

    /**
     * Builds the html for the search result page headline.<p>
     * 
     * @return the html for the search result page headline 
     */
    public String buildSearchHeadline() {

        StringBuffer result = new StringBuffer(32);
        result.append(key("search.headline"));
        if (!isSearchAll() && !"/".equals(getStartFolder())) {
            result.append(key("search.headline.area"));
            result.append(getAreaName());
        } else {
            result.append(key("search.headline.all"));
        }
        return result.toString();
    }

    /**
     * Builds the html for the links to previous and next search result pages.<p>
     * 
     * @param search the search result object holding all necessary information
     * @return the html for the links to previous and next search result pages
     */
    public String buildSearchLinks(CmsSearch search) {

        StringBuffer result = new StringBuffer(32);
        boolean showPageLinks = false;
        // additional parameters for search result page in module folder
        StringBuffer additionalParams = new StringBuffer(16);
        if (search.getPreviousUrl() != null || search.getNextUrl() != null) {
            // there is at least one previous or next page, build page links
            showPageLinks = true;
            result.append("<div class=\"searchlinks\">\n");
            // fill additional parameters
            additionalParams.append("&uri=");
            additionalParams.append(CmsEncoder.encode(getRequestContext().getUri()));
            additionalParams.append("&");
            additionalParams.append(I_CmsConstants.C_PARAMETER_LOCALE);
            additionalParams.append("=");
            additionalParams.append(getRequestContext().getLocale());
            additionalParams.append("&");
            additionalParams.append(C_PARAM_SEARCHALL);
            additionalParams.append("=");
            additionalParams.append(isSearchAll());
        }
        if (search.getPreviousUrl() != null) {
            // build the link to the previous page
            result.append("\t<input type=\"button\" class=\"formbutton\" value=\"&lt;&lt; ");
            result.append(key("search.previous"));
            result.append("\" onclick=\"location.href=\'");
            result.append(link(search.getPreviousUrl()));
            result.append(additionalParams);
            result.append("\';\">");
        }
        Map pageLinks;
        try {
            // first set request context URI to form URI to obtain right page links
            getRequestContext().setUri(I_CmsWpConstants.C_VFS_PATH_MODULES + C_MODULE_NAME + "/pages/search.html");
            pageLinks = search.getPageLinks();
        } finally {
            // reset URI to page
            getRequestContext().setUri(getPageUri());
        }
        Iterator i = pageLinks.keySet().iterator();
        while (i.hasNext()) {
            // show the page links
            int pageNumber = ((Integer)i.next()).intValue();
            String pageLink = link((String)pageLinks.get(new Integer(pageNumber)));
            result.append("&nbsp;&nbsp;");
            if (pageNumber != search.getPage()) {
                // create a link to the page
                result.append("<a href=\"");
                result.append(pageLink);
                result.append(additionalParams);
                result.append("\">");
                result.append(pageNumber);
                result.append("</a>");
            } else {
                // show currently active page, but not as link
                result.append(pageNumber);
            }
        }
        if (search.getNextUrl() != null) {
            // build the link to the next page
            result.append("&nbsp;&nbsp;<input type=\"button\" class=\"formbutton\" value=\"");
            result.append(key("search.next"));
            result.append(" &gt;&gt;\" onclick=\"location.href=\'");
            result.append(link(search.getNextUrl()));
            result.append(additionalParams);
            result.append("\';\">");
        }
        if (showPageLinks) {
            result.append("\n</div>\n");
        }
        return result.toString();
    }

    /**
     * Builds the html for the search result list for a single page.<p>
     * 
     * @param results the list of result objects to display
     * @return the html for the search result list
     */
    public String buildSearchResultList(List results) {

        StringBuffer result = new StringBuffer(128);
        Iterator iterator = results.iterator();
        while (iterator.hasNext()) {
            // create the output for a single result
            CmsSearchResult entry = (CmsSearchResult)iterator.next();
            result.append("<div class=\"searchresult\">");
            String path = entry.getPath();
            // remove the site root from the path of the result
            path = getRequestContext().removeSiteRoot(path);
            // get the file icon
            String fileIcon = getFileIcon(path);
            if (CmsStringUtil.isNotEmpty(fileIcon)) {
                result.append("<a href=\"");
                result.append(link(path));
                result.append("\">");
                result.append(fileIcon);
                result.append("</a>&nbsp;");
            }

            result.append("<a href=\"");
            result.append(link(path));
            result.append("\">");
            String title = entry.getTitle();
            if (CmsStringUtil.isEmpty(title)) {
                // title is not set, show file name instead
                title = CmsResource.getName(path);
            }
            result.append(title);
            result.append("</a>&nbsp;(");
            result.append(entry.getScore());
            result.append("%)<br>");
            if (entry.getExcerpt() != null) {
                // add the excerpt
                result.append(entry.getExcerpt());
            }
            if (entry.getKeywords() != null) {
                // add the keywords
                result.append("<br>");
                result.append(key("search.keywords"));
                result.append(": ");
                result.append(entry.getKeywords());
            }
            if (entry.getDescription() != null) {
                // add the file description
                result.append("<br>");
                result.append(key("search.description"));
                result.append(": ");
                result.append(entry.getDescription());
            }
            // add the last modification date of the result
            result.append("<br>");
            result.append(messages().getDateTime(entry.getDateLastModified().getTime()));
            result.append("</div>\n");
        }
        return result.toString();
    }

    /**
     * Returns the HTML of the file icon for the given resource name or an empty String if no icon can be found.<p>
     * 
     * @param fileName the filename to check
     * @return the HTML of the file icon for the given resource name or an empty String
     */
    public String getFileIcon(String fileName) {

        int lastDot = fileName.lastIndexOf('.');
        String extension = "";
        // get the file extension 
        if ((lastDot > 0) && (lastDot < (fileName.length() - 1))) {
            extension = fileName.substring(lastDot + 1).toLowerCase();
            String iconPath = I_CmsWpConstants.C_VFS_PATH_MODULES
                + C_MODULE_NAME
                + "/resources/icons/ic_app_"
                + extension
                + ".gif";
            // check if an icon exists
            if (getCmsObject().existsResource(iconPath)) {
                StringBuffer result = new StringBuffer(8);
                String title = property(CmsPropertyDefinition.PROPERTY_TITLE, iconPath, "");
                result.append("<img src=\"");
                result.append(link(iconPath));
                result.append("\" border=\"0\" alt=\"");
                result.append(title);
                result.append("\" title=\"");
                result.append(title);
                result.append("\" align=\"left\" hspace=\"2\">");
                return result.toString();
            }
        }
        return "";

    }

    /**
     * Returns the URI of the page calling the search result page.<p>
     * 
     * @return the URI of the page calling the search result page
     */
    public String getPageUri() {

        return m_pageUri;
    }

    /**
     * Returns the "checked" attribute String if the user checked the "search all" checkbox.<p>
     * 
     * @return the "checked" attribute String or an empty String
     */
    public String getSearchAllChecked() {

        if (isSearchAll() || "/".equals(getStartFolder())) {
            return " checked=\"checked\"";
        }
        return "";
    }

    /**
     * Returns the list of search results depending on the search root and the form data.<p>
     * 
     * Either returns the results of the entire website or of the search root.<p>
     * 
     * @param search the instanciated search object
     * @return the results of the entire website or of the search root
     */
    public List getSearchResults(CmsSearch search) {

        List result;
        if (isSearchAll()) {
            // set search root to root folder to get all search results
            search.setSearchRoot("/");
        } else {
            // set search root to start folder
            search.setSearchRoot(getStartFolder());
        }
        String queryString = search.getQuery();
        try {
            if (queryString == null || "".equals(queryString.trim())) {
                // no query String found, return empty list
                queryString = "";
                search.setQuery("");
                result = new ArrayList();
            } else {
                // first set request context URI to form URI to obtain right page links
                getRequestContext().setUri(I_CmsWpConstants.C_VFS_PATH_MODULES + C_MODULE_NAME + "/pages/search.html");
                result = search.getSearchResult();
            }
        } finally {
            // reset URI to page
            getRequestContext().setUri(getPageUri());
        }
        return result;
    }

    /**
     * Includes the specified template element with the page URI specified in the request parameter "uri".<p>
     * 
     * After inclusion, the request context URI is reset to the old value.<p>
     * 
     * @param element the element (template selector) to display from the target template
     * @throws JspException if including the target fails
     */
    public void includeWithPageUri(String element) throws JspException {

        String template = property(CmsPropertyDefinition.PROPERTY_TEMPLATE, "search", null);
        // include target
        include(template, element);
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

        // call initialization of super class
        super.init(context, req, res);
        // initialize members from request
        m_pageUri = req.getParameter(CmsTemplateBean.C_PARAM_URI);
        if (m_pageUri == null) {
            m_pageUri = getRequestContext().getUri();
        }
        m_searchAll = Boolean.valueOf(req.getParameter(C_PARAM_SEARCHALL)).booleanValue();
        // change URI to point at page that called the search          
        getRequestContext().setUri(m_pageUri);
    }

    /**
     * Returns true if the entire website should be searched.<p>
     *
     * @return true if the entire website should be searched, otherwise false
     */
    public boolean isSearchAll() {

        return m_searchAll;
    }

    /**
     * Returns true if the checkbox to search the entire website should be displayed.<p>
     * 
     * @return true if the checkbox to search the entire website should be displayed, otherwise false
     */
    public boolean isSearchAllDisplayed() {

        return !"/".equals(getStartFolder());
    }

    /**
     * Sets if the entire website should be searched.<p>
     *
     * @param searchAll true if the entire website should be searched, otherwise false
     */
    public void setSearchAll(boolean searchAll) {

        m_searchAll = searchAll;
    }

}