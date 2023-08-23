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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsDialogOptions;
import org.opencms.ade.containerpage.shared.CmsDialogOptionsAndInfo;
import org.opencms.ade.contenteditor.client.CmsContentEditor;
import org.opencms.ade.contenteditor.shared.CmsEditHandlerData;
import org.opencms.ade.upload.client.I_CmsUploadContext;
import org.opencms.ade.upload.client.lists.CmsUploadPopup;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.A_CmsDirectEditButtons;
import org.opencms.gwt.client.ui.CmsCreateModeSelectionDialog;
import org.opencms.gwt.client.ui.CmsDeleteWarningDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Class to provide direct edit buttons within list collector elements.<p>
 *
 * @since 8.0.0
 */
public class CmsListCollectorEditor extends A_CmsDirectEditButtons {

    /** True if the parent element has offset height or width. */
    private boolean m_parentHasDimensions;

    /** The currently active upload popup. */
    private CmsUploadPopup m_uploadPopup;

    /**
     * Creates a new instance.<p>
     *
     * @param editable the editable element
     * @param parentId the parent id
     */
    public CmsListCollectorEditor(Element editable, String parentId) {

        super(editable, parentId);
    }

    /**
     * Creates the button to add an element to the user's favorites.<p>
     *
     * @return the created button
     */
    public CmsPushButton createFavButton() {

        CmsPushButton favButton = new CmsPushButton();
        favButton.setImageClass(I_CmsButton.ButtonData.ADD_TO_FAVORITES.getSmallIconClass());
        favButton.setTitle(I_CmsButton.ButtonData.ADD_TO_FAVORITES.getTitle());
        favButton.setButtonStyle(I_CmsButton.ButtonStyle.FONT_ICON, null);
        add(favButton);
        favButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                CmsContainerpageController.get().getHandler().addToFavorites(getContentId().toString());
            }
        });
        return favButton;
    }

    /**
     * Returns true if the element view of the element is compatible with the currently set element view in the container page editor.<p>
     *
     * @return true if the element should be visible in the current mode
     */
    public boolean isVisibleInCurrentView() {

        return CmsContainerpageController.get().matchRootView(m_editableData.getElementView());
    }

    /**
     * Sets the 'parentHasDimensions' flag.<p>
     *
     * @param parentHasDimensions the new value of the flag
     */
    public void setParentHasDimensions(boolean parentHasDimensions) {

        m_parentHasDimensions = parentHasDimensions;
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#setPosition(org.opencms.gwt.client.util.CmsPositionBean, com.google.gwt.dom.client.Element)
     */
    @Override
    public void setPosition(CmsPositionBean position, Element containerElement) {

        m_position = position;
        Element parent = CmsDomUtil.getPositioningParent(getElement());
        Style style = getElement().getStyle();
        int right = parent.getOffsetWidth()
            - ((m_position.getLeft() + m_position.getWidth()) - parent.getAbsoluteLeft());

        int top = m_position.getTop() - parent.getAbsoluteTop();
        if (m_position.getHeight() < 24) {
            // if the highlighted area has a lesser height than the buttons, center vertically
            top -= (24 - m_position.getHeight()) / 2;
        }

        if (top < 25) {
            // check if there is a parent option bar element present
            Element parentOptionBar = CmsDomUtil.getFirstChildWithClass(
                containerElement,
                I_CmsLayoutBundle.INSTANCE.containerpageCss().optionBar());
            if ((parentOptionBar != null) && !getElement().equals(parentOptionBar)) {
                int optBarTop = parentOptionBar.getAbsoluteTop();
                int optBarLeft = parentOptionBar.getAbsoluteLeft() + 22;
                if ((Math.abs(optBarLeft - (m_position.getLeft() + m_position.getWidth())) < 25)
                    && (Math.abs(optBarTop - m_position.getTop()) < 25)) {
                    // in case the edit buttons overlap, move to the left
                    right = ((parent.getOffsetWidth() + parent.getAbsoluteLeft()) - optBarLeft) + 25;
                }
            }
        }
        style.setRight(right, Unit.PX);
        style.setTop(top, Unit.PX);
        updateExpiredOverlayPosition(parent);
    }

    /**
     * Shows or hides the widget depending on the current view and whether the parent element has width or height.<p>
     *
     * @param editableContainer true if this list element is part of an element in an editable container
     */
    public void updateVisibility(boolean editableContainer) {

        boolean visible = m_parentHasDimensions && isVisibleInCurrentView() && editableContainer;
        setDisplayNone(!visible);

    }

    /**
     * Handles the 'default case' when using the new function on an editable element.<p>
     */
    protected void defaultNew() {

        CmsUUID referenceId = m_editableData.getStructureId();
        CmsCreateModeSelectionDialog.showDialog(referenceId, new AsyncCallback<String>() {

            public void onFailure(Throwable caught) {

                // is never called
            }

            public void onSuccess(String result) {

                openEditDialog(true, result, null);
                removeHighlighting();
            }
        });
    }

    /**
     * Delete the editable element from page and VFS.<p>
     */
    protected void deleteElement() {

        CmsContainerpageController.get().deleteElement(m_editableData.getStructureId().toString(), m_parentResourceId);
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#getAdditionalButtons()
     */
    @Override
    protected Map<Integer, CmsPushButton> getAdditionalButtons() {

        Map<Integer, CmsPushButton> result = Maps.newHashMap();
        // only show add to favorites and info button, in case there actually is a resource and not in case of create new only
        if (m_editableData.hasResource()) {
            if (m_editableData.canFavorite()) {
                result.put(Integer.valueOf(130), createFavButton());
            }
            result.put(Integer.valueOf(160), createInfoButton());
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#getInfoContext()
     */
    @Override
    protected Map<String, String> getInfoContext() {

        Map<String, String> result = new HashMap<>();
        String elementId = m_editableData.getElementId();
        if (elementId != null) {
            result.put(CmsGwtConstants.ATTR_ELEMENT_ID, elementId);
        }
        String uri = CmsCoreProvider.get().getUri();
        String siteRoot = CmsCoreProvider.get().getSiteRoot();
        String pageRootPath = CmsStringUtil.joinPaths(siteRoot, uri);
        result.put(CmsGwtConstants.ATTR_PAGE_ROOT_PATH, pageRootPath);
        return result;

    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#getUploadButtonTitle(java.lang.String)
     */
    @Override
    protected String getUploadButtonTitle(String uploadFolder) {

        return org.opencms.ade.galleries.client.Messages.get().key(
            org.opencms.ade.galleries.client.Messages.GUI_GALLERY_UPLOAD_TITLE_1,
            uploadFolder);
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#onClickDelete()
     */
    @Override
    protected void onClickDelete() {

        removeHighlighting();
        CmsDomUtil.ensureMouseOut(getElement());
        if (m_editableData.hasEditHandler()) {
            final String elementId = CmsContentEditor.getClientIdForEditable(m_editableData);
            final I_CmsSimpleCallback<String> deleteCallback = new I_CmsSimpleCallback<String>() {

                public void execute(String arg) {

                    if (CmsDialogOptions.REGULAR_DELETE.equals(arg)) {
                        openWarningDialog();
                    } else {
                        CmsContainerpageController.get().handleDelete(elementId, arg, new I_CmsSimpleCallback<Void>() {

                            public void execute(Void arg1) {

                                CmsContainerpageController.get().reloadElements(
                                    new String[] {getParentResourceId()},
                                    () -> {/*do nothing*/});
                            }
                        });
                    }
                }
            };
            CmsContainerpageController.get().getDeleteOptions(
                elementId,
                new I_CmsSimpleCallback<CmsDialogOptionsAndInfo>() {

                    public void execute(CmsDialogOptionsAndInfo arg) {

                        if (arg == null) {
                            deleteCallback.execute(CmsDialogOptions.REGULAR_DELETE);
                        } else if (arg.getOptions().getOptions().size() == 1) {
                            String deleteOpt = arg.getOptions().getOptions().get(0).getValue();
                            deleteCallback.execute(deleteOpt);

                        } else {
                            CmsOptionDialog dialog = new CmsOptionDialog(
                                Messages.get().key(Messages.GUI_EDIT_HANDLER_SELECT_DELETE_OPTION_0),
                                arg.getOptions(),
                                arg.getInfo(),
                                deleteCallback);
                            dialog.center();
                        }
                    }
                });
        } else {
            openWarningDialog();
        }
        m_delete.clearHoverState();
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#onClickEdit()
     */
    @Override
    protected void onClickEdit() {

        openEditDialog(false, null, null);
        removeHighlighting();
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#onClickNew(boolean)
     */
    @Override
    protected void onClickNew(boolean askCreateMode) {

        if (!askCreateMode) {
            openEditDialog(true, null, null);
            removeHighlighting();
        } else {
            if (m_editableData.hasEditHandler()) {
                final String elementId = CmsContentEditor.getClientIdForEditable(m_editableData);
                CmsCntPageData cntPageData = CmsContainerpageController.get().getData();
                final CmsUUID pageId = cntPageData.getRpcContext().getPageStructureId();
                final String requestParamStr = cntPageData.getRequestParams();

                final I_CmsSimpleCallback<String> newCallback = new I_CmsSimpleCallback<String>() {

                    public void execute(String choice) {

                        CmsEditHandlerData data = new CmsEditHandlerData(elementId, choice, pageId, requestParamStr);
                        openEditDialog(true, null, data);
                        removeHighlighting();

                    }
                };

                CmsContainerpageController.get().getNewOptions(
                    elementId,
                    new I_CmsSimpleCallback<CmsDialogOptionsAndInfo>() {

                        public void execute(CmsDialogOptionsAndInfo arg) {

                            if (arg == null) {
                                CmsDebugLog.consoleLog("dialog options null, using default behavior");
                                defaultNew();
                            } else {
                                CmsOptionDialog dialog = new CmsOptionDialog(
                                    null,
                                    arg.getOptions(),
                                    arg.getInfo(),
                                    newCallback);
                                dialog.center();
                            }

                        }
                    });
            } else {
                defaultNew();
            }
        }

    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsDirectEditButtons#onClickUpload()
     */
    @Override
    protected void onClickUpload() {

        removeHighlighting();
        I_CmsUploadContext context = new I_CmsUploadContext() {

            @SuppressWarnings("synthetic-access")
            public void onUploadFinished(List<String> uploadedFiles) {

                closeUploadPopup();
                CmsContainerpageController.get().reloadElements(
                    new String[] {getParentResourceId()},
                    () -> {/*do nothing*/});
            }
        };
        if (m_editableData.getStructureId() != null) {
            CmsRpcAction<CmsListInfoBean> action = new CmsRpcAction<CmsListInfoBean>() {

                @SuppressWarnings("synthetic-access")
                @Override
                public void execute() {

                    start(0, false);
                    CmsCoreProvider.get();
                    CmsCoreProvider.getVfsService().getUploadFolderInfo(
                        m_editableData.getExtensions().getUploadFolder(),
                        this);
                }

                @SuppressWarnings("synthetic-access")
                @Override
                protected void onResponse(CmsListInfoBean result) {

                    stop(false);
                    setUploadPopup(
                        new CmsUploadPopup(
                            m_editableData.getExtensions().getUploadFolder(),
                            m_editableData.getPostCreateHandler(),
                            context,
                            result));
                    m_uploadPopup.center();

                }

            };
            action.execute();
        } else {
            setUploadPopup(
                new CmsUploadPopup(
                    m_editableData.getExtensions().getUploadFolder(),
                    m_editableData.getPostCreateHandler(),
                    context,
                    null));
            m_uploadPopup.center();

        }

    }

    /**
     * Opens the content editor.<p>
     *
     * @param isNew <code>true</code> to create and edit a new resource
     * @param mode the content creation mode
     * @param handlerDataForNew the data for the edit handler if it is used for the 'new' function
     */
    protected void openEditDialog(boolean isNew, String mode, CmsEditHandlerData handlerDataForNew) {

        CmsContainerpageController.get().getContentEditorHandler().openDialog(
            m_editableData,
            isNew,
            m_parentResourceId,
            mode,
            handlerDataForNew);
    }

    /**
     * Shows the delete warning dialog.<p>
     */
    protected void openWarningDialog() {

        CmsDeleteWarningDialog dialog = new CmsDeleteWarningDialog(m_editableData.getSitePath());
        Command callback = new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                deleteElement();
            }
        };
        dialog.loadAndShow(callback);
    }

    /**
     * Returns the edit content id.<p>
     *
     * @return the content id
     */
    CmsUUID getContentId() {

        return m_editableData.getStructureId();
    }

    /**
     * Returns the parent resource id.<p>
     *
     * @return the parent resource id
     */
    String getParentResourceId() {

        return m_parentResourceId;
    }

    /**
     * Sets the display CSS property to none, or clears it, depending on the given parameter.<p>
     *
     * @param displayNone true if the widget should not be displayed
     */
    void setDisplayNone(boolean displayNone) {

        if (displayNone) {
            getElement().getStyle().setDisplay(Display.NONE);
        } else {
            getElement().getStyle().clearDisplay();
        }
    }

    /**
     * Closes the currently active upload popup.
     */
    private void closeUploadPopup() {

        if (m_uploadPopup != null) {
            m_uploadPopup.hide();
            m_uploadPopup = null;
        }
    }

    /**
     * Sets the upload popup, and closes the previous one if it exists.
     *
     * @param popup the upload popup
     */
    private void setUploadPopup(CmsUploadPopup popup) {

        closeUploadPopup();
        m_uploadPopup = popup;
    }

}
