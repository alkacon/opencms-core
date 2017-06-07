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
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.extractors.CmsExtractorMsOfficeOLE2;
import org.opencms.search.extractors.CmsExtractorPdf;
import org.opencms.search.extractors.CmsExtractorRtf;
import org.opencms.search.extractors.I_CmsTextExtractor;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;

import com.google.common.base.Optional;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Interprets two versions of a resource as text files, and shows a diff view for the two texts.<p>
 *
 * This should work for both plaintext files as well as binary documents which from which OpenCms can extract text content.
 */
public class CmsTextDiff implements I_CmsDiffProvider {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTextDiff.class);

    /**
     * @see org.opencms.ui.dialogs.history.diff.I_CmsDiffProvider#diff(org.opencms.file.CmsObject, org.opencms.gwt.shared.CmsHistoryResourceBean, org.opencms.gwt.shared.CmsHistoryResourceBean)
     */
    public Optional<Component> diff(CmsObject cms, CmsHistoryResourceBean v1, CmsHistoryResourceBean v2)
    throws CmsException {

        CmsResource resource1 = A_CmsAttributeDiff.readResource(cms, v1);
        String encoding = CmsLocaleManager.getResourceEncoding(cms, resource1);
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource1);
        if ((type instanceof CmsResourceTypeXmlContent)
            || (type instanceof CmsResourceTypePlain)
            || (type instanceof CmsResourceTypeJsp)
            || (type instanceof CmsResourceTypeXmlPage)
            || (type instanceof CmsResourceTypePointer)
            || (type instanceof CmsResourceTypeBinary)) {
            CmsResource resource2 = A_CmsAttributeDiff.readResource(cms, v2);

            String path1 = resource1.getRootPath();
            String path2 = resource2.getRootPath();

            CmsFile file1 = cms.readFile(resource1);
            CmsFile file2 = cms.readFile(resource2);

            byte[] content1 = file1.getContents();
            byte[] content2 = file2.getContents();

            String originalSource = null;
            String copySource = null;

            I_CmsTextExtractor textExtractor = null;
            // only if both files have contents
            if ((content1.length > 0) && (content2.length > 0)) {
                if (path1.endsWith(".pdf") && path2.endsWith(".pdf")) {
                    textExtractor = CmsExtractorPdf.getExtractor();
                } else if (path1.endsWith(".doc") && path2.endsWith(".doc")) {
                    textExtractor = CmsExtractorMsOfficeOLE2.getExtractor();
                } else if (path1.endsWith(".xls") && path2.endsWith(".xls")) {
                    textExtractor = CmsExtractorMsOfficeOLE2.getExtractor();
                } else if (path1.endsWith(".rtf") && path2.endsWith(".rtf")) {
                    textExtractor = CmsExtractorRtf.getExtractor();
                } else if (path1.endsWith(".ppt") && path2.endsWith(".ppt")) {
                    textExtractor = CmsExtractorMsOfficeOLE2.getExtractor();
                }
            }
            if (textExtractor != null) {
                try {
                    // extract the content
                    originalSource = textExtractor.extractText(content1).getContent();
                    copySource = textExtractor.extractText(content2).getContent();
                } catch (Exception e) {
                    // something goes wrong on extracting content
                    // set the content to null, so the content dialog will not be shown
                    originalSource = null;
                    copySource = null;
                    LOG.error(e.getMessage(), e);
                }
            } else if ((type instanceof CmsResourceTypePlain)
                || (type instanceof CmsResourceTypeJsp)
                || (type instanceof CmsResourceTypePointer)) {
                try {
                    originalSource = new String(content1, encoding);
                    copySource = new String(content2, encoding);
                } catch (UnsupportedEncodingException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            if ((copySource == null) || (originalSource == null)) {
                return Optional.absent();
            }
            try {
                CmsTextDiffPanel diffPanel = new CmsTextDiffPanel(originalSource, copySource, false, true);
                diffPanel.setWidth("100%");
                Panel panel = new Panel(
                    CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_TEXT_COMPARISON_CAPTION_0));
                panel.setWidth("100%");
                VerticalLayout vl = new VerticalLayout();
                vl.setMargin(true);
                vl.addComponent(diffPanel);

                panel.setContent(vl);
                return Optional.<Component> fromNullable(panel);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return Optional.absent();
            }
        } else {
            return Optional.absent();
        }
    }

    /**
    *
    * Returns a diff text wrapped with formatting style.<p>
    *
    * @param diff the text to wrap with CSS formatting
    * @return the text with formatting styles wrapped
    * @throws IOException if something goes wrong
    */
    protected String wrapLinesWithUnchangedStyle(String diff) throws IOException {

        String line;
        StringBuffer result = new StringBuffer();
        BufferedReader br = new BufferedReader(new StringReader(diff));
        while ((line = br.readLine()) != null) {
            if ("".equals(line.trim())) {
                line = "&nbsp;";
            }
            result.append("<div class=\"df-unc\"><span class=\"df-unc\">").append(line).append("</span></div>\n");
        }
        return result.toString();
    }

}
