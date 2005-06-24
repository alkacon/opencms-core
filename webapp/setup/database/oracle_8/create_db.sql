#
# replacer = "${database}"
############################

CREATE USER ${user} 
	IDENTIFIED BY ${password}
	DEFAULT TABLESPACE ${defaultTablespace}
	TEMPORARY TABLESPACE ${temporaryTablespace}
	QUOTA UNLIMITED ON ${defaultTablespace}
	QUOTA UNLIMITED ON ${temporaryTablespace};

GRANT CONNECT, RESOURCE TO ${user};