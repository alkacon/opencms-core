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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;

import java.util.HashMap;
import java.util.Map;

/**
 * Pre editor action for XML content resource types, checks if model files are available for the XML content
 * to create in direct edit mode and shows the selection before opening the editor.<p>
 *
 * @since 6.5.4
 */
public class CmsPreEditorActionDefinitionXmlContent extends A_CmsPreEditorActionDefinition {

    /**
     * Constructor, without parameters.<p>
     */
    public CmsPreEditorActionDefinitionXmlContent() {

        // empty constructor, needed for initialization
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsPreEditorActionDefinition#doPreAction(org.opencms.file.CmsResource, org.opencms.workplace.CmsDialog, java.lang.String)
     */
    @Override
    public boolean doPreAction(CmsResource resource, CmsDialog dialog, String originalParams) throws Exception {

        String newlink = dialog.getJsp().getRequest().getParameter(CmsXmlContentEditor.PARAM_NEWLINK);
        if (CmsStringUtil.isNotEmpty(newlink)) {
            // pre editor action not executed yet and new link is provided, now check model files for resource type
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
            String folderPath = dialog.getSettings().getExplorerResource();
            // get the name of the currently edited resource
            String resName = dialog.getJsp().getRequest().getParameter(CmsDialog.PARAM_RESOURCE);
            if (CmsStringUtil.isNotEmpty(resName)) {
                // get the folder path from the currently edited resource
                folderPath = CmsResource.getFolderPath(resName);
            }
            if (CmsResourceTypeXmlContent.getModelFiles(dialog.getCms(), folderPath, type.getTypeName()).size() > 0) {
                // model files present, display model file selection dialog before opening editor
                Map<String, String[]> params = new HashMap<String, String[]>(4);
                // put the original request parameters to a new parameter value
                params.put(CmsDialog.PARAM_ORIGINALPARAMS, new String[] {originalParams});
                // set action for dialog to open
                params.put(CmsDialog.PARAM_ACTION, new String[] {CmsResourceTypeXmlContent.DIALOG_CHOOSEMODEL});
                // set the title for the dialog
                params.put(
                    CmsDialog.PARAM_TITLE,
                    new String[] {dialog.getJsp().getRequest().getParameter("editortitle")});
                // set the resource type to create for the dialog
                params.put(CmsWorkplace.PARAM_NEWRESOURCETYPE, new String[] {type.getTypeName()});
                // set the back link URL to return to if pressing the cancel button
                String paramBackLink = dialog.getJsp().getRequest().getParameter(CmsEditor.PARAM_BACKLINK);
                if (CmsStringUtil.isNotEmpty(paramBackLink)) {
                    params.put(CmsEditor.PARAM_BACKLINK, new String[] {paramBackLink});
                }
                // set the resource name
                if (CmsStringUtil.isNotEmpty(resName)) {
                    params.put(CmsDialog.PARAM_RESOURCE, new String[] {resName});
                }
                // forward to model file selection dialog
                dialog.sendForward(CmsWorkplace.VFS_PATH_MODELDIALOG, params);
                return true;
            }
        }
        return false;
    }

}
