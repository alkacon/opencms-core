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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Preference for the start site.<p>
 */
public class CmsStartViewPreference extends CmsBuiltinPreference {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsStartViewPreference.class);

    /** The nice name. */
    private static final String NICE_NAME = "%(key."
        + org.opencms.workplace.commons.Messages.GUI_PREF_STARTUP_VIEW_0
        + ")";

    /**
     * Creates a new instance.<p>
     *
     * @param name the preference name
     */
    public CmsStartViewPreference(String name) {

        super(name);
    }

    /**
     * Gets the select options for the view selector.<p>
     *
     * @param cms the CMS context
     * @param value the current value
     * @return the select options
     */
    public static SelectOptions getViewSelectOptions(CmsObject cms, String value) {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);

        List<String> options = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        int selectedIndex = 0;

        List<I_CmsWorkplaceAppConfiguration> apps = OpenCms.getWorkplaceAppManager().getDefaultQuickLaunchConfigurations();

        for (I_CmsWorkplaceAppConfiguration app : apps) {
            if (OpenCms.getRoleManager().hasRole(
                cms,
                cms.getRequestContext().getCurrentUser().getName(),
                app.getRequiredRole())) {
                values.add(app.getId());
                options.add(app.getName(locale));
            }
        }

        SelectOptions optionBean = new SelectOptions(options, values, selectedIndex);
        return optionBean;
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

        String options = getViewSelectOptions(cms, null).toClientSelectWidgetConfiguration();
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

    /**
     * @see org.opencms.configuration.preferences.A_CmsPreference#isDisabled(org.opencms.file.CmsObject)
     */
    @Override
    public boolean isDisabled(CmsObject cms) {

        return !OpenCms.getRoleManager().hasRole(cms, CmsRole.WORKPLACE_USER);
    }

}
