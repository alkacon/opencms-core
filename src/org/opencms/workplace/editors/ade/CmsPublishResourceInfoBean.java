/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsPublishResourceInfoBean.java,v $
 * Date   : $Date: 2009/10/28 15:38:11 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.editors.ade;

/**
 * A publish resource additional information bean.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.6 
 */
public class CmsPublishResourceInfoBean {

    /** Reason value constants, when resources can not be published. */
    public enum Type {

        /** The resource is still used in the online project. */
        BROKENLINK,
        /** Resource is locked by another user. */
        LOCKED,
        /** The resource is missing in the online project. */
        MISSING,
        /** User does not have enough permissions. */
        PERMISSIONS,
        /** Resource has been already published. */
        PUBLISHED,
        /** Changed related resource can not be published. */
        RELATED;
    }

    /** The additional info type.*/
    private final Type m_type;

    /** The additional info.*/
    private final String m_value;

    /** 
     * Creates a new publish resource additional information bean.<p> 
     * 
     * @param value the additional info
     * @param type the additional info type
     **/
    public CmsPublishResourceInfoBean(String value, Type type) {

        m_type = type;
        m_value = value;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public Type getType() {

        return m_type;
    }

    /**
     * Returns the value.<p>
     *
     * @return the value
     */
    public String getValue() {

        return m_value;
    }
}
