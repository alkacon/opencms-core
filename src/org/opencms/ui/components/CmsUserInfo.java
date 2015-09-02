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

import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Displays the current user info.<p>
 */
public class CmsUserInfo extends VerticalLayout {

    /** The serial version id. */
    private static final long serialVersionUID = 7215454442218119869L;

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

        String infoHtml = "<b>" + user.getFullName() + "</b><br />";
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getEmail())) {
            infoHtml += user.getEmail() + "<br />";
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getInstitution())) {
            infoHtml += user.getInstitution() + "<br />";
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getAddress())) {
            infoHtml += user.getAddress() + "<br />";
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getZipcode())) {
            infoHtml += user.getZipcode() + "<br />";
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getCity())) {
            infoHtml += user.getCity() + "<br />";
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getCountry())) {
            infoHtml += user.getCountry() + "<br />";
        }

        return infoHtml;
    }

}
