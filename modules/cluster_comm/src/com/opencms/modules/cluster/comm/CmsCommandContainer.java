package com.opencms.modules.cluster.comm;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * A instance of this class is a container for several command that should be
 * called on a remote opencms.
 */
public class CmsCommandContainer implements Serializable {

    /** The username to use for all following commands in this container */
    protected String m_username = null;

    /** The password to use for all following commands in this container */
    protected String m_password = null;

    /** Indicates, if all results (return values) should be waited for */
    protected boolean m_waitForResult = false;

    /** A list of commands that should be started on the remote system */
    protected ArrayList m_commands = new ArrayList();

    /**
     * The default constructor
     */
    public CmsCommandContainer() {
    }

    /**
     * Constructs a new container
     * @param username The name of the user to log in on the remote system.
     * @param password The password of the user to log in on the remote system.
     */
    public CmsCommandContainer(String username, String password) {
        this(username, password, false);
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append(getUsername() + ",")
           .append("***,")
           .append(getWaitForResult() + ",{");
        ArrayList list = getCommands();
        for(int i = 0; i < list.size(); i++) {
            ret.append(list.get(i).toString());
        }
        ret.append("}");
        return ret.toString();
    }

    /**
     * Serializes this Command Container.
     */
    protected void serializeThis(OutputStream out) throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(out);
        stream.writeObject(this);
        stream.flush();
        stream.close();
    }

    public String sendCommand(URL url) throws IOException {
        URLConnection httpConn = url.openConnection();
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setUseCaches(false);
        httpConn.setRequestProperty("Content-Type","application/octet-stream");
        DataOutputStream out = new DataOutputStream(httpConn.getOutputStream());

        serializeThis(out);

        out.flush();
        out.close();

        return httpConn.getHeaderField(0);
    }

    /**
     * Constructs a new container
     * @param username The name of the user to log in on the remote system.
     * @param password The password of the user to log in on the remote system.
     * @param waitForResult Indicates, if all results (return values) should be
     * waited for
     */
    public CmsCommandContainer(String username, String password, boolean waitForResult) {
        setUsername(username);
        setPassword(password);
        setWaitForResult(waitForResult);
    }

    public void addCommand(CmsCommand command) {
        m_commands.add(command);
    }

    public ArrayList getCommands() {
        return m_commands;
    }

    public void setUsername(String value) {
        m_username = value;
    }

    public void setPassword(String value) {
        m_password = value;
    }

    public void setWaitForResult(boolean value) {
        m_waitForResult = value;
    }

    public String getUsername() {
        return m_username;
    }

    public String getPassword() {
        return m_password;
    }

    boolean getWaitForResult() {
        return m_waitForResult;
    }
}