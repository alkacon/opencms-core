/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapManager.java,v $
 * Date   : $Date: 2011/02/02 07:37:52 $
 * Version: $Revision: 1.74 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.sitemap;

import org.opencms.adeconfig.CmsSitemapConfigurationData;
import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsConfigurationItem;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;
import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Sitemap Manager.<p>
 * 
 * Provides all relevant functions for using the sitemap.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.74 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapManager {

    /** Property name constants. */
    public enum Property {

        /** <code>externalRedirect</code> property name. */
        externalRedirect("externalRedirect"),
        /** <code>internalRedirect</code> property name. */
        internalRedirect("internalRedirect"),
        /** <code>isRedirect</code> property name. */
        isRedirect("isRedirect"),
        /** <code>navigation</code> property name. */
        navigation("navigation"),
        /** <code>sitemap</code> property name. */
        sitemap("sitemap"),

        /** <code>template</code> property name. */
        template("template"),
        /** <code>template-inhertited</code> property name. */
        templateInherited("template-inherited");

        /** The name of the property. */
        private final String m_name;

        /**
         * Default constructor.<p>
         * 
         * @param name the name of the property
         */
        private Property(String name) {

            m_name = name;
        }

        /**
         * Returns the name of the property.<p>
         * 
         * @return the name of the property
         */
        public String getName() {

            return m_name;
        }

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {

            return m_name;
        }
    }

    /** Request attribute name constant for the current sitemap entry bean. */
    public static final String ATTR_SITEMAP_ENTRY = "__currentSitemapEntry";

    /** Path to the default sitemap config. */
    public static final String PATH_SITEMAP_DEFAULT_CONFIG = "/system/modules/org.opencms.ade.sitemap/config/sitemap.config";

    /** The path to the sitemap editor jsp. */
    public static final String PATH_SITEMAP_EDITOR_JSP = "/system/modules/org.opencms.ade.sitemap/sitemap.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapManager.class);

    /** The internal admin CmsObject. */
    private CmsObject m_adminCms;

    /** The detail page finder. */
    private I_CmsDetailPageFinder m_detailPageFinder = new CmsSitemapDetailPageFinder();

    /** Lazy initialized sitemap type id. */
    private int m_sitemapTypeId;

    /**
     * Creates a new sitemap manager.<p>
     * 
     * @param adminCms The admin context
     * @param memoryMonitor the memory monitor instance
     * @param systemConfiguration the system configuration
     */
    public CmsSitemapManager(
        CmsObject adminCms,
        CmsMemoryMonitor memoryMonitor,
        CmsSystemConfiguration systemConfiguration) {

        m_adminCms = adminCms;
        // initialize the sitemap cache
        CmsSitemapCacheSettings cacheSettings = systemConfiguration.getSitemapCacheSettings();
        if (cacheSettings == null) {
            cacheSettings = new CmsSitemapCacheSettings();
        }
        CmsVfsMemoryObjectCache structureIdCache = new CmsVfsMemoryObjectCache();

    }

    //    /**
    //     * Creates a dummy root entry for a sub-sitemap from a given sitemap entry.<p>
    //     * 
    //     * @param cms the CmsObject to use for VFS operations 
    //     * @param entry the sitemap entry from which to create the dummy entry 
    //
    //     * @return a dummy sub-sitemap root
    //     */
    //    public static CmsInternalSitemapEntry copyAsSubSitemapRoot(CmsObject cms, CmsInternalSitemapEntry entry) {
    //
    //        CmsInternalSitemapEntry clone = new CmsInternalSitemapEntry(
    //            entry.getId(),
    //            "",
    //            entry.getStructureId(),
    //            "",
    //            entry.getTitle(),
    //            false,
    //            entry.getNewProperties(),
    //            new ArrayList<CmsInternalSitemapEntry>(),
    //            entry.getContentId(),
    //            entry.getContentName());
    //
    //        clone.setRuntimeInfo(entry.getSitePath(cms), 0, new HashMap<String, CmsComputedPropertyValue>());
    //        return clone;
    //    }

    /**
     * Returns the navigation URI from a given request.<p>
     * 
     * @param cms the current CMS context
     * @param request the current request 
     *  
     * @return the current uri for the navigation
     */
    public static String getNavigationUri(CmsObject cms, HttpServletRequest request) {

        CmsSitemapEntry sitemap = OpenCms.getSitemapManager().getRuntimeInfo(request);
        if (sitemap == null) {
            return cms.getRequestContext().getUri();
        }
        return sitemap.getSitePath(cms);
    }

    /**
     * Creates a new container page for a given sitemap entry.<p>
     * 
     * @param cms the current CMS context 
     * @param sitemapUri the sitemap URI 
     * @param title the title of the sitemap entry  
     * @param sitePath the path of the sitemap entry 
     * 
     * @return the newly created container page resource
     * 
     * @throws CmsException if something goes wrong 
     */
    @Deprecated
    public CmsResource createPage(CmsObject cms, String sitemapUri, String title, String sitePath) throws CmsException {

        //TODO: remove this method
        throw new RuntimeException("error: createPage has been removed");
    }

    /**
     * Finds the entry point to a sitemap.<p>
     * 
     * @param cms the CMS context
     * @param openPath the resource path to find the sitemap to
     * 
     * @return the sitemap entry point
     * 
     * @throws CmsException
     */
    public String findEntryPoint(CmsObject cms, String openPath) throws CmsException {

        String openRootPath = cms.getRequestContext().addSiteRoot(openPath);
        CmsResource entryPoint = OpenCms.getADEConfigurationManager().getEntryPoint(cms, openRootPath);
        String result = cms.getSitePath(entryPoint);
        return result;
    }

    /**
     * The resource to copy for a new sitemap entry page.<p>
     * 
     * @param cms the current CMS context 
     * @param sitemapUri the sitemap URI
     *  
     * @return the copy resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource getCopyPage(CmsObject cms, String sitemapUri) throws CmsException {

        CmsSitemapConfigurationData config = OpenCms.getADEConfigurationManager().getSitemapConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(sitemapUri));
        Map<String, CmsConfigurationItem> typeConfig = config.getTypeConfiguration();
        CmsConfigurationItem item = typeConfig.get(CmsResourceTypeXmlContainerPage.getStaticTypeName());
        return item.getSourceFile();
    }

    /**
     * Returns the default template for the given sitemap, or null if there is none.<p>
     * 
     * @param cms the CmsObject to use for VFS operations
     * @param sitemapUri the URI of the sitemap
     * @param request the servlet request
     * 
     * @return the default template
     *  
     * @throws CmsException if something goes wrong 
     */
    public CmsResource getDefaultTemplate(CmsObject cms, String sitemapUri, ServletRequest request) throws CmsException {

        CmsProperty prop = cms.readPropertyObject(sitemapUri, CmsPropertyDefinition.PROPERTY_TEMPLATE, true);
        String templatePath = prop.getValue();
        try {
            return cms.readResource(templatePath);
        } catch (CmsException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Returns a list of the descendants of a given sitemap entry.<p>
     * 
     * Whether the list includes the entry itself can be controlled by a boolean parameter.<p>
     * 
     * @param rootEntry the root entry whose descendants should be found
     * @param includeRoot if true, the root entry will be included in the resulting list 
     * 
     * @return a list of descendant sitemap entries 
     */
    public List<CmsInternalSitemapEntry> getDescendants(CmsInternalSitemapEntry rootEntry, boolean includeRoot) {

        List<CmsInternalSitemapEntry> result = new ArrayList<CmsInternalSitemapEntry>();
        LinkedList<CmsInternalSitemapEntry> entriesToProcess = new LinkedList<CmsInternalSitemapEntry>();
        if (includeRoot) {
            entriesToProcess.add(rootEntry);

        } else {
            entriesToProcess.addAll(rootEntry.getSubEntries());
        }
        while (!entriesToProcess.isEmpty()) {
            CmsInternalSitemapEntry currentEntry = entriesToProcess.removeFirst();
            result.add(currentEntry);
            entriesToProcess.addAll(currentEntry.getSubEntries());
        }
        return result;
    }

    /**
     * Returns the descendants of a list of sitemap entries.<p>
     * 
     * Whether the resulting list includes the root entries can be controlled with a boolean parameter.<p>
     * 
     * @param rootEntries the root entries whose descendants should be found 
     * @param includeRoots if true, the original root entries will be included in the result list 
     * 
     * @return the list of descendants of the root entries 
     */
    public List<CmsInternalSitemapEntry> getDescendants(List<CmsInternalSitemapEntry> rootEntries, boolean includeRoots) {

        List<CmsInternalSitemapEntry> result = new ArrayList<CmsInternalSitemapEntry>();
        for (CmsInternalSitemapEntry entry : rootEntries) {
            result.addAll(getDescendants(entry, includeRoots));
        }
        return result;
    }

    /**
     * Gets the detail page finder.
     *
     * @return the detail page finder
     */
    public I_CmsDetailPageFinder getDetailPageFinder() {

        return m_detailPageFinder;
    }

    /**
     * Returns the property configuration for a given resource.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource
     * @param includeNonSchemaProperties if true, the properties defined not in the schema but in a configuration file will also be added to the result 
     * 
     * @return the property configuration
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, CmsXmlContentProperty> getElementPropertyConfiguration(
        CmsObject cms,
        CmsResource resource,
        boolean includeNonSchemaProperties) throws CmsException {

        CmsSitemapConfigurationData sitemapConfig = OpenCms.getADEConfigurationManager().getSitemapConfiguration(
            cms,
            resource.getRootPath());
        Map<String, CmsXmlContentProperty> result = sitemapConfig.getPropertyConfiguration();
        return CmsXmlContentPropertyHelper.copyPropertyConfiguration(result);
    }

    /**
     * Returns the property configuration for a given resource.<p>
     * 
     * @param cms the current cms context
     * @param entryPoint the the sitemap entry point
     * 
     * @return the property configuration
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, CmsXmlContentProperty> getElementPropertyConfiguration(CmsObject cms, String entryPoint)
    throws CmsException {

        CmsSitemapConfigurationData sitemapConfig = OpenCms.getADEConfigurationManager().getSitemapConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(entryPoint));
        return sitemapConfig.getPropertyConfiguration();
    }

    /**
     * Returns the configured export name for a given site root.<p>
     * 
     * @param siteRoot a site root 
     * @return the configured export name for the site root  
     * 
     * @throws CmsException if something goes wrong 
     */
    public String getExportnameForSiteRoot(String siteRoot) throws CmsException {

        //TODO: change the client code to use individual exportname properties 
        return "INSERT_EXPORTNAME_HERE";
    }

    /**
     * Returns the maximum depth of a sitemap.<p>
     * 
     * @param cms the current CMS context 
     * @param resource the sitemap resource 
     * @return the maximum depth for a sitemap 
     * 
     * @throws CmsException if something goes wrong 
     */
    public int getMaxDepth(CmsObject cms, CmsResource resource) throws CmsException {

        CmsSitemapConfigurationData configData = OpenCms.getADEConfigurationManager().getSitemapConfiguration(
            cms,
            resource.getRootPath());
        return configData.getMaxDepth();
    }

    /**
     * Returns the entry which refers to a sub-sitemap with a given URI.
     * 
     * @param cms the current CMS context 
     * @param sitemapUri the URI of the sub-sitemap
     *  
     * @return the parent entry
     *  
     * @throws CmsException if something goes wrong 
     */
    //    public CmsSitemapEntry getParentEntryOfSitemap(CmsObject cms, String sitemapUri) throws CmsException {
    //
    //        Map<String, String> entryPoints = m_cache.getEntryPoints(cms);
    //        String sitemapRootUri = cms.getRequestContext().addSiteRoot(sitemapUri);
    //        String entryPoint = entryPoints.get(sitemapRootUri);
    //        entryPoint = cms.getRequestContext().removeSiteRoot(entryPoint);
    //        CmsSitemapEntry entry = getEntryForUri(cms, entryPoint);
    //        if (entry.isRootEntry()) {
    //            return null;
    //        }
    //        String parentUri = CmsResource.getParentFolder(entryPoint);
    //        return getEntryForUri(cms, parentUri);
    //    }

    /**
     * Returns the properties of a sitemap entry as {@link CmsProperty} instances.<p>
     * 
     * @param entry the sitemap entry whose properties should be returned 
     * 
     * @return a map from property names to {@link CmsProperty} objects 
     */
    public Map<String, CmsProperty> getProperties(CmsSitemapEntry entry) {

        return CmsXmlContentPropertyHelper.createCmsProperties(entry.getComputedProperties());
    }

    /**
     * Reads the current sitemap URI bean from the request.<p>
     * 
     * @param req the servlet request
     * 
     * @return the sitemap URI bean, or <code>null</code> if not found
     */
    public CmsSitemapEntry getRuntimeInfo(ServletRequest req) {

        return (CmsSitemapEntry)req.getAttribute(ATTR_SITEMAP_ENTRY);
    }

    /**
     * Returns the sitemap URI for the given sitemap entry URI.<p>
     * 
     * @param cms the current CMS context
     * @param uri the sitemap entry URI to get the sitemap URI for
     * 
     * @return the sitemap URI for the given sitemap entry URI
     * 
     * @throws CmsException if something goes wrong
     */
    @SuppressWarnings("null")
    public String getSitemapForUri(CmsObject cms, String uri) throws CmsException {

        //TODO: implement this
        throw new RuntimeException("not implemented yet");
    }

    /**
     * Clean up at shutdown time. Only intended to be called at system shutdown.<p>
     * 
     * @see org.opencms.main.OpenCmsCore#shutDown
     */
    public void shutdown() {

    }

    /**
     * Searches a sitemap entry with a given resource structure id.<p>
     * 
     * The search starts from a list of sitemap entries passed as an argument, and does not follow references to sub-sitemaps.<p>
     * 
     * @param rootEntries the entries from which the search should be started 
     * @param resourceId the structure id to search for 
     * 
     * @return a sitemap entry with the given structure id, or null if none were found 
     */
    private CmsInternalSitemapEntry getSitemapEntryByStructureId(
        List<CmsInternalSitemapEntry> rootEntries,
        CmsUUID resourceId) {

        LinkedList<CmsInternalSitemapEntry> entriesToProcess = new LinkedList<CmsInternalSitemapEntry>();
        entriesToProcess.addAll(rootEntries);
        while (!entriesToProcess.isEmpty()) {
            CmsInternalSitemapEntry currentEntry = entriesToProcess.removeFirst();
            if (currentEntry.getStructureId().equals(resourceId)) {
                return currentEntry;
            }
            CmsSimplePropertyValue sitemapProp = currentEntry.getNewProperties().get(
                CmsSitemapManager.Property.sitemap.name());
            if (sitemapProp == null) {
                entriesToProcess.addAll(currentEntry.getSubEntries());
            }
        }
        return null;
    }

    /**
     * Checks whether the given sitemap entry should be included in the list of sub-entries of its parent.<p>
     * 
     * @param cms the current CMS context 
     * @param entry the sitemap entry to check
     *  
     * @return true if the sitemap entry should be included in the list of sub-entries of its parent
     */
    private boolean isValidSubEntry(CmsObject cms, CmsInternalSitemapEntry entry) {

        CmsUUID id = entry.getStructureId();
        if (id != null) {
            return cms.existsResource(id);
        } else {
            Map<String, CmsSimplePropertyValue> props = entry.getNewProperties();
            return props.containsKey(CmsSitemapManager.Property.externalRedirect.getName())
                || props.containsKey(CmsSitemapManager.Property.internalRedirect.getName());
        }
    }
}
