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

package org.opencms.ui.login;

import org.opencms.i18n.CmsResourceBundleLoader;
import org.opencms.main.CmsLog;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;

/**
 * Helper class for getting localized messages for the 'lock inactive users' feature.<p>
 *
 * We are not just using the standard Messages class here because the messages are supposed to be customizable,
 * and we do not want to lose the customizations if e.g. we install a new opencms.jar.<p>
 */
public class CmsInactiveUserMessages {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsInactiveUserMessages.class);

    /**
     * Text to display for the lockout screen.<p>
     *
     * @param locale the locale
     *
     * @return the localized text
     */
    public static String getLockoutText(Locale locale) {

        return getMessage("inactiveusers.lockout.text", locale);
    }

    /**
     * Gets the message for the given key and locale.<p>
     *
     * @param key the key
     * @param locale the locale
     * @return the localized text
     */
    public static String getMessage(String key, Locale locale) {

        ResourceBundle bundle = null;
        try {
            bundle = CmsResourceBundleLoader.getBundle("org.opencms.inactiveusers.custom", locale);
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            LOG.info("Customization bundle not found: org.opencms.inactiveusers.custom", e);
            bundle = CmsResourceBundleLoader.getBundle("org.opencms.ui.messages", locale);
            return bundle.getString(key);
        }
    }

    /**
     * Gets the header text for the report mail.<p>
     *
     * @param locale the locale
     * @return the localized text
     */
    public static String getReportHeader(Locale locale) {

        return getMessage("inactiveusers.report.header", locale);
    }

    /**
     * Gets the subject for the report mail.<p>
     *
     * @param locale the locale
     *
     * @return the localized text
     */
    public static String getReportSubject(Locale locale) {

        return getMessage("inactiveusers.report.subject", locale);
    }

}
