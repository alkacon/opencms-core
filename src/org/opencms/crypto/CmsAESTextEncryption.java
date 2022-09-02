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

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

import com.google.common.io.BaseEncoding;

/**
 * Default text encryption class using AES, where the encryption key is generated from a string passed in as a parameter.
 */
public class CmsAESTextEncryption implements I_CmsTextEncryption {

    /** The name of the algorithm. */
    public static final String AES = "AES";

    /** URL parameter safe base64 encoder. */
    public static final BaseEncoding BASE64 = BaseEncoding.base64Url().withPadChar('.');

    /** The configuration parameter for configuring the secret. */
    public static final String PARAM_SECRET = "secret";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAESTextEncryption.class);

    /** The parameter configuration. */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /** The key used for encryption / decryption. */
    private SecretKey m_key;

    /** The name under which this is registered. */
    private String m_name;

    /**
     * Default constructor (used when instantiated automatically during OpenCms configuration).
     */
    public CmsAESTextEncryption() {}

    /**
     * Constructor used to manually, conveniently create a new encryption object with a given secret.
     *
     * <p>When using this constructor, it is not necessary to call initialize() to make the object usable.
     *
     * @param secret the secret used to generate the key
     */
    public CmsAESTextEncryption(String secret) {

        m_key = generateAESKey(secret);
    }

    /**
     * Helper method for generating an AES key from a secret string.
     *
     * @param secret the secret string
     * @return the AES key
     */
    public static SecretKey generateAESKey(String secret) {

        HKDFParameters params = HKDFParameters.defaultParameters(secret.getBytes(StandardCharsets.UTF_8));
        HKDFBytesGenerator keyGenerator = new HKDFBytesGenerator(new SHA256Digest());
        keyGenerator.init(params);
        byte[] keyBytes = new byte[16];
        keyGenerator.generateBytes(keyBytes, 0, 16);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);
        return keySpec;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_config.add(paramName, paramValue);
    }

    /**
     * @see org.opencms.crypto.I_CmsTextEncryption#decrypt(java.lang.String)
     */
    public String decrypt(String input) throws CmsEncryptionException {

        byte[] encryptedBytes = BASE64.decode(input);
        try {
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, m_key);
            byte[] decData = cipher.doFinal(encryptedBytes);
            String result = new String(decData, StandardCharsets.UTF_8);
            return result;
        } catch (Exception e) {
            throw new CmsEncryptionException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.crypto.I_CmsTextEncryption#encrypt(java.lang.String)
     */
    public String encrypt(String input) throws CmsEncryptionException {

        try {
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, m_key);
            byte[] encData = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            String lit = BASE64.encode(encData);
            return lit;
        } catch (Exception e) {
            throw new CmsEncryptionException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_config;
    }

    /**
     * @see org.opencms.crypto.I_CmsTextEncryption#getName()
     */
    public String getName() {

        return m_name;

    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // never called.
    }

    /**
     * @see org.opencms.crypto.I_CmsTextEncryption#initialize(org.opencms.file.CmsObject)
     */
    public void initialize(CmsObject cms) {

        String secret = m_config.get(PARAM_SECRET);
        if (secret == null) {
            throw new IllegalArgumentException("Parameter 'secret' must be set for CmsAESTextEncryption!");
        }
        m_key = generateAESKey(secret);
    }

    /**
     * @see org.opencms.crypto.I_CmsTextEncryption#setName(java.lang.String)
     */
    public void setName(String name) {

        if (m_name != null) {
            throw new IllegalStateException("Can't call setName twice!");
        }
        m_name = name;
    }

}
