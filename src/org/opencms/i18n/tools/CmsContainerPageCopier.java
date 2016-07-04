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

package org.opencms.i18n.tools;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.I_CmsFileNameGenerator;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.Messages;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Helper class for copying container pages including some of their elements.<p>
 */
public class CmsContainerPageCopier {

    /**
     * Enum representing the element copy mode.<p>
     */
    public enum CopyMode {
        /** Do not copy elements. */
        reuse,

        /** Automatically determine when to copy elements. */
        smartCopy,

        /** Like smartCopy, but also converts locales of copied elements. */
        smartCopyAndChangeLocale;
    }

    /** The log instance used for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContainerPageCopier.class);

    /** The CMS context used by this object. */
    private CmsObject m_cms;

    /** The copy mode. */
    private CopyMode m_copyMode = CopyMode.smartCopy;

    /** Maps structure ids of original container elements to structure ids of their copies/replacements. */
    private Map<CmsUUID, CmsUUID> m_elementReplacements = Maps.newHashMap();

    /** The original page. */
    private CmsResource m_originalPage;

    /** The target folder. */
    private CmsResource m_targetFolder;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context to use
     */
    public CmsContainerPageCopier(CmsObject cms) {
        m_cms = cms;
    }

    /**
     * Converts locales for the copied container element.<p>
     *
     * @param elementResource the copied container element
     * @throws CmsException if something goes wrong
     */
    public void adjustLocalesForElement(CmsResource elementResource) throws CmsException {

        if (m_copyMode != CopyMode.smartCopyAndChangeLocale) {
            return;
        }

        CmsFile file = m_cms.readFile(elementResource);
        Locale oldLocale = OpenCms.getLocaleManager().getDefaultLocale(m_cms, m_originalPage);
        Locale newLocale = OpenCms.getLocaleManager().getDefaultLocale(m_cms, m_targetFolder);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, file);
        try {
            content.moveLocale(oldLocale, newLocale);
            LOG.info("Replacing locale " + oldLocale + " -> " + newLocale + " for " + elementResource.getRootPath());
            file.setContents(content.marshal());
            m_cms.writeFile(file);
        } catch (CmsXmlException e) {
            LOG.info(
                "NOT replacing locale for "
                    + elementResource.getRootPath()
                    + ": old="
                    + oldLocale
                    + ", new="
                    + newLocale
                    + ", contentLocales="
                    + content.getLocales());
        }

    }

    /**
     * Produces the replacement for a container page element to use in a copy of an existing container page.<p>
     *
     * @param targetPage the target container page
     * @param originalElement the original element
     * @return the replacement element for the copied page
     *
     * @throws CmsException if something goes wrong
     */
    public CmsContainerElementBean replaceContainerElement(
        CmsResource targetPage,
        CmsContainerElementBean originalElement) throws CmsException {
        // if (m_elementReplacements.containsKey(originalElement.getId()

        if (m_copyMode == CopyMode.reuse) {
            LOG.info("Reusing element " + originalElement.getId() + " because we are in reuse mode");
            return originalElement;
        }
        if (m_elementReplacements.containsKey(originalElement.getId())) {
            return new CmsContainerElementBean(
                m_elementReplacements.get(originalElement.getId()),
                originalElement.getFormatterId(),
                originalElement.getIndividualSettings(),
                originalElement.isCreateNew());
        } else {
            CmsResource originalResource = m_cms.readResource(
                originalElement.getId(),
                CmsResourceFilter.IGNORE_EXPIRATION);
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(originalResource);
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(m_cms, targetPage.getRootPath());
            CmsResourceTypeConfig typeConfig = config.getResourceType(type.getTypeName());
            boolean shouldCopyElement;
            if (typeConfig == null) {
                LOG.warn(
                    "Type configuration for type " + type.getTypeName() + " not found at " + targetPage.getRootPath());
                shouldCopyElement = false;
            } else {
                shouldCopyElement = (originalElement.isCreateNew() || typeConfig.isCopyInModels())
                    && !type.getTypeName().equals(CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME);
            }
            if (shouldCopyElement) {
                CmsResource resourceCopy = typeConfig.createNewElement(
                    m_cms,
                    originalResource,
                    targetPage.getRootPath());
                CmsContainerElementBean copy = new CmsContainerElementBean(
                    resourceCopy.getStructureId(),
                    originalElement.getFormatterId(),
                    originalElement.getIndividualSettings(),
                    originalElement.isCreateNew());
                m_elementReplacements.put(originalElement.getId(), resourceCopy.getStructureId());
                LOG.info(
                    "Copied container element " + originalResource.getRootPath() + " -> " + resourceCopy.getRootPath());
                CmsLockActionRecord record = null;
                try {
                    record = CmsLockUtil.ensureLock(m_cms, resourceCopy);
                    adjustLocalesForElement(resourceCopy);
                } finally {
                    if ((record != null) && (record.getChange() == LockChange.locked)) {
                        m_cms.unlockResource(resourceCopy);
                    }
                }
                return copy;
            } else {
                LOG.info("Reusing container element: " + originalResource.getRootPath());
                return originalElement;
            }
        }
    }

    /**
     * Replaces the elements in the copied container page with copies, if appropriate based on the current copy mode.<p>
     *
     * @param containerPage the container page copy whose elements should be replaced with copies
     * @throws CmsException if something goes wrong
     */
    public void replaceElements(CmsResource containerPage) throws CmsException {

        CmsXmlContainerPage pageXml = CmsXmlContainerPageFactory.unmarshal(m_cms, containerPage);
        CmsContainerPageBean page = pageXml.getContainerPage(m_cms);
        List<CmsContainerBean> newContainers = Lists.newArrayList();
        for (CmsContainerBean container : page.getContainers().values()) {
            List<CmsContainerElementBean> newElements = Lists.newArrayList();
            for (CmsContainerElementBean element : container.getElements()) {
                CmsContainerElementBean newBean = replaceContainerElement(containerPage, element);
                if (newBean != null) {
                    newElements.add(newBean);
                }
            }
            CmsContainerBean newContainer = new CmsContainerBean(
                container.getName(),
                container.getType(),
                container.getParentInstanceId(),
                newElements);
            newContainers.add(newContainer);
        }
        CmsContainerPageBean newPageBean = new CmsContainerPageBean(newContainers);
        pageXml.save(m_cms, newPageBean);
    }

    /**
     * Starts the page copying process.<p>
     *
     * @param source the source (can be either a container page, or a folder whose default file is a container page)
     * @param target the target folder
     *
     * @throws CmsException if soemthing goes wrong
     */
    public void run(CmsResource source, CmsResource target) throws CmsException {

        LOG.info(
            "Starting page copy process: page='"
                + source.getRootPath()
                + "', targetFolder='"
                + target.getRootPath()
                + "'");
        CmsObject rootCms = OpenCms.initCmsObject(m_cms);
        rootCms.getRequestContext().setSiteRoot("");
        if (source.isFolder()) {
            if (source.equals(target)) {
                throw new CmsException(Messages.get().container(Messages.ERR_PAGECOPY_SOURCE_IS_TARGET_0));
            }
            CmsResource page = m_cms.readDefaultFile(source, CmsResourceFilter.IGNORE_EXPIRATION);
            if ((page == null) || !CmsResourceTypeXmlContainerPage.isContainerPage(page)) {
                throw new CmsException(Messages.get().container(Messages.ERR_PAGECOPY_INVALID_PAGE_0));
            }
            List<CmsProperty> properties = m_cms.readPropertyObjects(source, false);
            I_CmsFileNameGenerator nameGen = OpenCms.getResourceManager().getNameGenerator();
            String copyPath = CmsFileUtil.removeTrailingSeparator(
                CmsStringUtil.joinPaths(target.getRootPath(), source.getName()));
            copyPath = nameGen.getNewFileName(rootCms, copyPath + "%(number)", 4, true);
            Double maxNavPosObj = readMaxNavPos(target);
            double maxNavpos = maxNavPosObj == null ? 0 : maxNavPosObj.doubleValue();
            boolean hasNavpos = maxNavPosObj != null;
            CmsResource copiedFolder = rootCms.createResource(
                copyPath,
                OpenCms.getResourceManager().getResourceType(source.getTypeId()),
                null,
                properties);
            if (hasNavpos) {
                String newNavPosStr = "" + (maxNavpos + 10);
                rootCms.writePropertyObject(
                    copiedFolder.getRootPath(),
                    new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVPOS, newNavPosStr, null));
            }
            String pageCopyPath = CmsStringUtil.joinPaths(copiedFolder.getRootPath(), page.getName());
            m_originalPage = page;
            m_targetFolder = copiedFolder;
            rootCms.copyResource(page.getRootPath(), pageCopyPath);

            CmsResource copiedPage = rootCms.readResource(pageCopyPath, CmsResourceFilter.IGNORE_EXPIRATION);
            replaceElements(copiedPage);
            tryUnlock(copiedFolder);
        } else {
            CmsResource page = source;
            if (!CmsResourceTypeXmlContainerPage.isContainerPage(page)) {
                throw new CmsException(Messages.get().container(Messages.ERR_PAGECOPY_INVALID_PAGE_0));
            }
            I_CmsFileNameGenerator nameGen = OpenCms.getResourceManager().getNameGenerator();
            String copyPath = CmsFileUtil.removeTrailingSeparator(
                CmsStringUtil.joinPaths(target.getRootPath(), source.getName()));
            int lastDot = copyPath.lastIndexOf(".");
            int lastSlash = copyPath.lastIndexOf("/");
            if (lastDot > lastSlash) {
                String macroPath = copyPath.substring(0, lastDot) + "%(number)" + copyPath.substring(lastDot);
                copyPath = nameGen.getNewFileName(rootCms, macroPath, 4, true);
            } else {
                copyPath = nameGen.getNewFileName(rootCms, copyPath + "%(number)", 4, true);
            }
            Double maxNavPosObj = readMaxNavPos(target);
            double maxNavpos = maxNavPosObj == null ? 0 : maxNavPosObj.doubleValue();
            boolean hasNavpos = maxNavPosObj != null;
            rootCms.copyResource(page.getRootPath(), copyPath);
            if (hasNavpos) {
                String newNavPosStr = "" + (maxNavpos + 10);
                rootCms.writePropertyObject(
                    copyPath,
                    new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVPOS, newNavPosStr, null));
            }
            CmsResource copiedPage = rootCms.readResource(copyPath);
            m_originalPage = page;
            m_targetFolder = target;
            replaceElements(copiedPage);
            tryUnlock(copiedPage);

        }
    }

    /**
     * Sets the copy mode.<p>
     *
     * @param copyMode the copy mode
     */
    public void setCopyMode(CopyMode copyMode) {

        m_copyMode = copyMode;
    }

    /**
     * Reads the max nav position from the contents of a folder.<p>
     *
     * @param target a folder
     * @return the maximal NavPos from the contents of the folder, or null if no resources with a valid NavPos were found in the folder
     *
     * @throws CmsException if something goes wrong
     */
    Double readMaxNavPos(CmsResource target) throws CmsException {

        List<CmsResource> existingResourcesInFolder = m_cms.readResources(
            target,
            CmsResourceFilter.IGNORE_EXPIRATION,
            false);

        double maxNavpos = 0.0;
        boolean hasNavpos = false;
        for (CmsResource existingResource : existingResourcesInFolder) {
            CmsProperty navpos = m_cms.readPropertyObject(
                existingResource,
                CmsPropertyDefinition.PROPERTY_NAVPOS,
                false);
            if (navpos.getValue() != null) {
                try {
                    double navposNum = Double.parseDouble(navpos.getValue());
                    hasNavpos = true;
                    maxNavpos = Math.max(navposNum, maxNavpos);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        if (hasNavpos) {
            return Double.valueOf(maxNavpos);
        } else {
            return null;
        }
    }

    /**
     * Tries to unlock the given resource.<p>
     *
     * @param resource the resource to unlock
     */
    private void tryUnlock(CmsResource resource) {

        try {
            m_cms.unlockResource(resource);
        } catch (CmsException e) {
            // usually not a problem
            LOG.debug("failed to unlock " + resource.getRootPath(), e);
        }

    }

}
