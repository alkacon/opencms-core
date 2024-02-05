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

package org.opencms.main;

/**
 * Implement this interface in case your class has to react
 * to CmsEvents that are thrown by system.<p>
 *
 * In order to receive system events, your class must register with
 * the OpenCms event mechanism. This can be done in the constructor of a class
 * like this:
 * <pre>
 * org.opencms.main.OpenCms.addCmsEventListener(this);
 * </pre>
 *
 * A typical implementation might look like this:
 * <pre>
 * public void cmsEvent(org.opencms.main.CmsEvent event) {
 *     switch (event.getType()) {
 *         case org.opencms.main.I_CmsEventListener.EVENT_PUBLISH_PROJECT:
 *         case org.opencms.main.I_CmsEventListener.EVENT_CLEAR_CACHES:
 *             // do something
 *             break;
 *         case org.opencms.main.I_CmsEventListener.EVENT_LOGIN_USER:
 *            // do something else
 *             break;
 *         }
 * }
 * </pre>
 *
 * @since 6.0.0
 *
 * @see CmsEvent
 * @see org.opencms.main.OpenCms#addCmsEventListener(I_CmsEventListener)
 * @see org.opencms.main.OpenCms#addCmsEventListener(I_CmsEventListener, int[])
 */
public interface I_CmsEventListener {

    /**
     * Event "a project is to published" (but has not yet been published).<p>
     *
     * Event data:
     * <ul>
     * <li><code>{@link #KEY_REPORT}</code>: a <code>{@link org.opencms.report.I_CmsReport}</code> to print output messages to</li>
     * <li><code>{@link #KEY_PUBLISHLIST}</code>: a <code>{@link org.opencms.db.CmsPublishList}</code> that contains the resources that are to be published</li>
     * <li><code>{@link #KEY_PROJECTID}</code>: the ID of the project that is to be published</li>
     * <li><code>{@link #KEY_DBCONTEXT}</code>: the current users database context</li>
     * </ul>
     *
     * @see org.opencms.publish.CmsPublishManager#publishProject(org.opencms.file.CmsObject)
     * @see #EVENT_PUBLISH_PROJECT
     */
    int EVENT_BEFORE_PUBLISH_PROJECT = 3;

    /**
     * Event "all caches must be cleared".<p>
     *
     * Not thrown by the core classes, but might be used in modules.
     */
    int EVENT_CLEAR_CACHES = 5;

    /**
     * Event "clear all offline caches".<p>
     *
     * Event data: none
     */
    int EVENT_CLEAR_OFFLINE_CACHES = 16;

    /**
     * Event "clear all online caches".<p>
     *
     * Event data: none
     */
    int EVENT_CLEAR_ONLINE_CACHES = 17;

    /**
     * Event "all caches related to user and groups must be cleared".<p>
     *
     * Not thrown by the core classes, but might be used in modules.
     */
    int EVENT_CLEAR_PRINCIPAL_CACHES = 6;

    /**
     * Event "the FlexCache must be cleared".<p>
     *
     * This is thrown on the "FlexCache Administration" page if you press
     * one ot the "Clear cache" buttons, or if you use the <code>_flex=clearcache</code>
     * request parameter.
     */
    int EVENT_FLEX_CACHE_CLEAR = 9;

    /**
     * Event "delete all JSP pages in the "real" file system
     * (so they will be rebuild next time the JSP is requested)".<p>
     *
     * This is thrown on the "FlexCache Administration" page if you press
     * the button "Purge JSP repository", or if you use the <code>_flex=purge</code>
     * request parameter.
     */
    int EVENT_FLEX_PURGE_JSP_REPOSITORY = 8;

    /**
     * Event "full static export".<p>
     *
     * This is thrown in {@link org.opencms.staticexport.CmsStaticExportManager}.
     *
     * Event data:
     * <ul>
     * <li>key "purge": the boolean value to purge the export folders first</li>
     * <li><code>{@link #KEY_REPORT}</code>:  a <code>{@link org.opencms.report.I_CmsReport}</code> to print output messages to</li>
     * </ul>
     */
    int EVENT_FULLSTATIC_EXPORT = 4;

    /**
     * Event "group modified".<p>
     *
     * Includes create, write and delete group.<p>
     *
     * Event data:
     * <ul>
     * <li>key "id": the uuid of the modified group</li>
     * <li>key "groupname": the name of the modified group</li>
     * <li>key "useraction": the name of the action to do on the replicated server</li>
     * <ul>
     * <li>createGroup</li>
     * <li>writeGroup</li>
     * <li>deleteGroup</li>
     * </ul>
     * </ul>
     */
    int EVENT_GROUP_MODIFIED = 31;

    /**
     * Event "user has logged in".<p>
     *
     * Event data:
     * <ul>
     * <li>key "data" (mandatory): the user who was logged in</li>
     * </ul>
     *
     * @see org.opencms.file.CmsObject#loginUser(String, String)
     */
    int EVENT_LOGIN_USER = 1;

    /**
     * Event "ou modified".<p>
     *
     * Includes create OU and delete OU.<p>
     *
     * Event data:
     * <ul>
     * <li>key "id": the uuid of the modified ou</li>
     * <li>key "ouname": the name of the modified ou</li>
     * <li>key "useraction": the name of the action to do on the replicated server</li>
     * <ul>
     * <li>createOu</li>
     * <li>deleteOu</li>
     * </ul>
     * </ul>
     */
    int EVENT_OU_MODIFIED = 30;

    /**
     * Event "a project was modified" (e.g. a project has been deleted,
     * or the project resources have been changed).<p>
     *
     * Event data:
     * <ul>
     * <li>key "project" (mandatory): the deleted CmsProject</li>
     * </ul>
     */
    int EVENT_PROJECT_MODIFIED = 18;

    /**
     * Event "a property definition has been created".<p>
     *
     * Event data:
     * <ul>
     * <li>key "propertyDefinition" (mandatory): the modified property definition</li>
     * </ul>
     */
    int EVENT_PROPERTY_DEFINITION_CREATED = 28;

    /**
     * Event "a property definition has been modified".<p>
     *
     * Event data:
     * <ul>
     * <li>key "propertyDefinition" (mandatory): the modified property definition</li>
     * </ul>
     */
    int EVENT_PROPERTY_DEFINITION_MODIFIED = 26;

    /**
     * Event "a single property (and so the resource itself, too) have been modified".<p>
     *
     * Event data:
     * <ul>
     * <li>key "resource" (mandatory): the CmsResource that has the modified property attached</li>
     * <li>key "property" (mandatory): the modified property</li>
     * </ul>
     */
    int EVENT_PROPERTY_MODIFIED = 14;

    /**
     * Event "a project was published".<p>
     *
     * Event data:
     * <ul>
     * <li><code>{@link #KEY_REPORT}</code>: a <code>{@link org.opencms.report.I_CmsReport}</code> to print output messages to</li>
     * <li><code>{@link #KEY_PUBLISHID}</code>: the ID of the publish task in the publish history</li>
     * <li><code>{@link #KEY_PROJECTID}</code>: the ID of the project that has been published</li>
     * <li><code>{@link #KEY_DBCONTEXT}</code>: the current users database context</li>
     * </ul>
     *
     * @see org.opencms.publish.CmsPublishManager#publishProject(org.opencms.file.CmsObject)
     * @see #EVENT_BEFORE_PUBLISH_PROJECT
     */
    int EVENT_PUBLISH_PROJECT = 2;

    /**
     * Event "rebuild search indexes".<p>
     *
     * Event data:
     * <ul>
     * <li><code>{@link #KEY_REPORT}</code>: a <code>{@link org.opencms.report.I_CmsReport}</code> to print output messages to</li>
     * <li><code>{@link #KEY_INDEX_NAMES}</code>: a comma separated list of names of the search indexes to rebuild, empty for all indexes</li>
     * </ul>
     */
    int EVENT_REBUILD_SEARCHINDEXES = 32;

    /**
     * Event "reindex" in the online project.<p>
     *
     * Note we split offline/online in two events since only for the online project, the event has to be forwarded in a cluster.<p>
     *
     * Event data:
     * <ul>
     * <li><code>{@link #KEY_RESOURCES}</code>: a list of {@link org.opencms.file.CmsResource}.</li>
     * <li><code>{@link #KEY_REPORT}</code>: a <code>{@link org.opencms.report.I_CmsReport}</code> to print output messages to</li>
     * <li><code>{@link #KEY_USER_ID}</code>: id of the user triggering the action. Used to show a message on success/failure.</li>
     * <li><code>{@link #KEY_REINDEX_RELATED}</code>: flag, indicating if related resources should be reindexed as well.</li>
     * </ul>
     */
    int EVENT_REINDEX_ONLINE = 34;

    /**
     * Event "reindex" in an offline project.<p>
     *
     * Note we split offline/online in two events since only for the online project, the event has to be forwarded in a cluster.<p>
     *
     * Event data:
     * <ul>
     * <li><code>{@link #KEY_RESOURCES}</code>: a list of {@link org.opencms.file.CmsResource}.</li>
     * <li><code>{@link #KEY_PROJECTID}</code>: the id of the project re-indexing is triggered in.</li>
     * <li><code>{@link #KEY_REPORT}</code>: a <code>{@link org.opencms.report.I_CmsReport}</code> to print output messages to</li>
     * <li><code>{@link #KEY_USER_ID}</code>: id of the user triggering the action. Used to show a message on success/failure.</li>
     * <li><code>{@link #KEY_REINDEX_RELATED}</code>: flag, indicating if related resources should be reindexed as well.</li>
     * </ul>
     */
    int EVENT_REINDEX_OFFLINE = 35;

    /**
     * Event "all properties (and so the resource itself, too) have been modified".<p>
     *
     * Event data:
     * <ul>
     * <li>key "resource" (mandatory): the CmsResource that has the modified properties attached</li>
     * </ul>
     */
    int EVENT_RESOURCE_AND_PROPERTIES_MODIFIED = 15;

    /**
     * @see #EVENT_RESOURCES_MODIFIED
     */
    int EVENT_RESOURCE_COPIED = 24;

    /**
     * @see #EVENT_RESOURCE_AND_PROPERTIES_MODIFIED
     */
    int EVENT_RESOURCE_CREATED = 23;

    /**
     * @see #EVENT_RESOURCES_MODIFIED
     */
    int EVENT_RESOURCE_DELETED = 25;

    /**
     * Event "a single resource has been modified".<p>
     *
     * Event data:
     * <ul>
     * <li>key "resource" (mandatory): the modified CmsResource</li>
     * </ul>
     */
    int EVENT_RESOURCE_MODIFIED = 11;

    /**
     * @see #EVENT_RESOURCE_CREATED
     * @see #EVENT_RESOURCE_COPIED
     * @see #EVENT_RESOURCE_DELETED
     */
    int EVENT_RESOURCE_MOVED = 22;

    /**
     * Event "a list of resources and their properties have been modified".<p>
     *
     * Event data:
     * <ul>
     * <li>key "resources" (mandatory): a List of modified CmsResources</li>
     * </ul>
     */
    int EVENT_RESOURCES_AND_PROPERTIES_MODIFIED = 27;

    /**
     * Event "a bunch of resources has been modified".<p>
     *
     * Event data:
     * <ul>
     * <li>key "resources" (mandatory): a List of modified CmsResources</li>
     * </ul>
     */
    int EVENT_RESOURCES_MODIFIED = 12;

    /**
     * Event "a sitemap has been modified".<p>
     *
     * Event data:
     * <ul>
     * <li>key "resources" (mandatory): a List of modified sitemap entries identified by their root path</li>
     * </ul>
     */
    int EVENT_SITEMAP_CHANGED = 33;

    /**
     * Event "update exported resources".<p>
     *
     * This event updates all export points, deletes the content
     * of the "export" folder, purges the JSP repository, and clears
     * all caches.<p>
     *
     * This event is for internal use.<p>
     */
    int EVENT_UPDATE_EXPORTS = 19;

    /**
     * Event "user modified".<p>
     *
     * Event data:
     * <ul>
     * <li>key "id": the uuid of the modified user</li>
     * <li>key "username": the name of the modified user</li>
     * <li>key "groupname": the name of the group which is effected</li>
     * <li>key "useraction": the name of the action to do on the replicated server</li>
     * <ul>
     * <li>createUser</li>
     * <li>writeUser</li>
     * <li>deleteUser</li>
     * <li>setOu</li>
     * <li>addUserToGroup</li>
     * <li>removeUserFromGroup</li>
     * <li>resetPassword</li>
     * </ul>
     * </ul>
     */
    int EVENT_USER_MODIFIED = 29;

    /** Key name for passing a change int in the data map - see the <code>CHANGED_XXX</code> constants in {@link org.opencms.db.CmsDriverManager}. */
    String KEY_CHANGE = "change";

    /** Key name for passing a database context in the data map. */
    String KEY_DBCONTEXT = "dbContext";

    /** Key name for passing a group ID. */
    String KEY_GROUP_ID = "groupId";

    /** Key name for passing a group name. */
    String KEY_GROUP_NAME = "groupName";

    /** Key name for passing a comma separated list of search index names in the data map. */
    String KEY_INDEX_NAMES = "indexNames";

    /** Key name for passing a flag, indicating if we are in the online project. */
    String KEY_IS_ONLINE = "isOnline";

    /** Key name for passing an OU ID. */
    String KEY_OU_ID = "ouId";

    /** Key name for passing a group name. */
    String KEY_OU_NAME = "ouName";

    /** Key name for passing a project id in the data map. */
    String KEY_PROJECTID = "projectId";

    /** Key name for passing a publish history id in the data map. */
    String KEY_PUBLISHID = "publishHistoryId";

    /** Key name for passing a publish list in the data map. */
    String KEY_PUBLISHLIST = "publishList";

    /** Key name for passing a flag, indicating if related resources should be taken into account in the data map. */
    String KEY_REINDEX_RELATED = "related";

    /** Key name for passing a report in the data map. */
    String KEY_REPORT = "report";

    /** Key name for passing a {@link org.opencms.file.CmsResource} in the data map. */
    String KEY_RESOURCE = "resource";

    /** Key name for passing a List of {@link org.opencms.file.CmsResource} in the data map. */
    String KEY_RESOURCES = "resources";

    /** Key name for skipping searchindexing. */
    String KEY_SKIPINDEX = "skipindex";

    /** Key name for passing a user action. */
    String KEY_USER_ACTION = "userAction";

    /** Key name for passing user changes flag. */
    String KEY_USER_CHANGES = "userChanges";

    /** Key name for passing an user ID. */
    String KEY_USER_ID = "userId";

    /** Key name for passing a user name. */
    String KEY_USER_NAME = "userName";

    /** Marker for "all events". */
    Integer LISTENERS_FOR_ALL_EVENTS = Integer.valueOf(-1);

    /** Value for the "group modified" action. */
    String VALUE_GROUP_MODIFIED_ACTION_CREATE = "createGroup";

    /** Value for the "group modified" action. */
    String VALUE_GROUP_MODIFIED_ACTION_DELETE = "deleteGroup";

    /** Value for the "group modified" action. */
    String VALUE_GROUP_MODIFIED_ACTION_WRITE = "writeGroup";

    /** Value for the "ou modified" action. */
    String VALUE_OU_MODIFIED_ACTION_CREATE = "createOu";

    /** Value for the "ou modified" action. */
    String VALUE_OU_MODIFIED_ACTION_DELETE = "deleteOu";

    /** Value for the "user modified" action. */
    String VALUE_USER_MODIFIED_ACTION_ADD_USER_TO_GROUP = "addUserToGroup";

    /** Value for the "user modified" action. */
    String VALUE_USER_MODIFIED_ACTION_CREATE_USER = "createUser";

    /** Value for the "user modified" action. */
    String VALUE_USER_MODIFIED_ACTION_DELETE_USER = "deleteUser";

    /** Value for the "user modified" action. */
    String VALUE_USER_MODIFIED_ACTION_REMOVE_USER_FROM_GROUP = "removeUserFromGroup";

    /** Value for the "user modified" action. */
    String VALUE_USER_MODIFIED_ACTION_RESET_PASSWORD = "resetPassword";

    /** Value for the "user modified" action. */
    String VALUE_USER_MODIFIED_ACTION_SET_OU = "setOu";

    /** Value for the "user modified" action. */
    String VALUE_USER_MODIFIED_ACTION_WRITE_USER = "writeUser";

    /** Value to mark modification events from sibling creation. */
    String VALUE_CREATE_SIBLING = "createSibling";

    /**
     * Acknowledge the occurrence of the specified event, implement this
     * method to check for CmsEvents in your class.
     *
     * @param event CmsEvent that has occurred
     */
    void cmsEvent(CmsEvent event);
}
