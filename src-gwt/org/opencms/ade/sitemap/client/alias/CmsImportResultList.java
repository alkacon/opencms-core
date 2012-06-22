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

package org.opencms.ade.sitemap.client.alias;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class CmsImportResultList extends Composite {

    public static interface I_Css extends CssResource {

        String aliasImportError();

        String aliasImportOk();

        String aliasImportOverwrite();

        String rightLabel();
    }

    public static interface I_Resources extends ClientBundle {

        @Source("resultlabel.css")
        public I_Css css();
    }

    public static final I_Resources RESOURCES = GWT.create(I_Resources.class);

    private FlowPanel m_root = new FlowPanel();

    private FlexTable m_table = new FlexTable();

    public CmsImportResultList() {

        m_root.add(m_table);
        initWidget(m_root);
    }

    static {
        RESOURCES.css().ensureInjected();
    }

    public void addRow(String leftText, String rightText, String styleName) {

        ensureTable();
        int row = m_table.getRowCount();
        m_table.setWidget(row, 0, new Label(leftText));
        Label rightLabel = new Label(rightText);
        rightLabel.addStyleName(styleName);
        rightLabel.addStyleName(RESOURCES.css().rightLabel());
        m_table.setWidget(row, 1, rightLabel);
    }

    public void clear() {

        m_root.clear();
        m_table = null;
    }

    private void ensureTable() {

        if (m_table == null) {
            m_table = new FlexTable();
            m_root.add(m_table);
        }
    }

}
