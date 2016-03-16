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

package org.opencms.ui.apps;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsQuickLaunchData;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.I_CmsToolHandler;

import java.util.Locale;

import com.google.common.base.Optional;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;

/**
 * Holding configuration for legacy admin tools.<p>
 */
public class CmsLegacyAppConfiguration implements I_CmsWorkplaceAppConfiguration, I_CmsHasADEQuickLaunchData {

    /** The tool handler. */
    private I_CmsToolHandler m_toolHandler;

    /**
     * Constructor.<p>
     *
     * @param toolHandler the tool handler
     */
    public CmsLegacyAppConfiguration(I_CmsToolHandler toolHandler) {

        m_toolHandler = toolHandler;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsHasADEQuickLaunchData#getADEQuickLaunchData(org.opencms.file.CmsObject, java.lang.String)
     */
    public Optional<CmsQuickLaunchData> getADEQuickLaunchData(CmsObject cms, String context) {

        CmsUserSettings settings = new CmsUserSettings(cms);
        if (!settings.usesNewWorkplace() || !getVisibility(cms).isActive()) {
            return Optional.absent();
        } else {
            String wpToolsUrl = OpenCms.getSystemInfo().getWorkplaceContext() + "#!" + m_toolHandler.getPath();
            return Optional.of(
                new CmsQuickLaunchData(
                    m_toolHandler.getPath(),
                    wpToolsUrl,
                    CmsGwtConstants.WIN_WORKPLACE,
                    getName(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)),
                    getImageLink(),
                    true));
        }
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getAppCategory()
     */
    public String getAppCategory() {

        return "Legacy";
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getAppInstance()
     */
    public I_CmsWorkplaceApp getAppInstance() {

        return new CmsLegacyApp(m_toolHandler);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getButtonStyle()
     */
    public String getButtonStyle() {

        return "";
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getHelpText(java.util.Locale)
     */
    public String getHelpText(Locale locale) {

        return CmsMacroResolver.newInstance().setMessages(
            OpenCms.getWorkplaceManager().getMessages(locale)).resolveMacros(m_toolHandler.getHelpText());
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getIcon()
     */
    public Resource getIcon() {

        return new ExternalResource(getImageLink());
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getId()
     */
    public String getId() {

        return m_toolHandler.getPath();
    }

    public String getImageLink() {

        return CmsWorkplace.getSkinUri() + m_toolHandler.getIconPath();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getName(java.util.Locale)
     */
    public String getName(Locale locale) {

        return CmsMacroResolver.newInstance().setMessages(
            OpenCms.getWorkplaceManager().getMessages(locale)).resolveMacros(m_toolHandler.getName());
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getOrder()
     */
    public int getOrder() {

        return 10 + Math.round(m_toolHandler.getPosition());
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getPriority()
     */
    public int getPriority() {

        return I_CmsWorkplaceAppConfiguration.DEFAULT_PRIORIY;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getVisibility(org.opencms.file.CmsObject)
     */
    public CmsAppVisibilityStatus getVisibility(CmsObject cms) {

        return new CmsAppVisibilityStatus(m_toolHandler.isVisible(cms), m_toolHandler.isEnabled(cms), "");
    }

}
