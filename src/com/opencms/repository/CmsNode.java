/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/repository/Attic/CmsNode.java,v $
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
import com.opencms.file.CmsResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jcr.*;

/**
 * Level 1 implementation of a JCR node.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2003/05/28 16:46:54 $
 * @since 5.1.2
 */
public class CmsNode extends Object implements Node {

    /**
     * The ticket associated with this node.
     */
    private CmsTicket m_ticket;
    
    /**
     * The Cms resource associated with this node.
     */
    private CmsResource m_resource;
    
    /**
     * The driver manager associated with this node to access the OpenCms drivers.
     */
    private CmsDriverManager m_driverManager;

    public CmsNode(CmsTicket ticket, CmsResource resource) {
        m_ticket = ticket;
        m_driverManager = m_ticket.getDriverManager();
        m_resource = resource;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addNode(java.lang.String)
     */
    public Node addNode(String path) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addNode(java.lang.String, boolean)
     */
    public Node addNode(String path, boolean adjustName) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addNode(java.io.InputStream)
     */
    public Node addNode(InputStream in) throws ElementExistsException, ParentChildMismatchException, InvalidSerializedDataException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addNode(java.io.InputStream, boolean)
     */
    public Node addNode(InputStream in, boolean adjustName) throws ElementExistsException, ParentChildMismatchException, InvalidSerializedDataException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addNode(java.lang.String, java.lang.String[])
     */
    public Node addNode(String path, String[] objectClasses) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, NoSuchObjectClassException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addNode(java.lang.String, java.lang.String[], boolean)
     */
    public Node addNode(String path, String[] objectClasses, boolean adjustName) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, NoSuchObjectClassException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addNode(java.lang.String, java.lang.String[], boolean, java.lang.String)
     */
    public Node addNode(String path, String[] objectClasses, boolean adjustName, String versionSpec) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, NoSuchObjectClassException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addFile(java.lang.String)
     */
    public Node addFile(String path) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addFile(java.lang.String, boolean)
     */
    public Node addFile(String path, boolean adjustName) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#isFile()
     */
    public boolean isFile() {
        // not yet implemented
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addFolder(java.lang.String)
     */
    public Node addFolder(String path) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addFolder(java.lang.String, boolean)
     */
    public Node addFolder(String path, boolean adjustName) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#isFolder()
     */
    public boolean isFolder() {
        // not yet implemented
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addHierarchyNode(java.lang.String)
     */
    public Node addHierarchyNode(String path) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addHierarchyNode(java.lang.String, boolean)
     */
    public Node addHierarchyNode(String path, boolean adjustName) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#isHierarchyNode()
     */
    public boolean isHierarchyNode() {
        // not yet implemented
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addNodes(java.util.List)
     */
    public NodeIterator addNodes(List list) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, NoSuchObjectClassException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addProperty(java.lang.String)
     */
    public Property addProperty(String path) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addProperty(java.lang.String, boolean)
     */
    public Property addProperty(String path, boolean adjustName) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addProperty(java.lang.String, javax.jcr.Value)
     */
    public Property addProperty(String path, Value value) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, ValueFormatException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addProperty(java.lang.String, javax.jcr.Value, boolean)
     */
    public Property addProperty(String path, Value value, boolean adjustName) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, ValueFormatException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addProperty(java.lang.String, javax.jcr.Value, javax.jcr.PropertyType)
     */
    public Property addProperty(String path, Value value, PropertyType type) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, ValueFormatException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addProperty(java.lang.String, javax.jcr.Value, javax.jcr.PropertyType, boolean)
     */
    public Property addProperty(String path, Value value, PropertyType type, boolean adjustName) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, ValueFormatException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addProperty(java.lang.String, java.lang.String)
     */
    public Property addProperty(String path, String value) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, ValueFormatException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addProperty(java.lang.String, java.lang.String, boolean)
     */
    public Property addProperty(String path, String value, boolean adjustName) throws ElementExistsException, InvalidPathException, ParentChildMismatchException, ValueFormatException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#moveTo(java.lang.String)
     */
    public void moveTo(String absPath) throws ParentChildMismatchException, InvalidPathException, RepositoryException {
        // not yet implemented

    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#copyTo(java.lang.String)
     */
    public void copyTo(String absPath) throws ParentChildMismatchException, InvalidPathException, RepositoryException {
        // not yet implemented

    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getElement(java.lang.String)
     */
    public Element getElement(String path) throws ElementNotFoundException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getElement(java.lang.String, java.util.Calendar)
     */
    public Element getElement(String path, Calendar date) throws ElementNotFoundException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getElement(java.lang.String, java.lang.String)
     */
    public Element getElement(String path, String versionSpec) throws ElementNotFoundException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addProperties(java.util.List)
     */
    public PropertyIterator addProperties(List list) throws ElementExistsException, InvalidPathException, ValueFormatException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getNode(java.lang.String)
     */
    public Node getNode(String path) throws ElementNotFoundException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getNode(java.lang.String, java.lang.String)
     */
    public Node getNode(String path, String versionSpec) throws ElementNotFoundException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getNode(java.lang.String, java.util.Calendar)
     */
    public Node getNode(String path, Calendar date) throws ElementNotFoundException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getProperty(java.lang.String)
     */
    public Property getProperty(String path) throws ElementNotFoundException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getProperty(java.lang.String, java.lang.String)
     */
    public Property getProperty(String path, String versionSpec) throws ElementNotFoundException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getProperty(java.lang.String, java.util.Calendar)
     */
    public Property getProperty(String path, Calendar date) throws ElementNotFoundException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#findProperties(javax.jcr.Value)
     */
    public PropertyIterator findProperties(Value value) {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#findProperty(javax.jcr.Value)
     */
    public Property findProperty(Value value) {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getElements()
     */
    public ElementIterator getElements() {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getElements(int)
     */
    public ElementIterator getElements(int depth) {
        // not yet implemented
        return null;
    }

    /**
     * Returns a NodeIterator over all child Nodes of this Node. 
     * Does not include properties of this Node.
     * 
     * @see javax.jcr.Node#getNodes()
     */
    public NodeIterator getNodes() {
        ArrayList childResources = new ArrayList();
        ArrayList childNodes = new ArrayList();

        try {
            // the "site root" has to be added here!
            CmsCredentials credentials = ((CmsRepository) m_ticket.getRepository()).getCredentials();
            childResources.addAll(m_driverManager.getResourcesInFolder(credentials.getUser(), credentials.getProject(), getPath()));

            Iterator i = childResources.iterator();
            while (i.hasNext()) {
                childNodes.add(new CmsNode(m_ticket, (CmsResource) i.next()));
            }
        } catch (CmsException e) {
            childResources.clear();
        }

        return (NodeIterator) Collections.unmodifiableList(childNodes).iterator();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getNodes(int)
     */
    public NodeIterator getNodes(int depth) {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getProperties()
     */
    public PropertyIterator getProperties() {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#remove(java.lang.String)
     */
    public void remove(String path) throws InvalidPathException, RepositoryException {
        // not yet implemented

    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#hasElement(java.lang.String)
     */
    public boolean hasElement(String path) {
        // not yet implemented
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#hasNode(java.lang.String)
     */
    public boolean hasNode(String path) {
        // not yet implemented
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#hasProperty(java.lang.String)
     */
    public boolean hasProperty(String path) {
        // not yet implemented
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#hasElements()
     */
    public boolean hasElements() {
        // not yet implemented
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#hasNodes()
     */
    public boolean hasNodes() {
        // not yet implemented
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#hasProperties()
     */
    public boolean hasProperties() {
        // not yet implemented
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addHardLink(java.lang.String)
     */
    public Node addHardLink(String absPath) throws UnsupportedRepositoryOperationException, ParentChildMismatchException, InvalidPathException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addHardLink(java.lang.String, java.lang.String)
     */
    public Node addHardLink(String absPath, String newName) throws UnsupportedRepositoryOperationException, ParentChildMismatchException, InvalidPathException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addHardLink(javax.jcr.Node)
     */
    public Node addHardLink(Node childNode) throws UnsupportedRepositoryOperationException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addHardLink(javax.jcr.Node, java.lang.String)
     */
    public Node addHardLink(Node childNode, String newName) throws UnsupportedRepositoryOperationException, ParentChildMismatchException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getObjectClasses()
     */
    public ObjectClassIterator getObjectClasses() throws UnsupportedRepositoryOperationException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#isObjectClass(java.lang.String)
     */
    public boolean isObjectClass(String objectClassName) throws UnsupportedRepositoryOperationException {
        // not yet implemented
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#serialize(java.io.OutputStream, boolean, boolean)
     */
    public void serialize(OutputStream out, boolean binaryAsLink, boolean noRecurse) throws IOException, RepositoryException {
        // not yet implemented

    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#orderElement(java.lang.String, java.lang.String)
     */
    public void orderElement(String path, String beforeName) throws InvalidPathException, RepositoryException {
        // not yet implemented

    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setSortOrder(java.lang.String[])
     */
    public void setSortOrder(String[] names) throws RepositoryException {
        // not yet implemented

    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#createVersion()
     */
    public void createVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        // not yet implemented

    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#createVersion(java.lang.String)
     */
    public void createVersion(String versionLabel) throws UnsupportedRepositoryOperationException, InvalidVersionLabelException, RepositoryException {
        // not yet implemented

    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#createVersion(java.lang.String, java.lang.String)
     */
    public void createVersion(String versionLabel, String comment) throws UnsupportedRepositoryOperationException, InvalidVersionLabelException, RepositoryException {
        // not yet implemented

    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getVersionDate()
     */
    public Calendar getVersionDate() throws UnsupportedRepositoryOperationException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getVersionLabels()
     */
    public StringIterator getVersionLabels() throws UnsupportedRepositoryOperationException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#restoreVersion(javax.jcr.Node)
     */
    public Node restoreVersion(Node toBeRestored) throws UnsupportedRepositoryOperationException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#restoreVersion(java.util.Calendar)
     */
    public Node restoreVersion(Calendar date) throws UnsupportedRepositoryOperationException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#restoreVersion(java.lang.String)
     */
    public Node restoreVersion(String versionSpec) throws UnsupportedRepositoryOperationException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getVersion(java.util.Calendar)
     */
    public Node getVersion(Calendar date) throws UnsupportedRepositoryOperationException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getVersion(java.lang.String)
     */
    public Node getVersion(String versionLabel) throws UnsupportedRepositoryOperationException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getAllVersions()
     */
    public NodeIterator getAllVersions() throws UnsupportedRepositoryOperationException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value)
     */
    public Property setProperty(String name, Value value) throws ElementNotFoundException, ValueFormatException, StaleValueException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String)
     */
    public Property setProperty(String name, String value) throws ElementNotFoundException, ValueFormatException, StaleValueException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, java.io.InputStream)
     */
    public Property setProperty(String name, InputStream value) throws ElementNotFoundException, ValueFormatException, StaleValueException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, boolean)
     */
    public Property setProperty(String name, boolean value) throws ElementNotFoundException, ValueFormatException, StaleValueException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, double)
     */
    public Property setProperty(String name, double value) throws ElementNotFoundException, ValueFormatException, StaleValueException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, long)
     */
    public Property setProperty(String name, long value) throws ElementNotFoundException, ValueFormatException, StaleValueException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, java.util.Calendar)
     */
    public Property setProperty(String name, Calendar value) throws ElementNotFoundException, ValueFormatException, StaleValueException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperties(java.util.List)
     */
    public PropertyIterator setProperties(List list) throws ElementNotFoundException, ValueFormatException, StaleValueException, RepositoryException {
        // not yet implemented
        return null;
    }

    /**
     * Returns the path to this Element.
     * 
     * @see javax.jcr.Element#getPath()
     * @see com.opencms.file.CmsResource#getAbsolutePath()
     */
    public String getPath() {
        return m_resource.getAbsolutePath();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Element#getPaths()
     */
    public StringIterator getPaths() {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Element#getName()
     */
    public String getName() {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Element#getAncestor(int)
     */
    public Element getAncestor(int degree) throws ElementNotFoundException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Element#getParent()
     */
    public Node getParent() throws ElementNotFoundException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Element#getParents()
     */
    public NodeIterator getParents() throws ElementNotFoundException, RepositoryException {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Element#getTicket()
     */
    public Ticket getTicket() {
        // not yet implemented
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Element#accept(javax.jcr.ElementVisitor)
     */
    public void accept(ElementVisitor visitor) throws RepositoryException {
        // not yet implemented

    }

    /* (non-Javadoc)
     * @see javax.jcr.Element#isNode()
     */
    public boolean isNode() {
        // not yet implemented
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Element#isProperty()
     */
    public boolean isProperty() {
        // not yet implemented
        return false;
    }

}
