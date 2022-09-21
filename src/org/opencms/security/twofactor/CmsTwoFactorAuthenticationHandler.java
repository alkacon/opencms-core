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

package org.opencms.security.twofactor;

import org.opencms.crypto.CmsAESTextEncryption;
import org.opencms.crypto.CmsEncryptionException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsUserLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.workplace.CmsWorkplaceMessages;

import java.util.Locale;

import org.apache.commons.logging.Log;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;

/**
 * Implements two-factor authentication for OpenCms users via TOTP.
 *
 * <p>
 * This class can both set up a TOTP second factor for a user, as well as be used to authenticate a user using the verification code generated using their second factor.
 */
public class CmsTwoFactorAuthenticationHandler {

    /**
     * The hashing algorithm to use.
     * <p>Other algorithms are technically possible, but potentially unsupported by some apps.
     */
    public static final HashingAlgorithm ALGORITHM = HashingAlgorithm.SHA1;

    /** User info attribute for storing the second factor data. */
    public static final String ATTR_TWOFACTOR_INFO = "two_factor_auth";

    /**
     * The number of digits to use for verification codes.
     * <p>Other numbers are technically possible, but potentially unsupported by some apps.
     */
    public static final int DIGITS = 6;

    /** JSON key for storing the shared secret. */
    public static final String KEY_SECRET = "secret";

    /** JSON key for storing the user name. */
    public static final String KEY_USER = "user";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTwoFactorAuthenticationHandler.class);

    /** The stored CMS context. */
    private CmsObject m_cms;

    /** The code generator used for TOTP. */
    private final CodeGenerator m_codeGenerator = new DefaultCodeGenerator(ALGORITHM, DIGITS);

    /** The configuration object. */
    private CmsTwoFactorAuthenticationConfig m_config;

    /** The encryption for the additional infos of a user. */
    private CmsAESTextEncryption m_encryption;

    /** Shared secret generator (threadsafe). */
    private final SecretGenerator m_secretGenerator = new DefaultSecretGenerator();

    /** The time provider used for TOTP. */
    private final TimeProvider m_timeProvider = new SystemTimeProvider();

    /** The code verifier used for TOTP. */
    private final CodeVerifier m_verifier = new DefaultCodeVerifier(m_codeGenerator, m_timeProvider);

    /**
     * Creates a new instance.
     *
     * @param adminCms an Admin CMS context
     * @param config the configuration for the two-factor authentication
     */
    public CmsTwoFactorAuthenticationHandler(CmsObject adminCms, CmsTwoFactorAuthenticationConfig config) {

        m_config = config;
        m_cms = adminCms;
        if (config != null) {
            m_encryption = new CmsAESTextEncryption(config.getSecret());
        }
    }

    /**
     * Generates the information needed to share a secret with the user for the purpose of setting up 2FA.
     *
     * <p>This contains both the data for a scannable QR code as well as the secret in textual form.
     *
     * @param user the user
     * @return the second factor setup information
     */
    public CmsSecondFactorSetupInfo generateSetupInfo(CmsUser user) {

        checkEnabled();
        try {
            String secret = m_secretGenerator.generate();
            // apart from the secret, the QR code contains other data like user name, etc.
            String issuer = m_config.getIssuer();
            int period = 30;
            // use full name as fallback if macro resolution fails
            String label = user.getFullName();
            QrData qrData = new QrData.Builder().label(label).secret(secret).issuer(issuer).algorithm(ALGORITHM).digits(
                DIGITS).period(period).build();
            QrGenerator generator = new ZxingPngQrGenerator();
            byte[] imageData = generator.generate(qrData);
            String qrImageData = Utils.getDataUriForImage(imageData, "image/png");
            return new CmsSecondFactorSetupInfo(secret, qrImageData);
        } catch (QrGenerationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the message to display during two-factor authentication setup.
     *
     * @param locale the locale
     * @return the message
     */
    public String getSetupMessage(Locale locale) {

        String rawMessage = m_config.getSetupMessage();
        CmsMacroResolver resolver = new CmsMacroResolver();
        CmsWorkplaceMessages messages = OpenCms.getWorkplaceManager().getMessages(locale);
        resolver.setMessages(messages);
        return resolver.resolveMacros(rawMessage);
    }

    /**
     * Checks if there is already a second factor configured for the given user.
     *
     * <p>For users excluded from two-factor authentication, this will usually return false, while for users who should use two-factor authentication, the result depends
     * on whether the second factor has already been set up.
     *
     *
     * @param user the user to check
     * @return true if there is a second factor set up for the given user
     */
    public boolean hasSecondFactor(CmsUser user) {

        return user.getAdditionalInfo().containsKey(ATTR_TWOFACTOR_INFO);

    }

    /**
     * Checks if two-factor authentication is enabled.
     *
     * @return true if two-factor auth is enabled
     */
    public boolean isEnabled() {

        return (m_config != null) && m_config.isEnabled();
    }

    /**
     * Checks if two-factor authentication should be used for the given user.
     *
     * @param user the user to check
     * @return true if two-factor authentication should be used
     */
    public boolean needsTwoFactorAuthentication(CmsUser user) {

        if (!isEnabled()) {
            return false;
        }

        boolean result = m_config.getPolicy().shouldUseTwoFactorAuthentication(m_cms, user);
        return result;
    }

    /**
     * Deletes the two-factor authentication in the user object, but does not write the user to the database.
     *
     * @param user the user for whom 2FA should be reset
     */
    public void resetTwoFactorAuthentication(CmsUser user) {

        user.deleteAdditionalInfo(ATTR_TWOFACTOR_INFO);
    }

    /**
     * Sets up the second factor for the given user, and immediately verifies it with the authentication code given.
     *
     * @param newUser the user for whom to set up the second factor
     * @param code contains both the shared secret and the authentication code generated by the user
     * @return true if the second factor could be set up, false if  the verification failed
     *
     * @throws CmsSecondFactorSetupException in unexpected circumstances, e.g. if the user already has a second factor set up or there is no authentication code
     */
    public boolean setUpAndVerifySecondFactor(CmsUser newUser, CmsSecondFactorInfo code)
    throws CmsSecondFactorSetupException {

        checkEnabled();
        String secret = code.getSecret();
        if (secret == null) {
            throw new CmsSecondFactorSetupException("Secret must not be null.");
        }
        JSONObject secondFactorInfo = decodeSecondFactor(newUser);
        if (secondFactorInfo != null) {
            // we shouldn't get here during the normal operation of the system
            throw new CmsSecondFactorSetupException("Two-factor authentication already set up.");
        }
        try {
            secondFactorInfo = new JSONObject();
            secondFactorInfo.put(KEY_SECRET, secret);
            // we store the user name (and check it later) so you can't just transfer the stored second factor data to a different user via the GUI
            secondFactorInfo.put(KEY_USER, newUser.getName());
            if (m_verifier.isValidCode(secret, code.getCode())) {
                encodeSecondFactor(newUser, secondFactorInfo);
                return true;
            }
            return false;
        } catch (JSONException e) {
            // should not happen
            throw new CmsSecondFactorSetupException(e);
        }

    }

    /**
     * Gets called when a user is changed so we can check if the second factor information
     * was changed and generate appropriate log messages.
     *
     * @param requestContext the current request context
     *
     * @param oldUser the user before modification
     * @param newUser the user after modification
     */
    @SuppressWarnings("null")
    public void trackUserChange(CmsRequestContext requestContext, CmsUser oldUser, CmsUser newUser) {

        String info1 = (String)oldUser.getAdditionalInfo(ATTR_TWOFACTOR_INFO);
        String info2 = (String)newUser.getAdditionalInfo(ATTR_TWOFACTOR_INFO);
        if ((info1 == null) && (info2 == null)) {
            return;
        } else if ((info1 == null) && (info2 != null)) {
            CmsUserLog.logSecondFactorAdded(requestContext, oldUser.getName());
        } else if ((info1 != null) && (info2 == null)) {
            CmsUserLog.logSecondFactorReset(requestContext, oldUser.getName());
        } else if (!info1.equals(info2)) {
            CmsUserLog.logSecondFactorInfoModified(requestContext, oldUser.getName());
        }

    }

    /**
     * Verifies the second factor information for a user.
     *
     * <p>Note that this method assumes that two-factor authentication should be applied to the given user, and always checks the second factor.
     *
     * @param user the user
     * @param secondFactorInfo the second factor information
     * @return true if the verification was successful
     */
    public boolean verifySecondFactor(CmsUser user, CmsSecondFactorInfo secondFactorInfo) {

        if (secondFactorInfo == null) {
            return false;
        }
        if (secondFactorInfo.getSecret() != null) {
            LOG.warn("Secret set in second-factor information for non-setup case", new Exception());
        }

        JSONObject secondFactorJson = decodeSecondFactor(user);
        if (!user.getName().equals(secondFactorJson.optString(KEY_USER))) {
            LOG.error("User mismatch for two-factor authentication data for user: " + user.getName());
            return false;
        }
        String secret = secondFactorJson.optString(KEY_SECRET);
        boolean result = m_verifier.isValidCode(secret, secondFactorInfo.getCode());
        return result;

    }

    /**
     * Verifies that the verification code is correct for a secret.
     *
     * @param secondFactorInfo object containing the secret and verification code
     *
     * @return true if the verification is successful
     */
    public boolean verifySecondFactorSetup(CmsSecondFactorInfo secondFactorInfo) {

        return m_verifier.isValidCode(secondFactorInfo.getSecret(), secondFactorInfo.getCode());

    }

    /**
     * Throws an exception if 2FA is disabled.
     */
    private void checkEnabled() {

        if (!isEnabled()) {
            throw new UnsupportedOperationException("Two-factor authentication is disabled");
        }

    }

    /**
     * Helper method to decode the second factor information for a user.
     *
     * @param user the user
     * @return the JSON representing the second factor information
     */
    private JSONObject decodeSecondFactor(CmsUser user) {

        try {
            String val = (String)(user.getAdditionalInfo().get(ATTR_TWOFACTOR_INFO));
            if (val == null) {
                return null;
            }
            JSONObject result = new JSONObject(m_encryption.decrypt(val));
            return result;
        } catch (JSONException | CmsEncryptionException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Helper method to encode the second factor information for a user.
     *
     * @param user the user
     * @param json the JSON data to encode
     */
    private void encodeSecondFactor(CmsUser user, JSONObject json) {

        try {
            user.getAdditionalInfo().put(ATTR_TWOFACTOR_INFO, m_encryption.encrypt(json.toString()));
        } catch (CmsEncryptionException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        }
    }

}
