/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsStaticExportManager.java,v $
 * Date   : $Date: 2003/08/12 19:41:02 $
 * Version: $Revision: 1.3 $
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

package org.opencms.staticexport;

import org.opencms.loader.CmsDumpLoader;
import org.opencms.loader.CmsJspLoader;
import org.opencms.loader.CmsPageLoader;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.security.CmsSecurityException;

import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides the functionaility to export resources from the OpenCms VFS
 * to the file system.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsStaticExportManager {
    
    /** Marker for externally redirected 404 uri's */
    public static final String C_EXPORT_MARKER = "exporturi";
    
    /** Name for the folder default index file */
    public static final String C_EXPORT_DEFAULT_FILE = "index.html";
    
    /** The initialized cms context */
    private CmsObject m_cms;
    
    /** The uri to export in the rfs */
    private String m_rfsName;      
    
    /** The uri in the vfs */
    private String m_vfsName;
    
    /** The resource to export */
    private CmsResource m_resource;
    
    /**
     * Generates a new static export manager.<p>
     * 
     * @param request the current request
     * @param cms the current cms context object
     */
    public CmsStaticExportManager(HttpServletRequest request, CmsObject cms) {        
        if (! A_OpenCms.getStaticExportProperties().isStaticExportEnabled()) {
            return;
        }
        
        m_rfsName = request.getParameter(C_EXPORT_MARKER);
        if ((m_rfsName == null) || !m_rfsName.startsWith(A_OpenCms.getStaticExportProperties().getRfsPrefix())) {
            // this is not an export request, no further processing is required
            return;
        }
        
        // check if export uri exists in the OpenCms VFS
        m_rfsName = m_rfsName.substring(A_OpenCms.getStaticExportProperties().getRfsPrefix().length());
        
        try {
            m_resource = cms.readFileHeader(m_rfsName);
            if (m_resource.isFolder() && !m_rfsName.endsWith("/")) {
                m_rfsName += "/";
            }
            m_vfsName = m_rfsName;
        } catch (Throwable t) {
            // resource not found, no export possible
            m_resource = null;
        }          
        
        boolean match = false;
        if (m_resource == null) {
            // name of export resource could not be resolved by reading the resource directly,
            // now try to find a match with the "exportname" folders            
            Map exportnameFolders = A_OpenCms.getStaticExportProperties().getExportnames();
            Iterator i = exportnameFolders.keySet().iterator();
            while (i.hasNext()) {
                String exportName = (String)i.next();
                if (m_rfsName.startsWith(exportName)) {
                    // prefix match
                    match = true;       
                    // TODO: handle multiple matches         
                    m_vfsName = exportnameFolders.get(exportName) + m_rfsName.substring(exportName.length());
                    try {
                        m_resource = cms.readFileHeader(m_vfsName);
                        if (m_resource.isFolder()) {
                            if (! m_rfsName.endsWith("/")) m_rfsName += "/";
                            if (! m_vfsName.endsWith("/")) m_vfsName += "/";
                        }                        
                    } catch (CmsException e) {
                        m_rfsName = null;
                    }
                    break;
                }
            }    
            
            if (! match) {
                // no match found, nothing to export
                m_rfsName = null;
            }   
        }
        
        m_cms = cms;            
    }
    
    /**
     * Exports the requested uri and at the same time writes the uri to the response output stream
     * if required.<p>
     * 
     * @param req the current request
     * @param res the current response
     * @throws Exception in case something goes wrong
     */
    public synchronized void export(HttpServletRequest req, HttpServletResponse res) throws Exception {
        
        CmsFile file;
        if (m_resource.isFile()) {
            file = m_cms.readFile(m_vfsName);
        } else {
            file = initResource(m_cms, m_vfsName);
            m_vfsName = m_vfsName + file.getResourceName();
            m_rfsName += C_EXPORT_DEFAULT_FILE;
        } 
        
        int loaderId = file.getLoaderId();
        I_CmsResourceLoader loader = A_OpenCms.getLoaderManager().getLoader(loaderId);
        if ((loader == null) 
            || ((loaderId != CmsDumpLoader.C_RESOURCE_LOADER_ID)
                && (loaderId != CmsJspLoader.C_RESOURCE_LOADER_ID)
                && (loaderId != CmsPageLoader.C_RESOURCE_LOADER_ID)
                )           
        ) {
            throw new CmsException("Unable to export VFS file " + m_vfsName + ", invalid loader id " + loaderId);
        }        
        
        String exportFolderName = A_OpenCms.getStaticExportProperties().getExportPath() + CmsResource.getPath(m_rfsName).substring(1);
        File exportFolder = new File(exportFolderName);
        if (!exportFolder.exists()) {
            if (!exportFolder.mkdirs()) {
                throw new CmsException("Creation of export folder failed for RFS file " + m_rfsName);
            }
        }
        
        String exportFileName = A_OpenCms.getStaticExportProperties().getExportPath() + m_rfsName.substring(1);
        File exportFile = new File(exportFileName);
        FileOutputStream exportStream;
        try {
            exportStream = new FileOutputStream(exportFile);
        } catch (Throwable t) {
            throw new CmsException("Creation of export output stream failed for RFS file " + m_rfsName);
        }

        String mimetype = A_OpenCms.getMimeType(file.getResourceName(), m_cms.getRequestContext().getEncoding());
        res.setContentType(mimetype);  
                
        String oldUri = m_cms.getRequestContext().getUri();
        m_cms.getRequestContext().setUri(m_vfsName);          
        byte[] content = loader.export(m_cms, file, req, res);    
        m_cms.getRequestContext().setUri(oldUri);
                
        exportStream.write(content);
        exportStream.close();   
    }
    
    /**
     * Returns true if the uri is static exportable.<p>
     * 
     * @return true if the uri is static exportable
     */
    public boolean isStaticExportable() {
        return m_rfsName != null;
    }
    
        
    private CmsFile initResource(CmsObject cms, String resourceName) throws CmsException {
        CmsFile file = null;

        try {
            // Try to read the requested file
            file = cms.readFile(resourceName);
        } catch (CmsException e) {
            if (e.getType() == CmsException.C_NOT_FOUND) {
                // The requested file was not found
                // Check if a folder name was requested, and if so, try
                // to read the default pages in that folder

                try {
                    // Try to read the requested resource name as a folder
                    CmsFolder folder = cms.readFolder(resourceName);
                    // If above call did not throw an exception the folder
                    // was sucessfully read, so lets go on check for default 
                    // pages in the folder now

                    // Check if C_PROPERTY_DEFAULT_FILE is set on folder
                    String defaultFileName = cms.readProperty(CmsResource.getPath(cms.readAbsolutePath(folder)), I_CmsConstants.C_PROPERTY_DEFAULT_FILE);
                    if (defaultFileName != null) {
                        // Property was set, so look up this file first
                        String tmpResourceName = CmsResource.getPath(cms.readAbsolutePath(folder)) + defaultFileName;

                        try {
                            file = cms.readFile(tmpResourceName);
                            // No exception? So we have found the default file                         
                            cms.getRequestContext().getRequest().setRequestedResource(tmpResourceName);
                        } catch (CmsSecurityException se) {
                            // Maybe no access to default file?
                            throw se;
                        } catch (CmsException exc) {
                            // Ignore all other exceptions
                        }
                    }
                    if (file == null) {
                        // No luck with the property, so check default files specified in opencms.properties (if required)
                        Iterator i = A_OpenCms.getDefaultFilenames().iterator(); 
                        while (i.hasNext()) {
                            String tmpResourceName = CmsResource.getPath(cms.readAbsolutePath(folder)) + (String)i.next();
                            try {
                                file = cms.readFile(tmpResourceName);
                                // No exception? So we have found the default file                         
                                cms.getRequestContext().getRequest().setRequestedResource(tmpResourceName);
                                // Stop looking for default files   
                                break;
                            } catch (CmsSecurityException se) {
                                // Maybe no access to default file?
                                throw se;
                            } catch (CmsException exc) {
                                // Ignore all other exceptions
                            }
                        }
                    }
                    if (file == null) {
                        // No default file was found, throw original exception
                        throw e;
                    }
                } catch (CmsException ex) {
                    // Exception trying to read the folder (or it's properties)
                    throw ex;
                }

            } else {
                // Throw the CmsException (possible cause e.g. no access permissions)
                throw e;
            }
        }

        if (file != null) {
            // test if this file is only available for internal access operations
            if ((file.getFlags() & I_CmsConstants.C_ACCESS_INTERNAL_READ) > 0) {
                throw new CmsException(CmsException.C_ERROR_DESCRIPTION[CmsException.C_INTERNAL_FILE] + cms.getRequestContext().getUri(), CmsException.C_INTERNAL_FILE);
            }
        }    
        
        return file;    
    }    
    
}