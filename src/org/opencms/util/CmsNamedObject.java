/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsNamedObject.java,v $
 * Date   : $Date: 2005/04/15 13:02:43 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.util;

/**
 * Wrapper for objects to become named.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsNamedObject implements I_CmsNamedObject {

    /** Name of the object. */
    private final String m_name;

    /** Associated object. */
    private final Object m_object;

    /**
     * Default Constructor.<p>
     * 
     * @param name the name
     * @param object the object
     */
    public CmsNamedObject(String name, Object object) {

        m_name = name;
        m_object = object;
    }

    /**
     * @see org.opencms.util.I_CmsNamedObject#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the original object.<p>
     * 
     * @return the original object
     */
    public Object getObject() {

        return m_object;
    }

}