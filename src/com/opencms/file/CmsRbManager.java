/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRbManager.java,v $
* Date   : $Date: 2002/11/17 16:43:16 $
* Version: $Revision: 1.9 $
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

package com.opencms.file;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.*;
import com.opencms.util.*;
import java.util.*;
import source.org.apache.java.util.*;

/**
 * Initializes the configuret ResourceBroker and starts its init-method.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.9 $ $Date: 2002/11/17 16:43:16 $
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
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsRbManager] ressourcebroker-name: " + rbName);
        }
        
        // read the class-name of the rb from the properties
        rbClassName = (String)configurations.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATION_CLASS);
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsRbManager] ressourcebroker-classname: " + rbClassName);
        }

        try {
            // try to get the class
            rbClass = Class.forName(rbClassName);                          
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsRbManager] got rb-class");
            }
            
            // try to create a instance
            rbInstance = (I_CmsResourceBroker)rbClass.newInstance();
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsRbManager] created rb-instance");
            }
            
            // invoke the init-method of this rb
            rbInstance.init(configurations);
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsRbManager] initialized the rb-instance");
            }
            
            // return the configured resource-broker.
            return(rbInstance);
        } catch(Exception exc) {
            String message = "Critical error while loading resourcebroker";
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_INIT, "[CmsRbManager] " + message);
            }
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }
    }
}
