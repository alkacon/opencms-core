/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsMultipartRequest.java,v $
 * Date   : $Date: 2000/03/08 08:59:28 $
 * Version: $Revision: 1.5 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


package com.opencms.core
                        ;
import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.opencms.file.*;


/**
* This class implements the methods of the HttpServletRequest Interface
* <p>
* It is a modification of the MultipartRequest published in the O'Reilly 
* book <it>Java Servlet Programming </it> by J. Junte, <a href=http://www.servlets.com/ > www.servlets.com </a>
* <p>
* It Constructs a new MultipartRequest to handle the specified request, 
* saving any uploaded files to the given directory, and limiting the upload size to
* a maxumum size of 8 MB.
* <p>
* The idea is to modify the given MultiPartRequest to make it transparent to normal 
* requests and store file into CmsFile objects so that they can be transferred into
* the OpenCms document database.
* 
* @author Alexander Kandzior
* @author Michael Emmerich
* @version $Revision: 1.5 $ $Date: 2000/03/08 08:59:28 $  
* 
*/
public class CmsMultipartRequest implements HttpServletRequest {

  /**
   * Define the maximum size for an uploaded file (8 MB)
   */
  private static final int DEFAULT_MAX_POST_SIZE = 8192 * 1024;// 8 Meg

  /**
   * Definition of the error message for an empty request.
   */
  static final String C_REQUEST_NOTNULL="The Request cannot be null.";
  
   /**
   * Definition of the error message for being not a multipart request.
   */
  static final String C_REQUEST_NOMULTIPART="Posted content type isn't multipart/form-data";
  
  /**
   * Definition of the error message for a negative maximum post size.
   */
  static final String C_REQUEST_SIZENOTNEGATIVE="The maxPostSize must be positive.";

   /**
   * Definition of the error message for a premature end.
   */
  static final String C_REQUEST_PROMATUREEND="Corrupt form data: premature ending";
   /**
   * Definition of the error message for missing boundary.
   */
  static final String C_REQUEST_NOBOUNDARY="Separation boundary was not specified";
  
  /**
   * The HTTP-Request containing the uploaded data.
   */
  private HttpServletRequest m_req;
  
  /**
   * The maximum size of the uploaded data.
   */
  private int m_maxSize;

  /**
  * Storage for all uploaded name values
  */
  private Hashtable m_parameters = new Hashtable(); 
  
  /**
  * Storage for all uploaded files.
  */
  private Hashtable m_files = new Hashtable();      

  /**
   * Constructs a new MultipartRequest to handle the specified request,
   * saving any uploaded files in a CmsFile Object, and limiting the
   * upload size to 8 Megabyte.  If the content is too large, an
   * IOException is thrown.  This constructor actually parses the
   * <tt>multipart/form-data</tt> and throws an IOException if there's any
   * problem reading or parsing the request.
   *
   * @param request The servlet request.
   * @exception IOException Tf the uploaded content is larger than 8 Megabyte
   * or there's a problem reading or parsing the request.
   */
  public CmsMultipartRequest(HttpServletRequest request)
	  throws IOException  {
	  this(request, DEFAULT_MAX_POST_SIZE);
  }

  /**
   * Constructs a new MultipartRequest to handle the specified request,
   * saving any uploaded files in a CmsFile Object, and limiting the
   * upload size to the specified length.  If the content is too large, an
   * IOException is thrown.  This constructor actually parses the
   * <tt>multipart/form-data</tt> and throws an IOException if there's any
   * problem reading or parsing the request.
   *
   * @param request The servlet request.
   * @param maxPostSize The maximum size of the POST content.
   * @exception IOException If the uploaded content is larger than
   * <tt>maxPostSize</tt> or there's a problem reading or parsing the request.
   */

	public CmsMultipartRequest(HttpServletRequest request, int maxPostSize)
		  throws IOException {
		  // Sanity check values
        if (request == null) {
			  throw new IllegalArgumentException(C_REQUEST_NOTNULL);
        }
        if (maxPostSize <= 0) {
			  throw new IllegalArgumentException(C_REQUEST_SIZENOTNEGATIVE);
        }
		  m_req = request;
		  m_maxSize = maxPostSize;
		  
          // Now parse the request 
		  readRequest();
	}

	/**
	 * Implements HttpServletRequest method.
	 * 
	 * @return Array of Cookie Objects.
	 */
	public Cookie[] getCookies() {
		return m_req.getCookies();
	}

	/**
	 * Implements HttpServletRequest method.
	 * 
	 * @return HTTP method to make the request.
	 */
    public String getMethod() {
		return m_req.getMethod();
	}
	
	/**
	 * Implements HttpServletRequest method.
	 * 
	 * @return The URI of the request.
	 */
    public String getRequestURI() {
		return m_req.getRequestURI();
	}

	/**
	 * Implements HttpServletRequest method.
	 * 
	 * @return Part of the URI that refers to the Servlet.
	 */
    public String getServletPath() {
		return m_req.getServletPath();
	}

	/**
	 * Implements HttpServletRequest method.
	 * 
	 * @return Extra path info associated with the request or null.
	 */
    public String getPathInfo()	{
		return m_req.getPathInfo();
	}

	/**
	 * Implements HttpServletRequest method.
	 * 
	 * @return Extra path info translated into the file system or null.
	 */
    public String getPathTranslated() {
		return m_req.getPathTranslated();
	}

	/**
	 * Implements HttpServletRequest method.
	 * 
	 * @return Query string from request URL.
	 */	
    public String getQueryString() {
		return m_req.getQueryString();
	}

	 /**
	 * Implements HttpServletRequest method.
	 * 
	 * @return Name of the user making the request or null.
	 */
    public String getRemoteUser() {
		return m_req.getRemoteUser();
	}
	
	/**
	 * Implements HttpServletRequest method.
	 * 
	 * @return Servlet's authentication scheme or null.
	 */
    public String getAuthType()	{
		return m_req.getAuthType();
	}
	
	 /**
	 * Implements HttpServletRequest method.
	 * 
	 * @param name Name of the header.
	 * @return Value of the header.
	 */
    public String getHeader(String name) {
		return m_req.getHeader(name);
	}

	 /**
	 * Implements HttpServletRequest method.
	 * 
	 * @param name Name of the header.
	 * @return Value of the header.
	 */
    public int getIntHeader(String name) {
		return m_req.getIntHeader(name);
	}

	 /**
	 * Implements HttpServletRequest method.
	 * 
	 * @param name Name of the Header.
	 * @return Value of the header that represents a date.
	 */
    public long getDateHeader(String name) {
		return m_req.getDateHeader(name);
	}

	 /**
	 * Implements HttpServletRequest method.
	 * 
	 * @return The names of all headers as an enumartion of strings.
	 */
    public Enumeration getHeaderNames()	{
		return m_req.getHeaderNames();
	}

	 /**
	 * Implements HttpServletRequest method.
	 * 
	 * @param create If true, a new session is created.
	 * @return Current Session associated with the user making the request.
	 */
    public HttpSession getSession (boolean create) {
		return m_req.getSession(create);
	}

     /**
	 * Implements HttpServletRequest method.
	 * 
	 * @return Current Session associated with the user making the request.
	 */
	public HttpSession getSession () {
		return m_req.getSession();
	}
    
	
	 /**
	 * Implements HttpServletRequest method.
	 * 
	 * @return Session ID that is requested by the client.
	 */
    public String getRequestedSessionId () {
		return m_req.getRequestedSessionId();
	}

	 /**
	 * Implements HttpServletRequest method.
	 * 
	 * @return True if the requested session is valid and in use.
	 */
    public boolean isRequestedSessionIdValid ()	{
		return m_req.isRequestedSessionIdValid();
	}

	 /**
	 * Implements HttpServletRequest method.
	 * 
	 * @return True, if the client submitted a session ID via a cookie.
	 */
    public boolean isRequestedSessionIdFromCookie () {
		return m_req.isRequestedSessionIdFromCookie();
	}

	 /**
	 * Implements HttpServletRequest method.
	 * 
	 * @return True, if the client submitted a session ID via  an URL.
	 */
    public boolean isRequestedSessionIdFromURL () {
		return m_req.isRequestedSessionIdFromURL();
	}

    /**
	 * Implements HttpServletRequest method.
	 * 
	 * @return True, if the client submitted a session ID via  an URL.
	 */
    public boolean isRequestedSessionIdFromUrl () {
		return m_req.isRequestedSessionIdFromUrl();
    }
    
	 
	/**
	 * Implements ServletRequest method.
	 * 
	 * @return Lenght of the received request in bytes.
	 */
    public int getContentLength() {
		return m_req.getContentLength();
	}
	
	/**
	 * Implements ServletRequest method.
	 * 
	 * @return Content type of the received request.
	 */
    public String getContentType() {
		return m_req.getContentType();
	}

	/**
	 * Implements ServletRequest method.
	 * 
	 * @return Name and version of the protocol used for the request.
	 */	
    public String getProtocol()	{
		return m_req.getProtocol();
	}

	 /**
	 * Implements ServletRequest method.
	 * 
	 * @return Scheme used by the request.
	 */
    public String getScheme() {
		return m_req.getScheme();
	}
	
	 /**
	 * Implements ServletRequest method.
	 * 
	 * @return Name of the server that received the request.
	 */
    public String getServerName() {
		return m_req.getServerName();
	}

	 /**
	 * Implements ServletRequest method.
	 * 
	 * @return Port of the server that received the request.
	 */
    public int getServerPort() {
		return m_req.getServerPort();
	}

	 /**
	 * Implements ServletRequest method.
	 * 
	 * @return IP Address of the client that send the request.
	 */
    public String getRemoteAddr() {
		return m_req.getRemoteAddr();
	}

	 /**
	 * Implements ServletRequest method.
	 * 
	 * @return Name of the client host.
	 */
    public String getRemoteHost() {
		return m_req.getRemoteHost();
	}

	 /**
	 * Implements ServletRequest method.
	 * 
	 * @param path Virtual file system path.
	 * @return Real file system path.
	 */
    public String getRealPath(String path) {
		return m_req.getRealPath(path);
	}

	 /**
	 * Implements ServletRequest method.
	 * 
	 * @return Retrieves the input stream as a ServletInputStream object.
	 * @exception IOException Thrown if input stream could not accessed.
	 */
    public ServletInputStream getInputStream() 
        throws IOException {
		return m_req.getInputStream();
	}

	 /**
	 * Implements ServletRequest method.
	 * 
	 * @param name Name of the attribute.
	 * @return Value of a named server-specific attribute as an Object.
	 */
    public Object getAttribute(String name) {
		return m_req.getAttribute(name);
	}

	 /**
	 * Implements ServletRequest method.
	 * 
	 * @param name Name of the attribute.
	 * @param valut Value of the attibute.
	 */
    public void setAttribute(String name, Object object) {
		m_req.setAttribute(name, object);
	}

     /**
	 * Implements ServletRequest method.
	 * 
	 * @return The names of all attibutes.
	 */
    public Enumeration getAttributeNames(){
		return m_req.getAttributeNames();
	}
    
	
	 /**
	 * Implements ServletRequest method.
	 * 
	 * @param name Name of the Parameter.
	 * @return Values of a named parameter as an arry of String.
	 */
    public String[] getParameterValues(String name)	{
		return m_req.getParameterValues(name);
	}

	 /**
	 * Implements ServletRequest method.
	 * 
	 * @return Retrieves the input stream as a BufferedReader object.
	 * @exception IOException Thrown if reader could not accessed.
	 */
    public BufferedReader getReader () 
        throws IOException {
		return m_req.getReader();
	}
		
	 /**
	 * Implements ServletRequest method.
	 * 
	 * @return Retrieves the input stream as a BufferedReader object.
	 * @exception IOException  Thrown if input stream could not accessed .
	 */	
    public String getCharacterEncoding () {
		return m_req.getCharacterEncoding ();
	}
	
  /**
   * Implements ServletRequest method.
   * Returns the names of all the parameters as an Enumeration of 
   * Strings.  It returns an empty Enumeration if there are no parameters.
   *
   * @return The names of all the parameters as an Enumeration of Strings.
   */
  public Enumeration getParameterNames() {
    return m_parameters.keys();
  }

  /**
   * Returns the names of all the uploaded files as an Enumeration of 
   * Strings.  It returns an empty Enumeration if there are no uploaded 
   * files.  Each file name is the name specified by the form, not by 
   * the user.
   *
   * @return The names of all the uploaded files as an Enumeration of Strings.
   */
  public Enumeration getFileNames() {
    return m_files.keys();
  }

  /**
   * Returns the value of the named parameter as a String, or null if 
   * the parameter was not given.  The value is guaranteed to be in its 
   * normal, decoded form.  If the parameter has multiple values, only 
   * the last one is returned.
   *
   * @param name The name of the parameter.
   * @return The value of the parameter.
   */
  public String getParameter(String name) {
    try {
	  String param = m_req.getParameter(name);
	  if (param==null) {
          param = (String)m_parameters.get(name);
          if (param.equals("")) return null;
      }
      return param;
    }
    catch (Exception e) {
      return null;
    }
  }  
  
  /**
   * This method actually parses the request.  A subclass 
   * can override this method for a better optimized or differently
   * behaved implementation.
   *
   * @exception IOException If the uploaded content is larger than 
   * <tt>maxSize</tt> or there's a problem parsing the request.
   */
  protected void readRequest() throws IOException {
    // Check the content type to make sure it's "multipart/form-data"
    String type = m_req.getContentType();
   
    if (type == null || 
        !type.toLowerCase().startsWith("multipart/form-data")) {
      throw new IOException(C_REQUEST_NOMULTIPART);
    }

    // Check the content length to prevent denial of service attacks
    int length = m_req.getContentLength();
    if (length > m_maxSize) {
      throw new IOException("Posted content length of " + length + 
                            " exceeds limit of " + m_maxSize);
    }

    // Get the boundary string; it's included in the content type.
    // Should look something like "------------------------12012133613061"
    String boundary = extractBoundary(type);
    if (boundary == null) {
      throw new IOException(C_REQUEST_NOBOUNDARY);
    }

    // Construct the special input stream we'll read from
    CmsMultipartInputStreamHandler in =
      new CmsMultipartInputStreamHandler(m_req.getInputStream(), boundary, length);

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
   * @exception IOException If there's a problem reading or parsing the
   * request
   *
   * @see readParameter
   * @see readAndSaveFile
   */

  protected boolean readNextPart(CmsMultipartInputStreamHandler in,
                                 String boundary) throws IOException {
    // Read the first line, should look like this:
    // content-disposition: form-data; name="field1"; filename="file1.txt"
    String line = in.readLine();

    if (line == null) {
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
      if (line == null || line.length() > 0) {  // line should be empty
        throw new 
          IOException("Malformed line after content type: " + line);
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
      // This is a file
      String value = readAndSaveFile(in, boundary);
	  filecounter ++;
	  m_parameters.put("file" + filecounter, filename);
      m_parameters.put("file" + filecounter + ".content", value);

    }
    // there's more to read
    return false;  
  }

  int filecounter = 0;
  
  /**
   * A utility method that reads a single part of the multipart request 
   * that represents a parameter.  A subclass can override this method 
   * for a better optimized or differently behaved implementation.
   *
   * @param in The stream from which to read the parameter information
   * @param boundary The boundary signifying the end of this part
   * @return The parameter value
   * @exception IOException If there's a problem reading or parsing the 
   * request
   */
  protected String readParameter(CmsMultipartInputStreamHandler in,
                                 String boundary) throws IOException {
	
    StringBuffer sbuf = new StringBuffer();
    String line;

    while ((line = in.readLine()) != null) {
      if (line.startsWith(boundary)) break;
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
   * A utility method that reads a single part of the multipart request 
   * that represents a file. Unlike the method name it does NOT saves the file to disk.
   * The name is from the original O'Reilly implmentaton.
   * <p>
   * A subclass can override this method for a better optimized or 
   * differently behaved implementation.
   *
   * @param in The stream from which to read the file.
   * @param boundary The boundary signifying the end of this part.
   * @exception IOException If there's a problem reading or parsing the request.
   */
  protected String readAndSaveFile(CmsMultipartInputStreamHandler in,
                                 String boundary) throws IOException {
	 	  
	ByteArrayOutputStream out = new ByteArrayOutputStream(8 * 1024);
	byte[] bbuf = new byte[100 * 1024]; 
    int result;
    String line;
    long counter=0;
	
    // ServletInputStream.readLine() has the annoying habit of 
    // adding a \r\n to the end of the last line.  
    // Since we want a byte-for-byte transfer, we have to cut those chars.
    boolean rnflag = false;
	int l=0;
	
    while ((result = in.readLine(bbuf, 0, bbuf.length)) != -1) {
      // Check for boundary
	  l=l+result;
	  counter++;
      // quick pre-check
      if (result > 2 && bbuf[0] == '-' && bbuf[1] == '-'){ 
	    line = new String(bbuf, 0, result, "ISO-8859-1");
        if (line.startsWith(boundary)) break;
      }
      // Are we supposed to write \r\n for the last iteration?
      if (rnflag) {
        out.write('\r'); out.write('\n');
        rnflag = false;
      }
      // Write the buffer, postpone any ending \r\n
      if (result >= 2 && bbuf[result - 2] == '\r' && 
          bbuf[result - 1] == '\n') {
            // skip the last 2 chars
            out.write(bbuf, 0, result - 2); 
            // make a note to write them on the next iteration
            rnflag = true; 
      } else {
	     out.write(bbuf, 0, result);
      }
     }	
    out.flush();
	return new String(out.toString());	
  }

  /**
  * Extracts and returns the boundary token from a line.
  * 
  * @param Line with boundary from input stream.
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
  * Extracts and returns disposition info from a line, as a String array
  * with elements: disposition, name, filename.  Throws an IOException 
  * if the line is malformatted.
  *
  * @param line Line from input stream.
  * @return Array of string containing disposition information.
  * @exception IOException Throws an IOException if the line is malformatted.
  */
  
  private String[] extractDispositionInfo(String line)
      throws IOException {
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
      int slash =
        Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
      if (slash > -1) {
        filename = filename.substring(slash + 1);  // past last slash
      }
      if (filename.equals("")) filename = "unknown"; // sanity check
    }

    // Return a String array: disposition, name, filename
    retval[0] = disposition;
    retval[1] = name;
    retval[2] = filename;
    return retval;
  }

  /**
   * Extracts and returns the content type from a line, or null if the
   * line was empty.  
   * @param line Line from input stream.
   * @return Content type of the line.
   * @exception IOException Throws an IOException if the line is malformatted.
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
    } else if (line.length() != 0) { 
      // no content type, so should be empty
      throw new IOException("Malformed line after disposition: " + origline);
    }
    return contentType;
  }
}

