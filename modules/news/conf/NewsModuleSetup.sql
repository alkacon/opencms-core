# Setupskript for NewsModule
# usage: mysql opencms < NewsModuleSetup.sql
								 

# Table channels
create table news_channel				(id int not null,
										 name varchar(20) not null,
										 description varchar(30),
										 lockstate int not null,
										 primary key(id),
										 unique(name));

# Testdaten										 
INSERT INTO news_channel VALUES (1,'ARD','eins',-1);
INSERT INTO news_channel VALUES (2,'ZDF','zwei',-1);

# Table news										 
create table news_entry					(id int not null,
										 headline varchar(100),
										 description varchar(100),
										 text blob,
										 author varchar(40),
										 link varchar(100),
										 linkText varchar(40),
										 date date,
										 lockstate int not null,
										 channel int not null,
										 a_info1 varchar (100),
										 a_info2 varchar (100),
										 a_info3 varchar (100),
										 primary key(id));
										 
INSERT INTO news_entry VALUES (1,'opencms noch besser!','opencms ist jetzt noch besser!','opencms ist jetzt noch besser!\r\nViele neue Features wurden eingebaut!','Theo Tester','http://www.opencms.com','opencms','1999-12-11',-1,1,'','','');
INSERT INTO news_entry VALUES (2,'Neue OpenCms-Version','Version 5.33 von OpenCms ist jetzt online!','Version 5.33 von OpenCms ist jetzt online!\r\nDie Version weist viele Verbesserungen auf!','Max Mustermann','http://opencms.com','oc','1999-12-11',-1,1,'Dateigrösse: 5.3 Mb','','');
INSERT INTO news_entry VALUES (3,'OpenCms Homepage im neuen Design!','Die OpenCms Homepage wurde runderneuert!','Die OpenCms Homepage wurde runderneuert!','Theo Tester','http://www.opencms.com','oc','1999-12-11',-1,2,'','','');


# Table cms_systemid
# update the keys for the defautl entries

INSERT INTO CMS_SYSTEMID VALUES ('NEWS_ENTRY',10);
INSERT INTO CMS_SYSTEMID VALUES ('NEWS_CHANNEL',10);