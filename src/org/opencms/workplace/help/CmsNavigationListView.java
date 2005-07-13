/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/help/Attic/CmsNavigationListView.java,v $
 * Date   : $Date: 2005/07/13 10:19:15 $
 * Version: $Revision: 1.1 $
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspNavElement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Generates a simple TOC - list by using a navigatio model 
 * obtained from a <code>{@link org.opencms.jsp.CmsJspNavBuilder}</code>. <p>
 * 
 * This is a simpler facade to a fixed html - list based layoutsiteRootPath.<p>
 * 
 * @author Achim Westermann 
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 6.0.0
 */
public final class CmsNavigationListView {

    /** The end level of the navigation. **/
    private int m_endLevel;

    /** The CmsJspActionElement to use. **/
    private CmsJspActionElement m_jsp;
    /** The root path where the navigatio will start. **/
    private String m_siteRootPath;
    /** The start level of the navigation. **/
    private int m_startLevel;

    /**
     * Creates a <code>CmsNaviationListView</code> which uses the given 
     * <code>CmsJspActionElement</code> for accessing the underlying 
     * navigation API. <p>
     *   
     * @param jsp the <code>CmsJspActionElement</code> to use
     */
    public CmsNavigationListView(CmsJspActionElement jsp) {

        m_jsp = jsp;
        m_siteRootPath = m_jsp.getCmsObject().getRequestContext().getUri();
        m_startLevel = 2;
        m_endLevel = m_startLevel + 3;

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
     * @see #CmsNavigationListView(CmsJspActionElement)
     */
    public CmsNavigationListView(PageContext context, HttpServletRequest request, HttpServletResponse response) {

        this(new CmsJspActionElement(context, request, response));
    }

    /**
     * Returns a string containing the navigation created by using the internal members.<p>
     * 
     * The navigation is a nested html list. <p>
     * 
     * @return a string containing the navigation created by using the internal members
     */
    public String createNavigation() {

        //String sitePath = CmsResource.getPathPart(cms.getCmsObject().getRequestContext().getUri(), 0);
        StringBuffer result = new StringBuffer(2048);
        List navElements = m_jsp.getNavigation().getSiteNavigation(m_siteRootPath, m_endLevel);
        int oldLevel = -1;
        for (int i = 0; i < navElements.size(); i++) {
            CmsJspNavElement nav = (CmsJspNavElement)navElements.get(i);

            // get resource name of navelement
            String resName = nav.getResourceName();

            // compute current level from 1 to 3
            int level = nav.getNavTreeLevel() - (m_startLevel - 1);

            if (oldLevel != -1) {
                // manage level transitions
                if (level == oldLevel) {
                    // same level, close only previous list item
                    result.append("</li>\n");
                } else if (level < oldLevel) {
                    // lower level transition, determine delta
                    int delta = oldLevel - level;
                    for (int k = 0; k < delta; k++) {
                        // close sub list and list item
                        result.append("</li>\n</ul></li>\n");
                    }
                } else {
                    // higher level transition, create new sub list
                    result.append("<ul>\n");
                }
            } else {
                // initial list creation
                result.append("<ul>\n");
            }

            // create the navigation entry
            result.append("<li><a href=\"");
            result.append(m_jsp.link(resName));
            result.append("\" title=\"");
            result.append(nav.getNavText());
            result.append("\"");
            if (level == 1) {
                result.append(" class=\"bold\"");
            }
            result.append(">");
            result.append(nav.getNavText());
            result.append("</a>");
            // set old level for next loop
            oldLevel = level;

        }
        for (int i = 0; i < oldLevel; i++) {
            // close the remaining lists
            result.append("</li></ul>\n");
        }
        return result.toString();
    }

    /**
     * Returns the end level of the navigation to generate a view for. <p>
     * 
     * @return the end level of the navigation to generat a view for
     */
    public int getEndLevel() {

        return m_endLevel;
    }

    /**
     * Returns the site root path of the navigation to generate a view for. <p>
     * 
     * @return the site root path of the navigation to generate a view for.
     */
    public String getSiteRootPath() {

        return m_siteRootPath;
    }

    /**
     * Returns the start level of the navigation. <p>
     * 
     * @return the start level of the navigation
     */
    public int getStartLevel() {

        return m_startLevel;
    }

    /**
     * Set the end level of the navigation to generate a view for. <p>
     * 
     * @param endLevel the end level of the navigation to generate a view for to set
     */
    public void setEndLevel(int endLevel) {

        m_endLevel = endLevel;
    }

    /**
     * Set the site root path of the navigation to generate a view for. <p> 
     * 
     * The navigation will start there. <p> 
     * 
     * @param siteRootPath the site root path of the navigation to generate a view for to set
     */
    public void setSiteRootPath(String siteRootPath) {

        m_siteRootPath = siteRootPath;
    }

    /**
     * Set the start level of the navigation to generate a view for. <p>
     * 
     * @param startLevel the start level of the navigation to generate a view for to set
     */
    public void setStartLevel(int startLevel) {

        m_startLevel = startLevel;
    }
}
