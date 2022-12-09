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

package org.opencms.module;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.importexport.CmsImportExportException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModuleLog.Action;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Class which manages import/export of modules from repositories configured in opencms-importexport.xml.<p>
 */
public class CmsModuleImportExportRepository {

    /**
     * Holds exported module data and a modification date.
     */
    public static class ModuleExportData {

        /** The file content. */
        private byte[] m_content;

        /** The modification date. */
        private long m_dateLastModified;

        /**
         * Creates a new instance.
         *
         * @param content the exported data
         * @param dateLastModified the modification date
         */
        public ModuleExportData(byte[] content, long dateLastModified) {

            m_content = content;
            m_dateLastModified = dateLastModified;
        }

        /**
         * Gets the exported data.
         *
         * @return the exported data
         */
        public byte[] getContent() {

            return m_content;
        }

        /**
         * Gets the last modification date.
         *
         * @return the last modification date
         */
        public long getDateLastModified() {

            return m_dateLastModified;
        }
    }

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

    /** Cache for module hashes, used to detect changes in modules. */
    private Map<CmsModule, String> m_moduleHashCache = new ConcurrentHashMap<CmsModule, String>();

    /** Module log. */
    private CmsModuleLog m_moduleLog = new CmsModuleLog();

    /** Timed cache for newly calculated module hashes, used to avoid very frequent recalculation. */
    private Map<CmsModule, String> m_newModuleHashCache = CacheBuilder.newBuilder().expireAfterWrite(
        3,
        TimeUnit.SECONDS).<CmsModule, String> build().asMap();

    /**
     * Creates a new instance.<p>
     */
    public CmsModuleImportExportRepository() {

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

        String moduleName = null;
        boolean ok = true;
        try {
            CmsModule module = getModuleForFileName(fileName);
            if (module == null) {
                LOG.error("Deletion request for invalid module file name: " + fileName);
                ok = false;
                return false;
            }
            I_CmsReport report = createReport();
            moduleName = module.getName();
            OpenCms.getModuleManager().deleteModule(m_adminCms, module.getName(), false, report);
            ok = !(report.hasWarning() || report.hasError());
            return true;
        } catch (Exception e) {
            ok = false;
            if (e instanceof CmsException) {
                throw (CmsException)e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            return true;
        } finally {
            m_moduleLog.log(moduleName, Action.deleteModule, ok);
        }
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
    public synchronized ModuleExportData getExportedModuleData(String virtualModuleFileName, CmsProject project)
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

            boolean needToRunExport = needToExportModule(module, moduleFile, project);
            if (needToRunExport) {
                CmsObject exportCms = OpenCms.initCmsObject(m_adminCms);
                exportCms.getRequestContext().setCurrentProject(project);
                LOG.info("Module export is needed for " + module.getName());
                moduleFile.delete();
                CmsModuleImportExportHandler handler = new CmsModuleImportExportHandler();
                List<String> moduleResources = CmsModule.calculateModuleResourceNames(exportCms, module);
                handler.setAdditionalResources(moduleResources.toArray(new String[] {}));
                // the import/export handler adds the zip extension if it is not there, so we append it here
                String tempFileName = RandomStringUtils.randomAlphanumeric(8) + ".zip";
                String tempFilePath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
                    CmsStringUtil.joinPaths(EXPORT_FOLDER_PATH, tempFileName));
                handler.setFileName(tempFilePath);
                handler.setModuleName(moduleName);
                CmsException exportException = null;
                I_CmsReport report = createReport();
                try {
                    handler.exportData(exportCms, report);
                } catch (CmsException e) {
                    exportException = e;
                }
                boolean failed = ((exportException != null) || report.hasWarning() || report.hasError());
                m_moduleLog.log(moduleName, Action.exportModule, !failed);

                if (exportException != null) {
                    new File(tempFilePath).delete();
                    throw exportException;
                }
                new File(tempFilePath).renameTo(new File(moduleFilePath));
                LOG.info("Created module export " + moduleFilePath);
            }
            byte[] result = CmsFileUtil.readFully(new FileInputStream(moduleFilePath));
            return new ModuleExportData(result, new File(moduleFilePath).lastModified());
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
     * Gets the object used to access the module log.<p>
     *
     * @return the module log
     */
    public CmsModuleLog getModuleLog() {

        return m_moduleLog;
    }

    /**
     * Imports module data.<p>
     *
     * @param name the module file name
     * @param content the module ZIP file data
     * @throws CmsException if something goes wrong
     */
    public synchronized void importModule(String name, byte[] content) throws CmsException {

        String moduleName = null;
        boolean ok = true;
        try {
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
                    throw new CmsImportExportException(
                        Messages.get().container(Messages.ERR_FILE_IO_1, targetFilePath));
                }
                CmsModule module = CmsModuleImportExportHandler.readModuleFromImport(targetFilePath);
                moduleName = module.getName();
                I_CmsReport report = createReport();
                OpenCms.getModuleManager().replaceModule(m_adminCms, targetFilePath, report);
                new File(targetFilePath).delete();
                if (report.hasError() || report.hasWarning()) {
                    ok = false;
                }
            }
        } catch (CmsException e) {
            ok = false;
            throw e;
        } catch (RuntimeException e) {
            ok = false;
            throw e;
        } finally {
            m_moduleLog.log(moduleName, Action.importModule, ok);
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
     * Computes a module hash, which should change when a module changes and stay the same when the module doesn't change.<p>
     *
     * We only use the modification time of the module resources and their descendants and the modification time of the metadata
     * for computing it.
     *
     * @param module the module for which to compute the module signature
     * @param project the project in which to compute the module hash
     * @return the module signature
     * @throws CmsException if something goes wrong
     */
    private String computeModuleHash(CmsModule module, CmsProject project) throws CmsException {

        LOG.info("Getting module hash for " + module.getName());
        // This method may be called very frequently during a short time, but it is unlikely
        // that a module changes multiple times in a few seconds, so we use a timed cache here
        String cachedValue = m_newModuleHashCache.get(module);
        if (cachedValue != null) {
            LOG.info("Using cached value for module hash of " + module.getName());
            return cachedValue;
        }

        CmsObject cms = OpenCms.initCmsObject(m_adminCms);
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(module.getSite())) {
            cms.getRequestContext().setSiteRoot(module.getSite());
        }
        cms.getRequestContext().setCurrentProject(project);

        // We compute a hash code from the paths of all resources belonging to the module and their respective modification dates.
        List<String> entries = Lists.newArrayList();
        for (String path : module.getResources()) {
            try {
                Set<CmsResource> resources = Sets.newHashSet();
                CmsResource moduleRes = cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
                resources.add(moduleRes);
                if (moduleRes.isFolder()) {
                    resources.addAll(cms.readResources(path, CmsResourceFilter.IGNORE_EXPIRATION, true));
                }
                for (CmsResource res : resources) {
                    entries.add(res.getRootPath() + ":" + res.getDateLastModified());
                }
            } catch (CmsVfsResourceNotFoundException e) {
                entries.add(path + ":null");
            }
        }
        Collections.sort(entries);
        String inputString = CmsStringUtil.listAsString(entries, "\n") + "\nMETA:" + module.getObjectCreateTime();
        LOG.debug("Computing module hash from base string:\n" + inputString);
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(inputString.getBytes("UTF-8"));
            String result = Hex.encodeHexString(md5.digest());
            return result;
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            // This shouldn't happen
            LOG.error(e.getLocalizedMessage(), e);
            return RandomStringUtils.randomAlphanumeric(8);
        }

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
     * Creates a new report for an export/import.<p>
     *
     * @return the new report
     */
    private I_CmsReport createReport() {

        return new CmsLogReport(Locale.ENGLISH, CmsModuleImportExportRepository.class);
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

    /**
     * Checks if a given module needs to be re-exported.<p>
     *
     * @param module the module to check
     * @param moduleFile the file representing the exported module (doesn't necessarily exist)
     * @param project the project in which to check
     *
     * @return true if the module needs to be exported
     */
    private boolean needToExportModule(CmsModule module, File moduleFile, CmsProject project) {

        if (!moduleFile.exists()) {
            LOG.info("Module export file doesn't exist, export is needed.");
            try {
                String moduleSignature = computeModuleHash(module, project);
                if (moduleSignature != null) {
                    m_moduleHashCache.put(module, moduleSignature);
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return true;
        } else {
            if (moduleFile.lastModified() < module.getObjectCreateTime()) {
                return true;
            }

            String oldModuleSignature = m_moduleHashCache.get(module);
            String newModuleSignature = null;
            try {
                newModuleSignature = computeModuleHash(module, project);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }

            LOG.info(
                "Comparing module hashes for "
                    + module.getName()
                    + " to check if export is needed: old = "
                    + oldModuleSignature
                    + ",  new="
                    + newModuleSignature);
            if ((newModuleSignature == null) || !Objects.equal(oldModuleSignature, newModuleSignature)) {
                if (newModuleSignature != null) {
                    m_moduleHashCache.put(module, newModuleSignature);
                }
                // if an error occurs or the module signatures don't match
                return true;
            } else {
                return false;
            }
        }
    }

}
