/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsLinkManager.java,v $
 * Date   : $Date: 2003/08/15 17:38:04 $
 * Version: $Revision: 1.6 $
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
import com.opencms.util.Utils;

/**
 * Does the link replacement for the &lg;link&gt; tags.<p> 
 *
 * Since this functionality is closely related to the static export,
 * this class resides in the static export package.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.6 $
 */
public class CmsLinkManager {

    /**
     * Public constructor.<p>
     */
    public CmsLinkManager() {
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
            // not a valid link parameter, return an empty String
            return "";
        }
        
        String absoluteLink;
        if (!link.startsWith("/")) {
            // this is a relative link, lets make an absolute out of it
            absoluteLink = Utils.mergeAbsolutePath(cms.getRequestContext().getUri(), link);
        } else {
            absoluteLink = link;
        }
        
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
        boolean exportRequired = false;
        
        if (OpenCms.getStaticExportManager().isStaticExportEnabled() 
        && cms.getRequestContext().currentProject().isOnlineProject()) {           
            try {
                // first check if we already have the result cached
                resultLink = OpenCms.getStaticExportManager().getCachedOnlineLink(vfsName);
                if (resultLink == null) {                
                    // not cached, let's look up export property in VFS
                    String exportValue = cms.readProperty(vfsName, I_CmsConstants.C_PROPERTY_EXPORT, true);
                    if (exportValue == null) {
                        // no setting found for "export" property
                        if (OpenCms.getStaticExportManager().getExportPropertyDefault()) {
                            // if the default is "true" we always export
                            exportRequired = true;
                        } else {
                            // check if the resource is exportable by suffix
                            exportRequired = OpenCms.getStaticExportManager().isSuffixExportable(vfsName);
                        }                        
                    } else {
                        // "export" value found, if it was "true" we export
                        exportRequired = "true".equalsIgnoreCase(exportValue.trim());
                    }
                }
            } catch (Throwable t) {
                // ignore, no export required
            }
        }
        
        if (resultLink == null) {
            // link was not already found in the cache
            if (!exportRequired) {
                // no export required
                resultLink = OpenCms.getStaticExportManager().getVfsPrefix() + vfsName;
            } else {
                // export required, get export name
                resultLink = OpenCms.getStaticExportManager().getRfsName(cms, vfsName);
            }
            if (cms.getRequestContext().currentProject().isOnlineProject()) {
                // cache the result for the online project
                OpenCms.getStaticExportManager().cacheOnlineLink(vfsName, resultLink);
            }
        }
        
        // add cut off parameters and return the result
        if (parameters != null) {
            return resultLink + parameters;
        } else {
            return resultLink;
        }
    }
}