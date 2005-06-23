/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/modules/CmsTemplateModules.java,v $
 * Date   : $Date: 2005/06/23 10:47:27 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.frontend.templateone.modules;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.frontend.templateone.CmsPropertyTemplateOne;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * A helper bean for the template one modules.<p>
 * 
 * Provides methods to create list views with category browsing and 
 * convenience methods to display date values and links.<p>
 * 
 * @author Thomas Weckert  
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsTemplateModules extends CmsJspActionElement {

    /** Request parameter name for the category folder. */
    public static final String C_PARAM_CATEGORYFOLDER = "categoryfolder";
    /** Request parameter name for the collector. */
    public static final String C_PARAM_COLLECTOR = "collector";
    /** Request parameter name for the list count. */
    public static final String C_PARAM_COUNT = "count";
    /** Request parameter name maximum number of elements to show. */
    public static final String C_PARAM_ELEMENTCOUNT = "elementcount";
    /** Request parameter name for the xmlcontent folder. */
    public static final String C_PARAM_FOLDER = "folder";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPropertyTemplateOne.class);

    private String m_categoryFolder;
    private boolean m_hasCategoryFolders;

    /**
     * @see CmsJspActionElement#CmsJspActionElement(PageContext, HttpServletRequest, HttpServletResponse)
     */
    public CmsTemplateModules(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Creates a html breadcrumb navigation.<p>
     * 
     * Instead of the NavText properties the title properties are used as anchor texts in this breadcrumb navigation! 
     * The navigation starts from a folder as specified in the request parameter "folder" and 
     * goes to the folder specified in the request parameter "categoryfolder".<p>
     * 
     * @param separator a String to separate the anchors in the breadcrumb navigation
     * @return the html for the breadcrumb navigation
     */
    public String buildHtmlNavBreadcrumb(String separator) {

        StringBuffer result = new StringBuffer(16);
        // get the value of the start folder request parameter
        String startfolder = getRequest().getParameter(C_PARAM_FOLDER);
        // calculate levels to go down
        int displayLevels = -(CmsResource.getPathLevel(getCategoryFolder()) - CmsResource.getPathLevel(startfolder) + 1);
        // get the navigation list
        List breadcrumb = getNavigation().getNavigationBreadCrumb(getCategoryFolder(), displayLevels, -1, true);

        for (int i = 0, n = breadcrumb.size(); i < n; i++) {
            CmsJspNavElement navElement = (CmsJspNavElement)breadcrumb.get(i);
            // get the title of the current navigation element
            String title = navElement.getTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().key(
                        Messages.LOG_ERR_MISSING_PROP_2,
                        navElement.getResourceName(),
                        CmsPropertyDefinition.PROPERTY_TITLE));
                }
                title = CmsResource.getName(navElement.getResourceName());
            }
            // generate the link for the navigation element
            String faqUri = link(getRequestContext().getUri()
                + "?"
                + C_PARAM_CATEGORYFOLDER
                + "="
                + navElement.getResourceName());

            // append the anchor
            result.append("<a href=\"");
            result.append(faqUri);
            result.append("\">");
            result.append(title);
            result.append("</a>");

            // append the separator
            if (i < (n - 1)) {
                result.append(separator);
            }
        }

        return result.toString();
    }

    /**
     * Creates a html &lt;li&gt; list of all folders inside the current folder.<p>
     * 
     * Additionally, behind each folder the number of resources of a specified resource type gets listed.<p>
     * 
     * @param faqResourceTypeId the resource type to count resources inside folders
     * @param attrs optional html attributes to use in the &lt;ul&gt; tag
     * @return a html &lt;li&gt; list of all folders inside the current folder
     * @throws CmsException if something goes wrong
     */
    public String buildHtmlNavList(int faqResourceTypeId, String attrs) throws CmsException {

        // get the start folder from request
        String startfolder = getCategoryFolder();

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(
                Messages.LOG_DEBUG_BUILD_HTML_NAVLIST_2,
                startfolder,
                new Integer(faqResourceTypeId)));
        }

        // read the resource tree
        CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
        List resourceTree = getCmsObject().readResources(startfolder, filter, true);

        String indent = "&nbsp;&nbsp;";
        StringBuffer result = new StringBuffer(32);

        if (resourceTree.size() > 0) {

            // open the list
            result.append("<ul");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(attrs)) {
                // append additional attributes
                result.append(" ").append(attrs);
            }

            result.append(">\n");

            int indentCount = 0;
            int startLevel = CmsResource.getPathLevel(getCmsObject().getSitePath((CmsResource)resourceTree.get(0)));
            int lastLevel = startLevel;
            // set flag that category folders are present
            m_hasCategoryFolders = true;

            for (int i = 0, n = resourceTree.size(); i < n; i++) {

                CmsResource resource = (CmsResource)resourceTree.get(i);
                String resourceName = getCmsObject().getSitePath(resource);

                // skip files
                if (!resource.isFolder()) {
                    continue;
                }

                // count resources of the specified type inside folder
                int faqCount = getResourceCount(resourceName, faqResourceTypeId);

                int level = CmsResource.getPathLevel(resourceName);

                if (lastLevel < level) {
                    // increase indentation level
                    indentCount++;
                } else if (lastLevel > level) {
                    // decrease indentation level
                    indentCount--;
                }

                // open a new list item (by closing a previous list item first eventually)
                if (level == startLevel) {
                    if (i == 0) {
                        result.append("<li>\n");
                    } else {
                        result.append("<br>&nbsp;&nbsp;\n");
                        result.append("</li>\n");
                        result.append("<li>\n");
                    }
                }

                // append a line break on sub-FAQs
                if (level > startLevel) {
                    result.append("<br>");
                }

                // append indentation for sub-FAQs
                for (int j = 0; j < indentCount; j++) {
                    result.append(indent);
                }

                String faqUri = link(getRequestContext().getUri() + "?" + C_PARAM_CATEGORYFOLDER + "=" + resourceName);

                String title = getCmsObject().readPropertyObject(
                    resourceName,
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    false).getValue(null);

                if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().key(
                            Messages.LOG_ERR_MISSING_PROP_2,
                            resourceName,
                            CmsPropertyDefinition.PROPERTY_TITLE));
                    }
                    title = resource.getName();
                }

                // append the anchor
                result.append("<a href=\"");
                result.append(faqUri);
                result.append("\">");
                if (level == startLevel) {
                    result.append("<b>").append(title).append("</b>");
                } else {
                    result.append(title);
                }
                result.append("</a>");

                // append number of FAQ articles on "top-level" FAQs
                if (level == startLevel) {
                    result.append("&nbsp;&nbsp;(");
                    result.append(faqCount);
                    result.append(")\n");
                }

                result.append("\n");
                lastLevel = level;
            }

            // close the last open list item
            result.append("</li>\n");

            // close the list
            result.append("</ul>\n");
        }

        return result.toString();
    }

    /**
     * Creates a HTML anchor from the values of three page context attribute names.
     * 
     * @param hrefAttrib the name of the page context attribute containing the link URL
     * @param descrAttrib the name of the page context attribute containing the link description
     * @param targetAttrib the name of the page context attribute containing the link target
     * @return an HTML anchor
     */
    public String getAnchor(String hrefAttrib, String descrAttrib, String targetAttrib) {

        String attribHref = (String)getJspContext().getAttribute(hrefAttrib);
        String attribDescr = (String)getJspContext().getAttribute(descrAttrib);
        boolean openBlank = Boolean.valueOf((String)getJspContext().getAttribute(targetAttrib)).booleanValue();

        String description = attribDescr;
        if (CmsStringUtil.isEmpty(attribDescr) || attribDescr.startsWith("???")) {
            description = attribHref;
        }

        String href = attribHref;
        if (!attribHref.toLowerCase().startsWith("http")) {
            href = link(attribHref);
        }

        String target = "";
        if (openBlank) {
            target = "_blank";
        }

        StringBuffer anchor = new StringBuffer();
        anchor.append("<a href=\"").append(href).append("\"");

        if (CmsStringUtil.isNotEmpty(description)) {
            anchor.append(" title=\"").append(description).append("\"");
        }

        if (CmsStringUtil.isNotEmpty(target)) {
            anchor.append(" target=\"").append(target).append("\"");
        }

        anchor.append(">").append(description).append("</a>");

        return anchor.toString();
    }

    /**
     * Returns the URI of the currently displayed category folder.<p>
     * 
     * @return the URI of the currently displayed category folder
     */
    public String getCategoryFolder() {

        if (m_categoryFolder == null) {
            // get the category folder from request
            m_categoryFolder = getRequest().getParameter(C_PARAM_CATEGORYFOLDER);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_categoryFolder)) {
                // no folder found in request, use folder parameter
                m_categoryFolder = getRequest().getParameter(C_PARAM_FOLDER);
            }
        }
        return m_categoryFolder;
    }

    /**
     * Returns the number of resources with a given resource type inside a folder.<p>
     * 
     * @param foldername the folder to read resources
     * @param resourceTypeId the desired resource type
     * 
     * @return the number of resources
     */
    public int getResourceCount(String foldername, int resourceTypeId) {

        int result = -1;

        try {
            // filter the resources with the specified id
            CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(resourceTypeId);
            List resources = getCmsObject().readResources(foldername, filter, false);
            result = resources.size();
        } catch (CmsException e) {
            // error reading the resources
            if (LOG.isErrorEnabled()) {
                LOG.error(org.opencms.db.Messages.get().key(
                    org.opencms.db.Messages.ERR_READ_RESOURCES_WITH_TYPE_2,
                    new Integer(resourceTypeId),
                    foldername), e);
            }
            result = -1;
        }

        return result;
    }

    /**
     * Returns true if the currently displayed folder contains subfolders which are used as category folders.<p>
     * 
     * This method has to be called after the method {@link CmsTemplateModules#buildHtmlNavList(int, String)}.<p> 
     * 
     * @return true if the currently displayed folder contains subfolders which are used as category folders
     */
    public boolean hasCategoryFolders() {

        return m_hasCategoryFolders;
    }

    /**
     * Saves a {@link Date} object in the page context that was created from the value of
     * a specified page context attribute.<p>
     * 
     * @param dateAttrib the name of the page context attribute containing the date string
     */
    public void setDate(String dateAttrib) {

        String timeString = (String)getJspContext().getAttribute(dateAttrib);
        long timestamp = (new Long(timeString)).longValue();
        Date date = new Date(timestamp);
        getJspContext().setAttribute("date", date);
    }

    /**
     * Returns true if the bread crumb navigation should be shown.<p>
     * 
     * This method has to be called after the method {@link CmsTemplateModules#buildHtmlNavList(int, String)}.<p> 
     * 
     * @return true if the bread crumb navigation should be shown
     */
    public boolean showNavBreadCrumb() {

        return hasCategoryFolders() || !getRequest().getParameter(C_PARAM_FOLDER).equals(getCategoryFolder());
    }
}