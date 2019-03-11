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

package org.opencms.ui.apps.user;

import org.opencms.file.CmsObject;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;

/**Type of element.*/
public enum CmsOuTreeType implements I_CmsOuTreeType {

    /**Group. */
    GROUP(Messages.GUI_USERMANAGEMENT_GROUPS_0, "g", true, new CmsCssIcon(OpenCmsTheme.ICON_GROUP),
    Messages.GUI_USERMANAGEMENT_NO_GROUPS_0),
    /**OU. */
    OU(Messages.GUI_USERMANAGEMENT_USER_OU_0, "o", true, new CmsCssIcon(OpenCmsTheme.ICON_OU), ""),
    /**Role. */
    ROLE(Messages.GUI_USERMANAGEMENT_ROLES_0, "r", true, new CmsCssIcon(OpenCmsTheme.ICON_ROLE),
    Messages.GUI_USERMANAGEMENT_NO_USER_0),
    /**User.*/
    USER(Messages.GUI_USERMANAGEMENT_USERS_0, "u", false, new CmsCssIcon(OpenCmsTheme.ICON_USER),
    Messages.GUI_USERMANAGEMENT_NO_USER_0);

    /**Bundle key for empty message.*/
    private String m_emptyMessageKey;

    /**Icon for type. */
    private CmsCssIcon m_icon;

    /**ID for entry. */
    private String m_id;

    /**Is expandable?*/
    private boolean m_isExpandable;

    /**Name of entry. */
    private String m_name;

    /**
     * constructor.<p>
     *
     * @param name name
     * @param id id
     * @param isExpandable boolean
     * @param icon icon
     * @param empty empty string
     */
    CmsOuTreeType(String name, String id, boolean isExpandable, CmsCssIcon icon, String empty) {

        m_name = name;
        m_id = id;
        m_isExpandable = isExpandable;
        m_icon = icon;
        m_emptyMessageKey = empty;
    }

    /**
     * Returns the key for the empty-message.<p>
     *
     * @return key as string
     */
    public String getEmptyMessageKey() {

        return m_emptyMessageKey;
    }

    /**
     * Get the icon.<p>
     *
     * @return CmsCssIcon
     */
    public CmsCssIcon getIcon() {

        return m_icon;
    }

    /**
     * Gets the id of the type.<p>
     *
     * @return id string
     */
    public String getId() {

        return m_id;
    }

    /**
     * Gets the name of the element.<p>
     *
     * @return name
     */
    public String getName() {

        return CmsVaadinUtils.getMessageText(m_name);
    }

    /**
     * Checks if type is expandable.<p>
     *
     * @return true if expandable
     */
    public boolean isExpandable() {

        return m_isExpandable;
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsOuTreeType#isGroup()
     */
    public boolean isGroup() {

        return GROUP.equals(this);
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsOuTreeType#isOrgUnit()
     */
    public boolean isOrgUnit() {

        return OU.equals(this);
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsOuTreeType#isRole()
     */
    public boolean isRole() {

        return ROLE.equals(this);
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsOuTreeType#isUser()
     */
    public boolean isUser() {

        return USER.equals(this);
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsOuTreeType#isValidForOu(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean isValidForOu(CmsObject cms, String ou) {

        return true;
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsOuTreeType#showInOuTable()
     */
    public boolean showInOuTable() {

        return !(OU.equals(this));
    }
}