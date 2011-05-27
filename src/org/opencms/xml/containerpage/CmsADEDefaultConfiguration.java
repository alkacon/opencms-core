/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/CmsADEDefaultConfiguration.java,v $
 * Date   : $Date: 2011/05/27 10:00:38 $
 * Version: $Revision: 1.32 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.ade.config.CmsContainerPageConfigurationData;
import org.opencms.ade.config.CmsSitemapConfigurationData;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

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
 * @version $Revision: 1.32 $ 
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

    /**
     * The default constructor.<p>
     */
    public CmsADEDefaultConfiguration() {

        // do nothing 
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#createNewElement(CmsObject, String, ServletRequest, String, java.util.Locale)
     */
    public CmsResource createNewElement(
        CmsObject cms,
        String cntPageUri,
        ServletRequest request,
        String type,
        Locale locale) throws CmsException {

        CmsContainerPageConfigurationData config = OpenCms.getADEConfigurationManager().getContainerPageConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(cntPageUri));
        CmsConfigurationItem item = config.getTypeConfiguration().get(type);
        item.getLazyFolder().getOrCreateFolder(cms);
        String newFileName = getNextNewFileName(cms, cntPageUri, request, type);
        cms.copyResource(cms.getSitePath(item.getSourceFile()), newFileName);
        CmsResource resource = cms.readResource(newFileName);
        if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
            CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, resource, request);
            if (!xmlContent.hasLocale(locale)) {

                Locale copyLocale = xmlContent.getBestMatchingLocale(locale);
                if (copyLocale == null) {
                    copyLocale = OpenCms.getLocaleManager().getDefaultLocale(cms, resource);
                }
                cms.lockResourceTemporary(newFileName);
                if (xmlContent.hasLocale(copyLocale)) {
                    xmlContent.copyLocale(copyLocale, locale);
                } else {
                    xmlContent.addLocale(cms, locale);
                }
                CmsFile file = xmlContent.getFile();
                file.setContents(xmlContent.marshal());
                cms.writeFile(file);
                resource = cms.readResource(newFileName);
            }
            cms.unlockResource(newFileName);
        }

        return resource;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getCreatableElements(CmsObject, String, ServletRequest)
     */
    public Collection<CmsResource> getCreatableElements(CmsObject cms, String cntPageUri, ServletRequest request)
    throws CmsException {

        CmsContainerPageConfigurationData configData = OpenCms.getADEConfigurationManager().getContainerPageConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(cntPageUri));
        return configData.getNewElements(cms);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getFavoriteListMaxSize(CmsObject)
     */
    public int getFavoriteListMaxSize(CmsObject cms) {

        Integer maxElems = (Integer)cms.getRequestContext().getCurrentUser().getAdditionalInfo(
            ADDINFO_ADE_FAVORITE_LIST_SIZE);
        if (maxElems == null) {
            maxElems = new Integer(DEFAULT_FAVORITE_LIST_SIZE);
        }
        return maxElems.intValue();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getFormattersForResource(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResource)
     */
    public CmsFormatterConfiguration getFormattersForResource(
        CmsObject cms,
        String containerPageRootPath,
        CmsResource res) throws CmsException {

        I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(res);
        String resTypeName = resType.getTypeName();
        CmsContainerPageConfigurationData config = OpenCms.getADEConfigurationManager().getContainerPageConfiguration(
            cms,
            containerPageRootPath);
        CmsFormatterConfiguration result = config.getFormatterConfiguration().get(resTypeName);
        if (result == null) {
            result = resType.getFormattersForResource(cms, res);
        }
        return result;
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

        CmsSitemapConfigurationData sitemapConfig = OpenCms.getADEConfigurationManager().getSitemapConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(uri));
        return sitemapConfig.getMaxDepth();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getNextNewFileName(CmsObject, String, ServletRequest, String)
     */
    public synchronized String getNextNewFileName(CmsObject cms, String cntPageUri, ServletRequest request, String type)
    throws CmsException {

        CmsContainerPageConfigurationData cntPageConfig = OpenCms.getADEConfigurationManager().getContainerPageConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(cntPageUri));
        Map<String, CmsConfigurationItem> typeConfig = cntPageConfig.getTypeConfiguration();
        CmsConfigurationItem item = typeConfig.get(type);
        CmsResource folderRes = item.getLazyFolder().getFolder(cms);
        String destination = cms.getSitePath(folderRes) + item.getPattern();
        return OpenCms.getResourceManager().getNameGenerator().getNewFileName(cms, destination, 5);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getRecentListMaxSize(CmsObject)
     */
    public int getRecentListMaxSize(CmsObject cms) {

        Integer maxElems = (Integer)cms.getRequestContext().getCurrentUser().getAdditionalInfo(
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

        CmsContainerPageConfigurationData configData = OpenCms.getADEConfigurationManager().getContainerPageConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(cntPageUri));
        return configData.getSearchableElements(cms);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getSearchPageSize(CmsObject)
     */
    public int getSearchPageSize(CmsObject cms) {

        Integer maxElems = (Integer)cms.getRequestContext().getCurrentUser().getAdditionalInfo(
            ADDINFO_ADE_SEARCH_PAGE_SIZE);
        if (maxElems == null) {
            maxElems = new Integer(DEFAULT_SEARCH_PAGE_SIZE);
        }
        return maxElems.intValue();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#init(org.opencms.file.CmsObject)
     */
    public void init(CmsObject adminCms) {

        if (m_adminCms == null) {
            m_adminCms = adminCms;
        }
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#isCreatableType(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public boolean isCreatableType(CmsObject cms, String currentUri, String typeName) throws CmsException {

        CmsContainerPageConfigurationData config = OpenCms.getADEConfigurationManager().getContainerPageConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(currentUri));
        CmsConfigurationItem item = config.getTypeConfiguration().get(typeName);
        return (item != null) && CmsContainerPageConfigurationData.isCreatableType(cms, typeName, item);
    }

}
