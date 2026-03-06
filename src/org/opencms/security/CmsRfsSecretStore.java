/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.security;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.crypto.CmsAESCBCTextEncryption;
import org.opencms.crypto.I_CmsTextEncryption;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;

/**
 * RFS secret store which just loads secrets from a .properties file, whose path is configured via the 'path' parameter.
 *
 * <p>This class already initializes itself in the initConfiguration() method, which means it can return secrets before the initialize()
 * method (which does nothing) is even called.
 *
 * <p>When OpenCms is running, this class will also track modifications in the configured secrets file and reload it.
 */
public class CmsRfsSecretStore implements I_CmsSecretStore {

    /**
     * Tracks modifications of the secrets file.
     */
    class WatchThread extends Thread {

        public WatchThread() {

            super("CmsRfsSecretStore.WatchThread");
            // needs to shut down when OpenCms shuts down
            setDaemon(true);
        }

        public void run() {

            try (WatchService watch = FileSystems.getDefault().newWatchService()) {
                m_path.getParent().register(
                    watch,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
                while (true) {
                    try {
                        WatchKey watchKey = watch.take();
                        try {
                            List<WatchEvent<?>> events = watchKey.pollEvents();
                            for (WatchEvent<?> event : events) {
                                if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                                    continue;
                                }
                                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                                if (ev.context().getFileName().equals(m_path.getFileName())) {
                                    m_needsReload.set(true);
                                }
                            }
                        } finally {
                            if (!watchKey.reset()) {
                                LOG.error(
                                    "Watch key for " + m_path.getParent() + " has become invalid, stop tracking it");
                                return;
                            }
                        }
                    } catch (InterruptedException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }

            } catch (IOException | UnsupportedOperationException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /** The parameter used to configure the path of the properties file. */
    public static final String PARAM_PATH = "path";

    /** The parameter for the encryption password (use no encryption if not set) .*/
    public static final String PARAM_PASSWORD = "password";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRfsSecretStore.class);

    /** The configuration. */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /** True when the secrets file has changed and not been reloaded. */
    private AtomicBoolean m_needsReload = new AtomicBoolean(false);

    /** The path of the secrets file. */
    private Path m_path;

    /** The properties. */
    private volatile Properties m_properties;

    /** The encryption used to decrypt the property values (may be null). */
    private I_CmsTextEncryption m_encryption;

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String key, String value) {

        m_config.add(key, value);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    @Override
    public CmsParameterConfiguration getConfiguration() {

        return m_config;
    }

    /**
     * @see org.opencms.security.I_CmsSecretStore#getSecret(java.lang.String)
     */
    @Override
    public String getSecret(String key) {

        return m_properties.getProperty(key);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    @Override
    public void initConfiguration() throws CmsConfigurationException {

        m_path = Path.of(m_config.get(PARAM_PATH));
        m_properties = new Properties();
        Properties props = new Properties();
        String password = m_config.get(PARAM_PASSWORD);
        if (password != null) {
            m_encryption = new CmsAESCBCTextEncryption(password);
        }
        try (InputStream stream = new FileInputStream(m_path.toString())) {
            props.load(stream);
            m_properties = decrypt(props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @see org.opencms.security.I_CmsSecretStore#initialize(org.opencms.file.CmsObject)
     */
    @Override
    public void initialize(CmsObject cmsObject) {

        WatchThread thread = new WatchThread();
        thread.start();
        OpenCms.getExecutor().scheduleWithFixedDelay(this::checkReload, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Reloads the properties file, if necessary.
     */
    private void checkReload() {

        if (m_needsReload.compareAndSet(true, false)) {
            LOG.info("Reloading secrets file...");
            Properties props = new Properties();
            try (InputStream stream = new FileInputStream(m_path.toString())) {
                props.load(stream);
                m_properties = decrypt(props);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }

        }
    }

    /**
     * Decrypts the properties if necessary.
     *
     * @param originalProps the original properties
     * @return the decrypted properties, or the original ones if no decryption is needed
     */
    private Properties decrypt(Properties originalProps) {

        if (m_encryption != null) {
            Properties result = new Properties();
            for (String propName : originalProps.stringPropertyNames()) {
                try {
                    String encryptedValue = originalProps.getProperty(propName);
                    String decryptedValue = m_encryption.decrypt(encryptedValue);
                    result.setProperty(propName, decryptedValue);
                } catch (Exception e) {
                    LOG.error("Can't decrypt encrypted property " + propName, e);
                }
            }
            return result;
        } else {
            return originalProps;
        }
    }

}
