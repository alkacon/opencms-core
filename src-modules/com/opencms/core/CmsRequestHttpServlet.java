/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/core/Attic/CmsRequestHttpServlet.java,v $
* Date   : $Date: 2005/05/31 15:51:19 $
* Version: $Revision: 1.2 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.core;

import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsResourceTranslator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Implementation of the I_CmsRequest interface which wraps a HttpServletRequest
 * and includes handling of multipart - requests.<p>
 *
 * This implementation uses a HttpServletRequest as original request to create a
 * CmsRequestHttpServlet. This either can be a normal HttpServletRequest or a
 * CmsMultipartRequest which is used to upload file into the OpenCms.<p>
 *
 * This class contains a modification of the MultipartRequest published in the O'Reilly
 * book <it>Java Servlet Programming </it> by J. Junte, <a href=http://www.servlets.com/ > www.servlets.com </a>
 * <p>
 * It Constructs a new MultipartRequest to handle the specified request,
 * saving any uploaded files to the given directory, and limiting the upload size to
 * a maximum size of 8 MB by default.
 * <p>
 * The idea is to modify the given MultiPartRequest to make it transparent to normal
 * requests and store file into CmsFile objects so that they can be transferred into
 * the OpenCms document database.
 *
 * @author Michael Emmerich
 * @author Alexander Lucas
 * 
 * @version $Revision: 1.2 $ $Date: 2005/05/31 15:51:19 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsRequestHttpServlet implements I_CmsRequest {

    /**
     * Definition of the error message for missing boundary.
     */
    static final String C_REQUEST_NOBOUNDARY = "Separation boundary was not specified";

    /**
     * Definition of the error message for being not a multipart request.
     */
    static final String C_REQUEST_NOMULTIPART = "Posted content type isn't multipart/form-data";

    /**
     * Definition of the error message for an empty request.
     */
    static final String C_REQUEST_NOTNULL = "The Request cannot be null.";

    /**
     * Definition of the error message for a premature end.
     */
    static final String C_REQUEST_PROMATUREEND = "Corrupt form data: premature ending";

    /**
     * Definition of the error message for a negative maximum post size.
     */
    static final String C_REQUEST_SIZENOTNEGATIVE = "The maxPostSize must be positive.";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRequestHttpServlet.class);
    
    /**
     * File counter.
     */
    int m_filecounter;

    /**
     * Storage for all uploaded files.
     */
    private Hashtable m_files = new Hashtable();

    /**
     * Storage for all uploaded name values.
     */
    private Hashtable m_parameters = new Hashtable();

    /**
     * The original request.
     */
    private HttpServletRequest m_req;

    /** String to the requested resource. */
    private String m_requestedResource;
    private String m_scheme="";
    private String m_serverName="";
    private int m_serverPort;
    private String m_servletUrl="";

    /**
     * Resource translator (for uploaded file names).
     */
    private CmsResourceTranslator m_translator;
    
    /**
     * The type of theis CmsRequest.
     */
    private int m_type = I_CmsConstants.C_REQUEST_HTTP;

    /**
     * The data from the original request. We save them to get them after the
     * original request is expired.
     */
    private String m_webAppUrl="";

    /**
     * Constructor, creates a new CmsRequestHttpServlet object.
     *
     * @param req The original HttpServletRequest used to create this CmsRequest
     * @param translator the translator
     * @throws IOException if something goes wrong
     */
    public CmsRequestHttpServlet(HttpServletRequest req, CmsResourceTranslator translator) throws IOException {
        m_req = req;
        m_translator = translator;
        
        // get the webAppUrl and the servletUrl
        try {
            m_webAppUrl = m_req.getContextPath();
        } catch (NoSuchMethodError err) {
            // this is the old servlet-api without this method
            // ignore this missing method and the context-path
        }
        m_serverName = m_req.getServerName();
        m_scheme = m_req.getScheme();
        m_serverPort = m_req.getServerPort();
        m_servletUrl = m_webAppUrl + m_req.getServletPath();
        // Test if this is a multipart-request.
        // If it is, extract all files from it.
        String type = req.getHeader("content-type");
        if ((type != null) && type.startsWith("multipart/form-data") && (req.getContentLength() > -1)) {
            readRequest();
        } else {
            // Encoding project:
            // Set request content encoding
            String encoding = req.getCharacterEncoding();
            if (encoding == null) {
                // First try to get current encoding from session
                HttpSession httpSession = req.getSession(false);
                I_CmsSession session = (httpSession != null) 
                    ? new CmsSession(httpSession) : null;
                if (session != null) {
                    encoding = (String)session.getValue(
                        I_CmsConstants.C_SESSION_CONTENT_ENCODING);
                }
                // If encoding not found in session - use default one
                if (encoding == null) {
                    encoding = OpenCms.getSystemInfo().getDefaultEncoding();
                }
                req.setCharacterEncoding(encoding);
            }
            if (LOG.isDebugEnabled()) { 
                LOG.debug("Request character encoding is: '" + req.getCharacterEncoding() + "'");
            }
        }
    }

    /**
     * Returns the content of an uploaded file.
     * Returns null if no file with this name has been uploaded with this request.
     * Returns an empty byte[] if a file without content has been uploaded.
     *
     * @param name The name of the uploaded file.
     * @return The selected uploaded file content.
     */
    public byte[] getFile(String name) {
        return (byte[])m_files.get(name);
    }

    /**
     * Returns the names of all uploaded files in this request.
     * Returns an empty eumeration if no files were included in the request.
     *
     * @return An Enumeration of file names.
     */
    public Enumeration getFileNames() {
        Enumeration names = m_files.keys();
        return names;
    }

    /**
     * Returns the original request that was used to create the CmsRequest.
     *
     * @return The original request of the CmsRequest.
     */
    public HttpServletRequest getOriginalRequest() {
        return m_req;
    }

    /**
     * Returns the type of the request that was used to create the CmsRequest.
     * The returned int must be one of the constants defined above in this interface.
     *
     * @return The type of the CmsRequest.
     */
    public int getOriginalRequestType() {
        return m_type;
    }

    /**
     * Returns the value of a named parameter as a String.
     * Returns null if the parameter does not exist or an empty string if the parameter
     * exists but without a value.
     *
     * @param name The name of the parameter.
     * @return The value of the parameter.
     */
    public String getParameter(String name) {
        String parameter = null;

        // Test if this is a multipart-request.
        // If it is, extract all files from it.
        String type = m_req.getHeader("content-type");
        if ((type != null) && type.startsWith("multipart/form-data")) {
            parameter = (String)m_parameters.get(name);
        } else {
            parameter = m_req.getParameter(name);
        }
        return parameter;
    }

    /**
     * Returns all parameter names as an Enumeration of String objects.
     * Returns an empty Enumeratrion if no parameters were included in the request.
     *
     * @return Enumeration of parameter names.
     */
    public Enumeration getParameterNames() {
        String type = m_req.getHeader("content-type");
        if ((type != null) && type.startsWith("multipart/form-data")) {

            // add all parameters extreacted in the multipart handling
            return m_parameters.keys();
        } else {

            // add all parameters from the original request
            return m_req.getParameterNames();
        }
    }

    /**
     * Returns all parameter values of a parameter key.
     *
     * @param key the parameter key
     * @return Aarray of String containing the parameter values.
     */
    public String[] getParameterValues(String key) {
        return m_req.getParameterValues(key);
    }

    /**
     * This funtion returns the name of the requested resource.
     * <P>
     * For a http request, the name of the resource is extracted as follows:
     * <CODE>http://{servername}/{servletpath}/{path to the cms resource}</CODE>
     * In the following example:
     * <CODE>http://my.work.server/servlet/opencms/system/def/explorer</CODE>
     * the requested resource is <CODE>/system/def/explorer</CODE>.
     * </P>
     *
     * @return The path to the requested resource.
     */
    public String getRequestedResource() {
        if (m_requestedResource != null) {
            return m_requestedResource;
        }
        m_requestedResource = m_req.getPathInfo();
        if (m_requestedResource == null) {
            m_requestedResource = "/";
        }       
        return m_requestedResource;
    }
    /**
     * Methods to get the data from the original request.
     * 
     * @return the scheme
     */
    public String getScheme() {
        return m_scheme;
    }

    /**
     * Methods to get the data from the original request.
     * 
     * @return the server name
     */
    public String getServerName() {
        return m_serverName;
    }
    /**
     * Methods to get the data from the original request.
     * 
     * @return the server port
     */
    public int getServerPort() {
        return m_serverPort;
    }

    /**
     * Gets the part of the Url that describes the current servlet of this
     * Web-Application.
     * 
     * @return the servlet part of the url
     */
    public String getServletUrl() {
        return m_servletUrl;
    }

    /**
     * Returns the part of the Url that descibes the Web-Application.
     *
     * E.g: http://www.myserver.com/opencms/engine/index.html returns
     * http://www.myserver.com/opencms
     * 
     * @return the web application part of the url
     */
    public String getWebAppUrl() {
        return m_webAppUrl;
    }
    
    /**
     * Overwrites the original request that was used to create the CmsRequest.
     * 
     * @param request the request
     */
    public void setOriginalRequest(HttpServletRequest request) {
        m_req = request;
    }
    
    /**
     * Set the name returned by getRequestedResource().
     * This is required in case there was a folder name requested and 
     * a default file (e.g. index.html) has to be used instead of the folder.
     * 
     * @param resourceName The name to set the requested resource name to 
     */
    public void setRequestedResource(String resourceName) {
        m_requestedResource = resourceName;
    }    

    /**
     * Extracts and returns the boundary token from a line.
     *
     * @param line with boundary from input stream.
     * @return The boundary token.
     */
    private String extractBoundary(String line) {
        int index = line.indexOf("boundary=");
        if (index == -1) {
            return null;
        }

        // 9 for "boundary="
        String boundary = line.substring(index + 9);

        // The real boundary is always preceeded by an extra "--"
        boundary = "--" + boundary;
        return boundary;
    }

    /**
     * Extracts and returns the content type from a line, or null if the
     * line was empty.
     * @param line Line from input stream.
     * @return Content type of the line.
     * @throws IOException Throws an IOException if the line is malformatted.
     */
    private String extractContentType(String line) throws IOException {
        String contentType = null;

        // Convert the line to a lowercase string
        String origline = line;
        line = origline.toLowerCase();

        // Get the content type, if any
        if (line.startsWith("content-type")) {
            int start = line.indexOf(" ");
            if (start == -1) {
                throw new IOException("Content type corrupt: " + origline);
            }
            contentType = line.substring(start + 1);
        } else {
            if (line.length() != 0) {
                // no content type, so should be empty
                throw new IOException("Malformed line after disposition: " + origline);
            }
        }
        return contentType;
    }

    /**
     * Extracts and returns disposition info from a line, as a String array
     * with elements: disposition, name, filename.  Throws an IOException
     * if the line is malformatted.
     *
     * @param line Line from input stream.
     * @return Array of string containing disposition information.
     * @throws IOException if the line is malformatted
     */
    private String[] extractDispositionInfo(String line) throws IOException {

        // Return the line's data as an array: disposition, name, filename
        String[] retval = new String[3];

        // Convert the line to a lowercase string without the ending \r\n
        // Keep the original line for error messages and for variable names.
        String origline = line;
        line = origline.toLowerCase();

        // Get the content disposition, should be "form-data"
        int start = line.indexOf("content-disposition: ");
        int end = line.indexOf(";");
        if (start == -1 || end == -1) {
            throw new IOException("Content disposition corrupt: " + origline);
        }
        String disposition = line.substring(start + 21, end);
        if (!disposition.equals("form-data")) {
            throw new IOException("Invalid content disposition: " + disposition);
        }

        // Get the field name
        // start at last semicolon
        start = line.indexOf("name=\"", end);

        // skip name=\"
        end = line.indexOf("\"", start + 7);
        if (start == -1 || end == -1) {
            throw new IOException("Content disposition corrupt: " + origline);
        }
        String name = origline.substring(start + 6, end);

        // Get the filename, if given
        String filename = null;

        // start after name
        start = line.indexOf("filename=\"", end + 2);

        // skip filename=\"
        end = line.indexOf("\"", start + 10);

        // note the !=
        if (start != -1 && end != -1) {
            filename = origline.substring(start + 10, end);

            // The filename may contain a full path.  Cut to just the filename.
            int slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
            if (slash > -1) {
                filename = filename.substring(slash + 1); // past last slash
            }
            if (filename.equals("")) {
                filename = "unknown"; // sanity check
            }
        }

        // Translate the filename using the resource translator        
        filename = m_translator.translateResource(filename);

        // Return a String array: disposition, name, filename
        retval[0] = disposition;
        retval[1] = name;
        retval[2] = filename;
        return retval;
    }

    /**
     * A utility method that reads a single part of the multipart request
     * that represents a file. Unlike the method name it does NOT saves the file to disk.
     * The name is from the original O'Reilly implmentaton.
     * <p>
     * A subclass can override this method for a better optimized or
     * differently behaved implementation.
     *
     * @param in The stream from which to read the file.
     * @param boundary The boundary signifying the end of this part.
     * @return the output
     * @throws IOException If there's a problem reading or parsing the request.
     */
    private byte[] readAndSaveFile(CmsMultipartInputStreamHandler in, String boundary) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(8 * 1024);
        byte[] boundaryBytes = boundary.getBytes();
        int[] lookaheadBuf = new int[boundary.length() + 3];
        int[] newLineBuf =  {
            -1, -1
        };
        int matches = 0;
        int matchingByte = new Byte(boundaryBytes[matches]).intValue();

        /*
        File parts of multipart request should not be read line by line.
        Since some servlet environments touch and change the new line
        character(s) when calling the ServletInputStream's <code>readLine</code>
        this may cause problems with binary file uploads.
        We decided to read this parts byte by byte here.
        */
        int read = in.read();
        while (read > -1) {
            if (read == matchingByte) {

                // read byte is matching the next byte of the boundary
                // we should not write to the output stream here.
                lookaheadBuf[matches] = read;
                matches++;
                if (matches == boundary.length()) {

                    // The end of the Boundary has been reached.
                    // Now snip the following line feed.
                    read = in.read();
                    if (newLineBuf[1] == read) {

                        // New line contains ONE single character.
                        // Write the last byte of the buffer to the output stream.
                        out.write(newLineBuf[0]);
                    } else {

                        // new line contains TWO characters, possibly "\r\n"
                        // The bytes in the buffer are not part of the file.
                        // We even have to read one more byte.
                        in.read();
                    }
                    break;
                }
                matchingByte = new Byte(boundaryBytes[matches]).intValue();
            } else {

                // read byte does not match the next byte of the boundary
                // write the first buffer byte to the output stream
                if (newLineBuf[0] != -1) {
                    out.write(newLineBuf[0]);
                }
                if (matches == 0) {

                    // this may be the most propably case.
                    newLineBuf[0] = newLineBuf[1];
                } else {

                    // we have started to read the boundary.
                    // Unfortunately, this was NOT the real boundary.
                    // Fall back to normal read mode.
                    // write the complete buffer to the output stream
                    if (newLineBuf[1] != -1) {
                        out.write(newLineBuf[1]);
                    }
                    for (int i = 0; i < matches; i++) {
                        out.write(lookaheadBuf[i]);
                    }

                    // reset boundary matching counter
                    matches = 0;
                    matchingByte = new Byte(boundaryBytes[matches]).intValue();

                    // clear buffer
                    newLineBuf[0] = -1;
                }

                // put the last byte read into the buffer.
                // it may be part of a line feed.
                newLineBuf[1] = read;
            }
            read = in.read();
        }
        out.flush();
        return out.toByteArray();
    }

    /**
     * A utility method that reads an individual part.  Dispatches to
     * readParameter() and readAndSaveFile() to do the actual work.
     * <p>
     * The single files are stored in a hashtable (seperated in filename and contents)
     * for later addition to a CmsFile Object
     * <p> A subclass can override this method for a better optimized or
     * differently behaved implementation.
     *
     * @param in The stream from which to read the part
     * @param boundary The boundary separating parts
     * @return A flag indicating whether this is the last part
     * @throws IOException If there's a problem reading or parsing the
     * request
     */
    private boolean readNextPart(CmsMultipartInputStreamHandler in, String boundary) throws IOException {

        // Read the first line, should look like this:
        // content-disposition: form-data; name="field1"; filename="file1.txt"
        String line = in.readLine();
        if (line == null || line.equals("")) {

            // No parts left, we're done
            return true;
        }

        // Parse the content-disposition line
        String[] dispInfo = extractDispositionInfo(line);

        // String disposition = dispInfo[0];
        String name = dispInfo[1];
        String filename = dispInfo[2];

        // Now onto the next line.  This will either be empty
        // or contain a Content-Type and then an empty line.
        line = in.readLine();
        if (line == null) {

            // No parts left, we're done
            return true;
        }

        // Get the content type, or null if none specified
        String contentType = extractContentType(line);
        if (contentType != null) {

            // Eat the empty line
            line = in.readLine();
            if (line == null || line.length() > 0) { // line should be empty
                line = in.readLine();
                if (line == null || line.length() > 0) { // line should be empty
                    throw new IOException("Malformed line after content type: " + line);
                }
            }
            
        } else {

            // Assume a default content type
            contentType = "application/octet-stream";
        }

        // Now, finally, we read the content (end after reading the boundary)
        if (filename == null) {

            // This is a parameter
            String value = readParameter(in, boundary);
            m_parameters.put(name, value);
        } else {
            m_filecounter++;
            // stroe the filecontent
            m_files.put(filename, readAndSaveFile(in, boundary));
            // store the name of the file to the parameters
            m_parameters.put(name, filename);
        }

        // there's more to read
        return false;
    }

    /**
     * A utility method that reads a single part of the multipart request
     * that represents a parameter.  A subclass can override this method
     * for a better optimized or differently behaved implementation.
     *
     * @param in The stream from which to read the parameter information
     * @param boundary The boundary signifying the end of this part
     * @return The parameter value
     * @throws IOException If there's a problem reading or parsing the
     * request
     */
    private String readParameter(CmsMultipartInputStreamHandler in, String boundary) throws IOException {
        StringBuffer sbuf = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null) {
            if (line.startsWith(boundary)) {
                break;
            }

            // add the \r\n in case there are many lines
            sbuf.append(line + "\r\n");
        }
        if (sbuf.length() == 0) {

            // nothing read
            return null;
        }

        // cut off the last line's \r\n
        sbuf.setLength(sbuf.length() - 2);

        // no URL decoding needed
        return sbuf.toString();
    }

    /**
     * This method actually parses the request.  A subclass
     * can override this method for a better optimized or differently
     * behaved implementation.
     *
     * @throws IOException If the uploaded content is larger than
     * <tt>maxSize</tt> or there's a problem parsing the request.
     */
    private void readRequest() throws IOException {

        // Check the content type to make sure it's "multipart/form-data"
        String type = m_req.getContentType();
        if (type == null || !type.toLowerCase().startsWith("multipart/form-data")) {
            throw new IOException(C_REQUEST_NOMULTIPART);
        }

        int length = m_req.getContentLength();

        // Get the boundary string; it's included in the content type.
        // Should look something like "------------------------12012133613061"
        String boundary = extractBoundary(type);
        if (boundary == null) {
            throw new IOException(C_REQUEST_NOBOUNDARY);
        }

        // Construct the special input stream we'll read from
        CmsMultipartInputStreamHandler in = new CmsMultipartInputStreamHandler(m_req.getInputStream(), length);

        // Read the first line, should be the first boundary
        String line = in.readLine();
        if (line == null) {
            throw new IOException(C_REQUEST_PROMATUREEND);
        }

        // Verify that the line is the boundary
        if (!line.startsWith(boundary)) {
            throw new IOException(C_REQUEST_NOBOUNDARY);
        }

        // Now that we're just beyond the first boundary, loop over each part
        boolean done = false;
        while (!done) {
            done = readNextPart(in, boundary);
        }

        // Unfortunately some servlet environmets cannot handle multipart
        // requests AND URL parameters at the same time, we have to manage
        // the URL params ourself here. So try to read th URL parameters:
        String queryString = m_req.getQueryString();
        if (queryString != null) {
            StringTokenizer st = new StringTokenizer(m_req.getQueryString(), "&");
            while (st.hasMoreTokens()) {

                // Loop through all parameters
                String currToken = st.nextToken();
                if (currToken != null && !"".equals(currToken)) {

                    // look for the "=" character to divide parameter name and value
                    int idx = currToken.indexOf("=");
                    if (idx > -1) {
                        String key = currToken.substring(0, idx);
                        String value = (idx < (currToken.length() - 1)) ? currToken.substring(idx + 1) : "";
                        m_parameters.put(key, value);
                    }
                }
            }
        }
    }
}
