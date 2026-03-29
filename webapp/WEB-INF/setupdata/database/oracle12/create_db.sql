#
# replacer = "${database}"
############################

ALTER PLUGGABLE DATABASE SAVE STATE;

CREATE USER ${user} 
	IDENTIFIED BY ${password}
	DEFAULT TABLESPACE ${defaultTablespace}
	TEMPORARY TABLESPACE ${temporaryTablespace}
	QUOTA UNLIMITED ON ${defaultTablespace};

GRANT CONNECT, RESOURCE TO ${user};