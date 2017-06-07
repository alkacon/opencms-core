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

package org.opencms.security;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

/**
 * Validating bean for changing the password.<p>
 *
 * @since 6.0.0
 */
public class CmsPasswordInfo {

    /** Cms Context. */
    private final CmsObject m_cms;
    /** Password Confirmation. */
    private String m_confirmation;
    /** Current (old) users password. */
    private String m_currentPwd;
    /** New Password. */
    private String m_newPwd;
    /** Current logged in user name. */
    private final String m_userName;

    /**
     * Default Constructor.<p>
     */
    public CmsPasswordInfo() {

        this(null);
    }

    /**
     * Use this Constructor if you need to check the old password of the current logged in user.<p>
     *
     * @param cms the cms context
     */
    public CmsPasswordInfo(CmsObject cms) {

        m_cms = cms;
        if (m_cms != null) {
            m_userName = m_cms.getRequestContext().getCurrentUser().getName();
        } else {
            m_userName = null;
        }
    }

    /**
     * Sets the new password for the current logged in user.<p>
     *
     * @throws CmsException if something goes wrong
     */
    public void applyChanges() throws CmsException {

        if (m_userName == null) {
            throw new CmsIllegalStateException(Messages.get().container(Messages.ERR_INVALID_USER_CONTEXT_0));
        }
        validate();
        m_cms.setPassword(m_userName, getCurrentPwd(), getNewPwd());
    }

    /**
     * Returns the confirmation.<p>
     *
     * @return the confirmation
     */
    public String getConfirmation() {

        return m_confirmation;
    }

    /**
     * Returns the current password.<p>
     *
     * @return the current password
     */
    public String getCurrentPwd() {

        return m_currentPwd;
    }

    /**
     * Returns the new password.<p>
     *
     * @return the new password
     */
    public String getNewPwd() {

        return m_newPwd;
    }

    /**
     * Sets the confirmation.<p>
     *
     * @param confirmation the confirmation to set
     */
    public void setConfirmation(String confirmation) {

        // leave password unchanged, if the new password and the confirmation is empty
        if (CmsStringUtil.isEmpty(getNewPwd()) && CmsStringUtil.isEmpty(confirmation)) {
            return;
        }
        m_confirmation = confirmation;
    }

    /**
     * Sets the current password.<p>
     *
     * @param currentPwd the current password to set
     */
    public void setCurrentPwd(String currentPwd) {

        if (m_userName == null) {
            throw new CmsIllegalStateException(Messages.get().container(Messages.ERR_INVALID_USER_CONTEXT_0));
        }
        try {
            m_cms.readUser(m_userName, currentPwd);
        } catch (CmsException e) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_INVALID_USER_PWD_1, m_userName));
        }
        m_currentPwd = currentPwd;
    }

    /**
     * Sets the new password.<p>
     *
     * @param newPwd the new password to set
     */
    public void setNewPwd(String newPwd) {

        // leave password unchanged, if the new password is empty
        if (CmsStringUtil.isEmpty(newPwd)) {
            return;
        }
        try {
            OpenCms.getPasswordHandler().validatePassword(newPwd);
        } catch (CmsSecurityException e) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_NEWPWD_0), e);
        }
        m_newPwd = newPwd;
    }

    /**
     * Validates that the confirmation matches the new password.<p>
     */
    public void validate() {

        if (CmsStringUtil.isEmpty(getNewPwd())) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_NEWPWD_0));
        }
        if (!getNewPwd().equals(getConfirmation())) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_NEWPWD_MISMATCH_0));
        }
    }
}