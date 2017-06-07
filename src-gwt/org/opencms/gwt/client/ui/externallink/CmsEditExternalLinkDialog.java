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

package org.opencms.gwt.client.ui.externallink;

import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.form.CmsFieldsetFormFieldPanel;
import org.opencms.gwt.shared.CmsExternalLinkInfoBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Dialog to create and edit external link resources.<p>
 */
public final class CmsEditExternalLinkDialog extends CmsPopup implements ValueChangeHandler<String> {

    /** The link gallery resource type name. */
    public static final String LINK_GALLERY_RESOURCE_TYPE_NAME = "linkgallery";

    /** The pointer resource type name. */
    public static final String POINTER_RESOURCE_TYPE_NAME = "pointer";

    /** The text metrics key. */
    private static final String METRICS_KEY = "CREATE_NEW_GALLERY_DIALOG";

    /** The context menu handler. */
    I_CmsContextMenuHandler m_contextMenuHandler;

    /** The link info bean. */
    CmsExternalLinkInfoBean m_linkInfo;

    /** The parent folder path. */
    String m_parentFolderPath;

    /** The pointer resource structure id. */
    CmsUUID m_structureId;

    /** The dialog content panel. */
    private CmsFieldsetFormFieldPanel m_dialogContent;

    /** The file name input. */
    private CmsTextBox m_fileName;

    /** The create new flag. */
    private boolean m_isCreateNew;

    /** The folder name input. */
    private CmsTextBox m_linkContent;

    /** The title input. */
    private CmsTextBox m_linkTitle;

    /** The OK button. */
    private CmsPushButton m_okButton;

    /** The previous link. */
    private String m_previousLink;

    /** The previous link title. */
    private String m_previousTitle;

    /**
     * Constructor.<p>
     *
     * @param structureId the structure id of the resource to edit
     */
    private CmsEditExternalLinkDialog(CmsUUID structureId) {

        this(Messages.get().key(Messages.GUI_EDIT_LINK_DIALOG_TITLE_0));
        m_structureId = structureId;
    }

    /**
     * Constructor.<p>
     *
     * @param title the dialog title
     */
    private CmsEditExternalLinkDialog(String title) {

        super(title);
    }

    /**
     * Constructor. Use to create new link resources.<p>
     *
     * @param niceName the pointer resource nice name
     * @param description the type description
     * @param parentFolderPath the parent folder path
     */
    private CmsEditExternalLinkDialog(String niceName, String description, String parentFolderPath) {

        this(Messages.get().key(Messages.GUI_CREATE_NEW_LINK_DIALOG_TITLE_0));
        m_isCreateNew = true;
        m_parentFolderPath = parentFolderPath;
        CmsExternalLinkInfoBean linkInfo = new CmsExternalLinkInfoBean();
        linkInfo.setTitle(niceName);
        linkInfo.setSubTitle(description);
        linkInfo.setResourceType(POINTER_RESOURCE_TYPE_NAME);
        initContent(linkInfo);
    }

    /**
     * Loads the link info and shows the edit dialog.<p>
     *
     * @param structureId the structure id
     *
     * @return the dialog object
     */
    public static CmsEditExternalLinkDialog loadAndShowDialog(final CmsUUID structureId) {

        final CmsEditExternalLinkDialog dialog = new CmsEditExternalLinkDialog(structureId);
        CmsRpcAction<CmsExternalLinkInfoBean> action = new CmsRpcAction<CmsExternalLinkInfoBean>() {

            @Override
            public void execute() {

                CmsCoreProvider.getVfsService().loadLinkInfo(structureId, this);
            }

            @Override
            protected void onResponse(CmsExternalLinkInfoBean result) {

                dialog.initContent(result);
            }
        };
        action.execute();
        dialog.center();
        return dialog;
    }

    /**
     * Shows the create new link dialog.<p>
     *
     * @param niceName the pointer type nice name
     * @param description the pointer type description
     * @param parentFolderPath the parent folder site path
     *
     * @return the dialog object
     */
    public static CmsEditExternalLinkDialog showNewLinkDialog(
        String niceName,
        String description,
        String parentFolderPath) {

        CmsEditExternalLinkDialog dialog = new CmsEditExternalLinkDialog(niceName, description, parentFolderPath);
        dialog.center();
        return dialog;
    }

    /**
     * Validates the form input.<p>
     *
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public void onValueChange(ValueChangeEvent<String> event) {

        String message = null;
        boolean enableOk = true;
        if (m_isCreateNew) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_fileName.getFormValueAsString())) {
                enableOk = false;
                message = Messages.get().key(Messages.GUI_EDIT_LINK_NO_FILE_NAME_0);
            } else if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_linkContent.getFormValueAsString())) {
                enableOk = false;
                message = Messages.get().key(Messages.GUI_EDIT_LINK_NO_LINK_0);
            }
        } else {
            if ((m_linkContent.getFormValueAsString().equals(m_previousLink)
                && (m_linkTitle.getFormValueAsString().equals(m_previousTitle)))) {
                enableOk = false;
                message = Messages.get().key(Messages.GUI_EDIT_LINK_NO_CHANGES_0);
            } else if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_linkContent.getFormValueAsString())) {
                enableOk = false;
                message = Messages.get().key(Messages.GUI_EDIT_LINK_NO_LINK_0);
            }
        }
        setOkEnabled(enableOk, message);
    }

    /**
     * Sets the context menu handler.<p>
     *
     * @param contextMenuHandler the context menu handler to set
     */
    public void setContextMenuHandler(I_CmsContextMenuHandler contextMenuHandler) {

        m_contextMenuHandler = contextMenuHandler;
    }

    /**
     * Initializes the dialog content.<p>
     *
     * @param linkInfo the link info bean
     */
    protected void initContent(CmsExternalLinkInfoBean linkInfo) {

        m_linkInfo = linkInfo;
        m_previousLink = m_linkInfo.getLink() != null ? m_linkInfo.getLink() : "";
        m_previousTitle = m_linkInfo.getTitle() != null ? m_linkInfo.getTitle() : "";
        m_dialogContent = new CmsFieldsetFormFieldPanel(m_linkInfo, null);
        m_dialogContent.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().highTextBoxes());
        m_dialogContent.getFieldSet().setOpenerVisible(false);
        m_dialogContent.getFieldSet().getElement().getStyle().setMarginTop(4, Style.Unit.PX);
        setMainContent(m_dialogContent);
        if (m_isCreateNew) {
            m_fileName = new CmsTextBox();
            m_fileName.setTriggerChangeOnKeyPress(true);
            m_fileName.addValueChangeHandler(this);
            addInputRow(Messages.get().key(Messages.GUI_EDIT_LINK_LABEL_FILE_NAME_0), m_fileName);
        }

        m_linkTitle = new CmsTextBox();
        m_linkTitle.setFormValueAsString(m_previousTitle);
        m_linkTitle.setTriggerChangeOnKeyPress(true);
        m_linkTitle.addValueChangeHandler(this);
        addInputRow(Messages.get().key(Messages.GUI_EDIT_LINK_LABEL_TITLE_0), m_linkTitle);
        m_linkContent = new CmsTextBox();
        m_linkContent.setTriggerChangeOnKeyPress(true);
        m_linkContent.addValueChangeHandler(this);
        m_linkContent.setFormValueAsString(m_previousLink);
        addInputRow(Messages.get().key(Messages.GUI_EDIT_LINK_LABEL_LINK_0), m_linkContent);
        addDialogClose(null);

        CmsPushButton closeButton = new CmsPushButton();
        closeButton.setText(Messages.get().key(Messages.GUI_CANCEL_0));
        closeButton.setUseMinWidth(true);
        closeButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
        closeButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                hide();
            }
        });
        addButton(closeButton);

        m_okButton = new CmsPushButton();
        m_okButton.setText(Messages.get().key(Messages.GUI_OK_0));
        m_okButton.setUseMinWidth(true);
        m_okButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        m_okButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                onOk();
            }
        });
        addButton(m_okButton);
        setOkEnabled(
            false,
            m_isCreateNew
            ? Messages.get().key(Messages.GUI_EDIT_LINK_NO_FILE_NAME_0)
            : Messages.get().key(Messages.GUI_EDIT_LINK_NO_CHANGES_0));

        m_dialogContent.truncate(METRICS_KEY, CmsPopup.DEFAULT_WIDTH - 20);
    }

    /**
     * Called on dialog OK.<p>
     */
    protected void onOk() {

        final String title = m_linkTitle.getFormValueAsString();
        final String link = m_linkContent.getFormValueAsString();
        m_linkTitle.setEnabled(false);
        m_linkContent.setEnabled(false);
        m_okButton.setEnabled(false);
        if (m_isCreateNew) {
            final String fileName = m_fileName.getFormValueAsString();
            CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                @Override
                public void execute() {

                    CmsCoreProvider.getVfsService().createNewExternalLink(
                        title,
                        link,
                        fileName,
                        m_parentFolderPath,
                        this);
                }

                @Override
                protected void onResponse(Void result) {

                    hide();
                }
            };
            action.execute();
        } else {
            CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                @Override
                public void execute() {

                    CmsCoreProvider.getVfsService().saveExternalLink(
                        m_structureId,
                        title,
                        link,
                        CmsResource.getName(m_linkInfo.getSitePath()),
                        this);
                }

                @Override
                protected void onResponse(Void result) {

                    if (m_contextMenuHandler != null) {
                        m_contextMenuHandler.refreshResource(m_structureId);
                    }
                    hide();
                }
            };
            action.execute();
        }
    }

    /**
     * Enables or disables the OK button.<p>
     *
     * @param enabled <code>true</code> to enable the button
     * @param message the disabled reason
     */
    protected void setOkEnabled(boolean enabled, String message) {

        if (enabled) {
            m_okButton.enable();
        } else {
            m_okButton.disable(message);
        }
    }

    /**
     * Adds a row to the form.<p>
     *
     * @param label the label
     * @param inputWidget the input widget
     */
    private void addInputRow(String label, Widget inputWidget) {

        FlowPanel row = new FlowPanel();
        row.setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().simpleFormRow());
        CmsLabel labelWidget = new CmsLabel(label);
        labelWidget.setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().simpleFormLabel());
        row.add(labelWidget);
        inputWidget.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().simpleFormInputBox());
        row.add(inputWidget);
        m_dialogContent.getFieldSet().add(row);
    }

}
