/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsFieldValue.java,v $
 * Date   : $Date: 2005/06/23 09:05:01 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.frontend.templateone.form;

import java.util.Iterator;

/**
 * Represents a single input field value of a submitted form.<p>
 * 
 * This object is needed to create the output for the optional confirmation page, the notification email
 * or the final page after submission.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public class CmsFieldValue {

    private String m_label;
    private boolean m_show;
    private String m_value;

    /**
     * Constructor that creates an initialized field item.<p>
     * 
     * @param field the form field to create the value from 
     */
    public CmsFieldValue(CmsField field) {

        if (field.needsItems()) {
            // check which item has been selected
            StringBuffer fieldValue = new StringBuffer(8);
            Iterator k = field.getItems().iterator();
            boolean isSelected = false;
            while (k.hasNext()) {
                CmsFieldItem currentItem = (CmsFieldItem)k.next();
                if (currentItem.isSelected()) {
                    if (isSelected) {
                        fieldValue.append(", ");
                    }
                    fieldValue.append(currentItem.getLabel());
                    isSelected = true;
                }
            }
            m_value = fieldValue.toString();
        } else {
            // for other field types, append value
            m_value = field.getValue();
        }

        if (CmsField.C_TYPE_HIDDEN.equals(field.getType())) {
            // for hidden fields, set show field flag to false
            m_show = false;
        } else {
            // all other fields are shown           
            m_show = true;
        }

        // set the label String of current field
        m_label = field.getLabel();
    }

    /**
     * Returns the label text of the field item.<p>
     * 
     * @return the label text of the field item
     */
    public String getLabel() {

        return m_label;
    }

    /**
     * Returns the value of the field item.<p>
     * 
     * @return the value of the field item
     */
    public String getValue() {

        return m_value;
    }

    /**
     * Returns if the current item is shown or not.<p>
     * 
     * @return true if the current item is shown, otherwise false
     */
    public boolean isShow() {

        return m_show;
    }

    /**
     * Sets the label text of the field item.<p>
     * 
     * @param label the label text of the field item
     */
    protected void setLabel(String label) {

        m_label = label;
    }

    /**
     * Sets if the current item is shown or not.<p>
     * 
     * @param show true if the current item is shown, otherwise false
     */
    protected void setShow(boolean show) {

        m_show = show;
    }

    /**
     * Sets the value of the field item.<p>
     * 
     * @param value the value of the field item
     */
    protected void setValue(String value) {

        m_value = value;
    }
}
