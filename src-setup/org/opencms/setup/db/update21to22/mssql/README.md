# Manual Storage Schema Update for MS SQL Server

For large installations, consider applying these schema changes manually under DBA supervision instead of relying on the automatic OpenCms updater.

Creating the indexes on `CMS_CONTENTS` and `CMS_OFFLINE_CONTENTS` can take a significant amount of time on large databases and may cause relevant I/O load or locking, depending on the database product, storage engine, and configuration.

Plan a suitable maintenance window and create a verified backup before applying the changes.

Statistics updates such as `ANALYZE`, `UPDATE STATISTICS`, or equivalent database-specific maintenance commands are not part of the automatic updater. They can be executed manually afterwards if required by the DBA.

The following statements are intended for manual execution. They are not necessarily idempotent. The DBA should verify which objects already exist before running them.

```sql
CREATE TABLE CMS_STORAGE (
    HASH NVARCHAR(128) NOT NULL,
    FILE_CONTENT IMAGE NOT NULL,
    PRIMARY KEY(HASH)
);

ALTER TABLE CMS_CONTENTS ADD STORAGE NVARCHAR(32);
ALTER TABLE CMS_CONTENTS ADD HASH NVARCHAR(128);
CREATE NONCLUSTERED INDEX CMS_CONTENTS_05_IDX ON CMS_CONTENTS (HASH, STORAGE);
CREATE NONCLUSTERED INDEX CMS_CONTENTS_07_IDX ON CMS_CONTENTS (STORAGE, HASH);

ALTER TABLE CMS_OFFLINE_CONTENTS ADD STORAGE NVARCHAR(32);
ALTER TABLE CMS_OFFLINE_CONTENTS ADD HASH NVARCHAR(128);
CREATE NONCLUSTERED INDEX CMS_OFFLINE_CONTENTS_01_IDX ON CMS_OFFLINE_CONTENTS (HASH, STORAGE);
CREATE NONCLUSTERED INDEX CMS_OFFLINE_CONTENTS_02_IDX ON CMS_OFFLINE_CONTENTS (STORAGE, HASH);
```
