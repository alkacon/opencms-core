package com.opencms.core;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsShell.java,v $
 * Date   : $Date: 2000/10/31 13:46:36 $
 * Version: $Revision: 1.56 $
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

import java.util.*; 
import java.io.*;
import com.opencms.file.*;
import java.lang.reflect.*;
import source.org.apache.java.util.*;

/**
 * This class is a commadnlineinterface for the opencms. It can be used to test
 * the opencms, and for the initial setup. It uses the OpenCms-Object.
 * 
 * @author Andreas Schouten
 * @author Anders Fugmann
 * @version $Revision: 1.56 $ $Date: 2000/10/31 13:46:36 $
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
	private boolean m_echo = false;
	
/**
 * Calls a command
 * 
 * @param command The command to be called.
 */
private void call(Vector command)
{
	if(m_echo) {
		// all commands should be echoed to the shell
		for(int i=0; i < command.size(); i++) {
			System.out.print(command.elementAt(i) + " ");
		}
		System.out.println();
	}
	
	if ((command == null) || (command.size() == 0))
	{
		return;
	} else if(command.elementAt(0).equals("echo") && command.elementAt(1).equals("on")) {
		// all commands should be echoed
		m_echo = true;
		return;
	} else if(command.elementAt(0).equals("echo") && command.elementAt(1).equals("off")) {
		// all commands should not be echoed (default)
		m_echo = false;
		return;
	}
	String splittet[] = new String[command.size()];
	String toCall;
	command.copyInto(splittet);
	toCall = splittet[0];
	if (toCall == null)
		return;
	Class paramClasses[] = new Class[splittet.length - 1];
	String params[] = new String[splittet.length - 1];
	for (int z = 0; z < splittet.length - 1; z++)
	{
		params[z] = splittet[z + 1];
		paramClasses[z] = String.class;
	}
	try
	{
		shellCommands.getClass().getMethod(toCall, paramClasses).invoke(shellCommands, params);
	}
	catch (InvocationTargetException ite)
	{
		System.err.println("Got Exception while using reflection:");
		ite.getTargetException().printStackTrace();
	}
	catch (NoSuchMethodException nsm)
	{
			System.out.println("The requested command was not found.\n-----------------------------------------------");
			shellCommands.printHelpText();
	}
	catch (Exception exc)
	{
		System.err.println("Got Nullpointer Exception while using reflection:");
		printException(exc);
	}
}
	/**
	 * The commandlineinterface.
	 */
	private void commands() {
		try{			
			FileReader fr = new FileReader(FileDescriptor.in);
			LineNumberReader lnr = new LineNumberReader(fr);
//			System.out.println("Type help to get a list of commands.");
			for(;;) { // ever
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
		}catch(Exception exc){
			
			printException(exc);
		}
	}
/**
 * Insert the method's description here.
 * Creation date: (10/05/00 %r)
 * @author: 
 */
public CmsShell(String args[])
{
	try
	{
		Configurations conf = new Configurations(new ExtendedProperties(args[0]));
		m_openCms = new OpenCms(conf);
		m_cms = new CmsObject();
		this.shellCommands = new CmsShellCommands(args, m_openCms, m_cms);

		//log in default user.
		m_openCms.initUser(m_cms, null, null, C_USER_GUEST, C_GROUP_GUEST, C_PROJECT_ONLINE_ID);
	}
	catch (Exception exc)
	{
		printException(exc);
	}
}
	/**
 * The main entry point for the commandline interface to the opencms. 
 *
 * @param args Array of parameters passed to the application
 * via the command line.
 */
public static void main(String[] args)
{
	CmsShell shell;
	try
	{
		if (args.length < 1)
		{
			// print out usage-information.
			CmsShell.usage();
		}
		else
		{

			// initializes the db and connects to it
			shell = new CmsShell(args);


			// wait for user-input
			shell.commands();
		}
	}
	catch (Exception exc)
	{
		exc.printStackTrace();
	}
}
	/**
	 * Prints a exception with the stacktrace.
	 * 
	 * @param exc The exception to print.
	 */
	protected static void printException(Exception exc) {
		exc.printStackTrace();
	}
	/**
 * Prints the full name and signature of a method.<br>
 * Called by help-methods.
 * Creation date: (09/28/00)
 * @author Jan Krag
 * @param param java.lang.reflect.Method
 */
protected static void printMethod(Method method)
{
	System.out.print("  " + method.getName() + " (");
	Class[] params = method.getParameterTypes();
	for (int i = 0; i < params.length; i++)
	{
		String par = params[i].getName();
		par = par.substring(par.lastIndexOf(".") + 1);
		if (i == 0)
			System.out.print(par);
		else
			System.out.print(", " + par);
	}
	System.out.println(")");
}
	/**
 * Prints the current prompt.
 * Creation date: (10/03/00 %r)
 * @author: Jan Krag
 */
private void printPrompt()
{
	System.out.print("{" + m_cms.getRequestContext().currentUser().getName() + "@" + m_cms.getRequestContext().currentProject().getName() + "}> ");
}
/**
 * Gives the usage-information to the user.
 */
private static void usage()
{
	System.out.println("Usage: java com.opencms.core.CmsShell properties-file");
}
}
