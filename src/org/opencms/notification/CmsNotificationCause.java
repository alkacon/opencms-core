/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/notification/CmsNotificationCause.java,v $
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

import org.opencms.util.CmsUUID;

import java.io.Serializable;

/**
 * Objects of this class are serialized in the additional infos of a user to store, which resources were
 * already confirmed by the user.
 * This class is the counterpart to <code>{@link org.opencms.notification.CmsExtendedNotificationCause}</code>, to be used
 * for serialization in the AdditionalInfos of a <code>{@link org.opencms.file.CmsUser}</code>, and therefore only 
 * contains the essential information
 * <p>
 * 
 * @author Jan Baudisch
 * 
 */
public class CmsNotificationCause implements Serializable {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 257325098377830418L;

    /** The reason that the resource occures in the notification. */
    private int m_cause;

    /** The resource. */
    private CmsUUID m_resourceId;

    /**
     * Creates a new CmsNotificationResourceInfo.<p>
     * 
     * @param resource the specific resource
     * @param cause that the resource occures in the notification
     */
    public CmsNotificationCause(CmsUUID resource, int cause) {

        m_resourceId = resource;
        m_cause = cause;
    }

    /**
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_cause + m_resourceId.hashCode();
    }

    /**
     * Returns true if the Object equals to the corresponding CmsNotificationCause, that means a notification cause
     * with the same resource and cause.
     * 
     * @return true if the resource info is equal to a notification cause or resource info with the same resource and cause
     * 
     * @param o the object to check for equality
     * 
     * @see org.opencms.notification.CmsExtendedNotificationCause#equals(java.lang.Object)
     */
    public boolean equals(Object o) {

        if (!(o instanceof CmsExtendedNotificationCause) || !(o instanceof CmsNotificationCause)) {
            return false;
        }
        return hashCode() == o.hashCode();
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
     * Returns the resource.<p>
     *
     * @return the resource
     */
    public CmsUUID getResourceId() {

        return m_resourceId;
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
     * Sets the resource.<p>
     *
     * @param resourceId the resource to set
     */
    public void setResourceId(CmsUUID resourceId) {

        m_resourceId = resourceId;
    }
}
