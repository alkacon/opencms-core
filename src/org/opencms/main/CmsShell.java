/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsShell.java,v $
 * Date   : $Date: 2004/01/25 12:42:45 $
 * Version: $Revision: 1.9 $
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


import org.opencms.db.CmsDriverManager;

import com.opencms.boot.CmsBase;
import com.opencms.boot.CmsSetupUtils;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;

/**
 * This class is a commad line interface to OpenCms which 
 * is used for the initial setup and also can be used to directly access the
 * without the OpenCms workplace.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.9 $ $Date: 2004/01/25 12:42:45 $
 */
public class CmsShell {

    /** Comment Char */
    public static final String C_COMMENT_CHAR = "#";

    /** If set to true, all commands are echoed */
    static boolean m_echo = false;

    /** Indicates if the 'exit' command has been called */
    static boolean m_exitCalled = false;

    /** If true, then print only the short version of the Exception in the command line */
    static boolean m_shortException = false;

    /** The OpenCms context object */
    protected CmsObject m_cms = null;

    /** If set to true, the memory-logging is enabled */
    boolean m_logMemory = false;

    /** The OpenCms system object */
    private OpenCmsCore m_openCms = null;
    
    /** Internal shell command object */
    private CmsShellCommands m_shellCommands = null;
    
    /** Internal driver manager */
    private CmsDriverManager m_driverManager = null;

    /**
     * Creates a new CmsShell.<p>
     * 
     * @param basePath the OpenCms base application path
     */
    public CmsShell(String basePath) {
        try {
            
            // first initialize runlevel 1 to set all path information
            m_openCms = OpenCmsCore.getInstance();
            basePath = m_openCms.setBasePath(basePath);
            
            String propsPath = CmsBase.getPropertiesPath(true);
            System.out.println("[OpenCms] Property path: " + propsPath);
            ExtendedProperties conf = CmsSetupUtils.loadProperties(propsPath);
            
            // now upgrade to runlevel 2
            m_openCms = m_openCms.upgradeRunlevel(conf);

            m_echo = false;
            m_exitCalled = false;
            m_shortException = false;
            m_logMemory = conf.getBoolean("log.memory", false);
                    
            m_driverManager = m_openCms.getDriverManager();
            m_cms = m_openCms.initCmsObject(null, null, m_openCms.getDefaultUsers().getUserGuest(), m_openCms.getSiteManager().getDefaultSite().getSiteRoot(), I_CmsConstants.C_PROJECT_ONLINE_ID, null);
    
        } catch (Exception exc) {
            printException(exc);
        }
    }

    /**
     * Prints an exception stacktrace to the command shell.<p>
     *
     * @param t the exception to print
     */
    protected static void printException(Throwable t) {
        if (CmsShell.m_shortException) {
            String exceptionText;

            if (t instanceof CmsException) {
                exceptionText = ((CmsException)t).getTypeText(); // this is a cms-exception: print a very short exeption-text
            } else {
                exceptionText = t.getMessage(); // only return the exception message
            }
            if ((exceptionText == null) || (exceptionText.length() == 0)) {
                // the exception-text was empty, return the class-name of the exeption
                exceptionText = t.getClass().getName();
            }
            System.out.println(exceptionText);
        } else {
            t.printStackTrace();
        }
    }

    /**
     * Prints the full name and signature of a method,
     * used by help methods.<p>
     * 
     * @param method the method to print the full name and signature for
     */
    protected static void printMethod(Method method) {
        System.out.print("  " + method.getName() + " (");
        Class[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            String par = params[i].getName();
            par = par.substring(par.lastIndexOf(".") + 1);
            if (i == 0) {
                System.out.print(par);
            } else {
                System.out.print(", " + par);
            }
        }
        System.out.println(")");
    }

    /**
     * Calls a list of commands.<p>
     *
     * @param commands a Vector of commands to be called
     */
    private void call(Vector commands) {
        if (m_echo) {
            // all commands should be echoed to the shell
            for (int i = 0; i < commands.size(); i++) {
                System.out.print(commands.elementAt(i) + " ");
            }
            System.out.println();
        }
        if ((commands == null) || (commands.size() == 0)) {
            return;
        }
        String splittet[] = new String[commands.size()];
        String toCall;
        commands.copyInto(splittet);
        toCall = splittet[0];
        if (toCall == null) {
            return;
        }
        Class paramClasses[] = new Class[splittet.length - 1];
        String params[] = new String[splittet.length - 1];
        for (int z = 0; z < splittet.length - 1; z++) {
            params[z] = splittet[z + 1];
            paramClasses[z] = String.class;
        }
        try {
            m_shellCommands.getClass().getMethod(toCall, paramClasses).invoke(m_shellCommands, params);
        } catch (InvocationTargetException ite) {
            System.err.println("Got Exception while using reflection:");
            ite.getTargetException().printStackTrace();
        } catch (NoSuchMethodException nsm) {
            System.out.println("The requested command was not found.\n-----------------------------------------------");
            m_shellCommands.printHelpText();
        } catch (Throwable t) {
            System.err.println("Got Exception while using reflection:");
            printException(t);
        }
    }

    /**
     * The OpenCms command line interface.<p>
     * 
     * @param input a file input stream from which the commands are read
     */
    public void commands(FileInputStream input) {
        try {
            this.m_shellCommands = new CmsShellCommands(m_openCms, m_driverManager);
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(input));
            while (!m_exitCalled) {
                printPrompt();
                String line = lnr.readLine();
                if ((line != null) && line.trim().startsWith(C_COMMENT_CHAR)) {
                    System.out.println(line);
                    continue;                    
                }
                StringReader reader = new StringReader(line);
                StreamTokenizer st = new StreamTokenizer(reader);
                st.eolIsSignificant(true);                
                
                // put all tokens into a vector
                Vector args = new Vector();
                while (st.nextToken() != StreamTokenizer.TT_EOF) {
                    if (st.ttype == StreamTokenizer.TT_NUMBER) {
                        args.addElement(Integer.toString(new Double(st.nval).intValue()));
                    } else {
                        args.addElement(st.sval);
                    }
                }
                reader.close();

                // exec the command
                call(args);
            }
        } catch (Exception exc) {
            printException(exc);
        }
    }

    /**
     * Prints the current shell prompt.<p>
     */
    private void printPrompt() {
        System.out.print("{" + m_cms.getRequestContext().currentUser().getName() + "@" + m_cms.getRequestContext().currentProject().getName() + "}");

        // print out memory-informations, if needed
        if (m_logMemory) {
            long total = Runtime.getRuntime().totalMemory() / 1024;
            long free = Runtime.getRuntime().freeMemory() / 1024;
            System.out.print(("[" + total + "/" + free + "/" + (total - free) + "]"));
        }
        System.out.print("> ");
    }
}
