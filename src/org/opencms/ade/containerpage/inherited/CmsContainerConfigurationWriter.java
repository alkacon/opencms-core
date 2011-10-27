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

import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_HIDDEN;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_KEY;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_NEWELEMENT;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_ORDERKEY;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_VISIBLE;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class CmsContainerConfigurationWriter {

    private Map<String, CmsXmlContentProperty> m_propertyConfig = new HashMap<String, CmsXmlContentProperty>();

    public byte[] serialize(Map<Locale, Map<String, CmsContainerConfiguration>> data, String encoding)
    throws CmsException {

        throw new NotImplementedException();
    }

    public Element serializeSingleConfiguration(CmsObject cms, String name, CmsContainerConfiguration config)
    throws DocumentException {

        String emptyRoot = "<Configuration></Configuration>";
        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(emptyRoot));
        Element root = document.getRootElement();
        List<String> ordering = config.getOrdering();
        for (String orderKey : ordering) {
            root.addElement(N_ORDERKEY).addCDATA(orderKey);
        }
        List<String> visibles = new ArrayList<String>();
        List<String> invisibles = new ArrayList<String>();
        for (String key : config.getVisibility().keySet()) {
            Boolean value = config.getVisibility().get(key);
            if (value.booleanValue()) {
                visibles.add(key);
            } else {
                invisibles.add(key);
            }
        }
        for (String visible : visibles) {
            root.addElement(N_VISIBLE).addCDATA(visible);
        }
        for (String invisible : invisibles) {
            root.addElement(N_HIDDEN).addCDATA(invisible);
        }
        for (Map.Entry<String, CmsContainerElementBean> entry : config.getNewElements().entrySet()) {
            String key = entry.getKey();
            CmsContainerElementBean elementBean = entry.getValue();
            CmsUUID structureId = elementBean.getId();
            Map<String, String> settings = elementBean.getIndividualSettings();
            Element newElementElement = root.addElement(N_NEWELEMENT);
            newElementElement.addElement(N_KEY).addCDATA(key);
            Element elementElement = newElementElement.addElement("Element");
            Element linkElement = elementElement.addElement("Uri").addElement("link");
            linkElement.addAttribute("type", "STRONG");
            linkElement.addElement("uuid").addText(structureId.toString());
            // TODO: use correct property definition for resource type
            CmsXmlContentPropertyHelper.saveProperties(cms, elementElement, settings, m_propertyConfig);
        }
        return root;
    }

    public void setPropertyConfiguration(Map<String, CmsXmlContentProperty> propertyConfig) {

        m_propertyConfig = propertyConfig;
    }

}
