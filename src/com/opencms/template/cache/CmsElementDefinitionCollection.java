/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementDefinitionCollection.java,v $
* Date   : $Date: 2001/05/09 12:28:49 $
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
import com.opencms.file.*;

/**
 * Used to collect a set of element definitions.
 * Two CmsElementDefinitionCollections can be merged using the join constructor.
 *
 * @author: Alexander Lucas <alexander.lucas@framfab.de>
 */
public class CmsElementDefinitionCollection {

    /** Hashtable for storing all definitions */
    private Hashtable m_eldefs = new Hashtable();

    /** Default constructor */
    public CmsElementDefinitionCollection() {
    }

    /**
     * Join cunstructor.
     * Two CmsElementDefinitionCollections can be merged using this constructor.
     * If a definition is defined in both source collections
     * the single parts of this definitions will be merged.
     * If a <em>part</em> of a definition is defined twice, the collection
     * primary will be preferred.
     * @param primary Source CmsElementDefinitionCollection
     * @param secondary Source CmsElementDefinitionCollection
     */
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

    /**
     * Add a definition to the collection.
     * @param def CmsElementDefinition that should be added
     */
    public void add(CmsElementDefinition def) {
        m_eldefs.put(def.getName(), def);
    }

    /**
     * Geta definition from the collection.
     * @param name Name of the element definition requested
     * @return CmsElementDefinition for <code>name</code> or <code>null</code>, if not defined.
     */
    public CmsElementDefinition get(String name) {
        return (CmsElementDefinition)m_eldefs.get(name);
    }

    /**
     * Get a string representation of this collection.
     * @return String representation.
     */
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