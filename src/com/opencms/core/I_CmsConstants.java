/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/I_CmsConstants.java,v $
* Date   : $Date: 2003/05/21 16:08:28 $
* Version: $Revision: 1.223 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.core;

/**
 * This interface is a pool for constants in OpenCms.<p>
 * 
 * Other classes may implement this class to get easy access to this constants.
 *
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Michaela Schleich
 * @author Thomas Weckert
 *
 * @version $Revision: 1.223 $ $Date: 2003/05/21 16:08:28 $
 */
public interface I_CmsConstants {

    /**
     * The copyright message for OpenCms.
     */
    static String C_COPYRIGHT[] =  {
        "",
        "Copyright (c) 2002-2003 Alkacon Software",        
        "Copyright (c) 2000-2002 The OpenCms Group",
        "OpenCms comes with ABSOLUTELY NO WARRANTY",
        "This is free software, and you are welcome to",
        "redistribute it under certain conditions.",
        "Please see the GNU Lesser General Public Licence for",
        "further details.",
        ""
    };

    /**
     * Replacekey for the webapplication name (used in opencms.properties)
     */
    static final String C_WEB_APP_REPLACE_KEY = "${WEB_APP_NAME}";

    /**
     * The minimum-size of a passwordstring.
     */
    static final int C_PASSWORD_MINIMUMSIZE = 4;

    /**
     * The maximum length of a resource name (incl. path).
     */
    static final int C_MAX_LENGTH_RESOURCE_NAME = 240;

    /**
     * This flag is set for enabled entrys in the database.
     * (GROUP_FLAGS for example)
     */
    static final int C_FLAG_ENABLED = 0;

    /**
     * This flag is set for disabled entrys in the database.
     * (GROUP_FLAGS for example)
     */
    static final int C_FLAG_DISABLED = 1;

    /**
     * Flag constant: Projectmanager
     * flag for groups
     */
    static final int C_FLAG_GROUP_PROJECTMANAGER = 2;

    /**
     * Flag constant: ProjectCoWorker
     * flag for groups
     */
    static final int C_FLAG_GROUP_PROJECTCOWORKER = 4;

    /**
     * Flag constant: Role (for coworkers)
     * flag for groups
     */
    static final int C_FLAG_GROUP_ROLE = 8;

    /** prefix for temporary files */
    public static final String C_TEMP_PREFIX = "~";

    /**
     * The last index, that was used for resource-types.
     */
    final static String C_TYPE_LAST_INDEX = "lastIndex";

    /**
     * The resource type-id for a folder.
     */
    final static int C_TYPE_FOLDER = 0;

    /**
     * The resource type-id for a folder.
     */
    final static String C_TYPE_FOLDER_NAME = "folder";

    /** The resource type-name for an image. */
    final static String C_TYPE_IMAGE_NAME = "image";

    /**
     * The resource type-name for a page file.
     */
    final static String C_TYPE_PAGE_NAME = "page";

    /**
     * The resource type-name for plain files.
     */
    final static String C_TYPE_PLAIN_NAME = "plain";

    /**
     * The resource type-name for compatiblePlain files. This type is
     * used by the opencms system to show the user that this resource doesent
     * fit to the new standard in the folder content.
     */
    final static String C_TYPE_COMPATIBLEPLAIN_NAME = "compatiblePlain";

    /**
     * The resource type-name for body files.
     */
    final static String C_TYPE_BODY_NAME = "body";

    /**
     * Property for resource title
     */
    static final String C_PROPERTY_TITLE = "Title";

    /**
     * Property for resource navigation title
     */
    static final String C_PROPERTY_NAVTEXT = "NavText";

    /**
     * Property for resource navigation title
     */
    static final String C_PROPERTY_VISIBLE = "visiblemethod";

    /**
     * Property for resource export name. When resource is exported
     * by the static export this name is used instead of the realname.
     */
    static final String C_PROPERTY_EXPORTNAME = "exportname";

    /**
     * Property for resource export name. When resource is exported
     * by the static export this name is used instead of the realname.
     */
    static final String C_PROPERTY_EXPORT = "export";

    /**
     * Property for resource navigation title
     */
    static final String C_PROPERTY_ACTIV = "activemethod";

    /**
     * Property for resource navigation position
     */
    static final String C_PROPERTY_NAVPOS = "NavPos";

    /**
     * Property for template type
     */
    static final String C_PROPERTY_TEMPLATETYPE = "TemplateType";

    /**
     * Property for keywords
     */
    static final String C_PROPERTY_KEYWORDS = "Keywords";

    /**
     * Property for the description
     */
    static final String C_PROPERTY_DESCRIPTION = "Description";

    /**
     * Property for the description
     */
    static final String C_PROPERTY_CHANNELID = "ChannelId";
    
    /**
     * Property for content encoding
     */
    // Encoding project:
    static final String C_PROPERTY_CONTENT_ENCODING = "content-encoding";

    /**
     * Property for default file in folders
     */
    static final String C_PROPERTY_DEFAULT_FILE = "default-file";

    /**
     * Property for relative root link substitution
     */
    static final String C_PROPERTY_RELATIVEROOT = "relativeroot";

    /**
     * This is the group for guests.
     */
    static final String C_GROUP_GUEST = "Guests";

    /**
     * This is the group for administrators.
     */
    static final String C_GROUP_ADMIN = "Administrators";

    /**
     * This is the group for projectleaders. It is the only group, which
     * can create new projects.
     */
    static final String C_GROUP_PROJECTLEADER = "Projectmanager";

    /**
     * This is the group for users. If you are in this group, you can use
     * the workplace.
     */
    static final String C_GROUP_USERS = "Users";

    /**
     * This is the group for guests.
     */
    static final String C_USER_GUEST = "Guest";

    /**
     * This is the group for administrators.
     */
    static final String C_USER_ADMIN = "Admin";

    /**
     * A user-type
     */
    static final int C_USER_TYPE_SYSTEMUSER = 0;

    /**
     * A user-type
     */
    static final int C_USER_TYPE_WEBUSER = 1;

    /**
     * A user-type
     */
    static final int C_USER_TYPE_SYSTEMANDWEBUSER = 2;

    /**
     * Key for additional info address.
     */
    final static String C_ADDITIONAL_INFO_ZIPCODE = "USER_ZIPCODE";

    /**
     * Key for additional info address.
     */
    final static String C_ADDITIONAL_INFO_TOWN = "USER_TOWN";

    /**
     * Key for additional info flags.
     */
    final static String C_ADDITIONAL_INFO_PREFERENCES = "USER_PREFERENCES";

    /** Key for additional info explorer settings. */
    final static String C_ADDITIONAL_INFO_EXPLORERSETTINGS = "USER_EXPLORERSETTINGS";

    /** Key for additional info task settings. */
    final static String C_ADDITIONAL_INFO_TASKSETTINGS = "USER_TASKSETTINGS";

    /** Key for additional info start settings. */
    final static String C_ADDITIONAL_INFO_STARTSETTINGS = "USER_STARTSETTINGS";

    /**
     * This constant is used to order the tasks by date.
     */
    static final int C_TASK_ORDER_BY_DATE = 1;

    /**
     * This constant is used to order the tasks by name.
     */
    static final int C_TASK_ORDER_BY_NAME = 2;

    /**
     * This constant defines the onlineproject. This is the project which
     * is used to show the resources for guestusers
     */
    static final String C_PROJECT_ONLINE = "Online";

    /**
     * This constant defines the onlineproject. This is the project which
     * is used to show the resources for guestusers
     */
    static final int C_PROJECT_ONLINE_ID = 1;

    /**
     * This constant defines a normal project-type.
     */
    static final int C_PROJECT_TYPE_NORMAL = 0;

    /**
     * This constant defines a temporary project-type.
     * The project will be deleted after publishing
     */
    static final int C_PROJECT_TYPE_TEMPORARY = 1;

    /**
     * This constant defines a invisible project-type.
     * The project exists only for temporary files
     * and will never be displayed, published or deleted
     */
    static final int C_PROJECT_TYPE_INVISIBLE = 3;

    /**
     * This constant defines a unlocked project.
     * Resources may be changed in this project.
     */
    static final int C_PROJECT_STATE_UNLOCKED = 0;

    /**
     * This constant defines a locked project.
     * Resources can't be changed in this project.
     */
    static final int C_PROJECT_STATE_LOCKED = 1;

    /**
     * This constant defines a project in a archive.
     * Resources can't be changed in this project. Its state will never
     * go back to the previos one.
     */
    static final int C_PROJECT_STATE_ARCHIVE = 2;

    /**
     * This constant defines a project that is invisible.
     * The project is invisible for users. It is needed
     * for creating and editing temporary files
     */
    static final int C_PROJECT_STATE_INVISIBLE = 3;

    /**
     * This id will be returned for resources with no id.
     * (filesystem resources)
     */
    final static int C_UNKNOWN_ID = -1;

    /**
     * Owner may read this resource
     */
    final static int C_ACCESS_OWNER_READ = 1;

    /**
     * Owner may write this resource
     */
    final static int C_ACCESS_OWNER_WRITE = 2;

    /**
     * Owner may view this resource
     */
    final static int C_ACCESS_OWNER_VISIBLE = 4;

    /**
     * Group may read this resource
     */
    final static int C_ACCESS_GROUP_READ = 8;

    /**
     * Group may write this resource
     */
    final static int C_ACCESS_GROUP_WRITE = 16;

    /**
     * Group may view this resource
     */
    final static int C_ACCESS_GROUP_VISIBLE = 32;

    /**
     *  may read this resource
     */
    final static int C_ACCESS_PUBLIC_READ = 64;

    /**
     *  may write this resource
     */
    final static int C_ACCESS_PUBLIC_WRITE = 128;

    /**
     *  may view this resource
     */
    final static int C_ACCESS_PUBLIC_VISIBLE = 256;

    /**
     * This is an internal resource, it can't be launched directly.
     */
    final static int C_ACCESS_INTERNAL_READ = 512;

    /**
     * All may read this resource.
     */
    final static int C_ACCESS_READ = C_ACCESS_OWNER_READ + C_ACCESS_GROUP_READ + C_ACCESS_PUBLIC_READ;

    /**
     * All may write this resource.
     */
    final static int C_ACCESS_WRITE = C_ACCESS_OWNER_WRITE + C_ACCESS_GROUP_WRITE + C_ACCESS_PUBLIC_WRITE;

    /**
     * All may view this resource.
     */
    final static int C_ACCESS_VISIBLE = C_ACCESS_OWNER_VISIBLE + C_ACCESS_GROUP_VISIBLE + C_ACCESS_PUBLIC_VISIBLE;

    /**
     * Owner has full access to this resource.
     */
    final static int C_ACCESS_OWNER = C_ACCESS_OWNER_READ + C_ACCESS_OWNER_WRITE + C_ACCESS_OWNER_VISIBLE;

    /**
     * Group has full access to this resource.
     */
    final static int C_ACCESS_GROUP = C_ACCESS_GROUP_READ + C_ACCESS_GROUP_WRITE + C_ACCESS_GROUP_VISIBLE;

    /**
     *  has full access to this resource.
     */
    final static int C_ACCESS_PUBLIC = C_ACCESS_PUBLIC_READ + C_ACCESS_PUBLIC_WRITE + C_ACCESS_PUBLIC_VISIBLE;

    /**
     * The default-flags for a new resource.
     */
    final static int C_ACCESS_DEFAULT_FLAGS = C_ACCESS_OWNER_READ + C_ACCESS_OWNER_WRITE + C_ACCESS_OWNER_VISIBLE
            + C_ACCESS_GROUP_READ + C_ACCESS_GROUP_WRITE + C_ACCESS_GROUP_VISIBLE + C_ACCESS_PUBLIC_READ + C_ACCESS_PUBLIC_VISIBLE;

    /**
     * Is set, if the resource is unchanged in this project.
     */
    static final int C_STATE_UNCHANGED = 0;

    /**
     * Is set, if the resource was changed in this project.
     */
    static final int C_STATE_CHANGED = 1;

    /**
     * Is set, if the resource is new in this project.
     */
    static final int C_STATE_NEW = 2;

    /**
     * Is set, if the resource was deleted in this project.
     */
    static final int C_STATE_DELETED = 3;

    /**
     * This value will be returned for int's withaout a value.
     */
    static final int C_UNKNOWN_INT = -1;

    /**
     * This value will be returned for long's withaout a value.
     */
    static final int C_UNKNOWN_LONG = -1;

    /**
     * This is the id for an undefined launcher.
     */
    static final int C_UNKNOWN_LAUNCHER_ID = -1;

    /**
     * This is the classname for an undefined launcher.
     */
    static final String C_UNKNOWN_LAUNCHER = "UNKNOWN";

    /**
     * This is the defintion for a filesystem mountpoint.
     */
    static final int C_MOUNTPOINT_FILESYSTEM = 1;

    /**
     * This is the defintion for a database mountpoint.
     */
    static final int C_MOUNTPOINT_MYSQL = 2;

    /**
     * A string in the configuration-file.
     */
    static final String C_EXPORTPOINT = "exportpoint.";

    /**
     * A string in the configuration-file.
     */
    static final String C_EXPORTPOINT_PATH = "exportpoint.path.";

    /**
     * A string in the configuration-file.
     */
    static final String C_STATICEXPORT_PATH = "staticexport.path";

    /**
     * A string in the configuration-file.
     */
    static final String C_STATICEXPORT_START = "staticexport.start";

    /**
     * A string in the configuration-file.
     */
    static final String C_URL_PREFIX_EXPORT = "url_prefix_export";

    /**
     * A string in the configuration-file.
     */
    static final String C_URL_PREFIX_HTTP = "url_prefix_http";

    /**
     * A string in the configuration-file.
     */
    static final String C_URL_PREFIX_HTTPS = "url_prefix_https";

    /**
     * A string in the configuration-file.
     */
    static final String C_URL_PREFIX_SERVERNAME = "url_prefix_servername";

    /**
     * The folder - seberator in this system
     */
    static final String C_FOLDER_SEPARATOR = "/";

    /**
     * The name of the rood folder
     */
    static final String C_ROOT = C_FOLDER_SEPARATOR;

    /**
     * The name of the exportpath-systemproperty.
     */
    static final String C_SYSTEMPROPERTY_CRONTABLE = "CRONTABLE";

    /**
     * The name of the exportpath-systemproperty.
     */
    static final String C_SYSTEMPROPERTY_EXPORTPATH = "EXPORTPATH";

    /**
     * The name of the mountpoint-systemproperty.
     */
    static final String C_SYSTEMPROPERTY_MOUNTPOINT = "MOUNTPOINT";

    /**
     * The name of the mimetypes-systemproperty.
     */
    static final String C_SYSTEMPROPERTY_MIMETYPES = "MIMETYPES";

    /**
     * The name of the resourcetype-systemproperty.
     */
    static final String C_SYSTEMPROPERTY_RESOURCE_TYPE = "RESOURCE_TYPE";

    /**
     * The name of the resourcetype-extension.
     */
    static final String C_SYSTEMPROPERTY_EXTENSIONS = "EXTENSIONS";

    /**
     * The name of the linkchecktable-systemproperty.
     */
    static final String C_SYSTEMPROPERTY_LINKCHECKTABLE = "LINKCHECKTABLE";

    /**
     * The key for the username in the user information hashtable.
     */
    static final String C_SESSION_USERNAME = "USERNAME";

    /**
     * The key for the current usergroup the user information hashtable.
     */
    static final String C_SESSION_CURRENTGROUP = "CURRENTGROUP";

    /**
     * The key for the project in the user information hashtable.
     */
    static final String C_SESSION_PROJECT = "PROJECT";

    /**
     * The key for the dirty-flag in the session.
     */
    static final String C_SESSION_IS_DIRTY = "_core_session_is_dirty_";

    /**
     * The key for the original session to store the session data.
     */
    static final String C_SESSION_DATA = "_session_data_";

    /**
     * The key for the session to store Broadcast messages.
     */
    static final String C_SESSION_BROADCASTMESSAGE = "BROADCASTMESSAGE";

    /**
     * The key for the session to store, if a message is pending for this user.
     */
    static final String C_SESSION_MESSAGEPENDING = "BROADCASTMESSAGE_PENDING";

    /**
     * Session key for storing the possition in the administration navigation
     */
    public static final String C_SESSION_ADMIN_POS = "adminposition";

    /**
     * Session key for storing a possible error while executing a thread
     */
    public static final String C_SESSION_THREAD_ERROR = "threaderror";

    /**
     * Session key for storing the files Vector for moduleimport.
     */
    public static final String C_SESSION_MODULE_VECTOR = "modulevector";
    
    /**
     * Session key for storing the current charcter encoding to be used in HTTP
     * requests and responses.
     */
    // Encoding project:
    public static final String C_SESSION_CONTENT_ENCODING = "content-encoding";

    /** Identifier for request type http */
    public static final int C_REQUEST_HTTP = 0;

    /** Identifier for request type console */
    public static final int C_REQUEST_CONSOLE = 1;

    /** Identifier for request type http */
    public static final int C_RESPONSE_HTTP = 0;

    /** Identifier for request type console */
    public static final int C_RESPONSE_CONSOLE = 1;

    /** Task type value of getting all tasks  */
    public static final int C_TASKS_ALL = 1;

    /** Task type value of getting new tasks  */
    public static final int C_TASKS_NEW = 2;

    /** Task type value of getting open tasks  */
    public static final int C_TASKS_OPEN = 3;

    /** Task type value of getting active tasks  */
    public static final int C_TASKS_ACTIVE = 4;

    /** Task type value of getting done tasks  */
    public static final int C_TASKS_DONE = 5;

    /** Task order value */
    public static final String C_ORDER_ID = "id";

    /** Task order value */
    public static final String C_ORDER_NAME = "name";

    /** Task order value */
    public static final String C_ORDER_STATE = "state";

    /** Task order value */
    public static final String C_ORDER_TASKTYPE = "tasktyperef";

    /** Task order value */
    public static final String C_ORDER_INITIATORUSER = "initiatoruserref";

    /** Task order value */
    public static final String C_ORDER_ROLE = "roleref";

    /** Task order value */
    public static final String C_ORDER_AGENTUSER = "agentuserref";

    /** Task order value */
    public static final String C_ORDER_ORIGINALUSER = "originaluserref";

    /** Task order value */
    public static final String C_ORDER_STARTTIME = "starttime";

    /** Task order value */
    public static final String C_ORDER_WAKEUPTIME = "wakeuptime";

    /** Task order value */
    public static final String C_ORDER_TIMEOUT = "timeout";

    /** Task order value */
    public static final String C_ORDER_ENDTIME = "endtime";

    /** Task order value */
    public static final String C_ORDER_PERCENTAGE = "percentage";

    /** Task order value */
    public static final String C_ORDER_PRIORITY = "priorityref";

    /** Task sort value ascending  */
    public static final String C_SORT_ASC = "ASC";

    /** Task sort value descending */
    public static final String C_SORT_DESC = "DESC";

    /** Task priority high */
    public static final int C_TASK_PRIORITY_HIGH = 1;

    /** Task priority normal */
    public static final int C_TASK_PRIORITY_NORMAL = 2;

    /** Task priority low */
    public static final int C_TASK_PRIORITY_LOW = 3;

    /** Value for order tasks by none */
    public static final int C_TASKORDER_NONE = 0;

    /** Value for order tasks by startdate */
    public static final int C_TASKORDER_STARTDATE = 1;

    /** Value for order tasks by timeout */
    public static final int C_TASKORDER_TIMEOUT = 2;

    /** Value for order tasks by taskname */
    public static final int C_TASKSORDER_TASKNAME = 3;

    /** state values of a task prepared to start*/
    public static final int C_TASK_STATE_PREPARE = 0;

    /** state values of a task ready to start */
    public static final int C_TASK_STATE_START = 1;

    /** state values of a task started */
    public static final int C_TASK_STATE_STARTED = 2;

    /** state values of a task ready to end */
    public static final int C_TASK_STATE_NOTENDED = 3;

    /** state values of a task ended */
    public static final int C_TASK_STATE_ENDED = 4;

    /** state values of a task halted */
    public static final int C_TASK_STATE_HALTED = 5;

    /**System type values for the task log */
    public static final int C_TASKLOG_SYSTEM = 0;

    /**User type value for the task log */
    public static final int C_TASKLOG_USER = 1;

    /** state values of task messages when accepted */
    public static final int C_TASK_MESSAGES_ACCEPTED = 1;

    /** state values of task messages when forwared */
    public static final int C_TASK_MESSAGES_FORWARDED = 2;

    /** state values of task messages when completed */
    public static final int C_TASK_MESSAGES_COMPLETED = 4;

    /** state values of task messages when members */
    public static final int C_TASK_MESSAGES_MEMBERS = 8;
    public final static String C_FILE = "FILE";
    public final static String C_FILECONTENT = "CONTENT";
    public final static String C_FOLDER = "FOLDER";
    public final static String C_USER = "USER";
    public final static String C_GROUP = "GROUP";

    /**
     * Values for the database import and export
     */

    /** Export type value - exports users and resources */
    final static int C_EXPORTUSERSFILES = 0;

    /**Export type value - exports only users */
    final static int C_EXPORTONLYUSERS = 1;

    /**Export type value - exports only resources */
    final static int C_EXPORTONLYFILES = 2;

    /**Are files imported - NO */
    final static int C_NO_FILES_IMPORTED = 0;

    /**Are files imported - yes */
    final static int C_FILES_IMPORTED = 1;

    /**
     * root element in the XML file (the document node)
     * needed, to insert other elements
     */
    final static String C_FELEMENT = "CMS_EXPORT";

    /** first XML element tag for the resources */
    final static String C_TFILES = "FILES";

    /** XML tag to defines one resource */
    final static String C_TFILEOBJ = "FILEOBJ";

    /** XML tag to defines the resource name */
    final static String C_TFNAME = "NAME";

    /** XML tag to defines the resource type */
    final static String C_TFTYPE = "TYPE";

    /** XML tag to defines the resource typename */
    final static String C_TFTYPENAME = "TYPENAME";

    /** XML tag to defines user acces */
    final static String C_TFUSER = "USER";

    /** XML tag to defines group acces */
    final static String C_TFGROUP = "GROUP";

    /** XML tag to defines file acces */
    final static String C_TFACCESS = "ACCESFLAG";

    /** XML tag to defines the resource property */
    final static String C_TFPROPERTYINFO = "PROPERTYINFO";

    /** XML tag to defines the resource property */
    final static String C_TFPROPERTYNAME = "PROPERTYNAME";

    /** XML tag to defines the resource propertytype */
    final static String C_TFPROPERTYTYPE = "PROPERTYTYP";

    /** XML tag to defines the resource propertyvalue */
    final static String C_TFPROPERTYVALUE = "PROPERTYVALUE";

    /** XML tag to defines the resource content if resource is a file */
    final static String C_FCONTENT = "CONTENT";

    /** first XML element tag for the groups */
    final static String C_TGROUPS = "GROUPS";

    /** XML tag to defines one group */
    final static String C_TGROUPOBJ = "GROUPOBJ";

    /** XML tag to defines the group name */
    final static String C_TGNAME = "NAME";

    /** XML tag to defines the parentgroup name */
    final static String C_TGPARENTGROUP = "PARENTGROUP";

    /** XML tag to defines the description of group */
    final static String C_TGDESC = "DESC";

    /** XML tag to defines the flag of group */
    final static String C_TGFLAG = "FLAG";

    /** XML tag to defines all users of the group */
    final static String C_TGROUPUSERS = "GROUPUSERS";

    /** XML tag to defines the name of  user in group */
    final static String C_TGUSER = "USER";

    /** first XML element tag for the users */
    final static String C_TUSERS = "USERS";

    /** XML tag to defines one user */
    final static String C_TUSEROBJ = "USEROBJ";

    /** XML tag to defines the user login */
    final static String C_TULOGIN = "LOGIN";

    /** XML tag to defines the user PASSWD default "Kennwort" */
    final static String C_TUPASSWD = "PASSWD";

    /** XML tag to defines the user Lastname */
    final static String C_TUNAME = "NAME";

    /** XML tag to defines the user Firstname */
    final static String C_TUFIRSTNAME = "FIRSTNAME";

    /** XML tag to defines the user Description */
    final static String C_TUDESC = "DESC";

    /** XML tag to defines the user EMail */
    final static String C_TUEMAIL = "EMAIL";

    /** XML tag to defines the user defaultgroup */
    final static String C_TUDGROUP = "DEFAULTGROUP";

    /** XML tag to defines if the user is disabled */
    final static String C_TUDISABLED = "DISABLED";

    /** XML tag to defines the user flag */
    final static String C_TUFLAG = "FLAG";

    /** XML tag to defines the groups in which the user is in */
    final static String C_TUSERGROUPS = "USERGROUPS";

    /** XML tag to defines the user group name */
    final static String C_TUGROUP = "GROUP";

    /** XML tag to defines additional user info */
    final static String C_TUADDINFO = "ADDINFO";

    /** XML tag to defines additional user info key */
    final static String C_TUINFOKEY = "INFOKEY";

    /** XML tag to defines additional user info value */
    final static String C_TUINFOVALUE = "INFOVALUE";

    // Contants for preferences

    /** Task preferenses filter */
    public static final String C_TASK_FILTER = "TaskFilter";

    /** Task preferenses view all */
    public static final String C_TASK_VIEW_ALL = "TaskViewAll";

    /** Task preferenses message flags */
    public static final String C_TASK_MESSAGES = "TaskMessages";

    /** Start preferenses Language */
    public static final String C_START_LANGUAGE = "StartLanguage";

    /** Start preferenses Project */
    public static final String C_START_PROJECT = "StartProject";

    /** Start preferenses View */
    public static final String C_START_VIEW = "StartView";

    /** Start preferenses DefaultGroup */
    public static final String C_START_DEFAULTGROUP = "StartDefaultGroup";

    /** Start preferenses lock dialog */
    public static final String C_START_LOCKDIALOG = "StartLockDialog";

    /** Start preferenses AccessFlags */
    public static final String C_START_ACCESSFLAGS = "StartAccessFlags";

    /** Template element name used for the canonical root template */
    public static final String C_ROOT_TEMPLATE_NAME = "root";

    // Constants for import/export

    /**
     * The filename of the xml manifest.
     */
    public static String C_EXPORT_XMLFILENAME = "manifest.xml";

    /**
     * The version of the opencms export (appears in the export manifest-file).
     */
    public static String C_EXPORT_VERSION = "2";

    /**
     * A tag in the export manifest-file.
     */
    public static String C_EXPORT_TAG_INFO = "info";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    public static String C_EXPORT_TAG_CREATOR = "creator";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    public static String C_EXPORT_TAG_OC_VERSION = "opencms_version";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    public static String C_EXPORT_TAG_DATE = "createdate";

    /**
     * A tag in the manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    public static String C_EXPORT_TAG_PROJECT = "project";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    public static String C_EXPORT_TAG_VERSION = "export_version";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_FILE = "file";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_SOURCE = "source";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_DESTINATION = "destination";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_TYPE = "type";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_USER = "user";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_GROUP = "group";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_ACCESS = "access";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_PROPERTY = "property";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_NAME = "name";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_VALUE = "value";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_EXPORT = "export";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_MODULEXPORT = "modulexport";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_PROPERTIES = "properties";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_LAUNCHER_START_CLASS = "startclass";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_USERGROUPDATA = "usergroupdata";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_USERDATA = "userdata";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_GROUPDATA = "groupdata";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_DESCRIPTION = "description";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_FLAGS = "flags";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_PARENTGROUP = "parentgroup";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_PASSWORD = "password";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_RECOVERYPASSWORD = "recoverypassword";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_FIRSTNAME = "firstname";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_LASTNAME = "lastname";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_EMAIL = "email";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_DEFAULTGROUP = "defaultgroup";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_ADDRESS = "address";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_SECTION = "section";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_USERINFO = "userinfo";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_USERGROUPS = "usergroups";

    /**
     * A tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_GROUPNAME = "groupname";
    
    /**
     * The "lastmodified" tag in the manifest-file.
     */
    public static String C_EXPORT_TAG_LASTMODIFIED = "lastmodified";
    
    /**
     * A string in the configuration-file.
     */    
    public static String C_CONFIGURATIONS_POOL = "pool";

	/**
	 * A string in the configuration-file.
	 */    
	public static String C_CONFIGURATIONS_ACCESS = "access";

	/**
	 * A string in the configuration-file.
	 */  
	public static String C_CONFIGURATION_DB = "db";
		
    /**
     * A string in the configuration-file.
     */
    public static String C_CONFIGURATION_CACHE = "cache";

    /**
     * A string in the configuration-file.
     */
    public static String C_CONFIGURATION_CLASS = "class";

    /**
     * A string in the configuration-file.
     */
    public static String C_CONFIGURATION_REGISTRY = "registry";

    /**
     * A string in the configuration-file.
     */
    public static String C_CLUSTERURL = "clusterurl";
    
    /**
     * wasLoggedIn
     */
    static final int C_NEVER = 1;

    /**
     * wasLoggedIn
     */
    static final int C_AT_LEAST_ONCE = 2;

    /**
     * wasLoggedIn
     */
    static final int C_WHATEVER = 3;

    /**
     * The name of the synchronizationpath-tag in registry.
     */
    static final String C_SYNCHRONISATION_PATH = "syncpath";

    /**
     * The name of the synchronizationproject-tag in registry.
     */
    static final String C_SYNCHRONISATION_PROJECT = "syncproject";

    /**
     * The name of the synchronizationresource-tag in registry.
     */
    static final String C_SYNCHRONISATION_RESOURCE = "syncresource";

    /**
     * The name of the synchronizationresource-tag in registry.
     */
    static final String C_SYNCHRONISATION_RESOURCETAG = "res";

    /**
     * The name of the known launchers tag in registry.
     */
    static final String C_REGISTRY_KNOWNLAUNCHERS = "launchers";

    /**
     * The name of the launcher tag in registry.
     */
    static final String C_REGISTRY_LAUNCHER = "launcher";
    
    /**
     * The name of the exportpoint source tag in registry.
     */
    static final String C_REGISTRY_SOURCE = "source";

    /**
     * The name of the exportpoint destination tag in registry.
     */
    static final String C_REGISTRY_DESTINATION = "destination";
    
    /**
     * The name of the error tag seperator in backoffice templates
     */
    static final String C_ERRSPERATOR="_";

    /**
    * The name of the error tag prefix in backoffice templates
    */
    static final String C_ERRPREFIX="err";

    /**
     * The name of the default site
     */
    static final String C_DEFAULT_SITE = "/default";

    /**
     * The root name of the VFS
     */
    static final String C_ROOTNAME_VFS = "/vfs";

    /**
     * The root name of the COS
     */
    static final String C_ROOTNAME_COS = "/cos";

    /**
     * The name of the entry for the id generator to create new channelid's
     */
    static final String C_TABLE_CHANNELID = "CORE_CHANNEL_ID";

    /**
     * The modus the cmsObject runs in. There are three diffrent: online, offline
     * and export. It is used by the link replacement mechanism. (the extern mode is
     * used for the names in the filesystem for the exported files.)
     */
    static final int C_MODUS_AUTO = -1;
    static final int C_MODUS_ONLINE = 0;
    static final int C_MODUS_OFFLINE = 1;
    static final int C_MODUS_EXPORT = 2;
    static final int C_MODUS_EXTERN = 3;

    /**
     * The attribute of the publishclass tag in the modules registry used to show
     * that this method needs the vector of the changed links for publishing.
     * (i.e. the search module)
     */
    static final String C_PUBLISH_METHOD_LINK = "linkpublish";

    /**
     * The key for the date of the last linkcheck in the linkchecktable
     */
    static final String C_LINKCHECKTABLE_DATE = "linkcheckdate";
    
    /**
     * The name of the tag in registry for history properties.
     */ 
    static final String C_REGISTRY_HISTORY = "history";
    
    /**
     * The name of the tag in registry if versions of the history should be deleted 
     */     
    static final String C_DELETE_HISTORY = "deleteversions";
    
    /**
     * The name of the tag in registry for the number of weeks the versions should remain in the history.
     */ 
    static final String C_WEEKS_HISTORY = "weeks";
    
    /**
     * The name of the tag in registry if history is enabled.
     */     
    static final String C_ENABLE_HISTORY = "enabled";
    
    /**
     * The module property key name to specifiy additional resources which are
     * part of a module outside of {system/modules}.
     */
    static final String C_MODULE_PROPERTY_ADDITIONAL_RESOURCES = "additionalresources";
    
    /**
     * Character to separate additional resources specified in the module properties.
     */
    static final String C_MODULE_PROPERTY_ADDITIONAL_RESOURCES_SEPARATOR = ";";    
    
    /** name of the special body element from an XMLTemplate */
    public final static String C_XML_BODY_ELEMENT = "body";    
}


