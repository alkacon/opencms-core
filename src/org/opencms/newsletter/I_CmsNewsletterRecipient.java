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
 * Interface for recipients of {@link I_CmsNewsletter} objects.<p>
 */
public interface I_CmsNewsletterRecipient {

    /**
     * Returns the email address of the recipient.<p>
     *
     * @return the email address of the recipient.
     */
    String getEmail();

    /**
     * Returns the firstname of the recipient.<p>
     *
     * @return the firstname of the recipient.
     */
    String getFirstname();

    /**
     * Returns the nicename of the recipient.<p>
     *
     * @return the nicename of the recipient.
     */
    String getFullName();

    /**
     * Returns the lastname of the recipient.<p>
     *
     * @return the lastname of the recipient.
     */
    String getLastname();

    /**
     * Returns <code>true</code> in case this newsletter recipient has subscribed to the given content.<p>
     *
     * @param content the content to check for subscription
     * @return <code>true</code> in case this newsletter recipient has subscribed to the given content
     */
    boolean isSubscriber(I_CmsNewsletterContent content);
}