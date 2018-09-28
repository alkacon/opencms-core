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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.importexport.CmsImportResourceDataReader;
import org.opencms.importexport.CmsImportVersion10;
import org.opencms.importexport.CmsImportVersion10.RelationData;
import org.opencms.importexport.Messages;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsShell;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

/**
 * Class used for updating modules.<p>
 *
 * This class updates modules in a smarter way than simply deleting and importing them again: The resources in the import
 * ZIP file are compared to the resources in the currently installed module and only makes changes when necessary. The reason
 * for this is that deletions of resources can be slow in some very large OpenCms installations, and the classic way of updating modules
 * (delete/import) can take a long time because of this.
 */
public class CmsModuleUpdater {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleUpdater.class);

    /** Structure ids of imported resources.*/
    private Set<CmsUUID> m_importIds = new HashSet<CmsUUID>();

    /** The module data read from the ZIP. */
    private CmsModuleImportData m_moduleData;

    /** The report to write to. */
    private I_CmsReport m_report;

    /**
     * Creates a new instance.<p>
     *
     * @param moduleData the module import data
     * @param report the report to write to
     */
    public CmsModuleUpdater(CmsModuleImportData moduleData, I_CmsReport report) {

        m_moduleData = moduleData;
        m_report = report;
    }

    /**
     * Checks if the installed module is updatable with the version from the import zip file.<p>
     *
     * @param cms the current CMS context
     * @param moduleData the data read from the ZIP file
     *
     * @return true if the module is updatable
     */
    public static boolean checkUpdatable(CmsObject cms, CmsModuleImportData moduleData) {

        CmsModule newModule = moduleData.getModule();
        LOG.info("Checking if module " + newModule.getName() + " is updateable");
        String exportVersion = newModule.getExportVersion();
        CmsModule installedModule = OpenCms.getModuleManager().getModule(moduleData.getModule().getName());
        if (!checkCompatibleModuleResources(installedModule, newModule)) {
            LOG.info("Module is not updateable because of incompatible module resources. ");
            return false;
        }

        if ((exportVersion == null) || !exportVersion.equals("" + CmsImportVersion10.IMPORT_VERSION10)) {
            LOG.info("Module is not updateable because the export version is not 10.");
            return false;
        }

        try {
            cms = moduleData.getCms();
            CmsObject onlineCms = OpenCms.initCmsObject(cms);
            CmsProject online = cms.readProject(CmsProject.ONLINE_PROJECT_NAME);
            onlineCms.getRequestContext().setCurrentProject(online);
            for (CmsResourceImportData resData : moduleData.getResourceData()) {
                if (resData.getResource().isFile() && (!resData.hasContent())) {
                    LOG.info(
                        "Module is not updateable because the update of files without content in imports (siblings) is not supported.");
                    return false;
                }
                String importPath = normalizePath(resData.getResource().getRootPath());
                if (resData.hasStructureId()) {
                    CmsUUID importId = resData.getResource().getStructureId();
                    for (CmsObject cmsToRead : Arrays.asList(cms, onlineCms)) {
                        try {
                            CmsResource resourceFromVfs = cmsToRead.readResource(
                                importPath,
                                CmsResourceFilter.IGNORE_EXPIRATION);
                            if (!resourceFromVfs.getStructureId().equals(importId)) {
                                LOG.info(
                                    "Module is not updateable because the same path is maps to different ids in the import and in the VFS for "
                                        + importPath);
                                return false;
                            }
                        } catch (CmsVfsResourceNotFoundException e) {
                            // ignore
                        }
                    }

                    try {
                        CmsResource vfsResource = cms.readResource(importId, CmsResourceFilter.ALL);
                        if (vfsResource.isFolder() != resData.getResource().isFolder()) {
                            LOG.info(
                                "Module is not updateable because the same id belongs to a file in the import and a folder in the VFS, or vice versa");
                            return false;
                        }
                    } catch (CmsVfsResourceNotFoundException e) {
                        // ignore
                    }
                }
            }
            return true;
        } catch (CmsException e) {
            LOG.info("Module is not updateable because of error: " + e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Tries to create a new updater instance.<p>
     *
     * If the module is deemed non-updatable, an empty result is returned.<p>
     *
     * @param cms the current CMS context
     * @param importFile the import file path
     * @param report the report to write to
     * @return an optional module updater
     *
     * @throws CmsException if something goes wrong
     */
    public static Optional<CmsModuleUpdater> create(CmsObject cms, String importFile, I_CmsReport report)
    throws CmsException {

        CmsModuleImportData moduleData = readModuleData(cms, importFile, report);
        if (checkUpdatable(cms, moduleData)) {
            return Optional.of(new CmsModuleUpdater(moduleData, report));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Check if a resource needs to be updated because of its direct fields.<p>
     *
     * @param existingRes the existing resource
     * @param newRes the new resource
     * @param reduced true if we are in reduced export mode
     *
     * @return true if we need to update the resource based on its direct fields
     */
    public static boolean needToUpdateResourceFields(CmsResource existingRes, CmsResource newRes, boolean reduced) {

        boolean result = false;
        result |= existingRes.getTypeId() != newRes.getTypeId();
        result |= differentDates(existingRes.getDateCreated(), newRes.getDateCreated()); // Export format date is not precise to millisecond
        result |= differentDates(existingRes.getDateReleased(), newRes.getDateReleased());
        result |= differentDates(existingRes.getDateExpired(), newRes.getDateExpired());
        result |= existingRes.getFlags() != newRes.getFlags();
        if (!reduced) {
            result |= !Objects.equal(existingRes.getUserCreated(), newRes.getUserCreated());
            result |= !Objects.equal(existingRes.getUserLastModified(), newRes.getUserLastModified());
            result |= existingRes.getDateLastModified() != newRes.getDateLastModified();
        }
        return result;
    }

    /**
     * Reads the module data from an import zip file.<p>
     *
     * @param cms the CMS context
     * @param importFile the import file
     * @param report the report to write to
     * @return the module data
     * @throws CmsException if something goes wrong
     */
    public static CmsModuleImportData readModuleData(CmsObject cms, String importFile, I_CmsReport report)
    throws CmsException {

        CmsModuleImportData result = new CmsModuleImportData();
        CmsModule module = CmsModuleImportExportHandler.readModuleFromImport(importFile);
        cms = OpenCms.initCmsObject(cms);

        String importSite = module.getImportSite();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(importSite)) {
            cms.getRequestContext().setSiteRoot(importSite);
        } else {
            String siteToSet = cms.getRequestContext().getSiteRoot();
            if ("".equals(siteToSet)) {
                siteToSet = "/";
            }
            module.setSite(siteToSet);
        }
        result.setModule(module);
        result.setCms(cms);
        CmsImportResourceDataReader importer = new CmsImportResourceDataReader(result);
        CmsImportParameters params = new CmsImportParameters(importFile, "/", false);
        importer.importData(cms, report, params); // This only reads the module data into Java objects
        result.getResourceData();
        return result;

    }

    /**
     * Checks that two longs representing dates differ by more than 1000 (milliseconds).<p>
     *
     * @param d1 the first date
     * @param d2 the second date
     *
     * @return true if the dates differ by more than 1000 milliseconds
     */
    static boolean differentDates(long d1, long d2) {

        return 1000 < Math.abs(d2 - d1);
    }

    /**
     * Checks whether the module resources and sites of the two module versions are suitable for updating.<p>
     *
     * @param installedModule the installed module
     * @param newModule the module to import
     *
     * @return true if the module resources are compatible
     */
    private static boolean checkCompatibleModuleResources(CmsModule installedModule, CmsModule newModule) {

        if (installedModule.hasOnlySystemAndSharedResources() && newModule.hasOnlySystemAndSharedResources()) {
            return true;
        }
        String oldSite = installedModule.getSite();
        String newSite = newModule.getSite();
        return (oldSite != null) && (newSite != null) && CmsStringUtil.comparePaths(oldSite, newSite);
    }

    /**
     * Gets all resources in the module.<p>
     *
     * @param cms the current CMS context
     * @param module the module
     * @return the resources in the module
     * @throws CmsException if something goes wrong
     */
    private static Set<CmsResource> getAllResourcesInModule(CmsObject cms, CmsModule module) throws CmsException {

        Set<CmsResource> result = new HashSet<>();
        for (CmsResource resource : CmsModule.calculateModuleResources(cms, module)) {
            result.add(resource);
            if (resource.isFolder()) {
                result.addAll(cms.readResources(resource, CmsResourceFilter.ALL, true));
            }
        }
        return result;
    }

    /**
     * Normalizes the path.<p>
     *
     * @param pathComponents the path components
     *
     * @return the normalized path
     */
    private static String normalizePath(String... pathComponents) {

        return CmsFileUtil.removeTrailingSeparator(CmsStringUtil.joinPaths(pathComponents));
    }

    /**
     * Update relations for all imported resources.<p>
     *
     * @param cms the current CMS context
     * @throws CmsException if something goes wrong
     */
    public void importRelations(CmsObject cms) throws CmsException {

        for (CmsResourceImportData resData : m_moduleData.getResourceData()) {
            if (!resData.getRelations().isEmpty()) {
                CmsResource importResource = resData.getImportResource();
                if (importResource != null) {
                    importResource = cms.readResource(importResource.getStructureId(), CmsResourceFilter.ALL);
                    updateRelations(cms, importResource, resData.getRelations());
                }
            }
        }

    }

    /**
     * Performs the module update.<p>
     */
    public void run() {

        try {
            List<CmsResource> toUnlock = new ArrayList<>();
            CmsObject cms = m_moduleData.getCms();
            CmsModule module = m_moduleData.getModule();
            CmsModule oldModule = OpenCms.getModuleManager().getModule(module.getName());
            CmsProject importProject = cms.createProject(
                org.opencms.module.Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                    org.opencms.module.Messages.GUI_IMPORT_MODULE_PROJECT_NAME_1,
                    new Object[] {module.getName()}),
                org.opencms.module.Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                    org.opencms.module.Messages.GUI_IMPORT_MODULE_PROJECT_DESC_1,
                    new Object[] {module.getName()}),
                OpenCms.getDefaultUsers().getGroupAdministrators(),
                OpenCms.getDefaultUsers().getGroupAdministrators(),
                CmsProject.PROJECT_TYPE_TEMPORARY);
            cms.getRequestContext().setCurrentProject(importProject);
            cms.copyResourceToProject("/");
            CmsModuleImportExportHandler.reportBeginImport(m_report, module.getName());

            Map<CmsUUID, CmsResourceImportData> importResourcesById = new HashMap<>();
            for (CmsResourceImportData resData : m_moduleData.getResourceData()) {
                importResourcesById.put(resData.getResource().getStructureId(), resData);
            }
            Set<CmsResource> oldModuleResources = getAllResourcesInModule(cms, oldModule);
            List<CmsResource> toDelete = new ArrayList<>();
            for (CmsResource oldRes : oldModuleResources) {
                CmsResourceImportData newRes = importResourcesById.get(oldRes.getStructureId());
                if (newRes == null) {
                    toDelete.add(oldRes);
                }
            }
            int index = 0;
            for (CmsResourceImportData resData : m_moduleData.getResourceData()) {
                index += 1;
                boolean changed = false;
                m_report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        "( " + index + " / " + m_moduleData.getResourceData().size() + " ) "),
                    I_CmsReport.FORMAT_NOTE);
                m_report.print(Messages.get().container(Messages.RPT_IMPORTING_0), I_CmsReport.FORMAT_NOTE);
                m_report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        resData.getPath()));
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                try {
                    CmsResource oldRes = null;
                    try {
                        if (resData.hasStructureId()) {
                            oldRes = cms.readResource(
                                resData.getResource().getStructureId(),
                                CmsResourceFilter.IGNORE_EXPIRATION);
                        } else {
                            oldRes = cms.readResource(resData.getPath(), CmsResourceFilter.IGNORE_EXPIRATION);
                        }
                    } catch (CmsVfsResourceNotFoundException e) {
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                    CmsResource currentRes = oldRes;
                    if (oldRes != null) {
                        String oldPath = cms.getSitePath(oldRes);
                        String newPath = resData.getPath();
                        if (!CmsStringUtil.comparePaths(oldPath, resData.getPath())) {
                            cms.moveResource(oldPath, newPath);
                            changed = true;
                            currentRes = cms.readResource(oldRes.getStructureId(), CmsResourceFilter.IGNORE_EXPIRATION);
                        }
                    }
                    boolean needsImport = true;
                    boolean reducedExport = !resData.hasDateLastModified();
                    byte[] content = resData.getContent();
                    if (oldRes != null) {
                        if (!resData.hasStructureId()) {
                            needsImport = false;
                        } else if (!needToUpdateResourceFields(oldRes, resData.getResource(), reducedExport)) {
                            if (oldRes.isFile()) {
                                CmsFile file = cms.readFile(oldRes);
                                if (Arrays.equals(file.getContents(), content)) {
                                    needsImport = false;
                                }
                            } else {
                                needsImport = false;
                            }
                        }
                    }
                    if (needsImport || (oldRes == null)) { // oldRes null check is redundant, we just do it to remove the warning in Eclipse
                        currentRes = cms.importResource(
                            resData.getPath(),
                            resData.getResource(),
                            content,
                            new ArrayList<CmsProperty>());
                        changed = true;
                        m_importIds.add(currentRes.getStructureId());
                    } else {
                        currentRes = cms.readResource(oldRes.getStructureId(), CmsResourceFilter.ALL);
                        CmsLock lock = cms.getLock(currentRes);
                        if (lock.isUnlocked()) {
                            cms.lockResourceTemporary(currentRes);
                            toUnlock.add(currentRes);
                        }

                    }
                    resData.setImportResource(currentRes);
                    List<CmsProperty> propsToWrite = compareProperties(cms, resData, currentRes);
                    if (!propsToWrite.isEmpty()) {
                        cms.writePropertyObjects(currentRes, propsToWrite);
                        changed = true;
                    }
                    changed |= updateAcls(cms, resData, currentRes);
                    if (changed) {
                        m_report.println(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                            I_CmsReport.FORMAT_OK);
                    } else {
                        m_report.println(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SKIPPED_0),
                            I_CmsReport.FORMAT_NOTE);
                    }

                } catch (Exception e) {
                    m_report.println(e);
                    LOG.error(e.getLocalizedMessage(), e);

                }
            }
            for (CmsResource deleteRes : toDelete) {
                m_report.print(
                    org.opencms.importexport.Messages.get().container(
                        org.opencms.importexport.Messages.RPT_DELFOLDER_0),
                    I_CmsReport.FORMAT_NOTE);
                m_report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        deleteRes.getRootPath()));
                cms.deleteResource(deleteRes, CmsResource.DELETE_PRESERVE_SIBLINGS);
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

            }
            List<CmsResource> linkParseables = new ArrayList<>();
            for (CmsResourceImportData resData : m_moduleData.getResourceData()) {
                CmsResource importRes = resData.getImportResource();
                if ((importRes != null)
                    && m_importIds.contains(importRes.getStructureId())
                    && isLinkParsable(importRes)) {
                    linkParseables.add(importRes);
                }
            }
            m_report.println(Messages.get().container(Messages.RPT_START_PARSE_LINKS_0), I_CmsReport.FORMAT_HEADLINE);
            CmsImportVersion10.parseLinks(cms, linkParseables, m_report);
            m_report.println(Messages.get().container(Messages.RPT_END_PARSE_LINKS_0), I_CmsReport.FORMAT_HEADLINE);

            importRelations(cms);
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(module.getImportScript())) {
                LOG.info("Executing import script for module " + module.getName());
                m_report.println(
                    org.opencms.module.Messages.get().container(org.opencms.module.Messages.RPT_IMPORT_SCRIPT_HEADER_0),
                    I_CmsReport.FORMAT_HEADLINE);
                String importScript = "echo on\n" + module.getImportScript();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                PrintStream out = new PrintStream(buffer);
                CmsShell shell = new CmsShell(cms, "${user}@${project}:${siteroot}|${uri}>", null, out, out);
                shell.execute(importScript);
                String outputString = buffer.toString();
                LOG.info("Shell output for import script was: \n" + outputString);
                m_report.println(
                    org.opencms.module.Messages.get().container(
                        org.opencms.module.Messages.RPT_IMPORT_SCRIPT_OUTPUT_1,
                        outputString));
            }
            cms.unlockProject(importProject.getUuid());
            OpenCms.getModuleManager().updateModule(cms, module);
            module.setCheckpointTime(System.currentTimeMillis());
            // reinitialize the resource manager with additional module resource types if necessary
            if (module.getResourceTypes() != Collections.EMPTY_LIST) {
                OpenCms.getResourceManager().initialize(cms);
            }
            // reinitialize the workplace manager with additional module explorer types if necessary
            if (module.getExplorerTypes() != Collections.EMPTY_LIST) {
                OpenCms.getWorkplaceManager().addExplorerTypeSettings(module);
            }
            OpenCms.getPublishManager().publishProject(cms, m_report);
            OpenCms.getPublishManager().waitWhileRunning();
            CmsModuleImportExportHandler.reportEndImport(m_report);
        } catch (Exception e) {
            m_report.println(e);
        } finally {
            cleanUp();
        }
    }

    /**
     * Updates the access control list fr a resource.<p>
     *
     * @param cms the current cms context
     * @param resData the resource data
     * @param resource the existing resource
     * @return the resource
     *
     * @throws CmsException if something goes wrong
     */
    public boolean updateAcls(CmsObject cms, CmsResourceImportData resData, CmsResource resource) throws CmsException {

        boolean changed = false;
        Map<CmsUUID, CmsAccessControlEntry> importAces = buildAceMap(resData.getAccessControlEntries());
        String path = cms.getSitePath(resource);
        List<CmsAccessControlEntry> existingAcl = cms.getAccessControlEntries(path, false);
        Map<CmsUUID, CmsAccessControlEntry> existingAces = buildAceMap(existingAcl);
        Set<CmsUUID> keys = new HashSet<>(existingAces.keySet());
        keys.addAll(importAces.keySet());
        for (CmsUUID key : keys) {
            CmsAccessControlEntry existingEntry = existingAces.get(key);
            CmsAccessControlEntry newEntry = importAces.get(key);

            if (existingEntry == null) {
                I_CmsPrincipal principal = CmsPrincipal.readPrincipal(cms, newEntry.getPrincipal());

                cms.chacc(
                    path,
                    CmsPrincipal.getType(principal),
                    "" + newEntry.getPrincipal(),
                    newEntry.getAllowedPermissions(),
                    newEntry.getDeniedPermissions(),
                    newEntry.getFlags());
                changed = true;

            } else if (newEntry == null) {

                cms.rmacc(path, null, "" + existingEntry.getPrincipal());
                changed = true;

            } else {
                existingEntry = existingEntry.withNulledResource();
                newEntry = newEntry.withNulledResource();
                // we null the resource so that equals() returning false means they differ in their other fields
                if (!existingEntry.equals(newEntry)) {
                    I_CmsPrincipal principal = CmsPrincipal.readPrincipal(cms, newEntry.getPrincipal());

                    cms.rmacc(path, null, "" + existingEntry.getPrincipal());
                    cms.chacc(
                        path,
                        CmsPrincipal.getType(principal),
                        "" + newEntry.getPrincipal(),
                        newEntry.getAllowedPermissions(),
                        newEntry.getDeniedPermissions(),
                        newEntry.getFlags());
                    changed = true;
                }

            }

        }
        return changed;
    }

    /**
     * Converts access control list to map form, with principal ids as keys.<p>
     *
     * @param acl an access control list
     * @return the map with the access control entries
     */
    Map<CmsUUID, CmsAccessControlEntry> buildAceMap(Collection<CmsAccessControlEntry> acl) {

        if (acl == null) {
            acl = new ArrayList<>();
        }
        Map<CmsUUID, CmsAccessControlEntry> result = new HashMap<>();
        for (CmsAccessControlEntry ace : acl) {
            result.put(ace.getPrincipal(), ace);
        }
        return result;
    }

    /**
     * Cleans up temp files.
     */
    private void cleanUp() {

        for (CmsResourceImportData resData : m_moduleData.getResourceData()) {
            resData.cleanUp();
        }
    }

    /**
     * Compares properties of an existing resource with those to be imported, and returns a list of properties that need to be updated.<p>
     *
     * @param cms the current CMS context
     * @param resData  the resource import data
     * @param existingResource the existing resource
     * @return the list of properties that need to be updated
     *
     * @throws CmsException if something goes wrong
     */
    private List<CmsProperty> compareProperties(
        CmsObject cms,
        CmsResourceImportData resData,
        CmsResource existingResource)
    throws CmsException {

        if (existingResource == null) {
            return Collections.emptyList();
        }

        Map<String, CmsProperty> importProps = resData.getProperties();
        Map<String, CmsProperty> existingProps = CmsProperty.getPropertyMap(
            cms.readPropertyObjects(existingResource, false));
        Map<String, CmsProperty> propsToWrite = new HashMap<>();
        Set<String> keys = new HashSet<>();
        keys.addAll(existingProps.keySet());
        keys.addAll(importProps.keySet());

        for (String key : keys) {
            CmsProperty existingProp = existingProps.get(key);
            CmsProperty importProp = importProps.get(key);
            if (existingProp == null) {
                propsToWrite.put(key, importProp);
            } else if (importProp == null) {
                propsToWrite.put(key, new CmsProperty(key, "", ""));
            } else if (!existingProp.isIdentical(importProp)) {
                propsToWrite.put(key, importProp);
            }
        }
        return new ArrayList<>(propsToWrite.values());

    }

    /**
     * Checks if a resource is link parseable.<P>
     *
     * @param importRes the resource to check
     * @return true if the resource is link parseable
     *
     * @throws CmsException if something goes wrong
     */
    private boolean isLinkParsable(CmsResource importRes) throws CmsException {

        int typeId = importRes.getTypeId();
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(typeId);
        return type instanceof I_CmsLinkParseable;

    }

    /**
     * Compares the relation (not defined in content) for a resource with those to be imported, and makes
     * the necessary modifications.
     *
     * @param cms the CMS context
     * @param importResource the resource
     * @param relations the relations to be imported
     *
     * @throws CmsException if something goes wrong
     */
    private void updateRelations(CmsObject cms, CmsResource importResource, List<RelationData> relations)
    throws CmsException {

        Map<String, CmsRelationType> relTypes = new HashMap<>();
        for (CmsRelationType relType : OpenCms.getResourceManager().getRelationTypes()) {
            relTypes.put(relType.getName(), relType);
        }
        Set<CmsRelation> existingRelations = Sets.newHashSet(
            cms.readRelations(CmsRelationFilter.relationsFromStructureId(importResource.getStructureId())));
        List<CmsRelation> noContentRelations = existingRelations.stream().filter(
            rel -> !rel.getType().isDefinedInContent()).collect(Collectors.toList());
        Set<CmsRelation> newRelations = new HashSet<>();
        for (RelationData rel : relations) {
            if (!rel.getType().isDefinedInContent()) {
                newRelations.add(
                    new CmsRelation(
                        importResource.getStructureId(),
                        importResource.getRootPath(),
                        rel.getTargetId(),
                        rel.getTarget(),
                        rel.getType()));
            }
        }
        if (!newRelations.equals(noContentRelations)) {

            CmsRelationFilter relFilter = CmsRelationFilter.TARGETS.filterNotDefinedInContent();

            cms.deleteRelationsFromResource(importResource, relFilter);
            for (CmsRelation newRel : newRelations) {
                cms.addRelationToResource(
                    importResource,
                    cms.readResource(newRel.getTargetId(), CmsResourceFilter.IGNORE_EXPIRATION),
                    newRel.getType().getName());

            }
        }
    }

}
