#
# replacer = "$$database$$"
############################

create user $$user$$ identified by $$password$$
default tablespace $$defaultTablespace$$
temporary tablespace $$temporaryTablespace$$
quota unlimited on $$defaultTablespace$$
quota unlimited on $$temporaryTablespace$$;

grant connect, resource to $$user$$ ;