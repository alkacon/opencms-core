/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/setup/Attic/CmsShell.java,v $
 * Date   : $Date: 2000/03/15 09:46:13 $
 * Version: $Revision: 1.33 $
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

package com.opencms.setup;

import java.util.*;
import java.io.*;
import com.opencms.file.*;
import com.opencms.core.*;
import java.lang.reflect.*;

/**
 * This class is a commadnlineinterface for the opencms. It can be used to test
 * the opencms, and for the initial setup. It uses the OpenCms-Object.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.33 $ $Date: 2000/03/15 09:46:13 $
 */
public class CmsShell implements I_CmsConstants {
	
	/**
	 * The resource broker to get access to the cms.
	 */
	private A_CmsObject m_cms;

	/**
	 * The main entry point for the commandline interface to the opencms. 
	 *
	 * @param args Array of parameters passed to the application
	 * via the command line.
	 */
	public static void main (String[] args)	{
		
		CmsShell shell = new CmsShell();
		
		try {
		
			if( (args.length == 0) || (args.length > 3) ) {
				// print out usage-information.
				shell.usage();
			} else {
		
				// initializes the db and connects to it
				shell.init(args);
				
				// print the version-string
				shell.version();
				shell.copyright();
		
				// wait for user-input
				shell.commands();	
			}
		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}
	
	/**
	 * Inits the database.
	 * 
	 * @param args A Array of args to get connected.
	 */
	private void init(String[] args)
		throws Exception {
		m_cms = new CmsObject();
		m_cms.init(((A_CmsInit) Class.forName(args[0]).newInstance() ).init(args[1], args[2]));
		m_cms.init(null, null, C_USER_GUEST, C_GROUP_GUEST, C_PROJECT_ONLINE);
	}	
	
	/**
	 * The commandlineinterface.
	 */
	private void commands() {
		try{
			Reader reader = new FileReader(java.io.FileDescriptor.in);
			StreamTokenizer tokenizer = new StreamTokenizer(reader);
			tokenizer.eolIsSignificant(true);
			Vector input;
			System.out.println("Type help to get a list of commands.");
			for(;;) { // ever
				System.out.print("> ");
				input = new Vector();
				while(tokenizer.nextToken() != tokenizer.TT_EOL) {
					if(tokenizer.ttype == tokenizer.TT_NUMBER) {
						input.addElement(tokenizer.nval + "");
					} else {
						input.addElement(tokenizer.sval);
					}
				}
				// call the command
				call(input);
			}
		}catch(Exception exc){
			printException(exc);
		}
	}
	
	/**
	 * Gives the usage-information to the user.
	 */
	private void usage() {
		System.out.println("Usage: java com.opencms.setup.CmsShell initializer-classname sqldriver-classname connectstring");
	}
	
	/**
	 * Calls a command
	 * 
	 * @param command The command to be called.
	 */
	private void call(Vector command) {
		if((command == null) || (command.size() == 0)) {
			return;
		}
		
		String splittet[] = new String[command.size()];
		String toCall;
		
		command.copyInto(splittet);
		toCall = splittet[0];		
		Class paramClasses[] = new Class[splittet.length - 1];
		String params[] = new String[splittet.length - 1];
		for(int z = 0; z < splittet.length - 1; z++) {
			params[z] = splittet[z+1];
			paramClasses[z] = String.class;
		}
		
		try {
			getClass().getMethod(toCall, paramClasses).invoke(this,params);
		} catch(Exception exc) {
			printException(exc);
		}
	}


	/**
	 * Reads a given file from the local harddisk and uploads
	 * it to the OpenCms system.
	 * Used in the OpenCms console only.
	 * 
	 * @author Alexander Lucas
	 * @param filename Local file to be uploaded.
	 * @return Byte array containing the file content.
	 * @throws CmsException
	 */
    private byte[] importFile(String filename) throws CmsException {     
        File file = null;
        long len = 0;
        FileInputStream importInput = null;
        byte[] result;        
                
        // First try to load the file
        try {
            file = new File(filename);
        } catch(Exception e) {
            file = null;
        }
        if(file == null) {
            throw new CmsException("Could not load local file " + filename, CmsException.C_NOT_FOUND); 
        } 
        
        // File was loaded successfully.
        // Now try to read the content.
        try {
            len = file.length();
            result = new byte[(int)len];
            importInput = new FileInputStream(file);
            importInput.read(result);
            importInput.close();
        } catch(Exception e) {
            throw new CmsException(e.toString() , CmsException.C_UNKNOWN_EXCEPTION); 
        }
        return result;
    }
	
	/**
	 * Prints a exception with the stacktrace.
	 * 
	 * @param exc The exception to print.
	 */
	private void printException(Exception exc) {
		exc.printStackTrace();
	}
	
	// All methods, that may be called by the user:
	
	/**
	 * Exits the commandline-interface
	 */
	public void exit() {
		System.exit(0);
	}

	/**
	 * Prints all possible commands.
	 */
	public void help() {
		Method meth[] = getClass().getMethods();
		for(int z=0 ; z < meth.length ; z++) {
			if( ( meth[z].getDeclaringClass() == getClass() ) &&
				( meth[z].getModifiers()  == Modifier.PUBLIC ) ) {
				System.out.print(meth[z].getName() + "(");
				System.out.println(meth[z].getParameterTypes().length + ")");
			}
		}
	}
	
	/**
	 * Echos the input to output.
	 * 
	 * @param echo The echo to be written to output.
	 */
	public void echo(String echo) {
		System.out.println(echo);
	}
	
	/**
	 * Returns the current user.
	 */
	public void whoami() {
		System.out.println(m_cms.getRequestContext().currentUser());
	}

	/**
	 * Logs a user into the system.
	 * 
	 * @param username The name of the user to log in.
	 * @param password The password.
	 */
	public void login(String username, String password) {
		try {
			m_cms.loginUser(username, password);
			whoami();
		} catch( Exception exc ) {
			printException(exc);
			System.out.println("Login failed!");
		}
	}
	
	/**
	 * Returns all users of the cms.
	 */
	public void getUsers() {
		try {
			Vector users = m_cms.getUsers();
			for( int i = 0; i < users.size(); i++ ) {
				System.out.println( (A_CmsUser)users.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Returns all users of the cms.
	 */
	public void getGroups() {
		try {
			Vector groups = m_cms.getGroups();
			for( int i = 0; i < groups.size(); i++ ) {
				System.out.println( (A_CmsGroup)groups.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Determines, if the user is Admin.
	 */
	public void isAdmin() {
		try {
			System.out.println( m_cms.getRequestContext().isAdmin() );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Determines, if the user is Projectleader.
	 */
	public void isProjectManager() {
		try {
			System.out.println( m_cms.getRequestContext().isProjectManager() );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Returns all groups of a user.
	 * 
	 * @param username The name of the user.
	 */
	public void getGroupsOfUser(String username) {
		try {
			Vector groups = m_cms.getGroupsOfUser(username);
			for( int i = 0; i < groups.size(); i++ ) {
				System.out.println( (A_CmsGroup)groups.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}
	}
	
	/**
	 * Adds a user to the cms.
	 * 
	 * @param name The new name for the user.
	 * @param password The new password for the user.
	 * @param group The default groupname for the user.
	 * @param description The description for the user.
	 */
	public void addUser( String name, String password, 
						 String group, String description) {
		try {
			System.out.println(m_cms.addUser( name, password, group, 
											  description, new Hashtable(), 
											  C_FLAG_ENABLED) );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Adds a user to the cms.
	 * 
	 * @param name The new name for the user.
	 * @param password The new password for the user.
	 * @param group The default groupname for the user.
	 * @param description The description for the user.
	 * @param flags The flags for the user.
	 */
	public void addUser( String name, String password, 
						 String group, String description, String flags) {
		try {
			System.out.println(m_cms.addUser( name, password, group, 
											  description, new Hashtable(), 
											  Integer.parseInt(flags)) );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Adds a user to the cms.
	 * 
	 * @param name The new name for the user.
	 * @param password The new password for the user.
	 * @param group The default groupname for the user.
	 * @param description The description for the user.
	 * @param flags The flags for the user.
	 */
	public void addUser( String name, String password, 
						 String group, String description,
						 String firstname, String lastname, String email) {
		try {
			A_CmsUser user = m_cms.addUser( name, password, group, 
											description, new Hashtable(), C_FLAG_ENABLED);
			user.setEmail(email);
			user.setFirstname(firstname);
			user.setLastname(lastname);
			m_cms.writeUser(user);
		} catch( Exception exc ) {
			printException(exc);
		}
	}
	
	/** 
	 * Deletes a user from the Cms.
	 * 
	 * @param name The name of the user to be deleted.
	 */
	public void deleteUser( String name ) {
		try {
			m_cms.deleteUser( name );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/** 
	 * Writes a user to the Cms.
	 * 
	 * @param name The name of the user to be written.
	 * @param flags The flags of the user to be written.
	 */
	public void writeUser( String name, String flags ) {
		try {
			// get the user, which has to be written
			A_CmsUser user = m_cms.readUser(name);
			
			if(Integer.parseInt(flags) == C_FLAG_DISABLED) {
				user.setDisabled();
			} else {
				user.setEnabled();
			}
			
			// write it back
			m_cms.writeUser(user);		

		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Adds a Group to the cms.
	 * 
	 * @param name The name of the new group.
	 * @param description The description for the new group.
	 * @int flags The flags for the new group.
	 * @param name The name of the parent group (or null).
	 */
	public void addGroup(String name, String description, String flags, String parent) {
		try {
			m_cms.addGroup( name, description, Integer.parseInt(flags), parent );
		} catch( Exception exc ) {
			printException(exc);
		}
	}	

	/**
	 * Adds a Group to the cms.
	 * 
	 * @param name The name of the new group.
	 * @param description The description for the new group.
	 */
	public void addGroup(String name, String description) {
		try {
			m_cms.addGroup( name, description, C_FLAG_ENABLED, null );
		} catch( Exception exc ) {
			printException(exc);
		}
	}	

	/**
	 * Returns a group in the Cms.
	 * 
	 * @param groupname The name of the group to be returned.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void readGroup(String groupname) { 
		try {
			System.out.println( m_cms.readGroup( groupname ) );
		} catch( Exception exc ) {
			printException(exc);
		}
	}	

	/**
	 * Returns all groups of a user.
	 * 
	 * @param groupname The name of the group.
	 */
	public void getUsersOfGroup(String groupname) {
		try {
			Vector users = m_cms.getUsersOfGroup(groupname);
			for( int i = 0; i < users.size(); i++ ) {
				System.out.println( (A_CmsUser)users.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Checks if a user is member of a group.<P/>
	 *  
	 * @param nameuser The name of the user to check.
	 * @param groupname The name of the group to check.
	 */
	public void userInGroup(String username, String groupname)
	{
		try {
			System.out.println( m_cms.userInGroup( username, groupname ) );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/** 
	 * Writes a group to the Cms.
	 * 
	 * @param name The name of the group to be written.
	 * @param flags The flags of the user to be written.
	 */
	public void writeGroup( String name, String flags ) {
		try {
			// get the group, which has to be written
			A_CmsGroup group = m_cms.readGroup(name);
			
			if(Integer.parseInt(flags) == C_FLAG_DISABLED) {
				group.setDisabled();
			} else {
				group.setEnabled();
			}
			
			// write it back
			m_cms.writeGroup(group);		

		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Delete a group from the Cms.<BR/>
	 * 
	 * @param delgroup The name of the group that is to be deleted.
	 */	
	public void deleteGroup(String delgroup) {
		try {
			m_cms.deleteGroup( delgroup );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Adds a user to a group.
     *
	 * @param username The name of the user that is to be added to the group.
	 * @param groupname The name of the group.
	 */	
	public void addUserToGroup(String username, String groupname) {
		try {
			m_cms.addUserToGroup( username, groupname );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Removes a user from a group.
	 * 
	 * @param username The name of the user that is to be removed from the group.
	 * @param groupname The name of the group.
	 */	
	public void removeUserFromGroup(String username, String groupname) {
		try {
			m_cms.removeUserFromGroup( username, groupname );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Returns all child groups of a group<P/>
	 * 
	 * @param groupname The name of the group.
	 */
	public void getChild(String groupname) {
		try {
			Vector groups = m_cms.getChild(groupname);
			for( int i = 0; i < groups.size(); i++ ) {
				System.out.println( (A_CmsGroup)groups.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}
	}
	
	/** 
	 * Sets the password for a user.
	 * 
	 * @param username The name of the user.
	 * @param oldPassword The old password.
	 * @param newPassword The new password.
	 */
	public void setPassword(String username, String oldPassword, String newPassword) {
		try {
			m_cms.setPassword( username, oldPassword, newPassword );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

    /**
	 * Adds a new CmsMountPoint. 
	 * A new mountpoint for a mysql filesystem is added.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 * @param driver The driver for the db-system. 
	 * @param connect The connectstring to access the db-system.
	 * @param name A name to describe the mountpoint.
	 */
	public void addMountPoint(String mountpoint, String driver, 
							  String connect, String name) {
		try {
			m_cms.addMountPoint( mountpoint, driver, connect, name );
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

    /**
	 * Adds a new CmsMountPoint. 
	 * A new mountpoint for a disc filesystem is added.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 * @param mountpath The physical location this mount point directs to. 
	 * @param name The name of this mountpoint.
	 * @param user The default user for this mountpoint.
	 * @param group The default group for this mountpoint.
	 * @param type The default resourcetype for this mountpoint.
	 * @param accessFLags The access-flags for this mountpoint.
	 */
	public void addMountPoint(String mountpoint, String mountpath, 
							  String name, String user, String group,
							  String type, String accessFlags) {
		try {
			m_cms.addMountPoint( mountpoint, mountpath, name, user, group, type, 
								 Integer.parseInt(accessFlags) );
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Gets a CmsMountPoint. 
	 * A mountpoint will be returned.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 * 
	 * @return the mountpoint - or null if it doesen't exists.
	 */
	public void readMountPoint(String mountpoint ) {
		try {
			System.out.println( m_cms.readMountPoint( mountpoint ) );
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Gets all CmsMountPoints. 
	 * All mountpoints will be returned.
	 * 
	 * @return the mountpoints - or null if they doesen't exists.
	 */
	public void getAllMountPoints() {
		try {
			Hashtable mountPoints = m_cms.getAllMountPoints();
			Enumeration keys = mountPoints.keys();
			
			while(keys.hasMoreElements()) {
				System.out.println(mountPoints.get(keys.nextElement()));
			}
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

    /**
	 * Deletes a CmsMountPoint. 
	 * A mountpoint will be deleted.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 */
	public void deleteMountPoint(String mountpoint ) {
		try {
			m_cms.deleteMountPoint(mountpoint);
		} catch( Exception exc ) {
			printException(exc);
		}		
	}
	
	/**
	 * Creates a project.
	 * 
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param groupname the name of the group to be set.
	 */
	public void createProject(String name, String description, String groupname,
							  String managergroupname) {
		try {
			m_cms.createProject(name, description, groupname, managergroupname);
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Reads a project from the Cms.
	 * 
	 * @param name The name of the project to read.
	 */
	public void readProject(String name) {
		try {
			System.out.println( m_cms.readProject(name) );
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Reads a the online-project from the Cms.
	 */
	public void onlineProject() {
		try {
			System.out.println( m_cms.onlineProject() );
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Tests if the user can access the project.
	 * 
	 * @param projectname the name of the project.
	 */
	public void accessProject(String projectname) {
		try {
			System.out.println( m_cms.accessProject(projectname) );
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Returns a user object.<P/>
	 * 
	 * @param username The name of the user that is to be read.
	 */
	public void readUser(String username) {
		try {
			System.out.println( m_cms.readUser(username) );
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Returns all projects, which the user may access.
	 * 
	 * @return a Vector of projects.
	 */
	public void getAllAccessibleProjects() {
		try {
			Vector projects = m_cms.getAllAccessibleProjects();
			for( int i = 0; i < projects.size(); i++ ) {
				System.out.println( (A_CmsProject)projects.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}		
	}
	
	/**
	 * Reads a folder from the Cms.<BR/>
	 * 
	 * @param folder The complete path to the folder that will be read.
	 */
	public void readFolder(String folder) {
		try {
			System.out.println( m_cms.readFolder(folder, "") );
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Creates a new folder.
	 * 
	 * @param folder The complete path to the folder in which the new folder 
	 * will be created.
	 * @param newFolderName The name of the new folder (No pathinformation allowed).
	 */
	public void createFolder(String folder, String newFolderName) {
		try {
			System.out.println( m_cms.createFolder(folder, newFolderName) );
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Returns  all I_CmsResourceTypes.
	 */
	public void getAllResourceTypes() {
		try {
			Hashtable resourceTypes = m_cms.getAllResourceTypes();
			Enumeration keys = resourceTypes.keys();
			
			while(keys.hasMoreElements()) {
				System.out.println(resourceTypes.get(keys.nextElement()));
			}
		} catch( Exception exc ) {
			printException(exc);
		}		
	}
	
	/**
	 * Returns a A_CmsResourceTypes.
	 * 
	 * @param resourceType the name of the resource to get.
	 */
	public void getResourceType(String resourceType) {
		try {
			System.out.println( m_cms.getResourceType(resourceType) );
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Adds a CmsResourceTypes.
	 * 
	 * @param resourceType the name of the resource to get.
	 * @param launcherType the launcherType-id
	 * @param launcherClass the name of the launcher-class normaly ""
	 */
	public void addResourceType(String resourceType, String launcherType, 
								String launcherClass) {		
		try {
			System.out.println( m_cms.addResourceType(resourceType, 
													  Integer.parseInt(launcherType), 
													  launcherClass) );
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The name of the resource type to read the 
	 * metadefinitions for.
	 */	
	public void readAllMetadefinitions(String resourcetype) {
		try {
			Vector metadefs = m_cms.readAllMetadefinitions(resourcetype);
			for( int i = 0; i < metadefs.size(); i++ ) {
				System.out.println( (A_CmsMetadefinition)metadefs.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Creates the metadefinition for the resource type.<BR/>
	 * 
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The name of the resource-type for the metadefinition.
	 * @param type The type of the metadefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void createMetadefinition(String name, String resourcetype, String type)
		throws CmsException {
		try {
			System.out.println( m_cms.createMetadefinition(name, resourcetype, 
														   Integer.parseInt(type)) );
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The name of the resource type to read the 
	 * metadefinitions for.
	 * @param type The type of the metadefinition (normal|mandatory|optional).
	 */	
	public void readAllMetadefinitions(String resourcetype, String type) {
		try {
			Vector metadefs = m_cms.readAllMetadefinitions(resourcetype, 
														   Integer.parseInt(type));
			for( int i = 0; i < metadefs.size(); i++ ) {
				System.out.println( (A_CmsMetadefinition)metadefs.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Reads the Metadefinition for the resource type.<BR/>
	 * 
	 * @param name The name of the Metadefinition to read.
	 * @param resourcetype The name of the resource type for the Metadefinition.
	 */
	public void readMetadefinition(String name, String resourcetype) {
		try {
			System.out.println( m_cms.readMetadefinition(name, resourcetype) );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Writes the Metadefinition for the resource type.<BR/>
	 * 
	 * @param name The name of the Metadefinition to overwrite.
	 * @param resourcetype The name of the resource type to read the 
	 * metadefinitions for.
	 * @param type The new type of the metadefinition (normal|mandatory|optional).
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void writeMetadefinition(String name, 
									String resourcetype, 
									String type) {
		try {
			A_CmsMetadefinition metadef = m_cms.readMetadefinition(name, resourcetype);
			metadef.setMetadefType(Integer.parseInt(type));			
			System.out.println( m_cms.writeMetadefinition(metadef) );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Delete the Metadefinition for the resource type.<BR/>
	 * 
	 * @param name The name of the Metadefinition to overwrite.
	 * @param resourcetype The name of the resource-type for the Metadefinition.
	 */
	public void deleteMetadefinition(String name, String resourcetype) {
		try {
			m_cms.deleteMetadefinition(name, resourcetype);
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Returns a Metainformation of a file or folder.
	 * 
	 * @param name The resource-name of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 */
	public void readMetainformation(String name, String meta) {
		try {
			System.out.println( m_cms.readMetainformation(name, meta) );
		} catch( Exception exc ) {
			printException(exc);
		}
	}
		
	/**
	 * Writes a Metainformation for a file or folder.
	 * 
	 * @param name The resource-name of which the Metainformation has to be set.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * @param value The value for the metainfo to be set.
	 */
	public void writeMetainformation(String name, String meta, String value) {
		try {
			m_cms.writeMetainformation(name, meta, value);
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Returns a list of all Metainformations of a file or folder.
	 * 
	 * @param resource The name of the resource of which the Metainformation has to be 
	 * read.
	 */
	public void readAllMetainformations(String resource) {
		try {
			Hashtable metainfos = m_cms.readAllMetainformations(resource);
			Enumeration keys = metainfos.keys();
			Object key;
			
			while(keys.hasMoreElements()) {
				key = keys.nextElement();
				System.out.print(key + "=");
				System.out.println(metainfos.get(key));
			}
		} catch( Exception exc ) {
			printException(exc);
		}		
	}

	/**
	 * Deletes all Metainformation for a file or folder.
	 * 
	 * @param resource The name of the resource of which the Metainformations 
	 * have to be deleted.
	 */
	public void deleteAllMetainformations(String resource) {
		try {
			m_cms.deleteAllMetainformations(resource);
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Deletes a Metainformation for a file or folder.
	 * 
	 * @param resourcename The resource-name of which the Metainformation has to be delteted.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 */
	public void deleteMetainformation(String resourcename, String meta) {
		try {
			m_cms.deleteMetainformation(resourcename, meta);
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Returns the anonymous user object.
	 */
	public void anonymousUser() {
		try {
			System.out.println( m_cms.anonymousUser() );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Returns the default group of the current user.
	 */
	public void userDefaultGroup() {
		System.out.println(m_cms.getRequestContext().currentUser().getDefaultGroup());
	}

	/**
	 * Returns the current group of the current user.
	 */
	public void userCurrentGroup() {
		System.out.println(m_cms.getRequestContext().currentGroup());
	}
	
	/**
	 * Sets the current group of the current user.
	 */
	public void setUserCurrentGroup(String groupname) {
		try {
			m_cms.getRequestContext().setCurrentGroup(groupname);
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Returns the current project for the user.
	 */
	public void getCurrentProject() {
		System.out.println(m_cms.getRequestContext().currentProject());
	}
	
	/**
	 * Sets the current project for the user.
	 * 
	 * @param projectname The name of the project to be set as current.
	 */
	public void setCurrentProject(String projectname) {
		try {
			System.out.println( m_cms.getRequestContext().setCurrentProject(projectname) );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Returns a version-string for this OpenCms.
	 */
	 public void version() {
		 System.out.println(m_cms.version());
	 }	 

	/**
	 * Returns a copyright-string for this OpenCms.
	 */
	 public void copyright() {
		 String[] copy = m_cms.copyright();
		 for(int i = 0; i < copy.length; i++) {
			 System.out.println(copy[i]);
		 }
	 }	 
	 
    /**
     * Copies a resource from the online project to a new, specified project.<br>
     * Copying a resource will copy the file header or folder into the specified 
     * offline project and set its state to UNCHANGED.
     * 
	 * @param resource The name of the resource.
     */
	 public void copyResourceToProject(String resource) {
		try {
			m_cms.copyResourceToProject(resource);
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Deletes the folder.
	 * 
	 * @param foldername The complete path of the folder.
	 */	
	 public void deleteFolder(String foldername) {
		try {
			m_cms.deleteFolder(foldername);
		} catch( Exception exc ) {
			printException(exc);
		}
	 }

	/**
	 * Returns a Vector with all subfolders.<BR/>
	 * 
	 * @param foldername the complete path to the folder.
	 */
	public void getSubFolders(String foldername)
		throws CmsException { 
		try {
			Vector folders = m_cms.getSubFolders(foldername);
			for( int i = 0; i < folders.size(); i++ ) {
				System.out.println( (CmsFolder)folders.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}
	}	

	/**
	 * Locks a resource<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource.
	 * 
	 * @param resource The complete path to the resource to lock.
	 */
	public void lockResource(String resource) {
		try {
			m_cms.lockResource(resource);
		} catch( Exception exc ) {
			printException(exc);
		}
	}
	
	/**
	 * Unlocks a resource<BR/>
	 * 
	 * A user can unlock a resource, so other users may lock this file.
	 * 
	 * @param resource The complete path to the resource to lock.
	 */
	public void unlockResource(String resource) {
		try {
			m_cms.unlockResource(resource);
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Loads a File up to the cms from the lokal disc.
	 * 
	 * @param lokalfile The lokal file to load up.
	 * @param folder The folder in the cms to put the new file
	 * @param filename The name of the new file.
	 * @param type the filetype of the new file in the cms.
	 */
	public void uploadFile(String lokalfile, String folder, String filename, String type) {
		try {
			System.out.println(m_cms.createFile(folder, filename, 
												importFile(lokalfile), type));
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * @param filename The complete path to the file
	 */
	public void readFile(String filename) {
		try {
			System.out.println(m_cms.readFile(filename));
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Publishes a project.
	 * 
	 * @param name The name of the project to be published.
	 */
	public void publishProject(String name) {
		try {
			Vector resources = m_cms.publishProject(name);
			for( int i = 0; i < resources.size(); i++ ) {
				System.out.println( (String)resources.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent.
	 * 
	 * @param filename The complete path of the file to be read.
	 */
	public void readFileHeader(String filename) {
		try {
			System.out.println( m_cms.readFileHeader(filename) );
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Renames the file to the new name.
	 * 
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (No path information allowed).
	 */		
	public void renameFile(String oldname, String newname) {
		try {
			m_cms.renameFile(oldname, newname);
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Deletes the file.
	 * 
	 * @param filename The complete path of the file.
	 */	
	public void deleteFile(String filename) {
		try {
			m_cms.deleteFile(filename);
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Copies the file.
	 * 
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destination.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be copied. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void copyFile(String source, String destination) {
		try {
			m_cms.copyFile(source, destination);
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Changes the flags for this resource<BR/>
	 * 
	 * The user may change the flags, if he is admin of the resource.
	 * 
	 * @param filename The complete path to the resource.
	 * @param flags The new flags for the resource.
	 */
	public void chmod(String filename, String flags) {
		try {
			m_cms.chmod(filename, Integer.parseInt(flags));
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Changes the owner for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * @param filename The complete path to the resource.
	 * @param newOwner The name of the new owner for this resource.
	 */
	public void chown(String filename, String newOwner) {
		try {
			m_cms.chown(filename, newOwner);
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Changes the group for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * @param filename The complete path to the resource.
	 * @param newGroup The new of the new group for this resource.
	 */
	public void chgrp(String filename, String newGroup) {
		try {
			m_cms.chgrp(filename, newGroup);
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * Returns a Vector with all subfiles.<BR/>
	 * 
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfiles A Vector with all subfiles for the overgiven folder.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public void getFilesInFolder(String foldername) {
		try {
			Vector files = m_cms.getFilesInFolder(foldername);
			for( int i = 0; i < files.size(); i++ ) {
				System.out.println( (CmsFile)files.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}
	}

     /**
	 * Reads all file headers of a file in the OpenCms.<BR>
	 * This method returns a vector with the histroy of all file headers, i.e. 
	 * the file headers of a file, independent of the project they were attached to.<br>
	 * 
	 * The reading excludes the filecontent.
	 * 
	 * @param filename The name of the file to be read.
	 */
	public void readAllFileHeaders(String filename) {
		try {
			Vector files = m_cms.readAllFileHeaders(filename);
			for( int i = 0; i < files.size(); i++ ) {
				System.out.println( (A_CmsResource)files.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}
	}

    /**
	 * Returns the parent group of a group<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param groupname The name of the group.
	 */
	public void getParent(String groupname) {
		try {
			System.out.println(m_cms.getParent(groupname));
		} catch( Exception exc ) {
			printException(exc);
		}
	}

    /**
	 * Returns all child groups of a group<P/>
	 * This method also returns all sub-child groups of the current group.
	 * 
	 * @param groupname The name of the group.
	 */
	public void getChilds(String groupname) {
		try {
			Vector groups = m_cms.getChilds(groupname);
			for( int i = 0; i < groups.size(); i++ ) {
				System.out.println( (A_CmsGroup)groups.elementAt(i) );
			}
		} catch( Exception exc ) {
			printException(exc);
		}
	}

	/**
	 * This method can be called, to determine if the file-system was changed 
	 * in the past. A module can compare its previosly stored number with this
	 * returned number. If they differ, a change was made.
	 * 
	 * @return the number of file-system-changes.
	 */
	 public void getFileSystemChanges() {
		System.out.println( m_cms.getFileSystemChanges() );
	 }

	/**
	 * exports database (files, groups, users) into a specified file
	 * 
	 * @param exportFile the name (absolute Path) for the XML file
	 * @param exportPath the name (absolute Path) for the folder to export
	 */
	public void exportDb(String exportFile, String exportPath){
		try {
			m_cms.exportDb(exportFile, exportPath, C_EXPORTONLYFILES);
		} catch( Exception exc ) {
			printException(exc);
		}
	}
	
	/**
	 * imports a (files, groups, users) XML file into database
	 * 
	 * @param importPath the name (absolute Path) of folder in which should be imported
	 * @param importFile the name (absolute Path) of the XML import file
	 */
	public void importDb(String importFile, String importPath ){
		try {
			m_cms.importDb(importFile, importPath);
		} catch( Exception exc ) {
			printException(exc);
		}
	}
}
