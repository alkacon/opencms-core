/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceView.java,v $
 * Date   : $Date: 2005/06/23 10:47:19 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace;

/**
 * Contains the data of a single workplace view.<p>
 *  
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsWorkplaceView implements Comparable {

    /** The localization key of this view. */
    private String m_key;

    /** The sort order of the view. */
    private Float m_order;

    /** The URI of the OpenCms VFS resource (folder) of the view. */
    private String m_uri;

    /**
     * Creates a new workplace view.<p>
     * 
     * @param key the localization key for the display name of the view 
     * @param uri of the view page in the OpenCms VFS
     * @param order the sort order of the view 
     */
    public CmsWorkplaceView(String key, String uri, Float order) {

        m_key = key;
        m_uri = uri;
        m_order = order;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsWorkplaceView) {
            return m_order.compareTo(((CmsWorkplaceView)obj).getOrder());
        }
        return 0;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsWorkplaceView) {
            return ((CmsWorkplaceView)obj).m_uri.equals(m_uri);
        }
        return false;
    }

    /**
     * Returns the localization key for the display name of this view .<p>
     * 
     * @return the localization key
     */
    public String getKey() {

        return m_key;
    }

    /**
     * Returns the sort order of this view.<p>
     * 
     * @return the sort order of this view
     */
    public Float getOrder() {

        return m_order;
    }

    /**
     * Returns the OpenCms VFS uri of this view.<p>
     * 
     * @return the uri
     */
    public String getUri() {

        return m_uri;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return getUri().hashCode();
    }
}