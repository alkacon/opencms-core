/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/I_CmsListFormatter.java,v $
 * Date   : $Date: 2005/06/22 10:38:20 $
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
 
package org.opencms.workplace.list;

import java.util.Locale;

/**
 * Provides an interface for list data formatting.<p>
 * 
 * @author Michael Moossen 
 * @version $Revision: 1.3 $
 * @since 5.7.3
 */
public interface I_CmsListFormatter {

    /**
     * Returns a formatted string for the specified data.<p>
     * 
     * Take care of <code>null</code> data.<p>
     * 
     * @param data the data to format
     * @param locale the locale for formatting
     * 
     * @return formatted data
     */
    String format(Object data, Locale locale);

}
