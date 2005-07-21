/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsFormHandler.java,v $
 * Date   : $Date: 2005/07/21 07:29:58 $
 * Version: $Revision: 1.15 $
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.mail.CmsSimpleMail;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * The form handler controls the html or mail output of a configured email form.<p>
 * 
 * Provides methods to determine the action that takes place and methods to create different
 * output formats of a submitted form.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.15 $ 
 * 
 * @since 6.0.0 
 */
public class CmsFormHandler extends CmsJspActionElement {

    /** Request parameter value for the form action parameter: correct the input. */
    public static final String ACTION_CONFIRMED = "confirmed";
    
    /** Request parameter value for the form action parameter: correct the input. */
    public static final String ACTION_CORRECT_INPUT = "correct";
    
    /** Request parameter value for the form action parameter: form submitted. */
    public static final String ACTION_SUBMIT = "submit";

    /** Form error: mandatory field not filled out. */
    public static final String ERROR_MANDATORY = "mandatory";
    
    /** Form error: validation error of input. */
    public static final String ERROR_VALIDATION = "validation";

    /** Request parameter name for the hidden form action parameter to determine the action. */
    public static final String PARAM_FORMACTION = "formaction";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFormHandler.class);

    /** Contains eventual validation errors. */
    private Map m_errors;

    /** Temporarily stores the submitted field values for output generation. */
    private List m_fieldValues;

    /** The form configuration object. */
    private CmsForm m_formConfiguration;

    /** Temporarily stores the fields as hidden fields in the String. */
    private String m_hiddenFields;

    /** Flag indicating if the form is displayed for the first time. */
    private boolean m_initial;

    /** The localized messages for the form handler. */
    private CmsMessages m_messages;

    /**
     * Constructor, creates the necessary form configuration objects.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     * @throws Exception if creating the form configuration objects fails
     */
    public CmsFormHandler(PageContext context, HttpServletRequest req, HttpServletResponse res)
    throws Exception {

        super(context, req, res);
        init(req, null);
    }

    /**
     * Constructor, creates the necessary form configuration objects using a given configuration file URI.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     * @param formConfigUri URI of the form configuration file, if not provided, current URI is used for configuration
     * @throws Exception if creating the form configuration objects fails
     */
    public CmsFormHandler(PageContext context, HttpServletRequest req, HttpServletResponse res, String formConfigUri)
    throws Exception {

        super(context, req, res);
        init(req, formConfigUri);
    }

    /**
     * Replaces line breaks with html &lt;br&gt;.<p>
     * 
     * @param value the value to substitute
     * @return the substituted value
     */
    public String convertToHtmlValue(String value) {

        return convertValue(value, "html");
    }

    /**
     * Replaces html &lt;br&gt; with line breaks.<p>
     * 
     * @param value the value to substitute
     * @return the substituted value
     */
    public String convertToPlainValue(String value) {

        return convertValue(value, "");
    }

    /**
     * Converts a given String value to the desired output format.<p>
     * 
     * The following output formats are possible:
     * <ul>
     * <li>"html" meaning that &lt;br&gt; tags are added</li>
     * <li>"plain"  or any other String value meaning that &lt;br&gt; tags are removed</li>
     * </ul>
     *  
     * @param value the String value to convert
     * @param outputType the type of the resulting output
     * @return the converted String in the desired output format
     */
    public String convertValue(String value, String outputType) {

        if ("html".equalsIgnoreCase(outputType)) {
            // output should be html, add line break tags and characters
            value = CmsStringUtil.substitute(value, "\n", "<br>");
        } else {
            // output should be plain, remove html line break tags and characters
            value = CmsStringUtil.substitute(value, "<br>", "\n");
        }
        return value;
    }

    /**
     * Returns the configured form field values as hidden input fields.<p>
     * 
     * @return the configured form field values as hidden input fields
     */
    public String createHiddenFields() {

        if (CmsStringUtil.isEmpty(m_hiddenFields)) {
            StringBuffer result = new StringBuffer(getFormConfiguration().getFields().size() * 8);
            // iterate the form fields
            Iterator i = getFormConfiguration().getFields().iterator();
            while (i.hasNext()) {
                CmsField currentField = (CmsField)i.next();
                if (CmsField.TYPE_CHECKBOX.equals(currentField.getType())) {
                    // special case: checkbox, can have more than one value
                    Iterator k = currentField.getItems().iterator();
                    while (k.hasNext()) {
                        CmsFieldItem item = (CmsFieldItem)k.next();
                        if (item.isSelected()) {
                            result.append("<input type=\"hidden\" name=\"");
                            result.append(currentField.getName());
                            result.append("\" value=\"");
                            result.append(CmsEncoder.escapeXml(item.getValue()));
                            result.append("\">\n");
                        }
                    }
                } else if (CmsStringUtil.isNotEmpty(currentField.getValue())) {
                    // all other fields are converted to a simple hidden field
                    result.append("<input type=\"hidden\" name=\"");
                    result.append(currentField.getName());
                    result.append("\" value=\"");
                    result.append(CmsEncoder.escapeXml(currentField.getValue()));
                    result.append("\">\n");
                }

            }
            // store the generated input fields for further usage to avoid unnecessary rebuilding
            m_hiddenFields = result.toString();
        }
        // return generated result list
        return m_hiddenFields;
    }

    /**
     * Creates a list of field values to create an output for email or confirmation pages.<p>
     * 
     * The list contains CmsFieldValue objects with the following information:
     * <ol>
     * <li>the label (or name) of the field</li>
     * <li>the submitted value of the field, for checkboxes a comma separated list</li>
     * <li>a flag if the field should be displayed, false for hidden fields</li>
     * </ol>
     * 
     * @return list of field values to create an output for email or confirmation pages
     */
    public List createValuesFromFields() {

        if (m_fieldValues == null) {
            // get the form field size
            int fieldSize = getFormConfiguration().getFields().size();
            if (getFormConfiguration().isConfirmationMailEnabled()
                && getFormConfiguration().isConfirmationMailOptional()) {
                // descrease field size to avoid displaying optional confirmation email checkbox
                fieldSize -= 1;
            }
            // create the empty result list
            List result = new ArrayList(fieldSize);

            // validate each form field
            for (int i = 0; i < fieldSize; i++) {
                CmsField currentField = (CmsField)getFormConfiguration().getFields().get(i);
                CmsFieldValue fieldValue = new CmsFieldValue(currentField);
                // add field field value object to list
                result.add(fieldValue);

            }
            // store the generated list for further usage to avoid unnecessary rebuilding
            m_fieldValues = result;
        }
        // return generated list of field values
        return m_fieldValues;
    }

    /**
     * Returns the errors found when validating the form.<p>
     * 
     * @return the errors found when validating the form
     */
    public Map getErrors() {

        return m_errors;
    }

    /**
     * Returns the form configuration.<p>
     * 
     * @return the form configuration
     */
    public CmsForm getFormConfiguration() {

        return m_formConfiguration;
    }

    /**
     * Returns the localized messages.<p>
     *
     * @return the localized messages
     */
    public CmsMessages getMessages() {

        return m_messages;
    }

    /**
     * Returns if the submitted values contain validation errors.<p>
     * 
     * @return true if the submitted values contain validation errors, otherwise false
     */
    public boolean hasValidationErrors() {

        return (!isInitial() && getErrors().size() > 0);
    }

    /**
     * Initializes the form handler and creates the necessary configuration objects.<p>
     * 
     * @param req the JSP request 
     * @param formConfigUri URI of the form configuration file, if not provided, current URI is used for configuration
     * @throws Exception if creating the form configuration objects fails
     */
    public void init(HttpServletRequest req, String formConfigUri) throws Exception {

        setErrors(new HashMap());
        m_fieldValues = null;
        setInitial(CmsStringUtil.isEmpty(req.getParameter(PARAM_FORMACTION)));
        // get the localized messages
        setMessages(new CmsMessages("/org/opencms/frontend/templateone/form/workplace", getRequestContext().getLocale()));
        // get the form configuration
        setFormConfiguration(new CmsForm(this, getMessages(), isInitial(), formConfigUri));
    }

    /**
     * Returns if the form is displayed for the first time.<p>
     * 
     * @return true if the form is displayed for the first time, otherwise false
     */
    public boolean isInitial() {

        return m_initial;
    }

    /**
     * Sends the confirmation mail with the form data to the specified email address.<p>
     * 
     * @throws Exception if sending the confirmation mail fails
     */
    public void sendConfirmationMail() throws Exception {

        // get the field which contains the confirmation email address
        CmsField mailField = (CmsField)getFormConfiguration().getFields().get(
            getFormConfiguration().getConfirmationMailField());
        String mailTo = mailField.getValue();

        // create the new confirmation mail message depending on the configured email type
        if (getFormConfiguration().getMailType().equals(CmsForm.MAILTYPE_HTML)) {
            // create a HTML email
            CmsHtmlMail theMail = new CmsHtmlMail();
            theMail.setCharset(getCmsObject().getRequestContext().getEncoding());
            if (CmsStringUtil.isNotEmpty(getFormConfiguration().getMailFrom())) {
                theMail.setFrom(getFormConfiguration().getMailFrom());
            }
            theMail.setTo(createInternetAddresses(mailTo));
            theMail.setSubject(getFormConfiguration().getMailSubjectPrefix()
                + getFormConfiguration().getConfirmationMailSubject());
            theMail.setHtmlMsg(createMailTextFromFields(true, true));
            theMail.setTextMsg(createMailTextFromFields(false, true));
            // send the mail
            theMail.send();
        } else {
            // create a plain text email
            CmsSimpleMail theMail = new CmsSimpleMail();
            theMail.setCharset(getCmsObject().getRequestContext().getEncoding());
            if (CmsStringUtil.isNotEmpty(getFormConfiguration().getMailFrom())) {
                theMail.setFrom(getFormConfiguration().getMailFrom());
            }
            theMail.setTo(createInternetAddresses(mailTo));
            theMail.setSubject(getFormConfiguration().getMailSubjectPrefix()
                + getFormConfiguration().getConfirmationMailSubject());
            theMail.setMsg(createMailTextFromFields(false, true));
            // send the mail
            theMail.send();
        }
    }

    /**
     * Sends the mail with the form data to the specified recipients.<p>
     * 
     * If configured, sends also a confirmation mail to the form submitter.<p>
     * 
     * @return true if the mail has been successfully sent, otherwise false
     */
    public boolean sendMail() {

        try {
            // send optional confirmation mail
            if (getFormConfiguration().isConfirmationMailEnabled()) {
                if (!getFormConfiguration().isConfirmationMailOptional()
                    || "true".equals(getRequest().getParameter(CmsForm.PARAM_SENDCONFIRMATION))) {
                    sendConfirmationMail();
                }
            }
            // create the new mail message depending on the configured email type
            if (getFormConfiguration().getMailType().equals(CmsForm.MAILTYPE_HTML)) {
                // create a HTML email
                CmsHtmlMail theMail = new CmsHtmlMail();
                theMail.setCharset(getCmsObject().getRequestContext().getEncoding());
                if (CmsStringUtil.isNotEmpty(getFormConfiguration().getMailFrom())) {
                    theMail.setFrom(getFormConfiguration().getMailFrom());
                }
                theMail.setTo(createInternetAddresses(getFormConfiguration().getMailTo()));
                theMail.setCc(createInternetAddresses(getFormConfiguration().getMailCC()));
                theMail.setBcc(createInternetAddresses(getFormConfiguration().getMailBCC()));
                theMail.setSubject(getFormConfiguration().getMailSubjectPrefix()
                    + getFormConfiguration().getMailSubject());
                theMail.setHtmlMsg(createMailTextFromFields(true, false));
                theMail.setTextMsg(createMailTextFromFields(false, false));
                // send the mail
                theMail.send();
            } else {
                // create a plain text email
                CmsSimpleMail theMail = new CmsSimpleMail();
                theMail.setCharset(getCmsObject().getRequestContext().getEncoding());
                if (CmsStringUtil.isNotEmpty(getFormConfiguration().getMailFrom())) {
                    theMail.setFrom(getFormConfiguration().getMailFrom());
                }
                theMail.setTo(createInternetAddresses(getFormConfiguration().getMailTo()));
                theMail.setCc(createInternetAddresses(getFormConfiguration().getMailCC()));
                theMail.setBcc(createInternetAddresses(getFormConfiguration().getMailBCC()));
                theMail.setSubject(getFormConfiguration().getMailSubjectPrefix()
                    + getFormConfiguration().getMailSubject());
                theMail.setMsg(createMailTextFromFields(false, false));
                // send the mail
                theMail.send();
            }
        } catch (Exception e) {
            // an error occured during mail creation
            if (LOG.isErrorEnabled()) {
                LOG.error(e);
            }
            m_errors.put("sendmail", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Returns if the optional check page should be displayed.<p>
     * 
     * @return true if the optional check page should be displayed, otherwise false
     */
    public boolean showCheck() {

        return getFormConfiguration().getShowCheck()
            && ACTION_SUBMIT.equals(getRequest().getParameter(PARAM_FORMACTION));
    }

    /**
     * Returns if the input form should be displayed.<p>
     * 
     * @return true if the input form should be displayed, otherwise false
     */
    public boolean showForm() {

        return isInitial()
            || !validate()
            || ACTION_CORRECT_INPUT.equals(getRequest().getParameter(PARAM_FORMACTION));
    }

    /**
     * Validation method that checks the given input fields.<p>
     * 
     * All errors are stored in the member m_errors Map, with the input field name as key
     * and the error message String as value.<p>
     * 
     * @return true if all neccessary fields can be validated, otherwise false
     */
    public boolean validate() {

        boolean allOk = true;
        // iterate the form fields
        Iterator i = getFormConfiguration().getFields().iterator();
        // validate each form field
        while (i.hasNext()) {
            CmsField currentField = (CmsField)i.next();
            if (currentField.isMandatory()) {
                // check if the field has a value
                if (currentField.needsItems()) {
                    // check if at least one item has been selected
                    Iterator k = currentField.getItems().iterator();
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
                        getErrors().put(currentField.getName(), ERROR_MANDATORY);
                        allOk = false;
                        continue;
                    }
                } else {
                    // check if the field has been filled out
                    if (CmsStringUtil.isEmpty(currentField.getValue())) {
                        getErrors().put(currentField.getName(), ERROR_MANDATORY);
                        allOk = false;
                        continue;
                    }
                }
            }
            // validate non-empty values with given regular expression
            if (CmsStringUtil.isNotEmpty(currentField.getValue())
                && !currentField.needsItems()
                && !"".equals(currentField.getValidationExpression())) {
                Pattern pattern = null;
                try {
                    pattern = Pattern.compile(currentField.getValidationExpression());
                    if (!pattern.matcher(currentField.getValue()).matches()) {
                        getErrors().put(currentField.getName(), ERROR_VALIDATION);
                        allOk = false;
                    }
                } catch (PatternSyntaxException e) {
                    // syntax error in regular expression, log to opencms.log
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().key(Messages.LOG_ERR_PATTERN_SYNTAX_0), e);
                    }
                }
            }
            // validate captcha fields
            if (CmsField.TYPE_CAPTCHA.equals(currentField.getType())) {
                if (!CmsCaptcha.validateCaptchaPhrase(this, currentField.getValue())) {
                    getErrors().put(currentField.getName(), ERROR_VALIDATION);
                    allOk = false;
                }
            }
        }
        return allOk;
    }

    /**
     * Creates a list of internet addresses (email) from a semicolon separated String.<p>
     * 
     * @param mailAddresses a semicolon separated String with email addresses
     * @return list of internet addresses (email)
     * @throws AddressException if an email address is not correct
     */
    protected List createInternetAddresses(String mailAddresses) throws AddressException {

        if (CmsStringUtil.isNotEmpty(mailAddresses)) {
            // at least one email address is present, generate list
            StringTokenizer T = new StringTokenizer(mailAddresses, ";");
            List addresses = new ArrayList(T.countTokens());
            while (T.hasMoreTokens()) {
                InternetAddress address = new InternetAddress(T.nextToken());
                addresses.add(address);
            }
            return addresses;
        } else {
            // no address given, return empty list
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Creates the output String of the submitted fields for email creation.<p>
     * 
     * @param isHtmlMail if true, the output is formatted as HTML, otherwise as plain text
     * @param isConfirmationMail if true, the text for the confirmation mail is created, otherwise the text for mail receiver
     * @return the output String of the submitted fields for email creation
     */
    protected String createMailTextFromFields(boolean isHtmlMail, boolean isConfirmationMail) {

        List resultList = createValuesFromFields();
        StringBuffer result = new StringBuffer(resultList.size() * 8);
        if (isHtmlMail) {
            // create html head with style definitions and body
            result.append("<html><head>\n");
            result.append("<style type=\"text/css\"><!--\n");
            String style = getMessages().key("form.email.style.body");
            if (CmsStringUtil.isNotEmpty(style)) {
                result.append("body,h1,p,td { ");
                result.append(style);
                result.append(" }\n");
            }
            style = getMessages().key("form.email.style.h1");
            if (CmsStringUtil.isNotEmpty(style)) {
                result.append("h1 { ");
                result.append(style);
                result.append(" }\n");
            }
            style = getMessages().key("form.email.style.p");
            if (CmsStringUtil.isNotEmpty(style)) {
                result.append("p { ");
                result.append(style);
                result.append(" }\n");
            }
            style = getMessages().key("form.email.style.fields");
            if (CmsStringUtil.isNotEmpty(style)) {
                result.append("table.fields { ");
                result.append(style);
                result.append(" }\n");
            }
            style = getMessages().key("form.email.style.fieldlabel");
            if (CmsStringUtil.isNotEmpty(style)) {
                result.append("td.fieldlabel { ");
                result.append(style);
                result.append(" }\n");
            }
            style = getMessages().key("form.email.style.fieldvalue");
            if (CmsStringUtil.isNotEmpty(style)) {
                result.append("td.fieldvalue { ");
                result.append(style);
                result.append(" }\n");
            }
            style = getMessages().key("form.email.style.misc");
            if (CmsStringUtil.isNotEmpty(style)) {
                result.append(getMessages().key("form.email.style.misc"));
            }
            result.append("//--></style>\n");
            result.append("</head><body>\n");
            if (isConfirmationMail) {
                // append the confirmation mail text
                result.append(getFormConfiguration().getConfirmationMailText());
            } else {
                // append the email text
                result.append(getFormConfiguration().getMailText());
            }
            result.append("<table border=\"0\" class=\"fields\">\n");
        } else {
            // generate simple text mail
            if (isConfirmationMail) {
                // append the confirmation mail text
                result.append(getFormConfiguration().getConfirmationMailTextPlain());
            } else {
                // append the email text
                result.append(getFormConfiguration().getMailTextPlain());
            }
            result.append("\n\n");
        }
        // generate output for submitted form fields
        Iterator i = resultList.iterator();
        while (i.hasNext()) {
            CmsFieldValue current = (CmsFieldValue)i.next();
            if (isHtmlMail) {
                // format output as HTML
                result.append("<tr><td class=\"fieldlabel\">");
                result.append(current.getLabel());
                result.append("</td><td class=\"fieldvalue\">");
                result.append(convertToHtmlValue(current.getValue()));
                result.append("</td></tr>\n");
            } else {
                // format output as plain text
                result.append(current.getLabel());
                result.append("\t");
                result.append(current.getValue());
                result.append("\n");
            }
        }
        if (isHtmlMail) {
            // create html table closing tag
            result.append("</table>\n");
            if (!isConfirmationMail && getFormConfiguration().hasConfigurationErrors()) {
                // write form configuration errors to html mail
                result.append("<h1>");
                result.append(getMessages().key("form.configuration.error.headline"));
                result.append("</h1>\n<p>");
                for (int k = 0; k < getFormConfiguration().getConfigurationErrors().size(); k++) {
                    result.append(getFormConfiguration().getConfigurationErrors().get(k));
                    result.append("<br>");
                }
                result.append("</p>\n");
            }
            // create body and html closing tags
            result.append("</body></html>");
        } else if (!isConfirmationMail && getFormConfiguration().hasConfigurationErrors()) {
            // write form configuration errors to text mail
            result.append("\n");
            result.append(getMessages().key("form.configuration.error.headline"));
            result.append("\n");
            for (int k = 0; k < getFormConfiguration().getConfigurationErrors().size(); k++) {
                result.append(getFormConfiguration().getConfigurationErrors().get(k));
                result.append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Sets the errors found when validating the form.<p>
     * 
     * @param errors the errors found when validating the form
     */
    protected void setErrors(Map errors) {

        m_errors = errors;
    }

    /**
     * Sets the form configuration.<p>
     * 
     * @param configuration the form configuration
     */
    protected void setFormConfiguration(CmsForm configuration) {

        m_formConfiguration = configuration;
    }

    /**
     * Sets if the form is displayed for the first time.<p>
     * @param initial true if the form is displayed for the first time, otherwise false
     */
    protected void setInitial(boolean initial) {

        m_initial = initial;
    }

    /**
     * Sets the localized messages.<p>
     *
     * @param messages the localized messages
     */
    protected void setMessages(CmsMessages messages) {

        m_messages = messages;
    }

}
