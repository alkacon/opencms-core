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

package org.opencms.ui.apps.lists;

import org.opencms.ade.containerpage.shared.CmsDialogOptions;
import org.opencms.ade.containerpage.shared.CmsDialogOptions.Option;
import org.opencms.file.CmsResource;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;

import java.util.Collections;

import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * Option dialog.<p>
 */
public class CmsOptionDialog extends CmsBasicDialog {

    /**
     * Dialog handler to handle the selected option.<p>
     */
    public interface I_OptionHandler {

        /**
         * Handles the selected option.<p>
         *
         * @param option the selected option
         */
        void handleOption(String option);
    }

    /** The serial version id. */
    private static final long serialVersionUID = 8398169182381160373L;

    /**
     * Constructor.<p>
     *
     * @param resource the handled resource
     * @param options the available options
     * @param handler the option handler
     * @param onClose called on dialog close or cancel
     * @param window the dialog window
     */
    public CmsOptionDialog(
        CmsResource resource,
        CmsDialogOptions options,
        final I_OptionHandler handler,
        final Runnable onClose,
        final Window window) {
        if (resource != null) {
            displayResourceInfo(Collections.singletonList(resource));
        }

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(new Label(options.getInfo(), ContentMode.HTML));
        final OptionGroup opt = new OptionGroup();
        for (Option option : options.getOptions()) {
            opt.addItem(option.getValue());
            opt.setItemCaption(option.getValue(), option.getLabel());
        }
        opt.setValue(options.getOptions().get(0).getValue());
        layout.addComponent(opt);
        setContent(layout);
        Button ok = new Button(CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));
        ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                String option = (String)opt.getValue();
                if (window != null) {
                    window.close();
                }
                handler.handleOption(option);
            }

        });
        addButton(ok);
        Button cancel = new Button(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0));
        cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                if (window != null) {
                    window.close();
                }
                if (onClose != null) {
                    onClose.run();
                }
            }

        });
        addButton(cancel);
        if ((window != null) && (onClose != null)) {
            window.addCloseListener(new CloseListener() {

                private static final long serialVersionUID = 1L;

                public void windowClose(CloseEvent e) {

                    onClose.run();
                }
            });
        }
    }

}
