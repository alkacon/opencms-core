/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/Attic/CmsPublishProvider.java,v $
 * Date   : $Date: 2010/04/07 13:34:41 $
 * Version: $Revision: 1.2 $
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
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

/**
 * The provider class for the publish module.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public final class CmsPublishProvider {

    /** Name of the dictionary.<p> */
    protected static String DICT_NAME = "org.opencms.ade.publish";

    /** Internal instance. */
    private static CmsPublishProvider INSTANCE;

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
     * Returns the JSON code for the core provider and the given message bundle.<p>
     * 
     * @param request the current request to get the default locale from 
     * 
     * @return the JSON code
     */
    public String export(HttpServletRequest request) {

        CmsObject cms = CmsFlexController.getCmsObject(request);

        StringBuffer sb = new StringBuffer();
        sb.append(org.opencms.gwt.CmsCoreProvider.get().export(request));
        sb.append(I_CmsPublishProviderConstants.DICT_NAME.replace('.', '_')).append("=").append(
            getData(cms, request).toString()).append(";");
        sb.append(ClientMessages.get().export(request));
        return sb.toString();
    }

    /**
     * Returns the provided json data.<p>
     * 
     * @param cms the current cms object
     * @param request the current request
     * 
     * @return the provided json data
     */
    public JSONObject getData(CmsObject cms, HttpServletRequest request) {

        JSONObject keys = new JSONObject();
        return keys;
    }

}
