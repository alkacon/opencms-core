/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryPrincipal;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * Definitions of all required history driver methods.<p>
 *
 * A history driver is a driver to write projects, resources and properties of
 * resources optionally to a second set of history database tables while resources
 * get published. A unique publish tag ID is used to identify a set of resource that
 * were saved during one publish process.<p>
 *
 * @since 6.9.1
 */
public interface I_CmsHistoryDriver {

    /** The type ID to identify history driver implementations. */
    int DRIVER_TYPE_ID = 0;

    /**
     * Creates a new property defintion in the database.<p>
     *
     * @param dbc the current database context
     * @param name the name of the property definition
     * @param type the type of the property definition
     *
     * @return the new property definition object
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsPropertyDefinition createPropertyDefinition(
        CmsDbContext dbc,
        String name,
        CmsPropertyDefinition.CmsPropertyType type) throws CmsDataAccessException;

    /**
     * Deletes all historical versions of a resource
     * keeping maximal <code>versionsToKeep</code> versions.<p>
     *
     * @param dbc the current database context
     * @param histResource the historical resource to delete versions for
     * @param versionsToKeep the number of versions to keep
     * @param time deleted resources older than this will also be deleted, is ignored if negative
     *
     * @return the number of versions that were deleted
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    int deleteEntries(CmsDbContext dbc, I_CmsHistoryResource histResource, int versionsToKeep, long time)
    throws CmsDataAccessException;

    /**
     * Deletes a property definition.<p>
     *
     * @param dbc the current database context
     * @param propertyDef the property definition to be deleted
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void deletePropertyDefinition(CmsDbContext dbc, CmsPropertyDefinition propertyDef) throws CmsDataAccessException;

    /**
     * Destroys this driver.<p>
     *
     * @throws Throwable if something goes wrong
     */
    void destroy() throws Throwable;

    /**
     * Returns all historical resources (of deleted resources).<p>
     *
     * @param dbc the current database context
     *
     * @return a list of {@link I_CmsHistoryResource} objects
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<I_CmsHistoryResource> getAllDeletedEntries(CmsDbContext dbc) throws CmsDataAccessException;

    /**
     * Returns all historical resources (of not deleted resources).<p>
     *
     * @param dbc the current database context
     *
     * @return a list of {@link I_CmsHistoryResource} objects
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<I_CmsHistoryResource> getAllNotDeletedEntries(CmsDbContext dbc) throws CmsDataAccessException;

    /**
     * Returns the SqlManager of this driver.<p>
     *
     * @return the SqlManager of this driver
     */
    CmsSqlManager getSqlManager();

    /**
     * Initializes the SQL manager for this driver.<p>
     *
     * @param classname the classname of the SQL manager
     *
     * @return the SQL manager for this driver
     */
    CmsSqlManager initSqlManager(String classname);

    /**
     * Reads all file headers of the resource with the given structure id.<p>
     *
     * This method returns a list with the history of the resource, i.e.
     * the historical resources, independent of the project they were attached to.<br>
     *
     * The reading excludes the file content.<p>
     *
     * @param dbc the current database context
     * @param structureId the structure id
     *
     * @return a list of historical resources, as <code>{@link I_CmsHistoryResource}</code> objects
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<I_CmsHistoryResource> readAllAvailableVersions(CmsDbContext dbc, CmsUUID structureId)
    throws CmsDataAccessException;

    /**
     * Reads the content of the historical version of the resource
     * identified by its structure id.<p>
     *
     * @param dbc the current database context
     * @param resourceId the resource id of the resource to read the content for
     * @param publishTag the publish tag of the version
     *
     * @return the content if found
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    byte[] readContent(CmsDbContext dbc, CmsUUID resourceId, int publishTag) throws CmsDataAccessException;

    /**
     * Reads all deleted (historical) resources below the given path, that the given user deleted by itself.<p>
     *
     * @param dbc the current db context
     * @param structureId the structure id of the parent resource to read the deleted resources from
     * @param userId the id of the user that deleted the resources, or <code>null</code> to retrieve them all
     *
     * @return a list of <code>{@link I_CmsHistoryResource}</code> objects
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<I_CmsHistoryResource> readDeletedResources(CmsDbContext dbc, CmsUUID structureId, CmsUUID userId)
    throws CmsDataAccessException;

    /**
     * Returns the last historical version of a resource.<p>
     *
     * @param dbc the current database context
     * @param structureId the structure ID of the resource
     *
     * @return the last historical version of a resource
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    int readLastVersion(CmsDbContext dbc, CmsUUID structureId) throws CmsDataAccessException;

    /**
     * Reads the maximal publish tag for a specified resource id.<p>
     *
     * @param dbc the current database context
     * @param resourceId the id of the resource the get the publish tag for
     *
     * @return the maximal publish tag for the given resource
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    int readMaxPublishTag(CmsDbContext dbc, CmsUUID resourceId) throws CmsDataAccessException;

    /**
     * Returns the next available history publish tag.<p>
     *
     * @param dbc the current database context
     *
     * @return the next available history publish tag
     */
    int readNextPublishTag(CmsDbContext dbc);

    /**
     * Reads an historical principal entry.<p>
     *
     * @param dbc the current database context
     * @param principalId the id of the principal to retrieve
     *
     * @return the historical principal entry
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsHistoryPrincipal readPrincipal(CmsDbContext dbc, CmsUUID principalId) throws CmsDataAccessException;

    /**
     * Reads the latest historical project version with the given id.<p>
     *
     * @param dbc the current database context
     * @param projectId the project id
     *
     * @return the requested historical project
     *
     * @throws CmsDataAccessException is something goes wrong
     */
    CmsHistoryProject readProject(CmsDbContext dbc, CmsUUID projectId) throws CmsDataAccessException;

    /**
     * Reads an historical project version.<p>
     *
     * @param dbc the current database context
     * @param publishTag the publish tag
     *
     * @return the requested historical project
     *
     * @throws CmsDataAccessException is something goes wrong
     */
    CmsHistoryProject readProject(CmsDbContext dbc, int publishTag) throws CmsDataAccessException;

    /**
     * Reads all resources that belong to the historical project identified by the given publish tag.<p>
     *
     * @param dbc the current database context
     * @param publishTag the publish tag
     *
     * @return all resources that belong to the historical project identified by the given publish tag
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<String> readProjectResources(CmsDbContext dbc, int publishTag) throws CmsDataAccessException;

    /**
     * Returns all projects from the history.<p>
     *
     * @param dbc the current database context
     *
     * @return list of <code>{@link CmsHistoryProject}</code> objects
     *           with all projects from history.
     *
     * @throws CmsDataAccessException if an error occurs
     */
    List<CmsHistoryProject> readProjects(CmsDbContext dbc) throws CmsDataAccessException;

    /**
     * Returns a list of all properties of a historical file or folder.<p>
     *
     * @param dbc the current database context
     * @param historicalResource the resource to read the properties from
     *
     * @return a list of {@link org.opencms.file.CmsProperty} objects
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsProperty> readProperties(CmsDbContext dbc, I_CmsHistoryResource historicalResource)
    throws CmsDataAccessException;

    /**
     * Reads a property definition with the given name.<p>
     *
     * @param dbc the current database context
     * @param name the name of the property definition to read
     *
     * @return the property definition that corresponds to the given arguments - or <code>null</code> if not found
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsPropertyDefinition readPropertyDefinition(CmsDbContext dbc, String name) throws CmsDataAccessException;

    /**
     * Gets the publish tag of the first historical project after a given date.<p>
     *
     * This method is used during the deletion process of older historical data.<p>
     *
     * @param dbc the current database context
     * @param maxdate the date to compare the historical projects with
     *
     * @return publish tag of the first historical project after maxdate
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    int readPublishTag(CmsDbContext dbc, long maxdate) throws CmsDataAccessException;

    /**
     * Reads a historical resource version without including the file content.<p>
     *
     * @param dbc the current database context
     * @param structureId the structure id of the resource to read
     * @param version the desired version number
     *
     * @return the historical resource version
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    I_CmsHistoryResource readResource(CmsDbContext dbc, CmsUUID structureId, int version) throws CmsDataAccessException;

    /**
     * Sets the driver manager for this driver if possible.<p>
     *
     * @param driverManager the new driver manager
     */
    void setDriverManager(CmsDriverManager driverManager);

    /**
     * Sets the SQL manager for this driver if possible.<p>
     *
     * @param sqlManager the new SQL manager
     */
    void setSqlManager(CmsSqlManager sqlManager);

    /**
     * Writes an historical entry for the given principal.<p>
     *
     * @param dbc the current database context
     * @param principal the principal to write
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void writePrincipal(CmsDbContext dbc, I_CmsPrincipal principal) throws CmsDataAccessException;

    /**
     * Creates an historical entry for the current project.<p>
     *
     * @param dbc the current database context
     * @param publishTag the publish tag
     * @param publishDate long timestamp when the current project was published
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeProject(CmsDbContext dbc, int publishTag, long publishDate) throws CmsDataAccessException;

    /**
     * Writes the properties of a resource to the history.<p>
     *
     * @param dbc the current database context
     * @param resource the resource of the properties
     * @param properties the properties to write
     * @param publishTag the publish tag
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeProperties(CmsDbContext dbc, CmsResource resource, List<CmsProperty> properties, int publishTag)
    throws CmsDataAccessException;

    /**
     * Writes a resource to the history.<p>
     *
     * @param dbc the current database context
     * @param resource the resource that is written to the history
     * @param properties the properties of the resource
     * @param publishTag the publish tag
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeResource(CmsDbContext dbc, CmsResource resource, List<CmsProperty> properties, int publishTag)
    throws CmsDataAccessException;
}
