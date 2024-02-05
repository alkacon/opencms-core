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
 * Content for newsletters.<p>
 */
public class CmsNewsletterContent implements I_CmsNewsletterContent {

    /** The output channel for this content. */
    String m_channel;

    /** The content String. */
    String m_content;

    /** The order of this content. */
    int m_order;

    /** The type of this content. */
    CmsNewsletterContentType m_type;

    /**
     * Creates a new CmsNewsletterContent instance.<p>
     *
     * @param order the order of the newsletter content
     * @param content the content
     * @param type the newsletter contents' type
     */
    public CmsNewsletterContent(int order, String content, CmsNewsletterContentType type) {

        m_order = order;
        m_content = content;
        m_type = type;
        m_channel = "";
    }

    /**
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(I_CmsNewsletterContent o) {

        return Integer.valueOf(m_order).compareTo(Integer.valueOf(((CmsNewsletterContent)o).getOrder()));
    }

    /**
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof CmsNewsletterContent)) {
            return false;
        }
        CmsNewsletterContent newsletterContent = (CmsNewsletterContent)obj;
        if (getOrder() != newsletterContent.getOrder()) {
            return false;
        }
        if (!getContent().equals(newsletterContent.getContent())) {
            return false;
        }
        if (!getChannel().equals(newsletterContent.getChannel())) {
            return false;
        }
        if (!getType().equals(newsletterContent.getType())) {
            return false;
        }
        return true;
    }

    /**
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_channel.hashCode() + m_content.hashCode() + m_order + m_type.hashCode();
    }

    /**
     * @see org.opencms.newsletter.I_CmsNewsletterContent#getChannel()
     */
    public String getChannel() {

        return m_channel;
    }

    /**
     * @see org.opencms.newsletter.I_CmsNewsletterContent#getContent()
     */
    public String getContent() {

        return m_content;
    }

    /**
     * @see org.opencms.newsletter.I_CmsNewsletterContent#getOrder()
     */
    public int getOrder() {

        return m_order;
    }

    /**
     * @see org.opencms.newsletter.I_CmsNewsletterContent#getType()
     */
    public CmsNewsletterContentType getType() {

        return m_type;
    }
}