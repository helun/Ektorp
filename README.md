Ektorp is a persistence API that uses [CouchDB](http://couchdb.apache.org/) as storage engine. The goal of Ektorp is to combine JPA like functionality with the simplicity and flexibility that CouchDB provides.

Features
--------
Here are some good reasons why you should consider to use Ektorp in your project:

* *Rich domain models.* With powerful JSON-object mapping provided by Jackson it is easy to create rich domain models.
* *Schemaless comfort.* As CouchDB is schemaless, the database gets out of the way during application development. With a schemaless database, most adjustments to the database become transparent and automatic.
* *Out-of-the-Box CRUD.* The generic repository support makes it trivial to create persistence classes.
* *Simple and fluent API.*
* *Spring Support.* Ektorp features an optional spring support module.
* *Active development.* Ektorp is actively developed and has a growing community.
* *Choice of abstraction level.* From full object-document mapping to raw streams, Ektorp will never stop you if you need to step down an abstraction level.

Simple API
----------
It is very easy to get started with Ektorp:

	HttpClient httpClient = new StdHttpClient.Builder()
                                    .url("http://localhost:5984")
                                    .build();

	CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
	CouchDbConnector db = new StdCouchDbConnector("mydatabase", dbInstance);

	db.createDatabaseIfNotExists();

	Sofa sofa = db.get(Sofa.class, "ektorp");
	sofa.setColor("blue");
	db.update(sofa);

Out-of-the-Box CRUD
-------------------
Ektorp features a generic repository support class. It provides all Create, Read, Update and Delete operations for a persistent class.

Here's how a SofaRepository implemented with the generic repository looks like

	public class SofaRepository extends CouchDbRepositorySupport<Sofa> {

		public SofaRepository(CouchDbConnector db) {
			super(Sofa.class, db);
		}
		
	}

This repository will have the following methods "out of the box":

	SofaRepository repo = new SofaRepository(db);
			
	repo.add(Sofa s);
	repo.contains("doc_id");
	Sofa sofa = repo.get("doc_id");
	repo.update(Sofa s);
	repo.remove(Sofa s);
	List<Sofa> repo.getAll();

Convenient Managent of View Definitions
---------------------------------------
The concept of views in CouchDB can be a little daunting at first and there will always be the task of managing view definitions to go along your mapped classes.
Ektorp provides two solutions for this:

Embedded View Definitions
-------------------------
It is possible to embed view definitions in your repository classes through a @View annotation:

	@View( name="complicated_view", file = "complicated_view.json")
	public class BlogPostRepository extends CouchDbRepositorySupport<BlogPost> {

        @Autowired
        public BlogPostRepository(@Qualifier("blogPostDatabase") CouchDbConnector db) {
                super(BlogPost.class, db);
                initStandardDesignDocument();
        }

        @Override
        @View( name="all", map = "function(doc) { if (doc.title) { emit(doc.dateCreated, doc._id) } }")
        public List<BlogPost> getAll() {
                ViewQuery q = createQuery("all").descending(true);
                return db.queryView(q, BlogPost.class);
        }
        
        @GenerateView
        public List<BlogPost> findByTag(String tag) {
                return queryView("by_tag", tag);
        }

	}


Automatic view generation for finder methods
--------------------------------------------
Finder methods annotated with @GenerateView will have their view definitions automatically created.
CouchDbRepositorySupport will generate a "by_tag" view in CouchDB at application start up for the method "findByTag" in the example above.

Simple and Powerful JSON / Object Mapping
-----------------------------------------
The JSON / Object mapping in Ektorp is handled by the excellent [http://wiki.fasterxml.com/JacksonHome Jackson JSON library].

Jackson makes it easy to map the common cases and provides for instance the possibility to map polymorph types for more advanced use cases.

All persistent objects managed by Ektorp need define properties for id and revision and they need to be accessible by getters and setters.

Here's an trivial example class:


	import org.codehaus.jackson.annotate.*;

	@JsonWriteNullProperties(false)
	@JsonIgnoreProperties({"id", "revision"})
	public class Sofa {

		@JsonProperty("_id")
		private String id;

	        @JsonProperty("_rev")
		private String revision;

		private String color;
	
	        public String setId(String s) {
			id = s;
		}

		public String getId() {
			return id;
		}

		public String getRevision() {
			return rev;
		}
	
		public void setColor(String s) {
			color = s;
		}
	
		public String getColor() {
			return color;
		}
	}

Querying Views
--------------
There are several methods for querying CouchDB [views](http://wiki.apache.org/couchdb/Introduction_to_CouchDB_views) from Ektorp.

Query for Objects
-----------------
If the view's result value field is a document, Ektorp can load the result as a List of Objects

	ViewQuery query = new ViewQuery()
                     .designDocId("_design/Sofa")
                     .viewName("by_color")
                     .key("red");
		
	List<Sofa> redSofas = db.queryView(query, Sofa.class);

Scalar queries
--------------
It is possible to query for scalar values. Currently just String and int values are supported.

	ViewQuery query = new ViewQuery()
          .designDocId("_design/somedoc")
          .viewName("some_view_name");
		
	ViewResult result = db.queryView(query);
	for (ViewResult.Row row : result.getRows()) {
    	String stringValue = row.getValue();
    	int intValue = row.getValueAsInt();
	}

It is of course possible to parse a string value as JSON.
View Result as Raw JSON Stream
------------------------------
The most flexible method is query for stream. The result is returned as a stream.

	ViewQuery query = new ViewQuery()
          .designDocId("_design/somedoc")
          .viewName("view_with_huge_result");

	InputStream data = db.queryForStream(query);
	...
	data.close();

Try it Out
------------
[Download binaries here](https://github.com/helun/Ektorp/downloads)

If you are using Maven:

    <dependency>
        <groupId>org.ektorp</groupId>
        <artifactId>org.ektorp</artifactId>
        <version>1.2.1</version>
    </dependency>

Getting Help
------------
You can usually get quick answers at the [Ektorp google group](http://groups.google.com/group/ektorp-discuss)