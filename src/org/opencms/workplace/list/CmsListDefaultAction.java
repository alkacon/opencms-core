/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListDefaultAction.java,v $
 * Date   : $Date: 2006/03/27 14:52:28 $
 * Version: $Revision: 1.18 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.18 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListDefaultAction extends CmsListDirectAction {

    /** the id of column to use for the link. */
    private String m_columnForLink;

    /**
     * Default Constructor.<p>
     * 
     * @param id unique id
     */
    public CmsListDefaultAction(String id) {

        super(id);
    }

    /**
     * Sets the id of column to use for the link.<p>
     *
     * @param columnForLink the id of column to use for the link to set
     */
    public void setColumnForLink(String columnForLink) {

        m_columnForLink = columnForLink;
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#resolveButtonStyle()
     */
    protected CmsHtmlIconButtonStyleEnum resolveButtonStyle() {

        if (getColumnForLink() == null) {
            return super.resolveButtonStyle();
        }
        return CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT;
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#resolveName(java.util.Locale)
     */
    protected String resolveName(Locale locale) {

        if (getColumnForLink() == null) {
            return super.resolveName(locale);
        }
        return (getItem().get(getColumnForLink()) != null) ? getItem().get(getColumnForLink()).toString()
        : getName().key(locale);
    }

    /**
     * Resturns the id of column to use for the link.<p>
     * 
     * @return the id of column to use for the link
     */
    private String getColumnForLink() {

        return m_columnForLink;
    }
}