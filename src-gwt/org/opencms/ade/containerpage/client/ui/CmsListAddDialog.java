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

import org.opencms.ade.containerpage.client.Messages;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.shared.CmsListElementCreationDialogData;
import org.opencms.gwt.shared.CmsListElementCreationOption;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;

import java.util.function.Consumer;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Dialog that allows the user to choose the resource type of a new content to be created.
 */
public class CmsListAddDialog extends CmsPopup {

    /**
     * Creates a new instance.
     *
     * @param elementId the container element id
     * @param listAddData the data about the creatable resource types
     * @param optionHandler the handler to call with the selected option
     */
    public CmsListAddDialog(
        CmsUUID elementId,
        CmsListElementCreationDialogData listAddData,
        Consumer<CmsListElementCreationOption> optionHandler) {

        super();
        setCaption(listAddData.getCaption());
        setGlassEnabled(true);
        addDialogClose(() -> {});
        CmsPushButton cancel = new CmsPushButton();
        cancel.setUseMinWidth(true);
        cancel.setText(Messages.get().key(Messages.GUI_BUTTON_CANCEL_TEXT_0));
        cancel.addClickHandler(event -> CmsListAddDialog.this.hide());
        addButton(cancel);
        CmsListItemWidget listConfigInfoWidget = new CmsListItemWidget(listAddData.getListInfo());
        add(listConfigInfoWidget);
        if (listAddData.getOptions().size() == 0) {
            FlowPanel labelContainer = new FlowPanel();
            labelContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.listAddCss().labelContainer());
            Label label = new Label(listAddData.getMessage());
            labelContainer.add(label);
            add(labelContainer);
        } else {
            final FlowPanel optionContainer = new FlowPanel();
            add(optionContainer);

            optionContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.listAddCss().optionContainer());

            listAddData.getOptions().stream().forEach(option -> {
                CmsListInfoBean listInfo = option.getInfo();
                CmsListItemWidget itemWidget = new CmsListItemWidget(listInfo);
                itemWidget.addClickHandler(evt -> {
                    CmsListAddDialog.this.hide();
                    optionHandler.accept(option);
                });
                CmsPushButton plusButton = new CmsPushButton();
                plusButton.setImageClass(I_CmsButton.ADD_SMALL);
                plusButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
                itemWidget.getButtonPanel().add(plusButton);
                CmsListItem item = new CmsListItem(itemWidget);
                optionContainer.add(item);
            });
        }
    }

}
