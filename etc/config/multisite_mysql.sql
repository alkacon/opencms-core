####################################################################################
# SQL script for adding multisite features to OpenCMS.
#
# The script should be run on top of a "clean" import of standard data. 
# The multisitedatasetup.txt file should be runned from the cmsshell.
#
# Author: Henrik Kjær Hansen
# Date  : 2000-09-15 
#
####################################################################################



####################################################################################


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
   DELETED int(1) DEFAULT 0, 
   PRIMARY KEY (SITE_ID)
);


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


