/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsDependencyIconAction.java,v $
 * Date   : $Date: 2005/10/13 11:06:32 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.CmsListResourceIconAction;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;

/**
 * Displays an icon action for dependency lists.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsDependencyIconAction extends CmsListResourceIconAction {

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** the type of the icon. */
    private final CmsDependencyIconActionType m_type;

    /**
     * Default Constructor.<p>
     * 
     * @param id the unique id
     * @param type the type of the icon
     * @param cms the cms context
     */
    public CmsDependencyIconAction(String id, CmsDependencyIconActionType type, CmsObject cms) {

        super(id + type.getId(), CmsGroupDependenciesList.LIST_COLUMN_TYPE, cms);
        m_type = type;
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#buttonHtml(org.opencms.workplace.CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        if (m_type == CmsDependencyIconActionType.RESOURCE) {
            return super.buttonHtml(wp);
        } else {
            return A_CmsHtmlIconButton.defaultButtonHtml(
                wp.getJsp(),
                resolveButtonStyle(),
                getId() + getItem().getId(),
                getId(),
                resolveName(wp.getLocale()),
                resolveHelpText(wp.getLocale()),
                isEnabled(),
                getIconPath(),
                null,
                resolveOnClic(wp.getLocale()),
                getColumnForTexts() == null);
        }
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
     */
    public String getIconPath() {

        if (m_type == CmsDependencyIconActionType.USER) {
            return PATH_BUTTONS + "user.png";
        } else if (m_type == CmsDependencyIconActionType.GROUP) {
            return PATH_BUTTONS + "group.png";
        } else {
            return super.getIconPath();
        }
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public CmsDependencyIconActionType getType() {

        return m_type;
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
     */
    public boolean isVisible() {

        boolean visible = false;
        if (getItem() != null) {
            CmsUUID id = new CmsUUID(getItem().getId());
            try {
                if (m_type == CmsDependencyIconActionType.RESOURCE) {
                    try {
                        getCms().readUser(id);
                    } catch (CmsException e1) {
                        try {
                            getCms().readGroup(id);
                        } catch (CmsException e2) {
                            visible = true;
                        }
                    }
                } else if (m_type == CmsDependencyIconActionType.USER) {
                    getCms().readUser(id);
                    visible = true;
                } else if (m_type == CmsDependencyIconActionType.GROUP) {
                    getCms().readGroup(id);
                    visible = true;
                } 
            } catch (CmsException e) {
                // not visible
            }
        } else {
            visible = super.isVisible();
        }
        return visible;
    }
}