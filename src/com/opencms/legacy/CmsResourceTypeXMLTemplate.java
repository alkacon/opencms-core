/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/legacy/Attic/CmsResourceTypeXMLTemplate.java,v $
 * Date   : $Date: 2005/02/18 15:18:52 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  Alkacon Software (http://www.alkacon.com)
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

package com.opencms.legacy;

import org.opencms.file.types.A_CmsResourceType;

/**
 * Describes the resource type "XMLTemplate".
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.4 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsResourceTypeXMLTemplate extends A_CmsResourceType {

    /** The type id of this resource type. */
    public static final int C_RESOURCE_TYPE_ID = 4;
    
    /** The name of this resource type. */
    public static final String C_RESOURCE_TYPE_NAME = "XMLTemplate";
    
    /**
     * @see org.opencms.file.types.I_CmsResourceType#getTypeId()
     */
    public int getTypeId() {
        return C_RESOURCE_TYPE_ID;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getTypeName()
     */
    public String getTypeName() {
        return C_RESOURCE_TYPE_NAME;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {
        return CmsXmlTemplateLoader.C_RESOURCE_LOADER_ID;
    }
}