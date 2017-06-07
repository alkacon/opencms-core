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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.xml.v7;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.setup.xml.A_CmsSetupXmlUpdate;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Update the default properties, from 6.2.3 to 7.0.x.<p>
 *
 * @since 6.9.2
 */
public class CmsXmlUpdateDefaultProperties extends A_CmsSetupXmlUpdate {

    /**
     * A simple class for keeping to related strings.<p>
     */
    private static class Pair {

        /** First value. */
        private String m_first;

        /** Second value. */
        private String m_second;

        /**
         * The constructor.<p>
         *
         * @param first the first member
         * @param second the second member
         */
        protected Pair(String first, String second) {

            m_first = first;
            m_second = second;
        }

        /**
         * Returns the first member.<p>
         *
         * @return the first member
         */
        public String getFirst() {

            return m_first;
        }

        /**
         * Returns the second member.<p>
         *
         * @return the second member
         */
        public String getSecond() {

            return m_second;
        }
    }

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /** List of xpaths to remove. */
    private List<String> m_xpathsRemove;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Update default properties";
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getXmlFilename()
     */
    public String getXmlFilename() {

        return CmsWorkplaceConfiguration.DEFAULT_XML_FILE_NAME;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            int index = getXPathsToUpdate().indexOf(xpath);
            if (index > -1) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_NAME,
                    getKeys().get(index).getSecond());
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        return null;
        /*
         // /opencms/workplace/explorertypes/
         StringBuffer xp = new StringBuffer(256);
         xp.append("/").append(CmsConfigurationManager.N_ROOT);
         xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
         xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
         return xp.toString();
         */
    }

    /**
     * Returns a list of keys for creating the new nodes.<p>
     *
     * @return a list of pairs (resource type, property name)
     */
    protected List<Pair> getKeys() {

        List<Pair> keys = new ArrayList<Pair>();
        keys.add(new Pair(CmsResourceTypeFolder.getStaticTypeName(), CmsPropertyDefinition.PROPERTY_TITLE));
        keys.add(new Pair(CmsResourceTypeBinary.getStaticTypeName(), CmsPropertyDefinition.PROPERTY_TITLE));
        keys.add(new Pair(CmsResourceTypePointer.getStaticTypeName(), CmsPropertyDefinition.PROPERTY_TITLE));
        keys.add(new Pair("imagegallery", CmsPropertyDefinition.PROPERTY_TITLE));
        keys.add(new Pair("downloadgallery", CmsPropertyDefinition.PROPERTY_TITLE));
        keys.add(new Pair(CmsResourceTypeImage.getStaticTypeName(), CmsPropertyDefinition.PROPERTY_TITLE));
        keys.add(new Pair(CmsResourceTypeImage.getStaticTypeName(), CmsPropertyDefinition.PROPERTY_DESCRIPTION));
        keys.add(new Pair("xmlcontent", CmsPropertyDefinition.PROPERTY_TITLE));
        keys.add(new Pair("xmlcontent", CmsPropertyDefinition.PROPERTY_DESCRIPTION));
        keys.add(new Pair("xmlcontent", CmsPropertyDefinition.PROPERTY_KEYWORDS));
        keys.add(new Pair(CmsResourceTypePlain.getStaticTypeName(), CmsPropertyDefinition.PROPERTY_TITLE));
        keys.add(new Pair(CmsResourceTypePlain.getStaticTypeName(), CmsPropertyDefinition.PROPERTY_EXPORT));
        keys.add(new Pair(CmsResourceTypeJsp.getStaticTypeName(), CmsPropertyDefinition.PROPERTY_TITLE));
        keys.add(new Pair(CmsResourceTypeJsp.getStaticTypeName(), CmsPropertyDefinition.PROPERTY_EXPORT));
        keys.add(new Pair(CmsResourceTypeJsp.getStaticTypeName(), CmsPropertyDefinition.PROPERTY_CACHE));
        keys.add(new Pair(CmsResourceTypeJsp.getStaticTypeName(), CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING));
        return keys;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToRemove()
     */
    @Override
    protected List<String> getXPathsToRemove() {

        if (m_xpathsRemove == null) {
            // /opencms/workplace/explorertypes/explorertype/editoptions/defaultproperties/property
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EDITOPTIONS);
            xp.append("/").append(CmsWorkplaceConfiguration.N_DEFAULTPROPERTIES);
            xp.append("/").append(I_CmsXmlConfiguration.N_PROPERTY);
            m_xpathsRemove = Collections.singletonList(xp.toString());
        }
        return m_xpathsRemove;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // /opencms/workplace/explorertypes/explorertype[@name='${etype}']/editoptions/defaultproperties/defaultproperty[@name='${pname}']
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='${etype}']/");
            xp.append(CmsWorkplaceConfiguration.N_EDITOPTIONS);
            xp.append("/").append(CmsWorkplaceConfiguration.N_DEFAULTPROPERTIES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_DEFAULTPROPERTY);
            xp.append("[@").append(I_CmsXmlConfiguration.A_NAME);
            xp.append("='${pname}']");

            /*
             folder          Title
             binary          Title
             pointer         Title
             imagegallery    Title
             downloadgallery Title
             image           Title Description
             xmlpage         Title Keywords Description
             xmlcontent      Title Keywords Description
             plain           Title export
             jsp             Title cache content-encoding export
             */

            m_xpaths = new ArrayList<String>();
            Iterator<Pair> it = getKeys().iterator();
            while (it.hasNext()) {
                Pair entry = it.next();
                String eType = entry.getFirst();
                String prop = entry.getSecond();

                Map<String, String> subs = new HashMap<String, String>();
                subs.put("${etype}", eType);
                subs.put("${pname}", prop);
                m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            }
        }
        return m_xpaths;
    }

}