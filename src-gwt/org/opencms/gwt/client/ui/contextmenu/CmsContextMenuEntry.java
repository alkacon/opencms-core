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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.util.CmsClientCollectionUtil;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * Implementation for a context menu entry.<p>
 *
 * @since version 8.0.0
 */
public class CmsContextMenuEntry implements I_CmsContextMenuEntry {

    /** The server bean for this menu entry. */
    private CmsContextMenuEntryBean m_bean;

    /** The context menu handler. */
    private I_CmsContextMenuHandler m_handler;

    /** The command for this entry. */
    private I_CmsContextMenuCommand m_menuCommand;

    /** The structure id of the resource to execute the command on. */
    private CmsUUID m_structureId;

    /** The sub menu entries as list. */
    private List<I_CmsContextMenuEntry> m_subMenu;

    /**
     * Constructor.<p>
     *
     * @param handler the context menu handler
     * @param structureId the structure id
     * @param menuCommand the menu command
     */
    public CmsContextMenuEntry(
        I_CmsContextMenuHandler handler,
        CmsUUID structureId,
        I_CmsContextMenuCommand menuCommand) {

        m_menuCommand = menuCommand;
        m_handler = handler;
        m_structureId = structureId;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        if (m_menuCommand != null) {
            m_menuCommand.execute(m_structureId, m_handler, m_bean);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#generateMenuItem()
     */
    public A_CmsContextMenuItem generateMenuItem() {

        if ((m_menuCommand != null) && m_menuCommand.hasItemWidget() && m_bean.isActive()) {
            return m_menuCommand.getItemWidget(m_structureId, m_handler, m_bean);
        } else {
            return new CmsContextMenuItem(this);
        }
    }

    /**
     * Returns the bean.<p>
     *
     * @return the bean
     */
    public CmsContextMenuEntryBean getBean() {

        return m_bean;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getIconClass()
     */
    public String getIconClass() {

        return m_bean.getIconClass();

    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getJspPath()
     */
    public String getJspPath() {

        return m_bean.getJspPath();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getLabel()
     */
    public String getLabel() {

        return m_bean.getLabel();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getName()
     */
    public String getName() {

        return m_bean.getName();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getReason()
     */
    public String getReason() {

        return m_bean.getReason();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#getSubMenu()
     */
    public List<I_CmsContextMenuEntry> getSubMenu() {

        return m_subMenu;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#hasSubMenu()
     */
    public boolean hasSubMenu() {

        if (!CmsClientCollectionUtil.isEmptyOrNull(getSubMenu())) {
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#isActive()
     */
    public boolean isActive() {

        return m_bean.isActive();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#isSeparator()
     */
    public boolean isSeparator() {

        return m_bean.isSeparator();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#isVisible()
     */
    public boolean isVisible() {

        return m_bean.isVisible();
    }

    /**
     * Sets the bean.<p>
     *
     * @param bean the bean to set
     */
    public void setBean(CmsContextMenuEntryBean bean) {

        m_bean = bean;
    }

    /**
     * Sets the command.<p>
     *
     * @param command the command to set
     */
    public void setMenuCommand(I_CmsContextMenuCommand command) {

        m_menuCommand = command;
    }

    /**
     * Sets the sub menu.<p>
     *
     * @param subMenu the sub menu to set
     */
    public void setSubMenu(List<I_CmsContextMenuEntry> subMenu) {

        m_subMenu = subMenu;
    }
}
