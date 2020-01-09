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

package org.opencms.ui.apps.filehistory;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.ui.Component;

/**
 * App for the history settings and clearing of history.<p>
 */
public class CmsFileHistoryApp extends A_CmsWorkplaceApp {

    /**Mode don't keep versions.*/
    static final String MODE_DISABLED = "disabled";

    /**Mode keep without versions.*/
    static final String MODE_WITHOUTVERSIONS = "withoutversions";

    /**Mode keep with versions.*/
    static final String MODE_WITHVERSIONS = "withversions";

    /**Version disabled option.*/
    static final int NUMBER_VERSIONS_DISABLED = -2;

    /**Version unlimited option.*/
    static final int NUMBER_VERSIONS_UNLIMITED = -1;

    /**Path to save invalid.*/
    static final String PATH_SETTINGS_INVALID = "settings_invalid";

    /**App icon path.*/
    public static final String ICON = "apps/filehistory/history.png";

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();
        crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_TOOL_NAME_0));
        return crumbs;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            m_rootLayout.setMainHeightFull(false);
            return new CmsFileHistoryPanel();
        }
        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }
}
