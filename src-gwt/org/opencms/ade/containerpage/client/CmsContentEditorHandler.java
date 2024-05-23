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

import org.opencms.ade.containerpage.client.CmsContainerpageController.I_ReloadHandler;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsOptionDialog;
import org.opencms.ade.containerpage.client.ui.I_CmsDropContainer;
import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsDialogOptionsAndInfo;
import org.opencms.ade.contenteditor.client.CmsContentEditor;
import org.opencms.ade.contenteditor.client.CmsEditorContext;
import org.opencms.ade.contenteditor.client.I_CmsEditorCloseHandler;
import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.ade.contenteditor.shared.CmsEditHandlerData;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsEditableData;
import org.opencms.gwt.client.I_CmsEditableData;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog.DialogOptions;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;

import jsinterop.base.Js;

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

    /** The container-page handler. */
    CmsContainerpageHandler m_handler;

    /** The content element to be replaced by the edited content. */
    CmsContainerPageElementPanel m_replaceElement;

    /** The currently edited element's id. */
    private String m_currentElementId;

    /** The depending element's id. */
    private String m_dependingElementId;

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
     * @see org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler#onClose(java.lang.String, org.opencms.util.CmsUUID, boolean, boolean, boolean)
     */
    public void onClose(
        String sitePath,
        CmsUUID structureId,
        boolean isNew,
        boolean hasChangedSettings,
        boolean usedPublishDialog) {

        if (m_currentElementId == null) {
            m_currentElementId = structureId.toString();
        }
        // we keep track of how many reloads are still ongoing - if all of them are done, we can send the edit events
        final int[] reloadCounter = {0};

        I_ReloadHandler reloadHandler = new I_ReloadHandler() {

            private List<Runnable> m_todo = new ArrayList<>();

            @Override
            public void finish() {

                if (usedPublishDialog) {
                    m_handler.m_controller.startPublishLockCheck();
                }
                reloadCounter[0] -= 1;
                if (reloadCounter[0] <= 0) {
                    m_todo.forEach(item -> item.run());
                }

            }

            @Override
            public void onReload(CmsContainerPageElementPanel oldElement, CmsContainerPageElementPanel newElement) {

                m_todo.add(() -> {
                    m_handler.m_controller.sendElementEditedContent(
                        newElement,
                        Js.cast(oldElement.getElement()),
                        isNew);

                });
            }

        };

        if (m_replaceElement != null) {
            if ((m_handler.m_controller.getData().getDetailId() != null)
                && m_replaceElement.getId().startsWith(m_handler.m_controller.getData().getDetailId().toString())) {
                Window.Location.assign(
                    CmsStringUtil.joinPaths(
                        CmsCoreProvider.get().getVfsPrefix(),
                        CmsContainerpageController.getCurrentUri(),
                        CmsContainerpageController.getServerId(m_currentElementId)));
            }
            reloadCounter[0] += 1;
            m_handler.m_controller.replaceElement(
                m_replaceElement,
                m_currentElementId,
                reloadHandler);
            m_replaceElement = null;
            if (m_dependingElementId != null) {
                reloadCounter[0] += 1;
                m_handler.m_controller.reloadElements(new String[] {m_dependingElementId}, reloadHandler);
                m_dependingElementId = null;
            }
        } else if (m_dependingElementId != null) {
            reloadCounter[0] += 1;
            m_handler.m_controller.reloadElements(new String[] {m_currentElementId, m_dependingElementId}, reloadHandler);
            m_dependingElementId = null;
        } else {
            reloadCounter[0] += 1;
            m_handler.m_controller.reloadElements(new String[] {m_currentElementId}, reloadHandler);
        }
        if (m_currentElementId != null) {
            m_handler.addToRecent(m_currentElementId);
        }
        m_handler.enableToolbarButtons();
        m_handler.activateSelection();
        m_handler.m_controller.setContentEditing(false);
        m_handler.m_controller.reInitInlineEditing();
        m_currentElementId = null;
        if (hasChangedSettings) {
            m_handler.m_controller.setPageChanged(new Runnable[] {});
        }
        m_editorOpened = false;
    }

    /**
     * Opens the XML content editor.<p>
     *
     * @param element the container element widget
     * @param inline <code>true</code> to open the in-line editor for the given element if available
     * @param wasNew <code>true</code> in case this is a newly created element not previously edited
     */
    public void openDialog(final CmsContainerPageElementPanel element, final boolean inline, boolean wasNew) {

        if (!inline && element.hasEditHandler()) {
            m_handler.m_controller.getEditOptions(
                element.getId(),
                false,
                new I_CmsSimpleCallback<CmsDialogOptionsAndInfo>() {

                    public void execute(CmsDialogOptionsAndInfo editOptions) {

                        final I_CmsSimpleCallback<CmsUUID> editCallBack = new I_CmsSimpleCallback<CmsUUID>() {

                            public void execute(CmsUUID arg) {

                                String contentId = element.getId();
                                if (!element.getId().startsWith(arg.toString())) {
                                    // the content structure ID has changed, the current element needs to be replaced after editing
                                    m_replaceElement = element;
                                    contentId = arg.toString();
                                }
                                internalOpenDialog(element, contentId, inline, wasNew);
                            }
                        };
                        if (editOptions == null) {
                            internalOpenDialog(element, element.getId(), inline, wasNew);
                        } else if (editOptions.getOptions().getOptions().size() == 1) {
                            m_handler.m_controller.prepareForEdit(
                                element.getId(),
                                editOptions.getOptions().getOptions().get(0).getValue(),
                                editCallBack);
                        } else {
                            CmsOptionDialog dialog = new CmsOptionDialog(
                                Messages.get().key(Messages.GUI_EDIT_HANDLER_SELECT_EDIT_OPTION_0),
                                editOptions.getOptions(),
                                editOptions.getInfo(),
                                new I_CmsSimpleCallback<String>() {

                                    public void execute(String arg) {

                                        m_handler.m_controller.prepareForEdit(element.getId(), arg, editCallBack);
                                    }
                                });
                            dialog.addDialogClose(new Command() {

                                public void execute() {

                                    cancelEdit();
                                }
                            });
                            dialog.center();
                        }
                    }
                });
        } else {
            internalOpenDialog(element, element.getId(), inline, wasNew);
        }
    }

    /**
     * Opens the XML content editor, checking for if an edit handler is configured first.<p>
     *
     * @param editableData the data of the element to edit
     * @param isNew <code>true</code> if a new resource should be created
     * @param dependingElementId the id of a depending element
     * @param mode the element creation mode
     * @param handlerDataForNew the edit handler data, if we are using an edit handler to create a new element; null otherwise
     */
    public void openDialog(
        final I_CmsEditableData editableData,
        final boolean isNew,
        final String dependingElementId,
        final String mode,
        final CmsEditHandlerData handlerDataForNew) {

        if (!m_editorOpened) {
            m_editorOpened = true;
            m_handler.disableToolbarButtons();
            m_handler.deactivateCurrentButton();

            if (!isNew && (editableData.getStructureId() != null) && editableData.hasEditHandler()) {
                final String elementId = CmsContentEditor.getClientIdForEditable(editableData);
                m_handler.m_controller.getEditOptions(
                    elementId,
                    true,
                    new I_CmsSimpleCallback<CmsDialogOptionsAndInfo>() {

                        public void execute(CmsDialogOptionsAndInfo editOptions) {

                            final I_CmsSimpleCallback<CmsUUID> editCallBack = new I_CmsSimpleCallback<CmsUUID>() {

                                public void execute(CmsUUID arg) {

                                    I_CmsEditableData data = editableData;
                                    if (!data.getStructureId().equals(arg)) {
                                        // the content structure ID has changed, change the editableData
                                        data = new CmsEditableData(data);
                                        ((CmsEditableData)data).setStructureId(arg);
                                    }
                                    internalOpenDialog(data, isNew, dependingElementId, mode, null);
                                }
                            };
                            if (editOptions == null) {
                                internalOpenDialog(editableData, isNew, dependingElementId, mode, null);
                            } else if (editOptions.getOptions().getOptions().size() == 1) {
                                m_handler.m_controller.prepareForEdit(
                                    elementId,
                                    editOptions.getOptions().getOptions().get(0).getValue(),
                                    editCallBack);
                            } else {
                                CmsOptionDialog dialog = new CmsOptionDialog(
                                    Messages.get().key(Messages.GUI_EDIT_HANDLER_SELECT_EDIT_OPTION_0),
                                    editOptions.getOptions(),
                                    editOptions.getInfo(),
                                    new I_CmsSimpleCallback<String>() {

                                        public void execute(String arg) {

                                            m_handler.m_controller.prepareForEdit(elementId, arg, editCallBack);
                                        }
                                    });
                                dialog.addDialogClose(new Command() {

                                    public void execute() {

                                        cancelEdit();
                                    }
                                });
                                dialog.center();
                            }
                        }
                    });
            } else {
                internalOpenDialog(editableData, isNew, dependingElementId, mode, handlerDataForNew);
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
                I_CmsEditorCloseHandler onClose = new I_CmsEditorCloseHandler() {

                    public void onClose(boolean hasChangedSettings, boolean usedPublishDialog) {

                        addClosedEditorHistoryItem();
                        CmsContentEditorHandler.this.onClose(
                            null,
                            new CmsUUID(getCurrentElementId()),
                            false,
                            hasChangedSettings,
                            usedPublishDialog);
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
                    null,
                    m_handler.m_controller.getData().getMainLocale(),
                    null,
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
     * Cancels opening the editor.<p>
     */
    void cancelEdit() {

        m_handler.enableToolbarButtons();
        m_handler.activateSelection();
        m_handler.m_controller.setContentEditing(false);
        m_handler.m_controller.reInitInlineEditing();
        m_replaceElement = null;
        m_dependingElementId = null;
        m_currentElementId = null;
        m_editorOpened = false;
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
        elemental2.dom.HTMLMetaElement meta = Js.cast(
            elemental2.dom.DomGlobal.document.querySelector(
                "meta[name=" + CmsGwtConstants.META_EDITOR_STYLESHEET + "]"));
        if (meta != null) {
            result.setEditorStylesheet(meta.content);
        }
        return result;
    }

    /**
     * Opens the edit dialog.<p>
     *
     * @param element the element to edit
     * @param editContentId the edit content id
     * @param inline <code>true</code> to edit the content inline
     * @param wasNew <code>true</code> in case this is a newly created element not previously edited
     */
    void internalOpenDialog(
        final CmsContainerPageElementPanel element,
        String editContentId,
        final boolean inline,
        boolean wasNew) {

        if (!m_editorOpened) {
            m_editorOpened = true;
            m_handler.disableToolbarButtons();
            m_handler.deactivateCurrentButton();
            m_currentElementId = editContentId;
            final String serverId = CmsContainerpageController.getServerId(m_currentElementId);
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
                final String mainLocale;
                if (m_handler.m_controller.getData().getMainLocale() == null) {
                    Element htmlEl = CmsDomUtil.querySelector(
                        "[" + CmsGwtConstants.ATTR_DATA_ID + "*='" + serverId + "']",
                        element.getElement());
                    if (htmlEl != null) {
                        String entityId = htmlEl.getAttribute(CmsGwtConstants.ATTR_DATA_ID);
                        mainLocale = CmsContentDefinition.getLocaleFromId(entityId);
                    } else {
                        mainLocale = null;
                    }
                } else {
                    mainLocale = m_handler.m_controller.getData().getMainLocale();
                }
                I_CmsEditorCloseHandler onClose = new I_CmsEditorCloseHandler() {

                    public void onClose(boolean hasChangedSettings, boolean usedPublishDialog) {

                        addClosedEditorHistoryItem();
                        CmsContentEditorHandler.this.onClose(
                            element.getSitePath(),
                            new CmsUUID(serverId),
                            wasNew,
                            hasChangedSettings,
                            usedPublishDialog);

                    }
                };
                if (inline && CmsContentEditor.hasEditable(element.getElement())) {
                    addEditingHistoryItem(true);
                    CmsEditorContext context = getEditorContext();
                    context.setReusedElement(element.isReused());
                    context.setHtmlContextInfo(getContextInfo(element));
                    // remove expired style before initializing the editor
                    element.setReleasedAndNotExpired(true);
                    // in case of new elements, ignore load time
                    long loadTime = wasNew ? Long.MAX_VALUE : m_handler.m_controller.getLoadTime();
                    CmsContentEditor.getInstance().openInlineEditor(
                        context,
                        new CmsUUID(serverId),
                        editorLocale,
                        element,
                        mainLocale,
                        loadTime,
                        onClose);
                } else {
                    addEditingHistoryItem(false);
                    Map<String, String> settingPresets = new HashMap<String, String>();
                    CmsEditorContext context = getEditorContext();
                    context.setReusedElement(element.isReused());
                    I_CmsDropContainer dropContainer = element.getParentTarget();
                    if (dropContainer instanceof CmsContainerPageContainer) {
                        CmsContainerPageContainer container = (CmsContainerPageContainer)dropContainer;
                        settingPresets.putAll(container.getSettingPresets());
                    }
                    context.setSettingPresets(settingPresets);

                    boolean allowSettings = m_handler.m_controller.getData().allowSettingsInEditor()
                        && !m_handler.m_controller.isEditingDisabled()
                        && !serverId.equals(String.valueOf(m_handler.m_controller.getData().getDetailId()));
                    I_CmsSimpleCallback<Boolean> openEditor = new I_CmsSimpleCallback<Boolean>() {

                        public void execute(Boolean lockedPage) {

                            CmsContentEditor.getInstance().openFormEditor(
                                context,
                                editorLocale,
                                serverId,
                                lockedPage.booleanValue() ? getCurrentElementId() : null,
                                null,
                                null,
                                null,
                                null,
                                mainLocale,
                                null,
                                onClose);
                        }
                    };
                    if (allowSettings) {
                        if (m_handler.m_controller.getData().getDetailContainerPage() != null) {
                            CmsCoreProvider.get().lock(
                                m_handler.m_controller.getData().getDetailContainerPage(),
                                m_handler.m_controller.getLoadTime(),
                                openEditor);
                        } else {
                            CmsCoreProvider.get().lock(
                                CmsCoreProvider.get().getStructureId(),
                                m_handler.m_controller.getLoadTime(),
                                openEditor);
                        }
                    } else {
                        openEditor.execute(Boolean.FALSE);
                    }

                }
            }
        } else {
            CmsDebugLog.getInstance().printLine("Editor is already being opened.");
        }
    }

    /**
     * Opens the XML content editor internally.<p>
     *
     * @param editableData the data of the element to edit
     * @param isNew <code>true</code> if a new resource should be created
     * @param dependingElementId the id of a depending element
     * @param mode the element creation mode
     * @param editHandlerData the edit handler data, if we are using an edit handler to create a new element; null otherwise
     */
    void internalOpenDialog(
        final I_CmsEditableData editableData,
        final boolean isNew,
        String dependingElementId,
        String mode,
        CmsEditHandlerData editHandlerData) {

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
                null,
                newLink,
                null,
                editableData.getPostCreateHandler(),
                mode,
                m_handler.m_controller.getData().getMainLocale(),
                editHandlerData,
                new I_CmsEditorCloseHandler() {

                    public void onClose(boolean hasChangedSettings, boolean usedPublishDialog) {

                        addClosedEditorHistoryItem();
                        CmsContentEditorHandler.this.onClose(
                            editableData.getSitePath(),
                            editableData.getStructureId(),
                            isNew,
                            hasChangedSettings,
                            usedPublishDialog);
                    }
                });
        }

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
        JSONObject result = new JSONObject();
        putString(result, CmsCntPageData.JSONKEY_ELEMENT_ID, element.getId());
        CmsUUID detailId = m_handler.m_controller.getData().getDetailId();
        if (detailId != null) {
            putString(result, CmsCntPageData.JSONKEY_DETAIL_ELEMENT_ID, "" + detailId);
        }
        putString(result, CmsCntPageData.JSONKEY_NAME, "" + container.getContainerId());
        putString(result, CmsCntPageData.JSONKEY_TYPE, "" + container.getContainerType());
        result.put(CmsCntPageData.JSONKEY_WIDTH, new JSONNumber(container.getConfiguredWidth()));
        result.put(CmsCntPageData.JSONKEY_DETAILVIEW, JSONBoolean.getInstance(container.isDetailView()));
        result.put(
            CmsCntPageData.JSONKEY_ISDETAILVIEWCONTAINER,
            JSONBoolean.getInstance(container.isDetailViewContainer()));
        result.put(CmsCntPageData.JSONKEY_DETAILONLY, JSONBoolean.getInstance(container.isDetailOnly()));
        result.put(CmsCntPageData.JSONKEY_MAXELEMENTS, new JSONNumber(1));
        JSONObject presets = new JSONObject();
        Map<String, String> presetsMap = container.getSettingPresets();
        for (Map.Entry<String, String> entry : presetsMap.entrySet()) {
            presets.put(entry.getKey(), new JSONString(entry.getValue()));
        }
        result.put(CmsCntPageData.JSONKEY_PRESETS, presets);
        return result.toString();
    }

    /**
     * Adds a String value to the JSON object.<p>
     *
     * @param obj the object to manipulate
     * @param key the key
     * @param val the value
     */
    private void putString(JSONObject obj, String key, String val) {

        obj.put(key, new JSONString(val));
    }

}
