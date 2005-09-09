/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/A_CmsField.java,v $
 * Date   : $Date: 2005/09/09 10:31:59 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;

/**
 * Abstract base class for all input fields.<p>
 * 
 * @author Andreas Zahner 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.3 $ 
 * @since 6.0.0 
 */
public abstract class A_CmsField implements I_CmsField {
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFormHandler.class);

    private List m_items;
    private String m_label;
    private boolean m_mandatory;
    private String m_name;
    private String m_validationExpression;
    private String m_value;
    private String m_errorMessage;

    /**
     * Default constructor.<p>
     */
    public A_CmsField() {

        super();
        
        m_items = new ArrayList();
        m_mandatory = false;
        m_value = "";
        m_validationExpression = "";
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        
        try {
            
            if (m_items != null) {
                m_items.clear();
            }
            
            m_label = null;
            m_name = null;
            m_validationExpression = null;
            m_value = null;
        } catch (Throwable t) {
            // ignore
        }
        
        super.finalize();
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#getItems()
     */
    public List getItems() {

        return m_items;
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#getLabel()
     */
    public String getLabel() {

        return m_label;
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#getValidationExpression()
     */
    public String getValidationExpression() {

        return m_validationExpression;
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#getValue()
     */
    public String getValue() {

        return m_value;
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#isMandatory()
     */
    public boolean isMandatory() {

        return m_mandatory;
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#needsItems()
     */
    public boolean needsItems() {

        return (CmsCheckboxField.class.isAssignableFrom(getClass()) || CmsSelectionField.class.isAssignableFrom(getClass()) || CmsRadioButtonField.class.isAssignableFrom(getClass()));
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
    protected void setItems(List items) {

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
    
    /**
     * Validates the constraints if this field is mandatory.<p>
     * 
     * @return {@link CmsFormHandler#ERROR_MANDATORY} if a constraint is violated
     */
    protected String validateConstraints() {
        
        if (isMandatory()) {
            
            // check if the field has a value
            if (needsItems()) {
                
                // check if at least one item has been selected
                Iterator k = m_items.iterator();
                boolean isSelected = false;
                while (k.hasNext()) {
                    
                    CmsFieldItem currentItem = (CmsFieldItem)k.next();
                    if (currentItem.isSelected()) {
                        isSelected = true;
                        continue;
                    }
                }
                
                if (!isSelected) {
                    // no item has been selected, create an error message
                    return CmsFormHandler.ERROR_MANDATORY;
                }
            } else {
                
                // check if the field has been filled out
                if (CmsStringUtil.isEmpty(m_value)) {
                    return CmsFormHandler.ERROR_MANDATORY;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Validates the input value of this field.<p>
     * 
     * @return {@link CmsFormHandler#ERROR_VALIDATION} if validation of the input value failed
     */
    protected String validateValue() {
        
        // validate non-empty values with given regular expression
        if (CmsStringUtil.isNotEmpty(m_value) /*&& !needsItems()*/ && !"".equals(m_validationExpression)) {
            
            Pattern pattern = null;
            try {
                
                pattern = Pattern.compile(m_validationExpression);
                if (!pattern.matcher(m_value).matches()) {
                    return CmsFormHandler.ERROR_VALIDATION;
                }
            } catch (PatternSyntaxException e) {
                
                // syntax error in regular expression, log to opencms.log
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().key(Messages.LOG_ERR_PATTERN_SYNTAX_0), e);
                }
            }
        }
        
        return null;
    }
    
    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#validate(CmsFormHandler)
     */
    public String validate(CmsFormHandler formHandler) {
        
        // validate the constraints
        String validationError = validateConstraints();
        if (CmsStringUtil.isEmpty(validationError)) {
            
            // no constraint error- validate the input value
            validationError = validateValue();
        }
        
        return validationError;
    }
    
    /**
     * Sets the error message if validation failed.<p>
     * 
     * @param errorMessage the error message if validation failed
     */
    protected void setErrorMessage(String errorMessage) {
        m_errorMessage = errorMessage;
    }
    
    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#getErrorMessage()
     */
    public String getErrorMessage() {
        return m_errorMessage;
    }

}
