/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt;

import org.opencms.ade.containerpage.inherited.CmsInheritanceGroupUtils;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.shared.CmsBrokenLinkBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * A helper class used to generate the necessary information for displaying links which will be broken
 * if the user tries to delete a file in the ADE GUI.<p>
 */
public class CmsBrokenLinkRenderer {

    /** The logger instance for this class.*/
    private static final Log LOG = CmsLog.getLog(CmsBrokenLinkRenderer.class);

    /** The CMS context used by the broken link renderer.<p> */
    private CmsObject m_cms;

    /**
     * Creates a new broken link renderer instance.<p>
     *
     * @param cms the current CMS context
     */
    public CmsBrokenLinkRenderer(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Renders the source of a broken link as a list of CmsBrokenLinkBean instances.<p>
     *
     * @param target the broken link target
     * @param source the broken link source
     * @return the list of broken link beans to display to the user
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsBrokenLinkBean> renderBrokenLink(CmsResource target, CmsResource source) throws CmsException {

        I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(source);
        String typeName = resType.getTypeName();
        if (typeName.equals(CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_CONFIG_TYPE_NAME)) {
            return renderBrokenLinkInheritanceGroup(target, source);
        } else if (CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME.equals(typeName)) {
            return renderBrokenLinkGroupContainer(target, source);
        } else {
            return renderBrokenLinkDefault(target, source);
        }
    }

    /**
     * The default method for rendering broken link sources.<p>
     *
     * @param target the link target
     * @param source the link source
     * @return the list of broken link beans to display to the user
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsBrokenLinkBean> renderBrokenLinkDefault(CmsResource target, CmsResource source) throws CmsException {

        List<CmsBrokenLinkBean> result = new ArrayList<CmsBrokenLinkBean>();
        result.add(createSitemapBrokenLinkBean(source));
        return result;
    }

    /**
     * Renders the broken links for a group container.<p>
     *
     * @param target the broken link target
     * @param source the broken link source
     *
     * @return the list of broken link beans to display to the user
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsBrokenLinkBean> renderBrokenLinkGroupContainer(CmsResource target, CmsResource source)
    throws CmsException {

        List<CmsBrokenLinkBean> result = new ArrayList<CmsBrokenLinkBean>();
        CmsBrokenLinkBean brokenLinkBean = createSitemapBrokenLinkBean(source);
        result.add(brokenLinkBean);
        try {
            CmsResource referencingPage = findReferencingPage(source);
            if (referencingPage != null) {
                String pagePath = m_cms.getRequestContext().removeSiteRoot(referencingPage.getRootPath());
                String title = CmsResource.getName(pagePath);
                CmsProperty titleProp = m_cms.readPropertyObject(
                    referencingPage,
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    false);
                if (!titleProp.isNullProperty()) {
                    title = titleProp.getValue();
                }
                addPageInfo(brokenLinkBean, title, pagePath);
            }
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        return result;
    }

    /**
     * Renders broken links from an inheritance group.<p>
     *
     * @param target the link target
     * @param source the link source
     *
     * @return the list of broken link beans to display to the user
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsBrokenLinkBean> renderBrokenLinkInheritanceGroup(CmsResource target, CmsResource source)
    throws CmsException {

        List<CmsBrokenLinkBean> result = new ArrayList<CmsBrokenLinkBean>();
        try {
            Set<String> names = CmsInheritanceGroupUtils.getNamesOfGroupsContainingResource(m_cms, source, target);
            if (!names.isEmpty()) {
                for (String name : names) {
                    String title = null;
                    String path = null;
                    String extraTitle = null;
                    String extraPath = null;

                    CmsResource group = CmsInheritanceGroupUtils.getInheritanceGroupContentByName(m_cms, name);
                    String groupParent = CmsResource.getParentFolder(source.getRootPath());
                    CmsProperty titleProp = m_cms.readPropertyObject(
                        group,
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        false);
                    title = CmsResource.getName(group.getRootPath());
                    if (!titleProp.isNullProperty()) {
                        title = titleProp.getValue();
                    }
                    path = m_cms.getRequestContext().removeSiteRoot(source.getRootPath());
                    List<CmsRelation> relations = m_cms.readRelations(
                        CmsRelationFilter.relationsToStructureId(group.getStructureId()));
                    List<CmsResource> referencingPages = new ArrayList<CmsResource>();
                    for (CmsRelation relation : relations) {
                        CmsResource relSource = relation.getSource(m_cms, CmsResourceFilter.ALL);
                        String pageParent = CmsResource.getParentFolder(relSource.getRootPath());
                        if (CmsResourceTypeXmlContainerPage.isContainerPage(relSource)
                            && pageParent.equals(groupParent)) {
                            referencingPages.add(relSource);
                        }
                    }
                    if (!referencingPages.isEmpty()) {
                        CmsResource firstPage = referencingPages.get(0);
                        extraPath = m_cms.getRequestContext().removeSiteRoot(firstPage.getRootPath());
                        extraTitle = m_cms.readPropertyObject(
                            firstPage,
                            CmsPropertyDefinition.PROPERTY_TITLE,
                            true).getValue();
                    }
                    result.add(
                        createBrokenLinkBean(
                            group.getStructureId(),
                            CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_CONFIG_TYPE_NAME,
                            title,
                            path,
                            extraTitle,
                            extraPath));
                }
            } else {
                result.add(createSitemapBrokenLinkBean(source));
            }
        } catch (CmsException e) {
            result.add(createSitemapBrokenLinkBean(source));
        }
        return result;
    }

    /**
     * Adds optional page information to the broken link bean.<p>
     *
     * @param bean the broken link bean
     * @param extraTitle the optional page title
     * @param extraPath the optional page path
     */
    protected void addPageInfo(CmsBrokenLinkBean bean, String extraTitle, String extraPath) {

        if (extraTitle != null) {
            bean.addInfo(messagePageTitle(), "" + extraTitle);
        }
        if (extraPath != null) {
            bean.addInfo(messagePagePath(), "" + extraPath);
        }
    }

    /**
     * Creates a broken link bean from the necessary values.<p>
     *
     * @param structureId the structure id of the resource
     * @param type the resource type
     * @param title the title
     * @param path the path
     * @param extraTitle an optional additional page title
     * @param extraPath an optional additional page path
     *
     * @return the created broken link bean
     */
    protected CmsBrokenLinkBean createBrokenLinkBean(
        CmsUUID structureId,
        String type,
        String title,
        String path,
        String extraTitle,
        String extraPath) {

        CmsBrokenLinkBean result = new CmsBrokenLinkBean(structureId, title, path, type);
        addPageInfo(result, extraTitle, extraPath);
        return result;
    }

    /**
     * Creates a "broken link" bean based on a resource.<p>
     *
     * @param resource the resource
     * @return the "broken link" bean with the data from the resource
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsBrokenLinkBean createSitemapBrokenLinkBean(CmsResource resource) throws CmsException {

        CmsProperty titleProp = m_cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, true);
        String typeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        String defaultTitle = CmsResource.getName(resource.getRootPath());
        String title = titleProp.getValue(defaultTitle);
        String path = m_cms.getSitePath(resource);
        String subtitle = path;
        return new CmsBrokenLinkBean(resource.getStructureId(), title, subtitle, typeName);
    }

    /**
     * Finds a page which references another resource.<p>
     *
     * @param source a resource
     * @return a page which references the resource, or null if no such page was found.
     *
     * @throws CmsException if something goes wrong
     */
    private CmsResource findReferencingPage(CmsResource source) throws CmsException {

        List<CmsRelation> relationsToFile = m_cms.readRelations(
            CmsRelationFilter.relationsToStructureId(source.getStructureId()));
        for (CmsRelation relation : relationsToFile) {
            try {
                CmsResource referencingPage = relation.getSource(m_cms, CmsResourceFilter.DEFAULT);
                if (CmsResourceTypeXmlContainerPage.isContainerPage(referencingPage)) {
                    return referencingPage;
                }
            } catch (CmsException e) {
                LOG.info(e.getLocalizedMessage(), e);
            }
        }
        return null;
    }

    /**
     * Gets the workplace locale.<p>
     *
     * @return the workplace locale
     */
    private Locale getLocale() {

        return OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    private String messagePagePath() {

        return org.opencms.gwt.Messages.get().getBundle(getLocale()).key(
            org.opencms.gwt.Messages.GUI_DEPENDENCY_PAGE_PATH_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    private String messagePageTitle() {

        return org.opencms.gwt.Messages.get().getBundle(getLocale()).key(
            org.opencms.gwt.Messages.GUI_DEPENDENCY_PAGE_TITLE_0);
    }

}
