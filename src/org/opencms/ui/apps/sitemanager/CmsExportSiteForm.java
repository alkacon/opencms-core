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

package org.opencms.ui.apps.sitemanager;

import org.opencms.file.CmsObject;
import org.opencms.importexport.CmsExportParameters;
import org.opencms.importexport.CmsVfsImportExportHandler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule.ExportMode;
import org.opencms.report.A_CmsReportThread;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.dbmanager.CmsExportThreadDialog;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.util.CmsFileUtil;
import org.opencms.workplace.threads.CmsExportThread;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

/**
 * Dialog used to export the contents of a site.
 */
public class CmsExportSiteForm extends CmsBasicDialog {

    /** The log for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExportSiteForm.class);

    /** The Cancel button. */
    protected Button m_cancel;

    /** The CMS context. */
    protected CmsObject m_cms;

    /** The site manager instance. */
    protected CmsSiteManager m_manager;

    /** The OK button. */
    protected Button m_ok;

    /** The project label. */
    protected Label m_projectLabel;

    /** The site root of the site to export. */
    protected String m_siteRoot;

    /**
     * Creates a new instance.
     *
     * @param cms the CMS context
     * @param manager the site manager app instance
     * @param siteRoot the site root of the site to export
     */
    public CmsExportSiteForm(CmsObject cms, CmsSiteManager manager, String siteRoot) {

        m_manager = manager;
        m_siteRoot = siteRoot;
        m_cms = cms;
        String projectName = cms.getRequestContext().getCurrentProject().getName();

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_projectLabel.setValue(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_EXPORT_PROJECT_LABEL_1, projectName));
        m_cancel.addClickListener(event -> CmsVaadinUtils.closeWindow(CmsExportSiteForm.this));
        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
        if (site != null) {
            CmsResourceInfo resourceInfo = new CmsResourceInfo(
                site.getTitle(),
                site.getSiteRoot(),
                m_manager.getFavIcon(site.getSiteRoot()));
            resourceInfo.addStyleName("o-res-site-info");
            displayResourceInfoDirectly(Collections.singletonList(resourceInfo));
        }
        m_ok.addClickListener(event -> startThread());
    }

    /**
     * Starts the export thread and displays it's report.<p>
     */
    protected void startThread() {

        CmsObject cms = null;
        try {
            cms = OpenCms.initCmsObject(m_cms);
        } catch (CmsException e1) {
            LOG.error(e1.getLocalizedMessage(), e1);
        }
        cms.getRequestContext().setSiteRoot(m_siteRoot);
        CmsExportParameters m_exportParams = new CmsExportParameters();

        m_exportParams.setExportAccountData(false);
        m_exportParams.setExportAsFiles(false);
        m_exportParams.setExportProjectData(false);
        m_exportParams.setExportResourceData(true);
        m_exportParams.setInProject(false);
        m_exportParams.setIncludeSystemFolder(false);
        m_exportParams.setIncludeUnchangedResources(true);
        m_exportParams.setSkipParentFolders(false);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        String dateStr = fmt.format(new Date());
        String filename = dateStr
            + "_"
            + CmsFileUtil.removeTrailingSeparator(CmsFileUtil.removeLeadingSeparator(m_siteRoot)).replace("/", "_")
            + ".zip";
        String exportFilePath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            OpenCms.getSystemInfo().getPackagesRfsPath() + File.separator + filename);
        m_exportParams.setPath(exportFilePath);
        m_exportParams.setRecursive(true);
        m_exportParams.setResources(Collections.singletonList("/"));
        m_exportParams.setExportMode(ExportMode.DEFAULT);
        m_exportParams.setContentAge(0);

        CmsVfsImportExportHandler handler = new CmsVfsImportExportHandler();
        handler.setExportParams(m_exportParams);
        A_CmsReportThread exportThread = new CmsExportThread(cms, handler, false);

        Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
        window.setContent(new CmsExportThreadDialog(handler, exportThread, window));
        A_CmsUI.get().addWindow(window);
        CmsVaadinUtils.closeWindow(CmsExportSiteForm.this);
        exportThread.start();
    }

}
