/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsClassLoader.java,v $
* Date   : $Date: 2001/05/17 14:10:31 $
* Version: $Revision: 1.5 $
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

package com.opencms.boot;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import java.lang.reflect.*;

/**
 * This class loader loads classes from the opencms system using
 * a CmsObject object to get access to all system resources.
 * It is based on the AdaptiveClassLoader published by Apache.
 * All classes have to be in original .class format and must not
 * be packed in jar or zip files.
 * <P>
 * When the classloader finds a class, it will be cached for
 * the next request.
 * Autoload features are not implemented.
 * <P>
 * The classloader first tries to load classes and resources
 * with a parent classloader. Normally this should be the classloader
 * that loaded this loader.
 * @author Alexander Lucas
 * @version $Revision: 1.5 $ $Date: 2001/05/17 14:10:31 $
 * @see java.lang.ClassLoader
 */
public class CmsClassLoader extends ClassLoader implements I_CmsLogChannels {

    /**
     * Indicates, if classes should be reloaded
     */
    private boolean m_shouldReload = false;

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
    private Hashtable cache;

    public static final int C_REPOSITORY_CLASSIC_FS = 1;
    public static final int C_REPOSITORY_VIRTUAL_FS = 2;

    /**
     * The classpath which this classloader searches for class definitions.
     * Each element of the vector should be a String that desribes a cms folder.
     */
    private Vector repository;

    private Vector repositoryTypes;

    /**
     * The filenames incl path of the classes loaded from the virtual file system.
     * Each element of the vector is a String.
     */
    private Vector m_filenames = new Vector();

    private ClassLoader m_parent = null;

    /**
     * Some Objects we need to read classes from the virtual file system.
     */
    private Object m_cms;
    private Class m_cmsObjectClass;
    private Class m_cmsFileClass;
    private Method m_readFile;
    private Method m_getContent;

    /**
     * Creates a new class loader that will load classes from specified
     * class repositories.
     *
     * @param cms CmsObject Object to get access to system resources
     * @param classRepository An set of Strings indicating directories.
     * @throw java.lang.IllegalArgumentException if the objects contained
     *        in the vector are not valid cms folders.
     */
    //public void init(Object cms, Vector classRepository) throws IllegalArgumentException {
    public void init(Object cms) throws IllegalArgumentException {

        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && C_DEBUG && CmsBase.isLogging()) {
            CmsBase.log(C_OPENCMS_DEBUG, "[CmsClassLoader] initialize");
        }
        m_cms = cms;

        // Verify that all the repository are valid.
        /*if(classRepository == null) {
            classRepository = new Vector();
        }
        Enumeration e = classRepository.elements();
        while(e.hasMoreElements()) {
            Object o = e.nextElement();
            String file;

            // Check to see if element is a File instance.
            try {
                file = (String)o;
            }
            catch(ClassCastException objectIsNotFile) {
                throw new IllegalArgumentException("Object " + o
                        + " is not a valid \"String\" instance");
            }
        }*/
        // get the method we need to read files from opencms.
        try{
            m_cmsObjectClass = Class.forName("com.opencms.file.CmsObject", true, this);
            m_cmsFileClass = Class.forName("com.opencms.file.CmsFile", true, this);
            m_readFile = m_cmsObjectClass.getMethod("readFile", new Class[] {String.class});
            m_getContent = m_cmsFileClass.getMethod("getContents", new Class[0]);
        } catch (Exception exc){
            throw new IllegalArgumentException("Error in CmsClassloader.init() "+exc.toString() );
        }

        // Store the class repository for use
    /*    repository = classRepository;  */
        // Increment and store generation counter
//        generation = generationCounter++;
    }

    public synchronized void addRepository(String newEntry, int type) {
        if(!(repository.contains(newEntry)
                && ((Integer)repositoryTypes.elementAt(repository.indexOf(newEntry))).intValue() == type)) {
            // OK. This entry is really a new one. Add it now.
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && CmsBase.isLogging()) {
                CmsBase.log(C_OPENCMS_INFO, "[CmsClassLoader] Adding repository " + newEntry);
            }
            repository.addElement(newEntry);
            repositoryTypes.addElement(new Integer(type));
        }
    }

    /**
     * Creates a new class loader that will load classes from specified
     * class repositories.
     */
    public CmsClassLoader(){
        // Create the cache of loaded classes if it does not exist
        if (cache == null){
            cache = new Hashtable();
        }
        if (this.repository == null){
            this.repository = new Vector();
        }
        if (this.repositoryTypes == null){
            this.repositoryTypes = new Vector();
        }
        // Increment and store generation counter
        this.generation = generationCounter++;

        // Get the parent classloader
        //ClassLoader parent = getParent();
        m_parent = getClass().getClassLoader();
    }

    /**
     * Loads all the bytes of an InputStream.
     */
    /*private void copyStream(InputStream in, OutputStream out) throws IOException {
        synchronized(in) {
            synchronized(out) {
                byte[] buffer = new byte[256];
                while(true) {
                    int bytesRead = in.read(buffer);
                    if(bytesRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }*/

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
        return this.getClass().getClassLoader().getResource(name);
    }

    /**
     * Get an InputStream on a given resource.  Will return null if no
     * resource with this name is found.
     *
     * @see     java.lang.Class#getResourceAsStream(String)
     * @param   name    the name of the resource, to be used as is.
     * @return  an InputStream on the resource, or null if not found.
     */
    public InputStream getResourceAsStream(String name) {
        // First ask the parent class loader to fetch it, if possible
        InputStream s = null;
        if (m_parent != null) {
            s = m_parent.getResourceAsStream(name);
        }

        // Otherwise let's look in our own repositories
        if (s == null) {

            Enumeration allRepositories = repository.elements();
            String filename = null;
            int type = 0;

            while((allRepositories.hasMoreElements()) && (s == null)) {
                filename = (String)allRepositories.nextElement();
                type = ((Integer)repositoryTypes.elementAt(repository.indexOf(filename))).intValue();

                try {
                    if(type == C_REPOSITORY_CLASSIC_FS) {
                        File f = new File(filename);
                        if (f.isDirectory()) {
                            s = loadResourceFromDirectory(f, name);
                        } else {
                            s = loadResourceFromZipFile(f, name);
                        }
                    } else {
                        // Try to load from the virtual OpenCms filesystem

                        // check if the repository name is just a path.
                        // if so, add the complete classname and the fileextension ".class"
                        boolean fromJar = false;
                        byte[] myResourceData = null;

                        if(filename.endsWith("/")) {
                            filename = filename + name;
                        } else {
                            fromJar = true;
                        }

                        try {
                           myResourceData = readFileContent(filename);
                        }
                        catch(Exception e) {
                            // File could not be read for any reason
                            myResourceData = null;
                        }

                        if(fromJar && myResourceData != null) {
                            s = loadResourceFromZipFile(new ByteArrayInputStream(myResourceData), name);
                        } else {
                            s = new ByteArrayInputStream(myResourceData);
                        }
                    }
                } catch(IOException exc) {
                    // Error while reading data, consider it as not found
                    s = null;
                }
            } // while repositories

        } // if(s == 0)

        return s;
    }

    /**
     * Test if a file is a ZIP or JAR archive.
     *
     * @param file the file to be tested.
     * @return true if the file is a ZIP/JAR archive, false otherwise.
     */
    /*private boolean isZipOrJarArchive(String file) {
        boolean isArchive = false;
        if(file.endsWith(".zip") || file.endsWith(".jar")) {
            isArchive = true;
        }
        return isArchive;
    }*/

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
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && C_DEBUG && CmsBase.isLogging()) {
            CmsBase.log(C_OPENCMS_DEBUG, "[CmsClassLoader] Class " + name + " requested.");
        }
        Class c = null;
        // I shall not load myself, I shall not load myself
        String heyItsMe = "com.opencms.core.CmsClassLoader";
        if (heyItsMe.equals(name)  ){
            c = Class.forName(name);
            if (c != null){
                return c;
            }
        }
        // Let's have a look in our own class cache
        c = (Class)cache.get(name);
        if(c != null) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && C_DEBUG && CmsBase.isLogging()) {
                CmsBase.log(C_OPENCMS_DEBUG, "[CmsClassLoader] BINGO! Class " + name + "was found in cache.");
            }

            // bingo! the class is already loaded and is
            // stored in our classcache
            if(resolve) {
                resolveClass(c);
            }
            return c;
        }
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && C_DEBUG && CmsBase.isLogging()) {
            CmsBase.log(C_OPENCMS_DEBUG, "[CmsClassLoader] Class " + name + " was NOT found in cache.");
        }
        // No class found.



        // try to load the class using the parent class loader.
        try {
            /*ClassLoader apClassLoader = this.getClass().getClassLoader();
            InputStream fromAp = apClassLoader.getResourceAsStream(name.replace('.','/')+".class");
            byte[] myClassData = null;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            if(fromAp != null){
                copyStream(fromAp, outStream);
                myClassData = outStream.toByteArray();
            }
            // Class data successfully read. Now define a new class using this data
            if(myClassData != null) {
                try {
                    c = defineClass(null, myClassData, 0, myClassData.length);
                }
                catch(Exception e) {
                    throw new ClassNotFoundException(e.toString());
                }
                catch(Error e) {
                    throw new ClassNotFoundException("Something really bad happened while loading class " + name);
                }
                cache.put(name, c);
                if(resolve) {
                    resolveClass(c);
                }
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && C_DEBUG && A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_DEBUG, "Classloader returned class "
                            + name + " successfully!");
                }
                return c;
            }

            //c = Class.forName(name);*/

            if(m_parent != null) {
                c = m_parent.loadClass(name);
                if (c != null) {
                    if (resolve) resolveClass(c);
                    return c;
                }
            }
        }catch(ClassNotFoundException e) {
            // to continue set c back to null
            c = null;
        }catch(Exception  e) {
            // to continue set c back to null
            c = null;
        }
        if(c != null) {
            return c;
        }


        // OK. The parent loader didn't find the class.
        // first try to load the class using the systemClassloader
        /*try{
            ClassLoader sysClassLoader = this.getSystemClassLoader();
            c = sysClassLoader.loadClass(name);
        }catch(ClassNotFoundException exc){
             // to continue set c back to null
            c = null;
        }
        if(c != null) {
            return c;
        }*/


        // Even the system classloader didn't find it.
        // Then we have to search in the OpenCMS System.
        Enumeration allRepositories = repository.elements();
        String filename = null;
        int type = 0;
        byte[] myClassData = null;
        while((allRepositories.hasMoreElements()) && (myClassData == null)) {
            filename = (String)allRepositories.nextElement();
            type = ((Integer)repositoryTypes.elementAt(repository.indexOf(filename))).intValue();

            try {
                if(type == C_REPOSITORY_CLASSIC_FS) {
                    File f = new File(filename);
                    if (f.isDirectory()) {
                        myClassData = loadClassFromDirectory(f, name);
                    } else {
                        myClassData = loadClassFromZipFile(f, name);
                    }
                } else {
                    // Try to load from the virtual OpenCms filesystem

                    // check if the repository name is just a path.
                    // if so, add the complete classname and the fileextension ".class"
                    boolean fromJar = false;
                    if(filename.endsWith("/")) {
                        String classname = name.replace('.', '/');
                        filename = filename + classname + ".class";
                    } else {
                        fromJar = true;
                    }

                    try {
                       myClassData = readFileContent(filename);
                    }
                    catch(Exception e) {
                        // File could not be read for any reason
                        myClassData = null;
                    }

                    if(fromJar && myClassData != null) {
                        myClassData = loadClassFromZipFile(new ByteArrayInputStream(myClassData), name);
                    }

                }
            } catch(IOException ioe) {
                // Error while reading data, consider it as not found
                myClassData = null;
            }




/*            if(isZipOrJarArchive(filename)) {
                try {
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(C_OPENCMS_DEBUG, "Try to load archive file " + filename + ".");
                    }
                    myClassData = loadClassFromZipFile(readFile(filename), name);
                }
                catch(Exception e) {
                    myClassData = null;
                }
            }
            else  {*/

                //filename = filename + className;
                // check if the repository name is just a path.
                // if so, add the complete classname and the fileextension ".class"
                /*if(filename.endsWith("/")) {
                    String classname = name.replace('.', '/');
                    filename = filename + classname + ".class";
                }
                try {
                   myClassData = readFileContent(filename);
                }
                catch(Exception e) {
                    // File could not be read for any reason
                    myClassData = null;
                }
            }*/
        }
        if(myClassData == null) {
            throw new ClassNotFoundException(name);
        }

        // Class data successfully read. Now define a new class using this data
        if(myClassData != null) {
            try {
                c = defineClass(null, myClassData, 0, myClassData.length);
            }
            catch(ClassFormatError e) {
                throw new ClassNotFoundException(filename + " seems to be no class file. Sorry.");
            }
            catch(Exception e) {
                throw new ClassNotFoundException(e.toString());
            }
            catch(Error e) {
                throw new ClassNotFoundException("Something really bad happened while loading class " + name + ": " + e);
            }
            cache.put(name, c);
            m_filenames.addElement(filename);
            if(resolve) {
                resolveClass(c);
            }
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && C_DEBUG && CmsBase.isLogging()) {
                CmsBase.log(C_OPENCMS_DEBUG, "Classloader returned class "
                        + name + " successfully!");
            }
            return c;
        }
        throw new ClassNotFoundException(name);
    }


    /**
     * Tries to load the class from a directory.
     *
     * @param dir The directory that contains classes.
     * @param name The classname
     */
    private byte[] loadClassFromDirectory(File dir, String name) throws IOException {
        // Translate class name to file name
        String classFileName = name.replace('.', File.separatorChar) + ".class";

        // Check for garbage input at beginning of file name
        // i.e. ../ or similar
        if (!Character.isJavaIdentifierStart(classFileName.charAt(0))) {
            // Find real beginning of class name
            int start = 1;
            while (!Character.isJavaIdentifierStart(
                classFileName.charAt(start++)));
            classFileName = classFileName.substring(start);
        }

        File classFile = new File(dir, classFileName);

        if (classFile.exists()) {
            InputStream in = new FileInputStream(classFile);
            try {
                /*ByteArrayOutputStream out = new ByteArrayOutputStream();
                copyStream(in, out);
                return out.toByteArray();*/
                return getBytesFromStream(in, (int)classFile.length());
            } finally {
                in.close();
            }
        } else {
            // Not found
            return null;
        }
    }

    /**
     * Tries to load the class from a zip file.
     *
     * @param file The zipfile that contains classes.
     * @param name The classname
     * @param cache The cache entry to set the file if successful.
     */
      private byte[] loadClassFromZipFile(InputStream in, String name) throws IOException {
        String className = name.replace('.', '/') + ".class";
        //InputStream in = new ByteArrayInputStream(buf);
        ZipInputStream zipStream = new ZipInputStream(in);
        try {
         ZipEntry entry = zipStream.getNextEntry();
            while((entry != null) && (!entry.getName().equals(className))) {
                entry = zipStream.getNextEntry();
            }
            if(entry != null) {
                return getBytesFromStream(zipStream, (int)entry.getSize());
            }
        }
        finally {
            zipStream.close();
        }
        return null;
    }

     private byte[] loadClassFromZipFile(File in, String name) throws IOException {
        String className = name.replace('.', '/') + ".class";
        //InputStream in = new ByteArrayInputStream(buf);
        ZipFile zipFile = new ZipFile(in);
        try {
            ZipEntry entry = zipFile.getEntry(className);
            if(entry != null) {
                return getBytesFromStream(zipFile.getInputStream(entry), (int)entry.getSize());
            } else {
                return null;
            }
        }
        finally {
            zipFile.close();
        }
    }

    /**
     * Tries to load the class from a directory.
     *
     * @param dir The directory that contains classes.
     * @param name The classname
     */
    private InputStream loadResourceFromDirectory(File dir, String name) throws IOException {
        // Name of resources are always separated by /
        String fileName = name.replace('/', File.separatorChar);
        File resFile = new File(dir, fileName);

        if (resFile.exists()) {
            try {
                return new FileInputStream(resFile);
            } catch (FileNotFoundException shouldnothappen) {
                return null;
            }
        }
        return null;
    }

    private InputStream loadResourceFromZipFile(File in, String name) throws IOException {
        ZipFile zipFile = new ZipFile(in);
        try {
            ZipEntry entry = zipFile.getEntry(name);
            if(entry != null) {
                // Some people report problems with reading a stream
                // from a closed zip file. We better follow the workaround here
                // and create a new stream
                byte[] data= getBytesFromStream(zipFile.getInputStream(entry), (int)entry.getSize());
                if(data != null) {
                    InputStream newStream = new ByteArrayInputStream(data);
                    return newStream;
                }
            }
        }
        finally {
            zipFile.close();
        }
        return null;
    }

     private InputStream loadResourceFromZipFile(InputStream in, String name) throws IOException {
        ZipInputStream zipStream = new ZipInputStream(in);
        try {
            ZipEntry entry = zipStream.getNextEntry();
            while((entry != null) && (!entry.getName().equals(name))) {
                entry = zipStream.getNextEntry();
            }
            if(entry != null) {
                // Some people report problems with reading a stream
                // from a closed zip file. We better follow the workaround here
                // and create a new stream
                byte[] data= getBytesFromStream(zipStream, (int)entry.getSize());
                if(data != null) {
                    InputStream newStream = new ByteArrayInputStream(data);
                    return newStream;
                }
            }
        }
        finally {
            zipStream.close();
        }
        return null;
    }

    private byte[] readFileContent(String filename) {
        try {

            Object file = m_readFile.invoke(m_cms, new Object[] {filename});
            byte[] content = (byte[])m_getContent.invoke(file, new Object[0]);
            return content;

        } catch(Exception exc) {
            return null;
        }

    }

    /**
     * Returns all the files loaded from the virtual file system
     * this is for checking if the classes are changed.
     *
     * @return  a Vector with Strings containing absolute path of the classes.
     */
    public Vector getFilenames(){
        return m_filenames;
    }

    /**
     * Sets the should reload value.
     */
    public void setShouldReload(boolean value) {
        m_shouldReload = value;
    }

    /**
     * Gets the should reload value.
     */
    public boolean shouldReload() {
        return m_shouldReload;
    }


    /**
     * Loads all the bytes of an InputStream.
     */
    private byte[] getBytesFromStream(InputStream in, int length) throws IOException {
        if(length == -1) {
            // Length was set to -1.
            // Possibly the size information could not be read out of the zipfile.
            // Try to read it anyway using the available() method
            return getBytesFromStream(in);
        }

        byte[] buffer = new byte[length];
        int numRead, count = 0;

        while ((length > 0) && ((numRead = in.read(buffer, count, length)) != -1)) {
            count += numRead;
            length -= numRead;
        }

        return buffer;
    }

    /**
     * Loads all the bytes of an InputStream.
     */
    private byte[] getBytesFromStream(InputStream in) throws IOException {
        byte[] buffer = new byte[8160];
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        int numRead = 0;

        while ((in.available() > 0) && ((numRead = in.read(buffer, 0, 8160)) != -1)) {
            bas.write(buffer, 0, numRead);
        }

        return bas.toByteArray();
    }
}
