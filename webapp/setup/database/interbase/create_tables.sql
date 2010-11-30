CREATE TABLE cms_contents
(
    resource_id VARCHAR(36) NOT NULL,
    file_content BLOB,
    publish_tag_from INTEGER NOT NULL,
    publish_tag_to INTEGER,
    online_flag INTEGER,
    PRIMARY KEY (resource_id, publish_tag_from)
);

CREATE UNIQUE INDEX pk_contents ON cms_contents (resource_id, publish_tag_from);

CREATE INDEX cms_contents_01_idx ON cms_contents (resource_id, publish_tag_to);

CREATE INDEX cms_contents_02_idx ON cms_contents (resource_id);

CREATE INDEX cms_contents_03_idx ON cms_contents (publish_tag_from);

CREATE INDEX cms_contents_04_idx ON cms_contents (publish_tag_to);

CREATE INDEX cms_contents_05_idx ON cms_contents (resource_id, online_flag);

/* ----------------------------------------------------------------------- */
/* cms_groups */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_groups
(
    group_id VARCHAR(36) NOT NULL,
    parent_group_id VARCHAR(36) NOT NULL,
    group_name VARCHAR(128) NOT NULL,
    group_description VARCHAR(255) NOT NULL,
    group_flags INTEGER NOT NULL,
    group_ou VARCHAR(128) NOT NULL,
    PRIMARY KEY (group_id)
);

CREATE UNIQUE INDEX pk_groups ON cms_groups (group_id);

CREATE UNIQUE INDEX uk_groups ON cms_groups (group_name, group_ou);

CREATE INDEX cms_groups_01_idx ON cms_groups (parent_group_id);

CREATE INDEX cms_groups_02_idx ON cms_groups (group_name);

CREATE INDEX cms_groups_03_idx ON cms_groups (group_ou);

/* ----------------------------------------------------------------------- */
/* cms_groupusers */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_groupusers
(
    group_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    groupuser_flags INTEGER NOT NULL,
    PRIMARY KEY (group_id, user_id)
);

CREATE UNIQUE INDEX pk_groupusers ON cms_groupusers (group_id, user_id);

CREATE INDEX cms_groupusers_01_idx ON cms_groupusers (group_id);

CREATE INDEX cms_groupusers_02_idx ON cms_groupusers (user_id);

/* ----------------------------------------------------------------------- */
/* cms_history_principals */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_history_principals
(
    principal_id VARCHAR(36) NOT NULL,
    principal_name VARCHAR(128) NOT NULL,
    principal_description VARCHAR(255) NOT NULL,
    principal_ou VARCHAR(128),
    principal_email VARCHAR(128) NOT NULL,
    principal_type VARCHAR(5) NOT NULL,
    principal_userdeleted VARCHAR(36) NOT NULL,
    principal_datedeleted NUMERIC(18,0) NOT NULL,
    PRIMARY KEY (principal_id)
);

CREATE UNIQUE INDEX pk_hist_principals ON cms_history_principals (principal_id);

/* ----------------------------------------------------------------------- */
/* cms_history_projectresources */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_history_projectresources
(
    publish_tag INTEGER NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    resource_path VARCHAR(1024) NOT NULL,
    PRIMARY KEY (publish_tag, project_id, resource_path)
);

CREATE UNIQUE INDEX pk_history_projectresources ON cms_history_projectresources (publish_tag, project_id, resource_path);

/* ----------------------------------------------------------------------- */
/* cms_history_projects */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_history_projects
(
    project_id VARCHAR(36) NOT NULL,
    project_name VARCHAR(255) NOT NULL,
    project_description VARCHAR(255) NOT NULL,
    project_type INTEGER NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    group_id VARCHAR(36) NOT NULL,
    managergroup_id VARCHAR(36) NOT NULL,
    date_created NUMERIC(18,0) NOT NULL,
    publish_tag INTEGER NOT NULL,
    project_publishdate NUMERIC(18,0) NOT NULL,
    project_published_by VARCHAR(36) NOT NULL,
    project_ou VARCHAR(128) NOT NULL,
    PRIMARY KEY (publish_tag)
);

CREATE UNIQUE INDEX pk_history_projects ON cms_history_projects (publish_tag);

/* ----------------------------------------------------------------------- */
/* cms_history_properties */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_history_properties
(
    structure_id VARCHAR(36) NOT NULL,
    propertydef_id VARCHAR(36) NOT NULL,
    property_mapping_id VARCHAR(36) NOT NULL,
    property_mapping_type INTEGER NOT NULL,
    property_value VARCHAR(2048) NOT NULL,
    publish_tag INTEGER NOT NULL,
    PRIMARY KEY (structure_id, propertydef_id, property_mapping_type, publish_tag)
);

CREATE UNIQUE INDEX pk_history_properties ON cms_history_properties (structure_id, propertydef_id, property_mapping_type, publish_tag);

CREATE INDEX cms_history_properties_01_idx ON cms_history_properties (propertydef_id);

CREATE INDEX cms_history_properties_02_idx ON cms_history_properties (property_mapping_id);

CREATE INDEX cms_history_properties_03_idx ON cms_history_properties (publish_tag);

CREATE INDEX cms_history_properties_04_idx ON cms_history_properties (propertydef_id, property_mapping_id);

CREATE INDEX cms_history_properties_05_idx ON cms_history_properties (structure_id, publish_tag);

/* ----------------------------------------------------------------------- */
/* cms_history_propertydef */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_history_propertydef
(
    propertydef_id VARCHAR(36) NOT NULL,
    propertydef_name VARCHAR(128) NOT NULL,
    propertydef_type INTEGER NOT NULL,
    PRIMARY KEY (propertydef_id)
);

CREATE UNIQUE INDEX pk_history_propertydef ON cms_history_propertydef (propertydef_id);

/* ----------------------------------------------------------------------- */
/* cms_history_resources */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_history_resources
(
    resource_id VARCHAR(36) NOT NULL,
    resource_type INTEGER NOT NULL,
    resource_flags INTEGER NOT NULL,
    resource_state INTEGER NOT NULL,
    resource_size INTEGER NOT NULL,
    date_content NUMERIC(18,0) NOT NULL,
    sibling_count INTEGER NOT NULL,
    date_created NUMERIC(18,0) NOT NULL,
    date_lastmodified NUMERIC(18,0) NOT NULL,
    user_created VARCHAR(36) NOT NULL,
    user_lastmodified VARCHAR(36) NOT NULL,
    project_lastmodified VARCHAR(36) NOT NULL,
    publish_tag INTEGER NOT NULL,
    resource_version INTEGER NOT NULL,
    PRIMARY KEY (resource_id, publish_tag)
);

CREATE UNIQUE INDEX pk_history_resources ON cms_history_resources (resource_id, publish_tag);

CREATE INDEX cms_history_resources_01_idx ON cms_history_resources (project_lastmodified);

CREATE INDEX cms_history_resources_02_idx ON cms_history_resources (project_lastmodified, resource_size);

CREATE INDEX cms_history_resources_03_idx ON cms_history_resources (resource_size);

CREATE INDEX cms_history_resources_04_idx ON cms_history_resources (date_lastmodified);

CREATE INDEX cms_history_resources_05_idx ON cms_history_resources (resource_type);

CREATE INDEX cms_history_resources_06_idx ON cms_history_resources (publish_tag);

/* ----------------------------------------------------------------------- */
/* cms_history_structure */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_history_structure
(
    publish_tag INTEGER NOT NULL,
    version INTEGER NOT NULL,
    structure_id VARCHAR(36) NOT NULL,
    resource_id VARCHAR(36) NOT NULL,
    parent_id VARCHAR(36) NOT NULL,
    resource_path VARCHAR(1024),
    structure_state INTEGER NOT NULL,
    date_released NUMERIC(18,0) NOT NULL,
    date_expired NUMERIC(18,0) NOT NULL,
    structure_version INTEGER NOT NULL,
    PRIMARY KEY (publish_tag, version, structure_id)
);

CREATE UNIQUE INDEX pk_history_structure ON cms_history_structure (structure_id, publish_tag, version);

CREATE INDEX cms_history_structure_01_idx ON cms_history_structure (structure_id, resource_path);

CREATE INDEX cms_history_structure_02_idx ON cms_history_structure (resource_path, resource_id);

CREATE INDEX cms_history_structure_03_idx ON cms_history_structure (structure_id, resource_id);

CREATE INDEX cms_history_structure_04_idx ON cms_history_structure (structure_state);

CREATE INDEX cms_history_structure_05_idx ON cms_history_structure (resource_id);

CREATE INDEX cms_history_structure_06_idx ON cms_history_structure (resource_path);

CREATE INDEX cms_history_structure_07_idx ON cms_history_structure (publish_tag);

CREATE INDEX cms_history_structure_08_idx ON cms_history_structure (version);

/* ----------------------------------------------------------------------- */
/* cms_log */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_log
(
    user_id VARCHAR(36) NOT NULL,
    log_date NUMERIC(18,0) NOT NULL,
    structure_id VARCHAR(36),
    log_type INTEGER NOT NULL,
    log_data VARCHAR(1024),
    PRIMARY KEY (user_id, log_date, log_type)
);

CREATE UNIQUE INDEX pk_log ON cms_log (user_id, log_date, log_type);

CREATE INDEX cms_log_01_idx ON cms_log (user_id);

CREATE INDEX cms_log_02_idx ON cms_log (log_date);

CREATE INDEX cms_log_03_idx ON cms_log (structure_id);

CREATE INDEX cms_log_04_idx ON cms_log (log_type);

CREATE INDEX cms_log_05_idx ON cms_log (user_id, structure_id);

CREATE INDEX cms_log_06_idx ON cms_log (user_id, log_date);

CREATE INDEX cms_log_07_idx ON cms_log (user_id, structure_id, log_date);

CREATE INDEX cms_log_08_idx ON cms_log (user_id, log_type, structure_id, log_date);

/* ----------------------------------------------------------------------- */
/* cms_offline_accesscontrol */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_offline_accesscontrol
(
    resource_id VARCHAR(36) NOT NULL,
    principal_id VARCHAR(36) NOT NULL,
    access_allowed INTEGER,
    access_denied INTEGER,
    access_flags INTEGER,
    PRIMARY KEY (resource_id, principal_id)
);

CREATE UNIQUE INDEX pk_offline_accesscontrol ON cms_offline_accesscontrol (resource_id, principal_id);

CREATE INDEX offline_accesscontrol_01_idx ON cms_offline_accesscontrol (principal_id);

/* ----------------------------------------------------------------------- */
/* cms_offline_contents */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_offline_contents
(
    resource_id VARCHAR(36) NOT NULL,
    file_content BLOB,
    PRIMARY KEY (resource_id)
);

CREATE UNIQUE INDEX pk_offline_contents ON cms_offline_contents (resource_id);

/* ----------------------------------------------------------------------- */
/* cms_offline_properties */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_offline_properties
(
    property_id VARCHAR(36) NOT NULL,
    propertydef_id VARCHAR(36) NOT NULL,
    property_mapping_id VARCHAR(36) NOT NULL,
    property_mapping_type INTEGER NOT NULL,
    property_value VARCHAR(2048) NOT NULL,
    PRIMARY KEY (property_id)
);

CREATE UNIQUE INDEX pk_offline_properties ON cms_offline_properties (property_id);

CREATE UNIQUE INDEX uk_offline_properties ON cms_offline_properties (propertydef_id, property_mapping_id);

CREATE INDEX cms_offline_properties_01_idx ON cms_offline_properties (propertydef_id);

CREATE INDEX cms_offline_properties_02_idx ON cms_offline_properties (property_mapping_id);

/* ----------------------------------------------------------------------- */
/* cms_offline_propertydef */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_offline_propertydef
(
    propertydef_id VARCHAR(36) NOT NULL,
    propertydef_name VARCHAR(128) NOT NULL,
    propertydef_type INTEGER NOT NULL,
    PRIMARY KEY (propertydef_id)
);

CREATE UNIQUE INDEX pk_offline_propertydef ON cms_offline_propertydef (propertydef_id);

CREATE UNIQUE INDEX uk_offline_propertydef ON cms_offline_propertydef (propertydef_name);

/* ----------------------------------------------------------------------- */
/* cms_offline_resource_relations */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_offline_resource_relations
(
    relation_source_id VARCHAR(36) NOT NULL,
    relation_source_path VARCHAR(1024) NOT NULL,
    relation_target_id VARCHAR(36) NOT NULL,
    relation_target_path VARCHAR(1024) NOT NULL,
    relation_type INTEGER NOT NULL
);

CREATE INDEX cms_offline_relations_01_idx ON cms_offline_resource_relations (relation_source_id);

CREATE INDEX cms_offline_relations_02_idx ON cms_offline_resource_relations (relation_target_id);

CREATE INDEX cms_offline_relations_03_idx ON cms_offline_resource_relations (relation_source_path);

CREATE INDEX cms_offline_relations_04_idx ON cms_offline_resource_relations (relation_target_path);

CREATE INDEX cms_offline_relations_05_idx ON cms_offline_resource_relations (relation_type);

/* ----------------------------------------------------------------------- */
/* cms_offline_resources */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_offline_resources
(
    resource_id VARCHAR(36) NOT NULL,
    resource_type INTEGER NOT NULL,
    resource_flags INTEGER NOT NULL,
    resource_state INTEGER NOT NULL,
    resource_size INTEGER NOT NULL,
    date_content NUMERIC(18,0) NOT NULL,
    sibling_count INTEGER NOT NULL,
    date_created NUMERIC(18,0) NOT NULL,
    date_lastmodified NUMERIC(18,0) NOT NULL,
    user_created VARCHAR(36) NOT NULL,
    user_lastmodified VARCHAR(36) NOT NULL,
    project_lastmodified VARCHAR(36) NOT NULL,
    resource_version INTEGER NOT NULL,
    PRIMARY KEY (resource_id)
);

CREATE UNIQUE INDEX pk_offline_resources ON cms_offline_resources (resource_id);

CREATE INDEX cms_offline_resources_01_idx ON cms_offline_resources (project_lastmodified);

CREATE INDEX cms_offline_resources_02_idx ON cms_offline_resources (project_lastmodified, resource_size);

CREATE INDEX cms_offline_resources_03_idx ON cms_offline_resources (resource_size);

CREATE INDEX cms_offline_resources_04_idx ON cms_offline_resources (date_lastmodified);

CREATE INDEX cms_offline_resources_05_idx ON cms_offline_resources (resource_type);

/* ----------------------------------------------------------------------- */
/* cms_offline_structure */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_offline_structure
(
    structure_id VARCHAR(36) NOT NULL,
    resource_id VARCHAR(36) NOT NULL,
    parent_id VARCHAR(36) NOT NULL,
    resource_path VARCHAR(1024),
    structure_state INTEGER NOT NULL,
    date_released NUMERIC(18,0) NOT NULL,
    date_expired NUMERIC(18,0) NOT NULL,
    structure_version INTEGER NOT NULL,
    PRIMARY KEY (structure_id)
);

CREATE UNIQUE INDEX pk_offline_structure ON cms_offline_structure (structure_id);

CREATE INDEX cms_offline_structure_01_idx ON cms_offline_structure (structure_id, resource_path);

CREATE INDEX cms_offline_structure_02_idx ON cms_offline_structure (resource_path, resource_id);

CREATE INDEX cms_offline_structure_03_idx ON cms_offline_structure (structure_id, resource_id);

CREATE INDEX cms_offline_structure_04_idx ON cms_offline_structure (structure_state);

CREATE INDEX cms_offline_structure_05_idx ON cms_offline_structure (parent_id);

CREATE INDEX cms_offline_structure_06_idx ON cms_offline_structure (resource_id);

CREATE INDEX cms_offline_structure_07_idx ON cms_offline_structure (resource_path);

/* ----------------------------------------------------------------------- */
/* cms_online_accesscontrol */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_online_accesscontrol
(
    resource_id VARCHAR(36) NOT NULL,
    principal_id VARCHAR(36) NOT NULL,
    access_allowed INTEGER,
    access_denied INTEGER,
    access_flags INTEGER,
    PRIMARY KEY (resource_id, principal_id)
);

CREATE UNIQUE INDEX pk_online_accesscontrol ON cms_online_accesscontrol (resource_id, principal_id);

CREATE INDEX online_accesscontrol_01_idx ON cms_online_accesscontrol (principal_id);

/* ----------------------------------------------------------------------- */
/* cms_online_properties */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_online_properties
(
    property_id VARCHAR(36) NOT NULL,
    propertydef_id VARCHAR(36) NOT NULL,
    property_mapping_id VARCHAR(36) NOT NULL,
    property_mapping_type INTEGER NOT NULL,
    property_value VARCHAR(2048) NOT NULL,
    PRIMARY KEY (property_id)
);

CREATE UNIQUE INDEX pk_online_properties ON cms_online_properties (property_id);

CREATE UNIQUE INDEX uk_online_properties ON cms_online_properties (propertydef_id, property_mapping_id);

CREATE INDEX cms_online_properties_01_idx ON cms_online_properties (propertydef_id);

CREATE INDEX cms_online_properties_02_idx ON cms_online_properties (property_mapping_id);

/* ----------------------------------------------------------------------- */
/* cms_online_propertydef */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_online_propertydef
(
    propertydef_id VARCHAR(36) NOT NULL,
    propertydef_name VARCHAR(128) NOT NULL,
    propertydef_type INTEGER NOT NULL,
    PRIMARY KEY (propertydef_id)
);

CREATE UNIQUE INDEX pk_online_propertydef ON cms_online_propertydef (propertydef_id);

CREATE UNIQUE INDEX uk_online_propertydef ON cms_online_propertydef (propertydef_name);

/* ----------------------------------------------------------------------- */
/* cms_online_resource_relations */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_online_resource_relations
(
    relation_source_id VARCHAR(36) NOT NULL,
    relation_source_path VARCHAR(1024) NOT NULL,
    relation_target_id VARCHAR(36) NOT NULL,
    relation_target_path VARCHAR(1024) NOT NULL,
    relation_type INTEGER NOT NULL
);

CREATE INDEX cms_online_relations_01_idx ON cms_online_resource_relations (relation_source_id);

CREATE INDEX cms_online_relations_02_idx ON cms_online_resource_relations (relation_target_id);

CREATE INDEX cms_online_relations_03_idx ON cms_online_resource_relations (relation_source_path);

CREATE INDEX cms_online_relations_04_idx ON cms_online_resource_relations (relation_target_path);

CREATE INDEX cms_online_relations_05_idx ON cms_online_resource_relations (relation_type);

/* ----------------------------------------------------------------------- */
/* cms_online_resources */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_online_resources
(
    resource_id VARCHAR(36) NOT NULL,
    resource_type INTEGER NOT NULL,
    resource_flags INTEGER NOT NULL,
    resource_state INTEGER NOT NULL,
    resource_size INTEGER NOT NULL,
    date_content NUMERIC(18,0) NOT NULL,
    sibling_count INTEGER NOT NULL,
    date_created NUMERIC(18,0) NOT NULL,
    date_lastmodified NUMERIC(18,0) NOT NULL,
    user_created VARCHAR(36) NOT NULL,
    user_lastmodified VARCHAR(36) NOT NULL,
    project_lastmodified VARCHAR(36) NOT NULL,
    resource_version INTEGER NOT NULL,
    PRIMARY KEY (resource_id)
);

CREATE UNIQUE INDEX pk_online_resources ON cms_online_resources (resource_id);

CREATE INDEX cms_online_resources_01_idx ON cms_online_resources (project_lastmodified);

CREATE INDEX cms_online_resources_02_idx ON cms_online_resources (project_lastmodified, resource_size);

CREATE INDEX cms_online_resources_03_idx ON cms_online_resources (resource_size);

CREATE INDEX cms_online_resources_04_idx ON cms_online_resources (date_lastmodified);

CREATE INDEX cms_online_resources_05_idx ON cms_online_resources (resource_type);

/* ----------------------------------------------------------------------- */
/* cms_online_structure */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_online_structure
(
    structure_id VARCHAR(36) NOT NULL,
    resource_id VARCHAR(36) NOT NULL,
    parent_id VARCHAR(36) NOT NULL,
    resource_path VARCHAR(1024),
    structure_state INTEGER NOT NULL,
    date_released NUMERIC(18,0) NOT NULL,
    date_expired NUMERIC(18,0) NOT NULL,
    structure_version INTEGER NOT NULL,
    PRIMARY KEY (structure_id)
);

CREATE UNIQUE INDEX pk_online_structure ON cms_online_structure (structure_id);

CREATE INDEX cms_online_structure_01_idx ON cms_online_structure (structure_id, resource_path);

CREATE INDEX cms_online_structure_02_idx ON cms_online_structure (resource_path, resource_id);

CREATE INDEX cms_online_structure_03_idx ON cms_online_structure (structure_id, resource_id);

CREATE INDEX cms_online_structure_04_idx ON cms_online_structure (structure_state);

CREATE INDEX cms_online_structure_05_idx ON cms_online_structure (parent_id);

CREATE INDEX cms_online_structure_06_idx ON cms_online_structure (resource_id);

CREATE INDEX cms_online_structure_07_idx ON cms_online_structure (resource_path);

/* ----------------------------------------------------------------------- */
/* cms_projectresources */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_projectresources
(
    project_id VARCHAR(36) NOT NULL,
    resource_path VARCHAR(1024) NOT NULL,
    PRIMARY KEY (project_id, resource_path)
);

CREATE UNIQUE INDEX pk_projectresources ON cms_projectresources (project_id, resource_path);

CREATE INDEX cms_projectresources_01_idx ON cms_projectresources (resource_path);

/* ----------------------------------------------------------------------- */
/* cms_projects */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_projects
(
    project_id VARCHAR(36) NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    project_description VARCHAR(255) NOT NULL,
    project_flags INTEGER NOT NULL,
    project_type INTEGER NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    group_id VARCHAR(36) NOT NULL,
    managergroup_id VARCHAR(36) NOT NULL,
    date_created NUMERIC(18,0) NOT NULL,
    project_ou VARCHAR(128) NOT NULL,
    PRIMARY KEY (project_id)
);

CREATE UNIQUE INDEX pk_projects ON cms_projects (project_id);

CREATE UNIQUE INDEX uk_projects ON cms_projects (project_ou, project_name, date_created);

CREATE INDEX cms_projects_01_idx ON cms_projects (project_flags);

CREATE INDEX cms_projects_02_idx ON cms_projects (group_id);

CREATE INDEX cms_projects_03_idx ON cms_projects (managergroup_id);

CREATE INDEX cms_projects_04_idx ON cms_projects (user_id);

CREATE INDEX cms_projects_05_idx ON cms_projects (project_name);

CREATE INDEX cms_projects_06_idx ON cms_projects (project_ou);

CREATE INDEX cms_projects_07_idx ON cms_projects (project_ou, project_name);

/* ----------------------------------------------------------------------- */
/* cms_publish_history */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_publish_history
(
    history_id VARCHAR(36) NOT NULL,
    publish_tag INTEGER NOT NULL,
    structure_id VARCHAR(36) NOT NULL,
    resource_id VARCHAR(36) NOT NULL,
    resource_path VARCHAR(1024) NOT NULL,
    resource_state INTEGER NOT NULL,
    resource_type INTEGER NOT NULL,
    sibling_count INTEGER NOT NULL,
    PRIMARY KEY (history_id, publish_tag, structure_id, resource_path)
);

CREATE UNIQUE INDEX pk_publish_history ON cms_publish_history (history_id, publish_tag, structure_id, resource_path);

CREATE INDEX cms_publish_history_01_idx ON cms_publish_history (publish_tag);

/* ----------------------------------------------------------------------- */
/* cms_publish_jobs */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_publish_jobs
(
    history_id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    project_name VARCHAR(255) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    publish_locale VARCHAR(16) NOT NULL,
    publish_flags INTEGER NOT NULL,
    publish_list BLOB,
    publish_report BLOB,
    resource_count INTEGER NOT NULL,
    enqueue_time NUMERIC(18,0) NOT NULL,
    start_time NUMERIC(18,0) NOT NULL,
    finish_time NUMERIC(18,0) NOT NULL,
    PRIMARY KEY (history_id)
);

CREATE UNIQUE INDEX pk_publish_jobs ON cms_publish_jobs (history_id);

/* ----------------------------------------------------------------------- */
/* cms_resource_locks */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_resource_locks
(
    resource_path VARCHAR(1024),
    user_id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    lock_type INTEGER NOT NULL
);

/* ----------------------------------------------------------------------- */
/* cms_staticexport_links */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_staticexport_links
(
    link_id VARCHAR(36) NOT NULL,
    link_rfs_path VARCHAR(1024),
    link_type INTEGER NOT NULL,
    link_parameter VARCHAR(1024),
    link_timestamp NUMERIC(18,0) NOT NULL,
    PRIMARY KEY (link_id)
);

CREATE UNIQUE INDEX pk_cms_staticexport_links ON cms_staticexport_links (link_id);

CREATE INDEX cms_staticexport_links_01_idx ON cms_staticexport_links (link_rfs_path);

/* ----------------------------------------------------------------------- */
/* cms_subscription */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_subscription
(
    principal_id VARCHAR(36) NOT NULL,
    structure_id VARCHAR(36) NOT NULL,
    date_deleted NUMERIC(18,0) NOT NULL,
    PRIMARY KEY (principal_id, structure_id)
);

CREATE UNIQUE INDEX pk_subscription ON cms_subscription (principal_id, structure_id);

CREATE INDEX cms_subscription_01_idx ON cms_subscription (principal_id);

CREATE INDEX cms_subscription_02_idx ON cms_subscription (structure_id);

CREATE INDEX cms_subscription_03_idx ON cms_subscription (date_deleted);

CREATE INDEX cms_subscription_04_idx ON cms_subscription (principal_id, structure_id, date_deleted);

/* ----------------------------------------------------------------------- */
/* cms_subscription_visit */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_subscription_visit
(
    user_id VARCHAR(36) NOT NULL,
    visit_date NUMERIC(18,0) NOT NULL,
    structure_id VARCHAR(36),
    PRIMARY KEY (user_id, visit_date)
);

CREATE UNIQUE INDEX pk_visit ON cms_subscription_visit (user_id, visit_date);

CREATE INDEX cms_subscription_visit_01_idx ON cms_subscription_visit (user_id);

CREATE INDEX cms_subscription_visit_02_idx ON cms_subscription_visit (visit_date);

CREATE INDEX cms_subscription_visit_03_idx ON cms_subscription_visit (structure_id);

CREATE INDEX cms_subscription_visit_04_idx ON cms_subscription_visit (user_id, structure_id);

CREATE INDEX cms_subscription_visit_05_idx ON cms_subscription_visit (user_id, visit_date);

CREATE INDEX cms_subscription_visit_06_idx ON cms_subscription_visit (user_id, structure_id, visit_date);

/* ----------------------------------------------------------------------- */
/* cms_userdata */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_userdata
(
    user_id VARCHAR(36) NOT NULL,
    data_key VARCHAR(255) NOT NULL,
    data_value BLOB,
    data_type VARCHAR(128) NOT NULL,
    PRIMARY KEY (user_id, data_key)
);

CREATE UNIQUE INDEX pk_userdata ON cms_userdata (user_id, data_key);

CREATE INDEX cms_userdata_01_idx ON cms_userdata (user_id);

CREATE INDEX cms_userdata_02_idx ON cms_userdata (data_key);

/* ----------------------------------------------------------------------- */
/* cms_users */
/* ----------------------------------------------------------------------- */

CREATE TABLE cms_users
(
    user_id VARCHAR(36) NOT NULL,
    user_name VARCHAR(128) NOT NULL,
    user_password VARCHAR(64) NOT NULL,
    user_firstname VARCHAR(128) NOT NULL,
    user_lastname VARCHAR(128) NOT NULL,
    user_email VARCHAR(128) NOT NULL,
    user_lastlogin NUMERIC(18,0) NOT NULL,
    user_flags INTEGER NOT NULL,
    user_ou VARCHAR(128) NOT NULL,
    user_datecreated NUMERIC(18,0) NOT NULL,
    PRIMARY KEY (user_id)
);

CREATE UNIQUE INDEX pk_users ON cms_users (user_id);

CREATE UNIQUE INDEX uk_users ON cms_users (user_name, user_ou);

CREATE INDEX cms_users_01_idx ON cms_users (user_name);

CREATE INDEX cms_users_02_idx ON cms_users (user_ou);