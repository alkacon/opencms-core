/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/Attic/CmsPublishProvider.java,v $
 * Date   : $Date: 2010/04/26 12:36:45 $
 * Version: $Revision: 1.4 $
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

package org.opencms.ade.publish;

import org.opencms.ade.publish.shared.I_CmsPublishProviderConstants;
import org.opencms.ade.sitemap.CmsSitemapProvider;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.I_CmsCoreProvider;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * The provider class for the publish module.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public final class CmsPublishProvider implements I_CmsCoreProvider, I_CmsPublishProviderConstants {

    /** Internal instance. */
    private static CmsPublishProvider INSTANCE;

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapProvider.class);

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private CmsPublishProvider() {

        // hide the constructor
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsPublishProvider get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsPublishProvider();
        }
        return INSTANCE;
    }

    /**
     * @see org.opencms.gwt.I_CmsCoreProvider#export(javax.servlet.http.HttpServletRequest)
     */
    public String export(HttpServletRequest request) {

        StringBuffer sb = new StringBuffer();
        sb.append(DICT_NAME.replace('.', '_')).append("=").append(getData(request).toString()).append(";");
        sb.append(ClientMessages.get().export(request));
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

        CmsObject cms = CmsFlexController.getCmsObject(request);
        JSONObject data = new JSONObject();
        try {
            data.put(KEY_IS_ADMIN_USER, isAdminUser(cms));
        } catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
            try {
                data.put("error", e.getLocalizedMessage());
            } catch (JSONException e1) {
                // ignore, should never happen
                LOG.error(e1.getLocalizedMessage(), e1);
            }
        }
        return data;
    }

    /**
     * Checks whether the user for a CmsObject is the Admin user.<p>
     * 
     * @param cms the CmsObject for which the user should be checked
     * @return true if the user is the Admin user
     */
    public boolean isAdminUser(CmsObject cms) {

        CmsUser user = cms.getRequestContext().currentUser();
        return OpenCms.getDefaultUsers().isUserAdmin(user.getName());
    }
}
