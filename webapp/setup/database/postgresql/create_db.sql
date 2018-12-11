#
# replacer = "${database}"
############################

# Create the user;

CREATE USER ${user} WITH PASSWORD '${password}';

#create the database

CREATE DATABASE ${database}
  WITH ENCODING='UNICODE' OWNER=${user};
	 
#commit all (if connection is not autocommit)
commit;
