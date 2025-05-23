/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.dialogs.history.diff;

import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.main.CmsException;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.workplace.comparison.CmsAttributeComparison;
import org.opencms.workplace.comparison.CmsResourceComparison;

import java.util.List;

/**
 * Displays differences between the sets of properties of two versions of a resource.<p>
 */
public class CmsPropertyDiff extends A_CmsAttributeDiff {

    /**
     * @see org.opencms.ui.dialogs.history.diff.A_CmsAttributeDiff#getCaption()
     */
    @Override
    public String getCaption() {

        return CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_PROPERTIES_CAPTION_0);
    }

    /**
     * @see org.opencms.ui.dialogs.history.diff.A_CmsAttributeDiff#getDifferences(org.opencms.file.CmsObject, org.opencms.gwt.shared.CmsHistoryResourceBean, org.opencms.gwt.shared.CmsHistoryResourceBean)
     */
    @Override
    public List<CmsAttributeComparison> getDifferences(
        CmsObject cms,
        CmsHistoryResourceBean v1,
        CmsHistoryResourceBean v2) throws CmsException {

        return filterDifferent(
            CmsResourceComparison.compareProperties(
                cms,
                readResource(cms, v1),
                getVersionString(v1),
                readResource(cms, v2),
                getVersionString(v2)));
    }

}
