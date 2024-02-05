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

package org.opencms.workplace.tools.content.convertxml;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishManager;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsXsltUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Converting xml contents according to new schema.
 * <p>
 *
 * @since 7.0.5
 */
public class CmsConvertXmlThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsConvertXmlThread.class);

    /** Number of files transformed already. */
    private int m_alreadyTransformed;

    /** Current CmsObject. */
    private CmsObject m_cmsObject;

    /** Number of errors while transforming. */
    private int m_errorTransform;

    /** Number of locked files during of transformation. */
    private int m_lockedFiles;

    /** Number of files where encoding type could not become get. */
    private int m_missingEncodingType;

    /** Settings. */
    private CmsConvertXmlSettings m_settings;

    /**
     * Creates a replace html tag Thread.<p>
     *
     * @param cms the current cms context.
     *
     * @param settings the settings needed to perform the operation.
     */
    public CmsConvertXmlThread(CmsObject cms, CmsConvertXmlSettings settings) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_CONVERTXML_THREAD_NAME_0));
        initHtmlReport(cms.getRequestContext().getLocale());
        m_cmsObject = cms;
        m_settings = settings;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        I_CmsReport report = getReport();
        report.println(
            Messages.get().container(Messages.RPT_CONVERTXML_BEGIN_TRANSFORM_THREAD_0),
            I_CmsReport.FORMAT_HEADLINE);
        try {
            // convert xml contents
            mainTransform(
                report,
                m_settings.getResourceType(),
                m_settings.getVfsFolder(),
                m_settings.getIncludeSubFolders(),
                m_settings.getXslFile(),
                m_cmsObject,
                m_settings.getOnlyCountFiles());
        } catch (Throwable f) {
            m_errorTransform += 1;
            report.println(Messages.get().container(Messages.RPT_CONVERTXML_TRANSFORMATION_ERROR_0));
            if (LOG.isErrorEnabled()) {
                LOG.error(f.toString());
            }
        }

        // append runtime statistics to report
        getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_STAT_0));
        getReport().println(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_STAT_DURATION_1,
                getReport().formatRuntime()));
        getReport().println(
            Messages.get().container(Messages.RPT_CONVERTXML_THREAD_END_0),
            I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Locks the current resource.<p>
     *
     * @param cms the current CmsObject
     * @param cmsProject the current project
     * @param cmsResource the resourcfe to lock
     * @param report the report
     *
     * @throws CmsException if some goes wrong
     */
    private boolean lockResource(CmsObject cms, CmsProject cmsProject, CmsResource cmsResource, I_CmsReport report)
    throws CmsException {

        cms.getRequestContext().setCurrentProject(cmsProject);
        CmsLock lock = cms.getLock(getCms().getSitePath(cmsResource));
        // check the lock
        if ((lock != null)
            && lock.isOwnedBy(getCms().getRequestContext().getCurrentUser())
            && lock.isOwnedInProjectBy(getCms().getRequestContext().getCurrentUser(), cmsProject)) {
            // prove is current lock from current user in current project
            return true;
        } else if ((lock != null)
            && !lock.isUnlocked()
            && !lock.isOwnedBy(getCms().getRequestContext().getCurrentUser())) {
            // the resource is not locked by the current user, so can not lock it
            m_lockedFiles += 1;
            return false;
        } else if ((lock != null)
            && !lock.isUnlocked()
            && lock.isOwnedBy(getCms().getRequestContext().getCurrentUser())
            && !lock.isOwnedInProjectBy(getCms().getRequestContext().getCurrentUser(), cmsProject)) {
            // prove is current lock from current user but not in current project
            // file is locked by current user but not in current project
            // change the lock
            cms.changeLock(getCms().getSitePath(cmsResource));
        } else if ((lock != null) && lock.isUnlocked()) {
            // lock resource from current user in current project
            cms.lockResource(getCms().getSitePath(cmsResource));
        }
        lock = cms.getLock(getCms().getSitePath(cmsResource));
        if ((lock != null)
            && lock.isOwnedBy(getCms().getRequestContext().getCurrentUser())
            && !lock.isOwnedInProjectBy(getCms().getRequestContext().getCurrentUser(), cmsProject)) {
            // resource could not be locked
            m_lockedFiles += 1;

            return false;
        }
        // resource is locked successfully
        return true;
    }

    /**
     * Main method to transforms xml contents from files of one format because
     * of new xsd file.<p>
     *
     * @param report I_CmsReport
     * @param fileFormat File format of xml contents to transform
     * @param resourcePath Path where to transform xml contents
     * @param inclSubFolder True, if also transform xml contents in sub folders
     * @param xsltFile XLST file which includes logic for transforming
     * @param cmsObject Current CmsObject
     * @param countFilesToTransformOnly Only count files to transform
     *
     * @return True if transformation of all xml contents was successful
     */
    @SuppressWarnings("unchecked")
    private boolean mainTransform(
        I_CmsReport report,
        int fileFormat,
        String resourcePath,
        boolean inclSubFolder,
        String xsltFile,
        CmsObject cmsObject,
        boolean countFilesToTransformOnly) {

        boolean transformSuccess = true;
        boolean transformConditions = true;
        // write parameters to report
        report.println(Messages.get().container(Messages.RPT_CONVERTXML_BEGIN_TRANSFORM_0), I_CmsReport.FORMAT_NOTE);
        report.println(Messages.get().container(Messages.RPT_CONVERTXML_PARAMETERS_0), I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(Messages.RPT_CONVERTXML_PARAMETERS_RESOURCE_PATH_1, resourcePath),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(
                Messages.RPT_CONVERTXML_PARAMETERS_INC_SUBFOLDERS_1,
                Boolean.valueOf(inclSubFolder).toString()),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(Messages.RPT_CONVERTXML_PARAMETERS_XSLT_FILE_1, xsltFile),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(
                Messages.RPT_CONVERTXML_PARAMETERS_ONLY_COUNT_1,
                Boolean.valueOf(countFilesToTransformOnly).toString()),
            I_CmsReport.FORMAT_NOTE);
        // check if xslt file is available
        if (CmsStringUtil.isEmpty(xsltFile)) {
            report.println(Messages.get().container(Messages.RPT_CONVERTXML_NO_XSLT_FILE_0), I_CmsReport.FORMAT_ERROR);
            transformConditions = false;
        }
        // check if new xsd main file is available
        String newXsdMainFile = "";
        String xsltString = "";
        try {
            xsltString = new String(cmsObject.readFile(xsltFile).getContents());
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getMessageContainer(), e);
            }
        }
        // get main xsd file string in xml content in format: xsi:noNamespaceSchemaLocation="opencms://system/modules/org.opencms.frontend.templatetwo.demo/schemas/article.xsd"
        int posMainFileBegin = xsltString.indexOf("xsi:noNamespaceSchemaLocation=\"");
        if (posMainFileBegin > 0) {
            String fileName = xsltString.substring(posMainFileBegin + "xsi:noNamespaceSchemaLocation=\"".length());
            int posMainFileEnd = fileName.indexOf("\"");
            if (posMainFileEnd > 0) {
                newXsdMainFile = fileName.substring(0, posMainFileEnd);
            }
        }
        // check file
        int fileLength = newXsdMainFile.length();
        if ((fileLength < 5) || !newXsdMainFile.substring(fileLength - 4, fileLength).toUpperCase().equals(".XSD")) {
            newXsdMainFile = "";
        }
        if (CmsStringUtil.isEmpty(newXsdMainFile)) {
            report.println(Messages.get().container(Messages.RPT_CONVERTXML_NO_XSD_FILE_0), I_CmsReport.FORMAT_ERROR);
            transformConditions = false;
        } else {
            report.println(
                Messages.get().container(Messages.RPT_CONVERTXML_PARAMETERS_NEW_XSD_MAINFILE_1, newXsdMainFile),
                I_CmsReport.FORMAT_NOTE);
        }
        // check if vfs folder is set
        if (CmsStringUtil.isEmpty(resourcePath)) {
            report.println(Messages.get().container(Messages.RPT_CONVERTXML_NO_VFS_FOLDER_0), I_CmsReport.FORMAT_ERROR);
            transformConditions = false;
        }
        // only start actions if all conditions are okay
        if (!transformConditions) {
            return false;
        }
        // read all files to transform
        report.println(Messages.get().container(Messages.RPT_CONVERTXML_START_SEARCHING_0), I_CmsReport.FORMAT_NOTE);
        List<CmsResource> files2Transform = null;
        try {
            files2Transform = cmsObject.readResources(
                resourcePath,
                CmsResourceFilter.requireType(fileFormat),
                inclSubFolder);
        } catch (CmsException e) {
            m_errorTransform += 1;
            report.println(Messages.get().container(Messages.RPT_CONVERTXML_SEARCH_ERROR_0), I_CmsReport.FORMAT_ERROR);
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getMessageContainer(), e);
            }
            report.println(Messages.get().container(Messages.RPT_CONVERTXML_TRANSFORM_END_0), I_CmsReport.FORMAT_NOTE);
            return false;
        }
        int file2Transform = 0;
        if (files2Transform != null) {
            file2Transform = files2Transform.size();
            report.println(
                Messages.get().container(Messages.RPT_CONVERTXML_FOUND_FILES_1, Integer.valueOf(file2Transform).toString()),
                I_CmsReport.FORMAT_OK);
        } else {
            report.println(Messages.get().container(Messages.RPT_CONVERTXML_NO_FILES_FOUND_0), I_CmsReport.FORMAT_OK);
            return false;
        }
        if (countFilesToTransformOnly || (file2Transform < 1)) {
            report.println(Messages.get().container(Messages.RPT_CONVERTXML_NO_FILES_FOUND_0), I_CmsReport.FORMAT_OK);
            return false;
        }
        // transform and write files
        CmsObject cmsObject2Publish = transformAndWriteFiles(
            files2Transform,
            xsltFile,
            cmsObject,
            newXsdMainFile,
            report);
        // publish files in project
        report.println(Messages.get().container(Messages.RPT_CONVERTXML_PUBLISHING_FILES_0), I_CmsReport.FORMAT_NOTE);
        CmsPublishManager cmsPublishManager = OpenCms.getPublishManager();
        try {
            cmsPublishManager.publishProject(cmsObject2Publish);
        } catch (Exception e) {
            m_errorTransform += 1;
            report.println(
                Messages.get().container(Messages.RPT_CONVERTXML_TRANSFORMATION_ERROR_0),
                I_CmsReport.FORMAT_ERROR);
            if (LOG.isErrorEnabled()) {
                LOG.error(e.toString());
            }
        }
        // output from the results
        report.println(Messages.get().container(Messages.RPT_CONVERTXML_RESULT_0), I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(Messages.RPT_CONVERTXML_FOUND_FILES_1, Integer.valueOf(file2Transform).toString()),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(
                Messages.RPT_CONVERTXML_FILES_ALREADY_TRANSFORMED_1,
                Integer.valueOf(m_alreadyTransformed).toString()),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(
                Messages.RPT_CONVERTXML_TRANSFORM_NUMBER_ERRORS_1,
                Integer.valueOf(m_errorTransform).toString()),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(Messages.RPT_CONVERTXML_LOCKED_FILES_1, Integer.valueOf(m_lockedFiles).toString()),
            I_CmsReport.FORMAT_NOTE);
        if ((m_lockedFiles > 0) || (m_errorTransform > 0)) {
            report.println(
                Messages.get().container(Messages.RPT_CONVERTXML_TRANSFORMING_FAILED_0),
                I_CmsReport.FORMAT_ERROR);
        } else {
            report.println(
                Messages.get().container(Messages.RPT_CONVERTXML_TRANSFORMING_SUCCESS_0),
                I_CmsReport.FORMAT_OK);
        }

        report.println(Messages.get().container(Messages.RPT_CONVERTXML_TRANSFORM_END_0), I_CmsReport.FORMAT_NOTE);
        return transformSuccess;
    }

    /**
     * Gets file xml content.<p>
     *
     * @param cmsResource current resource CmsResource
     * @param cmsFile current CmsFile
     * @param cmsObject current CmsObject
     * @param xmlContent xml content to write
     * @param encodingType encoding type
     * @param report I_CmsReport
     */
    private void setXmlContentFromFile(
        CmsResource cmsResource,
        CmsFile cmsFile,
        CmsObject cmsObject,
        String xmlContent,
        String encodingType,
        I_CmsReport report) {

        try {
            byte[] fileContent = xmlContent.getBytes(encodingType);
            cmsFile.setContents(fileContent);
            // write into file
            cmsObject.writeFile(cmsFile);
            // unlock resource
            try {
                cmsObject.unlockResource(cmsObject.getSitePath(cmsResource));
            } catch (CmsException e) {
                m_errorTransform += 1;
                report.println(
                    Messages.get().container(Messages.RPT_CONVERTXML_UNLOCK_FILE_1, cmsObject.getSitePath(cmsResource)),
                    I_CmsReport.FORMAT_ERROR);
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessageContainer(), e);
                }
            }
        } catch (Exception e) {
            m_errorTransform += 1;
            String reportContent = "<br/>";
            reportContent = reportContent + CmsEncoder.escapeXml(xmlContent);
            reportContent = reportContent.replaceAll("\r\n", "<br/>");
            report.println(
                Messages.get().container(Messages.RPT_CONVERTXML_WRITE_ERROR_1, reportContent),
                I_CmsReport.FORMAT_ERROR);
            if (LOG.isErrorEnabled()) {
                LOG.error(e.toString());
            }
        }
    }

    /**
     * Transforms and write files.<p>
     *
     * @param files2Transform Files to transform
     * @param xsltFile XLST file which includes logic for transforming
     * @param cmsObject Current CmsObject
     * @param newXsdMainFile New xsd main file
     * @param report I_CmsReport
     *
     * @return Project with files to publish
     *
     * @throws CmsException Can become thrown while creating temporary OpenCms Projects
     */
    private CmsObject transformAndWriteFiles(
        List<CmsResource> files2Transform,
        String xsltFile,
        CmsObject cmsObject,
        String newXsdMainFile,
        I_CmsReport report) {

        // the CmsObject to publish resources
        CmsObject cms1 = null;
        // the CmsObject to handle resources which are not to publish
        CmsObject cms2 = null;
        // the publish project
        CmsProject project2Publish = null;
        // initialize the CmsObjects and the publish project
        try {
            cms1 = OpenCms.initCmsObject(cmsObject);
            cms2 = OpenCms.initCmsObject(cmsObject);
            cms1.copyResourceToProject("/");
            project2Publish = cms1.createTempfileProject(); // init new
            cms1.getRequestContext().setCurrentProject(project2Publish);
        } catch (CmsException e) {
            report.println(Messages.get().container(Messages.RPT_CONVERTXML_INITIALIZE_CMS_ERROR_0));
            if (LOG.isErrorEnabled()) {
                LOG.error(e.toString());
            }
            return cms1;
        }

        // iterate over all the resources to transform
        Iterator<CmsResource> iter = files2Transform.iterator();
        while (iter.hasNext()) {
            // get the next resource to transform
            CmsResource cmsResource = iter.next();
            // check if the resource has to be published after transforming
            boolean resource2Publish = false;
            // get info if resource shall become published
            CmsResourceState cmsResourceState = cmsResource.getState();
            if (!(cmsResourceState.equals(CmsResourceState.STATE_CHANGED)
                || cmsResourceState.equals(CmsResourceState.STATE_NEW))) {
                // resource is not touched or is not new
                resource2Publish = true;
            }

            // get current lock from file
            if (resource2Publish) {
                // lock the resource in the publish project
                try {
                    // try to lock the resource
                    if (!lockResource(cms1, project2Publish, cmsResource, report)) {
                        report.println(
                            Messages.get().container(
                                Messages.RPT_CONVERTXML_LOCKED_FILE_1,
                                cmsObject.getSitePath(cmsResource)),
                            I_CmsReport.FORMAT_ERROR);
                        continue;
                    }
                } catch (CmsException e) {
                    report.println(
                        Messages.get().container(
                            Messages.RPT_CONVERTXML_LOCKED_FILE_1,
                            cmsObject.getSitePath(cmsResource)),
                        I_CmsReport.FORMAT_ERROR);
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getMessageContainer(), e);
                    }
                    continue;
                }
            } else {
                // lock the resource in the project where the resource was last modified in
                try {
                    // get the project id from the project where the resource is last modified in
                    CmsUUID pid = cmsResource.getProjectLastModified();
                    CmsProject fileProject = cms2.readProject(pid);
                    cms2.getRequestContext().setCurrentProject(fileProject);
                    // try to lock the resource
                    if (!lockResource(cms2, fileProject, cmsResource, report)) {
                        report.println(
                            Messages.get().container(
                                Messages.RPT_CONVERTXML_LOCKED_FILE_1,
                                cmsObject.getSitePath(cmsResource)),
                            I_CmsReport.FORMAT_ERROR);
                        continue;
                    }
                } catch (CmsException e) {
                    report.println(
                        Messages.get().container(
                            Messages.RPT_CONVERTXML_LOCKED_FILE_1,
                            cmsObject.getSitePath(cmsResource)),
                        I_CmsReport.FORMAT_ERROR);
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getMessageContainer(), e);
                    }
                    continue;
                }
            }

            // get CmsFile object and the xml content
            CmsFile cmsFile = null;
            String fileXmlContent = "";
            try {
                cmsFile = cmsObject.readFile(cmsResource);
                CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCms(), cmsFile);
                fileXmlContent = xmlContent.toString();
            } catch (CmsException e) {
                m_errorTransform += 1;
                report.println(
                    Messages.get().container(Messages.RPT_CONVERTXML_TRANSFORMATION_ERROR_0),
                    I_CmsReport.FORMAT_ERROR);
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessageContainer(), e);
                }
                continue;
            }

            // get encoding per resource
            String encodingType = "";
            try {
                encodingType = cmsObject.readPropertyObject(
                    cmsResource.getRootPath(),
                    CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                    true).getValue(OpenCms.getSystemInfo().getDefaultEncoding());
            } catch (CmsException e) {
                encodingType = OpenCms.getSystemInfo().getDefaultEncoding();
            }

            // check transform conditions per resource
            // encoding type given?
            if (CmsStringUtil.isEmpty(encodingType)) {
                m_missingEncodingType += 1;
                report.println(
                    Messages.get().container(
                        Messages.RPT_CONVERTXML_MISSION_ENCODING_TYPE_1,
                        cmsResource.getRootPath()),
                    I_CmsReport.FORMAT_ERROR);
                continue;
            }
            // already transformed?
            if (fileXmlContent.toUpperCase().contains(newXsdMainFile.toUpperCase())) {
                m_alreadyTransformed += 1;
                report.println(
                    Messages.get().container(
                        Messages.RPT_CONVERTXML_FILE_ALREADY_TRANSFORMED_1,
                        cmsResource.getRootPath()),
                    I_CmsReport.FORMAT_OK);
                continue;
            }

            // create and write the changed xml content
            try {
                String transformedXmlContent = CmsXsltUtil.transformXmlContent(cmsObject, xsltFile, fileXmlContent);
                transformedXmlContent = "<?xml version=\"1.0\" encoding=\"".concat(encodingType).concat("\"?>").concat(
                    transformedXmlContent);
                // write file xml content
                if (resource2Publish) {
                    report.println(
                        Messages.get().container(
                            Messages.RPT_CONVERTXML_TRANSFORM_CURRENT_FILE_NAME2_2,
                            cmsResource.getRootPath(),
                            encodingType),
                        I_CmsReport.FORMAT_OK);
                    cms1.getRequestContext().setCurrentProject(project2Publish);
                    setXmlContentFromFile(cmsResource, cmsFile, cms1, transformedXmlContent, encodingType, report);
                } else {
                    report.println(Messages.get().container(
                        Messages.RPT_CONVERTXML_TRANSFORM_CURRENT_FILE_NAME_2,
                        cmsResource.getRootPath(),
                        encodingType), I_CmsReport.FORMAT_OK);
                    setXmlContentFromFile(cmsResource, cmsFile, cms2, transformedXmlContent, encodingType, report);
                }
            } catch (CmsXmlException e) {
                m_errorTransform += 1;
                report.println(
                    Messages.get().container(Messages.RPT_CONVERTXML_TRANSFORMATION_ERROR_0),
                    I_CmsReport.FORMAT_ERROR);
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessageContainer(), e);
                }
            } catch (CmsException e) {
                m_errorTransform += 1;
                report.println(
                    Messages.get().container(Messages.RPT_CONVERTXML_TRANSFORMATION_ERROR_0),
                    I_CmsReport.FORMAT_ERROR);
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessageContainer(), e);
                }
            }

        }
        return cms1;
    }
}
