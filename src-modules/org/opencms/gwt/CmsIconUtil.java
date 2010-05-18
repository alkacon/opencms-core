/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/Attic/CmsIconUtil.java,v $
 * Date   : $Date: 2010/05/18 12:31:13 $
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

package org.opencms.gwt;

import org.opencms.file.types.CmsResourceTypeUnknownFile;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

/**
 * Utility class to generate the resource icon CSS.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsIconUtil extends org.opencms.gwt.shared.CmsIconUtil {

    /**
     * Returns the resource icon CSS.<p>
     * 
     * @return the icon CSS
     */
    public static String buildResourceIconCss() {

        String unknown = CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES
            + OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypeUnknownFile.getStaticTypeName()).getIcon());
        StringBuffer sb = new StringBuffer(".");
        sb.append(TYPE_ICON_CLASS).append("{\nbackground: transparent scroll 50% 50% no-repeat url(\"").append(unknown).append(
            "\");\nheight: 16px;\nwidth: 16px;\n}\n");

        for (I_CmsResourceType type : OpenCms.getResourceManager().getResourceTypes()) {
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
            if (settings != null) {
                sb.append(".").append(TYPE_ICON_CLASS).append(".").append(getResourceTypeIconClass(type.getTypeName())).append(
                    "{\nbackground-image: url(\"").append(
                    CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + settings.getIcon())).append(
                    "\");\n}\n");
            }
        }
        //TODO: add icon CSS for binary type like doc, pdf, xls etc. 
        return sb.toString();
    }
}
