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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.contenteditor.client.CmsContentEditor;
import org.opencms.ade.contenteditor.client.CmsEditorContext;
import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsEditableData;
import org.opencms.gwt.client.I_CmsEditableData;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog.DialogOptions;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsUUID;

import com.google.gwt.dom.client.Element;
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
    CmsContainerpageHandler m_handler;

    /** Flag indicating the content editor is currently opened. */
    private boolean m_editorOpened;

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
        m_editorOpened = false;
    }

    /**
     * @see org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler#onClose(java.lang.String, org.opencms.util.CmsUUID, boolean)
     */
    public void onClose(String sitePath, CmsUUID structureId, boolean isNew) {

        if (m_currentElementId == null) {
            m_currentElementId = structureId.toString();
        }
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
        m_editorOpened = false;
    }

    /**
     * Opens the XML content editor.<p>
     *
     * @param element the container element widget
     * @param inline <code>true</code> to open the in-line editor for the given element if available
     */
    public void openDialog(final CmsContainerPageElementPanel element, final boolean inline) {

        if (!m_editorOpened) {
            m_editorOpened = true;
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
                    editableData.setMainLanguage(m_handler.m_controller.getData().getMainLocale());
                    CmsContentEditorDialog.get().openEditDialog(
                        editableData,
                        false,
                        null,
                        new DialogOptions(),
                        CmsContentEditorHandler.this);
                }
            };

            if (m_handler.m_controller.getData().isUseClassicEditor() || element.isNewEditorDisabled()) {
                classicEdit.run();
            } else {
                String editorLocale = CmsCoreProvider.get().getLocale();
                String mainLocale = m_handler.m_controller.getData().getMainLocale();
                if (mainLocale == null) {
                    Element htmlEl = CmsDomUtil.querySelector("[about*='" + serverId + "']", element.getElement());
                    if (htmlEl != null) {
                        String entityId = htmlEl.getAttribute("about");
                        mainLocale = CmsContentDefinition.getLocaleFromId(entityId);
                    }
                }
                Command onClose = new Command() {

                    public void execute() {

                        addClosedEditorHistoryItem();
                        onClose(element.getSitePath(), new CmsUUID(serverId), false);
                    }
                };
                if (inline && CmsContentEditor.hasEditable(element.getElement())) {
                    addEditingHistoryItem(true);
                    CmsEditorContext context = getEditorContext();
                    context.setHtmlContextInfo(getContextInfo(element));
                    // remove expired style before initializing the editorm_dependingElementId
                    element.setReleasedAndNotExpired(true);

                    CmsContentEditor.getInstance().openInlineEditor(
                        context,
                        new CmsUUID(serverId),
                        editorLocale,
                        element,
                        mainLocale,
                        onClose);
                } else {
                    addEditingHistoryItem(false);

                    CmsContentEditor.getInstance().openFormEditor(
                        getEditorContext(),
                        editorLocale,
                        serverId,
                        null,
                        null,
                        null,
                        null,
                        mainLocale,
                        onClose);
                }
            }
        } else {
            CmsDebugLog.getInstance().printLine("Editor is already being opened.");
        }
    }

    /**
     * Opens the XML content editor.<p>
     *
     * @param editableData the data of the element to edit
     * @param isNew <code>true</code> if a new resource should be created
     * @param dependingElementId the id of a depending element
     * @param mode the element creation mode
     */
    public void openDialog(
        final I_CmsEditableData editableData,
        final boolean isNew,
        String dependingElementId,
        String mode) {

        if (!m_editorOpened) {
            m_editorOpened = true;
            m_handler.disableToolbarButtons();
            m_handler.deactivateCurrentButton();
            if ((editableData.getStructureId() != null) && !isNew) {
                m_currentElementId = editableData.getStructureId().toString();
            } else {
                m_currentElementId = null;
            }
            m_dependingElementId = dependingElementId;
            if (m_handler.m_controller.getData().isUseClassicEditor()) {
                CmsContentEditorDialog.get().openEditDialog(editableData, isNew, mode, new DialogOptions(), this);
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
                    getEditorContext(),
                    CmsCoreProvider.get().getLocale(),
                    editableData.getStructureId().toString(),
                    newLink,
                    null,
                    editableData.getPostCreateHandler(),
                    mode,
                    m_handler.m_controller.getData().getMainLocale(),
                    new Command() {

                        public void execute() {

                            addClosedEditorHistoryItem();
                            onClose(editableData.getSitePath(), editableData.getStructureId(), isNew);
                        }
                    });
            }
        } else {
            CmsDebugLog.getInstance().printLine("Editor is already being opened.");
        }
    }

    /**
     * Opens the content editor according to the history hash.<p>
     *
     * @param historyHash the history hash
     */
    public void openEditorForHistory(String historyHash) {

        if (historyHash.startsWith(EDITOR_HASH_KEY)) {
            if (!m_editorOpened) {
                m_editorOpened = true;
                CmsDebugLog.getInstance().printLine("EditorHandler - Opening editor from history");
                m_handler.m_controller.setContentEditing(true);
                String id = historyHash.substring(EDITOR_HASH_KEY.length(), historyHash.indexOf(";"));
                if (id.contains(",")) {
                    String[] ids = id.split(",");
                    m_currentElementId = URL.decodePathSegment(ids[0]);
                    m_dependingElementId = URL.decodePathSegment(ids[1]);
                } else {
                    m_currentElementId = URL.decodePathSegment(id);
                }
                Command onClose = new Command() {

                    public void execute() {

                        addClosedEditorHistoryItem();
                        onClose(null, new CmsUUID(getCurrentElementId()), false);
                    }
                };
                String editorLocale = CmsCoreProvider.get().getLocale();

                CmsContentEditor.getInstance().openFormEditor(
                    getEditorContext(),
                    editorLocale,
                    m_currentElementId,
                    null,
                    null,
                    null,
                    null,
                    m_handler.m_controller.getData().getMainLocale(),
                    onClose);
            }
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
     * Gets the editor context to use for the Acacia editor.<p>
     *
     * @return the editor context
     */
    CmsEditorContext getEditorContext() {

        CmsEditorContext result = new CmsEditorContext();
        result.getPublishParameters().put(
            CmsPublishOptions.PARAM_CONTAINERPAGE,
            "" + CmsCoreProvider.get().getStructureId());
        result.getPublishParameters().put(
            CmsPublishOptions.PARAM_DETAIL,
            "" + CmsContainerpageController.get().getData().getDetailId());
        result.getPublishParameters().put(CmsPublishOptions.PARAM_START_WITH_CURRENT_PAGE, "");
        return result;
    }

    /**
     * Adds a history item for the opened editor.<p>
     * Use the prohibitReturn flag to deny a return to the opened editor through the browser history.
     * Use this feature for inline editing or when opening the editor for new resources.<p>
     *
     * @param prohibitReturn if <code>true</code> returning to the opened editor through the browser history is denied
     */
    private void addEditingHistoryItem(boolean prohibitReturn) {

        if (prohibitReturn) {
            History.newItem(EDITOR_FOR_NO_RETURN_HASH_KEY, false);
        } else {
            History.newItem(
                EDITOR_HASH_KEY
                    + CmsContainerpageController.getServerId(getCurrentElementId())
                    + (m_dependingElementId != null ? "," + m_dependingElementId + ";" : ";"),
                false);
        }
    }

    /**
     * Returns the HTML context info for the given element.<p>
     *
     * @param element the edited element
     *
     * @return the JSON string
     */
    private String getContextInfo(CmsContainerPageElementPanel element) {

        CmsContainerPageContainer container;
        if (m_handler.m_controller.isGroupcontainerEditing()) {
            container = (CmsContainerPageContainer)((CmsContainerPageElementPanel)element.getParentTarget()).getParentTarget();
        } else {
            container = (CmsContainerPageContainer)element.getParentTarget();
        }
        return "{"
            + CmsCntPageData.JSONKEY_ELEMENT_ID
            + ":'"
            + element.getId()
            + "', "
            + (m_handler.m_controller.getData().getDetailId() != null
            ? (CmsCntPageData.JSONKEY_DETAIL_ELEMENT_ID + ":'" + m_handler.m_controller.getData().getDetailId() + "', ")
            : "")
            + CmsCntPageData.JSONKEY_NAME
            + ":'"
            + container.getContainerId()
            + "', "
            + CmsCntPageData.JSONKEY_TYPE
            + ": '"
            + container.getContainerType()
            + "', "
            + CmsCntPageData.JSONKEY_WIDTH
            + ": "
            + container.getConfiguredWidth()
            + ", "
            + CmsCntPageData.JSONKEY_DETAILVIEW
            + ": "
            + container.isDetailView()
            + ", "
            + CmsCntPageData.JSONKEY_DETAILONLY
            + ": "
            + container.isDetailOnly()
            + ", "
            + CmsCntPageData.JSONKEY_MAXELEMENTS
            + ": "
            + 1
            + "}";
    }

}
