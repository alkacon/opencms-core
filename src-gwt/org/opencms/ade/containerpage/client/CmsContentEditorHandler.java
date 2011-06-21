/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.client;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsEditableData;
import org.opencms.gwt.client.I_CmsEditableData;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.util.CmsUUID;

/**
 * The container-page editor implementation of the XML content editor handler.<p>
 * 
 * @since 8.0.0
 */
public class CmsContentEditorHandler implements I_CmsContentEditorHandler {

    /** The currently edited element's id. */
    private String m_currentElementId;

    /** The depending element's id. */
    private String m_dependingElementId;

    /** The container-page handler. */
    private CmsContainerpageHandler m_handler;

    /**
     * Constructor.<p>
     * 
     * @param handler the container-page handler
     */
    public CmsContentEditorHandler(CmsContainerpageHandler handler) {

        m_handler = handler;
    }

    /**
     * @see org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler#onClose(java.lang.String, boolean)
     */
    public void onClose(String sitePath, boolean isNew) {

        if (m_dependingElementId != null) {
            m_handler.reloadElements(m_currentElementId, m_dependingElementId);
            m_dependingElementId = null;
        } else {
            m_handler.reloadElements(m_currentElementId);
        }
        m_handler.addToRecent(m_currentElementId);
        m_currentElementId = null;
    }

    /**
     * Opens the XML content editor.<p>
     * 
     * @param editableData the data of the element to edit
     * @param isNew <code>true</code> if a new resource should be created
     * @param dependingElementId the id of a depending element
     */
    public void openDialog(I_CmsEditableData editableData, boolean isNew, String dependingElementId) {

        if (editableData.getStructureId() != null) {
            m_currentElementId = editableData.getStructureId().toString();
        } else {
            m_currentElementId = null;
        }
        m_dependingElementId = dependingElementId;
        CmsContentEditorDialog.get().openEditDialog(editableData, isNew, this);
    }

    /**
     * Opens the XML content editor.<p>
     * 
     * @param elementId the element id
     * @param sitePath the element site-path
     */
    public void openDialog(String elementId, String sitePath) {

        m_currentElementId = elementId;
        CmsEditableData editableData = new CmsEditableData();
        editableData.setElementLanguage(CmsCoreProvider.get().getLocale());
        editableData.setStructureId(new CmsUUID(CmsContainerpageController.getServerId(m_currentElementId)));
        editableData.setSitePath(sitePath);
        CmsContentEditorDialog.get().openEditDialog(editableData, false, this);
    }
}
