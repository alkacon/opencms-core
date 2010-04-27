/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsElementOptionBar.java,v $
 * Date   : $Date: 2010/04/27 13:56:00 $
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement;
import org.opencms.gwt.client.ui.CmsHoverPanel;

import com.google.gwt.user.client.ui.Composite;

/**
 * A panel to be displayed inside a container element to provide optional functions like edit, move, remove... <p> 
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsElementOptionBar extends Composite {

    /** The CSS class to be assigned to each option-bar. */
    private static String CSS_CLASS = org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle.INSTANCE.containerpageCss().optionBar();

    /** The panel. */
    private CmsHoverPanel m_panel;

    /**
     * Constructor.<p>
     */
    public CmsElementOptionBar() {

        m_panel = new CmsHoverPanel();
        initWidget(m_panel);
        this.setStyleName(CSS_CLASS);
    }

    /**
     * Creates an option-bar for the given drag element.<p>
     * 
     * @param element the element to create the option-bar for
     * @param buttons the list of buttons to display
     * 
     * @return the created option-bar
     */
    public static CmsElementOptionBar createOptionBarForElement(
        CmsDragContainerElement element,
        A_CmsToolbarOptionButton... buttons) {

        CmsElementOptionBar optionBar = new CmsElementOptionBar();
        if (buttons != null) {
            for (int i = 0; i < buttons.length; i++) {
                CmsElementOptionButton option = buttons[i].createOptionForElement(element);
                optionBar.add(option);
            }
        }
        return optionBar;
    }

    /**
     * Adds another option button.<p>
     * 
     * @param w the button to add
     */
    public void add(CmsElementOptionButton w) {

        m_panel.add(w);
    }

    /**
    * Clears the bar.<p>
    */
    public void clear() {

        m_panel.clear();
    }
}
