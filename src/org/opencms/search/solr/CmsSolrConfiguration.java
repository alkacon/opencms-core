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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.solr.core.ConfigSetService.ConfigResource;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.IndexSchemaFactory;

/**
 * The Solr configuration class.<p>
 *
 * @since 8.5.0
 */
public class CmsSolrConfiguration {

    /** The default config set path. */
    public static final String DEFAULT_CONFIGSET_FOLDER = File.separatorChar
        + "configsets"
        + File.separatorChar
        + "default";
    /** The 'conf' folder inside the Solr home directory. */
    public static final String CONF_FOLDER = File.separatorChar + "conf" + File.separatorChar;

    /** The Solr configuration file name. */
    public static final String SOLR_CONFIG_FILE = "solr.xml";

    /** The default maximum number of results to return in a Solr search. */
    public static final int DEFAULT_MAX_PROCESSED_RESULTS = 400;

    /**
     * The default max time in ms before a commit will happen (10 seconds by default).<p>
     *
     * Can be configured in 'opencms-search.xml'.<p>
     */
    public static final long SOLR_DEFAULT_COMMIT_MS = 10000;

    /** The default name of the Solr home directory. */
    public static final String SOLR_HOME_DEFAULT = "solr" + File.separatorChar;

    /** The system property name for the Solr home directory. */
    public static final String SOLR_HOME_PROPERTY = "solr.solr.home";

    /** The Solr schema name. */
    public static final String SOLR_SCHEMA_NAME = "OpenCms SOLR schema";

    /** The Solr configuration name. */
    public static final String SOLR_CONFIG_NAME = "OpenCms SOLR configuration";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrConfiguration.class);

    /** Max time (in ms) before a commit will happen. */
    private long m_commitMs = SOLR_DEFAULT_COMMIT_MS;

    /** Signals whether the server is enabled or disabled. */
    private boolean m_enabled;

    /** The Solr home. */
    private String m_home;

    /** The configured path to the Solr home. */
    private String m_homeFolderPath;

    /** The schema file. */
    private IndexSchema m_schema;

    /** The servers URL, must be set if embedded is false. */
    private String m_serverUrl;

    /** The Solr configuration. */
    private SolrConfig m_solrConfig;

    /** The Solr configuration file "solr.xml". */
    private File m_solrFile;

    /** The file name of the Solr configuration. */
    private String m_solrFileName;

    /** The maximal number of results to be processed in a search request to a Solr index. */
    private int m_maxProcessedResults = DEFAULT_MAX_PROCESSED_RESULTS;

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

        if (m_homeFolderPath == null) {
            if (CmsStringUtil.isNotEmpty(System.getProperty(SOLR_HOME_PROPERTY))) {
                m_home = System.getProperty(SOLR_HOME_PROPERTY);
            } else {
                m_home = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(SOLR_HOME_DEFAULT);
            }
            m_home = (m_home.endsWith(File.separator)
            ? m_home.substring(0, m_home.lastIndexOf(File.separator))
            : m_home);
        } else {
            m_home = m_homeFolderPath;
        }
        return m_home;
    }

    /**
     * Returns the configured Solr home.<p>
     *
     * @return the configured Solr home
     */
    public String getHomeFolderPath() {

        return m_homeFolderPath;
    }

    /**
     * Returns the maximal number of results processed when querying a Solr index.
     *
     * Each index has a configuration option to overwrite this global value.
     *
     * @return the maximal number of results processed when querying a Solr index.
     */
    public int getMaxProcessedResults() {

        return m_maxProcessedResults;
    }

    /**
     * Returns the servers URL if embedded is set to <code>false</code>.<p>
     *
     * @return the external servers URL
     */
    public String getServerUrl() {

        return m_serverUrl;
    }

    /**
     * Returns the max time (in ms) before a commit will happen.<p>
     *
     * @return the max time (in ms) before a commit will happen
     */
    public long getSolrCommitMs() {

        return m_commitMs;
    }

    /**
     * Returns the Solr configuration (object representation of <code>'solrconfig.xml'</code>).<p>
     *
     * @return the Solr configuration
     *
     */
    @SuppressWarnings("deprecation")
    public SolrConfig getSolrConfig() {

        if (m_solrConfig == null) {
            try (FileInputStream fis = new FileInputStream(getSolrConfigFile())) {
                Path instanceDir = Paths.get(getHome(), DEFAULT_CONFIGSET_FOLDER);
                @SuppressWarnings("resource")
                SolrResourceLoader loader = new SolrResourceLoader(instanceDir);
                m_solrConfig = SolrConfig.readFromResourceLoader(
                    loader,
                    getSolrConfigFile().getName(),
                    true,
                    null); // the former loader.getCoreProperties() yielded null anyway
            } catch (FileNotFoundException e) {
                CmsConfigurationException ex = new CmsConfigurationException(
                    Messages.get().container(Messages.LOG_SOLR_ERR_CONFIG_XML_NOT_FOUND_1, getSolrConfigFile()),
                    e);
                LOG.error(ex.getLocalizedMessage(), ex);
            } catch (Exception e) {
                CmsConfigurationException ex = new CmsConfigurationException(
                    Messages.get().container(Messages.LOG_SOLR_ERR_CONFIG_XML_NOT_READABLE_1, getSolrConfigFile()),
                    e);
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

        return new File(getHome() + DEFAULT_CONFIGSET_FOLDER + CONF_FOLDER + SolrConfig.DEFAULT_CONF_FILE);
    }

    /**
     * Returns the Solr xml file, default: <code>'solr.xml'</code>.<p>
     *
     * @return the Solr xml file
     */
    public File getSolrFile() {

        if (m_solrFile == null) {
            String solrFileName = m_solrFileName != null ? m_solrFileName : SOLR_CONFIG_FILE;
            m_solrFile = new File(getHome() + File.separator + solrFileName);
        }
        return m_solrFile;
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
    @SuppressWarnings("resource")
    public IndexSchema getSolrSchema() {

        if (m_schema == null) {
            try (FileInputStream fis = new FileInputStream(getSolrSchemaFile())) {
                ConfigResource configRes = IndexSchemaFactory.getConfigResource(
                    null /* only used if it's a CloudConfigSetService */,
                    fis,
                    getSolrConfig().getResourceLoader(),
                    SOLR_CONFIG_NAME);
                m_schema = new IndexSchema(
                    SOLR_SCHEMA_NAME,
                    configRes,
                    getSolrConfig().luceneMatchVersion,
                    getSolrConfig().getResourceLoader(),
                    getSolrConfig().getSubstituteProperties());
            } catch (IOException e) {
                CmsConfigurationException ex = new CmsConfigurationException(
                    Messages.get().container(
                        Messages.LOG_SOLR_ERR_SCHEMA_XML_NOT_FOUND_1,
                        getSolrSchemaFile().getPath()),
                    e);
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

        final String dir = getHome() + DEFAULT_CONFIGSET_FOLDER + CONF_FOLDER;
        //SOLR7 Schema took a new name, also removed the file extension.
        File file = new File(dir, "managed-schema");
        if (file.exists()) {
            return file;
        }

        //If use the old Schema.xml, it will automatically "upgrade" to a new filename.
        file = new File(dir, IndexSchema.DEFAULT_SCHEMA_FILE);
        return file;
    }

    /**
     * Returns <code>true</code> if the Solr server is embedded, <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if the Solr server is embedded, <code>false</code> otherwise
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Sets the enabled flag.<p>
     *
     * @param isEnabled <code>true</code>, if the Solr server should be used, <code>false</code> otherwise
     */
    public void setEnabled(String isEnabled) {

        m_enabled = Boolean.valueOf(isEnabled).booleanValue();
    }

    /**
     * Sets the home folder for Solr.<p>
     *
     * @param homeFolderPath the Solr home folder to set
     */
    public void setHomeFolderPath(String homeFolderPath) {

        m_homeFolderPath = homeFolderPath;
    }

    /**
     * Sets the maximal number of results processed for a query to a Solr index.<p>
     *
     * The globally set value can be overwritten for each index.
     *
     * @param maxProcessedResults the maximal number of results processed for a query to a Solr index.
     */
    public void setMaxProcessedResults(String maxProcessedResults) {

        try {
            m_maxProcessedResults = Integer.parseInt(maxProcessedResults);
        } catch (Exception e) {
            LOG.warn(
                "Could not parse value "
                    + maxProcessedResults
                    + " as Integer to set the limit for the number of results a Solr index can return.");
        }
        if (m_maxProcessedResults <= 0) {
            m_maxProcessedResults = DEFAULT_MAX_PROCESSED_RESULTS;
            LOG.warn(
                "The maximal number of results to return by a Solr index should be greater than 0. Reset it to the default value "
                    + DEFAULT_MAX_PROCESSED_RESULTS
                    + ".");
        }
    }

    /**
     * Sets the external servers URL, should be not null if the embedded falg is <code>false</code>.<p>
     *
     * @param url the URL
     */
    public void setServerUrl(String url) {

        m_serverUrl = url;
    }

    /**
     * Sets the max time (in ms) before a commit will happen.<p>
     *
     * @param time the time as long value
     */
    public void setSolrCommitMs(String time) {

        m_commitMs = Long.parseLong(time);
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