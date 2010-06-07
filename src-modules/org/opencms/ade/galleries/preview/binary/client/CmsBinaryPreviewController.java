/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/binary/client/Attic/CmsBinaryPreviewController.java,v $
 * Date   : $Date: 2010/06/07 08:07:40 $
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

package org.opencms.ade.galleries.preview.binary.client;

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
 * @version $Revision: 1.2 $ 
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
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewController#closeGalleryDialog()
     */
    public boolean closeGalleryDialog() {

        if (m_previewDialog.getGalleryMode() == GalleryMode.editor) {
            if (m_previewDialog.hasChangedProperties()) {
                //TODO: Save properties
                return false;
            } else {
                CmsPreviewUtil.setLink(
                    CmsCoreProvider.get().link(m_infoBean.getResourcePath()),
                    m_infoBean.getTitle(),
                    "");
                return true;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

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
}
