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

package org.opencms.ui.components;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsUserIconHelper;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.login.CmsLoginController;
import org.opencms.util.CmsStringUtil;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Displays the current user info.<p>
 */
public class CmsUserInfo extends VerticalLayout {

    /** The serial version id. */
    private static final long serialVersionUID = 7215454442218119869L;

    /** The HTML line break. */
    private static final String LINE_BREAK = "<br />";

    /** The image. */
    private Image m_image;

    /** The info. */
    private Label m_info;

    /** The logout button. */
    private Button m_logout;

    /** The preferences. */
    private Button m_preferences;

    /**
     * Constructor.<p>
     */
    public CmsUserInfo() {
        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        CmsObject cms = A_CmsUI.getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();

        m_info.setContentMode(ContentMode.HTML);
        m_info.setValue(generateInfoHtml(user));
        m_image.setSource(new ExternalResource(CmsUserIconHelper.getInstance().getBigIconPath(cms, user)));
        m_logout.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                logout();
            }
        });
        m_preferences.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                showPreferences();
            }
        });

        // hiding the button for now
        m_preferences.setVisible(false);
    }

    /**
     * Executes the logout.<p>
     */
    void logout() {

        CmsLoginController.logout();
    }

    /**
     * Shows the user preferences dialog.<p>
     */
    void showPreferences() {
        //TODO: implement
    }

    /**
     * Generates the user info HTML.<p>
     *
     * @param user the user
     *
     * @return the user info
     */
    private String generateInfoHtml(CmsUser user) {

        StringBuffer infoHtml = new StringBuffer(128);
        infoHtml.append("<b>").append(user.getFullName()).append("</b>").append(LINE_BREAK);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getEmail())) {
            infoHtml.append(user.getEmail()).append(LINE_BREAK);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getInstitution())) {
            infoHtml.append(user.getInstitution()).append(LINE_BREAK);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getAddress())) {
            infoHtml.append(user.getAddress()).append(LINE_BREAK);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getZipcode())) {
            infoHtml.append(user.getZipcode()).append(LINE_BREAK);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getCity())) {
            infoHtml.append(user.getCity()).append(LINE_BREAK);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getCountry())) {
            infoHtml.append(user.getCountry()).append(LINE_BREAK);
        }
        Locale locale = UI.getCurrent().getLocale();
        infoHtml.append(
            Messages.get().getBundle(locale).key(
                Messages.GUI_USER_INFO_ONLINE_SINCE_1,
                DateFormat.getTimeInstance(DateFormat.DEFAULT, locale).format(new Date(user.getLastlogin())))).append(
                    LINE_BREAK);

        return infoHtml.toString();
    }

}
