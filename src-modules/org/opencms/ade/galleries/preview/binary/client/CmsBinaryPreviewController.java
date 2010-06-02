/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/binary/client/Attic/CmsBinaryPreviewController.java,v $
 * Date   : $Date: 2010/06/02 14:46:36 $
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

package org.opencms.ade.galleries.preview.binary.client;

import org.opencms.ade.galleries.client.preview.CmsEditorPreviewUtil;
import org.opencms.ade.galleries.client.preview.CmsPreviewUtil;
import org.opencms.ade.galleries.client.preview.I_CmsPreviewController;
import org.opencms.ade.galleries.shared.CmsPreviewInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.CmsCoreProvider;

/**
 * Binary preview dialog controller.<p>
 * 
 * This class handles the communication between preview dialog and the server.  
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public final class CmsBinaryPreviewController implements I_CmsPreviewController {

    /** The dialog instance. */
    private static CmsBinaryPreviewController INSTANCE;

    /** The field id of the input field in the xmlcontent. */
    private String m_fieldId;

    /** The binary preview handler. */
    private CmsBinaryPreviewControllerHandler m_handler;

    /** The info bean of the binary preview dialog. */
    private CmsPreviewInfoBean m_infoBean;

    /** The binary preview dialog. */
    private CmsBinaryPreview m_previewDialog;

    /**
     * Hiding constructor.<p>
     * 
     * @param handler the preview controller handler
     * @param previewDialog the preview dialog
     */
    private CmsBinaryPreviewController(CmsBinaryPreviewControllerHandler handler, CmsBinaryPreview previewDialog) {

        m_handler = handler;
        m_previewDialog = previewDialog;
        // TODO: set the fieldId, set over request params

    }

    /**
     * Returns the dialogs instance.<p>
     * 
     * @return the dialog instance
     */
    public static CmsBinaryPreviewController get() {

        return INSTANCE;
    }

    /**
     * Initializes the dialog.<p>
     * 
     * @param handler the preview controller handler
     * @param previewDialog the preview dialog
     */
    public static void init(CmsBinaryPreviewControllerHandler handler, CmsBinaryPreview previewDialog) {

        if (INSTANCE == null) {
            INSTANCE = new CmsBinaryPreviewController(handler, previewDialog);
        }
    }

    /**
     * Selects the resource from the preview in fck editor.<p>  
     */
    static void selectFromEditor() {

        get().onSelect();

    }

    /**
     * Exports the select method to the window object, so it can be accessed from within the content editor iFrame.<p>
     */
    public native void export() /*-{
        $wnd['Ok']=function(){
        @org.opencms.ade.galleries.preview.binary.client.CmsBinaryPreviewController::selectFromEditor();
        }
    }-*/;

    /**
     * Saves the changed properties.<p>
     */
    public void saveProperties() {

        // TODO: rpc call to save properties
        m_handler.onSaveProperties();
    }

    /**
     * Selects the resource.<p>
     * 
     * @param dialogMode the gallery mode 
     */
    // TODO: add parameters if necessary
    public void select(GalleryMode dialogMode) {

        switch (dialogMode) {
            case widget:
                // write the link to the selected resource back to the fck editor
                String linkPath = CmsCoreProvider.get().link(m_infoBean.getResourcePath());
                CmsPreviewUtil.setResourcePath(m_fieldId, linkPath);
                break;
            case sitemap:
                break;
            case ade:
            case view:
            default:
                break;
        }

        m_handler.onSelect(dialogMode);
    }

    /**
     * Selects the resource.<p>
     * @return
     */
    private boolean onSelect() {

        // linkpath , title, description        
        if (m_previewDialog.getPropertiesTab().isChanged()) {

            // TODO: call the confirmation dialog to save the properties
        } else {
            String linkPath = CmsCoreProvider.get().link(m_infoBean.getResourcePath());
            String title = m_infoBean.getTitle();
            String description = m_infoBean.getDescription();
            // TODO: read the target from select box
            String target = "_blank";
            if (CmsEditorPreviewUtil.isTextSelected()) {
                // text selected.
                CmsEditorPreviewUtil.updateLink(linkPath, title, target);
            } else {
                // insert new link 
                CmsEditorPreviewUtil.pasteLink(linkPath, title, description, target);

            }
        }
        // TODO: should be tested! does this work??
        // close the dialog, return true
        return true;
    }
}
