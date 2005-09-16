/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/notification/CmsExtendedNotificationCause.java,v $
 * Date   : $Date: 2005/09/16 08:51:27 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.notification;

import org.opencms.file.CmsResource;

import java.util.Date;

/**
 * Class to encapsulate a resource and the cause of its notification.<p>
 * 
 * @author Jan Baudisch
 * 
 */
public class CmsExtendedNotificationCause implements Comparable {

    /** The notification is sent because the resource will expire soon. */
    public static final int RESOURCE_EXPIRES = 0;

    /** The notification is sent because the resource will get outdated. */
    public static final int RESOURCE_OUTDATED = 1;

    /** constant indicating the cause of the notification for a resource. */
    public static final int RESOURCE_UPDATE_REQUIRED = 2;

    /** The notification is sent because the resource will be released soon. */
    public static final int RESOURCE_RELEASE = 3;

    /** The reason that the resource occures in the notification. */
    private int m_cause;

    /** The date when the event (e.g. release or expiration) will happen. */
    private Date m_date;

    /** The resource. */
    private CmsResource m_resource;

    /**
     * Creates a new CmsNotificationResourceInfo.<p>
     * 
     * @param resource the specific resource
     * @param cause that the resource occures in the notification
     * @param date when the event will happen
     */
    public CmsExtendedNotificationCause(CmsResource resource, int cause, Date date) {

        m_resource = resource;
        m_cause = cause;
        m_date = date;
    }

    /**
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {

        if (o instanceof CmsExtendedNotificationCause) {
            return getDate().compareTo(((CmsExtendedNotificationCause)o).getDate());
        } else {
            return -1;
        }
    }

    /**
     * Returns true if the Object equals to the corresponding CmsResourceInfo, that means a resource info
     * with the same resource and cause.
     * 
     * @return true if the resource info is equal to a notification cause or resource info with the same resource and cause
     * 
     * @param o the object to check for equality
     * 
     * @see org.opencms.notification.CmsNotificationCause#equals(java.lang.Object)
     */
    public boolean equals(Object o) {

        if (!(o instanceof CmsExtendedNotificationCause) && !(o instanceof CmsNotificationCause)) {
            return false;
        }
        return hashCode() == o.hashCode();
    }

    /**
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_cause + m_resource.getStructureId().hashCode();
    }

    /**
     * Returns the cause.<p>
     *
     * @return the cause
     */
    public int getCause() {

        return m_cause;
    }

    /**
     * Returns the date.<p>
     *
     * @return the date
     */
    public Date getDate() {

        return m_date;
    }

    /**
     * Returns the resource.<p>
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Sets the cause.<p>
     *
     * @param cause the cause to set
     */
    public void setCause(int cause) {

        m_cause = cause;
    }

    /**
     * Sets the date.<p>
     *
     * @param date the date to set
     */
    public void setDate(Date date) {

        m_date = date;
    }

    /**
     * Sets the resource.<p>
     *
     * @param resource the resource to set
     */
    public void setResource(CmsResource resource) {

        m_resource = resource;
    }
}
