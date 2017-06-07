/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ui.editors;

import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.workplace.editors.CmsWorkplaceEditorManager;

import org.apache.commons.logging.Log;

/**
 * The acacia XML content editor.<p>
 */
public class CmsAcaciaEditor extends A_CmsFrameEditor {

    /** The serial version id. */
    private static final long serialVersionUID = -5498365579090634771L;

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAcaciaEditor.class);

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#getPriority()
     */
    public int getPriority() {

        return 100;
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#matchesResource(org.opencms.file.CmsResource, boolean)
     */
    public boolean matchesResource(CmsResource resource, boolean plainText) {

        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
        boolean result = false;

        if (!plainText && (type instanceof CmsResourceTypeXmlContent)) {
            try {
                result = CmsWorkplaceEditorManager.checkAcaciaEditorAvailable(A_CmsUI.getCmsObject(), resource);
            } catch (CmsException e) {
                LOG.error("Error evaluating XML schema for acacia editor.", e);
            }
        }

        return result;
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#newInstance()
     */
    public I_CmsEditor newInstance() {

        return new CmsAcaciaEditor();
    }

    /**
     * @see org.opencms.ui.editors.A_CmsFrameEditor#getEditorUri()
     */
    @Override
    protected String getEditorUri() {

        return "/system/workplace/editors/acacia/editor.jsp";
    }
}
