CREATE OR REPLACE
FUNCTION getNextId(pTableId VARCHAR2) RETURN NUMBER IS
  vNextId NUMBER;
BEGIN
  select id into vNextId from cms_systemid where table_key = pTableId for update;
  update cms_systemid set id = vNextId + 1 where table_key = pTableId;
  commit;
  return vNextId;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    insert into cms_systemid (table_key, id) values (pTableId, 2);
    commit;
    return 1;
  WHEN OTHERS THEN
    rollback;
    raise;
END getNextId;
/
