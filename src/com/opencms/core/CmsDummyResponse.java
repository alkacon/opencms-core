package com.opencms.core;

import java.io.*;


public class CmsDummyResponse implements I_CmsResponse {

    /**
     * The OutputStream to the discfile.
     */
    private OutputStream m_outStream;

    /**
     *
     */
    private boolean m_outputWritten = false;

    public CmsDummyResponse() {

    }

    public Object getOriginalResponse() {
        return null;
    }

    public int getOriginalResponseType() {
        return 0;
    }

    public void putOutputStream(OutputStream outStream){
        m_outStream = outStream;
    }

    public OutputStream getOutputStream() throws IOException {
        m_outputWritten  = true;
        return m_outStream;
    }

    /**
     * Check if the output stream was requested previously.
     * @return <code>true</code> if getOutputStream() was called, <code>false</code> otherwise.
     */
    public boolean isOutputWritten() {
        return m_outputWritten;
    }

    public boolean isRedirected() {
        return false;
    }

    public void sendCmsRedirect(String location) throws IOException {
    }

    public void sendError(int code) throws IOException {
    }

    public void sendError(int code, String msg) throws IOException {
    }

    public void sendRedirect(String location) throws IOException {
    }

    public void setContentLength(int len) {
    }

    public void setContentType(String type) {
    }

    public void setHeader(String key, String value) {
    }

    public void addHeader(String key, String value) {
    }

    public void setLastModified(long time) {
    }

    public boolean containsHeader(String key) {
        return false;
    }
}