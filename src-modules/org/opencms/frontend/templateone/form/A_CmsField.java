/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/A_CmsField.java,v $
 * Date   : $Date: 2011/03/23 14:50:47 $
 * Version: $Revision: 1.17 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * @author Thomas Weckert
 * @author Jan Baudisch
 * @version $Revision: 1.17 $ 
 * @since 6.0.0 
 */
public abstract class A_CmsField implements I_CmsField {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFormHandler.class);

    private String m_errorMessage;
    private List m_items;
    private String m_label;
    private boolean m_mandatory;
    private String m_name;
    private int m_placeholder;
    private int m_position;
    private String m_validationExpression;
    private String m_value;

    /**
     * Default constructor.<p>
     */
    public A_CmsField() {

        super();

        m_items = new ArrayList();
        m_mandatory = false;
        m_value = "";
        m_validationExpression = "";
        m_placeholder = 0;
        m_position = 0;
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#getErrorMessage()
     */
    public String getErrorMessage() {

        return m_errorMessage;
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
     * Returns the placeholder.<p>
     *
     * @return the placeholder
     */
    public int getPlaceholder() {

        return m_placeholder;
    }

    /**
     * Returns the position.<p>
     *
     * @return the position
     */
    public int getPosition() {

        return m_position;
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

        return (CmsCheckboxField.class.isAssignableFrom(getClass())
            || CmsSelectionField.class.isAssignableFrom(getClass()) || CmsRadioButtonField.class.isAssignableFrom(getClass()));
    }

    /**
     * Sets the placeholder.<p>
     *
     * @param placeholder the placeholder to set
     */
    public void setPlaceholder(int placeholder) {

        m_placeholder = placeholder;
    }

    /**
     * Sets the position.<p>
     *
     * @param position the position to set
     */
    public void setPosition(int position) {

        m_position = position;
    }

    /**
     * Returns the field value as a String.<p>
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String result;
        if (needsItems()) {
            // check which item has been selected
            StringBuffer fieldValue = new StringBuffer(8);
            Iterator k = getItems().iterator();
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
            result = fieldValue.toString();
        } else {
            // for other field types, append value
            result = getValue();
        }

        return result;
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
     * This function sets the cells of placeholder. Its only work with 
     * a col size of 2.<p> 
     * 
     * @param message integer value of adding to the placeholder value
     */
    protected void incrementPlaceholder(String message) {

        int parse = 0;
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(message) && !message.startsWith("?")) {
            parse = Integer.parseInt(message.trim());
        }
        m_placeholder += parse;
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
     * This functions looks if the row should be end. By one colsize, its 
     * everytime ending. By two colsize every second cell its ending.
     * 
     * @param colSizeTwo if two cols should be shown
     * @return true the row end must shown
     */
    protected boolean showRowEnd(String colSizeTwo) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(colSizeTwo) || !colSizeTwo.trim().equalsIgnoreCase("true")) {
            return true;
        }

        boolean result = false;
        if (m_position != 0) {
            result = true;
        }
        if (m_position == 0) {
            m_position = 1;
        } else {
            m_position = 0;
        }
        //if its need a placeholder
        if ((m_position == 1) && (m_placeholder >= 1)) {
            result = true;
            m_position = 0;
            m_placeholder--;
        }
        return result;
    }

    /**
     * This functions looks if the row should be start. By one colsize, its 
     * everytime starting. By two colsize every second cell its starting.
     * 
     * @param colSizeTwo if two cols should be shown
     * @return true if the row should shown
     */
    protected boolean showRowStart(String colSizeTwo) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(colSizeTwo) || !colSizeTwo.trim().equalsIgnoreCase("true")) {
            return true;
        }
        if (m_position == 0) {
            return true;
        }
        return false;
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
        if (CmsStringUtil.isNotEmpty(m_value) && (!"".equals(m_validationExpression))) {

            Pattern pattern = null;
            try {

                pattern = Pattern.compile(m_validationExpression);
                if (!pattern.matcher(m_value).matches()) {
                    return CmsFormHandler.ERROR_VALIDATION;
                }
            } catch (PatternSyntaxException e) {

                // syntax error in regular expression, log to opencms.log
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_PATTERN_SYNTAX_0), e);
                }
            }
        }

        return null;
    }
}