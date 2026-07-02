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

import org.opencms.db.storage.I_CmsStorageDelivery;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsStoredContentInfo;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.util.CmsRequestUtil;
import org.opencms.workplace.CmsWorkplaceManager;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Helper for delivering externally stored VFS content directly to an HTTP response.<p>
 */
public class CmsStoredContentDeliveryHelper {

    /**
     * Delivery result.<p>
     */
    public enum DeliveryResult {

        /** The content has been delivered. */
        DELIVERED,

        /** The content can not be delivered by this helper. */
        NOT_DELIVERABLE,

        /** The response has been answered with 304. */
        NOT_MODIFIED,

        /** The response has been answered with 416. */
        RANGE_NOT_SATISFIABLE
    }

    /** Weak ETag prefix. */
    private static final String WEAK_ETAG_PREFIX = "W/";

    /** The maximum age for delivered contents in the clients cache. */
    private final long m_clientCacheMaxAge;

    /**
     * Creates a new delivery helper without explicit client cache max age.<p>
     */
    public CmsStoredContentDeliveryHelper() {

        this(-1);
    }

    /**
     * Creates a new delivery helper.<p>
     *
     * @param clientCacheMaxAge the maximum age for delivered contents in the clients cache
     */
    public CmsStoredContentDeliveryHelper(long clientCacheMaxAge) {

        m_clientCacheMaxAge = clientCacheMaxAge;
    }

    /**
     * Returns if the given content can be delivered by this helper.<p>
     *
     * @param info the stored content info
     * @param storage the delivery storage
     *
     * @return if the content can be delivered
     */
    public boolean canDeliver(CmsStoredContentInfo info, I_CmsStorageDelivery storage) {

        return (info != null) && info.isExternallyStored() && (info.getLength() >= 0) && (storage != null);
    }

    /**
     * Delivers stored content to the servlet response.<p>
     *
     * @param info the stored content info
     * @param storage the delivery storage
     * @param req the servlet request
     * @param res the servlet response
     *
     * @return the delivery result
     *
     * @throws IOException in case writing to the response fails
     * @throws CmsException in case delivery fails
     */
    public DeliveryResult deliver(
        CmsStoredContentInfo info,
        I_CmsStorageDelivery storage,
        HttpServletRequest req,
        HttpServletResponse res)
    throws IOException, CmsException {

        if (!canDeliver(info, storage) || (req == null) || (res == null)) {
            return DeliveryResult.NOT_DELIVERABLE;
        }
        CmsResource resource = info.getResource();
        String rangeHeader = req.getHeader(CmsRequestUtil.HEADER_RANGE);
        boolean rangeRequest = (rangeHeader != null)
            && storage.supportsRangeDelivery()
            && matchesIfRange(req, resource, info);
        if (!rangeRequest && isNotModified(info, req, res)) {
            return DeliveryResult.NOT_MODIFIED;
        }
        if (storage.supportsRangeDelivery()) {
            res.setHeader(CmsRequestUtil.HEADER_ACCEPT_RANGES, CmsByteRange.RANGE_UNIT_BYTES);
        }
        if (rangeRequest) {
            CmsByteRange range = CmsByteRange.parse(rangeHeader, info.getLength());
            if (range.isInvalid()) {
                prepareRangeNotSatisfiableResponse(info, res);
                return DeliveryResult.RANGE_NOT_SATISFIABLE;
            } else if (!range.isIgnored()) {
                preparePartialResponse(info, range, req, res);
                if (!isHeadRequest(req)) {
                    streamRangeTo(storage, info, range, res);
                }
                return DeliveryResult.DELIVERED;
            }
        }
        prepareFullResponse(info, req, res);
        if (!isHeadRequest(req)) {
            streamTo(storage, info, res);
        }
        return DeliveryResult.DELIVERED;
    }

    /**
     * Returns the ETag for a resource.<p>
     *
     * @param resource the resource
     *
     * @return the ETag
     */
    private String getETag(CmsStoredContentInfo info) {

        return "\"" + info.getHash() + "\"";
    }

    /**
     * Returns if the given value is an entity tag.<p>
     *
     * @param value the value
     *
     * @return <code>true</code> if the value is an entity tag
     */
    private boolean isEntityTag(String value) {

        String tag = value.trim();
        return tag.startsWith("\"") || tag.startsWith(WEAK_ETAG_PREFIX + "\"");
    }

    /**
     * Returns if the request is a HEAD request.<p>
     *
     * @param req the request
     *
     * @return if the request is a HEAD request
     */
    private boolean isHeadRequest(HttpServletRequest req) {

        return "HEAD".equalsIgnoreCase(req.getMethod());
    }

    /**
     * Returns if the content was not modified since the client request.<p>
     *
     * @param resource the resource
     * @param req the request
     * @param res the response
     *
     * @return if the content was not modified
     */
    private boolean isNotModified(CmsStoredContentInfo info, HttpServletRequest req, HttpServletResponse res) {

        CmsResource resource = info.getResource();

        boolean hasIfNoneMatch = req.getHeader(CmsRequestUtil.HEADER_IF_NONE_MATCH) != null;
        boolean notModified = hasIfNoneMatch
        ? matchesIfNoneMatch(req, info)
        : CmsFlexController.isNotModifiedSince(req, resource.getDateLastModified());
        if (resource.getState().isUnchanged() && !CmsWorkplaceManager.isWorkplaceUser(req) && notModified) {
            long now = System.currentTimeMillis();
            if ((resource.getDateReleased() < now) && (resource.getDateExpired() > now)) {
                res.setHeader(CmsRequestUtil.HEADER_ETAG, getETag(info));
                CmsFlexController.setDateExpiresHeader(res, resource.getDateExpired(), m_clientCacheMaxAge);
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns if the If-None-Match header matches the resource ETag.<p>
     *
     * @param req the request
     * @param info the stored content info
     *
     * @return if the entity tag matches
     */
    private boolean matchesIfNoneMatch(HttpServletRequest req, CmsStoredContentInfo info) {

        String ifNoneMatch = req.getHeader(CmsRequestUtil.HEADER_IF_NONE_MATCH);
        if (ifNoneMatch == null) {
            return false;
        }
        String contentETag = stripWeakPrefix(getETag(info));
        String[] tags = ifNoneMatch.split(",");
        for (int i = 0; i < tags.length; i++) {
            String tag = stripWeakPrefix(tags[i].trim());
            if ("*".equals(tag) || contentETag.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the If-Range header.<p>
     *
     * @param req the request
     * @param resource the resource
     *
     * @return if a range request should be processed
     */
    private boolean matchesIfRange(HttpServletRequest req, CmsResource resource, CmsStoredContentInfo info) {

        String ifRange = req.getHeader(CmsRequestUtil.HEADER_IF_RANGE);
        if (ifRange == null) {
            return true;
        }
        if (isEntityTag(ifRange)) {
            return getETag(info).equals(ifRange.trim());
        }
        try {
            long ifRangeDate = req.getDateHeader(CmsRequestUtil.HEADER_IF_RANGE);
            return (ifRangeDate >= 0) && ((resource.getDateLastModified() / 1000) * 1000 <= ifRangeDate);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Prepares cache headers.<p>
     *
     * @param info the stored content info
     * @param req the request
     * @param res the response
     */
    private void prepareCacheHeaders(CmsStoredContentInfo info, HttpServletRequest req, HttpServletResponse res) {

        CmsResource resource = info.getResource();
        res.setHeader(CmsRequestUtil.HEADER_ETAG, getETag(info));
        if (CmsWorkplaceManager.isWorkplaceUser(req)) {
            res.setDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED, System.currentTimeMillis());
            CmsRequestUtil.setNoCacheHeaders(res);
        } else {
            res.setDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED, resource.getDateLastModified());
            if (!res.containsHeader(CmsRequestUtil.HEADER_CACHE_CONTROL)) {
                long expireTime = resource.getDateExpired();
                if (expireTime == CmsResource.DATE_EXPIRED_DEFAULT) {
                    expireTime--;
                }
                CmsFlexController.setDateExpiresHeader(res, expireTime, m_clientCacheMaxAge);
            }
        }
    }

    /**
     * Prepares the full response.<p>
     *
     * @param info the stored content info
     * @param req the request
     * @param res the response
     */
    private void prepareFullResponse(CmsStoredContentInfo info, HttpServletRequest req, HttpServletResponse res) {

        res.setStatus(HttpServletResponse.SC_OK);
        setContentLength(res, info.getLength());
        prepareCacheHeaders(info, req, res);
    }

    /**
     * Prepares the partial response.<p>
     *
     * @param info the stored content info
     * @param range the byte range
     * @param req the request
     * @param res the response
     */
    private void preparePartialResponse(
        CmsStoredContentInfo info,
        CmsByteRange range,
        HttpServletRequest req,
        HttpServletResponse res) {

        res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        setContentLength(res, range.m_length);
        res.setHeader(
            CmsRequestUtil.HEADER_CONTENT_RANGE,
            CmsByteRange.RANGE_UNIT_BYTES
                + " "
                + range.m_start
                + "-"
                + (range.m_start + range.m_length - 1)
                + "/"
                + info.getLength());
        prepareCacheHeaders(info, req, res);
    }

    /**
     * Prepares a 416 response.<p>
     *
     * @param info the stored content info
     * @param res the response
     */
    private void prepareRangeNotSatisfiableResponse(CmsStoredContentInfo info, HttpServletResponse res) {

        res.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
        res.setHeader(CmsRequestUtil.HEADER_CONTENT_RANGE, CmsByteRange.RANGE_UNIT_BYTES + " */" + info.getLength());
        res.setHeader(CmsRequestUtil.HEADER_ETAG, getETag(info));
    }

    /**
     * Sets the content length.<p>
     *
     * @param res the response
     * @param contentLength the content length
     */
    private void setContentLength(HttpServletResponse res, long contentLength) {

        if (contentLength <= Integer.MAX_VALUE) {
            res.setContentLength((int)contentLength);
        } else {
            res.setHeader(CmsRequestUtil.HEADER_CONTENT_LENGTH, String.valueOf(contentLength));
        }
    }

    /**
     * Streams a range to the response.<p>
     *
     * @param storage the delivery storage
     * @param info the stored content info
     * @param range the byte range
     * @param res the response
     *
     * @throws IOException in case writing to the response fails
     * @throws CmsException in case delivery fails
     */
    private void streamRangeTo(
        I_CmsStorageDelivery storage,
        CmsStoredContentInfo info,
        CmsByteRange range,
        HttpServletResponse res)
    throws IOException, CmsException {

        try {
            storage.streamRangeTo(null, info.getHash(), range.m_start, range.m_length, res.getOutputStream());
        } catch (IOException | CmsException e) {
            throw e;
        } catch (Exception e) {
            throw new CmsException(
                Messages.get().container(Messages.ERR_STORED_CONTENT_DELIVERY_2, info.getStorage(), info.getHash()),
                e);
        }
    }

    /**
     * Streams the full content to the response.<p>
     *
     * @param storage the delivery storage
     * @param info the stored content info
     * @param res the response
     *
     * @throws IOException in case writing to the response fails
     * @throws CmsException in case delivery fails
     */
    private void streamTo(I_CmsStorageDelivery storage, CmsStoredContentInfo info, HttpServletResponse res)
    throws IOException, CmsException {

        try {
            storage.streamTo(null, info.getHash(), res.getOutputStream());
        } catch (IOException | CmsException e) {
            throw e;
        } catch (Exception e) {
            throw new CmsException(
                Messages.get().container(Messages.ERR_STORED_CONTENT_DELIVERY_2, info.getStorage(), info.getHash()),
                e);
        }
    }

    /**
     * Removes a weak entity tag prefix from the given value.<p>
     *
     * @param value the value
     *
     * @return the value without weak prefix
     */
    private String stripWeakPrefix(String value) {

        return value.startsWith(WEAK_ETAG_PREFIX) ? value.substring(WEAK_ETAG_PREFIX.length()) : value;
    }
}
