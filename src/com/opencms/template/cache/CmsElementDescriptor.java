/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementDescriptor.java,v $
* Date   : $Date: 2001/05/03 16:00:41 $
* Version: $Revision: 1.1 $
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.opencms.template.cache;

import java.util.*;
import java.io.*;
import com.opencms.file.*;

/**
 * This descriptor is used to locate CmsElement-Objects with the
 * CmsElementLocator. It is the key for a CmsElement.
 *
 * @author: Andreas Schouten
 */
public class CmsElementDescriptor {

    /**
     * The name of the class for this descriptor.
     */
    private String m_className;

    /**
     * The name of the template-file for this descriptor.
     */
    private String m_templateName;

    /**
     * The constructor to create a new CmsElementDescriptor.
     *
     * @param className the name of the class for this descriptor.
     * @param templateName the name of the template for this descriptor.
     */
    public CmsElementDescriptor(String className, String templateName) {
        m_className = className;
        m_templateName = templateName;
    }

    /**
     * Returns the key of this descriptor.
     *
     * @returns the key of this descriptor.
     */
    public String getKey() {
        return m_className + "|" + m_templateName;
    }
}