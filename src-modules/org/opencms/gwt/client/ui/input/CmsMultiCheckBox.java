/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsMultiCheckBox.java,v $
 * Date   : $Date: 2010/03/08 16:47:06 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.gwt.client.util.CmsPair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;

/**
 * A form widget consisting of a group of checkboxes.<p>
 *  
 */
public class CmsMultiCheckBox extends Composite implements I_CmsFormWidget {

    private List<CmsPair<String, String>> m_items;
    private Grid m_table;

    /**
     * Constructs a new checkbox group from a list of string pairs.<p>
     * 
     * The first string of every pair is the value of the checkbox, the second string is the label.
     * 
     * @param items a list of pairs of strings. 
     */
    public CmsMultiCheckBox(List<CmsPair<String, String>> items) {

        super();
        m_items = items;
        m_table = new Grid(items.size(), 2);
        initWidget(m_table);
        int i = 0;
        for (CmsPair<String, String> pair : items) {
            String value = pair.getSecond();
            m_table.setWidget(i, 0, new CmsCheckBox());
            m_table.setText(i, 1, value);
            i += 1;
        }
    }

    /**
     * 
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return I_CmsFormWidget.FieldType.STRING_LIST;
    }

    /**
     * 
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        return new ArrayList<String>(getSelected());
    }

    /**
     * Returns the set of values of the selected checkboxes.<p>
     * 
     * @return a set of strings
     */
    public Set<String> getSelected() {

        Set<String> result = new HashSet<String>();
        int i = 0;
        for (CmsPair<String, String> pair : m_items) {
            String key = pair.getFirst();
            CmsCheckBox checkBox = (CmsCheckBox)m_table.getWidget(i, 0);
            if (checkBox.isChecked()) {
                result.add(key);
            }
            i += 1;
        }
        return result;
    }

    /**
     * 
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        for (int i = 0; i < m_table.getRowCount(); i++) {
            CmsCheckBox checkbox = getCheckBox(i);
            checkbox.setChecked(false);
        }
    }

    /**
     * 
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        for (int i = 0; i < m_table.getRowCount(); i++) {
            CmsCheckBox checkbox = getCheckBox(i);
            checkbox.setEnabled(enabled);
        }
    }

    /**
     * 
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        int lastIndex = m_table.getRowCount() - 1;
        CmsCheckBox checkBox = getCheckBox(lastIndex);
        checkBox.setErrorMessage(errorMessage);
    }

    /**
     * 
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValue(java.lang.Object)
     */
    public void setFormValue(Object value) {

        if (value instanceof List) {
            List<String> keys = (List<String>)value;
            Set<String> keySet = new HashSet<String>(keys);
            int i = 0;
            for (CmsPair<String, String> pair : m_items) {
                String key = pair.getFirst();
                CmsCheckBox checkbox = getCheckBox(i);
                checkbox.setChecked(keySet.contains(key));
                i += 1;
            }
        }
    }

    private CmsCheckBox getCheckBox(int row) {

        return (CmsCheckBox)m_table.getWidget(row, 0);
    }
}
