/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsConfigurationManager.java,v $
 * Date   : $Date: 2005/04/26 16:37:54 $
 * Version: $Revision: 1.20 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.configuration;

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlErrorHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocumentType;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

/**
 * Configuration manager for digesting the OpenCms XML configuration.<p>
 * 
 * Reads the individual configuration class nodes first and creaes new 
 * instances of the "base" configuration classes.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsConfigurationManager implements I_CmsXmlConfiguration {

    /** The location of the OpenCms configuration DTD if the default prefix is the system ID. */
    public static final String C_DEFAULT_DTD_LOCATION = "org/opencms/configuration/";

    /** The default prefix for the OpenCms configuration DTD. */
    public static final String C_DEFAULT_DTD_PREFIX = "http://www.opencms.org/dtd/6.0/";

    /** The config node. */
    protected static final String N_CONFIG = "config";

    /** The configurations node. */
    protected static final String N_CONFIGURATION = "configuration";

    /** The "opencms" root node of the XML configuration. */
    protected static final String N_ROOT = "opencms";

    /** The name of the default XML file for this configuration. */
    private static final String C_DEFAULT_XML_FILE_NAME = "opencms.xml";

    /** The name of the DTD file for this configuration. */
    private static final String C_DTD_FILE_NAME = "opencms-configuration.dtd";

    /** The number of days to keep old backups for. */
    private static final long C_MAX_BACKUP_DAYS = 15;

    /** Date format for the backup file time prefix. */
    private static final SimpleDateFormat BACKUP_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_");

    /** The folder where to store the backup files of the configuration. */
    private File m_backupFolder;

    /** The base folder where the configuration files are located. */
    private File m_baseFolder;

    /** The initialized configuration classes. */
    private List m_configurations;

    /** The digester for reading the XML configuration. */
    private Digester m_digester;

    /** The legacy configuration based on "opencms.properties". */
    private Map m_legacyConfiguration;
    
    /**
     * Creates a new OpenCms configuration manager.<p>
     * 
     * @param baseFolder base folder where XML configurations to load are located
     */
    public CmsConfigurationManager(String baseFolder) {

        m_baseFolder = new File(baseFolder);
        if (!m_baseFolder.exists()) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(
                    "Configuration base folder " + m_baseFolder.getAbsolutePath() + " does not exist!");
            }
        }
        m_backupFolder = new File(m_baseFolder.getAbsolutePath() + File.separatorChar + "backup");
        if (!m_backupFolder.exists()) {
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Creating configuration backup folder " + m_backupFolder.getAbsolutePath());
            }
            m_backupFolder.mkdirs();
        }
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Configuration base folder is " + m_baseFolder.getAbsolutePath());
            OpenCms.getLog(this).debug("Configuration backup folder is " + m_backupFolder.getAbsolutePath());
        }
        cacheDtdSystemId(this);
        m_configurations = new ArrayList();
    }

    /**
     * Adds a configuration object to the configuration manager.<p>
     * 
     * @param configuration the configuration to add
     */
    public void addConfiguration(I_CmsXmlConfiguration configuration) {

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Adding configuration: " + configuration);
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
            I_CmsXmlConfiguration configuration = (I_CmsXmlConfiguration)m_configurations.get(i);
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
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public Map getConfiguration() {

        return m_legacyConfiguration;
    }

    /**
     * Returns a specific configuration from the list of initialized configurations.<p>
     * 
     * @param clazz the configuration class that should be returned
     * @return the initialized configuration class instance, or <code>null</code> if this is not found
     */
    public I_CmsXmlConfiguration getConfiguration(Class clazz) {

        for (int i = 0; i < m_configurations.size(); i++) {
            I_CmsXmlConfiguration configuration = (I_CmsXmlConfiguration)m_configurations.get(i);
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
    public List getConfigurations() {

        return m_configurations;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return C_DTD_FILE_NAME;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdSystemLocation()
     */
    public String getDtdSystemLocation() {

        return C_DEFAULT_DTD_LOCATION;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdUrlPrefix()
     */
    public String getDtdUrlPrefix() {

        return C_DEFAULT_DTD_PREFIX;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getXmlFileName()
     */
    public String getXmlFileName() {

        return C_DEFAULT_XML_FILE_NAME;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // does not need to be initialized
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("initConfiguration() called on " + this);
        }
    }

    /**
     * Loads the OpenCms configuration from the given XML file.<p>
     * 
     * @throws SAXException in case of XML parse errors
     * @throws IOException in case of file IO errors
     */
    public void loadXmlConfiguration() throws SAXException, IOException {

        URL baseUrl = m_baseFolder.toURL();
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Base URL is " + baseUrl);
        }

        // first load the base configuration
        loadXmlConfiguration(baseUrl, this);

        // now iterate all sub-configrations
        Iterator i = m_configurations.iterator();
        while (i.hasNext()) {
            I_CmsXmlConfiguration config = (I_CmsXmlConfiguration)i.next();
            loadXmlConfiguration(baseUrl, config);
        }

        // remove the old backups
        removeOldBackups(C_MAX_BACKUP_DAYS);
    }

    /**
     * Sets the configuration read from the legacy "opencms.properties".<p>
     * 
     * @param legacyConfiguration the configuration read from the legacy "opencms.properties"
     */
    public void setConfiguration(Map legacyConfiguration) {
        
        m_legacyConfiguration = legacyConfiguration;
    }
    
    /**
     * Writes the XML configuration for the provided configuration instance.<p>
     * 
     * @param clazz the configuration class to write the XML for
     * @throws IOException in case of I/O errors while writing
     * @throws CmsConfigurationException in case the given class is not a valid configuration classe
     */
    public void writeConfiguration(Class clazz) throws IOException, CmsConfigurationException {

        I_CmsXmlConfiguration configuration = getConfiguration(clazz);
        if (configuration == null) {
            throw new CmsConfigurationException("Configuration manager write requested for unknown class '"
                + clazz.getName()
                + "'", CmsConfigurationException.C_CONFIGURATION_ERROR);
        }

        // generate the file URL for the XML input
        File file = new File(m_baseFolder, configuration.getXmlFileName());
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("XML output file URL is " + file.getAbsolutePath());
        }

        // generate the XML document 
        Document config = generateXml(configuration);

        // output the document
        XMLWriter writer = null;
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setIndentSize(4);
        format.setTrimText(false);
        format.setEncoding("UTF-8");

        try {
            OutputStream out = new FileOutputStream(file);
            writer = new XMLWriter(out, format);
            writer.write(config);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        if (OpenCms.getLog(this).isInfoEnabled()) {
            OpenCms.getLog(this).info(
                "Successfully updated XML configuration file "
                    + file.getAbsolutePath()
                    + " for class '"
                    + configuration.getClass().getName()
                    + "'");
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

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Creating configuration backup of " + fromName + " in " + toName);
        }

        try {
            CmsFileUtil.copy(fromName, toName);
        } catch (IOException e) {
            OpenCms.getLog(this).error("Could not generate configuration backup file " + toName, e);
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
                String file = CmsFileUtil.readFile(configuration.getDtdSystemLocation()
                    + configuration.getDtdFilename(), CmsEncoder.C_UTF8_ENCODING);
                CmsXmlEntityResolver.cacheSystemId(
                    configuration.getDtdUrlPrefix() + configuration.getDtdFilename(),
                    file.getBytes(CmsEncoder.C_UTF8_ENCODING));
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug(
                        "Cached DTD system id: "
                            + configuration.getDtdUrlPrefix()
                            + configuration.getDtdFilename()
                            + " --> "
                            + configuration.getDtdSystemLocation()
                            + configuration.getDtdFilename());
                }
            } catch (IOException e) {
                OpenCms.getLog(this).error(
                    "Unable to cache DTD for file: "
                        + configuration.getDtdSystemLocation()
                        + configuration.getDtdFilename(),
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
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("XML input file URL is " + fileUrl);
        }

        // create a backup of the configuration
        backupXmlConfiguration(configuration);

        // instantiate Digester and enable XML validation
        m_digester = new Digester();
        m_digester.setValidating(true);
        m_digester.setEntityResolver(new CmsXmlEntityResolver(null));
        m_digester.setRuleNamespaceURI(null);
        m_digester.setErrorHandler(new CmsXmlErrorHandler());

        // add this class to the Digester
        m_digester.push(configuration);

        configuration.addXmlDigesterRules(m_digester);

        // start the parsing process        
        m_digester.parse(fileUrl.openStream());
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
            if ((lastMod < maxAge) & (!file.getAbsolutePath().endsWith(".ori"))) {
                file.delete();
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Deleting old configuration " + file.getAbsolutePath());
                }
            }
        }
    }
}