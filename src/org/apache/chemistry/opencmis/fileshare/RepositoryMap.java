/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.chemistry.opencmis.fileshare;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.server.CallContext;

/**
 * Repository map.
 */
public class RepositoryMap {

    private final Map<String, FileShareRepository> map;
    private final Map<String, String> logins;

    public RepositoryMap() {
        map = new HashMap<String, FileShareRepository>();
        logins = new HashMap<String, String>();
    }

    /**
     * Adds a repository object.
     */
    public void addRepository(FileShareRepository fsr) {
        if ((fsr == null) || (fsr.getRepositoryId() == null)) {
            return;
        }

        map.put(fsr.getRepositoryId(), fsr);
    }

    /**
     * Gets a repository object by id.
     */
    public FileShareRepository getRepository(String repositoryId) {
        // get repository object
        FileShareRepository result = map.get(repositoryId);
        if (result == null) {
            throw new CmisObjectNotFoundException("Unknown repository '" + repositoryId + "'!");
        }

        return result;
    }

    /**
     * Takes user and password from the CallContext and checks them.
     */
    public void authenticate(CallContext context) {
        // check user and password first
        if (!authenticate(context.getUsername(), context.getPassword())) {
            throw new CmisPermissionDeniedException();
        }
    }

    /**
     * Returns all repository objects.
     */
    public Collection<FileShareRepository> getRepositories() {
        return map.values();
    }

    /**
     * Adds a login.
     */
    public void addLogin(String username, String password) {
        if ((username == null) || (password == null)) {
            return;
        }

        logins.put(username.trim(), password);
    }

    /**
     * Authenticates a user against the configured logins.
     */
    private boolean authenticate(String username, String password) {
        String pwd = logins.get(username);
        if (pwd == null) {
            return false;
        }

        return pwd.equals(password);
    }
}
