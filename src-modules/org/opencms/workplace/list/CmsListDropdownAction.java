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

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.CmsIdentifiableObjectContainer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Independent action to switch the resource state filter.<p>
 *
 * Do not forget to add parameter evaluation code overwritting the A_CmsListDialog#getList() method,
 * like in <tt>CmsProjectFilesDialog</tt>.<p>
 *
 * @since 6.0.0
 */
public class CmsListDropdownAction extends CmsListIndependentAction {

    /** parameter name suffix. */
    public static final String SUFFIX_PARAM = "-sel";

    /** The item ids, as a list of String objects. */
    private List<String> m_ids = new ArrayList<String>();

    /** The items, a map of [ids, display names] as [String, CmsMessageContainer] objects. */
    private CmsIdentifiableObjectContainer<CmsMessageContainer> m_items = new CmsIdentifiableObjectContainer<CmsMessageContainer>(
        true,
        false);

    /** The selected item id. */
    private String m_selection;

    /**
     * Default Constructor.<p>
     *
     * @param id unique id
     */
    public CmsListDropdownAction(String id) {

        super(id);
    }

    /**
     * Adds an item to be displayed in the drop-down list.<p>
     *
     * @param id the id of the item
     * @param name the display name
     */
    public void addItem(String id, CmsMessageContainer name) {

        m_ids.add(id);
        m_items.addIdentifiableObject(id, name);
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(org.opencms.workplace.CmsWorkplace)
     */
    @Override
    public String buttonHtml(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(512);
        html.append("\t<span class='link'");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getHelpText().key(wp.getLocale()))) {
            html.append(" onMouseOver=\"sMH('");
            html.append(getId());
            html.append("');\" onMouseOut=\"hMH('");
            html.append(getId());
            html.append("');\"");
        }
        html.append("><p>");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getIconPath())) {
            html.append("<img src='");
            html.append(CmsWorkplace.getSkinUri());
            if (!isEnabled()) {
                StringBuffer icon = new StringBuffer(128);
                icon.append(getIconPath().substring(0, getIconPath().lastIndexOf('.')));
                icon.append("_disabled");
                icon.append(getIconPath().substring(getIconPath().lastIndexOf('.')));
                if (wp.getCms().existsResource(CmsWorkplace.VFS_PATH_RESOURCES + icon.toString())) {
                    html.append(icon);
                } else {
                    html.append(getIconPath());
                }
            } else {
                html.append(getIconPath());
            }
            html.append("'");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getName().key(wp.getLocale()))) {
                html.append(" alt='");
                html.append(getName().key(wp.getLocale()));
                html.append("'");
                html.append(" title='");
                html.append(getName().key(wp.getLocale()));
                html.append("'");
            }
            html.append(">");
        }
        html.append(getName().key(wp.getLocale()));
        html.append("<select name='").append(getId()).append(SUFFIX_PARAM).append("' onchange=\"");
        html.append(resolveOnClic(wp)).append("\">\n");
        Iterator<String> it = m_ids.iterator();
        while (it.hasNext()) {
            String itemId = it.next();
            html.append("\t\t\t\t<option value='");
            html.append(itemId);
            html.append("'");
            html.append(itemId.equals(getSelection()) ? " selected" : "");
            html.append(">");
            html.append(m_items.getObject(itemId).key(wp.getLocale()));
            html.append("</option>\n");
        }
        html.append("</select>\n");
        html.append("</p></span>");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getHelpText().key(wp.getLocale()))) {
            html.append("<div class='help' id='help");
            html.append(getId());
            html.append("' onMouseOver=\"sMH('");
            html.append(getId());
            html.append("');\" onMouseOut=\"hMH('");
            html.append(getId());
            html.append("');\">");
            html.append(getHelpText().key(wp.getLocale()));
            html.append("</div>\n");
        }
        return html.toString();
    }

    /**
     * Returns the selected item.<p>
     *
     * @return the selected item
     */
    public String getSelection() {

        return m_selection;
    }

    /**
     * Sets the selected item.<p>
     *
     * @param selection the selected item to set
     */
    public void setSelection(String selection) {

        m_selection = selection;
    }
}