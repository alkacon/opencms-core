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

package org.opencms.ui.dialogs.history.diff;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.dialogs.history.CmsHistoryDialog;
import org.opencms.ui.util.table.CmsBeanTableBuilder;
import org.opencms.util.CmsMacroResolver;
import org.opencms.workplace.comparison.CmsElementComparison;
import org.opencms.workplace.comparison.CmsXmlDocumentComparison;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Displays either a diff for the XML file, or a table displaying the differences between individual content values,
 * allowing the user to switch between the two views.<p>
 */
public class CmsValueDiff implements I_CmsDiffProvider {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsValueDiff.class);

    /**
     * @see org.opencms.ui.dialogs.history.diff.I_CmsDiffProvider#diff(org.opencms.file.CmsObject, org.opencms.gwt.shared.CmsHistoryResourceBean, org.opencms.gwt.shared.CmsHistoryResourceBean)
     */
    public Optional<Component> diff(final CmsObject cms, CmsHistoryResourceBean v1, CmsHistoryResourceBean v2)
    throws CmsException {

        CmsResource resource1 = A_CmsAttributeDiff.readResource(cms, v1);
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource1);
        CmsMacroResolver resolver = new CmsVersionMacroResolver(v1, v2);
        if ((type instanceof CmsResourceTypeXmlContent) || (type instanceof CmsResourceTypeXmlPage)) {
            CmsResource resource2 = A_CmsAttributeDiff.readResource(cms, v2);
            final Panel panel = new Panel(
                CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_CONTENT_VALUE_TABLE_CAPTION_0));

            final CmsFile file1 = cms.readFile(resource1);

            final CmsFile file2 = cms.readFile(resource2);
            VerticalLayout vl = new VerticalLayout();
            vl.setMargin(true);
            vl.setSpacing(true);
            Table table = buildValueComparisonTable(cms, panel, file1, file2, resolver);
            if (table.getContainerDataSource().size() == 0) {
                return Optional.absent();
            }
            Button fileTextCompareButton = new Button(
                CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_COMPARE_WHOLE_FILE_0));
            vl.addComponent(fileTextCompareButton);
            vl.setComponentAlignment(fileTextCompareButton, Alignment.MIDDLE_RIGHT);
            fileTextCompareButton.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                @SuppressWarnings("synthetic-access")
                public void buttonClick(ClickEvent event) {

                    Component diffView = buildWholeFileDiffView(cms, file1, file2);
                    CmsHistoryDialog.openChildDialog(
                        panel,
                        diffView,
                        CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_COMPARE_WHOLE_FILE_0));
                }
            });
            vl.addComponent(table);
            panel.setContent(vl);
            Component result = panel;
            return Optional.fromNullable(result);
        } else {
            return Optional.absent();
        }
    }

    /**
     * Builds the table for the content value comparisons.<p>
     *
     * @param cms the CMS context
     * @param parent the parent widget for the table (does not need to be the direct parent)
     * @param file1 the first file
     * @param file2 the second file
     * @param macroResolver the macro resolver to use for building the table
     *
     * @return the table with the content value comparisons
     *
     * @throws CmsException if something goes wrong
     */
    private Table buildValueComparisonTable(
        CmsObject cms,
        final Component parent,
        CmsFile file1,
        CmsFile file2,
        CmsMacroResolver macroResolver)
    throws CmsException {

        CmsXmlDocumentComparison comp = new CmsXmlDocumentComparison(cms, file1, file2);
        CmsBeanTableBuilder<CmsValueCompareBean> builder = CmsBeanTableBuilder.newInstance(
            CmsValueCompareBean.class,
            A_CmsUI.get().getDisplayType().toString());
        builder.setMacroResolver(macroResolver);

        List<CmsValueCompareBean> rows = Lists.newArrayList();
        for (CmsElementComparison entry : comp.getElements()) {
            final String text1 = entry.getVersion1();
            final String text2 = entry.getVersion2();
            if (Objects.equal(text1, text2)) {
                continue;
            }
            final CmsValueCompareBean row = new CmsValueCompareBean(cms, entry);
            row.getChangeType().addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    CmsTextDiffPanel diffPanel = new CmsTextDiffPanel(text1, text2, true, true);
                    diffPanel.setSizeFull();
                    CmsHistoryDialog.openChildDialog(
                        parent,
                        diffPanel,
                        CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_COMPARE_VALUE_1, row.getXPath()));
                }

            });
            rows.add(row);
        }
        Table table = builder.buildTable(rows);
        table.setSortEnabled(false);
        table.setWidth("100%");
        table.setPageLength(Math.min(rows.size(), 12));
        return table;
    }

    /**
     * Builds the diff view for the XML text.<p>
     *
     * @param cms the CMS context
     * @param file1 the first file
     * @param file2 the second file
     *
     * @return the diff view
     */
    private Component buildWholeFileDiffView(CmsObject cms, CmsFile file1, CmsFile file2) {

        String encoding = "UTF-8";
        try {
            CmsXmlContent content1 = CmsXmlContentFactory.unmarshal(cms, file1);
            encoding = content1.getEncoding();
        } catch (CmsException e) {
            String rootPath = file1.getRootPath();
            LOG.error(
                "Could not unmarshal file " + rootPath + " for determining encoding: " + e.getLocalizedMessage(),
                e);
        }
        String text1 = decode(file1.getContents(), encoding);
        String text2 = decode(file2.getContents(), encoding);
        CmsTextDiffPanel diffPanel = new CmsTextDiffPanel(text1, text2, false, true);
        return diffPanel;

    }

    /**
     * Decodes the given data with the given encoding, falling back to the system encoding if necessary.<p>
     *
     * @param data the data to decode
     * @param encoding the encoding to use
     *
     * @return if something goes wrong
     */
    private String decode(byte[] data, String encoding) {

        try {
            return new String(data, encoding);
        } catch (UnsupportedEncodingException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return new String(data);
        }
    }

}
