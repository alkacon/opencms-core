/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsTemplateClassManager.java,v $
 * Date   : $Date: 2000/07/17 07:48:33 $
 * Version: $Revision: 1.10 $
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

import java.util.*;
import java.lang.reflect.*;
import com.opencms.file.*;
import com.opencms.core.*;

/**
 * Class for managing of the instances of all
 * OpenCms template classes.
 * <P>
 * This class provides methods for getting the instance of a
 * class. Once a instance of a template class is build, it is 
 * be cached and re-used. 
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.10 $ $Date: 2000/07/17 07:48:33 $
 */
public class CmsTemplateClassManager implements I_CmsLogChannels { 
    
    /**
     * Hashtable for caching the template class
     */
    private static Hashtable instanceCache = new Hashtable();

    /**
     * Gets the instance of the class with the given classname.
     * If no instance exists a new one will be created using the default constructor.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param classname Name of the requested class.
     * @return Instance of the class with the given name.
     * @exception CmsException 
     */
    public static Object getClassInstance(CmsObject cms, String classname)
            throws CmsException {
        
        return getClassInstance(cms, classname, null);
    }
    
    /**
     * Gets the instance of the class with the given classname.
     * If no instance exists a new one will be created using the given arguments.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param classname Name of the requested class.
     * @param callParameters Array of arguments that should be passed to the Constructor.
     * @return Instance of the class with the given name.
     * @exception CmsException 
     */
    public static Object getClassInstance(CmsObject cms, String classname, Object[] callParameters)
            throws CmsException {
        
        int numParams = 0;
        if(callParameters != null) {
            numParams = callParameters.length;
        }

        Class[] parameterTypes = new Class[numParams];
        for(int i=0; i<numParams; i++) {
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
     * @exception CmsException 
     */
    public static Object getClassInstance(CmsObject cms, String classname, Object[] callParameters, Class[] parameterTypes)
            throws CmsException {
        
        Object o = null;
        if(callParameters == null) {
            callParameters = new Object[0];
        }
        
        if(parameterTypes == null) {
            parameterTypes = new Class[0];
        }
        
        if(instanceCache.containsKey(classname)) {
            o = instanceCache.get(classname);
        } else {
            Vector repositories = new Vector();
			String[] repositoriesFromConfigFile = null;
			repositoriesFromConfigFile = cms.getConfigurations().getStringArray("repositories");
			
			for (int i=0; i < repositoriesFromConfigFile.length; i++)
				repositories.addElement(repositoriesFromConfigFile[i]);

            try {
                CmsClassLoader loader = new CmsClassLoader(cms, repositories, null);
                Class c = loader.loadClass(classname);        
            
                // Now we have to look for the constructor
                Constructor con = c.getConstructor(parameterTypes); 
                o = con.newInstance(callParameters);
            } catch(Exception e) {
                String errorMessage = null;
                
                // Construct error message for the different exceptions
                if(e instanceof ClassNotFoundException) {                    
                    errorMessage = "Could not load template class " + classname + ". " + e.getMessage();
                } else if(e instanceof InstantiationException) {
                    errorMessage = "Could not instantiate template class " + classname;
                } else if(e instanceof NoSuchMethodException) {
                    errorMessage = "Could not find constructor of template class " + classname;
                } else {
                    errorMessage = "Unknown error while getting instance of template class " + classname;
                }

                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsTemplateClassManager] " + errorMessage);
                }
                throw new CmsException(errorMessage, CmsException.C_CLASSLOADER_ERROR, e);
            }
                                                
            instanceCache.put(classname, o);
        }
        
        return o;        
    }
    
    /**
     * Clears the cache for template class instances.
     */
      public static void clearCache() {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_INFO, "[CmsClassManager] clearing class instance cache.");
        }
        instanceCache.clear();
    }
}
