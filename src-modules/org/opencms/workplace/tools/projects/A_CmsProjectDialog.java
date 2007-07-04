/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/Attic/A_CmsProjectDialog.java,v $
 * Date   : $Date: 2007/07/04 16:57:36 $
 * Version: $Revision: 1.8 $
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

package org.opencms.workplace.tools.projects;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.workplace.CmsWidgetDialog;

/**
 * Base Dialog for project related dialogs.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsProjectDialog extends CmsWidgetDialog {

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Auxiliary Property for better representation of the bean managerGroupId property. */
    private String m_managerGroup;

    /** Auxiliary Property for better representation of the bean groupId property. */
    private String m_userGroup;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public A_CmsProjectDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Returns the manager Group name.<p>
     *
     * @return the manager Group name
     */
    public String getManagerGroup() {

        return m_managerGroup;
    }

    /**
     * Returns the user Group name.<p>
     *
     * @return the user Group name
     */
    public String getUserGroup() {

        return m_userGroup;
    }

    /**
     * Sets the manager Group name.<p>
     *
     * @param managerGroup the manager Group name to set
     */
    public void setManagerGroup(String managerGroup) {

        checkGroup(managerGroup);
        m_managerGroup = managerGroup;
    }

    /**
     * Sets the user Group name.<p>
     *
     * @param userGroup the user Group name to set
     */
    public void setUserGroup(String userGroup) {

        checkGroup(userGroup);
        m_userGroup = userGroup;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Checks if the given group name is a valid opencms user group.<p>
     * 
     * @param groupName the group name to chek
     */
    private void checkGroup(String groupName) {

        try {
            getCms().readGroup(groupName);
        } catch (CmsException e) {
            throw new CmsIllegalArgumentException(e.getMessageContainer());
        }
    }
}