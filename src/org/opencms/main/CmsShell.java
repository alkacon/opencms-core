/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsShell.java,v $
 * Date   : $Date: 2004/02/22 13:52:27 $
 * Version: $Revision: 1.22 $
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
 * A commad line interface to access OpenCms functions which 
 * is used for the initial setup and also can be used to directly access the OpenCms
 * repository without the Workplace.<p>
 * 
 * The CmsShell has direct access to all methods in the "command objects".
 * Currently the following classes are used as command objects:
 * {@link org.opencms.main.CmsShellCommands},
 * {@link org.opencms.file.CmsRequestContext} and
 * {@link org.opencms.file.CmsObject}.<p>
 * 
 * Only public methods in the command objects that use just supported data types 
 * as parameters can be called from the shell. Supported data types are:
 * <code>String, CmsUUID, boolean, int, long, double, float</code>.<p>
 *
 * If a method name is ambiguous, i.e. the method name with the same numer of parameter exist 
 * in more then one of the command objects, the method is only executed on the first matching object.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.22 $
 * @see org.opencms.main.CmsShellCommands
 * @see org.opencms.file.CmsRequestContext
 * @see org.opencms.file.CmsObject
 */
public class CmsShell {
    
    /**
     * Command object class.<p>
     */
    private class CmsCommandObject {
        
        // The list of methods 
        private Map m_methods;
        
        // The object to execute the methods on 
        private Object m_object;
        
        /**
         * Creates a new command object.<p>
         * 
         * @param object the object to execute the methods on
         */
        protected CmsCommandObject(Object object) {
            m_object = object;
            initShellMethods();
        }
        
        /**
         * Tries to execute a method for the provided parameters on this command object.<p>
         * 
         * If methods with the same name and number of parameters exist in this command object,
         * the given parameters are tried to be converted from String to matching types.<p>
         * 
         * @param command the command entered by the user in the shell
         * @param parameters the parameters entered by the user in the shell
         * @return true if a method was executed, false otherwise
         */
        protected boolean executeMethod(String command, List parameters) {
            
            // build the method lookup
            String lookup = buildMethodLookup(command, parameters.size());  
            
            // try to look up the methods of this command object
            List possibleMethods = (List)m_methods.get(lookup);
            if (possibleMethods == null) {
                return false;
            }
            
            // a match for the mehod name was found, now try to figure out if the parameters are ok 
            Method onlyStringMethod = null;
            Method foundMethod = null;
            Object[] params = null;
            Iterator i;
            
            // first check if there is one method with only has String parameters, make this the fallback
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
            
            if ((foundMethod == null) && (onlyStringMethod != null)) {
                // no match found but String only signature available, use this
                params = parameters.toArray();
                foundMethod = onlyStringMethod;
            }
            
            if (params == null) {
                // no match found at all
                return false;
            }
            
            // now try to invoke the method
            try {
                Object result = foundMethod.invoke(m_object, params);
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
                System.out.println("Exception while calling method '" + foundMethod.getName() + "'");
                ite.getTargetException().printStackTrace(System.out);
            } catch (Throwable t) {
                System.out.println("Exception while calling method '" + foundMethod.getName() + "'");
                t.printStackTrace(System.out);            
            }
            
            return true;
        }
        
        /**
         * Returns a signature overview of all methods containing the given search String.<p>
         * 
         * If no method name matches the given search String, the empty String is returned.<p>
         * 
         * @param searchString the String to search for, if null all methods are shown
         * @return a signature overview of all methods containing the given search String
         */
        protected String getMethodHelp(String searchString) {
            StringBuffer buf = new StringBuffer(512);
            Iterator i = m_methods.keySet().iterator();
            while (i.hasNext()) {
                List l = (List)m_methods.get(i.next());
                Iterator j = l.iterator();
                while (j.hasNext()) {
                    Method method = (Method)j.next();
                    if ((searchString == null) || (method.getName().toLowerCase().indexOf(searchString.toLowerCase()) > -1)) {
                        buf.append("* ");
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
         * Returns the object to execute the methods on.<p>
         * 
         * @return the object to execute the methods on
         */
        protected Object getObject() {
            return m_object;
        }
        
        /**
         * Builds a method lookup String.<p>
         * 
         * @param methodName the name of the method
         * @param paramCount the parameter count of the method
         * @return a method lookup String
         */
        private String buildMethodLookup(String methodName, int paramCount) {
            StringBuffer buf = new StringBuffer(32);
            buf.append(methodName.toLowerCase());
            buf.append(" [");
            buf.append(paramCount);
            buf.append("]");
            return buf.toString();
        }        
                
        /**
         * Initilizes the map of accessible methods.<p>
         */
        private void initShellMethods() {
            Map result = new TreeMap();        
            
            Method[] methods = m_object.getClass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                // only public methods directly declared in the base class can be used in the shell
                if ((methods[i].getDeclaringClass() == m_object.getClass()) && (methods[i].getModifiers() == Modifier.PUBLIC)) {
                    
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
                        String lookup = buildMethodLookup(methods[i].getName(), methods[i].getParameterTypes().length);
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
            m_methods = result;
        }        
    }

    /** The OpenCms context object */
    private CmsObject m_cms;

    /** All shell callable objects */   
    private List m_commandObjects;

    /** If set to true, all commands are echoed */
    private boolean m_echo;

    /** Indicates if the 'exit' command has been called */
    private boolean m_exitCalled;

    /** The OpenCms system object */
    private OpenCmsCore m_opencms;

    /** The shell prompt format */
    private String m_prompt;
    
    /** Internal shell command object */
    private I_CmsShellCommands m_shellCommands;
    
    /** Additional shell command object */
    private I_CmsShellCommands m_additionaShellCommands;

    /**
     * Creates a new CmsShell.<p>
     * 
     * @param fileInputStream a (file) input stream from which commands are read
     * @param prompt the prompt format to set
     * @param additionalShellCommands optional object for additional shell commands, or null
     * @param webInfPath the path to the 'WEB-INF' folder of the OpenCms installation
     */
    public CmsShell(String webInfPath, String prompt, FileInputStream fileInputStream, I_CmsShellCommands additionalShellCommands) {
        setPrompt(prompt);       
        
        try {
            // first initialize runlevel 1 
            m_opencms = OpenCmsCore.getInstance();
            // search for the WEB-INF folder
            if (webInfPath == null || "".equals(webInfPath)) {
                System.out.println("No OpenCms home folder given. Trying to guess...");
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
            // set the path to the WEB-INF folder (the 2nd and 3rd parameters are just reasonable dummies)
            m_opencms.getSystemInfo().init(webInfPath, "/opencms/*", "ROOT");
            
            // now read the configuration properties
            String propertyPath = m_opencms.getSystemInfo().getConfigurationFileRfsPath();
            System.out.println("OpenCms property file: " + propertyPath);
            System.out.println();            
            ExtendedProperties configuration = CmsSetupUtils.loadProperties(propertyPath);
            
            // now upgrade to runlevel 2
            m_opencms = m_opencms.upgradeRunlevel(configuration);  
            
            // create a context object with 'Guest' permissions
            m_cms = m_opencms.initCmsObject(m_opencms.getDefaultUsers().getUserGuest());
            // set the site root to the default site
            m_cms.getRequestContext().setSiteRoot(m_opencms.getSiteManager().getDefaultSite().getSiteRoot());

            // initialize shell command object
            m_shellCommands = new CmsShellCommands();
            m_shellCommands.initShellCmsObject(m_cms, this);
            
            // initialize additional shell command object
            if (additionalShellCommands != null) {
                m_additionaShellCommands = additionalShellCommands;
                m_additionaShellCommands.initShellCmsObject(m_cms, null);
                m_additionaShellCommands.shellStart();
            } else {
                m_shellCommands.shellStart();
            }
            
            m_commandObjects = new ArrayList();            
            if (m_additionaShellCommands != null) {
                // get all shell callable methods from the the additionsl shell command object
                m_commandObjects.add(new CmsCommandObject(m_additionaShellCommands));
            }
            // get all shell callable methods from the CmsShellCommands
            m_commandObjects.add(new CmsCommandObject(m_shellCommands));          
            // get all shell callable methods from the CmsRequestContext
            m_commandObjects.add(new CmsCommandObject(m_cms.getRequestContext())); 
            // get all shell callable methods from the CmsObject
            m_commandObjects.add(new CmsCommandObject(m_cms));            

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
            new CmsShell(webInfPath, "${user}@${project}:${siteroot}|${uri}>", stream, null);
        }
    }

    /**
     * Entry point when started from the OpenCms setup wizard.<p>
     *
     * @param fileName name of a script file containing the setup commands (e.g. cmssetup.txt)
     * @param webInfPath base folder for the OpenCms web application
     * @param object optional object for additional shell commands, or null
     */
    public static void startSetup(String webInfPath, String fileName, I_CmsShellCommands object) {
        try {
            new CmsShell(webInfPath, "${user}@${project}>", new FileInputStream(new File(fileName)), object);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Exits this shell.<p>
     */
    protected void exit() {
        if (m_additionaShellCommands != null) {
            m_additionaShellCommands.shellExit();
        } else {
            m_shellCommands.shellExit();
        }
        try {
            m_opencms.destroy();
        } catch (Throwable e) {
            e.printStackTrace();
        }        
        m_exitCalled = true;
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
    protected void help(String searchString) {
        String commandList;
        boolean foundSomething = false;
        System.out.println();
        
        Iterator i = m_commandObjects.iterator();
        while (i.hasNext()) {
            CmsCommandObject cmdObj = (CmsCommandObject)i.next();
            commandList = cmdObj.getMethodHelp(searchString);
            if (! "".equals(commandList)) {
                System.out.println("Available methods in " + cmdObj.getObject().getClass().getName() + ":");
                System.out.println(commandList);
                foundSomething = true;
            }            
        }
        
        if (! foundSomething) {
            System.out.println("No methods available matching: '*" + searchString + "*'");
        }        
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
        boolean executed = false;        
        Iterator i = m_commandObjects.iterator();
        while (!executed && i.hasNext()) {
            CmsCommandObject cmdObj = (CmsCommandObject)i.next();
            executed = cmdObj.executeMethod(command, parameters);
        }
        
        if (! executed) {
            // method not found
            System.out.println();
            System.out.print("Requested method not found: " + command + "(");
            for (int j=0; j<parameters.size(); j++) {
                System.out.print("value");
                if (j<parameters.size()-1) {
                    System.out.print(", ");
                }
            }
            System.out.println(")");
            System.out.println("-----------------------------------------------");
            ((CmsShellCommands)m_shellCommands).help();
        }        
    }
    
    
    /**
     * Executes all commands read from the given input stream.<p>
     * 
     * @param fileInputStream a file input stream from which the commands are read
     */
    private void executeCommands(FileInputStream fileInputStream) {
        try {
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
     * Prints the shell prompt.<p>
     */
    private void printPrompt() {
        String prompt = m_prompt;
        prompt = CmsStringSubstitution.substitute(prompt, "${user}", m_cms.getRequestContext().currentUser().getName());
        prompt = CmsStringSubstitution.substitute(prompt, "${siteroot}", m_cms.getRequestContext().getSiteRoot());
        prompt = CmsStringSubstitution.substitute(prompt, "${project}", m_cms.getRequestContext().currentProject().getName());
        prompt = CmsStringSubstitution.substitute(prompt, "${uri}", m_cms.getRequestContext().getUri());
        System.out.print(prompt);
    }
}
