#
# replacer = "${database}"
############################

CREATE DATABASE ${database} COLLATE SQL_Latin1_General_CP1_CS_AS;

# Prevents deadlocks (as default transaction management is based on locking Version 2005)
ALTER DATABASE ${database} SET ALLOW_SNAPSHOT_ISOLATION ON
ALTER DATABASE ${database} SET READ_COMMITTED_SNAPSHOT ON