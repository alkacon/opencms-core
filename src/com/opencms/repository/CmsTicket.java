/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/repository/Attic/CmsTicket.java,v $
 * Date   : $Date: 2003/05/28 16:46:54 $
 * Version: $Revision: 1.1 $
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

package com.opencms.repository;

import com.opencms.core.CmsException;
import com.opencms.db.CmsDriverManager;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsResource;

import javax.jcr.*;
import javax.transaction.xa.XAResource;

/**
 * Level 1 implementation of a JCR ticket.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2003/05/28 16:46:54 $
 * @since 5.1.2
 */
public class CmsTicket extends Object implements Ticket {

    /**
     * The repository associated with this ticket.
     */
    private CmsRepository m_repository;
    
    /**
     * The user credentials associated with this ticket.
     */
    private CmsCredentials m_credentials;
    
    /**
     * The driver manager associated with this ticket to access the OpenCms drivers.
     */
    private CmsDriverManager m_driverManager;

    /**
     * Creates a new ticket to access the specified repository.
     * This constructor is only within the OpenCms repository package visible.
     * 
     * @param repository
     */
    CmsTicket(CmsRepository repository) {
        m_repository = repository;
        m_credentials = m_repository.getCredentials();
        m_driverManager = m_repository.getDriverManager();
    }

    /**
     * Invoked by the Java garbage collection to release any resources.
     */
    protected void finalize() throws Throwable {
        close();
        
        m_repository = null;
        m_credentials = null;
        m_driverManager = null;
    }

    /**
     * Returns the root node of the repository.
     * 
     * @see javax.jcr.Ticket#getRootNode()
     * @see com.opencms.db.CmsDriverManager#readFolder(Ticket, String)
     */
    public Node getRootNode() throws RepositoryException {
        Node rootNode = null;

        try {
            CmsFolder folder = m_driverManager.readFolder(m_credentials.getUser(), m_credentials.getProject(), "/", false);
            rootNode = new CmsNode(this, (CmsResource) folder);
        } catch (CmsException cmsException) {
            throw new RepositoryException(cmsException.getMessage(), cmsException);
        }

        return rootNode;
    }

    /**
     * Returns the path to the node in the repository that represents the current user. 
     * 
     * @see javax.jcr.Ticket#getUserPath()
     */
    public String getUserPath() throws UnsupportedRepositoryOperationException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Returns the user ID of this ticket.
     * 
     * @see javax.jcr.Ticket#getUserId()
     */
    public String getUserId() {
        return m_credentials.getUser().getId().toString();
    }

    /**
     * Returns the node in the repository that represents the current user.
     * 
     * @see javax.jcr.Ticket#getUserNode()
     */
    public Node getUserNode() throws UnsupportedRepositoryOperationException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Close this ticket and release all associated resources.
     * 
     * @see javax.jcr.Ticket#close()
     */
    public void close() {
        // not yet implemented
    }

    /**
     * Throws an UnsupportedRepositoryOperationException. 
     * 
     * @see javax.jcr.Ticket#getXAResource()
     */
    public XAResource getXAResource() throws UnsupportedRepositoryOperationException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Throws an UnsupportedRepositoryOperationException. 
     * 
     * @see javax.jcr.Ticket#createQuery()
     */
    public Query createQuery() throws UnsupportedRepositoryOperationException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Throws an UnsupportedRepositoryOperationException. 
     * 
     * @see javax.jcr.Ticket#createQuery(java.lang.String)
     */
    public Query createQuery(String absPath) throws UnsupportedRepositoryOperationException, InvalidPathException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Throws an UnsupportedRepositoryOperationException. 
     * 
     * @see javax.jcr.Ticket#getQuery(java.lang.String)
     */
    public Query getQuery(String absPath) throws UnsupportedRepositoryOperationException, InvalidPathException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Throws an UnsupportedRepositoryOperationException. 
     * 
     * @see javax.jcr.Ticket#getObservation()
     */
    public Observation getObservation() throws UnsupportedRepositoryOperationException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Gets the AccessControl object. 
     * 
     * @see javax.jcr.Ticket#getAccessControl()
     */
    public AccessControl getAccessControl() throws UnsupportedRepositoryOperationException {
        // not yet implemented
        return null;
    }

    /**
     * Throws an UnsupportedRepositoryOperationException. 
     * 
     * @see javax.jcr.Ticket#getContentPackage(java.lang.String)
     */
    public ContentPackage getContentPackage(String absPath) throws UnsupportedRepositoryOperationException, InvalidPathException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Throws an UnsupportedRepositoryOperationException. 
     * 
     * @see javax.jcr.Ticket#getContentPackage()
     */
    public ContentPackage getContentPackage() throws UnsupportedRepositoryOperationException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Throws an UnsupportedRepositoryOperationException. 
     * 
     * @see javax.jcr.Ticket#getObjectClassRegistry()
     */
    public ObjectClassRegistry getObjectClassRegistry() throws UnsupportedRepositoryOperationException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Throws an UnsupportedRepositoryOperationException.
     * 
     * @see javax.jcr.Ticket#getSystemFolderPath()
     */
    public String getSystemFolderPath() throws UnsupportedRepositoryOperationException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Throws an UnsupportedRepositoryOperationException.
     * 
     * @see javax.jcr.Ticket#getUuidFolderPath()
     */
    public String getUuidFolderPath() throws UnsupportedRepositoryOperationException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Throws an UnsupportedRepositoryOperationException.
     * 
     * @see javax.jcr.Ticket#getAccessFolderPath()
     */
    public String getAccessFolderPath() throws UnsupportedRepositoryOperationException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Throws an UnsupportedRepositoryOperationException.
     * 
     * @see javax.jcr.Ticket#getObjectClassFolderPath()
     */
    public String getObjectClassFolderPath() throws UnsupportedRepositoryOperationException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Sets the VersionSelector being used when accessing versioned nodes through this ticket. 
     * 
     * @see javax.jcr.Ticket#setVersionSelector(javax.jcr.VersionSelector)
     */
    public void setVersionSelector(VersionSelector vs) throws UnsupportedRepositoryOperationException {
        // not yet implemented
    }

    /**
     * Gets the VersionSelector being used when accessing versioned nodes through this ticket.
     * 
     * @see javax.jcr.Ticket#getVersionSelector()
     */
    public VersionSelector getVersionSelector() throws UnsupportedRepositoryOperationException {
        // not yet implemented
        return null;
    }

    /**
     * Gets the repository associated with this ticket. 
     * 
     * @see javax.jcr.Ticket#getRepository()
     */
    public Repository getRepository() {
        return m_repository;
    }

    /**
     * Alters the state of this Ticket in accordance with the specified (new) Credentials.
     * 
     * @see javax.jcr.Ticket#impersonate(javax.jcr.Credentials)
     */
    public void impersonate(Credentials credentials) throws LoginException {
        // not yet implemented
    }

    /**
     * Returns this Ticket to its original state, before any impersonate commands that may 
     * have been performed on it.
     * 
     * @see javax.jcr.Ticket#revertToSelf()
     */
    public void revertToSelf() {
        // not yet implemented
    }

    /**
     * Returns the driver manager associated with this ticket to access the OpenCms drivers.
     * This method is only within the OpenCms repository package visible.
     * 
     * @return the driver manager associated with this ticket
     */
    CmsDriverManager getDriverManager() {
        return m_driverManager;
    }

}
