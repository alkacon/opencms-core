/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementLink.java,v $
* Date   : $Date: 2004/07/08 15:21:14 $
* Version: $Revision: 1.6 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.template.cache;


/**
 * An instance of CmsElementLink is a link to another element. The link contains
 * only the name of the linked element. Which element is linked will be
 * determinded during the runtime of the content-creation (getContent-method).
 *
 * @author Andreas Schouten
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsElementLink {

    /**
     * The name of the element to link to.
     */
    String m_elementName;

    /**
     * Creates a new Element-Link to find out where to link to.
     * @param nameToLinkTo - the name of the element to link to.
     */
    public CmsElementLink(String nameToLinkTo) {
        m_elementName = nameToLinkTo;
    }

    /**
     * Returns the name of the element to link to.
     * @return the name of the element to link to.
     */
    public String toString() {
        return m_elementName;
    }

    /**
     * Returns the name of the element to link to.
     * @return the name of the element to link to.
     */
    public String getElementName() {
        return toString();
    }
}