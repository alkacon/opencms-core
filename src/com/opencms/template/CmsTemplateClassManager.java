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
 * @version $Revision: 1.2 $ $Date: 2000/01/14 15:45:21 $
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
     * @param cms A_CmsObject Object for accessing system resources.
     * @param classname Name of the requested class.
     * @return Instance of the class with the given name.
     * @exception ClassNotFoundException
     * @exception InstantiationException
     * @exception IllegalAccessException
     * @exception NoSuchMethodException
     * @exception InvocationTargetException
     */
    public static Object getClassInstance(A_CmsObject cms, String classname)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException,
               InvocationTargetException {
        
        return getClassInstance(cms, classname, null);
    }
    
    /**
     * Gets the instance of the class with the given classname.
     * If no instance exists a new one will be created using the given arguments.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param classname Name of the requested class.
     * @param callParameters Array of arguments that should be passed to the Constructor.
     * @return Instance of the class with the given name.
     * @exception ClassNotFoundException
     * @exception InstantiationException
     * @exception IllegalAccessException
     * @exception NoSuchMethodException
     * @exception InvocationTargetException
     */
    public static Object getClassInstance(A_CmsObject cms, String classname, Object[] callParameters)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException,
               InvocationTargetException {
        
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
     * @param cms A_CmsObject Object for accessing system resources.
     * @param classname Name of the requested class.
     * @param callParameters Array of arguments that should be passed to the Constructor.
     * @param parameterTypes Array of the types of the arguments.
     * @return Instance of the class with the given name.
     * @exception ClassNotFoundException
     * @exception InstantiationException
     * @exception IllegalAccessException
     * @exception NoSuchMethodException
     * @exception InvocationTargetException
     */
    public static Object getClassInstance(A_CmsObject cms, String classname, Object[] callParameters, Class[] parameterTypes)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException,
               InvocationTargetException {
        
        Object o = null;
        if(callParameters == null) {
            callParameters = new Object[0];
        }
        
        if(parameterTypes == null) {
            parameterTypes = new Class[0];
        }
        
        if(!instanceCache.containsKey(classname)) {
            Vector repositories = new Vector();
            //repositories.addElement("/system/servlets/");
            repositories.addElement("/");

            CmsClassLoader loader = new CmsClassLoader(cms, repositories, null);
            Class c = loader.loadClass(classname);        
            
            // Now we have to look for the constructor
            Constructor con = c.getConstructor(parameterTypes); 
            Object o2 = con.newInstance(callParameters);
            instanceCache.put(classname, o2);
        }
        
        o = instanceCache.get(classname);
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
