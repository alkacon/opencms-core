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

package org.opencms.db.storage;

import org.opencms.db.CmsDbContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * File system based storage implementation.<p>
 *
 * This implementation stores binary content as files in the local file system.
 * It uses a 3-level directory structure based on the content hash to avoid
 * having too many files in a single directory.<p>
 */
public class CmsFsStorage extends A_CmsStorage implements I_CmsEnumerableStorage {

    /** The type name of the storage implementation. */
    public static final String STORAGE_TYPE = "fs";

    /** The base path of the storage repository. */
    private final Path m_repository;

    /** The configured backend id. */
    private final String m_name;

    /**
     * Creates a new file system storage.<p>
     *
     * @param name the configured backend id used as storage identifier
     * @param basePath the base path for the storage repository
     */
    public CmsFsStorage(String name, String basePath) {

        m_name = name;
        m_repository = Paths.get(basePath).toAbsolutePath().normalize();
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#deleteContent(org.opencms.db.CmsDbContext, java.lang.String)
     */
    @Override
    public void deleteContent(CmsDbContext dbc, String hash) throws Exception {

        Path file = getContentPath(hash);
        if (Files.exists(file, LinkOption.NOFOLLOW_LINKS) && !Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException(Messages.get().getBundle().key(Messages.ERR_STORAGE_PATH_NOT_REGULAR_FILE_1, file));
        }
        if (Files.deleteIfExists(file)) {
            deleteEmptyParents(file.getParent());
        }
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#getStorageIdentifier()
     */
    @Override
    public String getStorageIdentifier() {

        return m_name;
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#loadContent(org.opencms.db.CmsDbContext, java.lang.String)
     */
    @Override
    public byte[] loadContent(CmsDbContext dbc, String hash) throws Exception {

        Path file = getContentPath(hash);
        if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new CmsStorageBlobNotFoundException(
                Messages.get().getBundle().key(Messages.ERR_STORAGE_BLOB_MISSING_2, getStorageIdentifier(), hash));
        }
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException(Messages.get().getBundle().key(Messages.ERR_STORAGE_PATH_NOT_REGULAR_FILE_1, file));
        }
        return Files.readAllBytes(file);
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#storeContent(org.opencms.db.CmsDbContext, java.lang.String, byte[])
     */
    @Override
    public void storeContent(CmsDbContext dbc, String hash, byte[] content) throws Exception {

        if (content == null) {
            throw new IllegalArgumentException(Messages.get().getBundle().key(Messages.ERR_STORAGE_CONTENT_NULL_0));
        }
        Path file = getContentPath(hash);
        if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException(
                    Messages.get().getBundle().key(Messages.ERR_STORAGE_PATH_NOT_REGULAR_FILE_1, file));
            }
            return;
        }
        Path parent = file.getParent();
        Files.createDirectories(parent);
        Path tempFile = Files.createTempFile(parent, hash, ".tmp");
        try {
            Files.write(tempFile, content);
            Files.move(tempFile, file);
        } catch (FileAlreadyExistsException e) {
            Files.deleteIfExists(tempFile);
            if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException(
                    Messages.get().getBundle().key(Messages.ERR_STORAGE_PATH_NOT_REGULAR_FILE_1, file),
                    e);
            }
        } catch (Exception e) {
            Files.deleteIfExists(tempFile);
            throw e;
        }
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#validateAvailable(org.opencms.db.CmsDbContext)
     */
    @Override
    public void validateAvailable(CmsDbContext dbc) throws Exception {

        if (!Files.exists(m_repository, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException(
                Messages.get().getBundle().key(Messages.ERR_STORAGE_REPOSITORY_MISSING_1, m_repository));
        }
        if (!Files.isDirectory(m_repository, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException(
                Messages.get().getBundle().key(Messages.ERR_STORAGE_REPOSITORY_NOT_DIRECTORY_1, m_repository));
        }
        byte[] content = "OpenCms storage health check".getBytes(StandardCharsets.UTF_8);
        Path testFile = Files.createTempFile(m_repository, ".opencms-healthcheck-", ".tmp");
        try {
            Files.write(testFile, content);
            byte[] readContent = Files.readAllBytes(testFile);
            if (!Arrays.equals(content, readContent)) {
                throw new IOException(
                    Messages.get().getBundle().key(Messages.ERR_STORAGE_REPOSITORY_READ_WRITE_1, m_repository));
            }
        } finally {
            Files.deleteIfExists(testFile);
        }
    }

    /**
     * @see org.opencms.db.storage.I_CmsEnumerableStorage#visitContentHashes(org.opencms.db.CmsDbContext, org.opencms.db.storage.I_CmsEnumerableStorage.I_CmsContentHashVisitor)
     */
    @Override
    public void visitContentHashes(CmsDbContext dbc, I_CmsContentHashVisitor visitor) throws Exception {

        try (Stream<Path> files = Files.walk(m_repository)) {
            for (Path file : (Iterable<Path>)files::iterator) {
                if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                    continue;
                }
                String name = file.getFileName().toString();
                if (isHash(name)) {
                    visitor.visit(name.toLowerCase());
                }
            }
        }
    }

    /**
     * Deletes empty parent folders up to, but not including, the repository root.<p>
     *
     * @param start the folder to start with
     * @throws IOException if deleting an empty folder fails
     */
    private void deleteEmptyParents(Path start) throws IOException {

        Path current = start;
        while ((current != null) && !current.equals(m_repository)) {
            try {
                Files.delete(current);
            } catch (DirectoryNotEmptyException e) {
                return;
            } catch (NoSuchFileException e) {
                return;
            }
            current = current.getParent();
        }
    }

    /**
     * Resolves the storage path for the given content hash.<p>
     *
     * @param hash the content hash
     * @return the storage path
     */
    private Path getContentPath(String hash) {

        String normalizedHash = validateHash(hash);
        Path result = m_repository.resolve(normalizedHash.substring(0, 2)).resolve(
            normalizedHash.substring(2, 4)).resolve(normalizedHash.substring(4, 6)).resolve(normalizedHash).normalize();
        if (!result.startsWith(m_repository)) {
            throw new IllegalArgumentException(
                Messages.get().getBundle().key(Messages.ERR_STORAGE_HASH_OUTSIDE_REPOSITORY_0));
        }
        return result;
    }

    /**
     * Checks if the given string looks like a SHA-512 hash.<p>
     *
     * @param value the value
     * @return true if the value looks like a hash
     */
    private boolean isHash(String value) {

        if ((value == null) || (value.length() != 128)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!(((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'F')))) {
                return false;
            }
        }
        return true;
    }

}
