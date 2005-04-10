/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/Attic/I_CmsConstants.java,v $
 * Date   : $Date: 2005/04/10 11:00:14 $
 * Version: $Revision: 1.41 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.main;

import org.opencms.security.CmsPermissionSet;

/**
 * This interface is a pool for constants in OpenCms.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 *
 * @version $Revision: 1.41 $
 */
public interface I_CmsConstants {

    /**
     * Group may read this resource.
     */
    int C_ACCESS_GROUP_READ = 8;

    /**
     * Group may view this resource.
     */
    int C_ACCESS_GROUP_VISIBLE = 32;

    /**
     * Group may write this resource.
     */
    int C_ACCESS_GROUP_WRITE = 16;

    /**
     * This is an internal resource, it can't be accessed directly.
     */
    int C_ACCESS_INTERNAL_READ = 512;

    /**
     * Owner has full access to this resource.
     */
    int C_ACCESS_OWNER = CmsPermissionSet.PERMISSION_READ
        + CmsPermissionSet.PERMISSION_WRITE
        + CmsPermissionSet.PERMISSION_VIEW;

    /**
     *  Public may read this resource.
     */
    int C_ACCESS_PUBLIC_READ = 64;

    /**
     *  Public may view this resource.
     */
    int C_ACCESS_PUBLIC_VISIBLE = 256;

    /**
     *  Public may write this resource.
     */
    int C_ACCESS_PUBLIC_WRITE = 128;

    /**
     *  has full access to this resource.
     */
    int C_ACCESS_PUBLIC = C_ACCESS_PUBLIC_READ + C_ACCESS_PUBLIC_WRITE + C_ACCESS_PUBLIC_VISIBLE;

    /**
     * All may read this resource.
     */
    int C_ACCESS_READ = CmsPermissionSet.PERMISSION_READ + C_ACCESS_PUBLIC_READ;

    /**
     * All may view this resource.
     */
    int C_ACCESS_VISIBLE = CmsPermissionSet.PERMISSION_VIEW + C_ACCESS_GROUP_VISIBLE + C_ACCESS_PUBLIC_VISIBLE;

    /**
     * All may write this resource.
     */
    int C_ACCESS_WRITE = CmsPermissionSet.PERMISSION_WRITE + C_ACCESS_GROUP_WRITE + C_ACCESS_PUBLIC_WRITE;

    /**
     * The default-flags for a new resource.
     */
    int C_ACCESS_DEFAULT_FLAGS = CmsPermissionSet.PERMISSION_READ
        + CmsPermissionSet.PERMISSION_WRITE
        + CmsPermissionSet.PERMISSION_VIEW
        + C_ACCESS_GROUP_WRITE
        + C_ACCESS_GROUP_VISIBLE
        + C_ACCESS_PUBLIC_READ
        + C_ACCESS_PUBLIC_VISIBLE;

    /**
     * Group has full access to this resource.
     */
    int C_ACCESS_GROUP = C_ACCESS_GROUP_WRITE + C_ACCESS_GROUP_VISIBLE;

    /**
     * Flag to indicate that an access control entry is currently deleted.
     */
    int C_ACCESSFLAGS_DELETED = 1;

    /**
     * Flag to signal the pricipal type group.
     */
    int C_ACCESSFLAGS_GROUP = 32;

    /**
     * Flag to indicate that an access control entry should be inherited.
     */
    int C_ACCESSFLAGS_INHERIT = 2;

    /**
     * Flag to indicate that an access control entry was inherited (read only).
     */
    int C_ACCESSFLAGS_INHERITED = 8;

    /**
     * Flag to indicate that an access control entry overwrites inherited entries. 
     */
    int C_ACCESSFLAGS_OVERWRITE = 4;

    /**
     * Flag to signal the principal type user.
     */
    int C_ACCESSFLAGS_USER = 16;

    /** Key for additional info start settings. */
    String C_ADDITIONAL_INFO_DEFAULTGROUP = "USER_DEFAULTGROUP";

    /** Key for additional info explorer settings. */
    String C_ADDITIONAL_INFO_EXPLORERSETTINGS = "USER_EXPLORERSETTINGS";

    /**
     * Key for additional info flags.
     */
    String C_ADDITIONAL_INFO_PREFERENCES = "USER_PREFERENCES";

    /** Key for additional info start settings. */
    String C_ADDITIONAL_INFO_STARTSETTINGS = "USER_STARTSETTINGS";

    /** Key for additional info task settings. */
    String C_ADDITIONAL_INFO_TASKSETTINGS = "USER_TASKSETTINGS";

    /**
     * Key for additional info address.
     */
    String C_ADDITIONAL_INFO_TOWN = "USER_TOWN";

    /**
     * Key for additional info address.
     */
    String C_ADDITIONAL_INFO_ZIPCODE = "USER_ZIPCODE";

    /**
     * Was logged in: at least once.
     */
    int C_AT_LEAST_ONCE = 2;

    /**
     * 
     */
    String C_CONFIGURATION_BACKUP = "driver.backup";

    /**
     * A string in the configuration-file.
     */
    String C_CONFIGURATION_CACHE = "cache";

    /**
     * A string in the configuration-file.
     */
    String C_CONFIGURATION_DB = "db";

    /**
     * Prefix for history/backup config keys.
     */
    String C_CONFIGURATION_HISTORY = "history";

    /** "driver.project" string in the configuration-file. */
    String C_CONFIGURATION_PROJECT = "driver.project";

    /** Path to the "opencms.properties" file relative to the "WEB-INF" directory of the application. */
    String C_CONFIGURATION_PROPERTIES_FILE = "config/opencms.properties";

    /** "registry" string in the configuration-file. */
    String C_CONFIGURATION_REGISTRY = "registry";

    /** "driver.user" string in the configuration-file. */
    String C_CONFIGURATION_USER = "driver.user";

    /** "driver.vfs" string in the configuration-file. */    
    String C_CONFIGURATION_VFS = "driver.vfs";

    /** "driver.workflow" string in the configuration-file. */
    String C_CONFIGURATION_WORKFLOW = "driver.workflow";

    /** Copy mode for copy resources as new resource. */
    int C_COPY_AS_NEW = 1;

    /** Copy mode for copy resources as sibling. */
    int C_COPY_AS_SIBLING = 2;

    /** Copy mode to preserve siblings during copy. */
    int C_COPY_PRESERVE_SIBLING = 3;

    /**
     * The copyright message for OpenCms.
     */
    String[] C_COPYRIGHT = {
        "",
        "Copyright (c) 2002-2005 Alkacon Software",
        "OpenCms comes with ABSOLUTELY NO WARRANTY",
        "This is free software, and you are welcome to",
        "redistribute it under certain conditions.",
        "Please see the GNU Lesser General Public Licence for",
        "further details.",
        ""};

    /** Flag for leaving a date unchanged during a touch operation. */
    long C_DATE_UNCHANGED = -1;

    /** Signals that siblings of this resource should be deleted. */
    int C_DELETE_OPTION_DELETE_SIBLINGS = 1;

    /** Signals that siblings of this resource should not be deleted. */
    int C_DELETE_OPTION_PRESERVE_SIBLINGS = 0;

    /**
     * The name of the error tag prefix in backoffice templates.
     */
    String C_ERRPREFIX = "err";

    /**
     * The name of the error tag separator in backoffice templates.
     */
    String C_ERRSPERATOR = "_";

    /** Prefix for ace principal group. */
    String C_EXPORT_ACEPRINCIPAL_GROUP = "GROUP.";

    /** Prefix for ace principal user. */
    String C_EXPORT_ACEPRINCIPAL_USER = "USER.";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_ACCESS = "access";

    /**
     * Tag to identify allowed permissions.
     */
    String C_EXPORT_TAG_ACCESSCONTROL_ALLOWEDPERMISSIONS = "allowed";

    /**
     * Tag to identify denied permissions.
     */
    String C_EXPORT_TAG_ACCESSCONTROL_DENIEDPERMISSIONS = "denied";

    /**
     * Tag to identify access control entries .
     */
    String C_EXPORT_TAG_ACCESSCONTROL_ENTRIES = "accesscontrol";

    /**
     * Tag to identify a single access control entry.
     */
    String C_EXPORT_TAG_ACCESSCONTROL_ENTRY = "accessentry";

    /**
     * Tag to identify a permission set.
     */
    String C_EXPORT_TAG_ACCESSCONTROL_PERMISSIONSET = "permissionset";

    /**
     * Tag to identify a principal set.
     */
    String C_EXPORT_TAG_ACCESSCONTROL_PRINCIPAL = "uuidprincipal";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_ADDRESS = "address";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    String C_EXPORT_TAG_CREATOR = "creator";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    String C_EXPORT_TAG_DATE = "createdate";

    /**
     * The "datecreated" tag in the manifest-file.
     */
    String C_EXPORT_TAG_DATECREATED = "datecreated";

    /**
     * The "expire" tag in the manifest-file.
     */
    String C_EXPORT_TAG_DATEEXPIRED = "dateexpired";

    /**
     * The "datelastmodified" tag in the manifest-file.
     */
    String C_EXPORT_TAG_DATELASTMODIFIED = "datelastmodified";

    /**
     * The "release" tag in the manifest-file.
     */
    String C_EXPORT_TAG_DATERELEASED = "datereleased";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_DEFAULTGROUP = "defaultgroup";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_DESCRIPTION = "description";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_DESTINATION = "destination";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_EMAIL = "email";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_EXPORT = "export";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_FILE = "file";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_FILES = "files";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_FIRSTNAME = "firstname";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_FLAGS = "flags";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_GROUP = "group";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_GROUPDATA = "groupdata";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_GROUPNAME = "groupname";

    /**
     * Tag to identify a generic id.
     */
    String C_EXPORT_TAG_ID = "id";

    /**
     * A tag in the export manifest-file.
     */
    String C_EXPORT_TAG_INFO = "info";

    /**
     * The "lastmodified" tag in the manifest-file.
     */
    String C_EXPORT_TAG_LASTMODIFIED = "lastmodified";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_LASTNAME = "lastname";

    /**
     * The "link" tag in the manifest-file.
     */
    String C_EXPORT_TAG_LINK = "link";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_LOADER_START_CLASS = "startclass";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_MODULEXPORT = "modulexport";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_NAME = "name";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    String C_EXPORT_TAG_OC_VERSION = "opencms_version";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_PARENTGROUP = "parentgroup";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_PASSWORD = "password";

    /**
     * A tag in the manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    String C_EXPORT_TAG_PROJECT = "project";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_PROPERTIES = "properties";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_PROPERTY = "property";

    /**
     * Key for the type attrib. of a property element.<p>
     */
    String C_EXPORT_TAG_PROPERTY_ATTRIB_TYPE = "type";

    /**
     * Value for the "shared" type attrib. of a property element.<p>
     */
    String C_EXPORT_TAG_PROPERTY_ATTRIB_TYPE_SHARED = "shared";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_RECOVERYPASSWORD = "recoverypassword";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_SECTION = "section";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_SOURCE = "source";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_TYPE = "type";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_USER = "user";

    /**
     * The "usercreated" tag in the manifest-file.
     */
    String C_EXPORT_TAG_USERCREATED = "usercreated";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_USERDATA = "userdata";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_USERGROUPDATA = "usergroupdata";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_USERGROUPS = "usergroups";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_USERINFO = "userinfo";

    /**
     * The "userlastmodified" tag in the manifest-file.
     */
    String C_EXPORT_TAG_USERLASTMODIFIED = "userlastmodified";

    /**
     * The "uuidfile" tag in the manifest-file.
     */
    String C_EXPORT_TAG_UUIDCONTENT = "uuidcontent";

    /**
     * The "uuidresource" tag in the manifest-file.
     */
    String C_EXPORT_TAG_UUIDRESOURCE = "uuidresource";

    /**
     * The "uuid" tag in the manifest-file.
     */
    String C_EXPORT_TAG_UUIDSTRUCTURE = "uuidstructure";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_VALUE = "value";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    String C_EXPORT_TAG_VERSION = "export_version";

    /**
     * The version of the opencms export (appears in the export manifest-file).
     */
    String C_EXPORT_VERSION = "4";

    // Constants for import/export

    /** 
     * The filename of the xml manifest.
     */
    String C_EXPORT_XMLFILENAME = "manifest.xml";

    /**
     * A string in the configuration-file.
     */
    String C_EXPORTPOINT = "exportpoint.";

    /**
     * A string in the configuration-file.
     */
    String C_EXPORTPOINT_PATH = "exportpoint.path.";

    /**
     * This flag is set for disabled entrys in the database.
     * (GROUP_FLAGS for example)
     */
    int C_FLAG_DISABLED = 1;

    /**
     * This flag is set for enabled entrys in the database.
     * (GROUP_FLAGS for example).
     */
    int C_FLAG_ENABLED = 0;

    /**
     * Flag constant: ProjectCoWorker
     * flag for groups.
     */
    int C_FLAG_GROUP_PROJECTCOWORKER = 4;

    /**
     * Flag constant: Projectmanager
     * flag for groups.
     */
    int C_FLAG_GROUP_PROJECTMANAGER = 2;

    /**
     * Flag constant: Role (for coworkers)
     * flag for groups.
     */
    int C_FLAG_GROUP_ROLE = 8;

    /**
     * The folder separator.
     */
    String C_FOLDER_SEPARATOR = "/";

    /** HTTP Accept-Charset Header for internal requests used during static export. */
    String C_HEADER_ACCEPT_CHARSET = "Accept-Charset";

    /** HTTP Accept-Language Header for internal requests used during static export. */
    String C_HEADER_ACCEPT_LANGUAGE = "Accept-Language";

    /** HTTP Header "Cache-Control". */
    String C_HEADER_CACHE_CONTROL = "Cache-Control";

    /** HTTP Header "Expires". */
    String C_HEADER_EXPIRES = "Expires";

    /** HTTP Header "If-Modified-Since". */
    String C_HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

    /** HTTP Header "Last-Modified". */
    String C_HEADER_LAST_MODIFIED = "Last-Modified";

    /** HTTP Header for internal requests used during static export. */
    String C_HEADER_OPENCMS_EXPORT = "OpenCms-Export";

    /** HTTP Header "Pragma". */
    String C_HEADER_PRAGMA = "Pragma";

    /** HTTP Header "Server". */
    String C_HEADER_SERVER = "Server";

    /** HTTP Header value "max-age=" (for "Cache-Control"). */
    String C_HEADER_VALUE_MAX_AGE = "max-age=";

    /** HTTP Header value "must-revalidate" (for "Cache-Control"). */
    String C_HEADER_VALUE_MUST_REVALIDATE = "must-revalidate";

    /** HTTP Header value "no-cache" (for "Cache-Control"). */
    String C_HEADER_VALUE_NO_CACHE = "no-cache";

    /** HTTP Header "WWW-Authenticate". */
    String C_HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

    /** Identifier for x-forwarded-for (i.e. proxied) request headers. */
    String C_HEADER_X_FORWARDED_FOR = "x-forwarded-for";

    /** Localhost ip used in fallback cases. */
    String C_IP_LOCALHOST = "127.0.0.1";

    /**
     * The key for the date of the last linkcheck in the linkchecktable.
     */
    String C_LINKCHECKTABLE_DATE = "linkcheckdate";

    /**
     * The maximum length of a resource name (incl. path).
     */
    int C_MAX_LENGTH_RESOURCE_NAME = 240;

    /** The name of the module folder in the package path. */
    String C_MODULE_PATH = "modules/";

    /**
     * The module property key name to specifiy additional resources which are
     * part of a module outside of {system/modules}.
     */
    String C_MODULE_PROPERTY_ADDITIONAL_RESOURCES = "additionalresources";

    /**
     * Character to separate additional resources specified in the module properties.
     */
    String C_MODULE_PROPERTY_ADDITIONAL_RESOURCES_SEPARATOR = ";";

    /**
     * This is the defintion for a filesystem mountpoint.
     */
    int C_MOUNTPOINT_FILESYSTEM = 1;

    /**
     * This is the defintion for a database mountpoint.
     */
    int C_MOUNTPOINT_MYSQL = 2;

    /**
     * Was logged in: never.
     */
    int C_NEVER = 1;

    /** Task order value by agentuser. */
    String C_ORDER_AGENTUSER = "agentuserref";

    /** Task order value by endtime. */
    String C_ORDER_ENDTIME = "endtime";

    /** Task order value by id. */
    String C_ORDER_ID = "id";

    /** Task order value by initiator user. */
    String C_ORDER_INITIATORUSER = "initiatoruserref";

    /** Task order value by name. */
    String C_ORDER_NAME = "name";

    /** Task order value by original user. */
    String C_ORDER_ORIGINALUSER = "originaluserref";

    /** Task order value by percentage. */
    String C_ORDER_PERCENTAGE = "percentage";

    /** Task order value by priority. */
    String C_ORDER_PRIORITY = "priorityref";

    /** Task order value by role. */
    String C_ORDER_ROLE = "roleref";

    /** Task order value by start time. */
    String C_ORDER_STARTTIME = "starttime";

    /** Task order value by state. */
    String C_ORDER_STATE = "state";

    /** Task order value by type. */
    String C_ORDER_TASKTYPE = "tasktyperef";

    /** Task order value by timeout. */
    String C_ORDER_TIMEOUT = "timeout";

    /** Task order value by wakeup time. */
    String C_ORDER_WAKEUPTIME = "wakeuptime";

    /** Path to the "packages" folder relative to the "WEB-INF" directory of the application. */
    String C_PACKAGES_FOLDER = "packages/";

    /** Request parameter to force element selection. */
    String C_PARAMETER_ELEMENT = "__element";

    /** Request parameter to force encoding selection. */
    String C_PARAMETER_ENCODING = "__encoding";

    /** Request parameter to force locale selection. */
    String C_PARAMETER_LOCALE = "__locale";

    /**
     * This constant defines the onlineproject. This is the project which
     * is used to show the resources for guestusers
     */
    String C_PROJECT_ONLINE = "Online";

    /**
     * This constant defines the onlineproject. This is the project which
     * is used to show the resources for guestusers
     */
    int C_PROJECT_ONLINE_ID = 1;

    /**
     * This constant defines a project in a archive.
     * Resources can't be changed in this project. Its state will never
     * go back to the previos one.
     */
    int C_PROJECT_STATE_ARCHIVE = 2;

    /**
     * This constant defines a project that is invisible.
     * The project is invisible for users. It is needed
     * for creating and editing temporary files
     */
    int C_PROJECT_STATE_INVISIBLE = 3;

    /**
     * This constant defines a locked project.
     * Resources can't be changed in this project.
     */
    int C_PROJECT_STATE_LOCKED = 1;

    /** 
     * This constant defines a unlocked project.
     * Resources may be changed in this project.
     */
    int C_PROJECT_STATE_UNLOCKED = 0;

    /**
     * This constant defines a normal project-type.
     */
    int C_PROJECT_TYPE_NORMAL = 0;

    /**
     * This constant defines a temporary project-type.
     * The project will be deleted after publishing.
     */
    int C_PROJECT_TYPE_TEMPORARY = 1;

    /** Property for the active method in the administration view. */
    String C_PROPERTY_ACTIV = "activemethod";

    /** Property for the allowed set of locales. */
    String C_PROPERTY_AVAILABLE_LOCALES = "locale-available";

    /** Property to control the Java class for body. */
    String C_PROPERTY_BODY_CLASS = "templateclass";

    /** Property for the channel id. */
    String C_PROPERTY_CHANNELID = "ChannelId";

    /** Property for the content conversion. */
    String C_PROPERTY_CONTENT_CONVERSION = "content-conversion";

    /** Property for the content encoding. */
    String C_PROPERTY_CONTENT_ENCODING = "content-encoding";

    /** Property for the default file in folders. */
    String C_PROPERTY_DEFAULT_FILE = "default-file";

    /** Property for the description. */
    String C_PROPERTY_DESCRIPTION = "Description";

    /** Property for the static export. */
    String C_PROPERTY_EXPORT = "export";

    /** Property for the resource export name, during export this name is used instead of the resource name. */
    String C_PROPERTY_EXPORTNAME = "exportname";

    /** Property for JSP additional suffix during static export, default is "html". */
    String C_PROPERTY_EXPORTSUFFIX = "exportsuffix";

    /** Property for internal use (e.g. delete). */
    String C_PROPERTY_INTERNAL = "internal";

    /** Property for the keywords. */
    String C_PROPERTY_KEYWORDS = "Keywords";

    /** Property for the current locale. */
    String C_PROPERTY_LOCALE = "locale";

    /** Property for the login form. */
    String C_PROPERTY_LOGIN_FORM = "login-form";

    /** Property for the navigation position. */
    String C_PROPERTY_NAVPOS = "NavPos";

    /** Property for the navigation text. */
    String C_PROPERTY_NAVTEXT = "NavText";

    /** Property for the relative root link substitution. */
    String C_PROPERTY_RELATIVEROOT = "relativeroot";

    /** Property to boost certain search results. */
    String C_PROPERTY_SEARCH_PRIORITY = "search.priority";

    /** Property to sort search results in categories. */
    String C_PROPERTY_SEARCH_CATEGORY = "category";
    
    /** Property for the secure transmission of resources. */
    String C_PROPERTY_SECURE = "secure";    

    /** Property to control the template. */
    String C_PROPERTY_TEMPLATE = "template";

    /** Property to control the template. */
    String C_PROPERTY_TEMPLATE_ELEMENTS = "template-elements";

    /** Property for the resource title. */
    String C_PROPERTY_TITLE = "Title";

    /** Property for the visible method in the administration view. */
    String C_PROPERTY_VISIBLE = "visiblemethod";

    /** The propertydefinitiontype for resources. */
    int C_PROPERYDEFINITION_RESOURCE = 1;

    /**
     * The attribute of the publishclass tag in the modules registry used to show
     * that this method needs the vector of the changed links for publishing.
     * (i.e. the search module)
     */
    String C_PUBLISH_METHOD_LINK = "linkpublish";

    /** Indicates to ignore the resource path when matching resources. */
    String C_READ_IGNORE_PARENT = null;

    /** Indicates to ignore the resource state when matching resources. */
    int C_READ_IGNORE_STATE = -1;

    /** Indicates to ignore the time value. */
    long C_READ_IGNORE_TIME = 0L;

    /** Indicates to ignore the resource type when matching resources. */
    int C_READ_IGNORE_TYPE = -1;

    /** Indicates to match resources NOT having the given state. */
    int C_READMODE_EXCLUDE_STATE = 8;

    /** Indicates to match immediate children only. */
    int C_READMODE_EXCLUDE_TREE = 1;

    /** Indicates to match resources NOT having the given type. */
    int C_READMODE_EXCLUDE_TYPE = 4;

    /** Mode for reading project resources from the db. */
    int C_READMODE_IGNORESTATE = 0;

    /** Indicates to match resources in given project only. */
    int C_READMODE_INCLUDE_PROJECT = 2;

    /** Indicates to match all successors. */
    int C_READMODE_INCLUDE_TREE = 0;

    /** Mode for reading project resources from the db. */
    int C_READMODE_MATCHSTATE = 1;

    /** Indicates if only file resources should be read. */
    int C_READMODE_ONLY_FILES = 128;

    /** Indicates if only folder resources should be read. */
    int C_READMODE_ONLY_FOLDERS = 64;

    /** Indicates to order the result by the lastmodified date. */
    int C_READMODE_ORDER_BY_LASTMODIFIED = 32;

    /** Indicates to order the result by the paths to the resources. */
    int C_READMODE_ORDER_BY_PATH = 16;

    /** Mode for reading project resources from the db. */
    int C_READMODE_UNMATCHSTATE = 2;

    /**
     * The name of the exportpoint destination tag in registry.
     */
    String C_REGISTRY_DESTINATION = "destination";

    /**
     * The name of the tag in registry for history properties.
     */
    String C_REGISTRY_HISTORY = "history";

    /**
     * The name of the tag in registry if versions of the history should be deleted .
     */
    String C_REGISTRY_HISTORY_DELETE = "deleteversions";

    /**
     * The name of the tag in registry if history is enabled.
     */
    String C_REGISTRY_HISTORY_ENABLE = "enabled";

    /**
     * The name of the tag in registry storing the max number of versions.
     */
    String C_REGISTRY_HISTORY_VERSIONS = "versions";

    /**
     * The name of the tag in registry for the number of weeks the versions should remain in the history.
     */
    String C_REGISTRY_HISTORY_WEEKS = "weeks";

    /**
     * The name of the exportpoint source tag in registry.
     */
    String C_REGISTRY_SOURCE = "source";

    /** Identifier for request type console. */
    int C_REQUEST_CONSOLE = 1;

    /** Identifier for request type http. */
    int C_REQUEST_HTTP = 0;

    /** The resource is marked as internal. */
    int C_RESOURCEFLAG_INTERNAL = 512;

    /** The resource is linked inside a site folder specified in the OpenCms configuration. */
    int C_RESOURCEFLAG_LABELLINK = 2;

    /** Identifier for response type console. */
    int C_RESPONSE_CONSOLE = 1;

    /** Identifier for response type http. */
    int C_RESPONSE_HTTP = 0;

    /**
     * The name of the root folder.
     */
    String C_ROOT = C_FOLDER_SEPARATOR;

    /** Template element name used for the canonical root template. */
    String C_ROOT_TEMPLATE_NAME = "root";

    /**
     * Session key for storing the position in the administration navigation.
     */
    String C_SESSION_ADMIN_POS = "adminposition";

    /**
     * Session key for storing the current charcter encoding to be used in HTTP
     * requests and responses.
     */
    // Encoding project:
    String C_SESSION_CONTENT_ENCODING = "content-encoding";

    /**
     * The key for the current user group in the user information hashtable.
     */
    String C_SESSION_CURRENTGROUP = "CURRENTGROUP";

    /**
     * The key for the current site in the user information hashtable.
     */
    String C_SESSION_CURRENTSITE = "CURRENTSITE";

    /**
     * The key for the original session to store the session data.
     */
    String C_SESSION_DATA = "_session_data_";

    /**
     * Session key for storing the files Vector for moduleimport.
     */
    String C_SESSION_MODULE_VECTOR = "modulevector";

    /**
     * The key for the project in the user information hashtable.
     */
    String C_SESSION_PROJECT = "PROJECT";

    /**
     * Session key for storing a possible error while executing a thread.
     */
    String C_SESSION_THREAD_ERROR = "threaderror";

    /**
     * The key for the username in the user information hashtable.
     */
    String C_SESSION_USERNAME = "USERNAME";

    /** Start preferences DefaultGroup. */
    String C_START_DEFAULTGROUP = "StartDefaultGroup";

    /** Start preferences language. */
    String C_START_LOCALE = "StartLanguage";

    /** Start preferences lock dialog. */
    String C_START_LOCKDIALOG = "StartLockDialog";

    /** Start preferences project. */
    String C_START_PROJECT = "StartProject";

    /** Start preferences lock dialog. */
    String C_START_UPLOADAPPLET = "StartUploadApplet";

    /** Start preferences view. */
    String C_START_VIEW = "StartView";

    /**
     * Indicates if a resource has been changed in the offline version when compared to the online version.
     */
    int C_STATE_CHANGED = 1;

    /**
     * Indicates if a resource has been deleted in the offline version when compared to the online version.
     */
    int C_STATE_DELETED = 3;

    /**
     * Special state value that indicates the current state must be kept on a resource.
     * This value must not be written to the database!
     */
    int C_STATE_KEEP = 99;

    /**
     * Indicates if a resource in new in the offline version when compared to the online version.
     */
    int C_STATE_NEW = 2;

    /**
     * Indicates if a resource is unchanged in the offline version when compared to the online version.
     */
    int C_STATE_UNCHANGED = 0;

    /**
     * The name of the entry for the id generator to create new channelid's.
     */
    String C_TABLE_CHANNELID = "CORE_CHANNEL_ID";

    /** Task preferences filter. */
    String C_TASK_FILTER = "task.filter.";

    /** Task preferences message flags. */
    String C_TASK_MESSAGES = "TaskMessages";

    /** state values of task messages when accepted. */
    int C_TASK_MESSAGES_ACCEPTED = 1;

    /** state values of task messages when completed. */
    int C_TASK_MESSAGES_COMPLETED = 4;

    /** state values of task messages when forwarded. */
    int C_TASK_MESSAGES_FORWARDED = 2;

    /** state values of task messages when members. */
    int C_TASK_MESSAGES_MEMBERS = 8;

    /**
     * This constant is used to order the tasks by date.
     */
    int C_TASK_ORDER_BY_DATE = 1;

    /**
     * This constant is used to order the tasks by name.
     */
    int C_TASK_ORDER_BY_NAME = 2;

    /** Task priority high. */
    int C_TASK_PRIORITY_HIGH = 1;

    /** Task priority low. */
    int C_TASK_PRIORITY_LOW = 3;

    /** Task priority normal. */
    int C_TASK_PRIORITY_NORMAL = 2;

    /** state values of a task ended. */
    int C_TASK_STATE_ENDED = 4;

    /** state values of a task halted. */
    int C_TASK_STATE_HALTED = 5;

    /** state values of a task ready to end. */
    int C_TASK_STATE_NOTENDED = 3;

    /** state values of a task prepared to start. */
    int C_TASK_STATE_PREPARE = 0;

    /** state values of a task ready to start. */
    int C_TASK_STATE_START = 1;

    /** state values of a task started. */
    int C_TASK_STATE_STARTED = 2;

    /** Task preferences view all. */
    String C_TASK_VIEW_ALL = "TaskViewAll";

    /**System type values for the task log. */
    int C_TASKLOG_SYSTEM = 0;

    /**User type value for the task log. */
    int C_TASKLOG_USER = 1;

    /** Value for order tasks by none. */
    int C_TASKORDER_NONE = 0;

    /** Value for order tasks by startdate. */
    int C_TASKORDER_STARTDATE = 1;

    /** Value for order tasks by timeout. */
    int C_TASKORDER_TIMEOUT = 2;

    /** Task type value of getting active tasks. */
    int C_TASKS_ACTIVE = 4;

    /** Task type value of getting all tasks. */
    int C_TASKS_ALL = 1;

    /** Task type value of getting done tasks. */
    int C_TASKS_DONE = 5;

    /** Task type value of getting new tasks. */
    int C_TASKS_NEW = 2;

    /** Task type value of getting open tasks. */
    int C_TASKS_OPEN = 3;

    /** Value for order tasks by taskname. */
    int C_TASKSORDER_TASKNAME = 3;

    /** prefix for temporary files. */
    String C_TEMP_PREFIX = "~";

    /**
     * This id will be returned for resources with no id.
     * (filesystem resources).
     */
    int C_UNKNOWN_ID = -1;

    /**
     * This value will be returned for int's withaout a value.
     */
    int C_UNKNOWN_INT = -1;

    /**
     * This value will be returned for long's withaout a value.
     */
    int C_UNKNOWN_LONG = -1;

    /**
     * The user digest encoding.
     */
    String C_USER_DIGEST_ENCODING = "db.user.digest.encoding";

    /**
     * The user digest output coding.
     */
    String C_USER_DIGEST_OUTPUT = "db.user.digest.output";

    /**
     * The user digest algorithm.
     */
    String C_USER_DIGEST_TYPE = "db.user.digest.type";

    /**
     * A user-type system user.
     */
    int C_USER_TYPE_SYSTEMUSER = 0;

    /**
     * A user-type web user.
     */
    int C_USER_TYPE_WEBUSER = 2;

    /** The vfs path of the loast and found folder. */
    String C_VFS_LOST_AND_FOUND = "/system/lost-found";

    /**
     * Was logged in: whatever.
     */
    int C_WHATEVER = 3;

    /** Name of the special body element from an XMLTemplate. */
    String C_XML_BODY_ELEMENT = "body";

    /** Default class for templates. */
    String C_XML_CONTROL_DEFAULT_CLASS = "com.opencms.template.CmsXmlTemplate";

    /** Suffix for caching of simple pages. */
    String C_XML_CONTROL_FILE_SUFFIX = ".xmlcontrol";

    /**
     * The vfs path of the channel folders.
     */
    String VFS_FOLDER_CHANNELS = "/channels";

    /**
     * The vfs path of the sites master folder.
     */
    String VFS_FOLDER_SITES = "/sites";

    /**
     * The vfs path of the system folder.
     */
    String VFS_FOLDER_SYSTEM = "/system";

    /**
     * The vfs path of the default site.
     */
    String VFS_FOLDER_DEFAULT_SITE = VFS_FOLDER_SITES + "/default";
}