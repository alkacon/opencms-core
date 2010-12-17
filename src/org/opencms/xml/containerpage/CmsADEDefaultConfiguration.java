/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/CmsADEDefaultConfiguration.java,v $
 * Date   : $Date: 2010/12/17 08:45:29 $
 * Version: $Revision: 1.12 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFormatterUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsPair;
import org.opencms.util.PrintfFormat;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;

/**
 * Default implementation for the ADE configuration.<p>
 * 
 * List sizes are read from the user additional info, if not set used a fixed value of 10.<p>
 * 
 * New elements are read from a configuration file read by property.<p>
 * 
 * Search types are the same as the new elements.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.12 $ 
 * 
 * @since 7.6 
 */
public class CmsADEDefaultConfiguration implements I_CmsADEConfiguration {

    /** User additional info key constant. */
    public static final String ADDINFO_ADE_FAVORITE_LIST_SIZE = "ADE_FAVORITE_LIST_SIZE";

    /** User additional info key constant. */
    public static final String ADDINFO_ADE_RECENT_LIST_SIZE = "ADE_RECENT_LIST_SIZE";

    /** User additional info key constant. */
    public static final String ADDINFO_ADE_SEARCH_PAGE_SIZE = "ADE_SEARCH_PAGE_SIZE";

    /** Default favorite list size constant. */
    public static final int DEFAULT_FAVORITE_LIST_SIZE = 10;

    /** Default recent list size constant. */
    public static final int DEFAULT_RECENT_LIST_SIZE = 10;

    /** Default search page size constant. */
    public static final int DEFAULT_SEARCH_PAGE_SIZE = 10;

    /** The format used for the macro replacement. */
    public static final String FILE_NUMBER_FORMAT = "%0.4d";

    /** The macro name for new file name patterns. */
    public static final String MACRO_NUMBER = "number";

    /** The log to use (static for performance reasons).<p> */
    @SuppressWarnings("unused")
    private static final Log LOG = CmsLog.getLog(CmsADEDefaultConfiguration.class);

    /** The admin CMS context. */
    private CmsObject m_adminCms;

    /** The property from which the configuration file name should be read. */
    private String m_configFileProperty;

    /** The module configuration reader. */
    private CmsModuleADEConfigProvider m_moduleConfig;

    /**
     * The default constructor.<p>
     */
    public CmsADEDefaultConfiguration() {

        m_configFileProperty = CmsPropertyDefinition.PROPERTY_ADE_CNTPAGE_CONFIG;
    }

    /**
     * Creates a new configuration object which uses a custom property name for looking up the configuration file name.<p>
     * 
     * @param configFileProperty the name of the property containing the configuration file name 
     */
    public CmsADEDefaultConfiguration(String configFileProperty) {

        m_configFileProperty = configFileProperty;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#createNewElement(CmsObject, String, ServletRequest, String)
     */
    @SuppressWarnings("null")
    public CmsResource createNewElement(CmsObject cms, String cntPageUri, ServletRequest request, String type)
    throws CmsException {

        CmsConfigurationParser siteConfig = getConfigurationParser(cms, cntPageUri);
        CmsConfigurationItem item = siteConfig.getTypeConfiguration().get(type);
        if ((item == null) && (m_moduleConfig != null)) {
            CmsConfigurationParser moduleConfig = m_moduleConfig.getConfigurationParser(getAdminCmsObject(cms));
            item = moduleConfig.getTypeConfiguration().get(type);
        }
        String newFileName = getNextNewFileName(cms, cntPageUri, request, type);
        cms.copyResource(cms.getSitePath(item.getSourceFile()), newFileName);
        return cms.readResource(newFileName);
    }

    /**
     * Returns the configuration data for a given XML content.<p>
     * 
     * @param cms the CMS context 
     * @param cntPageUri the URI of the XML content 
     * @return the configuration data for that XML content
     *  
     * @throws CmsException if something goes wrong 
     */
    public CmsConfigurationParser getConfigurationParser(CmsObject cms, String cntPageUri) throws CmsException {

        return getConfigurationParser(cms, cntPageUri, m_configFileProperty);
    }

    /**
     * Returns the configuration parser instance.<p>
     * 
     * @param cms the current CMS context
     * @param cntPageUri the container page URI
     * @param propName the name of the property which should contain the configuration file name 
     * 
     * @return the configuration parser instance
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsConfigurationParser getConfigurationParser(CmsObject cms, String cntPageUri, String propName)
    throws CmsException {

        CmsConfigurationFileFinder finder = new CmsConfigurationFileFinder(propName);
        CmsResource cfg = finder.getConfigurationFile(cms, cntPageUri);
        return CmsConfigurationParser.getParser(cms, cfg);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getCreatableElements(CmsObject, String, ServletRequest)
     */
    public Collection<CmsResource> getCreatableElements(CmsObject cms, String cntPageUri, ServletRequest request)
    throws CmsException {

        CmsConfigurationParser siteConfigParser = getConfigurationParser(cms, cntPageUri);
        LinkedHashSet<CmsResource> resources = new LinkedHashSet<CmsResource>();
        if (m_moduleConfig != null) {
            CmsConfigurationParser moduleConfigParser = m_moduleConfig.getConfigurationParser(getAdminCmsObject(cms));
            resources.addAll(moduleConfigParser.getNewElements(getAdminCmsObject(cms)));
        }
        resources.addAll(siteConfigParser.getNewElements(cms));
        return resources;
    }

    /**
     * Returns the configured export name.<p>
     * 
     * @param cms the current CMS context 
     * @param uri the URI for which the configuration should be retrieved 
     * 
     * @return the configured export name 
     * 
     * @throws CmsException if something goes wrong 
     */
    public String getExportName(CmsObject cms, String uri) throws CmsException {

        CmsConfigurationParser config = getConfigurationParser(cms, uri);
        return config.getExportName();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getFavoriteListMaxSize(CmsObject)
     */
    public int getFavoriteListMaxSize(CmsObject cms) {

        Integer maxElems = (Integer)cms.getRequestContext().currentUser().getAdditionalInfo(
            ADDINFO_ADE_FAVORITE_LIST_SIZE);
        if (maxElems == null) {
            maxElems = new Integer(DEFAULT_FAVORITE_LIST_SIZE);
        }
        return maxElems.intValue();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getFormatterForContainerTypeAndWidth(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String, int)
     */
    public String getFormatterForContainerTypeAndWidth(CmsObject cms, CmsResource res, String cntType, int width)
    throws CmsException {

        I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(res);
        String typeName = resType.getTypeName();

        CmsConfigurationParser parser = getConfigurationParser(cms, cms.getSitePath(res));
        CmsPair<Map<String, String>, Map<Integer, String>> fmtConfig = parser.getFormatterConfigurationForType(typeName);
        if (fmtConfig != null) {
            return CmsFormatterUtil.selectFormatter(fmtConfig.getFirst(), fmtConfig.getSecond(), cntType, width);
        }
        if (m_moduleConfig != null) {
            CmsConfigurationParser moduleConfig = m_moduleConfig.getConfigurationParser(getAdminCmsObject(cms));
            fmtConfig = moduleConfig.getFormatterConfigurationForType(typeName);
            if (fmtConfig != null) {
                return CmsFormatterUtil.selectFormatter(fmtConfig.getFirst(), fmtConfig.getSecond(), cntType, width);
            }
        }
        return OpenCms.getResourceManager().getResourceType(res).getFormatterForContainerTypeAndWidth(
            cms,
            res,
            cntType,
            width);
    }

    /**
     * Returns the maximum sitemap depth.<p>
     * 
     * @param cms the current CMS context 
     * @param uri the URI for which the configuration should be retrieved
     *  
     * @return the maximum sitemap depth
     *  
     * @throws CmsException if something goes wrong 
     */
    public int getMaxDepth(CmsObject cms, String uri) throws CmsException {

        CmsConfigurationParser config = getConfigurationParser(cms, uri);
        return config.getMaxDepth();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getNextNewFileName(CmsObject, String, ServletRequest, String)
     */
    public synchronized String getNextNewFileName(CmsObject cms, String cntPageUri, ServletRequest request, String type)
    throws CmsException {

        CmsConfigurationParser parser = getConfigurationParser(cms, cntPageUri);
        Map<String, CmsConfigurationItem> types = new HashMap<String, CmsConfigurationItem>();
        if (m_moduleConfig != null) {
            CmsConfigurationParser moduleConfig = m_moduleConfig.getConfigurationParser(getAdminCmsObject(cms));
            types.putAll(moduleConfig.getTypeConfiguration());
        }

        types.putAll(parser.getTypeConfiguration());
        CmsConfigurationItem item = types.get(type);
        String destination = cms.getSitePath(item.getFolder()) + item.getPattern();
        return getNewFileName(cms, destination);
    }

    /**
     * Returns a list of properties defined in the configuration file.<p>
     * 
     * @param cms the CMS context 
     * @param cntPageUri the uri of the page
     *  
     * @return the list of properties in the configuration file
     *     
     * @throws CmsException if something goes wrong 
     */
    public List<CmsXmlContentProperty> getProperties(CmsObject cms, String cntPageUri) throws CmsException {

        return getConfigurationParser(cms, cntPageUri).getDefinedProperties();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getRecentListMaxSize(CmsObject)
     */
    public int getRecentListMaxSize(CmsObject cms) {

        Integer maxElems = (Integer)cms.getRequestContext().currentUser().getAdditionalInfo(
            ADDINFO_ADE_RECENT_LIST_SIZE);
        if (maxElems == null) {
            maxElems = new Integer(DEFAULT_RECENT_LIST_SIZE);
        }
        return maxElems.intValue();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getSearchableResourceTypes(CmsObject, String, ServletRequest)
     */
    public Collection<CmsResource> getSearchableResourceTypes(CmsObject cms, String cntPageUri, ServletRequest request)
    throws CmsException {

        return getCreatableElements(cms, cntPageUri, request);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getSearchPageSize(CmsObject)
     */
    public int getSearchPageSize(CmsObject cms) {

        Integer maxElems = (Integer)cms.getRequestContext().currentUser().getAdditionalInfo(
            ADDINFO_ADE_SEARCH_PAGE_SIZE);
        if (maxElems == null) {
            maxElems = new Integer(DEFAULT_SEARCH_PAGE_SIZE);
        }
        return maxElems.intValue();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#init(org.opencms.file.CmsObject, java.lang.String)
     */
    public void init(CmsObject adminCms, String moduleParamKey) {

        if (m_adminCms == null) {
            m_adminCms = adminCms;
            if (moduleParamKey != null) {
                m_moduleConfig = new CmsModuleADEConfigProvider(moduleParamKey);
            }
        }
    }

    /**
     * Returns a new file name for an element to be created based on a pattern.<p>
     * 
     * The pattern consists of a path which may contain the macro %(number), which 
     * will be replaced by the first 5-digit sequence for which the resulting file name is not already
     * used.<p>
     * 
     * Although this method is synchronized, it may still return a used file name in the unlikely
     * case that it is called after a previous call to this method, but before the resulting file name
     * was used to create a file.<p>  
     * 
     * This method was adapted from the method {@link org.opencms.file.collectors.A_CmsResourceCollector}<code>#getCreateInFolder</code>.<p>
     *
     * @param cms the CmsObject used for checking the existence of file names
     * @param pattern the pattern for new files
     * 
     * @return the new file name
     * 
     * @throws CmsException if something goes wrong
     */
    protected String getNewFileName(CmsObject cms, String pattern) throws CmsException {

        // this method was adapted from A_CmsResourceCollector#getCreateInFolder
        pattern = cms.getRequestContext().removeSiteRoot(pattern);
        PrintfFormat format = new PrintfFormat(FILE_NUMBER_FORMAT);
        String folderName = CmsResource.getFolderPath(pattern);
        List<CmsResource> resources = cms.readResources(folderName, CmsResourceFilter.ALL, false);
        // now create a list of all resources that just contains the file names
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < resources.size(); i++) {
            CmsResource resource = resources.get(i);
            result.add(cms.getSitePath(resource));
        }

        String checkFileName, checkTempFileName, number;
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        int j = 0;
        do {
            number = format.sprintf(++j);
            resolver.addMacro(MACRO_NUMBER, number);
            // resolve macros in file name
            checkFileName = resolver.resolveMacros(pattern);
            // get name of the resolved temp file
            checkTempFileName = CmsWorkplace.getTemporaryFileName(checkFileName);
        } while (result.contains(checkFileName) || result.contains(checkTempFileName));
        return checkFileName;
    }

    /**
     * Helper method for getting an admin CMS object with the same project as another CMS object.<p>
     *   
     * @param cms a CMS context 
     * @return an admin CMS object with the same project as cms 
     *  
     * @throws CmsException if something goes wrong 
     */
    private CmsObject getAdminCmsObject(CmsObject cms) throws CmsException {

        CmsObject result = OpenCms.initCmsObject(m_adminCms);
        result.getRequestContext().setCurrentProject(cms.getRequestContext().currentProject());
        return result;
    }

}
