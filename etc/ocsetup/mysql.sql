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
                                         unique(GROUP_NAME));

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
                                         PROJECT_PUBLISHDATE datetime,
                                         PROJECT_PUBLISHED_BY int not null,
                                         PROJECT_TYPE int not null,
                                         primary key(PROJECT_ID), 
                                         key(PROJECT_NAME, PROJECT_CREATEDATE),
                                         unique(PROJECT_NAME, PROJECT_CREATEDATE));

create table CMS_PROPERTYDEF            (PROPERTYDEF_ID int not null, 
                                         PROPERTYDEF_NAME VARCHAR(64) not null,
                                         RESOURCE_TYPE int not null,
                                         PROPERTYDEF_TYPE int not null,
                                         primary key(PROPERTYDEF_ID), unique(PROPERTYDEF_NAME, RESOURCE_TYPE));

create table CMS_PROPERTIES             (PROPERTY_ID int not null,
                                         PROPERTYDEF_ID int not null,
                                         RESOURCE_ID int not null,
                                         PROPERTY_VALUE VARCHAR(255) not null,
                                         primary key(PROPERTY_ID), unique(PROPERTYDEF_ID, RESOURCE_ID));

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
                                         unique(RESOURCE_NAME,PROJECT_ID));

create table CMS_FILES                  (FILE_ID int not null,
                                         FILE_CONTENT mediumblob not null,
                                         primary key(FILE_ID));

create table CMS_SYSTEMID               (TABLE_KEY int not null,
                                         ID int not null,
                                         primary key(TABLE_KEY));
										
create table CMS_SESSIONS               (SESSION_ID varchar(255) not null,
                                         SESSION_LASTUSED datetime not null,
										 SESSION_DATA mediumblob not null,
                                         primary key(SESSION_ID));
										 
#
# Table structure for table 'GlobeTask'
#
										 
										 CREATE TABLE CMS_Task (
										   autofinish int(11),
										   endtime datetime,
										   escalationtyperef int(11),
										   id int(11) NOT NULL,
										   initiatoruserref int(11),
										   milestoneref int(11),
										   name varchar(254),
										   originaluserref int(11),
										   agentuserref int(11),
										   parent int(11),
										   percentage varchar(50),
										   permission varchar(50),
										   priorityref int(11) DEFAULT '2',
										   roleref int(11),
										   root int(11),
										   starttime datetime,
										   state int(11),
										   tasktyperef int(11),
										   timeout datetime,
										   wakeuptime datetime,
										   htmllink varchar(254),
										   estimatetime int(11) DEFAULT '86400',
										   PRIMARY KEY (id)
										 );
										 
#
# Table structure for table 'GlobeTaskType'
#
										 
										 CREATE TABLE CMS_TaskType (
										   autofinish int(11),
										   escalationtyperef int(11),
										   htmllink varchar(254),
										   id int(11) NOT NULL,
										   name varchar(50),
										   permission varchar(50),
										   priorityref int(11),
										   roleref int(11),
										   PRIMARY KEY (id)
										 );
										 
										 INSERT INTO CMS_TaskType VALUES (1,0,'../taskforms/adhoc.asp',1,'Ad-Hoc','30308',1,1);
										 
#
# Table structure for table 'GlobeTaskLog'
#
										 
										 CREATE TABLE CMS_TaskLog (
										   coment text,
										   externalusername varchar(254),
										   id int(11) NOT NULL,
										   starttime datetime,
										   taskref int(11),
										   userref int(11),
										   type int(18) DEFAULT '0',
										   PRIMARY KEY (id)
										 );
										 
#
# Table structure for table 'GlobeTaskPar'
#
										 
										 CREATE TABLE CMS_TaskPar (
										   id int(11) NOT NULL ,
										   parname varchar(50),
										   parvalue varchar(50),
										   ref int(11),
										   PRIMARY KEY (id)
);

