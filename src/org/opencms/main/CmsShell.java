/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsShell.java,v $
 * Date   : $Date: 2004/02/14 15:27:38 $
 * Version: $Revision: 1.16 $
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

package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.setup.CmsSetupUtils;
import org.opencms.util.CmsStringSubstitution;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.ExtendedProperties;

/**
 * A commad line interface to OpenCms which 
 * is used for the initial setup and also can be used to directly access the OpenCms
 * repository without the Workplace.<p>
 * 
 * The CmsShell has direct access to all public methods in 
 * the {@link org.opencms.file.CmsObject} and the {@link org.opencms.main.CmsShellCommands}
 * that have only "primitive" data types as parameters.
 * Supported primitive types are <code>String, CmsUUID, boolean, int, long, double, float</code>.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.16 $
 */
public class CmsShell {

    /** The OpenCms context object */
    private CmsObject m_cms;

    /** All shell callable methods in the CmsObject class */   
    private Map m_cmsObjectMethods;
    
    /** All shell callable methods in the CmsShellCommands class */
    private Map m_cmsShellMethods;

    /** If set to true, all commands are echoed */
    private boolean m_echo;

    /** Indicates if the 'exit' command has been called */
    private boolean m_exitCalled;

    /** The OpenCms system object */
    private OpenCmsCore m_opencms;

    /** The shell prompt format */
    private String m_prompt;
    
    /** Internal shell command object */
    private CmsShellCommands m_shellCommands;

    /**
     * Creates a new CmsShell.<p>
     * 
     * @param fileInputStream a (file) input stream from which commands are read
     * @param prompt the prompt format to set
     * @param webInfPath the path to the 'WEB-INF' folder of the OpenCms installation
     */
    public CmsShell(String webInfPath, String prompt, FileInputStream fileInputStream) {
        setPrompt(prompt);
        
        System.out.println();         
        System.out.println("Welcome to the OpenCms shell!");            
        System.out.println();  
        
        try {
            // first initialize runlevel 1 
            m_opencms = OpenCmsCore.getInstance();
            // search for the WEB-INF folder
            if (webInfPath == null || "".equals(webInfPath)) {
                System.err.println("No OpenCms home folder given. Trying to guess...");
                System.out.println();         
                webInfPath = m_opencms.searchWebInfFolder(System.getProperty("user.dir"));
                if (webInfPath == null || "".equals(webInfPath)) {
                    System.err.println("-----------------------------------------------------------------------");
                    System.err.println("The OpenCms 'WEB-INF' folder can not be found.");
                    System.err.println();
                    System.err.println("Please start the OpenCms shell from the 'WEB-INF' directory of your");
                    System.err.println("OpenCms installation, or pass the OpenCms 'WEB-INF' folder as argument.");
                    System.err.println("-----------------------------------------------------------------------");
                    return;
                }
            }
            System.out.println("OpenCms WEB-INF path:  " + webInfPath);            
            // set the path to the WEB-INF folder
            m_opencms.getSystemInfo().setWebInfPath(webInfPath);
            
            // now read the configuration properties
            String propertyPath = m_opencms.getSystemInfo().getConfigurationFilePath();
            System.out.println("OpenCms property file: " + propertyPath);
            System.out.println();            
            ExtendedProperties configuration = CmsSetupUtils.loadProperties(propertyPath);
            
            // get all shell callable methods from the CmsObject
            m_cmsObjectMethods = getShellMethods(CmsObject.class);
            // get additional callable methods from the CmsShellCommands
            m_cmsShellMethods = getShellMethods(CmsShellCommands.class);
            
            // now upgrade to runlevel 2
            m_opencms = m_opencms.upgradeRunlevel(configuration);  
            
            // create a context object with 'Guest' permissions
            m_cms = m_opencms.initCmsObject(m_opencms.getDefaultUsers().getUserGuest());
            // set the site root to the default site
            m_cms.getRequestContext().setSiteRoot(m_opencms.getSiteManager().getDefaultSite().getSiteRoot());
            
            // execute the commands from the input stream
            executeCommands(fileInputStream);            
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }   
        
    /**
     * Main program entry point when started via the command line.<p>
     *
     * @param args parameters passed to the application via the command line
     */
    public static void main(String[] args) {
        boolean wrongUsage = false;    
        String webInfPath = null;
        String script = null;
    
        if (args.length > 2) {
            wrongUsage = true;
        } else {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("-base=")) {
                    webInfPath = arg.substring(6);
                } else if (arg.startsWith("-script=")) {
                    script = arg.substring(8);
                } else {
                    System.out.println("wrong usage!");
                    wrongUsage = true;
                }
            }
        }
        if (wrongUsage) {
            System.out.println("Usage: java " + CmsShell.class.getName() + " [-base={path to WEB-INF}] [-script={scriptfile}]");
        } else {
            FileInputStream stream = null;
            if (script != null) {
                try {
                    stream = new FileInputStream(script);
                } catch (IOException exc) {
                    System.out.println("trouble reading script file '" + script + "', using SDTIN instead");
                }
            }
            if (stream == null) {
                // no script-file, use standard input stream
                stream = new FileInputStream(FileDescriptor.in);
            }
            new CmsShell(webInfPath, "$u@$p:$s/>", stream);
        }
    }

    /**
     * Entry point when started from the OpenCms setup wizard.<p>
     *
     * @param fileName name of a script file containing the setup commands (e.g. cmssetup.txt)
     * @param webInfPath base folder for the OpenCms web application
     */
    public static void startSetup(String webInfPath, String fileName) {
        try {
            new CmsShell(webInfPath, "$u@$p>", new FileInputStream(new File(fileName)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Builds a method lookup String.<p>
     * 
     * @param methodName the name of the method
     * @param paramCount the parameter count of the method
     * @return a method lookup String
     */
    private String buildMethodString(String methodName, int paramCount) {
        StringBuffer buf = new StringBuffer(32);
        buf.append(methodName.toLowerCase());
        buf.append(" [");
        buf.append(paramCount);
        buf.append("]");
        return buf.toString();
    }

    /**
     * Executes a shell command with a list of parameters (the command must be the first item in the list).<p>
     *
     * @param command the command to execute
     * @param parameters the list of parameters for the command
     */
    private void executeCommand(String command, List parameters) {
        if (m_echo) {
            // echo the command to STDOUT
            System.out.print(command);
            for (int i = 0; i < parameters.size(); i++) {
                System.out.print(" " + parameters.get(i));
            }
            System.out.println();
        }
        
        // prepare to lookup a method in CmsObject or CmsShellCommands
        String lookup = buildMethodString(command, parameters.size());
        boolean executed = false;
         
        // check for a method in the CmsShellCommands
        if (! executed) {
            List possibleMethods = (List)m_cmsShellMethods.get(lookup);
            if (possibleMethods != null) {  
                executed = matchMethod(m_shellCommands, parameters, possibleMethods);
            }
        }
        
        // check for a method in the CmsObject
        if (! executed) {
            List possibleMethods = (List)m_cmsObjectMethods.get(lookup);
            if (possibleMethods != null) {        
                executed = matchMethod(m_cms, parameters, possibleMethods);
            }
        }      
        
        if (! executed) {
            // method not found
            System.out.println();
            System.out.print("Requested method not found: " + command + "(");
            for (int i=0; i<parameters.size(); i++) {
                System.out.print("value");
                if (i<parameters.size()-1) {
                    System.out.print(", ");
                }
            }
            System.out.println(")");
            System.out.println("-----------------------------------------------");
            m_shellCommands.help();
        }        
    }
    
    
    /**
     * Executes all commands read from the given input stream.<p>
     * 
     * @param fileInputStream a file input stream from which the commands are read
     */
    private void executeCommands(FileInputStream fileInputStream) {
        try {
            m_shellCommands = new CmsShellCommands(this, m_cms);
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(fileInputStream));
            while (! m_exitCalled) {
                printPrompt();
                String line = lnr.readLine();
                if ((line != null) && line.trim().startsWith("#")) {
                    System.out.println(line);
                    continue;                    
                }
                StringReader reader = new StringReader(line);
                StreamTokenizer st = new StreamTokenizer(reader);
                st.eolIsSignificant(true);                
                
                // put all tokens into a List
                List parameters = new ArrayList();
                while (st.nextToken() != StreamTokenizer.TT_EOF) {
                    if (st.ttype == StreamTokenizer.TT_NUMBER) {
                        parameters.add(Integer.toString(new Double(st.nval).intValue()));
                    } else {
                        parameters.add(st.sval);
                    }
                }
                reader.close();

                // extract command and arguments
                if ((parameters == null) || (parameters.size() == 0)) {
                    if (m_echo) {
                        System.out.println();
                    }
                    continue;
                }
                String command = (String)parameters.get(0);
                parameters = parameters.subList(1, parameters.size());

                // execute the command
                executeCommand(command, parameters);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);            
        }
    }

    /**
     * Exits this shell.<p>
     */
    protected void exit() {
        try {
            m_opencms.destroy();
        } catch (Throwable e) {
            e.printStackTrace();
        }        
        m_exitCalled = true;
    }

    private Map getShellMethods(Class base) {
        Map result = new TreeMap();        
        
        Method[] methods = base.getMethods();
        for (int i = 0; i < methods.length; i++) {
            // only public methods directly declared in the base class can be used in the shell
            if ((methods[i].getDeclaringClass() == base) && (methods[i].getModifiers() == Modifier.PUBLIC)) {
                
                // check if the method signature only uses primitive data types
                boolean onlyPrimitive = true;
                Class[] clazz = methods[i].getParameterTypes();
                for (int j=0; j<clazz.length; j++) {
                    if (!((clazz[j].equals(String.class))
                            || (clazz[j].equals(CmsUUID.class))
                            || (clazz[j].equals(boolean.class))
                            || (clazz[j].equals(int.class))
                            || (clazz[j].equals(long.class))
                            || (clazz[j].equals(double.class))
                            || (clazz[j].equals(float.class)))) {
                        // complex data type methods can not be called from the shell
                        onlyPrimitive = false;
                        break;
                    }                
                }
                
                if (onlyPrimitive) {    
                    // add this method to the set of methods that can be called from the shell
                    String lookup = buildMethodString(methods[i].getName(), methods[i].getParameterTypes().length);
                    List l;
                    if (result.containsKey(lookup)) {
                        l = (List)result.get(lookup);
                    } else {                   
                        l = new ArrayList(1);
                    }
                    l.add(methods[i]);
                    result.put(lookup, l);
                }
            }
        }    
        return result;
    }
    
    /**
     * Tries to find a mathing method for the provided parameters on the provided base class.<p>
     * 
     * If methods with the same name and number of parameters exist on the base class,
     * the given parameters are tried to be converted from String to matching types.<p>
     * 
     * @param baseClass the class to execute the method on
     * @param parameters the parameters entered by the user in the shell
     * @param possibleMethods a List of possible method signatures to try with the parameters
     * @return
     */
    private boolean matchMethod(Object baseClass, List parameters, List possibleMethods) {
        Method onlyStringMethod = null;
        Method foundMethod = null;
        Object[] params = null;
        Iterator i;
        
        // first check if there is one method with only has String parameters, this is the then fallback
        i = possibleMethods.iterator();
        while (i.hasNext()) {
            Method method = (Method)i.next();
            Class[] clazz = method.getParameterTypes();
            boolean onlyString = true;
            for (int j=0; j<clazz.length; j++) {
                if (! (clazz[j].equals(String.class))) {
                    onlyString = false;
                    break;
                }
            }
            if (onlyString) {
                onlyStringMethod = method;
                break;
            }
        }
        
        // now check a method matches the provided parameters
        // if so, use this method, else continue searching
        i = possibleMethods.iterator();
        while (i.hasNext()) {
            Method method = (Method)i.next();
            if (method == onlyStringMethod) {
                // skip the String only signature because this would always match
                continue;
            }
            // now try to convert the parameters to the required types
            Class[] clazz = method.getParameterTypes();
            Object[] converted = new Object[clazz.length];
            boolean match = true;
            for (int j=0; j<clazz.length; j++) {
                String value = (String)parameters.get(j);
                if (clazz[j].equals(String.class)) {
                    // no conversion required for String
                    converted[j] = value;
                } else if (clazz[j].equals(boolean.class)) {
                    // try to convert to boolean
                    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                        converted[j] = Boolean.valueOf(value);
                    } else {
                        match = false;
                    }
                } else if (clazz[j].equals(CmsUUID.class)) {
                    // try to convert to CmsUUID
                    try {
                        converted[j] = new CmsUUID(value);
                    } catch (NumberFormatException e) {
                        match = false;
                    }
                } else if (clazz[j].equals(int.class)) {
                    // try to convert to int
                    try {
                        converted[j] = Integer.valueOf(value);
                    } catch (NumberFormatException e) {
                        match = false;
                    }
                } else if (clazz[j].equals(long.class)) {
                    // try to convert to long
                    try {
                        converted[j] = Long.valueOf(value);
                    } catch (NumberFormatException e) {
                        match = false;
                    }                    
                } else if (clazz[j].equals(float.class)) {
                    // try to convert to float
                    try {
                        converted[j] = Float.valueOf(value);
                    } catch (NumberFormatException e) {
                        match = false;
                    }                       
                } else if (clazz[j].equals(double.class)) {
                    // try to convert to double
                    try {
                        converted[j] = Double.valueOf(value);
                    } catch (NumberFormatException e) {
                        match = false;
                    }                    
                }
                if (! match) {
                    break;
                }
            }
            if (match) {
                // we found a matching method signature
                params = converted;
                foundMethod = method;
                break;
            }
            
        }        
        
        if (onlyStringMethod != null) {
            // no match found but String only signature available, use this
            params = parameters.toArray();
            foundMethod = onlyStringMethod;
        }
        
        if (params == null) {
            // no match found at all
            return false;
        }
        
        try {
            Object result = foundMethod.invoke(baseClass, params);
            if (result != null) {
                if (result instanceof Collection) {
                    Collection c = (Collection)result;
                    System.out.println(c.getClass().getName() + " (size: " + c.size() + ")");                                                    
                    int count = 0;
                    if (result instanceof Map) {
                        Map m = (Map)result;
                        Iterator j = m.keySet().iterator();
                        while (j.hasNext()) {                    
                            Object key = j.next();
                            System.out.println(count++ + ": " + key + "= " + m.get(key));                            
                        }                       
                    } else {
                        Iterator j = c.iterator();
                        while (j.hasNext()) {
                            System.out.println(count++ + ": " + j.next());                            
                        }                        
                    }
                } else {
                    System.out.println(result.toString());
                }
            }
        } catch (InvocationTargetException ite) {
            System.err.println("Exception while calling method " + foundMethod.getName());
            ite.getTargetException().printStackTrace(System.err);
        } catch (Throwable t) {
            System.err.println("Exception while calling method " + foundMethod.getName());
            t.printStackTrace(System.err);            
        }
        
        return true;
    }
    

    /**
     * Shows the signature of all methods containing the given search String.<p>
     * 
     * @param methods the Map of methods to search
     * @param prefix a prefix to print in front of every method
     * @param searchString the String to search for, if null all methods are shown
     */
    private String printMethod(Map methods, String prefix, String searchString) {
        StringBuffer buf = new StringBuffer(512);
        Iterator i = methods.keySet().iterator();
        while (i.hasNext()) {
            List l = (List)methods.get(i.next());
            Iterator j = l.iterator();
            while (j.hasNext()) {
                Method method = (Method)j.next();
                if ((searchString == null) || (method.getName().toLowerCase().indexOf(searchString.toLowerCase()) > -1)) {
                    buf.append(prefix);
                    buf.append(method.getName());
                    buf.append("(");
                    Class[] params = method.getParameterTypes();
                    for (int k = 0; k < params.length; k++) {
                        String par = params[k].getName();
                        par = par.substring(par.lastIndexOf(".") + 1);
                        if (k != 0) {
                            buf.append(", ");
                        }
                        buf.append(par);
                    }
                    buf.append(")\n");
                }
            }
        }
        return buf.toString();
    }
    
    /**
     * Prints the shell prompt.<p>
     */
    private void printPrompt() {
        String prompt = m_prompt;
        prompt = CmsStringSubstitution.substitute(prompt, "$u", m_cms.getRequestContext().currentUser().getName());
        prompt = CmsStringSubstitution.substitute(prompt, "$s", m_cms.getRequestContext().getSiteRoot());
        prompt = CmsStringSubstitution.substitute(prompt, "$p", m_cms.getRequestContext().currentProject().getName());
        System.out.print(prompt);
    }
    
    /**
     * Sets the echo status.<p>
     * 
     * @param echo the echo status to set
     */
    protected void setEcho(boolean echo) {
        m_echo = echo;
    }
    
    /**
     * Sets the current shell prompt.<p>
     * 
     * To set the prompt, the following variables are available:<p>
     * 
     * <code>$u</code> the current user name<br>
     * <code>$s</code> the current site root<br>
     * <code>$p</code> the current project name<p>
     * 
     * @param prompt the prompt to set
     */
    protected void setPrompt(String prompt) {
        m_prompt = prompt;
    }
    
    /**
     * Shows the signature of all methods containing the given search String.<p>
     *
     * @param searchString the String to search for in the methods, if null all methods are shown
     */
    protected void showHelp(String searchString) {
        String commandList;
        boolean foundSomething = false;

        // iterate all methods in CmsShellCommands
        commandList = printMethod(m_cmsShellMethods, "- ", searchString);
        if (! "".equals(commandList)) {
            System.out.println("\nAvailable methods in CmsShell:");
            System.out.println(commandList);
            foundSomething = true;
        }
        
        // iterate all methods in CmsObject        
        commandList = printMethod(m_cmsObjectMethods, "* ", searchString);
        if (! "".equals(commandList)) {
            System.out.println("\nAvailable methods in CmsObject:");
            System.out.println(commandList);
            foundSomething = true;            
        }
        
        if (! foundSomething) {
            System.out.println("\nNo methods available matching: '" + searchString + "'");
        }        
    }    
}
