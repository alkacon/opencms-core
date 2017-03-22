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
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.server.ExternalResource;
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

    /**Path to delete. */
    static final String PATH_DELETE = "delete";

    /**Path to settings.*/
    static final String PATH_SETTINGS = "settings";

    /**Path to save invalid.*/
    static final String PATH_SETTINGS_INVALID = "settings_invalid";

    /**Path to save.*/
    static final String PATH_SETTINGS_SAVE = "settings_save";

    /**Icon for delete option.*/
    private static final String ICON_DELETE = "apps/filehistory/history_clear.png";

    /**App icon path.*/
    public static final String ICON = "apps/filehistory/history.png";

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        //Deeper path
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state) | state.startsWith(PATH_SETTINGS)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_TOOL_NAME_0));
            return crumbs;
        }

        crumbs.put(
            CmsFileHistoryConfiguration.APP_ID,
            CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_TOOL_NAME_0));
        if (state.startsWith(PATH_DELETE)) {

            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_DELETE_TOOL_NAME_0));
        }

        if (crumbs.size() > 1) {
            return crumbs;
        } else {
            return new LinkedHashMap<String, String>(); //size==1 & state was not empty -> state doesn't match to known path
        }
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state) | state.startsWith(PATH_SETTINGS)) {
            m_rootLayout.setMainHeightFull(false);
            return new CmsFileHistorySettings(this, state);
        }
        if (state.startsWith(PATH_DELETE)) {
            m_rootLayout.setMainHeightFull(false);
            return new CmsFileHistoryClear(this);
        }
        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        List<NavEntry> subNav = new ArrayList<NavEntry>();

        subNav.add(
            new NavEntry(
                CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_DELETE_TOOL_NAME_0),
                CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_DELETE_TOOL_NAME_HELP_0),
                new ExternalResource(OpenCmsTheme.getImageLink(ICON_DELETE)),
                PATH_DELETE));

        return subNav;
    }
}
