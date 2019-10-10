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

package org.opencms.jsp.userdata;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;

import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Configuration for user data requests.<p>
 *
 * Mostly used to access texts to show to the user. Currently, a thin wrapper around an XML content.
 */
public class CmsUserDataRequestConfig {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserDataRequestConfig.class);

    /** The CMS context used to load the configuration. */
    private CmsObject m_cms;

    /** The XML content with the configuration. */
    private CmsXmlContent m_content;

    /** The locale for which the data should be read from the XML content. .*/
    private Locale m_locale;

    /**
     * Creates a new instance.
     *
     * @param cms the CMS context
     * @param content the content with the configuration
     * @param locale the locale
     */
    public CmsUserDataRequestConfig(CmsObject cms, CmsXmlContent content, Locale locale) {

        m_content = content;
        m_cms = cms;
        m_locale = OpenCms.getLocaleManager().getBestMatchingLocale(locale, content.getLocales(), content.getLocales());
    }

    /**
     * Gets the email subject.
     *
     * @return the email subject
     */
    public String getMailSubject() {

        return getText("MailSubject");
    }

    /**
     * Gets the email text.
     *
     * @return the email text
     */
    public String getMailText() {

        return getText("MailText");

    }

    /**
     * Gets the time for which user data requests are valid (in milliseconds).
     *
     * @return the time for which user data requests are valid
     */
    public long getRequestLifetime() {

        String lifetimeStr = getText("LinkHoursValid");
        long hours = 24;
        try {
            hours = Long.parseLong(lifetimeStr.trim());
        } catch (NumberFormatException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return hours * 60 * 60 * 1000;
    }

    /**
     * Gets the text for the given key.
     *
     * @param key the text key
     *
     * @return the text for that key
     */
    public String getText(String key) {

        if (m_content.hasValue(key, m_locale)) {
            try {
                return m_content.getValue(key, m_locale).getStringValue(m_cms);
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        return "???" + key + "???";

    }

}
