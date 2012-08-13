/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.solr;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.schema.IndexSchema;

import org.xml.sax.InputSource;

/**
 * The Solr configuration class.<p>
 * 
 * @since 8.5.0
 */
public class CmsSolrConfiguration {

    /** The 'conf' folder inside the Solr home directory. */
    public static final String CONF_FOLDER = File.separatorChar + "conf" + File.separatorChar;

    /** The Solr configuration file name. */
    public static final String SOLR_CONFIG_FILE = "solr.xml";

    /** The default name of the Solr home directory. */
    public static final String SOLR_HOME_DEFAULT = "solr" + File.separatorChar;

    /** The system property name for the Solr home directory. */
    public static final String SOLR_HOME_PROPERTY = "solr.solr.home";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrConfiguration.class);

    /** Signals whether the server should be embedded or if to use an external. */
    private boolean m_embedded;

    /** The Solr home. */
    private String m_home;

    /** The schema file. */
    private IndexSchema m_schema;

    /** The servers URL, must be set if embedded is false. */
    private URL m_serverUrl;

    /** The Solr configuration. */
    private SolrConfig m_solrConfig;

    /** The file name of the Solr configuration. */
    private String m_solrFileName;

    /**
     * Default constructor.<p>
     */
    public CmsSolrConfiguration() {

        // needed for the digester
    }

    /**
     * Returns the home directory of Solr as String.<p>
     * 
     * @return the home directory of Solr as String
     */
    public String getHome() {

        if (m_home == null) {
            if (CmsStringUtil.isNotEmpty(System.getProperty(SOLR_HOME_PROPERTY))) {
                m_home = System.getProperty(SOLR_HOME_PROPERTY);
            } else {
                m_home = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(SOLR_HOME_DEFAULT);
            }
            m_home = (m_home.endsWith(File.separator)
            ? m_home.substring(0, m_home.lastIndexOf(File.separator))
            : m_home);
        }
        return m_home;
    }

    /**
     * Returns the servers URL if embedded is set to <code>false</code>
     * 
     * @return the external servers URL
     */
    public URL getServerUrl() {

        return m_serverUrl;
    }

    /**
     * Returns the Solr configuration (object representation of <code>'solrconfig.xml'</code>).<p>
     * 
     * @return the Solr configuration
     * 
     */
    public SolrConfig getSolrConfig() {

        if (m_solrConfig == null) {
            try {
                InputSource solrConfig = new InputSource(new FileInputStream(getSolrConfigFile()));
                m_solrConfig = new SolrConfig(m_home, null, solrConfig);
            } catch (FileNotFoundException e) {
                CmsConfigurationException ex = new CmsConfigurationException(Messages.get().container(
                    Messages.ERR_SOLR_CONFIG_XML_NOT_FOUND_1,
                    getSolrConfigFile().getPath()), e);
                LOG.error(ex.getLocalizedMessage(), ex);
            } catch (Exception e) {
                CmsConfigurationException ex = new CmsConfigurationException(Messages.get().container(
                    Messages.ERR_SOLR_CONFIG_XML_NOT_READABLE_1,
                    getSolrConfigFile().getPath()), e);
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }
        return m_solrConfig;
    }

    /**
     * Returns the solr configuration file, default: <code>'conf/solrconfig.xml'</code>.<p>
     * 
     * @return the solr configuration file
     */
    public File getSolrConfigFile() {

        return new File(getHome() + CONF_FOLDER + SolrConfig.DEFAULT_CONF_FILE);
    }

    /**
     * Returns the Solr xml file, default: <code>'solr.xml'</code>.<p>
     * 
     * @return the Solr xml file
     */
    public File getSolrFile() {

        if (m_solrFileName == null) {
            m_solrFileName = SOLR_CONFIG_FILE;
        }
        return new File(getHome() + File.separator + m_solrFileName);
    }

    /**
     * Returns the Solr xml file name, default: <code>'solr.xml'</code>.<p>
     * 
     * @return the Solr xml file name
     */
    public String getSolrFileName() {

        return m_solrFileName;
    }

    /**
     * Returns the Solr index schema.<p>
     * 
     * @return the Solr index schema
     */
    public IndexSchema getSolrSchema() {

        if (m_schema == null) {
            try {
                InputSource solrSchema = new InputSource(new FileInputStream(getSolrSchemaFile()));
                m_schema = new IndexSchema(getSolrConfig(), null, solrSchema);
            } catch (FileNotFoundException e) {
                CmsConfigurationException ex = new CmsConfigurationException(Messages.get().container(
                    Messages.ERR_SOLR_SCHEMA_XML_NOT_FOUND_1,
                    getSolrSchemaFile().getPath()), e);
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }
        return m_schema;
    }

    /** 
     * Returns the Solr index schema file.<p>
     * 
     * @return the Solr index schema file
     */
    public File getSolrSchemaFile() {

        return new File(getHome() + CONF_FOLDER + IndexSchema.DEFAULT_SCHEMA_FILE);
    }

    /**
     * Returns <code>true</code> if the Solr server is embedded, <code>false</code> otherwise.<p>
     * 
     * @return <code>true</code> if the Solr server is embedded, <code>false</code> otherwise
     */
    public boolean isEmbedded() {

        return m_embedded;
    }

    /**
     * Sets the embedded flag.<p>
     * 
     * Set it to <code>true</code>, if the embedded Solr server should be used, <code>false</code> otherwise.<p>
     * 
     * @param isEmbedded <code>true</code>, if the embedded Solr server should be used, <code>false</code> otherwise
     */
    public void setEmbedded(String isEmbedded) {

        m_embedded = Boolean.valueOf(isEmbedded).booleanValue();
    }

    /**
     * Sets the home folder for Solr.<p>
     * 
     * @param homeFolderPath the Solr home folder to set 
     */
    public void setHomeFolderPath(String homeFolderPath) {

        m_home = homeFolderPath;
    }

    /**
     * Sets the external servers URL, should be not null if the embedded falg is <code>false</code>.<p>
     * 
     * @param url the URL
     * 
     * @throws CmsConfigurationException if somehting goes wrong
     */
    public void setServerUrl(String url) throws CmsConfigurationException {

        try {
            m_serverUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_SOLR_BAD_SERVER_URL_1,
                m_serverUrl), e);
        }
    }

    /**
     * Sets the Solr file name.<p>
     * 
     * @param name the file name to set
     */
    public void setSolrFileName(String name) {

        m_solrFileName = name;
    }
}