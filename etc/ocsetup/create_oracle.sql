#
# replacer = "$$database$$"
############################

create user $$user$$ identified by $$password$$
default tablespace users
temporary tablespace temp
quota unlimited on users
quota unlimited on temp;

grant connect, resource to $$user$$ ;