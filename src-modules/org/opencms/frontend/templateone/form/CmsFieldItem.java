/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsFieldItem.java,v $
 * Date   : $Date: 2005/01/18 13:07:41 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

/**
 * Represents a single input field item object.<p>
 * 
 * This object is needed to create checkboxes, radio buttons and selectboxes
 * and represents an item for these types.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsFieldItem {
    
    private boolean m_isSelected;
    private String m_label;
    private String m_value;
    
    /**
     * Empty constructor creates an empty field item.<p>
     */
    public CmsFieldItem() {
        
        m_label = "";
        m_isSelected = false;
        m_value = "";
    }
    
    /**
     * Constructor that creates an initialized field item.<p>
     * 
     * @param value the value of the field item
     * @param description the description of the field item
     * @param isSelected true if the current item is selected, otherwise false
     */
    public CmsFieldItem(String value, String description, boolean isSelected) {
        
        m_label = description;
        m_isSelected = isSelected;
        m_value = value;
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
     * Returns if the current item is selected or not.<p>
     * 
     * @return true if the current item is selected, otherwise false
     */
    public boolean isSelected() {

        return m_isSelected;
    }
    
    /**
     * Sets the label text of the field item.<p>
     * 
     * @param label the description text of the field item
     */
    protected void setLabel(String label) {

        m_label = label;
    }
    
    /**
     * Sets if the current item is selected or not.<p>
     * 
     * @param isSelected true if the current item is selected, otherwise false
     */
    protected void setSelected(boolean isSelected) {

        m_isSelected = isSelected;
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
