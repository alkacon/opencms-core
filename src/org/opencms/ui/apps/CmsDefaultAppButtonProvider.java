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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.Locale;

import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The default app button provider.<p>
 */
public class CmsDefaultAppButtonProvider implements I_CmsAppButtonProvider {

    /**
     * Creates a properly styled button for the given app.<p>
     *
     * @param cms the cms context
     * @param appConfig the app configuration
     * @param locale the locale
     *
     * @return the button component
     */
    public static Component createAppButton(
        CmsObject cms,
        final I_CmsWorkplaceAppConfiguration appConfig,
        Locale locale) {

        Button button = createAppIconButton(appConfig, locale);
        button.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                if ((appConfig instanceof I_CmsHasAppLaunchCommand)
                    && (((I_CmsHasAppLaunchCommand)appConfig).getAppLaunchCommand() != null)) {
                    ((I_CmsHasAppLaunchCommand)appConfig).getAppLaunchCommand().run();
                } else {
                    CmsAppWorkplaceUi ui = (CmsAppWorkplaceUi)A_CmsUI.get();
                    ui.showApp(appConfig);
                }
            }
        });
        CmsAppVisibilityStatus status = appConfig.getVisibility(cms);
        if (!status.isActive()) {
            button.setEnabled(false);
            button.setDescription(status.getHelpText());
        } else {
            String helpText = appConfig.getHelpText(locale);
            button.setDescription(helpText);
        }
        return button;
    }

    /**
     * Creates a properly styled button for the given app, without adding a click handler or checking visibility settings.<p>
     *
     * @param appConfig the app configuration
     * @param locale the locale
     *
     * @return the button component
     */
    public static Button createAppIconButton(I_CmsWorkplaceAppConfiguration appConfig, Locale locale) {

        return createIconButton(
            appConfig.getName(locale),
            appConfig.getHelpText(locale),
            appConfig.getIcon(),
            appConfig.getButtonStyle());
    }

    /**
     * Creates an icon button.<p>
     *
     * @param name the name
     * @param description the description
     * @param icon the icon
     *
     * @return the created button
     */
    public static Button createIconButton(String name, String description, Resource icon) {

        return createIconButton(name, description, icon, I_CmsAppButtonProvider.BUTTON_STYLE_TRANSPARENT);
    }

    /**
     * Creates an icon button.<p>
     *
     * @param name the name
     * @param description the description
     * @param icon the icon
     * @param buttonStyle the button style
     *
     * @return the created button
     */
    public static Button createIconButton(String name, String description, Resource icon, String buttonStyle) {

        Button button = new Button(name);
        button.setIcon(icon, name);
        button.setDescription(description);
        button.addStyleName(OpenCmsTheme.APP_BUTTON);
        button.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        button.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
        button.addStyleName(buttonStyle);
        return button;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppButtonProvider#createAppButton(org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration)
     */
    public Component createAppButton(I_CmsWorkplaceAppConfiguration appConfig) {

        return createAppButton(A_CmsUI.getCmsObject(), appConfig, UI.getCurrent().getLocale());
    }
}
