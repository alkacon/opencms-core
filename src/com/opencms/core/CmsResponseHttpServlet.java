/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsResponseHttpServlet.java,v $
* Date   : $Date: 2003/07/18 12:44:46 $
* Version: $Revision: 1.30 $
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

package com.opencms.core;

import com.opencms.boot.I_CmsLogChannels;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementation of the I_CmsResponse interface which wraps a HttpServletResponse
 * and provides OpenCms with a facility to handle redirects.
 *
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.30 $ $Date: 2003/07/18 12:44:46 $
 */
public class CmsResponseHttpServlet implements I_CmsResponse {

    private static String C_LAST_MODIFIED = "Last-Modified";

    /** The original wrapped response. */
    private HttpServletResponse m_res;

    /** The original wrapped request. */
    private HttpServletRequest m_req;

    /** The type of this CmsResponset. */
    private int m_type = I_CmsConstants.C_RESPONSE_HTTP;

    /** Remember, if a redirect was sent */
    private boolean m_redir = false;

    /** Buffer for the output stream */
    private OutputStream m_orgOutputStream = null;

    /** Flag to indicate what JSDK is available */
    private static boolean jsdk2 = checkJsdk();

    /** String to save the content type */
    private String m_contentType = null;
    
    /** Debug flag */
    private static final boolean DEBUG = false;

    /**
     * Constructor, creates a new CmsResponseHttpServlet object.<p>
     *
     * @param req The original HttpServletRequest used to create this CmsRequest.
     * @param res The original HttpServletResponse used to create this CmsResponse.
     */
    CmsResponseHttpServlet(HttpServletRequest req, HttpServletResponse res) {
        m_res = res;
        m_req = req;
        // write OpenCms server identification in the response header
        m_res.setHeader("Server", "OpenCms/" + A_OpenCms.getVersionNumber());
    }
    
    /**
     * Check the JSDK version available at runtime.
     * 
     * @return <code>true</code> if JSDK 2, <code>false</code> if JSDK 1
     */
    public static boolean checkJsdk() {
        // Look for the method "addHeader". This method only is included in JSDK 2
        Class rc = HttpServletResponse.class;
        java.lang.reflect.Method m = null;
        try {
            m = rc.getMethod("addHeader", new Class[] {String.class, String.class});
        } catch(Exception e) {
            m = null;
        }

        // If m != null, the method could be found.
        boolean result = (m != null);

        if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
            if(result) {
                // We have JSDK 2
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Compatibility check  : JSDK 2 detected. ");
            } else {
                // We have JSDK 1
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Compatibility check  : JSDK 1 detected. ");
            }
        }
        return result;
    }

    /**
     * Returns the original response that was used to create the CmsResponse.
     *
     * @return The original response of the CmsResponse.
     */
    public Object getOriginalResponse() {
        return m_res;
    }

    /**
     * Returns the type of the response that was used to create the CmsResponse,
     * which will be a C_RESPONSE_HTTP value for this wrapper implementation.
     *
     * @return The type of the CmsResponse which is C_RESPONSE_HTTP
     */
    public int getOriginalResponseType() {
        return m_type;
    }

    /**
     * Returns an OutputStream for writing the response data.
     *
     * @return OutputStream for writing data.
     * @throws IOException if an error occurs
     */
    public OutputStream getOutputStream() throws IOException {
        if(m_orgOutputStream == null) {
            m_orgOutputStream = m_res.getOutputStream();
        }
        return m_orgOutputStream;
    }

    /**
     * Check if the output stream was written previously.
     * 
     * @return <code>true</code> if getOutputStream() was called, <code>false</code> otherwise.
     */
    public boolean isOutputWritten() {
        return m_orgOutputStream != null;
    }

    /**
     * Check if the current request was redirected. In this case, the
     * servlet must not write any bytes to the output stream.
     * 
     * @return <code>true</code> if the request is redirected, <code>false</code> otherwise.
     */

    public boolean isRedirected() {
        return m_redir;
    }

    /**
     * Sets a redirect to send the responst to.
     * The original HttpServletResponse redirect is used here. Additional information
     * about the servlets location is taken from the original HttpServletRequest.
     *
     * @param location The location the response is send to.
     * @param msg Additional error message.
     * @throws IOException if an error occurs
     */
    public void sendCmsRedirect(String location) throws IOException {
        if (DEBUG) System.err.println("CmsResponse.sendCmsRedirect(" + location + ")");          
        String hostName = m_req.getScheme() + "://" + m_req.getServerName() + ":" + m_req.getServerPort();
        m_redir = true;
        String servlet = m_req.getServletPath();
        String contextPath = "";
        try {
            contextPath = m_req.getContextPath();
        } catch(NoSuchMethodError err) {
            // ignore this error - old servlet-api
        }
        try {
            m_res.sendRedirect(hostName + contextPath + servlet + location);
        } catch(IOException exc) {
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsResponseHttpServlet] Couldn't redirect to: " + hostName + contextPath + servlet + location);
            }
        }
    }

    /**
     * Sets the error code that is returnd by the response. The error code is specified
     * by a numeric value.
     *
     * @param code The error code to be set.
     * @throws IOException if an error occurs
     */
    public void sendError(int code) throws IOException {
        if (DEBUG) System.err.println("CmsResponse.sendError(" + code + ")");           
        m_res.sendError(code);
    }

    /**
     * Sets the error code and a additional message that is returnd by the response.
     * The error code is specified by a numeric value.
     *
     * @param code The error code to be set.
     * @param msg Additional error message.
     * @throws IOException if an error occurs
     */
    public void sendError(int code, String msg) throws IOException {
        if (DEBUG) System.err.println("CmsResponse.sendError(" + code + "," + msg + ")");              
        m_res.sendError(code, msg);
    }

    /**
     *  Helper function for a redirect to the cluster url.
     *  If <code>location</code> has the same hostname as the host of this servlet use the cluster url.
     *
     * @param location a full url, eg. http://servername/servlets/opencms/index.html
     * @throws IOException if an error occurs
     */
    public void sendRedirect(String location) throws IOException {
        if (DEBUG) System.err.println("CmsResponse.sendRedirect(" + location + ")");        
        String shortLocation = location;
        String hostName = m_req.getServerName() + ":" + m_req.getServerPort();
        // remove 'http', '://', servername and '/servlets/opencms' and send CmsRedirect
        if(shortLocation.startsWith(m_req.getScheme())) {
            shortLocation = shortLocation.substring(m_req.getScheme().length());
        }
        if(shortLocation.startsWith("://")) {
            shortLocation = shortLocation.substring(3);
        }
        if(shortLocation.startsWith(hostName)) {
            shortLocation = shortLocation.substring(hostName.length());
            String contextPath = "";
            try {
                contextPath = m_req.getContextPath();
            } catch(NoSuchMethodError err) {
                // ignore this error - old servlet-api
            }
            if(shortLocation.startsWith(contextPath  + m_req.getServletPath())) {
                shortLocation = shortLocation.substring((contextPath + m_req.getServletPath()).length());
            }
            sendCmsRedirect(shortLocation);
        }
        else {

            // wanted to redirect on another site, don't use the cluster url
            m_res.sendRedirect(location);
        }
    }

    /**
     * Sets the length of the content being returned by the server.
     *
     * @param len Number of bytes to be returned by the response.
     */
    public void setContentLength(int len) {
        if (DEBUG) System.err.println("CmsResponse.setContentLength(" + len + ")");
        m_res.setContentLength(len);
    }

    /**
     * Sets the content type of the response to the specified type.
     *
     * @param type The contnent type of the response.
     */
    public void setContentType(String type) {        
        if (DEBUG) System.err.println("CmsResponse.setContentType(" + type + ")");     
        m_contentType = type;
        m_res.setContentType(type);
    }
    
    /**
     * Returns the content type of the response which has previously
     * been set using {@link #setContentType}.
     * 
     * @return the content type of the response.
     */
    public String getContentType() {
        if (DEBUG) System.err.println("CmsResponse.getContentType()");        
        return m_contentType;
    }    

    /**
     * Sets a header-field in the response.
     *
     * @param key The key for the header.
     * @param value The value for the header.
     */
    public void setHeader(String key, String value) {
        if (DEBUG) System.err.println("CmsResponse.setHeader(" + key + "," + value + ")");                
        m_res.setHeader(key, value);
    }

    /**
     * Add a header-field in the response.
     *
     * @param key The key for the header.
     * @param value The value for the header.
     */
    public void addHeader(String key, String value) {
        if (DEBUG) System.err.println("CmsResponse.addHeader(" + key + "," + value + ")");                
        if(jsdk2) {
            m_res.addHeader(key, value);
        } else {
            m_res.setHeader(key, value);
        }
    }

    /**
     * Sets the last modified header-field in the response.
     *
     * @param time The last-modified time.
     */
    public void setLastModified(long time) {
        if (DEBUG) System.err.println("CmsResponse.setLastModified(" + time + ")");                        
        m_res.setDateHeader(C_LAST_MODIFIED, time);
    }

    /**
     * Checks, if the header was set already.
     * @param key, the header-key to check.
     * @return true, if the header was set before else false.
     */
    public boolean containsHeader(String key) {
        if (DEBUG) System.err.println("CmsResponse.containsHeader(" + key + ")");          
        return m_res.containsHeader(key);
    }
}
