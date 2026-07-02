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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.loader;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * RFS based storage for generated image cache entries.<p>
 */
public class CmsRfsImageCache implements I_CmsImageCache, I_CmsConfigurationParameterHandler {

    /** Configuration parameter for the image cache folder. */
    public static final String PARAM_FOLDER = "folder";

    /** The image cache repository. */
    private Path m_repository;

    /** The configuration parameters. */
    private CmsParameterConfiguration m_configuration = new CmsParameterConfiguration();

    /**
     * Creates a new uninitialized RFS image cache.<p>
     */
    public CmsRfsImageCache() {

        // empty
    }

    /**
     * Creates a new RFS image cache.<p>
     *
     * @param repository the image cache repository path
     * @throws Exception if the repository can not be initialized
     */
    public CmsRfsImageCache(String repository)
    throws Exception {

        initRepository(Paths.get(repository).toAbsolutePath().normalize());
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_configuration.add(paramName, paramValue);
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#exists(java.lang.String)
     */
    public boolean exists(String key) throws Exception {

        return Files.isRegularFile(getPath(key), LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_configuration;
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#getLength(java.lang.String)
     */
    public long getLength(String key) throws Exception {

        return Files.size(getPath(key));
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {

        String folder = m_configuration.get(PARAM_FOLDER);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(folder)) {
            folder = CmsImageLoader.IMAGE_REPOSITORY_DEFAULT;
        }
        try {
            initRepository(resolveRepositoryFolder(folder));
        } catch (Exception e) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMAGE_CACHE_INIT_1, getClass().getName()),
                e);
        }
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#supportsRangeDelivery()
     */
    public boolean supportsRangeDelivery() {

        return true;
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#write(java.lang.String, byte[])
     */
    public void write(String key, byte[] content) throws Exception {

        Path path = getPath(key);
        Files.createDirectories(path.getParent());
        Files.write(path, content);
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#writeRangeTo(java.lang.String, long, long, java.io.OutputStream)
     */
    public void writeRangeTo(String key, long start, long length, OutputStream out) throws Exception {

        try (InputStream in = Files.newInputStream(getPath(key))) {
            skipFully(in, start);
            byte[] buffer = new byte[8192];
            long remaining = length;
            while (remaining > 0) {
                int read = in.read(buffer, 0, (int)Math.min(buffer.length, remaining));
                if (read < 0) {
                    return;
                }
                out.write(buffer, 0, read);
                remaining -= read;
            }
        }
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#writeTo(java.lang.String, java.io.OutputStream)
     */
    public void writeTo(String key, OutputStream out) throws Exception {

        Files.copy(getPath(key), out);
    }

    /**
     * Initializes the image cache repository.<p>
     *
     * @param repository the image cache repository
     *
     * @throws Exception if the repository can not be initialized
     */
    protected void initRepository(Path repository) throws Exception {

        m_repository = repository;
        Files.createDirectories(m_repository);
        if (!Files.isDirectory(m_repository, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("Image cache path is not a directory: " + m_repository);
        }
    }

    /**
     * Returns the cache path for a key.<p>
     *
     * @param key the image cache key
     * @return the cache path
     */
    private Path getPath(String key) {

        if (m_repository == null) {
            throw new IllegalStateException("Image cache has not been initialized.");
        }
        String normalizedKey = key;
        while (normalizedKey.startsWith("/")) {
            normalizedKey = normalizedKey.substring(1);
        }
        Path result = m_repository.resolve(normalizedKey).normalize();
        if (!result.startsWith(m_repository)) {
            throw new IllegalArgumentException("Image cache key outside repository: " + key);
        }
        return result;
    }

    /**
     * Resolves a classic RFS image cache folder relative to the web application root.<p>
     *
     * @param folder the configured image cache folder
     * @return the resolved repository folder
     */
    private Path resolveRepositoryFolder(String folder) {

        return Paths.get(
            OpenCms.getSystemInfo().getWebApplicationRfsPath(),
            folder.replace('/', File.separatorChar)).toAbsolutePath().normalize();
    }

    /**
     * Skips exactly the requested number of bytes.<p>
     *
     * @param in the input stream
     * @param bytes the number of bytes to skip
     * @throws Exception if skipping fails
     */
    private void skipFully(InputStream in, long bytes) throws Exception {

        long remaining = bytes;
        while (remaining > 0) {
            long skipped = in.skip(remaining);
            if (skipped > 0) {
                remaining -= skipped;
            } else if (in.read() < 0) {
                return;
            } else {
                remaining--;
            }
        }
    }
}
