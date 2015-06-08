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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class CmsWorkplaceAppManager {

    private ServiceLoader<I_CmsWorkplaceAppConfiguration> m_appLoader;

    private List<I_CmsWorkplaceAppConfiguration> m_appConfigurations;

    public CmsWorkplaceAppManager() {

        m_appLoader = ServiceLoader.load(I_CmsWorkplaceAppConfiguration.class);
    }

    public List<I_CmsWorkplaceAppConfiguration> getWorkplaceApps() {

        if (m_appConfigurations == null) {
            List<I_CmsWorkplaceAppConfiguration> appConfigurations = new ArrayList<I_CmsWorkplaceAppConfiguration>();
            Iterator<I_CmsWorkplaceAppConfiguration> configs = m_appLoader.iterator();
            while (configs.hasNext()) {
                appConfigurations.add(configs.next());
            }
            m_appConfigurations = Collections.unmodifiableList(appConfigurations);
        }
        return m_appConfigurations;
    }

}
