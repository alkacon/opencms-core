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
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import javax.mail.MessagingException;

import org.apache.commons.mail.Email;

/**
 * Interface for the newsletter.<p>
 */
public interface I_CmsNewsletter {

    /** Macro for the date. */
    String MACRO_SEND_DATE = "date";

    /** Macro for the email address. */
    String MACRO_USER_EMAIL = "email";

    /** Macro for the firstname. */
    String MACRO_USER_FIRSTNAME = "firstname";

    /** Macro for the full name. */
    String MACRO_USER_FULLNAME = "fullname";

    /** Macro for the lastname. */
    String MACRO_USER_LASTNAME = "lastname";

    /**
     * Adds a OpenCms resource as an attachment to the newsletter.<p>
     *
     * @param cms the CmsObject
     * @param resource the resource to attach
     *
     * @throws CmsException if something goes wrong
     */
    void addAttachment(CmsObject cms, CmsResource resource) throws CmsException;

    /**
     * Adds content to the newsletter.<p>
     *
     * @param content the content to add
     */
    void addContent(I_CmsNewsletterContent content);

    /**
     * Returns the newsletter as an e-mail to be sent.<p>
     *
     * @param cms the CmsObject
     * @param recipient the recipient to which the newsletter will be sent
     * @return the newsletter as an e-mail
     * @throws MessagingException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    Email getEmail(CmsObject cms, I_CmsNewsletterRecipient recipient) throws MessagingException, CmsException;

    /**
     * Sets the subject.<p>
     *
     * @param subject the subject to set
     */
    void setSubject(String subject);
}
