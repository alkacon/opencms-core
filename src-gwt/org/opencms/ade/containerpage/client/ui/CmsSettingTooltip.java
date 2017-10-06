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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Tooltip widget for element setting help texts.<p>
 */
public class CmsSettingTooltip extends Composite {

    /** The ui binder interface for this widget. */
    protected interface I_UiBinder extends UiBinder<Widget, CmsSettingTooltip> {
        //UIBinder interface

    }

    /** The currently displayed tooltip. */
    private static CmsSettingTooltip currentInstance;

    /** The label with the help text. */
    @UiField
    protected Label m_label;

    /**
     * Creates a new instance.<p>
     */
    public CmsSettingTooltip() {
        I_UiBinder uiBinder = GWT.create(I_UiBinder.class);
        initWidget(uiBinder.createAndBindUi(this));
        // force synchronous injection of styles
        StyleInjector.flush();
    }

    /**
     * Closes the currently displayed tooltip, if it exists.<p>
     */
    public static void closeTooltip() {

        if (currentInstance != null) {
            currentInstance.removeFromParent();
            currentInstance = null;
        }
    }

    /**
     * Positions the tooltip.<p>
     *
     *
     * @param elem the tooltip element
     * @param referenceElement the tooltip icon element
     */
    public static void position(Element elem, Element referenceElement) {

        int dy = 20;
        Style style = elem.getStyle();
        style.setLeft(0, Unit.PX);
        style.setTop(0, Unit.PX);
        int myX = elem.getAbsoluteLeft();
        int myY = elem.getAbsoluteTop();
        int refX = referenceElement.getAbsoluteLeft();
        int refY = referenceElement.getAbsoluteTop();
        int refWidth = referenceElement.getOffsetWidth();
        int newX = (refX - myX - ((2 * elem.getOffsetWidth()) / 3)) + (refWidth / 2);
        int newY = (refY - myY) + dy;
        style.setLeft(newX, Unit.PX);
        style.setTop(newY, Unit.PX);
    }

    /**
     * Shows a help text for the given widget in a tooltip.<p>
     *
     * @param reference the widget next to which the tooltip should be displayed
     * @param text the help text
     */
    public static void showTooltip(Label reference, String text) {

        closeTooltip();
        CmsSettingTooltip tooltip = new CmsSettingTooltip();
        tooltip.getLabel().setText(text);
        currentInstance = tooltip;
        RootPanel.get().add(tooltip);
        position(tooltip.getElement(), reference.getElement());
    }

    /**
     * Gets the label for the help text.<p>
     *
     * @return the label for the help text
     */
    public Label getLabel() {

        return m_label;
    }

}
