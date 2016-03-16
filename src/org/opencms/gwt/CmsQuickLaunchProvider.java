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

import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsQuickLaunchData;
import org.opencms.main.OpenCms;
import org.opencms.ui.apps.I_CmsHasADEQuickLaunchData;
import org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * Provides the data for the buttons in the quick launch menu.<p>
 */
public class CmsQuickLaunchProvider {

    /** Current CMS context. */
    private CmsObject m_cms;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the current CMS context
     */
    public CmsQuickLaunchProvider(CmsObject cms) {

        m_cms = cms;
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
        for (I_CmsWorkplaceAppConfiguration config : OpenCms.getWorkplaceAppManager().getQuickLaunchConfigurations(
            m_cms)) {
            if (config instanceof I_CmsHasADEQuickLaunchData) {
                Optional<CmsQuickLaunchData> optItem = ((I_CmsHasADEQuickLaunchData)config).getADEQuickLaunchData(
                    m_cms,
                    context);
                if (optItem.isPresent()) {
                    result.add(optItem.get());
                }

            }
        }
        return result;
    }

}
