/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/staging/Attic/CmsUri.java,v $
* Date   : $Date: 2001/04/27 15:21:48 $
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
 * An instance of CmsUri represents an requestable ressource in the OpenCms
 * staging-area. It points to the starting element and handles the access-
 * checks to this ressource in a simple way.
 *
 * If access is granted for the current user it starts the startingElement to
 * process the content of this ressource.
 *
 * @author: Andreas Schouten
 */
public class CmsUri {

    /**
     * The name of the group that can read this ressource,
     */
    private String m_readAccessGroup;

    /**
     * The Key to the Element used to start the contentgeneration for
     * this Uri.
     */
    private CmsElementDescriptor m_startingElement;

    /**
     * A Vector with Element Definitions. For normal URI's this Vector contains
     * only a definition for the body-element.
     */
    private Vector m_elementDefinitions = new Vector();

    /**
     * Constructor.
     *
     * @param startingElement the Element to start the contentgenerating for this uri.
     * @param readAccessGroup the Group that can read the uri.
     * @param def a content-definition for the an element (normaly body).
     */
    public CmsUri(CmsElementDescriptor startingElement, String readAccessGroup,
        CmsElementDefinition def){
        this(startingElement, readAccessGroup, new Vector());
        m_elementDefinitions.add(def);
    }

    /**
     * Constructor.
     *
     * @param startingElement the Element to start the contentgenerating for this uri.
     * @param readAccessGroup the Group that can read the uri.
     * @param definitions a vector of definitions for elements.
     */
    public CmsUri(CmsElementDescriptor startingElement, String readAccessGroup,
        Vector definitions) {
        m_startingElement = startingElement;
        m_readAccessGroup = readAccessGroup;
        m_elementDefinitions = definitions;
    }
}