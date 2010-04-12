/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/Attic/CmsGalleryProvider.java,v $
 * Date   : $Date: 2010/04/12 14:00:39 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.galleries;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.gwt.I_CmsCoreProvider;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Convenience class to provide gallery server-side data to the client.<p> 
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.galleries.client.util.CmsGalleryProvider
 */
public final class CmsGalleryProvider implements I_CmsGalleryProviderConstants, I_CmsCoreProvider {

    /** Internal instance. */
    private static CmsGalleryProvider INSTANCE;

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsGalleryProvider.class);

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private CmsGalleryProvider() {

        // hide the constructor
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsGalleryProvider get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsGalleryProvider();
        }
        return INSTANCE;
    }

    /**
     * @see org.opencms.gwt.I_CmsCoreProvider#export(javax.servlet.http.HttpServletRequest)
     */
    public String export(HttpServletRequest request) {

        StringBuffer sb = new StringBuffer();
        sb.append(ClientMessages.get().export(request));
        sb.append(DICT_NAME.replace('.', '_')).append("=").append(getData(request).toString()).append(";");
        return sb.toString();
    }

    /**
     * @see org.opencms.gwt.I_CmsCoreProvider#exportAll(javax.servlet.http.HttpServletRequest)
     */
    public String exportAll(HttpServletRequest request) {

        StringBuffer sb = new StringBuffer();
        sb.append(org.opencms.gwt.CmsCoreProvider.get().export(request));
        sb.append(export(request));
        return sb.toString();
    }

    /**
     * @see org.opencms.gwt.I_CmsCoreProvider#getData(javax.servlet.http.HttpServletRequest)
     */
    public JSONObject getData(HttpServletRequest request) {

        JSONObject keys = new JSONObject();
        try {
            keys.put(ReqParam.gallerypath.name(), getGalleryPath(request));
            keys.put(ReqParam.tabs.name(), getTabsConfig(request));
            keys.put(ReqParam.types.name(), getTypes(request));
        } catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
            try {
                keys.put("error", e.getLocalizedMessage());
            } catch (JSONException e1) {
                // ignore, should never happen
                LOG.error(e1.getLocalizedMessage(), e1);
            }
        }
        return keys;
    }

    /**
     * Returns the path to the gallery to open.<p>
     * 
     * @param request the current request to get the the parameter
     * 
     * @return the path to the gallery
     */
    private String getGalleryPath(HttpServletRequest request) {

        return request.getParameter(ReqParam.gallerypath.name());
    }

    /**
     * Returns the tabs configuration for the gallery dialog.<p>
     * 
     * @param request the current request to get the the parameter
     * 
     * @return the configuration string with tabs to display
     */
    private String getTabsConfig(HttpServletRequest request) {

        return request.getParameter(ReqParam.tabs.name());
    }

    /**
     * Returns the available resource types for this gallery dialog.<p>
     * 
     * @param request the current request to get the the parameter
     * 
     * @return the comma separated resource types
     */
    private String getTypes(HttpServletRequest request) {

        return request.getParameter(ReqParam.types.name());
    }
}
