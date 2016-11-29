/*
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

package org.opencms.ade.postupload.client.ui;

import org.opencms.ade.postupload.client.Messages;
import org.opencms.ade.postupload.shared.CmsPostUploadDialogBean;
import org.opencms.ade.postupload.shared.CmsPostUploadDialogPanelBean;
import org.opencms.ade.postupload.shared.rpc.I_CmsPostUploadDialogService;
import org.opencms.ade.postupload.shared.rpc.I_CmsPostUploadDialogServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsFrameDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Provides a dialog.<p>
 *
 * @since 8.0.0
 */
public class CmsUploadPropertyDialog {

    /** The panel for the content. */
    CmsScrollPanel m_dialogContent = GWT.create(CmsScrollPanel.class);

    /** The pre fetched date. */
    CmsPostUploadDialogBean m_dialogData;

    /** The index of the currently displayed resource. */
    int m_dialogIndex;

    /** List of resource uuids. */
    List<CmsUUID> m_resources;

    /** The advanced button. */
    private CmsPushButton m_buttonAdvanced = new CmsPushButton();

    /** The back button. */
    private CmsPushButton m_buttonBack = new CmsPushButton();

    /** The cancel button. */
    private CmsPushButton m_buttonClose = new CmsPushButton();

    /** The save button. */
    private CmsPushButton m_buttonNext = new CmsPushButton();

    /** The command that is executed on close action. */
    private Command m_closeCommand;

    /** The dialog service. */
    private I_CmsPostUploadDialogServiceAsync m_dialogService;

    /** The frame dialog. */
    private CmsFrameDialog m_frameDialog = new CmsFrameDialog();

    /** The next action to execute after a Save operation. */
    private Runnable m_nextAction;

    /** The current dialog bean. */
    private CmsPostUploadDialogPanelBean m_panelData;

    /** The current property panel. */
    private CmsUploadPropertyPanel m_uploadPropertyPanel;

    /**
     * Public constructor.<p>
     */
    public CmsUploadPropertyDialog() {

        m_frameDialog.setContent(m_dialogContent);
        m_dialogContent.addStyleName(
            org.opencms.ade.postupload.client.ui.css.I_CmsLayoutBundle.INSTANCE.dialogCss().propertyDialog());
        try {
            m_dialogData = (CmsPostUploadDialogBean)CmsRpcPrefetcher.getSerializedObjectFromDictionary(
                getDialogService(),
                CmsPostUploadDialogBean.DICT_NAME);
            m_resources = new ArrayList<CmsUUID>(m_dialogData.getResources().keySet());
        } catch (Exception e) {
            CmsErrorDialog.handleException(
                new Exception(
                    "Deserialization of upload hook data failed."
                        + "This may be caused by expired java-script resources, "
                        + " please clear your browser cache and try again.",
                    e));

        }
        if (!m_dialogData.getResources().isEmpty()) {
            createButtons();
        }
    }

    /**
     * Closes the dialog.<p>
     */
    public void closeDialog() {

        if (m_closeCommand != null) {
            m_closeCommand.execute();
        }
        m_frameDialog.hide();
    }

    /**
     * Returns <code>true</code> if we are currently in the explorer mode.<p>
     *
     * @return <code>true</code> if we are currently in the explorer mode
     */
    public native boolean isExplorerMode() /*-{

                                           if ($wnd.self.name == 'explorer_files') {
                                           return true;
                                           }
                                           return false;
                                           }-*/;

    /**
     * Returns if the dialog is in iFrame mode.<p>
     *
     * @return <code>true</code> if the dialog is in iFrame mode
     */
    public boolean isIFrameMode() {

        return CmsFrameDialog.hasParentFrame();
    }

    /**
     * Loads and shows the content of the dialog inside a popup.<p>
     */
    public void loadAndShow() {

        loadDialogBean(m_resources.get(m_dialogIndex));
    }

    /**
     * Runs the next action after finishing a save operation.
     */
    public void runAction() {

        if (m_nextAction != null) {
            m_nextAction.run();
            m_nextAction = null;
        }
    }

    /**
     * Adds a close "button" to the top of the popup.<p>
     *
     * @param cmd the command that should be executed when the close button is clicked
     */
    public void setCloseCmd(final Command cmd) {

        m_closeCommand = cmd;
    }

    /**
     * Delegation method.<p>
     *
     * @param height the height
     *
     * @see org.opencms.gwt.client.ui.CmsFrameDialog#setHeight(int)
     */
    public void setHeight(int height) {

        m_frameDialog.setHeight(height);

    }

    /**
     * Delegation method.<p>
     *
     * @param title the title
     *
     * @see org.opencms.gwt.client.ui.CmsFrameDialog#setTitle(java.lang.String)
     */
    public void setTitle(String title) {

        m_frameDialog.setTitle(title);
    }

    /**
     * Delegation method.<p>
     *
     * @param width the width
     *
     * @see org.opencms.gwt.client.ui.CmsFrameDialog#setWidth(int)
     */
    public void setWidth(int width) {

        m_frameDialog.setWidth(width);
    }

    /**
     * Updates the height of the dialog to fit the content.<p>
     */
    public void updateHeight() {

        int height = m_dialogContent.getOffsetHeight() + 76;
        setHeight(height);
    }

    /**
     *
     */
    protected void actionAdvanced() {

        String resPath = m_dialogData.getResources().get(m_resources.get(m_dialogIndex));
        String url = CmsCoreProvider.get().link("/system/workplace/commons/property_advanced.jsp?resource=" + resPath);
        Window.Location.assign(url);
    }

    /**
     * Action to display the dialog content for the previous resource.<p>
     */
    protected void actionBack() {

        if (m_dialogIndex <= 0) {
            return;
        }
        m_dialogIndex--;
        m_uploadPropertyPanel.getPropertyEditor().getForm().validateAndSubmit();
        m_nextAction = new Runnable() {

            public void run() {

                loadDialogBean(m_resources.get(m_dialogIndex));
            }
        };
    }

    /**
     * Action to close the dialog.<p>
     */
    protected void actionClose() {

        m_uploadPropertyPanel.getPropertyEditor().getForm().validateAndSubmit();
        m_nextAction = new Runnable() {

            public void run() {

                closeDialog();
            }
        };
    }

    /**
     * Action to display the dialog content for the next resource.<p>
     */
    protected void actionNext() {

        if (m_dialogIndex >= (m_resources.size() - 1)) {
            return;
        }

        m_dialogIndex++;
        m_uploadPropertyPanel.getPropertyEditor().getForm().validateAndSubmit();
        m_nextAction = new Runnable() {

            public void run() {

                loadDialogBean(m_resources.get(m_dialogIndex));
            }
        };
    }

    /**
     * Returns the dialog service instance.<p>
     *
     * @return the dialog service instance
     */
    protected I_CmsPostUploadDialogServiceAsync getDialogService() {

        if (m_dialogService == null) {
            m_dialogService = GWT.create(I_CmsPostUploadDialogService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.postupload.CmsPostUploadDialogService.gwt");
            ((ServiceDefTarget)m_dialogService).setServiceEntryPoint(serviceUrl);
        }
        return m_dialogService;
    }

    /**
     * Retrieves the resource information from the server,
     * creates a the dialogs content and puts the content into the mainpanel.<p>
     *
     * @param uuid the structure id to show the dialog for
     */
    protected void loadDialogBean(final CmsUUID uuid) {

        CmsRpcAction<CmsPostUploadDialogPanelBean> callback = new CmsRpcAction<CmsPostUploadDialogPanelBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getDialogService().load(
                    uuid,
                    m_dialogData.isUsePropertyConfiguration(),
                    m_dialogData.isAddBasicProperties(),
                    this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onFailure(java.lang.Throwable)
             */
            @Override
            public void onFailure(Throwable t) {

                super.onFailure(t);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsPostUploadDialogPanelBean result) {

                updateDialog(result);
            }
        };
        callback.execute();
    }

    /**
     * Updates the dialog.<p>
     *
     * @param result the result
     */
    protected void updateDialog(CmsPostUploadDialogPanelBean result) {

        m_panelData = result;

        if (m_dialogData.getResources().size() > 1) {
            if (m_dialogIndex == 0) {
                m_buttonBack.disable(
                    org.opencms.ade.postupload.client.Messages.get().key(
                        org.opencms.ade.postupload.client.Messages.GUI_DIALOG_INFO_FIRST_RESOURCE_0));
            } else {
                m_buttonBack.enable();
            }
            if (m_dialogIndex == (m_dialogData.getResources().size() - 1)) {
                m_buttonNext.disable(
                    org.opencms.ade.postupload.client.Messages.get().key(
                        org.opencms.ade.postupload.client.Messages.GUI_DIALOG_INFO_LAST_RESOURCE_0));
            } else {
                m_buttonNext.enable();
            }
        }
        m_uploadPropertyPanel = new CmsUploadPropertyPanel(this, m_dialogData, m_panelData);
        m_dialogContent.setWidget(m_uploadPropertyPanel);
        m_frameDialog.setWidth(600);
        if (!m_frameDialog.isShowing()) {
            m_frameDialog.show();
            m_dialogContent.onResizeDescendant();
        }
        m_uploadPropertyPanel.truncate("POST_UPLOAD_DIALOG", 600);
    }

    /**
     * Creates the buttons.<p>
     */
    private void createButtons() {

        m_buttonClose.setButtonStyle(ButtonStyle.TEXT, ButtonColor.GREEN);
        m_buttonClose.setTitle(Messages.get().key(Messages.GUI_DIALOG_BUTTON_CLOSE_0));
        m_buttonClose.setText(Messages.get().key(Messages.GUI_DIALOG_BUTTON_CLOSE_0));
        m_buttonClose.setUseMinWidth(true);
        m_buttonClose.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                actionClose();
            }
        });
        m_frameDialog.addButton(m_buttonClose);

        if (m_dialogData.getResources().size() > 1) {
            m_buttonNext.setTitle(Messages.get().key(Messages.GUI_DIALOG_BUTTON_NEXT_0));
            m_buttonNext.setText(Messages.get().key(Messages.GUI_DIALOG_BUTTON_NEXT_0));
            m_buttonNext.setUseMinWidth(true);
            m_buttonNext.addClickHandler(new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent event) {

                    actionNext();
                }
            });
            m_frameDialog.addButton(m_buttonNext);

            m_buttonBack.setTitle(Messages.get().key(Messages.GUI_DIALOG_BUTTON_BACK_0));
            m_buttonBack.setText(Messages.get().key(Messages.GUI_DIALOG_BUTTON_BACK_0));
            m_buttonBack.setUseMinWidth(true);
            m_buttonBack.addClickHandler(new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent event) {

                    actionBack();
                }
            });
            m_frameDialog.addButton(m_buttonBack);
        }

        if (isExplorerMode() && (m_resources.size() == 1)) {
            m_buttonAdvanced.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
            m_buttonAdvanced.setTitle(Messages.get().key(Messages.GUI_DIALOG_BUTTON_ADVANCED_0));
            m_buttonAdvanced.setText(Messages.get().key(Messages.GUI_DIALOG_BUTTON_ADVANCED_0));
            m_buttonAdvanced.setUseMinWidth(true);
            m_buttonAdvanced.addClickHandler(new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent event) {

                    actionAdvanced();
                }
            });
            m_frameDialog.addButton(m_buttonAdvanced);
        }
    }

}
