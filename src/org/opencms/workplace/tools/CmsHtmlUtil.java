/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/Attic/CmsHtmlUtil.java,v $
 * Date   : $Date: 2005/04/14 13:11:15 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools;


/**
 * This is an util class for html code related tasks.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.3 $
 * @since 5.7.3
 */
public final class CmsHtmlUtil {

    /**
     * provides only static methods.<p>
     */
    private CmsHtmlUtil() {

        // noop
    }

    /**
     * Returns an unique id for dhtml code, based on an element name.<p>
     * 
     * @param name the html element name
     * 
     * @return an dhtml id based on the name
     */
    public static String getId(String name) {

        StringBuffer id = new StringBuffer(name.length());
        name = name.toLowerCase();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                id.append(c);
            }
        }
        return id.toString();
    }
}