/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsLinkManager.java,v $
 * Date   : $Date: 2003/09/29 19:10:02 $
 * Version: $Revision: 1.11 $
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

import org.opencms.main.OpenCms;

import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Does the link replacement for the &lg;link&gt; tags.<p> 
 *
 * Since this functionality is closely related to the static export,
 * this class resides in the static export package.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.11 $
 */
public class CmsLinkManager {
    
    /** Base URL to calculate absolute links */
    private static URL m_baseUrl;

    /**
     * Public constructor.<p>
     */
    public CmsLinkManager() {
    }

    /**
     * Static initializer for the base URL.<p>
     */
    static {
        m_baseUrl = null;
        try {
            m_baseUrl = new URL("http://127.0.0.1");
        } catch (MalformedURLException e) {
            // this won't happen
        }
    }
    
    /**
     * Calculates an absolute uri from a relative "uri" and the given absolute "baseUri".<p> 
     * 
     * If "uri" is already absolute, it is returned unchanged.
     * This method also returns "uri" unchanged if it is not well-formed.<p>
     *    
     * @param relativeUri the relative uri to calculate an absolute uri for
     * @param baseUri the base uri, this must be an absolute uri
     * @return an absolute uri calculated from "uri" and "baseUri"
     */
    public static String getAbsoluteUri(String relativeUri, String baseUri) {
        if ((relativeUri == null) || (relativeUri.charAt(0) == '/')) {
            return relativeUri;
        }
        try {
            URL url = new URL(new URL(m_baseUrl, baseUri), relativeUri);
            if (url.getQuery() == null) {
                return url.getPath();
            } else {
                return url.getPath() + "?" + url.getQuery();
            }
        } catch (MalformedURLException e) {
            return relativeUri;
        }
    }
    
    /**
     * Normalizes a RFS path that might contain '../' or './' or '//' elements to a normal absolute path.<p>
     * 
     * @param path the path to normalize
     * @return the normalized path
     */
    public static String normalizeRfsPath(String path) {
        if (path != null) {
            path = path.replace('\\', '/');
            String drive = "";
            if ((path.length() > 1) && (path.charAt(1) == ':')) {
                // windows path like C:
                drive = path.substring(0, 2);
                path = path.substring(2);
            } 
            if (path.charAt(0) == '/') {
                // trick to resolve all ../ inside a path
                path = "." + path;
            }
            path = drive + CmsLinkManager.getAbsoluteUri(path, "/");
            path = path.replace('/', File.separatorChar);
        }
        return path;
    }    
    
    /**
     * Calculates a realtive uri from "fromUri" to "toUri",
     * both uri's must be absolute.<p>
     * 
     * @param fromUri the uri to start
     * @param toUri the uri to calculate a relative path to
     * @return a realtive uri from "fromUri" to "toUri"
     */
    public static String getRelativeUri(String fromUri, String toUri) {
        StringBuffer result = new StringBuffer();
        int pos = 0;

        while (true) {
            int i = fromUri.indexOf ('/', pos);
            int j = toUri.indexOf ('/', pos);
            if ((i == -1) || (i != j) || !fromUri.regionMatches(pos, toUri, pos, i-pos)) {
                break;
            }
            pos = i+1;
        }

        // count hops up from here to the common ancestor
        for (int i = fromUri.indexOf('/', pos); i > 0; i = fromUri.indexOf('/', i+1)) {
            result.append("../");
        }

        // append path down from common ancestor to there
        result.append(toUri.substring(pos));
        return result.toString();
    }
    
    /**
     * Checks if the export is required for a given vfs resource.<p>
     * 
     * @param cmsParam the current cms context
     * @param vfsName the vfs resource name to check
     * @return true if export is required for the given vfsName
     */
    private boolean exportRequired(CmsObject cmsParam, String vfsName) {
        boolean result = false;
        if (OpenCms.getStaticExportManager().isStaticExportEnabled()) { 
            try {
                // static export must always be checked with the export users permissions,
                // not the current users permissions
                CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
                cms.getRequestContext().setSiteRoot(cmsParam.getRequestContext().getSiteRoot());
                // let's look up export property in VFS
                String exportValue = cms.readProperty(vfsName, I_CmsConstants.C_PROPERTY_EXPORT, true);
                if (exportValue == null) {
                    // no setting found for "export" property
                    if (OpenCms.getStaticExportManager().getExportPropertyDefault()) {
                        // if the default is "true" we always export
                        result = true;
                    } else {
                        // check if the resource is exportable by suffix
                        result = OpenCms.getStaticExportManager().isSuffixExportable(vfsName);
                    }                        
                } else {
                    // "export" value found, if it was "true" we export
                    result = "true".equalsIgnoreCase(exportValue.trim());
                }
            } catch (Throwable t) {
                // no export required (probably security issues, e.g. no access for export user)
            }
        }
        return result;
    }

    /**
     * Substitutes the contents of a link by adding the context path and 
     * servlet name, and in the case of the "online" project also according
     * to the configured static export settings.<p>
     * 
     * @param cms the cms context
     * @param link the link to process (must be a valid link to a VFS resource with optional parameters)
     * @return the substituted link
     */
    public String substituteLink(CmsObject cms, String link) {
        if (link == null || "".equals(link)) {
            // not a valid parameter, return an empty String
            return "";
        }

        // make sure we have an absolute link        
        String absoluteLink = CmsLinkManager.getAbsoluteUri(link, cms.getRequestContext().getUri());
        
        String vfsName;
        String parameters;
        int pos = absoluteLink.indexOf('?');
        // check if the link has parameters, if so cut them
        if (pos >= 0) {
            vfsName = absoluteLink.substring(0, pos);
            parameters = absoluteLink.substring(pos);
        } else {
            vfsName = absoluteLink;
            parameters = null;
        }
        
        String resultLink = null;
        String uriBaseName = null;
        boolean useRelativeLinks = false;
        
        if (cms.getRequestContext().currentProject().isOnlineProject()) {
            
            // check if we need relative links in the exported pages
            if (OpenCms.getStaticExportManager().relativLinksInExport()) {
                // try to get base uri from cache  
                uriBaseName = OpenCms.getStaticExportManager().getCachedOnlineLink(cms.getRequestContext().getUri());                
                if (uriBaseName == null) {
                    // base not cached, check if we must export it
                    if (exportRequired(cms, cms.getRequestContext().getUri())) {
                        // base uri must also be exported
                        uriBaseName = OpenCms.getStaticExportManager().getRfsName(cms, cms.getRequestContext().getUri());
                    } else {
                        // base uri dosn't need to be exported
                        uriBaseName = OpenCms.getStaticExportManager().getVfsPrefix() + cms.getRequestContext().getUri();
                    }
                    // cache export base uri
                    OpenCms.getStaticExportManager().cacheOnlineLink(cms.getRequestContext().getUri(), uriBaseName);
                }
                // use relative links only on pages that get exported 
                useRelativeLinks = uriBaseName.startsWith(OpenCms.getStaticExportManager().getRfsPrefix());
            }

            // check if we have the absolute vfs name for the link target cached
            resultLink = OpenCms.getStaticExportManager().getCachedOnlineLink(vfsName);
            if (resultLink == null) {
                // didn't find the link in the cache
                if (exportRequired(cms, vfsName)) {
                    // export required, get export name for target link
                    resultLink = OpenCms.getStaticExportManager().getRfsName(cms, vfsName);
                } else {
                    // no export required for the target link
                    resultLink = OpenCms.getStaticExportManager().getVfsPrefix() + vfsName;
                }
            }            
            // cache the result 
            OpenCms.getStaticExportManager().cacheOnlineLink(vfsName, resultLink);

            if (useRelativeLinks) {
                // we want relative links in export, so make the absolute link relative
                resultLink = getRelativeUri(uriBaseName, resultLink);
            }

        } else {
            // offline project, no export required
            resultLink = OpenCms.getStaticExportManager().getVfsPrefix() + vfsName;
        }
        
        // add cut off parameters and return the result
        if (parameters != null) {
            return resultLink + parameters;
        } else {
            return resultLink;
        }
    }
}