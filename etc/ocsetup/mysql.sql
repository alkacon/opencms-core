# setupscript for mysql-database

create table CMS_SYSTEMPROPERTIES       (SYSTEMPROPERTY_ID int not null,
                                         SYSTEMPROPERTY_NAME VARCHAR(40) not null, 
                                         SYSTEMPROPERTY_VALUE blob,
                                         primary key(SYSTEMPROPERTY_ID),
                                         unique(SYSTEMPROPERTY_NAME));

create table CMS_USERS                  (USER_ID int not null,
                                         USER_NAME VARCHAR(32) not null,
                                         USER_PASSWORD VARCHAR(32) not null,
                                         USER_RECOVERY_PASSWORD VARCHAR(32) not null,
                                         USER_DESCRIPTION VARCHAR(255) not null,
                                         USER_FIRSTNAME VARCHAR(50) not null,
                                         USER_LASTNAME VARCHAR(50) not null,
                                         USER_EMAIL VARCHAR(50) not null,
                                         USER_LASTLOGIN DATETIME not null,
                                         USER_LASTUSED DATETIME not null,
                                         USER_FLAGS int not null,
                                         USER_INFO blob,
                                         USER_DEFAULT_GROUP_ID int not null,
                                         USER_ADDRESS VARCHAR(50) not null,
                                         USER_SECTION VARCHAR(50) not null,
                                         USER_TYPE int not null,
                                         primary key(USER_ID), unique(USER_NAME));

create table CMS_GROUPS                 (GROUP_ID int not null auto_increment,
                                         PARENT_GROUP_ID int not null,
                                         GROUP_NAME VARCHAR(16) not null,
                                         GROUP_DESCRIPTION VARCHAR(255) not null,
                                         GROUP_FLAGS int not null,
                                         primary key(GROUP_ID),
                                         unique(GROUP_NAME),
										 key group_parentid (parent_group_id));

create table CMS_GROUPUSERS             (GROUP_ID int not null,
                                         USER_ID int not null,
                                         GROUPUSER_FLAGS int not null,
                                         key(GROUP_ID),
                                         key(USER_ID));

create table CMS_PROJECTS               (PROJECT_ID int not null, 
                                         USER_ID int not null,
                                         GROUP_ID int not null, 
                                         MANAGERGROUP_ID int not null,
                                         TASK_ID int not null,
                                         PROJECT_NAME VARCHAR(16) not null,
                                         PROJECT_DESCRIPTION VARCHAR(255) not null,
                                         PROJECT_FLAGS int not null,
                                         PROJECT_CREATEDATE datetime not null,
                                         PROJECT_TYPE int not null,
                                         primary key(PROJECT_ID), 
                                         key(PROJECT_NAME, PROJECT_CREATEDATE),
										 key project_flags (project_flags),
										 key projects_groupid (group_id),
										 key projects_managerid (managergroup_id),
										 key projects_userid (user_id),
										 key projects_taskid (task_id),
                                         unique(PROJECT_NAME, PROJECT_CREATEDATE));

create table CMS_BACKUP_PROJECTS         (VERSION_ID int not null,
                                          PROJECT_ID int not null,
                                          PROJECT_NAME VARCHAR(16) not null,
                                          PROJECT_PUBLISHDATE datetime,
                                          PROJECT_PUBLISHED_BY int not null,
                                          PROJECT_PUBLISHED_BY_NAME VARCHAR(135),
                                          USER_ID int not null,
                                          USER_NAME VARCHAR(135),
                                          GROUP_ID int not null,
                                          GROUP_NAME VARCHAR(16),
                                          MANAGERGROUP_ID int not null,
                                          MANAGERGROUP_NAME VARCHAR(16),
                                          PROJECT_DESCRIPTION VARCHAR(255) not null,
                                          PROJECT_CREATEDATE datetime not null,
                                          PROJECT_TYPE int not null,
                                          TASK_ID int not null,
                                          primary key (VERSION_ID));

create table CMS_PROJECTRESOURCES       (PROJECT_ID int NOT NULL,
                                         RESOURCE_NAME VARCHAR(248) NOT NULL,
                                         primary key(PROJECT_ID, RESOURCE_NAME),
										 index projectresource_resource_name (RESOURCE_NAME));

create table CMS_BACKUP_PROJECTRESOURCES (VERSION_ID int NOT NULL,
                                          PROJECT_ID int NOT NULL,
                                          RESOURCE_NAME VARCHAR(248) NOT NULL,
                                          PRIMARY KEY (VERSION_ID, PROJECT_ID, RESOURCE_NAME));


create table CMS_PROPERTYDEF            (PROPERTYDEF_ID int not null, 
                                         PROPERTYDEF_NAME VARCHAR(64) not null,
                                         RESOURCE_TYPE int not null,
                                         primary key(PROPERTYDEF_ID), 
										 unique(PROPERTYDEF_NAME, RESOURCE_TYPE));

create table CMS_ONLINE_PROPERTYDEF      (PROPERTYDEF_ID int not null, 
                                         PROPERTYDEF_NAME VARCHAR(64) not null,
                                         RESOURCE_TYPE int not null,
                                         primary key(PROPERTYDEF_ID), 
										 unique(PROPERTYDEF_NAME, RESOURCE_TYPE));
										
create table CMS_BACKUP_PROPERTYDEF      (PROPERTYDEF_ID int not null, 
                                         PROPERTYDEF_NAME VARCHAR(64) not null,
                                         RESOURCE_TYPE int not null,
                                         primary key(PROPERTYDEF_ID), 
										 unique(PROPERTYDEF_NAME, RESOURCE_TYPE));

create table CMS_PROPERTIES             (PROPERTY_ID int not null,
                                         PROPERTYDEF_ID int not null,
                                         RESOURCE_ID int not null,
                                         PROPERTY_VALUE VARCHAR(255) not null,
                                         primary key(PROPERTY_ID), 
										 unique(PROPERTYDEF_ID, RESOURCE_ID));

create table CMS_ONLINE_PROPERTIES      (PROPERTY_ID int not null,
                                         PROPERTYDEF_ID int not null,
                                         RESOURCE_ID int not null,
                                         PROPERTY_VALUE VARCHAR(255) not null,
                                         primary key(PROPERTY_ID), 
										 unique(PROPERTYDEF_ID, RESOURCE_ID));
										 
create table CMS_BACKUP_PROPERTIES      (PROPERTY_ID int not null,
                                         PROPERTYDEF_ID int not null,
                                         RESOURCE_ID int not null,
                                         PROPERTY_VALUE VARCHAR(255) not null,
                                         primary key(PROPERTY_ID), 
										 unique(PROPERTYDEF_ID, RESOURCE_ID));
										 
create table CMS_RESOURCES              (RESOURCE_ID int not null,
                                         PARENT_ID int not null,
                                         RESOURCE_NAME VARCHAR(248) not null,
                                         RESOURCE_TYPE int not null,
                                         RESOURCE_FLAGS int not null,
                                         USER_ID int not null,
                                         GROUP_ID int not null,
                                         PROJECT_ID int not null,
                                         FILE_ID int not null,
                                         ACCESS_FLAGS int not null,
                                         STATE int not null,
                                         LOCKED_BY int not null,
                                         LAUNCHER_TYPE int not null,
                                         LAUNCHER_CLASSNAME VARCHAR(255) not null,
                                         DATE_CREATED datetime not null,
                                         DATE_LASTMODIFIED datetime not null,
                                         RESOURCE_SIZE int not null,
                                         RESOURCE_LASTMODIFIED_BY int not null,
                                         primary key(RESOURCE_ID),
                                         key(RESOURCE_NAME,PROJECT_ID),
										 key resource_fileid (FILE_ID),
										 key resource_group (GROUP_ID),
										 key resource_locked_by (LOCKED_BY),
										 key resource_parentid (PARENT_ID),
										 key resource_projectid (PROJECT_ID),
										 key resources_state (STATE),
										 key resources_type (RESOURCE_TYPE),
										 key resource_userid (USER_ID),
										 index parent_resource_type (PARENT_ID, RESOURCE_TYPE),
										 index resources_project_type (PROJECT_ID, RESOURCE_TYPE),
										 index resources_resourceid_project (RESOURCE_ID, PROJECT_ID),
                                         unique(RESOURCE_NAME));

create table CMS_ONLINE_RESOURCES       (RESOURCE_ID int not null,
                                         PARENT_ID int not null,
                                         RESOURCE_NAME VARCHAR(248) not null,
                                         RESOURCE_TYPE int not null,
                                         RESOURCE_FLAGS int not null,
                                         USER_ID int not null,
                                         GROUP_ID int not null,
                                         PROJECT_ID int not null,
                                         FILE_ID int not null,
                                         ACCESS_FLAGS int not null,
                                         STATE int not null,
                                         LOCKED_BY int not null,
                                         LAUNCHER_TYPE int not null,
                                         LAUNCHER_CLASSNAME VARCHAR(255) not null,
                                         DATE_CREATED datetime not null,
                                         DATE_LASTMODIFIED datetime not null,
                                         RESOURCE_SIZE int not null,
                                         RESOURCE_LASTMODIFIED_BY int not null,
                                         primary key(RESOURCE_ID),
                                         key(RESOURCE_NAME,PROJECT_ID),
										 key resource_fileid (FILE_ID),
										 key resource_group (GROUP_ID),
										 key resource_locked_by (LOCKED_BY),
										 key resource_parentid (PARENT_ID),
										 key resource_projectid (PROJECT_ID),
										 key resources_state (STATE),
										 key resources_type (RESOURCE_TYPE),
										 key resource_userid (USER_ID),
										 index parent_resource_type (PARENT_ID, RESOURCE_TYPE),
										 index resources_project_type (PROJECT_ID, RESOURCE_TYPE),
                                         unique(RESOURCE_NAME));
										 
create table CMS_BACKUP_RESOURCES       (RESOURCE_ID int not null,
                                         PARENT_ID int not null,
                                         RESOURCE_NAME VARCHAR(248) not null,
                                         RESOURCE_TYPE int not null,
                                         RESOURCE_FLAGS int not null,
                                         USER_ID int not null,
										 USER_NAME VARCHAR(135),
                                         GROUP_ID int not null,
										 GROUP_NAME VARCHAR(16),
                                         PROJECT_ID int not null,
                                         FILE_ID int not null,
                                         ACCESS_FLAGS int not null,
                                         STATE int not null,
                                         LAUNCHER_TYPE int not null,
                                         LAUNCHER_CLASSNAME VARCHAR(255) not null,
                                         DATE_CREATED datetime not null,
                                         DATE_LASTMODIFIED datetime not null,
                                         RESOURCE_SIZE int not null,
                                         RESOURCE_LASTMODIFIED_BY int not null,
										 RESOURCE_LASTMODIFIED_BY_NAME VARCHAR(135),
										 VERSION_ID int not null,
                                         primary key(RESOURCE_ID),
                                         key(VERSION_ID,RESOURCE_NAME,PROJECT_ID),
										 key resource_fileid (FILE_ID),
										 key resource_group (GROUP_ID),
										 key resource_parentid (PARENT_ID),
										 key resource_projectid (PROJECT_ID),
										 key resources_state (STATE),
										 key resources_type (RESOURCE_TYPE),
										 key resource_userid (USER_ID),
										 index parent_resource_type (PARENT_ID, RESOURCE_TYPE),
										 index resources_project_type (PROJECT_ID, RESOURCE_TYPE),
										 index resources_resourceid_project (RESOURCE_ID, PROJECT_ID),
                                         unique(RESOURCE_NAME,DATE_LASTMODIFIED));
										 
create table CMS_FILES                  (FILE_ID int not null,
                                         FILE_CONTENT mediumblob not null,
                                         primary key(FILE_ID));

create table CMS_ONLINE_FILES           (FILE_ID int not null,
                                         FILE_CONTENT mediumblob not null,
                                         primary key(FILE_ID));

create table CMS_BACKUP_FILES           (FILE_ID int not null,
                                         FILE_CONTENT mediumblob not null,
                                         primary key(FILE_ID));

create table CMS_SYSTEMID               (TABLE_KEY varchar(255) not null,
                                         ID int not null,
                                         primary key(TABLE_KEY));
										
create table CMS_SESSIONS               (SESSION_ID varchar(255) not null,
                                         SESSION_LASTUSED datetime not null,
										 SESSION_DATA mediumblob not null,
										 index session_lastused (SESSION_LASTUSED),
                                         primary key(SESSION_ID));
										 
#
# Table structure for table 'GlobeTask'
#
										 
										 CREATE TABLE CMS_TASK (
										   AUTOFINISH int(11),
										   ENDTIME datetime,
										   ESCALATIONTYPEREF int(11),
										   ID int(11) NOT NULL,
										   INITIATORUSERREF int(11),
										   MILESTONEREF int(11),
										   NAME varchar(254),
										   ORIGINALUSERREF int(11),
										   AGENTUSERREF int(11),
										   PARENT int(11),
										   PERCENTAGE varchar(50),
										   PERMISSION varchar(50),
										   PRIORITYREF int(11) DEFAULT '2',
										   ROLEREF int(11),
										   ROOT int(11),
										   STARTTIME datetime,
										   STATE int(11),
										   TASKTYPEREF int(11),
										   TIMEOUT datetime,
										   WAKEUPTIME datetime,
										   HTMLLINK varchar(254),
										   ESTIMATETIME int(11) DEFAULT '86400',
										   PRIMARY KEY (id)
										 );
										 
#
# Table structure for table 'GlobeTaskType'
#
										 
										 CREATE TABLE CMS_TASKTYPE (
										   AUTOFINISH int(11),
										   ESCALATIONTYPEREF int(11),
										   HTMLLINK varchar(254),
										   ID int(11) NOT NULL,
										   NAME varchar(50),
										   PERMISSION varchar(50),
										   PRIORITYREF int(11),
										   ROLEREF int(11),
										   PRIMARY KEY (id)
										 );
										 										 
#
# Table structure for table 'GlobeTaskLog'
#
										 
										 CREATE TABLE CMS_TASKLOG (
										   COMENT text,
										   EXTERNALUSERNAME varchar(254),
										   ID int(11) NOT NULL,
										   STARTTIME datetime,
										   TASKREF int(11),
										   USERREF int(11),
										   TYPE int(18) DEFAULT '0',
										   PRIMARY KEY (id)
										 );
										 
#
# Table structure for table 'GlobeTaskPar'
#
										 
										 CREATE TABLE CMS_TASKPAR (
										   ID int(11) NOT NULL ,
										   PARNAME varchar(50),
										   PARVALUE varchar(50),
										   REF int(11),
										   PRIMARY KEY (id)
);

