/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsGroupStateAction.java,v $
 * Date   : $Date: 2005/06/21 15:54:15 $
 * Version: $Revision: 1.4 $
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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;

import java.text.MessageFormat;
import java.util.List;

/**
 * Shows direct/indirect assigned groups and enabled/disabled a remove action.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsGroupStateAction extends CmsListDefaultAction {

    /** The cms context. */
    private CmsObject m_cms;

    /** The user name. */
    private String m_userName;

    /**
     * Default Constructor.<p>
     * 
     * @param id the unique id
     * @param cms the cms context
     * @param userName the user name
     */
    protected CmsGroupStateAction(String id, CmsObject cms, String userName) {

        super(id);
        m_userName = userName;
        m_cms = cms;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getHelpText()
     */
    public CmsMessageContainer getHelpText() {

        if (isEnabled()) {
            return super.getHelpText();
        }
        return Messages.get().container(Messages.GUI_USERGROUPS_LIST_ACTION_STATE_DISABLED_HELP_0);
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getIconPath()
     */
    public String getIconPath() {

        if (isEnabled()) {
            return super.getIconPath();
        } else if (super.getIconPath() == null) {
            return null;
        } else {
            return A_CmsListDialog.ICON_DISABLED;
        }
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getId()
     */
    public String getId() {

        if (!isEnabled()) {
            return "x" + super.getId();
        }
        return super.getId();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getName()
     */
    public CmsMessageContainer getName() {

        if (!isEnabled()) {
            return Messages.get().container(Messages.GUI_USERGROUPS_LIST_ACTION_STATE_DISABLED_NAME_0);
        }
        return super.getName();
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#helpTextHtml(org.opencms.workplace.CmsWorkplace)
     */
    public String helpTextHtml(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(512);
        // enabled
        String ht = super.getHelpText().key(wp.getLocale());
        String helptext = new MessageFormat(ht, wp.getLocale()).format(new Object[] {""});
        if (getColumn() == null
            || helptext.equals(new MessageFormat(ht, wp.getLocale()).format(new Object[] {getItem().get(getColumn())}))) {
            html.append(A_CmsHtmlIconButton.defaultHelpHtml(super.getId(), helptext));
        }
        // disabled
        String ht2 = Messages.get().key(wp.getLocale(), Messages.GUI_USERGROUPS_LIST_ACTION_STATE_DISABLED_HELP_0, null);
        String helptext2 = new MessageFormat(ht2, wp.getLocale()).format(new Object[] {""});
        if (getColumn() == null
            || helptext2.equals(new MessageFormat(ht2, wp.getLocale()).format(new Object[] {getItem().get(getColumn())}))) {
            html.append(A_CmsHtmlIconButton.defaultHelpHtml("x" + super.getId(), helptext2));
        }
        return html.toString();

    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isEnabled()
     */
    public boolean isEnabled() {

        if (getItem() != null) {
            String groupName = (String)getItem().get(A_CmsUserGroupsList.LIST_COLUMN_NAME);
            try {
                List dGroups = m_cms.getDirectGroupsOfUser(m_userName);
                CmsGroup group = m_cms.readGroup(groupName);
                return dGroups.contains(group);
            } catch (Exception e) {
                // ignore
            }
        }
        return super.isEnabled();
    }

    /**
     * Sets the userName.<p>
     *
     * @param userName the userName to set
     */
    public void setUserName(String userName) {

        m_userName = userName;
    }
}