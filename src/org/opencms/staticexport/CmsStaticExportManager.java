/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsStaticExportManager.java,v $
 * Date   : $Date: 2003/08/10 11:49:48 $
 * Version: $Revision: 1.1 $
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

import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import java.io.File;
import java.io.FileOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides the functionaility to export resources from the OpenCms VFS
 * to the file system.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsStaticExportManager {
    
    /** Marker for externally redirected 404 uri's */
    public static final String C_EXPORT_MARKER = "exporturi";
    
    /** The initialized cms context */
    private CmsObject m_cms;
    
    /** The uri to export */
    private String m_exportUri;
    
    /**
     * Generates a new static export manager.<p>
     * 
     * @param request the current request
     * @param cms the current cms context object
     */
    public CmsStaticExportManager(HttpServletRequest request, CmsObject cms) {
        m_exportUri = request.getParameter(C_EXPORT_MARKER);
        if ((m_exportUri == null) || !m_exportUri.startsWith(A_OpenCms.getStaticExportProperties().getExportPrefix())) {
            // this is not an export request, no further processing is required
            return;
        }
        
        // check if export uri exists in the OpenCms VFS
        m_exportUri = m_exportUri.substring(A_OpenCms.getStaticExportProperties().getExportPrefix().length());
        
        try {
            CmsResource res = cms.readFileHeader(m_exportUri);
            if (res.isFolder()) {
                // auto default-file export not yet supported
                throw new Exception(); 
            }
        } catch (Throwable t) {
            // resource not found, no export possible
            m_exportUri = null;
            return;
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
        CmsFile file = m_cms.readFile(m_exportUri);
        
        int loaderId = file.getLoaderId();
        I_CmsResourceLoader loader = A_OpenCms.getLoaderManager().getLoader(loaderId);
        if ((loader == null) 
            || ((loaderId != CmsDumpLoader.C_RESOURCE_LOADER_ID)
                && (loaderId != CmsJspLoader.C_RESOURCE_LOADER_ID)
                && (loaderId != CmsPageLoader.C_RESOURCE_LOADER_ID)
                )           
        ) {
            throw new CmsException("Unable to export file " + m_exportUri + ", invalid loader id " + loaderId);
        }        
        
        String exportFolderName = A_OpenCms.getStaticExportProperties().getExportPath() + CmsResource.getPath(m_exportUri).substring(1);
        File exportFolder = new File(exportFolderName);
        if (!exportFolder.exists()) {
            if (!exportFolder.mkdirs()) {
                throw new CmsException("Creation of export folder failed for file " + m_exportUri);
            }
        }
        
        String exportFileName = A_OpenCms.getStaticExportProperties().getExportPath() + m_exportUri.substring(1);
        File exportFile = new File(exportFileName);
        FileOutputStream exportStream;
        try {
            exportStream = new FileOutputStream(exportFile);
        } catch (Throwable t) {
            throw new CmsException("Creation of export output stream failed for file " + m_exportUri);
        }

        String mimetype = A_OpenCms.getMimeType(file.getResourceName(), m_cms.getRequestContext().getEncoding());
        res.setContentType(mimetype);  
                
        int oldMode = m_cms.getMode();
        String oldUri = m_cms.getRequestContext().getUri();        
        
        m_cms.setMode(I_CmsConstants.C_MODUS_EXPORT);      
        m_cms.getRequestContext().setUri(m_exportUri);          
        
        byte[] content = loader.export(m_cms, file, req, res); 
         
        m_cms.setMode(oldMode);              
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
        return m_exportUri != null;
    }
}