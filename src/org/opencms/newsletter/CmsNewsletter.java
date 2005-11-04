/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/newsletter/CmsNewsletter.java,v $
 * Date   : $Date: 2005/11/04 09:38:19 $
 * Version: $Revision: 1.1.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.newsletter;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.mail.CmsSimpleMail;
import org.opencms.mail.CmsVfsDataSource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.mail.Email;

/** 
 * Basic implementation of the interface {@link I_CmsNewsletter}.<p>
 * 
 * @author Jan Baudisch
 */
public class CmsNewsletter implements I_CmsNewsletter {

    /** The attachments, a list of {@link org.opencms.file.CmsResource} objects.<p> */
    private List m_attachments;

    /** The contents, a list of {@link CmsNewsletterContent} objects.<p> */
    private List m_contents;

    /** The subject of the newsletter.<p> */
    private String m_subject;

    /** Creates a new newsletter instance.<p> */
    public CmsNewsletter() {

        m_contents = new ArrayList();
        m_attachments = new ArrayList();
    }

    /**
     * 
     * @see org.opencms.newsletter.I_CmsNewsletter#addAttachment(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public void addAttachment(CmsObject cms, CmsResource resource) {

        m_attachments.add(resource);
    }

    /**
     * 
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
     * @throws MessagingException if there is an error attaching resources
     */
    public Email getEmail(CmsObject cms, I_CmsNewsletterRecipient recipient) throws MessagingException, CmsException {

        StringBuffer htmlMsg = new StringBuffer(1024);
        StringBuffer txtMsg = new StringBuffer(1024);
        Iterator contents = m_contents.iterator();
        while (contents.hasNext()) {
            I_CmsNewsletterContent content = (I_CmsNewsletterContent)contents.next();
            if (recipient.isSubscriber(content)) {
                if (content.getType().equals(CmsNewsletterContentType.TYPE_HTML)) {
                    htmlMsg.append(content.getContent());
                } else {
                    txtMsg.append(content.getContent());
                }
            }
        }
        Email email;
        if (htmlMsg.length() > 0 || !m_attachments.isEmpty()) {
            email = new CmsHtmlMail();
            ((CmsHtmlMail)email).setHtmlMsg(replaceMacros(htmlMsg.toString(), recipient));
            Iterator attachments = m_attachments.iterator();
            while (attachments.hasNext()) {
                CmsResource resource = (CmsResource)attachments.next();
                // set the description of the attachment either to the property description, if it is set, or 
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
                ((CmsHtmlMail)email).attach(new CmsVfsDataSource(cms, resource), resource.getName(), description);
            }
        } else {
            // only text content, return text mail
            email = new CmsSimpleMail();
        }
        ((CmsHtmlMail)email).setTextMsg(replaceMacros(txtMsg.toString(), recipient));
        email.addTo(recipient.getEmail());
        email.setSubject(m_subject);
        return email;
    }

    /**
     * 
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
