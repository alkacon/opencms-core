/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsShell.java,v $
 * Date   : $Date: 2004/02/14 00:22:01 $
 * Version: $Revision: 1.15 $
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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ExtendedProperties;

/**
 * A commad line interface to OpenCms which 
 * is used for the initial setup and also can be used to directly access the OpenCms
 * repository without the Workplace.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.15 $ $Date: 2004/02/14 00:22:01 $
 */
public class CmsShell {

    /** The OpenCms context object */
    private CmsObject m_cms;

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
     * Executes a shell command with a list of parameters (the command must be the first item in the list).<p>
     *
     * @param command a command with a list of parameters to execute
     */
    private void executeCommand(List commandWithParameters) {
        if (m_echo) {
            // all commands should be echoed to the shell
            for (int i = 0; i < commandWithParameters.size(); i++) {
                System.out.print(commandWithParameters.get(i) + " ");
            }
            System.out.println();
        }
        if ((commandWithParameters == null) || (commandWithParameters.size() == 0)) {
            return;
        }
        Object[] splittet = commandWithParameters.toArray();
        String toCall = (String)splittet[0];
        if (toCall == null) {
            return;
        }
        Class paramClasses[] = new Class[splittet.length - 1];
        String params[] = new String[splittet.length - 1];
        for (int i = 0; i < splittet.length - 1; i++) {
            params[i] = (String)splittet[i + 1];
            paramClasses[i] = String.class;
        }
        try {
            m_shellCommands.getClass().getMethod(toCall, paramClasses).invoke(m_shellCommands, params);
        } catch (InvocationTargetException ite) {
            System.err.println("Got Exception while using reflection:");
            ite.getTargetException().printStackTrace(System.err);
        } catch (NoSuchMethodException nsm) {
            System.out.println("The requested command was not found.\n-----------------------------------------------");
            m_shellCommands.help();
        } catch (Throwable t) {
            System.err.println("Got Exception while using reflection:");
            t.printStackTrace(System.err);            
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
            while (!m_exitCalled) {
                printPrompt();
                String line = lnr.readLine();
                if ((line != null) && line.trim().startsWith("#")) {
                    System.out.println(line);
                    continue;                    
                }
                StringReader reader = new StringReader(line);
                StreamTokenizer st = new StreamTokenizer(reader);
                st.eolIsSignificant(true);                
                
                // put all tokens into a vector
                List args = new ArrayList();
                while (st.nextToken() != StreamTokenizer.TT_EOF) {
                    if (st.ttype == StreamTokenizer.TT_NUMBER) {
                        args.add(Integer.toString(new Double(st.nval).intValue()));
                    } else {
                        args.add(st.sval);
                    }
                }
                reader.close();

                // exec the command
                executeCommand(args);
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
}
