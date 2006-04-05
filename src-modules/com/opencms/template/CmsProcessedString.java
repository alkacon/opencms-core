/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/template/Attic/CmsProcessedString.java,v $
* Date   : $Date: 2005/05/17 13:47:32 $
* Version: $Revision: 1.1 $
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

package com.opencms.template;

import java.io.UnsupportedEncodingException;

/**
 * Special class for String results returned in template engine.
 * Template objects should use this class instead of <code>String</code>
 * if they have written their results to the response output stream
 * theirselves (if the system is in streaming mode).
 * <P>
 * If an object doesn't care about HTTP streaming and simply generates
 * a String for returning to the template engine, it really shouldn't
 * make use of this class and return the original String.
 *
 * @author Alexander Lucas 
 * @version $Revision: 1.1 $ $Date: 2005/05/17 13:47:32 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsProcessedString {

    /** Store for the original String. */
    String m_orgString;

    /** 
     * Constructor for a new CmsProcessedString object.<p>
     * 
     * @param s the original String 
     */
    public CmsProcessedString(String s) {
        m_orgString = s;
    }

    /** 
     * Constructor for a new CmsProcessedString object.<p>
     * 
     * @param b the byte array to create the String from
     * @param encoding the encoding of the byte array
     */
    public CmsProcessedString(byte[] b, String encoding) {
        if (b == null) {
            m_orgString = null;
        } else {
            try {
                m_orgString = new String(b, encoding);
            } catch (UnsupportedEncodingException uee) {
                m_orgString = new String(b);
            }
        }
    }

    /** 
     * Get back the original String.<p> 
     * 
     * @return the original String 
     */
    public String toString() {
        return m_orgString;
    }
}
