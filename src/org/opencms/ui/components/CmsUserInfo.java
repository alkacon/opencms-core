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

package org.opencms.ui.components;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsUserIconHelper;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.actions.CmsPreferencesDialogAction;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.components.CmsUploadButton.I_UploadListener;
import org.opencms.ui.dialogs.CmsEmbeddedDialogContext;
import org.opencms.ui.dialogs.CmsUserDataDialog;
import org.opencms.ui.login.CmsChangePasswordDialog;
import org.opencms.ui.login.CmsLoginController;
import org.opencms.ui.shared.components.CmsUploadState.UploadType;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsAccountInfo;
import org.opencms.workplace.CmsAccountInfo.Field;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Displays the current user info.<p>
 */
public class CmsUserInfo extends VerticalLayout {

    /** The HTML line break. */
    private static final String LINE_BREAK = "<br />";

    /** The serial version id. */
    private static final long serialVersionUID = 7215454442218119869L;

    /** The dialog context. */
    I_CmsDialogContext m_context;

    /** The current user. */
    CmsUser m_user;

    /** The info. */
    private Label m_info;

    /** The details. */
    private Label m_details;

    /** The info panel. */
    private HorizontalLayout m_infoPanel;

    /** The user menu. */
    private CmsVerticalMenu m_menu;

    /** The upload listener. */
    private I_UploadListener m_uploadListener;

    /**
     * Constructor.<p>
     *
     * @param uploadListener the user image upload listener
     * @param context the dialog context
     */
    public CmsUserInfo(I_UploadListener uploadListener, I_CmsDialogContext context) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        CmsObject cms = A_CmsUI.getCmsObject();
        m_uploadListener = uploadListener;
        m_user = cms.getRequestContext().getCurrentUser();
        m_context = context;
        m_info.setContentMode(ContentMode.HTML);
        m_info.setValue(generateInfo(cms, UI.getCurrent().getLocale()));
        m_details.setContentMode(ContentMode.HTML);
        m_details.setValue(generateInfoDetails(cms, UI.getCurrent().getLocale()));
        m_infoPanel.addComponent(createImageButton(), 0);
        initUserMenu();

    }

    /**
     * Shows the user preferences dialog.<p>
     */
    void editUserData() {

        if (m_context instanceof CmsEmbeddedDialogContext) {
            ((CmsEmbeddedDialogContext)m_context).closeWindow(true);
        } else {
            A_CmsUI.get().closeWindows();
        }
        CmsUserDataDialog dialog = new CmsUserDataDialog(m_context);
        m_context.start(CmsVaadinUtils.getMessageText(Messages.GUI_USER_EDIT_0), dialog);
    }

    /**
     * Executes the logout.<p>
     */
    void logout() {

        CmsLoginController.logout();
    }

    /**
     * Creates the user image button.<p>
     *
     * @return the created button
     */
    private Component createImageButton() {

        CssLayout layout = new CssLayout();
        layout.addStyleName(OpenCmsTheme.USER_IMAGE);
        Image userImage = new Image();
        userImage.setSource(
            new ExternalResource(
                OpenCms.getWorkplaceAppManager().getUserIconHelper().getBigIconPath(A_CmsUI.getCmsObject(), m_user)));

        layout.addComponent(userImage);

        if (!CmsAppWorkplaceUi.isOnlineProject()) {
            CmsUploadButton uploadButton = createImageUploadButton(
                null,
                FontOpenCms.UPLOAD_SMALL,
                m_user,
                m_uploadListener);
            layout.addComponent(uploadButton);
            if (CmsUserIconHelper.hasUserImage(m_user)) {
                Button deleteButton = new Button(FontOpenCms.TRASH_SMALL);
                deleteButton.addClickListener(new ClickListener() {

                    private static final long serialVersionUID = 1L;

                    public void buttonClick(ClickEvent event) {

                        OpenCms.getWorkplaceAppManager().getUserIconHelper().deleteUserImage(A_CmsUI.getCmsObject());
                        m_context.updateUserInfo();

                    }
                });
                layout.addComponent(deleteButton);
            }
        }

        return layout;
    }

    /**
     * Creates an user image upload button.<p>
     *
     * @param label the label to use
     * @param icon the icon to use
     * @param user the user
     * @param uploadListener the upload listener
     *
     * @return the button
     */
    private CmsUploadButton createImageUploadButton(
        String label,
        Resource icon,
        CmsUser user,
        I_UploadListener uploadListener) {

        CmsUploadButton uploadButton = new CmsUploadButton(
            CmsUserIconHelper.USER_IMAGE_FOLDER + CmsUserIconHelper.TEMP_FOLDER);
        if (label != null) {
            uploadButton.setCaption(label);
        }
        if (icon != null) {
            uploadButton.setIcon(icon);
        }
        uploadButton.getState().setUploadType(UploadType.singlefile);
        uploadButton.getState().setTargetFileNamePrefix(user.getId().toString());
        uploadButton.getState().setDialogTitle(
            CmsVaadinUtils.getMessageText(Messages.GUI_USER_INFO_UPLOAD_IMAGE_DIALOG_TITLE_0));
        uploadButton.addUploadListener(uploadListener);
        return uploadButton;

    }

    /**
     * Generates the info data HTML.<p>
     *
     * @param cms the cms context
     * @param locale the locale
     *
     * @return the info data HTML
     */
    private String generateInfo(CmsObject cms, Locale locale) {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        StringBuffer infoHtml = new StringBuffer(128);
        infoHtml.append("<p>").append(CmsStringUtil.escapeHtml(user.getName())).append("</p>");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getFirstname())) {
            infoHtml.append(CmsStringUtil.escapeHtml(user.getFirstname())).append("&nbsp;");
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getLastname())) {
            infoHtml.append(CmsStringUtil.escapeHtml(user.getLastname()));
        }
        infoHtml.append(LINE_BREAK);
        return infoHtml.toString();
    }

    /**
     * Generates the user info details.<p>
     *
     * @param cms the cms context
     * @param locale the locale
     *
     * @return the user info details
     */
    private String generateInfoDetails(CmsObject cms, Locale locale) {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        StringBuffer infoHtml = new StringBuffer(128);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getEmail())) {
            infoHtml.append(CmsStringUtil.escapeHtml(user.getEmail())).append(LINE_BREAK);
        }
        for (CmsAccountInfo info : OpenCms.getWorkplaceManager().getAccountInfos()) {
            if (!info.getField().equals(Field.firstname)
                && !info.getField().equals(Field.lastname)
                && !Field.email.equals(info.getField())) {
                String value = info.getValue(user);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                    infoHtml.append(CmsStringUtil.escapeHtml(value)).append(LINE_BREAK);
                }
            }
        }
        infoHtml.append(
            Messages.get().getBundle(locale).key(
                Messages.GUI_USER_INFO_ONLINE_SINCE_1,
                DateFormat.getTimeInstance(DateFormat.DEFAULT, locale).format(new Date(user.getLastlogin())))).append(
                    LINE_BREAK);

        return infoHtml.toString();
    }

    /**
     * Initializes the use menu.<p>
     */
    private void initUserMenu() {

        if (!m_user.isManaged()) {
            m_menu.addMenuEntry(
                CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_CHANGE_PASSWORD_BUTTON_0),
                null).addClickListener(new ClickListener() {

                    private static final long serialVersionUID = 1L;

                    public void buttonClick(ClickEvent event) {

                        m_context.start(
                            CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_PWCHANGE_HEADER_0)
                                + m_user.getSimpleName(),
                            new CmsChangePasswordDialog(m_context));
                    }
                });
        }
        final CmsPreferencesDialogAction preferencesAction = new CmsPreferencesDialogAction();
        m_menu.addMenuEntry(preferencesAction.getTitle(), null).addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                preferencesAction.executeAction(m_context);
            }
        });
        if (!m_user.isManaged()) {
            m_menu.addMenuEntry(CmsVaadinUtils.getMessageText(Messages.GUI_USER_EDIT_0), null).addClickListener(
                new Button.ClickListener() {

                    private static final long serialVersionUID = 1L;

                    public void buttonClick(ClickEvent event) {

                        editUserData();
                    }
                });
        }
        m_menu.addMenuEntry(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_CONTEXT_LOGOUT_0),
            null).addClickListener(new Button.ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    logout();
                }
            });
    }
}
