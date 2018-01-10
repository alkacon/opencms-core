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

package org.opencms.letsencrypt;

import org.opencms.main.CmsLog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;

/**
 * Updates the certificate configuration for the LetsEncrypt container.<p>
 */
public class CmsLetsEncryptUpdater implements I_CmsLetsEncryptUpdater {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLetsEncryptUpdater.class);

    /** The LetsEncrypt configuration. */
    private CmsLetsEncryptConfiguration m_config;

    /**
     * Creates a new instance.<p>
     *
     * @param config the configuration
     */
    public CmsLetsEncryptUpdater(CmsLetsEncryptConfiguration config) {
        m_config = config;
    }

    /**
     * @see org.opencms.letsencrypt.I_CmsLetsEncryptUpdater#update(java.lang.String)
     */
    public boolean update(String certConfig) {

        if (m_config.isValidAndEnabled()) {
            LOG.debug("Trying to write certificate configuration: " + certConfig);
            String certConfigPath = m_config.getCertConfigPath();
            try (FileOutputStream fos = new FileOutputStream(certConfigPath)) {
                fos.write(certConfig.getBytes("UTF-8"));
            } catch (IOException e) {
                LOG.error("Error writing certificate configuration: " + e.getLocalizedMessage(), e);
                return false;
            }
            String host = m_config.getHost();
            int port = m_config.getPort();
            try (Socket socket = new Socket(host, port)) {
                socket.getOutputStream().write("update".getBytes("UTF-8"));
            } catch (Exception e) {
                LOG.error("Couldn't notify LetsEncrypt container: " + e.getLocalizedMessage(), e);
                return false;
            }
            return true;
        } else {
            LOG.info("LetsEncrypt configuration is invalid or disabled. ");
            return false;
        }
    }

}
