/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsJspLoader.java,v $
 * Date   : $Date: 2004/02/20 12:45:54 $
 * Version: $Revision: 1.39 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.loader;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexCache;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexRequest;
import org.opencms.flex.CmsFlexResponse;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ExtendedProperties;

/**
 * The JSP loader which enables the execution of JSP in OpenCms.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.39 $
 * @since FLEX alpha 1
 * 
 * @see I_CmsResourceLoader
 */
public class CmsJspLoader implements I_CmsResourceLoader {
    
    /** Encoding to write JSP files to disk (<code>ISO-8859-1</code>) */
    public static final String C_DEFAULT_JSP_ENCODING = "ISO-8859-1";

    /** Special JSP directive tag start (<code>%&gt;</code>)*/
    public static final String C_DIRECTIVE_END = "%>";

    /** Special JSP directive tag start (<code>&lt;%@</code>)*/
    public static final String C_DIRECTIVE_START = "<%@";
    
    /** Extension for JSP managed by OpenCms (<code>.jsp</code>) */
    public static final String C_JSP_EXTENSION = ".jsp";      
    
    /** Name of "error pages are commited or not" runtime property*/ 
    public static final String C_LOADER_ERRORPAGECOMMIT = "flex.jsp.errorpagecommit";

    /** The id of this loader */
    public static final int C_RESOURCE_LOADER_ID = 6;
    
    /** Flag for debugging output. Set to 9 for maximum verbosity. */ 
    private static final int DEBUG = 0;
    
    /** The CmsFlexCache used to store generated cache entries in */
    private static CmsFlexCache m_cache;
    
    /** Flag to indicate if error pages are mared a "commited" */
    // TODO: This is a hack, investigate this issue with different runtime environments
    private static boolean m_errorPagesAreNotCommited = false; // should work for Tomcat 4.1
    
    /** The directory to store the generated JSP pages in (absolute path) */
    private static String m_jspRepository = null;
    
    /** The directory to store the generated JSP pages in (relative path in web application */
    private static String m_jspWebAppRepository = null;
        
    /**
     * The constructor of the class is empty, the initial instance will be 
     * created by the loader manager upon startup of OpenCms.<p>
     * 
     * @see org.opencms.loader.CmsLoaderManager
     */
    public CmsJspLoader() {
        // NOOP
    }
    
    /**
     * Translates the JSP file name for a OpenCms VFS resourcn 
     * to the name used in the "real" file system.<p>
     * 
     * The name given must be a absolute URI in the OpenCms VFS,
     * e.g. CmsFile.getAbsolutePath()
     *
     * @param name The file to calculate the JSP name for
     * @return The JSP name for the file
     */    
    public static String getJspName(String name) {
        return name + C_JSP_EXTENSION;
    }
    
    /**
     * Returns the absolute path in the "real" file system for a given JSP.
     *
     * @param name The name of the JSP file 
     * @param online Flag to check if this is request is online or not
     * @return The full path to the JSP
     */
    public static String getJspPath(String name, boolean online) {
        return m_jspRepository + (online?"online":"offline") + name;
    }

    /**
     * Returns the absolute path in the "real" file system for the JSP repository
     * toplevel directory.
     *
     * @return The full path to the JSP repository
     */
    public static String getJspRepository() {        
        return m_jspRepository;
    } 
    
    /**
     * Returns the uri for a given JSP in the "real" file system, 
     * i.e. the path in the file
     * system relative to the web application directory.
     *
     * @param name The name of the JSP file 
     * @param online Flag to check if this is request is online or not
     * @return The full uri to the JSP
     */
    public static String getJspUri(String name, boolean online) {
        StringBuffer result = new StringBuffer(64);
        result.append(m_jspWebAppRepository);
        result.append(online?"/online":"/offline");
        result.append(getJspName(name));
        return result.toString();
    }
    
    /** Destroy this ResourceLoder, this is a NOOP so far.  */
    public void destroy() {
        // NOOP
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#dump(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String, java.util.Locale, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] dump(CmsObject cms, CmsResource file, String element, Locale locale, HttpServletRequest req, HttpServletResponse res) 
    throws ServletException, IOException {
        
        // access the Flex controller
        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        CmsFlexController oldController = null; 
        if (controller != null) {
            // for dumping we must create an new "top level" controller, save the old one to be restored later
            oldController = controller;
        }
        
        byte[] result = null;        
        try {               
            // create new request / response wrappers and crontoller
            controller = new CmsFlexController(cms, file, m_cache, req, res);
            req.setAttribute(CmsFlexController.ATTRIBUTE_NAME, controller);
            CmsFlexRequest f_req = new CmsFlexRequest(req, controller);
            CmsFlexResponse f_res = new CmsFlexResponse(res, controller, false, false);
            controller.pushRequest(f_req);
            controller.pushResponse(f_res);
    
            // now include the target
            try {
                f_req.getRequestDispatcher(cms.readAbsolutePath(file)).include(f_req, f_res);
            } catch (java.net.SocketException e) {
                // uncritical, might happen if client (browser) did not wait until end of page delivery
            }
    
            if (!f_res.isSuspended()) {
                if (!res.isCommitted() || m_errorPagesAreNotCommited) {
                    // if a JSP errorpage was triggered the response will be already committed here
                    result = f_res.getWriterBytes();
                    result = CmsEncoder.changeEncoding(result, OpenCms.getSystemInfo().getDefaultEncoding(), cms.getRequestContext().getEncoding());
                }
            }
            // remove temporary controller
            req.removeAttribute(CmsFlexController.ATTRIBUTE_NAME);
        } finally {
            if (oldController != null) {
                // reset saved controller
                req.setAttribute(CmsFlexController.ATTRIBUTE_NAME, oldController);
            }
        }
        
        return result;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.io.OutputStream, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void export(CmsObject cms, CmsResource resource, OutputStream exportStream, HttpServletRequest req, HttpServletResponse res) 
    throws ServletException, IOException, CmsException {
        
        CmsFile file = CmsFile.upgrade(resource, cms);
        
        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);

        CmsFlexRequest f_req;
        CmsFlexResponse f_res;

        if (controller != null) {
            // re-use currently wrapped request / response
            f_req = controller.getCurrentRequest();
            f_res = controller.getCurrentResponse();
        } else {
            // create new request / response wrappers
            controller = new CmsFlexController(cms, file, m_cache, req, res);
            req.setAttribute(CmsFlexController.ATTRIBUTE_NAME, controller);
            f_req = new CmsFlexRequest(req, controller);
            f_res = new CmsFlexResponse(res, controller, false, true);
            controller.pushRequest(f_req);
            controller.pushResponse(f_res);
        }
        
        byte[] result = null;

        try {
            f_req.getRequestDispatcher(cms.readAbsolutePath(file)).include(f_req, f_res);                                    
        } catch (java.net.SocketException e) {
            // uncritical, might happen if client (browser) did not wait until end of page delivery
        }
        
        if (!f_res.isSuspended()) {
            if (!res.isCommitted() || m_errorPagesAreNotCommited) {
                // if a JSP errorpage was triggered the response will be already committed here
                result = f_res.getWriterBytes();
                result = CmsEncoder.changeEncoding(result, OpenCms.getSystemInfo().getDefaultEncoding(), cms.getRequestContext().getEncoding());                
                // process headers and write output                                          
                res.setContentLength(result.length);
                CmsFlexResponse.processHeaders(f_res.getHeaders(), res);
                res.getOutputStream().write(result);
                res.getOutputStream().flush();                
            }
        }
        
        result = null;
        CmsFlexController.removeController(req);
        
        if (exportStream != null) {
            exportStream.write(file.getContents());
        }
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {
        return C_RESOURCE_LOADER_ID;
    }   
    
    /**
     * Return a String describing the ResourceLoader,
     * which is <code>"The OpenCms default resource loader for JSP"</code>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {
        return "The OpenCms default resource loader for JSP";
    }
    
    /** 
     * Initialize the ResourceLoader,
     * here the configuration for the JSP repository (directories used) is set.
     *
     * @param configuration the OpenCms configuration 
     */
    public void init(ExtendedProperties configuration) {
        m_jspRepository = OpenCms.getSystemInfo().getWebInfPath();
        if (m_jspRepository.indexOf("WEB-INF") >= 0) {
            // Should always be true, just make sure we don't generate an exception in untested environments
            m_jspRepository = m_jspRepository.substring(0, m_jspRepository.indexOf("WEB-INF")-1);
        }
        m_jspWebAppRepository = configuration.getString("flex.jsp.repository", "/WEB-INF/jsp");
        m_jspRepository += m_jspWebAppRepository.replace('/', File.separatorChar);
        if (!m_jspRepository.endsWith(File.separator)) {
            m_jspRepository += File.separator;
        }
        if (DEBUG > 0) {
            System.err.println("JspLoader: Setting jsp repository to " + m_jspRepository);
        }
        // Get the cache from the runtime properties
        m_cache = (CmsFlexCache)OpenCms.getRuntimeProperty(CmsFlexCache.C_LOADER_CACHENAME);
        // Get the export URL from the runtime properties
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) { 
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". JSP Loader           : JSP repository (absolute path): " + m_jspRepository);        
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". JSP Loader           : JSP repository (web application path): " + m_jspWebAppRepository);              
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Loader init          : " + this.getClass().getName() + " initialized");   
        }
        // Get the "error pages are commited or not" flag from the runtime properties
        Boolean errorPagesAreNotCommited = (Boolean)OpenCms.getRuntimeProperty(C_LOADER_ERRORPAGECOMMIT);
        if (errorPagesAreNotCommited != null) {
            m_errorPagesAreNotCommited = errorPagesAreNotCommited.booleanValue();
        }
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#load(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void load(CmsObject cms, CmsResource file, HttpServletRequest req, HttpServletResponse res) 
    throws ServletException, IOException, CmsException {

        // load and process the JSP         
        long timer1;
        if (DEBUG > 0) {
            timer1 = System.currentTimeMillis();
            System.err.println("========== JspLoader loading: " + cms.readAbsolutePath(file));
            System.err.println("JspLoader.load()  cms uri is: " + cms.getRequestContext().getUri());
        }

        boolean streaming = false;
        boolean bypass = false;
        String stream;
        
        try {
            // read caching property from requested VFS resource                                     
            stream = cms.readProperty(cms.readAbsolutePath(file), I_CmsResourceLoader.C_LOADER_STREAMPROPERTY);
        } catch (CmsException e) {
            throw new CmsLoaderException("Error while loading stream properties for " + cms.readAbsolutePath(file), e);
        }            
        if (stream != null) {
            if ("yes".equalsIgnoreCase(stream) || "true".equalsIgnoreCase(stream)) {
                // streaming not allowed in export mode
                streaming = true;
            } else if ("bypass".equalsIgnoreCase(stream) || "bypasscache".equalsIgnoreCase(stream)) {
                // bypass not allowed in export mode
                bypass = true;
            }
        }

        if (DEBUG > 1) {
            System.err.println("========== JspLoader stream=" + streaming + " bypass=" + bypass);
        }

        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);

        CmsFlexRequest f_req;
        CmsFlexResponse f_res;

        if (controller != null) {
            // re-use currently wrapped request / response
            f_req = controller.getCurrentRequest();
            f_res = controller.getCurrentResponse();
        } else {
            // create new request / response wrappers
            controller = new CmsFlexController(cms, file, m_cache, req, res);
            req.setAttribute(CmsFlexController.ATTRIBUTE_NAME, controller);
            f_req = new CmsFlexRequest(req, controller);
            f_res = new CmsFlexResponse(res, controller, streaming, true);
            controller.pushRequest(f_req);
            controller.pushResponse(f_res);
        }

        if (bypass) {
            // Bypass Flex cache for this page (this solves some compatibility issues in BEA Weblogic)        
            if (DEBUG > 1) {
                System.err.println("JspLoader.load() bypassing cache for file " + cms.readAbsolutePath(file));
            }
            // Update the JSP first if neccessary            
            String target = updateJsp(cms, file, f_req, controller, new HashSet(11));
            // Dispatch to external JSP
            req.getRequestDispatcher(target).forward(f_req, res);
            if (DEBUG > 1) {
                System.err.println("JspLoader.load() cache was bypassed!");
            }
        } else {
            // Flex cache not bypassed            
            try {
                f_req.getRequestDispatcher(cms.readAbsolutePath(file)).include(f_req, f_res);
            } catch (java.net.SocketException e) {
                // uncritical, might happen if client (browser) does not wait until end of page delivery
                OpenCms.getLog(this).debug("Ignoring SocketException" + e);
            }
            if (!streaming && !f_res.isSuspended()) {
                try {
                    if (!res.isCommitted() || m_errorPagesAreNotCommited) {
                        // If a JSP errorpage was triggered the response will be already committed here
                        byte[] result = f_res.getWriterBytes();

                        // Encoding project:  
                        // The byte array will internally be encoded in the OpenCms 
                        // default encoding. In case another encoding is set 
                        // in the 'content-encoding' property of the file, 
                        // we need to re-encode the output here. 
                        result = CmsEncoder.changeEncoding(result, OpenCms.getSystemInfo().getDefaultEncoding(), cms.getRequestContext().getEncoding());

                        // Process headers and write output                                          
                        res.setContentLength(result.length);
                        CmsFlexResponse.processHeaders(f_res.getHeaders(), res);
                        res.getOutputStream().write(result);
                        res.getOutputStream().flush();
                        
                        result = null;
                    }
                } catch (IllegalStateException e) {
                    // uncritical, might happen if JSP error page was used
                    OpenCms.getLog(this).debug("Ignoring IllegalStateException" + e);
                } catch (java.net.SocketException e) {
                    // uncritical, might happen if client (browser) does not wait until end of page delivery
                    OpenCms.getLog(this).debug("Ignoring SocketException", e);
                }
            }
        }

        if (DEBUG > 0) {
            long timer2 = System.currentTimeMillis() - timer1;
            System.err.println("========== JspLoader time delivering JSP for " + cms.readAbsolutePath(file) + ": " + timer2 + "ms");
        }

        CmsFlexController.removeController(req);
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource resource, ServletRequest req, ServletResponse res) throws ServletException, IOException {
        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        // Get JSP target name on "real" file system
        String target = updateJsp(cms, resource, req, controller, new HashSet(11));
        // Important: Indicate that all output must be buffered
        controller.getCurrentResponse().setOnlyBuffering(true);
        // Dispatch to external file
        controller.getCurrentRequest().getRequestDispatcherToExternal(cms.readAbsolutePath(resource), target).include(req, res);
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportEnabled()
     */
    public boolean isStaticExportEnabled() {
        return true;
    }    
    
    /**
     * Updates a JSP page in the "real" file system in case the VFS resource has changed.<p>
     * 
     * Also processes the <code>&lt;%@ cms %&gt;</code> tags before the JSP is written to the real FS.
     * Also recursivly updates all files that are referenced by a <code>&lt;%@ cms %&gt;</code> tag 
     * on this page to make sure the file actually exists in the real FS. 
     * All <code>&lt;%@ include %&gt;</code> tags are parsed and the name in the tag is translated
     * from the OpenCms VFS path to the path in the real FS. 
     * The same is done for filenames in <code>&lt;%@ page errorPage=... %&gt;</code> tags.
     * 
     * @param cms Used to access the OpenCms VFS
     * @param file The reqested JSP file resource in the VFS
     * @param req The current request
     * @param controller the controller for the JSP integration
     * @param updates A Set containing all JSP pages that have been already updated
     * 
     * @return The file name of the updated JSP in the "real" FS
     * 
     * @throws ServletException might be thrown in the process of including the JSP 
     * @throws IOException might be thrown in the process of including the JSP 
     */
    private synchronized String updateJsp(CmsObject cms, CmsResource file, ServletRequest req, CmsFlexController controller, Set updates) 
    throws IOException, ServletException {
        
        String jspTargetName = getJspName(cms.readAbsolutePath(file));

        // check for inclusion loops
        if (updates.contains(jspTargetName)) {
            return null;
        }
        updates.add(jspTargetName);

        String jspPath = getJspPath(jspTargetName, controller.getCurrentRequest().isOnline());
        
        File d = new File(jspPath).getParentFile();   
        if ((d == null) || (d.exists() && ! (d.isDirectory() && d.canRead()))) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Could not access directory for " + jspPath);
            }
            throw new ServletException("JspLoader: Could not access directory for " + jspPath);
        }   
         
        if (! d.exists()) {
            // create directory structure
            d.mkdirs();    
        }
                
        boolean mustUpdate = false;
        
        File f = new File(jspPath);        
        if (!f.exists()) {
            // File does not exist in FS
            mustUpdate = true;            
        } else if (f.lastModified() <= file.getDateLastModified()) {
            // File in FS is older then file in VFS
            mustUpdate = true;
        } else if (controller.getCurrentRequest().isDoRecompile()) {
            // Recompile is forced with parameter
            mustUpdate = true;
        }

        String jspfilename = getJspUri(cms.readAbsolutePath(file), controller.getCurrentRequest().isOnline());               
        
        if (mustUpdate) {
            if (DEBUG > 2) {
                System.err.println("JspLoader writing new file: " + jspfilename);
            }
            byte[] contents = null;
            String jspEncoding = null;
            try {
                contents = cms.readFile(cms.readAbsolutePath(file)).getContents();
                // Encoding project:
                // Check the JSP "content-encoding" property
                jspEncoding = cms.readProperty(cms.readAbsolutePath(file), I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, false);
                if (jspEncoding == null) {
                    jspEncoding = C_DEFAULT_JSP_ENCODING;
                }
                jspEncoding = jspEncoding.trim().toUpperCase();
            } catch (CmsException e) {
                throw new ServletException("JspLoader: Could not read contents for file '" + cms.readAbsolutePath(file) + "'", e);
            }
            
            try {
                FileOutputStream fs = new FileOutputStream(f);                
                // Encoding project:
                // We need to use some encoding to convert bytes to String
                // corectly. Internally a JSP will always be stored in the 
                // system default encoding since they are just a variation of
                // the "plain" resource type.
                String page = new String(contents, OpenCms.getSystemInfo().getDefaultEncoding());
                StringBuffer buf = new StringBuffer(contents.length);

                int p0 = 0, i2 = 0, slen = C_DIRECTIVE_START.length(), elen = C_DIRECTIVE_END.length();
                // Check if any jsp name references occur in the file
                int i1 = page.indexOf(C_DIRECTIVE_START);
                while (i1 >= 0) {
                    // Parse the file and replace jsp name references 
                    i2 = page.indexOf(C_DIRECTIVE_END, i1 + slen);
                    if (i2 > i1) {
                        String directive = page.substring(i1 + slen, i2);
                        if (DEBUG > 2) {
                            System.err.println("JspLoader: Detected " + C_DIRECTIVE_START + directive + C_DIRECTIVE_END);
                        }

                        int t1=0, t2=0, t3=0, t4=0, t5=0, t6=slen, t7=0;
                        while (directive.charAt(t1) == ' ') {
                            t1++;
                        }
                        String filename = null;                        
                        if (directive.startsWith("include", t1)) {            
                            if (DEBUG > 2) {
                                System.err.println("JspLoader: Detected 'include' directive!");
                            }
                            t2 = directive.indexOf("file", t1 + 7);
                            t5 = 6;
                        } else if (directive.startsWith("page", t1)) {
                            if (DEBUG > 2) {
                                System.err.println("JspLoader: Detected 'page' directive!");
                            }
                            t2 = directive.indexOf("errorPage", t1 + 4);
                            t5 = 11;
                        } else if (directive.startsWith("cms", t1)) {
                            if (DEBUG > 2) {
                                System.err.println("JspLoader: Detected 'cms' directive!");
                            }
                            t2 = directive.indexOf("file", t1 + 3);
                            t5 = 4; t6 = 0; t7 = elen; 
                        }
                        
                        if (t2 > 0) {
                            String sub = directive.substring(t2 + t5); 
                            char c1 = sub.charAt(t3);
                            while ((c1 == ' ') || (c1 == '=') || (c1 == '"')) {
                                c1 = sub.charAt(++t3);
                            }
                            t4 = t3;
                            while (c1 != '"') {
                                c1 = sub.charAt(++t4);
                            }
                            if (t4 > t3) {
                                filename=sub.substring(t3, t4);
                            }
                            if (DEBUG > 2) {
                                System.err.println("JspLoader: File given in directive is: " + filename);
                            }
                        }
                        
                        if (filename != null) {
                            // a file was found, changes have to be made
                            String pre = ((t7 == 0)?directive.substring(0, t2+t3+t5):"");
                            String suf = ((t7 == 0)?directive.substring(t2+t3+t5+filename.length()):"");
                            // Now try to update the referenced file 
                            String absolute = CmsLinkManager.getAbsoluteUri(filename, controller.getCurrentRequest().getElementUri());
                            if (DEBUG > 2) {
                                System.err.println("JspLoader: Absolute location=" + absolute);
                            }
                            String jspname = null;
                            try {
                                // Make sure the jsp referenced file is generated
                                CmsResource jsp = cms.readFileHeader(absolute);
                                updateJsp(cms, jsp, req, controller, updates);
                                jspname = getJspUri(cms.readAbsolutePath(jsp), controller.getCurrentRequest().isOnline());
                            } catch (Exception e) {
                                jspname = null;
                                if (DEBUG > 2) {
                                    System.err.println("JspLoader: Error while creating jsp file " + absolute + "\n" + e);
                                }
                            }
                            if (jspname != null) {
                                // Only change something in case no error had occured
                                if (DEBUG > 2) {
                                    System.err.println("JspLoader: Name of jsp file is " + jspname);
                                }
                                directive = pre + jspname + suf;
                                if (DEBUG > 2) {
                                    System.err.println("JspLoader: Changed directive to " + C_DIRECTIVE_START + directive + C_DIRECTIVE_END);
                                }
                            }
                        }
                        
                        buf.append(page.substring(p0, i1 + t6));
                        buf.append(directive);
                        p0 = i2 + t7;
                        i1 = page.indexOf(C_DIRECTIVE_START, p0);
                    }
                }                  
                if (i2 > 0) {
                    buf.append(page.substring(p0, page.length()));
                    // Encoding project:
                    // Now we are ready to store String data in file system.
                    // To convert String to bytes we also need to provide
                    // some encoding. The default (by the JSP standard) encoding 
                    // for JSP is ISO-8859-1.
                    contents = buf.toString().getBytes(jspEncoding);
                } else {
                    // Encoding project:
                    // Contents of original file where not modified,
                    // just translate to the required JSP encoding (if necessary)
                    contents = CmsEncoder.changeEncoding(contents, OpenCms.getSystemInfo().getDefaultEncoding(), jspEncoding);   
                }                                         
                fs.write(contents);                
                fs.close();
                contents = null;
                
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info("Updated JSP file \"" + jspfilename + "\" for resource \"" + cms.readAbsolutePath(file) + "\"");
                }
            } catch (FileNotFoundException e) {
                throw new ServletException("JspLoader: Could not write to file '" + f.getName() + "'\n" + e, e);
            }
        }                      
        return jspfilename;
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isUsableForTemplates()
     */
    public boolean isUsableForTemplates() {
        return true;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isUsingUriWhenLoadingTemplate()
     */
    public boolean isUsingUriWhenLoadingTemplate() {
        return false;
    }      
}
