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

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;

import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Validates the user passwords in with advanced password requirements.<p>
 */
public class CmsAdvancedPasswordHandler extends CmsDefaultPasswordHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAdvancedPasswordHandler.class);

    /**
     * @see org.opencms.security.I_CmsPasswordSecurityEvaluator#evaluatePasswordSecurity(java.lang.String)
     */
    @Override
    public SecurityLevel evaluatePasswordSecurity(String password) {

        try {
            validatePassword(password);
        } catch (CmsSecurityException sE) {
            return SecurityLevel.invalid;
        }

        // check password for weaknesses

        // first: length less than 10 chars
        if (password.length() < 10) {
            return SecurityLevel.weak;
        }
        // second: only capital letters
        if (password.equals(password.toUpperCase())) {
            return SecurityLevel.weak;
        }

        return SecurityLevel.strong;
    }

    /**
     * @see org.opencms.security.I_CmsPasswordSecurityEvaluator#getPasswordSecurityHint(java.util.Locale)
     */
    @Override
    public String getPasswordSecurityHint(Locale locale) {

        // return the hint
        return Messages.get().container(Messages.GUI_PWD_HINT_0).key(locale);
    }

    /**
     * @see org.opencms.security.I_CmsPasswordHandler#validatePassword(java.lang.String)
     */
    @Override
    public void validatePassword(String password) throws CmsSecurityException {

        // is null?
        if (password == null) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_PWD_NULL_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key());
            }
            throw new CmsSecurityException(message);
        }

        // first the size of the password: 8-16
        if ((password.length() < 8) || (password.length() > 64)) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_PWD_INVALID_SIZE_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key());
            }
            throw new CmsSecurityException(message);
        }

        // at least one capital letter must be present
        if (password.equals(password.toLowerCase())) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_PWD_NO_CAPITAL_LETTER_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key());
            }
            throw new CmsSecurityException(message);
        }

        // for the rest we need the char array
        char[] pw = password.toCharArray();
        int letters = 0;
        int specialCharacter = 0;
        for (int i = 0; i < pw.length; i++) {
            if (Character.isLetter(pw[i])) {
                letters++;
            } else {
                specialCharacter++;
            }
        }
        // are there at least two letters and two noLetters
        if ((letters < 2) || (specialCharacter < 2)) {
            CmsMessageContainer message = null;
            if (letters < 2) {
                message = Messages.get().container(Messages.ERR_PWD_NO_LETTERS_0);
            } else {
                message = Messages.get().container(Messages.ERR_PWD_NO_SPECIAL_CHARS_0);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key());
            }
            throw new CmsSecurityException(message);
        }

        // no descending or ascending row of more than two characters
        // and no more than two of a kind in a row
        char last = pw[0];
        int ascending = 0;
        int descending = 0;
        int equals = 0;
        for (int i = 1; i < pw.length; i++) {
            char current = pw[i];
            if ((last + 1) == current) {
                ascending++;
            } else {
                ascending = 0;
            }
            if ((last - 1) == current) {
                descending++;
            } else {
                descending = 0;
            }
            if (last == current) {
                equals++;
            } else {
                equals = 0;
            }
            if ((descending > 1) || (ascending > 1) || (equals > 1)) {
                Object[] msgArgs = new Object[] {
                    Character.valueOf(last),
                    Character.valueOf(current),
                    Integer.valueOf(descending),
                    Integer.valueOf(ascending),
                    Integer.valueOf(equals)};
                CmsMessageContainer message = Messages.get().container(Messages.ERR_PWD_CHARS_IN_A_ROW_5, msgArgs);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key());
                }
                throw new CmsSecurityException(message);
            }
            last = current;
        }
    }

}
