/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/I_CmsContainerpageToolbarButton.java,v $
 * Date   : $Date: 2010/04/06 09:49:44 $
 * Version: $Revision: 1.1 $
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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;

/**
 * Interface for all container-page tool-bar buttons.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsContainerpageToolbarButton extends HasClickHandlers {

    /**
     * Creates an element options button associated with this button and assigns the click-handler.<p>
     * 
     * @param element the element to create the button for
     * 
     * @return the created button
     * 
     * @throws UnsupportedOperationException if the button has no element functions and therefore no element option button
     */
    CmsElementOptionButton createOptionForElement(CmsDragContainerElement element) throws UnsupportedOperationException;

    /**
     * The icon CSS class of this button.<p>
     * 
     * @return the CSS class name
     */
    String getIconClass();

    /**
     * Returns the button name.<p>
     * 
     * @return the button name
     */
    String getName();

    /**
     * Returns the localised button title. Will show in tool-tip.<p> 
     * 
     * @return the button title
     */
    String getTitle();

    /**
     * Returns if there are element functions (like edit, move, delete, etc) available for this button.<p>
     * 
     * @return <code>true</code> if there element functions
     */
    boolean hasElementFunctions();

    /**
     * Determines if the user has permissions to use the element function on the given element 
     * (if there are permissions to edit, delete etc.).<p>. 
     * 
     * @param element the element to check
     * 
     * @return <code>true</code> if the user has permissions
     */
    boolean hasPermissions(CmsDragContainerElement element);

    /**
     * Initialises the button.<p>
     */
    void init();

    /**
     * Returns whether this button is active (pushed, not disabled).<p>
     * 
     * @return <code>true</code> if the button is active
     */
    boolean isActive();

    /**
     * Method is executed when the element option button is clicked.<p>
     * 
     * @param event the mouse event (stop propagation if appropriate)
     * @param element the element the option button is associated to
     */
    void onElementClick(ClickEvent event, CmsDragContainerElement element);

    /**
     * Method executed when the button is activated.<p>
     */
    void onToolbarActivate();

    /**
     * Method executed when the button is deactivated.<p>
     */
    void onToolbarDeactivate();

    /**
     * Sets the button to active (pushed, not disabled).<p>
     * 
     * 
     * @param active <code>true</code> if active
     */
    void setActive(boolean active);

    /**
     * Returns if the button should be added to the left or the right side of the tool-bar.<p>
     * 
     * @return <code>true</code> if to show left
     */
    boolean showLeft();
}
