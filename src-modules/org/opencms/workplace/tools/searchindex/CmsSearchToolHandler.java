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

package org.opencms.workplace.tools.searchindex;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchManager;
import org.opencms.security.CmsRole;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsToolHandler;

/**
 * Search management tool handler that hides the tools if the current user
 * has not the needed privileges.<p>
 *
 * @since 6.0.0
 */
public class CmsSearchToolHandler extends A_CmsToolHandler {

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    public boolean isEnabled(CmsObject cms) {

        return OpenCms.getRoleManager().hasRole(cms, CmsRole.WORKPLACE_MANAGER);
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isEnabled(org.opencms.workplace.CmsWorkplace)
     */
    @Override
    public boolean isEnabled(CmsWorkplace wp) {

        if (getPath().startsWith("/searchindex/singleindex/search")
            && (getParameters(wp).get(A_CmsEditSearchIndexDialog.PARAM_INDEXNAME) != null)
            && (getParameters(wp).get(A_CmsEditSearchIndexDialog.PARAM_INDEXNAME).length > 0)) {
            String indexName = getParameters(wp).get(A_CmsEditSearchIndexDialog.PARAM_INDEXNAME)[0];
            if (!CmsSearchManager.isLuceneIndex(indexName)) {
                return false;
            }
        }
        return isEnabled(wp.getCms());
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isVisible(org.opencms.workplace.CmsWorkplace)
     */
    @Override
    public boolean isVisible(CmsWorkplace wp) {

        return isEnabled(wp);
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isVisible(org.opencms.file.CmsObject)
     */
    public boolean isVisible(CmsObject cms) {

        return OpenCms.getRoleManager().hasRole(cms, CmsRole.WORKPLACE_MANAGER);
    }
}
