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

package org.opencms.ui.dialogs.history.diff;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.util.table.Column;
import org.opencms.workplace.comparison.CmsAttributeComparison;

/**
 * Represents a row in an attribute comparison table.<p>
 */
public class CmsPropertyCompareBean {

    /** The attribute comparison. */
    private CmsAttributeComparison m_comp;

    /**
     * Creates a new instance.<p>
     *
     * @param comp an attribute comparison
     */
    public CmsPropertyCompareBean(CmsAttributeComparison comp) {
        m_comp = comp;
    }

    /**
     * Gets the attribute name.<p>
     *
     * @return the attribute name
     */
    @Column(header = Messages.GUI_HISTORY_DIALOG_COL_PROPERTY_0, order = 10)
    public String getProperty() {

        String result = m_comp.getName();
        if (result.startsWith("GUI_")) {
            result = CmsVaadinUtils.getMessageText(result);
        }
        return result;
    }

    /**
     * Gets the value for the first version.<p>
     *
     * @return the first version's value
     */
    @Column(header = "V1 (%(v1))", order = 20)
    public String getV1() {

        return m_comp.getVersion1();
    }

    /**
     * Gets the value for the second version.<p>
     *
     * @return the second version's value
     */
    @Column(header = "V2 (%(v2))", order = 30)
    public String getV2() {

        return m_comp.getVersion2();
    }

}
