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

package org.opencms.workplace.list;

import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

/**
 * Implementation of a list item selection action where to define name and the column with the value.<p>
 *
 * @since 6.7.2
 */
public class CmsListItemSelectionCustomAction extends CmsListItemSelectionAction {

    /** The attributes to set at the input field. */
    private String m_attributes;

    /** The name of the column where to find the value. */
    private String m_column;

    /** The name of the input field. */
    private String m_fieldName;

    /**
     * Default Constructor.<p>
     *
     * @param id the unique id
     * @param columnValue the name of the column used for the value
     */
    public CmsListItemSelectionCustomAction(String id, String columnValue) {

        this(id, columnValue, null);
    }

    /**
     * Default Constructor.<p>
     *
     * @param id the unique id
     * @param name the name of the input field
     * @param columnValue the name of the column used for the value
     */
    public CmsListItemSelectionCustomAction(String id, String name, String columnValue) {

        super(id, null);
        m_fieldName = name;
        m_column = columnValue;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(CmsWorkplace)
     */
    @Override
    public String buttonHtml(CmsWorkplace wp) {

        if (!isVisible()) {
            return "";
        }

        String value = getItem().getId();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_column)) {
            value = (String)getItem().get(m_column);
        }
        String html = "<input type='radio' value='" + value + "' name='" + m_fieldName + "'";
        if (!isEnabled()) {
            html += " disabled";
        }

        if (getItem().getId().equals(getSelectedItemId())) {
            html += " checked";
        }

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_attributes)) {
            html += m_attributes;
        }
        html += ">\n";
        return A_CmsHtmlIconButton.defaultButtonHtml(
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            getId(),
            html,
            getHelpText().key(wp.getLocale()),
            false,
            null,
            null,
            null);
    }

    /**
     * Returns the attributes.<p>
     *
     * @return the attributes
     */
    public String getAttributes() {

        return m_attributes;
    }

    /**
     * Returns the column.<p>
     *
     * @return the column
     */
    public String getColumn() {

        return m_column;
    }

    /**
     * Returns the fieldName.<p>
     *
     * @return the fieldName
     */
    public String getFieldName() {

        return m_fieldName;
    }

    /**
     * Sets the attributes.<p>
     *
     * @param attributes the attributes to set
     */
    public void setAttributes(String attributes) {

        m_attributes = attributes;
    }

    /**
     * Sets the column.<p>
     *
     * @param column the column to set
     */
    public void setColumn(String column) {

        m_column = column;
    }

    /**
     * Sets the fieldName.<p>
     *
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName) {

        m_fieldName = fieldName;
    }

}
