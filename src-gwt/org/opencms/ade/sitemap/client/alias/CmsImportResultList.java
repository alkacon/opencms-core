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

/**
 * A widget used for displaying the results of an alias import operation.<p>
 */
public class CmsImportResultList extends Composite {

    /** The CSS classes used for this widget.<p> */
    public static interface I_Css extends CssResource {

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         */
        String aliasImportError();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         */
        String aliasImportOk();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         */
        String aliasImportOverwrite();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         */
        String rightLabel();
    }

    /**
     * The resource bundle used for this widget.<p>
     */
    public static interface I_Resources extends ClientBundle {

        /**
         * CSS bundle accessor.<p>
         *
         * @return the CSS bundle for this widget
         */
        @Source("resultlabel.css")
        I_Css css();
    }

    /**
     * Static instance of the resource bundle for this widget.<p>
     */
    public static final I_Resources RESOURCES = GWT.create(I_Resources.class);

    /** A label which is displayed before any alias files are imported. */
    protected Label m_emptyLabel;

    /** The main panel containing the other parts of this widget.<p> */
    private FlowPanel m_root = new FlowPanel();

    /** The table containing the messages for each single import operation. */
    private FlexTable m_table = new FlexTable();

    /**
     * Default constructor.<p>
     */
    public CmsImportResultList() {

        m_root.add(m_table);
        initWidget(m_root);
        ensureEmptyLabel();
    }

    static {
        RESOURCES.css().ensureInjected();
    }

    /**
     * Adds a single line of the import result to the widget.<p>
     *
     * @param leftText the text to display on the left
     * @param rightText the text to display on the right
     * @param styleName the style which should be applied to the right text
     */
    public void addRow(String leftText, String rightText, String styleName) {

        ensureTable();
        ensureNoEmptyLabel();
        int row = m_table.getRowCount();
        m_table.setWidget(row, 0, new Label(leftText));
        Label rightLabel = new Label(rightText);
        rightLabel.addStyleName(styleName);
        rightLabel.addStyleName(RESOURCES.css().rightLabel());
        m_table.setWidget(row, 1, rightLabel);
    }

    /**
     * Clears the result list.<p>
     */
    public void clear() {

        m_root.clear();
        ensureEmptyLabel();
        m_table = null;
    }

    /**
     * Ensures the existence of the 'empty' label.<p>
     */
    protected void ensureEmptyLabel() {

        if (m_emptyLabel == null) {
            m_emptyLabel = new Label(CmsAliasMessages.messagesEmptyImportResult());
        }
        m_root.add(m_emptyLabel);
    }

    /**
     * Ensure that the 'empty' label does not exist.<p>
     */
    protected void ensureNoEmptyLabel() {

        if (m_emptyLabel != null) {
            m_emptyLabel.removeFromParent();
            m_emptyLabel = null;
        }
    }

    /**
     * Ensures that the table is present in the widget.<p>
     */
    private void ensureTable() {

        if (m_table == null) {
            m_table = new FlexTable();
            m_root.add(m_table);
        }

    }

}
