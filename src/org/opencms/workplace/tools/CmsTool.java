/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/CmsTool.java,v $
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

package org.opencms.workplace.tools;

import org.opencms.main.OpenCms;
import org.opencms.util.CmsNamedObjectContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsNamedObject;
import org.opencms.workplace.CmsWorkplace;

import java.util.Iterator;
import java.util.List;

/**
 * This is the implementation of an administration tool.<p>
 * 
 * An admin tool can be a link to itself through 
 * the <code>{@link #buttonHtml(CmsWorkplace)}</code> method,
 * as also a group of <code>{@link CmsToolGroup}</code>s through the 
 * <code>{@link #groupHtml(CmsWorkplace)}</code> method.<p>
 * 
 * @author <a href="mailto:m.moossen@alkacon.com">Michael Moossen</a> 
 * @version $Revision: 1.1 $
 * @since 6.0
 */
public class CmsTool implements I_CmsNamedObject {

    private final CmsNamedObjectContainer m_container = new CmsNamedObjectContainer(true, true);
    private boolean m_enabled;
    private final String m_helpText;
    private final String m_iconPath;
    private final String m_id;
    private final String m_link;
    private final String m_name;

    /**
     * Default Ctor.<p> 
     * 
     * @param name the name of the item
     * @param iconPath the icon to display
     * @param link the link to open when selected
     * @param helpText the help text to display
     * @param enabled if enabled or not
     */
    public CmsTool(String name, String iconPath, String link, String helpText, boolean enabled) {

        m_name = name;
        m_id = CmsHtmlUtil.getId(name);
        m_iconPath = iconPath;
        m_link = link;
        m_helpText = helpText;
        m_enabled = enabled;
    }

    /**
     * Adds a group.<p>
     * 
     * @param group the group
     * 
     * @see org.opencms.util.I_CmsNamedObjectContainer#addNamedObject(org.opencms.util.I_CmsNamedObject)
     */
    public void addToolGroup(CmsToolGroup group) {

        m_container.addNamedObject(group);
    }

    /**
     * Adds a menu item at the given position.<p>
     * 
     * @param group the group
     * @param position the position
     * 
     * @see org.opencms.util.I_CmsNamedObjectContainer#addNamedObject(org.opencms.util.I_CmsNamedObject, float)
     */
    public void addToolGroup(CmsToolGroup group, float position) {

        m_container.addNamedObject(group, position);
    }

    /**
     * Returns the necessary html code.<p>
     * 
     * @param page the jsp page to write the code to
     * 
     * @return html code
     */
    public String buttonHtml(CmsWorkplace page) {

        String link = OpenCms.getWorkplaceManager().getToolManager().cmsLinkFromContext(page.getJsp(), this);
        StringBuffer html = new StringBuffer(512);
        html.append(CmsStringUtil.code("<"
            + (isEnabled() ? "div" : "span")
            + " class='commonButton' style='background-image: url("
            + page.getJsp().link(page.resolveMacros(getIconPath()))
            + ")' title='"
            + page.resolveMacros(getName())
            + "' onMouseOver=\"mouseHelpEvent('"
            + getId()
            + "', true);\"  onMouseOut=\"mouseHelpEvent('"
            + getId()
            + "', false);\""
            + (isEnabled() ? " onClick=\"openPage('" + link + "');\"" : "")
            + ">"));
        html.append(CmsStringUtil.code(1, "<button name='"
            + getId()
            + "'"
            + (isEnabled() ? "" : " disabled")
            + ">"
            + page.resolveMacros(getName())
            + "</button>"));
        html.append(CmsStringUtil.code(1, "<span>" + page.resolveMacros(getName()) + "</span>"));
        html.append(CmsStringUtil.code(1, "<div class='tip' id='" + getId() + "'>"));
        if (!isEnabled()) {
            html.append(CmsStringUtil.code(2, page.key("widget.button.disabled.helptext") + " "));
        }
        html.append(CmsStringUtil.code(2, page.resolveMacros(getHelpText())));
        html.append(CmsStringUtil.code(1, "</div>"));
        html.append(CmsStringUtil.code("</" + (isEnabled() ? "div" : "span") + ">"));
        return page.resolveMacros(html.toString());
    }

    /**
     * Compares to admin tools by name.<p>
     * 
     * @param that the other object
     * 
     * @return <code>true</code> if the objects are equal
     */
    public boolean equals(Object that) {

        if (!this.getClass().isInstance(that)) {
            return false;
        }
        return this.getName().equals(((CmsTool)that).getName());
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
     * Returns the icon.<p>
     *
     * @return the icon
     */
    public String getIconPath() {

        return m_iconPath;
    }

    /**
     * Returns the id.<p>
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
     * Returns the group name.<p>
     *
     * @return the group name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the requested group.<p>
     * 
     * @param name the name of the group
     * 
     * @return the group
     * 
     * @see org.opencms.util.I_CmsNamedObjectContainer#getObject(String)
     */
    public CmsToolGroup getToolGroup(String name) {

        return (CmsToolGroup)m_container.getObject(name);
    }

    /**
     * Retuns a list of groups.<p>
     * 
     * @return a list of <code>{@link CmsToolGroup}</code>
     */
    public List getToolGroups() {

        return m_container.elementList(CmsToolGroup.class);
    }

    /**
     * Returns the necessary html code.<p>
     * 
     * @param page the jsp page to write the code to
     * 
     * @return html code
     */
    public String groupHtml(CmsWorkplace page) {

        StringBuffer html = new StringBuffer(512);
        Iterator itHtml = getToolGroups().iterator();
        while (itHtml.hasNext()) {
            CmsToolGroup group = (CmsToolGroup)itHtml.next();
            html.append(CmsStringUtil.code(1, group.groupHtml(page)));
        }
        return html.toString();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return getName().hashCode();
    }

    /**
     * Returns if enabled or disabled.<p>
     *
     * @return if enabled or disabled
     */
    public boolean isEnabled() {

        return m_enabled;
    }
}