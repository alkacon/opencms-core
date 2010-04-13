/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/Attic/CmsContainerpageEditor.java,v $
 * Date   : $Date: 2010/04/13 14:27:44 $
 * Version: $Revision: 1.7 $
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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.ui.CmsToolbarClickHandler;
import org.opencms.ade.containerpage.client.ui.CmsToolbarClipboardMenu;
import org.opencms.ade.containerpage.client.ui.CmsToolbarEditButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarGalleryMenu;
import org.opencms.ade.containerpage.client.ui.CmsToolbarMoveButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarPropertiesButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarPublishButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarRemoveButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarResetButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarSaveButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarSelectionButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarSitemapButton;
import org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.ui.CmsImageButton;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Style;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The container page editor.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 8.0.0
 */
public class CmsContainerpageEditor extends A_CmsEntryPoint {

    /** Margin-top added to the document body element when the tool-bar is shown. */
    private int m_bodyMarginTop;

    /** The tool-bar. */
    private CmsToolbar m_toolbar;

    /**
     * Returns the tool-bar widget.<p>
     * 
     * @return the tool-bar widget
     */
    public CmsToolbar getToolbar() {

        return m_toolbar;
    }

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();

        I_CmsLayoutBundle.INSTANCE.containerpageCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.contentEditorCss().ensureInjected();

        List<I_CmsContainerpageToolbarButton> toolbarButtons = new ArrayList<I_CmsContainerpageToolbarButton>();
        toolbarButtons.add(new CmsToolbarPublishButton());
        toolbarButtons.add(new CmsToolbarSaveButton());
        toolbarButtons.add(new CmsToolbarSelectionButton());
        toolbarButtons.add(new CmsToolbarMoveButton());
        toolbarButtons.add(new CmsToolbarEditButton());
        toolbarButtons.add(new CmsToolbarRemoveButton());
        toolbarButtons.add(new CmsToolbarPropertiesButton());
        toolbarButtons.add(new CmsToolbarGalleryMenu());
        toolbarButtons.add(new CmsToolbarClipboardMenu());
        toolbarButtons.add(new CmsToolbarSitemapButton());
        toolbarButtons.add(new CmsToolbarResetButton());
        initToolbar(toolbarButtons);
        CmsContainerpageDataProvider.init(toolbarButtons);
    }

    /**
     * Shows the tool-bar.<p>
     * 
     * @param show if <code>true</code> the tool-bar will be shown
     */
    public void showToolbar(boolean show) {

        Element body = Document.get().getBody();
        if (show) {
            body.addClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarShow());
            body.removeClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarHide());
            body.getStyle().setMarginTop(m_bodyMarginTop + 36, Unit.PX);
        } else {
            body.removeClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarShow());
            body.addClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarHide());
            body.getStyle().setMarginTop(m_bodyMarginTop, Unit.PX);
        }
    }

    /**
     * Returns if the tool-bar is visible.<p>
     * 
     * @return <code>true</code> if the tool-bar is visible
     */
    public boolean toolbarVisible() {

        return !CmsDomUtil.hasClass(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarHide(),
            Document.get().getBody());
    }

    /**
     * Initializes the tool-bar and its buttons.<p>
     */
    private void initToolbar(List<I_CmsContainerpageToolbarButton> toolbarButtons) {

        m_bodyMarginTop = CmsDomUtil.getCurrentStyleInt(Document.get().getBody(), Style.marginTop);
        m_toolbar = new CmsToolbar();
        CmsToolbarClickHandler handler = new CmsToolbarClickHandler();
        Iterator<I_CmsContainerpageToolbarButton> it = toolbarButtons.iterator();
        while (it.hasNext()) {
            I_CmsContainerpageToolbarButton button = it.next();
            button.addClickHandler(handler);
            if (button.showLeft()) {
                m_toolbar.addLeft((Widget)button);
            } else {
                m_toolbar.addRight((Widget)button);
            }
        }
        RootPanel.get().add(m_toolbar);
        showToolbar(false);
        CmsImageButton toggleToolbarButton = new CmsImageButton(I_CmsImageBundle.INSTANCE.style().opencmsLogo(), true);
        RootPanel.get().add(toggleToolbarButton);
        toggleToolbarButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                showToolbar(!toolbarVisible());

            }

        });

        // TODO: use CSS for these properties
        toggleToolbarButton.getElement().getStyle().setPosition(Position.FIXED);
        toggleToolbarButton.getElement().getStyle().setTop(-3, Unit.PX);
        toggleToolbarButton.getElement().getStyle().setLeft(97, Unit.PCT);
        toggleToolbarButton.getElement().getStyle().setZIndex(10010);

    }

}
