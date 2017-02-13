/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.client.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

/**
 * Bundle for CSS resources.<p>
 */
public interface I_CmsLayoutBundle extends org.opencms.gwt.client.ui.css.I_CmsLayoutBundle {

    /** The attribute choice widget CSS. */
    interface I_AttributeChoice extends I_ButtonBar {

        /**
         * Returns the attribute choice CSS class.<p>
         *
         * @return the attribute CSS class
         */
        String attributeChoice();

        /**
         * Returns the bridging div CSS class.<p>
         *
         * @return the bridge CSS class
         */
        String bridge();

        /**
         * Returns the button icon CSS class name.<p>
         *
         * @return the button icon CSS class
         */
        String buttonIcon();

        /**
         * Returns the choice CSS class name.<p>
         *
         * @return the choice CSS class
         */
        String choice();

        /**
         * Returns the choices CSS class name.<p>
         *
         * @return the choices CSS class
         */
        String choices();

        /**
         * Returns the display above CSS class name.<p>
         *
         * @return the display above CSS class
         */
        String displayAbove();

        /**
         * Returns the choices frame CSS class name.<p>
         *
         * @return the choices frame CSS class
         */
        String frame();

        /**
         * Returns the hovering CSS class name.<p>
         *
         * @return the hovering CSS class
         */
        String hovering();

        /**
         * Returns the shadow CSS class name.<p>
         *
         * @return the shadow CSS class
         */
        String shadow();

        /**
         * Returns the shadow bottom CSS class name.<p>
         *
         * @return the shadow bottom CSS class
         */
        String shadowBottom();

        /**
         * Returns the submenu CSS class name.<p>
         *
         * @return the submenu CSS class name
         */
        String submenu();
    }

    /** The button bar CSS. */
    @Shared
    interface I_ButtonBar extends CssResource {

        /**
         * Returns the button CSS class name.<p>
         *
         * @return the button CSS class
         */
        String button();

        /**
         * Returns the button bar CSS class name.<p>
         *
         * @return the button bar CSS class
         */
        String buttonBar();

        /**
         * Returns the CSS class which is set when the user is hovering over a button of the button bar.<p>
         *
         * @return a CSS class
         */
        String hoverButton();

        /**
         * Returns the multi button bar CSS class name.<p>
         *
         * @return the multi button bar CSS class
         */
        String multiButtonBar();
    }

    /** The style CSS resource. */
    @Shared
    interface I_Style extends I_CmsDragCss, I_Widgets, I_ButtonBar {

        /**
         * Returns the attribute CSS class name.<p>
         *
         * @return the attribute CSS class
         */
        String attribute();

        /**
         * Returns the closed bubble CSS class name.<p>
         *
         * @return the closed bubble CSS class
         */
        String closedBubble();

        /**
         * Returns the CSS class for collapsed attribute value views.<p>
         *
         * @return a CSS class
         */
        String collapsed();

        /**
         * Returns the display bubble above CSS class name.<p>
         *
         * @return the display bubble above CSS class
         */
        String displayAbove();

        /**
         * Returns the attribute CSS class name.<p>
         *
         * @return the attribute CSS class
         */
        String dragElement();

        /**
         * Returns the drag overlay CSS class name.<p>
         *
         * @return the drag overlay CSS class
         */
        String dragOverlay();

        /**
         * Returns the focused CSS class name.<p>
         *
         * @return the focused CSS class
         */
        String focused();

        /**
         * Returns the form parent CSS class name.<p>
         *
         * @return the form parent CSS class
         */
        String formParent();

        /**
         * Returns the help bubble CSS class name.<p>
         *
         * @return the help bubble CSS class
         */
        String helpBubble();

        /**
         * Returns the help bubble arrow CSS class name.<p>
         *
         * @return the help bubble arrow CSS class
         */
        String helpBubbleArrow();

        /**
         * Returns the help bubble close CSS class name.<p>
         *
         * @return the help bubble close CSS class
         */
        String helpBubbleClose();

        /**
         * Returns the help message CSS class name.<p>
         *
         * @return the help message CSS class
         */
        String helpMessage();

        /**
         * Returns the help message icon CSS class name.<p>
         *
         * @return the help message icon CSS class
         */
        String helpMessageIcon();

        /**
         * Returns the hide help bubbles CSS class name.<p>
         *
         * @return the hide help bubbles CSS class
         */
        String hideHelpBubbles();

        /**
         * Returns the inline edit overlay CSS class name.<p>
         *
         * @return the inline edit overlay CSS class
         */
        String inlineEditOverlay();

        /**
          * Returns the input field CSS class name.<p>
          *
          * @return the input field CSS class name
          */
        String input();

        /**
         * Returns the move handle CSS class name.<p>
         *
         * @return the move handle CSS class
         */
        String moveHandle();

        /**
         * Style name for uncollapsed attribute value views.<p>
         *
         * @return a style name
         */
        String noHTML();

        /**
         * Returns the overlay button bar CSS class name.<p>
         *
         * @return the overlay button bar CSS class name
         */
        String overlayButtonBar();

        /**
         * Returns the overlay button panel CSS class name.<p>
         *
         * @return the overlay button panel CSS class name
         */
        String overlayButtonPanel();

        /**
         * The second column width constant.<p>
         *
         * @return the second column width constant
         */
        int SECOND_COLUMN_WIDTH();

        /**
         * Returns the shallow widget CSS class name.<p>
         *
         * @return the shallow widget CSS class
         */
        String shallowWidget();

        /**
         * Returns the simple value CSS class name.<p>
         *
         * @return the simple value CSS class name
         */
        String simpleValue();

        /**
         * Returns the simple value CSS class name.<p>
         *
         * @return the simple value CSS class name
         */
        String tabDescription();

        /**
         * Returns the tinymce toolbar container CSS class name.<p>
         *
         * @return the tinymce toolbar container CSS class name
         */
        String tinymceToolbarContainer();

        /**
         * Style name for uncollapsed attribute value views.<p>
         *
         * @return a style name
         */
        String uncollapsed();
    }

    /** The widget CSS classes. */
    @Shared
    interface I_Widgets extends CssResource {

        /**
         * Returns the attribute value CSS class name.<p>
         *
         * @return the attribute value CSS class
         */
        String attributeValue();

        /**
         * Returns the compact view CSS class name.<p>
         *
         * @return the compact view CSS class
         */
        String compactView();

        /**
         * Returns the default view CSS class name.<p>
         *
         * @return the compact view CSS class
         */
        String defaultView();

        /**
         * Returns the drag helper CSS class name.<p>
         *
         * @return the drag helper CSS class
         */
        String dragHelper();

        /**
         * Returns the drag helper CSS class name.<p>
         *
         * @return the drag helper CSS class
         */
        String emptyValue();

        /**
         * Returns the entity CSS class name.<p>
         *
         * @return the entity CSS class
         */
        String entity();

        /**
         * Returns the first column CSS class name.<p>
         *
         * @return the first column CSS class
         */
        String firstColumn();

        /**
         * Returns the has error CSS class name.<p>
         *
         * @return the has error CSS class
         */
        String hasError();

        /**
         * Returns the has warning CSS class name.<p>
         *
         * @return the has warning CSS class
         */
        String hasWarning();

        /**
         * Returns the help bubble close CSS class name.<p>
         *
         * @return the help bubble close CSS class
         */
        String inActive();

        /**
         * Returns the label CSS class name.<p>
         *
         * @return the label CSS class
         */
        String label();

        /**
         * Returns the second column CSS class name.<p>
         *
         * @return the second column CSS class
         */
        String secondColumn();

        /**
         * Returns the single line view CSS class name.<p>
         *
         * @return the single line view CSS class
         */
        String singleLine();

        /**
         * Returns the widget CSS class name.<p>
         *
         * @return the widget CSS class
         */
        String widget();

        /**
         * Returns the widget holder CSS class name.<p>
         *
         * @return the widget holder CSS class
         */
        String widgetHolder();
    }

    /** The bundle instance. */
    I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

    /**
     * Returns the attribute choice CSS.<p>
     *
     * @return the attribute choice CSS
     */
    @Source("attributeChoice.css")
    I_AttributeChoice attributeChoice();

    /**
     * Returns the style CSS.<p>
     *
     * @return the style CSS
     */
    @Source("form.css")
    I_Style form();
}
