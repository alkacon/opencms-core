package com.opencms.launcher;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;

import java.util.*;
import java.io.*;

import javax.servlet.http.*;

/**
 * Abstract OpenCms launcher class.
 * <P>
 * This class implements basic functionality for all OpenCms launchers.
 * For each relevant file type (e.g. XML control files, plain text files,
 * JavaScript files,...) a customized launcher has to be implemented.
 * <P>
 * Every extending class has to implement the abstract methods
 * <UL>
 * <LI>getLauncherId() to indicate the type of the launcher</LI>
 * <LI>launch() to be called by initlaunch</LI>
 * </UL>
 * <P>
 * The functionality of this class is 
 * <UL>
 * <LI>provide a global cache for template class results</LI>
 * <LI>receive the system's launcher call, do some relevant initial
 * things and call the launch() method</LI>
 * <LI>provide some utility methods</LI>
 * </UL>
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/01/14 13:46:51 $
 */
abstract class A_CmsLauncher implements I_CmsLauncher, I_CmsLogChannels {
        
 	/** The template cache that holds all cached templates */
	protected static I_CmsTemplateCache m_templateCache = new CmsTemplateCache();

    /** Default constructor to create a new launcher */
    public A_CmsLauncher() {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "Initialized successfully.");
        }
    }

    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */
	public abstract int getLauncherId();    

    /**
     * Gets a reference to the global template cache
     * @return Template cache
     */
    public static I_CmsTemplateCache getTemplateCache() {
        return m_templateCache;
    }
    
    /**
     * Start method called by the OpenCms system to show a resource.
     * <P>
     * In this method initial values valid for all launchers can be set
     * and the _clearcache parameter is checked.
     * After this the abstract method launch(...) is called to
     * invoke the customized part of the launcher.
     * 
	 * @param cms A_CmsObject Object for accessing system resources
	 * @param file CmsFile Object with the selected resource to be shown
     * @exception CmsException
     */
    public void initlaunch(A_CmsObject cms, CmsFile file) throws CmsException {
        // Check the clearcache parameter
        
        String clearcache = cms.getRequestContext().getRequest().getParameter("_clearcache");
        
        if(clearcache != null) {
            if("all".equals(clearcache) || "class".equals(clearcache)) {
                CmsTemplateClassManager.clearCache();
            }
            
            if("all".equals(clearcache) || "template".equals(clearcache)) {
                m_templateCache.clearCache();
            }
        }

        launch(cms, file);
    }

    /**
 	 * Unitary method to start generating the output.
 	 * Every launcher has to implement this method.
 	 * In it possibly the selected file will be analyzed, and the
 	 * Canonical Root will be called with the appropriate 
 	 * template class, template file and parameters. At least the 
 	 * canonical root's output must be written to the HttpServletResponse.
 	 * 
	 * @param cms A_CmsObject Object for accessing system resources
	 * @param file CmsFile Object with the selected resource to be shown
     * @exception CmsException
	 */	
	protected abstract void launch(A_CmsObject cms, CmsFile file) throws CmsException;
    
	/**
	 * Utility method used by the launcher implementation to give control
	 * to the CanonicalRoot.
	 * The CanonicalRoot will call the master template and return a byte array of the 
	 * generated output.
	 * 
	 * @param cms A_CmsObject Object for accessing system resources.
	 * @param templateClass Class that should generate the output of the master template.
	 * @param masterTemplate CmsFile Object with masterTemplate for the output.
	 * @param parameters Hashtable with all parameters for the template class.
     * @return byte array with the generated output or null if there were errors.
     * @exception CmsException
	 * 
	 */
	protected byte[] callCanonicalRoot(A_CmsObject cms, I_CmsTemplate templateClass, CmsFile masterTemplate, Hashtable parameters) throws CmsException {
        try {
            com.opencms.template.CmsRootTemplate root = (CmsRootTemplate)CmsTemplateClassManager.getClassInstance(cms, "com.opencms.template.CmsRootTemplate");
            return root.getMasterTemplate(cms, templateClass, masterTemplate, m_templateCache, parameters);
        } catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[A_CmsLauncher] cannot create root template.");
            }            
            // There is no document we could show.
            handleException(cms, e, "Cannot creat root template.");
        }
        return null;
    }	

    /**
     * Utility method to handle any occurence of an execption.
     * <P>
     * If the Exception is NO CmsException (i.e. it was not detected previously)
     * it will be written to the logfile.
     * <P>
     * If the current user is the anonymous user, no further execption will
     * be thrown, but a server error will be sent
     * (we want to prevent the user from seeing any exeptions).
     * Otherwise a new Exception will be thrown.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param e Exception that should be handled.
     * @param errorText Error message that should be shown.
     * @exception CmsException
     */
    public void handleException(A_CmsObject cms, Exception e, String errorText) throws CmsException {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorText);
            if(!(e instanceof CmsException)) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + e);
            }
            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "Cannot create output. Must send error. Sorry.");
        }        
        // If the user is "Guest", we send an servlet error.
        // Otherwise we try to throw an exception.
        A_CmsRequestContext reqContext = cms.getRequestContext();
        
        if(cms.anonymousUser().equals(reqContext.currentUser())) {
            throw new CmsException(CmsException.C_SERVICE_UNAVAILABLE);
        } else {                        
            if(e instanceof CmsException) {
                throw (CmsException)e;
            } else {
                throw new CmsException(errorText, e);
            }
        }
    }
    
    /**
     * Writes a given byte array to the HttpServletRespose output stream.
     * @param result byte array that should be written.
     * @param mimeType MIME type that should be set for the output.
     * @exception CmsException
     */
    protected void writeBytesToResponse(A_CmsObject cms, byte[] result, String mimeType) 
            throws CmsException {
        try {
            I_CmsResponse resp = cms.getRequestContext().getResponse();
           // resp.setContentType(mimeType);
            OutputStream out = resp.getOutputStream();
            out.write(result);
            out.flush();
            out.close();
        } catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[A_CmsLauncher] cannot write output to HTTP response.");
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[A_CmsLauncher] " + e);
                throw new CmsException("Cannot write output to HTTP response in A_CmsLauncher", e);
            }
        }
    }
    
    /**
     * Calls the CmsClassManager to get an instance of the given template class.
     * The returned Object is checked to be an implementing class of the interface
     * I_CmsTemplate.
     * If the template cache of the template class is not yet setted, this will
     * be done, too.
     * @param cms A_CmsObject object for accessing system resources.
     * @param classname Name of the requested template class.
     * @return Instance of the template class.
     * @exception CmsException.
     */
    protected Object getTemplateClass(A_CmsObject cms, String classname) throws CmsException {
        Object loadedTemplateClass = null;
   
        try {
            loadedTemplateClass = CmsTemplateClassManager.getClassInstance(cms, classname);
        } catch(ClassNotFoundException e) {
            System.err.println("Class " + classname + " could not be loaded!");
            throw new CmsException("Could not load template class " + classname);
        } catch(Exception e) {
            System.err.println("Class " + classname + " could not be instantiated!");
            throw new CmsException("Could not instantiate class " + classname + ". Original Exception: " + e);
        }

        if(! (loadedTemplateClass instanceof I_CmsTemplate)) {
            System.err.println("Class " + classname + " is no OpenCms template class.");
            throw new CmsException("Cannot launch class " + classname + ". This is no OpenCms template class.");
        }

        I_CmsTemplate cmsTemplate = (I_CmsTemplate)loadedTemplateClass;
        
        if(!cmsTemplate.isTemplateCacheSet()) {
            cmsTemplate.setTemplateCache(m_templateCache);
        }
        
        return loadedTemplateClass;        
    }

    /**
     * Gets the name of the class in the form "[ClassName] "
     * This can be used for error logging purposes.
     * @return name of this class
     */
    protected String getClassName() {
        String name = getClass().getName();
        return "[" + name.substring(name.lastIndexOf(".") + 1) + "] ";
    }
}
