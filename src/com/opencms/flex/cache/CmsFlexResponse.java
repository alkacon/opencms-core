/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/cache/Attic/CmsFlexResponse.java,v $
 * Date   : $Date: 2003/05/13 12:44:54 $
 * Version: $Revision: 1.17 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package com.opencms.flex.cache;

import com.opencms.core.A_OpenCms;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wrapper class for a HttpServletResponse.<p>
 *
 * This class wrapps the standard HttpServletResponse so that it's output can be delivered to
 * the CmsFlexCache.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.17 $
 */
public class CmsFlexResponse extends HttpServletResponseWrapper {
    
    /** The wrapped ServletResponse */
    private HttpServletResponse m_res = null;    
    
    /** The cached entry that is constructed from this response */
    private CmsFlexCacheEntry m_cachedEntry = null;
    
    /** A special wrapper class for a ServletOutputStream */
    private CmsFlexResponse.CmsServletOutputStream m_out;
    
    /** The CmsFlexController for this response */
    private CmsFlexController m_controller = null;
        
    /** A printwriter that writes in the m_out stream */
    private java.io.PrintWriter m_writer = null;
    
    /** Indicates that the OutputStream m_out should write ONLY in the buffer */
    private boolean m_writeOnlyToBuffer;
    
    /** Indicates that parent stream is writing only in the buffer */
    private boolean m_parentWritesOnlyToBuffer;
    
    /** A list of include calls that origin from this page, i.e. these are sub elements of this element */
    private List m_includeList = null;
    
    /** A list of parameters that belong to the include calls */
    private List m_includeListParameters = null;
    
    /** A list of results from the inclusions, needed because of JSP buffering */
    private List m_includeResults = null;
    
    /** Byte array used for "cached leafs" optimization */
    private byte[] m_cacheBytes = null;
    
    /** The CmsFlexCacheKey for this response */
    private CmsFlexCacheKey m_key = null;

    /** Map to save all response headers (including sub-elements) in */
    private Map m_headers;
    
    /** Map to save response headers belonging to a single include call in */
    private Map m_buffer_headers;

    /** Indicates if this response is suspended (probably because of a redirect) */
    private boolean m_suspended = false;

    /** String to hold a buffered redirect target */
    private String m_buffer_redirect = null;        

    /** Indicates if caching is required, will always be true if m_writeOnlyToBuffer is true */
    private boolean m_cachingRequired = false;       
    
    /** Indicates if this element is currently in include mode, i.e. processing a sub-element */
    private boolean m_includeMode = false;
    
    /** Static string to indicate a header is "set" in the header maps */
    public static final String C_SETHEADER = "[setHeader]";
                
    /** Flag for debugging output */
    private static final boolean DEBUG = false;
    
    /** The cache delimiter char */
    public static final char C_FLEX_CACHE_DELIMITER = (char)0;
    
    /** The encoding to use for the response */
    private String m_encoding;
    
    /** Flag to indicate if this is the top level element or an included sub - element */ 
    private boolean m_isTopElement;     
    
    /** 
     * Constructor for the CmsFlexResponse,
     * this variation is usually used for the "Top" response.<p>
     *
     * @param res the HttpServletResponse to wrap
     * @param controller the controller to use
     * @param streaming indicates if streaming should be enabled or not
     * @param isTopElement indicates if this is the top element of an include cascade
     */
    public CmsFlexResponse(HttpServletResponse res, CmsFlexController controller, boolean streaming, boolean isTopElement) {
        super(res);
        m_res = res;
        m_controller = controller;
        m_out = null;
        m_encoding = controller.getCmsObject().getRequestContext().getEncoding();
        m_isTopElement = isTopElement;
        m_parentWritesOnlyToBuffer = ! streaming;
        setOnlyBuffering(m_parentWritesOnlyToBuffer);
        m_headers = new java.util.HashMap(37);
        m_buffer_headers = new java.util.HashMap(17);        
    }  
    
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
        m_out = null;
        m_encoding = controller.getCurrentResponse().getEncoding();
        m_isTopElement = controller.getCurrentResponse().isTopElement();        
        m_parentWritesOnlyToBuffer = controller.getCurrentResponse().hasIncludeList();
        setOnlyBuffering(m_parentWritesOnlyToBuffer);
        m_headers = new java.util.HashMap(37);
        m_buffer_headers = new java.util.HashMap(17);
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
     * Sets buffering status of the response.<p>
     * 
     * This must be done before the first output is written.
     * Buffering is needed to process elements that can not be written
     * directly to the output stream because their sub - elements have to
     * be processed seperatly. Which is so far true only for JSP pages.<p>
     *
     * If buffering is on, nothing is written to the output stream
     * even if streaming for this resonse is enabled.<p>
     *
     * @param value the value to set
     */
    public void setOnlyBuffering(boolean value) {
        m_writeOnlyToBuffer = value;
        
        if (m_writeOnlyToBuffer) {
            setCmsCachingRequired(true);
        }      
    }

    /**
     * Set caching status for this reponse.<p>
     * 
     * Will always be set to "true" if setOnlyBuffering() is set to "true".
     * Currently this is an optimization for non - JSP elements that 
     * are known not to be cachable.<p>
     *
     * @param value the value to set     
     */
    void setCmsCachingRequired(boolean value) {  
        m_cachingRequired = value || m_writeOnlyToBuffer;
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
     * This flag indicates if the response is suspended or not.<p>
     * 
     * A suspended response mut not write further output to any stream or
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
     * Sets the suspended status of the response, and also sets
     * the suspend status of all responses wrapping this response.<p>
     * 
     * A suspended response mut not write further output to any stream or
     * process a cache entry for itself.<p>
     *
     * @param value the value to set     
     */
    void setSuspended(boolean value) {
        m_suspended = value;
    }
        
    /**
     * Provides access to the header cache of the top wrapper.<p>
     *
     * @return the Map of cached headers
     */
    public Map getHeaders() {
        return m_headers;
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
            m_includeResults = new ArrayList(10);
        }
        m_includeResults.add(result);
    }
        
    
    /** 
     * Adds an inclusion target to the list of include results.<p>
     * 
     * Should be used only in inclusion-scenarios
     * like the JSP cms:include tag processing.<p>
     *
     * @param target the include target name to add
     */
    public void addToIncludeList(String target, Map parameterMap) {
        if (m_includeList == null) {
            m_includeList = new ArrayList(10);
            m_includeListParameters = new ArrayList(10);
        }        
        m_includeListParameters.add(parameterMap);
        m_includeList.add(target);
    }
    
    /**
     * Is used to check if the response has an include list, 
     * which indicates a) it is probalbly processing a JSP element 
     * and b) it can never be streamed and alwys must be buffered.<p>
     *
     * @return true if this response has an include list, false otherwise
     */
    boolean hasIncludeList() {
        return m_includeList != null;
    }    

    /**
     * This method is needed to process pages that can NOT be analyzed
     * directly during delivering (like JSP) because they write to 
     * their own buffer.<p>
     *
     * So in this case, we don't actually write output of
     * include calls to the stream.
     * Where there are include calls we write a C_FLEX_CACHE_DELIMITER char on the stream to indicate
     * that at this point the output of the include must be placed later.
     * The include targets (resource names) are then saved in the m_includeList.<p>
     *
     * This method must be called after the complete page has been processed.
     * It will contain the output of the page only (no includes), 
     * with C_FLEX_CACHE_DELIMITER chars were the include calls should be placed. 
     * What we do here is analyze the output and cut it in parts 
     * of byte[] arrays which then are saved in the resulting cache entry.
     * For the includes, we just save the name of the resource in
     * the cache entry.<p>
     *  
     * If caching is disabled this method is just not called.<p>
     */
    private void processIncludeList() {
        byte[] result = getWriterBytes();            
        if (! hasIncludeList()) {
            // No include list, so no includes and we just use the bytes as they are in one block
            m_cachedEntry.add(result);
        } else {
            // Process the include list
            int max = result.length;
            int pos = 0;
            int last = 0;
            int size = 0;
            int count = 0;
            // Work through result and split this with include list calls
            java.util.Iterator i = m_includeList.iterator();
            java.util.Iterator j = m_includeListParameters.iterator();
            while (i.hasNext() && (pos<max)) {
                // Look for the first C_FLEX_CACHE_DELIMITER char
                while ((pos<max) && (result[pos] != C_FLEX_CACHE_DELIMITER)) {
                    pos++;
                }
                if (result[pos] == C_FLEX_CACHE_DELIMITER) {      
                    count++;
                    // A byte value of C_FLEX_CACHE_DELIMITER in our (string) output list indicates that the next include call
                    // must be placed here
                    size = pos - last;
                    if (size > 0) {
                        // If not (it might be 0) there would be 2 include calls back 2 back
                        byte[] piece = new byte[size];
                        System.arraycopy(result, last, piece, 0, size);
                        // Add the byte array to the cache entry
                        m_cachedEntry.add(piece);
                    }
                    last = ++pos;
                    // Add an include call to the cache entry
                    m_cachedEntry.add((String)i.next(), (java.util.HashMap)j.next());
                }
            } 
            // Is there something behind the last include call?
            if (pos<max) {
                // Yes!
                size = max - pos;
                byte[] piece = new byte[size];
                System.arraycopy(result, pos, piece, 0, size);                
                m_cachedEntry.add(piece);
            }           
            if (! i.hasNext()) {
                // Delete the include list if all include calls are handled
                m_includeList = null;
                m_includeListParameters = null;
            } else {
                // If something is left, remove the processed entries
                m_includeList = m_includeList.subList(count, m_includeList.size());
                m_includeListParameters = m_includeListParameters.subList(count, m_includeListParameters.size());
            }
        }
    }
    
    /** 
     * This delivers cached sub-elements back to the stream.
     * Needed to overcome JSP buffering.<p>
     *
     * @param res the response to write the cached results to
     * @throws IOException in case something goes wrong writing to the responses output stream
     */
    private void writeCachedResultToStream(HttpServletResponse res) throws IOException {        
        java.util.List elements = m_cachedEntry.elements();
        int count = 0;
        if (elements != null) {
            java.util.Iterator i = elements.iterator();
            while (i.hasNext()) {
                Object o = i.next();                
                if (o instanceof byte[]) {
                    res.getOutputStream().write((byte[])o);
                } else {
                    if ((m_includeResults != null) && (m_includeResults.size() > count)) {
                        // Make sure that we don't run behind end of list (should never happen, though)
                        res.getOutputStream().write((byte[])m_includeResults.get(count));
                        count++;
                    }
                    // Skip next entry, which is the parameter list for this incluce call
                    o = i.next();
                }
            }
        }
    }
    
    /**
     * Generates a CmsFlexCacheEntry from the current response using the 
     * stored include results.<p>
     * 
     * In case the results were written only to the buffer until now, 
     * they are now re-written on the output stream, with all included 
     * elements.<p>
     *
     * @throws IOException tn case something goes wrong while writing to the output stream
     * @return  the generated cache entry
     */    
    CmsFlexCacheEntry processCacheEntry() throws IOException {    
        if (isSuspended() && (m_buffer_redirect == null)) 
            // An included element redirected this response, no cache entry must be produced
            return null;
        
        if (m_cachingRequired) {
            // Cache entry must only be calculated if it's actually needed (always true if we write only to buffer)
            m_cachedEntry = new CmsFlexCacheEntry();    
            if (m_buffer_redirect != null) {
                // Only set et cached redirect target
                m_cachedEntry.setRedirect(m_buffer_redirect);
            } else {
                // Add cached headers
                m_cachedEntry.addHeaders(m_buffer_headers);
                // Add cached output 
                if (m_includeList != null) {
                    // Probably JSP: We must analyze out stream for includes calls
                    // Also, m_writeOnlyToBuffer must be "true" or m_includeList can not be != null
                    processIncludeList();   
                } else {
                    // Output is delivered directly, no include call parsing required
                    m_cachedEntry.add(getWriterBytes());
                }
            }
            m_cachedEntry.complete();
        }        
        // In case the output was only bufferd we have to re-write it to the "right" stream       
        if (m_writeOnlyToBuffer) {
            
            // Since we are processing a cache entry caching is not required
            m_cachingRequired = false;    
                        
            if (m_buffer_redirect != null) {
                // Send buffered redirect, will trigger redirect of top response
                sendRedirect(m_buffer_redirect);
            } else {  
                // Process the output               
                if (m_parentWritesOnlyToBuffer) {                                               
                    // Write results back to own stream, headers are already in buffer
                    if (m_out != null) {
                        try {
                            m_out.clear(); 
                        } catch (Exception e) {
                            if (DEBUG) System.err.println("FlexResponse: caught exception while calling m_out.clear() in processCacheEntry()\nException: " + e);                            
                        }
                    } else {
                        if (DEBUG) System.err.println("FlexResponse: m_out == null in processCacheEntry()");
                    }
                    writeCachedResultToStream(this);                 
                } else {
                    // We can use the parent stream
                    processHeaders(m_headers, m_res);
                    writeCachedResultToStream(m_res);  
                }
            }
        }        
        return m_cachedEntry;
    }
    
    /**
     * Returns the bytes that have been written on the current writers output stream.<p>
     *
     * @return the bytes that have been written on the current writers output stream
     */    
    public byte[] getWriterBytes() {
        if (isSuspended()) 
            // No output whatsoever if the response is suspended
            return new byte[0];
        if (m_cacheBytes != null)
            // Optimization for cached "leaf" nodes, here I re-use the array from the cache
            return m_cacheBytes;
        if (m_out == null) 
            // No output was written so far, just return an empty array
            return new byte[0];
        if (m_writer != null) 
            // Flush the writer in case something was written on it
            m_writer.flush();
        return m_out.getBytes();
    }
    
    /**
     * Initializes the current responses output stream 
     * and the corrosponding print writer.<p>
     *
     * @throws IOException in case something goes wrong while initializing
     */    
    private void initStream() throws IOException {        
        if (m_out == null) {
            if (! m_writeOnlyToBuffer) {
                // We can use the parents output stream
                if (m_cachingRequired || (m_controller.getResponseQueueSize() > 1)) {
                    // We are allowed to cache our results (probably to contruct a new cache entry)
                    m_out = new CmsFlexResponse.CmsServletOutputStream(m_res.getOutputStream());        
                } else {
                    // We are not allowed to cache so we just use the parents output stream
                    m_out = (CmsFlexResponse.CmsServletOutputStream)m_res.getOutputStream();
                }
            } else {
                // Construct a "buffer only" output stream
                m_out = new CmsFlexResponse.CmsServletOutputStream();
            }
        }
        if (m_writer == null) {
            // Encoding project:
            // Create a PrintWriter that uses the OpenCms default encoding
            m_writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(m_out, A_OpenCms.getDefaultEncoding())), false);
        }
    }
    
    /** 
     * Writes some bytes to the current output stream,
     * this method should be called from CmsFlexCacheEntry.service() only.<p>
     *
     * @param bytes an array of bytes
     * @param useArray indicates that the byte array should be used directly
     * @throws IOException in case something goes wrong while writing to the stream
     */
    void writeToOutputStream(byte[] bytes, boolean useArray) throws IOException {
        if (isSuspended()) return;        
        if (m_writeOnlyToBuffer) {
            if (useArray) {
                // This cached entry has no sub-elements (it a "leaf") and so we can just use it's bytes
                m_cacheBytes = bytes;                                  
            } else {
                if (m_out == null) initStream();
                // In this case the buffer will not write to the servlet stream, but to it's internal buffer only
                m_out.write(bytes);
            }
        } else {
            if (DEBUG) System.err.println("FlexResponse.writeToOutputStream(): Writing directly to wrapped output stream!");
            // The request is not buffered, so we can write directly to it's parents output stream 
            m_res.getOutputStream().write(bytes);
            m_res.getOutputStream().flush();
        }    
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
     * Sets the cache key for this response, which is calculated
     * from the provided parameters.<p>
     *
     * @param target the target resouce for which to create the cache key
     * @param value the value of the cache property of the resource
     * @param online indicates if this resource is online or offline
     * @return the generated cache key
     * @throws CmsException in case the value String had a parse error
     */
    CmsFlexCacheKey setCmsCacheKey(String target, String value, boolean online) throws com.opencms.core.CmsException {
        m_key = new CmsFlexCacheKey(target, value, online);
        if (m_key.hadParseError()) {
            // We throw the exception here to make sure this response has a valid key (cache=never)
            throw new com.opencms.core.CmsException(com.opencms.core.CmsException.C_FLEX_CACHE);            
        }
        return m_key;
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
     * Helper method to add a value in the internal header list.<p>
     *
     * @param headers the headers to look up the value in
     * @param name the name to look up
     * @param value the value to set
     */
    private void addHeaderList(Map headers, String name, String value) {
        ArrayList values = (ArrayList) headers.get(name);
        if (values == null) {
            values = new ArrayList();
            headers.put(name, values);            
        } 
        values.add(value);        
    }
   
    /**
     * Helper method to set a value in the internal header list.
     *
     * @param headers the headers to set the value in
     * @param name the name to set
     * @param value the value to set
     */
    private void setHeaderList(Map headers, String name, String value) {
        ArrayList values = new ArrayList();
        values.add(C_SETHEADER + value);
        headers.put(name, values);        
    }
    
    /**
     * Method overlodad from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.ServletResponse#getWriter()
     */
    public java.io.PrintWriter getWriter() throws IOException {
        if (m_writer == null) initStream();
        return m_writer;
    }
    
    /**
     * Method overlodad from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    public javax.servlet.ServletOutputStream getOutputStream() throws IOException {
        if (m_out == null) initStream();
        return m_out;
    }

    /**
     * Method overlodad from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
     */
    public void sendRedirect(String location) throws IOException {
        // Ignore any redirects after the first one
        if (isSuspended() && (! location.equals(m_buffer_redirect))) return;
        if (DEBUG) System.err.println("FlexResponse: sendRedirect to target " + location);
                
        if (m_cachingRequired && ! m_includeMode) {
            m_buffer_redirect = location;
        }
        
        if (! m_cachingRequired) {
            // If caching is required a cached entry will be constructed first and redirect will
            // be called after this is completed and stored in the cache
            if (DEBUG) System.err.println("FlexResponse: getTopResponse.sendRedirect() to target " + location);
            
            m_controller.getTopResponse().sendRedirect(location);        
        }
        
        m_controller.suspendFlexResponse();
    }
    
    /**
     * Process the headers stored in the provided map and add them to the response.<p>
     * 
     * @param headers the headers to add
     * @param res the resonse to add the headers to
     */
    public static void processHeaders(Map headers, HttpServletResponse res) {
        if (headers != null) {
            java.util.Iterator i = headers.keySet().iterator();
            while (i.hasNext()) {
                String key = (String)i.next();
                ArrayList l = (ArrayList)headers.get(key);  
                java.util.ListIterator j = l.listIterator(); 
                while (j.hasNext()) {
                    if ((j.nextIndex() == 0) && (((String)l.get(0)).startsWith(C_SETHEADER)))  {
                        String s = (String)j.next();
                        res.setHeader(key, s.substring(C_SETHEADER.length()));
                    } else {
                        res.addHeader(key, (String)j.next());
                    }
                }
            }        
        }          
    }
    
    /**
     * Method overlodad from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
     */
    public void setHeader(String name, String value) {
        if (isSuspended()) return;

        if (m_cachingRequired && ! m_includeMode) {
            setHeaderList(m_buffer_headers, name, value);
            if (DEBUG) System.err.println("FlexResponse: setHeader(" + name + ", " + value + ") in element buffer");
        }
        
        if (m_writeOnlyToBuffer) {
            setHeaderList(m_headers, name, value);
            if (DEBUG) System.err.println("FlexResponse: setHeader(" + name + ", " + value + ") in main header buffer");
        } else {
            if (DEBUG) System.err.println("FlexResponse: setHeader(" + name + ", " + value + ") passing to parent");
            m_res.setHeader(name, value);
        }
    }

    /**
     * Method overlodad from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
     */   
    public void addHeader(String name, String value) {
        if (isSuspended()) return;

        if (m_cachingRequired && ! m_includeMode) {
            addHeaderList(m_buffer_headers, name, value);
            if (DEBUG) System.err.println("FlexResponse: addHeader(" + name + ", " + value + ") to element buffer");
        }
        
        if (m_writeOnlyToBuffer) {
            addHeaderList(m_headers, name, value);
            if (DEBUG) System.err.println("FlexResponse: addHeader(" + name + ", " + value + ") to main header buffer");
        } else {
            if (DEBUG) System.err.println("FlexResponse: addHeader(" + name + ", " + value + ") passing to parent");
            m_res.addHeader(name, value);
        }
    }
    
    /**
     * Method overlodad from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
     */
    public void setDateHeader(String name, long date) {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);
        setHeader(name, format.format(new java.util.Date(date)));
    }

    /**
     * Method overlodad from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
     */
    public void addDateHeader(String name, long date) {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);
        addHeader(name, format.format(new java.util.Date(date)));
    }

    /**
     * Method overlodad from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
     */   
    public void setIntHeader(String name, int value) {
        setHeader(name, "" + value);       
    }

    /**
     * Method overlodad from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
     */    
    public void addIntHeader(String name, int value) {
        addHeader(name, "" + value);
    }    

    /**
     * Method overlodad from the standard HttpServletRequest API.<p>
     *
     * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
     */
    public void setContentType(String type) {
        if (DEBUG) System.err.println("FlexResponse: setContentType(" +type + ") called");
        // TODO: Check setContentType() implementation
        // If this is not the "Top-Level" element ignore all settings of content type    
        // If this is not done an included JSP could reset the type with some unwanted defaults    
        if (! m_isTopElement) return;
        /*       
        if (type != null) {
            // ensure that the encoding set by OpenCms is not overwritten by the default form the JSP            
            type = type.toLowerCase();
            int i = type.indexOf("charset");
            if (type.startsWith("text") && (i > 0)) {
                StringBuffer buf = new StringBuffer();
                buf.append(type.substring(0, i));
                buf.append("charset=");
                buf.append(m_encoding);
                type = new String(buf);
                if (DEBUG) System.err.println("FlexResponse: setContentType() changed type to " +type);
            }            
        }
        m_res.setContentType(type);
        */        
    }
    
    /**
     * Wrapped implementation of the ServletOutputStream.<p>
     * 
     * This implementation writes to an internal buffer and optionally to another 
     * output stream at the same time.
     * It should be fully transparent to the standard ServletOutputStream.<p>
     */    
    private class CmsServletOutputStream extends ServletOutputStream {

        /** The internal steam buffer */
        private java.io.ByteArrayOutputStream stream = null;
        
        /** The optional output stream to write to */
        private javax.servlet.ServletOutputStream servletStream = null;
        
        /** Debug flag */
        private static final boolean DEBUG = false;

        /**
         * Constructor that must be used if the stream should write 
         * only to a buffer.<p>
         */
        public CmsServletOutputStream() {
            this.servletStream = null;
            clear();
        }   

        /**
         * Constructor that must be used if the stream should write 
         * to a buffer and to another stream at the same time.<p>
         *
         * @param servletStream The stream to write to
         */        
        public CmsServletOutputStream(ServletOutputStream servletStream) {
            this.servletStream = servletStream;
            clear();                  
        }   
                
        /**
         * @see java.io.OutputStream#write(int)
         */
        public void write(int b) throws IOException {
            stream.write(b);
            if (servletStream != null) servletStream.write(b);
        }
        
        /**
         * @see java.io.OutputStream#write(byte[], int, int)
         */
        public void write(byte[] b, int off, int len) throws IOException {
            stream.write(b, off, len);
            if (servletStream != null) servletStream.write(b, off, len);
        }
        
        /**
         * Writes an array of bytes only to the included servlet stream,
         * not to the buffer.<p>
         *
         * @param b The bytes to write to the stream
         * @throws IOException In case the write() operation on the included servlet stream raises one
         */
        public void writeToServletStream(byte[] b) throws IOException {
            if (servletStream != null) servletStream.write(b);
        }
        
        /**
         * @see java.io.OutputStream#flush()
         */
        public void flush() throws IOException {
            if (DEBUG) System.err.println("CmsServletOutputStream: flush() called! servletStream=" + servletStream);
            if (servletStream != null) servletStream.flush();
        }
                
        /**
         * Provides access to the bytes cached in the buffer.<p>
         *
         * @return the cached bytes from the buffer
         */
        public byte[] getBytes() {
            return stream.toByteArray();
        }
        
        /**
         * Clears the buffer by initializing the buffer with a new stream.<p>
         */
        public void clear() {
            stream = new java.io.ByteArrayOutputStream(1024);
        }        
    }    
    
}
