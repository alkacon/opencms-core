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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * This file is based on:
 * - org.apache.catalina.servlets.WebdavServlet
 * - org.apache.catalina.servlets.DefaultServlet
 * from the Apache Tomcat project.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencms.webdav;

import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.repository.A_CmsRepository;
import org.opencms.repository.CmsRepositoryLockInfo;
import org.opencms.repository.I_CmsRepositoryItem;
import org.opencms.repository.I_CmsRepositorySession;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsRequestUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

/**
 * Servlet which adds support for WebDAV level 2.<p>
 *
 * @since 6.5.6
 */
public class CmsWebdavServlet extends HttpServlet {

    /** Basic authorization prefix constant. */
    public static final String AUTHORIZATION_BASIC_PREFIX = "BASIC ";

    /** Size of file transfer buffer in bytes. */
    public static final int BUFFER_SIZE = 4096;

    /** Credentials separator constant. */
    public static final String SEPARATOR_CREDENTIALS = ":";

    /** Date format for the last modified date. */
    protected static final DateFormat HTTP_DATE_FORMAT;

    /** Date format for the creation date. */
    protected static final DateFormat ISO8601_FORMAT;

    /** MD5 message digest provider. */
    protected static MessageDigest m_md5Helper;

    /** The MD5 helper object for this class. */
    protected static final CmsMD5Encoder MD5_ENCODER = new CmsMD5Encoder();

    /** WebDAV method: COPY. */
    protected static final String METHOD_COPY = "COPY";

    /** HTTP Method: DELETE. */
    protected static final String METHOD_DELETE = "DELETE";

    /** HTTP Method: GET. */
    protected static final String METHOD_GET = "GET";

    /** HTTP Method: HEAD. */
    protected static final String METHOD_HEAD = "HEAD";

    /** WebDAV method: LOCK. */
    protected static final String METHOD_LOCK = "LOCK";

    /** WebDAV method: MKCOL. */
    protected static final String METHOD_MKCOL = "MKCOL";

    /** WebDAV method: MOVE. */
    protected static final String METHOD_MOVE = "MOVE";

    /** HTTP Method: OPTIONS. */
    protected static final String METHOD_OPTIONS = "OPTIONS";

    /** HTTP Method: POST. */
    protected static final String METHOD_POST = "POST";

    /** WebDAV method: PROPFIND. */
    protected static final String METHOD_PROPFIND = "PROPFIND";

    /** WebDAV method: PROPPATCH. */
    protected static final String METHOD_PROPPATCH = "PROPPATCH";

    /** HTTP Method: PUT. */
    protected static final String METHOD_PUT = "PUT";

    /** HTTP Method: TRACE. */
    protected static final String METHOD_TRACE = "TRACE";

    /** WebDAV method: UNLOCK. */
    protected static final String METHOD_UNLOCK = "UNLOCK";

    /** MIME multipart separation string. */
    protected static final String MIME_SEPARATION = "CATALINA_MIME_BOUNDARY";

    /** Chars which are safe for urls. */
    protected static final BitSet URL_SAFE_CHARS;

    /** Name of the servlet attribute to get the path to the temp directory. */
    private static final String ATT_SERVLET_TEMPDIR = "javax.servlet.context.tempdir";

    /** The text to use as basic realm. */
    private static final String BASIC_REALM = "OpenCms WebDAV Servlet";

    /** Default namespace. */
    private static final String DEFAULT_NAMESPACE = "DAV:";

    /** The text to send if the depth is inifinity. */
    private static final String DEPTH_INFINITY = "Infinity";

    /** PROPFIND - Display all properties. */
    private static final int FIND_ALL_PROP = 1;

    /** PROPFIND - Specify a property mask. */
    private static final int FIND_BY_PROPERTY = 0;

    /** PROPFIND - Return property names. */
    private static final int FIND_PROPERTY_NAMES = 2;

    /** Full range marker. */
    private static ArrayList<CmsWebdavRange> FULL_RANGE = new ArrayList<CmsWebdavRange>();

    /** The name of the header "allow". */
    private static final String HEADER_ALLOW = "Allow";

    /** The name of the header "authorization". */
    private static final String HEADER_AUTHORIZATION = "Authorization";

    /** The name of the header "content-length". */
    private static final String HEADER_CONTENTLENGTH = "content-length";

    /** The name of the header "Content-Range". */
    private static final String HEADER_CONTENTRANGE = "Content-Range";

    /** The name of the header "Depth". */
    private static final String HEADER_DEPTH = "Depth";

    /** The name of the header "Destination". */
    private static final String HEADER_DESTINATION = "Destination";

    /** The name of the header "ETag". */
    private static final String HEADER_ETAG = "ETag";

    /** The name of the header "If-Range". */
    private static final String HEADER_IFRANGE = "If-Range";

    /** The name of the header "Last-Modified". */
    private static final String HEADER_LASTMODIFIED = "Last-Modified";

    /** The name of the header "Lock-Token". */
    private static final String HEADER_LOCKTOKEN = "Lock-Token";

    /** The name of the header "Overwrite". */
    private static final String HEADER_OVERWRITE = "Overwrite";

    /** The name of the header "Range". */
    private static final String HEADER_RANGE = "Range";

    /** The name of the init parameter in the web.xml to allow listing. */
    private static final String INIT_PARAM_LIST = "listings";

    /** The name of the init parameter in the web.xml to set read only. */
    private static final String INIT_PARAM_READONLY = "readonly";

    /** The name of the init-param where the repository class is defined. */
    private static final String INIT_PARAM_REPOSITORY = "repository";

    /** Create a new lock. */
    private static final int LOCK_CREATION = 0;

    /** Refresh lock. */
    private static final int LOCK_REFRESH = 1;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsWebdavServlet.class);

    /** The repository used from this servlet. */
    private static A_CmsRepository m_repository;

    /** The unique serial id for this class. */
    private static final long serialVersionUID = -122598983283724306L;

    /** The name of the tag "activelock" in the WebDAV protocol. */
    private static final String TAG_ACTIVELOCK = "activelock";

    /** The name of the tag "collection" in the WebDAV protocol. */
    private static final String TAG_COLLECTION = "collection";

    /** The name of the tag "getcontentlanguage" in the WebDAV protocol. */
    private static final String TAG_CONTENTLANGUAGE = "getcontentlanguage";

    /** The name of the tag "getcontentlength" in the WebDAV protocol. */
    private static final String TAG_CONTENTLENGTH = "getcontentlength";

    /** The name of the tag "getcontenttype" in the WebDAV protocol. */
    private static final String TAG_CONTENTTYPE = "getcontenttype";

    /** The name of the tag "creationdate" in the WebDAV protocol. */
    private static final String TAG_CREATIONDATE = "creationdate";

    /** The name of the tag "depth" in the WebDAV protocol. */
    private static final String TAG_DEPTH = "depth";

    /** The name of the tag "displayname" in the WebDAV protocol. */
    private static final String TAG_DISPLAYNAME = "displayname";

    /** The name of the tag "getetag" in the WebDAV protocol. */
    private static final String TAG_ETAG = "getetag";

    /** The name of the tag "href" in the WebDAV protocol. */
    private static final String TAG_HREF = "href";

    /** The name of the tag "getlastmodified" in the WebDAV protocol. */
    private static final String TAG_LASTMODIFIED = "getlastmodified";

    /** The name of the tag "lockdiscovery" in the WebDAV protocol. */
    private static final String TAG_LOCKDISCOVERY = "lockdiscovery";

    /** The name of the tag "lockentry" in the WebDAV protocol. */
    private static final String TAG_LOCKENTRY = "lockentry";

    /** The name of the tag "lockscope" in the WebDAV protocol. */
    private static final String TAG_LOCKSCOPE = "lockscope";

    /** The name of the tag "locktoken" in the WebDAV protocol. */
    private static final String TAG_LOCKTOKEN = "locktoken";

    /** The name of the tag "locktype" in the WebDAV protocol. */
    private static final String TAG_LOCKTYPE = "locktype";

    /** The name of the tag "multistatus" in the WebDAV protocol. */
    private static final String TAG_MULTISTATUS = "multistatus";

    /** The name of the tag "owner" in the WebDAV protocol. */
    private static final String TAG_OWNER = "owner";

    /** The name of the tag "prop" in the WebDAV protocol. */
    private static final String TAG_PROP = "prop";

    /** The name of the tag "propstat" in the WebDAV protocol. */
    private static final String TAG_PROPSTAT = "propstat";

    /** The name of the tag "resourcetype" in the WebDAV protocol. */
    private static final String TAG_RESOURCETYPE = "resourcetype";

    /** The name of the tag "response" in the WebDAV protocol. */
    private static final String TAG_RESPONSE = "response";

    /** The name of the tag "source" in the WebDAV protocol. */
    private static final String TAG_SOURCE = "source";

    /** The name of the tag "status" in the WebDAV protocol. */
    private static final String TAG_STATUS = "status";

    /** The name of the tag "supportedlock" in the WebDAV protocol. */
    private static final String TAG_SUPPORTEDLOCK = "supportedlock";

    /** The name of the tag "timeout" in the WebDAV protocol. */
    private static final String TAG_TIMEOUT = "timeout";

    /** The text to send if the timeout is infinite. */
    private static final String TIMEOUT_INFINITE = "Infinite";

    /** The input buffer size to use when serving resources. */
    protected int m_input = 2048;

    /** The output buffer size to use when serving resources. */
    protected int m_output = 2048;

    /** Should we generate directory listings? */
    private boolean m_listings;

    /** Read only flag. By default, it's set to true. */
    private boolean m_readOnly = true;

    /** Secret information used to generate reasonably secure lock ids. */
    private String m_secret = "catalina";

    /** The session which handles the action made with WebDAV. */
    private I_CmsRepositorySession m_session;

    /** The name of the user found in the authorization header. */
    private String m_username;

    static {
        URL_SAFE_CHARS = new BitSet();
        URL_SAFE_CHARS.set('a', 'z' + 1);
        URL_SAFE_CHARS.set('A', 'Z' + 1);
        URL_SAFE_CHARS.set('0', '9' + 1);
        URL_SAFE_CHARS.set('-');
        URL_SAFE_CHARS.set('_');
        URL_SAFE_CHARS.set('.');
        URL_SAFE_CHARS.set('*');
        URL_SAFE_CHARS.set('/');
        URL_SAFE_CHARS.set(':');

        ISO8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO8601_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));

        HTTP_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        HTTP_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Adds an xml element to the given parent and sets the appropriate namespace and
     * prefix.<p>
     *
     * @param parent the parent node to add the element
     * @param name the name of the new element
     *
     * @return the created element with the given name which was added to the given parent
     */
    public static Element addElement(Element parent, String name) {

        return parent.addElement(new QName(name, Namespace.get("D", DEFAULT_NAMESPACE)));
    }

    /**
     * Initialize this servlet.<p>
     *
     * @throws ServletException if something goes wrong
     */
    @Override
    public void init() throws ServletException {

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_INIT_WEBDAV_SERVLET_0));
        }

        String value = null;

        // init parameter: listings
        try {
            value = getServletConfig().getInitParameter(INIT_PARAM_LIST);
            if (value != null) {
                m_listings = Boolean.valueOf(value).booleanValue();
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_READ_INIT_PARAM_ERROR_2, INIT_PARAM_LIST, value),
                    e);
            }
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(
                Messages.get().getBundle().key(
                    Messages.LOG_READ_INIT_PARAM_2,
                    INIT_PARAM_LIST,
                    Boolean.valueOf(m_listings)));
        }

        // init parameter: read only
        try {
            value = getServletConfig().getInitParameter(INIT_PARAM_READONLY);
            if (value != null) {
                m_readOnly = Boolean.valueOf(value).booleanValue();
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_READ_INIT_PARAM_ERROR_2, INIT_PARAM_READONLY, value),
                    e);
            }
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(
                Messages.get().getBundle().key(
                    Messages.LOG_READ_INIT_PARAM_2,
                    INIT_PARAM_READONLY,
                    Boolean.valueOf(m_readOnly)));
        }

        // Load the MD5 helper used to calculate signatures.
        try {
            m_md5Helper = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_MD5_NOT_AVAILABLE_0), e);
            }

            throw new UnavailableException(Messages.get().getBundle().key(Messages.ERR_MD5_NOT_AVAILABLE_0));
        }

        // Instantiate repository from init-param
        String repositoryName = getInitParameter(INIT_PARAM_REPOSITORY);
        if (repositoryName == null) {

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_INIT_PARAM_MISSING_1, INIT_PARAM_REPOSITORY));
            }

            throw new ServletException(
                Messages.get().getBundle().key(Messages.ERR_INIT_PARAM_MISSING_1, INIT_PARAM_REPOSITORY));
        }

        m_repository = OpenCms.getRepositoryManager().getRepository(repositoryName, A_CmsRepository.class);
        if (m_repository == null) {

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_REPOSITORY_NOT_FOUND_1, repositoryName));
            }

            throw new ServletException(
                Messages.get().getBundle().key(Messages.ERR_REPOSITORY_NOT_FOUND_1, repositoryName));
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_USE_REPOSITORY_1, repositoryName));
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).<p>
     *
     * @param item the RepositoryItem
     * @param is the input stream to copy from
     * @param writer the writer to write to
     *
     * @throws IOException if an input/output error occurs
     */
    protected void copy(I_CmsRepositoryItem item, InputStream is, PrintWriter writer) throws IOException {

        IOException exception = null;

        InputStream resourceInputStream = null;
        if (!item.isCollection()) {
            resourceInputStream = new ByteArrayInputStream(item.getContent());
        } else {
            resourceInputStream = is;
        }

        Reader reader = new InputStreamReader(resourceInputStream);

        // Copy the input stream to the output stream
        exception = copyRange(reader, writer);

        // Clean up the reader
        try {
            reader.close();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_READER_0), e);
            }
        }

        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).<p>
     *
     * @param item the RepositoryItem
     * @param is the input stream to copy from
     * @param ostream the output stream to write to
     *
     * @throws IOException if an input/output error occurs
     */
    protected void copy(I_CmsRepositoryItem item, InputStream is, ServletOutputStream ostream) throws IOException {

        IOException exception = null;
        InputStream resourceInputStream = null;

        // Optimization: If the binary content has already been loaded, send
        // it directly
        if (!item.isCollection()) {
            byte[] buffer = item.getContent();
            if (buffer != null) {
                ostream.write(buffer, 0, buffer.length);
                return;
            }
            resourceInputStream = new ByteArrayInputStream(item.getContent());
        } else {
            resourceInputStream = is;
        }

        InputStream istream = new BufferedInputStream(resourceInputStream, m_input);

        // Copy the input stream to the output stream
        exception = copyRange(istream, ostream);

        // Clean up the input stream
        try {
            istream.close();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_INPUT_STREAM_0), e);
            }
        }

        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).<p>
     *
     * @param item the RepositoryItem
     * @param writer the writer to write to
     * @param range the range the client wants to retrieve
     *
     * @throws IOException if an input/output error occurs
     */
    protected void copy(I_CmsRepositoryItem item, PrintWriter writer, CmsWebdavRange range) throws IOException {

        IOException exception = null;

        InputStream resourceInputStream = new ByteArrayInputStream(item.getContent());

        Reader reader = new InputStreamReader(resourceInputStream);
        exception = copyRange(reader, writer, range.getStart(), range.getEnd());

        // Clean up the input stream
        try {
            reader.close();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_READER_0), e);
            }
        }

        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).<p>
     *
     * @param item the RepositoryItem
     * @param writer the writer to write to
     * @param ranges iterator of the ranges the client wants to retrieve
     * @param contentType the content type of the resource
     *
     * @throws IOException if an input/output error occurs
     */
    protected void copy(
        I_CmsRepositoryItem item,
        PrintWriter writer,
        Iterator<CmsWebdavRange> ranges,
        String contentType) throws IOException {

        IOException exception = null;

        while ((exception == null) && (ranges.hasNext())) {

            InputStream resourceInputStream = new ByteArrayInputStream(item.getContent());

            Reader reader = new InputStreamReader(resourceInputStream);
            CmsWebdavRange currentRange = ranges.next();

            // Writing MIME header.
            writer.println();
            writer.println("--" + MIME_SEPARATION);
            if (contentType != null) {
                writer.println("Content-Type: " + contentType);
            }
            writer.println(
                "Content-Range: bytes "
                    + currentRange.getStart()
                    + "-"
                    + currentRange.getEnd()
                    + "/"
                    + currentRange.getLength());
            writer.println();

            // Printing content
            exception = copyRange(reader, writer, currentRange.getStart(), currentRange.getEnd());

            try {
                reader.close();
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_READER_0), e);
                }
            }

        }

        writer.println();
        writer.print("--" + MIME_SEPARATION + "--");

        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).<p>
     *
     * @param item the RepositoryItem
     * @param ostream the output stream to write to
     * @param range the range the client wants to retrieve
     *
     * @throws IOException if an input/output error occurs
     */
    protected void copy(I_CmsRepositoryItem item, ServletOutputStream ostream, CmsWebdavRange range)
    throws IOException {

        IOException exception = null;

        InputStream resourceInputStream = new ByteArrayInputStream(item.getContent());
        InputStream istream = new BufferedInputStream(resourceInputStream, m_input);
        exception = copyRange(istream, ostream, range.getStart(), range.getEnd());

        // Clean up the input stream
        try {
            istream.close();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_INPUT_STREAM_0), e);
            }
        }

        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).<p>
     *
     * @param item the RepositoryItem
     * @param ostream the output stream to write to
     * @param ranges iterator of the ranges the client wants to retrieve
     * @param contentType the content type of the resource
     *
     * @throws IOException if an input/output error occurs
     */
    protected void copy(
        I_CmsRepositoryItem item,
        ServletOutputStream ostream,
        Iterator<CmsWebdavRange> ranges,
        String contentType) throws IOException {

        IOException exception = null;

        while ((exception == null) && (ranges.hasNext())) {

            InputStream resourceInputStream = new ByteArrayInputStream(item.getContent());
            InputStream istream = new BufferedInputStream(resourceInputStream, m_input);

            CmsWebdavRange currentRange = ranges.next();

            // Writing MIME header.
            ostream.println();
            ostream.println("--" + MIME_SEPARATION);
            if (contentType != null) {
                ostream.println("Content-Type: " + contentType);
            }
            ostream.println(
                "Content-Range: bytes "
                    + currentRange.getStart()
                    + "-"
                    + currentRange.getEnd()
                    + "/"
                    + currentRange.getLength());
            ostream.println();

            // Printing content
            exception = copyRange(istream, ostream, currentRange.getStart(), currentRange.getEnd());

            try {
                istream.close();
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_INPUT_STREAM_0), e);
                }
            }

        }

        ostream.println();
        ostream.print("--" + MIME_SEPARATION + "--");

        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).<p>
     *
     * @param istream the input stream to read from
     * @param ostream the output stream to write to
     *
     * @return the exception which occurred during processing
     */
    protected IOException copyRange(InputStream istream, ServletOutputStream ostream) {

        // Copy the input stream to the output stream
        IOException exception = null;
        byte[] buffer = new byte[m_input];
        int len = buffer.length;
        while (true) {
            try {
                len = istream.read(buffer);
                if (len == -1) {
                    break;
                }
                ostream.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).<p>
     *
     * @param istream the input stream to read from
     * @param ostream the output stream to write to
     * @param start the start of the range which will be copied
     * @param end the end of the range which will be copied
     *
     * @return the exception which occurred during processing
     */
    protected IOException copyRange(InputStream istream, ServletOutputStream ostream, long start, long end) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SERVE_BYTES_2, new Long(start), new Long(end)));
        }

        try {
            istream.skip(start);
        } catch (IOException e) {
            return e;
        }

        IOException exception = null;
        long bytesToRead = (end - start) + 1;

        byte[] buffer = new byte[m_input];
        int len = buffer.length;
        while ((bytesToRead > 0) && (len >= buffer.length)) {
            try {
                len = istream.read(buffer);
                if (bytesToRead >= len) {
                    ostream.write(buffer, 0, len);
                    bytesToRead -= len;
                } else {
                    ostream.write(buffer, 0, (int)bytesToRead);
                    bytesToRead = 0;
                }
            } catch (IOException e) {
                exception = e;
                len = -1;
            }

            if (len < buffer.length) {
                break;
            }
        }

        return exception;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).<p>
     *
     * @param reader the reader to read from
     * @param writer the writer to write to
     *
     * @return the exception which occurred during processing
     */
    protected IOException copyRange(Reader reader, PrintWriter writer) {

        // Copy the input stream to the output stream
        IOException exception = null;
        char[] buffer = new char[m_input];
        int len = buffer.length;
        while (true) {
            try {
                len = reader.read(buffer);
                if (len == -1) {
                    break;
                }
                writer.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).<p>
     *
     * @param reader the reader to read from
     * @param writer the writer to write to
     * @param start the start of the range which will be copied
     * @param end the end of the range which will be copied
     *
     * @return the exception which occurred during processing
     */
    protected IOException copyRange(Reader reader, PrintWriter writer, long start, long end) {

        try {
            reader.skip(start);
        } catch (IOException e) {
            return e;
        }

        IOException exception = null;
        long bytesToRead = (end - start) + 1;

        char[] buffer = new char[m_input];
        int len = buffer.length;
        while ((bytesToRead > 0) && (len >= buffer.length)) {
            try {
                len = reader.read(buffer);
                if (bytesToRead >= len) {
                    writer.write(buffer, 0, len);
                    bytesToRead -= len;
                } else {
                    writer.write(buffer, 0, (int)bytesToRead);
                    bytesToRead = 0;
                }
            } catch (IOException e) {
                exception = e;
                len = -1;
            }

            if (len < buffer.length) {
                break;
            }
        }

        return exception;
    }

    /**
     * Process a COPY WebDAV request for the specified resource.<p>
     *
     * @param req the servlet request we are processing
     * @param resp the servlet response we are creating
     */
    protected void doCopy(HttpServletRequest req, HttpServletResponse resp) {

        // Check if webdav is set to read only
        if (m_readOnly) {

            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_WEBDAV_READ_ONLY_0));
            }

            return;
        }

        // Get the source path to copy
        String src = getRelativePath(req);

        // Check if source exists
        if (!m_session.exists(src)) {

            resp.setStatus(CmsWebdavStatus.SC_NOT_FOUND);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_NOT_FOUND_1, src));
            }

            return;
        }

        // Get the destination path to copy to
        String dest = parseDestinationHeader(req);
        if (dest == null) {

            resp.setStatus(CmsWebdavStatus.SC_BAD_REQUEST);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_PARSE_DEST_HEADER_0));
            }

            return;
        }

        // source and destination are the same
        if (dest.equals(src)) {

            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_SRC_DEST_EQUALS_0));
            }

            return;
        }

        // Parsing overwrite header
        boolean overwrite = parseOverwriteHeader(req);

        // If the destination exists, then it's a conflict
        if ((m_session.exists(dest)) && (!overwrite)) {

            resp.setStatus(CmsWebdavStatus.SC_PRECONDITION_FAILED);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEST_PATH_EXISTS_1, dest));
            }

            return;
        }

        if ((!m_session.exists(dest)) && (overwrite)) {
            resp.setStatus(CmsWebdavStatus.SC_CREATED);
        }

        // Copying source to destination
        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_COPY_ITEM_2, src, dest));
            }

            m_session.copy(src, dest, overwrite);
        } catch (CmsSecurityException sex) {
            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_NO_PERMISSION_0));
            }

            return;
        } catch (CmsVfsResourceAlreadyExistsException raeex) {

            // should never happen
            resp.setStatus(CmsWebdavStatus.SC_PRECONDITION_FAILED);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_EXISTS_1, dest));
            }

            return;
        } catch (CmsVfsResourceNotFoundException rnfex) {

            // should never happen
            resp.setStatus(CmsWebdavStatus.SC_NOT_FOUND);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_NOT_FOUND_1, src));
            }

            return;
        } catch (CmsException ex) {
            resp.setStatus(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR);

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_REPOSITORY_ERROR_2, "COPY", src), ex);
            }
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_COPY_SUCCESS_0));
        }
    }

    /**
     * Process a DELETE WebDAV request for the specified resource.<p>
     *
     * @param req the servlet request we are processing
     * @param resp the servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // Get the path to delete
        String path = getRelativePath(req);

        // Check if webdav is set to read only
        if (m_readOnly) {

            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_WEBDAV_READ_ONLY_0));
            }

            return;
        }

        // Check if path exists
        boolean exists = m_session.exists(path);
        if (!exists) {

            resp.setStatus(CmsWebdavStatus.SC_NOT_FOUND);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_NOT_FOUND_1, path));
            }

            return;
        }

        // Check if resource is locked
        if (isLocked(req)) {

            resp.setStatus(CmsWebdavStatus.SC_LOCKED);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_LOCKED_1, path));
            }

            return;
        }

        // Check if resources found in the tree of the path are locked
        Hashtable<String, Integer> errorList = new Hashtable<String, Integer>();

        checkChildLocks(req, path, errorList);
        if (!errorList.isEmpty()) {
            sendReport(req, resp, errorList);

            if (LOG.isDebugEnabled()) {
                Iterator<String> iter = errorList.keySet().iterator();
                while (iter.hasNext()) {
                    String errorPath = iter.next();
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_CHILD_LOCKED_1, errorPath));
                }
            }

            return;
        }

        // Delete the resource
        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DELETE_ITEM_0));
            }

            m_session.delete(path);
        } catch (CmsVfsResourceNotFoundException rnfex) {

            // should never happen
            resp.setStatus(CmsWebdavStatus.SC_NOT_FOUND);
            return;
        } catch (CmsSecurityException sex) {
            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_NO_PERMISSION_0));
            }

            return;
        } catch (CmsException ex) {
            resp.setStatus(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR);

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_REPOSITORY_ERROR_2, "DELETE", path), ex);
            }

            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_DELETE_SUCCESS_0));
        }

        resp.setStatus(CmsWebdavStatus.SC_NO_CONTENT);
    }

    /**
     * Process a GET request for the specified resource.<p>
     *
     * @param request the servlet request we are processing
     * @param response the servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Serve the requested resource, including the data content
        serveResource(request, response, true);
    }

    /**
     * Process a HEAD request for the specified resource.<p>
     *
     * @param request the servlet request we are processing
     * @param response the servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Serve the requested resource, without the data content
        serveResource(request, response, false);
    }

    /**
     * Process a LOCK WebDAV request for the specified resource.<p>
     *
     * @param req the servlet request we are processing
     * @param resp the servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     */
    @SuppressWarnings("unchecked")
    protected void doLock(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String path = getRelativePath(req);

        // Check if webdav is set to read only
        if (m_readOnly) {

            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_WEBDAV_READ_ONLY_0));
            }

            return;
        }

        // Check if resource is locked
        if (isLocked(req)) {

            resp.setStatus(CmsWebdavStatus.SC_LOCKED);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_LOCKED_1, path));
            }

            return;
        }

        CmsRepositoryLockInfo lock = new CmsRepositoryLockInfo();

        // Parsing depth header
        String depthStr = req.getHeader(HEADER_DEPTH);
        if (depthStr == null) {
            lock.setDepth(CmsRepositoryLockInfo.DEPTH_INFINITY_VALUE);
        } else {
            if (depthStr.equals("0")) {
                lock.setDepth(0);
            } else {
                lock.setDepth(CmsRepositoryLockInfo.DEPTH_INFINITY_VALUE);
            }
        }

        // Parsing timeout header
        int lockDuration = CmsRepositoryLockInfo.TIMEOUT_INFINITE_VALUE;
        lock.setExpiresAt(System.currentTimeMillis() + (lockDuration * 1000));

        int lockRequestType = LOCK_CREATION;

        Element lockInfoNode = null;

        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new InputSource(req.getInputStream()));

            // Get the root element of the document
            Element rootElement = document.getRootElement();
            lockInfoNode = rootElement;
        } catch (Exception e) {
            lockRequestType = LOCK_REFRESH;
        }

        if (lockInfoNode != null) {

            // Reading lock information
            Iterator<Element> iter = lockInfoNode.elementIterator();

            Element lockScopeNode = null;
            Element lockTypeNode = null;
            Element lockOwnerNode = null;

            while (iter.hasNext()) {
                Element currentElem = iter.next();
                switch (currentElem.getNodeType()) {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        String nodeName = currentElem.getName();
                        if (nodeName.endsWith(TAG_LOCKSCOPE)) {
                            lockScopeNode = currentElem;
                        }
                        if (nodeName.endsWith(TAG_LOCKTYPE)) {
                            lockTypeNode = currentElem;
                        }
                        if (nodeName.endsWith(TAG_OWNER)) {
                            lockOwnerNode = currentElem;
                        }
                        break;
                    default:
                        break;
                }
            }

            if (lockScopeNode != null) {

                iter = lockScopeNode.elementIterator();
                while (iter.hasNext()) {
                    Element currentElem = iter.next();
                    switch (currentElem.getNodeType()) {
                        case Node.TEXT_NODE:
                            break;
                        case Node.ELEMENT_NODE:
                            String tempScope = currentElem.getName();
                            if (tempScope.indexOf(':') != -1) {
                                lock.setScope(tempScope.substring(tempScope.indexOf(':') + 1));
                            } else {
                                lock.setScope(tempScope);
                            }
                            break;
                        default:
                            break;
                    }
                }

                if (lock.getScope() == null) {

                    // Bad request
                    resp.setStatus(CmsWebdavStatus.SC_BAD_REQUEST);
                }

            } else {

                // Bad request
                resp.setStatus(CmsWebdavStatus.SC_BAD_REQUEST);
            }

            if (lockTypeNode != null) {

                iter = lockTypeNode.elementIterator();
                while (iter.hasNext()) {
                    Element currentElem = iter.next();
                    switch (currentElem.getNodeType()) {
                        case Node.TEXT_NODE:
                            break;
                        case Node.ELEMENT_NODE:
                            String tempType = currentElem.getName();
                            if (tempType.indexOf(':') != -1) {
                                lock.setType(tempType.substring(tempType.indexOf(':') + 1));
                            } else {
                                lock.setType(tempType);
                            }
                            break;
                        default:
                            break;
                    }
                }

                if (lock.getType() == null) {

                    // Bad request
                    resp.setStatus(CmsWebdavStatus.SC_BAD_REQUEST);
                }

            } else {

                // Bad request
                resp.setStatus(CmsWebdavStatus.SC_BAD_REQUEST);
            }

            if (lockOwnerNode != null) {

                iter = lockOwnerNode.elementIterator();
                while (iter.hasNext()) {
                    Element currentElem = iter.next();
                    switch (currentElem.getNodeType()) {
                        case Node.TEXT_NODE:
                            lock.setOwner(lock.getOwner() + currentElem.getStringValue());
                            break;
                        case Node.ELEMENT_NODE:
                            lock.setOwner(lock.getOwner() + currentElem.getStringValue());
                            break;
                        default:
                            break;
                    }
                }

                if (lock.getOwner() == null) {

                    // Bad request
                    resp.setStatus(CmsWebdavStatus.SC_BAD_REQUEST);
                }

            } else {
                lock.setOwner("");
            }

        }

        lock.setPath(path);
        lock.setUsername(m_username);

        if (lockRequestType == LOCK_REFRESH) {

            CmsRepositoryLockInfo currentLock = m_session.getLock(path);
            if (currentLock == null) {
                lockRequestType = LOCK_CREATION;
            }
        }

        if (lockRequestType == LOCK_CREATION) {

            try {

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_LOCK_ITEM_1, lock.getOwner()));
                }

                boolean result = m_session.lock(path, lock);
                if (result) {

                    // Add the Lock-Token header as by RFC 2518 8.10.1
                    // - only do this for newly created locks
                    resp.addHeader(HEADER_LOCKTOKEN, "<opaquelocktoken:" + generateLockToken(req, lock) + ">");

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_LOCK_ITEM_FAILED_0));
                    }

                } else {

                    resp.setStatus(CmsWebdavStatus.SC_LOCKED);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_LOCK_ITEM_SUCCESS_0));
                    }

                    return;
                }
            } catch (CmsVfsResourceNotFoundException rnfex) {
                resp.setStatus(CmsWebdavStatus.SC_NOT_FOUND);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_NOT_FOUND_1, path));
                }

                return;
            } catch (CmsSecurityException sex) {
                resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_NO_PERMISSION_0));
                }

                return;
            } catch (CmsException ex) {
                resp.setStatus(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR);

                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_REPOSITORY_ERROR_2, "LOCK", path), ex);
                }

                return;
            }
        }

        // Set the status, then generate the XML response containing
        // the lock information
        Document doc = DocumentHelper.createDocument();
        Element propElem = doc.addElement(new QName(TAG_PROP, Namespace.get(DEFAULT_NAMESPACE)));

        Element lockElem = addElement(propElem, TAG_LOCKDISCOVERY);
        addLockElement(lock, lockElem, generateLockToken(req, lock));

        resp.setStatus(CmsWebdavStatus.SC_OK);
        resp.setContentType("text/xml; charset=UTF-8");

        Writer writer = resp.getWriter();
        doc.write(writer);
        writer.close();
    }

    /**
     * Process a MKCOL WebDAV request for the specified resource.<p>
     *
     * @param req the servlet request we are processing
     * @param resp the servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     */
    protected void doMkcol(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String path = getRelativePath(req);

        // Check if Webdav is read only
        if (m_readOnly) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_WEBDAV_READ_ONLY_0));
            }

            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);
            return;
        }

        // Check if resource is locked
        if (isLocked(req)) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_LOCKED_1, path));
            }

            resp.setStatus(CmsWebdavStatus.SC_LOCKED);
            return;
        }

        boolean exists = m_session.exists(path);

        // Can't create a collection if a resource already exists at the given path
        if (exists) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_EXISTS_1, path));
            }

            // Get allowed methods
            StringBuffer methodsAllowed = determineMethodsAllowed(getRelativePath(req));
            resp.addHeader(HEADER_ALLOW, methodsAllowed.toString());
            resp.setStatus(CmsWebdavStatus.SC_METHOD_NOT_ALLOWED);
            return;
        }

        if (req.getInputStream().available() > 0) {
            try {
                new SAXReader().read(req.getInputStream());

                // TODO: Process this request body (from Apache Tomcat)
                resp.setStatus(CmsWebdavStatus.SC_NOT_IMPLEMENTED);
                return;

            } catch (DocumentException de) {

                // Parse error - assume invalid content
                resp.setStatus(CmsWebdavStatus.SC_BAD_REQUEST);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_INVALID_CONTENT_0));
                }

                return;
            }
        }

        // call session to create collection
        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_CREATE_COLLECTION_0));
            }

            m_session.create(path);
        } catch (CmsVfsResourceAlreadyExistsException raeex) {

            // should never happen, because it was checked if the item exists before
            resp.setStatus(CmsWebdavStatus.SC_PRECONDITION_FAILED);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_EXISTS_1, path));
            }

            return;
        } catch (CmsSecurityException sex) {

            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_NO_PERMISSION_0));
            }

            return;
        } catch (CmsException ex) {

            resp.setStatus(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR);

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_REPOSITORY_ERROR_2, "MKCOL", path), ex);
            }

            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_CREATE_SUCCESS_0));
        }

        resp.setStatus(CmsWebdavStatus.SC_CREATED);
    }

    /**
     * Process a MOVE WebDAV request for the specified resource.<p>
     *
     * @param req the servlet request we are processing
     * @param resp the servlet response we are creating
     */
    protected void doMove(HttpServletRequest req, HttpServletResponse resp) {

        // Get source path
        String src = getRelativePath(req);

        // Check if Webdav is read only
        if (m_readOnly) {

            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_WEBDAV_READ_ONLY_0));
            }

            return;
        }

        // Check if resource is locked
        if (isLocked(req)) {

            resp.setStatus(CmsWebdavStatus.SC_LOCKED);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_LOCKED_1, src));
            }

            return;
        }

        // Parsing destination header
        String dest = parseDestinationHeader(req);
        if (dest == null) {

            resp.setStatus(CmsWebdavStatus.SC_BAD_REQUEST);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_PARSE_DEST_HEADER_0));
            }

            return;
        }

        // source and destination are the same
        if (dest.equals(src)) {

            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_SRC_DEST_EQUALS_0));
            }

            return;
        }

        // Parsing overwrite header
        boolean overwrite = parseOverwriteHeader(req);

        // Check if source exists
        if (!m_session.exists(src)) {

            resp.setStatus(CmsWebdavStatus.SC_NOT_FOUND);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_NOT_FOUND_1, src));
            }

            return;
        }

        // If the destination exists, then it's a conflict
        if ((m_session.exists(dest)) && (!overwrite)) {

            resp.setStatus(CmsWebdavStatus.SC_PRECONDITION_FAILED);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEST_PATH_EXISTS_1, dest));
            }

            return;
        }

        if ((!m_session.exists(dest)) && (overwrite)) {
            resp.setStatus(CmsWebdavStatus.SC_CREATED);
        }

        // trigger move in session handler
        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_MOVE_ITEM_2, src, dest));
            }

            m_session.move(src, dest, overwrite);
        } catch (CmsVfsResourceNotFoundException rnfex) {
            resp.setStatus(CmsWebdavStatus.SC_NOT_FOUND);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_NOT_FOUND_1, src));
            }

            return;
        } catch (CmsSecurityException sex) {
            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_NO_PERMISSION_0));
            }

            return;
        } catch (CmsVfsResourceAlreadyExistsException raeex) {
            resp.setStatus(CmsWebdavStatus.SC_PRECONDITION_FAILED);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_EXISTS_1, dest));
            }

            return;
        } catch (CmsException ex) {
            resp.setStatus(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR);

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_REPOSITORY_ERROR_2, "MOVE", src), ex);
            }

            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_MOVE_ITEM_SUCCESS_0));
        }
    }

    /**
     * Process a OPTIONS WebDAV request for the specified resource.<p>
     *
     * @param req the servlet request we are processing
     * @param resp the servlet response we are creating
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {

        resp.addHeader("DAV", "1,2");

        StringBuffer methodsAllowed = determineMethodsAllowed(getRelativePath(req));

        resp.addHeader(HEADER_ALLOW, methodsAllowed.toString());
        resp.addHeader("MS-Author-Via", "DAV");
    }

    /**
     * Process a PROPFIND WebDAV request for the specified resource.<p>
     *
     * @param req the servlet request we are processing
     * @param resp the servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     */
    protected void doPropfind(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String path = getRelativePath(req);

        if (!m_listings) {

            // Get allowed methods
            StringBuffer methodsAllowed = determineMethodsAllowed(getRelativePath(req));

            resp.addHeader(HEADER_ALLOW, methodsAllowed.toString());
            resp.setStatus(CmsWebdavStatus.SC_METHOD_NOT_ALLOWED);
            return;
        }

        // Properties which are to be displayed.
        List<String> properties = new Vector<String>();

        // Propfind depth
        int depth = CmsRepositoryLockInfo.DEPTH_INFINITY_VALUE;

        // Propfind type
        int type = FIND_ALL_PROP;

        String depthStr = req.getHeader(HEADER_DEPTH);

        if (depthStr == null) {
            depth = CmsRepositoryLockInfo.DEPTH_INFINITY_VALUE;
        } else {
            if (depthStr.equals("0")) {
                depth = 0;
            } else if (depthStr.equals("1")) {
                depth = 1;
            } else if (depthStr.equalsIgnoreCase(DEPTH_INFINITY)) {
                depth = CmsRepositoryLockInfo.DEPTH_INFINITY_VALUE;
            }
        }

        Element propNode = null;

        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(req.getInputStream());

            // Get the root element of the document
            Element rootElement = document.getRootElement();
            @SuppressWarnings("unchecked")
            Iterator<Element> iter = rootElement.elementIterator();

            while (iter.hasNext()) {
                Element currentElem = iter.next();
                switch (currentElem.getNodeType()) {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        if (currentElem.getName().endsWith("prop")) {
                            type = FIND_BY_PROPERTY;
                            propNode = currentElem;
                        }
                        if (currentElem.getName().endsWith("propname")) {
                            type = FIND_PROPERTY_NAMES;
                        }
                        if (currentElem.getName().endsWith("allprop")) {
                            type = FIND_ALL_PROP;
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            // Most likely there was no content : we use the defaults.
        }

        if (propNode != null) {
            if (type == FIND_BY_PROPERTY) {
                @SuppressWarnings("unchecked")
                Iterator<Element> iter = propNode.elementIterator();
                while (iter.hasNext()) {
                    Element currentElem = iter.next();
                    switch (currentElem.getNodeType()) {
                        case Node.TEXT_NODE:
                            break;
                        case Node.ELEMENT_NODE:
                            String nodeName = currentElem.getName();
                            String propertyName = null;
                            if (nodeName.indexOf(':') != -1) {
                                propertyName = nodeName.substring(nodeName.indexOf(':') + 1);
                            } else {
                                propertyName = nodeName;
                            }
                            // href is a live property which is handled differently
                            properties.add(propertyName);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        boolean exists = m_session.exists(path);
        if (!exists) {

            resp.setStatus(CmsWebdavStatus.SC_NOT_FOUND);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_NOT_FOUND_1, path));
            }

            return;
        }

        I_CmsRepositoryItem item = null;
        try {
            item = m_session.getItem(path);
        } catch (CmsException e) {
            resp.setStatus(CmsWebdavStatus.SC_NOT_FOUND);
            return;
        }

        resp.setStatus(CmsWebdavStatus.SC_MULTI_STATUS);
        resp.setContentType("text/xml; charset=UTF-8");

        // Create multistatus object
        Document doc = DocumentHelper.createDocument();
        Element multiStatusElem = doc.addElement(new QName(TAG_MULTISTATUS, Namespace.get("D", DEFAULT_NAMESPACE)));

        if (depth == 0) {
            parseProperties(req, multiStatusElem, item, type, properties);
        } else {
            // The stack always contains the object of the current level
            Stack<I_CmsRepositoryItem> stack = new Stack<I_CmsRepositoryItem>();
            stack.push(item);

            // Stack of the objects one level below
            Stack<I_CmsRepositoryItem> stackBelow = new Stack<I_CmsRepositoryItem>();

            while ((!stack.isEmpty()) && (depth >= 0)) {

                I_CmsRepositoryItem currentItem = stack.pop();
                parseProperties(req, multiStatusElem, currentItem, type, properties);

                if ((currentItem.isCollection()) && (depth > 0)) {

                    try {
                        List<I_CmsRepositoryItem> list = m_session.list(currentItem.getName());
                        Iterator<I_CmsRepositoryItem> iter = list.iterator();
                        while (iter.hasNext()) {
                            I_CmsRepositoryItem element = iter.next();
                            stackBelow.push(element);
                        }

                    } catch (CmsException e) {

                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                        if (LOG.isErrorEnabled()) {
                            LOG.error(
                                Messages.get().getBundle().key(Messages.LOG_LIST_ITEMS_ERROR_1, currentItem.getName()),
                                e);
                        }

                        return;
                    }
                }

                if (stack.isEmpty()) {
                    depth--;
                    stack = stackBelow;
                    stackBelow = new Stack<I_CmsRepositoryItem>();
                }
            }
        }

        Writer writer = resp.getWriter();
        doc.write(writer);
        writer.close();
    }

    /**
     * Process a PROPPATCH WebDAV request for the specified resource.<p>
     *
     * Not implemented yet.<p>
     *
     * @param req the servlet request we are processing
     * @param resp the servlet response we are creating
     */
    protected void doProppatch(HttpServletRequest req, HttpServletResponse resp) {

        // Check if Webdav is read only
        if (m_readOnly) {

            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_WEBDAV_READ_ONLY_0));
            }

            return;
        }

        // Check if resource is locked
        if (isLocked(req)) {

            resp.setStatus(CmsWebdavStatus.SC_LOCKED);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_LOCKED_1, getRelativePath(req)));
            }

            return;
        }

        resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    /**
     * Process a POST request for the specified resource.<p>
     *
     * @param req the servlet request we are processing
     * @param resp the servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String path = getRelativePath(req);

        // Check if webdav is set to read only
        if (m_readOnly) {

            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_WEBDAV_READ_ONLY_0));
            }

            return;
        }

        // Check if resource is locked
        if (isLocked(req)) {

            resp.setStatus(CmsWebdavStatus.SC_LOCKED);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_LOCKED_1, path));
            }

            return;
        }

        boolean exists = m_session.exists(path);
        boolean result = true;

        // Temp. content file used to support partial PUT
        File contentFile = null;

        CmsWebdavRange range = parseContentRange(req, resp);

        InputStream resourceInputStream = null;

        // Append data specified in ranges to existing content for this
        // resource - create a temp. file on the local filesystem to
        // perform this operation
        // Assume just one range is specified for now
        if (range != null) {
            contentFile = executePartialPut(req, range, path);
            resourceInputStream = new FileInputStream(contentFile);
        } else {
            resourceInputStream = req.getInputStream();
        }

        try {

            // FIXME: Add attributes(from Apache Tomcat)
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_SAVE_ITEM_0));
            }

            m_session.save(path, resourceInputStream, exists);
        } catch (Exception e) {

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_REPOSITORY_ERROR_2, "PUT", path), e);
            }

            result = false;
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
        }

        // Bugzilla 40326: at this point content file should be safe to delete
        // as it's no longer referenced.  Let's not rely on deleteOnExit because
        // it's a memory leak, as noted in this Bugzilla issue.
        if (contentFile != null) {
            try {
                contentFile.delete();
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_DELETE_TEMP_FILE_0), e);
                }
            }
        }

        if (result) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_SAVE_SUCCESS_0));
            }

            if (exists) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }
        }
    }

    /**
     * Process a UNLOCK WebDAV request for the specified resource.<p>
     *
     * @param req the servlet request we are processing
     * @param resp the servlet response we are creating
     */
    protected void doUnlock(HttpServletRequest req, HttpServletResponse resp) {

        String path = getRelativePath(req);

        // Check if Webdav is read only
        if (m_readOnly) {

            resp.setStatus(CmsWebdavStatus.SC_FORBIDDEN);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_WEBDAV_READ_ONLY_0));
            }

            return;
        }

        // Check if resource is locked
        if (isLocked(req)) {

            resp.setStatus(CmsWebdavStatus.SC_LOCKED);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_LOCKED_1, path));
            }

            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_UNLOCK_ITEM_0));
        }

        m_session.unlock(path);

        resp.setStatus(CmsWebdavStatus.SC_NO_CONTENT);
    }

    /**
     * Handle a partial PUT.<p>
     *
     * New content specified in request is appended to
     * existing content in oldRevisionContent (if present). This code does
     * not support simultaneous partial updates to the same resource.<p>
     *
     * @param req the servlet request we are processing
     * @param range the range of the content in the file
     * @param path the path where to find the resource
     *
     * @return the new content file with the appended data
     *
     * @throws IOException if an input/output error occurs
     */
    protected File executePartialPut(HttpServletRequest req, CmsWebdavRange range, String path) throws IOException {

        // Append data specified in ranges to existing content for this
        // resource - create a temp. file on the local filesystem to
        // perform this operation
        File tempDir = (File)getServletContext().getAttribute(ATT_SERVLET_TEMPDIR);

        // Convert all '/' characters to '.' in resourcePath
        String convertedResourcePath = path.replace('/', '.');
        File contentFile = new File(tempDir, convertedResourcePath);
        contentFile.createNewFile();

        RandomAccessFile randAccessContentFile = new RandomAccessFile(contentFile, "rw");

        InputStream oldResourceStream = null;
        try {
            I_CmsRepositoryItem item = m_session.getItem(path);

            oldResourceStream = new ByteArrayInputStream(item.getContent());
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_ITEM_NOT_FOUND_1, path), e);
            }
        }

        // Copy data in oldRevisionContent to contentFile
        if (oldResourceStream != null) {

            int numBytesRead;
            byte[] copyBuffer = new byte[BUFFER_SIZE];
            while ((numBytesRead = oldResourceStream.read(copyBuffer)) != -1) {
                randAccessContentFile.write(copyBuffer, 0, numBytesRead);
            }

            oldResourceStream.close();
        }

        randAccessContentFile.setLength(range.getLength());

        // Append data in request input stream to contentFile
        randAccessContentFile.seek(range.getStart());
        int numBytesRead;
        byte[] transferBuffer = new byte[BUFFER_SIZE];
        BufferedInputStream requestBufInStream = new BufferedInputStream(req.getInputStream(), BUFFER_SIZE);
        while ((numBytesRead = requestBufInStream.read(transferBuffer)) != -1) {
            randAccessContentFile.write(transferBuffer, 0, numBytesRead);
        }
        randAccessContentFile.close();
        requestBufInStream.close();

        return contentFile;
    }

    /**
     * Get the ETag associated with a file.<p>
     *
     * @param item the WebDavItem
     *
     * @return the created ETag for the resource attributes
     */
    protected String getETag(I_CmsRepositoryItem item) {

        return "\"" + item.getContentLength() + "-" + item.getLastModifiedDate() + "\"";
    }

    /**
     * Parse the range header.<p>
     *
     * @param request the servlet request we are processing
     * @param response the servlet response we are creating
     * @param item the WebdavItem with the information
     *
     * @return Vector of ranges
     */
    protected ArrayList<CmsWebdavRange> parseRange(
        HttpServletRequest request,
        HttpServletResponse response,
        I_CmsRepositoryItem item) {

        // Checking If-Range
        String headerValue = request.getHeader(HEADER_IFRANGE);

        if (headerValue != null) {

            long headerValueTime = (-1L);
            try {
                headerValueTime = request.getDateHeader(HEADER_IFRANGE);
            } catch (Exception e) {
                // noop
            }

            String eTag = getETag(item);
            long lastModified = item.getLastModifiedDate();

            if (headerValueTime == (-1L)) {

                // If the ETag the client gave does not match the entity
                // etag, then the entire entity is returned.
                if (!eTag.equals(headerValue.trim())) {
                    return FULL_RANGE;
                }

            } else {

                // If the timestamp of the entity the client got is older than
                // the last modification date of the entity, the entire entity
                // is returned.
                if (lastModified > (headerValueTime + 1000)) {
                    return FULL_RANGE;
                }
            }
        }

        long fileLength = item.getContentLength();

        if (fileLength == 0) {
            return null;
        }

        // Retrieving the range header (if any is specified
        String rangeHeader = request.getHeader(HEADER_RANGE);

        if (rangeHeader == null) {
            return null;
        }

        // bytes is the only range unit supported (and I don't see the point
        // of adding new ones).
        if (!rangeHeader.startsWith("bytes")) {
            response.addHeader(HEADER_CONTENTRANGE, "bytes */" + fileLength);
            response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return null;
        }

        rangeHeader = rangeHeader.substring(6);

        // Vector which will contain all the ranges which are successfully parsed.
        ArrayList<CmsWebdavRange> result = new ArrayList<CmsWebdavRange>();
        StringTokenizer commaTokenizer = new StringTokenizer(rangeHeader, ",");

        // Parsing the range list
        while (commaTokenizer.hasMoreTokens()) {
            String rangeDefinition = commaTokenizer.nextToken().trim();

            CmsWebdavRange currentRange = new CmsWebdavRange();
            currentRange.setLength(fileLength);

            int dashPos = rangeDefinition.indexOf('-');

            if (dashPos == -1) {
                response.addHeader(HEADER_CONTENTRANGE, "bytes */" + fileLength);
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            if (dashPos == 0) {

                try {
                    long offset = Long.parseLong(rangeDefinition);
                    currentRange.setStart(fileLength + offset);
                    currentRange.setEnd(fileLength - 1);
                } catch (NumberFormatException e) {
                    response.addHeader(HEADER_CONTENTRANGE, "bytes */" + fileLength);
                    response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            } else {

                try {
                    currentRange.setStart(Long.parseLong(rangeDefinition.substring(0, dashPos)));
                    if (dashPos < (rangeDefinition.length() - 1)) {
                        currentRange.setEnd(
                            Long.parseLong(rangeDefinition.substring(dashPos + 1, rangeDefinition.length())));
                    } else {
                        currentRange.setEnd(fileLength - 1);
                    }
                } catch (NumberFormatException e) {
                    response.addHeader(HEADER_CONTENTRANGE, "bytes */" + fileLength);
                    response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            }

            if (!currentRange.validate()) {
                response.addHeader(HEADER_CONTENTRANGE, "bytes */" + fileLength);
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            result.add(currentRange);
        }

        return result;
    }

    /**
     * Return an InputStream to an HTML representation of the contents
     * of this directory.<p>
     *
     * @param contextPath context path to which our internal paths are relative
     * @param path the path of the resource to render the html for
     *
     * @return an input stream with the rendered html
     *
     * @throws IOException if an input/output error occurs
     */
    protected InputStream renderHtml(String contextPath, String path) throws IOException {

        String name = path;
        // Prepare a writer to a buffered area
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter osWriter = null;
        try {
            osWriter = new OutputStreamWriter(stream, "UTF8");
        } catch (Exception e) {

            // Should never happen
            osWriter = new OutputStreamWriter(stream);
        }
        PrintWriter writer = new PrintWriter(osWriter);

        StringBuffer sb = new StringBuffer();

        // rewriteUrl(contextPath) is expensive. cache result for later reuse
        String rewrittenContextPath = rewriteUrl(contextPath);

        // Render the page header
        sb.append("<html>\r\n");
        sb.append("<head>\r\n");
        sb.append("<title>");
        sb.append(Messages.get().getBundle().key(Messages.GUI_DIRECTORY_TITLE_1, name));
        sb.append("</title>\r\n");

        // TODO: add opencms css style
        sb.append("<STYLE><!--");
        sb.append(
            "H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} "
                + "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} "
                + "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} "
                + "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} "
                + "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} "
                + "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}"
                + "A {color : black;}"
                + "A.name {color : black;}"
                + "HR {color : #525D76;}");
        sb.append("--></STYLE> ");

        sb.append("</head>\r\n");
        sb.append("<body>");
        sb.append("<h1>");
        sb.append(Messages.get().getBundle().key(Messages.GUI_DIRECTORY_TITLE_1, name));

        sb.append("</h1>");
        sb.append("<HR size=\"1\" noshade=\"noshade\">");

        sb.append("<table width=\"100%\" cellspacing=\"0\"" + " cellpadding=\"5\" align=\"center\">\r\n");

        // Render the column headings
        sb.append("<tr>\r\n");
        sb.append("<td align=\"left\"><font size=\"+1\"><strong>");
        sb.append(Messages.get().getBundle().key(Messages.GUI_DIRECTORY_FILENAME_0));
        sb.append("</strong></font></td>\r\n");
        sb.append("<td align=\"center\"><font size=\"+1\"><strong>");
        sb.append(Messages.get().getBundle().key(Messages.GUI_DIRECTORY_SIZE_0));
        sb.append("</strong></font></td>\r\n");
        sb.append("<td align=\"right\"><font size=\"+1\"><strong>");
        sb.append(Messages.get().getBundle().key(Messages.GUI_DIRECTORY_LASTMODIFIED_0));
        sb.append("</strong></font></td>\r\n");
        sb.append("</tr>");

        boolean shade = false;

        // Render the link to our parent (if required)
        String parentDirectory = name;
        if (parentDirectory.endsWith("/")) {
            parentDirectory = parentDirectory.substring(0, parentDirectory.length() - 1);
        }
        int slash = parentDirectory.lastIndexOf('/');
        if (slash >= 0) {

            String parent = parentDirectory.substring(0, slash);

            sb.append("<tr");
            if (shade) {
                sb.append(" bgcolor=\"#eeeeee\"");
            }
            sb.append(">\r\n");
            shade = !shade;

            sb.append("<td align=\"left\">&nbsp;&nbsp;\r\n");
            sb.append("<a href=\"");
            sb.append(rewrittenContextPath);
            if (parent.equals("")) {
                parent = "/";
            }
            sb.append(rewriteUrl(parent));
            if (!parent.endsWith("/")) {
                sb.append("/");
            }
            sb.append("\"><tt>");
            sb.append("..");
            sb.append("</tt></a></td>\r\n");

            sb.append("<td align=\"right\"><tt>");
            sb.append("&nbsp;");
            sb.append("</tt></td>\r\n");

            sb.append("<td align=\"right\"><tt>");
            sb.append("&nbsp;");
            sb.append("</tt></td>\r\n");

            sb.append("</tr>\r\n");
        }

        try {

            // Render the directory entries within this directory
            List<I_CmsRepositoryItem> list = m_session.list(path);
            Iterator<I_CmsRepositoryItem> iter = list.iterator();
            while (iter.hasNext()) {

                I_CmsRepositoryItem childItem = iter.next();

                String resourceName = childItem.getName();
                if (resourceName.endsWith("/")) {
                    resourceName = resourceName.substring(0, resourceName.length() - 1);
                }
                slash = resourceName.lastIndexOf('/');
                if (slash > -1) {
                    resourceName = resourceName.substring(slash + 1, resourceName.length());
                }

                sb.append("<tr");
                if (shade) {
                    sb.append(" bgcolor=\"#eeeeee\"");
                }
                sb.append(">\r\n");
                shade = !shade;

                sb.append("<td align=\"left\">&nbsp;&nbsp;\r\n");
                sb.append("<a href=\"");
                sb.append(rewrittenContextPath);
                //resourceName = rewriteUrl(name + resourceName);
                sb.append(rewriteUrl(name + resourceName));
                if (childItem.isCollection()) {
                    sb.append("/");
                }
                sb.append("\"><tt>");
                sb.append(CmsEncoder.escapeXml(resourceName));
                if (childItem.isCollection()) {
                    sb.append("/");
                }
                sb.append("</tt></a></td>\r\n");

                sb.append("<td align=\"right\"><tt>");
                if (childItem.isCollection()) {
                    sb.append("&nbsp;");
                } else {
                    sb.append(renderSize(childItem.getContentLength()));
                }
                sb.append("</tt></td>\r\n");

                sb.append("<td align=\"right\"><tt>");
                sb.append(HTTP_DATE_FORMAT.format(new Date(childItem.getLastModifiedDate())));
                sb.append("</tt></td>\r\n");

                sb.append("</tr>\r\n");
            }

        } catch (CmsException e) {

            // Something went wrong
            e.printStackTrace();
        }

        // Render the page footer
        sb.append("</table>\r\n");

        sb.append("<HR size=\"1\" noshade=\"noshade\">");
        sb.append("</body>\r\n");
        sb.append("</html>\r\n");

        // Return an input stream to the underlying bytes
        writer.write(sb.toString());
        writer.flush();
        return (new ByteArrayInputStream(stream.toByteArray()));
    }

    /**
     * Render the specified file size (in bytes).<p>
     *
     * @param size file size (in bytes)
     *
     * @return a string with the given size formatted to output to user
     */
    protected String renderSize(long size) {

        long leftSide = size / 1024;
        long rightSide = (size % 1024) / 103; // Makes 1 digit
        if ((leftSide == 0) && (rightSide == 0) && (size > 0)) {
            rightSide = 1;
        }

        return ("" + leftSide + "." + rightSide + " kb");
    }

    /**
     * URL rewriter.<p>
     *
     * @param path path which has to be rewritten
     *
     * @return a string with the encoded path
     *
     * @throws UnsupportedEncodingException if something goes wrong while encoding the url
     */
    protected String rewriteUrl(String path) throws UnsupportedEncodingException {

        return new String(URLCodec.encodeUrl(URL_SAFE_CHARS, path.getBytes("ISO-8859-1")));
    }

    /**
     * Serve the specified resource, optionally including the data content.<p>
     *
     * @param request the servlet request we are processing
     * @param response the servlet response we are creating
     * @param content should the content be included?
     *
     * @throws IOException if an input/output error occurs
     */
    protected void serveResource(HttpServletRequest request, HttpServletResponse response, boolean content)
    throws IOException {

        // Identify the requested resource path
        String path = getRelativePath(request);
        if (LOG.isDebugEnabled()) {
            if (content) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_SERVE_ITEM_1, path));
            } else {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_SERVE_ITEM_HEADER_1, path));
            }
        }

        I_CmsRepositoryItem item = null;
        try {
            item = m_session.getItem(path);
        } catch (CmsException ex) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_NOT_FOUND_1, path));
            }

            return;
        }

        // If the resource is not a collection, and the resource path
        // ends with "/" or "\", return NOT FOUND
        if (!item.isCollection()) {
            if (path.endsWith("/") || (path.endsWith("\\"))) {

                response.setStatus(HttpServletResponse.SC_NOT_FOUND);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_ITEM_NOT_FOUND_1, path));
                }

                return;
            }
        }

        // Find content type.
        String contentType = item.getMimeType();
        if (contentType == null) {
            contentType = getServletContext().getMimeType(item.getName());
        }

        ArrayList<CmsWebdavRange> ranges = null;
        long contentLength = -1L;

        if (item.isCollection()) {

            // Skip directory listings if we have been configured to suppress them
            if (!m_listings) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            contentType = "text/html;charset=UTF-8";

        } else {

            // Parse range specifier
            ranges = parseRange(request, response, item);

            // ETag header
            response.setHeader(HEADER_ETAG, getETag(item));

            // Last-Modified header
            response.setHeader(HEADER_LASTMODIFIED, HTTP_DATE_FORMAT.format(new Date(item.getLastModifiedDate())));

            // Get content length
            contentLength = item.getContentLength();

            // Special case for zero length files, which would cause a
            // (silent) ISE when setting the output buffer size
            if (contentLength == 0L) {
                content = false;
            }

        }

        ServletOutputStream ostream = null;
        PrintWriter writer = null;

        if (content) {

            // Trying to retrieve the servlet output stream
            try {
                ostream = response.getOutputStream();
            } catch (IllegalStateException e) {

                // If it fails, we try to get a Writer instead if we're
                // trying to serve a text file
                if ((contentType == null) || (contentType.startsWith("text")) || (contentType.endsWith("xml"))) {
                    writer = response.getWriter();
                } else {
                    throw e;
                }
            }

        }

        if ((item.isCollection())
            || (((ranges == null) || (ranges.isEmpty())) && (request.getHeader(HEADER_RANGE) == null))
            || (ranges == FULL_RANGE)) {

            // Set the appropriate output headers
            if (contentType != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_SERVE_ITEM_CONTENT_TYPE_1, contentType));
                }
                response.setContentType(contentType);
            }

            if ((!item.isCollection()) && (contentLength >= 0)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_SERVE_ITEM_CONTENT_LENGTH_1,
                            new Long(contentLength)));
                }

                if (contentLength < Integer.MAX_VALUE) {
                    response.setContentLength((int)contentLength);
                } else {

                    // Set the content-length as String to be able to use a long
                    response.setHeader(HEADER_CONTENTLENGTH, "" + contentLength);
                }
            }

            InputStream renderResult = null;
            if (item.isCollection()) {

                if (content) {
                    // Serve the directory browser
                    renderResult = renderHtml(request.getContextPath() + request.getServletPath(), item.getName());
                }

            }

            // Copy the input stream to our output stream (if requested)
            if (content) {
                try {
                    response.setBufferSize(m_output);
                } catch (IllegalStateException e) {
                    // Silent catch
                }
                if (ostream != null) {
                    copy(item, renderResult, ostream);
                } else {
                    copy(item, renderResult, writer);
                }
            }

        } else {

            if ((ranges == null) || (ranges.isEmpty())) {
                return;
            }

            // Partial content response.
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

            if (ranges.size() == 1) {

                CmsWebdavRange range = ranges.get(0);
                response.addHeader(
                    HEADER_CONTENTRANGE,
                    "bytes " + range.getStart() + "-" + range.getEnd() + "/" + range.getLength());
                long length = (range.getEnd() - range.getStart()) + 1;
                if (length < Integer.MAX_VALUE) {
                    response.setContentLength((int)length);
                } else {
                    // Set the content-length as String to be able to use a long
                    response.setHeader(HEADER_CONTENTLENGTH, "" + length);
                }

                if (contentType != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_SERVE_ITEM_CONTENT_TYPE_1, contentType));
                    }
                    response.setContentType(contentType);
                }

                if (content) {
                    try {
                        response.setBufferSize(m_output);
                    } catch (IllegalStateException e) {
                        // Silent catch
                    }
                    if (ostream != null) {
                        copy(item, ostream, range);
                    } else {
                        copy(item, writer, range);
                    }
                }

            } else {

                response.setContentType("multipart/byteranges; boundary=" + MIME_SEPARATION);

                if (content) {
                    try {
                        response.setBufferSize(m_output);
                    } catch (IllegalStateException e) {
                        // Silent catch
                    }
                    if (ostream != null) {
                        copy(item, ostream, ranges.iterator(), contentType);
                    } else {
                        copy(item, writer, ranges.iterator(), contentType);
                    }
                }

            }

        }
    }

    /**
     * Handles the special WebDAV methods.<p>
     *
     * @param req the servlet request we are processing
     * @param resp the servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String method = req.getMethod();

        if (LOG.isDebugEnabled()) {
            String path = getRelativePath(req);
            LOG.debug("[" + method + "] " + path);
        }

        // check authorization
        String auth = req.getHeader(HEADER_AUTHORIZATION);
        if ((auth == null) || !auth.toUpperCase().startsWith(AUTHORIZATION_BASIC_PREFIX)) {

            // no authorization data is available
            requestAuthorization(resp);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_NO_AUTHORIZATION_0));
            }

            return;
        }

        // get encoded user and password, following after "BASIC "
        String base64Token = auth.substring(6);

        // decode it, using base 64 decoder
        String token = new String(Base64.decodeBase64(base64Token.getBytes()));
        String password = null;
        int pos = token.indexOf(SEPARATOR_CREDENTIALS);
        if (pos != -1) {
            m_username = token.substring(0, pos);
            password = token.substring(pos + 1);
        }

        // get session
        try {
            m_session = m_repository.login(m_username, password);
        } catch (CmsException ex) {
            m_session = null;
        }

        if (m_session == null) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_LOGIN_FAILED_1, m_username));
            }

            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (method.equals(METHOD_PROPFIND)) {
            doPropfind(req, resp);
        } else if (method.equals(METHOD_PROPPATCH)) {
            doProppatch(req, resp);
        } else if (method.equals(METHOD_MKCOL)) {
            doMkcol(req, resp);
        } else if (method.equals(METHOD_COPY)) {
            doCopy(req, resp);
        } else if (method.equals(METHOD_MOVE)) {
            doMove(req, resp);
        } else if (method.equals(METHOD_LOCK)) {
            doLock(req, resp);
        } else if (method.equals(METHOD_UNLOCK)) {
            doUnlock(req, resp);
        } else {

            // DefaultServlet processing
            super.service(req, resp);
        }

    }

    /**
     * Generate a dom element from the given information with all needed subelements to
     * add to the parent.<p>
     *
     * @param lock the lock with the information to create the subelements
     * @param parent the parent element where to add the created element
     * @param lockToken the lock token to use
     */
    private void addLockElement(CmsRepositoryLockInfo lock, Element parent, String lockToken) {

        Element activeLockElem = addElement(parent, TAG_ACTIVELOCK);
        addElement(addElement(activeLockElem, TAG_LOCKTYPE), lock.getType());
        addElement(addElement(activeLockElem, TAG_LOCKSCOPE), lock.getScope());

        if (lock.getDepth() == CmsRepositoryLockInfo.DEPTH_INFINITY_VALUE) {
            addElement(activeLockElem, TAG_DEPTH).addText(DEPTH_INFINITY);
        } else {
            addElement(activeLockElem, TAG_DEPTH).addText("0");
        }

        Element ownerElem = addElement(activeLockElem, TAG_OWNER);
        addElement(ownerElem, TAG_HREF).addText(lock.getOwner());

        if (lock.getExpiresAt() == CmsRepositoryLockInfo.TIMEOUT_INFINITE_VALUE) {
            addElement(activeLockElem, TAG_TIMEOUT).addText(TIMEOUT_INFINITE);
        } else {
            long timeout = (lock.getExpiresAt() - System.currentTimeMillis()) / 1000;
            addElement(activeLockElem, TAG_TIMEOUT).addText("Second-" + timeout);
        }

        Element lockTokenElem = addElement(activeLockElem, TAG_LOCKTOKEN);
        addElement(lockTokenElem, TAG_HREF).addText("opaquelocktoken:" + lockToken);
    }

    /**
     * Checks if the items in the path or in a subpath are locked.<p>
     *
     * @param req the servlet request we are processing
     * @param path the path to check the items for locks
     * @param errorList the error list where to put the found errors
     */
    private void checkChildLocks(HttpServletRequest req, String path, Hashtable<String, Integer> errorList) {

        List<I_CmsRepositoryItem> list = null;
        try {
            list = m_session.list(path);
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_LIST_ITEMS_ERROR_1, path), e);
            }
            errorList.put(path, new Integer(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR));
            return;
        }

        Iterator<I_CmsRepositoryItem> iter = list.iterator();
        while (iter.hasNext()) {
            I_CmsRepositoryItem element = iter.next();

            if (isLocked(element.getName())) {
                errorList.put(element.getName(), new Integer(CmsWebdavStatus.SC_LOCKED));
            } else {
                if (element.isCollection()) {
                    checkChildLocks(req, element.getName(), errorList);
                }
            }
        }
    }

    /**
     * Determines the methods normally allowed for the resource.<p>
     *
     * @param path the path to the resource
     *
     * @return a StringBuffer with the WebDAV methods allowed for the resource at the given path
     */
    private StringBuffer determineMethodsAllowed(String path) {

        StringBuffer methodsAllowed = new StringBuffer();
        boolean exists = true;
        I_CmsRepositoryItem item = null;
        try {
            item = m_session.getItem(path);
        } catch (CmsException e) {
            exists = false;
        }

        if (!exists) {

            methodsAllowed.append(METHOD_OPTIONS);
            methodsAllowed.append(", ").append(METHOD_PUT);
            methodsAllowed.append(", ").append(METHOD_MKCOL);
            methodsAllowed.append(", ").append(METHOD_LOCK);
            return methodsAllowed;
        }

        // add standard http methods
        methodsAllowed.append(METHOD_OPTIONS);
        methodsAllowed.append(", ").append(METHOD_GET);
        methodsAllowed.append(", ").append(METHOD_HEAD);
        methodsAllowed.append(", ").append(METHOD_POST);
        methodsAllowed.append(", ").append(METHOD_DELETE);
        methodsAllowed.append(", ").append(METHOD_TRACE);

        // add special WebDAV methods
        methodsAllowed.append(", ").append(METHOD_LOCK);
        methodsAllowed.append(", ").append(METHOD_UNLOCK);
        methodsAllowed.append(", ").append(METHOD_MOVE);
        methodsAllowed.append(", ").append(METHOD_COPY);
        methodsAllowed.append(", ").append(METHOD_PROPPATCH);

        if (m_listings) {
            methodsAllowed.append(", ").append(METHOD_PROPFIND);
        }

        if (item != null) {
            if (!item.isCollection()) {
                methodsAllowed.append(", ").append(METHOD_PUT);
            }
        }
        return methodsAllowed;
    }

    /**
     * Print the lock discovery information associated with a path.<p>
     *
     * @param path the path to the resource
     * @param elem the dom element where to add the lock discovery elements
     * @param req the servlet request we are processing
     *
     * @return true if at least one lock was displayed
     */
    private boolean generateLockDiscovery(String path, Element elem, HttpServletRequest req) {

        CmsRepositoryLockInfo lock = m_session.getLock(path);

        if (lock != null) {

            Element lockElem = addElement(elem, TAG_LOCKDISCOVERY);
            addLockElement(lock, lockElem, generateLockToken(req, lock));

            return true;
        }

        return false;
    }

    /**
     * Generates a lock token out of the lock and some information out of the
     * request to make it unique.<p>
     *
     * @param req the servlet request we are processing
     * @param lock the lock with the information for the lock token
     *
     * @return the generated lock token
     */
    private String generateLockToken(HttpServletRequest req, CmsRepositoryLockInfo lock) {

        String lockTokenStr = req.getServletPath()
            + "-"
            + req.getUserPrincipal()
            + "-"
            + lock.getOwner()
            + "-"
            + lock.getPath()
            + "-"
            + m_secret;

        return MD5_ENCODER.encode(m_md5Helper.digest(lockTokenStr.getBytes()));
    }

    /**
     * Return the relative path associated with this servlet.<p>
     *
     * @param request the servlet request we are processing
     *
     * @return the relative path of the resource
     */
    private String getRelativePath(HttpServletRequest request) {

        String result = request.getPathInfo();
        if (result == null) {
            //result = request.getServletPath();
        }
        if ((result == null) || (result.equals(""))) {
            result = "/";
        }
        return (result);
    }

    /**
     * Check to see if a resource is currently write locked.<p>
     *
     * @param req the servlet request we are processing
     *
     * @return true if the resource is locked otherwise false
     */
    private boolean isLocked(HttpServletRequest req) {

        return isLocked(getRelativePath(req));
    }

    /**
     * Check to see if a resource is currently write locked.<p>
     *
     * @param path the path where to find the resource to check the lock
     *
     * @return true if the resource is locked otherwise false
     */
    private boolean isLocked(String path) {

        // get lock for path
        CmsRepositoryLockInfo lock = m_session.getLock(path);
        if (lock == null) {
            return false;
        }

        // check if found lock fits to the lock token from request
        //        String currentToken = "<opaquelocktoken:" + generateLockToken(req, lock) + ">";
        //        if (currentToken.equals(parseLockTokenHeader(req))) {
        //            return false;
        //        }

        if (lock.getUsername().equals(m_username)) {
            return false;
        }

        return true;
    }

    /**
     * Return a context-relative path, beginning with a "/".<p>
     *
     * That represents the canonical version of the specified path after ".."
     * and "." elements are resolved out. If the specified path attempts to go
     * outside the boundaries of the current context (i.e. too many ".." path
     * elements are present), return <code>null</code> instead.<p>
     *
     * @param path the path to be normalized
     *
     * @return the normalized path
     */
    private String normalize(String path) {

        if (path == null) {
            return null;
        }

        // Create a place for the normalized path
        String normalized = path;

        if (normalized.equals("/.")) {
            return "/";
        }

        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0) {
            normalized = normalized.replace('\\', '/');
        }

        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0) {
                break;
            }
            normalized = normalized.substring(0, index) + normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0) {
                break;
            }
            normalized = normalized.substring(0, index) + normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0) {
                break;
            }
            if (index == 0) {
                return (null); // Trying to go outside our context
            }

            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return (normalized);
    }

    /**
     * Parse the content-range header.<p>
     *
     * @param request the servlet request we are processing
     * @param response the servlet response we are creating
     *
     * @return the range of the content read from the header
     */
    private CmsWebdavRange parseContentRange(HttpServletRequest request, HttpServletResponse response) {

        // Retrieving the content-range header (if any is specified
        String rangeHeader = request.getHeader(HEADER_CONTENTRANGE);

        if (rangeHeader == null) {
            return null;
        }

        // bytes is the only range unit supported
        if (!rangeHeader.startsWith("bytes")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        rangeHeader = rangeHeader.substring(6).trim();

        int dashPos = rangeHeader.indexOf('-');
        int slashPos = rangeHeader.indexOf('/');

        if (dashPos == -1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if (slashPos == -1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        CmsWebdavRange range = new CmsWebdavRange();

        try {
            range.setStart(Long.parseLong(rangeHeader.substring(0, dashPos)));
            range.setEnd(Long.parseLong(rangeHeader.substring(dashPos + 1, slashPos)));
            range.setLength(Long.parseLong(rangeHeader.substring(slashPos + 1, rangeHeader.length())));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if (!range.validate()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        return range;
    }

    /**
     * Reads the information about a destination path out of the header of the
     * request.<p>
     *
     * @param req the servlet request we are processing
     *
     * @return the destination path
     */
    private String parseDestinationHeader(HttpServletRequest req) {

        // Parsing destination header
        String destinationPath = req.getHeader(HEADER_DESTINATION);

        if (destinationPath == null) {
            return null;
        }

        // Remove url encoding from destination
        destinationPath = CmsEncoder.decode(destinationPath, "UTF8");

        int protocolIndex = destinationPath.indexOf("://");
        if (protocolIndex >= 0) {

            // if the Destination URL contains the protocol, we can safely
            // trim everything upto the first "/" character after "://"
            int firstSeparator = destinationPath.indexOf("/", protocolIndex + 4);
            if (firstSeparator < 0) {
                destinationPath = "/";
            } else {
                destinationPath = destinationPath.substring(firstSeparator);
            }
        } else {
            String hostName = req.getServerName();
            if ((hostName != null) && (destinationPath.startsWith(hostName))) {
                destinationPath = destinationPath.substring(hostName.length());
            }

            int portIndex = destinationPath.indexOf(":");
            if (portIndex >= 0) {
                destinationPath = destinationPath.substring(portIndex);
            }

            if (destinationPath.startsWith(":")) {
                int firstSeparator = destinationPath.indexOf("/");
                if (firstSeparator < 0) {
                    destinationPath = "/";
                } else {
                    destinationPath = destinationPath.substring(firstSeparator);
                }
            }
        }

        // Normalise destination path (remove '.' and '..')
        destinationPath = normalize(destinationPath);

        String contextPath = req.getContextPath();
        if ((contextPath != null) && (destinationPath.startsWith(contextPath))) {
            destinationPath = destinationPath.substring(contextPath.length());
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            String servletPath = req.getServletPath();
            if ((servletPath != null) && (destinationPath.startsWith(servletPath))) {
                destinationPath = destinationPath.substring(servletPath.length());
            }
        }

        return destinationPath;
    }

    /**
     * Reads the information about overwriting out of the header of the
     * request.<p>
     *
     * @param req the servlet request we are processing
     *
     * @return true if overwrite was set in the header otherwise false
     */
    private boolean parseOverwriteHeader(HttpServletRequest req) {

        boolean overwrite = true;
        String overwriteHeader = req.getHeader(HEADER_OVERWRITE);

        if (overwriteHeader != null) {
            if (overwriteHeader.equalsIgnoreCase("T")) {
                overwrite = true;
            } else {
                overwrite = false;
            }
        }

        return overwrite;
    }

    /**
     * Propfind helper method.<p>
     *
     * @param req the servlet request
     * @param elem the parent element where to add the generated subelements
     * @param item the current item where to parse the properties
     * @param type the propfind type
     * @param propertiesVector if the propfind type is find properties by
     *          name, then this Vector contains those properties
     */
    private void parseProperties(
        HttpServletRequest req,
        Element elem,
        I_CmsRepositoryItem item,
        int type,
        List<String> propertiesVector) {

        String path = item.getName();
        Element responseElem = addElement(elem, TAG_RESPONSE);

        String status = "HTTP/1.1 "
            + CmsWebdavStatus.SC_OK
            + " "
            + CmsWebdavStatus.getStatusText(CmsWebdavStatus.SC_OK);

        // Generating href element
        Element hrefElem = addElement(responseElem, TAG_HREF);

        String href = req.getContextPath() + req.getServletPath();
        if ((href.endsWith("/")) && (path.startsWith("/"))) {
            href += path.substring(1);
        } else {
            href += path;
        }

        try {
            hrefElem.addText(rewriteUrl(href));
        } catch (UnsupportedEncodingException ex) {
            return;
        }

        String resourceName = path;

        Element propstatElem = addElement(responseElem, TAG_PROPSTAT);
        Element propElem = addElement(propstatElem, TAG_PROP);

        switch (type) {

            case FIND_ALL_PROP:

                addElement(propElem, TAG_CREATIONDATE).addText(ISO8601_FORMAT.format(new Date(item.getCreationDate())));
                addElement(propElem, TAG_DISPLAYNAME).addCDATA(resourceName);

                // properties only for files (no collections)
                if (!item.isCollection()) {

                    addElement(propElem, TAG_LASTMODIFIED).addText(
                        HTTP_DATE_FORMAT.format(new Date(item.getLastModifiedDate())));

                    addElement(propElem, TAG_CONTENTLENGTH).addText(String.valueOf(item.getContentLength()));

                    String contentType = getServletContext().getMimeType(item.getName());
                    if (contentType != null) {
                        addElement(propElem, TAG_CONTENTTYPE).addText(contentType);
                    }
                    addElement(propElem, TAG_ETAG).addText(getETag(item));
                    addElement(propElem, TAG_RESOURCETYPE);
                } else {
                    addElement(addElement(propElem, TAG_RESOURCETYPE), TAG_COLLECTION);
                }

                addElement(propElem, TAG_SOURCE).addText("");

                Element suppLockElem = addElement(propElem, TAG_SUPPORTEDLOCK);
                Element lockEntryElem = addElement(suppLockElem, TAG_LOCKENTRY);
                addElement(addElement(lockEntryElem, TAG_LOCKSCOPE), CmsRepositoryLockInfo.SCOPE_EXCLUSIVE);
                addElement(addElement(lockEntryElem, TAG_LOCKTYPE), CmsRepositoryLockInfo.TYPE_WRITE);
                lockEntryElem = addElement(suppLockElem, TAG_LOCKENTRY);
                addElement(addElement(lockEntryElem, TAG_LOCKSCOPE), CmsRepositoryLockInfo.SCOPE_SHARED);
                addElement(addElement(lockEntryElem, TAG_LOCKTYPE), CmsRepositoryLockInfo.TYPE_WRITE);

                generateLockDiscovery(path, propElem, req);

                addElement(propstatElem, TAG_STATUS).addText(status);

                break;

            case FIND_PROPERTY_NAMES:

                addElement(propElem, TAG_CREATIONDATE);
                addElement(propElem, TAG_DISPLAYNAME);
                if (!item.isCollection()) {

                    addElement(propElem, TAG_CONTENTLANGUAGE);
                    addElement(propElem, TAG_CONTENTLENGTH);
                    addElement(propElem, TAG_CONTENTTYPE);
                    addElement(propElem, TAG_ETAG);
                }
                addElement(propElem, TAG_LASTMODIFIED);
                addElement(propElem, TAG_RESOURCETYPE);
                addElement(propElem, TAG_SOURCE);
                addElement(propElem, TAG_LOCKDISCOVERY);

                addElement(propstatElem, TAG_STATUS).addText(status);

                break;

            case FIND_BY_PROPERTY:

                List<String> propertiesNotFound = new Vector<String>();

                // Parse the list of properties
                Iterator<String> iter = propertiesVector.iterator();
                while (iter.hasNext()) {
                    String property = iter.next();

                    if (property.equals(TAG_CREATIONDATE)) {
                        addElement(propElem, TAG_CREATIONDATE).addText(
                            ISO8601_FORMAT.format(new Date(item.getCreationDate())));
                    } else if (property.equals(TAG_DISPLAYNAME)) {
                        addElement(propElem, TAG_DISPLAYNAME).addCDATA(resourceName);
                    } else if (property.equals(TAG_CONTENTLANGUAGE)) {
                        if (item.isCollection()) {
                            propertiesNotFound.add(property);
                        } else {
                            addElement(propElem, TAG_CONTENTLANGUAGE);
                        }
                    } else if (property.equals(TAG_CONTENTLENGTH)) {
                        if (item.isCollection()) {
                            propertiesNotFound.add(property);
                        } else {
                            addElement(propElem, TAG_CONTENTLENGTH).addText((String.valueOf(item.getContentLength())));
                        }
                    } else if (property.equals(TAG_CONTENTTYPE)) {
                        if (item.isCollection()) {
                            propertiesNotFound.add(property);
                        } else {
                            String contentType = item.getMimeType();
                            if (contentType == null) {
                                contentType = getServletContext().getMimeType(item.getName());
                            }

                            if (contentType != null) {
                                addElement(propElem, TAG_CONTENTTYPE).addText(contentType);
                            }
                        }
                    } else if (property.equals(TAG_ETAG)) {
                        if (item.isCollection()) {
                            propertiesNotFound.add(property);
                        } else {
                            addElement(propElem, TAG_ETAG).addText(getETag(item));
                        }
                    } else if (property.equals(TAG_LASTMODIFIED)) {
                        addElement(propElem, TAG_LASTMODIFIED).addText(
                            HTTP_DATE_FORMAT.format(new Date(item.getLastModifiedDate())));
                    } else if (property.equals(TAG_RESOURCETYPE)) {
                        if (item.isCollection()) {
                            addElement(addElement(propElem, TAG_RESOURCETYPE), TAG_COLLECTION);
                        } else {
                            addElement(propElem, TAG_RESOURCETYPE);
                        }
                    } else if (property.equals(TAG_SOURCE)) {
                        addElement(propElem, TAG_SOURCE).addText("");
                    } else if (property.equals(TAG_SUPPORTEDLOCK)) {
                        suppLockElem = addElement(propElem, TAG_SUPPORTEDLOCK);
                        lockEntryElem = addElement(suppLockElem, TAG_LOCKENTRY);
                        addElement(addElement(lockEntryElem, TAG_LOCKSCOPE), CmsRepositoryLockInfo.SCOPE_EXCLUSIVE);
                        addElement(addElement(lockEntryElem, TAG_LOCKTYPE), CmsRepositoryLockInfo.TYPE_WRITE);
                        lockEntryElem = addElement(suppLockElem, TAG_LOCKENTRY);
                        addElement(addElement(lockEntryElem, TAG_LOCKSCOPE), CmsRepositoryLockInfo.SCOPE_SHARED);
                        addElement(addElement(lockEntryElem, TAG_LOCKTYPE), CmsRepositoryLockInfo.TYPE_WRITE);
                    } else if (property.equals(TAG_LOCKDISCOVERY)) {
                        if (!generateLockDiscovery(path, propElem, req)) {
                            addElement(propElem, TAG_LOCKDISCOVERY);
                        }
                    } else {
                        propertiesNotFound.add(property);
                    }
                }

                addElement(propstatElem, TAG_STATUS).addText(status);

                if (propertiesNotFound.size() > 0) {
                    status = "HTTP/1.1 "
                        + CmsWebdavStatus.SC_NOT_FOUND
                        + " "
                        + CmsWebdavStatus.getStatusText(CmsWebdavStatus.SC_NOT_FOUND);

                    propstatElem = addElement(responseElem, TAG_PROPSTAT);
                    propElem = addElement(propstatElem, TAG_PROP);

                    Iterator<String> notFoundIter = propertiesNotFound.iterator();
                    while (notFoundIter.hasNext()) {
                        addElement(propElem, notFoundIter.next());
                    }

                    addElement(propstatElem, TAG_STATUS).addText(status);
                }

                break;

            default:

                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_PROPFIND_TYPE_0));
                }
                break;
        }
    }

    /**
     * Sends a response back to authenticate the user.<p>
     *
     * @param resp the servlet response we are processing
     *
     * @throws IOException if errors while writing to response occurs
     */
    private void requestAuthorization(HttpServletResponse resp) throws IOException {

        // Authorisation is required for the requested action.
        resp.setHeader(CmsRequestUtil.HEADER_WWW_AUTHENTICATE, "Basic realm=\"" + BASIC_REALM + "\"");

        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * Send a multistatus element containing a complete error report to the
     * client.<p>
     *
     * @param req the servlet request we are processing
     * @param resp the servlet response we are processing
     * @param errors the errors to be displayed
     *
     * @throws IOException if errors while writing to response occurs
     */
    private void sendReport(HttpServletRequest req, HttpServletResponse resp, Map<String, Integer> errors)
    throws IOException {

        resp.setStatus(CmsWebdavStatus.SC_MULTI_STATUS);

        String absoluteUri = req.getRequestURI();
        String relativePath = getRelativePath(req);

        Document doc = DocumentHelper.createDocument();
        Element multiStatusElem = doc.addElement(new QName(TAG_MULTISTATUS, Namespace.get(DEFAULT_NAMESPACE)));

        Iterator<Entry<String, Integer>> it = errors.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Integer> e = it.next();
            String errorPath = e.getKey();
            int errorCode = e.getValue().intValue();

            Element responseElem = addElement(multiStatusElem, TAG_RESPONSE);

            String toAppend = errorPath.substring(relativePath.length());
            if (!toAppend.startsWith("/")) {
                toAppend = "/" + toAppend;
            }
            addElement(responseElem, TAG_HREF).addText(absoluteUri + toAppend);
            addElement(responseElem, TAG_STATUS).addText(
                "HTTP/1.1 " + errorCode + " " + CmsWebdavStatus.getStatusText(errorCode));
        }

        Writer writer = resp.getWriter();
        doc.write(writer);
        writer.close();
    }
}