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

package org.opencms.jsp.userdata;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

/**
 * Class which handles loading/saving user data requests.
 *
 * <p>Currently stores user data requests in a special RFS directory, with their ID as a filename.
 * Expired user data requests are removed each hour.
 */
public class CmsUserDataRequestStore {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserDataRequestStore.class);

    /** The admin CMS object. */
    @SuppressWarnings("unused")
    private CmsObject m_adminCms;

    /** Folder to store requests in. */
    private File m_folder;

    /**
     * Creates a new instance.
     */
    public CmsUserDataRequestStore() {

        LOG.debug("Creating user data request store.");

    }

    /**
     * Checks if the key is a valid user data request key.
     *
     * @param key the key to check
     * @return true if the key is valid
     */
    public static boolean isValidKey(String key) {

        return StringUtils.isAlphanumeric(key);
    }

    /**
     * Removes expired user data requests.
     */
    public void cleanup() {

        File[] files = m_folder.listFiles();
        if (files != null) {
            for (File file : files) {
                String key = file.getName();
                try {
                    CmsUserDataRequestInfo info = load(key).orElse(null);
                    if ((info != null) && info.isExpired()) {
                        file.delete();
                    }
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }

            }
        }
    }

    /**
     * Initializes the store with an admin CMS object.
     *
     * @param adminCms the admin CMS object
     */
    public void initialize(CmsObject adminCms) {

        m_adminCms = adminCms;
        m_folder = new File(OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("userdata-requests"));
        if (!m_folder.exists()) {
            m_folder.mkdir();
        }
        OpenCms.getExecutor().scheduleWithFixedDelay(() -> {
            cleanup();
        }, 30, 30, TimeUnit.MINUTES);
    }

    /**
     * Loads the user data request with the given id.
     *
     * @param key the id
     * @return the user data request with the id, or null if it was not found
     */
    public Optional<CmsUserDataRequestInfo> load(String key) {

        if (!isValidKey(key)) {
            return Optional.empty();
        }
        File child = new File(m_folder, key);
        if (child.exists() && child.isFile()) {
            try (FileInputStream stream = new FileInputStream(child)) {
                byte[] data = CmsFileUtil.readFully(stream, false);
                return Optional.of(new CmsUserDataRequestInfo(new String(data, "UTF-8")));
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return Optional.empty();
            }
        } else {
            LOG.info("user data request " + key + " not found.");
            return Optional.empty();
        }
    }

    /**
     * Saves the user data request, with the id taken from the user data request itself.
     *
     * @param info the user data request to save
     */
    public void save(CmsUserDataRequestInfo info) {

        if (!isValidKey(info.getId())) {
            return;
        }
        File child = new File(m_folder, info.getId());
        try (FileOutputStream out = new FileOutputStream(child)) {
            out.write(info.toJson().getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
