package com.opencms.util;

import com.opencms.core.*;
import com.opencms.file.*;

/**
 * Defines methods for password validating objects. 
 */
public interface I_PasswordValidation {

    /**
     * The method to check the password.
     *
     * @param cms The CmsObject
     * @param password the password to check
     * @param oldPassword the old password.
     * @throws CmsException if something goes wrong
     */
    void checkNewPassword(CmsObject cms, String password, String oldPassword) throws CmsException;
}