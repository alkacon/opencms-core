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

package org.opencms.configuration.preferences;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Preference for the start site.<p>
 */
public class CmsSitePreference extends CmsBuiltinPreference {

    /** The nice name. */
    private static final String NICE_NAME = "%(key."
        + org.opencms.workplace.commons.Messages.GUI_PREF_STARTUP_SITE_0
        + ")";

    /**
     * Creates a new instance.<p>
     *
     * @param name the preference name
     */
    public CmsSitePreference(String name) {

        super(name);
        m_basic = true;
    }

    /**
     * Gets the options for the site selector.<p>
     *
     * @param cms the CMS context
     * @param locale the locale for the select options
     *
     * @return the options for the site selector
     */
    public static String getSiteSelectOptionsStatic(CmsObject cms, Locale locale) {

        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(
            cms,
            true,
            false,
            cms.getRequestContext().getOuFqn());

        StringBuffer resultBuffer = new StringBuffer();
        Iterator<CmsSite> i = sites.iterator();
        int counter = 0;
        while (i.hasNext()) {
            CmsSite site = i.next();
            String siteRoot = site.getSiteRoot();
            if (!siteRoot.endsWith("/")) {
                siteRoot += "/";
            }
            if (counter != 0) {
                resultBuffer.append("|");
            }
            resultBuffer.append(siteRoot).append(":").append(
                CmsWorkplace.substituteSiteTitleStatic(site.getTitle(), locale));
            counter++;
        }

        if (sites.size() < 1) {
            // no site found, assure that at least the current site is shown in the selector
            String siteRoot = cms.getRequestContext().getSiteRoot();
            CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
            if (!siteRoot.endsWith("/")) {
                siteRoot += "/";
            }
            String title = "";
            if (site != null) {
                title = site.getTitle();
            }
            resultBuffer.append(siteRoot).append(":").append(title);
        }
        return resultBuffer.toString();
    }

    /**
     * @see org.opencms.configuration.preferences.CmsBuiltinPreference#getPropertyDefinition(org.opencms.file.CmsObject)
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition() {

        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            getName(), //name
            "string", //type
            null, //widget
            null, //widgetconfig
            null, //regex
            null, //ruletype
            null, //default
            NICE_NAME, //nicename
            null, //description
            null, //error
            null//preferfolder
        );
        return prop;
    }

    /**
     * @see org.opencms.configuration.preferences.CmsBuiltinPreference#getPropertyDefinition(org.opencms.file.CmsObject)
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition(CmsObject cms) {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        String options = getSiteSelectOptionsStatic(cms, locale);
        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            getName(), //name
            "string", //type
            "select_notnull", //widget
            options, //widgetconfig
            null, //regex
            null, //ruletype
            null, //default
            NICE_NAME, //nicename
            null, //description
            null, //error
            null//preferfolder
        );
        return prop;
    }

}
