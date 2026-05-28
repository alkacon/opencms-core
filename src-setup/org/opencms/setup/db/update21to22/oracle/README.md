# Manual Storage Schema Update for Oracle

For large installations, consider applying these schema changes manually under DBA supervision instead of relying on the automatic OpenCms updater.

Creating the indexes on `CMS_CONTENTS` and `CMS_OFFLINE_CONTENTS` can take a significant amount of time on large databases and may cause relevant I/O load or locking, depending on the database product, storage engine, and configuration.

Plan a suitable maintenance window and create a verified backup before applying the changes.

Statistics updates such as `ANALYZE`, `UPDATE STATISTICS`, or equivalent database-specific maintenance commands are not part of the automatic updater. They can be executed manually afterwards if required by the DBA.

The following statements are intended for manual execution. They are not necessarily idempotent. The DBA should verify which objects already exist before running them.

Replace `<indexTablespace>` with the index tablespace used by the OpenCms schema.

```sql
CREATE TABLE CMS_STORAGE (
    HASH VARCHAR2(128) NOT NULL,
    FILE_CONTENT BLOB NOT NULL,
    CONSTRAINT PK_STORAGE PRIMARY KEY(HASH) USING INDEX TABLESPACE <indexTablespace>
)
STORAGE (INITIAL 256K NEXT 1M PCTINCREASE 0)
LOB(FILE_CONTENT) STORE AS (
    CHUNK 32K PCTVERSION 20
    CACHE
);

ALTER TABLE CMS_CONTENTS ADD (STORAGE VARCHAR2(32));
ALTER TABLE CMS_CONTENTS ADD (HASH VARCHAR2(128));
CREATE INDEX CMS_CONTENTS_06_IDX ON CMS_CONTENTS (HASH, STORAGE) TABLESPACE <indexTablespace>;
CREATE INDEX CMS_CONTENTS_07_IDX ON CMS_CONTENTS (STORAGE, HASH) TABLESPACE <indexTablespace>;

ALTER TABLE CMS_OFFLINE_CONTENTS ADD (STORAGE VARCHAR2(32));
ALTER TABLE CMS_OFFLINE_CONTENTS ADD (HASH VARCHAR2(128));
CREATE INDEX CMS_OFFLINE_CONTENTS_01_IDX ON CMS_OFFLINE_CONTENTS (HASH, STORAGE) TABLESPACE <indexTablespace>;
CREATE INDEX CMS_OFFLINE_CONTENTS_02_IDX ON CMS_OFFLINE_CONTENTS (STORAGE, HASH) TABLESPACE <indexTablespace>;
```
