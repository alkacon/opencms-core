/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsNamedObject.java,v $
 * Date   : $Date: 2005/02/16 11:43:02 $
 * Version: $Revision: 1.1 $
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

package org.opencms.util;

/**
 * Wrapper for objects to become named.<p>
 * 
 * @author <a href="mailto:m.moossen@alkacon.com">Michael Moossen</a> 
 * @version $Revision: 1.1 $
 * @since 6.0
 */
public class CmsNamedObject implements I_CmsNamedObject {

    private final String m_name;
    private final Object m_object;

    /**
     * Default CTor.<p>
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