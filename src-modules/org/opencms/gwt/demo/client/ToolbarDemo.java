/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/demo/client/Attic/ToolbarDemo.java,v $
 * Date   : $Date: 2010/03/11 08:26:10 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.demo.client;

import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsImageButton;
import org.opencms.gwt.client.ui.CmsMenuButton;
import org.opencms.gwt.client.ui.CmsTextButton;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Basic demo to show toolbar and buttons.<p>
 */
public class ToolbarDemo implements EntryPoint {

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    public void onModuleLoad() {

        // generating toolbar
        CmsToolbar toolbar = new CmsToolbar();

        // adding buttons to the left side of the toolbar
        CmsTextButton button1 = new CmsTextButton(
            "Test",
            I_CmsImageBundle.INSTANCE.style().magnifierIcon(),
            CmsTextButton.BUTTON_STYLE.cmsButtonBig);
        toolbar.addLeft(button1);

        // generating a button to open a menu fly out
        CmsMenuButton button2 = new CmsMenuButton("Menu", I_CmsImageBundle.INSTANCE.style().magnifierIcon());
        button2.add(new Label("This is the menu content"));
        toolbar.addLeft(button2);

        // adding buttons to the right side of the toolbar
        toolbar.addRight(new CmsImageButton(I_CmsImageBundle.INSTANCE.style().magnifierIcon(), true));
        I_CmsImageBundle.INSTANCE.style().ensureInjected();

        // button style demo
        VerticalPanel buttonDemoPanel = new VerticalPanel();
        buttonDemoPanel.getElement().getStyle().setPadding(5, Unit.PX);
        buttonDemoPanel.setSpacing(10);
        CmsTextButton buttonS = new CmsTextButton("Small", null, CmsTextButton.BUTTON_STYLE.cmsButtonSmall);
        CmsTextButton buttonM = new CmsTextButton("Medium", null, CmsTextButton.BUTTON_STYLE.cmsButtonMedium);
        CmsTextButton buttonL = new CmsTextButton("Large", null, CmsTextButton.BUTTON_STYLE.cmsButtonBig);
        CmsTextButton buttonImage = new CmsTextButton(
            "Medium with image",
            I_CmsImageBundle.INSTANCE.style().deleteIcon());
        CmsImageButton imageButtonBorder = new CmsImageButton(I_CmsImageBundle.INSTANCE.style().moveIcon(), true);
        CmsImageButton imageButtonTransparent = new CmsImageButton(I_CmsImageBundle.INSTANCE.style().moveIcon(), false);
        CmsImageButton imageButtonIconBorder = new CmsImageButton(CmsImageButton.ICON.alert, true);
        CmsImageButton imageButtonIconTransparent = new CmsImageButton(CmsImageButton.ICON.alert, false);

        // button with click handler to open a confirm dialog
        CmsTextButton clickMe1 = new CmsTextButton("show confirm dialog", null, new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                CmsConfirmDialog dialog = new CmsConfirmDialog("Confirm dialog", "Please click a button.");
                dialog.setHandler(new I_CmsConfirmDialogHandler() {

                    /**
                     * @see org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler#onOk()
                     */
                    public void onOk() {

                        Window.alert("Thank you for confrming");

                    }

                    /**
                     * @see org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler#onCancel()
                     */
                    public void onCancel() {

                        Window.alert("You canceled");

                    }
                });
                dialog.center();

            }
        });

        buttonDemoPanel.add(buttonS);
        buttonDemoPanel.add(buttonM);
        buttonDemoPanel.add(buttonL);
        buttonDemoPanel.add(buttonImage);
        buttonDemoPanel.add(imageButtonBorder);
        buttonDemoPanel.add(imageButtonTransparent);
        buttonDemoPanel.add(imageButtonIconBorder);
        buttonDemoPanel.add(imageButtonIconTransparent);

        buttonDemoPanel.add(clickMe1);

        RootPanel.get().add(toolbar);
        RootPanel.get("main").add(buttonDemoPanel);

    }

}
