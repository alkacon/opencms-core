/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/staging/Attic/CmsElementVariant.java,v $
* Date   : $Date: 2001/04/30 15:19:36 $
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
package com.opencms.staging;

import java.util.*;
import java.io.*;
import com.opencms.file.*;

/**
 * An instance of CmsElementVariant stores a single cached variant for an
 * element. This is the generated output (content) of an element. This cache
 * stores all generated strings of this element and all links to other elements.
 *
 * @author: Andreas Schouten
 */
public class CmsElementVariant {

    /**
     * The content of this variant. In this vector object of type String
     * and of CmsElementLink can be stored.
     */
    Vector m_content;

    /**
     * Creates a new empty variant for an element.
     */
    public CmsElementVariant() {
        m_content = new Vector();
    }

    /**
     * Adds static content to this variant.
     * @param staticContent - part of the variant. A peace static content of
     * type string.
     */
    public void add(String staticContent) {
        m_content.add(staticContent);
    }

    /**
     * Adds static content to this variant.
     * @param staticContent - part of the variant. A peace static content of
     * type byte-array.
     */
    public void add(byte[] staticContent) {
        m_content.add(staticContent);
    }

    /**
     * Adds an element-link to this variant.
     * @param elementLink - part of the variant. A link to another element.
     */
    public void add(CmsElementLink elementLink) {
        m_content.add(elementLink);
    }

    /**
     * Returns a peace of this variant. It can be of the type String, byte[] or
     * CmsElementLink.
     * @param i - the index to the vector of variant-pieces.
     */
    public Object get(int i) {
        return m_content.get(i);
    }
}