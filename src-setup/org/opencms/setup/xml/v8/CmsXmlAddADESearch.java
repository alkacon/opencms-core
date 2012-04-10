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
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.galleries.CmsGallerySearchAnalyzer;
import org.opencms.search.galleries.CmsGallerySearchFieldConfiguration;
import org.opencms.search.galleries.CmsGallerySearchFieldMapping;
import org.opencms.search.galleries.CmsGallerySearchIndex;
import org.opencms.setup.xml.A_CmsXmlSearch;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.setup.xml.CmsXmlUpdateAction;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Adds the gallery search nodes.<p>
 * 
 * @since 8.0.0
 */
public class CmsXmlAddADESearch extends A_CmsXmlSearch {

    /**
     * Action to add the gallery modules index source.<p>
     */
    public static class CmsAddGalleryModuleIndexSourceAction extends CmsXmlUpdateAction {

        /**
         * @see org.opencms.setup.xml.CmsXmlUpdateAction#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
         */
        @Override
        public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

            Element node = (Element)doc.selectSingleNode("/opencms/search/indexsources");
            if (!node.selectNodes("indexsource[name='gallery_modules_source']").isEmpty()) {
                return false;
            }
            String galleryModulesSource = "            <indexsource>\n"
                + "                <name>gallery_modules_source</name>\n"
                + "                <indexer class=\"org.opencms.search.CmsVfsIndexer\" />\n"
                + "                <resources>\n"
                + "                    <resource>/system/modules/</resource>\n"
                + "                </resources>\n"
                + "                <documenttypes-indexed>\n"
                + "                    <name>xmlcontent-galleries</name>\n"
                + "                </documenttypes-indexed>                \n"
                + "            </indexsource>              \n";
            try {
                Element sourceElem = createElementFromXml(galleryModulesSource);
                node.add(sourceElem);
                return true;
            } catch (DocumentException e) {
                System.err.println("Failed to add gallery_modules_source");
                return false;
            }
        }

    }

    /**
     * An XML update action which adds the /system/galleries folder to the gallery index source. 
     */
    public static class CmsAddIndexSourceResourceAction extends CmsXmlUpdateAction {

        /**
         * @see org.opencms.setup.xml.CmsXmlUpdateAction#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
         */
        @Override
        public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

            if (!forReal) {
                CmsSetupXmlHelper.setValue(
                    doc,
                    "/opencms/search/indexsources/indexsource[name='gallery_source']/resources/resource[text()='/system/galleries/']",
                    "/system/galleries/");
                return true;
            }
            Element indexSourceResources = (Element)doc.selectSingleNode("/opencms/search/indexsources/indexsource[name='gallery_source']/resources");
            if (indexSourceResources == null) {
                return false;
            }
            Element source = (Element)indexSourceResources.selectSingleNode("resource[text()='/system/galleries/']");
            if (source != null) {
                return false;
            }
            Element resourceElement = indexSourceResources.addElement("resource");
            resourceElement.addText("/system/galleries/");
            return true;
        }
    }

    /**
     * Action for updating the office document types in the index sources.<p>
     */
    public static final class CmsIndexSourceTypeUpdateAction extends CmsXmlUpdateAction {

        /**
         * @see org.opencms.setup.xml.CmsXmlUpdateAction#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

            List<Node> nodes = doc.selectNodes("/opencms/search/indexsources/indexsource");
            boolean result = false;
            for (Node node : nodes) {
                if (containsOldType(node)) {
                    result = true;
                    removeTypes(node);
                    addNewTypes(node);
                }
            }
            return result;
        }

        /**
         * Adds new office document types.<p>
         * 
         * @param node the node to add the document types to 
         */
        protected void addNewTypes(Node node) {

            Element element = (Element)(node.selectSingleNode("documenttypes-indexed"));
            for (String type : new String[] {"openoffice", "msoffice-ole2", "msoffice-ooxml"}) {
                element.addElement("name").addText(type);
            }
        }

        /**
         * Checks whether a node contains the old office document types.<p>
         * 
         * @param node the node which should be checked
         *  
         * @return true if the node contains old office document types  
         */
        protected boolean containsOldType(Node node) {

            @SuppressWarnings("unchecked")
            List<Node> nodes = node.selectNodes("documenttypes-indexed/name[text()='msword' or text()='msexcel' or text()='mspowerpoint']");
            return !nodes.isEmpty();
        }

        /**
         * Removes the office document types from a node.<p>
         * 
         * @param node the node from which to remove the document types 
         */
        protected void removeTypes(Node node) {

            @SuppressWarnings("unchecked")
            List<Node> nodes = node.selectNodes("documenttypes-indexed/name[text()='msword' or text()='msexcel' or text()='mspowerpoint' or text()='msoffice-ooxml' or text()='openoffice' or text()='msoffice-ole2']");
            for (Node nodeToRemove : nodes) {
                nodeToRemove.detach();
            }
        }
    }

    /**
     * An XML update action which replaces an element given by an XPath with some other XML element.
     */
    class ElementReplaceAction extends CmsXmlUpdateAction {

        /** 
         * The XML which should be used as a replacement (as a string).
         */
        private String m_replacementXml;

        /**
         * The xpath of the element to replace.
         */
        private String m_xpath;

        /**
         * Creates a new instance.<p>
         * 
         * @param xpath the xpath of the element to replace
         * @param replacementXml the replacement xml 
         */
        public ElementReplaceAction(String xpath, String replacementXml) {

            m_xpath = xpath;
            m_replacementXml = replacementXml;
        }

        /**
         * @see org.opencms.setup.xml.CmsXmlUpdateAction#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
         */
        @Override
        public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

            if (!forReal) {
                return true;
            }
            Node node = doc.selectSingleNode(m_xpath);
            if (node != null) {
                Element parent = node.getParent();
                node.detach();
                try {
                    Element element = createElementFromXml(m_replacementXml);
                    parent.add(element);
                    return true;
                } catch (DocumentException e) {
                    e.printStackTrace(System.out);
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    /** A map from xpaths to XML update actions.<p> */
    private Map<String, CmsXmlUpdateAction> m_actions;

    /**
     * Creates a dom4j element from an XML string.<p>
     * 
     * @param xml the xml string 
     * @return the dom4j element 
     * 
     * @throws DocumentException if the XML parsing fails
     */
    public static org.dom4j.Element createElementFromXml(String xml) throws DocumentException {

        SAXReader reader = new SAXReader();
        Document newNodeDocument = reader.read(new StringReader(xml));
        return newNodeDocument.getRootElement();
    }

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

        CmsXmlUpdateAction action = m_actions.get(xpath);
        if (action == null) {
            return false;
        }
        return action.executeUpdate(document, xpath, forReal);

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

        if (m_actions == null) {
            initActions();
        }
        return new ArrayList<String>(m_actions.keySet());
    }

    /**
     * Builds the xpath for the documenttypes node.<p>
     * 
     * @return the xpath for the documenttypes node 
     */
    private String buildXpathForDoctypes() {

        return getCommonPath() + "/" + CmsSearchConfiguration.N_DOCUMENTTYPES;
    }

    /**
     * Builds an xpath for a document type node in an index source.<p>
     * 
     * @param source the name of the index source 
     * @param doctype the document type 
     * 
     * @return the xpath 
     */
    private String buildXpathForIndexedDocumentType(String source, String doctype) {

        StringBuffer xp = new StringBuffer(256);
        xp.append(getCommonPath());
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_INDEXSOURCES);
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_INDEXSOURCE);
        xp.append("[");
        xp.append(I_CmsXmlConfiguration.N_NAME);
        xp.append("='" + source + "']");
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_DOCUMENTTYPES_INDEXED);
        xp.append("/");
        xp.append(I_CmsXmlConfiguration.N_NAME);
        xp.append("[text()='" + doctype + "']");
        return xp.toString();
    }

    /**
     * Creates an action which adds an indexed type to an index source.<p>
     *  
     * @param type the type which should be indexed
     *  
     * @return the update action 
     */
    private CmsXmlUpdateAction createIndexedTypeAction(final String type) {

        return new CmsXmlUpdateAction() {

            @Override
            public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                Node node = doc.selectSingleNode(xpath);
                if (node != null) {
                    return false;
                }
                CmsSetupXmlHelper.setValue(doc, xpath + "/text()", type);
                return true;

            }
        };
    }

    /**
     * Initializes the map of XML update actions.<p>
     */
    private void initActions() {

        m_actions = new LinkedHashMap<String, CmsXmlUpdateAction>();
        StringBuffer xp;
        CmsXmlUpdateAction action0 = new CmsXmlUpdateAction() {

            @SuppressWarnings("unchecked")
            @Override
            public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                Node node = doc.selectSingleNode(xpath);
                org.dom4j.Element parent = node.getParent();
                int position = parent.indexOf(node);
                parent.remove(node);
                try {
                    parent.elements().add(
                        position,
                        createElementFromXml("      <documenttypes>     \n"
                            + "            <documenttype>\n"
                            + "                <name>generic</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentGeneric</class>\n"
                            + "                <mimetypes/>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>*</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype> \n"
                            + "            <documenttype>\n"
                            + "                <name>html</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentHtml</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>text/html</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>\n"
                            + "            <documenttype>\n"
                            + "                <name>image</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentGeneric</class>\n"
                            + "                <mimetypes/>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>image</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>     \n"
                            + "            <documenttype>\n"
                            + "                <name>jsp</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentPlainText</class>\n"
                            + "                <mimetypes/>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>jsp</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>     \n"
                            + "            <documenttype>\n"
                            + "                <name>pdf</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentPdf</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>application/pdf</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>binary</resourcetype>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>\n"
                            + "            <documenttype>\n"
                            + "                <name>rtf</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentRtf</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>text/rtf</mimetype>\n"
                            + "                    <mimetype>application/rtf</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>binary</resourcetype>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>     \n"
                            + "            <documenttype>\n"
                            + "                <name>text</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentPlainText</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>text/html</mimetype>\n"
                            + "                    <mimetype>text/plain</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype> \n"
                            + "            <documenttype>\n"
                            + "                <name>xmlcontent</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentXmlContent</class>\n"
                            + "                <mimetypes/>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>*</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>\n"
                            + "            <documenttype>\n"
                            + "                <name>containerpage</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentContainerPage</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>text/html</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>containerpage</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>                 \n"
                            + "            <documenttype>\n"
                            + "                <name>xmlpage</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentXmlPage</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>text/html</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>xmlpage</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>\n"
                            + "            <documenttype>\n"
                            + "                <name>xmlcontent-galleries</name>\n"
                            + "                <class>org.opencms.search.galleries.CmsGalleryDocumentXmlContent</class>\n"
                            + "                <mimetypes/>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>xmlcontent-galleries</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype> \n"
                            + "            <documenttype>\n"
                            + "                <name>xmlpage-galleries</name>\n"
                            + "                <class>org.opencms.search.galleries.CmsGalleryDocumentXmlPage</class>\n"
                            + "                <mimetypes />\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>xmlpage-galleries</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>     \n"
                            + "            <documenttype>\n"
                            + "                <name>msoffice-ole2</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentMsOfficeOLE2</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>application/vnd.ms-powerpoint</mimetype>\n"
                            + "                    <mimetype>application/msword</mimetype>     \n"
                            + "                    <mimetype>application/vnd.ms-excel</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>binary</resourcetype>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>                 \n"
                            + "            <documenttype>\n"
                            + "                <name>msoffice-ooxml</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentMsOfficeOOXML</class>\n"
                            + "                <mimetypes>             \n"
                            + "                    <mimetype>application/vnd.openxmlformats-officedocument.wordprocessingml.document</mimetype>\n"
                            + "                    <mimetype>application/vnd.openxmlformats-officedocument.spreadsheetml.sheet</mimetype>\n"
                            + "                    <mimetype>application/vnd.openxmlformats-officedocument.presentationml.presentation</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>binary</resourcetype>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>                 \n"
                            + "            <documenttype>\n"
                            + "                <name>openoffice</name>\n"
                            + "                <class>org.opencms.search.documents.CmsDocumentOpenOffice</class>\n"
                            + "                <mimetypes>\n"
                            + "                    <mimetype>application/vnd.oasis.opendocument.text</mimetype>\n"
                            + "                    <mimetype>application/vnd.oasis.opendocument.spreadsheet</mimetype>\n"
                            + "                </mimetypes>\n"
                            + "                <resourcetypes>\n"
                            + "                    <resourcetype>binary</resourcetype>\n"
                            + "                    <resourcetype>plain</resourcetype>\n"
                            + "                </resourcetypes>\n"
                            + "            </documenttype>\n"
                            + "        </documenttypes>\n"));
                } catch (DocumentException e) {
                    System.out.println("failed to update document types.");
                }
                return true;

            }
        };
        m_actions.put(buildXpathForDoctypes(), action0);
        //
        //=============================================================================================================
        //
        CmsXmlUpdateAction action1 = new CmsXmlUpdateAction() {

            @SuppressWarnings("synthetic-access")
            @Override
            public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                Node node = doc.selectSingleNode(xpath);
                if (node == null) {
                    createAnalyzer(doc, xpath, CmsGallerySearchAnalyzer.class, "all");
                    return true;
                }
                return false;
            }
        };
        xp = new StringBuffer(256);
        xp.append(getCommonPath());
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_ANALYZERS);
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_ANALYZER);
        xp.append("[");
        xp.append(CmsSearchConfiguration.N_CLASS);
        xp.append("='").append(CmsGallerySearchAnalyzer.class.getName()).append("']");
        m_actions.put(xp.toString(), action1);
        //
        //=============================================================================================================
        //
        CmsXmlUpdateAction action2 = new CmsXmlUpdateAction() {

            @SuppressWarnings("synthetic-access")
            @Override
            public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                Node node = doc.selectSingleNode(xpath);
                if (node != null) {
                    node.detach();
                }
                createIndex(
                    doc,
                    xpath,
                    CmsGallerySearchIndex.class,
                    CmsGallerySearchIndex.GALLERY_INDEX_NAME,
                    "offline",
                    "Offline",
                    "all",
                    "gallery_fields",
                    new String[] {"gallery_source", "gallery_modules_source"});
                return true;
            }
        };
        xp = new StringBuffer(256);
        xp.append(getCommonPath());
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_INDEXES);
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_INDEX);
        xp.append("[");
        xp.append(I_CmsXmlConfiguration.N_NAME);
        xp.append("='").append(CmsGallerySearchIndex.GALLERY_INDEX_NAME).append("']");
        m_actions.put(xp.toString(), action2);
        //
        //=============================================================================================================
        //
        CmsXmlUpdateAction action3 = new CmsXmlUpdateAction() {

            @SuppressWarnings("synthetic-access")
            @Override
            public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                Node node = doc.selectSingleNode(xpath);
                if (node != null) {
                    return false;
                }
                // create doc type
                createIndexSource(doc, xpath, "gallery_source", CmsVfsIndexer.class, new String[] {
                    "/sites/",
                    "/shared/",
                    "/system/galleries/"}, new String[] {
                    "xmlpage-galleries",
                    "xmlcontent-galleries",
                    "jsp",
                    "text",
                    "pdf",
                    "rtf",
                    "html",
                    "image",
                    "generic",
                    "openoffice",
                    "msoffice-ole2",
                    "msoffice-ooxml"});
                return true;

            }
        };
        xp = new StringBuffer(256);
        xp.append(getCommonPath());
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_INDEXSOURCES);
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_INDEXSOURCE);
        xp.append("[");
        xp.append(I_CmsXmlConfiguration.N_NAME);
        xp.append("='gallery_source']");
        m_actions.put(xp.toString(), action3);
        //
        //=============================================================================================================
        //
        CmsXmlUpdateAction action4 = new CmsXmlUpdateAction() {

            @Override
            public boolean executeUpdate(Document document, String xpath, boolean forReal) {

                Node node = document.selectSingleNode(xpath);

                if (node != null) {
                    node.detach();
                }
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
                field.setDisplayNameForConfiguration("%(key.field.content)");
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
                field.setDisplayNameForConfiguration("%(key.field.title)");
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
                field.setDisplayNameForConfiguration("%(key.field.description)");
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
                field.setIndexed("untokenized");
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
                return true;
            }
        };

        xp = new StringBuffer(256);
        xp.append(getCommonPath());
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_FIELDCONFIGURATIONS);
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_FIELDCONFIGURATION);
        xp.append("[");
        xp.append(I_CmsXmlConfiguration.N_NAME);
        xp.append("='gallery_fields']");
        m_actions.put(xp.toString(), action4);
        //
        //=============================================================================================================
        //

        m_actions.put("/opencms/search/indexsources", new CmsIndexSourceTypeUpdateAction());

        // use dummy check [1=1] to make the xpaths unique 
        m_actions.put("/opencms/search/indexsources[1=1]", new CmsAddGalleryModuleIndexSourceAction());
        m_actions.put(
            buildXpathForIndexedDocumentType("source1", "containerpage"),
            createIndexedTypeAction("containerpage"));

        //=============================================================================================================

        String analyzerEnPath = "/opencms/search/analyzers/analyzer[class='org.apache.lucene.analysis.standard.StandardAnalyzer'][locale='en']";
        m_actions.put(analyzerEnPath, new ElementReplaceAction(analyzerEnPath, "<analyzer>\n"
            + "                <class>org.apache.lucene.analysis.en.EnglishAnalyzer</class>\n"
            + "                <locale>en</locale>\n"
            + "            </analyzer>"));

        String analyzerItPath = "/opencms/search/analyzers/analyzer[class='org.apache.lucene.analysis.snowball.SnowballAnalyzer'][stemmer='Italian']";
        m_actions.put(analyzerItPath, new ElementReplaceAction(analyzerItPath, "<analyzer>\n"
            + "                <class>org.apache.lucene.analysis.it.ItalianAnalyzer</class>\n"
            + "                <locale>it</locale>\n"
            + "            </analyzer>"));

        m_actions.put(
            "/opencms/search/indexsources/indexsource[name='gallery_source']/resources['systemgalleries'='systemgalleries']",
            new CmsAddIndexSourceResourceAction());
    }
}