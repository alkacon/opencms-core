CREATE TABLE CMS_SYSTEMPROPERTIES
(SYSTEMPROPERTY_ID int not null,
 SYSTEMPROPERTY_NAME VARCHAR(40) not null,
 SYSTEMPROPERTY_VALUE blob,
 PRIMARY KEY (SYSTEMPROPERTY_ID),
 UNIQUE (SYSTEMPROPERTY_NAME));

CREATE TABLE CMS_USERS
(USER_ID int not null,
 USER_NAME VARCHAR(64) not null,
 USER_PASSWORD VARCHAR(32) not null,
 USER_RECOVERY_PASSWORD VARCHAR(32) not null,
 USER_DESCRIPTION VARCHAR(255) not null,
 USER_FIRSTNAME VARCHAR(50) not null,
 USER_LASTNAME VARCHAR(50) not null,
 USER_EMAIL VARCHAR(50) not null,
 USER_LASTLOGIN DATE not null,
 USER_LASTUSED DATE not null,
 USER_FLAGS int not null,
 USER_INFO blob,
 USER_DEFAULT_GROUP_ID int not null,
 USER_ADDRESS VARCHAR(50) not null,
 USER_SECTION VARCHAR(50) not null,
 USER_TYPE int not null,
 primary key(USER_ID),
 unique(USER_NAME));

CREATE TABLE CMS_PROJECTS
(PROJECT_ID int not null,
 USER_ID int not null,
 GROUP_ID int not null,
 MANAGERGROUP_ID int not null,
 TASK_ID int not null,
 PROJECT_NAME VARCHAR(64) not null,
 PROJECT_DESCRIPTION VARCHAR(255) not null,
 PROJECT_FLAGS int not null,
 PROJECT_CREATEDATE date not null,
 PROJECT_PUBLISHDATE date,
 PROJECT_PUBLISHED_BY int not null,
 PROJECT_TYPE int not null,
 primary key (PROJECT_ID),
 unique(PROJECT_NAME,PROJECT_CREATEDATE));

CREATE TABLE CMS_PROJECTRESOURCES
(PROJECT_ID int NOT NULL,
RESOURCE_NAME VARCHAR(248) NOT NULL,
PRIMARY KEY (PROJECT_ID, RESOURCE_NAME));

CREATE TABLE CMS_PROPERTYDEF
(PROPERTYDEF_ID int not null,
 PROPERTYDEF_NAME VARCHAR(64) not null,
 RESOURCE_TYPE int not null,
 PROPERTYDEF_TYPE int not null,
 primary key(PROPERTYDEF_ID),
 unique(PROPERTYDEF_NAME,
 RESOURCE_TYPE));

CREATE TABLE CMS_ONLINE_PROPERTYDEF
(PROPERTYDEF_ID int not null,
 PROPERTYDEF_NAME VARCHAR(64) not null,
 RESOURCE_TYPE int not null,
 PROPERTYDEF_TYPE int not null,
 primary key(PROPERTYDEF_ID),
 unique(PROPERTYDEF_NAME,
 RESOURCE_TYPE));

CREATE TABLE CMS_BACKUP_PROPERTYDEF
(PROPERTYDEF_ID int not null,
 PROPERTYDEF_NAME VARCHAR(64) not null,
 RESOURCE_TYPE int not null,
 PROPERTYDEF_TYPE int not null,
 primary key(PROPERTYDEF_ID),
 unique(PROPERTYDEF_NAME,
 RESOURCE_TYPE));
  
CREATE TABLE CMS_PROPERTIES
(PROPERTY_ID int not null,
 PROPERTYDEF_ID int not null,
 RESOURCE_ID int not null,
 PROPERTY_VALUE VARCHAR(255) not null,
 primary key(PROPERTY_ID),
 unique(PROPERTYDEF_ID,
 RESOURCE_ID));

CREATE TABLE CMS_ONLINE_PROPERTIES
(PROPERTY_ID int not null,
 PROPERTYDEF_ID int not null,
 RESOURCE_ID int not null,
 PROPERTY_VALUE VARCHAR(255) not null,
 primary key(PROPERTY_ID),
 unique(PROPERTYDEF_ID,
 RESOURCE_ID));

CREATE TABLE CMS_BACKUP_PROPERTIES
(PROPERTY_ID int not null,
 PROPERTYDEF_ID int not null,
 RESOURCE_ID int not null,
 PROPERTY_VALUE VARCHAR(255) not null,
 primary key(PROPERTY_ID),
 unique(PROPERTYDEF_ID,
 RESOURCE_ID));

CREATE TABLE CMS_RESOURCES
(RESOURCE_ID int not null,
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
 DATE_CREATED DATE not null,
 DATE_LASTMODIFIED DATE not null,
 RESOURCE_SIZE int not null,
 RESOURCE_LASTMODIFIED_BY int not null,
 primary key(RESOURCE_ID),
 unique(RESOURCE_NAME));

CREATE TABLE CMS_ONLINE_RESOURCES
(RESOURCE_ID int not null,
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
 DATE_CREATED DATE not null,
 DATE_LASTMODIFIED DATE not null,
 RESOURCE_SIZE int not null,
 RESOURCE_LASTMODIFIED_BY int not null,
 primary key(RESOURCE_ID),
 unique(RESOURCE_NAME));

CREATE TABLE CMS_BACKUP_RESOURCES
(RESOURCE_ID int not null,
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
 DATE_CREATED DATE not null,
 DATE_LASTMODIFIED DATE not null,
 RESOURCE_SIZE int not null,
 RESOURCE_LASTMODIFIED_BY int not null,
 VERSION_ID int not null,
 primary key(RESOURCE_ID),
 unique(VERSION_ID,RESOURCE_NAME,DATE_LASTMODIFIED));

CREATE TABLE CMS_FILES
(FILE_ID int not null,
 FILE_CONTENT blob not null,
 primary key (FILE_ID));

CREATE TABLE CMS_ONLINE_FILES
(FILE_ID int not null,
 FILE_CONTENT blob not null,
 primary key (FILE_ID));

CREATE TABLE CMS_BACKUP_FILES
(FILE_ID int not null,
 FILE_CONTENT blob not null,
 primary key (FILE_ID));

CREATE TABLE CMS_GROUPS
(GROUP_ID int not null,
 PARENT_GROUP_ID int not null,
 GROUP_NAME VARCHAR(64) not null,
 GROUP_DESCRIPTION VARCHAR(255) not null,
 GROUP_FLAGS int not null,
 primary key(GROUP_ID),
 unique(GROUP_NAME));

CREATE TABLE CMS_SYSTEMID
(TABLE_KEY VARCHAR(255) not null,
 ID int not null,
 primary key (TABLE_KEY));

CREATE TABLE CMS_GROUPUSERS
(GROUP_ID int not null,
 USER_ID int not null,
 GROUPUSER_FLAGS int not null);

CREATE TABLE CMS_Task
(autofinish int,
 endtime date,
 escalationtyperef int,
 id int NOT NULL,
 initiatoruserref int,
 milestoneref int,
 name varchar(254),
 originaluserref int,
 agentuserref int,
 parent int,
 percentage varchar(50),
 permission varchar(50),
 priorityref int DEFAULT 2,
 roleref int,
 root int,
 starttime date,
 state int,
 tasktyperef int,
 timeout date,
 wakeuptime date,
 htmllink varchar(254),
 estimatetime int DEFAULT 86400,
 PRIMARY KEY (id));

CREATE TABLE CMS_TaskType
(autofinish int,
 escalationtyperef int,
 htmllink varchar(254),
 id int NOT NULL,
 name varchar(50),
 permission varchar(50),
 priorityref int,
 roleref int,
 PRIMARY KEY (id));

CREATE TABLE CMS_TaskLog
(coment blob,
 externalusername varchar(254),
 id int NOT NULL,
 starttime date,
 taskref int,
 userref int,
 type int DEFAULT 0,
 PRIMARY KEY (id));

CREATE TABLE CMS_TaskPar
(id int NOT NULL,
 parname varchar(50),
 parvalue varchar(50),
 ref int,
 PRIMARY KEY (id));

CREATE INDEX GROUP_PARENTID ON
  CMS_GROUPS(PARENT_GROUP_ID);

CREATE INDEX GROUPUSER_GROUPID ON
  CMS_GROUPUSERS(GROUP_ID);

CREATE INDEX GROUPUSER_USERID ON
  CMS_GROUPUSERS(USER_ID);

CREATE INDEX PROJECTS_GROUPID ON
  CMS_PROJECTS(GROUP_ID);

CREATE INDEX PROJECTS_MANAGERID ON
  CMS_PROJECTS(MANAGERGROUP_ID);

CREATE INDEX PROJECTS_USERID ON
  CMS_PROJECTS(USER_ID);

CREATE INDEX PROJECT_NAME ON
  CMS_PROJECTS(PROJECT_NAME);

CREATE INDEX PROJECT_TASKID ON
  CMS_PROJECTS(TASK_ID);

create index projects_flags on
cms_projects (project_flags);

create index resources_type on
cms_resources (resource_type);

create index resources_state on
cms_resources (state);

create index resources_project_type on
cms_resources (project_id, resource_type);

create index resources_resourceid_project on
cms_resources (resource_id, project_id);

CREATE INDEX RESOURCE_FILEID ON
  CMS_RESOURCES(FILE_ID);

CREATE INDEX RESOURCE_GROUP ON
  CMS_RESOURCES(GROUP_ID);

CREATE INDEX RESOURCE_LOCKED_BY ON
  CMS_RESOURCES(LOCKED_BY);

CREATE INDEX RESOURCE_PARENTID ON
  CMS_RESOURCES(PARENT_ID);

CREATE INDEX RESOURCE_PROJECTID ON
  CMS_RESOURCES(PROJECT_ID);

CREATE INDEX RESOURCE_USERID ON
  CMS_RESOURCES(USER_ID);

CREATE INDEX PARENT_RESOURCE_TYPE ON
  CMS_RESOURCES(PARENT_ID, RESOURCE_TYPE);

CREATE INDEX SYSTEMID ON
  CMS_SYSTEMID(TABLE_KEY, ID);

CREATE INDEX TASK_PARENT ON
  CMS_TASK(PARENT);

CREATE INDEX TASK_TYPETASKREF ON
  CMS_TASK(TASKTYPEREF);

CREATE INDEX TASKLOG_REF ON
  CMS_TASKLOG(TASKREF);

CREATE INDEX TASKLOG_USERREF ON
  CMS_TASKLOG(USERREF);

CREATE INDEX TASKPAR_REF ON
  CMS_TASKPAR(REF);
  
CREATE INDEX PROJECTRESOURCE_RESOURCE_NAME ON
  CMS_PROJECTRESOURCES(RESOURCE_NAME);