/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsShell.java,v $
* Date   : $Date: 2003/06/16 11:18:43 $
* Version: $Revision: 1.75.2.1 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.core;

import com.opencms.boot.CmsBase;
import com.opencms.file.CmsObject;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import source.org.apache.java.util.Configurations;
import source.org.apache.java.util.ExtendedProperties;

/**
 * This class is a commad line interface to OpenCms which 
 * can be used for the initial setup and to test the system.<p>
 *
 * @author Andreas Schouten
 * @author Anders Fugmann
 * 
 * @version $Revision: 1.75.2.1 $ $Date: 2003/06/16 11:18:43 $
 */
public class CmsShell implements I_CmsConstants {

    /**
     * The resource broker to get access to the cms.
     */
    protected CmsObject m_cms;

    /**
     * The open-cms.
     */
    private OpenCms m_openCms;

    /** Comment Char. */
    public static final String COMMENT_CHAR = "#";
    private CmsShellCommands shellCommands;

    /**
     * If this member is set to true, all commands are echoed
     */
    static boolean m_echo = false;

    /**
     * If this member is set to true the memory-logging is enabled.
     */
    boolean m_logMemory = false;

    /**
     * if m_shortException is true then print only the short version of the Exception in the commandshell
     */
    static boolean m_shortException = false;

    /**
     * m_exitCalled indicates if the 'exit' command has been called
     */
    static boolean m_exitCalled = false;

    /**
     * the prompt for ecmaShell
     */
    String ecmaPrompt;

    /**
     * Creates a new CmsShell-Object.
     */
    public CmsShell() {
        A_OpenCms.initVersion(this);        
        try {
            String propsPath = CmsBase.getPropertiesPath(true);
            System.out.println("%%% props: " + propsPath);
            Configurations conf = new Configurations(new ExtendedProperties(propsPath));
            m_openCms = new OpenCms(conf);
            m_cms = new CmsObject();

            m_logMemory = conf.getBoolean("log.memory", false);
            //log in default user.
            m_openCms.initUser(m_cms, null, null, C_USER_GUEST, C_GROUP_GUEST, C_PROJECT_ONLINE_ID, null);
        }
        catch(Exception exc) {
            printException(exc);
        }
    }

    /**
     * Calls a command
     *
     * @param command The command to be called.
     */
    private void call(Vector command) {
        if(m_echo) {
            // all commands should be echoed to the shell
            for(int i = 0;i < command.size();i++) {
                System.out.print(command.elementAt(i) + " ");
            }
            System.out.println();
        }
        if((command == null) || (command.size() == 0)) {
            return ;
        }
        String splittet[] = new String[command.size()];
        String toCall;
        command.copyInto(splittet);
        toCall = splittet[0];
        if(toCall == null) {
            return ;
        }
        Class paramClasses[] = new Class[splittet.length - 1];
        String params[] = new String[splittet.length - 1];
        for(int z = 0;z < splittet.length - 1;z++) {
            params[z] = splittet[z + 1];
            paramClasses[z] = String.class;
        }
        try {
            shellCommands.getClass().getMethod(toCall, paramClasses).invoke(shellCommands, params);
        }
        catch(InvocationTargetException ite) {
            System.err.println("Got Exception while using reflection:");
            ite.getTargetException().printStackTrace();
        }
        catch(NoSuchMethodException nsm) {
            System.out.println("The requested command was not found.\n-----------------------------------------------");
            shellCommands.printHelpText();
        }
        catch(Exception exc) {
            System.err.println("Got Nullpointer Exception while using reflection:");
            printException(exc);
        }
    }

    /**
     * The commandlineinterface.
     */
    public void commands(FileInputStream fis) {
        try {
            this.shellCommands = new CmsShellCommands(m_openCms, m_cms);
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(fis));
            while(!m_exitCalled) { // ever
                printPrompt();
                StringReader reader = new StringReader(lnr.readLine());
                StreamTokenizer st = new StreamTokenizer(reader);
                st.eolIsSignificant(true);

                //put all tokens into a vector.
                Vector args = new Vector();
                while(st.nextToken() != StreamTokenizer.TT_EOF) {
                    if(st.ttype == StreamTokenizer.TT_NUMBER) {
                        args.addElement(new Double(st.nval).intValue() + "");
                    } else {
                        args.addElement(st.sval);
                    }
                }
                reader.close();

                //exec the command
                call(args);
            }
        }
        catch(Exception exc) {
            printException(exc);
        }
    }

    /**
    *   The ecmaScript welcome text
    */
    public void printEcmaHelpText(){
        System.out.println("");
        System.out.println("help();              Gives a list of available commands with signature");
        System.out.println("help(\"<command>\"     Shows signature of command");
        System.out.println("exit();              Quit the Shell");
        System.out.println("");
    }

    /**
    *   The ecmaScript help-command
    */
    public void cmsHelp(Method m, String par) {
        if(!m.getName().equals(par)){
              System.out.print(par+"."+m.getName());

              Class[] parameterTypes=m.getParameterTypes();

              System.out.print("(");
              for(int k=0;k<parameterTypes.length;k++){
                if(k>0)System.out.print(", ");
                System.out.print(""+ parameterTypes[k].getName());
              }

              String returnString=m.getReturnType().toString();
              if(!returnString.equals("void") && !returnString.equals("int")){
                  System.out.println("); returns: "+returnString);
              }else{
                  System.out.println(");");
              }
          }
    }

    /**
     * Prints a exception with the stacktrace.
     *
     * @param exc The exception to print.
     */
    protected static void printException(Exception exc) {
        if(CmsShell.m_shortException) {
            String exceptionText;

            if(exc instanceof CmsException) {
                exceptionText = ((CmsException)exc).getTypeText();    // this is a cms-exception: print a very short exeption-text
            } else {
                exceptionText = exc.getMessage();     // only return the exception message
            }
            if((exceptionText == null) || (exceptionText.length() == 0)) {
                // the exception-text was empty, return the class-name of the exeption
                exceptionText = exc.getClass().getName();
            }
            System.out.println(exceptionText);
        }
        else {
            exc.printStackTrace();
        }
    }

    /**
     * Prints the full name and signature of a method.<br>
     * Called by help-methods.
     * Creation date: (09/28/00)
     * @author Jan Krag
     * @param param java.lang.reflect.Method
     */
    protected static void printMethod(Method method) {
        System.out.print("  " + method.getName() + " (");
        Class[] params = method.getParameterTypes();
        for(int i = 0;i < params.length;i++) {
            String par = params[i].getName();
            par = par.substring(par.lastIndexOf(".") + 1);
            if(i == 0) {
                System.out.print(par);
            }
            else {
                System.out.print(", " + par);
            }
        }
        System.out.println(")");
    }

    /**
     * Prints the current prompt.
     * Creation date: (10/03/00 %r)
     * @author Jan Krag
     */
    private void printPrompt() {
        System.out.print("{" + m_cms.getRequestContext().currentUser().getName() + "@"
                + m_cms.getRequestContext().currentProject().getName() + "}");

        // print out memory-informations, if needed
        if(m_logMemory) {
            long total = Runtime.getRuntime().totalMemory() / 1024;
            long free = Runtime.getRuntime().freeMemory() / 1024;
            System.out.print(("[" + total + "/" + free + "/" + (total - free) + "]"));
        }
        System.out.print("> ");
    }
}
