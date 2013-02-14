/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.flex;

import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsRequestUtil;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;

/**
 * Wrapper class for a HttpServletResponse, required in order to process JSPs from the OpenCms VFS.<p>
 *
 * This class wraps the standard HttpServletResponse so that it's output can be delivered to
 * the CmsFlexCache.<p>
 * 
 * @since 6.0.0 
 */
public class CmsFlexResponse extends HttpServletResponseWrapper {

    /**
     * Wrapped implementation of the ServletOutputStream.<p>
     * 
     * This implementation writes to an internal buffer and optionally to another 
     * output stream at the same time.<p>
     * 
     * It should be fully transparent to the standard ServletOutputStream.<p>
     */
    private static class CmsServletOutputStream extends ServletOutputStream {

        /** The optional output stream to write to. */
        private ServletOutputStream m_servletStream;

        /** The internal stream buffer. */
        private ByteArrayOutputStream m_stream;

        /**
         * Constructor that must be used if the stream should write 
         * only to a buffer.<p>
         */
        public CmsServletOutputStream() {

            m_servletStream = null;
            clear();
        }

        /**
         * Constructor that must be used if the stream should write 
         * to a buffer and to another stream at the same time.<p>
         *
         * @param servletStream The stream to write to
         */
        public CmsServletOutputStream(ServletOutputStream servletStream) {

            m_servletStream = servletStream;
            clear();
        }

        /**
         * Clears the buffer by initializing the buffer with a new stream.<p>
         */
        public void clear() {

            m_stream = new java.io.ByteArrayOutputStream(1024);
        }

        /**
         * @see java.io.OutputStream#close()
         */
        @Override
        public void close() throws IOException {

            if (m_stream != null) {
                m_stream.close();
            }
            if (m_servletStream != null) {
                m_servletStream.close();
            }
            super.close();
        }

        /**
         * @see java.io.OutputStream#flush()
         */
        @Override
        public void flush() throws IOException {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXRESPONSE_FLUSHED_1, m_servletStream));
            }
            if (m_servletStream != null) {
                m_servletStream.flush();
            }
        }

        /**
         * Provides access to the bytes cached in the buffer.<p>
         *
         * @return the cached bytes from the buffer
         */
        public byte[] getBytes() {

            return m_stream.toByteArray();
        }

        /**
         * @see java.io.OutputStream#write(byte[], int, int)
         */
        @Override
        public void write(byte[] b, int off, int len) throws IOException {

            m_stream.write(b, off, len);
            if (m_servletStream != null) {
                m_servletStream.write(b, off, len);
            }
        }

        /**
         * @see java.io.OutputStream#write(int)
         */
        @Override
        public void write(int b) throws IOException {

            m_stream.write(b);
            if (m_servletStream != null) {
                m_servletStream.write(b);
            }
        }
    }

    /** The cache delimiter char. */
    public static final char FLEX_CACHE_DELIMITER = (char)0;

    /** Static string to indicate a header is "set" in the header maps. */
    public static final String SET_HEADER = "[setHeader]";

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsFlexResponse.class);

    /** Map to save response headers belonging to a single include call in .*/
    private Map<String, List<String>> m_bufferHeaders;

    /** String to hold a buffered redirect target. */
    private String m_bufferRedirect;

    /** Byte array used for "cached leafs" optimization. */
    private byte[] m_cacheBytes;

    /** The cached entry that is constructed from this response. */
    private CmsFlexCacheEntry m_cachedEntry;

    /** Indicates if caching is required, will always be true if m_writeOnlyToBuffer is true. */
    private boolean m_cachingRequired;

    /** The CmsFlexController for this response. */
    private CmsFlexController m_controller;

    /** The encoding to use for the response. */
    private String m_encoding;

    /** Map to save all response headers (including sub-elements) in. */
    private Map<String, List<String>> m_headers;

    /** A list of include calls that origin from this page, i.e. these are sub elements of this element. */
    private List<String> m_includeList;

    /** A list of attributes that belong to the include calls. */
    private List<Map<String, Object>> m_includeListAttributes;

    /** A list of parameters that belong to the include calls. */
    private List<Map<String, String[]>> m_includeListParameters;

    /** Indicates if this element is currently in include mode, i.e. processing a sub-element. */
    private boolean m_includeMode;

    /** A list of results from the inclusions, needed because of JSP buffering. */
    private List<byte[]> m_includeResults;

    /** Flag to indicate if this is the top level element or an included sub - element. */
    private boolean m_isTopElement;

    /** The CmsFlexCacheKey for this response. */
    private CmsFlexCacheKey m_key;

    /** A special wrapper class for a ServletOutputStream. */
    private CmsFlexResponse.CmsServletOutputStream m_out;

    /** Indicates that parent stream is writing only in the buffer. */
    private boolean m_parentWritesOnlyToBuffer;

    /** The wrapped ServletResponse. */
    private HttpServletResponse m_res;

    /** Indicates if this response is suspended (probably because of a redirect). */
    private boolean m_suspended;

    /** State bit indicating whether content type has been set, type may only be set once according to spec. */
    private boolean m_typeSet;

    /** Indicates that the OutputStream m_out should write ONLY in the buffer. */
    private boolean m_writeOnlyToBuffer;

    /** A print writer that writes in the m_out stream. */
    private java.io.PrintWriter m_writer;

    /**
     * Constructor for the CmsFlexResponse,
     * this variation one is usually used to wrap responses for further include calls in OpenCms.<p>
     *
     * @param res the CmsFlexResponse to wrap     
     * @param controller the controller to use
     */
    public CmsFlexResponse(HttpServletResponse res, CmsFlexController controller) {

        super(res);
        m_res = res;
        m_controller = controller;
        m_encoding = controller.getCurrentResponse().getEncoding();
        m_isTopElement = controller.getCurrentResponse().isTopElement();
        m_parentWritesOnlyToBuffer = controller.getCurrentResponse().hasIncludeList() && !controller.isForwardMode();
        setOnlyBuffering(m_parentWritesOnlyToBuffer);
        m_headers = new HashMap<String, List<String>>(16);
        m_bufferHeaders = new HashMap<String, List<String>>(8);
    }

    /** 
     * Constructor for the CmsFlexResponse,
     * this variation is usually used for the "top" response.<p>
     *
     * @param res the HttpServletResponse to wrap
     * @param controller the controller to use
     * @param streaming indicates if streaming should be enabled or not
     * @param isTopElement indicates if this is the top element of an include cascade
     */
    public CmsFlexResponse(
        HttpServletResponse res,
        CmsFlexController controller,
        boolean streaming,
        boolean isTopElement) {

        super(res);
        m_res = res;
        m_controller = controller;
        m_encoding = controller.getCmsObject().getRequestContext().getEncoding();
        m_isTopElement = isTopElement;
        m_parentWritesOnlyToBuffer = !streaming && !controller.isForwardMode();
        setOnlyBuffering(m_parentWritesOnlyToBuffer);
        m_headers = new HashMap<String, List<String>>(16);
        m_bufferHeaders = new HashMap<String, List<String>>(8);
    }

    /**
     * Process the headers stored in the provided map and add them to the response.<p>
     * 
     * @param headers the headers to add
     * @param res the response to add the headers to
     */
    public static void processHeaders(Map<String, List<String>> headers, HttpServletResponse res) {

        if (headers != null) {
            Iterator<Map.Entry<String, List<String>>> i = headers.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, List<String>> entry = i.next();
                String key = entry.getKey();
                List<String> l = entry.getValue();
                for (int j = 0; j < l.size(); j++) {
                    if ((j == 0) && ((l.get(0)).startsWith(SET_HEADER))) {
                        String s = l.get(0);
                        res.setHeader(key, s.substring(SET_HEADER.length()));
                    } else {
                        res.addHeader(key, l.get(j));
                    }
                }
            }
        }
    }

    /**
     * Method overloaded from the standard HttpServletRequest API.<p>
     *
     * Cookies must be set directly as a header, otherwise they might not be set
     * in the super class.<p>
     *
     * @see javax.servlet.http.HttpServletResponseWrapper#addCookie(javax.servlet.http.Cookie)
     */
    @Override
    public void addCookie(Cookie cookie) {

        if (cookie == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_ADD_COOKIE_0));
        }

        StringBuffer header = new StringBuffer(128);

        // name and value
        header.append(cookie.getName());
        header.append('=');
        header.append(cookie.getValue());

        // add version 1 / RFC 2109 specific information
        if (cookie.getVersion() == 1) {
            header.append("; Version=1");

            // comment
            if (cookie.getComment() != null) {
                header.append("; Comment=");
                header.append(cookie.getComment());
            }
        }

        // domain
        if (cookie.getDomain() != null) {
            header.append("; Domain=");
            header.append(cookie.getDomain());
        }

        // max-age / expires
        if (cookie.getMaxAge() >= 0) {
            if (cookie.getVersion() == 0) {
                // old Netscape format
                header.append("; Expires=");
                long time;
                if (cookie.getMaxAge() == 0) {
                    time = 10000L;
                } else {
                    time = System.currentTimeMillis() + (cookie.getMaxAge() * 1000L);
                }
                header.append(CmsDateUtil.getOldCookieDate(time));
            } else {
                // new RFC 2109 format 
                header.append("; Max-Age=");
                header.append(cookie.getMaxAge());
            }
        }

        // path
        if (cookie.getPath() != null) {
            header.append("; Path=");
            header.append(cookie.getPath());
        }

        // secure
        if (cookie.getSecure()) {
            header.append("; Secure");
        }

        addHeader("Set-Cookie", header.toString());
    }

    /**
     * Method overload from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
     */
    @Override
    public void addDateHeader(String name, long date) {

        addHeader(name, CmsDateUtil.getHeaderDate(date));
    }

    /**
     * Method overload from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void addHeader(String name, String value) {

        if (isSuspended()) {
            return;
        }

        if (CmsRequestUtil.HEADER_CONTENT_TYPE.equalsIgnoreCase(name)) {
            setContentType(value);
            return;
        }

        if (m_cachingRequired && !m_includeMode) {
            addHeaderList(m_bufferHeaders, name, value);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_FLEXRESPONSE_ADDING_HEADER_TO_ELEMENT_BUFFER_2,
                    name,
                    value));
            }
        }

        if (m_writeOnlyToBuffer) {
            addHeaderList(m_headers, name, value);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_FLEXRESPONSE_ADDING_HEADER_TO_HEADERS_2,
                    name,
                    value));
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_FLEXRESPONSE_ADDING_HEADER_TO_PARENT_RESPONSE_2,
                    name,
                    value));
            }
            m_res.addHeader(name, value);
        }
    }

    /**
     * Method overload from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
     */
    @Override
    public void addIntHeader(String name, int value) {

        addHeader(name, String.valueOf(value));
    }

    /** 
     * Adds an inclusion target to the list of include results.<p>
     * 
     * Should be used only in inclusion-scenarios
     * like the JSP cms:include tag processing.<p>
     *
     * @param target the include target name to add
     * @param parameterMap the map of parameters given with the include command
     * @param attributeMap the map of attributes given with the include command
     */
    public void addToIncludeList(String target, Map<String, String[]> parameterMap, Map<String, Object> attributeMap) {

        if (m_includeList == null) {
            m_includeList = new ArrayList<String>(10);
            m_includeListParameters = new ArrayList<Map<String, String[]>>(10);
            m_includeListAttributes = new ArrayList<Map<String, Object>>(10);
        }
        // never cache the Flex controller
        attributeMap.remove(CmsFlexController.ATTRIBUTE_NAME);
        // only cache a copy of the JSP standard context bean
        CmsJspStandardContextBean bean = (CmsJspStandardContextBean)attributeMap.get(CmsJspStandardContextBean.ATTRIBUTE_NAME);
        if (bean != null) {
            attributeMap.put(CmsJspStandardContextBean.ATTRIBUTE_NAME, bean.createCopy());
        }
        m_includeListAttributes.add(attributeMap);
        m_includeListParameters.add(parameterMap);
        m_includeList.add(target);
    }

    /**
     * @see javax.servlet.ServletResponseWrapper#flushBuffer()
     */
    @Override
    public void flushBuffer() throws IOException {

        if (OpenCms.getSystemInfo().getServletContainerSettings().isPreventResponseFlush()) {
            // Websphere does not allow to set headers afterwards, so we have to prevent this call
            return;
        }
        super.flushBuffer();
    }

    /**
     * Returns the value of the encoding used for this response.<p>
     * 
     * @return the value of the encoding used for this response
     */
    public String getEncoding() {

        return m_encoding;
    }

    /**
     * Provides access to the header cache of the top wrapper.<p>
     *
     * @return the Map of cached headers
     */
    public Map<String, List<String>> getHeaders() {

        return m_headers;
    }

    /**
     * Method overload from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {

        if (m_out == null) {
            initStream();
        }
        return m_out;
    }

    /**
     * Method overload from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.ServletResponse#getWriter()
     */
    @Override
    public PrintWriter getWriter() throws IOException {

        if (m_writer == null) {
            initStream();
        }
        return m_writer;
    }

    /**
     * Returns the bytes that have been written on the current writers output stream.<p>
     *
     * @return the bytes that have been written on the current writers output stream
     */
    public byte[] getWriterBytes() {

        if (isSuspended()) {
            // No output whatsoever if the response is suspended
            return new byte[0];
        }
        if (m_cacheBytes != null) {
            // Optimization for cached "leaf" nodes, here I re-use the array from the cache
            return m_cacheBytes;
        }
        if (m_out == null) {
            // No output was written so far, just return an empty array
            return new byte[0];
        }
        if (m_writer != null) {
            // Flush the writer in case something was written on it
            m_writer.flush();
        }
        return m_out.getBytes();
    }

    /** 
     * This flag indicates if the response is suspended or not.<p>
     * 
     * A suspended response must not write further output to any stream or
     * process a cache entry for itself.<p>
     *
     * Currently, a response is only suspended if it is redirected.<p>
     *
     * @return true if the response is suspended, false otherwise
     */
    public boolean isSuspended() {

        return m_suspended;
    }

    /**
     * Returns <code>true</code> if this response has been constructed for the 
     * top level element of this request, <code>false</code> if it was 
     * constructed for an included sub-element.<p>
     * 
     * @return <code>true</code> if this response has been constructed for the 
     * top level element of this request, <code>false</code> if it was 
     * constructed for an included sub-element.
     */
    public boolean isTopElement() {

        return m_isTopElement;
    }

    /**
     * Method overload from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
     * 
     * @throws IllegalArgumentException In case of a malformed location string
     */
    @Override
    public void sendRedirect(String location) throws IOException {

        // Ignore any redirects after the first one
        if (isSuspended() && (!location.equals(m_bufferRedirect))) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXRESPONSE_SENDREDIRECT_1, location));
        }
        if (m_cachingRequired && !m_includeMode) {
            m_bufferRedirect = location;
        }

        if (!m_cachingRequired) {
            // If caching is required a cached entry will be constructed first and redirect will
            // be called after this is completed and stored in the cache
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXRESPONSE_TOPRESPONSE_SENDREDIRECT_1, location));
            }
            if (LOG.isWarnEnabled()) {
                if (m_controller.getResponseStackSize() > 2) {
                    // sendRedirect in a stacked response scenario, this may cause issues in some app servers
                    LOG.warn(Messages.get().getBundle().key(
                        Messages.LOG_FLEXRESPONSE_REDIRECTWARNING_3,
                        m_controller.getCmsResource().getRootPath(),
                        m_controller.getCurrentRequest().getElementUri(),
                        location));
                }
            }
            
            try {
                // Checking for possible illegal characters (for example, XSS exploits) before sending the redirect
                // The constructor is key here. That method will throw an URISyntaxException if the URL
                // format is not according to standards (e.g. contains illegal characters, like spaces, < or >, etc).
                new URI(location);
            } catch (URISyntaxException e) {
                // Deliberately NOT passing the original exception, since the URISyntaxException contains the full path,
                // which may include the XSS attempt
                LOG.error(Messages.get().getBundle().key(
                    Messages.ERR_FLEXRESPONSE_URI_SYNTAX_EXCEPTION_0), e);
                throw new IllegalArgumentException("Illegal or malformed characters found in path");
            }
            
            // use top response for redirect
            HttpServletResponse topRes = m_controller.getTopResponse();
            // add all headers found to make sure cookies can be set before redirect
            processHeaders(getHeaders(), topRes);
            topRes.sendRedirect(location);
        }

        m_controller.suspendFlexResponse();
    }

    /**
     * Method overload from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
     */
    @Override
    public void setContentType(String type) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXRESPONSE_SETTING_CONTENTTYPE_1, type));
        }
        // only if this is the "Top-Level" element, do set the content type    
        // otherwise an included JSP could reset the type with some unwanted defaults  
        if (!m_typeSet && m_isTopElement) {
            // type must be set only once, otherwise some Servlet containers (not Tomcat) generate errors
            m_typeSet = true;
            super.setContentType(type);
            return;
        }
    }

    /**
     * Method overload from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
     */
    @Override
    public void setDateHeader(String name, long date) {

        setHeader(name, CmsDateUtil.getHeaderDate(date));
    }

    /**
     * Method overload from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void setHeader(String name, String value) {

        if (isSuspended()) {
            return;
        }

        if (CmsRequestUtil.HEADER_CONTENT_TYPE.equalsIgnoreCase(name)) {
            setContentType(value);
            return;
        }

        if (m_cachingRequired && !m_includeMode) {
            setHeaderList(m_bufferHeaders, name, value);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_FLEXRESPONSE_SETTING_HEADER_IN_ELEMENT_BUFFER_2,
                    name,
                    value));
            }
        }

        if (m_writeOnlyToBuffer) {
            setHeaderList(m_headers, name, value);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_FLEXRESPONSE_SETTING_HEADER_IN_HEADERS_2,
                    name,
                    value));
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_FLEXRESPONSE_SETTING_HEADER_IN_PARENT_RESPONSE_2,
                    name,
                    value));
            }
            m_res.setHeader(name, value);
        }
    }

    /**
     * Method overload from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
     */
    @Override
    public void setIntHeader(String name, int value) {

        setHeader(name, "" + value);
    }

    /** 
     * Sets buffering status of the response.<p>
     * 
     * This must be done before the first output is written.
     * Buffering is needed to process elements that can not be written
     * directly to the output stream because their sub - elements have to
     * be processed separately. Which is so far true only for JSP pages.<p>
     *
     * If buffering is on, nothing is written to the output stream
     * even if streaming for this response is enabled.<p>
     *
     * @param value the value to set
     */
    public void setOnlyBuffering(boolean value) {

        m_writeOnlyToBuffer = value && !m_controller.isForwardMode();

        if (m_writeOnlyToBuffer) {
            setCmsCachingRequired(true);
        }
    }

    /**
     * Adds some bytes to the list of include results.<p>
     * 
     * Should be used only in inclusion-scenarios 
     * like the JSP cms:include tag processing.<p>
     * 
     * @param result the byte array to add
     */
    void addToIncludeResults(byte[] result) {

        if (m_includeResults == null) {
            m_includeResults = new ArrayList<byte[]>(10);
        }
        m_includeResults.add(result);
    }

    /**
     * Returns the cache key for to this response.<p>
     *
     * @return the cache key for to this response
     */
    CmsFlexCacheKey getCmsCacheKey() {

        return m_key;
    }

    /**
     * Is used to check if the response has an include list, 
     * which indicates a) it is probably processing a JSP element 
     * and b) it can never be streamed and always must be buffered.<p>
     *
     * @return true if this response has an include list, false otherwise
     */
    boolean hasIncludeList() {

        return m_includeList != null;
    }

    /**
     * Generates a CmsFlexCacheEntry from the current response using the 
     * stored include results.<p>
     * 
     * In case the results were written only to the buffer until now, 
     * they are now re-written on the output stream, with all included 
     * elements.<p>
     *
     * @throws IOException in case something goes wrong while writing to the output stream
     * 
     * @return  the generated cache entry
     */
    CmsFlexCacheEntry processCacheEntry() throws IOException {

        if (isSuspended() && (m_bufferRedirect == null)) {
            // an included element redirected this response, no cache entry must be produced
            return null;
        }
        if (m_cachingRequired) {
            // cache entry must only be calculated if it's actually needed (always true if we write only to buffer)
            m_cachedEntry = new CmsFlexCacheEntry();
            if (m_bufferRedirect != null) {
                // only set et cached redirect target
                m_cachedEntry.setRedirect(m_bufferRedirect);
            } else {
                // add cached headers
                m_cachedEntry.addHeaders(m_bufferHeaders);
                // add cached output 
                if (m_includeList != null) {
                    // probably JSP: we must analyze out stream for includes calls
                    // also, m_writeOnlyToBuffer must be "true" or m_includeList can not be != null
                    processIncludeList();
                } else {
                    // output is delivered directly, no include call parsing required
                    m_cachedEntry.add(getWriterBytes());
                }
            }
            // update the "last modified" date for the cache entry
            m_cachedEntry.complete();
        }
        // in case the output was only buffered we have to re-write it to the "right" stream       
        if (m_writeOnlyToBuffer) {

            // since we are processing a cache entry caching is not required
            m_cachingRequired = false;

            if (m_bufferRedirect != null) {
                // send buffered redirect, will trigger redirect of top response
                sendRedirect(m_bufferRedirect);
            } else {
                // process the output               
                if (m_parentWritesOnlyToBuffer) {
                    // write results back to own stream, headers are already in buffer
                    if (m_out != null) {
                        try {
                            m_out.clear();
                        } catch (Exception e) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(Messages.get().getBundle().key(
                                    Messages.LOG_FLEXRESPONSE_ERROR_FLUSHING_OUTPUT_STREAM_1,
                                    e));
                            }
                        }
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(Messages.get().getBundle().key(
                                Messages.LOG_FLEXRESPONSE_ERROR_OUTPUT_STREAM_NULL_0));
                        }
                    }
                    writeCachedResultToStream(this);
                } else {
                    // we can use the parent stream
                    processHeaders(m_headers, m_res);
                    writeCachedResultToStream(m_res);
                }
            }
        }
        return m_cachedEntry;
    }

    /**
     * Sets the cache key for this response from 
     * a pre-calculated cache key.<p>
     *
     * @param value the cache key to set
     */
    void setCmsCacheKey(CmsFlexCacheKey value) {

        m_key = value;
    }

    /** 
     * Sets the cache key for this response, which is calculated
     * from the provided parameters.<p>
     * 
     * @param resourcename the target resource for which to create the cache key
     * @param cacheDirectives the cache directives of the resource (value of the property "cache")
     * @param online indicates if this resource is online or offline
     *
     * @return the generated cache key
     * 
     * @throws CmsFlexCacheException in case the value String had a parse error
     */
    CmsFlexCacheKey setCmsCacheKey(String resourcename, String cacheDirectives, boolean online)
    throws CmsFlexCacheException {

        m_key = new CmsFlexCacheKey(resourcename, cacheDirectives, online);
        if (m_key.hadParseError()) {
            // We throw the exception here to make sure this response has a valid key (cache=never)
            throw new CmsFlexCacheException(Messages.get().container(
                Messages.LOG_FLEXRESPONSE_PARSE_ERROR_IN_CACHE_KEY_2,
                cacheDirectives,
                resourcename));
        }
        return m_key;
    }

    /**
     * Set caching status for this response.<p>
     * 
     * Will always be set to <code>"true"</code> if setOnlyBuffering() is set to <code>"true"</code>.
     * Currently this is an optimization for non - JSP elements that 
     * are known not to be cachable.<p>
     *
     * @param value the value to set     
     */
    void setCmsCachingRequired(boolean value) {

        m_cachingRequired = (value || m_writeOnlyToBuffer) && !m_controller.isForwardMode();
    }

    /**
     * This flag indicates to the response if it is in "include mode" or not.<p>
     * 
     * This is important in case a cache entry is constructed, 
     * since the cache entry must not consist of output or headers of the
     * included elements.<p>
     *
     * @param value the value to set     
     */
    void setCmsIncludeMode(boolean value) {

        m_includeMode = value;
    }

    /**
     * Sets the suspended status of the response, and also sets
     * the suspend status of all responses wrapping this response.<p>
     * 
     * A suspended response must not write further output to any stream or
     * process a cache entry for itself.<p>
     *
     * @param value the value to set     
     */
    void setSuspended(boolean value) {

        m_suspended = value;
    }

    /** 
     * Writes some bytes to the current output stream,
     * this method should be called from CmsFlexCacheEntry.service() only.<p>
     *
     * @param bytes an array of bytes
     * @param useArray indicates that the byte array should be used directly
     * 
     * @throws IOException in case something goes wrong while writing to the stream
     */
    void writeToOutputStream(byte[] bytes, boolean useArray) throws IOException {

        if (isSuspended()) {
            return;
        }
        if (m_writeOnlyToBuffer) {
            if (useArray) {
                // This cached entry has no sub-elements (it a "leaf") and so we can just use it's bytes
                m_cacheBytes = bytes;
            } else {
                if (m_out == null) {
                    initStream();
                }
                // In this case the buffer will not write to the servlet stream, but to it's internal buffer only
                m_out.write(bytes);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXRESPONSE_ERROR_WRITING_TO_OUTPUT_STREAM_0));
            }
            // The request is not buffered, so we can write directly to it's parents output stream 
            m_res.getOutputStream().write(bytes);
            m_res.getOutputStream().flush();
        }
    }

    /**
     * Helper method to add a value in the internal header list.<p>
     *
     * @param headers the headers to look up the value in
     * @param name the name to look up
     * @param value the value to set
     */
    private void addHeaderList(Map<String, List<String>> headers, String name, String value) {

        List<String> values = headers.get(name);
        if (values == null) {
            values = new ArrayList<String>();
            headers.put(name, values);
        }
        values.add(value);
    }

    /**
     * Initializes the current responses output stream 
     * and the corresponding print writer.<p>
     *
     * @throws IOException in case something goes wrong while initializing
     */
    private void initStream() throws IOException {

        if (m_out == null) {
            if (!m_writeOnlyToBuffer) {
                // we can use the parents output stream
                if (m_cachingRequired || (m_controller.getResponseStackSize() > 1)) {
                    // we are allowed to cache our results (probably to construct a new cache entry)
                    m_out = new CmsFlexResponse.CmsServletOutputStream(m_res.getOutputStream());
                } else {
                    // we are not allowed to cache so we just use the parents output stream
                    m_out = (CmsFlexResponse.CmsServletOutputStream)m_res.getOutputStream();
                }
            } else {
                // construct a "buffer only" output stream
                m_out = new CmsFlexResponse.CmsServletOutputStream();
            }
        }
        if (m_writer == null) {
            // create a PrintWriter that uses the encoding required for the request context
            m_writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(m_out, m_encoding)), false);
        }
    }

    /**
     * This method is needed to process pages that can NOT be analyzed
     * directly during delivering (like JSP) because they write to 
     * their own buffer.<p>
     *
     * In this case, we don't actually write output of include calls to the stream.
     * Where there are include calls we write a <code>{@link #FLEX_CACHE_DELIMITER}</code> char on the stream 
     * to indicate that at this point the output of the include must be placed later.
     * The include targets (resource names) are then saved in the m_includeList.<p>
     *
     * This method must be called after the complete page has been processed.
     * It will contain the output of the page only (no includes), 
     * with <code>{@link #FLEX_CACHE_DELIMITER}</code> chars were the include calls should be placed. 
     * What we do here is analyze the output and cut it in parts 
     * of <code>byte[]</code> arrays which then are saved in the resulting cache entry.
     * For the includes, we just save the name of the resource in
     * the cache entry.<p>
     *  
     * If caching is disabled this method is just not called.<p>
     */
    private void processIncludeList() {

        byte[] result = getWriterBytes();
        if (!hasIncludeList()) {
            // no include list, so no includes and we just use the bytes as they are in one block
            m_cachedEntry.add(result);
        } else {
            // process the include list
            int max = result.length;
            int pos = 0;
            int last = 0;
            int size = 0;
            int count = 0;

            // work through result and split this with include list calls            
            int i = 0;
            while ((i < m_includeList.size()) && (pos < max)) {
                // look for the first FLEX_CACHE_DELIMITER char
                while ((pos < max) && (result[pos] != FLEX_CACHE_DELIMITER)) {
                    pos++;
                }
                if ((pos < max) && (result[pos] == FLEX_CACHE_DELIMITER)) {
                    count++;
                    // a byte value of C_FLEX_CACHE_DELIMITER in our (String) output list indicates 
                    // that the next include call must be placed here
                    size = pos - last;
                    if (size > 0) {
                        // if not (it might be 0) there would be 2 include calls back 2 back
                        byte[] piece = new byte[size];
                        System.arraycopy(result, last, piece, 0, size);
                        // add the byte array to the cache entry
                        m_cachedEntry.add(piece);
                        piece = null;
                    }
                    last = ++pos;
                    // add an include call to the cache entry
                    m_cachedEntry.add(
                        m_includeList.get(i),
                        m_includeListParameters.get(i),
                        m_includeListAttributes.get(i));
                    i++;
                }
            }
            if (pos < max) {
                // there is content behind the last include call
                size = max - pos;
                byte[] piece = new byte[size];
                System.arraycopy(result, pos, piece, 0, size);
                m_cachedEntry.add(piece);
                piece = null;
            }
            if (i >= m_includeList.size()) {
                // clear the include list if all include calls are handled
                m_includeList = null;
                m_includeListParameters = null;
                m_includeListAttributes = null;
            } else {
                // if something is left, remove the processed entries
                m_includeList = m_includeList.subList(count, m_includeList.size());
                m_includeListParameters = m_includeListParameters.subList(count, m_includeListParameters.size());
                m_includeListAttributes = m_includeListAttributes.subList(count, m_includeListAttributes.size());
            }
        }
    }

    /**
     * Helper method to set a value in the internal header list.
     *
     * @param headers the headers to set the value in
     * @param name the name to set
     * @param value the value to set
     */
    private void setHeaderList(Map<String, List<String>> headers, String name, String value) {

        List<String> values = new ArrayList<String>();
        values.add(SET_HEADER + value);
        headers.put(name, values);
    }

    /** 
     * This delivers cached sub-elements back to the stream.
     * Needed to overcome JSP buffering.<p>
     *
     * @param res the response to write the cached results to
     * 
     * @throws IOException in case something goes wrong writing to the responses output stream
     */
    private void writeCachedResultToStream(HttpServletResponse res) throws IOException {

        List<Object> elements = m_cachedEntry.elements();
        int count = 0;
        if (elements != null) {
            for (int i = 0; i < elements.size(); i++) {
                Object o = elements.get(i);
                if (o instanceof byte[]) {
                    res.getOutputStream().write((byte[])o);
                } else {
                    if ((m_includeResults != null) && (m_includeResults.size() > count)) {
                        // make sure that we don't run behind end of list (should never happen, though)
                        res.getOutputStream().write(m_includeResults.get(count));
                        count++;
                    }
                    // skip next entry, which is the parameter map for this include call
                    i++;
                    // skip next entry, which is the attribute map for this include call
                    i++;
                }
            }
        }
    }
}