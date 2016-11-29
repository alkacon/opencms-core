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

import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;

import java.io.IOException;

import javax.servlet.jsp.JspException;

/**
 * Provides a method to perform a user defined action when editing a page.<p>
 *
 * @since 6.0.0
 */
public class CmsEditorActionDefault implements I_CmsEditorActionHandler {

    /**
     * Default constructor needed for editor action handler implementation.<p>
     */
    public CmsEditorActionDefault() {

        // empty constructor
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsEditorActionHandler#editorAction(org.opencms.workplace.editors.CmsEditor, org.opencms.jsp.CmsJspActionElement)
     */
    public void editorAction(CmsEditor editor, CmsJspActionElement jsp) throws IOException, JspException {

        // save the edited content
        editor.actionSave();
        // delete temporary file and unlock resource in direct edit mode
        editor.actionClear(true);
        // create the publish link to redirect to
        String publishLink = jsp.link(CmsWorkplace.PATH_DIALOGS + "publishresource.jsp");
        // define the parameters which are necessary for publishing the resource
        StringBuffer params = new StringBuffer(256);
        params.append("?").append(CmsDialog.PARAM_RESOURCE).append("=").append(editor.getParamResource());
        params.append("&").append(CmsDialog.PARAM_ACTION).append("=").append(CmsDialog.DIALOG_CONFIRMED);
        params.append("&").append(CmsWorkplace.PARAM_DIRECTPUBLISH).append("=").append(CmsStringUtil.TRUE);
        params.append("&").append(CmsWorkplace.PARAM_PUBLISHSIBLINGS).append("=").append(
            editor.getSettings().getUserSettings().getDialogPublishSiblings());
        // set the related resources option
        String pubRelated = CmsStringUtil.TRUE;
        if (OpenCms.getWorkplaceManager().getDefaultUserSettings().getPublishRelatedResources() == CmsDefaultUserSettings.PUBLISH_RELATED_RESOURCES_MODE_FALSE) {
            pubRelated = CmsStringUtil.FALSE;
        }
        params.append("&").append(CmsWorkplace.PARAM_RELATEDRESOURCES).append("=").append(pubRelated);
        params.append("&").append(CmsDialog.PARAM_TITLE).append("=");
        params.append(
            CmsEncoder.escapeWBlanks(
                editor.key(Messages.GUI_MESSAGEBOX_TITLE_PUBLISHRESOURCE_0) + ": " + editor.getParamResource(),
                CmsEncoder.ENCODING_UTF_8));
        params.append("&").append(CmsDialog.PARAM_REDIRECT).append("=").append(CmsStringUtil.TRUE);
        params.append("&").append(CmsDialog.PARAM_CLOSELINK).append("=");
        if (Boolean.valueOf(editor.getParamDirectedit()).booleanValue()) {
            String linkTarget;
            if (!"".equals(editor.getParamBacklink())) {
                linkTarget = jsp.link(editor.getParamBacklink());
            } else {
                linkTarget = jsp.link(editor.getParamResource());
            }
            // append the parameters and the report "ok" button action to the link
            publishLink += params.toString() + CmsEncoder.escapeWBlanks(linkTarget, CmsEncoder.ENCODING_UTF_8);
        } else {
            // check for links to the new workplace
            if (CmsLinkManager.isWorkplaceLink(editor.getParamBacklink())) {
                publishLink += params.toString()
                    + CmsEncoder.escapeWBlanks(jsp.link(editor.getParamBacklink()), CmsEncoder.ENCODING_UTF_8);
            } else {
                publishLink += params.toString()
                    + CmsEncoder.escapeWBlanks(jsp.link(CmsWorkplace.JSP_WORKPLACE_URI), CmsEncoder.ENCODING_UTF_8);
            }

        }
        // redirect to the publish dialog with all necessary parameters
        jsp.getResponse().sendRedirect(publishLink);
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsEditorActionHandler#getButtonName()
     */
    public String getButtonName() {

        return Messages.GUI_EXPLORER_CONTEXT_PUBLISH_0;
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsEditorActionHandler#getButtonUrl(CmsJspActionElement, java.lang.String)
     */
    public String getButtonUrl(CmsJspActionElement jsp, String resourceName) {

        // get the button image
        String button = CmsWorkplace.VFS_PATH_RESOURCES + "buttons/publish.png";
        if (!isButtonActive(jsp, resourceName)) {
            // show disabled button if not active
            button = CmsWorkplace.VFS_PATH_RESOURCES + "buttons/publish_in.png";
        }
        return jsp.link(button);
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsEditorActionHandler#isButtonActive(CmsJspActionElement, java.lang.String)
     */
    public boolean isButtonActive(CmsJspActionElement jsp, String resourceName) {

        try {
            OpenCms.getPublishManager().getPublishList(
                jsp.getCmsObject(),
                jsp.getCmsObject().readResource(resourceName, CmsResourceFilter.IGNORE_EXPIRATION),
                false);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
