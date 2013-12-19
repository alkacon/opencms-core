`Version 1.0 (12/2013)`


# Abstract #

After searching with Apache's Lucene for years Apache Solr has grown and grown and can now be called an enterprise search platform that is based on Lucene. It is a standalone enterprise search server with a REST-like API. You put documents in it (called "indexing") via XML, JSON or binary over HTTP. You query it via HTTP GET and receive XML, JSON, or binary results. To get a more detailed knowledge what Solr exactly is and how it works, please visit the [Apache Solr](http://lucene.apache.org/solr/)  project website. Searching with the powerful and flexible Apache Solr's REST-like interface will drill down the development complexity. More over you can rely on existing graphical interfaces that provide comfortable AJAX based search functionality to the end user of your internet/intranet application. 

Since version 8.5 OpenCms integrates Apache Solr. This document will give you a brief introduction on the Solr/OpenCms integration details. It uses links referring a locally installed OpenCms verion >= 8.5. Assuming you run OpenCms on localhost:8080 and the OpenCmsServlet is reachable under http://localhost:8080/opencms/opencms you can click the links and the examples will open.

# Searching for Content in OpenCms #
*OpenCms 8.5 integrates Apache Solr. And not only for full text search, but as a powerful enterprise search platform as well.*


## Full featured faceted search based on Solr ##

The OpenCms standard distribution covers a full featured search demo, that shows

- Faceted search
- Auto completion
- Spellchecking
- Pagination
- Sorting
- Share results
- and more ...

Click here to open the [full featured faceted search based on Solr](http://localhost:8080/opencms/opencms/demo/search-page/).


## Retrieve OpenCms content via HTTP endpoint ##

Imagine you want search for "OpenCms" in all articles, that have been changed within the last week and sort the results by modification date:

<pre>
http://localhost:8080/opencms/opencms/handleSolrSelect 
                                         // URL of the Solr HTTP endpoint
    ?q=OpenCms                           // Search for the word 'OpenCms'
    &fq=type:bs-blog                     // Restrict the results by type
    &fq=lastmodified:[NOW-7DAY TO NOW]   // Filter query on the field lastmodified with a range of seven days
    &sort=lastmodified desc              // Sort the result by date beginning with the newest one
</pre>


## Pass any Solr query to the Solr select request handler ##

As parameter of the new OpenCms Solr request handler (handleSolrSelect) you can pass any "Solr valid" input parameters.

To get familiar with the Solr query syntax have a look at [Solr query syntax](https://cwiki.apache.org/confluence/display/solr/Query+Syntax+and+Parsing). OpenCms uses the [edismax](https://cwiki.apache.org/confluence/display/solr/The+Extended+DisMax+Query+Parser) query parser as default. For advanced query syntax features the [Solr Reference Guide](https://cwiki.apache.org/confluence/display/solr/Searching) will lend a hand.

Please note that many characters in the Solr Query Syntax (most notable the plus sign: "+") are special characters in URLs, so when constructing request URLs manually, you must properly URL-Encode these characters.
<pre>
                                                          q=  +popularity:[10   TO   *]     +section:0
   http://localhost:8080/opencms/opencms/handleSolrSelect?q=%2Bpopularity:[10%20TO%20*]%20%2Bsection:0
</pre>

For more information, see Yonik Seeley's blog on [Nested Queries in Solr](http://www.lucidimagination.com/blog/2009/03/31/nested-queries-in-solr/).


## Handle the response ##

The response produced by OpenCms/Solr can be XML or JSON. With an additional parameter 'wt' you can specify the [QueryResponseWriter](http://wiki.apache.org/solr/QueryResponseWriter) that should be used by Solr. For the above shown query example a result can look like this:

```xml
<response>
  <lst name="responseHeader">
    <int name="status">0</int>
    <int name="QTime">7</int>
    <lst name="params">
      <str name="qt">dismax</str>
      <str name="fl">*,score</str>
      <int name="rows">50</int>
      <str name="q">*:*</str>
      <arr name="fq">
        <str>type:v8article</str>
        <str>contentdate:[NOW-1DAY TO NOW]</str>
        <str>Title_prop:Flower</str>
      </arr>
      <long name="start">0</long>
    </lst>
  </lst>
  <result name="response" numFound="2" start="0">
    <doc>
      <str name="id">51041618-77f5-11e0-be13-000c2972a6a4</str>
      <str name="contentblob">[B:[B@6c1cb5</str>
      <str name="path">/sites/default/.content/article/a_00003.html</str>
      <str name="type">v8article</str>
      <str name="suffix">.html</str>
      <date name="created">2011-05-06T15:27:13Z</date>
      <date name="lastmodified">2011-08-17T13:58:29Z</date>
      <date name="contentdate">2012-09-03T10:41:13.56Z</date>
      <date name="relased">1970-01-01T00:00:00Z</date>
      <date name="expired">292278994-08-17T07:12:55.807Z</date>
      <arr name="res_locales">
        <str>en</str>
        <str>de</str>
      </arr>
      <arr name="con_locales">
        <str>en</str>
        <str>de</str>
      </arr>
      <str name="template_prop">/system/modules/com.alkacon.opencms.v8.template3/templates/main.jsp</str>
      <str name="style.layout_prop">/.content/style</str>
      <str name="NavText_prop">OpenCms 8 Demo</str>
      <str name="Title_prop">Flower Today</str>
      <str name="ahtml_de_t">Nachfolgend finden Sie aktuelle Meldungen und Veranstaltungen rund um die Blume.</str>
      <str name="ahtml_en_t">In this section, you find current flower related news and events.</str>
      <arr name="content_en">
        <str>News from the world of flowers you find current flower related news and events.</str>
      </arr>
      <arr name="content_de">
        <str>Neuigkeiten aus der Welt der Blumen Blume aktuell Nachfolgend [...]</str>
      </arr>
      <date name="timestamp">2012-09-03T10:45:47.055Z</date>
      <float name="score">1.0</float>
    </doc>
    <doc>
      <str name="id">ac56418f-77fd-11e0-be13-000c2972a6a4</str>
      <str name="contentblob">[B:[B@1d0e4a2</str>
      <str name="path">/sites/default/.content/article/a_00030.html</str>
      <str name="type">v8article</str>
      <str name="suffix">.html</str>
      <date name="created">2011-05-06T16:27:02Z</date>
      <date name="lastmodified">2011-08-17T14:03:27Z</date>
      <date name="contentdate">2012-09-03T10:41:18.155Z</date>
      <date name="relased">1970-01-01T00:00:00Z</date>
      <date name="expired">292278994-08-17T07:12:55.807Z</date>
      <arr name="res_locales">
        <str>en</str>
        <str>de</str>
      </arr>
      <arr name="con_locales">
        <str>en</str>
        <str>de</str>
      </arr>
      <str name="template_prop">/system/modules/com.alkacon.opencms.v8.template3/templates/main.jsp</str>
      <str name="style.layout_prop">/.content/style</str>
      <str name="NavText_prop">OpenCms 8 Demo</str>
      <str name="Title_prop">Flower Dictionary</str>
      <str name="ahtml_de_t">In der Botanik existieren zahlreiche Gewächsfamilien [...]</str>
      <str name="ahtml_en_t">There are many different types of plants [...]</str>
      <arr name="content_en">
        <str>The different types of flowers Flower Dictionary of plants and flowers [...]</str>
      </arr>
      <arr name="content_de">
        <str>Die verschiedenen Gewächsfamilien Blumen Lexikon In der Botanik existieren zahlreiche [...]</str>
      </arr>
      <date name="timestamp">2012-09-03T10:45:49.265Z</date>
      <float name="score">1.0</float>
    </doc>
  </result>
</response>
```

## Send a Java-API query ##
```java
  String query = "fq=type:v8article&fq=lastmodified:[NOW-1DAY TO NOW]&fq=Title_prop:Flower";
  CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr("Solr Online Index");
  CmsSolrResultList results = index.search(getCmsObject(), query);
  for (CmsSearchResource sResource : results) {
    String path = sResource.getField(I_CmsSearchField.FIELD_PATH);
    Date date = sResource.getMultivaluedField(I_CmsSearchField.FIELD_DATE_LASTMODIFIED);
    List<String> cats = sResource.getMultivaluedField(I_CmsSearchField.FIELD_CATEGORY);
  }
```

The class org.opencms.search.solr.CmsSolrResultList encapsulates a list of 'OpenCms resource documents' ({@link CmsSearchResource}). This list can be accessed exactly like an {@link ArrayList} which entries are {@link CmsSearchResource} that extend {@link CmsResource} and holds the Solr implementation of {@link I_CmsSearchDocument} as member. **This enables you to deal with the resulting list as you do with well known {@link List} and work on it's entries like you do on {@link CmsResource}**.


## Use CmsSolrQuery class for querying Solr ##

```java
  CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr("Solr Online Index");
  CmsSolrQuery squery = new CmsSolrQuery(getCmsObject(), "path:/sites/default/xmlcontent/article_0001.html");
  List<CmsResource> results = index.search(getCmsObject(), squery);
```

## Advanced search features ##

- [Faceted search](http://wiki.apache.org/solr/SimpleFacetParameters)
- [Highlighting](http://wiki.apache.org/solr/HighlightingParameters)
- [Range queries](http://wiki.apache.org/solr/SolrQuerySyntax)
- [Sorting](http://wiki.apache.org/solr/CommonQueryParameters)
- [Spellchecking](http://wiki.apache.org/solr/SpellCheckComponent)
- [Auto suggestion/completion/correction](http://wiki.apache.org/solr/Suggester)
- [Thesaurus/Synonyms](http://wiki.apache.org/solr/AnalyzersTokenizersTokenFilters)


## Querying multiple cores (indexes) ##

'Core' is the wording in the Solr world for thinking of several indexes. Preferring the correct speech, let's say core instead index. Multiple cores should only be required if you have completely different applications but want a single Solr Server that manages all the data. See [Solr Core Administration](http://wiki.apache.org/solr/CoreAdmin) for detailed information. So assuming you have configured multiple Solr cores and you would like to query a specific one you have to tell Solr/OpenCms which core/index you want to search on. This is done by a special parameter:

<pre>
http://localhost:8080/opencms/opencms/handleSolrSelect?   
                              // The URI of the OpenCms Solr Select Handler configured in 'opencms-system.xml'
    &core=My Solr Index Name  // Searches on the core with the name 'My Solr Index Name'
    &q=content_en:Flower      // for the text 'Flower'
</pre>


## Using the standard OpenCms Solr collector ##

OpenCms delivers a standard Solr collector using <tt>byQuery</tt> as name to simply pass a query string and <tt>byContext</tt> as name to pass a query String and let OpenCms use the user's request context. The implementing class for this collector can be found at <tt>org.opencms.file.collectors.CmsSolrCollector</tt>.

```jsp
<cms:contentload
  collector="byQuery" 
  param="q=+parent-folders:/sites/default/ +type:ddarticle
        &rows=4
        &start=7
        &type=dismax
        &fl=*,score
        &sort=lastmodified desc" 
  preload="true">
    <cms:contentinfo var="info" />
    <c:if test='${info.resultSize != 0}'>
        <cms:contentinfo var="info" />			
        <c:if test='${info.resultSize != 0}'>
            <h3>Solr Collector Demo</h3>
            <cms:contentload editable="false">
                <cms:contentaccess var="content" />
                <%-- Title of the article --%>
                <h6>${content.value.Title}</h6>
                <%-- The text field of the article with image --%>
                <div class="paragraph">
                    <%-- Set the requied variables for the image. --%>
                    <c:if test="${content.value.Image.isSet}">								
                        <%-- Output of the image using cms:img tag --%>				
                        <c:set var="imgwidth">${(cms.container.width - 20) / 3}</c:set>
                        <%-- The image is scaled to the one third of the container width. --%>
                        <cms:img
                              src="${content.value.Image}" 
                              width="${imgwidth}"
                              scaleColor="transparent"
                              scaleType="0" 
                              cssclass="left" 
                              alt="${content.value.Image}" 
                              title="${content.value.Image}" />
                    </c:if>									
                    ${cms:trimToSize(cms:stripHtml(content.value.Text), 300)}
                </div>
                <div class="clear"></div>
            </cms:contentload>
        </c:if>
    </c:if>
</cms:contentload>
```




# Indexing content of OpenCms #

The OpenCms search/index configuration is done in the file **<tt>'opencms-search.xml'</tt>** (<tt><CATALINA_HOME>/webapps/&lt;OPENCMS_WEBAPP&gt;/WEB_INF/config/opencms-search.xml</tt>). The following section will explain the OpenCms specific configuration options.

But before going into details, let's say some words about the OpenCms-Index-Strategy in general. In previous days  a typical approach was to create multiple Lucene indexes per use cases. For example if you managed multiple sites or languag versions within a single OpenCms instance one would have created an index per site/language/use-case. Such an index contained only those documents/resources that were releated to that site/language/use-case. Now a days the approach is to index all data (accross all sites and languages or use-cases) in one big index. 

**Having all resources in one big index**

- reduces expense for pushing same data into several indexes,
- reduces computational effort during the indexing process and
- moves responsibilities from the index time to the search time.


### Embedded Solr Server ###

A optional node <tt>solr (XPath: opencms/search/solr)</tt> is available. To simply enable the embedded Solr Server the <tt>opencms-search.xml</tt> should start like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE opencms SYSTEM "http://www.opencms.org/dtd/6.0/opencms-search.dtd">
<opencms>
  <search>
    <solr enabled="true"/>

      [...]

  </search>
</opencms>
```

Optionally you can configure the Solr home directory and the name of the main Solr configuration file **<tt>solr.xml</tt>**. OpenCms then concats those two paths: **<tt>{solr_home}{configfile}</tt>**

**Example:**

```xml
<solr enabled="true">
    <home>/my/solr/home/folder</home>
    <configfile>rabbit.xml</configfile>
</solr>
```

In order to disable Solr system wide remove the **<tt>solr-node</tt>** or set the enabled attribute to <tt>'false'</tt> like:

```xml
<solr enabled="false"/>
```


### External Solr Server ###

It is also possible to make use of an external HTTP Solr server, to do so, replace the line 
```xml
<solr enabled="true"/>
```
with the following:
```xml
<solr enabled="true" serverUrl="http://yourSolrServer" />
```

The OpenCms SolrSelect request handler does not support the external HTTP Solr Server. So if your HTTP Solr Server is directly reachable by **<tt>http://{yourSolrServer}</tt>** there will no permission check performed and indexed data that is secret will be accessible. What means that you are **self-responsible** for resources that have permission restrictions set on the VFS of OpenCms. But of course you can use the method 

**<tt>org.opencms.search.solr.CmsSolrIndex.search(CmsObject, SolrQuery)</tt>** or 

**<tt>org.opencms.search.solr.CmsSolrIndex.search(CmsObject, String)</tt>**

and be sure permissions are checked also for HTTP Solr Servers. Maybe a future version of OpenCms will feature a secure access on HTTP Solr server.


### Configuring Solr search indexes ###

By default OpenCms comes along with a "Solr Online" and a "Solr Offline" index. To add a new Solr index you can use the default configuration as copy template.

```xml
<index class="org.opencms.search.solr.CmsSolrIndex">
  <name>Solr Online</name>
  <rebuild>auto</rebuild>
  <project>Online</project>
  <locale>all</locale>
  <configuration>solr_fields</configuration>
  <sources>
    <source>solr_source</source>
  </sources>
</index>
```


### Index sources ###

Index sources for Solr can be configured in the file **<tt>opencms-search.xml</tt>** exactly the same way as you do for Lucene indexes. In order to use the advanced XSD field mapping for XML contents, you must add the new document type **<tt>xmlcontent-solr</tt>** to the list of document types that are indexed:

```xml
<indexsource>
  <name>solr_source</name>
  <indexer class="org.opencms.search.CmsVfsIndexer"/>
  <resources>
    <resource>/sites/default/</resource>
  </resources>
  <documenttypes-indexed>
    <name>xmlcontent-solr</name>
    <name>containerpage</name>
    <name>xmlpage</name>
    <name>text</name>
    <name>pdf</name>
    <name>image</name>
    <name>msoffice-ole2</name>
    <name>msoffice-ooxml</name>
    <name>openoffice</name>
  </documenttypes-indexed>
</indexsource>
```


### Solr XML document types ###

A special document type called **<tt>xmlcontent-solr</tt>** implemented in **<tt>CmsSolrDocumentXmlContent</tt>** performs a localized content extraction that is later used to fill the Solr input documents. As explained in section "Custom fields for XML contents" it is possible to define a mapping between elements defined in the XSD of an XML resource type and a field of the Solr document. The values for those defined XSD field mappings are also extracted by the document type named **<tt>xmlcontent-solr</tt>**. More over there is another Solr related document type doing the extraction for container pages: **<tt>containerpage-solr</tt>**.

```xml
<documenttype>
  <name>xmlcontent-solr</name>
  <class>org.opencms.search.solr.CmsSolrDocumentXmlContent</class>
  <mimetypes>
    <mimetype>text/html</mimetype>
  </mimetypes>
  <resourcetypes>
    <resourcetype>xmlcontent-solr</resourcetype>
  </resourcetypes>
</documenttype>
```

### The Solr default field configuration ###

By default the field configuration for OpenCms Solr indexes is implemented by the class **<tt>org.opencms.search.solr.CmsSolrFieldConfiguration</tt>**. The Solr field configuration declared in **<tt>opencms-search.xml</tt>** looks like the following.

```xml
<fieldconfiguration class="org.opencms.search.solr.CmsSolrFieldConfiguration">
  <name>solr_fields</name>
  <description>The Solr search index field configuration.</description>
  <fields/>
</fieldconfiguration>
```

### Migrating a Lucene index to a Solr index ###

An existing Lucene field configuration can easily be transformed into a Solr index. To do so create a new Solr field configuration. Therefore you can use the snippet shown in the sction above as template and copy the list of fields from the Lucene index you want to convert into that skeleton.

There exists a specific strategy to map the Lucene field names to Solr field names:

* **Exact name matching:** OpenCms tries to determine an explicit Solr field that has the exact name like the value of the name-attribute. E.g. OpenCms tries to find an explicit Solr filed definition named **<tt>meta</tt>** for **<tt>&lt;field name="meta"&gt; ... &lt;/field&gt;</tt>**. To make use of this strategy you have to edit the **<tt>schema.xml</tt>** of Solr manually and add an explicit field definition named according to the exact Lucene field names.

* **Type specific fields:** In the existing Lucene configuration type specific field definitions are not designated, but the Solr **<tt>schema.xml</tt>** defines different data types for fields. If you are interested in making use of those type specific advantages (like language specific field analyzing/tokenizing) without manipulating the **<tt>schema.xml</tt>** of Solr, you have to define a type attribute for those fields at least. The value of the attribute **<tt>type</tt>** can be any name of each **<tt>&lt;dynamicField&gt;</tt>** configured in the **<tt>schema.xml</tt>** that starts with a **<tt>*&#95;</tt>**. The resulting field inside the Solr document is then named **<tt>&lt;luceneFieldName&gt;_&lt;dynamicFieldSuffix&gt;</tt>**.

* **Fallback:** If you don't have defined a type attribute and there does not exist an explicit field in the **<tt>schema.xml</tt>** named according to the Lucene field name OpenCms uses **<tt>text_general</tt>** as fallback. E.g. a Lucene field **<tt>&lt;field name="title" index="true"&gt; ... &lt;/field&gt;</tt>** will be stored as a dynamic field named **<tt>title&#95;txt</tt>** in the Solr index.

An originally field configuration like:

```xml
      <fieldconfiguration>
        <name>standard</name>
        <description>The standard OpenCms 8.0 search index field configuration.</description>
        <fields>
          <field name="content" display="%(key.field.content)" store="compress" index="true" excerpt="true">
            <mapping type="content"/>
          </field>
          <field name="title-key" display="-" store="true" index="untokenized" boost="0.0">
            <mapping type="property">Title</mapping>
          </field>
          <field name="title" display="%(key.field.title)" store="false" index="true">
            <mapping type="property">Title</mapping>
          </field>
          <field name="keywords" display="%(key.field.keywords)" store="true" index="true">
            <mapping type="property">Keywords</mapping>
          </field>
          <field name="description" display="%(key.field.description)" store="true" index="true">
            <mapping type="property">Description</mapping>
          </field>
          <field name="meta" display="%(key.field.meta)" store="false" index="true">
            <mapping type="property">Title</mapping>
            <mapping type="property">Keywords</mapping>
            <mapping type="property">Description</mapping>
          </field>
        </fields>
      </fieldconfiguration>
```

Could look after the conversion like this:

```xml
      <fieldconfiguration class="org.opencms.search.solr.CmsSolrFieldConfiguration">
        <name>standard</name>
        <description>The standard OpenCms 8.0 Solr search index field configuration.</description>
        <fields>
          <field name="content" display="%(key.field.content)" store="compress" index="true" excerpt="true">
            <mapping type="content"/>
          </field>
          <field name="title-key" display="-" store="true" index="untokenized" boost="0.0" type="s">
            <mapping type="property">Title</mapping>
          </field>
          <field name="title" display="%(key.field.title)" store="false" index="true" type="prop">
            <mapping type="property">Title</mapping>
          </field>
          <field name="keywords" display="%(key.field.keywords)" store="true" index="true" type="prop">
            <mapping type="property">Keywords</mapping>
          </field>
          <field name="description" display="%(key.field.description)" store="true" index="true" type="prop">
            <mapping type="property">Description</mapping>
          </field>
          <field name="meta" display="%(key.field.meta)" store="false" index="true" type="en">
            <mapping type="property">Title</mapping>
            <mapping type="property">Keywords</mapping>
            <mapping type="property">Description</mapping>
          </field>
        </fields>
      </fieldconfiguration>
```

## Indexed data ##

The following sections will show what data is indexed by default and what possibilities are offered by OpenCms to configure / implement additional field configurations / mappings.

### The Solr index schema (schema.xml) ###

Have a look at the Solr **<tt>schema.xml</tt>** first. In the file **<tt>&lt;CATALINA&#95;HOME&gt;/webapps/&lt;MOPENCMS&gt;/WEB-INF/solr/conf/schema.xml</tt>** you will find the field definitions that will be used by OpenCms that were briefly summarized before.

```xml
<fields>
   <field name="id"                  type="uuid"         indexed="true"  stored="true"  />
   <field name="timestamp"           type="date"         indexed="true"  stored="true"  default="NOW"/>
   <field name="path"                type="string"       indexed="true"  stored="true"  required="true" />
   <field name="path_hierarchy"      type="text_path"    indexed="true"  stored="false" required="true" />
   <field name="parent-folders"      type="string"       indexed="true"  stored="false" required="true" multiValued="true" />
   <field name="type"                type="string"       indexed="true"  stored="true"  required="true" />
   <field name="state"               type="string"       indexed="true"  stored="true" />
   <field name="userLastModified"    type="string"       indexed="true"  stored="true" />
   <field name="userCreated"         type="string"       indexed="true"  stored="true" />
   <field name="version"             type="int"          indexed="true"  stored="true" />
   <field name="filename"            type="string"       indexed="true"  stored="true" />
   <field name="search_exclude"      type="boolean"      indexed="true"  stored="true" />
   <field name="search_channel"      type="string"       indexed="true"  stored="true"  multiValued="true" />
   <field name="mimetype"            type="string"       indexed="true"  stored="true" />
   <field name="container_types"     type="string"       indexed="true"  stored="true" />
   <field name="suffix"              type="string"       indexed="true"  stored="true" />
   <field name="size"                type="int"          indexed="true"  stored="true"  required="true" />
   <field name="res_locales"         type="string"       indexed="true"  stored="true"  required="true" multiValued="true" />
   <field name="con_locales"         type="string"       indexed="true"  stored="true"  required="true" multiValued="true" />
   <field name="contentdate"         type="date"         indexed="true"  stored="true"  required="true" />
   <field name="created"             type="date"         indexed="true"  stored="true"  required="true" />
   <field name="lastmodified"        type="date"         indexed="true"  stored="true"  required="true" />
   <field name="expired"             type="date"         indexed="true"  stored="true"  />
   <field name="released"            type="date"         indexed="true"  stored="true"  />
   <field name="meta"                type="text_general" indexed="true"  stored="false" multiValued="true" />
   <field name="content"             type="text_general" indexed="true"  stored="false" multiValued="true" />
   <field name="contentblob"         type="binary"       indexed="false" stored="true"  />
   <field name="category"            type="text_general" indexed="true"  stored="true"  multiValued="true" />
   <field name="category_exact"      type="string"       indexed="true"  stored="false" multiValued="true" termVectors="true" />
   <field name="additional_info"     type="string"       indexed="false" stored="true" />
   <field name="dependencyType"      type="string"       indexed="true"  stored="true" />
   <field name="place"               type="location"     indexed="true"  stored="true" />
   <field name="spell"               type="spell"        indexed="true"  stored="true"  multiValued="true"/>
   <field name="text"                type="text_general" indexed="true"  stored="false" multiValued="true"/>
   <field name="text_en"             type="text_en"      indexed="true"  stored="false" multiValued="true"/>
   <field name="text_de"             type="text_de"      indexed="true"  stored="false" multiValued="true"/>
   <field name="text_el"             type="text_el"      indexed="true"  stored="false" multiValued="true"/>
   <field name="text_es"             type="text_es"      indexed="true"  stored="false" multiValued="true"/>
   <field name="text_fr"             type="text_fr"      indexed="true"  stored="false" multiValued="true"/>
   <field name="text_hu"             type="text_hu"      indexed="true"  stored="false" multiValued="true"/>
   <field name="text_it"             type="text_it"      indexed="true"  stored="false" multiValued="true"/>

   <dynamicField name="*_excerpt"    type="text_general" indexed="true"  stored="true" termVectors="on" termPositions="on" termOffsets="on" />
   <dynamicField name="*_exact"      type="string"       indexed="true"  stored="false"/>
   <dynamicField name="*_prop"       type="text_general" indexed="true"  stored="true"/>
   <dynamicField name="*_i"          type="int"          indexed="true"  stored="true"/>
   <dynamicField name="*_s"          type="string"       indexed="true"  stored="true"/>
   <dynamicField name="*_l"          type="long"         indexed="true"  stored="true"/>
   <dynamicField name="*_t"          type="text_general" indexed="true"  stored="true"/>
   <dynamicField name="*_txt"        type="text_general" indexed="true"  stored="true" multiValued="true"/>
   <dynamicField name="*_en"         type="text_en"      indexed="true"  stored="true" multiValued="true"/>
   <dynamicField name="*_de"         type="text_de"      indexed="true"  stored="true" multiValued="true"/>
   <dynamicField name="*_el"         type="text_el"      indexed="true"  stored="true" multiValued="true"/>
   <dynamicField name="*_es"         type="text_es"      indexed="true"  stored="true" multiValued="true"/>
   <dynamicField name="*_fi"         type="text_fi"      indexed="true"  stored="true" multiValued="true"/>
   <dynamicField name="*_fr"         type="text_fr"      indexed="true"  stored="true" multiValued="true"/>
   <dynamicField name="*_hu"         type="text_hu"      indexed="true"  stored="true" multiValued="true"/>
   <dynamicField name="*_it"         type="text_it"      indexed="true"  stored="true" multiValued="true"/>
   <dynamicField name="*_b"          type="boolean"      indexed="true"  stored="true"/>
   <dynamicField name="*_f"          type="float"        indexed="true"  stored="true"/>
   <dynamicField name="*_d"          type="double"       indexed="true"  stored="true"/>
   <dynamicField name="*_coordinate" type="tdouble"      indexed="true"  stored="false"/>
   <dynamicField name="*_ti"         type="tint"         indexed="true"  stored="true"/>
   <dynamicField name="*_tl"         type="tlong"        indexed="true"  stored="true"/>
   <dynamicField name="*_tf"         type="tfloat"       indexed="true"  stored="true"/>
   <dynamicField name="*_td"         type="tdouble"      indexed="true"  stored="true"/>
   <dynamicField name="*_tdt"        type="tdate"        indexed="true"  stored="true"/>
   <dynamicField name="*_dt"         type="date"         indexed="true"  stored="true"/>
   <dynamicField name="*_pi"         type="pint"         indexed="true"  stored="true"/>
   <dynamicField name="*_loc"        type="location"     indexed="true"  stored="true"/>
   <dynamicField name="attr_*"       type="text_general" indexed="true"  stored="true" multiValued="true"/>
   <dynamicField name="random_*"     type="random" />
   <dynamicField name="text_*"       type="text_general" indexed="true"  stored="true" termVectors="true" termPositions="true" termOffsets="true"/>
   <dynamicField name="dep_*"        type="string"       indexed="false" stored="true" multiValued="true"/>
 </fields>

 <copyField source="*_en"      dest="text_en"/>
 <copyField source="*_de"      dest="text_de"/>
 <copyField source="*_el"      dest="text_el"/>
 <copyField source="*_es"      dest="text_es"/>
 <copyField source="*_fi"      dest="text_fi"/>
 <copyField source="*_fr"      dest="text_fr"/>
 <copyField source="*_hu"      dest="text_hu"/>
 <copyField source="*_it"      dest="text_it"/>
 <copyField source="text_*"    dest="text"/>
 <copyField source="*_prop"    dest="text" />
 <copyField source="content"   dest="text" />
 <copyField source="category"  dest="text" />
 <copyField source="*_prop"    dest="*_exact" />
 <copyField source="path"      dest="path_hierarchy" /> 
 <copyField source="category"  dest="category_exact" />

 <copyField source="Title_prop" dest="spell"/>
 <copyField source="content_de" dest="spell"/>
 <copyField source="content_en" dest="spell"/>

 <uniqueKey>id</uniqueKey>
```

### Default index fields ###

OpenCms indexes several information for each resource by default:

* **id** - Structure id used as unique identifier for an document (The structure id of the resource)
* **path** - Full root path (The root path of the resource e.g. /sites/default/flower&#95;en/.content/article.html)
* **path&#95;hierarchy** - The full path as (path tokenized field type: text&#95;path)
* **parent-folders** - Parent folders (multi-valued field containing an entry for each parent path)
* **type** - Type name (the resource type name)
* **res&#95;locales** - Existing locale nodes for XML content and all available locales in case of binary files
* **created** - The creation date (The date when the resource itself has being created)
* **lastmodified** - The date last modified (The last modification date of the resource itself)
* **contentdate** - The content date (The date when the resource's content has been modified)
* **released** - The release and expiration date of the resource
* **content** A general content field that holds all extracted resource data (all languages, type text&#95;general)
* **contentblob** - The serialized extraction result (content&#95;blob) to improve the extraction performance while indexing
* **category** - All categories as general text
* **category&#95;exact** - All categories as exact string for faceting reasons
* **text&#95;&lt;locale&gt;** - Extracted textual content optimized for the language specific search (Default languages: en, de, el, es, fr, hu, it)
* **timestamp** - The time when the document was indexed last time
* **&#42;&#95;prop** - All properties of a resource as searchable and stored text (field name: &lt;Property&#95;Definition&#95;Name&gt;&#95;prop)
* **&#42;&#95;exact** - All properties as exact not stored string (field name: &lt;Property&#95;Definition&#95;Name&gt;&#95;exact)

### Custom fields for XML contents ###

Declarative field configuration with field mappings can also be bone via the **<tt>XSD-Content-Definition</tt>** of an XML resource type as defined in the **<tt>DefaultAppinfoTypes.xsd</tt>**

```xsd
  <xsd:complexType name="OpenCmsDefaultAppinfoSearchsetting">
    <xsd:sequence>
      <xsd:element name="solrfield" type="OpenCmsDefaultAppinfoSolrField" minOccurs="0" maxOccurs="unbounded" />
    </xsd:sequence>
    <xsd:attribute name="element" type="xsd:string" use="required" />
    <xsd:attribute name="searchcontent" type="xsd:boolean" use="optional" default="true" />
  </xsd:complexType>

  <xsd:complexType name="OpenCmsDefaultAppinfoSolrField">
    <xsd:sequence>
      <xsd:element name="mapping" type="OpenCmsDefaultAppinfoSolrFieldMapping" minOccurs="0" maxOccurs="unbounded" />
    </xsd:sequence>
    <xsd:attribute name="targetfield" type="xsd:string" use="required" />
    <xsd:attribute name="sourcefield" type="xsd:string" use="optional" />
    <xsd:attribute name="copyfields" type="xsd:string" use="optional" />
    <xsd:attribute name="locale" type="xsd:string" use="optional" />
    <xsd:attribute name="default" type="xsd:string" use="optional" />
    <xsd:attribute name="boost" type="xsd:string" use="optional" />
  </xsd:complexType>
```

You are able to declare search field mappings for XML content elements directly in the XSD Content Definition. A XSD using this feature can then look like:

```xml
  <searchsettings>
    <searchsetting element="Title" searchcontent="true">
      <solrfield targetfield="atitle">
        <mapping type="property">Author</mapping>
      </solrfield>
    </searchsetting>
    <searchsetting element="Teaser">
      <solrfield targetfield="ateaser">
        <mapping type="item" default="Homepage n.a.">Homepage</mapping>
        <mapping type="content"/>
        <mapping type="property-search">search.special</mapping>
        <mapping type="attribute">dateReleased</mapping>
        <mapping type="dynamic" class="org.opencms.search.solr.CmsDynamicDummyField">special</mapping>
      </solrfield>
    </searchsetting>
    <searchsetting element="Text" searchcontent="true">
      <solrfield targetfield="ahtml" boost="2.0"/>
    </searchsetting>
    <searchsetting element="Release" searchcontent="false">
      <solrfield targetfield="arelease" sourcefield="*_dt" />
    </searchsetting>
    <searchsetting element="Author" searchcontent="true">
      <solrfield targetfield="aauthor" locale="de" copyfields="test_text_de,test_text_en" />
    </searchsetting>
    <searchsetting element="Homepage" searchcontent="true">
      <solrfield targetfield="ahomepage" default="Homepage n.a." />
    </searchsetting>
  </searchsettings>
```

### Dynamic field mappings ###

If the requirements for the field mapping are more "dynamic" than just: **<tt>'static piece of content' -> 'specified field defined in the Solr schema'</tt>**, you are able to implement the the interface **<tt>org.opencms.search.fields.I_CmsSearchFieldMapping</tt>**.

### Custom field configuration ###

Declarative field configurations with field mappings can be defined in the file **<tt>opencms-search.xml</tt>**. You can use exactly the same features as already known for OpenCms Lucene field configurations.

### Extend the CmsSolrFieldConfiguration ###

If the standard configuration options are still not flexible enough you are able to extends from the class: **<tt>org.opencms.search.solr.CmsSolrFieldConfiguration</tt>** and define a custom Solr field configuration in the **<tt>opencms-search.xml</tt>**:

```xml
  <fieldconfiguration class="your.package.YourSolrFieldConfiguration">
    <name>solr_fields</name>
    <description>The Solr search index field configuration.</description>
    <fields/>
  </fieldconfiguration>
```




# Behind the walls #

## The request handler ##
The class org.opencms.main.OpenCmsSolrHandler offers the same functionality as the default select request handler of an standard Solr server installation. In the OpenCms default system configuration (opencms-system.xml) the Solr request handler is configured:
```xml
  <requesthandlers>
    <requesthandler class="org.opencms.main.OpenCmsSolrHandler" />
  </requesthandlers>
```

Alternativly the request handler class can be used as Servlet, therefore add the handler class to
the WEB-INF/web.xml of your OpenCms application:

```xml
  <servlet>
    <description>Zhe OpenCms Solr servlet.</description>
    <servlet-name>OpenCmsSolrServlet</servlet-name>
    <servlet-class>org.opencms.main.OpenCmsSolrHandler</servlet-class>
    <load-on-startup>1</load-on-startup>
  </servlet>
    [...]
  <servlet-mapping>
    <servlet-name>OpenCmsSolrServlet</servlet-name>
    <url-pattern>/solr/*</url-pattern>
  </servlet-mapping>
```

## Permission check ##
OpenCms performs a permission check for all resulting documents and throws those away that
the current user is not allowed to retrieve and expands the result for the next best matching
documents on the fly. This security check is very cost intensive and should be
replaced/improved with a pure index based permission check.


## Configurable post processor ##
OpenCms offers the capability for post search processing Solr documents after the document has been checked for permissions. This capability allows you to add fields to the found document before the search result is returned. In order to make use of the post processor you have to add an optional parameter for the search index as follows:

```xml
  <index class="org.opencms.search.solr.CmsSolrIndex">
    <name>Solr Offline</name>
    <rebuild>offline</rebuild>
    <project>Offline</project>
    <locale>all</locale>
    <configuration>solr_fields</configuration>
    <sources>
      [...]
    </sources>
    <param name="org.opencms.search.solr.CmsSolrIndex.postProcessor">my.package.MyPostProcessor</param>
  </index>
```

The specified class for the parameter **<tt>org.opencms.search.solr.CmsSolrIndex.postProcessor</tt>** must be an implementation of **<tt>org.opencms.search.solr.I_CmsSolrPostSearchProcessor</tt>**.

## Multilingual support ##
There is a default strategy implemented for the multi-language support within OpenCms Solr search index. For binary documents the language is determined automatically based on the extracted text. The default mechanism is implemented with: [Laguage detection](http://code.google.com/p/language-detection/)

For XML contents we have the concrete language/locale information and the localized fields are ending with underscore followed by the locale. E.g.: **<tt>content&#95;en, content&#95;de or text&#95;en, text&#95;de</tt>**. By default all the field mappings definied within the XSD of a resource type are extended by the **‘&#95;&lt;locale&gt;’**.

## Multilingual dependency resolving ##
Based on the file name of a resource in OpenCms there exists a concept to index documents that are distributed over more than one resource in OpenCms. The standard implementation can be found at: 

**<tt>org.opencms.search.documents.CmsDocumentDependency</tt>**

## Extraction result cache ##
For better index performance the extracted result is cached for siblings

**<tt>@see org.opencms.search.extractors.I_CmsExtractionResult</tt>**


# Frequently asked questions #

## How is Solr integrated in general? ##

Independent from OpenCms a standard Solr Server offers a HTTP-Interface that is reachable
at: [Standard Solr Server URL](http://localhost:8983/solr/select). In order to query a Solr server you can attach each valid Solr query documented at: [Solr query syntax](http://wiki.apache.org/solr/SolrQuerySyntax) to this URL. The HTTP response can either be JSON or XML and the answer of the query
http://localhost:8983/solr/select?q=*:*&rows=2 could look like:

```xml
  <response>
    <lst name="responseHeader">
      <int name="status">0</int>
      <int name="QTime">32</int>
      <lst name="params">
        <str name="q">*:*</str>
        <int name="rows">2</int>
        <long name="start">0</long>
     </lst>
     <result name="response" numFound="139" start="0">
       <doc>...</doc>
       <doc>...</doc>
     </result>
  </response>
```

Solr is implemented in Java and there exists an Apache library called solrj that enables to access a running Solr server by writing native Java code against this API. The Solr integration in OpenCms offers both Interfaces: Java and HTTP. The default URL for request Solr responses from OpenCms is:

**<tt>http://localhost:8080/opencms/opencms/handleSolrSelect</tt>**

this handler can answer any syntactically correct Solr query.

The following code shows a simple example how to use the OpenCms Java API to send a Solr query:

```jsp
//////////////////
// SEARCH START //
//////////////////

CmsObject cmsO = new CmsJspActionElement(pageContext, request, response).getCmsObject();

String query = ((request.getParameter("query") != null && request.getParameter("query") != "") 
				? "q=" + request.getParameter("query") : "")
                + "&fq=type:ddarticle&sort=path asc&rows=5";

CmsSolrResultList hits = OpenCms.getSearchManager().getIndexSolr("Solr Offline").search(cmsO, query);
if (hits.size() > 0) { %>
  <h4>New way: <fmt:message key="v8.solr.results" />
    <%= hits.getNumFound() %> found / rows <%= hits.getRows() %>
  </h4>

  <div class="boxbody"><%
    //////////////////
    // RESULTS LOOP //
    //////////////////
    for (CmsSearchResource resource : hits) { %>
      <div class="boxbody_listentry">
        <div class="twocols">
          <div>Path: <strong><%= resource.getRootPath() %></strong></div>
          <div>German: <strong><%= resource.getDocument().getFieldValueAsString("Title_de")%></strong></div>
          <div>English: <strong><%= resource.getDocument().getFieldValueAsString("Title_en")%></strong></div>
        </div>
      </div> 
    <% } %>
  </div>

<% } %>
```

## How to sort text for specific languages? ##

In this example, text is sorted according to the default German rules provided by Java. The
rules for sorting German in Java are defined in a package called Java Locale.
Locales are typically defined as a combination of language and country, but you can specify just
the language if you want. For example, if you specify "de" as the language, you will get sorting
that works well for German language. If you specify "de" as the language and "CH" as the
country, you will get German sorting specifically tailored for Switzerland. You can see a list of
supported Locales [here](http://docs.oracle.com/javase/1.5.0/docs/guide/intl/locale.doc.html). And in order to get more general information about how text analysis is
working with Solr have a look at [Language Analysis](https://cwiki.apache.org/confluence/display/solr/Language+Analysis) page.

```xml
<!-- define a field type for German collation -->
<fieldType name="collatedGERMAN" class="solr.TextField">
  <analyzer>
    <tokenizer class="solr.KeywordTokenizerFactory"/>
    <filter class="solr.CollationKeyFilterFactory" language="de" strength="primary" />
  </analyzer>
</fieldType>
...
<!-- define a field to store the German collated manufacturer names -->
<field name="manuGERMAN" type="collatedGERMAN" indexed="true" stored="false" />
...
<!-- copy the text to this field. We could create French, English, Spanish versions
     too, and sort differently for different users! -->
<copyField source="manu" dest="manuGERMAN"/>
```

## How to highlight the search query in results? ##

### Does OpenCms support result highlighting? ###
Yes, use the OpenCms Solr Select handler at:

<tt>http://localhost:8080/opencms/opencms/handleSolrSelect</tt>

and you will find the highlighting section below the list of documents within the returned
XML/JSON:

```xml
<lst name="highlighting">
  <lst name="a710bb16-1e04-11e2-b767-6805ca037347">
    <arr name="content_en">
      <str><em>YIPI</em> <em>YOHO</em> text text text</str>
    </arr>
  </lst>
  [...]
</lst>
```

### Does the Java API of OpenCms support highlighting? ###
Currently the OpenCms search API does not support full featured Solr highlighting. But you can
make use of the Solr default highlighting mechanism or course @see [1] or [2] and:

1. Call org.opencms.search.solr.CmsSolrResultList#getSolrQueryResponse() that returns a
SolrQueryResponse that is documented at: http://lucene.apache.org/solr/api-
3_6_1/org/apache/solr/response/SolrQueryResponse.html

2. Or you can use the above mentioned OpenCms Solr Select handler at:
localhost:8080/opencms/opencms/handleSolrSelect

### Is highlighting a performance killer? ###
Yes, for this reason highlighting is turned off before the first search is executed. After all not
permitted resources are filtered out of the result list, the highlighting is performed again.


## Solr indexing questions ##

### Please explain the differences between the "Solr Online and Offline"? ###
Please explain the differences between the "Solr Online and Offline"?
As the name of the indexes let assume Offline indexes are also containing changes that have
not yet been published and Online indexes only contain thoses resources that have already
been published. The "Online EN VFS" is a Lucene based index and also contains only those
resources that have been published.

### When executing a Solr query, does only the solr index get used? ###
No, permissions are checked by OpenCms API afterwards.
14.5.3.7 Where to find general information about Solr?
If you are interested in Solr in general the Solr wiki is a good starting point:
http://wiki.apache.org/solr/ The Documentation from CMS side you will find within the distributed
PDF file.

### Is there a way to create a full backup of the complete index? ###
You can copy the index folder 'WEB-INF/index/${INDEX_NAME}’' by hand.

### How to rebuild indexes with a fail-safe? ###
Edit the opencms-search.xml within your WEB-INF/config directory and add the following node
to your index:

```xml
<param name="org.opencms.search.CmsSearchIndex.useBackupReindexing">true</param>
```

This will create a snapshot as explained here:

http://wiki.apache.org/solr/CollectionDistribution

### Solr result size gets limited to 50 by default, how to get more than 50 results? ###
In order to return only permission checked resources (what is an expensive task) we only return
this limited number of results. For paging over results please have a look at at the Solr
parameters: rows and start: http://wiki.apache.org/solr/CommonQueryParameters

Since version 8.5.x you can increase the resulting documents to a size of your choice.


## Solr mailing list questions ##

### A class cast exception is thrown, what can I do? ###
You have to set the right classes for the index, and the field configuration otherwise the Lucene
search index implementation is used.
```xml
<index class="org.opencms.search.solr.CmsSolrIndex">[...]</index>
  <fieldconfiguration class="org.opencms.search.solr.CmsSolrFieldConfiguration">[...]
</fieldconfiguration>
```

### Is it possible to map elements with maxoccurres > 1? ###
Since version >= 8.5.1 they are mapped to a multivalved field.

### How to index OpenCmsDateTime elements? ###
```xml
<searchsetting element="Release" searchcontent="false">
  <solrfield targetfield="arelease" sourcefield="*_dt" />
</searchsetting>
```
This XSD search field mapping will result in multiple Solr fields (one per locale): <tt>arelease&#95;&lt;locale&gt;&#95;dt</tt>


# Solr development references #

## General documentation ##
- [The official Apache Solr documentation page](http://lucene.apache.org/solr/documentation.html)
- [Apache Solr Reference Guide from lucidworks](http://docs.lucidworks.com/display/solr/Apache+Solr+Reference+Guide)
- [SearchHub as another information source for Apache Solr related topics](http://searchhub.org)

## Performance guides ##
- [Apache Solr performance Wiki page](http://wiki.apache.org/solr/SolrPerformanceFactors)
- [The Seven Deadly Sins of Solr](http://searchhub.org/2010/01/21/the-seven-deadly-sins-of-solr/)

## Spellchecker configuration ##
- [Apache Solr Spellchecker and Configuration](http://www.arunchinnachamy.com/apache-solr-spellchecker-configuration/)
- [Super flexible AutoComplete with Apache Solr](http://www.cominvent.com/2012/01/25/super-flexible-autocomplete-with-solr/)

## External data sources ##
- [The "Data Import Request Handler" to adapt external data](http://wiki.apache.org/solr/DataImportHandler)

## Permission architecture ##
- [How Nuxeo indexes ACLs](https://github.com/nuxeo/nuxeo-solr/tree/master/architecture)
