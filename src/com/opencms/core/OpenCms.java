
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCms.java,v $
* Date   : $Date: 2001/05/10 12:30:35 $
* Version: $Revision: 1.51 $
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

package com.opencms.core;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import javax.servlet.*;
import javax.servlet.http.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;
import com.opencms.file.*;
import com.opencms.launcher.*;
import com.opencms.template.cache.*;

/**
 * This class is the main class of the OpenCms system.
 * <p>
 * It is used to read a requested resource from the OpenCms System and forward it to
 * a launcher, which is performs the output of the requested resource. <br>
 *
 * The OpenCms class is independent of access module to the OpenCms (e.g. Servlet,
 * Command Shell), therefore this class is <b>not</b> responsible for user authentification.
 * This is done by the access module to the OpenCms.
 *
 * @author Michael Emmerich
 * @author Alexander Lucas
 * @version $Revision: 1.51 $ $Date: 2001/05/10 12:30:35 $
 *
 * */
public class OpenCms extends A_OpenCms implements I_CmsConstants,I_CmsLogChannels {

    /**
     * Definition of the index page
     */
    private static String C_INDEX = "index.html";

    /**
     * The default mimetype
     */

    //private static String C_DEFAULT_MIMETYPE="application/octet-stream";
    private static String C_DEFAULT_MIMETYPE = "text/html";

    /**
     * The resource-broker to access the database.
     */
    private static I_CmsResourceBroker c_rb;

    /**
     * The session storage for all active users.
     */
    private CmsCoreSession m_sessionStorage;

    /**
     * Reference to the OpenCms launcer manager
     */
    private CmsLauncherManager m_launcherManager;

    /**
     * Hashtable with all available Mimetypes.
     */
    private Hashtable m_mt = new Hashtable();

    /**
     * Indicates, if the session-failover should be enabled or not.
     */
    private boolean m_sessionFailover = false;

    /**
     * Indicates, if the streaming should be enabled by the configurations
     */
    private boolean m_streaming = true;

    /**
     * Indicates, if the element cache should be enabled by the configurations
     */
    private boolean m_enableElementCache = true;

    /**
     * Reference to the CmsElementCache object containing locators for all
     * URIs and elements in cache.
     */
    private static CmsElementCache c_elementCache = null;

    /**
     * Constructor, creates a new OpenCms object.
     *
     * It gets the configurations and inits a rb via the CmsRbManager.
     *
     * @param conf The configurations from the property-file.
     */
    OpenCms(Configurations conf) throws Exception {

        // invoke the ResourceBroker via the initalizer
        try {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] creating first cms-object");
            }
            CmsObject cms = new CmsObject();
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] initializing the main resource-broker");
            }
            m_sessionFailover = conf.getBoolean("sessionfailover.enabled", false);

            // init the rb via the manager with the configuration
            // and init the cms-object with the rb.
            c_rb = CmsRbManager.init(conf);
            printCopyrightInformation(cms);

            // initalize the Hashtable with all available mimetypes
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] read mime types");
            }
            m_mt = c_rb.readMimeTypes(null, null);
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] found "
                        + m_mt.size() + " mime-type entrys");
            }

            // Check, if the HTTP streaming should be enabled
            m_streaming = conf.getBoolean("httpstreaming.enabled", true);
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] HTTP streaming " + (m_streaming?"en":"dis") + "abled. ");
            }
        }
        catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] " + e.getMessage());
            }
            throw e;
        }

        // try to initialize the launchers.
        try {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] initialize launchers...");
            }
            m_launcherManager = new CmsLauncherManager();
        }
        catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] " + e.getMessage());
            }
        }

        // Check, if the element cache should be enabled
        m_enableElementCache = conf.getBoolean("elementcache.enabled", false);
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] element cache " + (m_enableElementCache?"en":"dis") + "abled. ");
            }
            if(m_enableElementCache) {
            try {
                c_elementCache = new CmsElementCache();
            }
            catch(Exception e) {
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] " + e.getMessage());
                }
            }
        }
    }

    /**
     * Destructor, called when the the servlet is shut down.
     * @exception CmsException Throws CmsException if something goes wrong.
     */
    public void destroy() throws CmsException {
        CmsObject cms = new CmsObject();
        cms.init(c_rb);
        cms.destroy();
    }

    /**
     * Insert the method's description here.
     * Creation date: (10/25/00 12:42:11)
     * @return com.opencms.launcher.CmsLauncherManager
     */
    public CmsLauncherManager getLauncherManager() {
        return m_launcherManager;
    }

    /**
     * This method gets the requested document from the OpenCms and returns it to the
     * calling module.
     *
     * @param cms The CmsObject containing all information about the requested document
     * and the requesting user.
     * @return CmsFile object.
     */
    CmsFile initResource(CmsObject cms) throws CmsException,IOException {
        CmsFile file = null;

        //check if the requested resource is a folder
        // if this is the case, redirect to the according index.html
        String resourceName = cms.getRequestContext().getUri();
        if(resourceName.endsWith("/")) {
            resourceName += C_INDEX;
            cms.getRequestContext().getResponse().sendCmsRedirect(resourceName);
        }
        try {

            //read the requested file
            file = cms.readFile(cms.getRequestContext().getUri());
        }
        catch(CmsException e) {
            if(e.getType() == CmsException.C_NOT_FOUND) {

                // there was no file found with this name.
                // it is possible that the requested resource was a folder, so try to access an
                // index.html there
                resourceName = cms.getRequestContext().getUri();

                // test if the requested file is already the index.html
                if(!resourceName.endsWith(C_INDEX)) {

                    // check if the requested file ends with an "/"
                    if(!resourceName.endsWith("/")) {
                        resourceName += "/";
                    }

                    //redirect the request to the index.html
                    resourceName += C_INDEX;
                    cms.getRequestContext().getResponse().sendCmsRedirect(resourceName);
                }
                else {

                    // throw the CmsException.
                    throw e;
                }
            }
            else {

                // throw the CmsException.
                throw e;
            }
        }
        if(file != null) {

            // test if this file is only available for internal access operations
            if((file.getAccessFlags() & C_ACCESS_INTERNAL_READ) > 0) {
                throw new CmsException(CmsException.C_EXTXT[CmsException.C_INTERNAL_FILE]
                        + cms.getRequestContext().getUri(), CmsException.C_INTERNAL_FILE);
            }
        }
        return file;
    }

    /**
     * Inits a new user and sets it into the overgiven cms-object.
     *
     * @param cms the cms-object to use.
     * @param cmsReq the cms-request for this http-request.
     * @param cmsRes the cms-response for this http-request.
     * @param user The name of the user to init.
     * @param group The name of the current group.
     * @param project The id of the current project.
     */
    public void initUser(CmsObject cms, I_CmsRequest cmsReq, I_CmsResponse cmsRes, String user,
            String group, int project) throws CmsException {
        cms.init(c_rb, cmsReq, cmsRes, user, group, project, m_streaming, c_elementCache);
    }

    /**
     * Prints a copyright information to all log-files.
     */
    private void printCopyrightInformation(CmsObject cms) {
        System.err.println(cms.version());
        if(isLogging()) {
            this.log(C_OPENCMS_INFO, cms.version());
        }
        String copy[] = cms.copyright();
        for(int i = 0;i < copy.length;i++) {
            System.err.println(copy[i]);
            if(isLogging()) {
                this.log(C_OPENCMS_INFO, copy[i]);
            }
        }
    }

    /**
     * This method loads old sessiondata from the database. It is used
     * for sessionfailover.
     *
     * @param oldSessionId the id of the old session.
     * @return the old sessiondata.
     */
    Hashtable restoreSession(String oldSessionId) throws CmsException {

        // is session-failopver enabled?
        if(m_sessionFailover) {

            // yes
            return c_rb.restoreSession(oldSessionId);
        }
        else {

            // no - do nothing
            return null;
        }
    }

    /**
     * Sets the mimetype of the response.<br>
     * The mimetype is selected by the file extension of the requested document.
     * If no available mimetype is found, it is set to the default
     * "application/octet-stream".
     *
     * @param cms The actual OpenCms object.
     * @param file The requested document.
     *
     */
    void setResponse(CmsObject cms, CmsFile file) {
        String ext = null;
        String mimetype = null;
        int lastDot = file.getName().lastIndexOf(".");

        // check if there was a file extension
        if((lastDot > 0) && (!file.getName().endsWith("."))) {
            ext = file.getName().substring(lastDot + 1, file.getName().length());
            mimetype = (String)m_mt.get(ext);

            // was there a mimetype fo this extension?
            if(mimetype != null) {
                cms.getRequestContext().getResponse().setContentType(mimetype);
            }
            else {
                cms.getRequestContext().getResponse().setContentType(C_DEFAULT_MIMETYPE);
            }
        }
        else {
            cms.getRequestContext().getResponse().setContentType(C_DEFAULT_MIMETYPE);
        }
    }

    /**
     * Selects the appropriate launcher for a given file by analyzing the
     * file's launcher id and calls the initlaunch() method to initiate the
     * generating of the output.
     *
     * @param cms CmsObject containing all document and user information
     * @param file CmsFile object representing the selected file.
     * @exception CmsException
     */
    public void showResource(CmsObject cms, CmsFile file) throws CmsException {
        int launcherId = file.getLauncherType();
        String startTemplateClass = file.getLauncherClassname();
        I_CmsLauncher launcher = m_launcherManager.getLauncher(launcherId);
        if(launcher == null) {
            String errorMessage = "Could not launch file " + file.getName() + ". Launcher for requested launcher ID "
                    + launcherId + " could not be found.";
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_INFO, "[OpenCms] " + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
        }
        cms.setLauncherManager(m_launcherManager);
        launcher.initlaunch(cms, file, startTemplateClass, this);
    }

    /**
     * This method stores sessiondata into the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session.
     * @param isNew determines, if the session is new or not.
     * @return data the sessionData.
     */
    void storeSession(String sessionId, Hashtable sessionData) throws CmsException {

        // is session failover enabled?
        if(m_sessionFailover) {

            // yes
            c_rb.storeSession(sessionId, sessionData);
        }
    }

    /**
     * Returns the registry to read values from it. You don't have the right to write
     * values. This is useful for modules, to read module-parameters.
     *
     * @return the registry to READ values from it.
     * @exception Throws CmsException, if the registry can not be returned.
     */
    public static I_CmsRegistry getRegistry() throws CmsException {
        return c_rb.getRegistry(null, null, null);
    }
}
