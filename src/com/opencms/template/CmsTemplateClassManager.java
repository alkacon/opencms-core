package com.opencms.template;

import java.util.*;
import java.lang.reflect.*;
import com.opencms.file.*;
import com.opencms.core.*;

public class CmsTemplateClassManager implements I_CmsLogChannels { 
    private static Hashtable instanceCache = new Hashtable();

    public static Object getClassInstance(A_CmsObject cms, String classname)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException,
               InvocationTargetException {
        
        return getClassInstance(cms, classname, null);
    }

    
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
            repositories.addElement("/system/servlets/");
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
    
    public static void clearCache() {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_INFO, "[CmsClassManager] clearing class instance cache.");
        }
        instanceCache.clear();
    }
}
