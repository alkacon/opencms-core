CREATE DATABASE $$user$$;
USE $$user$$;
EXEC sp_addlogin $$user$$ , $$password$$ , $$user$$;
EXEC sp_grantdbaccess $$user$$;
EXEC sp_addrolemember 'db_owner', $$user$$;