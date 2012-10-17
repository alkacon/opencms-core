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

import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.contenteditor.client.CmsContentEditor;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsEditableData;
import org.opencms.gwt.client.I_CmsEditableData;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.util.CmsUUID;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
        m_handler.enableToolbarButtons();
        m_handler.activateSelection();
        m_handler.m_controller.setContentEditing(false);
        m_handler.m_controller.reInitInlineEditing();
        m_currentElementId = null;
    }

    /**
     * Opens the XML content editor.<p>
     * 
     * @param element the container element widget
     * @param inline <code>true</code> to open the in-line editor for the given element if available
     */
    public void openDialog(final CmsContainerPageElementPanel element, final boolean inline) {

        m_handler.disableToolbarButtons();
        m_handler.deactivateCurrentButton();
        m_currentElementId = element.getId();
        final String serverId = CmsContainerpageController.getServerId(getCurrentElementId());
        final Runnable classicEdit = new Runnable() {

            public void run() {

                CmsEditableData editableData = new CmsEditableData();
                editableData.setElementLanguage(CmsCoreProvider.get().getLocale());
                editableData.setStructureId(new CmsUUID(serverId));
                editableData.setSitePath(element.getSitePath());
                CmsContentEditorDialog.get().openEditDialog(editableData, false, CmsContentEditorHandler.this);
            }
        };
        if (m_handler.m_controller.getData().isUseClassicEditor()) {
            classicEdit.run();
        } else {
            final String editorLocale = CmsCoreProvider.get().getLocale();
            m_handler.checkNewWidgetsAvailable(new CmsUUID(serverId), new AsyncCallback<Boolean>() {

                public void onFailure(Throwable caught) {

                    // TODO: Auto-generated method stub

                }

                public void onSuccess(Boolean result) {

                    if (result.booleanValue()) {
                        Command onClose = new Command() {

                            public void execute() {

                                onClose(element.getSitePath(), false);
                            }
                        };
                        if (inline && CmsContentEditor.hasEditable(element.getElement())) {
                            CmsContentEditor.getInstance().openInlineEditor(
                                new CmsUUID(serverId),
                                editorLocale,
                                element,
                                onClose);
                        } else {
                            CmsContentEditor.getInstance().openFormEditor(editorLocale, serverId, null, null, onClose);
                        }
                    } else {
                        classicEdit.run();
                    }
                }

            });
        }
    }

    /**
     * Opens the XML content editor.<p>
     * 
     * @param editableData the data of the element to edit
     * @param isNew <code>true</code> if a new resource should be created
     * @param dependingElementId the id of a depending element
     */
    public void openDialog(final I_CmsEditableData editableData, final boolean isNew, String dependingElementId) {

        m_handler.disableToolbarButtons();
        m_handler.deactivateCurrentButton();
        if (editableData.getStructureId() != null) {
            m_currentElementId = editableData.getStructureId().toString();
        } else {
            m_currentElementId = null;
        }
        m_dependingElementId = dependingElementId;
        if (m_handler.m_controller.getData().isUseClassicEditor()) {
            CmsContentEditorDialog.get().openEditDialog(editableData, isNew, this);
        } else {
            String newLink = null;
            if (isNew) {
                newLink = editableData.getNewLink();
                // the new link is URL encoded twice, decode it
                newLink = URL.decodeQueryString(newLink);
                newLink = URL.decodeQueryString(newLink);
            }
            CmsContentEditor.getInstance().openFormEditor(
                CmsCoreProvider.get().getLocale(),
                CmsContainerpageController.getServerId(getCurrentElementId()),
                newLink,
                null,
                new Command() {

                    public void execute() {

                        onClose(editableData.getSitePath(), isNew);
                    }
                });
        }
    }

    /**
     * Returns the currently edited element's id.<p>
     *
     * @return the currently edited element's id
     */
    protected String getCurrentElementId() {

        return m_currentElementId;
    }
}
