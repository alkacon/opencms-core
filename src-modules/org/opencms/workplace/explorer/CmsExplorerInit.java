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

package org.opencms.workplace.explorer;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides methods for building the top frame Javascript code for the Explorer view of the OpenCms Workplace.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/views/top_js.jsp
 * </ul>
 * <p>
 *
 * @since 6.2.0
 */
public class CmsExplorerInit extends CmsWorkplace {

    /** Stores already generated javascript menu outputs with a Locale object as key. */
    private HashMap<Locale, String> m_generatedScripts;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsExplorerInit(CmsJspActionElement jsp) {

        super(jsp);
        m_generatedScripts = new HashMap<Locale, String>();
    }

    /**
     * Builds the Javascript for the Workplace context menus.<p>
     *
     * @return the Javascript for the Workplace context menus
     */
    public String buildContextMenues() {

        // try to get the stored entries from the Map
        String entries = m_generatedScripts.get(getMessages().getLocale());

        if (entries == null) {
            StringBuffer result = new StringBuffer();
            // get all available resource types
            List<I_CmsResourceType> allResTypes = OpenCms.getResourceManager().getResourceTypesWithUnknown();
            for (int i = 0; i < allResTypes.size(); i++) {
                // loop through all types
                I_CmsResourceType type = allResTypes.get(i);
                // get explorer type settings for current resource type
                CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                    type.getTypeName());
                if (settings != null) {
                    // append the context menu of the current resource type
                    result.append(settings.getJSEntries(settings, type.getTypeId(), getMessages()));
                }
            }
            entries = result.toString();
            // store the generated entries
            m_generatedScripts.put(getMessages().getLocale(), entries);
        }
        return entries;
    }

    /**
     * Returns the file settings for the Workplace explorer view.<p>
     *
     * @return the file settings for the Workplace explorer view
     */
    public int getExplorerSettings() {

        CmsUserSettings settings = new CmsUserSettings(getCms());
        int value = settings.getExplorerSettings();
        return value;
    }

    /**
     * Returns the server name for initializing the explorer view.<p>
     *
     * @return the server name
     */
    public String getServerName() {

        return getJsp().getRequest().getServerName();
    }

    /**
     * Returns the server path for initializing the explorer view.<p>
     *
     * @return the server path
     */
    public String getServerPath() {

        return OpenCms.getStaticExportManager().getVfsPrefix();
    }

    /**
     * Returns the setting for the upload button for initializing the explorer view.<p>
     *
     * @return the setting for the upload button
     */
    public String getShowFileUploadButtons() {

        return OpenCms.getWorkplaceManager().getDefaultUserSettings().getShowFileUploadButtonString();
    }

    /**
     * Returns the name of the current user for initializing the explorer view.<p>
     *
     * @return the name of the user
     */
    public String getUserName() {

        return getSettings().getUser().getName();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // nothing to do for JS creation
    }
}