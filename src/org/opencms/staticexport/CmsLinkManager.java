/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsLinkManager.java,v $
 * Date   : $Date: 2003/08/11 18:30:52 $
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

import com.opencms.core.A_OpenCms;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.util.Utils;

/**
 * Does the link replacement for the &lg;link&gt; tags.<p> 
 *
 * Since this functionality is closley related to the static export,
 * this class resides in the static export package.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 */
public class CmsLinkManager {

    /**
     * Hides the public constructor.<p>
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
            return "";
        }
        String absoluteLink;
        if (!link.startsWith("/")) {
            // this is a relative link, lets make an absolute out of it
            absoluteLink = Utils.mergeAbsolutePath(cms.getRequestContext().getUri(), link);
        } else {
            absoluteLink = link;
        }
        
        String uri;
        int pos = absoluteLink.indexOf('?');
        if (pos >= 0) {
            uri = absoluteLink.substring(0, pos-1);
        } else {
            uri = absoluteLink;
        }

        String exportProperty;
        if (A_OpenCms.getStaticExportProperties().isStaticExportEnabled() 
        && cms.getRequestContext().currentProject().isOnlineProject()) {           
            try {
                exportProperty = cms.readProperty(uri, I_CmsConstants.C_PROPERTY_EXPORT, true, "false");
            } catch (Throwable t) {
                exportProperty = "false";
            }
        } else {
            exportProperty = "false";
        }

        String result;
        
        if ("true".equalsIgnoreCase(exportProperty)) {
            result = A_OpenCms.getStaticExportProperties().getRfsPrefix() + absoluteLink;
        } else {
            result = A_OpenCms.getStaticExportProperties().getVfsPrefix() + absoluteLink;
        }
        return result;
    }

}