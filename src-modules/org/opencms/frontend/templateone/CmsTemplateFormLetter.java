/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateFormLetter.java,v $
 * Date   : $Date: 2004/11/15 17:09:19 $
 * Version: $Revision: 1.3 $
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
package org.opencms.frontend.templateone;

import org.opencms.mail.CmsHtmlMail;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.I_CmsWpConstants;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;


/**
 * Provides methods to build the page "letter to the editor" form.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsTemplateFormLetter extends CmsTemplateForm {
    
    /** Stores the type of concern.<p> */
    private String m_concern;
    /** Stores the details of the concern.<p> */
    private String m_concernDetail;
    /** Stores the contact city.<p> */
    private String m_contactCity;
    /** Stores the contact country.<p> */
    private String m_contactCountry;
    /** Stores the contact email address.<p> */
    private String m_contactEmail;
    /** Stores the contacts first name.<p> */
    private String m_contactFirstName;
    /** Stores the contacts last name.<p> */
    private String m_contactLastName;
    /** Stores the contact street number.<p> */
    private String m_contactNumber;
    /** Stores the contact phone number.<p> */
    private String m_contactPhone;
    /** Stores the contact salutation.<p> */
    private String m_contactSalutation;
    /** Stores the contact street.<p> */
    private String m_contactStreet;
    /** Stores the contact title.<p> */
    private String m_contactTitle;
    /** Stores the contact zip code.<p> */
    private String m_contactZip;
    /** Stores the send copy to sender flag.<p> */
    private String m_copy;
    /** Stores the message for the recipient.<p> */
    private String m_message;
    
    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsTemplateFormLetter() {
        super();
        // set the members to empty Strings
        m_concern = "";
        m_concernDetail = "";
        m_contactCity = "";
        m_contactCountry = "";
        m_contactEmail = "";
        m_contactFirstName = "";
        m_contactLastName = "";
        m_contactNumber = "";
        m_contactPhone = "";
        m_contactSalutation = "";
        m_contactStreet = "";
        m_contactTitle = "";
        m_contactZip = "";
        m_copy = "";
        m_message = "";

    }
    
    /**
     * Constructor, with parameters.<p>
     * 
     * Use this constructor for the template.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsTemplateFormLetter(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        super();
        super.init(context, req, res);
    }
    
    
    
    /**
     * Returns the concern of contact.<p>
     *
     * @return the concern of contact
     */
    public String getConcern() {
        return m_concern;
    }
    
    /**
     * Returns the concern details if specified.<p>
     *
     * @return the concern details if specified
     */
    public String getConcernDetail() {
        return m_concernDetail;
    }
    
    /**
     * Returns the contact city.<p>
     *
     * @return the contact city
     */
    public String getContactCity() {
        return m_contactCity;
    }
    
    /**
     * Returns the contact country.<p>
     *
     * @return the contact country
     */
    public String getContactCountry() {
        return m_contactCountry;
    }
    
    /**
     * Returns the contact email address.<p>
     *
     * @return the contact email address
     */
    public String getContactEmail() {

        return m_contactEmail;
    }
    
    /**
     * Returns the contact first name.<p>
     *
     * @return the contact first name
     */
    public String getContactFirstName() {
        return m_contactFirstName;
    }
    
    /**
     * Returns the contact last name.<p>
     *
     * @return the contact last name
     */
    public String getContactLastName() {
        return m_contactLastName;
    }
    
    /**
     * Returns the contact street number.<p>
     *
     * @return the contact street number
     */
    public String getContactNumber() {
        return m_contactNumber;
    }
    
    /**
     * Returns the contact phone number.<p>
     *
     * @return the contact phone number
     */
    public String getContactPhone() {
        return m_contactPhone;
    }
    
    /**
     * Returns the contact salutation.<p>
     *
     * @return the contact salutation
     */
    public String getContactSalutation() {
        return m_contactSalutation;
    }
    
    /**
     * Returns the contact street.<p>
     *
     * @return the contact street
     */
    public String getContactStreet() {
        return m_contactStreet;
    }
    
    /**
     * Returns the contact title.<p>
     *
     * @return the contact title
     */
    public String getContactTitle() {
        return m_contactTitle;
    }
    
    /**
     * Returns the contact zip code.<p>
     *
     * @return the contact zip code
     */
    public String getContactZip() {
        return m_contactZip;
    }
    
    /**
     * Returns the send copy to sender flag.<p>
     *
     * @return the send copy to sender flag
     */
    public String getCopy() {

        return m_copy;
    }
    
    /**
     * Returns the message for the recipient.<p>
     *
     * @return the message for the recipient
     */
    public String getMessage() {

        return m_message;
    }
    
    /**
     * Returns the "checked" attribute if the current "concern" radio button is checked.<p>
     * 
     * @param currentValue the value of the current radio button to check
     * @return the "checked" attribute if the current "concern" radio button is checked
     */
    public String isConcernChecked(String currentValue) {
        if (isSelected(currentValue, getConcern())) {
            return " checked=\"checked\"";    
        }
        return "";
    }
    
    /**
     * Examines the value of the send copy checkbox and returns the "checked" attribute.<p>
     * 
     * @return the "checked" attribute or an empty String
     */
    public String isCopyChecked() {
        return isChecked(getCopy());    
    }
    
    /**
     * Returns the "selected" attribute if the current "contact option" is selected.<p>
     * 
     * @param currentValue the value of the current radio button to check
     * @return the "selected" attribute if the current "contact option" is selected
     */
    public String isSalutationSelected(String currentValue) {
        if (isSelected(currentValue, getContactSalutation())) {
            return " selected=\"selected\"";    
        }
        return "";
    }
    
    /**
     * Sends the recommendation email(s) to the recipient and/or the sender.<p>
     * 
     * @return true if the emails were successfully sent, otherwise false;
     */
    public boolean sendMail() {

        // create the new mail message
        CmsHtmlMail theMail = new CmsHtmlMail();
        theMail.setSubject(key("letter.mail.subject.prefix") + getPageTitle());
        theMail.setHtmlMsg(getContent("letter_mail.html", "html", getRequestContext().getLocale()));
        theMail.setTextMsg(getContent("letter_mail.html", "text", getRequestContext().getLocale()));
        try {
            // store the uri
            String uri = getRequestContext().getUri();
            // set the recipient from imprint information
            try {
                // create an instance of imprint bean
                CmsTemplateImprint imprint = new CmsTemplateImprint(getJspContext(), getRequest(), getResponse());
                // get the author email address
                String receiver = imprint.getEmail(null);
                theMail.addTo(receiver);
            } finally {
                // set request context uri back because this is changed in imprint bean
                getRequestContext().setUri(uri);
            }
            // set the recipient and the reply to address
            String sender = OpenCms.getSystemInfo().getMailSettings().getMailFromDefault();
            String contactMail = getContactEmail();
            if (contactMail == null || "".equals(contactMail.trim())) {
                contactMail = sender;    
            }
            theMail.setFrom(sender);
            theMail.addReplyTo(contactMail);
            if (!"".equals(getCopy())) {
                // send a copy of the mail to the sender
                theMail.addCc(contactMail);
            }
            // send the mail
            theMail.send();
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(e);
            }
            return false;
        }
        return true;
    }    
    
    /**
     * Sets the concern of contact.<p>
     *
     * @param concern the concern of contact
     */
    public void setConcern(String concern) {
        m_concern = concern;
    }
    
    /**
     * Sets the concern details if specified.<p>
     *
     * @param concernDetail the concern details if specified
     */
    public void setConcernDetail(String concernDetail) {
        m_concernDetail = concernDetail;
    }
    
    /**
     * Sets the contact city.<p>
     *
     * @param contactCity the contact city
     */
    public void setContactCity(String contactCity) {
        m_contactCity = contactCity;
    }
    
    /**
     * Sets the contact country.<p>
     *
     * @param contactCountry the contact country
     */
    public void setContactCountry(String contactCountry) {
        m_contactCountry = contactCountry;
    }
    
    /**
     * Sets the contact email address.<p>
     *
     * @param email the contact email address
     */
    public void setContactEmail(String email) {

        m_contactEmail = email;
    }
    
    /**
     * Sets the contact first name.<p>
     *
     * @param contactFirstName the contact first name
     */
    public void setContactFirstName(String contactFirstName) {
        m_contactFirstName = contactFirstName;
    }
    
    /**
     * Sets the contact last name.<p>
     *
     * @param contactLastName the contact last name
     */
    public void setContactLastName(String contactLastName) {
        m_contactLastName = contactLastName;
    }
    
    /**
     * Sets the contact street number.<p>
     *
     * @param contactNumber the contact street number
     */
    public void setContactNumber(String contactNumber) {
        m_contactNumber = contactNumber;
    }
    
    /**
     * Sets the contact phone number.<p>
     *
     * @param contactPhone the contact phone number
     */
    public void setContactPhone(String contactPhone) {
        m_contactPhone = contactPhone;
    }
    
    /**
     * Sets the contact salutation.<p>
     *
     * @param contactSalutation the contact salutation
     */
    public void setContactSalutation(String contactSalutation) {
        m_contactSalutation = contactSalutation;
    }
    
    /**
     * Sets the contact street.<p>
     *
     * @param contactStreet the contact street
     */
    public void setContactStreet(String contactStreet) {
        m_contactStreet = contactStreet;
    }
    
    /**
     * Sets the contact title.<p>
     *
     * @param contactTitle the contact title
     */
    public void setContactTitle(String contactTitle) {
        m_contactTitle = contactTitle;
    }
    
    /**
     * Sets the contact zip code.<p>
     *
     * @param contactZip the contact zip code
     */
    public void setContactZip(String contactZip) {
        m_contactZip = contactZip;
    }
    
    /**
     * Sets the send copy to sender flag.<p>
     *
     * @param copy the send copy to sender flag
     */
    public void setCopy(String copy) {

        m_copy = copy;
    }
    
    /**
     * Sets the message for the recipient.<p>
     *
     * @param message the message for the recipient
     */
    public void setMessage(String message) {

        m_message = message;
    }
    
    /**
     * Validates the values of the input fields and creates error messages, if necessary.<p>
     * 
     * @return true if all checked input values are valid, otherwise false
     */
    public boolean validate() {
        boolean allOk = true;
        setErrors(new HashMap());
        // check concern
        if (getConcern() == null || "".equals(getConcern())) {
            getErrors().put("concern", key("letter.error.concern.empty"));
            allOk = false;   
        } else {
            // concern given, check if "other" is selected
            if ("other".equals(getConcern())) {
                if (getConcernDetail() == null || "".equals(getConcernDetail())) {
                    // details not given  
                    getErrors().put("concern", key("letter.error.concerndetails.empty"));
                    allOk = false;  
                }    
            }
        }
        // check message
        if (getMessage() == null || "".equals(getMessage().trim())) {
            getErrors().put("message", key("letter.error.message.empty"));
            allOk = false;
        }
        if (!"".equals(getCopy())) {
            // send copy to sender is checked, check email address
            if (getContactEmail() == null || "".equals(getContactEmail())) {
                // email address is empty
                getErrors().put("email", key("letter.error.email.empty"));
                allOk = false;
            }
        }
        if (getContactEmail() != null && !"".equals(getContactEmail()) && !isValidEmailAddress(getContactEmail())) {
            // email address is not valid
            getErrors().put("email", key("letter.error.email.wrong"));
            allOk = false;
        }
        return allOk;    
    }
    
    /**
     * @see org.opencms.frontend.templateone.CmsTemplateForm#checkTextsUri()
     */
    protected String checkTextsUri() {
        String fileUri = getConfigurationValue("page.form.letter", null);
        if (fileUri != null) {
            fileUri = getRequestContext().removeSiteRoot(fileUri);
            try {
                getCmsObject().readResource(fileUri);
                return fileUri;
            } catch (CmsException e) {
                // file not found, use default texts page file
            }
        }
        return I_CmsWpConstants.C_VFS_PATH_MODULES + C_MODULE_NAME + "/pages/letter_content.html";
    }

}
