# OpenCms Storage Support

OpenCms can store binary resource content outside the normal VFS content database columns. The storage layer is mainly useful for:

- deduplicating identical binary content by SHA-512 hash to reduce storage usage
- keeping large blobs out of the `CMS_CONTENTS.FILE_CONTENT` and `CMS_OFFLINE_CONTENTS.FILE_CONTENT` database columns
- storing blobs in the database, on a file system, or in S3-compatible object storage
- delivering externally stored large media files (Audio, Video, PDF) directly from the storage backends with HTTP range requests

The storage implementation supports gradual migration from classic database content storage to dedicated blob storage. Storage delivery is also an optional feature supplementing the classic delivery via the static export folder.

## Activation Summary

In new OpenCms installations storage-related database support is present by default: the setup creates the storage-aware database schema and configures the storage-aware VFS driver `CmsStorageVfsDriver` in `WEB-INF/config/opencms.properties`.

As a default, OpenCms uses the default `db` storage backend. Configure `storage.active` in `opencms.properties` as described below when a different active backend, such as S3 or file system storage, should be used.

For the time being and for backward compatibility, new installations use the `CmsNoExternalStoragePolicy`. Although storage-related database support is present by default, content is not stored externally but still written to the normal VFS content tables as was the case before.

To enable external storage, change the `CmsNoExternalStoragePolicy` to `CmsDefaultStoragePolicy` in `WEB-INF/config/opencms-vfs.xml`. The default storage policy stores all non-empty binary and image resources deduplicated in the active storage backend.

Existing installations can also continue to use the classic `CmsVfsDriver`, which is still supported. In that mode, no storage database schema changes are required.

## Storage Policy

The storage policy decides whether file content remains in the VFS content tables or is stored in the active storage backend. The installation default is `org.opencms.db.storage.policy.CmsNoExternalStoragePolicy`.

`CmsNoExternalStoragePolicy` always keeps content in `CMS_CONTENTS` and `CMS_OFFLINE_CONTENTS`. This makes it possible to install the storage-aware schema and driver without changing where binary content is stored.

The policy is configured in `WEB-INF/config/opencms-vfs.xml` inside the `<resources>` section:

```xml
<storage-policy class="org.opencms.db.storage.policy.CmsNoExternalStoragePolicy" />
```

Switch from `CmsNoExternalStoragePolicy` to `CmsDefaultStoragePolicy` to enable offloading for binary and image resources:

```xml
<storage-policy class="org.opencms.db.storage.policy.CmsDefaultStoragePolicy">
    <param name="threshold">0</param>
    <param name="resourceTypeIds">2,3</param>
</storage-policy>
```

`CmsDefaultStoragePolicy` can be tuned with configuration parameters such as `threshold` and `resourceTypeIds`. The threshold value is configured in bytes. The default `0` means that every non-empty resource covered by the policy is stored externally. `resourceTypeIds` is a comma-separated list of numeric resource type ids. The default is `2,3`, which means binary files and images. With the `db` storage backend, this stores binary and image resources in the deduplicated `CMS_STORAGE` table. With an S3 backend with higher latency, configure a higher threshold so that only larger files are stored externally.

For S3 and file system storage, the storage policy also determines which resources can use direct delivery. Direct delivery is only available for resources whose content has actually been offloaded to the configured storage backend. Resources which remain in the normal VFS content columns continue to use the classic database and static export delivery path.

The storage policy can also be replaced with a custom implementation of `org.opencms.db.storage.policy.I_CmsStoragePolicy`.

## Image Cache

The OpenCms image cache stores generated image derivatives, for example scaled or cropped variants created by the `CmsImageLoader`. Traditionally these derivatives are stored on the application server file system below `WEB-INF/imagecache/`.

When an S3 or file system storage backend is used, generated image derivatives can now also be stored in the corresponding storage backend. This is especially useful in clustered installations: derivatives no longer have to be stored locally on every node. It can also reduce work on individual nodes, because image derivatives generated on one node can be reused by another node instead of being generated again there.

Image cache storage is configured in `WEB-INF/config/opencms-importexport.xml` via the top-level `<imagecache>` element. For example, an installation using S3 storage can configure `CmsS3ImageCache`:

```xml
<imagecache class="org.opencms.loader.CmsS3ImageCache">
    <params>
        <param name="bucket">opencms-image-cache</param>
    </params>
</imagecache>
```

The detailed configuration options for RFS, S3 and file system image cache storage are described in the configuration details below.

## Storage Delivery

The storage delivery feature is optional on top of the storage layer.

With storage delivery enabled, PDFs, audio files, videos and other stored binaries can be streamed from the configured storage backend through OpenCms. The storage delivery path supports HTTP range requests. This is especially relevant for large PDFs, audio files and videos, where clients often request only parts of a file.

The storage delivery feature is activated in `WEB-INF/config/opencms-importexport.xml` with the `storedcontentdelivery` configuration.

```xml
<storedcontentdelivery enabled="true">
    <enabledsuffixes>
        <suffix key=".pdf" />
        <suffix key=".mp3" />
        <suffix key=".mp4" />
    </enabledsuffixes>
</storedcontentdelivery>
```

The default configuration focuses on large media and download files.

Images require special attention. Without an additional HTTP cache in front of OpenCms, original images and scaled image derivatives should generally continue to use the classic `/export` path. Do not add image suffixes to the `enabledsuffixes` list casually. If a CDN, Varnish, reverse proxy, or another capable HTTP cache is used for frontend traffic, image suffixes can be enabled for storage delivery as well. In that setup, images can be delivered directly from storage and the additional delivery copies in the `/export` folder are avoided.

The static export folder remains part of the delivery architecture for images by default, and for file types such as CSS and JavaScript that benefit from GZIP compression.

The storage delivery feature is available for S3 and file system storage. The database storage backend keeps the classic behavior and continues to use the static export folder and the classic image cache. Offline resources, unpublished files and ACL-protected public resources remain protected in the same way as with the classic database and static export delivery model.

## Supported Storage Backends

OpenCms supports three storage backend types:


| Backend                      | Type | Typical use                                                                                                                                                                                     |
| ---------------------------- | ---- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Database                     | `db` | Default active backend. When an offloading policy is configured, binary resource content is stored in the same database as the other OpenCms data, but deduplicated in the `CMS_STORAGE` table. |
| S3-compatible object storage | `s3` | Binary resource content is stored in an S3-compatible object store such as RustFS. Direct delivery with HTTP range requests is supported.                                                       |
| File system                  | `fs` | Binary resource content is stored on a local or shared file system path. Direct delivery with HTTP range requests is supported.                                                                 |


### Choosing a Backend

Use `db` when the installation should stay as simple and close to the classic OpenCms behavior as possible, or when binary content should be deduplicated without introducing a separate storage service. Use the default `CmsNoExternalStoragePolicy` when the storage-aware driver and schema should be active but content should keep using the classic content columns.

Use `s3` when blob storage should be independent from the application server file system, for example for container deployments, clustered installations, large media volumes, external backups, or object-storage lifecycle management. The current implementation has been tested with RustFS. Other S3-compatible services, including AWS S3 and MinIO, should be validated before production use.

Use `fs` when blob data should be moved out of the database but a reliable local or shared file system path is available. In clustered or containerized setups, this path must be shared and durable for all OpenCms nodes that need to read the blobs.

For installations which want to deliver PDFs, audio files, videos and other large binaries efficiently, choose an S3 or file system backend together with storage delivery and a storage policy which includes these media resource types. The default `CmsDefaultStoragePolicy` settings are a good starting point because they include binary and image resources. For production frontend delivery with storage delivery enabled, place a suitable HTTP cache such as a CDN, Varnish, reverse proxy, or web server cache in front of OpenCms. This keeps repeated requests for large PDFs, videos, audio files and other large downloads away from both OpenCms and the storage backend. If large media files are excluded from the storage policy, they remain database-backed and can still increase the size of the static export folder.

## Configuration Details

### Driver Configuration

New OpenCms installations already use the storage-aware VFS driver. Existing installations can either keep using the classic VFS driver or switch to the storage-aware VFS driver.

If an existing installation keeps the classic driver, no storage database schema migration is needed:

```properties
db.vfs.driver=org.opencms.db.mysql.CmsVfsDriver
db.vfs.pool=opencms:default
db.vfs.sqlmanager=org.opencms.db.mysql.CmsSqlManager
```

To enable storage-aware content handling in an existing installation, replace the normal VFS driver with the storage variant for your database package after applying the storage schema migration. Keep the normal SQL manager classes.

Example for MySQL:

```properties
db.vfs.driver=org.opencms.db.mysql.CmsStorageVfsDriver
db.vfs.pool=opencms:default
db.vfs.sqlmanager=org.opencms.db.mysql.CmsSqlManager
```

Use the matching package for your database, for example:

```text
org.opencms.db.mysql
org.opencms.db.postgresql
org.opencms.db.oracle
org.opencms.db.mssql
org.opencms.db.db2
org.opencms.db.as400
org.opencms.db.hsqldb
```

Only `db.vfs.driver` must use the storage variant. Storage-specific content handling, including history content loading and cleanup, is implemented in the VFS driver path.

### Storage Configuration

Basic storage configuration is also done in `WEB-INF/config/opencms.properties`.

`storage.active` is the backend id used for new externally stored content.

`storage.legacy` is an optional comma-separated list of backend ids used for existing content. Legacy backends are not used for new writes, but they remain readable during migration. When existing content is rewritten, the old blob can be read from a legacy backend while the new blob is written to the active backend. Legacy content may be deleted when it is no longer referenced.

Content rows store the configured backend id as stable storage identifier:


| Backend     | Configured backend id | Stored identifier |
| ----------- | --------------------- | ----------------- |
| Database    | `db`                  | `db`              |
| File system | `fs1`                 | `fs1`             |
| S3          | `s3main`              | `s3main`          |


The configured backend id must remain stable because it is stored in content rows. Do not derive it from mutable details such as bucket names, endpoint URLs or file system paths. Also do not change the backend type for an existing id while content rows still reference it.

#### Database Storage

Database storage is a built-in singleton backend. It always uses the reserved backend id `db` and does not need a `storage.backend.db.*` block.

Default configuration:

```properties
storage.active=db
```

This is also the implicit default if `storage.active` is not set.

When migrating existing database content to an external backend, configure `db` as legacy:

```properties
storage.active=s3main
storage.legacy=db
```

Do not configure a named database backend:

```properties
# Invalid
storage.active=legacydb
storage.backend.legacydb.type=db
```

There can only be one database storage backend, and its id is always `db`.

#### File System Active, Database Legacy

```properties
storage.active=fs1
storage.legacy=db

storage.backend.fs1.type=fs
storage.backend.fs1.path=/var/opencms/storage/fs1
```

The stored identifier for this backend is `fs1`.

#### Multiple Legacy Backends

Multiple legacy backends can be configured when content has already moved through several storage locations:

```properties
storage.active=s3main
storage.legacy=db, fs1

storage.backend.s3main.type=s3
storage.backend.s3main.endpoint=http://localhost:9000
storage.backend.s3main.bucket=opencms-test
storage.backend.s3main.accessKey=YOUR_ACCESS_KEY
storage.backend.s3main.secretKey=YOUR_SECRET_KEY
storage.backend.s3main.pathStyle=true

storage.backend.fs1.type=fs
storage.backend.fs1.path=/var/opencms/storage/fs1
```

For a migration from database to S3, configure:

```properties
storage.active=s3main
storage.legacy=db
```

This keeps existing database blobs readable while new externally stored blobs go to S3.

#### S3 Client Configuration

The S3 backend currently uses the generic AWS SDK based client `org.opencms.db.storage.s3.CmsGenericS3Client`. The client implementation itself is not selected through `opencms.properties`; configure the client behavior with the S3 backend properties instead.

The configured `accessKey` and `secretKey` values are passed through the OpenCms credentials resolver before the S3 client is created. The default resolver returns the configured values unchanged. A custom resolver can use placeholders or external secret references in these properties and resolve them at startup.

Use `pathStyle=true` for RustFS and other local S3-compatible services which require path-style bucket access. Configure `region`, timeouts and retries when the defaults do not match the local object storage setup:

```properties
storage.backend.s3main.pathStyle=true
storage.backend.s3main.region=aws-global
storage.backend.s3main.connectionTimeout=5000
storage.backend.s3main.socketTimeout=30000
storage.backend.s3main.apiCallAttemptTimeout=30000
storage.backend.s3main.apiCallTimeout=60000
storage.backend.s3main.maxRetries=2
```

### Image Cache Configuration

The image cache is configured in `WEB-INF/config/opencms-importexport.xml` via the top-level `<imagecache>` element. If no separate image cache configuration is present, OpenCms keeps using the classic RFS image cache behavior from `CmsImageLoader`, i.e., the default `WEB-INF/imagecache/` folder.

For database storage, use `CmsRfsImageCache` or omit the separate image cache configuration:

```xml
<imagecache class="org.opencms.loader.CmsRfsImageCache">
    <params>
        <param name="folder">WEB-INF/imagecache/</param>
    </params>
</imagecache>
```

The `folder` parameter is optional. If it is not configured, the old `image.folder` parameter from the `CmsImageLoader` configuration in `WEB-INF/config/opencms-vfs.xml` is still honored. If neither parameter is configured, `WEB-INF/imagecache/` is used.

For S3 storage, configure `CmsS3ImageCache` and the image cache bucket:

```xml
<imagecache class="org.opencms.loader.CmsS3ImageCache">
    <params>
        <param name="bucket">opencms-image-cache</param>
    </params>
</imagecache>
```

The S3 image cache uses the active S3 storage backend connection settings from `opencms.properties`, but writes generated derivatives to the configured image cache bucket. The image cache may use the same bucket as the active S3 storage backend. In that case, a `prefix` parameter is required so that generated image cache objects are stored outside the storage hash namespace:

```xml
<imagecache class="org.opencms.loader.CmsS3ImageCache">
    <params>
        <param name="bucket">opencms-test</param>
        <param name="prefix">imagecache/</param>
    </params>
</imagecache>
```

The prefix is normalized to have no leading slash and exactly one trailing slash. Startup fails if the S3 image cache uses the active storage bucket without a `prefix` parameter.

For file system storage, configure `CmsFsImageCache` and the target path:

```xml
<imagecache class="org.opencms.loader.CmsFsImageCache">
    <params>
        <param name="path">/var/opencms/image-cache</param>
    </params>
</imagecache>
```

Do not configure image cache classes for a different backend type. For example, `CmsFsImageCache` is invalid when the active storage backend is S3, and `CmsS3ImageCache` is invalid when the active storage backend is file system or database storage. Invalid combinations are rejected during startup. Mixed setups are not supported: S3 storage uses an S3 image cache, file system storage uses a file system image cache, and database storage uses the classic RFS image cache.

### Direct Delivery Configuration

Direct delivery is controlled by the `storedcontentdelivery` configuration in `WEB-INF/config/opencms-importexport.xml`. When it is enabled and the active storage backend is S3 or file system storage, OpenCms can use direct delivery for externally stored resources.

The optional `enabledsuffixes` list can be used to decide which resources are delivered directly and which continue to use the classic static export path. This list is an additional storage delivery filter; it does not replace the normal `<staticexport>` configuration. For unchanged original files, matching suffixes enable delivery from the configured storage backend. For scaled image requests, matching suffixes enable delivery from the configured image cache backend without writing an additional `/export` copy. The `<imagecache>` configuration still controls where image derivatives are stored.

The normal static export rules still decide whether a resource is exportable at all. If an exportable resource is stored externally and its suffix is listed under `storedcontentdelivery/enabledsuffixes`, the storage delivery path wins and no `/export` copy is written for that resource. For scaled image requests, the suffix check is applied to the generated export file name, so a generated file such as `image.jpg_123.jpg` is controlled by `.jpg`. If the suffix is not listed there, the resource continues to use the classic static export behavior even if the binary content itself is stored externally or the image derivative is stored in an external image cache. If the `enabledsuffixes` element is omitted, all exportable resources handled by direct-delivery-capable loaders are eligible for storage delivery.

The default suffix list intentionally focuses on large media, office and archive downloads. Image suffixes such as `.jpg`, `.jpeg`, `.png`, `.gif`, `.tif`, `.tiff` and `.webp` are not enabled by default because original images and scaled image derivatives should normally use the classic `/export` path. Enable image suffixes only when a capable HTTP cache such as a CDN, Varnish or reverse proxy is in front of OpenCms for public frontend traffic and avoiding additional `/export` copies is desired.

Example for enabling direct delivery for PDF, MP3 and MP4 files:

```xml
<storedcontentdelivery enabled="true">
    <enabledsuffixes>
        <suffix key=".pdf" />
        <suffix key=".mp3" />
        <suffix key=".mp4" />
    </enabledsuffixes>
</storedcontentdelivery>
```

The image cache is configured separately in the top-level `imagecache` element. It controls where generated image derivatives are stored and is not required to enable direct delivery for unchanged original files.

## Backward Compatibility

Switching from "without storage" to "with storage" is intended to be gradual.

For existing installations, the first decision is whether to switch drivers at all:

- Keep `CmsVfsDriver` when the installation should continue with the classic behavior and no storage schema migration should be applied.
- With the `CmsNoExternalStoragePolicy` it is possible to switch to `CmsStorageVfsDriver` and migrate the database schema as described below, even if content should initially remain in the normal VFS content tables.

After the database migration, driver switch and policy configuration:

- Existing content continues to be read from the database because old rows have no storage identifier and no hash.
- New content is stored according to the configured storage policy and `storage.active`.
- If `CmsDefaultStoragePolicy` is configured and `storage.active=db`, qualifying blobs are written to the deduplicated `CMS_STORAGE` table.
- If `CmsDefaultStoragePolicy` is configured and `storage.active=s3main`, qualifying blobs are written to S3.
- Existing database content is not automatically moved to the `CMS_STORAGE` table or to S3. It remains readable from the database until it is rewritten or otherwise migrated.

### Migration Notes For Exported Resources

When switching an installation from the classic database/static export behavior to S3 or file system storage with direct delivery, existing files in the static export folder need special attention. If a requested file already exists in the static export folder, the servlet container may serve this file directly. In that case the request does not reach OpenCms, and OpenCms cannot use the storage direct delivery path for that request.

For a clean switch to S3 or file system direct delivery, stop OpenCms and clear the static export folder after changing the storage and image cache configuration. This ensures that requests for externally stored PDFs, audio files, videos and other enabled binaries are handled by OpenCms and can use direct delivery where supported. The static export folder will still be used afterwards for resources which are not handled by direct delivery, for example images not listed in `enabledsuffixes`, generated CSS, JavaScript or resources handled by loaders without direct delivery support.

When switching from the classic RFS image cache to an S3 or file system image cache, the old RFS image cache folder can also be cleared. This is mainly a cleanup step to free disk space and avoid confusion during verification. With S3 or file system image cache configuration, new scaled image variants are generated in the configured image cache backend.

If a reverse proxy, web server or CDN is used in front of OpenCms, purge its cached copies as part of the same migration. Otherwise stale exported media files may still be served outside OpenCms even though the OpenCms storage configuration has already changed.

## Automatic Migration

New OpenCms installations already create the storage-aware database schema during setup and configure the storage-aware VFS driver. No separate storage schema migration is required for new installations. The default `CmsNoExternalStoragePolicy` still keeps binary content in the normal VFS content columns until the storage policy is changed.

Existing installations can be upgraded with the OpenCms updater. The updater detects whether the storage schema is missing or incomplete and can add the required `CMS_STORAGE` table, storage metadata columns and storage indexes.

The automatic storage schema update is controlled by this property in `WEB-INF/config/opencms.properties`:

```properties
setup.storage.schema.update=false
```

Supported values are:


| Value   | Meaning                                                                                                                                                               |
| ------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `auto`  | Update the storage schema if the updater detects that it is missing or incomplete.                                                                                    |
| `true`  | Enable the storage schema update explicitly. The updater still skips schema objects that already exist.                                                               |
| `false` | Skip the storage schema update. This is also the default when the property is not set. Use this when the database changes are applied manually, for example by a DBA. |


For existing installations, this automatic migration only updates the database schema. It does not modify `WEB-INF/config/opencms.properties` and does not switch `db.vfs.driver` from `CmsVfsDriver` to `CmsStorageVfsDriver`. Changing the VFS driver remains a separate manual configuration step and should be done only after the storage-aware schema is present.

Use the automatic updater for normal-sized installations where the database schema changes can safely run during the OpenCms update window. The updater is idempotent for the storage schema objects and skips tables, columns and indexes that already exist.

For very large installations, consider applying the database changes manually before running or completing the OpenCms update. In particular, creating the indexes on `CMS_CONTENTS` and `CMS_OFFLINE_CONTENTS` can take a significant amount of time and may cause relevant I/O load or locking, depending on the database product, storage engine and configuration.

## Manual Migration

At a high level, enabling storage in an existing installation means:

1. Make sure the storage-aware database schema is present.
2. Replace the standard VFS driver with the storage-aware VFS driver.

### Schema Migration

While new OpenCms installations create the required schema during setup, existing installations should first verify and, if necessary, apply the schema migration, and only then manually switch from `CmsVfsDriver` to `CmsStorageVfsDriver`.

The manual migration SQL is documented per database product in:

```text
opencms-core/src-setup/org/opencms/setup/db/update21to22/<db>/README.md
```

Run the matching SQL against the OpenCms database while OpenCms is stopped. Back up the database before running the migration. The manual SQL statements are intended for DBA-supervised execution and are not necessarily idempotent; verify which objects already exist before running them.

The migration creates the `CMS_STORAGE` table, adds storage metadata columns to the VFS content tables and creates indexes on `(HASH, STORAGE)`. Existing content remains readable because old rows have no storage identifier and no hash, so content is still read from the normal `FILE_CONTENT` columns.

Statistics updates such as `ANALYZE`, `UPDATE STATISTICS`, or equivalent database-specific maintenance commands are not part of the automatic updater. For manual migrations, the DBA should decide whether and when to run such maintenance after the schema changes.

### Driver Configuration

Before:

```properties
db.vfs.driver=org.opencms.db.mysql.CmsVfsDriver
db.vfs.pool=opencms:default
db.vfs.sqlmanager=org.opencms.db.mysql.CmsSqlManager
```

After:

```properties
db.vfs.driver=org.opencms.db.mysql.CmsStorageVfsDriver
db.vfs.pool=opencms:default
db.vfs.sqlmanager=org.opencms.db.mysql.CmsSqlManager
```

Only the VFS driver uses a storage-aware class. The other drivers such as project, user, subscription and history, keep using the normal driver class.

Before starting OpenCms with the storage-aware VFS driver, the storage-aware schema should be present.

## Content Migration Tool

The storage schema migration only prepares the database for storage-aware content handling. It does not move existing binary content. Existing rows in `CMS_CONTENTS` and `CMS_OFFLINE_CONTENTS` remain in their current storage location until they are rewritten by OpenCms or migrated explicitly.

For planned downtime migrations, OpenCms provides a standalone storage migration tool. The tool is built from `tools/storage/src` and can be packaged with:

```bash
./gradlew storageMigrationJar
```

The resulting executable JAR scans `CMS_CONTENTS` and `CMS_OFFLINE_CONTENTS`, evaluates the configured storage policy from `WEB-INF/config/opencms-vfs.xml`, and rewrites rows whose current storage state does not match the policy.

Typical use cases:

- move local `FILE_CONTENT` blobs to the active external backend
- move blobs from the legacy `db` backend / `CMS_STORAGE` to a new active backend such as `fs` or `s3`
- move externally stored blobs back into the local VFS content tables by configuring `CmsNoExternalStoragePolicy`
- verify after migration that no candidates remain

The safest procedure is to stop all OpenCms nodes while the tool runs. In controlled cluster setups, read-only frontend nodes may remain online only if they are guaranteed not to write to the OpenCms database and are configured to read both the legacy and active storage backends during the migration.

### Preparing The Content Migration

Before running the tool:

1. Back up the OpenCms database and the external storage backend.
2. Stop all OpenCms nodes, or make sure only strictly read-only frontend nodes remain online as described above.
3. Make sure the storage-aware schema is present.
4. Configure the final storage setup in `WEB-INF/config/opencms.properties`.
5. Configure the final storage policy in `WEB-INF/config/opencms-vfs.xml`.
6. For migrations from an existing backend, keep the old backend readable through `storage.legacy`.

Example: migrate existing database-backed blobs to file system storage:

```properties
storage.active=fs1
storage.legacy=db

storage.backend.fs1.type=fs
storage.backend.fs1.path=/var/opencms/storage/fs1
```

### Running The Tool

Dry-run first:

```bash
java -jar opencms-storage-migration.jar \
  --webinf /path/to/opencms/WEB-INF \
  --dry-run
```

Execute and verify:

```bash
java -jar opencms-storage-migration.jar \
  --webinf /path/to/opencms/WEB-INF \
  --execute \
  --verify
```

Useful options:


| Option                      | Meaning                                                                             |
| --------------------------- | ----------------------------------------------------------------------------------- |
| `--webinf <path>`           | WEB-INF folder containing `config/opencms.properties` and `config/opencms-vfs.xml`. |
| `--properties <path>`       | Explicit path to `opencms.properties`.                                              |
| `--vfs-config <path>`       | Explicit path to `opencms-vfs.xml`.                                                 |
| `--tables offline,contents` | Restrict migration to `CMS_OFFLINE_CONTENTS`, `CMS_CONTENTS`, or both.              |
| `--batch-size <n>`          | JDBC fetch size and commit interval.                                                |
| `--driver-jar <path>`       | Additional JDBC driver JAR.                                                         |
| `--driver-dir <path>`       | Directory containing JDBC driver JARs.                                              |
| `--dry-run`                 | Scan only. This is the default.                                                     |
| `--execute`                 | Rewrite matching rows.                                                              |
| `--verify`                  | Re-scan after execution and fail if candidates remain.                              |


### Migration Semantics

The tool does not simply migrate rows with empty `STORAGE` values. It compares each candidate row with the configured storage policy.

A row is migrated when:

- the policy requires external storage but the row is still stored locally
- the policy requires external storage but the row references a non-active backend
- the policy requires local storage but the row currently references an external backend

This means the same tool can also be used for a planned rollback to local table storage by configuring `CmsNoExternalStoragePolicy` and running the tool with `--execute --verify`.

For migrations from local `FILE_CONTENT` columns to an external storage backend, the migrated rows are updated with an empty `FILE_CONTENT` value and the new `STORAGE` / `HASH` reference. For migrations from one external storage backend to another, the tool writes the blob to the target backend and updates the content rows, but it does not delete the blob from the previous source backend. After a successful external-to-external migration, run the storage maintenance tool in orphan-scan mode and then in orphan-delete mode to remove source blobs which are no longer referenced by `CMS_CONTENTS` or `CMS_OFFLINE_CONTENTS`.

### Performance Notes

The candidate scan uses the `STORAGE` and `HASH` columns and benefits from indexes with leading `STORAGE`, such as `(STORAGE, HASH)`, on both content tables. New setup schemas and the storage schema updater create these indexes.

For very large installations, run a dry-run first and check database load and execution plans before the downtime window.

## Storage Maintenance Tool

The storage maintenance tool performs operational checks for configured storage backends. It is also built from `tools/storage/src`:

```bash
./gradlew storageMaintenanceJar
```

The resulting executable JAR can validate backend availability, verify that database references can still be loaded, scan for orphaned blobs, and optionally delete orphaned blobs.

Read-only checks:

```bash
java -jar opencms-storage-maintenance.jar \
  --webinf /path/to/opencms/WEB-INF \
  --mode all
```

Useful read-only modes:


| Mode                | Meaning                                                                                                               |
| ------------------- | --------------------------------------------------------------------------------------------------------------------- |
| `validate-backends` | Run a lightweight read/write availability check for all configured storage backends.                                  |
| `verify-references` | Load all distinct `STORAGE` / `HASH` references from the content tables and report missing blobs.                     |
| `scan-orphans`      | List blobs in configured storage backends which are no longer referenced by `CMS_CONTENTS` or `CMS_OFFLINE_CONTENTS`. |
| `all`               | Run the read-only checks.                                                                                             |


Orphan deletion is intentionally a separate mode. Without `--execute`, it remains a dry-run:

```bash
java -jar opencms-storage-maintenance.jar \
  --webinf /path/to/opencms/WEB-INF \
  --mode delete-orphans
```

To actually delete orphaned blobs:

```bash
java -jar opencms-storage-maintenance.jar \
  --webinf /path/to/opencms/WEB-INF \
  --mode delete-orphans \
  --execute
```

By default, `delete-orphans` deletes at most 1000 blobs per run. Use `--delete-limit <number>` to tune the limit, or `--delete-limit 0` for an unlimited maintenance run.

The tool checks references again immediately before deleting a blob. It can enumerate file system and S3 storage backends, and scans the database backend through `CMS_STORAGE`. For S3, the backend must allow bucket listing in addition to object read/write/delete permissions.

When an S3 image cache shares the active S3 storage bucket and uses the `prefix` parameter, the maintenance tool reads `WEB-INF/config/opencms-importexport.xml` and ignores objects below that prefix during orphan scans and orphan deletion.

## Step-by-Step: Switch To S3 Storage

1. Stop OpenCms.
2. Back up the OpenCms database, `WEB-INF/config/opencms.properties`, `WEB-INF/config/opencms-vfs.xml`, `WEB-INF/config/opencms-importexport.xml`, and the current static export and image cache folders if their contents need to be preserved.
3. Verify or apply the storage schema migration. For normal-sized installations, use the OpenCms updater with `setup.storage.schema.update=auto` or `setup.storage.schema.update=true`. The default value `false` skips the automatic storage schema update. For large installations, consider applying the matching manual migration SQL:

```text
opencms-core/src-setup/org/opencms/setup/db/update21to22/<db>/README.md
```

1. Manually configure the storage-aware VFS driver in `WEB-INF/config/opencms.properties`.

Example for MySQL:

```properties
db.vfs.driver=org.opencms.db.mysql.CmsStorageVfsDriver
db.vfs.pool=opencms:default
db.vfs.sqlmanager=org.opencms.db.mysql.CmsSqlManager

db.project.sqlmanager=org.opencms.db.mysql.CmsSqlManager
db.user.sqlmanager=org.opencms.db.mysql.CmsSqlManager
db.subscription.sqlmanager=org.opencms.db.mysql.CmsSqlManager
db.history.sqlmanager=org.opencms.db.mysql.CmsSqlManager
```

1. Configure S3 as the active storage backend and database storage as legacy:

```properties
storage.active=s3main
storage.legacy=db

storage.backend.s3main.type=s3
storage.backend.s3main.endpoint=http://localhost:9000
storage.backend.s3main.bucket=opencms-test
storage.backend.s3main.accessKey=YOUR_ACCESS_KEY
storage.backend.s3main.secretKey=YOUR_SECRET_KEY
storage.backend.s3main.pathStyle=true
```

1. Optional: configure S3 client timeouts, retries and region:

```properties
storage.backend.s3main.region=aws-global
storage.backend.s3main.connectionTimeout=5000
storage.backend.s3main.socketTimeout=30000
storage.backend.s3main.apiCallAttemptTimeout=30000
storage.backend.s3main.apiCallTimeout=60000
storage.backend.s3main.maxRetries=2
```

1. Configure the S3 image cache bucket in `WEB-INF/config/opencms-importexport.xml`:

```xml
<imagecache class="org.opencms.loader.CmsS3ImageCache">
    <params>
        <param name="bucket">opencms-image-cache</param>
    </params>
</imagecache>
```

If the image cache should share the active S3 storage bucket, use that bucket name and add a dedicated prefix, for example `<param name="prefix">imagecache/</param>`.

1. Configure an offloading storage policy in `WEB-INF/config/opencms-vfs.xml`. Make sure the policy includes the media resource types which should use direct delivery. The default `resourceTypeIds` value `2,3` includes binary and image resources:

```xml
<storage-policy class="org.opencms.db.storage.policy.CmsDefaultStoragePolicy">
    <param name="threshold">0</param>
    <param name="resourceTypeIds">2,3</param>
</storage-policy>
```

1. For a full downtime migration of existing binary content, run the content migration tool with `--execute --verify` before starting OpenCms again. For gradual migration, skip this step and let existing content remain readable through the `db` legacy backend until it is rewritten.
2. Clear the static export folder so stale exported media files do not bypass OpenCms and prevent direct delivery from being used. When switching from the classic RFS image cache to the S3 image cache, optionally clear the old RFS image cache folder as well.
3. Start OpenCms and check the startup log. The storage initialization should validate the configured S3 backend, and the image loader should initialize the configured S3 image cache bucket.
4. Upload or modify a binary/image resource which matches the storage policy. New qualifying content should be written to S3, while existing database content remains readable through the `db` legacy backend if gradual migration is used. Requests for supported stored media resources should be streamed through OpenCms without creating a local static export copy.

### Complete Example: S3 Active With Database Legacy

This is a complete storage-related `opencms.properties` excerpt for MySQL and S3-compatible storage:

```properties
################################################################################
# Storage-aware VFS database driver
################################################################################

db.vfs.driver=org.opencms.db.mysql.CmsStorageVfsDriver
db.vfs.pool=opencms:default
db.vfs.sqlmanager=org.opencms.db.mysql.CmsSqlManager

db.project.sqlmanager=org.opencms.db.mysql.CmsSqlManager
db.user.sqlmanager=org.opencms.db.mysql.CmsSqlManager
db.subscription.sqlmanager=org.opencms.db.mysql.CmsSqlManager
db.history.sqlmanager=org.opencms.db.mysql.CmsSqlManager

################################################################################
# Storage backends
################################################################################

storage.active=s3main
storage.legacy=db

storage.backend.s3main.type=s3
storage.backend.s3main.endpoint=http://localhost:9000
storage.backend.s3main.bucket=opencms-test
storage.backend.s3main.accessKey=YOUR_ACCESS_KEY
storage.backend.s3main.secretKey=YOUR_SECRET_KEY
storage.backend.s3main.pathStyle=true

storage.backend.s3main.region=aws-global
storage.backend.s3main.connectionTimeout=5000
storage.backend.s3main.socketTimeout=30000
storage.backend.s3main.apiCallAttemptTimeout=30000
storage.backend.s3main.apiCallTimeout=60000
storage.backend.s3main.maxRetries=2
```

For PostgreSQL, Oracle, MSSQL, DB2, AS/400 or HSQLDB, replace `org.opencms.db.mysql` with the corresponding database package.
