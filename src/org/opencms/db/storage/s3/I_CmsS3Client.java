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
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /** Visitor for S3 object metadata. */
    @FunctionalInterface
    interface I_CmsS3ObjectMetadataVisitor {

        /**
         * Visits S3 object metadata.<p>
         *
         * @param metadata the object metadata
         * @throws Exception if the visitor fails
         */
        void visit(CmsS3ObjectMetadata metadata) throws Exception;
    }

    /**
     * Visitor for S3 objects.<p>
     */
    interface I_CmsS3ObjectVisitor {

        /**
         * Visits an S3 object.<p>
         *
         * @param key the object key
         * @param length the object length
         * @throws Exception if the visitor fails
         */
        void visit(String key, long length) throws Exception;
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
     * Deletes multiple objects.<p>
     *
     * The optimized S3 implementation supports at most 1000 keys per call.
     * This default implementation preserves compatibility for other clients.<p>
     *
     * @param keys the object keys
     * @return the per-object delete result
     * @throws Exception if the complete request fails
     */
    default CmsS3DeleteResult deleteObjects(List<String> keys) throws Exception {

        List<String> requestedKeys = new ArrayList<String>(keys);
        Map<String, Exception> failures = new LinkedHashMap<String, Exception>();
        for (String key : requestedKeys) {
            try {
                deleteObject(key);
            } catch (Exception e) {
                failures.put(key, e);
            }
        }
        return new CmsS3DeleteResult(requestedKeys, failures);
    }

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
     * Returns the length of an object's content if the object exists.<p>
     *
     * @param key the identifier for the object
     * @return the object content length, or {@code -1} if the object does not exist
     * @throws Exception if the object can not be accessed
     */
    default long getObjectLengthIfExists(String key) throws Exception {

        return exists(key) ? getObjectLength(key) : -1;
    }

    /**
     * Returns an object's metadata.<p>
     *
     * @param key the identifier for the object
     * @return the object metadata
     * @throws Exception if the object can not be accessed
     */
    default CmsS3ObjectMetadata getObjectMetadata(String key) throws Exception {

        return new CmsS3ObjectMetadata(key, getObjectLength(key), null, null);
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
     * Renews an object by conditionally copying it onto itself.<p>
     *
     * The copy must only be applied when the source still has the expected revision. A missing object or revision
     * mismatch is not an error and is reported by returning {@code false}. The S3 last-modified time is always
     * assigned by the storage server.<p>
     *
     * @param key the object key
     * @param expectedRevision the expected source revision
     * @param renewalTime the requested renewal time
     * @return {@code true} if the object was renewed, or {@code false} if it no longer matched
     * @throws Exception if the copy fails
     */
    default boolean renewObject(String key, String expectedRevision, Instant renewalTime) throws Exception {

        throw new UnsupportedOperationException("S3 object renewal is not supported by this client.");
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
     * Visits all objects in the configured bucket.<p>
     *
     * @param visitor the object visitor
     * @throws Exception if listing fails
     */
    default void visitObjects(I_CmsS3ObjectVisitor visitor) throws Exception {

        visitObjectKeys(key -> visitor.visit(key, getObjectLength(key)));
    }

    /**
     * Visits object metadata for objects matching an optional prefix.<p>
     *
     * This compatibility implementation filters client-side and can not provide
     * timestamps or revisions. Optimized clients should override it.<p>
     *
     * @param prefix the object key prefix, or {@code null}
     * @param visitor the metadata visitor
     * @throws Exception if listing fails
     */
    default void visitObjects(String prefix, I_CmsS3ObjectMetadataVisitor visitor) throws Exception {

        visitObjects((key, length) -> {
            if ((prefix == null) || key.startsWith(prefix)) {
                visitor.visit(new CmsS3ObjectMetadata(key, length, null, null));
            }
        });
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
     * Writes an object byte range and returns the metadata received with the object response.<p>
     *
     * Optimized clients should override this method so that no additional metadata request is needed. The default
     * implementation preserves compatibility by reading the range first and loading the metadata afterwards.<p>
     *
     * @param key the identifier for the object
     * @param start the first byte to write
     * @param length the number of bytes to write
     * @param out the output stream to write to
     * @return the object metadata
     * @throws Exception if the object cannot be retrieved or written
     */
    default CmsS3ObjectMetadata writeObjectRangeToWithMetadata(String key, long start, long length, OutputStream out)
    throws Exception {

        writeObjectRangeTo(key, start, length, out);
        return getObjectMetadata(key);
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

    /**
     * Writes an object's content and returns the metadata received with the object response.<p>
     *
     * Optimized clients should override this method so that no additional metadata request is needed. The default
     * implementation preserves compatibility by reading the object first and loading the metadata afterwards.<p>
     *
     * @param key the identifier for the object
     * @param out the output stream to write to
     * @return the object metadata
     * @throws Exception if the object cannot be retrieved or written
     */
    default CmsS3ObjectMetadata writeObjectToWithMetadata(String key, OutputStream out) throws Exception {

        writeObjectTo(key, out);
        return getObjectMetadata(key);
    }
}
