package com.opencms.core;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsClassLoader.java,v $
 * Date   : $Date: 2000/08/31 15:10:36 $
 * Version: $Revision: 1.15 $
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
 *
 *
 * This software is based on the Apache Project.
 *
 *
 * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and 
 *    "Java Apache Project" must not be used to endorse or promote products 
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *    
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import com.opencms.file.*;

/**
 * This class loader loads classes from the opencms system using
 * a CmsObject object to get access to all system resources.
 * It is based on the AdaptiveClassLoader published by Apache.
 * All classes have to be in original .class format and must not
 * be packed in jar or zip files.
 * <P>
 * When the classloader finds a class, it will be cached for
 * the next request. The only way to delete the cache is
 * to restart the servlet-zone! There are no methods
 * to clean up the cache and restart the classloader.
 * We can do this since in opencms production environments
 * no classes will be updated.
 * Autoload features are not implemented.
 * <P>
 * The classloader first tries to load classes and resources
 * with a parent classloader. Normally this should be the classloader 
 * that loaded this loader. 
 * @author Alexander Lucas
 * @version $Revision: 1.15 $ $Date: 2000/08/31 15:10:36 $
 * @see java.lang.ClassLoader
 */
public class CmsClassLoader extends ClassLoader implements I_CmsLogChannels {

	/** Boolean for additional debug output control */
	private static final boolean C_DEBUG = false;

	/**
	 * Generation counter, incremented for each classloader as they are
	 * created.
	 */
	static private int generationCounter = 0;

	/**
	 * Generation number of the classloader, used to distinguish between
	 * different instances.
	 */
	private int generation;

	/**
	 * Cache of the loaded classes. This contains Classes keyed
	 * by class names.
	 */
	private static Hashtable cache;

	/**
	 * The classpath which this classloader searches for class definitions.
	 * Each element of the vector should be a String that desribes a cms folder.
	 */
	private Vector repository;
	
	private CmsObject m_cms;

	/**
	 * Loads all the bytes of an InputStream.
	 */
	private void copyStream(InputStream in, OutputStream out)
		throws IOException
	{
		synchronized (in) {
			synchronized (out) {
				byte[] buffer = new byte[256];
				while (true) {
					int bytesRead = in.read(buffer);
					if (bytesRead == -1) break;
					out.write(buffer,0,bytesRead);
				}
			}
		}
	}
	/**
	 * Get an InputStream on a given resource.  Will return null if no
	 * resource with this name is found.
	 * <p>
	 * To be implemented.
	 *
	 * @see     java.lang.Class#getResourceAsStream(String)
	 * @param   name    the name of the resource, to be used as is.
	 * @return  an InputStream on the resource, or null if not found.
	 */
	public InputStream getResourceAsStream(String name) {
		return null;
	}
	/**
	 * Find a resource with a given name.  The return is a URL to the
	 * resource. Doing a getContent() on the URL may return an Image,
	 * an AudioClip,or an InputStream.
	 * <P>
	 * To be implemented
	 * 
	 * @param   name    the name of the resource, to be used as is.
	 * @return  an URL on the resource, or null if not found.
	 */
	public URL getResource(String name) {
		return null;
	}
	/**
	 * Test if a file is a ZIP or JAR archive.
	 *
	 * @param file the file to be tested.
	 * @return true if the file is a ZIP/JAR archive, false otherwise.
	 */
	private boolean isZipOrJarArchive(String file) {
		boolean isArchive = false;
		if(file.endsWith(".zip")||file.endsWith(".jar"))
			isArchive = true;
		return isArchive;
	}
	/**
	 * Creates a new class loader that will load classes from specified
	 * class repositories.
	 *
	 * @param cms CmsObject Object to get access to system resources
	 * @param classRepository An set of Strings indicating directories.
	 * @param parent Parent classloader that should be called first before 
	 *        trying to load classes from the opencms system.
	 * @throw java.lang.IllegalArgumentException if the objects contained
	 *        in the vector are not valid cms folders.
	 */
	public CmsClassLoader(CmsObject cms, Vector classRepository, ClassLoader parent)
		throws IllegalArgumentException { 

		// Create the cache of loaded classes
		cache = new Hashtable();

		// Verify that all the repository are valid.
		
		if(classRepository == null) {
			classRepository = new Vector();
		}
		
		Enumeration e = classRepository.elements();
		while(e.hasMoreElements()) {
			Object o = e.nextElement();
			String file;

			// Check to see if element is a File instance.
			try {
				file = (String)o;
			} catch (ClassCastException objectIsNotFile) {
				throw new IllegalArgumentException("Object " + o
					+ " is not a valid \"String\" instance");
			}

			// Check to see if we have proper access.
			try {
				cms.readFolder(file);
			} catch (Exception exc) {
				throw new IllegalArgumentException("Repository "
					+ file + " could not be accessed while initializing class loader.");
		   }
			
		}


		// Store the class repository for use
		this.repository = classRepository;

		// Increment and store generation counter
		this.generation = generationCounter++;
		
		this.m_cms = cms;        
	}
	 /**
	 * Tries to load the class from a zip file.
	 *
	 * @param file The zipfile that contains classes.
	 * @param name The classname
	 * @param cache The cache entry to set the file if successful.
	 */
	private byte[] loadClassFromZipFile(CmsFile file, String name)
			throws IOException
	{
		String className = name.replace('.', '/') + ".class"; 
		InputStream in = new ByteArrayInputStream(file.getContents());
		ZipInputStream zipStream = new ZipInputStream(in);
		
		try {
			ZipEntry entry = zipStream.getNextEntry(); 
			while ((!entry.getName().equals(className))&&(entry != null)) {
				entry = zipStream.getNextEntry();
			}
			if (entry != null) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				copyStream(zipStream, out);
				return out.toByteArray();
			} 
			else {
				return null;
				}
		} finally {
			zipStream.close();
		}
	}
	/**
	 * Resolves the specified name to a Class. The method loadClass()
	 * is called by the virtual machine.  As an abstract method,
	 * loadClass() must be defined in a subclass of ClassLoader.
	 *
	 * @param      name the name of the desired Class.
	 * @param      resolve true if the Class needs to be resolved;
	 *             false if the virtual machine just wants to determine
	 *             whether the class exists or not
	 * @return     the resulting Class.
	 * @exception  ClassNotFoundException  if the class loader cannot
	 *             find a the requested class.
	 */
	protected synchronized Class loadClass(String name, boolean resolve)
		throws ClassNotFoundException { 

		if(C_DEBUG && A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsClassLoader] Class " + name + " requested.");
		}
		
		Class c = null;
		CmsFile classFile = null;
				
		// first try to load the class using the parent class loader.
		try {
			
			c = Class.forName(name);
		} catch(ClassNotFoundException e) {
			// to continue set c back to null
			
			c = null;
		}
			
		if(c != null) {
			return c;
		}
			  
		// OK. The parent loader didn't find the class.
		// Let's have a look in our own class cache
		
		c = (Class)cache.get(name);		
		if(c != null) {
			
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_DEBUG, "BINGO! Class " + name + "was found in cache.");
			}
			// bingo! the class is already loaded and is
			// stored in our classcache
			if(resolve) {
				resolveClass(c);
			}
			return c;
		}
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_DEBUG,"Class " + name + "was NOT found in cache.");
		}
		
		// No class found.
		// Then we have to search in the OpenCMS System.
	   
		Enumeration allRepositories = repository.elements();
		String filename = null;
		byte[] myClassData = null;
		
		while( (allRepositories.hasMoreElements()) && (classFile==null)) {
			filename = (String)allRepositories.nextElement();
			System.err.println("****Testing repository "+filename);
			
			if (isZipOrJarArchive(filename)) {
				try {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(C_OPENCMS_DEBUG,"Try to load archive file " + filename + ".");
					}
					myClassData = loadClassFromZipFile(m_cms.readFile(filename),name);
				} catch (Exception e) { 
					myClassData = null;
				}
			}
			else {
  			
				//filename = filename + className;
				// check if the repository name is just a path.
				// if so, add the complete classname and the fileextension ".class"
				if (filename.endsWith("/")) {
					String classname=name.replace('.','/');
					filename=filename+classname+".class";
				}
				try {
					System.err.println("****Try to load file "+filename);
					classFile = m_cms.readFile(filename);
					System.err.println("****Got classfile "+classFile);
					myClassData = classFile.getContents();
				} catch(Exception e) {
					// File could not be read for any reason
					classFile = null;
					myClassData = null;
				}				
				
			}
		}

		if(classFile == null) {
					throw new ClassNotFoundException(name);
		}
		
		// Class data successfully read. Now define a new class using this data
		if(myClassData != null) {
			try {
				c = defineClass(null, myClassData, 0, myClassData.length);
			} catch(ClassFormatError e) {
				throw new ClassNotFoundException(filename + " seems to be no class file. Sorry.");
			} catch(Exception e) {
				throw new ClassNotFoundException(e.toString());
			} catch(Error e) {
				throw new ClassNotFoundException("Something really bad happened while loading class " + filename);
			} 

			
			cache.put(name, c);
			if(resolve) {
				resolveClass(c);
			}
			
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_DEBUG,"Classloader returned class " + name + " successfully!");
			}
			return c;
		}
		throw new ClassNotFoundException(name);
	}
}
