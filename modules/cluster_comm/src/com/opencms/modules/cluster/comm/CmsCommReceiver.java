package com.opencms.modules.cluster.comm;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;
import java.util.*;
import java.io.*;
import javax.servlet.http.*;
import java.lang.reflect.*;


public class CmsCommReceiver extends CmsXmlTemplate {

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

            try {
                // read the container from stream
                CmsCommandContainer container = readContainerFromRequest(cms);
                // login the user
                cms.loginUser(container.getUsername(), container.getPassword());
                // invoke the methods specified
                invokeMethods(cms, container);
            } catch(Exception exc) {
                // DEBUG ONLY:
                exc.printStackTrace();
                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
            }

            // nothing to return!
            return null;
    }

    protected CmsCommandContainer readContainerFromRequest(CmsObject cms)
        throws IOException, ClassNotFoundException  {
        HttpServletRequest req = (HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest();
        InputStream in = req.getInputStream();
        ObjectInputStream oin = new ObjectInputStream(in);
        Object obj = oin.readObject();
        oin.close();
        return (CmsCommandContainer) obj;
    }

    protected void invokeMethods(CmsObject cms, CmsCommandContainer container)
        throws CmsException, InvocationTargetException, IllegalAccessException , NoSuchMethodException {
        ArrayList commands = container.getCommands();
        for(int i = 0; i < commands.size(); i++) {
            invokeMethod(cms, (CmsCommand) commands.get(i));
        }
    }

    protected void invokeMethod(CmsObject cms, CmsCommand command)
        throws CmsException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object[] parameters = command.getParameters();
        Class[] classes = new Class[parameters.length];
        for(int i = 0; i < parameters.length; i++) {
            classes[i] = parameters[i].getClass();
        }

        Method toInvoke = cms.getClass().getMethod(command.getMethodName(), classes);
        System.err.println(toInvoke.invoke(cms, parameters));
    }

    /**
     * gets the caching information from the current template class.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        // no caching!
        return new CmsCacheDirectives(false);
    }


}