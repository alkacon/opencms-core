package com.opencms.core;

import java.util.*; 
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Implementation of the CmsRequest interface.
 * 
 * This implementation uses a HttpServletRequest as original request to create a
 * CmsRequestHttpServlet. This either can be a normal HttpServletRequest or a
 * CmsMultipartRequest which is used to upload file into the OpenCms.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/01/12 16:38:14 $  
 */
public class CmsRequestHttpServlet implements I_CmsConstants,     
                                              I_CmsRequest { 
    
    /**
     * The original request.
     */
    private HttpServletRequest m_req;
    
    /**
     * The type of theis CmsRequest.
     */
    private int m_type=C_REQUEST_HTTP;
    
    
    /** 
     * Constructor, creates a new CmsRequestHttpServlet object.
     * 
     * @param req The original HttpServletRequest used to create this CmsRequest.
     */
    public CmsRequestHttpServlet(HttpServletRequest req){
        m_req=req;
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
        return m_req.getPathInfo();
     }
    
    /**
     * Returns the value of a named parameter as a String.
     * Returns null if the parameter does not exist or an empty string if the parameter
     * exists but without a value.
     * 
     * @param name The name of the parameter.
     * @returns The value of the parameter.
     */
    public String getParameter(String name) {
        return m_req.getParameter(name);
    }
                                
    /**
     * Returns all parameter names as an Enumeration of String objects.
     * Returns an empty Enumeratrion if no parameters were included in the request.
     * 
     * @return Enumeration of parameter names.
     */
    public Enumeration getParameterNames() {
        return m_req.getParameterNames();
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
        byte[] content=null;
        // check if the HttpServletRequest was a CmsMultipartRequest
        if (m_req instanceof CmsMultipartRequest){
            content = getParameter(name).getBytes();
        }
        return content;
    }
    
    /**
     * Returns the names of all uploaded files in this request.
     * Returns an empty eumeration if no files were included in the request.
     * 
     * @return An Enumeration of file names.
     */
    public Enumeration getFileNames() {
        Enumeration names=null;
        // check if the HttpServletRequest was a CmsMultipartRequest
        if (m_req instanceof CmsMultipartRequest){
            ((CmsMultipartRequest)m_req).getFileNames();
        }
        return names;
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
     * Returns the original request that was used to create the CmsRequest.
     * 
     * @return The original request of the CmsRequest.
     */
    public Object getOriginalRequest() {
        return m_req;
    }
}
