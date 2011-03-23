/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsEditorHandler.java,v $
 * Date   : $Date: 2011/03/23 14:51:44 $
 * Version: $Revision: 1.19 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.editors;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;

import org.apache.commons.logging.Log;

/**
 * This editor handler class returns the editor URI depending on various factors.<p>
 * 
 * Editor selection criteria:
 * <ul>
 * <li>the user preferences</li>
 * <li>the users current browser</li>
 * <li>the resource type</li>
 * </ul>
 * <p>
 * 
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.19 $ 
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.workplace.editors.I_CmsEditorHandler
 * @see org.opencms.workplace.editors.CmsWorkplaceEditorManager
 */
public class CmsEditorHandler implements I_CmsEditorHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditorHandler.class);

    /**
     * @see org.opencms.workplace.editors.I_CmsEditorHandler#getEditorUri(java.lang.String, CmsJspActionElement)
     */
    public String getEditorUri(String resource, CmsJspActionElement jsp) throws CmsException {

        // first try to get the "edit as text" and "load default" parameters from the request
        boolean editAsText = Boolean.valueOf(jsp.getRequest().getParameter(CmsEditor.PARAM_EDITASTEXT)).booleanValue();
        boolean loadDefault = Boolean.valueOf(jsp.getRequest().getParameter(CmsEditor.PARAM_LOADDEFAULT)).booleanValue();
        // initialize resource type with -1 (unknown resource type)
        int resTypeId = -1;
        String resourceType = "";
        if (editAsText) {
            // the resource should be treated as text, set the plain resource id
            resTypeId = CmsResourceTypePlain.getStaticTypeId();
        } else {
            // get the resource type id of the edited resource
            CmsResource res = jsp.getCmsObject().readResource(resource, CmsResourceFilter.ALL);
            resTypeId = res.getTypeId();
        }

        // get the resource type name
        resourceType = OpenCms.getResourceManager().getResourceType(resTypeId).getTypeName();

        // get the editor URI from the editor manager
        String editorUri = null;

        // get the browser identification from the request
        String userAgent = jsp.getRequest().getHeader(CmsRequestUtil.HEADER_USER_AGENT);

        if (loadDefault) {
            // get default editor because loaddefault parameter was found
            editorUri = OpenCms.getWorkplaceManager().getWorkplaceEditorManager().getDefaultEditorUri(
                jsp.getRequestContext(),
                resourceType,
                userAgent);
        } else {
            // get preferred editor
            editorUri = OpenCms.getWorkplaceManager().getWorkplaceEditorManager().getEditorUri(
                jsp.getRequestContext(),
                resourceType,
                userAgent);
        }

        try {
            // check the presence of the editor
            jsp.getCmsObject().readResource(editorUri);
        } catch (Throwable t) {
            // preferred or selected editor not found, try default editor
            if (LOG.isInfoEnabled()) {
                LOG.info(t);
            }
            editorUri = OpenCms.getWorkplaceManager().getWorkplaceEditorManager().getDefaultEditorUri(
                jsp.getRequestContext(),
                resourceType,
                userAgent);
        }

        return editorUri;
    }

}
