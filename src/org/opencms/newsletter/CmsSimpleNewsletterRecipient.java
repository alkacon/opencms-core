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

/**
 * Simple implementation of interface {@link I_CmsNewsletterRecipient}, with
 * {@link I_CmsNewsletterRecipient#isSubscriber(org.opencms.newsletter.I_CmsNewsletterContent)} always returning true.<p>
 *
 * @since 6.0.2
 *
 */
public class CmsSimpleNewsletterRecipient implements I_CmsNewsletterRecipient {

    /** The email address of this recipient. */
    private String m_email;

    /** The firstname of this recipient. */
    private String m_firstname;

    /** The lastname of this recipient. */
    private String m_lastname;

    /** The nicename of this recipient. */
    private String m_name;

    /**
     * Creates a new CmsSimpleNewsletterRecipient.<p>
     *
     * @param email the email address to be sent
     * @param name the nicename of the recipient
     */
    public CmsSimpleNewsletterRecipient(String email, String name) {

        m_email = email;
        m_name = name;
    }

    /**
     * Creates a new CmsSimpleNewsletterRecipient.<p>
     *
     * @param email the email address to be sent
     * @param firstname the firstname of the recipient
     * @param lastname the newsletter recipient's lastname
     */
    public CmsSimpleNewsletterRecipient(String email, String firstname, String lastname) {

        m_email = email;
        m_firstname = firstname;
        m_lastname = lastname;
        m_name = firstname + ' ' + lastname;
    }

    /**
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof CmsSimpleNewsletterRecipient)) {
            return false;
        }
        CmsSimpleNewsletterRecipient recipient = (CmsSimpleNewsletterRecipient)obj;
        if (getEmail() != recipient.getEmail()) {
            return false;
        }
        if (!getName().equals(recipient.getName())) {
            return false;
        }
        return true;
    }

    /**
     * @see org.opencms.newsletter.I_CmsNewsletterRecipient#getEmail()
     */
    public String getEmail() {

        return m_email;
    }

    /**
     * @see org.opencms.newsletter.I_CmsNewsletterRecipient#getFirstname()
     */
    public String getFirstname() {

        return m_firstname;
    }

    /**
     * @see org.opencms.newsletter.I_CmsNewsletterRecipient#getFullName()
     */
    public String getFullName() {

        return m_name;
    }

    /**
     * @see org.opencms.newsletter.I_CmsNewsletterRecipient#getLastname()
     */
    public String getLastname() {

        return m_lastname;
    }

    /**
     * Returns the name of the recipient.<p>
     *
     * @return the name of the recipient.
     */
    public String getName() {

        return m_name;
    }

    /**
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_email.hashCode() + m_firstname.hashCode() + m_lastname.hashCode() + m_name.hashCode();
    }

    /**
     * @see org.opencms.newsletter.I_CmsNewsletterRecipient#isSubscriber(org.opencms.newsletter.I_CmsNewsletterContent)
     */
    public boolean isSubscriber(I_CmsNewsletterContent content) {

        return true;
    }

    /**
     * Set the email address of this recepient.<p>
     *
     * @param email the email address of this recepient to set.
     */
    protected void setEmail(String email) {

        m_email = email;
    }
}