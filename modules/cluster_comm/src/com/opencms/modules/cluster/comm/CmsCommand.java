package com.opencms.modules.cluster.comm;

import java.util.*;
import java.io.*;

/**
 * A instance of this class represents a single command (method) that should be
 * invoked on the cms-object.
 */
public class CmsCommand implements Serializable{

    /** The methos that should be called */
    protected String m_methodName = null;

    /** A list of parameters for the method to call,
     *  NOTE: All parameters must be serializeable !*/
    protected Object[] m_parameters = new Object[] {};

    /**
     * The default constructor.
     */
    public CmsCommand() {
    }

    /**
     * Constructs a command with methodName and parameters.
     * @param methodName the name of the method to invoke.
     * @param parameters the parameters to invoke the methos with.
     */
    public CmsCommand(String methodName, Object[] parameters) {
        setMethodName(methodName);
        setParameters(parameters);
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append(getMethodName() + "(");
        Object[] params = getParameters();
        for(int i = 0; i < params.length; i++) {
            if(i!=0) {ret.append(',');}
            ret.append(params[i].toString());
        }
        ret.append(");");
        return ret.toString();
    }

    public void setMethodName(String value) {
        m_methodName = value;
    }

    public void setParameters(Object[] value) {
        m_parameters = value;
    }

    public String getMethodName() {
        return m_methodName;
    }

    public Object[] getParameters() {
        return m_parameters;
    }
}