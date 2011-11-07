Change log
==========
Changes in version 1.2.2
------------------------
1.2.2 is a bug fix release.
### Fixes
* #44: Sequence fields are integers when replicating from cloudant
* #43: ensureFullCommit leaks connections
* #34: Default url overwrites host and port in spring config
* #32: HTTP errors leak StreamingJsonSerializer writer threads
* #31: String keys with double quote is not correctly encoded in view queries
* #28 "ok":true in bulk update response causes problems
* #20: Avoid runtime dependency to httpclient-cache if not using caching
* #19: avoid dependency to commons-beanutils

### Improvements
* New Android module: org.ektorp.android
* Support for consuming changes as a stream
* Support for streaming bulk operations
* #55: Move DesignDocument instance creation to a separate method
* #50: Multiple Ektorps Race Condition with Design Docs
* #51: New option for controlling the cache in ViewQuery: cacheOk
* #17: New @UpdateHandler annotation
* Support for the update_seq parameter when querying views

### Other Changes
* Jackson Version changed to 1.8.6
* Cached queries are now disabled by default.

Changes in version 1.2.1
------------------------
1.2.1 is a minor bug fix release.

### Fixes
* #18: nullpointer exception in ChangesCommand.Builder.merge

Changes in version 1.2.0
------------------------
Besides bugfixes and increased stability, Ektorp 1.2.0 brings compatibility with CouchDB 1.1 and Jackson 1.8.
New features are transparent caching of http responses from CouchDB through conditional GETs, a Spring XML configuration namespace and support for easily paging view results.

### Improvements
* Transparent cache (requires no code changes besides configuring the cache).
* Spring XML configuration namespace
* Paging view results
* Support for calling update handlers.
* Support for stale=update_after query option in CouchDB 1.1.0
* Added method ensureFullCommit() to CouchDbConnector
* Added support for purging deleted docs.
* Custom design document names for repositories
* Map and reduce functions can now be loaded from separate source files in the classpath. (Write functions in pure javascript files).
* New efficient checkIfDbExists() method in CouchDbInstance

### Fixes
* GC#98: Blocking queue in ContinuousChangesFeed is not blocking and cause out of memory on huge changes 
* GC#103: Exception during bulk operation
* GC#105: Viewresult creates inital array with the size of the whole view
* GC#108: sourceLastSequence not integer when replicating from cloudant
* GC#113: ChangesCommand does not manage "limit" parameter
* GC#115  'Unrecognized field "digest"' when fetching docs w/ attachments in couchdb 1.1
* GC#116: URI.toString() changes path value
* GC#117: Map definition in external view file should be allowed to contain newlines (mac/unix/windows)
* GC#118: Ektorp 1.1.1 does not work with Jackson 1.8.3

### Contributors
Benoit Decherf

Henrik Lundgren

Ragnar Rova

Sverre Kristian Valskrå

all defect reporters

Changes in version 1.1.1
------------------------
1.1.1 is just a minor bug fix release.

### Fixes
* #94: Ektorp 1.1.0 not working on Android with SSL & other SSL problem
* #96: org.ektorp.spring.HttpClientFactoryBean? doesn't support SSL-related properties
* #97: Ektorp gets Logger with runtime class as an argument.

Changes in version 1.1.0
------------------------
### Improvements
* New ability to model a collection as external documents with transparent lazy loading
* Added support for the changes API in CouchDB
* Support for accessing design doc info
* Added the ability to specify query parameters for list functions
* SSL/TLS connections
* Support for loading documents with conflict info attached
* Better view generation through the new @TypeDiscriminator? annotation
* Support for auto generation of the 'all' view

### Fixes
* #72: Cannot write complex view in external view.json file
* #76: Option to disable connection test at bean creation tim
* #79: Timeout when iterating through multiple DBs
* #81: HTTP request not always retried when idle HTTP connection is closed by server
* #84: Socket timeout doesn't work
* #86: Server error response masked by NullPointerException?
* #89: ComplexKey? ObjectMapper? config differs from StdCouchDbConnector?
* #90: Generated views return documents out of scope of the 'all' view.
* #92: revision id in UpdateConflictException? of SdtCouchDbConnector?.update

### Breaking Changes
* A constructor in org.ektorp.impl.StdCouchDbConnector? has changed signature.
* A constructor in org.ektorp.impl.StdCouchDbInstance? has changed signature.

### Contributors
Henrik Lundgren

Ragnar Rova (external document references)

Pasi Eronen (http connection debugging)

All defect reporters

Changes in version 1.0.2
------------------------
### Improvements
* Added support for replication.
* Removed the requirement for mapped Pojos to be annotated with @JsonSerialize?(include = Inclusion.NON_NULL)
* Support for filter, lists and show functions through new annotations.
* Accessor added for doc field in ViewResult?
* Accessor added for total number of rows in ViewResult?
* Added support for open content types through new class org.ektorp.support.OpenCouchDbDocument?
* Added support for setting revision limit for a database

### Fixes
* #54: StdCouchDbConnector?.create() doesn't work for ObjectNodes?
* #59: StdHttpClient? attempts un-authenticated request every time despite of credentials being provided
* #60: Non-existing document IDs break ViewQuery?.keys()
* #69: local_id is not marked as ignorable in ResponseStatus? class exception.

Changes in version 1.0.1
------------------------
* Fixed defect #50: View result cannot be loaded from all_docs views
* Fixed defect #53: The 'language' field is missing from design documents created using CouchDbRepositorySupport?
* Fixed defect #55: org.ektorp.util.DocumentAccessor? is not public
* DesignDocument? is now properly serialized when SerializationConfig?.Feature.AUTO_DETECT_GETTERS is disabled

Changes in version 1.0.0
------------------------
* Support for bulk operations
* Support for complex keys
* Support for multiple keys
* Support for compacting operations
* View definitions can be loaded from classpath
* Support for multiple @View annotations on Types
* Property access for id and rev fields in persitent classes.
* Performance improvements when loading result from view queries
* Support for accessing list functions
* Upgraded to Jackson 1.6.0
* Source is now JDK 5 compatible
* Fixed defect #49: Connection leak when requesting non existing resources through getAsStream or getAttachment
* Fixed defect #48: Using allDocs() in ViewQuery? can cause IllegalStateException?
* Fixed defect #38: delete(Object o) at StdCouchDbConnector? produces DocumentNotFound?
* Fixed defect #28: Better error reporting during attachments creation

Changes in version 0.9.4
------------------------
* New @View annotation for embedding view definitions in repository classes.
* New @GenerateView? annotation for automatically generate view definitions for finder methods in repository classes.
* New Spring support module, with factory bean for HttpClient?, Retry advice and an InitialDataLoader?.
* Document ids are now URL-encoded when needed.
* Map can now be used as a document class
* Fixed defect #10: serialization format for java.util.Date and org.joda.time.DateTime?
* CouchDbConnector? and CouchDbInstance? can now be configured with an external Jackson ObjectMapper?

Changes in version 0.9.2
------------------------
* persistent classes no longer have to extend CouchDbDocument?
* upgraded to Http Components Client 4.0.1
* upgraded to jackson 1.5.1
* ViewQuery? now URL-encodes key, startKey and endKey parameters
* added support for basic authentication
* moved support classes to separate org.ektorp.support
* operations in CouchDb? that return revision now returns it in Ektorp aswell
* added getters for key, startKey and endKey equals and hashCode overrides all changes made to simply unit testing queries
* getAll use getAllDocIds now filters design docs if no "all" view is defined