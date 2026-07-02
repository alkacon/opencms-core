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

package org.opencms.db.storage.s3;

import org.opencms.file.I_CmsFileContentStreamHandler;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;

/**
 * Interface for S3 storage operations.<p>
 *
 * Provides a simplified abstraction for interacting with S3-compatible
 * storage backends like MinIO or AWS S3.<p>
 */
public interface I_CmsS3Client extends AutoCloseable {

    /**
     * Visitor for S3 object keys.<p>
     */
    interface I_CmsS3ObjectKeyVisitor {

        /**
         * Visits an S3 object key.<p>
         *
         * @param key the object key
         * @throws Exception if the visitor fails
         */
        void visit(String key) throws Exception;
    }

    /**
     * Closes the client and releases associated resources.<p>
     *
     * @throws Exception if closing the client fails
     */
    default void close() throws Exception {

        // default no-op
    }

    /**
     * Deletes an object from the specified bucket.<p>
     *
     * @param key the identifier for the object to delete
     * @throws Exception if the deletion fails
     */
    void deleteObject(String key) throws Exception;

    /**
     * Checks if an object exists in the specified bucket.<p>
     *
     * @param key the identifier for the object
     * @return true if the object exists, false otherwise
     * @throws Exception if the existence check fails
     */
    boolean exists(String key) throws Exception;

    /**
     * Retrieves an object's content from the specified bucket.<p>
     *
     * @param key the identifier for the object
     * @return the binary data of the object
     * @throws Exception if the object cannot be retrieved or does not exist
     */
    byte[] getObject(String key) throws Exception;

    /**
     * Returns the length of an object's content.<p>
     *
     * @param key the identifier for the object
     * @return the object content length
     * @throws Exception if the object can not be accessed
     */
    default long getObjectLength(String key) throws Exception {

        return getObject(key).length;
    }

    /**
     * Uploads an object to the specified bucket.<p>
     *
     * @param key the unique identifier (path) for the object
     * @param content the binary data to upload
     * @throws Exception if the upload fails
     */
    void putObject(String key, byte[] content) throws Exception;

    /**
     * Passes an object's content from the specified bucket to the given input stream handler.<p>
     *
     * @param key the identifier for the object
     * @param handler the stream handler
     * @throws Exception if the object cannot be retrieved or handled
     */
    default void readObjectFrom(String key, I_CmsFileContentStreamHandler handler) throws Exception {

        try (ByteArrayInputStream in = new ByteArrayInputStream(getObject(key))) {
            handler.read(in);
        }
    }

    /**
     * Validates that the configured bucket is accessible.<p>
     *
     * @throws Exception if the bucket can not be accessed
     */
    default void validateBucketAccess() throws Exception {

        // default no-op
    }

    /**
     * Visits all object keys in the configured bucket.<p>
     *
     * @param visitor the object key visitor
     * @throws Exception if listing fails
     */
    default void visitObjectKeys(I_CmsS3ObjectKeyVisitor visitor) throws Exception {

        throw new UnsupportedOperationException("S3 object listing is not supported by this client.");
    }

    /**
     * Writes an object byte range from the specified bucket to the given output stream.<p>
     *
     * @param key the identifier for the object
     * @param start the first byte to write
     * @param length the number of bytes to write
     * @param out the output stream to write to
     * @throws Exception if the object cannot be retrieved or written
     */
    default void writeObjectRangeTo(String key, long start, long length, OutputStream out) throws Exception {

        if ((start < 0) || (length < 1) || ((Long.MAX_VALUE - start) < length)) {
            throw new IllegalArgumentException("Invalid byte range: start=" + start + ", length=" + length);
        }
        byte[] content = getObject(key);
        if (start >= content.length) {
            return;
        }
        int offset = (int)start;
        int rangeLength = (int)Math.min(length, content.length - start);
        out.write(content, offset, rangeLength);
    }

    /**
     * Writes an object's content from the specified bucket to the given output stream.<p>
     *
     * @param key the identifier for the object
     * @param out the output stream to write to
     * @throws Exception if the object cannot be retrieved or written
     */
    default void writeObjectTo(String key, OutputStream out) throws Exception {

        out.write(getObject(key));
    }
}
