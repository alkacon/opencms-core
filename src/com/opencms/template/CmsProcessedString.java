/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsProcessedString.java,v $
* Date   : $Date: 2001/02/28 10:57:47 $
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

package com.opencms.template;

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
 * @author Alexander Lucas <alexander.lucas@framfab.de>
 * @version $Revision: 1.2 $ $Date: 2001/02/28 10:57:47 $
 */
public class CmsProcessedString {

    /** Store for the original String */
    String m_orgString;

    /** Constructor for a new CmsProcessedString object */
    public CmsProcessedString(String s) {
        m_orgString = s;
    }

    /** Constructor for a new CmsProcessedString object */
    public CmsProcessedString(byte[] b) {
        if(b == null) {
            m_orgString = null;
        } else {
            m_orgString = new String(b);
        }
    }

    /** Get back the original String */
    public String toString() {
        return m_orgString;
    }
}
