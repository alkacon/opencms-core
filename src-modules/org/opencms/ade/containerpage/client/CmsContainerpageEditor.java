/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/Attic/CmsContainerpageEditor.java,v $
 * Date   : $Date: 2010/04/06 09:49:44 $
 * Version: $Revision: 1.3 $
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
import org.opencms.ade.containerpage.client.ui.CmsToolbarEditButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarMoveButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarPropertiesButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarPublishButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarRemoveButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarSelectionButton;
import org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.draganddrop.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsToolbar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The container page editor.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsContainerpageEditor extends A_CmsEntryPoint {

    /** The editor instance. */
    public static CmsContainerpageEditor INSTANCE;

    /** The currently active button. */
    private I_CmsContainerpageToolbarButton m_currentButton;

    /** The tool-bar. */
    private CmsToolbar m_toolbar;

    /** List of buttons of the tool-bar. */
    private List<I_CmsContainerpageToolbarButton> m_toolbarButtons;

    /**
     * Returns the currently active button. May return <code>null</code>, if none is active.<p>
     * 
     * @return the current button
     */
    public I_CmsContainerpageToolbarButton getCurrentButton() {

        return m_currentButton;
    }

    /**
     * Returns the tool-bar widget.<p>
     * 
     * @return the tool-bar widget
     */
    public CmsToolbar getToolbar() {

        return m_toolbar;
    }

    /**
     * Returns the list of registered tool-bar buttons.<p>
     * 
     * @return the tool-bar buttons
     */
    public List<I_CmsContainerpageToolbarButton> getToolbarButtons() {

        return m_toolbarButtons;
    }

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        INSTANCE = this;
        m_toolbarButtons = new ArrayList<I_CmsContainerpageToolbarButton>();
        m_toolbarButtons.add(new CmsToolbarPublishButton());
        m_toolbarButtons.add(new CmsToolbarSelectionButton());
        m_toolbarButtons.add(new CmsToolbarMoveButton());
        m_toolbarButtons.add(new CmsToolbarEditButton());
        m_toolbarButtons.add(new CmsToolbarRemoveButton());
        m_toolbarButtons.add(new CmsToolbarPropertiesButton());
        initToolbar();
        CmsContainerpageDataProvider.init();

        I_CmsLayoutBundle.INSTANCE.dragdropCss().ensureInjected();
        org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle.INSTANCE.containerpageCss().ensureInjected();

    }

    /**
     * Sets the current button.<p>
     * 
     * @param button the current button
     */
    public void setCurrentButton(I_CmsContainerpageToolbarButton button) {

        m_currentButton = button;
    }

    /**
     * Initialises the tool-bar and its buttons.<p>
     */
    private void initToolbar() {

        m_toolbar = new CmsToolbar();
        CmsToolbarClickHandler handler = new CmsToolbarClickHandler();
        Iterator<I_CmsContainerpageToolbarButton> it = m_toolbarButtons.iterator();
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

    }

}
