/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementDefinitionCollection.java,v $
* Date   : $Date: 2001/05/09 11:47:41 $
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
 * An instance of CmsElementDefinitions represents an "pointer" to other
 * elements. This pointers are stored in CmsElement's. An ElementDfinitions
 * stores information about an element, like the name, className, templateName
 * and parameters for the pointed element.
 *
 * @author: Alexander Lucas
 */
public class CmsElementDefinitionCollection {

    private Hashtable m_eldefs = new Hashtable();

    public CmsElementDefinitionCollection() {
    }

    public CmsElementDefinitionCollection(CmsElementDefinitionCollection primary, CmsElementDefinitionCollection secondary) {
        Vector allKeys = new Vector();
        Enumeration keys1 = primary.m_eldefs.keys();
        Enumeration keys2 = secondary.m_eldefs.keys();
        while(keys1.hasMoreElements()) {
            allKeys.addElement(keys1.nextElement());
        }
        while(keys2.hasMoreElements()) {
            Object o = keys2.nextElement();
            if(!allKeys.contains(o)) {
                allKeys.addElement(o);
            }
        }
        Enumeration loop = allKeys.elements();
        while(loop.hasMoreElements()) {
            String currentKey = (String)loop.nextElement();
            CmsElementDefinition def1 = (CmsElementDefinition)primary.m_eldefs.get(currentKey);
            CmsElementDefinition def2 = (CmsElementDefinition)secondary.m_eldefs.get(currentKey);
            m_eldefs.put(currentKey, new CmsElementDefinition(def1, def2));
        }
    }

    public void add(CmsElementDefinition def) {
        try {
            m_eldefs.put(def.getName(), def);
        } catch(NullPointerException e) {
            System.err.println("*** NULL " + def);
            System.err.println("*** " + def.getName());
        }
    }

    public CmsElementDefinition get(String name) {
        return (CmsElementDefinition)m_eldefs.get(name);
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("-----------------------------------------------------------------\n");
        result.append("Element definition dump: \n");
        Enumeration keys = m_eldefs.keys();
        while(keys.hasMoreElements()) {
            String name = (String)keys.nextElement();
            CmsElementDefinition current = (CmsElementDefinition)m_eldefs.get(name);
            result.append(current.toString() + "\n");
        }
        result.append("-----------------------------------------------------------------\n");
        return result.toString();
    }
}