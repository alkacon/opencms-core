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

package org.opencms.gwt.client.ui.input;

import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.Grid;

/**
 * Implements a select cell that can contain multiple entries.
 * And its value is a list of all selected entry values.
 * The {@link #getValue()} returns the parsed String
 * representation of the selected values, using a pipe
 * '|' as separator.<p>
 *
 * @since 8.5.0
 */
public class CmsMultiSelectCell extends A_CmsSelectCell {

    /** The list of checkboxes. */
    private List<CmsCheckBox> m_checkboxes = new ArrayList<CmsCheckBox>();

    /** The Grid panel. */
    private Grid m_checkboxWrapper = new Grid();

    /** The select options of the multi check box. */
    private Map<String, String> m_items = new LinkedHashMap<String, String>();

    /** The value of the selection text. */
    private String m_openerText;

    /**
     * Creates a CmsMultiSelectCell.<p>
     *
     * @param optins the values witch should be shown
     */
    @SuppressWarnings("boxing")
    public CmsMultiSelectCell(Map<String, CmsPair<String, Boolean>> optins) {

        int count = optins.size();
        int i = 0, y = 0;
        Map<String, String> items = new LinkedHashMap<String, String>();
        m_checkboxWrapper = getGridLayout(count);
        int modolo = m_checkboxWrapper.getColumnCount();
        for (Map.Entry<String, CmsPair<String, Boolean>> entry : optins.entrySet()) {
            String value = entry.getKey();
            items.put(value, entry.getValue().getFirst());
            CmsCheckBox checkbox = new CmsCheckBox(value);
            // wrap the check boxes in FlowPanels to arrange them vertically
            m_checkboxWrapper.setWidget(y, i % modolo, checkbox);
            i++;
            if ((i % modolo) == 0) {
                y++;
            }
            checkbox.setChecked(Boolean.valueOf(entry.getValue().getSecond()));
            m_checkboxes.add(checkbox);
        }
        m_items = new LinkedHashMap<String, String>(items);
        initWidget(m_checkboxWrapper);
    }

    /**
     * Returns the selected CmsCheckBox.<p>
     *
     * @param i the value of the selected CmsCheckBox
     * @return CmsCheckBox Returns the selected CmsCheckBox
     */
    public CmsCheckBox get(int i) {

        return m_checkboxes.get(i);
    }

    /**
     * Returns all CmsSelectBoxes.<p>
     *
     * @return a list of CmsCheckBox
     */
    public List<CmsCheckBox> getCheckbox() {

        return m_checkboxes;
    }

    /**
     * Returns the opener text.<p>
     *
     * @return the opener text
     */
    public String getOpenerText() {

        return m_openerText;
    }

    /**
     * Returns the set of values of the selected checkboxes.<p>
     *
     * @return a set of strings
     */
    public Set<String> getSelected() {

        Set<String> result = new HashSet<String>();
        int i = 0;
        for (Map.Entry<String, String> entry : m_items.entrySet()) {
            String key = entry.getKey();
            CmsCheckBox checkBox = m_checkboxes.get(i);
            if (checkBox.isChecked()) {
                result.add(key);
            }
            i += 1;
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectCell#getValue()
     */
    @Override
    public String getValue() {

        List<String> selected = new ArrayList<String>(getSelected());
        return CmsStringUtil.listAsString(selected, "|");
    }

    /**
    * Sets the opener text.<p>
    *
    * @param openerText the new opener text
    */
    public void setOpenerText(String openerText) {

        if (openerText != null) {
            m_openerText = openerText;
        }
    }

    /**
     * Helper function to generate the grid layout.<p>
     * @param count
     *
     *@return the new grid
     * */
    private Grid getGridLayout(int count) {

        Grid grid = new Grid();
        int x, y, z, modolo = 0;
        x = count % 3;
        y = count % 5;
        z = count % 7;
        if ((z <= y) && (z <= x)) {
            modolo = 7;
        } else if ((y <= z) && (y <= x)) {
            modolo = 5;
        } else if ((x <= z) && (x <= y)) {
            modolo = 3;
        }

        if (count < modolo) {
            grid = new Grid(count, 1);
        } else if ((count % modolo) == 0) {
            grid = new Grid(count / modolo, modolo);
        } else {
            grid = new Grid((count / modolo) + 1, modolo);
        }
        return grid;
    }

}
