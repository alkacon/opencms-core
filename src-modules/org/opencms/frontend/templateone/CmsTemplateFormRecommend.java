/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateFormRecommend.java,v $
 * Date   : $Date: 2005/06/23 09:05:01 $
 * Version: $Revision: 1.10 $
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

package org.opencms.frontend.templateone;

import org.opencms.mail.CmsHtmlMail;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.I_CmsWpConstants;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods to build the page recommendation form.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
 */
public class CmsTemplateFormRecommend extends CmsTemplateForm {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateFormRecommend.class);

    /** Stores the send copy to sender flag.<p> */
    private String m_copy;
    /** Stores the email recipient address.<p> */
    private String m_emailRecipient;
    /** Stores the email sender address.<p> */
    private String m_emailSender;
    /** Stores the message for the recipient.<p> */
    private String m_message;

    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsTemplateFormRecommend() {

        super();
        // set the members to empty Strings
        m_emailRecipient = "";
        m_emailSender = "";
        m_message = "";
        m_copy = "";
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
    public CmsTemplateFormRecommend(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super();
        super.init(context, req, res);
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
     * Returns the email recipient address.<p>
     *
     * @return the email recipient address
     */
    public String getEmailRecipient() {

        return m_emailRecipient;
    }

    /**
     * Returns the email sender address.<p>
     *
     * @return the email sender address
     */
    public String getEmailSender() {

        return m_emailSender;
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
     * Examines the value of the send copy checkbox and returns the "checked" attribute.<p>
     * 
     * @return the "checked" attribute or an empty String
     */
    public String isCopyChecked() {

        return isChecked(getCopy());
    }

    /**
     * Sends the recommendation email(s) to the recipient and/or the sender.<p>
     * 
     * @return true if the emails were successfully sent, otherwise false;
     */
    public boolean sendMail() {

        // create the new mail message
        CmsHtmlMail theMail = new CmsHtmlMail();
        theMail.setSubject(key("recommend.mail.subject.prefix") + getPageTitle());
        theMail.setCharset(getRequestContext().getEncoding());
        theMail.setHtmlMsg(getContent("recommend_mail.html", "html", getRequestContext().getLocale()));
        theMail.setTextMsg(getContent("recommend_mail.html", "text", getRequestContext().getLocale()));
        try {
            // set the recipient and the reply to address
            theMail.addTo(getEmailRecipient());
            String sender = OpenCms.getSystemInfo().getMailSettings().getMailFromDefault();
            String replyTo = getEmailSender();
            if (replyTo == null || "".equals(replyTo.trim())) {
                replyTo = sender;
            }
            theMail.setFrom(sender);
            theMail.addReplyTo(replyTo);
            if (!"".equals(getCopy())) {
                // send a copy of the mail to the sender
                theMail.addCc(replyTo);
            }
            // send the mail
            theMail.send();
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(e);
            } else if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(Messages.LOG_SEND_MAIL_RECOMMENDPAGE_1, getRequestContext().getUri()));
            }
            return false;
        }
        return true;
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
     * Sets the email recipient address.<p>
     *
     * @param emailRecipient email recipient address
     */
    public void setEmailRecipient(String emailRecipient) {

        m_emailRecipient = emailRecipient;
    }

    /**
     * Sets the email sender address.<p>
     *
     * @param emailSender the email sender address
     */
    public void setEmailSender(String emailSender) {

        m_emailSender = emailSender;
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

        // check email recipient
        if (getEmailRecipient() == null || "".equals(getEmailRecipient())) {
            // recipient is empty
            getErrors().put("recipient", key("recommend.error.recipient.empty"));
            allOk = false;
        } else if (!isValidEmailAddress(getEmailRecipient())) {
            // recipient is not valid
            getErrors().put("recipient", key("recommend.error.recipient.wrong"));
            allOk = false;
        }
        // check message
        if (getMessage() == null || "".equals(getMessage().trim())) {
            getErrors().put("message", key("recommend.error.message.empty"));
            allOk = false;
        }
        if (!"".equals(getCopy())) {
            // send copy to sender is checked, check sender address
            if (getEmailSender() == null || "".equals(getEmailSender())) {
                // sender is empty
                getErrors().put("sender", key("recommend.error.sender.empty"));
                allOk = false;
            }
        }
        if (getEmailSender() != null && !"".equals(getEmailSender()) && !isValidEmailAddress(getEmailSender())) {
            // sender is not valid
            getErrors().put("sender", key("recommend.error.sender.wrong"));
            allOk = false;
        }
        return allOk;
    }

    /**
     * @see org.opencms.frontend.templateone.CmsTemplateForm#checkTextsUri()
     */
    protected String checkTextsUri() {

        String fileUri = getConfigurationValue("page.form.recommend", null);
        if (fileUri != null) {
            fileUri = getRequestContext().removeSiteRoot(fileUri);
            try {
                getCmsObject().readResource(fileUri);
                return fileUri;
            } catch (CmsException e) {
                // file not found, use default texts page file
            }
        }
        return I_CmsWpConstants.C_VFS_PATH_MODULES + C_MODULE_NAME + "/pages/recommend_content.html";
    }

}
