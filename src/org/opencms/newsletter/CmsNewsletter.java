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

package org.opencms.newsletter;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.mail.CmsSimpleMail;
import org.opencms.mail.CmsVfsDataSource;
import org.opencms.mail.Messages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

/**
 * Basic implementation of the interface {@link I_CmsNewsletter}.
 * <p>
 */
public class CmsNewsletter implements I_CmsNewsletter {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNewsletter.class);

    /** The attachments, a list of {@link org.opencms.file.CmsResource} objects. */
    private List<CmsResource> m_attachments;

    /** The contents, a list of {@link I_CmsNewsletterContent} objects. */
    private List<I_CmsNewsletterContent> m_contents;

    /** The subject of the newsletter. */
    private String m_subject;

    /**
     * Creates a new newsletter instance.<p>
     */
    public CmsNewsletter() {

        m_contents = new ArrayList<I_CmsNewsletterContent>();
        m_attachments = new ArrayList<CmsResource>();
    }

    /**
     * @see org.opencms.newsletter.I_CmsNewsletter#addAttachment(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public void addAttachment(CmsObject cms, CmsResource resource) {

        m_attachments.add(resource);
    }

    /**
     * @see org.opencms.newsletter.I_CmsNewsletter#addContent(org.opencms.newsletter.I_CmsNewsletterContent)
     */
    public void addContent(I_CmsNewsletterContent content) {

        m_contents.add(content);
    }

    /**
     * Returns the e-mail for the newsletter.<p>
     *
     * @param recipient the recipient to whom the newsletter is sent
     * @param cms the CmsObject
     *
     * @return the e-mail for the newsletter
     *
     * @throws CmsException if something goes wrong
     */
    public Email getEmail(CmsObject cms, I_CmsNewsletterRecipient recipient) throws CmsException {

        StringBuffer htmlMsg = new StringBuffer(1024);
        StringBuffer txtMsg = new StringBuffer(1024);
        Iterator<I_CmsNewsletterContent> contents = m_contents.iterator();
        while (contents.hasNext()) {
            I_CmsNewsletterContent content = contents.next();
            if (recipient.isSubscriber(content)) {
                if (content.getType().equals(CmsNewsletterContentType.TYPE_HTML)) {
                    htmlMsg.append(content.getContent());
                } else {
                    txtMsg.append(content.getContent());
                }
            }
        }
        Email email = null;
        try {
            if ((htmlMsg.length() > 0) || !m_attachments.isEmpty()) {
                // we need to create a HTML mail
                CmsHtmlMail htmlMail = new CmsHtmlMail();
                htmlMail.setHtmlMsg(replaceMacros(htmlMsg.toString(), recipient));
                Iterator<CmsResource> attachments = m_attachments.iterator();
                while (attachments.hasNext()) {
                    CmsResource resource = attachments.next();
                    // set the description of the attachment either to the
                    // property description, if it is set, or
                    // to the property title
                    String description = "";
                    String propertyDescription = cms.readPropertyObject(
                        cms.getSitePath(resource),
                        CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                        true).getValue();
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(propertyDescription)) {
                        description = propertyDescription;
                    } else {
                        String propertyTitle = cms.readPropertyObject(
                            cms.getSitePath(resource),
                            CmsPropertyDefinition.PROPERTY_TITLE,
                            true).getValue();
                        description = propertyTitle;
                    }
                    htmlMail.attach(new CmsVfsDataSource(cms, resource), resource.getName(), description);
                }
                htmlMail.setTextMsg(replaceMacros(txtMsg.toString(), recipient));
                email = htmlMail;
            } else {
                // only text content, return text mail
                CmsSimpleMail textMail = new CmsSimpleMail();
                textMail.setMsg(replaceMacros(txtMsg.toString(), recipient));
                email = textMail;
            }
            email.addTo(recipient.getEmail());
            email.setSubject(m_subject);
        } catch (EmailException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_COMPOSE_MAIL_ERR_0), e);
        }
        return email;
    }

    /**
     * @see org.opencms.newsletter.I_CmsNewsletter#setSubject(java.lang.String)
     */
    public void setSubject(String subject) {

        m_subject = subject;
    }

    /**
     * Replaces the macros in the given message.<p>
     *
     * @param msg the message in which the macros are replaced
     * @param recipient the recipient in the message
     *
     * @return the message with the macros replaced
     */
    private String replaceMacros(String msg, I_CmsNewsletterRecipient recipient) {

        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.addMacro(MACRO_USER_FIRSTNAME, recipient.getFirstname());
        resolver.addMacro(MACRO_USER_LASTNAME, recipient.getLastname());
        resolver.addMacro(MACRO_USER_FULLNAME, recipient.getFullName());
        resolver.addMacro(MACRO_USER_EMAIL, recipient.getEmail());
        resolver.addMacro(
            MACRO_SEND_DATE,
            DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())));
        return resolver.resolveMacros(msg);
    }
}