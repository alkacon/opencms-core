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

package org.opencms.setup.xml;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xml.utils.SystemIDResolver;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Class for updating the XML configuration files using a set of XSLT transforms.
 *
 * The XSLT transforms are stored in the directory update/xmlupdate, together with a file transforms.xml
 * that contains the list of transformation files and the configuration files to which they should be applied to.
 *
 */
public class CmsXmlConfigUpdater {

    /**
     * Need this so that 'dummy' entity resolver is also used for documents read with the document() function.
     */
    public class EntityIgnoringUriResolver implements URIResolver {

        public Source resolve(String href, String base) throws TransformerException {

            try {
                String uri = SystemIDResolver.getAbsoluteURI(href, base);
                XMLReader reader = m_parserFactory.newSAXParser().getXMLReader();
                reader.setEntityResolver(NO_ENTITY_RESOLVER);
                Source source;
                source = new SAXSource(reader, new InputSource(uri));
                return source;
            } catch (Exception e) {
                throw new TransformerException(e);
            }
        }
    }

    /**
     * Single entry from transforms.xml.
     */
    private class TransformEntry {

        /** Name of the config file. */
        private String m_configFile;

        /** Name of the XSLT file. */
        private String m_xslt;

        /**
         * Creates a new entry.
         *
         * @param configFile the name of the config file
         * @param xslt the name of the XSLT file
         */
        public TransformEntry(String configFile, String xslt) {

            super();
            m_xslt = xslt;
            m_configFile = configFile;
        }

        /**
         * Gets the name of the config file.
         *
         * @return the name of the config file
         */
        public String getConfigFile() {

            return m_configFile;
        }

        /**
         * Gets the name of the XSLT file.
         *
         * @return the name of the XSLT file
         */
        public String getXslt() {

            return m_xslt;
        }

    }

    /**
     * Default XML for new config files.
     */
    public static final String DEFAULT_XML = "<opencms/>";

    /** Entity resolver to skip dtd validation. */
    private static final EntityResolver NO_ENTITY_RESOLVER = new EntityResolver() {

        /**
         * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
         */
        public InputSource resolveEntity(String publicId, String systemId) {

            // return new InputSource(new StringReader("<!ELEMENT opencms ANY>"));
            return new InputSource(new StringReader(""));
        }
    };

    /** Directory for the config files. */
    private File m_configDir;

    /**Flag to indicate if transformation was done.*/
    private boolean m_isDone = false;

    /** The parser factory. */
    private SAXParserFactory m_parserFactory = SAXParserFactory.newInstance();

    /** The transformer factory. */
    private TransformerFactory m_transformerFactory = new org.apache.xalan.processor.TransformerFactoryImpl();

    /** The directory containing the XSLT transforms. */
    private File m_xsltDir;

    /**
     * Creates a new instance.
     *
     * @param xsltDir the directory containing the XSLT files
     * @param configDir the configuration directory
     */
    public CmsXmlConfigUpdater(File xsltDir, File configDir) {

        m_configDir = configDir;
        m_xsltDir = xsltDir;
        m_parserFactory.setNamespaceAware(true);
        m_parserFactory.setValidating(false);
        m_transformerFactory.setURIResolver(new EntityIgnoringUriResolver());
    }

    /**
     * Helper method for determining the position for a top-level configuration element in opencms-system.xml.
     *
     * <p>This can be used by XSL transformations to insert optional nodes for new features on the top level.
     * @param name the element name
     * @return the position for the element name, or -1 if the position could not be determined
     *
     * @throws Exception if something goes wrong
     */
    public static int getSystemConfigPosition(String name) throws Exception {

        byte[] fileData = CmsFileUtil.readFully(
            CmsConfigurationManager.class.getResourceAsStream("opencms-system.dtd"),
            true);
        String dtdText = new String(fileData, StandardCharsets.UTF_8);
        // Assumption: declaration of 'system' in the DTD is just a list of elements (with +/*/? suffixes), and doesn't have nested expressions
        String regex = "(?s)<!ELEMENT +system +\\(([^()]*?)\\)>";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(dtdText);
        List<String> elementNames = new ArrayList<>();
        if (m.find()) {
            String items = m.group(1);
            for (String token : items.split("(?:\\s|,)+")) {
                token = token.trim();
                if (token.length() == 0) {
                    continue;
                }
                if (token.endsWith("*") || token.endsWith("?") || token.endsWith("+")) {
                    token = token.substring(0, token.length() - 1);
                }
                elementNames.add(token);
            }
            return elementNames.indexOf(name);
        }
        return -1;
    }

    /**
     * Checks if updater has tried to transform.<p>
     *
     * @return boolean
     */
    public boolean isDone() {

        return m_isDone;
    }

    /**
     * Transforms a config file with an XSLT transform.
     *
     * @param name file name of the config file
     * @param transform file name of the XSLT file
     *
     * @throws Exception if something goes wrong
     */
    public void transform(String name, String transform) throws Exception {

        File configFile = new File(m_configDir, name);
        File transformFile = new File(m_xsltDir, transform);
        try (InputStream stream = new FileInputStream(transformFile)) {
            StreamSource source = new StreamSource(stream);
            transform(configFile, source);
        }
    }

    /**
     * Transforms the configuration.
     *
     * @throws Exception if something goes wrong
     */
    public void transformConfig() throws Exception {

        List<TransformEntry> entries = readTransformEntries(new File(m_xsltDir, "transforms.xml"));
        for (TransformEntry entry : entries) {
            transform(entry.getConfigFile(), entry.getXslt());
        }
        m_isDone = true;
    }

    /**
     * Gets validation errors either as a JSON string, or null if there are no validation errors.
     *
     * @return the validation error JSON
     */
    public String validationErrors() {

        List<String> errors = new ArrayList<>();
        for (File config : getConfigFiles()) {
            String filename = config.getName();
            try (FileInputStream stream = new FileInputStream(config)) {
                CmsXmlUtils.unmarshalHelper(CmsFileUtil.readFully(stream, false), new CmsXmlEntityResolver(null), true);
            } catch (CmsXmlException e) {
                errors.add(filename + ":" + e.getCause().getMessage());
            } catch (Exception e) {
                errors.add(filename + ":" + e.getMessage());
            }
        }
        if (errors.size() == 0) {
            return null;
        }
        String errString = CmsStringUtil.listAsString(errors, "\n");
        JSONObject obj = new JSONObject();
        try {
            obj.put("err", errString);
        } catch (JSONException e) {

        }
        return obj.toString();
    }

    /**
     * Gets existing config files.
     *
     * @return the existing config files
     */
    private List<File> getConfigFiles() {

        String[] filenames = {
            "opencms-modules.xml",
            "opencms-system.xml",
            "opencms-vfs.xml",
            "opencms-importexport.xml",
            "opencms-sites.xml",
            "opencms-variables.xml",
            "opencms-scheduler.xml",
            "opencms-workplace.xml",
            "opencms-search.xml"};
        List<File> result = new ArrayList<>();
        for (String fn : filenames) {
            File file = new File(m_configDir, fn);
            if (file.exists()) {
                result.add(file);
            }
        }
        return result;
    }

    /**
     * Reads entries from transforms.xml.
     *
     * @param file the XML file
     * @return the transform entries read from the file
     *
     * @throws Exception if something goes wrong
     */
    private List<TransformEntry> readTransformEntries(File file) throws Exception {

        List<TransformEntry> result = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = CmsFileUtil.readFully(fis, false);
            Document doc = CmsXmlUtils.unmarshalHelper(data, null, false);
            for (Node node : doc.selectNodes("//transform")) {
                Element elem = ((Element)node);
                String xslt = elem.attributeValue("xslt");
                String conf = elem.attributeValue("config");
                TransformEntry entry = new TransformEntry(conf, xslt);
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Transforms a single configuration file using the given transformation source.
     *
     * @param file the configuration file
     * @param transformSource the transform soruce
     *
     * @throws TransformerConfigurationException -
     * @throws IOException -
     * @throws SAXException -
     * @throws TransformerException -
     * @throws ParserConfigurationException -
     */
    private void transform(File file, Source transformSource)
    throws TransformerConfigurationException, IOException, SAXException, TransformerException,
    ParserConfigurationException {

        Transformer transformer = m_transformerFactory.newTransformer(transformSource);
        transformer.setOutputProperty(OutputKeys.ENCODING, "us-ascii");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        String configDirPath = m_configDir.getAbsolutePath();
        configDirPath = configDirPath.replaceFirst("[/\\\\]$", "");
        transformer.setParameter("configDir", configDirPath);
        XMLReader reader = m_parserFactory.newSAXParser().getXMLReader();
        reader.setEntityResolver(NO_ENTITY_RESOLVER);

        Source source;

        if (file.exists()) {
            source = new SAXSource(reader, new InputSource(file.getCanonicalPath()));
        } else {
            source = new SAXSource(reader, new InputSource(new ByteArrayInputStream(DEFAULT_XML.getBytes("UTF-8"))));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Result target = new StreamResult(baos);
        transformer.transform(source, target);
        byte[] transformedConfig = baos.toByteArray();
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(transformedConfig);
        }
    }

}
