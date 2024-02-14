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

package org.opencms.ade.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsGroupContainerBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.CmsXmlGroupContainer;
import org.opencms.xml.containerpage.CmsXmlGroupContainerFactory;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Analyzes content type and formatter usage in a site / folder.
 */
public class CmsTypeAnalyzer {

    /**
     * Bean for formatter information.
     */
    public static class FormatterBean implements Serializable {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /** The id. */
        private CmsUUID m_id;

        /** The key. */
        private String m_key;

        /** The path. */
        private String m_path;

        /** The nice name. */
        private String m_niceName;

        /**
         * Instantiates a new formatter bean.
         *
         * @param id the id
         * @param path the path
         * @param key the key
         * @param niceName the nice name
         */
        public FormatterBean(CmsUUID id, String path, String key, String niceName) {

            super();
            m_id = id;
            m_path = path;
            m_key = key;
            m_niceName = niceName;
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public CmsUUID getId() {

            return m_id;
        }

        /**
         * Gets the key.
         *
         * @return the key
         */
        public String getKey() {

            return m_key;
        }

        /**
         * Gets the nice name.
         *
         * @return the nice name
         */
        public String getNiceName() {

            return m_niceName;
        }

        /**
         * Gets the path.
         *
         * @return the path
         */
        public String getPath() {

            return m_path;
        }

    }

    /**
     * Data for a single resource.
     */
    public static class ResourceBean implements Serializable {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /** The id. */
        private CmsUUID m_id;

        /** The path. */
        private String m_path;

        /**
         * Instantiates a new resource bean.
         *
         * @param id the id
         * @param path the path
         */
        public ResourceBean(CmsUUID id, String path) {

            super();
            m_id = id;
            m_path = path;
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public CmsUUID getId() {

            return m_id;
        }

        /**
         * Gets the path.
         *
         * @return the path
         */
        public String getPath() {

            return m_path;
        }
    }

    /**
     * Represents all data collected by the CmsTypeAnalyzer class.
     */
    public static class State implements Serializable {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /** The type usage. */
        protected Map<String, Multimap<CmsUUID, CmsUUID>> m_typeUsage = new HashMap<>();

        /** The formatters. */
        protected Map<CmsUUID, FormatterBean> m_formatters = new HashMap<>();

        /** The pages. */
        protected Map<CmsUUID, ResourceBean> m_pages = new HashMap<>();

        /** The types. */
        protected Map<String, TypeBean> m_types = new LinkedHashMap<>();

        /** The path. */
        protected String m_path;

        /** The site root. */
        protected String m_siteRoot;

        /** The set of containers to exclude. */
        protected Set<String> m_excludedContainers;

        /** True if detail only contents are skipped. */
        public boolean m_skipDetailOnly;

        /** The map of (legacy) function usages. */
        private Map<String, Set<String>> m_functionUsage = new HashMap<>();

        /** The template regex. */
        public String m_templateRegex;

        public static long getSerialversionuid() {

            return serialVersionUID;
        }

        /**
         * Gets the container names to exclude.
         *
         * @return the top-level containers to exclude
         */
        public Set<String> getExcludedContainers() {

            return m_excludedContainers;
        }

        /**
         * Gets the formatters.
         *
         * @return the formatters
         */
        public Map<CmsUUID, FormatterBean> getFormatters() {

            return m_formatters;
        }

        /**
         * Gets the (legacy) dynamic function usages.
         *
         * @return the legacy function usages
         */
        public Map<String, Set<String>> getFunctionUsages() {

            return m_functionUsage;
        }

        /**
         * Gets the pages.
         *
         * @return the pages
         */
        public Map<CmsUUID, ResourceBean> getPages() {

            return m_pages;
        }

        /**
         * Gets the pages.
         *
         * @param type the type
         * @param formatter the formatter
         * @return the pages
         */
        public List<String> getPages(String type, CmsUUID formatter) {

            Collection<CmsUUID> usage = getTypeUsage().get(type).get(formatter);
            return usage.stream().map(id -> m_pages.get(id).getPath()).distinct().sorted().collect(Collectors.toList());
        }

        /**
         * Gets the path.
         *
         * @return the path
         */
        public String getPath() {

            return m_path;
        }

        /**
         * Gets the site root.
         *
         * @return the site root
         */
        public String getSiteRoot() {

            return m_siteRoot;
        }

        /**
         * Gets the sorted formatters.
         *
         * @param type the type
         * @return the sorted formatters
         */
        public List<FormatterBean> getSortedFormatters(String type) {

            if (!m_typeUsage.containsKey(type)) {
                return Collections.emptyList();
            }
            Multimap<CmsUUID, CmsUUID> formatterUsages = m_typeUsage.get(type);
            return formatterUsages.keySet().stream().sorted((f1, f2) -> {
                return -Integer.compare(formatterUsages.get(f1).size(), formatterUsages.get(f2).size());
            }).map(id -> m_formatters.get(id)).collect(Collectors.toList());
        }

        public String getTemplateRegex() {

            return m_templateRegex;
        }

        /**
         * Gets the types.
         *
         * @return the types
         */
        public Map<String, TypeBean> getTypes() {

            return m_types;
        }

        /**
         * Gets the type usage.
         *
         * @return the type usage
         */
        public Map<String, Multimap<CmsUUID, CmsUUID>> getTypeUsage() {

            return m_typeUsage;
        }

        public boolean isSkipDetailOnly() {

            return m_skipDetailOnly;
        }
    }

    /**
     * Data for a single content type.
     */
    public static class TypeBean implements Serializable {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /** The name. */
        private String m_name;

        /** The nice name. */
        private String m_niceName;

        /** The count. */
        private int m_count;

        /** The usage count. */
        private int m_usageCount;

        /**
         * Instantiates a new type bean.
         *
         * @param name the name
         * @param niceName the nice name
         * @param count the count
         */
        public TypeBean(String name, String niceName, int count) {

            super();
            m_name = name;
            m_niceName = niceName;
            m_count = count;
        }

        /**
         * Gets the count.
         *
         * @return the count
         */
        public int getCount() {

            return m_count;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Gets the nice name.
         *
         * @return the nice name
         */
        public String getNiceName() {

            return m_niceName;
        }

        /**
         * Gets the usage count.
         *
         * @return the usage count
         */
        public int getUsageCount() {

            return m_usageCount;
        }

        /**
         * Sets the count.
         *
         * @param count the new count
         */
        public void setCount(int count) {

            m_count = count;
        }

        /**
         * Sets the usage count.
         *
         * @param referenceCount the new usage count
         */
        public void setUsageCount(int referenceCount) {

            m_usageCount = referenceCount;
        }
    }

    /** The Constant LOG. */
    private static final Log LOG = CmsLog.getLog(CmsTypeAnalyzer.class);

    /** The Constant UNKNOWN_FORMATTER. */
    public static final CmsUUID UNKNOWN_FORMATTER = CmsUUID.getNullUUID();

    /** The m state. */
    private State m_state = new State();

    /** The m locale. */
    private Locale m_locale;

    /** The m cms. */
    private CmsObject m_cms;

    private Pattern m_templatePattern;

    /**
     * Creates a new instance.
     *
     * @param cms the CMS context
     * @param siteRoot the site root
     * @param path the site path to analyze
     * @throws CmsException if something goes wrong
     */
    public CmsTypeAnalyzer(
        CmsObject cms,
        String siteRoot,
        String path,
        boolean skipDetailOnly,
        Set<String> excludedContainers,
        String templateRegex)
    throws CmsException {

        m_cms = OpenCms.initCmsObject(cms);
        m_cms.getRequestContext().setSiteRoot(siteRoot);
        m_state.m_path = path;
        m_state.m_siteRoot = siteRoot;
        m_state.m_skipDetailOnly = skipDetailOnly;
        m_state.m_excludedContainers = excludedContainers;
        m_state.m_templateRegex = templateRegex;
        m_templatePattern = Pattern.compile(templateRegex);
        m_locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
    }

    /**
     * Deserializes the state from a byte array.
     *
     * @param data the data
     * @return the deserialized state
     * @throws Exception if something goes wrong
     */
    public static State readState(byte[] data) throws Exception {

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ObjectInputStream stream = new ObjectInputStream(new InflaterInputStream(bais))) {
            return (State)stream.readObject();
        }

    }

    /**
     * Runs the type analysis and returns the state object with all the collected data.
     *
     * @param cms the CMS context
     * @param path the path
     * @param skipDetailOnly true if detail only pages should be skipped
     * @param excludeContainersStr a comma-separated list of container names to exclude from analysis (only direct elements)
     * @param templateRegex a regular expression such that only pages whose template matches that regex should be processed
     * @return the state
     * @throws CmsException if something goes wrong
     */
    public static State run(
        CmsObject cms,
        String path,
        boolean skipDetailOnly,
        String excludeContainersStr,
        String templateRegex)
    throws CmsException {

        Set<String> excludedContainers = new HashSet<>();
        for (String token : excludeContainersStr.split(",")) {
            token = token.trim();
            if ("".equals(token)) {
                continue;
            }
            excludedContainers.add(token);
        }

        return (new CmsTypeAnalyzer(
            cms,
            cms.getRequestContext().getSiteRoot(),
            path,
            skipDetailOnly,
            excludedContainers,
            templateRegex)).processFolder();

    }

    /**
     * Serializes a state to a byte array.
     *
     * @param state the state
     * @return the serialized data
     * @throws IOException if something goes wrong with serialization
     */
    public static byte[] writeState(State state) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ObjectOutputStream out = new ObjectOutputStream(new DeflaterOutputStream(baos))) {
            out.writeObject(state);
        }
        return baos.toByteArray();

    }

    /**
     * Gets the state.
     *
     * @return the state
     */
    public State getState() {

        return m_state;
    }

    /**
     * Process folder.
     *
     * @return the state
     * @throws CmsException the cms exception
     */
    public State processFolder() throws CmsException {

        long start = System.currentTimeMillis();
        I_CmsResourceType pageType = OpenCms.getResourceManager().getResourceType(
            CmsResourceTypeXmlContainerPage.RESOURCE_TYPE_NAME);
        I_CmsResourceType modelGroupType = OpenCms.getResourceManager().getResourceType(
            CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME);
        List<CmsResource> pages = m_cms.readResources(
            m_state.m_path,
            CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(pageType));
        for (CmsResource page : pages) {
            if (!checkTemplate(page)) {
                continue;
            }
            if (m_state.m_skipDetailOnly && page.getRootPath().contains(".detailContainers")) {
                continue;
            }
            processPage(page);
        }
        List<CmsResource> modelGroups = m_cms.readResources(
            m_state.m_path,
            CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(modelGroupType));
        for (CmsResource modelGroup : modelGroups) {
            if (!checkTemplate(modelGroup)) {
                continue;
            }
            processPage(modelGroup);
        }

        List<CmsResource> elementGroups = m_cms.readResources(
            m_state.m_path,
            CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(
                OpenCms.getResourceManager().getResourceType(
                    CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME)));
        for (CmsResource elementGroup : elementGroups) {
            if (!checkTemplate(elementGroup)) {
                continue;
            }
            processElementGroup(elementGroup);
        }
        for (String type : OpenCms.getADEManager().getContentTypeNames(false)) {
            addTypeInfo(type);
        }

        long end = System.currentTimeMillis();

        for (String type : m_state.m_types.keySet()) {
            Multimap<CmsUUID, CmsUUID> usage = m_state.m_typeUsage.get(type);
            if (usage != null) {
                m_state.m_types.get(type).setUsageCount(usage.values().size());
            }
        }
        LinkedHashMap<String, TypeBean> typesSorted = new LinkedHashMap<>();
        m_state.m_types.values().stream().sorted(
            (a, b) -> Integer.compare(b.getUsageCount(), a.getUsageCount())).forEach(
                type -> typesSorted.put(type.getName(), type));
        m_state.m_types = typesSorted;
        LOG.info("Processed " + pages.size() + " pages, took " + (end - start) + "ms");
        return m_state;

    }

    /**
     * Adds the entry for a specific content type usage in a page.
     *
     * @param typeName the type name
     * @param pageId the page id
     * @param formatterId the formatter id
     */
    private void addEntry(String typeName, CmsUUID pageId, CmsUUID formatterId) {

        m_state.m_typeUsage.computeIfAbsent(typeName, k -> ArrayListMultimap.create()).put(formatterId, pageId);

    }

    /**
     * Adds the resource.
     *
     * @param pageResource the page resource
     */
    private void addResource(CmsResource pageResource) {

        m_state.m_pages.put(
            pageResource.getStructureId(),
            new ResourceBean(pageResource.getStructureId(), m_cms.getSitePath(pageResource)));
    }

    /**
     * Adds the type info.
     *
     * @param type the type
     */
    private void addTypeInfo(String type) {

        if (m_state.m_types.get(type) != null) {
            return;
        }
        int count = -1;
        try {
            List<CmsResource> resources = m_cms.readResources(
                m_state.m_path,
                CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(OpenCms.getResourceManager().getResourceType(type)),
                true);
            count = resources.size();
            String key = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type).getKey();
            String label = OpenCms.getWorkplaceManager().getMessages(m_locale).key(key);
            m_state.m_types.put(type, new TypeBean(type, label, count));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /**
     * If the container element is a (legacy) dynamic function, add it to map of function usages.
     *
     * @param pageResource the current page
     * @param element the container element
     */
    private void checkFunction(CmsResource pageResource, CmsContainerElementBean element) {

        if (OpenCms.getResourceManager().matchResourceType("function", element.getResource().getTypeId())) {
            m_state.m_functionUsage.computeIfAbsent(element.getResource().getRootPath(), p -> new HashSet<>()).add(
                m_cms.getSitePath(pageResource));

        }
    }

    /**
     * Checks that the template property of the page matches the template regex.<p>
     *
     * @param page the page to check
     * @return true if the template matches the template regex
     */
    private boolean checkTemplate(CmsResource page) {

        try {
            CmsProperty templateProp = m_cms.readPropertyObject(page, "template", true);
            String templateValue = templateProp.getValue();
            if (templateValue == null) {
                templateValue = "";
            }
            return m_templatePattern.matcher(templateValue).matches();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Process element group.
     *
     * @param groupResource the group resource
     * @throws CmsException if something goes wrong
     */
    private void processElementGroup(CmsResource groupResource) throws CmsException {

        addResource(groupResource);

        CmsXmlGroupContainer groupXml = CmsXmlGroupContainerFactory.unmarshal(m_cms, m_cms.readFile(groupResource));
        CmsGroupContainerBean group = groupXml.getGroupContainer(m_cms);

        for (CmsContainerElementBean element : group.getElements()) {
            try {
                element.initResource(m_cms);
                checkFunction(groupResource, element);
                addEntry(element.getTypeName(), groupResource.getStructureId(), UNKNOWN_FORMATTER);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

    }

    /**
     * Process page.
     *
     * @param pageResource the page resource
     * @throws CmsException the cms exception
     */
    private void processPage(CmsResource pageResource) throws CmsException {

        LOG.debug("processing page " + pageResource.getRootPath());

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(
            m_cms,
            pageResource.getRootPath());
        addResource(pageResource);
        CmsXmlContainerPage pageXml = CmsXmlContainerPageFactory.unmarshal(m_cms, m_cms.readFile(pageResource));
        CmsContainerPageBean page = pageXml.getContainerPage(m_cms);
        for (CmsContainerBean container : page.getContainers().values()) {
            if (m_state.m_excludedContainers.contains(container.getName())) {
                continue;
            }
            for (CmsContainerElementBean element : container.getElements()) {
                try {
                    element.initResource(m_cms);
                    checkFunction(pageResource, element);
                    Map<String, String> settings = element.getIndividualSettings();
                    String formatterRef = settings.get(CmsFormatterConfig.FORMATTER_SETTINGS_KEY + container.getName());
                    if (formatterRef == null) {
                        for (String key : settings.keySet()) {
                            if (key.startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)) {
                                formatterRef = settings.get(key);
                            }
                        }
                    }
                    I_CmsFormatterBean formatter = config.findFormatter(formatterRef, /*nowarn=*/true);
                    CmsUUID formatterId = UNKNOWN_FORMATTER;
                    if (formatter != null) {
                        formatterId = new CmsUUID(formatter.getId());
                        FormatterBean bean = new FormatterBean(
                            formatterId,
                            formatter.getLocation(),
                            formatter.getKey(),
                            formatter.getNiceName(m_locale));
                        m_state.m_formatters.putIfAbsent(formatterId, bean);
                    } else {
                        m_state.m_formatters.putIfAbsent(
                            formatterId,
                            new FormatterBean(UNKNOWN_FORMATTER, "unknown", null, "Unknown formatter"));
                    }
                    addEntry(element.getTypeName(), pageResource.getStructureId(), formatterId);
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }
}
