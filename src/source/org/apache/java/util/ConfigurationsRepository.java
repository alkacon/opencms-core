package source.org.apache.java.util;

/*
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
import java.util.*;

/**
 * This class must be extended by properties providers that are
 * syntax dependent.
 *
 * <p>The implementing classes should place into the encapsulated
 * <code>Hashtable</code> only properties of the form:
 * <li><code>[String key, String value]</code> for single values
 * <li><code>[String key, Vector values]</code> where the vector must be
 * a sequence of strings.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.4 $ $Date: 2003/07/12 11:29:22 $
 */
public abstract class ConfigurationsRepository extends Hashtable {

    /**
     * The file connected to this repository (holding comments and such)
     * @serial 
     */
    protected String m_file;

    /**
     * The file that contains the model of this repository
     * @serial
     */
    protected String m_model;

    /**
     * Creates an empty configuration repository.
     */
    public ConfigurationsRepository() {
        super();
    }

    /**
     * Creates a configuration repository parsing given file.
     * @param file the file
     * @throws IOException if something goes wrong
     */
    public ConfigurationsRepository(String file) throws IOException {
        this(file, null);
    }

    /**
     * Creates a configuration repository parsing given file and
     * using given model.
     * @param file the file
     * @param model the model
     * @throws IOException if something goes wrong
     */
    public ConfigurationsRepository(String file, String model) throws IOException {
        super();
        this.m_file = file;
        this.m_model = model;
        this.load(new FileInputStream(file));
    }

    /**
     * Loads from an input stream.<p>
     * @param input the stream to load from
     * @throws IOException if something goes wrong
     */
    public abstract void load(InputStream input) throws IOException;

    /**
     * Saves to an input stream.<p>
     * @param output the stream to save to
     * @param Header the header
     * @throws IOException if something goes wrong
     */
    public abstract void save(OutputStream output, String Header) throws IOException;
}
