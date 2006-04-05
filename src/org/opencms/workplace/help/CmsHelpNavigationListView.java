/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/help/CmsHelpNavigationListView.java,v $
 * Date   : $Date: 2005/07/17 13:34:50 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
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

package org.opencms.workplace.help;

import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsIllegalArgumentException;

import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Generates a simple TOC - list by using a navigation model 
 * obtained from a <code>{@link org.opencms.jsp.CmsJspNavBuilder}</code>. <p>
 * 
 * This is a simpler facade to a fixed html - list based layout. 
 * Only a navigation root path and a desired depth have to be set. 
 * It is not specific to the online help.<p>
 * 
 * @author Achim Westermann 
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 6.0.0
 */
public final class CmsHelpNavigationListView {

    /** The depth (in levels) of the navigation. **/
    private int m_depth;

    /** The CmsJspActionElement to use. **/
    private CmsJspActionElement m_jsp;

    /** The root path where the navigation will start. **/
    private String m_navRootPath;

    /**
     * Creates a <code>CmsNaviationListView</code> which uses the given 
     * <code>CmsJspActionElement</code> for accessing the underlying 
     * navigation API. <p>
     *   
     * @param jsp the <code>CmsJspActionElement</code> to use
     */
    public CmsHelpNavigationListView(CmsJspActionElement jsp) {

        m_jsp = jsp;
        m_navRootPath = m_jsp.getCmsObject().getRequestContext().getUri();

    }

    /**
     * Creates a <code>CmsNaviationListView</code> which creates a 
     * <code>CmsJspActionElement</code> for accessing the underlying 
     * navigation API with the given arguments . <p>
     * 
     * @param context the <code>PageContext</code> to use
     * @param request the <code>HttpServletRequest</code> to use 
     * @param response the <code>HttpServletResponse</code> to use 
     * 
     */
    public CmsHelpNavigationListView(PageContext context, HttpServletRequest request, HttpServletResponse response) {

        this(new CmsJspActionElement(context, request, response));
    }

    private static String getSpaces(int n) {

        // avoid negative NegativeArraySizeException in case uri is missing
        n = Math.max(n, 0);
        StringBuffer result = new StringBuffer(n);
        for (; n > 0; n--) {
            result.append(' ');
        }
        return result.toString();
    }

    /**
     * Returns a string containing the navigation created by using the internal members.<p>
     * 
     * The navigation is a nested html list. <p>
     * 
     * @return a string containing the navigation created by using the internal members
     */
    public String createNavigation() {

        StringBuffer buffer = new StringBuffer(2048);
        int endlevel = calculateEndLevel();
        String spaces = getSpaces((endlevel - m_depth) * 2);
        if (m_navRootPath != null) {
            buffer.append("\n").append(spaces).append("<p>\n");
            buffer.append(spaces).append("  <ul>\n");
            List navElements = m_jsp.getNavigation().getSiteNavigation(m_navRootPath, endlevel);
            if (navElements.size() > 0) {
                createNavigationInternal(buffer, navElements);
            }
            buffer.append(spaces).append("  </ul>\n");
            buffer.append(spaces).append("</p>");
            return buffer.toString();
        } else {
            CmsIllegalArgumentException ex = new CmsIllegalArgumentException(Messages.get().container(
                Messages.GUI_HELP_ERR_SITEMAP_MISSING_PARAM_1,
                "navRootPath"));
            throw ex;
        }
    }

    /**
     * Returns the depth in levels of the navigation. <p>
     * 
     * @return the depth in levels of the navigation. 
     */
    public int getDepth() {

        return m_depth;
    }

    /**
     * Returns the navigation root path of the navigation to generate a view for. <p>
     * 
     * @return the navigation root path of the navigation to generate a view for.
     */
    public String getSiteRootPath() {

        return m_navRootPath;
    }

    /**
     * Set the depth in level of the navigation to generate a view for. <p>
     * 
     * @param depth  the depth in level of the navigation to generate a view for to set
     */
    public void setDepth(int depth) {

        m_depth = depth;
    }

    /**
     * Set the navigation root path of the navigation to generate a view for. <p> 
     * 
     * The navigation will start there. <p> 
     * 
     * @param navRootPath the navigation root path of the navigation to generate a view for to set
     */
    public void setNavigationRootPath(String navRootPath) {

        m_navRootPath = navRootPath;
    }

    private int calculateEndLevel() {

        int result = 0;
        if (m_navRootPath != null) {
            // where are we? (start level)

            StringTokenizer counter = new StringTokenizer(m_navRootPath, "/", false);
            // one less as level 0 nav elements accepted is one level (depth 1).
            result = counter.countTokens() - 1;
            if (!CmsResource.isFolder(m_navRootPath)) {
                // cut stuff like system/workpalce/locale/de/help/index.html
                result--;
            }
            result += m_depth;
        }
        if (result < 0) {
            result = 0;
        }
        return result;
    }

    private void createNavigationInternal(StringBuffer buffer, List navElements) {

        // take the element to render.
        CmsJspNavElement element = (CmsJspNavElement)navElements.remove(0);
        int elementLevel = element.getNavTreeLevel();
        String spacing = getSpaces(elementLevel * 2);
        // render element:
        buffer.append(spacing).append("<li>\n");
        buffer.append(spacing).append("  <a href=\"");
        buffer.append(m_jsp.link(element.getResourceName()));
        buffer.append("\" title=\"");
        buffer.append(element.getNavText());
        buffer.append("\"");
        if (elementLevel == 1) {
            buffer.append(" class=\"bold\"");
        }
        buffer.append(">");
        buffer.append(element.getNavText());
        buffer.append("</a>\n");

        // peek at the next (list is depth - first by contract)
        if (!navElements.isEmpty()) {
            CmsJspNavElement child = (CmsJspNavElement)navElements.get(0);
            int childLevel = child.getNavTreeLevel();
            if (elementLevel < childLevel) {
                // next one goes down a level: it is a child by tree means
                buffer.append(spacing).append("  <ul>\n");
            } else if (elementLevel == childLevel) {
                // it is a sibling: close our list item, no recursion
                buffer.append(spacing).append("</li>\n");
            } else {
                // next element gets up one layer
                // this has to happen because of the depth-first contract!
                buffer.append(spacing).append("  </li>\n").append(spacing).append("</ul>\n");
            }
            createNavigationInternal(buffer, navElements);
        } else {
            // no more next elements: get back and close all lists (by using the recursion we are in)
            buffer.append(spacing).append("  </li>\n").append(spacing).append("</ul>\n");
        }

    }
}
