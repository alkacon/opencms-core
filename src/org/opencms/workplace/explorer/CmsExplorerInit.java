/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorerInit.java,v $
 * Date   : $Date: 2011/03/23 14:52:19 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
 * @author  Andreas Zahner
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.2.0 
 */
public class CmsExplorerInit extends CmsWorkplace {

    /** Stores already generated javascript menu outputs with a Locale object as key. */
    private HashMap m_generatedScripts;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsExplorerInit(CmsJspActionElement jsp) {

        super(jsp);
        m_generatedScripts = new HashMap();
    }

    /**
     * Builds the Javascript for the Workplace context menus.<p>
     * 
     * @return the Javascript for the Workplace context menus
     */
    public String buildContextMenues() {

        // try to get the stored entries from the Map
        String entries = (String)m_generatedScripts.get(getMessages().getLocale());

        if (entries == null) {
            StringBuffer result = new StringBuffer();
            // get all available resource types
            List allResTypes = OpenCms.getResourceManager().getResourceTypesWithUnknown();
            for (int i = 0; i < allResTypes.size(); i++) {
                // loop through all types
                I_CmsResourceType type = (I_CmsResourceType)allResTypes.get(i);
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