CREATE OR REPLACE
FUNCTION getNextId(pTableId NUMBER) RETURN NUMBER IS
  vNextId NUMBER;
BEGIN
  select id into vNextId from cms_systemid where table_key = pTableId for update;
  update cms_systemid set id = vNextId + 1 where table_key = pTableId;
  commit;
  return vNextId;
EXCEPTION
  WHEN OTHERS THEN
    rollback;
    raise;
END getNextId;
/
