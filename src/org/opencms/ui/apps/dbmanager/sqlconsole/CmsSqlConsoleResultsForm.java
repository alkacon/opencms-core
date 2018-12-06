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

package org.opencms.ui.apps.dbmanager.sqlconsole;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.Table;

/**
 * Displays results from an SQL query.<p>
 */
public class CmsSqlConsoleResultsForm extends CmsBasicDialog {

    /**
     * CSV generator for the download button.
     */
    public class CsvSource implements StreamSource {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /** The results. */
        private CmsSqlConsoleResults m_results;

        /**
         * Creates a new instance.
         *
         * @param results the results
         */
        public CsvSource(CmsSqlConsoleResults results) {

            m_results = results;

        }

        /**
         * @see com.vaadin.server.StreamResource.StreamSource#getStream()
         */
        public InputStream getStream() {

            try {
                return new ByteArrayInputStream(m_results.getCsv().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }

    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The CSV download button. */
    protected Button m_csv;

    /** The OK button. */
    protected Button m_ok;

    /** The table container. */
    protected VerticalLayout m_tableContainer;

    /** The label for displaying the report output. */
    private Label m_reportOutput;

    /**
     * Creates a new instance.<p>
     *
     * @param results the database results
     * @param reportOutput the report output
     */
    public CmsSqlConsoleResultsForm(CmsSqlConsoleResults results, String reportOutput) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_ok.addClickListener(evt -> CmsVaadinUtils.getWindow(CmsSqlConsoleResultsForm.this).close());
        if (results != null) {
            Table table = buildTable(results);
            m_tableContainer.addComponent(table);
            StreamResource res = new StreamResource(new CsvSource(results), "data.csv");
            res.setMIMEType("text/plain; charset=utf-8");
            FileDownloader downloader = new FileDownloader(res);
            downloader.extend(m_csv);
        } else {
            m_csv.setVisible(false);
        }
        m_reportOutput.setContentMode(ContentMode.PREFORMATTED);
        m_reportOutput.setValue(reportOutput);

    }

    /**
     * Builds the table for the database results.
     *
     * @param results the database results
     * @return the table
     */
    private Table buildTable(CmsSqlConsoleResults results) {

        IndexedContainer container = new IndexedContainer();
        int numCols = results.getColumns().size();
        for (int c = 0; c < numCols; c++) {
            container.addContainerProperty(Integer.valueOf(c), results.getColumnType(c), null);
        }
        int r = 0;
        for (List<Object> row : results.getData()) {
            Item item = container.addItem(Integer.valueOf(r));
            for (int c = 0; c < numCols; c++) {
                item.getItemProperty(Integer.valueOf(c)).setValue(row.get(c));
            }
            r += 1;
        }
        Table table = new Table();
        table.setContainerDataSource(container);
        for (int c = 0; c < numCols; c++) {
            String col = (results.getColumns().get(c));
            table.setColumnHeader(Integer.valueOf(c), col);
        }
        table.setWidth("100%");
        table.setHeight("100%");
        table.setColumnCollapsingAllowed(true);
        return table;
    }

}
