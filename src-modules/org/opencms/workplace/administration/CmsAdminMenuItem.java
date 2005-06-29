/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/administration/CmsAdminMenuItem.java,v $
 * Date   : $Date: 2005/06/29 09:24:47 $
 * Version: $Revision: 1.11 $
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

package org.opencms.workplace.administration;

import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;
import org.opencms.workplace.tools.CmsToolMacroResolver;

/**
 * Html icon button implementation that generates the
 * required html code for a menu item.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAdminMenuItem {

    /** Enabled flag. */
    private boolean m_enabled;

    /** Help text. */
    private final String m_helpText;

    /** Path to the icon. */
    private final String m_iconPath;

    /** Dhtml id, from name. */
    private final String m_id;

    /** Link to follow when selected. */
    private final String m_link;

    /** Display name of the item. */
    private final String m_name;

    /** Target frame. */
    private final String m_target;

    /**
     * Default Constructor.<p> 
     * 
     * @param id a unique id
     * @param name the name of the item
     * @param iconPath the icon to display
     * @param link the link to open when selected
     * @param helpText the help text to display
     * @param enabled if enabled or not
     * @param target the target frame to open the link into
     */
    public CmsAdminMenuItem(
        String id,
        String name,
        String iconPath,
        String link,
        String helpText,
        boolean enabled,
        String target) {

        m_id = id;
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
     * Returns the dhtml unique id.<p>
     *
     * @return the dhtml unique id
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
     * Returns the display name.<p>
     *
     * @return the name
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
        html.append("\t\t<td>\n");
        String onClic = "return openView('" + getId() + "', '" + m_link + "', '" + m_target + "');";
        html.append(A_CmsHtmlIconButton.defaultButtonHtml(
            wp.getJsp(),
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            getId(),
            getName(),
            getHelpText(),
            isEnabled(),
            getIconPath(),
            onClic));

        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("</table>\n");
        return CmsToolMacroResolver.resolveMacros(html.toString(), wp);
    }

}