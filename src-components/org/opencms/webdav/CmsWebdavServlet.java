/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/webdav/Attic/CmsWebdavServlet.java,v $
 * Date   : $Date: 2007/01/12 17:24:42 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.webdav;

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.webdav.tomcat.WebDavHandler;
import org.opencms.webdav.util.MD5Encoder;

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
 * Servlet which adds support for WebDAV level 2. All the basic HTTP requests
 * are handled by the DefaultServlet.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.1.2.1 $ $Date: 2007/01/12 17:24:42 $
 */
public class CmsWebdavServlet extends HttpServlet {

    /** Size of file transfer buffer in bytes. */
    public static final int BUFFER_SIZE = 4096;

    /** Default depth is infinite. */
    public static final int INFINITY = 3; // To limit tree browsing a bit

    /** Date format for the last modified date. */
    protected static final DateFormat HTTP_DATE_FORMAT;

    /** Date format for the creation date. */
    protected static final DateFormat ISO8601_FORMAT;

    /** WebDAV method: COPY. */
    protected static final String METHOD_COPY = "COPY";

    /** WebDAV method: LOCK. */
    protected static final String METHOD_LOCK = "LOCK";

    /** WebDAV method: MKCOL. */
    protected static final String METHOD_MKCOL = "MKCOL";

    /** WebDAV method: MOVE. */
    protected static final String METHOD_MOVE = "MOVE";

    /** WebDAV method: PROPFIND. */
    protected static final String METHOD_PROPFIND = "PROPFIND";

    /** WebDAV method: PROPPATCH. */
    protected static final String METHOD_PROPPATCH = "PROPPATCH";

    /** WebDAV method: UNLOCK. */
    protected static final String METHOD_UNLOCK = "UNLOCK";

    /** MIME multipart separation string. */
    protected static final String MIME_SEPARATION = "CATALINA_MIME_BOUNDARY";

    /** Chars which are safe for urls. */
    protected static final BitSet URL_SAFE_CHARS;

    /** Default namespace. */
    private static final String DEFAULT_NAMESPACE = "DAV:";

    /** Default lock timeout value. */
    private static final int DEFAULT_TIMEOUT = 3600;

    /** PROPFIND - Display all properties. */
    private static final int FIND_ALL_PROP = 1;

    /** PROPFIND - Specify a property mask. */
    private static final int FIND_BY_PROPERTY = 0;

    /** PROPFIND - Return property names. */
    private static final int FIND_PROPERTY_NAMES = 2;

    /** Full range marker. */
    private static ArrayList FULL_RANGE = new ArrayList();

    /** Create a new lock. */
    private static final int LOCK_CREATION = 0;

    /** Refresh lock. */
    private static final int LOCK_REFRESH = 1;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsWebdavServlet.class);

    /** Keeps the handler for the webdav requests. Temporary solution. */
    private static WebDavHandler m_handler = null;

    /** The MD5 helper object for this class. */
    protected static final MD5Encoder MD5_ENCODER = new MD5Encoder();

    /** Maximum lock timeout. */
    private static final int MAX_TIMEOUT = 604800;

    private static final long serialVersionUID = -122598983283724306L;

    /** MD5 message digest provider. */
    protected static MessageDigest m_md5Helper;

    /** The input buffer size to use when serving resources. */
    protected int m_input = 2048;

    /** The output buffer size to use when serving resources. */
    protected int m_output = 2048;

    /** Should we generate directory listings? */
    private boolean m_listings = false;

    /** Read only flag. By default, it's set to true. */
    private boolean m_readOnly = true;

    /** Secret information used to generate reasonably secure lock ids. */
    private String m_secret = "catalina";

    /** The session wich handles the action made with WebDAV. */
    private I_CmsWebdavSession m_session;

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
     * Initialize this servlet.
     * 
     * @throws ServletException if something went wrong
     */
    public void init() throws ServletException {

        String value = null;
        try {
            value = getServletConfig().getInitParameter("listings");
            if (value != null) {
                m_listings = (new Boolean(value)).booleanValue();
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("DefaultServlet.init: couldn't read listings from " + value);
            }
        }

        try {
            value = getServletConfig().getInitParameter("readonly");
            if (value != null) {
                m_readOnly = (new Boolean(value)).booleanValue();
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("DefaultServlet.init: couldn't read readonly from " + value);
            }
        }

        // Load the MD5 helper used to calculate signatures.
        try {
            m_md5Helper = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new UnavailableException("No MD5");
        }

        m_handler = new WebDavHandler();
        m_handler.init(getServletContext());
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param item The WebdavItem
     * @param is The input stream to copy from
     * @param writer The writer to write to
     *
     * @exception IOException if an input/output error occurs
     */
    protected void copy(I_CmsWebdavItem item, InputStream is, PrintWriter writer) throws IOException {

        IOException exception = null;

        InputStream resourceInputStream = null;
        if (!item.isCollection()) {
            resourceInputStream = item.getStreamContent();
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
            log("DefaultServlet.copy: exception closing reader: " + e.getMessage());
        }

        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param item The WebdavItem
     * @param is The input stream to copy from
     * @param ostream The output stream to write to
     *
     * @exception IOException if an input/output error occurs
     */
    protected void copy(I_CmsWebdavItem item, InputStream is, ServletOutputStream ostream) throws IOException {

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
            resourceInputStream = item.getStreamContent();
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
            log("DefaultServlet.copy: exception closing input stream: " + e.getMessage());
        }

        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param item The WebdavItem
     * @param writer The writer to write to
     * @param ranges Enumeration of the ranges the client wanted to retrieve
     * @param contentType Content type of the resource
     * @exception IOException if an input/output error occurs
     */
    protected void copy(I_CmsWebdavItem item, PrintWriter writer, Iterator ranges, String contentType)
    throws IOException {

        IOException exception = null;

        while ((exception == null) && (ranges.hasNext())) {

            InputStream resourceInputStream = item.getStreamContent();

            Reader reader = new InputStreamReader(resourceInputStream);
            CmsWebdavRange currentRange = (CmsWebdavRange)ranges.next();

            // Writing MIME header.
            writer.println();
            writer.println("--" + MIME_SEPARATION);
            if (contentType != null) {
                writer.println("Content-Type: " + contentType);
            }
            writer.println("Content-Range: bytes "
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
                log("DefaultServlet.copy: exception closing reader: " + e.getMessage());
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
     * (even in the face of an exception).
     *
     * @param item The WebdavItem
     * @param writer The writer to write to
     * @param range Range the client wanted to retrieve
     * @exception IOException if an input/output error occurs
     */
    protected void copy(I_CmsWebdavItem item, PrintWriter writer, CmsWebdavRange range) throws IOException {

        IOException exception = null;

        InputStream resourceInputStream = item.getStreamContent();

        Reader reader = new InputStreamReader(resourceInputStream);
        exception = copyRange(reader, writer, range.getStart(), range.getEnd());

        // Clean up the input stream
        try {
            reader.close();
        } catch (Exception e) {
            log("DefaultServlet.copy: exception closing reader: " + e.getMessage());
        }

        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param item The WebdavItem
     * @param ostream The output stream to write to
     * @param ranges Enumeration of the ranges the client wanted to retrieve
     * @param contentType Content type of the resource
     * @throws IOException if an input/output error occurs
     */
    protected void copy(I_CmsWebdavItem item, ServletOutputStream ostream, Iterator ranges, String contentType)
    throws IOException {

        IOException exception = null;

        while ((exception == null) && (ranges.hasNext())) {

            InputStream resourceInputStream = item.getStreamContent();
            InputStream istream = new BufferedInputStream(resourceInputStream, m_input);

            CmsWebdavRange currentRange = (CmsWebdavRange)ranges.next();

            // Writing MIME header.
            ostream.println();
            ostream.println("--" + MIME_SEPARATION);
            if (contentType != null) {
                ostream.println("Content-Type: " + contentType);
            }
            ostream.println("Content-Range: bytes "
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
                log("DefaultServlet.copy: exception closing input stream: " + e.getMessage());
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
     * (even in the face of an exception).
     *
     * @param item The WebdavItem
     * @param ostream The output stream to write to
     * @param range Range the client wanted to retrieve
     * @throws IOException if an input/output error occurs
     */
    protected void copy(I_CmsWebdavItem item, ServletOutputStream ostream, CmsWebdavRange range) throws IOException {

        IOException exception = null;

        InputStream resourceInputStream = item.getStreamContent();
        InputStream istream = new BufferedInputStream(resourceInputStream, m_input);
        exception = copyRange(istream, ostream, range.getStart(), range.getEnd());

        // Clean up the input stream
        try {
            istream.close();
        } catch (Exception e) {
            log("DefaultServlet.copy: exception closing input stream: " + e.getMessage());
        }

        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @return Exception which occurred during processing
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
     * (even in the face of an exception).
     *
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing
     */
    protected IOException copyRange(InputStream istream, ServletOutputStream ostream, long start, long end) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Serving bytes:" + start + "-" + end);
        }

        try {
            istream.skip(start);
        } catch (IOException e) {
            return e;
        }

        IOException exception = null;
        long bytesToRead = end - start + 1;

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
     * (even in the face of an exception).
     *
     * @param reader The reader to read from
     * @param writer The writer to write to
     * @return Exception which occurred during processing
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
     * (even in the face of an exception).
     *
     * @param reader The reader to read from
     * @param writer The writer to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing
     */
    protected IOException copyRange(Reader reader, PrintWriter writer, long start, long end) {

        try {
            reader.skip(start);
        } catch (IOException e) {
            return e;
        }

        IOException exception = null;
        long bytesToRead = end - start + 1;

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
     * Process a COPY WebDAV request for the specified resource.
     * 
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     * 
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void doCopy(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (m_readOnly) {
            resp.sendError(CmsWebdavStatus.SC_FORBIDDEN);
            return;
        }

        copyResource(req, resp);
    }

    /**
     * Process a DELETE WebDAV request for the specified resource.
     * 
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     * 
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (m_readOnly) {
            resp.sendError(CmsWebdavStatus.SC_FORBIDDEN);
            return;
        }

        if (isLocked(req)) {
            resp.sendError(CmsWebdavStatus.SC_LOCKED);
            return;
        }

        deleteResource(getRelativePath(req), req, resp, true);
    }

    /**
     * Process a GET request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // Serve the requested resource, including the data content
        serveResource(request, response, true);
    }

    /**
     * Process a HEAD request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {

        // Serve the requested resource, without the data content
        serveResource(request, response, false);
    }

    /**
     * Process a LOCK WebDAV request for the specified resource.
     * 
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void doLock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (m_readOnly) {
            resp.sendError(CmsWebdavStatus.SC_FORBIDDEN);
            return;
        }

        if (isLocked(req)) {
            resp.sendError(CmsWebdavStatus.SC_LOCKED);
            return;
        }

        CmsWebdavLockInfo lock = new CmsWebdavLockInfo();

        // Parsing depth header
        String depthStr = req.getHeader("Depth");
        if (depthStr == null) {
            lock.setDepth(INFINITY);
        } else {
            if (depthStr.equals("0")) {
                lock.setDepth(0);
            } else {
                lock.setDepth(INFINITY);
            }
        }

        // Parsing timeout header
        int lockDuration = DEFAULT_TIMEOUT;
        String lockDurationStr = req.getHeader("Timeout");
        if (lockDurationStr == null) {
            lockDuration = DEFAULT_TIMEOUT;
        } else {
            int commaPos = lockDurationStr.indexOf(",");

            // If multiple timeouts, just use the first
            if (commaPos != -1) {
                lockDurationStr = lockDurationStr.substring(0, commaPos);
            }
            if (lockDurationStr.startsWith("Second-")) {
                lockDuration = (new Integer(lockDurationStr.substring(7))).intValue();
            } else {
                if (lockDurationStr.equalsIgnoreCase("infinity")) {
                    lockDuration = MAX_TIMEOUT;
                } else {
                    try {
                        lockDuration = (new Integer(lockDurationStr)).intValue();
                    } catch (NumberFormatException e) {
                        lockDuration = MAX_TIMEOUT;
                    }
                }
            }
            if (lockDuration == 0) {
                lockDuration = DEFAULT_TIMEOUT;
            }
            if (lockDuration > MAX_TIMEOUT) {
                lockDuration = MAX_TIMEOUT;
            }
        }
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

            Iterator iter = lockInfoNode.elementIterator();

            Element lockScopeNode = null;
            Element lockTypeNode = null;
            Element lockOwnerNode = null;

            while (iter.hasNext()) {
                Element currentElem = (Element)iter.next();
                switch (currentElem.getNodeType()) {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        String nodeName = currentElem.getName();
                        if (nodeName.endsWith("lockscope")) {
                            lockScopeNode = currentElem;
                        }
                        if (nodeName.endsWith("locktype")) {
                            lockTypeNode = currentElem;
                        }
                        if (nodeName.endsWith("owner")) {
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
                    Element currentElem = (Element)iter.next();
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
                    Element currentElem = (Element)iter.next();
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
                    Element currentElem = (Element)iter.next();
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
                lock.setOwner(new String());
            }

        }

        String path = getRelativePath(req);
        lock.setPath(path);

        if (lockRequestType == LOCK_CREATION) {

            try {
                // Generating lock id
                String lockTokenStr = req.getServletPath()
                    + "-"
                    + lock.getType()
                    + "-"
                    + lock.getScope()
                    + "-"
                    + req.getUserPrincipal()
                    + "-"
                    + lock.getDepth()
                    + "-"
                    + lock.getOwner()
                    + "-"
                    + lock.getTokens()
                    + "-"
                    + lock.getExpiresAt()
                    + "-"
                    + System.currentTimeMillis()
                    + "-"
                    + m_secret;

                String lockToken = MD5_ENCODER.encode(m_md5Helper.digest(lockTokenStr.getBytes()));
                ArrayList errorLocks = new ArrayList();
                boolean result = m_session.lock(path, lock, lockToken, errorLocks);
                if (result) {

                    // Add the Lock-Token header as by RFC 2518 8.10.1
                    // - only do this for newly created locks
                    resp.addHeader("Lock-Token", "<opaquelocktoken:" + lockToken + ">");

                } else {

                    if (!errorLocks.isEmpty()) {
                        
                        resp.setStatus(CmsWebdavStatus.SC_CONFLICT);
                        
                        // One of the child paths was locked
                        // We generate a multistatus LockException
                        CmsWebdavLockException lockEx = new CmsWebdavLockException(CmsWebdavStatus.SC_CONFLICT, true);

                        Document doc = DocumentHelper.createDocument();
                        Element multiStatusElem = doc.addElement(new QName("multistatus", Namespace.get(DEFAULT_NAMESPACE)));

                        Iterator iter = errorLocks.iterator();
                        while (iter.hasNext()) {
                            String href = (String)iter.next();

                            Element responseElem = addElement(multiStatusElem, "response");
                            responseElem.addElement("href").addText(href);
                            responseElem.addElement("status").addText(
                                "HTTP/1.1 " + CmsWebdavStatus.SC_LOCKED + " " + CmsWebdavStatus.getStatusText(CmsWebdavStatus.SC_LOCKED));
                        }
                        
                        Writer writer = resp.getWriter();
                        doc.write(writer);
                        writer.close();

                        return;
                    }
                }

            } catch (CmsException e) {

                // TODO: fix that

            }
        }

        if (lockRequestType == LOCK_REFRESH) {

            String ifHeader = req.getHeader("If");
            if (ifHeader == null) {
                ifHeader = "";
            }

            m_handler.renewLock(lock, path, ifHeader);
        }

        // Set the status, then generate the XML response containing
        // the lock information
        Document doc = DocumentHelper.createDocument();
        Element propElem = doc.addElement(new QName("prop", Namespace.get(DEFAULT_NAMESPACE)));

        Element lockElem = addElement(propElem, "lockdiscovery");
        lock.toXML(lockElem);

        resp.setStatus(CmsWebdavStatus.SC_OK);
        resp.setContentType("text/xml; charset=UTF-8");

        Writer writer = resp.getWriter();
        doc.write(writer);
        writer.close();
    }

    /**
     *  Process a MKCOL WebDAV request for the specified resource.
     * 
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void doMkcol(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (m_readOnly) {
            resp.sendError(CmsWebdavStatus.SC_FORBIDDEN);
            return;
        }

        if (isLocked(req)) {
            resp.sendError(CmsWebdavStatus.SC_LOCKED);
            return;
        }

        String path = getRelativePath(req);
        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            resp.sendError(CmsWebdavStatus.SC_FORBIDDEN);
            return;
        }

        boolean exists = m_session.exists(path);

        // Can't create a collection if a resource already exists at the given path
        if (exists) {

            // Get allowed methods
            StringBuffer methodsAllowed = determineMethodsAllowed(getRelativePath(req));

            resp.addHeader("Allow", methodsAllowed.toString());

            resp.sendError(CmsWebdavStatus.SC_METHOD_NOT_ALLOWED);
            return;
        }

        if (req.getInputStream().available() > 0) {
            try {
                new SAXReader().read(req.getInputStream());
                // TODO : Process this request body
                resp.sendError(CmsWebdavStatus.SC_NOT_IMPLEMENTED);
                return;

            } catch (DocumentException de) {

                // Parse error - assume invalid content
                resp.sendError(CmsWebdavStatus.SC_BAD_REQUEST);
                return;
            }
        }

        boolean result = true;
        try {
            m_session.create(path);
        } catch (CmsException e) {
            result = false;
        }

        if (!result) {
            resp.sendError(CmsWebdavStatus.SC_CONFLICT, CmsWebdavStatus.getStatusText(CmsWebdavStatus.SC_CONFLICT));
        } else {
            resp.setStatus(CmsWebdavStatus.SC_CREATED);
        }
    }

    /**
     * Process a MOVE WebDAV request for the specified resource.
     * 
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void doMove(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (m_readOnly) {
            resp.sendError(CmsWebdavStatus.SC_FORBIDDEN);
            return;
        }

        if (isLocked(req)) {
            resp.sendError(CmsWebdavStatus.SC_LOCKED);
            return;
        }

        String path = getRelativePath(req);

        if (copyResource(req, resp)) {
            deleteResource(path, req, resp, false);
        }
    }

    /**
     * Process a OPTIONS WebDAV request for the specified resource.
     * 
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.addHeader("DAV", "1,2");

        StringBuffer methodsAllowed = determineMethodsAllowed(getRelativePath(req));

        resp.addHeader("Allow", methodsAllowed.toString());
        resp.addHeader("MS-Author-Via", "DAV");
    }

    /**
     * Process a PROPFIND WebDAV request for the specified resource.
     * 
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void doPropfind(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (!m_listings) {

            // Get allowed methods
            StringBuffer methodsAllowed = determineMethodsAllowed(getRelativePath(req));

            resp.addHeader("Allow", methodsAllowed.toString());
            resp.sendError(CmsWebdavStatus.SC_METHOD_NOT_ALLOWED);
            return;
        }

        String path = getRelativePath(req);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            resp.sendError(CmsWebdavStatus.SC_FORBIDDEN);
            return;
        }

        // Properties which are to be displayed.
        List properties = null;

        // Propfind depth
        int depth = INFINITY;

        // Propfind type
        int type = FIND_ALL_PROP;

        String depthStr = req.getHeader("Depth");

        if (depthStr == null) {
            depth = INFINITY;
        } else {
            if (depthStr.equals("0")) {
                depth = 0;
            } else if (depthStr.equals("1")) {
                depth = 1;
            } else if (depthStr.equals("infinity")) {
                depth = INFINITY;
            }
        }

        Element propNode = null;

        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(req.getInputStream());

            // Get the root element of the document
            Element rootElement = document.getRootElement();
            Iterator iter = rootElement.elementIterator();

            while (iter.hasNext()) {
                Element currentElem = (Element)iter.next();
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
            // TODO : Enhance that !
        }

        if (type == FIND_BY_PROPERTY) {
            properties = new Vector();

            Iterator iter = propNode.elementIterator();
            while (iter.hasNext()) {
                Element currentElem = (Element)iter.next();
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

        boolean exists = m_session.exists(path);
        if (!exists) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);
            return;
        }

        resp.setStatus(CmsWebdavStatus.SC_MULTI_STATUS);
        resp.setContentType("text/xml; charset=UTF-8");

        // Create multistatus object
        Document doc = DocumentHelper.createDocument();
        Element multiStatusElem = doc.addElement(new QName("multistatus", Namespace.get(DEFAULT_NAMESPACE)));

        if (depth == 0) {
            parseProperties(req, multiStatusElem, path, type, properties);
        } else {
            // The stack always contains the object of the current level
            Stack stack = new Stack();
            stack.push(path);

            // Stack of the objects one level below
            Stack stackBelow = new Stack();

            while ((!stack.isEmpty()) && (depth >= 0)) {

                String currentPath = (String)stack.pop();
                parseProperties(req, multiStatusElem, currentPath, type, properties);

                I_CmsWebdavItem item;
                try {
                    item = m_session.getItem(currentPath);
                } catch (CmsException e) {
                    continue;
                }

                if ((item.isCollection()) && (depth > 0)) {

                    try {
                        List list = m_session.list(currentPath);
                        Iterator iter = list.iterator();
                        while (iter.hasNext()) {
                            String element = (String)iter.next();
                            String newPath = currentPath;
                            if (!(newPath.endsWith("/"))) {
                                newPath += "/";
                            }
                            newPath += element;
                            stackBelow.push(newPath);
                        }

                    } catch (CmsException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("WebdavServlet: naming exception processing " + currentPath);
                        }
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
                        return;
                    }
                }

                if (stack.isEmpty()) {
                    depth--;
                    stack = stackBelow;
                    stackBelow = new Stack();
                }
            }
        }

        Writer writer = resp.getWriter();
        doc.write(writer);
        writer.close();
    }

    /**
     *  Process a PROPPATCH WebDAV request for the specified resource.
     *  Not implemented yet.
     * 
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void doProppatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (m_readOnly) {
            resp.sendError(CmsWebdavStatus.SC_FORBIDDEN);
            return;
        }

        if (isLocked(req)) {
            resp.sendError(CmsWebdavStatus.SC_LOCKED);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    /**
     * Process a POST request for the specified resource.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (isLocked(req)) {
            resp.sendError(CmsWebdavStatus.SC_LOCKED);
            return;
        }

        if (m_readOnly) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = getRelativePath(req);

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
            // FIXME: Add attributes
            m_session.create(path, resourceInputStream, exists);
        } catch (CmsException e) {
            result = false;
        }

        // Bugzilla 40326: at this point content file should be safe to delete
        // as it's no longer referenced.  Let's not rely on deleteOnExit because
        // it's a memory leak, as noted in this Bugzilla issue.
        try {
            contentFile.delete();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("DefaultServlet.doPut: couldn't delete temporary file: " + e.getMessage());
            }
        }

        if (result) {
            if (exists) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_CONFLICT);
        }
    }

    /**
     * Process a UNLOCK WebDAV request for the specified resource.
     * 
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void doUnlock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (m_readOnly) {
            resp.sendError(CmsWebdavStatus.SC_FORBIDDEN);
            return;
        }

        if (isLocked(req)) {
            resp.sendError(CmsWebdavStatus.SC_LOCKED);
            return;
        }

        String path = getRelativePath(req);

        String lockTokenHeader = req.getHeader("Lock-Token");
        if (lockTokenHeader == null) {
            lockTokenHeader = "";
        }

        m_handler.unlock(path, lockTokenHeader);

        resp.setStatus(CmsWebdavStatus.SC_NO_CONTENT);
    }

    /**
     * Handle a partial PUT.  New content specified in request is appended to
     * existing content in oldRevisionContent (if present). This code does
     * not support simultaneous partial updates to the same resource.
     * 
     * @param req The servlet request we are processing
     * @param range The range of the content in the file
     * @param path The path where to find the resource
     * @return The new content file with the appended data
     * 
     * @exception IOException if an input/output error occurs
     */
    protected File executePartialPut(HttpServletRequest req, CmsWebdavRange range, String path) throws IOException {

        // Append data specified in ranges to existing content for this
        // resource - create a temp. file on the local filesystem to
        // perform this operation
        File tempDir = (File)getServletContext().getAttribute("javax.servlet.context.tempdir");

        // Convert all '/' characters to '.' in resourcePath
        String convertedResourcePath = path.replace('/', '.');
        File contentFile = new File(tempDir, convertedResourcePath);
        contentFile.createNewFile();

        RandomAccessFile randAccessContentFile = new RandomAccessFile(contentFile, "rw");

        InputStream oldResourceStream = null;
        try {
            oldResourceStream = m_handler.getStreamContent(path);
        } catch (CmsWebdavResourceException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("DefaultServlet.executePartialPut: couldn't find resource at " + path);
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
     * Get the ETag associated with a file.
     *
     * @param item The WebDavItem
     * @return The created ETag for the resource attributes
     */
    protected String getETag(I_CmsWebdavItem item) {

        return "W/\"" + item.getContentLength() + "-" + item.getLastModifiedDate() + "\"";

        // TODO: add ETAG again
        //        String result = resourceAttributes.getETag(true);
        //        if (result != null) {
        //            return result;
        //        } else {
        //            result = resourceAttributes.getETag();
        //            if (result != null) {
        //                return result;
        //            } else {
        //                return "W/\""
        //                    + resourceAttributes.getContentLength()
        //                    + "-"
        //                    + resourceAttributes.getLastModified()
        //                    + "\"";
        //            }
        //        }
    }

    /**
     * Parse the range header.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param item The WebdavItem with the information
     * @return Vector of ranges
     * 
     * @throws IOException if an input/output error occurs
     */
    protected ArrayList parseRange(HttpServletRequest request, HttpServletResponse response, I_CmsWebdavItem item)
    throws IOException {

        // Checking If-Range
        String headerValue = request.getHeader("If-Range");

        if (headerValue != null) {

            long headerValueTime = (-1L);
            try {
                headerValueTime = request.getDateHeader("If-Range");
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
        String rangeHeader = request.getHeader("Range");

        if (rangeHeader == null) {
            return null;
        }

        // bytes is the only range unit supported (and I don't see the point
        // of adding new ones).
        if (!rangeHeader.startsWith("bytes")) {
            response.addHeader("Content-Range", "bytes */" + fileLength);
            response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return null;
        }

        rangeHeader = rangeHeader.substring(6);

        // Vector which will contain all the ranges which are successfully parsed.
        ArrayList result = new ArrayList();
        StringTokenizer commaTokenizer = new StringTokenizer(rangeHeader, ",");

        // Parsing the range list
        while (commaTokenizer.hasMoreTokens()) {
            String rangeDefinition = commaTokenizer.nextToken().trim();

            CmsWebdavRange currentRange = new CmsWebdavRange();
            currentRange.setLength(fileLength);

            int dashPos = rangeDefinition.indexOf('-');

            if (dashPos == -1) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            if (dashPos == 0) {

                try {
                    long offset = Long.parseLong(rangeDefinition);
                    currentRange.setStart(fileLength + offset);
                    currentRange.setEnd(fileLength - 1);
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range", "bytes */" + fileLength);
                    response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            } else {

                try {
                    currentRange.setStart(Long.parseLong(rangeDefinition.substring(0, dashPos)));
                    if (dashPos < rangeDefinition.length() - 1) {
                        currentRange.setEnd(Long.parseLong(rangeDefinition.substring(
                            dashPos + 1,
                            rangeDefinition.length())));
                    } else {
                        currentRange.setEnd(fileLength - 1);
                    }
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range", "bytes */" + fileLength);
                    response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            }

            if (!currentRange.validate()) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            result.add(currentRange);
        }

        return result;
    }

    /**
     * Return an InputStream to an HTML representation of the contents
     * of this directory.
     *
     * @param contextPath Context path to which our internal paths are relative
     * @param path The path of the resource to render the html for 
     * @return An input stream with the rendered html
     * 
     * @throws IOException if an input/output error occurs
     */
    protected InputStream renderHtml(String contextPath, String path) throws IOException {

        String name = path;

        // Number of characters to trim from the beginnings of filenames
        int trim = name.length();
        if (!name.endsWith("/")) {
            trim += 1;
        }

        if (name.equals("/")) {
            trim = 1;
        }

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
        sb.append(Messages.get().getBundle().key(Messages.DIRECTORY_TITLE_1, name));
        sb.append("</title>\r\n");

        // TODO: add opencms css style
        //        sb.append("<STYLE><!--");
        //        sb.append(org.opencms.webdav.util.TomcatCSS.TOMCAT_CSS);
        //        sb.append("--></STYLE> ");

        sb.append("</head>\r\n");
        sb.append("<body>");
        sb.append("<h1>");
        sb.append(Messages.get().getBundle().key(Messages.DIRECTORY_TITLE_1, name));

        // Render the link to our parent (if required)
        String parentDirectory = name;
        if (parentDirectory.endsWith("/")) {
            parentDirectory = parentDirectory.substring(0, parentDirectory.length() - 1);
        }
        int slash = parentDirectory.lastIndexOf('/');
        if (slash >= 0) {
            String parent = name.substring(0, slash);
            sb.append(" - <a href=\"");
            sb.append(rewrittenContextPath);
            if (parent.equals("")) {
                parent = "/";
            }
            sb.append(rewriteUrl(parent));
            if (!parent.endsWith("/")) {
                sb.append("/");
            }
            sb.append("\">");
            sb.append("<b>");
            sb.append(Messages.get().getBundle().key(Messages.DIRECTORY_PARENT_1, parent));
            sb.append("</b>");
            sb.append("</a>");
        }

        sb.append("</h1>");
        sb.append("<HR size=\"1\" noshade=\"noshade\">");

        sb.append("<table width=\"100%\" cellspacing=\"0\"" + " cellpadding=\"5\" align=\"center\">\r\n");

        // Render the column headings
        sb.append("<tr>\r\n");
        sb.append("<td align=\"left\"><font size=\"+1\"><strong>");
        sb.append(Messages.get().getBundle().key(Messages.DIRECTORY_FILENAME_0));
        sb.append("</strong></font></td>\r\n");
        sb.append("<td align=\"center\"><font size=\"+1\"><strong>");
        sb.append(Messages.get().getBundle().key(Messages.DIRECTORY_SIZE_0));
        sb.append("</strong></font></td>\r\n");
        sb.append("<td align=\"right\"><font size=\"+1\"><strong>");
        sb.append(Messages.get().getBundle().key(Messages.DIRECTORY_LASTMODIFIED_0));
        sb.append("</strong></font></td>\r\n");
        sb.append("</tr>");

        try {

            // Render the directory entries within this directory
            List list = m_session.list(path);
            Iterator iter = list.iterator();
            boolean shade = false;
            while (iter.hasNext()) {

                String resourceName = (String)iter.next();
                String trimmed = resourceName/*.substring(trim)*/;
                if (trimmed.equalsIgnoreCase("WEB-INF") || trimmed.equalsIgnoreCase("META-INF")) {
                    continue;
                }

                I_CmsWebdavItem childItem = null;
                try {
                    childItem = m_session.getItem(path + resourceName);
                } catch (CmsException ex) {
                    continue;
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
                resourceName = rewriteUrl(name + resourceName);
                sb.append(resourceName);
                if (childItem.isCollection()) {
                    sb.append("/");
                }
                sb.append("\"><tt>");
                sb.append(CmsEncoder.escapeXml(trimmed));
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
     * Render the specified file size (in bytes).
     *
     * @param size File size (in bytes)
     * @return A string with the given size formatted to output to user
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
     * URL rewriter.
     *
     * @param path Path which has to be rewriten
     * @return A string with the encoded path
     * 
     * @throws UnsupportedEncodingException if something goes wrong while encoding the url
     */
    protected String rewriteUrl(String path) throws UnsupportedEncodingException {

        return new String(URLCodec.encodeUrl(URL_SAFE_CHARS, path.getBytes("ISO-8859-1")));
    }

    /**
     * Serve the specified resource, optionally including the data content.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param content Should the content be included?
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    protected void serveResource(HttpServletRequest request, HttpServletResponse response, boolean content)
    throws IOException, ServletException {

        // Identify the requested resource path
        String path = getRelativePath(request);
        if (LOG.isDebugEnabled()) {
            if (content) {
                LOG.debug("DefaultServlet.serveResource:  Serving resource '" + path + "' headers and data");
            } else {
                LOG.debug("DefaultServlet.serveResource:  Serving resource '" + path + "' headers only");
            }
        }

        I_CmsWebdavItem item = null;
        try {
            item = m_session.getItem(path);
        } catch (CmsException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
            return;
        }

        // If the resource is not a collection, and the resource path
        // ends with "/" or "\", return NOT FOUND
        if (!item.isCollection()) {
            if (path.endsWith("/") || (path.endsWith("\\"))) {

                response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
                return;
            }
        }

        // Check if the conditions specified in the optional If headers are
        // satisfied.
        if (!item.isCollection()) {

            // Checking If headers
            // TODO: check this
            //            boolean included = (request.getAttribute(Globals.INCLUDE_CONTEXT_PATH_ATTR) != null);
            //            if (!included && !checkIfHeaders(request, response, cacheEntry.attributes)) {
            //                return;
            //            }

        }

        // Find content type.
        String contentType = item.getMimeType();
        if (contentType == null) {
            contentType = getServletContext().getMimeType(item.getName());
            //            cacheEntry.attributes.setMimeType(contentType);
        }

        ArrayList ranges = null;
        long contentLength = -1L;

        if (item.isCollection()) {

            // Skip directory listings if we have been configured to
            // suppress them
            if (!m_listings) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
                return;
            }
            contentType = "text/html;charset=UTF-8";

        } else {

            // Parse range specifier
            ranges = parseRange(request, response, item);

            // ETag header
            response.setHeader("ETag", getETag(item));

            // Last-Modified header
            response.setHeader("Last-Modified", HTTP_DATE_FORMAT.format(new Date(item.getLastModifiedDate())));

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
            || (((ranges == null) || (ranges.isEmpty())) && (request.getHeader("Range") == null))
            || (ranges == FULL_RANGE)) {

            // Set the appropriate output headers
            if (contentType != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("DefaultServlet.serveFile:  contentType='" + contentType + "'");
                }
                response.setContentType(contentType);
            }

            if ((!item.isCollection()) && (contentLength >= 0)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("DefaultServlet.serveFile:  contentLength=" + contentLength);
                }

                if (contentLength < Integer.MAX_VALUE) {
                    response.setContentLength((int)contentLength);
                } else {

                    // Set the content-length as String to be able to use a long
                    response.setHeader("content-length", "" + contentLength);
                }
            }

            InputStream renderResult = null;
            if (item.isCollection()) {

                if (content) {
                    // Serve the directory browser
                    renderResult = renderHtml(request.getContextPath(), item.getName());
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

                CmsWebdavRange range = (CmsWebdavRange)ranges.get(0);
                response.addHeader("Content-Range", "bytes "
                    + range.getStart()
                    + "-"
                    + range.getEnd()
                    + "/"
                    + range.getLength());
                long length = range.getEnd() - range.getStart() + 1;
                if (length < Integer.MAX_VALUE) {
                    response.setContentLength((int)length);
                } else {
                    // Set the content-length as String to be able to use a long
                    response.setHeader("content-length", "" + length);
                }

                if (contentType != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("DefaultServlet.serveFile:  contentType='" + contentType + "'");
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
     * Handles the special WebDAV methods.
     * 
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String method = req.getMethod();

        if (LOG.isDebugEnabled()) {
            String path = getRelativePath(req);
            LOG.debug("[" + method + "] " + path);
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
            //doUnlock(req, resp);
        } else {

            // DefaultServlet processing
            super.service(req, resp);
        }
    }

    private Element addElement(Element parent, String name) {

        return parent.addElement(new QName(name, Namespace.get(DEFAULT_NAMESPACE)));
    }

    /**
     * Copy a resource.
     *
     * @param req Servlet request
     * @param resp Servlet response
     * @return boolean true if the copy is successful
     */
    private boolean copyResource(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Parsing destination header
        String destinationPath = req.getHeader("Destination");

        if (destinationPath == null) {
            resp.sendError(CmsWebdavStatus.SC_BAD_REQUEST);
            return false;
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

        if ((destinationPath.toUpperCase().startsWith("/WEB-INF"))
            || (destinationPath.toUpperCase().startsWith("/META-INF"))) {
            resp.sendError(CmsWebdavStatus.SC_FORBIDDEN);
            return false;
        }

        String path = getRelativePath(req);

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            resp.sendError(CmsWebdavStatus.SC_FORBIDDEN);
            return false;
        }

        // source and destination are the same
        if (destinationPath.equals(path)) {
            resp.sendError(CmsWebdavStatus.SC_FORBIDDEN);
            return false;
        }

        // Parsing overwrite header
        boolean overwrite = true;
        String overwriteHeader = req.getHeader("Overwrite");

        if (overwriteHeader != null) {
            if (overwriteHeader.equalsIgnoreCase("T")) {
                overwrite = true;
            } else {
                overwrite = false;
            }
        }

        // Check if source exists
        if (!m_session.exists(path)) {
            resp.sendError(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR);
            return false;
        }

        // If the destination exists, then it's a conflict
        if ((m_session.exists(destinationPath)) && (!overwrite)) {
            resp.sendError(CmsWebdavStatus.SC_PRECONDITION_FAILED);
            return false;
        }

        if ((!m_session.exists(destinationPath)) && (overwrite)) {
            resp.setStatus(CmsWebdavStatus.SC_CREATED);
        }

        // Copying source to destination
        Hashtable errorList = new Hashtable();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Copy: " + path + " To: " + destinationPath);
        }

        boolean result = m_session.copy(path, destinationPath, overwrite, errorList);
        if ((!result) || (!errorList.isEmpty())) {

            sendReport(req, resp, errorList);
            return false;
        }

        return true;
    }

    /**
     * Checks if the items in the path or in a subpath are locked.
     * 
     * @param path The path to check the items for locks
     * @param lockTokens The lock tokens to use for the check
     * @param errorList The error list where to put the found errors
     */
    private void checkChildLocks(String path, String lockTokens, Hashtable errorList) {

        List list = null;
        try {
            list = m_session.list(path);
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("WebdavServlet: naming exception listing resources for " + path);
            }
            errorList.put(path, new Integer(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR));
            return;
        }

        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            String element = (String)iter.next();
            String childName = path;
            if (!childName.equals("/")) {
                childName += "/";
            }

            childName += element;
            if (m_session.isLocked(childName, lockTokens)) {
                errorList.put(childName, new Integer(CmsWebdavStatus.SC_LOCKED));
            } else {
                try {
                    I_CmsWebdavItem item = m_session.getItem(childName);
                    if (item.isCollection()) {
                        checkChildLocks(childName, lockTokens, errorList);
                    }
                } catch (CmsException e) {
                    errorList.put(childName, new Integer(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR));
                }
            }
        }
    }

    /**
     * Delete a resource.
     *
     * @param path Path of the resource which is to be deleted
     * @param req Servlet request
     * @param resp Servlet response
     * @param setStatus Should the response status be set on successful
     *                  completion
     */
    private boolean deleteResource(String path, HttpServletRequest req, HttpServletResponse resp, boolean setStatus)
    throws ServletException, IOException {

        String ifHeader = req.getHeader("If");
        if (ifHeader == null) {
            ifHeader = "";
        }

        String lockTokenHeader = req.getHeader("Lock-Token");
        if (lockTokenHeader == null) {
            lockTokenHeader = "";
        }

        boolean exists = m_session.exists(path);
        if (!exists) {
            resp.sendError(CmsWebdavStatus.SC_NOT_FOUND);
            return false;
        }

        Hashtable errorList = new Hashtable();
        checkChildLocks(path, ifHeader + lockTokenHeader, errorList);
        if (!errorList.isEmpty()) {
            sendReport(req, resp, errorList);
            return false;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Delete:" + path);
        }

        boolean result = m_session.delete(path, errorList);
        if ((!result) || (!errorList.isEmpty())) {

            sendReport(req, resp, errorList);
            return false;
        }

        if (setStatus) {
            resp.setStatus(CmsWebdavStatus.SC_NO_CONTENT);
        }

        return true;
    }

    /**
     * Determines the methods normally allowed for the resource.
     *
     */
    private StringBuffer determineMethodsAllowed(String path) {

        StringBuffer methodsAllowed = new StringBuffer();
        boolean exists = true;
        I_CmsWebdavItem item = null;
        try {
            item = m_session.getItem(path);
        } catch (CmsException e) {
            exists = false;
        }

        if (!exists) {
            methodsAllowed.append("OPTIONS, MKCOL, PUT, LOCK");
            return methodsAllowed;
        }

        methodsAllowed.append("OPTIONS, GET, HEAD, POST, DELETE, TRACE");
        methodsAllowed.append(", PROPPATCH, COPY, MOVE, LOCK, UNLOCK");

        if (m_listings) {
            methodsAllowed.append(", PROPFIND");
        }

        if (!item.isCollection()) {
            methodsAllowed.append(", PUT");
        }

        return methodsAllowed;
    }

    /**
     * Print the lock discovery information associated with a path.
     *
     * @param path Path
     * @param generatedXML XML data to which the locks info will be appended
     * @return true if at least one lock was displayed
     */
    private boolean generateLockDiscovery(String path, Element elem) {

        List locks = m_handler.getLocks(path);

        if (locks.size() > 0) {

            Element lockElem = addElement(elem, "lockdiscovery");

            Iterator iter = locks.iterator();
            while (iter.hasNext()) {
                CmsWebdavLockInfo current = (CmsWebdavLockInfo)iter.next();
                current.toXML(lockElem);
            }
            return true;
        }

        return false;
    }

    /**
     * Return the relative path associated with this servlet.
     *
     * @param request The servlet request we are processing
     */
    private String getRelativePath(HttpServletRequest request) {

        String result = request.getPathInfo();
        if (result == null) {
            result = request.getServletPath();
        }
        if ((result == null) || (result.equals(""))) {
            result = "/";
        }
        return (result);
    }

    /**
     * Check to see if a resource is currently write locked. The method
     * will look at the "If" header to make sure the client
     * has give the appropriate lock tokens.
     *
     * @param req Servlet request
     * @return boolean true if the resource is locked (and no appropriate
     * lock token has been found for at least one of the non-shared locks which
     * are present on the resource).
     */
    private boolean isLocked(HttpServletRequest req) {

        String path = getRelativePath(req);

        String ifHeader = req.getHeader("If");
        if (ifHeader == null) {
            ifHeader = "";
        }

        String lockTokenHeader = req.getHeader("Lock-Token");
        if (lockTokenHeader == null) {
            lockTokenHeader = "";
        }

        return m_handler.isLocked(path, ifHeader + lockTokenHeader);
    }

    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     *
     * @param path Path to be normalized
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
     * Parse the content-range header.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Range
     */
    private CmsWebdavRange parseContentRange(HttpServletRequest request, HttpServletResponse response)
    throws IOException {

        // Retrieving the content-range header (if any is specified
        String rangeHeader = request.getHeader("Content-Range");

        if (rangeHeader == null) {
            return null;
        }

        // bytes is the only range unit supported
        if (!rangeHeader.startsWith("bytes")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        rangeHeader = rangeHeader.substring(6).trim();

        int dashPos = rangeHeader.indexOf('-');
        int slashPos = rangeHeader.indexOf('/');

        if (dashPos == -1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if (slashPos == -1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        CmsWebdavRange range = new CmsWebdavRange();

        try {
            range.setStart(Long.parseLong(rangeHeader.substring(0, dashPos)));
            range.setEnd(Long.parseLong(rangeHeader.substring(dashPos + 1, slashPos)));
            range.setLength(Long.parseLong(rangeHeader.substring(slashPos + 1, rangeHeader.length())));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if (!range.validate()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        return range;
    }

    /**
     * Propfind helper method.
     *
     * @param req The servlet request
     * @param resources Resources object associated with this context
     * @param generatedXML XML response to the Propfind request
     * @param path Path of the current resource
     * @param type Propfind type
     * @param propertiesVector If the propfind type is find properties by
     * name, then this Vector contains those properties
     */
    private void parseProperties(HttpServletRequest req, Element elem, String path, int type, List propertiesVector)
    throws IOException {

        // Exclude any resource in the /WEB-INF and /META-INF subdirectories
        // (the "toUpperCase()" avoids problems on Windows systems)
        if (path.toUpperCase().startsWith("/WEB-INF") || path.toUpperCase().startsWith("/META-INF")) {
            return;
        }

        I_CmsWebdavItem item = null;
        try {
            item = m_session.getItem(path);
        } catch (CmsException e) {
            return;
        }

        Element responseElem = addElement(elem, "response");

        String status = new String("HTTP/1.1 "
            + CmsWebdavStatus.SC_OK
            + " "
            + CmsWebdavStatus.getStatusText(CmsWebdavStatus.SC_OK));

        // Generating href element
        Element hrefElem = addElement(responseElem, "href");

        String href = req.getContextPath() + req.getServletPath();
        if ((href.endsWith("/")) && (path.startsWith("/"))) {
            href += path.substring(1);
        } else {
            href += path;
        }

        if ((item.isCollection()) && (!href.endsWith("/"))) {
            href += "/";
        }

        hrefElem.addText(rewriteUrl(href));

        String resourceName = path;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1) {
            resourceName = resourceName.substring(lastSlash + 1);
        }

        Element propstatElem = addElement(responseElem, "propstat");
        Element propElem = propstatElem.addElement("prop");

        switch (type) {

            case FIND_ALL_PROP:

                addElement(propElem, "creationdate").addText(ISO8601_FORMAT.format(new Date(item.getCreationDate())));
                addElement(propElem, "displayname").addCDATA(resourceName);

                // properties only for files (no collections)
                if (!item.isCollection()) {

                    addElement(propElem, "getlastmodified").addText(
                        HTTP_DATE_FORMAT.format(new Date(item.getLastModifiedDate())));

                    addElement(propElem, "getcontentlength").addText(String.valueOf(item.getContentLength()));

                    String contentType = getServletContext().getMimeType(item.getName());
                    if (contentType != null) {
                        addElement(propElem, "getcontenttype").addText(contentType);
                    }
                    addElement(propElem, "getetag").addText(getETag(item));
                    addElement(propElem, "resourcetype");
                } else {
                    addElement(addElement(propElem, "resourcetype"), "collection");
                }

                addElement(propElem, "source").addText("");

                Element suppLockElem = addElement(propElem, "supportedlock");
                Element lockEntryElem = addElement(suppLockElem, "lockentry");
                addElement(addElement(lockEntryElem, "lockscope"), "exclusive");
                addElement(addElement(lockEntryElem, "locktype"), "write");
                lockEntryElem = addElement(suppLockElem, "lockentry");
                addElement(addElement(lockEntryElem, "lockscope"), "shared");
                addElement(addElement(lockEntryElem, "locktype"), "write");

                generateLockDiscovery(path, propElem);

                addElement(propstatElem, "status").addText(status);

                break;

            case FIND_PROPERTY_NAMES:

                addElement(propElem, "creationdate");
                addElement(propElem, "displayname");
                if (!item.isCollection()) {

                    addElement(propElem, "getcontentlanguage");
                    addElement(propElem, "getcontentlength");
                    addElement(propElem, "getcontenttype");
                    addElement(propElem, "getetag");
                    addElement(propElem, "getlastmodified");
                }
                addElement(propElem, "resourcetype");
                addElement(propElem, "source");
                addElement(propElem, "lockdiscovery");

                addElement(propstatElem, "status").addText(status);

                break;

            case FIND_BY_PROPERTY:

                List propertiesNotFound = new Vector();

                // Parse the list of properties
                Iterator iter = propertiesVector.iterator();
                while (iter.hasNext()) {
                    String property = (String)iter.next();

                    if (property.equals("creationdate")) {
                        addElement(propElem, "creationdate").addText(
                            ISO8601_FORMAT.format(new Date(item.getCreationDate())));
                    } else if (property.equals("displayname")) {
                        addElement(propElem, "displayname").addCDATA(resourceName);
                    } else if (property.equals("getcontentlanguage")) {
                        if (item.isCollection()) {
                            propertiesNotFound.add(property);
                        } else {
                            addElement(propElem, "getcontentlanguage");
                        }
                    } else if (property.equals("getcontentlength")) {
                        if (item.isCollection()) {
                            propertiesNotFound.add(property);
                        } else {
                            addElement(propElem, "getcontentlength").addText((String.valueOf(item.getContentLength())));
                        }
                    } else if (property.equals("getcontenttype")) {
                        if (item.isCollection()) {
                            propertiesNotFound.add(property);
                        } else {
                            addElement(propElem, "getcontenttype").addText(
                                getServletContext().getMimeType(item.getName()));
                        }
                    } else if (property.equals("getetag")) {
                        if (item.isCollection()) {
                            propertiesNotFound.add(property);
                        } else {
                            addElement(propElem, "getetag").addText(getETag(item));
                        }
                    } else if (property.equals("getlastmodified")) {
                        if (item.isCollection()) {
                            propertiesNotFound.add(property);
                        } else {
                            addElement(propElem, "getlastmodified").addText(
                                HTTP_DATE_FORMAT.format(new Date(item.getLastModifiedDate())));
                        }
                    } else if (property.equals("resourcetype")) {
                        if (item.isCollection()) {
                            addElement(addElement(propElem, "resourcetype"), "collection");
                        } else {
                            addElement(propElem, "resourcetype");
                        }
                    } else if (property.equals("source")) {
                        addElement(propElem, "source").addText("");
                    } else if (property.equals("supportedlock")) {
                        suppLockElem = addElement(propElem, "supportedlock");
                        lockEntryElem = addElement(suppLockElem, "lockentry");
                        addElement(addElement(lockEntryElem, "lockscope"), "exclusive");
                        addElement(addElement(lockEntryElem, "locktype"), "write");
                        lockEntryElem = addElement(suppLockElem, "lockentry");
                        addElement(addElement(lockEntryElem, "lockscope"), "shared");
                        addElement(addElement(lockEntryElem, "locktype"), "write");
                    } else if (property.equals("lockdiscovery")) {
                        if (!generateLockDiscovery(path, propElem)) {
                            propertiesNotFound.add(property);
                        }
                    } else {
                        propertiesNotFound.add(property);
                    }
                }

                addElement(propstatElem, "status").addText(status);

                if (propertiesNotFound.size() > 0) {
                    status = new String("HTTP/1.1 "
                        + CmsWebdavStatus.SC_NOT_FOUND
                        + " "
                        + CmsWebdavStatus.getStatusText(CmsWebdavStatus.SC_NOT_FOUND));

                    propstatElem = addElement(responseElem, "propstat");
                    propElem = addElement(propstatElem, "prop");

                    Iterator notFoundIter = propertiesNotFound.iterator();
                    while (notFoundIter.hasNext()) {
                        addElement(propElem, (String)notFoundIter.next());
                    }

                    addElement(propstatElem, "status").addText(status);
                }

                break;

            default:

                // TODO: what to do here?
                break;
        }
    }

    /**
     * Send a multistatus element containing a complete error report to the
     * client.
     *
     * @param req Servlet request
     * @param resp Servlet response
     * @param errors The errors to be displayed
     */
    private void sendReport(HttpServletRequest req, HttpServletResponse resp, Map errors)
    throws ServletException, IOException {

        resp.setStatus(CmsWebdavStatus.SC_MULTI_STATUS);

        String absoluteUri = req.getRequestURI();
        String relativePath = getRelativePath(req);

        Document doc = DocumentHelper.createDocument();
        Element multiStatusElem = doc.addElement(new QName("multistatus", Namespace.get(DEFAULT_NAMESPACE)));

        Iterator iter = errors.keySet().iterator();
        while (iter.hasNext()) {
            String errorPath = (String)iter.next();
            int errorCode = ((Integer)errors.get(errorPath)).intValue();

            Element responseElem = addElement(multiStatusElem, "response");

            String toAppend = errorPath.substring(relativePath.length());
            if (!toAppend.startsWith("/")) {
                toAppend = "/" + toAppend;
            }
            addElement(responseElem, "href").addText(absoluteUri + toAppend);
            addElement(responseElem, "status").addText(
                "HTTP/1.1 " + errorCode + " " + CmsWebdavStatus.getStatusText(errorCode));
        }

        Writer writer = resp.getWriter();
        doc.write(writer);
        writer.close();
    }
}
