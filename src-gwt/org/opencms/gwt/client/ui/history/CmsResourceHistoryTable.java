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

package org.opencms.gwt.client.ui.history;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsCellTableResources;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.gwt.shared.CmsHistoryResourceCollection;

import com.google.common.base.Predicate;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;

/**
 * Cell table used to display historical versions of a content.<p>
 *
 * Has buttons to preview or restore a previous version.<p>
 */
public class CmsResourceHistoryTable extends CellTable<CmsHistoryResourceBean> {

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

        /**
         * Template for a span with a title.<p>
         *
         * @param text the span text
         * @param title the span title
         *
         * @return the HTML for the span
         */
        @Template("<span title=\"{1}\">{0}</span>")
        SafeHtml textSpanWithTitle(String text, String title);
    }

    /** The template instance. */
    static Templates templates = GWT.create(Templates.class);
    /** Handler instance for performing actions on the table entries. */
    private I_CmsHistoryActionHandler m_handler;

    /**
     * Creates a new instance.<p>
     *
     * @param data the data to display in the table
     * @param handler the handler instance used for performing actions on the table entries
     */
    public CmsResourceHistoryTable(CmsHistoryResourceCollection data, I_CmsHistoryActionHandler handler) {

        super(
            Integer.MAX_VALUE,
            (CellTable.Resources)GWT.create(I_CmsCellTableResources.class),
            new ProvidesKey<CmsHistoryResourceBean>() {

                public Object getKey(CmsHistoryResourceBean item) {

                    return item.getStructureId() + "_" + item.getVersion();
                }

            });
        m_handler = handler;
        setWidth("100%", true);
        setTableLayoutFixed(true);

        addVersionColumn();
        addPreviewColumn();
        addRevertColumn();
        addPathColumn();
        addSizeColumn();
        addModificationDateColumn();
        addUserLastModifiedColumn();
        addPublishDateColumn();

        ListDataProvider<CmsHistoryResourceBean> dataProvider = new ListDataProvider<CmsHistoryResourceBean>();
        dataProvider.addDataDisplay(this);
        dataProvider.setList(data.getResources());
    }

    /**
     * Helper method for adding a table column with a given width and label.<p>
     *
     * @param label the column label
     * @param width the column width in pixels
     * @param col the column to add
     */
    private void addColumn(String label, int width, Column<CmsHistoryResourceBean, ?> col) {

        addColumn(col, label);
        setColumnWidth(col, width, Unit.PX);
    }

    /**
     * Adds a table column.<p>
     */
    private void addModificationDateColumn() {

        addColumn(CmsHistoryMessages.columnModificationDate(), 190, new TextColumn<CmsHistoryResourceBean>() {

            @Override
            public String getValue(CmsHistoryResourceBean historyRes) {

                return historyRes.getModificationDate().getDateText();
            }
        });
    }

    /**
     * Adds a table column.<p>
     */
    private void addPathColumn() {

        Column<CmsHistoryResourceBean, ?> col = new TextColumn<CmsHistoryResourceBean>() {

            @Override
            public String getValue(CmsHistoryResourceBean historyRes) {

                String path = historyRes.getRootPath();
                String siteRoot = CmsCoreProvider.get().getSiteRoot();
                if (path.startsWith(siteRoot)) {
                    path = path.substring(siteRoot.length());
                    if (!path.startsWith("/")) {
                        path = "/" + path;
                    }
                }
                return path;
            }
        };
        addColumn(col, CmsHistoryMessages.columnPath());
        setColumnWidth(col, 100, Unit.PCT);
    }

    /**
     * Adds a table column.<p>
     */
    private void addPreviewColumn() {

        CmsButtonCell<CmsHistoryResourceBean> previewCell = new CmsButtonCell<CmsHistoryResourceBean>(
            CmsHistoryMessages.titlePreview(),
            I_CmsButton.ICON_FONT + " " + I_CmsButton.PREVIEW_SMALL,
            new ActionCell.Delegate<CmsHistoryResourceBean>() {

                @SuppressWarnings("synthetic-access")
                public void execute(CmsHistoryResourceBean historyRes) {

                    m_handler.showPreview(historyRes);
                }
            },
            new Predicate<CmsHistoryResourceBean>() {

                public boolean apply(CmsHistoryResourceBean bean) {

                    return true;

                }
            });

        addColumn(CmsHistoryMessages.columnPreview(), 30, new IdentityColumn<CmsHistoryResourceBean>(previewCell));
    }

    /**
     * Adds a table column.<p>
     */
    private void addPublishDateColumn() {

        addColumn(CmsHistoryMessages.columnPublishDate(), 190, new TextColumn<CmsHistoryResourceBean>() {

            @Override
            public String getValue(CmsHistoryResourceBean historyRes) {

                if (historyRes.getPublishDate() != null) {
                    return historyRes.getPublishDate().getDateText();
                }
                return "-";
            }
        });
    }

    /**
     * Adds a table column.<p>
     */
    private void addRevertColumn() {

        CmsButtonCell<CmsHistoryResourceBean> replaceCell = new CmsButtonCell<CmsHistoryResourceBean>(
            CmsHistoryMessages.titleRevert(),
            I_CmsButton.ICON_FONT + " " + I_CmsButton.RESET,
            new ActionCell.Delegate<CmsHistoryResourceBean>() {

                @SuppressWarnings("synthetic-access")
                public void execute(CmsHistoryResourceBean historyRes) {

                    m_handler.revert(historyRes);
                }
            },
            new Predicate<CmsHistoryResourceBean>() {

                public boolean apply(CmsHistoryResourceBean bean) {

                    return bean.getVersion().getVersionNumber() != null;

                }
            });
        addColumn(CmsHistoryMessages.columnReplace(), 30, new IdentityColumn<CmsHistoryResourceBean>(replaceCell));
    }

    /**
     * Adds a table column.<p>
     */
    private void addSizeColumn() {

        Column<CmsHistoryResourceBean, ?> col = new TextColumn<CmsHistoryResourceBean>() {

            @Override
            public String getValue(CmsHistoryResourceBean historyRes) {

                return "" + historyRes.getSize();
            }
        };
        addColumn(col, CmsHistoryMessages.columnSize());
        setColumnWidth(col, 100, Unit.PX);
    }

    /**
     * Adds a table column.<p>
     */
    private void addUserLastModifiedColumn() {

        addColumn(CmsHistoryMessages.columnUserLastModified(), 120, new TextColumn<CmsHistoryResourceBean>() {

            @Override
            public String getValue(CmsHistoryResourceBean historyRes) {

                return historyRes.getUserLastModified();
            }
        });
    }

    /**
     * Adds a table column.<p>
     */
    private void addVersionColumn() {

        AbstractCell<CmsHistoryResourceBean> cell = new CmsVersionCell();
        addColumn(CmsHistoryMessages.columnVersion(), 40, new IdentityColumn<CmsHistoryResourceBean>(cell));
    }

}
