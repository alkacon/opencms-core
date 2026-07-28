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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
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

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * File system based storage for generated image cache entries.<p>
 */
public class CmsFsImageCache extends CmsRfsImageCache {

    /** Maximum number of file lengths kept locally. */
    private static final int LENGTH_CACHE_MAX_SIZE = 10000;

    /** Number of minutes after which a cached file length expires. */
    private static final int LENGTH_CACHE_EXPIRY_MINUTES = 10;

    /** Locally cached file lengths. */
    private Cache<Path, Long> m_lengthCache = CacheBuilder.newBuilder().maximumSize(
        LENGTH_CACHE_MAX_SIZE).expireAfterAccess(LENGTH_CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES).build();

    /**
     * Creates a new FS image cache.<p>
     *
     * @param path the file system path
     * @throws Exception if the path can not be initialized
     */
    public CmsFsImageCache(String path)
    throws Exception {

        super(path);
        validateAvailable();
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#close()
     */
    @Override
    public void close() throws Exception {

        m_lengthCache.invalidateAll();
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#exists(java.lang.String)
     */
    @Override
    public boolean exists(String key) throws Exception {

        Path path = getPath(key);
        try {
            BasicFileAttributes attributes = Files.readAttributes(
                path,
                BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
            if (attributes.isRegularFile()) {
                m_lengthCache.put(path, Long.valueOf(attributes.size()));
                return true;
            }
            m_lengthCache.invalidate(path);
            return false;
        } catch (NoSuchFileException e) {
            m_lengthCache.invalidate(path);
            return false;
        }
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#getLength(java.lang.String)
     */
    @Override
    public long getLength(String key) throws Exception {

        Path path = getPath(key);
        Long cachedLength = m_lengthCache.getIfPresent(path);
        if (cachedLength != null) {
            return cachedLength.longValue();
        }
        long length = Files.size(path);
        m_lengthCache.put(path, Long.valueOf(length));
        return length;
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#write(java.lang.String, byte[])
     */
    @Override
    public void write(String key, byte[] content) throws Exception {

        Path path = getPath(key);
        Path parent = path.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, ".opencms-image-", ".tmp");
        try {
            Files.write(temporaryFile, content, StandardOpenOption.TRUNCATE_EXISTING);
            Files.move(temporaryFile, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            temporaryFile = null;
            m_lengthCache.put(path, Long.valueOf(content.length));
        } finally {
            if (temporaryFile != null) {
                Files.deleteIfExists(temporaryFile);
            }
        }
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#writeRangeTo(java.lang.String, long, long, java.io.OutputStream)
     */
    @Override
    public void writeRangeTo(String key, long start, long length, OutputStream out) throws Exception {

        Path path = getPath(key);
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            channel.position(start);
            byte[] buffer = new byte[8192];
            long remaining = length;
            while (remaining > 0) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, (int)Math.min(buffer.length, remaining));
                int read = channel.read(byteBuffer);
                if (read < 0) {
                    return;
                }
                out.write(buffer, 0, read);
                remaining -= read;
            }
        } catch (Exception e) {
            m_lengthCache.invalidate(path);
            throw e;
        }
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#writeTo(java.lang.String, java.io.OutputStream)
     */
    @Override
    public void writeTo(String key, OutputStream out) throws Exception {

        Path path = getPath(key);
        try {
            Files.copy(path, out);
        } catch (Exception e) {
            m_lengthCache.invalidate(path);
            throw e;
        }
    }

    /**
     * Returns the cache path for a key.<p>
     *
     * @param key the image cache key
     * @return the cache path
     */
    protected Path getPath(String key) {

        String normalizedKey = key;
        while (normalizedKey.startsWith("/")) {
            normalizedKey = normalizedKey.substring(1);
        }
        Path repository = getRepository();
        Path result = repository.resolve(normalizedKey).normalize();
        if (!result.startsWith(repository)) {
            throw new IllegalArgumentException("Image cache key outside repository: " + key);
        }
        return result;
    }

    /**
     * Validates read and write access to the configured file system repository.<p>
     *
     * @throws Exception if validation fails
     */
    private void validateAvailable() throws Exception {

        byte[] content = "OpenCms image cache health check".getBytes(StandardCharsets.UTF_8);
        String key = ".opencms-healthcheck-" + UUID.randomUUID().toString();
        Path testFile = getPath(key);
        try {
            write(key, content);
            if (!Arrays.equals(content, Files.readAllBytes(testFile))) {
                throw new IllegalStateException("FS image cache health check returned different content.");
            }
        } finally {
            Files.deleteIfExists(testFile);
            m_lengthCache.invalidate(testFile);
        }
    }
}
