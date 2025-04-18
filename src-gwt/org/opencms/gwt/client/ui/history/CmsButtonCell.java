/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client.ui.history;

import com.google.common.base.Predicate;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell class for rendering a button inside a cell table.<p>
 *
 * @param <T> the cell type
 */
public class CmsButtonCell<T> extends ActionCell<T> {

    /** Function to check whether the button should be available. */
    private Predicate<T> m_checkActive;

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
     * @param checkActive a predicate to check whether the button should be active
     */
    public CmsButtonCell(
        String title,
        String cssClass,
        final ActionCell.Delegate<T> delegate,
        final Predicate<T> checkActive) {

        super("", new ActionCell.Delegate<T>() {

            public void execute(T object) {

                if (checkActive.apply(object)) {
                    delegate.execute(object);
                }
            }
        });
        m_title = title;
        m_cssClass = cssClass;
        m_checkActive = checkActive;
    }

    /**
     * @see com.google.gwt.cell.client.ActionCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
     */
    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, T value, SafeHtmlBuilder sb) {

        if (m_checkActive.apply(value)) {
            sb.append(CmsResourceHistoryTable.templates.button(m_title, m_cssClass));
        }
    }
}
