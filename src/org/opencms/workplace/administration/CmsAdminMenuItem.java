/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/administration/Attic/CmsAdminMenuItem.java,v $
 * Date   : $Date: 2005/02/16 11:43:02 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.administration;

import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsNamedObject;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.CmsHtmlUtil;

/**
 * This is a html icon button implementation that generates the
 * required html code for a menu item.<p>
 * 
 * @author <a href="mailto:m.moossen@alkacon.com">Michael Moossen</a> 
 * @version $Revision: 1.1 $
 * @since 6.0
 */
public class CmsAdminMenuItem implements I_CmsNamedObject {

    private boolean m_enabled;
    private final String m_helpText;
    private final String m_iconPath;
    private final String m_id;
    private final String m_link;
    private final String m_name;
    private final String m_target;

    /**
     * Default Ctor.<p> 
     * 
     * @param name the name of the item
     * @param iconPath the icon to display
     * @param link the link to open when selected
     * @param helpText the help text to display
     * @param enabled if enabled or not
     * @param target the target frame to open the link into
     */
    public CmsAdminMenuItem(String name, String iconPath, String link, String helpText, boolean enabled, String target) {

        m_id = CmsHtmlUtil.getId(name);
        m_name = name;
        m_iconPath = iconPath;
        m_link = link;
        m_helpText = helpText;
        m_enabled = enabled;
        m_target = target;
    }

    /**
     * Returns the help text.<p>
     *
     * @return the help text
     */
    public String getHelpText() {

        return m_helpText;
    }

    /**
     * Returns the path to the icon.<p>
     *
     * @return the path to the icon
     */
    public String getIconPath() {

        return m_iconPath;
    }

    /**
     * Returns the id of the html component.<p>
     * 
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the link.<p>
     *
     * @return the link
     */
    public String getLink() {

        return m_link;
    }

    /**
     * @see org.opencms.util.I_CmsNamedObject#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the target.<p>
     *
     * @return the target
     */
    public String getTarget() {

        return m_target;
    }

    /**
     * Returns if enabled or disabled.<p>
     *
     * @return if enabled or disabled
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Returns the necessary html code.<p>
     * 
     * @param page the jsp page to write the code to
     * 
     * @return html code
     */
    public String itemHtml(CmsWorkplace page) {

        StringBuffer html = new StringBuffer(512);
        html.append(CmsStringUtil
            .code("<table border='0' cellspacing='0' cellpadding='0' width='100%' class='node' id='" + getId() + "'>"));
        html.append(CmsStringUtil.code(1, "<tr>"));
        html.append(CmsStringUtil.code(2, "<td class='nodeImage'>"));
        if (isEnabled()) {
            html.append(CmsStringUtil.code(3, generatelink(page)));
        } else {
            html.append(CmsStringUtil.code(3, "<span onMouseOver=\"mouseHelpEvent('"
                + getId()
                + "Help', true)\" onMouseOut=\"mouseHelpEvent('"
                + getId()
                + "Help', false)\">"));
        }
        html.append(CmsStringUtil.code(4, "<img src='"
            + page.getJsp().link(page.resolveMacros(getIconPath()))
            + "' width='16' height='16' border='0' alt='"
            + page.resolveMacros(getName())
            + "'>"));
        if (isEnabled()) {
            html.append(CmsStringUtil.code(3, "</a>"));
        }
        html.append(CmsStringUtil.code(2, "</td>"));
        html.append(CmsStringUtil.code(2, "<td width='100%'><span class='name'>"));
        if (isEnabled()) {
            html.append(CmsStringUtil.code(3, generatelink(page)));
        }
        html.append(CmsStringUtil.code(4, page.resolveMacros(getName())));
        if (isEnabled()) {
            html.append(CmsStringUtil.code(3, "</a>"));
        } else {
            html.append(CmsStringUtil.code(3, "</span>"));
        }
        html.append(CmsStringUtil.code(3, "<div class='tip' id='" + getId() + "Help'>"));
        if (!isEnabled()) {
            html.append(CmsStringUtil.code(4, page.key("widget.button.disabled.helptext") + " "));
        }
        html.append(CmsStringUtil.code(4, page.resolveMacros(getHelpText())));
        html.append(CmsStringUtil.code(3, "</div>"));
        html.append(CmsStringUtil.code(2, "</span></td>"));
        html.append(CmsStringUtil.code(1, "</tr>"));
        html.append(CmsStringUtil.code("</table>"));
        return page.resolveMacros(html.toString());
    }

    private String generatelink(CmsWorkplace page) {

        if (m_target.toString().indexOf("_") != 0) {
            return "<a href='#' title='"
                + getName()
                + "' onClick=\"return openView('"
                + getId()
                + "', '"
                + page.getJsp().link(page.resolveMacros(m_link))
                + "', '"
                + m_target
                + "');\" onMouseOver=\"mouseHelpEvent('"
                + getId()
                + "Help', true)\" onMouseOut=\"mouseHelpEvent('"
                + getId()
                + "Help', false)\">";
        }
        return "<a target='"
            + m_target
            + "' href='"
            + page.getJsp().link(page.resolveMacros(m_link))
            + "' title='"
            + getName()
            + "' onMouseOver=\"mouseHelpEvent('"
            + getId()
            + "Help', true)\" onMouseOut=\"mouseHelpEvent('"
            + getId()
            + "Help', false)\">";
    }
}