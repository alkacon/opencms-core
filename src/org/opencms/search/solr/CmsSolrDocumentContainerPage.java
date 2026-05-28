/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search.solr;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsFormatterUtils;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.documents.Messages;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Lucene document factory class to extract index data from a resource
 * of type <code>CmsResourceTypeContainerPage</code>.<p>
 *
 * @since 8.5.0
 */
public class CmsSolrDocumentContainerPage extends CmsSolrDocumentXmlContent {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrDocumentContainerPage.class);

    /** The solr document type name for xml-contents. */
    public static final String TYPE_CONTAINERPAGE_SOLR = "containerpage-solr";

    /**
     * Sitemap attribute to list (comma-separated) names of containers
     * whose elements content should not be extracted for the content of the page.
     * NOTE: For nested containers, we match against the names without prefix.
     */
    private static final String SITEMAP_ATTRIBUTE_EXCLUDE_CONTAINER_NAMES = "search.containers.exclude.names";

    /**
     * Sitemap attribute to list (comma-separated) types of containers
     * whose elements content should not be extracted for the content of the page.
     */
    private static final String SITEMAP_ATTRIBUTE_EXCLUDE_CONTAINER_TYPES = "search.containers.exclude.types";

    /**
     * Creates a new instance of this lucene document factory.<p>
     *
     * @param name name of the document type
     */
    public CmsSolrDocumentContainerPage(String name) {

        super(name);
    }

    /**
     * Returns the raw text content of a VFS resource of type <code>CmsResourceTypeContainerPage</code>.<p>
     *
     * @see org.opencms.search.documents.I_CmsSearchExtractor#extractContent(CmsObject, CmsResource, I_CmsSearchIndex)
     */
    @Override
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, I_CmsSearchIndex index)
    throws CmsException {

        return extractContent(cms, resource, index, null);
    }

    /**
     * Extracts the content of a given index resource according to the resource file type and the
     * configuration of the given index.<p>
     *
     * @param cms the cms object
     * @param resource the resource to extract the content from
     * @param index the index to extract the content for
     * @param forceLocale if set, only the content values for the given locale will be extracted
     *
     * @return the extracted content of the resource
     *
     * @throws CmsException if something goes wrong
     */
    public I_CmsExtractionResult extractContent(
        CmsObject cms,
        CmsResource resource,
        I_CmsSearchIndex index,
        Locale forceLocale)
    throws CmsException {

        logContentExtraction(resource, index);
        I_CmsExtractionResult ex = null;
        try {
            CmsFile file = readFile(cms, resource);
            CmsXmlContainerPage containerPage = CmsXmlContainerPageFactory.unmarshal(cms, file);

            List<I_CmsExtractionResult> all = new ArrayList<I_CmsExtractionResult>();
            CmsContainerPageBean containerBean = containerPage.getContainerPage(cms);
            if (containerBean != null) {
                CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfigurationWithCache(
                    cms,
                    file.getRootPath());

                Collection<String> excludeContainerNames = CmsStringUtil.splitAsSet(
                    adeConfig.getAttribute(SITEMAP_ATTRIBUTE_EXCLUDE_CONTAINER_NAMES, ""),
                    ",",
                    true);
                Collection<String> excludeContainerTypes = CmsStringUtil.splitAsSet(
                    adeConfig.getAttribute(SITEMAP_ATTRIBUTE_EXCLUDE_CONTAINER_TYPES, ""),
                    ",",
                    true);

                for (Map.Entry<String, CmsContainerBean> entry : containerBean.getContainers().entrySet()) {
                    String containerName = entry.getKey();
                    if (includeContainerForSearch(
                        entry.getValue(),
                        containerBean,
                        excludeContainerNames,
                        excludeContainerTypes)) {
                        for (CmsContainerElementBean element : entry.getValue().getElements()) {
                            // check all elements in this container
                            // get the formatter configuration for this element
                            try {
                                element.initResource(cms);
                                CmsResource elementResource = element.getResource();
                                if (!(cms.readProject(index.getProject()).isOnlineProject()
                                    && elementResource.isExpired(System.currentTimeMillis()))) {
                                    CmsFormatterConfiguration formatters = adeConfig.getFormatters(
                                        cms,
                                        element.getResource());
                                    boolean shouldExtractElement = false;
                                    if ((formatters != null)
                                        && (element.getFormatterId() != null)
                                        && (formatters.isSearchContent(element.getFormatterId())
                                            || adeConfig.isSearchContentFormatter(element.getFormatterId()))) {
                                        // the content of this element must be included for the container page
                                        shouldExtractElement = true;
                                    } else if (formatters != null) {
                                        String key = CmsFormatterUtils.getFormatterKey(containerName, element);
                                        I_CmsFormatterBean formatter = adeConfig.findFormatter(key);
                                        if (formatter != null) {
                                            shouldExtractElement = formatter.isSearchContent();
                                        }
                                    }
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug(
                                            "Should extract element "
                                                + element.getResource().getRootPath()
                                                + ": "
                                                + shouldExtractElement);
                                    }
                                    if (shouldExtractElement) {
                                        all.add(
                                            CmsSolrDocumentXmlContent.extractXmlContent(
                                                cms,
                                                elementResource,
                                                index,
                                                forceLocale));
                                    }

                                }
                            } catch (Exception e) {
                                LOG.debug(
                                    Messages.get().getBundle().key(
                                        Messages.LOG_SKIPPING_CONTAINERPAGE_ELEMENT_WITH_UNREADABLE_RESOURCE_2,
                                        file.getRootPath(),
                                        element.getId()),
                                    e);
                            }
                        }
                    } else if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            "Skip content extraction for container "
                                + containerName
                                + " in  page "
                                + file.getRootPath()
                                + " because of type or name excludes configured via sitemap attribute "
                                + SITEMAP_ATTRIBUTE_EXCLUDE_CONTAINER_NAMES
                                + " or "
                                + SITEMAP_ATTRIBUTE_EXCLUDE_CONTAINER_TYPES
                                + ".");
                    }
                }
            }
            // we have to overwrite the resource and content locales with the one from this container page
            // TODO: Is this really the wanted behavior? It seems to be done like this before.
            Map<String, String> fieldMappings = new HashMap<String, String>(1);
            // Add to each container page the contents in all available locales,
            // in case one containerpage is used in multiple languages.
            List<Locale> localesAvailable = OpenCms.getLocaleManager().getAvailableLocales(cms, resource);
            Map<Locale, LinkedHashMap<String, String>> multilingualValues = new HashMap<Locale, LinkedHashMap<String, String>>(
                localesAvailable.size());
            for (Locale localeAvailable : localesAvailable) {
                multilingualValues.put(localeAvailable, new LinkedHashMap<String, String>());
            }
            Locale locale = forceLocale != null
            ? forceLocale
            : index.getLocaleForResource(cms, resource, containerPage.getLocales());
            ex = new CmsExtractionResult(locale, multilingualValues, fieldMappings);
            ex = ex.merge(all);
            return ex;
        } catch (Exception e) {
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                e);
        }
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#isLocaleDependend()
     */
    @Override
    public boolean isLocaleDependend() {

        return true;
    }

    /**
     * Checks if the elements in the provided container should be added to the extracted content of the page.
     * They are only added if they should be added for the container and all its parent containers.
     *
     * @param container the container to check
     * @param containerPageBean the container page the container is on
     * @param excludeContainerNames set of container names to skip content extraction for
     * @param excludeContainerTypes set of container types to skip content extraction for
     * @return flag, indicating if the container's elements content should be extracted for the page content.
     */
    private boolean includeContainerForSearch(
        CmsContainerBean container,
        CmsContainerPageBean containerPageBean,
        Collection<String> excludeContainerNames,
        Collection<String> excludeContainerTypes) {

        if (container.isNestedContainer()) {
            // The parent of a nested container is the element (identified by its element instance id)
            // whose formatter renders this container. Find the container that holds that parent element.
            String parentInstanceId = container.getParentInstanceId();
            for (CmsContainerBean c : containerPageBean.getContainers().values()) {
                boolean containsParentElement = c.getElements().stream().anyMatch(
                    element -> parentInstanceId.equals(element.getInstanceId()));
                if (containsParentElement) {
                    if (!includeContainerForSearch(
                        c,
                        containerPageBean,
                        excludeContainerNames,
                        excludeContainerTypes)) {
                        // the elements of the parent container are not included, so this container can't be included as well
                        return false;
                    }
                    // the parent container's elements are included, so check the container itself
                    break;
                }
            }
        }
        Set<String> containerTypes = CmsStringUtil.splitAsSet(container.getType(), ",", true);
        return !excludeContainerNames.contains(container.getSimpleName())
            && Collections.disjoint(excludeContainerTypes, containerTypes);
    }

}
