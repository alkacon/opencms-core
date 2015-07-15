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
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.CmsAppVisibilityStatus;
import org.opencms.ui.apps.CmsDefaultAppButtonProvider;
import org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration;
import org.opencms.workplace.CmsWorkplace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.ComparisonChain;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.UI;

/**
 * The workplace toolbar.<p>
 */
public class CmsToolBar extends CustomLayout {

    /** Template location name. */
    private static final String LOCATION_APP_INDICATOR = "appIndicator";

    /** Template location name. */
    private static final String LOCATION_INDICATOR_SMALL = "indicatorSmall";

    /** Template location name. */
    private static final String LOCATION_INFO_CONTAINER = "infoContainer";

    /** Template location name. */
    private static final String LOCATION_INFO_ICON = "infoIcon";

    /** Template location name. */
    private static final String LOCATION_ITEMS_LEFT = "itemsLeft";

    /** Template location name. */
    private static final String LOCATION_ITEMS_RIGHT = "itemsRight";

    /** Template location name. */
    private static final String LOCATION_LOGO = "logo";

    /** The serial version id. */
    private static final long serialVersionUID = -4551194983054069395L;

    /** The app indicator. */
    private Label m_appIndicator;

    /** The small app indicator label, visible only in the small toolbar variant. */
    private Label m_appIndicatorSmall;

    /** The app info icon. */
    private Label m_infoIcon;

    /** Toolbar items left. */
    private HorizontalLayout m_itemsLeft;

    /** Toolbar items right. */
    private HorizontalLayout m_itemsRight;

    /** OpenCms logo. */
    private Image m_logo;

    /**
     * Constructor.<p>
     *
     * @throws IOException in case the layout is not available
     */
    public CmsToolBar()
    throws IOException {

        super(CmsToolBar.class.getClassLoader().getResourceAsStream("VAADIN/themes/opencms/layouts/ToolBar.html"));

        m_itemsLeft = new HorizontalLayout();
        addComponent(m_itemsLeft, LOCATION_ITEMS_LEFT);
        m_itemsRight = new HorizontalLayout();
        addComponent(m_itemsRight, LOCATION_ITEMS_RIGHT);
        m_appIndicator = new Label();
        addComponent(m_appIndicator, LOCATION_APP_INDICATOR);
        m_appIndicatorSmall = new Label();
        addComponent(m_appIndicatorSmall, LOCATION_INDICATOR_SMALL);
        m_logo = new Image();
        addComponent(m_logo, LOCATION_LOGO);
        m_infoIcon = new Label();
        addComponent(m_infoIcon, LOCATION_INFO_ICON);

        m_logo.setSource(new ExternalResource(CmsWorkplace.getResourceUri("commons/login_logo.png")));
        m_itemsRight.addComponent(createButton(FontOpenCms.CONTEXT_MENU));
        m_itemsRight.addComponent(createDropDown());
        m_itemsRight.addComponent(createButton(FontAwesome.USER));

    }

    /**
     * Creates a properly styled toolbar button.<p>
     *
     * @param icon the button icon
     *
     * @return the button
     */
    public static Component createButton(Resource icon) {

        Button button = new Button(icon);
        button.addStyleName("borderless");
        button.addStyleName("toolbar");
        return button;
    }

    /**
     * Adds a button to left toolbar side.<p>
     *
     * @param button the button
     */
    public void addButtonLeft(Component button) {

        m_itemsLeft.addComponent(button);
    }

    /**
     * Adds a button to right toolbar side.<p>
     *
     * @param button the button
     */
    public void addButtonRight(Component button) {

        m_itemsRight.addComponent(button);
    }

    /**
     * Clears the left toolbar buttons.<p>
     */
    public void clearButtonsLeft() {

        m_itemsLeft.removeAllComponents();
    }

    /**
     * Clears the right toolbar buttons.<p>
     */
    public void clearButtonsRight() {

        m_itemsRight.removeAllComponents();
    }

    /**
     * Sets the app info icon.<p>
     *
     * @param icon the icon resource
     */
    public void setAppIcon(Resource icon) {

        m_infoIcon.setIcon(icon);
        m_appIndicatorSmall.setIcon(icon);
    }

    /**
     * Sets the app info component.<p>
     *
     * @param infoComponent the info component
     */
    public void setAppInfo(Component infoComponent) {

        if (infoComponent == null) {
            removeComponent(LOCATION_INFO_CONTAINER);
        } else {
            addComponent(infoComponent, LOCATION_INFO_CONTAINER);
        }
    }

    /**
     * Sets the app title.<p>
     *
     * @param appTitle the app title
     */
    public void setAppTitle(String appTitle) {

        m_appIndicator.setValue(appTitle);
        m_appIndicatorSmall.setValue(appTitle);
    }

    /**
     * Creates the app select drop down.<p>
     *
     * @return the drop down component
     */
    private Component createDropDown() {

        CmsObject cms = A_CmsUI.getCmsObject();
        Locale locale = UI.getCurrent().getLocale();
        HorizontalLayout layout = new HorizontalLayout();
        layout.addStyleName("wrapping");
        //    layout.setSpacing(true);
        layout.setMargin(true);
        List<I_CmsWorkplaceAppConfiguration> configs = new ArrayList<I_CmsWorkplaceAppConfiguration>(
            OpenCms.getWorkplaceAppManager().getWorkplaceApps());

        Collections.sort(configs, new Comparator<I_CmsWorkplaceAppConfiguration>() {

            public int compare(I_CmsWorkplaceAppConfiguration cat1, I_CmsWorkplaceAppConfiguration cat2) {

                return ComparisonChain.start().compare(cat1.getOrder(), cat2.getOrder()).result();
            }
        });

        for (I_CmsWorkplaceAppConfiguration appConfig : configs) {
            CmsAppVisibilityStatus status = appConfig.getVisibility(cms);
            if (status.isVisible()) {
                layout.addComponent(CmsDefaultAppButtonProvider.createAppIconWidget(appConfig, locale));
                // show only the top 10
                if (layout.getComponentCount() > 9) {
                    break;
                }
            }
        }
        String html = "<div tabindex=\"0\" role=\"button\" class=\"v-button v-widget borderless v-button-borderless toolbar v-button-toolbar\"><span class=\"v-button-wrap\">"
            + FontAwesome.TH_LARGE.getHtml()
            + "</span></div>";
        PopupView pv = new PopupView(html, layout);
        pv.addStyleName("opencms-navigator-dropdown");
        pv.setHideOnMouseOut(false);
        return pv;
    }
}
