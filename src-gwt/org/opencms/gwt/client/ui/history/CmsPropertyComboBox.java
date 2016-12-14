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

package org.opencms.gwt.client.ui.history;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.input.CmsComboBox;
import org.opencms.gwt.client.ui.input.CmsLabelSelectCell;
import org.opencms.util.CmsStringUtil;

import java.util.Map;

/**
 * A combo box class for the property dialog.<p>
 */
public class CmsPropertyComboBox extends CmsComboBox {

    /**
     * Creates a new instance.<p>
     *
     * @param options the widget options
     */
    public CmsPropertyComboBox(Map<String, String> options) {
        super(options);
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // do nothing, we just have to implement this method because the superclass inherits the I_CmsHasInit interface
    }

    /**
     * @see org.opencms.gwt.client.ui.input.CmsSelectBox#updateCell(org.opencms.gwt.client.ui.input.CmsLabelSelectCell)
     */
    @Override
    public void updateCell(CmsLabelSelectCell cell) {

        String value = cell.getValue();
        if ("".equals(value)) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_ghostValue)) {
                String unselected = Messages.get().key(Messages.GUI_SELECTBOX_UNSELECTED_0);
                cell.setText(unselected);
                cell.setOpenerText(unselected);
            } else {
                CmsLabelSelectCell ghostCell = m_selectCells.get(m_ghostValue);
                String ghostValueMessage = m_ghostValue;
                if (ghostCell != null) {
                    ghostValueMessage = ghostCell.getText();
                }
                String inheritMsg = Messages.get().key(Messages.GUI_SELECTBOX_INHERIT_1, ghostValueMessage);
                cell.setText(inheritMsg);
                cell.setOpenerText(ghostValueMessage);
            }
        }
    }

}
