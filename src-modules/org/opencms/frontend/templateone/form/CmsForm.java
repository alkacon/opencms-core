/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsForm.java,v $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringMapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

/**
 * Represents an input form with all configured fields and options.<p>
 * 
 * Provides the necessary information to create an input form and confirmation outputs.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsForm {
    
    /** Mail type: html mail. */
    public static final String C_MAILTYPE_HTML = "html";
    /** Mail type: text mail. */
    public static final String C_MAILTYPE_TEXT = "text";
    
    /** Configuration node name for the Email node. */
    public static final String C_NODE_EMAIL = "Email";
    /** Configuration node name for the field value node. */
    public static final String C_NODE_FIELDDEFAULTVALUE = "FieldDefault";
    /** Configuration node name for the field item node. */
    public static final String C_NODE_FIELDITEM = "FieldItem";
    
    /** Configuration node name for the field description node. */
    public static final String C_NODE_FIELDLABEL = "FieldLabel"; 
    /** Configuration node name for the field mandatory node. */
    public static final String C_NODE_FIELDMANDATORY = "FieldMandatory";
    /** Configuration node name for the field type node. */
    public static final String C_NODE_FIELDTYPE = "FieldType";
    /** Configuration node name for the field validation node. */
    public static final String C_NODE_FIELDVALIDATION = "FieldValidation";
    
    /** Configuration node name for the form attributes node. */
    public static final String C_NODE_FORMATTRIBUTES = "FormAttributes";
    /** Configuration node name for the form check page text node. */
    public static final String C_NODE_FORMCHECKTEXT = "CheckText";
    /** Configuration node name for the form confirmation text node. */
    public static final String C_NODE_FORMCONFIRMATION = "FormConfirmation";
    /** Configuration node name for the form field attributes node. */
    public static final String C_NODE_FORMFIELDATTRIBUTES = "FormFieldAttributes";
    /** Configuration node name for the form text node. */
    public static final String C_NODE_FORMTEXT = "FormText";
    
    /** Configuration node name for the input field node. */
    public static final String C_NODE_INPUTFIELD = "InputField";
    
    /** Configuration node name for the item description node. */
    public static final String C_NODE_ITEMDESCRIPTION = "ItemDescription";
    /** Configuration node name for the item selected node. */
    public static final String C_NODE_ITEMSELECTED = "ItemSelected";
    /** Configuration node name for the item value node. */
    public static final String C_NODE_ITEMVALUE = "ItemValue";
    
    /** Configuration node name for the Email bcc recipient(s) node. */
    public static final String C_NODE_MAILBCC = "MailBCC";
    /** Configuration node name for the Email cc recipient(s) node. */
    public static final String C_NODE_MAILCC = "MailCC";
    /** Configuration node name for the Email sender address node. */
    public static final String C_NODE_MAILFROM = "MailFrom";
    /** Configuration node name for the Email subject node. */
    public static final String C_NODE_MAILSUBJECT = "MailSubject";
    /** Configuration node name for the Email text node. */
    public static final String C_NODE_MAILTEXT = "MailText";
    /** Configuration node name for the Email recipient(s) node. */
    public static final String C_NODE_MAILTO = "MailTo";
    /** Configuration node name for the Email type node. */
    public static final String C_NODE_MAILTYPE = "MailType";
    /** Configuration node name for the optional form configuration. */
    public static final String C_NODE_OPTIONALCONFIGURATION = "OptionalFormConfiguration";
    /** Configuration node name for the Show check page node. */
    public static final String C_NODE_SHOWCHECK = "ShowCheck";

    /** Stores the form input fields. */
    private List m_fields;
    
    private String m_formAttributes;
    private String m_formCheckText;
    private String m_formConfirmationText;
    private String m_formFieldAttributes;
    private String m_formText;
    
    private boolean m_hasMandatoryFields;
    
    private String m_mailBCC;
    private String m_mailCC;   
    private String m_mailFrom;
    private String m_mailSubject;
    private String m_mailText;
    private String m_mailTextPlain;
    private String m_mailTo;
    private String m_mailType;
    
    private boolean m_showCheck;
    
    /**
     * Default constructor which parses the configuration file.<p>
     * 
     * @param jsp the initialized CmsJspActionElement to access the OpenCms API
     * @param messages the localized messages
     * @param initial if true, field values are filled with values specified in the configuration file, otherwise from the request
     * @throws Exception if parsing the configuration fails
     */
    public CmsForm(CmsJspActionElement jsp, CmsMessages messages, boolean initial) throws Exception {    
        
        init(jsp, messages, initial);
    }
    
    /**
     * Returns a list of field objects for the online form.<p>
     * 
     * @return a list of field objects for the online form
     */
    public List getFields() {
        
        return m_fields;
    }
    
    /** 
     * Returns the global form attributes.<p>
     * 
     * @return the global form attributes
     */
    public String getFormAttributes() {

        return m_formAttributes;
    }
    
    /**
     * Returns the form check text.<p>
     * 
     * @return the form check text
     */
    public String getFormCheckText() {

        return m_formCheckText;
    }
    
    /**
     * Returns the form confirmation text.<p>
     * 
     * @return the form confirmation text
     */
    public String getFormConfirmationText() {

        return m_formConfirmationText;
    }
    
    /**
     * Returns the optional form input field attributes.<p>
     * 
     * @return the optional form input field attributes
     */
    public String getFormFieldAttributes() {

        return m_formFieldAttributes;
    }
    
    /**
     * Returns the form text.<p>
     * 
     * @return the form text
     */
    public String getFormText() {

        return m_formText;
    }
    
    /**
     * Returns the mail bcc recipient(s).<p>
     * 
     * @return the mail bcc recipient(s)
     */
    public String getMailBCC() {

        return m_mailBCC;
    }
    
    /**
     * Returns the mail cc recipient(s).<p>
     * 
     * @return the mail cc recipient(s)
     */
    public String getMailCC() {

        return m_mailCC;
    }
    
    /**
     * Returns the mail sender address.<p>
     * 
     * @return the mail sender address
     */
    public String getMailFrom() {

        return m_mailFrom;
    }
    
    /**
     * Returns the mail subject.<p>
     * 
     * @return the mail subject
     */
    public String getMailSubject() {

        return m_mailSubject;
    }
    
    /**
     * Returns the mail text.<p>
     * 
     * @return the mail text
     */
    public String getMailText() {

        return m_mailText;
    }
    
    /**
     * Returns the mail text as plain text.<p>
     * 
     * @return the mail text as plain text
     */
    public String getMailTextPlain() {

        return m_mailTextPlain;
    }
    
    /**
     * Returns the mail recipient(s).<p>
     * 
     * @return the mail recipient(s)
     */
    public String getMailTo() {

        return m_mailTo;
    }
    
    /**
     * Returns the mail type ("text" or "html").<p>
     * 
     * @return the mail type
     */
    public String getMailType() {

        return m_mailType;
    }
    
    /**
     * Returns if the check page should be shown.<p>
     *
     * @return true if the check page should be shown, otherwise false
     */
    public boolean getShowCheck() {

        return m_showCheck;
    }
    
    /**
     * Returns true if at least one of the configured fields is mandatory.<p>
     *
     * @return true if at least one of the configured fields is mandatory, otherwise false
     */
    public boolean hasMandatoryFields() {

        return m_hasMandatoryFields;
    }
    
    /**
     * Initializes the form configuration and creates the necessary form field objects.<p>
     * 
     * @param jsp the initialized CmsJspActionElement to access the OpenCms API
     * @param messages the localized messages
     * @param initial if true, field values are filled with values specified in the XML configuration
     * @throws Exception if parsing the configuration fails
     */
    public void init(CmsJspActionElement jsp, CmsMessages messages, boolean initial) throws Exception {
        
        // read the form configuration file from VFS
        CmsFile file = jsp.getCmsObject().readFile(jsp.getRequestContext().getUri());
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(jsp.getCmsObject(), file);
        
        // get current Locale
        Locale locale = jsp.getRequestContext().getLocale();
        
        // init member variables
        initMembers();
        
        // initialize general form configuration
        initFormGlobalConfiguration(content, jsp.getCmsObject(), locale, messages);
        
        // initialize the form input fields
        initInputFields(content, jsp, locale, initial);
    }
    
    
    /**
     * Marks the individual items of checkboxes, selectboxes and radiobuttons as selected depending on the given request parameters.<p>
     * 
     * @param request the current request
     * @param fieldType the type of the input field
     * @param fieldName the name of the input field
     * @param value the value of the input field
     * @return "true" if the current item is selected or checked, otherwise false
     */
    public String readSelectedFromRequest(HttpServletRequest request, String fieldType, String fieldName, String value) {
        
        String result = "";
        if (CmsField.C_TYPE_CHECKBOX.equals(fieldType)) {
            // this is a checkbox
            String[] values = request.getParameterValues(fieldName);
            if (values != null) {
                for (int i=0; i<values.length; i++) {
                    if (value.equals(values[i])) {
                        return "true";
                    }
                }
            }
        } else {
            // select box or radio button
            String fieldValue = request.getParameter(fieldName);
            if (CmsStringUtil.isNotEmpty(fieldValue) && fieldValue.equals(value) && !"".equals(value)) {
                // mark this as selected
                result = "true";
            } else {
                // do not mark it as selected
                result = "";
            }
        }
        return result;
    }
    
    /**
     * Sets the list of field objects for the online form.<p>
     * 
     * @param fields the list of field objects for the online form
     */
    protected void setFields(List fields) {
        
        m_fields = fields;
    }
    
    /**
     * Sets the global form attributes.<p>
     * 
     * @param formAttributes the global form attributes
     */
    protected void setFormAttributes(String formAttributes) {

        m_formAttributes = formAttributes;
    }
    
    /**
     * Sets the form check text.<p>
     * 
     * @param formCheckText the form confirmation text
     */
    protected void setFormCheckText(String formCheckText) {

        m_formCheckText = formCheckText;
    }
    
    /**
     * Sets the form confirmation text.<p>
     * 
     * @param formConfirmationText the form confirmation text
     */
    protected void setFormConfirmationText(String formConfirmationText) {

        m_formConfirmationText = formConfirmationText;
    }
    
    /**
     * Sets the optional form input field attributes.<p>
     * 
     * @param formFieldAttributes the optional form input field attributes
     */
    protected void setFormFieldAttributes(String formFieldAttributes) {

        m_formFieldAttributes = formFieldAttributes;
    }
    
    /**
     * Sets the form text.<p>
     * 
     * @param formText the form text
     */
    protected void setFormText(String formText) {

        m_formText = formText;
    }
    
    /**
     * Sets if at least one of the configured fields is mandatory.<p>
     *
     * @param hasMandatoryFields true if at least one of the configured fields is mandatory, otherwise false
     */
    protected void setHasMandatoryFields(boolean hasMandatoryFields) {

        m_hasMandatoryFields = hasMandatoryFields;
    }
    
    /**
     * Sets the mail bcc recipient(s).<p>
     * 
     * @param mailBCC the mail bcc recipient(s)
     */
    protected void setMailBCC(String mailBCC) {

        m_mailBCC = mailBCC;
    }
    
    /**
     * Sets the mail cc recipient(s).<p>
     * 
     * @param mailCC the mail cc recipient(s)
     */
    protected void setMailCC(String mailCC) {

        m_mailCC = mailCC;
    }
    
    /**
     * Sets the mail sender address.<p>
     * 
     * @param mailFrom the mail sender address
     */
    protected void setMailFrom(String mailFrom) {

        m_mailFrom = mailFrom;
    }
    
    /**
     * Sets the mail subject.<p>
     * 
     * @param mailSubject the mail subject
     */
    protected void setMailSubject(String mailSubject) {

        m_mailSubject = mailSubject;
    }
    
    /**
     * Sets the mail text.<p>
     * 
     * @param mailText the mail text
     */
    protected void setMailText(String mailText) {

        m_mailText = mailText;
    }
    
    /**
     * Sets the mail text as plain text.<p>
     * 
     * @param mailTextPlain the mail text as plain text
     */
    protected void setMailTextPlain(String mailTextPlain) {

        m_mailTextPlain = mailTextPlain;
    }
    
    /**
     * Sets the mail recipient(s).<p>
     * 
     * @param mailTo the mail recipient(s)
     */
    protected void setMailTo(String mailTo) {

        m_mailTo = mailTo;
    }
    
    /**
     * Sets the mail type ("text" or "html").<p>
     * 
     * @param mailType the mail type
     */
    protected void setMailType(String mailType) {

        m_mailType = mailType;
    }
    
    /**
     * Sets if the check page should be shown.<p>
     *
     * @param showCheck true if the check page should be shown, otherwise false
     */
    protected void setShowCheck(boolean showCheck) {

        m_showCheck = showCheck;
    }
    
    /**
     * Initializes the general online form settings.<p>
     * 
     * @param content the XML configuration content
     * @param cms the CmsObject to access the content values
     * @param locale the currently active Locale
     * @param messages the localized messages
     * @throws Exception if initializing the form settings fails
     */
    private void initFormGlobalConfiguration(CmsXmlContent content, CmsObject cms, Locale locale, CmsMessages messages) throws Exception {
        
        // get the form text
        String stringValue = content.getStringValue(cms, C_NODE_FORMTEXT, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setFormText(stringValue);
        }
        // get the form confirmation text
        stringValue = content.getStringValue(cms, C_NODE_FORMCONFIRMATION, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setFormConfirmationText(stringValue);
        }        
        // get the mail from address
        stringValue = content.getStringValue(cms, C_NODE_MAILFROM, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setMailFrom(stringValue);
        }
        // get the mail to address(es)
        stringValue = content.getStringValue(cms, C_NODE_MAILTO, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setMailTo(stringValue);
        }        
        // get the mail subject
        stringValue = content.getStringValue(cms, C_NODE_MAILSUBJECT, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setMailSubject(stringValue);
        }
        // get the mail text
        stringValue = content.getStringValue(cms, C_NODE_MAILTEXT, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setMailText(stringValue);
        }
        // set the mail text as plain text
        CmsXmlHtmlValue mailTextValue = (CmsXmlHtmlValue)content.getValue(C_NODE_MAILTEXT, locale);
        stringValue = mailTextValue.getPlainText(cms);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setMailTextPlain(stringValue);
        }
        
        // optional configuration options
        String pathPrefix = C_NODE_OPTIONALCONFIGURATION + "/";
        
        // get the mail type
        stringValue = content.getStringValue(cms, pathPrefix + C_NODE_MAILTYPE, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setMailType(stringValue);
        }
        // get the mail CC recipient(s)
        stringValue = content.getStringValue(cms, pathPrefix + C_NODE_MAILCC, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setMailCC(stringValue);
        }
        // get the mail BCC recipient(s)
        stringValue = content.getStringValue(cms, pathPrefix + C_NODE_MAILBCC, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setMailBCC(stringValue);
        }
        // get the form check page flag
        stringValue = content.getStringValue(cms, pathPrefix + C_NODE_SHOWCHECK, locale);
        setShowCheck(Boolean.valueOf(stringValue).booleanValue()); 
        // get the check page text
        stringValue = content.getStringValue(cms, pathPrefix + C_NODE_FORMCHECKTEXT, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setFormCheckText(stringValue);
        }
        // get the form attributes
        stringValue = content.getStringValue(cms, pathPrefix + C_NODE_FORMATTRIBUTES, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setFormAttributes(" " + stringValue);
        }
        // get the field attributes
        stringValue = content.getStringValue(cms, pathPrefix + C_NODE_FORMFIELDATTRIBUTES, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setFormFieldAttributes(" " + stringValue);
        } else {
            // no field attributes specified, check default field attributes
            String defaultAttributes = messages.key("form.field.default.attributes");
            if (CmsStringUtil.isNotEmpty(defaultAttributes)) {
                setFormFieldAttributes(" " + defaultAttributes);
            }
        }
    }
    
    /**
     * Initializes the field objects of the form.<p>
     * 
     * @param content the XML configuration content
     * @param jsp the initialized CmsJspActionElement to access the OpenCms API
     * @param locale the currently active Locale
     * @param initial if true, field values are filled with values specified in the XML configuration, otherwise values are read from the request
     * @throws Exception if parsing the configuration fails 
     */
    private void initInputFields(CmsXmlContent content, CmsJspActionElement jsp, Locale locale, boolean initial) throws Exception {
        
        CmsObject cms = jsp.getCmsObject();
        List fieldValues = content.getValues(C_NODE_INPUTFIELD, locale);
        List fields = new ArrayList(fieldValues.size());
        Iterator i = fieldValues.iterator();
        while (i.hasNext()) {
            I_CmsXmlContentValue inputField = (I_CmsXmlContentValue)i.next();
            String inputFieldPath = inputField.getPath() + "/";
            CmsField field = new CmsField();
            
            // get the field type
            String stringValue = content.getStringValue(cms, inputFieldPath + C_NODE_FIELDTYPE, locale);
            if (CmsStringUtil.isNotEmpty(stringValue)) {
                field.setType(stringValue);
            }
            // create the field name
            field.setName(inputFieldPath.substring(0, inputFieldPath.length() - 1));
            // get the field description
            stringValue = content.getStringValue(cms, inputFieldPath + C_NODE_FIELDLABEL, locale);
            if (CmsStringUtil.isNotEmpty(stringValue)) {
                field.setDescription(stringValue);
            }
            // get the field value
            if (initial) {
                // only fill in values from configuration file if called initially
                String fieldValue = content.getStringValue(cms, inputFieldPath + C_NODE_FIELDDEFAULTVALUE, locale);
                if (CmsStringUtil.isNotEmpty(fieldValue)) {
                    field.setValue(CmsStringUtil.substituteMacros(fieldValue, new CmsStringMapper(cms, jsp.getJspContext())));
                }               
            } else {
                // get field value from request for standard fields
                if (! field.getType().equals(CmsField.C_TYPE_CHECKBOX)) {
                    String fieldValue = jsp.getRequest().getParameter(field.getName());
                    if (fieldValue == null) {
                        // set empty String as value for non present request parameters
                        fieldValue = "";
                    }
                    field.setValue(fieldValue);                   
                }
            }
            
            // fill object members in case this is no hidden field
            if (! CmsField.C_TYPE_HIDDEN.equals(field.getType())) {
                // get the field validation regular expression
                stringValue = content.getStringValue(cms, inputFieldPath + C_NODE_FIELDVALIDATION, locale);
                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    field.setValidationExpression(stringValue);
                }
                // get the field mandatory flag
                stringValue = content.getStringValue(cms, inputFieldPath + C_NODE_FIELDMANDATORY, locale);
                boolean isMandatory = Boolean.valueOf(stringValue).booleanValue();
                field.setMandatory(isMandatory);
                if (isMandatory) {
                    // set flag that determines if mandatory fields are present
                    setHasMandatoryFields(isMandatory);    
                }
                
                if (field.needsItems()) {
                    // create items for checkboxes, radio buttons and selectboxes
                    String fieldValue = content.getStringValue(cms, inputFieldPath + C_NODE_FIELDDEFAULTVALUE, locale);
                    if (CmsStringUtil.isNotEmpty(fieldValue)) {
                        // get items from String                       
                        StringTokenizer T = new StringTokenizer(fieldValue, "|");
                        List items = new ArrayList(T.countTokens());
                        while (T.hasMoreTokens()) {
                            String part = T.nextToken();
                            // check preselection of current item
                            boolean isPreselected = part.indexOf('*') != -1;
                            String value = "";
                            String label = "";
                            String selected = "";
                            int delimPos = part.indexOf(':');
                            if (delimPos != -1) {
                                // a special label text is given
                                value = part.substring(0, delimPos);
                                label = part.substring(delimPos + 1);
                            } else {
                                // no special label text present, use complete String
                                value = part;
                                label = value;
                            }
                            
                            if (isPreselected) {
                                // remove preselected flag marker from Strings
                                value = CmsStringUtil.substitute(value, "*", "");
                                label = CmsStringUtil.substitute(label, "*", "");
                            }
                            
                            if (initial) {
                                // only fill in values from configuration file if called initially
                                if (isPreselected) {
                                    selected = "true";
                                }
                            } else {
                                // get selected flag from request for current item
                                selected = readSelectedFromRequest(jsp.getRequest(), field.getType(), field.getName(), value);
                            }
                            // add new item object
                            items.add(new CmsFieldItem(value, label, Boolean.valueOf(selected).booleanValue()));
                        }
                        field.setItems(items);                        
                    } else {
                        // no items specified for checkbox, radio button or selectbox
                        throw new Exception("No items specified for input field \"" + field.getName() + "\", type: " + field.getType());
                    }
                }
            }
            fields.add(field);
        }
        setFields(fields);
    }
    
    /**
     * Initializes the member variables.<p>
     */
    private void initMembers() {
        
        setFormAttributes("");
        setFormCheckText("");
        setFormConfirmationText("");
        setFormFieldAttributes("");
        setFormText("");
        setMailBCC("");
        setMailCC("");
        setMailFrom("");
        setMailSubject("");
        setMailText("");
        setMailTextPlain("");
        setMailTo("");
        setMailType(C_MAILTYPE_HTML);
    }
}
