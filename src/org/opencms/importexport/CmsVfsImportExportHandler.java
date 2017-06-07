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

package org.opencms.importexport;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.module.CmsModule.ExportMode;
import org.opencms.module.CmsModuleXmlHandler;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Import/export handler implementation for VFS data.<p>
 *
 * @since 6.0.0
 */
public class CmsVfsImportExportHandler implements I_CmsImportExportHandler {

    /** The description of this import/export handler. */
    private long m_contentAge;

    /** The description of this import/export handler.<p> */
    private String m_description;

    /** The export parameters. */
    private CmsExportParameters m_exportParams;

    /** The VFS paths to be exported. */
    private List<String> m_exportPaths;

    /** The name of the export file in the real file system. */
    private boolean m_exportUserdata;

    /** The name of the export file in the real file system.<p> */
    private String m_fileName;

    /** The import parameters. */
    private CmsImportParameters m_importParams;

    /** Boolean flag to decide whether VFS resources under /system/ should be exported or not. */
    private boolean m_includeSystem;

    /** Boolean flag to decide whether unchanged resources should be exported or not. */
    private boolean m_includeUnchanged;

    /** Boolean flag to indicate that only the resources of the current project should be exported. */
    private boolean m_projectOnly;

    /** Boolean flag to indicate if the folders are exported recursively or not. */
    private boolean m_recursive;

    /**
     * Creates a new VFS import/export handler.<p>
     */
    public CmsVfsImportExportHandler() {

        super();
        m_description = Messages.get().getBundle().key(Messages.GUI_CMSIMPORTHANDLER_DEFAULT_DESC_0);
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#exportData(org.opencms.file.CmsObject, org.opencms.report.I_CmsReport)
     */
    public void exportData(CmsObject cms, I_CmsReport report)
    throws CmsImportExportException, CmsRoleViolationException {

        CmsExportParameters parameters = getExportParams();
        if (parameters == null) {
            parameters = new CmsExportParameters(
                getFileName(),
                null,
                true,
                isExportUserdata(),
                false,
                getExportPaths(),
                isIncludeSystem(),
                isIncludeUnchanged(),
                getContentAge(),
                isRecursive(),
                isProjectOnly(),
                ExportMode.DEFAULT);
        }

        report.println(Messages.get().container(Messages.RPT_EXPORT_DB_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        new CmsExport(cms, report).exportData(parameters);
        report.println(Messages.get().container(Messages.RPT_EXPORT_DB_END_0), I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Returns the timestamp to limit the resources to be exported by date.<p>
     *
     * Only resources that have been modified after this date will be exported.<p>
     *
     * @return the timestamp to limit the resources to be exported by date
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public long getContentAge() {

        return m_contentAge;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#getDescription()
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the export parameters.<p>
     *
     * @return the export parameters
     */
    public CmsExportParameters getExportParams() {

        return m_exportParams;
    }

    /**
     * Returns the list with VFS paths to be exported.<p>
     *
     * @return the list with VFS paths to be exported
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public List<String> getExportPaths() {

        return m_exportPaths;
    }

    /**
     * Returns the name of the export file in the real file system.<p>
     *
     * @return the name of the export file in the real file system
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public String getFileName() {

        return m_fileName;
    }

    /**
     * Returns the import parameters.<p>
     *
     * @return the import parameters
     */
    public CmsImportParameters getImportParameters() {

        return m_importParams;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#importData(CmsObject, I_CmsReport)
     */
    public synchronized void importData(CmsObject cms, I_CmsReport report)
    throws CmsImportExportException, CmsXmlException, CmsRoleViolationException {

        report.println(Messages.get().container(Messages.RPT_IMPORT_DB_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);

        CmsImport vfsImport = new CmsImport(cms, report);
        vfsImport.importData(getImportParameters());

        report.println(Messages.get().container(Messages.RPT_IMPORT_DB_END_0), I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#importData(org.opencms.file.CmsObject, java.lang.String, java.lang.String, org.opencms.report.I_CmsReport)
     *
     * @deprecated use {@link #importData(CmsObject, I_CmsReport)} instead
     */
    @Deprecated
    public void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report)
    throws CmsXmlException, CmsImportExportException, CmsRoleViolationException, CmsException {

        CmsImportParameters parameters = new CmsImportParameters(importFile, importPath, true);

        setImportParameters(parameters);

        importData(cms, report);
    }

    /**
     * Returns the boolean flag to decide whether user/group data should be exported or not.<p>
     *
     * @return true, if user/group data should be exported
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public boolean isExportUserdata() {

        return m_exportUserdata;
    }

    /**
     * Returns the boolean flag to decide whether VFS resources under /system/ should be exported or not.<p>
     *
     * @return true, if VFS resources under /system/ should not be exported
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public boolean isIncludeSystem() {

        return m_includeSystem;
    }

    /**
     * Returns the boolean flag to decide whether unchanged resources should be exported or not.<p>
     *
     * @return true, if unchanged resources should not be exported
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public boolean isIncludeUnchanged() {

        return m_includeUnchanged;
    }

    /**
     * Returns the projectOnly.<p>
     *
     * @return the projectOnly
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public boolean isProjectOnly() {

        return m_projectOnly;
    }

    /**
     * Returns the recursive flag.<p>
     *
     * @return the recursive flag
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public boolean isRecursive() {

        return m_recursive;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#matches(org.dom4j.Document)
     */
    public boolean matches(Document manifest) {

        Element rootElement = manifest.getRootElement();
        boolean hasModuleNode = (rootElement.selectNodes(
            "./" + CmsModuleXmlHandler.N_MODULE + "/" + CmsModuleXmlHandler.N_NAME).size() > 0);
        boolean hasInfoNode = (rootElement.selectNodes("./" + CmsImportExportManager.N_INFO).size() == 1);

        return (!hasModuleNode && hasInfoNode);
    }

    /**
     * Sets the timestamp to limit the resources to be exported by date.<p>
     *
     * Only resources that have been modified after this date will be exported.<p>
     *
     * @param contentAge the timestamp to limit the resources to be exported by date
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public void setContentAge(long contentAge) {

        if (contentAge < 0) {
            String ageString = Long.toString(contentAge);
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_BAD_CONTENT_AGE_1, ageString));
        }
        m_contentAge = contentAge;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#setDescription(java.lang.String)
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the export parameters.<p>
     *
     * @param exportParams the parameters to set
     */
    public void setExportParams(CmsExportParameters exportParams) {

        m_exportParams = exportParams;
    }

    /**
     * Sets the list with VFS paths to be exported.<p>
     *
     * @param exportPaths the list with VFS paths to be exported
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public void setExportPaths(List<String> exportPaths) {

        m_exportPaths = exportPaths;
    }

    /**
     * Sets the boolean flag to decide whether user/group data should be exported or not.<p>
     *
     * @param exportUserdata true, if user/group data should not be exported
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public void setExportUserdata(boolean exportUserdata) {

        m_exportUserdata = exportUserdata;
    }

    /**
     * Sets the name of the export file in the real file system.<p>
     *
     * @param fileName the name of the export file in the real file system
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public void setFileName(String fileName) {

        if (CmsStringUtil.isEmpty(fileName) || !fileName.trim().equals(fileName)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_BAD_FILE_NAME_1, fileName));
        }
        m_fileName = fileName;
    }

    /**
     * Sets the import parameters.<p>
     *
     * @param importParams the parameters to set
     */
    public void setImportParameters(CmsImportParameters importParams) {

        m_importParams = importParams;
    }

    /**
     * Sets the boolean flag to decide whether VFS resources under /system/ should be exported or not.<p>
     *
     * @param excludeSystem true, if VFS resources under /system/ should not be exported
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public void setIncludeSystem(boolean excludeSystem) {

        m_includeSystem = excludeSystem;
    }

    /**
     * Sets the boolean flag to decide whether unchanged resources should be exported or not.<p>
     *
     * @param excludeUnchanged true, if unchanged resources should not be exported
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public void setIncludeUnchanged(boolean excludeUnchanged) {

        m_includeUnchanged = excludeUnchanged;
    }

    /**
     * Sets the projectOnly.<p>
     *
     * @param projectOnly the projectOnly to set
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public void setProjectOnly(boolean projectOnly) {

        m_projectOnly = projectOnly;
    }

    /**
     * Sets the recursive flag.<p>
     *
     * @param recursive the recursive flag to set
     *
     * @deprecated use {@link #setExportParams(CmsExportParameters)} instead
     */
    @Deprecated
    public void setRecursive(boolean recursive) {

        m_recursive = recursive;
    }
}
