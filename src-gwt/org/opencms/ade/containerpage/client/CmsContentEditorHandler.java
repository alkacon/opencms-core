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
import com.google.gwt.user.client.History;

/**
 * The container-page editor implementation of the XML content editor handler.<p>
 * 
 * @since 8.0.0
 */
public class CmsContentEditorHandler implements I_CmsContentEditorHandler {

    /** Content editor hash key whre a return to the opened editor is not possible. */
    private static final String EDITOR_FOR_NO_RETURN_HASH_KEY = "cE";

    /** Content editor hash key used for history management. */
    private static final String EDITOR_HASH_KEY = "cE:";

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
     * Closes the content editor.<p>
     */
    public void closeContentEditor() {

        CmsContentEditor.getInstance().closeEditor();
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
        if (m_currentElementId != null) {
            m_handler.addToRecent(m_currentElementId);
        }
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
        String serverId = CmsContainerpageController.getServerId(getCurrentElementId());
        if (m_handler.m_controller.getData().isUseClassicEditor() || element.isNewEditorDisabled()) {
            CmsEditableData editableData = new CmsEditableData();
            editableData.setElementLanguage(CmsCoreProvider.get().getLocale());
            editableData.setStructureId(new CmsUUID(serverId));
            editableData.setSitePath(element.getSitePath());
            CmsContentEditorDialog.get().openEditDialog(editableData, false, CmsContentEditorHandler.this);
        } else {
            String editorLocale = CmsCoreProvider.get().getLocale();

            Command onClose = new Command() {

                public void execute() {

                    addClosedEditorHistoryItem();
                    onClose(element.getSitePath(), false);
                }
            };
            if (inline && CmsContentEditor.hasEditable(element.getElement())) {
                addEditingHistoryItem(true);
                CmsContentEditor.getInstance().openInlineEditor(new CmsUUID(serverId), editorLocale, element, onClose);
            } else {
                addEditingHistoryItem(false);
                CmsContentEditor.getInstance().openFormEditor(editorLocale, serverId, null, null, onClose);
            }
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
        if ((editableData.getStructureId() != null) && !isNew) {
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
            addEditingHistoryItem(isNew);
            CmsContentEditor.getInstance().openFormEditor(
                CmsCoreProvider.get().getLocale(),
                editableData.getStructureId().toString(),
                newLink,
                null,
                new Command() {

                    public void execute() {

                        addClosedEditorHistoryItem();
                        onClose(editableData.getSitePath(), isNew);
                    }
                });
        }
    }

    /**
     * Opens the content editor according to the history hash.<p>
     * 
     * @param historyHash the history hash
     */
    public void openEditorForHistory(String historyHash) {

        m_handler.m_controller.setContentEditing(true);
        if (historyHash.startsWith(EDITOR_HASH_KEY)) {
            String id = historyHash.substring(EDITOR_HASH_KEY.length(), historyHash.indexOf(";"));
            if (id.contains(",")) {
                String[] ids = id.split(",");
                m_currentElementId = ids[0];
                m_dependingElementId = ids[1];
            } else {
                m_currentElementId = id;
            }
            Command onClose = new Command() {

                public void execute() {

                    addClosedEditorHistoryItem();
                    onClose(null, false);
                }
            };
            String editorLocale = CmsCoreProvider.get().getLocale();
            CmsContentEditor.getInstance().openFormEditor(editorLocale, m_currentElementId, null, null, onClose);
        } else {
            closeContentEditor();
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

    /**
     * Adds a history item for the closed editor.<p>
     */
    void addClosedEditorHistoryItem() {

        History.newItem("", false);
    }

    /**
     * Adds a history item for the opened editor.<p>
     * Use the prihibitReturn flag to deny a return to the opened editor through the browser history. 
     * Use this feature for inline editing or when opening the editor for new resources.<p> 
     * 
     * @param prohibitReturn if <code>true</code> returning to the opened editor through the browser history is denied
     */
    private void addEditingHistoryItem(boolean prohibitReturn) {

        if (prohibitReturn) {
            History.newItem(EDITOR_FOR_NO_RETURN_HASH_KEY, false);
        } else {
            History.newItem(EDITOR_HASH_KEY
                + CmsContainerpageController.getServerId(getCurrentElementId())
                + (m_dependingElementId != null ? "," + m_dependingElementId + ";" : ";"), false);
        }
    }
}
