package com.opencms.core;

import java.util.*; 

/**
 * This interface defines a CmsRequest.
 * 
 * The CmsRequest is a genereic request object that is used in the CmsObject provinding
 * methods to read the data included in the request.
 * 
 * Implementations of this interface use an existing request (e.g. HttpServletRequest) to
 * initialize a CmsRequest. 
 * 
 * @author Michael Emmerich
 * @author Alexander Kandzior
 * @version $Revision: 1.1 $ $Date: 2000/01/12 16:38:14 $  
 */
public interface I_CmsRequest { 
    

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
    public String getRequestedResource();
    
    /**
     * Returns the value of a named parameter as a String.
     * Returns null if the parameter does not exist or an empty string if the parameter
     * exists but without a value.
     * 
     * @param name The name of the parameter.
     * @returns The value of the parameter.
     */
    public String getParameter(String name);
                                
    /**
     * Returns all parameter names as an Enumeration of String objects.
     * Returns an empty Enumeratrion if no parameters were included in the request.
     * 
     * @return Enumeration of parameter names.
     */
    public Enumeration getParameterNames();

    /**
     * Returns the content of an uploaded file.
     * Returns null if no file with this name has been uploaded with this request.
     * Returns an empty byte[] if a file without content has been uploaded.
     * 
     * @param name The name of the uploaded file.
     * @return The selected uploaded file content.
     */
    public byte[] getFile(String name);   
    
    /**
     * Returns the names of all uploaded files in this request.
     * Returns an empty eumeration if no files were included in the request.
     * 
     * @return An Enumeration of file names.
     */
    public Enumeration getFileNames();

    /**
     * Returns the type of the request that was used to create the CmsRequest.
     * The returned int must be one of the constants defined above in this interface.
     * 
     * @return The type of the CmsRequest.
     */
    public int getOriginalRequestType();

    /**
     * Returns the original request that was used to create the CmsRequest.
     * 
     * @return The original request of the CmsRequest.
     */
    public Object getOriginalRequest();
}
