/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/I_CmsDumpTemplate.java,v $
 * Date   : $Date: 2000/02/15 17:44:00 $
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
 * Interface for OpenCms dump template classes.
 * <P>
 * All methods extending the functionality of the common
 * template class interface to the special behaviour
 * of dump template classes may be defined here.
 * <P>
 * Primarily, this interface is important for the launcher, 
 * NOT for the template engine.
 * The CmsDumpLauncher can launch all templates that
 * implement the I_CmsDumpTemplate interface. 
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/02/15 17:44:00 $
 */
public interface I_CmsDumpTemplate extends I_CmsTemplate {
}
