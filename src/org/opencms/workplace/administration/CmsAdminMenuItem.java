/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/administration/Attic/CmsAdminMenuItem.java,v $
 * Date   : $Date: 2005/04/14 13:11:15 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.administration;

import org.opencms.util.I_CmsNamedObject;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.CmsHtmlUtil;

/**
 * Html icon button implementation that generates the
 * required html code for a menu item.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.3 $
 * @since 5.7.3
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
     * Default Constructor.<p> 
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
     * @param wp the workplace
     * 
     * @return html code
     */
    public String itemHtml(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(1024);
        html.append("<table border='0' cellspacing='0' cellpadding='0' width='100%' class='node' id='");
        html.append(getId());
        html.append("'>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td class='nodeImage'>\n");
        if (isEnabled()) {
            html.append(generatelink(wp));
        } else {
            html.append("<span onMouseOver=\"mouseHelpEvent('");
            html.append(getId());
            html.append("Help', true)\" onMouseOut=\"mouseHelpEvent('");
            html.append(getId());
            html.append("Help', false)\">");
        }
        html.append("\t\t<img src='");
        html.append(wp.getJsp().link(getIconPath()));
        html.append("' width='16' height='16' border='0' alt='");
        html.append(getName());
        html.append("'>\n");
        if (isEnabled()) {
            html.append("</a>");
        }
        html.append("\t\t</td>\n");
        html.append("\t\t<td width='100%'><span class='name'>\n");
        if (isEnabled()) {
            html.append(generatelink(wp));
        }
        html.append(getName());
        if (isEnabled()) {
            html.append("</a>");
        } else {
            html.append("</span>");
        }
        html.append("\t\t\t<div class='tip' id='" + getId() + "Help'>\n");
        if (!isEnabled()) {
            html.append(wp.key("widget.button.disabled.helptext") + " ");
        }
        html.append(getHelpText());
        html.append("</div>\n");
        html.append("\t\t</span></td>\n");
        html.append("\t</tr>\n");
        html.append("</table>\n");
        return wp.resolveMacros(html.toString());
    }

    /**
     * Generates a link, differentiating internal and external links.<p>
     * 
     * @param wp the workplace
     * 
     * @return html code
     */
    private String generatelink(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(1024);
        if (m_target.toString().indexOf("_") != 0) {
            html.append("<a href='#' title='");
            html.append(getName());
            html.append("' onClick=\"return openView('");
            html.append(getId());
            html.append("', '");
            html.append(wp.getJsp().link(m_link));
            html.append("', '");
            html.append(m_target);
            html.append("');\" onMouseOver=\"mouseHelpEvent('");
            html.append(getId());
            html.append("Help', true)\" onMouseOut=\"mouseHelpEvent('");
            html.append(getId());
            html.append("Help', false)\">");
        } else {
            html.append("<a target='");
            html.append(m_target);
            html.append("' href='");
            html.append(wp.getJsp().link(m_link));
            html.append("' title='");
            html.append(getName());
            html.append("' onMouseOver=\"mouseHelpEvent('");
            html.append(getId());
            html.append("Help', true)\" onMouseOut=\"mouseHelpEvent('");
            html.append(getId());
            html.append("Help', false)\">");
        }
        return html.toString();
    }
}