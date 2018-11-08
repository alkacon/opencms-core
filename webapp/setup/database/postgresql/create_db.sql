#
# replacer = "${database}"
############################

# Create the user;

CREATE USER ${user} 
  PASSWORD '${password}'
  CREATEDB CREATEROLE;

#create the database

CREATE DATABASE ${database}
  WITH ENCODING='UNICODE' OWNER=${user};
	 
#commit all (if connection is not autocommit)
commit;
