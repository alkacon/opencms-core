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

package org.opencms.staticexport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Optional HTTP integration tests for stored content direct delivery.<p>
 *
 * These tests run against an externally provided OpenCms installation. They are skipped unless
 * the base URL is supplied as a system property or environment variable.<p>
 */
public class TestStoredContentHttpDeliveryIntegration {

    /**
     * HTTP response data.<p>
     */
    private static class Response {

        /** Response body. */
        private byte[] m_body;

        /** Response headers. */
        private Map<String, String> m_headers;

        /** HTTP status code. */
        private int m_status;

        /**
         * Creates a new response bean.<p>
         *
         * @param status the HTTP status code
         * @param headers the response headers
         * @param body the response body
         */
        Response(int status, Map<String, String> headers, byte[] body) {

            m_status = status;
            m_headers = headers;
            m_body = body;
        }

        /**
         * Returns the response body.<p>
         *
         * @return the response body
         */
        byte[] getBody() {

            return m_body;
        }

        /**
         * Returns a response header.<p>
         *
         * @param name the header name
         * @return the header value
         */
        String getHeader(String name) {

            return m_headers.get(name.toLowerCase());
        }

        /**
         * Returns the HTTP status code.<p>
         *
         * @return the HTTP status code
         */
        int getStatus() {

            return m_status;
        }
    }

    /**
     * Test configuration.<p>
     */
    private static class TestConfiguration {

        /** The base URL. */
        private String m_baseUrl;

        /** The public binary path. */
        private String m_publicBinaryPath;

        /** The scaled image path. */
        private String m_scaledImagePath;

        /**
         * Creates a new test configuration.<p>
         *
         * @param baseUrl the base URL
         * @param publicBinaryPath the public binary path
         * @param scaledImagePath the scaled image path
         */
        TestConfiguration(String baseUrl, String publicBinaryPath, String scaledImagePath) {

            m_baseUrl = baseUrl;
            m_publicBinaryPath = publicBinaryPath;
            m_scaledImagePath = scaledImagePath;
        }

        /**
         * Builds an absolute URL for a configured path.<p>
         *
         * @param path the configured path
         * @return the absolute URL
         */
        String getUrl(String path) {

            String normalizedPath = path.startsWith("/") ? path : "/" + path;
            return m_baseUrl + normalizedPath;
        }
    }

    /** Default public binary path. */
    private static final String DEFAULT_PUBLIC_BINARY_PATH = "/export/sites/default/mercury-demo/.galleries/Downloads/OpenCms-is-amazing.pdf";

    /** Default scaled image path. */
    private static final String DEFAULT_SCALED_IMAGE_PATH = "/export/sites/default/.galleries/people/15.jpg_1230966203.jpg";

    /** Environment variable for the OpenCms integration test base URL. */
    private static final String ENV_BASE_URL = "OPENCMS_INTEGRATION_BASE_URL";

    /** Environment variable for the public binary test path. */
    private static final String ENV_PUBLIC_BINARY_PATH = "OPENCMS_INTEGRATION_PUBLIC_BINARY_PATH";

    /** Environment variable for the scaled image test path. */
    private static final String ENV_SCALED_IMAGE_PATH = "OPENCMS_INTEGRATION_SCALED_IMAGE_PATH";

    /** System property for the OpenCms integration test base URL. */
    private static final String PROP_BASE_URL = "opencms.integration.baseUrl";

    /** System property for the public binary test path. */
    private static final String PROP_PUBLIC_BINARY_PATH = "opencms.integration.publicBinaryPath";

    /** System property for the scaled image test path. */
    private static final String PROP_SCALED_IMAGE_PATH = "opencms.integration.scaledImagePath";

    /** Range header used by the tests. */
    private static final String TEST_RANGE = "bytes=0-99";

    /** Expected range body length. */
    private static final int TEST_RANGE_LENGTH = 100;

    /**
     * Tests direct HTTP delivery headers and validators for a public binary resource.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testPublicBinaryDeliverySupportsRangesAndValidators() throws Exception {

        TestConfiguration configuration = getConfiguration();
        assertDirectDeliverySupportsRangesAndValidators(configuration.getUrl(configuration.m_publicBinaryPath));
    }

    /**
     * Tests direct HTTP delivery headers and validators for a scaled image resource.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testScaledImageDeliverySupportsRangesAndValidators() throws Exception {

        TestConfiguration configuration = getConfiguration();
        assertDirectDeliverySupportsRangesAndValidators(configuration.getUrl(configuration.m_scaledImagePath));
    }

    /**
     * Asserts that direct delivery supports range requests and HTTP validators.<p>
     *
     * @param url the URL to test
     * @throws Exception if the test fails
     */
    private void assertDirectDeliverySupportsRangesAndValidators(String url) throws Exception {

        Response full = request(url, null);
        assertEquals(HttpURLConnection.HTTP_OK, full.getStatus());
        assertFalse(full.getBody().length == 0, "The full response body must not be empty.");
        assertEquals("bytes", full.getHeader(CmsRequestUtil.HEADER_ACCEPT_RANGES));
        String etag = full.getHeader(CmsRequestUtil.HEADER_ETAG);
        assertNotNull(etag, "The direct delivery response must contain an ETag header.");

        Response head = request("HEAD", url, null);
        assertEquals(HttpURLConnection.HTTP_OK, head.getStatus());
        assertEquals("bytes", head.getHeader(CmsRequestUtil.HEADER_ACCEPT_RANGES));
        assertEquals(etag, head.getHeader(CmsRequestUtil.HEADER_ETAG));
        assertEquals(0, head.getBody().length);

        Response range = request(url, header(CmsRequestUtil.HEADER_RANGE, TEST_RANGE));
        assertPartialContent(range);

        Response ifRange = request(
            url,
            header(CmsRequestUtil.HEADER_RANGE, TEST_RANGE, CmsRequestUtil.HEADER_IF_RANGE, etag));
        assertPartialContent(ifRange);

        Response ifRangeMismatch = request(
            url,
            header(CmsRequestUtil.HEADER_RANGE, TEST_RANGE, CmsRequestUtil.HEADER_IF_RANGE, "\"opencms-mismatch\""));
        assertEquals(HttpURLConnection.HTTP_OK, ifRangeMismatch.getStatus());
        assertTrue(ifRangeMismatch.getBody().length >= TEST_RANGE_LENGTH);

        Response notModified = request(url, header(CmsRequestUtil.HEADER_IF_NONE_MATCH, etag));
        assertEquals(HttpURLConnection.HTTP_NOT_MODIFIED, notModified.getStatus());
        assertEquals(0, notModified.getBody().length);
    }

    /**
     * Asserts a partial content response for the configured test range.<p>
     *
     * @param response the response
     */
    private void assertPartialContent(Response response) {

        assertEquals(HttpURLConnection.HTTP_PARTIAL, response.getStatus());
        assertEquals(TEST_RANGE_LENGTH, response.getBody().length);
        String contentRange = response.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE);
        assertNotNull(contentRange, "The partial response must contain a Content-Range header.");
        assertTrue(contentRange.startsWith("bytes 0-99/"));
    }

    /**
     * Returns the test configuration, or skips the test if no base URL was configured.<p>
     *
     * @return the test configuration
     */
    private TestConfiguration getConfiguration() {

        String baseUrl = getConfiguredValue(PROP_BASE_URL, ENV_BASE_URL, null);
        Assumptions.assumeTrue(
            CmsStringUtil.isNotEmptyOrWhitespaceOnly(baseUrl),
            "Skipping optional OpenCms HTTP delivery integration test because no base URL was configured.");
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String publicBinaryPath = getConfiguredValue(
            PROP_PUBLIC_BINARY_PATH,
            ENV_PUBLIC_BINARY_PATH,
            DEFAULT_PUBLIC_BINARY_PATH);
        String scaledImagePath = getConfiguredValue(
            PROP_SCALED_IMAGE_PATH,
            ENV_SCALED_IMAGE_PATH,
            DEFAULT_SCALED_IMAGE_PATH);
        return new TestConfiguration(baseUrl, publicBinaryPath, scaledImagePath);
    }

    /**
     * Returns the configured value from system properties or environment variables.<p>
     *
     * @param propertyName the system property name
     * @param environmentName the environment variable name
     * @param defaultValue the default value
     * @return the configured value
     */
    private String getConfiguredValue(String propertyName, String environmentName, String defaultValue) {

        String result = System.getProperty(propertyName);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = System.getenv(environmentName);
        }
        return CmsStringUtil.isEmptyOrWhitespaceOnly(result) ? defaultValue : result.trim();
    }

    /**
     * Creates a header map.<p>
     *
     * @param values alternating header names and values
     * @return the header map
     */
    private Map<String, String> header(String... values) {

        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            result.put(values[i], values[i + 1]);
        }
        return result;
    }

    /**
     * Reads the response body.<p>
     *
     * @param connection the connection
     * @return the body
     * @throws IOException if reading the body fails
     */
    private byte[] readBody(HttpURLConnection connection) throws IOException {

        InputStream stream = null;
        try {
            stream = connection.getInputStream();
        } catch (IOException e) {
            stream = connection.getErrorStream();
        }
        if (stream == null) {
            return new byte[0];
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            stream.close();
        }
    }

    /**
     * Reads the response headers.<p>
     *
     * @param connection the connection
     * @return the response headers
     */
    private Map<String, String> readHeaders(HttpURLConnection connection) {

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, java.util.List<String>> entry : connection.getHeaderFields().entrySet()) {
            if ((entry.getKey() != null) && !entry.getValue().isEmpty()) {
                result.put(entry.getKey().toLowerCase(), entry.getValue().get(0));
            }
        }
        return result;
    }

    /**
     * Executes a HTTP GET request.<p>
     *
     * @param url the URL
     * @param headers the request headers
     * @return the response
     * @throws IOException if the request fails
     */
    private Response request(String url, Map<String, String> headers) throws IOException {

        return request("GET", url, headers);
    }

    /**
     * Executes a HTTP request.<p>
     *
     * @param method the request method
     * @param url the URL
     * @param headers the request headers
     * @return the response
     * @throws IOException if the request fails
     */
    private Response request(String method, String url, Map<String, String> headers) throws IOException {

        HttpURLConnection connection = (HttpURLConnection)(new URL(url)).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(30000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Accept-Encoding", "identity");
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        int status = connection.getResponseCode();
        try {
            return new Response(status, readHeaders(connection), readBody(connection));
        } finally {
            connection.disconnect();
        }
    }
}
