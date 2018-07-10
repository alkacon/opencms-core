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

package org.opencms.site;

import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Enumeration for different SSL Modes of sites.<p>
 */
public enum CmsSSLMode {

    /**No encryption. */
    NO("no", Messages.GUI_SSL_MODE_NOSSL_0),
    /**Manual ssl configuration of server. */
    MANUAL("manual", Messages.GUI_SSL_MODE_MANUAL_0),
    /**Manual ssl configuration of server with endpoint termination. */
    MANUAL_EP_TERMINATION("manual-ep-termination", Messages.GUI_SSL_MODE_MANUAL_EP_0),
    /**Encryption via Let's encrypt. */
    LETS_ENCRYPT("lets-encrypt", Messages.GUI_SSL_MODE_LETS_ENCRYPT_0),
    /**Encryption via secure server (the old OpenCms way). */
    SECURE_SERVER("secure-server", Messages.GUI_SSL_MODE_SECURE_SERVER_0);

    /**Message key for label. */
    private String m_message;

    /**XML value representing mode. */
    private String m_xmlValue;

    /**
     * Constructor.<p>
     *
     * @param xmlValue xmlValue
     * @param message Message
     */
    CmsSSLMode(String xmlValue, String message) {

        m_xmlValue = xmlValue;
        m_message = message;
    }

    /**
     * List of all available modes.<p>
     *
     * @param includeOldStyle include old Secure Server Styles?
     * @param includeLetsEncrypt if true, include the LETS_ENCRYPT mode in the result
     *
     * @return List<CmsSSLMode> -- the list of available modes
     */
    public static List<CmsSSLMode> availableModes(boolean includeOldStyle, boolean includeLetsEncrypt) {

        includeOldStyle = includeOldStyle & OpenCms.getSiteManager().isOldStyleSecureServerAllowed();
        List<CmsSSLMode> res = new ArrayList<CmsSSLMode>();
        for (CmsSSLMode mode : values()) {
            switch (mode) {
                case SECURE_SERVER:
                    if (includeOldStyle) {
                        res.add(mode);
                    }
                    break;
                case LETS_ENCRYPT:
                    if (includeLetsEncrypt) {
                        res.add(mode);
                    }
                    break;
                default:
                    res.add(mode);
            }
        }
        return res;
    }

    /**
     * The default SSL Mode.<p>
     *
     * @return CmsSSLMode
     */
    public static CmsSSLMode getDefault() {

        if (OpenCms.getSiteManager().isOldStyleSecureServerAllowed()) {
            return SECURE_SERVER;
        }
        return NO;
    }

    /**
     * Gets CmsSSLMode from given XML value.<p>
     *
     * @param xmlValue to get CmsSSLMode for
     * @return CmsSSLMode
     */
    public static CmsSSLMode getModeFromXML(String xmlValue) {

        for (CmsSSLMode mode : values()) {
            if (mode.getXMLValue().equals(xmlValue)) {
                return mode;
            }
        }
        return SECURE_SERVER;
    }

    /**
     * Gets localized message.<p>
     *
     * @return localized message
     */
    public String getLocalizedMessage() {

        return CmsVaadinUtils.getMessageText(m_message);
    }

    /**
     * Gets the XML value.<p>
     *
     * @return the XML value
     */
    public String getXMLValue() {

        return m_xmlValue;
    }

    /**
     * Returns if SSL Mode is secure.<p>
     *
     * @return true if secure
     */
    public boolean isSecure() {

        return this.equals(MANUAL_EP_TERMINATION) || this.equals(MANUAL) || this.equals(LETS_ENCRYPT);
    }
}
