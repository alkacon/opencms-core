package com.opencms.modules.cluster.comm;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;
import java.util.*;
import java.io.*;
import javax.servlet.http.*;
import java.lang.reflect.*;


public class CmsCommReceiver extends CmsXmlTemplate {

    private static final boolean C_DEBUG = false;

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

            try {
                if(C_DEBUG){
                    System.err.println("");
                    System.err.println("-------------------------------------------");
                    System.err.println("---   Communication module is started   ---");
                    System.err.println("    read container...");
                }
                // read the container from stream
                CmsCommandContainer container = readContainerFromRequest(cms);
                // login the user
                if(C_DEBUG){
                    System.err.println("    login user...");
                }
                cms.loginUser(container.getUsername(), container.getPassword());
                // invoke the methods specified
                if(C_DEBUG){
                    System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXX system info before invoke methods XXXXXXXXXXXXXXXXXXXXXXX");
                    printSystemInfo(cms);
                    System.err.println("    invoke methods...");
                }
                invokeMethods(cms, container);
                if(C_DEBUG){
                    System.err.println("    ...done.");
                    System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXX system info after invoke methods XXXXXXXXXXXXXXXXXXXXXXX");
                    printSystemInfo(cms);
                    System.err.println("--------------------------------------------");
                    System.err.println("");
                }
            } catch(Exception exc) {
                if(C_DEBUG){
                    System.err.println("    ... EXCEPTION:");
                    exc.printStackTrace();
                    System.err.println("");
                    System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXX system info after invoke methods XXXXXXXXXXXXXXXXXXXXXXX");
                    printSystemInfo(cms);
                    System.err.println("");
                    System.err.println("");
                }
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(A_OpenCms.C_MODULE_DEBUG,
                        " module com.opencms.modules.cluster.comm cant invoce methods."
                        +exc.getMessage());
                }
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
            try{
                if(C_DEBUG){
                    System.err.println("        invoke methode "+ ((CmsCommand) commands.get(i)).toString());
                }
                invokeMethod(cms, (CmsCommand) commands.get(i));
                if(C_DEBUG){
                    System.err.println("        ok.");
                }
            }catch(Exception e){
                if(C_DEBUG){
                    System.err.println("        ... EXCEPTION:");
                    e.printStackTrace();
                    System.err.println("");
                }
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(A_OpenCms.C_MODULE_DEBUG,
                        " module com.opencms.modules.cluster.comm cant invoke methode "
                        + ((CmsCommand) commands.get(i)).getMethodName()
                        +e.getMessage());
                }
            }
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
        toInvoke.invoke(cms, parameters);
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

    /**
     * private methode to print some system inforamtion. Just used for debug reasons.
     *
     * @param cms The cms object
     */
    private void printSystemInfo(CmsObject cms){
        try{
            System.err.println("");
            System.err.println("***********************************************************************");
            System.err.println("**                          SYSTEM INFO                              **");
            System.err.println("***********************************************************************");
            System.err.println("    online ElementCache:");
            Vector cacheInfo = cms.getOnlineElementCache().getCacheInfo();
            System.err.println("        uri: "+((Integer)cacheInfo.elementAt(1)).toString() + " | " +
                                                    ((Integer)cacheInfo.elementAt(0)).toString());
            System.err.println("        element: "+((Integer)cacheInfo.elementAt(1)).toString() + " | " +
                                                    ((Integer)cacheInfo.elementAt(0)).toString());
            System.err.println("");
            System.err.println("    cache:");
            System.err.println("        "+cms.getCacheInfo().toString());
                System.err.println("");
                System.err.println("The dynamic rulesets created by OpenCms:");
                System.err.println("----------------------------------------");
                System.err.println("");
                System.err.println("---- Online ----");
                if(CmsStaticExport.m_dynamicExportRulesOnline != null){
                    for(int i=0; i<CmsStaticExport.m_dynamicExportRulesOnline.size();System.err.println(CmsStaticExport.m_dynamicExportRulesOnline.elementAt(i++)));
                }
                System.err.println("");
                System.err.println("---- Extern ----");
                if(CmsStaticExport.m_dynamicExportRulesExtern != null){
                    for(int i=0; i<CmsStaticExport.m_dynamicExportRulesExtern.size();System.err.println(CmsStaticExport.m_dynamicExportRulesExtern.elementAt(i++)));
                }
                System.err.println("");
                System.err.println("---- nicename Online ----");
                if(CmsStaticExport.m_dynamicExportNameRules != null){
                    for(int i=0; i<CmsStaticExport.m_dynamicExportNameRules.size();System.err.println(CmsStaticExport.m_dynamicExportNameRules.elementAt(i++)));
                }
                System.err.println("");
                System.err.println("---- nicename  Extern----");
                if(CmsStaticExport.m_dynamicExportNameRulesExtern != null){
                    for(int i=0; i<CmsStaticExport.m_dynamicExportNameRulesExtern.size();System.err.println(CmsStaticExport.m_dynamicExportNameRulesExtern.elementAt(i++)));
                }
                System.err.println("");
            System.err.println("***********************************************************************");
            System.err.println("");
        }catch(Exception e){
            System.err.println(" Exception while printing Systeminfo.");
            e.printStackTrace();
        }
    }
}