/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/A_CmsInit.java,v $
 * Date   : $Date: 2000/02/15 17:43:59 $
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

package com.opencms.file;

import com.opencms.core.*;

/**
 * The class which extends this abstract class initializes a network of 
 * resource-brokers and acces-modules. It helps the core to set up all layers
 * correctly.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.2 $ $Date: 2000/02/15 17:43:59 $
 */
public abstract class A_CmsInit {
	
	/**
	 * The init - Method creates a complete network of resource-borkers and 
	 * access-modules.
	 * 
	 * @param propertyDriver The driver-classname of the jdbc-driver.
	 * @param propertyConnectString the conectionstring to the database 
	 * for the propertys.
	 * 
	 * @return The resource-borker, this resource-broker has acces to the
	 * network of created classes.
	 */
	abstract public I_CmsResourceBroker init( String propertyDriver, 
											  String propertyConnectString )
		throws Exception;
}
