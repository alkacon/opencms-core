/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/StringMapper.java,v $
 * Date   : $Date: 2004/12/09 15:59:46 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.util;

/**
 * Abstract class used to pass a string mapping function for string substitution.<p>
 */
public abstract class StringMapper implements I_CmsStringMapper {
    
    /**
     * Method to map a key to a string value.<p>
     * 
     * @param key th e key to map
     * @return the mapped value or <code>null</code>
     */
    public abstract String getValue(String key);
}