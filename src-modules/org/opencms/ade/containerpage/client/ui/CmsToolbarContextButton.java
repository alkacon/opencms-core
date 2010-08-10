/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsToolbarContextButton.java,v $
 * Date   : $Date: 2010/08/10 07:02:03 $
 * Version: $Revision: 1.6 $
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsContextMenuGXT;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsContextMenuEntry;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsCollectionUtil;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;

import java.util.List;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.impl.ClippedImagePrototype;

/**
 * The context tool-bar menu button.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarContextButton extends Button {

    /** The menu data. */
    protected List<I_CmsContextMenuEntry> m_menuEntries;

    /**
     * Constructor.<p>
     * 
     * @param handler the container-page handler
     */
    public CmsToolbarContextButton(final CmsContainerpageHandler handler) {

        super(I_CmsButton.ButtonData.CONTEXT.getTitle());

        addStyleName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().cmsContextMenu());
        setScale(ButtonScale.MEDIUM);
        setIcon(new ClippedImagePrototype(I_CmsImageBundle.INSTANCE.toolbarContext().getURL(), 0, -1, 24, 24));

        addHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                if (m_menuEntries == null) {
                    handler.loadContextMenu(CmsCoreProvider.get().getUri(), AdeContext.containerpage);
                }
            }
        }, ClickEvent.getType());

        /*
        // create the menu panel (it's a table because of ie6)
        m_menuPanel = new FlexTable();
        // set a style name for the menu table
        m_menuPanel.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().menuPanel());

        // set the widget
        setMenuWidget(m_menuPanel);

        // remove the style attribute of the popup because its width is set to 100%
        DOM.removeElementAttribute(getPopupContent().getWidget().getElement(), "style");
        */

    }

    /**
     * Creates the menu and adds it to the panel.<p>
     * 
     * @param menuEntries the menu entries 
     */
    public void showMenu(List<I_CmsContextMenuEntry> menuEntries) {

        if (!CmsCollectionUtil.isEmptyOrNull(menuEntries)) {
            m_menuEntries = menuEntries;
            CmsContextMenuGXT buttonMenu = new CmsContextMenuGXT(menuEntries);
            setMenu(buttonMenu);
            onClick(new ComponentEvent(this));
        } else {
            // if no entries were found, inform the user 
            Menu buttonMenu = new Menu();
            buttonMenu.add(new Label("No entries found for this resource type!"));
            setMenu(buttonMenu);
            onClick(new ComponentEvent(this));
        }
    }

    /**
     * Creates the menu and adds it to the panel.<p>
     * 
     * @param menuEntries the menu entries 
     */
    /*
    public void showMenu(List<I_CmsContextMenuEntry> menuEntries) {

        if (!CmsCollectionUtil.isEmptyOrNull(menuEntries)) {
            // if there were entries found for the menu, create the menu
            CmsContextMenu menu = new CmsContextMenu(menuEntries, true);
            // add the resize handler for the menu
            m_resizeRegistration = Window.addResizeHandler(menu);
            // set the menu as widget for the panel 
            m_menuPanel.setWidget(0, 0, menu);
            // add the close handler for the menu
            getPopupContent().addCloseHandler(new CmsContextMenuHandler(menu));
        } else {
            // if no entries were found, inform the user 
            CmsLabel label = new CmsLabel("No entries found for this resource type!");
            label.addStyleName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().menuInfoLabel());
            m_menuPanel.setWidget(0, 0, label);
        }
    }
    */
}