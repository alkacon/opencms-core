
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsResponseHttpServlet.java,v $
* Date   : $Date: 2001/07/10 16:05:47 $
* Version: $Revision: 1.20 $
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
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Implementation of the CmsResponse interface.
 *
 * This implementation uses a HttpServletResponse as original response to create a
 * CmsResponseHttpServlet.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.20 $ $Date: 2001/07/10 16:05:47 $
 */
public class CmsResponseHttpServlet implements I_CmsConstants, I_CmsResponse, I_CmsLogChannels {

    private static String C_LAST_MODIFIED = "Last-Modified";

    /**
     * The original response.
     */
    private HttpServletResponse m_res;

    /**
     * The original request.
     */
    private HttpServletRequest m_req;

    /**
     * The clusterurl.
     */
    private String m_clusterurl = null;

    /**
     * The type of this CmsResponset.
     */
    private int m_type = C_RESPONSE_HTTP;

    /** Remember, if a redirect was sent */
    private boolean m_redir = false;

    /** Buffer for the output stream */
    private OutputStream m_orgOutputStream = null;

    private static boolean jsdk2 = checkJsdk();

    /**
     * Check the JSDK version available at runtime.
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

        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            if(result) {
                // We have JSKD 2
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsResponseHttpServlet] JSDK 2 detected. ");
            } else {
                // We have JSKD 1
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsResponseHttpServlet] JSDK 1 detected. ");
            }
        }
        return result;
    }


    /**
     * Constructor, creates a new CmsResponseHttpServlet object.
     * It is nescessary to give the HttpServletRequest as well, because it is needed
     * to transform the CmsRedirect to a real Http redirect.
     *
     * @param req The original HttpServletRequest used to create this CmsRequest.
     * @param res The original HttpServletResponse used to create this CmsResponse.
     * @param clusterurl The clusterurl.
     */
    CmsResponseHttpServlet(HttpServletRequest req, HttpServletResponse res, String clusterurl) {
        m_res = res;
        m_req = req;
        m_clusterurl = clusterurl;
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
     * Returns the type of the response that was used to create the CmsResponse.
     * The returned int must be one of the constants defined above in this interface.
     *
     * @return The type of the CmsResponse.
     */
    public int getOriginalResponseType() {
        return m_type;
    }

    /**
     * Returns an OutputStream for writing the response data.
     *
     * @return OutputStream for writing data.
     * @exception Throws IOException if an error occurs.
     */
    public OutputStream getOutputStream() throws IOException {
        if(m_orgOutputStream == null) {
            m_orgOutputStream = m_res.getOutputStream();
        }
        return m_orgOutputStream;
    }

    /**
     * Check if the output stream was requested previously.
     * @return <code>true</code> if getOutputStream() was called, <code>false</code> otherwise.
     */
    public boolean isOutputWritten() {
        return m_orgOutputStream != null;
    }

    /**
     * Check if the current request was redirected. In this case, the
     * servlet must not write any bytes to the output stream.
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
     * @exception Throws IOException if an error occurs.
     */
    public void sendCmsRedirect(String location) throws IOException {
        String hostName;
        if((m_clusterurl == null) || (m_clusterurl.length() < 1)) {
            hostName = m_req.getScheme() + "://" + m_req.getHeader("HOST");
        }
        else {
            hostName = m_req.getScheme() + "://" + m_clusterurl;
        }
        m_redir = true;
        String servlet = m_req.getServletPath();
        String contextPath = "";
        try {
            contextPath = m_req.getContextPath();
        } catch(NoSuchMethodError err) {
            // ignore this error - old servlet-api
        }
        m_res.sendRedirect(hostName + contextPath + servlet + location);
    }

    /**
     * Sets the error code that is returnd by the response. The error code is specified
     * by a numeric value.
     *
     * @param code The error code to be set.
     * @exception Throws IOException if an error occurs.
     */
    public void sendError(int code) throws IOException {
        m_res.sendError(code);
    }

    /**
     * Sets the error code and a additional message that is returnd by the response.
     * The error code is specified by a numeric value.
     *
     * @param code The error code to be set.
     * @param msg Additional error message.
     * @exception Throws IOException if an error occurs.
     */
    public void sendError(int code, String msg) throws IOException {
        m_res.sendError(code, msg);
    }

    /**
     *  Helper function for a redirect to the cluster url.
     *  If <code>location</code> has the same hostname as the host of this servlet use the cluster url.
     *
     * @param location a full url, eg. http://servername/servlets/opencms/index.html
     * @exception Throws IOException if an error occurs.
     */
    public void sendRedirect(String location) throws IOException {
        String shortLocation = location;
        String hostName = m_req.getHeader("HOST");

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
        m_res.setContentLength(len);
    }

    /**
     * Sets the content type of the response to the specified type.
     *
     * @param type The contnent type of the response.
     */
    public void setContentType(String type) {
        m_res.setContentType(type);
    }

    /**
     * Sets a header-field in the response.
     *
     * @param key The key for the header.
     * @param value The value for the header.
     */
    public void setHeader(String key, String value) {
        //if(!m_res.containsHeader(key)) {
            m_res.setHeader(key, value);
        //}
    }

    /**
     * Add a header-field in the response.
     *
     * @param key The key for the header.
     * @param value The value for the header.
     */
    public void addHeader(String key, String value) {
        //if(!m_res.containsHeader(key)) {
        if(jsdk2) {
            m_res.addHeader(key, value);
        } else {
            m_res.setHeader(key, value);
        }
        //}
    }

    /**
     * Sets the last modified header-field in the response.
     *
     * @param time The last-modified time.
     */
    public void setLastModified(long time) {
        m_res.setDateHeader(C_LAST_MODIFIED, time);
    }

    /**
     * Checks, if the header was set already.
     * @param key, the header-key to check.
     * @return true, if the header was set before else false.
     */
    public boolean containsHeader(String key) {
        return m_res.containsHeader(key);
    }
}
