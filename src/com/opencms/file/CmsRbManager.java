package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRbManager.java,v $
 * Date   : $Date: 2000/08/08 14:08:23 $
 * Version: $Revision: 1.3 $
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

import com.opencms.core.*;
import com.opencms.util.*;
import java.util.*;
import source.org.apache.java.util.*;

/**
 * Initializes the configuret ResourceBroker and starts its init-method.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 2000/08/08 14:08:23 $
 */
public class CmsRbManager implements I_CmsLogChannels, I_CmsConstants {

	/**
	 * Reads the needed configurations from the property-file and instanciates the
	 * needed resource-broker to access the cms-resources.
	 * 
	 * @param configurations The configurations from the propertyfile.
	 * @return I_CmsResourceBroker the instanciated resourcebroker.
	 * @exception CmsException if the resource-broker couldn't be instanciated.
	 */
	public static final I_CmsResourceBroker init(Configurations configurations) 
		throws CmsException {
		
		String rbName = null;
		String rbClassName = null;
		Class rbClass = null;
		I_CmsResourceBroker rbInstance = null;
		
		// read the name of the rb from the properties
		rbName = (String)configurations.getString(C_CONFIGURATION_RESOURCEBROKER);
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsRbManager] ressourcebroker-name: " + rbName);
		}
		
		// read the class-name of the rb from the properties
		rbClassName = (String)configurations.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATION_CLASS);
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsRbManager] ressourcebroker-classname: " + rbClassName);
		}

		try {
			// try to get the class
			rbClass = Class.forName(rbClassName);                          
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsRbManager] got rb-class");
			}
			
			// try to create a instance
			rbInstance = (I_CmsResourceBroker)rbClass.newInstance();
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsRbManager] created rb-instance");
			}
			
			// invoke the init-method of this rb
			rbInstance.init(configurations);
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsRbManager] initialized the rb-instance");
			}
			
			// return the configured resource-broker.
			return(rbInstance);
		} catch(Exception exc) {
			String message = "Critical error while loading resourcebroker: " + Utils.getStackTrace(exc);
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INIT, "[CmsRbManager] " + message);
			}
			throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
		}
	}
}
