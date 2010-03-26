/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsDebugLog.java,v $
 * Date   : $Date: 2010/03/26 09:42:20 $
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

package org.opencms.gwt.client.util;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * A basic debug log, to print messages into the client window.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsDebugLog extends Composite {

    /** The wrapped widget. */
    HTML m_html;

    /**
     * Constructor.<p>
     */
    public CmsDebugLog() {

        m_html = new HTML();
        initWidget(m_html);
        Style style = getElement().getStyle();
        style.setWidth(200, Unit.PX);
        style.setHeight(500, Unit.PX);
        style.setPadding(10, Unit.PX);
        style.setOverflow(Overflow.AUTO);
        style.setBorderStyle(BorderStyle.SOLID);
        style.setBorderColor("#aaaaaa");
        style.setBorderWidth(1, Unit.PX);
        style.setPosition(Position.FIXED);
        style.setTop(50, Unit.PX);
        style.setRight(50, Unit.PX);
        style.setBackgroundColor("#ffffff");

    }

    /**
     * Prints a new line into the log window by adding a p-tag including given text as HTML.<p>
     * 
     * @param text the text to print
     */
    public void printLine(String text) {

        Element child = DOM.createElement("p");
        child.setInnerHTML(text);
        m_html.getElement().appendChild(child);

    }

    /**
     * Clears the debug log.<p>
     */
    public void clear() {

        m_html.setHTML("");
    }

}
