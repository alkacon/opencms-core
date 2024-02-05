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

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;

import com.lambdaworks.crypto.SCryptUtil;

/**
 * Default implementation for OpenCms password validation,
 * just checks if a password is at last 4 characters long.<p>
 *
 * @since 6.0.0
 */
public class CmsDefaultPasswordHandler
implements I_CmsPasswordHandler, I_CmsPasswordSecurityEvaluator, I_CmsPasswordGenerator {

    /** Parameter for SCrypt fall back. */
    public static String PARAM_SCRYPT_FALLBACK = "scrypt.fallback";

    /** Parameter for SCrypt settings. */
    public static String PARAM_SCRYPT_SETTINGS = "scrypt.settings";

    /**  The minimum length of a password. */
    public static final int PASSWORD_MIN_LENGTH = 4;

    /** The password length that is considered to be secure. */
    public static final int PASSWORD_SECURE_LENGTH = 8;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultPasswordHandler.class);

    /** The secure random number generator. */
    private static SecureRandom m_secureRandom;

    /** The configuration of the password handler. */
    private CmsParameterConfiguration m_configuration;

    /** The digest type used. */
    private String m_digestType = DIGEST_TYPE_SCRYPT;

    /** The encoding the encoding used for translating the input string to bytes. */
    private String m_inputEncoding = CmsEncoder.ENCODING_UTF_8;

    /** SCrypt fall back algorithm. */
    private String m_scryptFallback;

    /** SCrypt parameter: CPU cost, must be a power of 2. */
    private int m_scryptN;

    /** SCrypt parameter: Parallelization parameter. */
    private int m_scryptP;

    /** SCrypt parameter: Memory cost. */
    private int m_scryptR;

    /**
     * The constructor does not perform any operation.<p>
     */
    public CmsDefaultPasswordHandler() {

        m_configuration = new CmsParameterConfiguration();
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_configuration.put(paramName, paramValue);
    }

    /**
     * @see org.opencms.security.I_CmsPasswordHandler#checkPassword(String, String, boolean)
     */
    public boolean checkPassword(String plainPassword, String digestedPassword, boolean useFallback) {

        boolean success = false;
        if (DIGEST_TYPE_PLAIN.equals(m_digestType)) {

            success = plainPassword.equals(digestedPassword);
        } else if (DIGEST_TYPE_SCRYPT.equals(m_digestType)) {
            try {
                success = SCryptUtil.check(plainPassword, digestedPassword);
            } catch (IllegalArgumentException e) {
                // hashed valued not right, check if we want to fall back to MD5
                if (useFallback) {
                    try {
                        success = digestedPassword.equals(digest(plainPassword, m_scryptFallback, m_inputEncoding));
                    } catch (CmsPasswordEncryptionException e1) {
                        // success will be false
                    }
                }
            }
        } else {
            // old default MD5
            try {
                success = digestedPassword.equals(digest(plainPassword));
            } catch (CmsPasswordEncryptionException e) {
                // this indicates validation has failed
            }
        }
        return success;
    }

    /**
     * @see org.opencms.security.I_CmsPasswordHandler#digest(java.lang.String)
     */
    public String digest(String password) throws CmsPasswordEncryptionException {

        return digest(password, m_digestType, m_inputEncoding);
    }

    /**
     * @see org.opencms.security.I_CmsPasswordHandler#digest(java.lang.String, java.lang.String, java.lang.String)
     */
    public String digest(String password, String digestType, String inputEncoding)
    throws CmsPasswordEncryptionException {

        MessageDigest md;
        String result;

        try {
            if (DIGEST_TYPE_PLAIN.equals(digestType.toLowerCase())) {

                result = password;

            } else if (DIGEST_TYPE_SCRYPT.equals(digestType.toLowerCase())) {

                result = SCryptUtil.scrypt(password, m_scryptN, m_scryptR, m_scryptP);
            } else if (DIGEST_TYPE_SSHA.equals(digestType.toLowerCase())) {

                byte[] salt = new byte[4];
                byte[] digest;
                byte[] total;

                if (m_secureRandom == null) {
                    m_secureRandom = SecureRandom.getInstance("SHA1PRNG");
                }
                m_secureRandom.nextBytes(salt);

                md = MessageDigest.getInstance(DIGEST_TYPE_SHA);
                md.reset();
                md.update(password.getBytes(inputEncoding));
                md.update(salt);

                digest = md.digest();
                total = new byte[digest.length + salt.length];
                System.arraycopy(digest, 0, total, 0, digest.length);
                System.arraycopy(salt, 0, total, digest.length, salt.length);

                result = new String(Base64.encodeBase64(total));

            } else {

                md = MessageDigest.getInstance(digestType);
                md.reset();
                md.update(password.getBytes(inputEncoding));
                result = new String(Base64.encodeBase64(md.digest()));

            }
        } catch (NoSuchAlgorithmException e) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_UNSUPPORTED_ALGORITHM_1, digestType);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key(), e);
            }
            throw new CmsPasswordEncryptionException(message, e);
        } catch (UnsupportedEncodingException e) {
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_UNSUPPORTED_PASSWORD_ENCODING_1,
                inputEncoding);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key(), e);
            }
            throw new CmsPasswordEncryptionException(message, e);
        }

        return result;
    }

    /**
     * @see org.opencms.security.I_CmsPasswordSecurityEvaluator#evaluatePasswordSecurity(java.lang.String)
     */
    public SecurityLevel evaluatePasswordSecurity(String password) {

        SecurityLevel result;
        if (password.length() < PASSWORD_MIN_LENGTH) {
            result = SecurityLevel.invalid;
        } else if (password.length() < PASSWORD_SECURE_LENGTH) {
            result = SecurityLevel.weak;
        } else {
            result = SecurityLevel.strong;
        }
        return result;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_configuration;
    }

    /**
     * Returns the digestType.<p>
     *
     * @return the digestType
     */
    public String getDigestType() {

        return m_digestType;
    }

    /**
     * Returns the input encoding.<p>
     *
     * @return the input encoding
     */
    public String getInputEncoding() {

        return m_inputEncoding;
    }

    /**
     * @see org.opencms.security.I_CmsPasswordSecurityEvaluator#getPasswordSecurityHint(java.util.Locale)
     */
    public String getPasswordSecurityHint(Locale locale) {

        return Messages.get().getBundle(locale).key(
            Messages.GUI_PASSWORD_SECURITY_HINT_1,
            Integer.valueOf(PASSWORD_SECURE_LENGTH));
    }

    /**
     * @see org.opencms.security.I_CmsPasswordGenerator#getRandomPassword()
     */
    public String getRandomPassword() {

        return CmsDefaultPasswordGenerator.getRandomPWD();
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // simple default configuration does not need to be initialized
        if (LOG.isDebugEnabled()) {
            CmsMessageContainer message = Messages.get().container(Messages.LOG_INIT_CONFIG_CALLED_1, this);
            LOG.debug(message.key());
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_INIT_CONFIG_CALLED_1, this));
        }
        m_configuration = CmsParameterConfiguration.unmodifiableVersion(m_configuration);

        // Set default SCrypt parameter values
        m_scryptN = 16384; // CPU cost, must be a power of 2
        m_scryptR = 8; // Memory cost
        m_scryptP = 1; // Parallelization parameter

        String scryptSettings = m_configuration.get(PARAM_SCRYPT_SETTINGS);
        if (scryptSettings != null) {
            String[] settings = CmsStringUtil.splitAsArray(scryptSettings, ',');
            if (settings.length == 3) {
                // we just require 3 correct parameters
                m_scryptN = CmsStringUtil.getIntValue(settings[0], m_scryptN, "scryptN using " + m_scryptN);
                m_scryptR = CmsStringUtil.getIntValue(settings[1], m_scryptR, "scryptR using " + m_scryptR);
                m_scryptP = CmsStringUtil.getIntValue(settings[2], m_scryptP, "scryptP using " + m_scryptP);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_SCRYPT_PARAMETERS_1, scryptSettings));
                }
            }
        }

        // Initialize the SCrypt fall back
        m_scryptFallback = DIGEST_TYPE_MD5;
        String scryptFallback = m_configuration.get(PARAM_SCRYPT_FALLBACK);
        if (scryptFallback != null) {

            try {
                MessageDigest.getInstance(scryptFallback);
                // Configured fall back algorithm available
                m_scryptFallback = scryptFallback;
            } catch (NoSuchAlgorithmException e) {
                // Configured fall back algorithm not available, use default MD5
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_SCRYPT_PARAMETERS_1, scryptFallback));
                }
            }
        }
    }

    /**
     * Sets the digestType.<p>
     *
     * @param digestType the digestType to set
     */
    public void setDigestType(String digestType) {

        m_digestType = digestType.toLowerCase();
    }

    /**
     * Sets the input encoding.<p>
     *
     * @param inputEncoding the input encoding to set
     */
    public void setInputEncoding(String inputEncoding) {

        m_inputEncoding = inputEncoding;
    }

    /**
     * @see org.opencms.security.I_CmsPasswordHandler#validatePassword(java.lang.String)
     */
    public void validatePassword(String password) throws CmsSecurityException {

        if ((password == null) || (password.length() < PASSWORD_MIN_LENGTH)) {
            throw new CmsSecurityException(
                Messages.get().container(Messages.ERR_PASSWORD_TOO_SHORT_1, Integer.valueOf(PASSWORD_MIN_LENGTH)));
        }
    }
}