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

import org.opencms.security.twofactor.CmsTwoFactorAuthenticationUserPolicy.CheckType;

import java.util.List;
import java.util.stream.Collectors;

import org.dom4j.Element;

/**
 * Represents the configuration for the two-factor authentication feature.
 */
public class CmsTwoFactorAuthenticationConfig {

    /** The original XML element from which the configuration was read. */
    private Element m_element;

    /** True if two-factor authentication should be enabled. */
    private boolean m_enabled;

    /** The issuer (displayed by authenticator apps). */
    private String m_issuer;

    /** The policy that controls which users should use two-factor authentication. */
    private CmsTwoFactorAuthenticationUserPolicy m_policy;

    /** The secret key used to encrypt/decrypt the two-factor authentication data for users. */
    private String m_secret;

    /** The setup message. */
    private String m_setupMessage;

    /**
     * Creates a new configuration from the given XML configuration element.
     *
     * @param element the element from which to read the configuration
     */
    public CmsTwoFactorAuthenticationConfig(Element element) {

        m_element = element;
        Element issuerElem = (Element)(element.selectSingleNode("issuer"));
        m_issuer = issuerElem.getTextTrim();
        m_enabled = Boolean.parseBoolean(((Element)element.selectSingleNode("enabled")).getTextTrim());
        m_secret = ((Element)element.selectSingleNode("secret")).getTextTrim();
        m_policy = parsePolicy(element);
        Element messageElem = (Element)(element.selectSingleNode("setup-message"));
        if (messageElem != null) {
            m_setupMessage = messageElem.getText();
        }

    }

    /**
     * Gets the configuration element.
     *
     * @return the configuration element
     */
    public Element getConfigElement() {

        return m_element;
    }

    /**
     * Gets the issuer (encoded in generated QR codes and displayed by authenticator apps).
     *
     * @return the issuer
     */
    public String getIssuer() {

        return m_issuer;
    }

    /**
     * Gets the policy which controls which users should use two-factor authentication.
     *
     * @return the user policy
     */
    public CmsTwoFactorAuthenticationUserPolicy getPolicy() {

        return m_policy;
    }

    /**
     * Gets the secret key which is used to encrypt/decrypt two-factor authentication information stored in the user's additional infos.
     *
     * @return the secret key
     */
    public String getSecret() {

        return m_secret;
    }

    /**
     * Gets the setup message.
     *
     * @return the setup message
     */
    public String getSetupMessage() {

        return m_setupMessage;

    }

    /**
     * Checks if two-factor authentication is enabled.
     *
     * <p>If it is disabled, users can just log in with user name and password.
     *
     * @return true if 2FA is enabled
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Parses a user policy from the configuration element.
     *
     * @param parent the parent XML element
     * @return the user policy
     */
    private CmsTwoFactorAuthenticationUserPolicy parsePolicy(Element parent) {

        List<CmsTwoFactorAuthenticationUserPolicy.Rule> includes = parsePolicyRules(parent, "include-users/*");
        List<CmsTwoFactorAuthenticationUserPolicy.Rule> excludes = parsePolicyRules(parent, "exclude-users/*");
        return new CmsTwoFactorAuthenticationUserPolicy(includes, excludes);

    }

    /**
     * Parses a list of policy rules from the given XML element.
     *
     * @param parent the parent XML element
     * @param xpath the xpath used to locate the rules
     * @return the parsed rules
     */
    private List<CmsTwoFactorAuthenticationUserPolicy.Rule> parsePolicyRules(Element parent, String xpath) {

        return parent.selectNodes(xpath).stream().map(node -> {
            Element elem = (Element)node;
            CheckType type = CheckType.valueOf(elem.getName());
            String name = elem.getTextTrim();
            return new CmsTwoFactorAuthenticationUserPolicy.Rule(type, name);
        }).collect(Collectors.toList());

    }

}
