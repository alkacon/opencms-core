/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/binary/Attic/CmsPreviewProvider.java,v $
 * Date   : $Date: 2010/05/27 10:28:29 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries.preview.binary;

import org.opencms.ade.galleries.I_CmsPreviewProvider;
import org.opencms.ade.galleries.preview.binary.shared.I_CmsBinaryPreviewProvider;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The preview provider for binary resources.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsPreviewProvider implements I_CmsPreviewProvider {

    /** The JS resource URI. */
    private static final String NO_CACHE_URI = "/system/modules/org.opencms.ade.galleries.preview.binary/binary_preview/binary_preview.nocache.js";

    /**
     * @see org.opencms.ade.galleries.I_CmsPreviewProvider#getPreviewInclude(org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public String getPreviewInclude(CmsObject cms, HttpServletRequest request, HttpServletResponse response) {

        // include the script tag to load module resources
        StringBuffer sb = new StringBuffer();
        sb.append("<script type=\"text/javascript\" src=\"").append(
            OpenCms.getLinkManager().substituteLink(cms, NO_CACHE_URI)).append("\"></script>").append("\n");
        return sb.toString();
    }

    /**
     * @see org.opencms.ade.galleries.I_CmsPreviewProvider#getPreviewName()
     */
    public String getPreviewName() {

        return I_CmsBinaryPreviewProvider.PREVIEW_NAME;
    }

}
