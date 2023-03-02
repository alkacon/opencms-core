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

package org.opencms.jsp.util;

import org.opencms.ade.containerpage.CmsDetailOnlyContainerUtil;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Allows accessing 'attachments' of an XML content via the EL in JSP code, which in OpenCms are defined as the contents of its detail-only containers.
 */
public class CmsJspContentAttachmentsBean {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspContentAttachmentsBean.class);

    /** The container page bean. */
    protected CmsContainerPageBean m_page;

    /** Lazy map from container names to lists of elements. */
    private Map<String, List<CmsContainerElementBean>> m_byContainer;

    /** Lazy map from type names to lists of elements. */
    private Map<String, List<CmsContainerElementBean>> m_byType;

    /** Flag which indicates whether this is an empty attachments bean. */
    private boolean m_undefined;

    /** The CmsObject to read the detail page and the resources on that page. */
    private CmsObject m_cms;

    /**
     * Creates an 'undefined' attachments bean.<p>
     */
    public CmsJspContentAttachmentsBean() {

        m_page = new CmsContainerPageBean(new ArrayList<CmsContainerBean>());
        m_undefined = true;

    }

    /**
     * Initializes this bean with the contents of a detail-only page.<p>
     *
     * @param cms the CMS context
     * @param pageResource the detail-only container page
     *
     * @throws CmsException if something goes wrong
     */
    public CmsJspContentAttachmentsBean(CmsObject cms, CmsResource pageResource)
    throws CmsException {

        CmsXmlContainerPage xmlContainerPage = CmsXmlContainerPageFactory.unmarshal(
            cms,
            cms.readFile(pageResource),
            true,
            /*nocache=*/true); // using cache causes problems for the EL container rendering feature
        m_page = xmlContainerPage.getContainerPage(cms);
        m_cms = cms;

    }

    /**
     * Gets the list of locales for which attachments / detail-only containers are available.<p>
     *
     * @param cms the current CMS context
     * @param content the content resource
     *
     * @return the list of locales for which there are attachments
     */
    public static List<String> getAttachmentLocales(CmsObject cms, CmsResource content) {

        List<CmsResource> detailOnlyResources = CmsDetailOnlyContainerUtil.getDetailOnlyResources(cms, content);
        Set<String> validLocaleNames = Sets.newHashSet();
        List<String> result = Lists.newArrayList();
        for (Locale locale : OpenCms.getLocaleManager().getAvailableLocales()) {
            validLocaleNames.add(locale.toString());
        }
        validLocaleNames.add(CmsDetailOnlyContainerUtil.LOCALE_ALL);
        for (CmsResource resource : detailOnlyResources) {
            String parent = CmsResource.getParentFolder(resource.getRootPath());
            String parentName = CmsResource.getName(parent);
            parentName = CmsFileUtil.removeTrailingSeparator(parentName);
            if (validLocaleNames.contains(parentName)) {
                result.add(parentName);
            }
        }
        return result;

    }

    /**
     * Gets the attachments / detail-only contents for the current page (i.e. cms.getRequestContext().getUri()).<p>
     *
     * @param cms the CMS context
     * @param content the content for which to get the attachments
     * @return a bean providing access to the attachments for the resource
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsJspContentAttachmentsBean getAttachmentsForCurrentPage(CmsObject cms, CmsResource content)
    throws CmsException {

        CmsResource page = cms.readResource(cms.getRequestContext().getUri(), CmsResourceFilter.IGNORE_EXPIRATION);
        String locale = CmsDetailOnlyContainerUtil.getDetailContainerLocale(
            cms,
            cms.getRequestContext().getLocale().toString(),
            page);
        Optional<CmsResource> detailOnly = CmsDetailOnlyContainerUtil.getDetailOnlyPage(cms, content, locale);
        if (detailOnly.isPresent()) {
            try {
                return new CmsJspContentAttachmentsBean(cms, detailOnly.get());
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return new CmsJspContentAttachmentsBean();
            }
        } else {
            return new CmsJspContentAttachmentsBean();
        }
    }

    /**
     * Loads the attachments for a given content.<p>
     *
     * @param cms the CMS context
     * @param content the content
     * @param locale the locale
     *
     * @return the attachment bean for the given content and locale
     */
    public static CmsJspContentAttachmentsBean getAttachmentsForLocale(
        CmsObject cms,
        CmsResource content,
        String locale) {

        Optional<CmsResource> detailOnly = CmsDetailOnlyContainerUtil.getDetailOnlyPage(cms, content, locale);
        if (!detailOnly.isPresent()) {
            return new CmsJspContentAttachmentsBean();
        } else {
            try {
                return new CmsJspContentAttachmentsBean(cms, detailOnly.get());
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return new CmsJspContentAttachmentsBean();
            }
        }
    }

    /**
     * Gets lazy map that returns lists of element beans for the container whose name is given as a key.<p>
     *
     * @return a lazy map to fetch contents of a container
     */
    public Map<String, List<CmsContainerElementBean>> getByContainer() {

        if (m_byContainer == null) {
            m_byContainer = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                public Object transform(Object arg0) {

                    String key = (String)arg0;
                    CmsContainerBean container = m_page.getContainers().get(key);
                    if (container == null) {
                        return Collections.emptyList();
                    } else {
                        return container.getElements();
                    }
                }
            });
        }
        return m_byContainer;
    }

    /**
     * Gets lazy map that maps type names to lists of container elements of that type.<p>
     *
     * @return a map from type names to lists of container elements
     */
    public Map<String, List<CmsContainerElementBean>> getByType() {

        if (m_byType == null) {
            m_byType = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @SuppressWarnings("synthetic-access")
                public Object transform(Object arg0) {

                    String key = (String)arg0;
                    List<CmsContainerElementBean> result = Lists.newArrayList();
                    for (Map.Entry<String, CmsContainerBean> entry : getPage().getContainers().entrySet()) {
                        CmsContainerBean value = entry.getValue();
                        for (CmsContainerElementBean element : value.getElements()) {
                            try {
                                element.initResource(m_cms);
                                if (key.equals(element.getTypeName())) {
                                    result.add(element);
                                }
                            } catch (CmsException e) {
                                LOG.error(
                                    "Could not initialize resource with site path \""
                                        + element.getSitePath()
                                        + "\" to determine the container elements type.",
                                    e);
                            }
                        }
                    }
                    return result;
                }
            });
        }
        return m_byType;
    }

    /**
     * Converts this to a container page wrapper.
     *
     * @return the container page wrapper
     */
    public CmsJspContainerPageWrapper getContainerPage() {

        return new CmsJspContainerPageWrapper(m_page);
    }

    /**
     * Gets the set of container names.<p>
     *
     * @return the set of container names
     */
    public Set<String> getContainers() {

        return Sets.newHashSet(m_page.getContainers().keySet());
    }

    /**
     * Returns true if the attachments are undefined.<p>
     *
     * @return true if the attachments are undefined
     */
    public boolean isUndefined() {

        return m_undefined;
    }

    /**
     * Gets the container page bean for the detail-only page.<p>
     *
     * @return the container page bean
     */
    protected CmsContainerPageBean getPage() {

        return m_page;
    }

}
