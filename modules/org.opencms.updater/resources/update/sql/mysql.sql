alter table CMS_GROUPS add column GROUP_OU varchar(128);
alter table CMS_GROUPS drop index GROUP_NAME_IDX;
alter table CMS_GROUPS add INDEX GROUP_NAME_IDX (GROUP_NAME);
alter table CMS_GROUPS add INDEX GROUP_OU_IDX (GROUP_OU);
alter table CMS_GROUPS add unique INDEX GROUP_FQN_IDX (GROUP_OU, GROUP_NAME);
update CMS_GROUPS set GROUP_OU = "";

alter table CMS_USERS add column USER_OU varchar(128);
alter table CMS_USERS drop index USER_NAME_IDX;
alter table CMS_USERS add INDEX USER_NAME_IDX (USER_NAME);
alter table CMS_USERS add INDEX USER_OU_IDX (USER_OU);
alter table CMS_USERS add unique INDEX USER_FQN_IDX (USER_OU, USER_NAME);
alter table CMS_USERS drop column USER_TYPE;
update CMS_USERS set USER_OU = "";