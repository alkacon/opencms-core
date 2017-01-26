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

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;

import java.util.Locale;

/**
 * Default implementation for the validation handler.<p>
 *
 * @since 6.3.0
 */
public class CmsDefaultValidationHandler implements I_CmsValidationHandler {

    /** The email regular expression. */
    public static final String EMAIL_REGEX = "\\A[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\z";

    /** The user name constraints. */
    public static final String USERNAME_CONSTRAINTS = "-._~$@";

    /** The zipcode regular expression. */
    public static final String ZIPCODE_REGEX = "[\\w]*";

    /**
     * The email should only be composed by digits and standard english letters, points,
     * underscores and exact one "At" symbol.<p>
     *
     * @see org.opencms.security.I_CmsValidationHandler#checkEmail(java.lang.String)
     */
    public void checkEmail(String email) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isNotEmpty(email)) {
            email = email.trim();
            email = email.toLowerCase(Locale.ROOT);
        }
        if (!CmsStringUtil.validateRegex(email, EMAIL_REGEX, false)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_EMAIL_VALIDATION_1, email));
        }
    }

    /**
     * @see org.opencms.security.I_CmsValidationHandler#checkFirstname(java.lang.String)
     */
    public void checkFirstname(String firstname) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(firstname)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_FIRSTNAME_EMPTY_0));
        }
    }

    /**
     * @see org.opencms.security.I_CmsValidationHandler#checkGroupName(java.lang.String)
     */
    public void checkGroupName(String name) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_BAD_GROUPNAME_EMPTY_0));
        }
    }

    /**
     * @see org.opencms.security.I_CmsValidationHandler#checkLastname(java.lang.String)
     */
    public void checkLastname(String lastname) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(lastname)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_LASTNAME_EMPTY_0));
        }
    }

    /**
     * A user name can only be composed of digits,
     * standard ASCII letters and the symbols defined in {@link #USERNAME_CONSTRAINTS}.<p>
     *
     * @see org.opencms.security.I_CmsValidationHandler#checkUserName(java.lang.String)
     */
    public void checkUserName(String userName) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(userName)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_BAD_USERNAME_EMPTY_0, userName));
        }

        CmsStringUtil.checkName(userName, USERNAME_CONSTRAINTS, Messages.ERR_BAD_USERNAME_4, Messages.get());
    }

    /**
     * That means, the parameter should only be composed by digits and standard english letters.<p>
     *
     * @see org.opencms.security.I_CmsValidationHandler#checkZipCode(java.lang.String)
     */
    public void checkZipCode(String zipcode) throws CmsIllegalArgumentException {

        if (!CmsStringUtil.validateRegex(zipcode, ZIPCODE_REGEX, true)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_ZIPCODE_VALIDATION_1, zipcode));
        }
    }

}