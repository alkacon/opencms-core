/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/staging/Attic/CmsElement.java,v $
* Date   : $Date: 2001/04/27 17:01:51 $
* Version: $Revision: 1.3 $
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
package com.opencms.staging;

import java.util.*;
import java.io.*;
import com.opencms.file.*;

/**
 * An instance of CmsElement represents an requestable Element in the OpenCms
 * staging-area. It contains all informations to generate the content of this
 * element. It also stores the variants of once generated content to speed up
 * performance.
 *
 * It points to other depending elements. Theses elements are called to generate
 * their content on generation-time.
 *
 * @author: Andreas Schouten
 */
public class CmsElement {

    /**
     * A Vector with definitions declared in this element.
     */
     private Vector m_elementDefinitions;

     /**
      * The default constructor for an element.
      */
     public CmsElement() {
        m_elementDefinitions = new Vector();
     }

     /**
      * A construcor which creates an element with the given element
      * definitions.
      * @param defs - a vector with ElementDefinitions for this element.
      */
     public CmsElement(Vector defs) {
        m_elementDefinitions = defs;
     }

    /**
     * Adds a single definition to this element.
     * @param def - the ElementDefinition to add.
     */
     public void addDefinition(CmsElementDefinition def) {
        m_elementDefinitions.add(def);
     }

     /**
      * Returns a Vector with all ElementDefinitions
      * @returns a Vector with all ElementDefinitions.
      */
     public Vector getAllDefinitions() {
        return m_elementDefinitions;
     }

     public byte[] getContent(CmsStaging staging, CmsObject cms, Hashtable parameters) {
        return null;
     }
}