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

/**
 * The data needed to set up two-factor authentication for a user.
 */
public class CmsSecondFactorSetupInfo {

    /** A data URL with the QR code to scan with authenticator apps. */
    private String m_qrCodeImageUrl;

    /** The shared secret. */
    private String m_secret;

    /**
     * Creates a new instance.
     *
     * @param secret the shared secret
     * @param qrCodeImageUrl the QR code data URL
     */
    public CmsSecondFactorSetupInfo(String secret, String qrCodeImageUrl) {

        m_secret = secret;
        m_qrCodeImageUrl = qrCodeImageUrl;
    }

    /**
     * Gets the data URL with the QR code image that the user should scan.
     *
     * @return the QR code data URL
     */
    public String getQrCodeImageUrl() {

        return m_qrCodeImageUrl;
    }

    /**
     * Gets the shared secret in text form.
     *
     * @return the shared secret
     */
    public String getSecret() {

        return m_secret;
    }

}
