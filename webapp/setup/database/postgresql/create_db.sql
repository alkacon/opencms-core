#
# replacer = "${dbName}"
############################

# Create the user;

CREATE USER ${user} 
  PASSWORD '${password}'
  CREATEDB CREATEUSER;

#create the database

CREATE DATABASE ${dbName}
  WITH ENCODING='LATIN1' OWNER=${user};
	 
#commit all (if connection is not autocommit)
commit;
