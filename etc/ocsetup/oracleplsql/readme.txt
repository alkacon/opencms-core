******************************************************************
* INSTALLATION OF THE ORACLE-PL/SQL-StoredProcedures FOR OPENCMS *
******************************************************************

The StoredProcedures are created and compiled with the scripts in the folder /opencms/config/oracleplsql/. 

INSTALLATION WITH THE DATABASE-SCRIPT
The script oraclePlsql.sql which creates the tables and indexes for OpenCms also calls the installation-script install_with_db.sql for the StoredProcedures. Make sure that the folder oracleplsql with the scripts is in the same path as oraclePlsql.sql.


HOW TO INSTALL ONLY THE PROCEDURES
To start the installation you have to execute the file install.sql with sqlplus on the user where the procedures are used:
>sqlplus username/password < install.sql;
or
>sqlplus username/password
sqlplus>@install.sql;

Make sure that you are in the same path as the scripts when you start sqlplus (if you use the sqlplus-shell with Windows NT you can set the path by opening the file install.sql).