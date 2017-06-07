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

package org.opencms.configuration;

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlErrorHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocumentType;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Configuration manager for digesting the OpenCms XML configuration.<p>
 *
 * Reads the individual configuration class nodes first and creaes new
 * instances of the "base" configuration classes.<p>
 *
 * @since 6.0.0
 */
public class CmsConfigurationManager implements I_CmsXmlConfiguration {

    class MyStream extends OutputStream {

        private ByteArrayOutputStream m_baos = new ByteArrayOutputStream();

        /**
         * @see java.io.OutputStream#write(int)
         */
        @Override
        public void write(int b) throws IOException {

            m_baos.write(b);
            if ((b == 10) || (b == 13)) {
                System.out.println("*");
            }

        }
    }

    /** The location of the OpenCms configuration DTD if the default prefix is the system ID. */
    public static final String DEFAULT_DTD_LOCATION = "org/opencms/configuration/";

    /** Location of the optional XSLT file used to transform the configuration. */
    public static final String DEFAULT_XSLT_FILENAME = "opencms-configuration.xslt";

    /** The default prefix for the OpenCms configuration DTD. */
    public static final String DEFAULT_DTD_PREFIX = "http://www.opencms.org/dtd/6.0/";

    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms.xml";

    /** The name of the DTD file for this configuration. */
    public static final String DTD_FILE_NAME = "opencms-configuration.dtd";

    /** The "opencms" root node of the XML configuration. */
    public static final String N_ROOT = "opencms";

    /** Postfix for original configuration files. */
    public static final String POSTFIX_ORI = ".ori";

    /** The config node. */
    protected static final String N_CONFIG = "config";

    /** The configurations node. */
    protected static final String N_CONFIGURATION = "configuration";

    /** Date format for the backup file time prefix. */
    private static final SimpleDateFormat BACKUP_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_");

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsConfigurationManager.class);

    /** The number of days to keep old backups for. */
    private static final long MAX_BACKUP_DAYS = 15;

    /** The folder where to store the backup files of the configuration. */
    private File m_backupFolder;

    /** The base folder where the configuration files are located. */
    private File m_baseFolder;

    /** The initialized configuration classes. */
    private List<I_CmsXmlConfiguration> m_configurations;

    /** The digester for reading the XML configuration. */
    private Digester m_digester;

    /** The configuration based on <code>opencms.properties</code>. */
    private CmsParameterConfiguration m_propertyConfiguration;

    /**
     * Creates a new OpenCms configuration manager.<p>
     *
     * @param baseFolder base folder where XML configurations to load are located
     */
    public CmsConfigurationManager(String baseFolder) {

        m_baseFolder = new File(baseFolder);
        if (!m_baseFolder.exists()) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.LOG_INVALID_CONFIG_BASE_FOLDER_1,
                        m_baseFolder.getAbsolutePath()));
            }
        }
        m_backupFolder = new File(m_baseFolder.getAbsolutePath() + File.separatorChar + "backup");
        if (!m_backupFolder.exists()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_CREATE_CONFIG_BKP_FOLDER_1,
                        m_backupFolder.getAbsolutePath()));
            }
            m_backupFolder.mkdirs();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(Messages.LOG_CONFIG_BASE_FOLDER_1, m_baseFolder.getAbsolutePath()));
            LOG.debug(
                Messages.get().getBundle().key(Messages.LOG_CONFIG_BKP_FOLDER_1, m_backupFolder.getAbsolutePath()));
        }
        cacheDtdSystemId(this);
        m_configurations = new ArrayList<I_CmsXmlConfiguration>();
    }

    /**
     * Adds a configuration object to the configuration manager.<p>
     *
     * @param configuration the configuration to add
     */
    public void addConfiguration(I_CmsXmlConfiguration configuration) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_CONFIG_1, configuration));
        }
        m_configurations.add(configuration);
        cacheDtdSystemId(configuration);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        // noop, this configuration has no additional parameters
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        // add rule for <configuration> node
        digester.addObjectCreate(
            "*/" + N_CONFIGURATION + "/" + N_CONFIG,
            I_CmsXmlConfiguration.A_CLASS,
            CmsConfigurationException.class);
        digester.addSetNext("*/" + N_CONFIGURATION + "/" + N_CONFIG, "addConfiguration");
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        // add the <configuration> node
        Element configurationElement = parent.addElement(N_CONFIGURATION);
        for (int i = 0; i < m_configurations.size(); i++) {
            // append the individual configuration
            I_CmsXmlConfiguration configuration = m_configurations.get(i);
            configurationElement.addElement(N_CONFIG).addAttribute(
                I_CmsXmlConfiguration.A_CLASS,
                configuration.getClass().getName());
        }
        return parent;
    }

    /**
     * Creates the XML document build from the provided configuration.<p>
     *
     * @param configuration the configuration to build the XML for
     * @return the XML document build from the provided configuration
     */
    public Document generateXml(I_CmsXmlConfiguration configuration) {

        // create a new document
        Document result = DocumentHelper.createDocument();

        // set the document type
        DOMDocumentType docType = new DOMDocumentType();
        docType.setElementName(N_ROOT);
        docType.setSystemID(configuration.getDtdUrlPrefix() + configuration.getDtdFilename());
        result.setDocType(docType);

        Element root = result.addElement(N_ROOT);
        // start the XML generation
        configuration.generateXml(root);

        // return the resulting document
        return result;
    }

    /**
     * Returns the backup folder.<p>
     *
     * @return the backup folder
     */
    public File getBackupFolder() {

        return m_backupFolder;
    }

    /**
     * Returns the properties read from <code>opencms.properties</code>.<p>
     *
     * @see #setConfiguration(CmsParameterConfiguration)
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_propertyConfiguration;
    }

    /**
     * Returns a specific configuration from the list of initialized configurations.<p>
     *
     * @param clazz the configuration class that should be returned
     * @return the initialized configuration class instance, or <code>null</code> if this is not found
     */
    public I_CmsXmlConfiguration getConfiguration(Class<?> clazz) {

        for (int i = 0; i < m_configurations.size(); i++) {
            I_CmsXmlConfiguration configuration = m_configurations.get(i);
            if (clazz.equals(configuration.getClass())) {
                return configuration;
            }
        }
        return null;
    }

    /**
     * Returns the list of all initialized configurations.<p>
     *
     * @return the list of all initialized configurations
     */
    public List<I_CmsXmlConfiguration> getConfigurations() {

        return m_configurations;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return DTD_FILE_NAME;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdSystemLocation()
     */
    public String getDtdSystemLocation() {

        return DEFAULT_DTD_LOCATION;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdUrlPrefix()
     */
    public String getDtdUrlPrefix() {

        return DEFAULT_DTD_PREFIX;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getXmlFileName()
     */
    public String getXmlFileName() {

        return DEFAULT_XML_FILE_NAME;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // does not need to be initialized
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_INIT_CONFIGURATION_1, this));
        }
    }

    /**
     * Loads the OpenCms configuration from the given XML file.<p>
     *
     * @throws SAXException in case of XML parse errors
     * @throws IOException in case of file IO errors
     */
    public void loadXmlConfiguration() throws SAXException, IOException {

        URL baseUrl = m_baseFolder.toURI().toURL();
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_BASE_URL_1, baseUrl));
        }

        // first load the base configuration
        loadXmlConfiguration(baseUrl, this);

        // now iterate all sub-configurations
        Iterator<I_CmsXmlConfiguration> i = m_configurations.iterator();
        while (i.hasNext()) {
            loadXmlConfiguration(baseUrl, i.next());
        }

        // remove the old backups
        removeOldBackups(MAX_BACKUP_DAYS);
    }

    /**
     * Sets the configuration read from the <code>opencms.properties</code>.<p>
     *
     * @param propertyConfiguration the configuration read from the <code>opencms.properties</code>
     *
     * @see #getConfiguration()
     */
    public void setConfiguration(CmsParameterConfiguration propertyConfiguration) {

        m_propertyConfiguration = propertyConfiguration;
    }

    /**
     * Writes the XML configuration for the provided configuration instance.<p>
     *
     * @param clazz the configuration class to write the XML for
     * @throws IOException in case of I/O errors while writing
     * @throws CmsConfigurationException if the given class is not a valid configuration class
     */
    public void writeConfiguration(Class<?> clazz) throws IOException, CmsConfigurationException {

        I_CmsXmlConfiguration configuration = getConfiguration(clazz);
        if (configuration == null) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_CONFIG_WITH_UNKNOWN_CLASS_1, clazz.getName()));
        }

        // generate the file URL for the XML input
        File file = new File(m_baseFolder, configuration.getXmlFileName());
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_WRITE_CONFIG_XMLFILE_1, file.getAbsolutePath()));
        }

        // generate the XML document
        Document config = generateXml(configuration);

        // output the document
        XMLWriter writer = null;
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setIndentSize(4);
        format.setTrimText(false);
        format.setEncoding(CmsEncoder.ENCODING_UTF_8);

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            writer = new XMLWriter(out, format);
            writer.write(config);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (out != null) {
                out.close();
            }
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(
                Messages.get().getBundle().key(
                    Messages.LOG_WRITE_CONFIG_SUCCESS_2,
                    file.getAbsolutePath(),
                    configuration.getClass().getName()));
        }
    }

    /**
     * Gets the path to the XSLT transformation file that should be used for the configuration.<p>
     *
     * @return the path to the XSLT transformation
     */
    String getTransformationPath() {

        String path = System.getProperty("opencms.config.transform");
        if (path == null) {
            path = CmsStringUtil.joinPaths(m_baseFolder.getAbsolutePath(), DEFAULT_XSLT_FILENAME);
        }
        return path;
    }

    /**
     * Checks if an XSLT transformation file is available.<p>
     *
     * @return true if an XSLT transformation file is available
     */
    boolean hasTransformation() {

        String transformationPath = getTransformationPath();
        boolean result = (transformationPath != null) && new File(transformationPath).exists();
        if (result) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_XSLT_CONFIG_ENABLED_1, transformationPath));
        } else {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_XSLT_CONFIG_DISABLED_0));
        }
        return result;
    }

    /**
     * Transforms the given configuration using an XSLT transformation.<p>
     *
     * @param url the URL of the base folder
     * @param config the configuration object
     *
     * @return the InputSource to feed the configuration digester
     *
     * @throws TransformerException if the transformation fails
     * @throws IOException if an error occurs while reading the configuration or transformation
     * @throws SAXException if parsing the configuration file fails
     * @throws ParserConfigurationException if something goes wrong with configuring the parser
     */
    InputSource transformConfiguration(URL url, I_CmsXmlConfiguration config)
    throws TransformerException, IOException, SAXException, ParserConfigurationException {

        String configPath = CmsStringUtil.joinPaths(url.getFile(), config.getXmlFileName());
        String transformPath = getTransformationPath();
        TransformerFactory factory = TransformerFactory.newInstance();

        ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        System.setErr(new PrintStream(errBaos));
        try {
            LOG.info("Transforming '" + configPath + "' with transformation '" + transformPath + "'");
            Transformer transformer = factory.newTransformer(new StreamSource(new File(transformPath)));
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setParameter("file", config.getXmlFileName());
            InetAddress localhost = InetAddress.getLocalHost();
            transformer.setParameter("hostName", localhost.getHostName());
            transformer.setParameter("canonicalHostName", localhost.getCanonicalHostName());
            transformer.setParameter("hostAddress", localhost.getHostAddress());
            // use a SAXSource here because we need to set the correct entity resolver to prevent errors
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            parserFactory.setValidating(false); // Turn off validation
            XMLReader reader = parserFactory.newSAXParser().getXMLReader();
            reader.setEntityResolver(new CmsXmlEntityResolver(null));
            Source source = new SAXSource(reader, new InputSource(configPath));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Result target = new StreamResult(baos);

            transformer.transform(source, target);

            byte[] transformedConfig = baos.toByteArray();
            // We can't set the doctype dynamically from inside the XSLT transform using XSLT 1.0, and XSLT 2.0
            // isn't supported by the standard implementation in the JDK. So we do some macro replacement after the
            // transformation.
            String transformedConfigStr = new String(transformedConfig, "UTF-8").replaceFirst(
                "@dtd@",
                config.getDtdUrlPrefix() + config.getDtdFilename());
            if (LOG.isDebugEnabled()) {
                LOG.debug("");
                LOG.debug(
                    "=================== Transformation result for config file '" + config.getXmlFileName() + "':");
                LOG.debug(transformedConfigStr);
            }
            return new InputSource(new ByteArrayInputStream(transformedConfigStr.getBytes("UTF-8")));
        } finally {
            System.setErr(oldErr);
            byte[] errorBytes = errBaos.toByteArray();
            if (errorBytes.length > 0) {
                LOG.warn(new String(errorBytes, "UTF-8"));
            }
        }
    }

    /**
     * Creates a backup of the given XML configurations input file.<p>
     *
     * @param configuration the configuration for which the input file should be backed up
     */
    private void backupXmlConfiguration(I_CmsXmlConfiguration configuration) {

        String fromName = m_baseFolder.getAbsolutePath() + File.separatorChar + configuration.getXmlFileName();
        String toDatePrefix = BACKUP_DATE_FORMAT.format(new Date());
        String toName = m_backupFolder.getAbsolutePath()
            + File.separatorChar
            + toDatePrefix
            + configuration.getXmlFileName();

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_CREATE_CONFIG_BKP_2, fromName, toName));
        }

        try {
            CmsFileUtil.copy(fromName, toName);
        } catch (IOException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_CREATE_CONFIG_BKP_FAILURE_1, toName), e);
        }
    }

    /**
     * Adds a new DTD system id prefix mapping for internal resolution of external URLs.<p>
     *
     * @param configuration the configuration to add the mapping from
     */
    private void cacheDtdSystemId(I_CmsXmlConfiguration configuration) {

        if (configuration.getDtdSystemLocation() != null) {
            try {
                String file = CmsFileUtil.readFile(
                    configuration.getDtdSystemLocation() + configuration.getDtdFilename(),
                    CmsEncoder.ENCODING_UTF_8);
                CmsXmlEntityResolver.cacheSystemId(
                    configuration.getDtdUrlPrefix() + configuration.getDtdFilename(),
                    file.getBytes(CmsEncoder.ENCODING_UTF_8));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_CACHE_DTD_SYSTEM_ID_1,
                            configuration.getDtdUrlPrefix()
                                + configuration.getDtdFilename()
                                + " --> "
                                + configuration.getDtdSystemLocation()
                                + configuration.getDtdFilename()));
                }
            } catch (IOException e) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.LOG_CACHE_DTD_SYSTEM_ID_FAILURE_1,
                        configuration.getDtdSystemLocation() + configuration.getDtdFilename()),
                    e);
            }
        }
    }

    /**
     * Loads the OpenCms configuration from the given XML URL.<p>
     *
     * @param url the base URL of the XML configuration to load
     * @param configuration the configuration to load
     * @throws SAXException in case of XML parse errors
     * @throws IOException in case of file IO errors
     */
    private void loadXmlConfiguration(URL url, I_CmsXmlConfiguration configuration) throws SAXException, IOException {

        // generate the file URL for the XML input
        URL fileUrl = new URL(url, configuration.getXmlFileName());
        CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LOAD_CONFIG_XMLFILE_1, fileUrl));
        // Check transformation rule here so we have the XML file / XSLT file log output together
        boolean hasTransformation = hasTransformation();

        // create a backup of the configuration
        backupXmlConfiguration(configuration);

        // instantiate Digester and enable XML validation
        m_digester = new Digester();
        m_digester.setUseContextClassLoader(true);
        //TODO: For this to work with transformed configurations, we need to add the correct DOCTYPE declarations to the transformed files
        m_digester.setValidating(true);
        m_digester.setEntityResolver(new CmsXmlEntityResolver(null));
        m_digester.setRuleNamespaceURI(null);
        m_digester.setErrorHandler(new CmsXmlErrorHandler(fileUrl.getFile()));

        // add this class to the Digester
        m_digester.push(configuration);

        configuration.addXmlDigesterRules(m_digester);

        InputSource inputSource = null;
        if (hasTransformation) {
            try {
                inputSource = transformConfiguration(url, configuration);
            } catch (Exception e) {
                LOG.error("Error transforming " + configuration.getXmlFileName() + ": " + e.getLocalizedMessage(), e);
            }
        }
        if (inputSource == null) {
            inputSource = new InputSource(fileUrl.openStream());
        }
        // start the parsing process
        m_digester.parse(inputSource);
    }

    /**
     * Removes all backups that are older then the given number of days.<p>
     *
     * @param daysToKeep the days to keep the backups for
     */
    private void removeOldBackups(long daysToKeep) {

        long maxAge = (System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000));
        File[] files = m_backupFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            long lastMod = file.lastModified();
            if ((lastMod < maxAge) & (!file.getAbsolutePath().endsWith(CmsConfigurationManager.POSTFIX_ORI))) {
                file.delete();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(Messages.LOG_REMOVE_CONFIG_FILE_1, file.getAbsolutePath()));
                }
            }
        }
    }
}