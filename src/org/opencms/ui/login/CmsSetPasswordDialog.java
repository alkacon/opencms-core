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

package org.opencms.ui.login;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.workplace.CmsWorkplaceLoginHandler;

import java.util.Locale;

import org.apache.commons.logging.Log;

import com.vaadin.server.UserError;

/**
 * Dialog used to change the password.<p>
 */
public class CmsSetPasswordDialog extends CmsChangePasswordDialog {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSetPasswordDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param user the user
     * @param locale the locale
     */
    public CmsSetPasswordDialog(final CmsObject cms, CmsUser user, Locale locale) {
        super(cms, user, locale);
        // hide the old password field, it's not required
        m_form.hideOldPassword();
    }

    /**
     * Submits the password.<p>
     */
    @Override
    void submit() {

        if ((m_user == null) || m_user.isManaged()) {
            return;
        }
        String password1 = m_form.getPassword1();
        String password2 = m_form.getPassword2();
        String error = null;
        if (validatePasswords(password1, password2)) {
            try {
                m_cms.setPassword(m_user.getName(), password1);
                CmsTokenValidator.clearToken(CmsLoginUI.m_adminCms, m_user);
            } catch (CmsException e) {
                error = e.getLocalizedMessage(m_locale);
                LOG.debug(e.getLocalizedMessage(), e);
            } catch (Exception e) {
                error = e.getLocalizedMessage();
                LOG.error(e.getLocalizedMessage(), e);
            }

            if (error != null) {
                m_form.setErrorPassword1(new UserError(error), OpenCmsTheme.SECURITY_INVALID);
            } else {
                CmsVaadinUtils.showAlert(
                    Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_PWCHANGE_SUCCESS_HEADER_0),
                    Messages.get().getBundle(A_CmsUI.get().getLocale()).key(
                        Messages.GUI_PWCHANGE_GUI_PWCHANGE_SUCCESS_CONTENT_0),
                    new Runnable() {

                        public void run() {

                            A_CmsUI.get().getPage().setLocation(
                                OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                                    CmsLoginUI.m_adminCms,
                                    CmsWorkplaceLoginHandler.LOGIN_HANDLER,
                                    false));
                        }
                    });
            }
        }
    }
}
