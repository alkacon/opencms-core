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

import org.opencms.configuration.I_CmsConfigurationParameterHandler;

/**
 * Defines methods for OpenCms password validation.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsPasswordHandler extends I_CmsConfigurationParameterHandler {

    /**
     * Flag for conversion of the password encoding.<p>
     */
    String CONVERT_DIGEST_ENCODING = "compatibility.convert.digestencoding";

    /**
     * String to identify the key value for md5 password hashes.<p>
     */
    String DIGEST_TYPE_MD5 = "md5";

    /**
     * String to identify the key value for unhashed passwords.<p>
     */
    String DIGEST_TYPE_PLAIN = "plain";

    /**
     * String to identify the key value for sha password hashes.<p>
     */
    String DIGEST_TYPE_SHA = "sha";

    /**
     * String to identify the key value for sha password hashes with 4 byte salt.<p>
     */
    String DIGEST_TYPE_SSHA = "ssha";

    /**
     * String to identify the key value for SCrypt password hashes.<p>
     */
    String DIGEST_TYPE_SCRYPT = "scrypt";

    /**
     * This method checks if the given plain text password is equal to the given
     * digested password.<p>
     *
     * Use this to check salted passwords. If the password is salted, it needs to be checked with
     * the salt (and possible other parameters) stored in the digested password.
     * Just digesting the password again and comparing the result to a previous digest won't
     * work because the salt will usually be different.<p>
     *
     * @param plainPassword the plain text password to check
     * @param digestedPassword the digested password to compare with the plain password
     * @param useFallback if <code>true</code>, then use a fall back hashing algorithm in case first validation fails
     *
     * @return <code>false</code> if the validation of the password failed
     */
    boolean checkPassword(String plainPassword, String digestedPassword, boolean useFallback);

    /**
     * Creates an OpenCms password digest according to the default setting for method/encodings.<p>
     *
     * @param password the password to encrypt
     * @return the password digest
     * @throws CmsPasswordEncryptionException if something goes wrong
     */
    String digest(String password) throws CmsPasswordEncryptionException;

    /**
     * Creates an OpenCms password digest.<p>
     *
     * @param password the password to encrypt
     * @param digestType the algorithm used for encryption (i.e. MD5, SHA ...)
     * @param inputEncoding the encoding used when converting the password to bytes (i.e. UTF-8)
     * @return the password digest
     * @throws CmsPasswordEncryptionException if something goes wrong
     */
    String digest(String password, String digestType, String inputEncoding) throws CmsPasswordEncryptionException;

    /**
     * Returns the default digest type.<p>
     *
     * @return the default digest type
     */
    String getDigestType();

    /**
     * Returns the default password encoding.<p>
     *
     * @return the default password encoding
     */
    String getInputEncoding();

    /**
     * Sets the default digest type.<p>
     *
     * @param digestType the digest type used
     */
    void setDigestType(String digestType);

    /**
     * Sets the default input encoding.<p>
     *
     * @param inputEncoding the encoding used for translation the password string to bytes
     */
    void setInputEncoding(String inputEncoding);

    /**
     * This method checks if a new password follows the rules for
     * new passwords, which are defined by a Class configured in
     * the opencms.properties file.<p>
     *
     * If this method throws no exception the password is valid.<p>
     *
     * @param password the password to check
     *
     * @throws CmsSecurityException if validation of the password failed
     */
    void validatePassword(String password) throws CmsSecurityException;
}