CREATE OR REPLACE
PACKAGE userTypes IS
  TYPE groupCursor IS REF CURSOR RETURN cms_groups%ROWTYPE;
  TYPE anyCursor IS REF CURSOR;

  TYPE fileRecord IS RECORD 
                  (resource_id cms_resources.resource_id%TYPE,
                   parent_id cms_resources.parent_id%TYPE, 
                   resource_name cms_resources.resource_name%TYPE, 
                   resource_type cms_resources.resource_type%TYPE, 
                   resource_flags cms_resources.resource_flags%TYPE, 
                   user_id cms_resources.user_id%TYPE, 
                   group_id cms_resources.group_id%TYPE, 
                   project_id cms_resources.project_id%TYPE, 
                   file_id cms_resources.file_id%TYPE, 
                   access_flags cms_resources.access_flags%TYPE, 
                   state cms_resources.state%TYPE, 
                   locked_by cms_resources.locked_by%TYPE, 
                   launcher_type cms_resources.launcher_type%TYPE, 
                   launcher_classname cms_resources.launcher_classname%TYPE, 
                   date_created cms_resources.date_created%TYPE, 
                   date_lastmodified cms_resources.date_lastmodified%TYPE, 
                   resource_size cms_resources.resource_size%TYPE, 
                   resource_lastmodified_by cms_resources.resource_lastmodified_by%TYPE,
                   file_content cms_files.file_content%TYPE);

END ;
/
