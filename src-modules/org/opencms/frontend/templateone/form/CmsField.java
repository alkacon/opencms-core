/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/Attic/CmsField.java,v $
 * Date   : $Date: 2005/02/17 12:45:43 $
 * Version: $Revision: 1.3 $
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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single input field configuration object.<p>
 * 
 * Provides the necessary information to create a form input field,
 * e.g. the field type, label, name, validation rule, etc.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsField {

    /** HTML field type: checkbox. */
    public static final String C_TYPE_CHECKBOX = "checkbox";
    /** HTML field type: email field. */
    public static final String C_TYPE_EMAIL = "email";
    /** HTML field type: hidden field. */
    public static final String C_TYPE_HIDDEN = "hidden";
    /** HTML field type: radio button. */
    public static final String C_TYPE_RADIO = "radio";
    /** HTML field type: selectbox. */
    public static final String C_TYPE_SELECT = "select";
    /** HTML field type: text input. */
    public static final String C_TYPE_TEXT = "text";
    /** HTML field type: textarea. */
    public static final String C_TYPE_TEXTAREA = "textarea";
    
    /** Regular expression to validate email addresses. */
    public static final String C_VALIDATION_EMAIL = "(\\w[-._\\w]*\\w@\\w[-._\\w]*\\w\\.\\w{2,4})";
    
    private List m_items;  
    private String m_label;
    private boolean m_mandatory;
    private String m_name;
    private String m_type;
    private String m_validationExpression;
    private String m_value;
    
    /**
     * Default constructor, creates an empty CmsField object.<p>
     */
    public CmsField() {
        
        m_items = new ArrayList();
        m_mandatory = false;
        m_value = "";
        m_validationExpression = "";
    }
    
    /**
     * Returns the list of items for select boxes, radio buttons and checkboxes.<p>
     * 
     * The list contains CmsFieldItem objects with the following information:
     * <ol>
     * <li>the value of the item</li>
     * <li>the description of the item</li>
     * <li>the selection of the item (true or false)</li>
     * </ol>
     * 
     * @return the list of items for select boxes, radio buttons and checkboxes
     */
    public List getItems() {
        
        return m_items;
    }
    
    /**
     * Returns the description text of the input field.<p>
     * 
     * @return the description text of the input field
     */
    public String getLabel() {
        
        return m_label;
    }
    
    /**
     * Returns the name of the input field.<p>
     * 
     * @return the name of the input field
     */
    public String getName() {
        
        return m_name;
    }
    
    /**
     * Returns the type of the input field, e.g. "text" or "select".<p>
     * 
     * @return the type of the input field
     */
    public String getType() {
        
        return m_type;
    }
    
    /**
     * Returns the regular expression that is used for validation of the field.<p>
     * 
     * @return the regular expression that is used for validation of the field
     */
    public String getValidationExpression() {
        
        return m_validationExpression;
    }
    
    /**
     * Returns the initial value of the field.<p>
     * 
     * @return the initial value of the field
     */
    public String getValue() {
        
        return m_value;
    }
    
    /**
     * Returns if this input field is mandatory.<p>
     * 
     * @return true if this input field is mandatory, otherwise false
     */
    public boolean isMandatory() {
        
        return m_mandatory;
    }
    
    /**
     * Checks if an item list is needed for this field.<p>
     * 
     * @return true if an item list is needed for this field, otherwise false
     */
    public boolean needsItems() {

        return (C_TYPE_CHECKBOX.equals(this.getType()) || C_TYPE_SELECT.equals(this.getType()) || C_TYPE_RADIO.equals(this.getType()));
    }
    
    /**
     * Sets the list of items for select boxes, radio buttons and checkboxes.<p>
     * 
     * The list contains CmsFieldItem objects with the following information:
     * <ol>
     * <li>the value of the item</li>
     * <li>the description of the item</li>
     * <li>the selection flag of the item (true or false)</li>
     * </ol>
     * 
     * @param items the list of items for select boxes, radio buttons and checkboxes
     */
    public void setItems(List items) {
        
        m_items = items;
    }
    
    /**
     * Sets the description text of the input field.<p>
     * 
     * @param description the description text of the input field
     */
    protected void setLabel(String description) {
        
        m_label = description;
    }
    
    /**
     * Sets if this input field is mandatory.<p>
     * 
     * @param mandatory true if this input field is mandatory, otherwise false
     */
    protected void setMandatory(boolean mandatory) {
        
        m_mandatory = mandatory;
    }
    
    /**
     * Sets the name of the input field.<p>
     * 
     * @param name the name of the input field
     */
    protected void setName(String name) {
        
        m_name = name;
    }
    
    /**
     * Sets the type of the input field, e.g. "text" or "select".<p>
     * 
     * @param type the type of the input field
     */
    protected void setType(String type) {
        
        m_type = type;
    }
    
    /**
     * Sets the regular expression that is used for validation of the field.<p>
     * 
     * @param expression the regular expression that is used for validation of the field
     */
    protected void setValidationExpression(String expression) {
        
        m_validationExpression = expression;
    }
    
    /**
     * Sets the initial value of the field.<p>
     * 
     * @param value the initial value of the field
     */
    protected void setValue(String value) {
        
        m_value = value;
    }
    
}
