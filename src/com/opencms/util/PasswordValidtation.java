package com.opencms.util;

import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.workplace.CmsXmlLanguageFile;

/**
 * Validates OpenCms passwords.
 * 
 * @author Hanjo Riege
 * @version 1.0
 */
public class PasswordValidtation implements I_PasswordValidation {

    /**
     * The constructor does not perform any operation.<p>
     */
    public PasswordValidtation() {
    }

    /**
     * The method to check the password.<p>
     *
     * @param cms The CmsObject
     * @param password the password to check
     * @param oldPassword the old password
     * @throws CmsException if password was not valid
     */
    public void checkNewPassword(CmsObject cms, String password, String oldPassword) throws CmsException {
        if (password.length() < I_CmsConstants.C_PASSWORD_MINIMUMSIZE) {
            CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
            throw new CmsException(lang.getLanguageValue("error.reason.chpwd3"), CmsException.C_INVALID_PASSWORD);
        }
    }
}