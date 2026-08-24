/*
 * This library is part of OpenCms -
 * The Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.mail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Initialize a separate mailcap command map for the Javax Activation API.<p>
 *
 * Both Jakarta Activation and Javax Activation exist as dependencies. Both read their command maps from config files under META-INF (provided in their respective JARs),
 * but there is a collision for META-INF/mailcap (both jar files contain it). So for Javax Activation, we programmatically install a default (we don't need to
 * so for the Jakarta version because it uses a higher-priority fie META-INF/jakarta.mailcap.)
 */
public final class CmsMailcapCommandMapInitializer {

    /** Mailcap entries for Javax Mail. */
    private static final String JAVAX_MAILCAP = ""
        + "text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain\n"
        + "text/html;; x-java-content-handler=com.sun.mail.handlers.text_html\n"
        + "text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml\n"
        + "multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed; x-java-fallback-entry=true\n"
        + "message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822\n";

    /** Hidden constructor. */
    private CmsMailcapCommandMapInitializer() {

        // static utility class
    }

    /**
     * Installs namespace-specific default command maps.<p>
     */
    public static void initialize() {

        javax.activation.CommandMap.setDefaultCommandMap(
            new javax.activation.MailcapCommandMap(
                new ByteArrayInputStream(JAVAX_MAILCAP.getBytes(StandardCharsets.US_ASCII))));
    }
}
