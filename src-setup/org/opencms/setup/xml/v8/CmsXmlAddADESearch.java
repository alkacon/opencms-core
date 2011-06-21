/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup.xml.v8;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.search.CmsVfsIndexer;
import org.opencms.search.documents.CmsDocumentContainerPage;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.galleries.CmsGalleryDocumentXmlContent;
import org.opencms.search.galleries.CmsGalleryDocumentXmlPage;
import org.opencms.search.galleries.CmsGallerySearchAnalyzer;
import org.opencms.search.galleries.CmsGallerySearchFieldConfiguration;
import org.opencms.search.galleries.CmsGallerySearchFieldMapping;
import org.opencms.search.galleries.CmsGallerySearchIndex;
import org.opencms.setup.xml.A_CmsXmlSearch;
import org.opencms.setup.xml.CmsSetupXmlHelper;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the gallery search nodes.<p>
 * 
 * @since 8.0.0
 */
public class CmsXmlAddADESearch extends A_CmsXmlSearch {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add the ADE containerpage and gallery search nodes";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            if (xpath.equals(getXPathsToUpdate().get(0))) {
                // create doc type
                createDocType(
                    document,
                    xpath,
                    "xmlcontent-galleries",
                    CmsGalleryDocumentXmlContent.class,
                    new String[] {},
                    new String[] {"xmlcontent-galleries"});
            } else if (xpath.equals(getXPathsToUpdate().get(1))) {
                // create doc type
                createDocType(
                    document,
                    xpath,
                    "xmlpage-galleries",
                    CmsGalleryDocumentXmlPage.class,
                    new String[] {"text/html"},
                    new String[] {"xmlpage-galleries"});
            } else if (xpath.equals(getXPathsToUpdate().get(2))) {
                // create analyzer
                createAnalyzer(document, xpath, CmsGallerySearchAnalyzer.class, "all");
            } else if (xpath.equals(getXPathsToUpdate().get(3))) {
                // create doc type
                createIndex(
                    document,
                    xpath,
                    CmsGallerySearchIndex.class,
                    CmsGallerySearchIndex.GALLERY_INDEX_NAME,
                    "offline",
                    "Offline",
                    "all",
                    "gallery_fields",
                    new String[] {"gallery_source"});
            } else if (xpath.equals(getXPathsToUpdate().get(4))) {
                // create doc type
                createIndexSource(document, xpath, "gallery_source", CmsVfsIndexer.class, new String[] {
                    "/sites/",
                    "/shared/"}, new String[] {
                    "xmlpage-galleries",
                    "xmlcontent-galleries",
                    "jsp",
                    "page",
                    "text",
                    "pdf",
                    "rtf",
                    "html",
                    "msword",
                    "msexcel",
                    "mspowerpoint",
                    "image",
                    "generic",
                    "openoffice"});
            } else if (xpath.equals(getXPathsToUpdate().get(5))) {
                // create field config
                CmsSearchFieldConfiguration fieldConf = new CmsSearchFieldConfiguration();
                fieldConf.setName("gallery_fields");
                fieldConf.setDescription("The standard OpenCms search index field configuration.");
                CmsSearchField field = new CmsSearchField();
                // <field name="content" store="compress" index="true" excerpt="true">
                field.setName("content");
                field.setStored("compress");
                field.setIndexed("true");
                field.setInExcerpt("true");
                // <mapping type="content" />
                CmsSearchFieldMapping mapping = new CmsSearchFieldMapping();
                mapping.setType("content");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="title-key" store="true" index="untokenized" boost="0.0">
                field = new CmsSearchField();
                field.setName("title-key");
                field.setStored("true");
                field.setIndexed("untokenized");
                field.setBoost("0.0");
                // <mapping type="property">Title</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("property");
                mapping.setParam("Title");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="title" store="false" index="true">
                field = new CmsSearchField();
                field.setName("title");
                field.setStored("false");
                field.setIndexed("true");
                // <mapping type="property">Title</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("property");
                mapping.setParam("Title");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="description" store="true" index="true">
                field = new CmsSearchField();
                field.setName("description");
                field.setStored("true");
                field.setIndexed("true");
                // <mapping type="property">Description</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("property");
                mapping.setParam("Description");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="meta" store="false" index="true">
                field = new CmsSearchField();
                field.setName("meta");
                field.setStored("false");
                field.setIndexed("true");
                // <mapping type="property">Title</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("property");
                mapping.setParam("Title");
                field.addMapping(mapping);
                // <mapping type="property">Description</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("property");
                mapping.setParam("Description");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_dateExpired" store="true" index="untokenized">
                field = new CmsSearchField();
                field.setName("res_dateExpired");
                field.setStored("true");
                field.setIndexed("untokenized");
                // <mapping type="attribute">dateExpired</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("dateExpired");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_dateReleased" store="true" index="untokenized">
                field = new CmsSearchField();
                field.setName("res_dateReleased");
                field.setStored("true");
                field.setIndexed("untokenized");
                // <mapping type="attribute">dateReleased</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("dateReleased");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_length" store="true" index="untokenized">
                field = new CmsSearchField();
                field.setName("res_length");
                field.setStored("true");
                field.setIndexed("untokenized");
                // <mapping type="attribute">length</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("length");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_state" store="true" index="untokenized">
                field = new CmsSearchField();
                field.setName("res_state");
                field.setStored("true");
                field.setIndexed("untokenized");
                // <mapping type="attribute">state</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("state");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_structureId" store="true" index="false">
                field = new CmsSearchField();
                field.setName("res_structureId");
                field.setStored("true");
                field.setIndexed("false");
                // <mapping type="attribute">structureId</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("structureId");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_userCreated" store="true" index="untokenized">
                field = new CmsSearchField();
                field.setName("res_userCreated");
                field.setStored("true");
                field.setIndexed("untokenized");
                // <mapping type="attribute">userCreated</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("userCreated");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_userLastModified" store="true" index="untokenized">
                field = new CmsSearchField();
                field.setName("res_userLastModified");
                field.setStored("true");
                field.setIndexed("untokenized");
                // <mapping type="attribute">userLastModified</mapping>
                mapping = new CmsSearchFieldMapping();
                mapping.setType("attribute");
                mapping.setParam("userLastModified");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="res_locales" store="true" index="true" analyzer="WhitespaceAnalyzer">
                field = new CmsSearchField();
                field.setName("res_locales");
                field.setStored("true");
                field.setIndexed("true");
                try {
                    field.setAnalyzer("WhitespaceAnalyzer");
                } catch (Exception e) {
                    // ignore
                    e.printStackTrace();
                }
                // <mapping type="dynamic" class="org.opencms.search.galleries.CmsGallerySearchFieldMapping">res_locales</mapping>
                mapping = new CmsGallerySearchFieldMapping();
                mapping.setType("dynamic");
                mapping.setParam("res_locales");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="additional_info" store="true" index="false">
                field = new CmsSearchField();
                field.setName("additional_info");
                field.setStored("true");
                field.setIndexed("false");
                // <mapping type="dynamic" class="org.opencms.search.galleries.CmsGallerySearchFieldMapping">additional_info</mapping>
                mapping = new CmsGallerySearchFieldMapping();
                mapping.setType("dynamic");
                mapping.setParam("additional_info");
                field.addMapping(mapping);
                fieldConf.addField(field);
                // <field name="container_types" store="true" index="true" analyzer="WhitespaceAnalyzer">
                field = new CmsSearchField();
                field.setName("container_types");
                field.setStored("true");
                field.setIndexed("true");
                try {
                    field.setAnalyzer("WhitespaceAnalyzer");
                } catch (Exception e) {
                    // ignore
                    e.printStackTrace();
                }
                // <mapping type="dynamic" class="org.opencms.search.galleries.CmsGallerySearchFieldMapping">container_types</mapping>
                mapping = new CmsGallerySearchFieldMapping();
                mapping.setType("dynamic");
                mapping.setParam("container_types");
                field.addMapping(mapping);
                fieldConf.addField(field);
                createFieldConfig(document, xpath, fieldConf, CmsGallerySearchFieldConfiguration.class);
            } else if (xpath.equals(getXPathsToUpdate().get(6))) {
                // create doc type
                createDocType(
                    document,
                    xpath,
                    "containerpage",
                    CmsDocumentContainerPage.class,
                    new String[] {"text/html"},
                    new String[] {"containerpage"});
            } else if (xpath.equals(getXPathsToUpdate().get(7))) {
                CmsSetupXmlHelper.setValue(document, xpath + "/text()", "containerpage");
            } else if (xpath.equals(getXPathsToUpdate().get(8))) {
                CmsSetupXmlHelper.setValue(document, xpath + "/text()", "openoffice");
            }
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/search
        return new StringBuffer("/").append(CmsConfigurationManager.N_ROOT).append("/").append(
            CmsSearchConfiguration.N_SEARCH).toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            m_xpaths = new ArrayList<String>();
            // /opencms/search/documenttypes/documenttype[name='xmlcontent-galleries']    (0)
            StringBuffer xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_DOCUMENTTYPES);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_DOCUMENTTYPE);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='xmlcontent-galleries']");
            m_xpaths.add(xp.toString());
            // /opencms/search/documenttypes/documenttype[name='xmlpage-galleries']       (1)
            xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_DOCUMENTTYPES);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_DOCUMENTTYPE);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='xmlpage-galleries']");
            m_xpaths.add(xp.toString());
            // /opencms/search/analyzers/analyzer[class='org.opencms.search.galleries.CmsGallerySearchAnalyzer']  (2)
            xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_ANALYZERS);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_ANALYZER);
            xp.append("[");
            xp.append(CmsSearchConfiguration.N_CLASS);
            xp.append("='").append(CmsGallerySearchAnalyzer.class.getName()).append("']");
            m_xpaths.add(xp.toString());
            // /opencms/search/indexes/index[name='ADE Gallery Index']   (3)
            xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEXES);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEX);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='").append(CmsGallerySearchIndex.GALLERY_INDEX_NAME).append("']");
            m_xpaths.add(xp.toString());
            // /opencms/search/indexsources/indexsource[name='gallery_source']    (4)
            xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEXSOURCES);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEXSOURCE);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='gallery_source']");
            m_xpaths.add(xp.toString());
            // /opencms/search/fieldconfigurations/fieldconfiguration[name='gallery_fields']  (5)
            xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_FIELDCONFIGURATIONS);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_FIELDCONFIGURATION);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='gallery_fields']");
            m_xpaths.add(xp.toString());

            // /opencms/search/documenttypes/documenttype[name='containerpage']   (6)
            xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_DOCUMENTTYPES);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_DOCUMENTTYPE);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='containerpage']");
            m_xpaths.add(xp.toString());

            // /opencms/search/indexsources/indxsource[name='source1']/documenttypes_indexed/name[text()='containerpage']
            xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEXSOURCES);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEXSOURCE);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='source1']");
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_DOCUMENTTYPES_INDEXED);
            xp.append("/");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("[text()='containerpage']");
            m_xpaths.add(xp.toString());

            // /opencms/search/indexsources/indxsource[name='source1']/documenttypes_indexed/name[text()='openoffice']
            xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEXSOURCES);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEXSOURCE);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='source1']");
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_DOCUMENTTYPES_INDEXED);
            xp.append("/");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("[text()='openoffice']");
            m_xpaths.add(xp.toString());

        }
        return m_xpaths;
    }

}