/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsTemplateClassManager.java,v $
* Date   : $Date: 2003/07/31 13:19:37 $
* Version: $Revision: 1.31 $
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

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;

import java.lang.reflect.Constructor;

/**
 * Class for managing of the instances of all
 * OpenCms template classes.
 * <P>
 * This class provides methods for getting the instance of a
 * class. Once a instance of a template class is build, it is
 * be cached and re-used.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.31 $ $Date: 2003/07/31 13:19:37 $
 */
public class CmsTemplateClassManager {

    /**
     * Gets the instance of the class with the given classname.
     * If no instance exists a new one will be created using the default constructor.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param classname Name of the requested class.
     * @return Instance of the class with the given name.
     * @throws CmsException
     */
    public static Object getClassInstance(CmsObject cms, String classname) throws CmsException {
                
        return getClassInstance(cms, classname, null, null);
    }

    /**
     * Gets the instance of the class with the given classname.
     * If no instance exists a new one will be created using the given arguments.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param classname Name of the requested class.
     * @param callParameters Array of arguments that should be passed to the Constructor.
     * @return Instance of the class with the given name.
     * @throws CmsException
     */
    public static Object getClassInstance(CmsObject cms, String classname, Object[] callParameters) throws CmsException {
        int numParams = 0;
        if(callParameters != null) {
            numParams = callParameters.length;
        }
        Class[] parameterTypes = new Class[numParams];
        for(int i = 0;i < numParams;i++) {
            parameterTypes[i] = callParameters[i].getClass();
        }
        return getClassInstance(cms, classname, callParameters, parameterTypes);
    }

    /**
     * Gets the instance of the class with the given classname.
     * If no instance exists a new one will be created using the given arguments
     * interpreted as objects of the given classes.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param classname Name of the requested class.
     * @param callParameters Array of arguments that should be passed to the Constructor.
     * @param parameterTypes Array of the types of the arguments.
     * @return Instance of the class with the given name.
     * @throws CmsException
     */
    public static Object getClassInstance(CmsObject cms, String classname, Object[] callParameters, Class[] parameterTypes) throws CmsException {
        Object o = null;
        if(callParameters == null) {
            callParameters = new Object[0];
        }
        if(parameterTypes == null) {
            parameterTypes = new Class[0];
        }
        try {
            Class c = CmsTemplateClassManager.class.getClassLoader().loadClass(classname);
            // Now we have to look for the constructor
            Constructor con = c.getConstructor(parameterTypes);
            o = con.newInstance(callParameters);
        }catch(Exception e) {
            String errorMessage = null;

            // Construct error message for the different exceptions
            if(e instanceof ClassNotFoundException) {
                errorMessage = "Could not load template class " + classname + ". " + e.getMessage();
            }
            else {
                if(e instanceof InstantiationException) {
                    errorMessage = "Could not instantiate template class " + classname;
                }
                else {
                    if(e instanceof NoSuchMethodException) {
                        errorMessage = "Could not find constructor of template class " + classname;
                    }
                    else {
                        errorMessage = "Unknown error while getting instance of template class " + classname;
                    }
                }
            }
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsTemplateClassManager] " + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_CLASSLOADER_ERROR, e);
        }
        return o;
    }
}
