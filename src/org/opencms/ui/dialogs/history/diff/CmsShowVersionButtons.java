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

package org.opencms.ui.dialogs.history.diff;

import org.opencms.file.CmsObject;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.dialogs.history.CmsHistoryRow;
import org.opencms.workplace.comparison.CmsHistoryListUtil;

import com.google.common.base.Optional;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Provides buttons for showing the two versions being compared.<p>
 *
 */
public class CmsShowVersionButtons implements I_CmsDiffProvider {

    /**
     * Creates a 'show version' button.<p>
     *
     * @param cms the CMS context to use
     * @param version the version
     *
     * @return the new button
     */
    public Button createButton(final CmsObject cms, final CmsHistoryResourceBean version) {

        String label = CmsVaadinUtils.getMessageText(
            Messages.GUI_HISTORY_DIALOG_SHOW_VERSION_BUTTON_1,
            CmsHistoryRow.formatVersion(version));
        Button result = new Button(label);
        result.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                String v1Param = version.getVersion().getVersionNumber() != null
                ? "" + version.getVersion().getVersionNumber()
                : "" + CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION;
                String link = CmsHistoryListUtil.getHistoryLink(cms, version.getStructureId(), v1Param);
                link = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, link);
                A_CmsUI.get().openPageOrWarn(link, "_blank");
            }

        });
        return result;
    }

    /**
     * @see org.opencms.ui.dialogs.history.diff.I_CmsDiffProvider#diff(org.opencms.file.CmsObject, org.opencms.gwt.shared.CmsHistoryResourceBean, org.opencms.gwt.shared.CmsHistoryResourceBean)
     */
    public Optional<Component> diff(CmsObject cms, CmsHistoryResourceBean v1, CmsHistoryResourceBean v2) {

        Panel panel = new Panel("");
        panel.addStyleName(ValoTheme.PANEL_BORDERLESS);
        HorizontalLayout hl = new HorizontalLayout();
        panel.setContent(hl);
        hl.addComponent(createButton(cms, v1));
        hl.addComponent(createButton(cms, v2));
        VerticalLayout outerContainer = new VerticalLayout();
        outerContainer.addComponent(hl);
        outerContainer.setComponentAlignment(hl, Alignment.MIDDLE_RIGHT);
        outerContainer.setMargin(true);
        hl.setSpacing(true);
        return Optional.fromNullable((Component)outerContainer);
    }

}
