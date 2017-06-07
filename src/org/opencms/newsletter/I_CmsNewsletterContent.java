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
 * Every {@link I_CmsNewsletter} contains a list of I_CmsNewsletterContent objects. <p>
 *
 * These objects have an order, by which the newsletter content is sorted, the type either
 * {@link org.opencms.newsletter.CmsNewsletterContentType#TYPE_HTML} or
 * {@link org.opencms.newsletter.CmsNewsletterContentType#TYPE_TEXT}.
 */
public interface I_CmsNewsletterContent extends Comparable<I_CmsNewsletterContent> {

    /**
     * Returns the channel.<p>
     *
     * @return the channel
     */
    String getChannel();

    /**
     * Returns the content.<p>
     *
     * @return the content
     */
    String getContent();

    /**
     * Returns the order of this content in it's channel.<p>
     *
     * @return the order of this content in it's channel
     */
    int getOrder();

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    CmsNewsletterContentType getType();
}