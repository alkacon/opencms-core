####################################################################################
# SQL script for adding multisite features to OpenCMS.
#
# The script should be run on top of a "clean" import of standard data. The script 
# contains test data for to additional sites (next to the standard online site.
#
# Author: Henrik Kjær Hansen
# Date  : 2000-09-15 
####################################################################################



####################################################################################
# Dumping data for table 'CMS_PROJECTS'
#
DELETE FROM CMS_PROJECTS where PROJECT_NAME = 'COM corporate';
DELETE FROM CMS_PROJECTS where PROJECT_NAME = 'DK corporate';
INSERT INTO CMS_PROJECTS (PROJECT_ID, USER_ID, GROUP_ID, MANAGERGROUP_ID, TASK_ID, PROJECT_NAME, PROJECT_DESCRIPTION, PROJECT_FLAGS, PROJECT_CREATEDATE, PROJECT_PUBLISHDATE, PROJECT_PUBLISHED_BY, PROJECT_TYPE) VALUES ( '4', '2', '1', '3', '1', 'COM corporate', 'International corporate site - online', '0', '2000-09-07 11:06:41', '2000-09-07 11:06:41', '0', '0');
INSERT INTO CMS_PROJECTS (PROJECT_ID, USER_ID, GROUP_ID, MANAGERGROUP_ID, TASK_ID, PROJECT_NAME, PROJECT_DESCRIPTION, PROJECT_FLAGS, PROJECT_CREATEDATE, PROJECT_PUBLISHDATE, PROJECT_PUBLISHED_BY, PROJECT_TYPE) VALUES ( '5', '2', '1', '3', '1', 'DK corporate', 'Danish corporate site - online', '0', '2000-09-15 10:44:56', '2000-09-15 10:44:56', '0', '0');



####################################################################################
# Table structure for table 'CMS_SITES'
#

DROP TABLE IF EXISTS CMS_SITES;
CREATE TABLE CMS_SITES (
   SITE_ID int(11) DEFAULT '0' NOT NULL,
   NAME text,
   DESCRIPTION text,
   CATEGORY_ID int(11),
   LANGUAGE_ID int(11),
   COUNTRY_ID int(11),
   ONLINEPROJECT_ID int(11),
   PRIMARY KEY (SITE_ID)
);

#
# Dumping data for table 'CMS_SITES'
#

INSERT INTO CMS_SITES (SITE_ID, NAME, DESCRIPTION, CATEGORY_ID, LANGUAGE_ID, COUNTRY_ID, ONLINEPROJECT_ID) VALUES ( '1', 'company.com', 'Corporate International', '1', '1', '0', '4');
INSERT INTO CMS_SITES (SITE_ID, NAME, DESCRIPTION, CATEGORY_ID, LANGUAGE_ID, COUNTRY_ID, ONLINEPROJECT_ID) VALUES ( '2', 'company.dk', 'Corporate Danish', '1', '2', '2', '5');
INSERT INTO CMS_SITES (SITE_ID, NAME, DESCRIPTION, CATEGORY_ID, LANGUAGE_ID, COUNTRY_ID, ONLINEPROJECT_ID) VALUES ( '0', 'Default', 'Default online project', '0', '1', '0', '1');



####################################################################################
#
# Table structure for table 'CMS_SITE_PROJECTS'
#

DROP TABLE IF EXISTS CMS_SITE_PROJECTS;
CREATE TABLE CMS_SITE_PROJECTS (
   SITE_ID int(11) DEFAULT '0' NOT NULL,
   PROJECT_ID int(11) DEFAULT '0' NOT NULL,
   PRIMARY KEY (SITE_ID, PROJECT_ID)
);

#
# Dumping data for table 'CMS_SITE_PROJECTS'
#

INSERT INTO CMS_SITE_PROJECTS (SITE_ID, PROJECT_ID) VALUES ( '0', '1');
INSERT INTO CMS_SITE_PROJECTS (SITE_ID, PROJECT_ID) VALUES ( '0', '2');
INSERT INTO CMS_SITE_PROJECTS (SITE_ID, PROJECT_ID) VALUES ( '1', '4');
INSERT INTO CMS_SITE_PROJECTS (SITE_ID, PROJECT_ID) VALUES ( '2', '5');



####################################################################################
# Table structure for table 'CMS_SITE_URLS'
#

DROP TABLE IF EXISTS CMS_SITE_URLS;
CREATE TABLE CMS_SITE_URLS (
   URL_ID int(11) DEFAULT '0' NOT NULL,
   URL varchar(255) NOT NULL,
   SITE_ID int(11),
   PRIMARYURL int(11),
   PRIMARY KEY (URL_ID),
   UNIQUE URL (URL)
);

#
# Dumping data for table 'CMS_SITE_URLS'
#

INSERT INTO CMS_SITE_URLS (URL_ID, URL, SITE_ID, PRIMARYURL) VALUES ( '0', 'www.default.com', '0', '0');
INSERT INTO CMS_SITE_URLS (URL_ID, URL, SITE_ID, PRIMARYURL) VALUES ( '1', 'www.company.com', '1', '1');
INSERT INTO CMS_SITE_URLS (URL_ID, URL, SITE_ID, PRIMARYURL) VALUES ( '2', 'www.company.dk', '2', '2');



####################################################################################
# Table structure for table 'CMS_CATEGORY'
#

DROP TABLE IF EXISTS CMS_CATEGORY;
CREATE TABLE CMS_CATEGORY (
   CATEGORY_ID int(11) DEFAULT '0' NOT NULL,
   NAME text,
   DESCRIPTION text NOT NULL,
   SHORTNAME varchar(10),
   PRIORITY int(11),
   PRIMARY KEY (CATEGORY_ID)
);

#
# Dumping data for table 'CMS_CATEGORY'
#

INSERT INTO CMS_CATEGORY (CATEGORY_ID, NAME, DESCRIPTION, SHORTNAME) VALUES ( '1', 'Corporate', 'Corporate site', 'COM');
INSERT INTO CMS_CATEGORY (CATEGORY_ID, NAME, DESCRIPTION) VALUES ( '0', 'Default', 'Default Category');



####################################################################################
# Table structure for table 'CMS_LANGUAGE'
#

DROP TABLE IF EXISTS CMS_LANGUAGE;
CREATE TABLE CMS_LANGUAGE (
   LANGUAGE_ID int(11) DEFAULT '0' NOT NULL,
   NAME text,
   SHORTNAME varchar(10),
   PRIORITY int(11),
   PRIMARY KEY (LANGUAGE_ID)
);

#
# Dumping data for table 'CMS_LANGUAGE'
#

INSERT INTO CMS_LANGUAGE (LANGUAGE_ID, NAME, SHORTNAME) VALUES ( '1', 'English (uk)', 'UK');
INSERT INTO CMS_LANGUAGE (LANGUAGE_ID, NAME, SHORTNAME) VALUES ( '2', 'Danish', 'DK');



####################################################################################
# Table structure for table 'CMS_COUNTRY'
#

DROP TABLE IF EXISTS CMS_COUNTRY;
CREATE TABLE CMS_COUNTRY (
   COUNTRY_ID int(11) DEFAULT '0' NOT NULL,
   NAME text,
   SHORTNAME varchar(10),
   PRIORITY int(11),
   PRIMARY KEY (COUNTRY_ID)
);

#
# Dumping data for table 'CMS_COUNTRY'
#

INSERT INTO CMS_COUNTRY (COUNTRY_ID, NAME) VALUES ( '0', 'International');
INSERT INTO CMS_COUNTRY (COUNTRY_ID, NAME, SHORTNAME) VALUES ( '2', 'Denmark', 'DK');

