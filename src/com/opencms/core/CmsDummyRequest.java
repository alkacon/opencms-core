package com.opencms.core;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class CmsDummyRequest implements I_CmsRequest {


    /**
     * The original request.
     */
    private HttpServletRequest m_req;

    /**
     * The resource requested.
     */
    private String m_resourcePath;

    public CmsDummyRequest(HttpServletRequest req) {
        m_req = req;
    }
    public byte[] getFile(String name) {
        return null;
    }
    public Enumeration getFileNames() {
        Enumeration enu = (new Vector()).elements();
        return enu;
    }
    public Object getOriginalRequest() {

        return m_req;
    }
    public int getOriginalRequestType() {
        /**@todo: Implement this com.opencms.core.I_CmsRequest method*/
        return 0;
    }
    public String getParameter(String name) {
        return null;
    }
    public Enumeration getParameterNames() {
        Enumeration enu = (new Vector()).elements();
        return enu;
    }
    public String[] getParameterValues(String key) {
        return null;
    }
    public String getRequestedResource() {
        return m_resourcePath;
    }
    public void setRequestedResource(String res){
        m_resourcePath = res;
    }
}