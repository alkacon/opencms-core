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

package org.opencms.crypto;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;

/**
 * Interfaces for classes that encrypt text as text.
 */
public interface I_CmsTextEncryption extends I_CmsConfigurationParameterHandler {

    /**
     * Decrypts encrypted data.
     *
     * @param input the encrypted data
     * @return the decrypted data
     * @throws CmsEncryptionException if the data couldn't be decrypted
     */
    public String decrypt(String input) throws CmsEncryptionException;

    /**
     * Encrypts data.
     *
     * @param input the data to encrypt
     * @return the encrypted data
     * @throws CmsEncryptionException if the data couldn't be encrypted
     */
    public String encrypt(String input) throws CmsEncryptionException;

    /**
     * Gets the name of the encryption handler.
     *
     * @return the name
     */
    String getName();

    /**
     * Initializes the encryption handler.
     *
     * @param cms an Admin CMS context
     */
    void initialize(CmsObject cms);

    /**
     * Sets the name of the encryption handler.
     *
     * @param name the name that should be set
     */
    void setName(String name);

}
