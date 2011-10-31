/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package org.opencms.ade.containerpage.inherited;

import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.containerpage.CmsContainerElementBean;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Document;

import com.google.common.collect.Maps;

/**
 * A helper class for writing inherited container configuration back to a VFS file.<p>
 */
public class CmsContainerConfigurationWriter {

    /**
     * Saves a list of container element beans to a file in the VFS.<p>
     * 
     * @param cms the current CMS context 
     * @param name the name of the configuration to save
     * @param newOrdering true if a new ordering needs to be saved 
     * @param pageResource a container page or folder 
     * @param elements the elements whose data should be saved
     *  
     * @throws CmsException if something goes wrong 
     */
    public void save(
        CmsObject cms,
        String name,
        boolean newOrdering,
        CmsResource pageResource,
        List<CmsContainerElementBean> elements) throws CmsException {

        String encoding = OpenCms.getSystemInfo().getDefaultEncoding();
        try {
            CmsProperty encodingProperty = cms.readPropertyObject(
                pageResource,
                CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                true);
            encoding = CmsEncoder.lookupEncoding(encodingProperty.getValue(), encoding);
        } catch (CmsException e) {
            // ignore 
        }
        cms = OpenCms.initCmsObject(cms);
        cms.getRequestContext().setSiteRoot("");
        String configPath;
        if (pageResource.isFolder()) {
            configPath = CmsStringUtil.joinPaths(pageResource.getRootPath(), CmsContainerConfigurationCache.FILE_NAME);
        } else {
            configPath = CmsStringUtil.joinPaths(
                CmsResource.getParentFolder(pageResource.getRootPath()),
                CmsContainerConfigurationCache.FILE_NAME);
        }
        CmsContainerConfiguration configuration = createConfigurationBean(newOrdering, elements);
        CmsContainerConfigurationParser parser = new CmsContainerConfigurationParser(cms);
        CmsResource configResource = null;
        Map<Locale, Map<String, CmsContainerConfiguration>> oldGroups = null;
        try {
            configResource = cms.readResource(configPath);
            parser.parse(configResource);
        } catch (CmsVfsResourceNotFoundException e) {
            oldGroups = Maps.newHashMap();

        }
        oldGroups = parser.getParsedResults();
        Locale locale = cms.getRequestContext().getLocale();
        Map<String, CmsContainerConfiguration> groupForLocale = oldGroups.get(locale);
        if (groupForLocale == null) {
            groupForLocale = Maps.newHashMap();
            oldGroups.put(locale, groupForLocale);
        }
        groupForLocale.put(name, configuration);
        CmsContainerConfigurationGroup newGroups = new CmsContainerConfigurationGroup(oldGroups);
        Document doc = newGroups.createXml(cms);
        String newContentString = CmsXmlUtils.marshal(doc, encoding);

        byte[] contentBytes;
        try {
            contentBytes = newContentString.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            contentBytes = newContentString.getBytes();
        }
        if (configResource == null) {
            // file didn't exist, so create it 
            int typeId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_CONFIG_TYPE_NAME).getTypeId();
            cms.createResource(configPath, typeId, contentBytes, new ArrayList<CmsProperty>());
        } else {
            CmsFile file = cms.readFile(configResource);
            file.setContents(contentBytes);
            CmsLock lock = cms.getLock(configResource);
            if (lock.isUnlocked() || !lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
                cms.lockResource(configResource);
            }
            cms.writeFile(file);
        }
    }

    /**
     * Converts a list of container elements into a bean which should be saved to the inherited container configuration.<p>
     * 
     * @param newOrdering if true, save a new ordering 
     * @param elements the elements which should be converted 
     * 
     * @return the bean containing the information from the container elements which should be saved 
     */
    protected CmsContainerConfiguration createConfigurationBean(
        boolean newOrdering,
        List<CmsContainerElementBean> elements) {

        Map<String, CmsContainerElementBean> newElements = new HashMap<String, CmsContainerElementBean>();
        List<String> ordering = new ArrayList<String>();
        Map<String, Boolean> visibility = new HashMap<String, Boolean>();
        if (newOrdering) {
            for (CmsContainerElementBean elementBean : elements) {
                CmsInheritanceInfo info = elementBean.getInheritanceInfo();
                ordering.add(info.getKey());
            }
        }
        for (CmsContainerElementBean elementBean : elements) {
            CmsInheritanceInfo info = elementBean.getInheritanceInfo();
            if (info.getVisibility().booleanValue() != info.getParentVisibility()) {
                visibility.put(info.getKey(), new Boolean(info.getVisibility().booleanValue()));
            }
        }

        for (CmsContainerElementBean elementBean : elements) {
            CmsInheritanceInfo info = elementBean.getInheritanceInfo();
            if (info.isNew()) {
                newElements.put(info.getKey(), elementBean);
            }
        }
        CmsContainerConfiguration configuration = new CmsContainerConfiguration(ordering, visibility, newElements);
        return configuration;
    }
}
