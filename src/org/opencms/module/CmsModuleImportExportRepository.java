/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.module;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.importexport.CmsImportExportException;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/** 
 * Class which manages import/export of modules from repositories configured in opencms-importexport.xml.<p>
 */
public class CmsModuleImportExportRepository {

    /** Export folder path. */
    public static final String EXPORT_FOLDER_PATH = "packages/_export";

    /** Import folder path. */
    public static final String IMPORT_FOLDER_PATH = "packages/_import";

    /** Suffix for module zip files. */
    public static final String SUFFIX = ".zip";

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleImportExportRepository.class);

    /** The admin CMS context. */
    private CmsObject m_adminCms;

    /**
     * Creates a new instance.<p>
     */
    public CmsModuleImportExportRepository() {

        // nop

    }

    /**
     * Deletes the module corresponding to the given virtual module file name.<p>
     * 
     * @param fileName the file name 
     * @return true if the module could be deleted 
     * 
     * @throws CmsException if something goes wrong 
     */
    public synchronized boolean deleteModule(String fileName) throws CmsException {

        CmsModule module = getModuleForFileName(fileName);
        if (module == null) {
            LOG.error("Deletion request for invalid module file name: " + fileName);
            return false;
        }
        OpenCms.getModuleManager().deleteModule(m_adminCms, module.getName(), false, createReport());
        return true;

    }

    /** 
     * Exports a module and returns the export zip file content in a byte array.<p>
     *  
     * @param virtualModuleFileName the virtual file name for the module
     * @param project the project from which the module should be exported  
     * 
     * @return the module export data 
     * 
     * @throws CmsException if something goes wrong 
     */
    @SuppressWarnings("resource")
    public synchronized byte[] getExportedModuleData(String virtualModuleFileName, CmsProject project)
    throws CmsException {

        CmsModule module = getModuleForFileName(virtualModuleFileName);
        if (module == null) {
            LOG.warn("Invalid module export path requested: " + virtualModuleFileName);
            return null;
        }
        try {
            String moduleName = module.getName();
            ensureFoldersExist();

            String moduleFilePath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
                CmsStringUtil.joinPaths(EXPORT_FOLDER_PATH, moduleName + ".zip"));
            File moduleFile = new File(moduleFilePath);

            boolean needToRunExport = false;
            if (!moduleFile.exists()) {
                needToRunExport = true;
            } else if (moduleFile.lastModified() < module.getObjectCreateTime()) {
                needToRunExport = true;
                moduleFile.delete();
            }
            if (needToRunExport) {
                CmsModuleImportExportHandler handler = new CmsModuleImportExportHandler();
                handler.setAdditionalResources(module.getResources().toArray(new String[] {}));
                // the import/export handler adds the zip extension if it is not there, so we append it here  
                String tempFileName = RandomStringUtils.randomAlphanumeric(8) + ".zip";
                String tempFilePath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
                    CmsStringUtil.joinPaths(EXPORT_FOLDER_PATH, tempFileName));
                handler.setFileName(tempFilePath);
                handler.setModuleName(moduleName);
                CmsException exportException = null;
                try {
                    CmsObject exportCms = OpenCms.initCmsObject(m_adminCms);
                    exportCms.getRequestContext().setCurrentProject(project);
                    handler.exportData(exportCms, createReport());
                } catch (CmsException e) {
                    exportException = e;
                }
                if (exportException != null) {
                    new File(tempFilePath).delete();
                    throw exportException;
                }
                new File(tempFilePath).renameTo(new File(moduleFilePath));
            }
            byte[] result = CmsFileUtil.readFully(new FileInputStream(moduleFilePath));
            return result;
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Gets the list of modules as file names.<p>
     * 
     * @return the list of modules as file names 
     */
    public List<String> getModuleFileNames() {

        List<String> result = Lists.newArrayList();
        for (CmsModule module : OpenCms.getModuleManager().getAllInstalledModules()) {
            result.add(getFileNameForModule(module));
        }
        return result;
    }

    /** 
     * Imports module data.<p>
     * 
     * @param name the module file name 
     * @param content the module ZIP file data 
     * @throws CmsException if something goes wrong 
     */
    public synchronized void importModule(String name, byte[] content) throws CmsException {

        if (content.length == 0) {
            // Happens when using CmsResourceWrapperModules with JLAN and createResource is called 
            LOG.debug("Zero-length module import content, ignoring it...");
        } else {
            ensureFoldersExist();
            String targetFilePath = createImportZipPath(name);
            try {
                FileOutputStream out = new FileOutputStream(new File(targetFilePath));
                out.write(content);
                out.close();
            } catch (IOException e) {
                throw new CmsImportExportException(Messages.get().container(Messages.ERR_FILE_IO_1, targetFilePath));
            }
            CmsModuleImportExportHandler importHandler = new CmsModuleImportExportHandler();
            CmsModule module = CmsModuleImportExportHandler.readModuleFromImport(targetFilePath);
            String moduleName = module.getName();
            I_CmsReport report = createReport();
            if (OpenCms.getModuleManager().hasModule(moduleName)) {
                OpenCms.getModuleManager().deleteModule(m_adminCms, moduleName, true /*replace module*/, report);
            }
            CmsImportParameters params = new CmsImportParameters(targetFilePath, "/", false);
            importHandler.setImportParameters(params);
            importHandler.importData(m_adminCms, report);
            new File(targetFilePath).delete();
        }
    }

    /**
     * Initializes the CMS context.<p>
     * 
     * @param adminCms the admin CMS context 
     */
    public void initialize(CmsObject adminCms) {

        m_adminCms = adminCms;
    }

    /** 
     * Creates a new report for an export/import.<p>
     * 
     * @return the new report  
     */
    protected I_CmsReport createReport() {

        return new CmsLogReport(Locale.ENGLISH, CmsModuleImportExportRepository.class);
    }

    /** 
     * Creates a randomized path for the temporary file used to store the import data.<p>
     * 
     * @param name the module name
     *  
     * @return the generated path 
     */
    private String createImportZipPath(String name) {

        String path = "";
        do {
            String prefix = RandomStringUtils.randomAlphanumeric(6) + "-";
            path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
                CmsStringUtil.joinPaths(IMPORT_FOLDER_PATH, prefix + name));
        } while (new File(path).exists());
        return path;
    }

    /**
     * Makes sure that the folders used to store the import/export data exist.<p>
     */
    private void ensureFoldersExist() {

        for (String path : Arrays.asList(IMPORT_FOLDER_PATH, EXPORT_FOLDER_PATH)) {
            String folderPath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(path);
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
        }
    }

    /**
     * Gets the virtual file name to use for the given module.<p>
     * 
     * @param module the module for which to get the file name 
     *
     * @return the file name 
     */
    private String getFileNameForModule(CmsModule module) {

        return module.getName() + SUFFIX;
    }

    /**
     * Gets the module which corresponds to the given virtual file name.<p>
     * 
     * @param fileName the file name 
     * 
     * @return the module which corresponds to the given file name 
     */
    private CmsModule getModuleForFileName(String fileName) {

        String moduleName = fileName;
        if (fileName.endsWith(SUFFIX)) {
            moduleName = fileName.substring(0, fileName.length() - SUFFIX.length());
        }
        CmsModule result = OpenCms.getModuleManager().getModule(moduleName);
        return result;
    }

}
