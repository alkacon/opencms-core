/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/A_CmsLauncher.java,v $
* Date   : $Date: 2003/07/14 13:28:23 $
* Version: $Revision: 1.44 $
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


package com.opencms.launcher;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsResponse;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRequestContext;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsRootTemplate;
import com.opencms.template.CmsTemplateCache;
import com.opencms.template.CmsTemplateClassManager;
import com.opencms.template.I_CmsTemplate;
import com.opencms.template.I_CmsTemplateCache;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

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
 * @version $Revision: 1.44 $ $Date: 2003/07/14 13:28:23 $
 */
abstract class A_CmsLauncher implements I_CmsLauncher {

    /** Debug flag */
    private static final boolean DEBUG = false;

    /** Value of the filesystem counter, when the last template clear cache was done */
    private static long m_lastFsCounterTemplate = 0;

    /** Value of the filesystem counter, when the last XML file clear cache was done */
    private static long m_lastFsCounterFile = 0;

    /** The template cache that holds all cached templates */
    protected static I_CmsTemplateCache m_templateCache = new CmsTemplateCache();

    /**
     * Utility method used by the launcher implementation to give control
     * to the CanonicalRoot.<p>
     * 
     * The CanonicalRoot will call the master template and return a byte array of the
     * generated output.<p>
     *
     * @param cms the cms context object
     * @param templateClass to generate the output of the master template
     * @param masterTemplate masterTemplate for the output
     * @param parameters contains all parameters for the template class
     * @return the generated output or null if there were errors
     * @throws CmsException if something goes wrong
     */
    protected byte[] callCanonicalRoot(CmsObject cms, I_CmsTemplate templateClass, CmsFile masterTemplate, Hashtable parameters) throws CmsException {
        try {
            com.opencms.template.CmsRootTemplate root = (CmsRootTemplate)CmsTemplateClassManager.getClassInstance(cms, "com.opencms.template.CmsRootTemplate");
            return root.getMasterTemplate(cms, templateClass, masterTemplate, m_templateCache, parameters);
        } catch (Exception e) {
            // no document we could show...
            handleException(cms, e, "Received error while calling canonical root for requested file " + masterTemplate.getResourceName() + ". ");
        }
        return null;
    }

    /**
     * Clears this launchers template cache.<p>
     */
    public void clearCache() {
        m_templateCache.clearCache();
        System.gc();
    }

    /**
     * Returns the name of the class in the form "[ClassName] ",
     * to be used for error logging purposes.<p>
     * 
     * @return name of this class
     */
    protected String getClassName() {
        String name = getClass().getName();
        return "[" + name.substring(name.lastIndexOf(".") + 1) + "] ";
    }

    /**
     * Returns the ID that indicates the type of the launcher.<p>
     *
     * @return the ID that indicates the type of the launcher
     */
    public abstract int getLauncherId();

    /**
     * Returns a reference to the global template cache.<p>
     * 
     * @return a reference to the global template cache
     */
    public static I_CmsTemplateCache getTemplateCache() {
        return m_templateCache;
    }

    /**
     * Calls the CmsClassManager to get an instance of the given template class.<p>
     * 
     * The returned object is checked to be an implementing class of the interface
     * I_CmsTemplate.
     * If the template cache of the template class is not yet set up, 
     * this will be done, too.<p>
     * 
     * @param cms the cms context object
     * @param classname name of the requested template class
     * @return instance of the template class
     * @throws CmsException if something goes wrong
     */
    protected I_CmsTemplate getTemplateClass(CmsObject cms, String classname) throws CmsException {
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() && DEBUG) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_DEBUG, getClassName() + "Getting start template class " + classname + ". ");
        }
        Object o = CmsTemplateClassManager.getClassInstance(cms, classname);

        // Check, if the loaded class really is a OpenCms template class.

        // This is done be checking the implemented interface.
        if (!(o instanceof I_CmsTemplate)) {
            String errorMessage = "Class " + classname + " is no OpenCms template class.";
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsTemplateClassManager] " + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_XML_NO_TEMPLATE_CLASS);
        }
        I_CmsTemplate cmsTemplate = (I_CmsTemplate)o;
        if (!cmsTemplate.isTemplateCacheSet()) {
            cmsTemplate.setTemplateCache(m_templateCache);
        }
        return cmsTemplate;
    }

    /**
     * Utility method to handle any occurence of an execption.<p>
     * 
     * If the Exception is NO CmsException (i.e. it was not detected previously)
     * it will be written to the logfile.<p>
     * 
     * If the current user is the anonymous user, no further exception will
     * be thrown, but a server error will be sent
     * (we want to prevent the user from seeing any exeptions).
     * Otherwise a new Exception will be thrown.
     * This will trigger the OpenCms error message box.<p>
     *
     * @param cms the cms context object
     * @param e Exception that should be handled
     * @param errorText error message that should be shown
     * @throws CmsException if 
     */
    public void handleException(CmsObject cms, Exception e, String errorText) throws CmsException {

        // Print out some error messages
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, getClassName() + errorText);
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, getClassName() + "--> Exception: " + com.opencms.util.Utils.getStackTrace(e));
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, getClassName() + "--> Cannot create output for this file. Must send error. Sorry.");
        }

        // if the user is "Guest", we send an servlet error,
        // otherwise we try to throw an exception.
        CmsRequestContext reqContext = cms.getRequestContext();
        if ((!DEBUG) && cms.anonymousUser().equals(reqContext.currentUser())) {
            throw new CmsException(errorText, CmsException.C_SERVICE_UNAVAILABLE, e);
        } else {
            if (e instanceof CmsException) {
                throw (CmsException)e;
            } else {
                throw new CmsException(errorText, CmsException.C_LAUNCH_ERROR, e);
            }
        }
    }

    /**
     * Start method called by the OpenCms system to show a resource.<p>
     * 
     * In this method initial values valid for all launchers can be set
     * and the _clearcache parameter is checked.
     * After this the abstract method launch(...) is called to
     * invoke the customized part of the launcher.<p>
     *
     * @param cms  the cms context object
     * @param file the selected resource to be shown
     * @param startTemplateClass name of the template class to start with
     * @param openCms an instance of A_OpenCms for redirects
     * @throws CmsException if something goes wrong
     */
    public void initlaunch(CmsObject cms, CmsFile file, String startTemplateClass, A_OpenCms openCms) throws CmsException {

        // first some debugging output.
        if (DEBUG && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, getClassName() + "Launcher started for " + file.getResourceName());
        }

        // check all values to be valid
        String errorMessage = null;
        if (file == null) {
            errorMessage = "Got \"null\" CmsFile object. :-(";
        }
        if (cms == null) {
            errorMessage = "Actual cms object missing";
        }
        if (errorMessage != null) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, getClassName() + errorMessage);
            } 
            throw new CmsException(errorMessage, CmsException.C_LAUNCH_ERROR);
        }

        // Check the clearcache parameter
        String clearcache = cms.getRequestContext().getRequest().getParameter("_clearcache");
        
        // Clear launcher caches if this is required
        clearLauncherCache(cms, 
            ((clearcache != null) && ("all".equals(clearcache) || "file".equals(clearcache))),
            ((clearcache != null) && ("all".equals(clearcache) || "template".equals(clearcache))));
        
        launch(cms, file, startTemplateClass, openCms);
    }
    
    /**
     * Compatibility method to ensure the legacy cache command line parameters
     * are still supported.<p>
     * 
     * @param cms an initialized CmsObject
     * @param clearClasses if true, CmsTemplateClassManager is cleared
     * @param clearFiles if true, A_CmsXmlContent cache is cleared
     * @param clearTemplates if true, internal template cache is cleared.
     */
    private static void clearLauncherCache(CmsObject cms, boolean clearFiles, boolean clearTemplates) {
        long currentFsCounter = cms.getFileSystemChanges();
        if(clearFiles || (currentFsCounter > m_lastFsCounterFile)) {
            A_CmsXmlContent.clearFileCache();
            m_lastFsCounterFile = currentFsCounter;
        }
        if(clearTemplates || (currentFsCounter > m_lastFsCounterTemplate)) {
            m_templateCache.clearCache();
            m_lastFsCounterTemplate = currentFsCounter;
        }        
    }

    /**
     * Clear the XML template cache that is maintained in the launcher.
     * To use this method, call it on one of the classes that extend 
     * A_CmsLauncher (e.g. com.opencms.launcher.CmsXmlLauncher.clearLauncherCache()).
     * @param cms an initialized CmsObject
     */  
    public static void clearLauncherCache(CmsObject cms) {
        clearLauncherCache(cms, true, true);
    }
    

    /**
     * Unitary method to start generating the output.
     * Every launcher has to implement this method.
     * In it possibly the selected file will be analyzed, and the
     * Canonical Root will be called with the appropriate
     * template class, template file and parameters. At least the
     * canonical root's output must be written to the HttpServletResponse.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param file CmsFile Object with the selected resource to be shown
     * @param startTemplateClass Name of the template class to start with.
     * @param openCms a instance of A_OpenCms for redirect-needs
     * @throws CmsException
     */
    protected abstract void launch(CmsObject cms, CmsFile file, String startTemplateClass, A_OpenCms openCms) throws CmsException;

    /**
     * Writes a given byte array to the HttpServletRespose output stream.
     * @param result byte array that should be written.
     * @param mimeType MIME type that should be set for the output.
     * @throws CmsException
     */
    protected void writeBytesToResponse(CmsObject cms, byte[] result) throws CmsException {
        try {
            I_CmsResponse resp = cms.getRequestContext().getResponse();
            if((!cms.getRequestContext().isStreaming()) && result != null && !resp.isRedirected()) {
                // Only write any output to the response output stream if
                // the current request is neither redirected nor streamed.
                OutputStream out = resp.getOutputStream();

                resp.setContentLength(result.length);
                resp.setHeader("Connection", "keep-alive");
                out.write(result);
                out.close();
            }
        }
        catch(IOException ioe) {
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_DEBUG) ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_DEBUG, getClassName() + "IO error while writing to response stream for " + cms.getRequestContext().getFileUri());
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_DEBUG, getClassName() + ioe);
            }
        }
        catch(Exception e) {
            String errorMessage = "Cannot write output to HTTP response stream";
            handleException(cms, e, errorMessage);
        }
    }

    /**
     * Sets the currently running OpenCms instance.
     */
    public void setOpenCms(A_OpenCms openCms) {
        // normally we don't need the instance - ignoring
        // if a launcher uses this, it should overload this method
    }
}
