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

package org.opencms.setup.xml.v8;

import org.opencms.setup.xml.A_CmsXmlSearch;

import java.util.Collections;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * Adds the solr search node.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlAddSolrSearch extends A_CmsXmlSearch {

    /**
     * @see org.opencms.setup.xml.CmsXmlUpdateAction#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

        Element node = (Element)doc.selectSingleNode("/opencms/search");
        if (node.selectSingleNode("solr") == null) {
            String solrComment = " To enable Solr in OpenCms you must create a solr/ home\n"
                + "           directory in the WEB-INF folder of your OpenCms application.\n"
                + "           Copy the solr/ folder from the OpenCms standard distribution\n"
                + "           as a starting point for your configuration. ";
            try {
                Element solrElement = createElementFromXml("<solr enabled=\"false\"></solr>");
                solrElement.addComment(solrComment);
                node.elements().add(0, solrElement);
            } catch (DocumentException e) {
                System.out.println("Could not add solr node");
                return false;
            }
        } else {
            String solrComment = "\n"
                + "           During the update Solr will be disabled in the WEB-INF/config/opencms-search.xml.\n"
                + "           To update Solr you must update the 'schema.xml and' the 'solrconfig.xml' manually.\n"
                + "           The new default configuration files are located in the solr-update/ directory in\n"
                + "           the WEB-INF folder of your application. If you are using the default configuration\n"
                + "           from the distribution, it is sufficient to copy the new configuration files to the\n"
                + "           WEB-INF/solr folder. Else if you have customized the Solr configuration you might\n"
                + "           want to merge the 'schema.xml' and the 'solrconfig.xml' first. When you are done\n"
                + "           set the attribute enabled to 'true' again.\n";
            Element solrElement = (Element)node.selectSingleNode("solr");
            Attribute a = solrElement.attribute("enabled");
            if (a != null) {
                a.setValue("false");
            }
            solrElement.addComment(solrComment);
        }

        try {

            tryToAddMissingElement(
                doc,
                "//index[name='Solr Offline']",
                "//indexes",
                "<index class=\"org.opencms.search.solr.CmsSolrIndex\">\n"
                    + "                <name>Solr Offline</name>\n"
                    + "                <rebuild>offline</rebuild>\n"
                    + "                <project>Offline</project>\n"
                    + "                <locale>all</locale>\n"
                    + "                <configuration>solr_fields</configuration>\n"
                    + "                <sources>\n"
                    + "                    <source>solr_source</source>\n"
                    + "                </sources>\n"
                    + "                <param name=\"search.solr.postProcessor\">org.opencms.search.solr.CmsSolrLinkProcessor</param>\n"
                    + "            </index>");

            tryToAddMissingElement(
                doc,
                "//index[name='Solr Online']",
                "//indexes",
                "<index class=\"org.opencms.search.solr.CmsSolrIndex\">\n"
                    + "                <name>Solr Online</name>\n"
                    + "                <rebuild>auto</rebuild>\n"
                    + "                <project>Online</project>\n"
                    + "                <locale>all</locale>\n"
                    + "                <configuration>solr_fields</configuration>\n"
                    + "                <sources>\n"
                    + "                    <source>solr_source</source>\n"
                    + "                </sources>\n"
                    + "                <param name=\"search.solr.postProcessor\">org.opencms.search.solr.CmsSolrLinkProcessor</param>\n"
                    + "            </index>");

            Element solrSource = (Element)(doc.selectSingleNode("//indexsource[name='solr_source']"));
            // will be added again in next step
            if (solrSource != null) {
                solrSource.detach();
            }

            tryToAddMissingElement(
                doc,
                "//indexsource[name='solr_source']",
                "//indexsources",
                "<indexsource>\n"
                    + "                <name>solr_source</name>\n"
                    + "                <indexer class=\"org.opencms.search.CmsVfsIndexer\" />\n"
                    + "                <resources>\n"
                    + "                    <resource>/</resource>\n"
                    + "                </resources>\n"
                    + "                <documenttypes-indexed>\n"
                    + "                    <name>xmlcontent-solr</name>\n"
                    + "                    <name>containerpage-solr</name>\n"
                    + "                    <name>xmlpage</name>\n"
                    + "                    <name>text</name>\n"
                    + "                    <name>jsp</name>\n"
                    + "                    <name>pdf</name>\n"
                    + "                    <name>rtf</name>\n"
                    + "                    <name>html</name>\n"
                    + "                    <name>image</name>\n"
                    + "                    <name>generic</name>\n"
                    + "                    <name>msoffice-ole2</name>\n"
                    + "                    <name>msoffice-ooxml</name>\n"
                    + "                    <name>openoffice</name>\n"
                    + "                </documenttypes-indexed>\n"
                    + "            </indexsource>");

            tryToAddMissingElement(
                doc,
                "//fieldconfiguration[name='solr_fields']",
                "//fieldconfigurations",
                "<fieldconfiguration class=\"org.opencms.search.solr.CmsSolrFieldConfiguration\">\n"
                    + "                <name>solr_fields</name>\n"
                    + "                <description>The Solr search index field configuration.</description>\n"
                    + "                <fields />\n"
                    + "            </fieldconfiguration>");

            tryToAddMissingElement(
                doc,
                "//documenttype[name='xmlcontent-solr']",
                "//documenttypes",
                "<documenttype>\n"
                    + "                <name>xmlcontent-solr</name>\n"
                    + "                <class>org.opencms.search.solr.CmsSolrDocumentXmlContent</class>\n"
                    + "                <mimetypes>\n"
                    + "                    <mimetype>text/html</mimetype>\n"
                    + "                </mimetypes>\n"
                    + "                <resourcetypes>\n"
                    + "                    <resourcetype>xmlcontent-solr</resourcetype>\n"
                    + "                </resourcetypes>\n"
                    + "            </documenttype>");

            tryToAddMissingElement(
                doc,
                "//documenttype[name='containerpage-solr']",
                "//documenttypes",
                "<documenttype>\n"
                    + "                <name>containerpage-solr</name>\n"
                    + "                <class>org.opencms.search.solr.CmsSolrDocumentContainerPage</class>\n"
                    + "                <mimetypes>\n"
                    + "                    <mimetype>text/html</mimetype>\n"
                    + "                </mimetypes>\n"
                    + "                <resourcetypes>\n"
                    + "                    <resourcetype>containerpage-solr</resourcetype>\n"
                    + "                </resourcetypes>\n"
                    + "            </documenttype>");

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add the Solr configuration";
    }

    /**
     * Adds an XML configuration element if it is missing.<p>
     *
     * @param doc the document
     * @param checkPath the xpath of the element
     * @param parentPath the xpath of the parent to which the element should be added if it's not found
     * @param xmlToAdd the XML of the element to add
     *
     * @throws DocumentException if something goes wrong
     */
    public void tryToAddMissingElement(Document doc, String checkPath, String parentPath, String xmlToAdd)
    throws DocumentException {

        if (doc.selectSingleNode(checkPath) == null) {
            Element parent = (Element)(doc.selectSingleNode(parentPath));
            if (parent != null) {
                parent.add(createElementFromXml(xmlToAdd));
            } else {
                System.err.println("Failed to add missing element: checkPath = " + checkPath);
            }
        }
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        return "/opencms/search";

    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        return Collections.singletonList("/opencms/search");
    }
}