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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.list;

import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

import java.util.Locale;

/**
 * Implementation of a default action in a html list column.<p>
 *
 * @since 6.0.0
 */
public class CmsListDefaultAction extends CmsListDirectAction {

    /** The id of column to use for the link. */
    private String m_columnForLink;

    /** The formatter to use for the link. */
    private I_CmsListFormatter m_columnFormatter;

    /**
     * Default Constructor.<p>
     *
     * @param id unique id
     */
    public CmsListDefaultAction(String id) {

        super(id);
    }

    /**
     * Resturns the id of column to use for the link.<p>
     *
     * @return the id of column to use for the link
     */
    public String getColumnForLink() {

        return m_columnForLink;
    }

    /**
     * Resturns the formatter to use for the link.<p>
     *
     * @return the formatter to use for the link
     */
    public I_CmsListFormatter getColumnFormatter() {

        return m_columnFormatter;
    }

    /**
     * Sets the id of column to use for the link.<p>
     *
     * @param columnForLink the column to use for the link to set
     */
    public void setColumnForLink(CmsListColumnDefinition columnForLink) {

        m_columnForLink = columnForLink.getId();
        m_columnFormatter = columnForLink.getFormatter();
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#resolveButtonStyle()
     */
    @Override
    protected CmsHtmlIconButtonStyleEnum resolveButtonStyle() {

        if (getColumnForLink() == null) {
            return super.resolveButtonStyle();
        }
        return CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT;
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#resolveName(java.util.Locale)
     */
    @Override
    protected String resolveName(Locale locale) {

        if (getColumnForLink() == null) {
            return super.resolveName(locale);
        }
        Object content = (getItem().get(getColumnForLink()) != null)
        ? getItem().get(getColumnForLink())
        : getName().key(locale);
        if (getColumnFormatter() != null) {
            return getColumnFormatter().format(content, locale);
        }
        return content.toString();
    }
}