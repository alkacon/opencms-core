CREATE TABLE cms_contents (
	publish_tag_from INTEGER NOT NULL, 
	resource_id VARCHAR(36) NOT NULL, 
	file_content LONG BYTE, 
	online_flag INTEGER, 
	publish_tag_to INTEGER, 
	PRIMARY KEY (publish_tag_from, resource_id)
);

CREATE TABLE cms_groups (
	group_id VARCHAR(36) NOT NULL, 
	group_description VARCHAR(255) NOT NULL, 
	group_flags INTEGER, 
	group_name VARCHAR(128) NOT NULL, 
	group_ou VARCHAR(128) NOT NULL, 
	parent_group_id VARCHAR(36) NOT NULL, 
	PRIMARY KEY (group_id), 
	CONSTRAINT U_CMS_RPS_GROUP_NAME UNIQUE (group_name, group_ou)
);

CREATE TABLE cms_groupusers (
	group_id VARCHAR(36) NOT NULL, 
	user_id VARCHAR(36) NOT NULL, 
	groupuser_flags INTEGER, 
	PRIMARY KEY (group_id, user_id)
);

CREATE TABLE cms_history_principals (
	principal_id VARCHAR(36) NOT NULL, 
	principal_datedeleted BIGINT, 
	principal_description VARCHAR(255) NOT NULL, 
	principal_email VARCHAR(128) NOT NULL, 
	principal_name VARCHAR(128) NOT NULL, 
	principal_ou VARCHAR(128), 
	principal_type VARCHAR(5) NOT NULL, 
	principal_userdeleted VARCHAR(36) NOT NULL, 
	PRIMARY KEY (principal_id)
);

CREATE TABLE cms_history_projectresources (
	project_id VARCHAR(36) NOT NULL, 
	publish_tag INTEGER NOT NULL, 
	resource_path VARCHAR(1024) NOT NULL, 
	PRIMARY KEY (project_id, publish_tag, resource_path)
);

CREATE TABLE cms_history_projects (
	publish_tag INTEGER NOT NULL, 
	date_created BIGINT, 
	group_id VARCHAR(36) NOT NULL, 
	managergroup_id VARCHAR(36) NOT NULL, 
	project_description VARCHAR(255) NOT NULL, 
	project_id VARCHAR(36) NOT NULL, 
	project_name VARCHAR(255) NOT NULL, 
	project_ou VARCHAR(128) NOT NULL, 
	project_publishdate BIGINT, 
	project_published_by VARCHAR(36) NOT NULL, 
	project_type INTEGER, user_id VARCHAR(36) NOT NULL, 
	PRIMARY KEY (publish_tag)
);

CREATE TABLE cms_history_properties (
	propertydef_id VARCHAR(36) NOT NULL, 
	property_mapping_type INTEGER NOT NULL, 
	publish_tag INTEGER NOT NULL, 
	structure_id VARCHAR(36) NOT NULL, 
	property_mapping_id VARCHAR(36) NOT NULL, 
	property_value VARCHAR(2048) NOT NULL, 
	PRIMARY KEY (propertydef_id, property_mapping_type, publish_tag, structure_id)
);

CREATE TABLE cms_history_propertydef (
	propertydef_id VARCHAR(36) NOT NULL, 
	propertydef_name VARCHAR(128) NOT NULL, 
	propertydef_type INTEGER, 
	PRIMARY KEY (propertydef_id)
);

CREATE TABLE cms_history_resources (
	publish_tag INTEGER NOT NULL, 
	resource_id VARCHAR(36) NOT NULL, 
	date_content BIGINT, 
	date_created BIGINT, 
	date_lastmodified BIGINT, 
	project_lastmodified VARCHAR(36) NOT NULL, 
	resource_flags INTEGER, 
	resource_size INTEGER, 
	resource_state INTEGER, 
	resource_type INTEGER, 
	resource_version INTEGER, 
	sibling_count INTEGER, 
	user_created VARCHAR(36) NOT NULL, 
	user_lastmodified VARCHAR(36) NOT NULL, 
	PRIMARY KEY (publish_tag, resource_id)
);

CREATE TABLE cms_history_structure (
	publish_tag INTEGER NOT NULL, 
	structure_id VARCHAR(36) NOT NULL, 
	version INTEGER NOT NULL, 
	date_expired BIGINT, 
	date_released BIGINT, 
	parent_id VARCHAR(36) NOT NULL, 
	resource_id VARCHAR(36) NOT NULL, 
	resource_path VARCHAR(1024), 
	structure_state INTEGER, 
	structure_version INTEGER, 
	PRIMARY KEY (publish_tag, structure_id, version)
);

CREATE TABLE cms_log (
	log_date BIGINT NOT NULL, 
	log_type INTEGER NOT NULL, 
	user_id VARCHAR(36) NOT NULL, 
	log_data VARCHAR(1024), 
	structure_id VARCHAR(36), 
	PRIMARY KEY (log_date, log_type, user_id)
);

CREATE TABLE cms_offline_accesscontrol (
	principal_id VARCHAR(36) NOT NULL, 
	resource_id VARCHAR(36) NOT NULL, 
	access_allowed INTEGER, 
	access_denied INTEGER, 
	access_flags INTEGER, 
	PRIMARY KEY (principal_id, resource_id)
);

CREATE TABLE cms_offline_contents (
	resource_id VARCHAR(36) NOT NULL, 
	file_content LONG BYTE, 
	PRIMARY KEY (resource_id)
);

CREATE TABLE cms_offline_properties (
	property_id VARCHAR(36) NOT NULL, 
	propertydef_id VARCHAR(36) NOT NULL, 
	property_mapping_id VARCHAR(36) NOT NULL, 
	property_mapping_type INTEGER, 
	property_value VARCHAR(2048) NOT NULL, 
	PRIMARY KEY (property_id), 
	CONSTRAINT U_CMS_RTS_PROPERTYDEF_ID1 UNIQUE (propertydef_id, property_mapping_id)
);

CREATE TABLE cms_offline_propertydef (
	propertydef_id VARCHAR(36) NOT NULL, 
	propertydef_name VARCHAR(128) NOT NULL, 
	propertydef_type INTEGER, 
	PRIMARY KEY (propertydef_id), 
	CONSTRAINT U_CMS_YDF_PROPERTYDEF_NAME1 UNIQUE (propertydef_name)
);

CREATE TABLE cms_offline_resources (
	resource_id VARCHAR(36) NOT NULL, 
	date_content BIGINT, 
	date_created BIGINT, 
	date_lastmodified BIGINT, 
	project_lastmodified VARCHAR(36) NOT NULL, 
	resource_flags INTEGER, 
	resource_size INTEGER, 
	resource_state INTEGER, 
	resource_type INTEGER, 
	resource_version INTEGER, 
	sibling_count INTEGER, 
	user_created VARCHAR(36) NOT NULL, 
	user_lastmodified VARCHAR(36) NOT NULL, 
	PRIMARY KEY (resource_id)
);

CREATE TABLE cms_offline_resource_relations (
	relation_source_id VARCHAR(36) NOT NULL, 
	relation_source_path VARCHAR(1024) NOT NULL, 
	relation_target_id VARCHAR(36) NOT NULL, 
	relation_target_path VARCHAR(1024) NOT NULL, 
	relation_type INTEGER NOT NULL
);

CREATE TABLE cms_offline_structure (
	structure_id VARCHAR(36) NOT NULL, 
	date_expired BIGINT, 
	date_released BIGINT, 
	parent_id VARCHAR(36) NOT NULL, 
	resource_id VARCHAR(36) NOT NULL, 
	resource_path VARCHAR(1024), 
	structure_state INTEGER, 
	structure_version INTEGER, 
	PRIMARY KEY (structure_id)
);

CREATE TABLE cms_online_accesscontrol (
	principal_id VARCHAR(36) NOT NULL, 
	resource_id VARCHAR(36) NOT NULL, 
	access_allowed INTEGER, 
	access_denied INTEGER, 
	access_flags INTEGER, 
	PRIMARY KEY (principal_id, resource_id)
);

CREATE TABLE cms_online_properties (
	property_id VARCHAR(36) NOT NULL, 
	propertydef_id VARCHAR(36) NOT NULL, 
	property_mapping_id VARCHAR(36) NOT NULL, 
	property_mapping_type INTEGER, 
	property_value VARCHAR(2048) NOT NULL, 
	PRIMARY KEY (property_id), 
	CONSTRAINT U_CMS_RTS_PROPERTYDEF_ID UNIQUE (propertydef_id, property_mapping_id)
);

CREATE TABLE cms_online_propertydef (
	propertydef_id VARCHAR(36) NOT NULL, 
	propertydef_name VARCHAR(128) NOT NULL, 
	propertydef_type INTEGER, 
	PRIMARY KEY (propertydef_id), 
	CONSTRAINT U_CMS_YDF_PROPERTYDEF_NAME UNIQUE (propertydef_name)
);

CREATE TABLE cms_online_resources (
	resource_id VARCHAR(36) NOT NULL, 
	date_content BIGINT, 
	date_created BIGINT, 
	date_lastmodified BIGINT, 
	project_lastmodified VARCHAR(36) NOT NULL, 
	resource_flags INTEGER, 
	resource_size INTEGER, 
	resource_state INTEGER, 
	resource_type INTEGER, 
	resource_version INTEGER, 
	sibling_count INTEGER, 
	user_created VARCHAR(36) NOT NULL, 
	user_lastmodified VARCHAR(36) NOT NULL, 
	PRIMARY KEY (resource_id)
);

CREATE TABLE cms_online_resource_relations (
	relation_source_id VARCHAR(36) NOT NULL, 
	relation_source_path VARCHAR(1024) NOT NULL, 
	relation_target_id VARCHAR(36) NOT NULL, 
	relation_target_path VARCHAR(1024) NOT NULL, 
	relation_type INTEGER NOT NULL
);

CREATE TABLE cms_online_structure (
	structure_id VARCHAR(36) NOT NULL, 
	date_expired BIGINT, 
	date_released BIGINT, 
	parent_id VARCHAR(36) NOT NULL, 
	resource_id VARCHAR(36) NOT NULL, 
	resource_path VARCHAR(1024), 
	structure_state INTEGER, 
	structure_version INTEGER, 
	PRIMARY KEY (structure_id)
);

CREATE TABLE cms_projectresources (
	project_id VARCHAR(36) NOT NULL, 
	resource_path VARCHAR(1024) NOT NULL, 
	PRIMARY KEY (project_id, resource_path)
);

CREATE TABLE cms_projects (
	project_id VARCHAR(36) NOT NULL, 
	date_created BIGINT NOT NULL, 
	group_id VARCHAR(36) NOT NULL, 
	managergroup_id VARCHAR(36) NOT NULL, 
	project_description VARCHAR(255) NOT NULL, 
	project_flags INTEGER, 
	project_name VARCHAR(200) NOT NULL, 
	project_ou VARCHAR(128) NOT NULL, 
	project_type INTEGER, 
	user_id VARCHAR(36) NOT NULL, 
	PRIMARY KEY (project_id), 
	CONSTRAINT U_CMS_CTS_PROJECT_OU UNIQUE (project_ou, project_name, date_created)
);

CREATE TABLE cms_publish_history (
	history_id VARCHAR(36) NOT NULL, 
	publish_tag INTEGER NOT NULL, 
	resource_path VARCHAR(1024) NOT NULL, 
	structure_id VARCHAR(36) NOT NULL, 
	resource_id VARCHAR(36) NOT NULL, 
	resource_state INTEGER, 
	resource_type INTEGER, 
	sibling_count INTEGER, 
	PRIMARY KEY (history_id, publish_tag, resource_path, structure_id)
);

CREATE TABLE cms_publish_jobs (
	history_id VARCHAR(36) NOT NULL, 
	enqueue_time BIGINT, 
	finish_time BIGINT, 
	project_id VARCHAR(36) NOT NULL, 
	project_name VARCHAR(255) NOT NULL, 
	publish_flags INTEGER, 
	publish_list LONG BYTE, 
	publish_locale VARCHAR(16) NOT NULL, 
	publish_report LONG BYTE, 
	resource_count INTEGER, 
	start_time BIGINT, 
	user_id VARCHAR(36) NOT NULL, 
	PRIMARY KEY (history_id)
);

CREATE TABLE cms_resource_locks (
	resource_path VARCHAR(1024) NOT NULL, 
	lock_type INTEGER, 
	project_id VARCHAR(36) NOT NULL, 
	user_id VARCHAR(36) NOT NULL, 
	PRIMARY KEY (resource_path)
);

CREATE TABLE cms_staticexport_links (
	link_id VARCHAR(36) NOT NULL, 
	link_parameter VARCHAR(1024), 
	link_rfs_path VARCHAR(1024), 
	link_timestamp BIGINT, 
	link_type INTEGER, 
	PRIMARY KEY (link_id)
);

CREATE TABLE cms_subscription (
	principal_id VARCHAR(36) NOT NULL, 
	structure_id VARCHAR(36) NOT NULL, 
	date_deleted BIGINT, 
	PRIMARY KEY (principal_id, structure_id)
);

CREATE TABLE cms_subscription_visit (
	user_id VARCHAR(36) NOT NULL, 
	visit_date BIGINT NOT NULL, 
	structure_id VARCHAR(36), 
	PRIMARY KEY (user_id, visit_date)
);

CREATE TABLE cms_userdata (
	data_key VARCHAR(255) NOT NULL, 
	user_id VARCHAR(36) NOT NULL, 
	data_type VARCHAR(128) NOT NULL, 
	data_value LONG BYTE, 
	PRIMARY KEY (data_key, user_id)
);

CREATE TABLE cms_users (
	user_id VARCHAR(36) NOT NULL, 
	user_datecreated BIGINT, 
	user_email VARCHAR(128) NOT NULL, 
	user_firstname VARCHAR(128) NOT NULL, 
	user_flags INTEGER, 
	user_lastlogin BIGINT, 
	user_lastname VARCHAR(128) NOT NULL, 
	user_name VARCHAR(128) NOT NULL, 
	user_ou VARCHAR(128) NOT NULL, 
	user_password VARCHAR(64) NOT NULL, 
	PRIMARY KEY (user_id), 
	CONSTRAINT U_CMS_SRS_USER_NAME UNIQUE (user_name, user_ou)
);

CREATE TABLE cms_offline_urlname_mappings
(
	name VARCHAR(128) NOT NULL,
	structure_id VARCHAR(36) NOT NULL,
	state INTEGER NOT NULL,
	date_changed BIGINT NOT NULL,
	locale VARCHAR(10)
);


CREATE TABLE cms_online_urlname_mappings
(
	name VARCHAR(128) NOT NULL,
	structure_id VARCHAR(36) NOT NULL,
	state INTEGER NOT NULL,
	date_changed BIGINT NOT NULL,
	locale VARCHAR(10)
);






