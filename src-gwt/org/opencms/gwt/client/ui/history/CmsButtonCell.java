/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.history;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell class for rendering a button inside a cell table.<p>
 *
 * @param <T> the cell type
 */
public class CmsButtonCell<T> extends ActionCell<T> {

    /** The templates used by this cell. */
    static interface Templates extends SafeHtmlTemplates {

        /**
         * Template for the button HTML.<p>
         *
         * @param title the button title
         * @param cssClass the button CSS class
         *
         * @return the HTML for the button
         */
        @Template("<span class=\"{1}\" title=\"{0}\"></span>")
        SafeHtml button(String title, String cssClass);
    }

    /** The template instance. */
    private static Templates templates = GWT.create(Templates.class);

    /** The value for the CSS class of the button. */
    private String m_cssClass;

    /** The value for the title attribute of the button. */
    private String m_title;

    /**
     * Creates a new instance.<p>
     *
     * @param title the value for the title attribute of the button
     * @param cssClass the value for the CSS class of the button
     * @param delegate the delegate which should be called when the button is clicked
     */
    public CmsButtonCell(String title, String cssClass, ActionCell.Delegate<T> delegate) {

        super("", delegate);
        m_title = title;
        m_cssClass = cssClass;
    }

    /**
     * @see com.google.gwt.cell.client.ActionCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
     */
    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, T value, SafeHtmlBuilder sb) {

        sb.append(templates.button(m_title, m_cssClass));
    }
}
