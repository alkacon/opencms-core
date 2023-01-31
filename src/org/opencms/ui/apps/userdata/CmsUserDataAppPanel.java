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

package org.opencms.ui.apps.userdata;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.userdata.I_CmsUserDataDomain;
import org.opencms.jsp.userdata.Messages;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.WidgetType;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelectDialog;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.commons.logging.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.UserError;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The GUI form for the user data app.
 *
 * <p>Generates a user data report either for a selected OpenCms user, or for an email address.
 * The report is constructed by the I_CmsUserDataDomain plugins configured in opencms-system.xml.
 */
public class CmsUserDataAppPanel extends VerticalLayout {

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserDataAppPanel.class);

    /** The field for entering the email address. */
    protected TextField m_email;

    /** The button for generating the report based on the email address. */
    protected Button m_searchByEmail;

    /** The widget containing the report/results. */
    protected VerticalLayout m_resultsContainer;

    /** The label for the results. */
    protected Label m_resultsLabel;

    /** The button used to download the report. */
    protected Button m_download;

    /** The button used to select an OpenCms user for whom to generate the report. */
    protected Button m_pickUserButton;

    /** The field containing the name of the user for whom to generate the report. */
    protected TextField m_user;

    /** The button which generates the report for the selected OpenCms user. */
    protected Button m_searchByUser;

    /** The report as a string (the content of the download). */
    protected String m_result;

    /**
     * Creates a new instance.
     */
    public CmsUserDataAppPanel() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), new HashMap<>());
        m_resultsLabel.setContentMode(ContentMode.HTML);
        m_resultsContainer.setVisible(false);
        FileDownloader downloader = new FileDownloader(new StreamResource(() -> {
            String result = CmsUserDataAppPanel.this.getResult();
            byte[] resultBytes = result.getBytes(StandardCharsets.UTF_8);
            return new ByteArrayInputStream(resultBytes);
        }, "userdata.html"));
        downloader.extend(m_download);
        m_download.setIcon(VaadinIcons.DOWNLOAD);
        m_pickUserButton.setCaption("");
        m_pickUserButton.setIcon(FontAwesome.USER);
        m_pickUserButton.addStyleName(OpenCmsTheme.BUTTON_ICON);
        m_pickUserButton.addClickListener(evt -> {
            final Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);

            CmsPrincipalSelectDialog dialog = new CmsPrincipalSelectDialog(
                principal -> selectUser(principal),
                "",
                window,
                WidgetType.userwidget,
                true,
                CmsPrincipalSelect.PrincipalType.user);
            dialog.setOuComboBoxEnabled(true);
            window.setCaption(CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_USERDATA_SELECT_USER_0));
            window.setContent(dialog);
            A_CmsUI.get().addWindow(window);
        });

        m_searchByEmail.addClickListener(event -> {
            hideResult();
            m_email.setComponentError(null);
            String email = m_email.getValue().trim();
            CmsObject cms = getCmsObjectForReport();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(email)) {
                m_email.setComponentError(
                    new UserError(
                        CmsVaadinUtils.getMessageText(
                            org.opencms.ui.apps.Messages.GUI_USERDATA_EMPTY_EMAIL_NOT_ALLOWED_0)));
                return;
            }
            try {
                Document doc = Jsoup.parseBodyFragment("");
                doc.body().appendElement("h1").attr("class", "udr-header").text(
                    Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                        Messages.GUI_USER_INFORMATION_FOR_1,
                        email));

                if (OpenCms.getUserDataRequestManager().getInfoForEmail(
                    cms,
                    I_CmsUserDataDomain.Mode.workplace,
                    email,
                    doc.body())) {
                    showResult(doc);
                } else {
                    showResult(null);
                }
            } catch (Exception e) {
                CmsErrorDialog.showErrorDialog(e);
            }
        });

        m_searchByUser.addClickListener(evt -> {
            hideResult();
            m_user.setComponentError(null);
            String user = m_user.getValue().trim();
            CmsObject cms = getCmsObjectForReport();
            CmsUser userObj = null;
            try {
                userObj = cms.readUser(user);
            } catch (Exception e) {
                LOG.info(e.getLocalizedMessage(), e);
                m_user.setComponentError(
                    new UserError(
                        CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_USERDATA_USER_NOT_FOUND_0)));
                return;
            }
            try {
                Document doc = Jsoup.parseBodyFragment("");
                doc.body().appendElement("h1").attr("class", "udr-header").text(
                    Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                        Messages.GUI_USER_INFORMATION_FOR_1,
                        user));

                if (OpenCms.getUserDataRequestManager().getInfoForUser(
                    cms,
                    I_CmsUserDataDomain.Mode.workplace,
                    userObj,
                    doc.body())) {
                    showResult(doc);
                } else {
                    showResult(null);
                }
            } catch (Exception e) {
                CmsErrorDialog.showErrorDialog(e);
            }
        });
    }

    /**
     * Prepares the CmsObject to use for the report.
     *
     * <p>The CmsObject's locale needs to be set to the current locale, because the plugins
     * used to generate the report do not use a separate locale parameter.
     *
     * @return the CmsObject to use for generating the report
     */
    protected CmsObject getCmsObjectForReport() {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            cms = OpenCms.initCmsObject(cms);
            cms.getRequestContext().setLocale(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return cms;
    }

    /**
     * Gets the report for the download.
     *
     * @return the report as HTML text
     */
    protected String getResult() {

        return m_result;
    }

    /**
     * Hides the previous search results.
     */
    private void hideResult() {

        m_resultsLabel.setValue("");
        m_resultsContainer.setVisible(false);
    }

    /**
     * Called when an OpenCms user is selected.
     *
     * @param principal the selected user
     */
    private void selectUser(I_CmsPrincipal principal) {

        CmsUser user = (CmsUser)principal;
        m_user.setComponentError(null);
        m_user.setValue(user.getName());
    }

    /**
     * Shows the report.
     *
     * <p>If null is given an argument, reports previously shown are hidden.
     *
     * @param doc the report (or null, to hide previous results)
     */
    private void showResult(Document doc) {

        if (doc != null) {
            m_resultsContainer.setVisible(true);
            m_resultsLabel.setValue(doc.body().html());
            m_result = doc.toString();
        } else {
            hideResult();
            String notfound = CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_USERDATA_NOT_FOUND_0);
            Notification notification = new Notification(
                "",
                "<p>" + CmsEncoder.escapeXml(notfound) + "</p>",
                Notification.Type.WARNING_MESSAGE,
                true);
            notification.setDelayMsec(-1);
            notification.show(Page.getCurrent());

        }
    }

}