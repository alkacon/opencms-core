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

package org.opencms.main;

import org.opencms.util.CmsStringUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Handles the requests for static resources located in the classpath.<p>
 */
public class CmsStaticResourceHandler implements I_CmsRequestHandler {

    /** The handler name. */
    public static final String HANDLER_NAME = "Static";

    /** The static resource prefix '/handleStatic'. */
    public static final String STATIC_RESOURCE_PREFIX = OpenCmsServlet.HANDLE_PATH + HANDLER_NAME;

    /** The default output buffer size. */
    private static final int DEFAULT_BUFFER_SIZE = 32 * 1024;

    /** Default cache lifetime in seconds. */
    private static final int DEFAULT_CACHE_TIME = 3600;

    /** The handler names. */
    private static final String[] HANDLER_NAMES = new String[] {HANDLER_NAME};

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsStaticResourceHandler.class);

    /** The regular expression to remove the static resource path prefix. */
    private static String m_removePrefixRegex;

    /** The regular expression to identify static resource paths. */
    private static String m_staticResourceRegex;

    /** The opencms path prefix for static resources. */
    private static final String OPENCMS_PATH_PREFIX = "OPENCMS/";

    /**
     * Returns the context for static resources served from the class path, e.g. "/opencms/handleStatic/v5976v".<p>
     *
     * @param opencmsContext the OpenCms context
     * @param opencmsVersion the OpenCms version
     *
     * @return the static resource context
     */
    public static String getStaticResourceContext(String opencmsContext, String opencmsVersion) {

        return opencmsContext + STATIC_RESOURCE_PREFIX + "/v" + opencmsVersion.hashCode() + "v";
    }

    /**
     * Returns the URL to a static resource.<p>
     *
     * @param resourcePath the static resource path
     *
     * @return the resource URL
     */
    public static URL getStaticResourceURL(String resourcePath) {

        URL resourceURL = null;
        if (isStaticResourceUri(resourcePath)) {
            String path = removeStaticResourcePrefix(resourcePath);
            path = CmsStringUtil.joinPaths(OPENCMS_PATH_PREFIX, path);
            resourceURL = OpenCms.getSystemInfo().getClass().getClassLoader().getResource(path);
        }
        return resourceURL;
    }

    /**
     * Returns if the given URI points to a static resource.<p>
     *
     * @param path the path to test
     *
     * @return <code>true</code> in case the given URI points to a static resource
     */
    public static boolean isStaticResourceUri(String path) {

        return (path != null) && path.matches(getStaticResourceRegex());

    }

    /**
     * Returns if the given URI points to a static resource.<p>
     *
     * @param uri the URI to test
     *
     * @return <code>true</code> in case the given URI points to a static resource
     */
    public static boolean isStaticResourceUri(URI uri) {

        return (uri != null) && isStaticResourceUri(uri.getPath());

    }

    /**
     * Removes the static resource path prefix.<p>
     *
     * @param path the path
     *
     * @return the modified path
     */
    public static String removeStaticResourcePrefix(String path) {

        return path.replaceFirst(getRemovePrefixRegex(), "");
    }

    /**
     * Returns the regular expression to remove the static resource path prefix.<p>
     *
     * @return the regular expression to remove the static resource path prefix
     */
    private static String getRemovePrefixRegex() {

        if (m_removePrefixRegex == null) {
            m_removePrefixRegex = "^("
                + OpenCms.getStaticExportManager().getVfsPrefix()
                + ")?"
                + STATIC_RESOURCE_PREFIX
                + "(/v-?\\d+v/)?";
        }
        return m_removePrefixRegex;
    }

    /**
     * Returns the regular expression to identify static resource paths.<p>
     *
     * @return the regular expression to identify static resource paths
     */
    private static String getStaticResourceRegex() {

        if (m_staticResourceRegex == null) {
            m_staticResourceRegex = getRemovePrefixRegex() + ".*";
        }
        return m_staticResourceRegex;
    }

    /**
     * @see org.opencms.main.I_CmsRequestHandler#getHandlerNames()
     */
    public String[] getHandlerNames() {

        return HANDLER_NAMES;
    }

    /**
     * @see org.opencms.main.I_CmsRequestHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    public void handle(HttpServletRequest request, HttpServletResponse response, String name) throws IOException {

        String path = OpenCmsCore.getInstance().getPathInfo(request);
        URL resourceURL = getStaticResourceURL(path);
        if (resourceURL != null) {
            setResponseHeaders(request, response, path, resourceURL);
            writeStaticResourceResponse(request, response, resourceURL);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Returns whether this servlet should attempt to serve a precompressed
     * version of the given static resource. If this method returns true, the
     * suffix {@code .gz} is appended to the URL and the corresponding resource
     * is served if it exists. It is assumed that the compression method used is
     * gzip. If this method returns false or a compressed version is not found,
     * the original URL is used.<p>
     *
     * The base implementation of this method returns true if and only if the
     * request indicates that the client accepts gzip compressed responses and
     * the filename extension of the requested resource is .js, .css, or .html.<p>
     *
     * @param request the request for the resource
     * @param url the URL of the requested resource
     *
     * @return true if the servlet should attempt to serve a precompressed version of the resource, false otherwise
     */
    protected boolean allowServePrecompressedResource(HttpServletRequest request, String url) {

        String accept = request.getHeader("Accept-Encoding");
        return (accept != null)
            && accept.contains("gzip")
            && (url.endsWith(".js") || url.endsWith(".css") || url.endsWith(".html"));
    }

    /**
     * Calculates the cache lifetime for the given filename in seconds. By
     * default filenames containing ".nocache." return 0, filenames containing
     * ".cache." return one year, all other return the value defined in the
     * web.xml using resourceCacheTime (defaults to 1 hour).<p>
     *
     * @param filename the file name
     *
     * @return cache lifetime for the given filename in seconds
     */
    protected int getCacheTime(String filename) {

        /*
         * GWT conventions:
         *
         * - files containing .nocache. will not be cached.
         *
         * - files containing .cache. will be cached for one year.
         *
         * https://developers.google.com/web-toolkit/doc/latest/
         * DevGuideCompilingAndDebugging#perfect_caching
         */
        if (filename.contains(".nocache.")) {
            return 0;
        }
        if (filename.contains(".cache.")) {
            return 60 * 60 * 24 * 365;
        }
        /*
         * For all other files, the browser is allowed to cache for 1 hour
         * without checking if the file has changed. This forces browsers to
         * fetch a new version when the Vaadin version is updated. This will
         * cause more requests to the servlet than without this but for high
         * volume sites the static files should never be served through the
         * servlet.
         */
        return DEFAULT_CACHE_TIME;
    }

    /**
     * Sets the response headers.<p>
     *
     * @param request the request
     * @param response the response
     * @param filename the file name
     * @param resourceURL the resource URL
     */
    protected void setResponseHeaders(
        HttpServletRequest request,
        HttpServletResponse response,
        String filename,
        URL resourceURL) {

        String cacheControl = "public, max-age=0, must-revalidate";
        int resourceCacheTime = getCacheTime(filename);
        if (resourceCacheTime > 0) {
            cacheControl = "max-age=" + String.valueOf(resourceCacheTime);
        }
        response.setHeader("Cache-Control", cacheControl);
        response.setDateHeader("Expires", System.currentTimeMillis() + (resourceCacheTime * 1000));

        // Find the modification timestamp
        long lastModifiedTime = 0;
        URLConnection connection = null;
        try {
            connection = resourceURL.openConnection();
            lastModifiedTime = connection.getLastModified();
            // Remove milliseconds to avoid comparison problems (milliseconds
            // are not returned by the browser in the "If-Modified-Since"
            // header).
            lastModifiedTime = lastModifiedTime - (lastModifiedTime % 1000);
            response.setDateHeader("Last-Modified", lastModifiedTime);

            if (browserHasNewestVersion(request, lastModifiedTime)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        } catch (Exception e) {
            // Failed to find out last modified timestamp. Continue without it.
            LOG.debug("Failed to find out last modified timestamp. Continuing without it.", e);
        } finally {
            try {
                if (connection != null) {
                    // Explicitly close the input stream to prevent it
                    // from remaining hanging
                    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4257700
                    InputStream is = connection.getInputStream();
                    if (is != null) {
                        is.close();
                    }
                }
            } catch (Exception e) {
                LOG.info("Error closing URLConnection input stream", e);
            }
        }

        // Set type mime type if we can determine it based on the filename
        String mimetype = OpenCms.getResourceManager().getMimeType(filename, "UTF-8");
        if (mimetype != null) {
            response.setContentType(mimetype);
        }
    }

    /**
     * Writes the contents of the given resourceUrl in the response. Can be
     * overridden to add/modify response headers and similar.<p>
     *
     * @param request the request for the resource
     * @param response the response
     * @param resourceUrl the url to send
     *
     * @throws IOException in case writing the response fails
     */
    protected void writeStaticResourceResponse(
        HttpServletRequest request,
        HttpServletResponse response,
        URL resourceUrl)
    throws IOException {

        URLConnection connection = null;
        InputStream is = null;
        String urlStr = resourceUrl.toExternalForm();
        try {
            if (allowServePrecompressedResource(request, urlStr)) {
                // try to serve a precompressed version if available
                try {
                    connection = new URL(urlStr + ".gz").openConnection();
                    is = connection.getInputStream();
                    // set gzip headers
                    response.setHeader("Content-Encoding", "gzip");
                } catch (Exception e) {
                    LOG.debug("Unexpected exception looking for gzipped version of resource " + urlStr, e);
                }
            }
            if (is == null) {
                // precompressed resource not available, get non compressed
                connection = resourceUrl.openConnection();
                try {
                    is = connection.getInputStream();
                } catch (FileNotFoundException e) {
                    LOG.debug(e.getMessage(), e);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }

            try {
                @SuppressWarnings("null")
                int length = connection.getContentLength();
                if (length >= 0) {
                    response.setContentLength(length);
                }
            } catch (Throwable e) {
                LOG.debug(e.getMessage(), e);
                // This can be ignored, content length header is not required.
                // Need to close the input stream because of
                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4257700 to
                // prevent it from hanging, but that is done below.
            }

            streamContent(response, is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Checks if the browser has an up to date cached version of requested
     * resource. Currently the check is performed using the "If-Modified-Since"
     * header. Could be expanded if needed.<p>
     *
     * @param request the HttpServletRequest from the browser
     * @param resourceLastModifiedTimestamp the timestamp when the resource was last modified. 0 if the last modification time is unknown
     *
     * @return true if the If-Modified-Since header tells the cached version in the browser is up to date, false otherwise
     */
    private boolean browserHasNewestVersion(HttpServletRequest request, long resourceLastModifiedTimestamp) {

        if (resourceLastModifiedTimestamp < 1) {
            // We do not know when it was modified so the browser cannot have an
            // up-to-date version
            return false;
        }
        /*
         * The browser can request the resource conditionally using an
         * If-Modified-Since header. Check this against the last modification
         * time.
         */
        try {
            // If-Modified-Since represents the timestamp of the version cached
            // in the browser
            long headerIfModifiedSince = request.getDateHeader("If-Modified-Since");

            if (headerIfModifiedSince >= resourceLastModifiedTimestamp) {
                // Browser has this an up-to-date version of the resource
                return true;
            }
        } catch (Exception e) {
            // Failed to parse header. Fail silently - the browser does not have
            // an up-to-date version in its cache.
        }
        return false;
    }

    /**
     * Streams the input stream to the response.<p>
     *
     * @param response the response
     * @param is the input stream
     *
     * @throws IOException in case writing to the response fails
     */
    private void streamContent(HttpServletResponse response, InputStream is) throws IOException {

        OutputStream os = response.getOutputStream();
        try {
            byte buffer[] = new byte[DEFAULT_BUFFER_SIZE];
            int bytes;
            while ((bytes = is.read(buffer)) >= 0) {
                os.write(buffer, 0, bytes);
            }
        } finally {
            os.close();
        }
    }
}
