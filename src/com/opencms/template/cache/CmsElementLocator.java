/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementLocator.java,v $
* Date   : $Date: 2001/05/07 08:57:24 $
* Version: $Revision: 1.2 $
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
import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;

/**
 * The ElementLocator is used to receive CmsElement-Objects. It is the Cache for
 * these CmsElement-Objects. The CmsElement-Objects are stored in memory or -
 * if they are notc used a long time - written to an external database. The
 * locator manages all the reading, writing and management of the CmsElement's.
 *
 * @author: Andreas Schouten
 */
public class CmsElementLocator {

    /**
     * A hashtable to store the elements.
     */
    private Hashtable m_elements;

    /**
     * The default constructor for this locator.
     */
    CmsElementLocator() {
        m_elements = new Hashtable();
    }

    /**
     * Adds a new Element to this locator.
     * @param descriptor - the descriptor for this element.
     * @param element - the Element to put in this locator.
     */
    private void put(CmsElementDescriptor desc, A_CmsElement element) {
        m_elements.put(desc.getKey(), element);
    }

    /**
     * Gets a Elements from this locator.
     * @param desc - the descriptor to locate the element.
     * @returns the element that was found.
     */
    public A_CmsElement get(CmsObject cms, CmsElementDescriptor desc, Hashtable parameters) {
        A_CmsElement result;
        result = (A_CmsElement)m_elements.get(desc.getKey());
        if(result == null) {
            // the element was not found in the element cache
            // we have to generate it
            try {
                Object o = com.opencms.template.CmsTemplateClassManager.getClassInstance(cms, desc.getClassName());
                I_CmsTemplate cmsTemplate = (I_CmsTemplate)o;
                result = cmsTemplate.createElement(cms, desc.getTemplateName(), parameters);
            } catch(CmsException e) {
                System.err.println(e);
            }
            put(desc, result);
        }
        return result;

    }
}