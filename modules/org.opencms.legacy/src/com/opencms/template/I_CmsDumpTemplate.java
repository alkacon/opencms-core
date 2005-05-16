/*
* File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.legacy/src/com/opencms/template/Attic/I_CmsDumpTemplate.java,v $
* Date   : $Date: 2005/05/16 17:45:08 $
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


/**
 * Interface for OpenCms dump template classes.
 * <P>
 * All methods extending the functionality of the common
 * template class interface to the special behaviour
 * of dump template classes may be defined here.
 * <P>
 * Primarily, this interface is important for the loader, 
 * NOT for the template engine.
 * The CmsDumpLoader can load all templates that
 * implement the I_CmsDumpTemplate interface. 
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2005/05/16 17:45:08 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public interface I_CmsDumpTemplate extends I_CmsTemplate {
    
}
