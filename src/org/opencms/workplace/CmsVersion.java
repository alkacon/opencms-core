/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsVersion.java,v $
 * Date   : $Date: 2004/06/21 11:45:41 $
 * Version: $Revision: 1.10 $
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
package org.opencms.workplace;

import org.opencms.file.CmsBackupResource;
import org.opencms.file.CmsObject;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

/**
 * Delivers a backup resource to the response.<p> 
 * 
 * The following Workplace file uses this class:
 * <ul>
 * <li>/shared/showversion.html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.10 $
 * 
 * @since 5.1
 */
public class CmsVersion {
    
    /** The name of the request parameter which stores the resource information. */
    private static String PARAM_VERSION = "_version";
    
    /** The HTTP servlet response. */
    private HttpServletResponse m_response;
    
    /** The CmsObject needed to read the resource and set the encoding. */
    private CmsObject m_cms;
    
    /** The name of the backup resource. */
    private String m_resourceName;
    
    /** The tag id of the backup resource. */
    private int m_version = -1;
    
    /**
     * Default constructor, use the init() method when using this constructor.<p>
     */
    public CmsVersion() {
        // empty
    }
    
    /**
     * Public constructor.<p>
     * 
     * @param context the page context
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     */
    public CmsVersion(PageContext context, HttpServletRequest request, HttpServletResponse response) {
        init(context, request, response);
    }
    
    /**
     * Initializes the needed member variables.<p>
     * 
     * @param context the page context
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     */
    public void init(PageContext context, HttpServletRequest request, HttpServletResponse response) {
        // get the necessary parameter and parse it
        String paramVersion = request.getParameter(PARAM_VERSION);
        if (paramVersion != null && !"".equals(paramVersion)) {
            int sepIndex = paramVersion.indexOf(":");
            if (sepIndex != -1) {
                m_resourceName = paramVersion.substring(0, sepIndex);
                String versionString = paramVersion.substring(sepIndex + 1);
                try {
                    m_version = Integer.parseInt(versionString);
                } catch (NumberFormatException nf) {
                    // ignore
                }
            }
        }
        m_response = response;
        CmsJspActionElement jsp = new CmsJspActionElement(context, request, response);
        m_cms = jsp.getCmsObject();
    }
    
    /**
     * Loads the backup resource and writes it in the response output stream.<p>
     */
    public void load() {
        if (m_resourceName == null || m_version == -1) {
            return;
        } else {
            // try to load the backup resource
            CmsBackupResource res = null;
            try {
                res = m_cms.readBackupFile(m_resourceName, m_version);
            } catch (CmsException e) { 
                return;
            }
            byte[] result = res.getContents();
            result = CmsEncoder.changeEncoding(result, OpenCms.getSystemInfo().getDefaultEncoding(), m_cms.getRequestContext().getEncoding());
            m_response.setContentType(OpenCms.getResourceManager().getMimeType(res.getName(), m_cms.getRequestContext().getEncoding()) + ":cms");
            m_response.setContentLength(result.length);
            try {
                m_response.getOutputStream().write(result);
                m_response.getOutputStream().flush();
            } catch (IOException e) {
                return;
            }
        }
    }

}