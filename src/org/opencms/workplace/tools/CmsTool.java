/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.workplace.CmsWorkplace;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of an administration tool.<p>
 *
 * An admin tool can be a link to itself through
 * the <code>{@link #buttonHtml(CmsWorkplace)}</code> method,
 * as also a group of <code>{@link CmsToolGroup}</code>s through the
 * <code>{@link #groupHtml(CmsWorkplace)}</code> method.<p>
 *
 * @since 6.0.0
 */
public class CmsTool {

    /** Sub-tools container. */
    private final CmsIdentifiableObjectContainer<CmsToolGroup> m_container = new CmsIdentifiableObjectContainer<CmsToolGroup>(
        true,
        true);

    /** Handler that represents this tool. */
    private I_CmsToolHandler m_handler;

    /** Dhtml id, from name. */
    private final String m_id;

    /**
     * Default Constructor.<p>
     *
     * @param id a unique id
     * @param handler the handler that represents this tool
     */
    public CmsTool(String id, I_CmsToolHandler handler) {

        m_id = id;
        m_handler = handler;

    }

    /**
     * Adds a group.<p>
     *
     * @param group the group
     *
     * @see org.opencms.workplace.tools.CmsIdentifiableObjectContainer#addIdentifiableObject(String, Object)
     */
    public void addToolGroup(CmsToolGroup group) {

        m_container.addIdentifiableObject(group.getName(), group);
    }

    /**
     * Adds a group at the given position.<p>
     *
     * @param group the group
     * @param position the position
     *
     * @see org.opencms.workplace.tools.CmsIdentifiableObjectContainer#addIdentifiableObject(String, Object, float)
     */
    public void addToolGroup(CmsToolGroup group, float position) {

        m_container.addIdentifiableObject(group.getName(), group, position);
    }

    /**
     * Returns the necessary html code for a link to this tool.<p>
     *
     * @param wp the jsp page to write the code to
     *
     * @return html code
     */
    public String buttonHtml(CmsWorkplace wp) {

        if (!m_handler.isVisible(wp)) {
            return "";
        }
        String link = CmsToolManager.linkForToolPath(
            wp.getJsp(),
            getHandler().getPath(),
            getHandler().getParameters(wp));
        String onClic = "openPage('" + link + "');";
        return A_CmsHtmlIconButton.defaultButtonHtml(
            CmsHtmlIconButtonStyleEnum.BIG_ICON_TEXT,
            getId(),
            m_handler.getShortName(),
            m_handler.isEnabled(wp) ? m_handler.getHelpText() : m_handler.getDisabledHelpText(),
            m_handler.isEnabled(wp),
            m_handler.getIconPath(),
            m_handler.getConfirmationMessage(),
            onClic);
    }

    /**
     * Compares two tools by name.<p>
     *
     * @param obj the other tool
     *
     * @return <code>true</code> if the tools have the same name
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsTool) {
            return ((CmsTool)obj).m_id.equals(m_id);
        }
        return false;
    }

    /**
     * Returns the handler.<p>
     *
     * @return the handler
     */
    public I_CmsToolHandler getHandler() {

        return m_handler;
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
     * Returns the requested group.<p>
     *
     * @param name the name of the group
     *
     * @return the group
     *
     * @see org.opencms.workplace.tools.CmsIdentifiableObjectContainer#getObject(String)
     */
    public CmsToolGroup getToolGroup(String name) {

        return m_container.getObject(name);
    }

    /**
     * Returns a list of groups.<p>
     *
     * @return a list of <code>{@link CmsToolGroup}</code>
     */
    public List<CmsToolGroup> getToolGroups() {

        return m_container.elementList();
    }

    /**
     * Returns the necessary html code for the tool subgroups.<p>
     *
     * @param wp the jsp page to write the code to
     *
     * @return html code
     */
    public String groupHtml(CmsWorkplace wp) {

        List<CmsTool> subTools = OpenCms.getWorkplaceManager().getToolManager().getToolsForPath(
            wp,
            getHandler().getPath(),
            false);
        Iterator<CmsTool> itSubTools = subTools.iterator();
        m_container.clear();
        while (itSubTools.hasNext()) {
            CmsTool subTool = itSubTools.next();
            // locate group
            CmsToolGroup group = null;
            String groupName = CmsToolMacroResolver.resolveMacros(subTool.getHandler().getGroup(), wp);

            // in the parent tool
            group = getToolGroup(groupName);
            if (group == null) {
                // if does not exist, create it
                String gid = "group" + getToolGroups().size();
                group = new CmsToolGroup(gid, groupName);
                addToolGroup(group, subTool.getHandler().getPosition());
            }

            // add to group
            group.addAdminTool(subTool, subTool.getHandler().getPosition());

        }

        StringBuffer html = new StringBuffer(512);
        Iterator<CmsToolGroup> itHtml = getToolGroups().iterator();
        while (itHtml.hasNext()) {
            CmsToolGroup group = itHtml.next();
            html.append(group.groupHtml(wp));
        }
        return CmsToolMacroResolver.resolveMacros(html.toString(), wp);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_handler.getName().hashCode();
    }
}