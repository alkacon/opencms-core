/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.jlan;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;

import org.alfresco.jlan.server.auth.PasswordEncryptor;
import org.alfresco.jlan.server.auth.UserAccount;
import org.alfresco.jlan.server.auth.UsersInterface;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.springframework.extensions.config.ConfigElement;

/**
 * This class is used for authenticating OpenCms users to JLAN.<p>
 * 
 * Since JLAN requires MD4 password hashes for authentication, which are not by default stored in the database,
 * users who want to use the JLAN functionality should log in at least once into OpenCms normally so that the MD4 
 * hash of their password can be computed and stored.<p> 
 *
 */
public class CmsJlanUsers implements UsersInterface {

    /** The additional info key for the MD4 password hash. */
    public static final String JLAN_HASH = "jlan_hash";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJlanUsers.class);

    /** The CMS context. */
    private static CmsObject m_adminCms;

    /** 
     * Computes an MD4 hash for the password. 
     * 
     * @param password the password for which to compute the hash 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * 
     * @return the password hash 
     **/
    public static byte[] hashPassword(String password) throws InvalidKeyException, NoSuchAlgorithmException {

        PasswordEncryptor encryptor = new PasswordEncryptor();
        return encryptor.generateEncryptedPassword(password, null, PasswordEncryptor.MD4, null, null);
    }

    /**
     * Sets the CMS context.<p>
     * 
     * @param cms the context to set 
     */
    public static void setAdminCms(CmsObject cms) {

        if (m_adminCms == null) {
            m_adminCms = cms;
        } else {
            LOG.warn("Can't set admin CmsObject twice!");
        }
    }

    /**
     * @see org.alfresco.jlan.server.auth.UsersInterface#getUserAccount(java.lang.String)
     */
    public UserAccount getUserAccount(String userName) {

        try {
            CmsUser user = m_adminCms.readUser(userName);
            UserAccount account = new UserAccount(user.getName(), "");
            Object jlanHash = user.getAdditionalInfo(JLAN_HASH);
            if (jlanHash != null) {
                account.setMD4Password((byte[])jlanHash);
            } else {
                LOG.warn("JLAN: Could not log in user " + userName + " because of missing hash");
                return null;
            }
            return account;
        } catch (CmsException e) {
            return null;
        }
    }

    /**
     * @see org.alfresco.jlan.server.auth.UsersInterface#initializeUsers(org.alfresco.jlan.server.config.ServerConfiguration, org.springframework.extensions.config.ConfigElement)
     */
    public void initializeUsers(ServerConfiguration config, ConfigElement params) {

        // do nothing
    }

}
