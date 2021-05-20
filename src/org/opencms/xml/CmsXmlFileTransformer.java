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

package org.opencms.xml;

import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsFileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;

import org.w3c.dom.Document;

/**
 * Transforms all resources of a given type by
 */
public class CmsXmlFileTransformer {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlFileTransformer.class);

    /** The CmsObject for working on offline resources. */
    private CmsObject m_offlineCms;

    /** The CmsObject for working on online resources. */
    private CmsObject m_onlineCms;

    /** The path. */
    private String m_path;

    /** The type name. */
    private String m_type;

    /** The bytes of the XSL transformation. */
    private byte[] m_xslt;

    /** The transformer factory. */
    private TransformerFactory m_transformerFactory;

    /** The report to write to. */
    private I_CmsReport m_report;

    /** The origin of the XSL transform. */
    private String m_xslName;

    /**
     * Creates a new instance.
     *
     * @param cms the CMS context
     * @param path the ancestor folder under which files should be processed
     * @param type the resource type which should be processed
     * @param xslName a string containing information about where the XSL transform is coming from
     * @param xslStream the stream to read the XSL transformation from
     * @param report the report to write to
     *
     * @throws CmsException if something goes wrong
     * @throws IOException if an IO error occurs
     */
    public CmsXmlFileTransformer(
        CmsObject cms,
        String path,
        String type,
        String xslName,
        InputStream xslStream,
        I_CmsReport report)
    throws CmsException, IOException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.ROOT_ADMIN);
        m_xslName = xslName;
        m_transformerFactory = TransformerFactory.newInstance();
        m_offlineCms = OpenCms.initCmsObject(cms);
        m_offlineCms.getRequestContext().setSiteRoot("");
        m_onlineCms = OpenCms.initCmsObject(cms);
        m_onlineCms.getRequestContext().setSiteRoot("");
        m_offlineCms.getRequestContext().setCurrentProject(getTempfileProject(cms));
        m_onlineCms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_NAME));
        m_path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(path);
        m_type = type;
        m_xslt = CmsFileUtil.readFully(xslStream);
        m_report = report;
    }

    /**
     * Performs the transformation on all resources of the configured type.
     *
     * @throws CmsException if something goes wrong
     */
    public void run() throws CmsException {

        m_report.println(message("XSL transform: " + m_xslName));
        m_report.println(message("Path: " + m_path));
        m_report.println(message("Type: " + m_type));
        try {
            List<CmsResource> resources = m_offlineCms.readResources(
                m_path,
                CmsResourceFilter.ALL.addRequireType(OpenCms.getResourceManager().getResourceType(m_type)),
                true);
            processResources(resources);
            OpenCms.getEventManager().fireEvent(I_CmsEventListener.EVENT_CLEAR_CACHES);
        } catch (CmsException e) {
            m_report.println(e);
            throw e;
        }
    }

    /**
     * Gets the online path for the resource.
     *
     * @param res the resource
     * @return the online path
     * @throws CmsException if something goes wrong
     */
    private String getOnlinePath(CmsResource res) throws CmsException {

        return m_onlineCms.readResource(res.getStructureId(), CmsResourceFilter.ALL).getRootPath();
    }

    /**
     * Gets the temporary project.
     *
     * @param cms the current CMS context
     * @return the temporary project
     * @throws CmsException if something goes wrong
     */
    private CmsProject getTempfileProject(CmsObject cms) throws CmsException {

        try {
            return cms.readProject(I_CmsProjectDriver.TEMP_FILE_PROJECT_NAME);
        } catch (CmsException e) {
            return cms.createTempfileProject();
        }
    }

    /**
     * Helper for creating a message container from a literal string message string.
     *
     * @param content the message string
     * @return the message container
     */
    private CmsMessageContainer message(String content) {

        content = CmsXmlFileTransformer.class.getSimpleName() + ": " + content;
        return org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_GENERIC_1, content);
    }

    /**
     * Checks if we need to update the content.
     *
     * @param oldContent the old content
     * @param content the new content
     *
     * @return true if we need to update the content
     */
    private boolean needToUpdate(byte[] oldContent, byte[] content) {

        if (content == null) {
            return false;
        }
        if (sameXml(oldContent, content)) {
            return false;
        }
        return true;

    }

    /**
     * Processes the list of resources.
     *
     * @param resources the resources to process
     */
    private void processResources(List<CmsResource> resources) {

        List<String> changedPaths = new ArrayList<>();
        for (CmsResource resource : resources) {
            boolean changed = false;
            CmsResourceState state = resource.getState();
            m_report.println(message("Processing " + resource.getRootPath()));
            try (AutoCloseable c = CmsLockUtil.withLockedResources(m_offlineCms, resource)) {
                if (state.isNew()) {
                    byte[] content = readOfflineContent(resource);
                    byte[] newContent = transformContent(content);
                    if (needToUpdate(content, newContent)) {
                        changed = true;
                        writeContent(resource, newContent);
                    }
                } else if (state.isUnchanged()) {
                    if (!resource.getRootPath().equals(getOnlinePath(resource))) {
                        m_report.println(
                            message("Warning: Skipping " + resource.getRootPath() + " because of path inconsistency."));
                        continue;
                    }
                    byte[] content = readOfflineContent(resource);
                    byte[] newContent = transformContent(content);
                    if (needToUpdate(content, newContent)) {
                        changed = true;
                        writeContent(resource, newContent);
                        publishFile(resource);
                    }
                } else if (state.isDeleted()) {
                    m_report.println(message("Skipping " + resource.getRootPath() + " because it is deleted."));
                } else if (state.isChanged()) {
                    if (!resource.getRootPath().equals(getOnlinePath(resource))) {
                        byte[] content = readOfflineContent(resource);
                        byte[] newContent = transformContent(content);
                        if (needToUpdate(content, newContent)) {
                            changed = true;
                            writeContent(resource, newContent);
                        }
                        m_report.println(
                            message("Warning: Not publishing " + resource.getRootPath() + " because it is moved."));
                    } else {
                        byte[] offlineContent = readOfflineContent(resource);
                        byte[] onlineContent = readOnlineContent(resource);
                        byte[] newOfflineContent = transformContent(offlineContent);
                        byte[] newOnlineContent = transformContent(onlineContent);
                        if (needToUpdate(offlineContent, newOfflineContent)
                            || needToUpdate(onlineContent, newOnlineContent)) {
                            changed = true;
                            if (newOfflineContent == null) {
                                // the case where the onlne transformation works and actually changes something,
                                // but transforming the offline content fails for some reason
                                newOfflineContent = offlineContent;
                            }
                            try {
                                writeContent(resource, newOnlineContent);
                                publishFile(resource);
                            } finally {
                                // Put this in a finally block so we write back the offline content even if the preceding step fails
                                if (m_offlineCms.getLock(resource).isUnlocked()) {
                                    m_offlineCms.lockResourceTemporary(resource);
                                }
                                writeContent(resource, newOfflineContent);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                m_report.println(e);
            } finally {
                if (changed) {
                    changedPaths.add(resource.getRootPath());
                }
            }
        }
        m_report.println();
        m_report.println(message("Summary of changed resources: "));
        m_report.println();
        for (String path : changedPaths) {
            m_report.println(message(path));
        }
    }

    /**
     * Publishes a single file.
     *
     * @param resource the resource to publish
     * @throws CmsException if something goes wrong
     */
    private void publishFile(CmsResource resource) throws CmsException {

        CmsPublishList pubList = OpenCms.getPublishManager().getPublishList(
            m_offlineCms,
            m_offlineCms.readResource(resource.getStructureId(), CmsResourceFilter.ALL),
            false);
        OpenCms.getPublishManager().publishProject(m_offlineCms, m_report, pubList);
        OpenCms.getPublishManager().waitWhileRunning();

    }

    /**
     * Reads the offline contents of a resource.
     *
     * @param res the resource
     * @return the offline contents
     *
     * @throws CmsException if something goes wrong
     */
    private byte[] readOfflineContent(CmsResource res) throws CmsException {

        return m_offlineCms.readFile(res).getContents();
    }

    /**
     * Reads the online contents of a resource.
     *
     * @param res the resource
     * @return the online contents
     *
     * @throws CmsException if something goes wrong
     */
    private byte[] readOnlineContent(CmsResource res) throws CmsException {

        return m_onlineCms.readFile(
            m_onlineCms.readResource(res.getStructureId(), CmsResourceFilter.ALL)).getContents();
    }

    /**
     * Lenient XML comparison that ignores distinctions like CDATA vs normal text nodes and superfluous whitespace.
     *
     * @param xml1 the bytes of the first XML document
     * @param xml2 the bytes of the second XML document
     * @return true if the XML is equivalent
     *
     */
    private boolean sameXml(byte[] xml1, byte[] xml2) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new CmsXmlEntityResolver(m_offlineCms));
            Document doc1 = db.parse(new ByteArrayInputStream(xml1));
            doc1.normalizeDocument();
            Document doc2 = db.parse(new ByteArrayInputStream(xml2));
            doc2.normalizeDocument();
            return doc1.isEqualNode(doc2);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            m_report.println(e);
            return false;
        }

    }

    /**
     * Transforms the content using hte XSL transformation.
     *
     * @param content the content bytes
     * @return the transformed contents
     *
     * @throws TransformerException if something goes wrong with the XSL transformation
     */
    private byte[] transformContent(byte[] content) throws TransformerException {

        Transformer transformer = m_transformerFactory.newTransformer(
            new StreamSource(new ByteArrayInputStream(m_xslt)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new StreamSource(new ByteArrayInputStream(content)), new StreamResult(baos));
        byte[] result = baos.toByteArray();
        return result;

    }

    /**
     * Writes the content back to the given file.
     *
     * @param res the resource to write
     * @param content the content to write to the resource
     *
     * @return true if the content was updated
     *
     * @throws CmsException if something goes wrong
     */
    private boolean writeContent(CmsResource res, byte[] content) throws CmsException {

        CmsFile file = m_offlineCms.readFile(res);
        file.setContents(content);
        m_offlineCms.writeFile(file);
        return true;

    }

}
