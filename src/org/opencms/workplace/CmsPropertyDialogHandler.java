/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsPropertyDialogHandler.java,v $
 * Date   : $Date: 2003/08/19 12:04:41 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace;

import com.opencms.flex.jsp.CmsJspActionElement;

/**
 * Provides a method for selecting the default properties dialog.<p>
 * 
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1
 */
public class CmsPropertyDialogHandler implements I_CmsPropertyDialogHandler {

    /**
     * Default constructor.<p>
     */
    public CmsPropertyDialogHandler() { }
    
    /**
     * Returns the property dialog URI in the OpenCms VFS to the CmsPropertyDialogSelector class.<p>
     * 
     * @param resource the selected resource
     * @param jsp the CmsJspActionElement
     * @return the absolute path to the property dialog
     */
    public String getPropertyDialogUri(String resource, CmsJspActionElement jsp) {
        return CmsProperty.URI_PROPERTY_DIALOG;
    }

}
