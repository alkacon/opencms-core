
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsShell.java,v $
* Date   : $Date: 2001/07/20 15:35:10 $
* Version: $Revision: 1.68 $
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.core;

import java.util.*;
import java.io.*;
import com.opencms.boot.*;
import com.opencms.file.*;
import java.lang.reflect.*;
import source.org.apache.java.util.*;

import FESI.jslib.*;
import FESI.Exceptions.*;
import FESI.Parser.*;
import FESI.AST.*;
import FESI.Extensions.Extension;
import FESI.Extensions.BasicIOInterface;
import FESI.gui.*;
import FESI.Data.*;
import FESI.Interpreter.*;

/**
 * This class is a commadnlineinterface for the opencms. It can be used to test
 * the opencms, and for the initial setup. It uses the OpenCms-Object.
 *
 * @author Andreas Schouten
 * @author Anders Fugmann
 * @version $Revision: 1.68 $ $Date: 2001/07/20 15:35:10 $
 */
public class CmsShell implements I_CmsConstants {

    /**
     * The resource broker to get access to the cms.
     */
    private CmsObject m_cms;

    /**
     * The open-cms.
     */
    private A_OpenCms m_openCms;

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
        try {
            String propsPath = CmsBase.getPropertiesPath(true);
            System.out.println("%%% props: " + propsPath);
            Configurations conf = new Configurations(new ExtendedProperties(propsPath));
            m_openCms = new OpenCms(conf);
            m_cms = new CmsObject();

            m_logMemory = conf.getBoolean("log.memory", false);
            //log in default user.
            m_openCms.initUser(m_cms, null, null, C_USER_GUEST, C_GROUP_GUEST, C_PROJECT_ONLINE_ID);
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
                while(st.nextToken() != st.TT_EOF) {
                    if(st.ttype == st.TT_NUMBER) {
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

    /*
    *   The ecmaScript input-command
    */
    public String ecmaInput(String inputPrompt){
        String s="";
        System.out.print(inputPrompt);
        BufferedReader ins = new BufferedReader(new InputStreamReader(System.in));
        try{
            s=ins.readLine();
        } catch (IOException ef){
            System.out.println("IOException!!!");
        }
        if(s==null)s="";
        return s;
    }

    /**
    *   eval and print the ecmascript-prompt
    */
    public void printEcmaPrompt(JSGlobalObject jSGO,String s){

        if(s!=null){
            try {
                Object result= jSGO.eval("echoNoLF("+s+")");
            }catch (JSException je) {
                System.out.println(je.getMessage());
            }
        }else System.out.print("\n");
    }

    /**
     * the ecmascript interpreter
     *
     * @fis the file input stream for commands
     */
    public void ecmacommands(FileInputStream fis)throws Exception {

      boolean lineMode=true;
      boolean continueReading=true;

      String in="";
      String input = null;

      // the prompt: "user@projectname>"
      ecmaPrompt="cms.getRequestContext().currentUser().getName()+\"@\"+cms.getRequestContext().currentProject().getName()+\">\"";

      JSGlobalObject jSGO = null;
      jSGO = JSUtil.makeEvaluator();

      JSObject jsCMS = jSGO.makeObjectWrapper(m_cms);
      jSGO.setMember("cms", jsCMS);

      // create a bufferedReader to read the data from
      BufferedReader ins = new BufferedReader(new InputStreamReader(fis));

      // print the ecmascript welcome-text
      printEcmaHelpText();

      Class[] args = {String.class};

      // new command: echoNoLF (no linefeed)
      jSGO.setMember("echoNoLF", new JSFunctionAdapter(){
        public Object doCall(JSObject thisObject, Object args[]) throws JSException
        {
            if(args.length==0)System.out.println(" ");
            else System.out.print(args[0]);
            return null;
        }
      });

      // new command: echo
      jSGO.setMember("echo", new JSFunctionAdapter(){
        public Object doCall(JSObject thisObject, Object args[]) throws JSException
        {
            if(args.length==0)System.out.print(" ");
            else {
                //if m_echo=true all commands are echoed
                if(args[0].equals("on"))m_echo=true;
                if(args[0].equals("off"))m_echo=false;
                System.out.println(args[0]);
            }
            return null;
        }
      });

      // new command: input
      // the string-parameter is optional. if given it's used as prompt for the input
      jSGO.setMember("input", new JSFunctionAdapter(){
        public Object doCall(JSObject thisObject, Object args[]) throws JSException
        {
            String inputPrompt="? ";
            if(args.length!=0)inputPrompt=args[0].toString();
            return ecmaInput(inputPrompt);
        }
      });

      // new command: setprompt
      // if there is no string-parameter, there is no prompt
      jSGO.setMember("setPrompt", new JSFunctionAdapter(){
        public Object doCall(JSObject thisObject, Object args[]) throws JSException
        {
            if(args.length!=0)ecmaPrompt=args[0].toString();
                else ecmaPrompt=null;
            System.out.println("");
            return null;
        }
      });

      // new command: help
      jSGO.setMember("help", new JSFunctionAdapter(){
        public Object doCall(JSObject thisObject, Object args[]) throws JSException
        {
            Method meth[] = m_cms.getClass().getMethods();

            if(args.length==0){
                for(int z = 0;z < meth.length;z++) {
                    cmsHelp(meth[z],"cms");
                }
                System.out.println("echo(java.lang.String);");
                System.out.println("exit();");
                System.out.println("input(String); returns input (int or string)");
                System.out.println("input(); returns input (int or string)");

            } else {
                //because for example: user could search for: "cms.readUser", but
                //the search only processes m_cms methods, so the "cms." must be deleted.
                if(args[0].toString().startsWith("cms")){
                    if(args[0].toString().charAt(3)=='.'){
                        String ar=args[0].toString().substring(4);
                        args[0]=ar;
                    }
                }
                for(int z = 0;z < meth.length;z++)
                    if(meth[z].getName().equals(args[0]))cmsHelp(meth[z],"cms");
                    if(args[0].equals("echo"))System.out.println("echo(java.lang.String);");
                    if(args[0].equals("exit"))System.out.println("exit();");
                    if(args[0].equals("input"))System.out.println("input(String); returns input (int or string");
                    if(args[0].equals("input"))System.out.println("input(); returns input (int or string");
            }
            return null;
        }
      });

      // new command: exit
      jSGO.setMember("exit", new JSFunctionAdapter(){
        public Object doCall(JSObject thisObject, Object args[]) throws JSException
        {
            return "exit";
        }
      });

      String eol = System.getProperty("line.separator", "\n");
      ESValue theValue = ESUndefined.theUndefined;

      Evaluator evaluator = new Evaluator();

      // load fesi extension to access java from javascript
      try {
          evaluator.addMandatoryExtension("FESI.Extensions.JavaAccess");
      } catch (EcmaScriptException e) {
          System.out.println("Cannot initialize JavaAccess - exiting: " + eol + e);
          e.printStackTrace();
      }

      while (continueReading) {               // main input loop
          in="";
          while(in.equals("")){

              if(lineMode)printEcmaPrompt(jSGO,ecmaPrompt);
                  else if(ecmaPrompt!=null)System.out.print("More> ");    // linemode = false if the line is incomplete
              try{
                  in=ins.readLine();          // read a line from input
              } catch (IOException ef){
                  System.out.println("IOException!!!");
              }

              if(in==null){                   // reached end of file or control-c was pressed
                  continueReading=false;
                  break;
              }
          }

          if(lineMode) input = in;
              else input += in;

          if(continueReading)try {
                  theValue = evaluator.evaluate(input);
              } catch (EcmaScriptException e) {
                  if (e.isIncomplete()) {
                      lineMode=false;         // if the entered line is not complete
                  }else{
                      if (input == null) break;
                      try {
                          if(m_echo)System.out.println(input);

                          Object result = jSGO.eval(input);     // interpret!

                          if (result!=null) {
                              if (result.toString().equals("exit"))break;
                              System.out.println(result.toString());
                          }
                          lineMode=true;
                      }catch (JSException je) {
                          System.out.println(je.getMessage());
                          lineMode=true;
                          input="";
                      }
                  }// else
              } // catch
        } // while
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
     * @author: Jan Krag
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
