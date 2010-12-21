/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsContextMenuEntry.java,v $
 * Date   : $Date: 2010/12/21 10:23:32 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.util.CmsCollectionUtil;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;

import java.util.List;

import com.google.gwt.user.client.Command;

/**
 * Implementation for a context menu entry.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.3 $
 * 
 * @since version 8.0.0
 */
public class CmsContextMenuEntry implements I_CmsContextMenuEntry {

    /** The server bean for this menu entry. */
    private CmsContextMenuEntryBean m_bean;

    /** The command for this entry. */
    private Command m_command;

    /** The CSS image class for the icon in front of the label of this entry. */
    private String m_imageClass;

    /** The sub menu entries as list. */
    private List<I_CmsContextMenuEntry> m_subMenu;

    /**
     * Returns the bean.<p>
     *
     * @return the bean
     */
    public CmsContextMenuEntryBean getBean() {

        return m_bean;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsContextMenuEntry#getCommand()
     */
    public Command getCommand() {

        return m_command;
    }

    /**
     * Returns the imageClass.<p>
     *
     * @return the imageClass
     */
    public String getImageClass() {

        return m_imageClass;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsContextMenuEntry#getImagePath()
     */
    public String getImagePath() {

        return m_bean.getImagePath();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsContextMenuEntry#getJspPath()
     */
    public String getJspPath() {

        return m_bean.getJspPath();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsContextMenuEntry#getLabel()
     */
    public String getLabel() {

        return m_bean.getLabel();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsContextMenuEntry#getName()
     */
    public String getName() {

        return m_bean.getName();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsContextMenuEntry#getReason()
     */
    public String getReason() {

        return m_bean.getReason();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsContextMenuEntry#getSubMenu()
     */
    public List<I_CmsContextMenuEntry> getSubMenu() {

        return m_subMenu;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsContextMenuEntry#hasSubMenu()
     */
    public boolean hasSubMenu() {

        if (!CmsCollectionUtil.isEmptyOrNull(getSubMenu())) {
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsContextMenuEntry#isActive()
     */
    public boolean isActive() {

        return m_bean.isActive();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsContextMenuEntry#isSeparator()
     */
    public boolean isSeparator() {

        return m_bean.isSeparator();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsContextMenuEntry#isVisible()
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
     * @param cmd the command to set
     */
    public void setCommand(Command cmd) {

        m_command = cmd;
    }

    /**
     * Sets the imageClass.<p>
     *
     * @param imageClass the imageClass to set
     */
    public void setImageClass(String imageClass) {

        m_imageClass = imageClass;
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
