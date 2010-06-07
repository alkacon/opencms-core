/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsFlowPanel.java,v $
 * Date   : $Date: 2010/06/07 14:27:01 $
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

package org.opencms.gwt.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A basic panel which is like GWT's FlowPanel, except it allows you to choose the HTML tag
 * to use.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsFlowPanel extends ComplexPanel {

    /** 
     * Wrapping constructor.<p>
     * 
     * @param element the element to wrap 
     */
    public CmsFlowPanel(Element element) {

        element.removeFromParent();
        setElement(element);
    }

    /**
     * Creates an empty flow panel with a given tag name.
     * 
     * @param tag the HTML tag name to use
     */
    @UiConstructor
    public CmsFlowPanel(String tag) {

        setElement(DOM.createElement(tag));
    }

    /**
     * Adds a new child widget to the panel.
     * 
     * @param w the widget to be added
     */
    @Override
    public void add(Widget w) {

        super.add(w, getElement());
    }

    /**
     * Inserts a widget at a given position.<p>
     * 
     * @param w the widget to insert
     * @param beforeIndex the position before which the widget should be inserted 
     */
    public void insert(Widget w, int beforeIndex) {

        insert(w, getElement(), beforeIndex, true);
    }

}
