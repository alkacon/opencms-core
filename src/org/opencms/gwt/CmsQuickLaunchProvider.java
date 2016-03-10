/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt;

import static org.opencms.gwt.shared.CmsGwtConstants.QuickLaunch.Q_ACCOUNTMANAGER;
import static org.opencms.gwt.shared.CmsGwtConstants.QuickLaunch.Q_EXPLORER;
import static org.opencms.gwt.shared.CmsGwtConstants.QuickLaunch.Q_LAUNCHPAD;
import static org.opencms.gwt.shared.CmsGwtConstants.QuickLaunch.Q_PAGEEDITOR;
import static org.opencms.gwt.shared.CmsGwtConstants.QuickLaunch.Q_SITEMAP;
import static org.opencms.gwt.shared.CmsGwtConstants.QuickLaunch.Q_WORKPLACETOOLS;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsQuickLaunchData;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Provides the data for the buttons in the quick launch menu.<p>
 */
public class CmsQuickLaunchProvider {

    /** Current CMS context. */
    private CmsObject m_cms;

    /** The list of all possible (but not necessarily available to the current user) quick launch items. */
    private List<CmsQuickLaunchData> m_quickLaunchData = Lists.newArrayList();

    /**
     * Creates a new instance.<p>
     *
     * @param cms the current CMS context
     */
    public CmsQuickLaunchProvider(CmsObject cms) {

        m_cms = cms;
        m_quickLaunchData.add(
            new CmsQuickLaunchData(
                Q_LAUNCHPAD,
                OpenCms.getSystemInfo().getWorkplaceContext() + "#!launchpad",
                CmsGwtConstants.WIN_WORKPLACE,
                message(Messages.GUI_LAUNCHPAD_TITLE_0),
                OpenCmsTheme.getImageLink("apps/launchpad.png"),
                false));
        add(Q_PAGEEDITOR, message(Messages.GUI_PAGEEDITOR_TITLE_0), OpenCmsTheme.getImageLink("apps/editor.png"));
        add(Q_SITEMAP, message(Messages.GUI_SITEMAP_TITLE_0), OpenCmsTheme.getImageLink("apps/sitemap.png"));
        add(Q_EXPLORER, message(Messages.GUI_EXPLORER_TITLE_0), OpenCmsTheme.getImageLink("apps/explorer.png"));
        String accountUrl = OpenCms.getSystemInfo().getWorkplaceContext() + "#!/accounts";
        CmsWorkplaceMessages wpMessages = OpenCms.getWorkplaceManager().getMessages(
            OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms));

        m_quickLaunchData.add(
            new CmsQuickLaunchData(
                Q_ACCOUNTMANAGER,
                accountUrl,
                CmsGwtConstants.WIN_WORKPLACE,
                wpMessages.key("GUI_ACCOUNTS_ADMIN_TOOL_NAME_0"),
                CmsWorkplace.getResourceUri("tools/accounts/icons/big/accounts.png"),
                true));
        String wpToolsUrl = OpenCms.getSystemInfo().getWorkplaceContext() + "#!/workplace";
        m_quickLaunchData.add(
            new CmsQuickLaunchData(
                Q_WORKPLACETOOLS,
                wpToolsUrl,
                CmsGwtConstants.WIN_WORKPLACE,
                wpMessages.key("GUI_WORKPLACE_TOOL_NAME_0"),
                CmsWorkplace.getResourceUri("tools/workplace/icons/big/workplace.png"),
                true));

    }

    /**
     * Gets the quick launch data for the current user and context.<p>
     *
     * The context is a string which identifies where the quick launch menu is located
     *
     * @param context the context string
     *
     * @return the list of available quick launch items
     */
    public List<CmsQuickLaunchData> getQuickLaunchData(String context) {

        List<CmsQuickLaunchData> result = Lists.newArrayList();
        for (CmsQuickLaunchData item : m_quickLaunchData) {
            if (checkAvailable(context, item.getName())) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Adds a simple quick launch bean.<p>
     *
     * @param name the name
     * @param title the user-readable title
     * @param icon the icon
     */
    private void add(String name, String title, String icon) {

        m_quickLaunchData.add(new CmsQuickLaunchData(name, null, null, title, icon, false));
    }

    /**
     * Checks if the quick launch item should be displayed in the current context and for the current user.<p>
     *
     * @param context the context string
     * @param name the name of the quick launch item
     *
     * @return true if the quick launch item should be displayed
     */
    private boolean checkAvailable(String context, String name) {

        CmsUserSettings settings = new CmsUserSettings(m_cms);
        if (Q_EXPLORER.equals(name)) {
            return OpenCms.getRoleManager().hasRole(m_cms, CmsRole.WORKPLACE_USER);
        } else if (Q_LAUNCHPAD.equals(name)) {
            return OpenCms.getRoleManager().hasRole(m_cms, CmsRole.WORKPLACE_USER) && settings.usesNewWorkplace();
        } else if (Q_ACCOUNTMANAGER.equals(name)) {
            return OpenCms.getRoleManager().hasRole(m_cms, CmsRole.ACCOUNT_MANAGER) && settings.usesNewWorkplace();
        } else if (Q_SITEMAP.equals(name)) {
            return OpenCms.getRoleManager().hasRole(m_cms, CmsRole.EDITOR);
        } else if (Q_WORKPLACETOOLS.equals(name)) {
            return OpenCms.getRoleManager().hasRole(m_cms, CmsRole.WORKPLACE_MANAGER) && settings.usesNewWorkplace();
        }
        return true;
    }

    /**
     * Gets a message from the message bundle.<p>
     *
     * @param key the message key
     *
     * @return the message
     */
    private String message(String key) {

        return Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms)).key(key);
    }

}
