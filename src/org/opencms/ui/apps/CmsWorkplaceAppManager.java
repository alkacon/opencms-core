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

import org.opencms.main.OpenCms;
import org.opencms.workplace.tools.CmsTool;
import org.opencms.workplace.tools.CmsToolManager;
import org.opencms.workplace.tools.I_CmsToolHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CmsWorkplaceAppManager {

    public static class NavigationState {

        public static final String PARAM_SEPARATOR = "/";

        private String m_viewName = "";
        private String m_params = "";

        public NavigationState(String stateString) {

            if (stateString.startsWith("!")) {
                stateString = stateString.substring(1);
            }
            int separatorPos = stateString.indexOf(PARAM_SEPARATOR);
            if (separatorPos > 0) {
                m_viewName = stateString.substring(0, separatorPos);
                m_params = stateString.substring(separatorPos + 1);
            } else {
                m_viewName = stateString;
            }
            if (m_viewName.endsWith("/")) {
                m_viewName = m_viewName.substring(0, m_viewName.length() - 1);
            }
        }

        String getParams() {

            return m_params;
        }

        String getViewName() {

            return m_viewName;

        }
    }

    private ServiceLoader<I_CmsWorkplaceAppConfiguration> m_appLoader;

    private Map<String, I_CmsWorkplaceAppConfiguration> m_appsById = Maps.newHashMap();
    private List<CmsAppCategory> m_appCategories = Lists.newArrayList();

    public static List<I_CmsWorkplaceAppConfiguration> loadAppsUsingServiceLoader() {

        List<I_CmsWorkplaceAppConfiguration> appConfigurations = new ArrayList<I_CmsWorkplaceAppConfiguration>();
        Iterator<I_CmsWorkplaceAppConfiguration> configs = ServiceLoader.load(
            I_CmsWorkplaceAppConfiguration.class).iterator();
        while (configs.hasNext()) {
            appConfigurations.add(configs.next());
        }
        return appConfigurations;
    }

    public void addAppConfigurations(Collection<I_CmsWorkplaceAppConfiguration> appConfigs) {

        for (I_CmsWorkplaceAppConfiguration appConfig : appConfigs) {
            m_appsById.put(appConfig.getId(), appConfig);
        }
    }

    public I_CmsWorkplaceAppConfiguration getAppConfiguration(String viewName) {

        return m_appsById.get(viewName);
    }

    public List<CmsAppCategory> getCategories() {

        return m_appCategories;
    }

    public Collection<I_CmsWorkplaceAppConfiguration> getWorkplaceApps() {

        return m_appsById.values();
    }

    public void loadApps() {

        m_appsById.clear();
        if (m_appCategories == null) {
            m_appCategories = Lists.newArrayList();
        }
        m_appCategories.clear();
        CmsAppCategory c1 = new CmsAppCategory("Main", null, 0, 0, null);
        m_appCategories.addAll(Arrays.asList(c1, new CmsAppCategory("Legacy", null, 1, 0, null)));
        addAppConfigurations(loadDefaultApps());
        addAppConfigurations(loadAppsUsingServiceLoader());
        addAppConfigurations(loadLegacyApps());
    }

    protected Collection<I_CmsWorkplaceAppConfiguration> loadDefaultApps() {

        return Arrays.<I_CmsWorkplaceAppConfiguration> asList(
            new CmsSitemapEditorConfiguration(),
            new MyTestAppConfig(),
            new CmsPageEditorConfiguration(),
            new CmsFileExplorerConfiguration(),
            new CmsAppHierarchyConfiguration());
    }

    private Collection<I_CmsWorkplaceAppConfiguration> loadLegacyApps() {

        List<I_CmsWorkplaceAppConfiguration> configs = new ArrayList<I_CmsWorkplaceAppConfiguration>();

        List<CmsTool> tools = OpenCms.getWorkplaceManager().getToolManager().getToolHandlers();
        for (CmsTool tool : tools) {

            I_CmsToolHandler handler = tool.getHandler();
            String path = handler.getPath();
            // only collecting first path level tools
            if ((path.length() > 1) && (path.indexOf(CmsToolManager.TOOLPATH_SEPARATOR, 1) < 0)) {
                configs.add(new CmsLegacyAppConfiguration(handler));
            }
        }
        return configs;
    }

}
