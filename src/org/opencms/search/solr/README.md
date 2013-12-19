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
      <str name="template_prop">/system/modules/com.alkacon.opencms.v8.template3/
templates/main.jsp</str>
      <str name="style.layout_prop">/.content/style</str>
      <str name="NavText_prop">OpenCms 8 Demo</str>
      <str name="Title_prop">Flower Today</str>
      <str name="ahtml_de_t">Nachfolgend finden Sie aktuelle Meldungen und 
Veranstaltungen rund um die Blume.</str>
      <str name="ahtml_en_t">In this section, you find current flower related 
news and events.</str>
      <arr name="content_en">
        <str>News from the world of flowers Flower Today In this section, you 
find current flower related news and events.</str>
      </arr>
      <arr name="content_de">
        <str>Neuigkeiten aus der Welt der Blumen Blume aktuell Nachfolgend finden 
Sie aktuelle Meldungen[...]</str>
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
      <str name="template_prop">/system/modules/com.alkacon.opencms.v8.template3/
templates/main.jsp</str>
      <str name="style.layout_prop">/.content/style</str>
      <str name="NavText_prop">OpenCms 8 Demo</str>
      <str name="Title_prop">Flower Dictionary</str>
      <str name="ahtml_de_t">In der Botanik existieren zahlreiche Gewächsfamilien 
die uns durch Ihre dekorativen Blüten [...]</str>
      <str name="ahtml_en_t">There are many different types of plants and flowers 
existing. To give a short overview, [...]</str>
      <arr name="content_en">
        <str>The different types of flowers Flower Dictionary There are many different 
types of plants and flowers [...]</str>
      </arr>
      <arr name="content_de">
        <str>Die verschiedenen Gewächsfamilien Blumen Lexikon In der Botanik existieren 
zahlreiche Gewächsfamilien die uns durch [...]</str>
      </arr>
      <date name="timestamp">2012-09-03T10:45:49.265Z</date>
      <float name="score">1.0</float>
    </doc>
  </result>
</response>
```

===Send a Java-API query===
<source lang="java">
  String query = "fq=type:v8article&fq=lastmodified:[NOW-1DAY TO NOW]&fq=Title_prop:Flower";
  CmsSolrResultList results = OpenCms.getSearchManager().getIndexSolr("Solr Online Index").search(getCmsObject(), query);
  for (CmsSearchResource sResource : results) {
    String path = sResource.getField(I_CmsSearchField.FIELD_PATH);
    Date date = sResource.getMultivaluedField(I_CmsSearchField.FIELD_DATE_LASTMODIFIED);
    List<String> cats = sResource.getMultivaluedField(I_CmsSearchField.FIELD_CATEGORY);
  }
</source>

The class org.opencms.search.solr.CmsSolrResultList encapsulates a list of 'OpenCms resource documents' ({@link CmsSearchResource}).

This list can be accessed exactly like an {@link ArrayList} which entries are {@link CmsSearchResource} that extend {@link CmsResource} and holds the Solr implementation of {@link I_CmsSearchDocument} as member. <b>This enables you to deal with the resulting list as you do with well known {@link List} and work on it's entries like you do on {@link CmsResource}</b>.

===Use CmsSolrQuery class for querying Solr===

<source lang="java">
  CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr("Solr Online Index");
  CmsSolrQuery squery = new CmsSolrQuery(getCmsObject(), "path:/sites/default/xmlcontent/article_0001.html");
  List<CmsResource> results = index.search(getCmsObject(), squery);
</source>

==Advanced Search Feature Examples==
The documentation will follow soon. 
===Auto suggestion/completion/correction===
The documentation will follow soon. 
===Excerpt===
The documentation will follow soon. 
===Facteted search===
The documentation will follow soon.
===Grouping===
The documentation will follow soon.
===Highlighting===
The documentation will follow soon.
===Range queries===
The documentation will follow soon.
===Sorting===
The documentation will follow soon.
===Spellchecking===
The documentation will follow soon.
===Thesaurus/Synomys===
The documentation will follow soon.
===Querying multiple cores (indexes)===

'Core' is the wording in the Solr world for thinking of several indexes. Preferring the correct speech, let's say core instead index. Multiple cores should only be required if you have completely different applications but want a single Solr Server that manages all the data. See [http://wiki.apache.org/solr/CoreAdmin Solr Core Administration] for detailed information.

So assuming you have configured multiple Solr cores and you would like to query a specific one you have to tell Solr/OpenCms which core/index you want to search on. This is done by a special parameter:

<pre>
http://localhost:8080/opencms/opencms/handleSolrSelect?   
                              // The URI of the OpenCms Solr Select Handler configured in 'opencms-system.xml'
    &core=My Solr Index Name  // Searches on the core with the name 'My Solr Index Name'
    &q=content_en:Flower      // for the text 'Flower'
</pre>

==Using the standard OpenCms Solr collector==

OpenCms version 8.5 delivers a standard Solr collector using '''<tt>byQuery</tt>''' as name to simply pass a query string and '''<tt>byContext</tt>''' as name to pass a query string and led OpenCms use the user's request context. The implementing class for this collector can be found at '''<tt>org.opencms.file.collectors.CmsSolrCollector</tt>'''.

<source lang="xml"  strict="false">
<cms:contentload
  collector="byQuery" 
  param="q=+parent-folders:/sites/default/ +type:ddarticle&rows=4&start=7&type=dismax&fl=*,score&sort=lastmodified desc" 
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
</source>

=Indexing Content of OpenCms=

==Search Configuration==

In general the system wide search configuration for OpenCms is done in the file '''<tt>'opencms-search.xml'</tt>''' (<tt><CATALINA_HOME>/webapps/<OPENCMS_WEBAPP>/WEB_INF/config/opencms-search.xml</tt>).

===Embedded/HTTP Solr Server===

Since version 8.5 of OpenCms a new optional node with the '''XPath: <tt>opencms/search/solr</tt>''' is available. To simply enable the OpenCms embedded Solr Server your <tt>opencms-search.xml</tt> should start like this:

<source lang="xml">
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE opencms SYSTEM "http://www.opencms.org/dtd/6.0/opencms-search.dtd">
<opencms>
  <search>
    <solr enabled="true"/>

      [...]

  </search>
</opencms>
</source>

Optionally you can configure the '''<tt>Solr home directory</tt>''' and the main Solr configuration file name '''<tt>solr.xml</tt>'''. OpenCms then concats those two paths to '''<tt><solr_home><configfile></tt>''' an example for such a configuration would look like:

<source lang="xml">
<solr enabled="true">
    <home>/my/solr/home/folder</home>
    <configfile>rabbit.xml</configfile>
</solr>
</source>

In order to disable Solr system wide remove the '''<tt><solr/>-node</tt>''' or set the enabled attribute to <tt>'false'</tt> like:

<source lang="xml">
<solr enabled="false"/>
</source>

It is also possible to connect with an external HTTP Solr server, to do so replace the line '''<tt><solr enabled="true"/></tt>''' with the following:

<source lang="xml">
<solr enabled="true" serverUrl="http://mySolrServer" />
</source>

The OpenCms SolrSelect request handler does not support the external HTTP Solr Server. So if your HTTP Solr Server is directly reachable by '''<tt><nowiki>http://<your_server></nowiki></tt>''' there will no permission check performed and indexed data that is secret will be accessible. What means that you are '''self-responsible''' for resources that have permission restrictions set on the VFS of OpenCms. But of course you can use the method '''<tt>org.opencms.search.solr.CmsSolrIndex.search(CmsObject, SolrQuery)</tt>''' or '''<tt>org.opencms.search.solr.CmsSolrIndex.search(CmsObject, String)</tt>''' and be sure permissions are checked also for HTTP Solr Servers. Maybe a future version of OpenCms will feature a secure access on HTTP Solr server.

===Search index(es)===

By default OpenCms comes along with a "Solr Online" index. To add a new Solr index you can use the default configuration as copy template.

<source lang="xml">
<index class="org.opencms.search.solr.CmsSolrIndex">
  <name>Solr Online</name>
  <rebuild>auto</rebuild>
  <project>Online</project>
  <locale>all</locale>
  <configuration>solr_fields</configuration>
  <sources>
    < source >solr_source< /source >
  </sources>
</index>
</source>

===Index sources===

Index sources for Solr can be configured in the file '''<tt>opencms-search.cml</tt>''' exactly the same way as you do for Lucene indexes. In order to use the advanced XSD field mapping for XML contents, you must add the new document type '''<tt>xmlcontent-solr</tt>''' to the list of document types that are indexed:

<source lang="xml">
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
</source>

===A new document type===

With OpenCms version 8.5 there is a new document type called '''<tt>xmlcontent-solr</tt>'''. Its implementation ('''<tt>CmsSolrDocumentXmlContent</tt>''') performs a localized content extraction that is used later on to fill the Solr input document. As explained in section [[#Custom fields for XML contents]] it is possible to define a mapping between elements defined in the XSD of an XML resource type and a field of the Solr document. The values for those defined XSD field mappings are also extracted by the document type named '''<tt>xmlcontent-solr</tt>.

<source lang="xml">
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
</source>

===The Solr default field configuration===

By default the field configuration for OpenCms Solr indexes is implemented by the class '''<tt>org.opencms.search.solr.CmsSolrFieldConfiguration</tt>'''. The most easiest Solr field configuration declared in '''<tt>opencms-search.xml</tt>''' looks like the following. See also [[#Extend the CmsSolrFieldConfiguration]]

<source lang="xml">
<fieldconfiguration class="org.opencms.search.solr.CmsSolrFieldConfiguration">
  <name>solr_fields</name>
  <description>The Solr search index field configuration.</description>
  <fields/>
</fieldconfiguration>
</source>

===Migrating a Lucene index to a Solr index===

An existing Lucene field configuration can easily be transformed into a Solr index. To do so create a new Solr field configuration. Therefore you can use the snippet shown in section [[#The Solr default field configuration]] as template and copy the list of fields from the Lucene index you want to convert into that skeleton.

There exists a specific strategy to map the Lucene field names to Solr field names:

* '''Exact name matching:''' OpenCms tries to determine an explicit Solr field that has the exact name like the value of the name-attribute. E.g. OpenCms tries to find an explicit Solr filed definition named '''<tt>meta</tt>''' for '''<field name="meta"> ... </field>'''. To make use of this strategy you have to edit the '''<tt>schema.xml</tt>''' of Solr manually and add an explicit field definition named according to the exact Lucene field names.

* '''Type specific fields:''' In the existing Lucene configuration type specific field definitions are not designated, but the Solr '''<tt>schema.xml</tt>''' defines different data types for fields. If you are interested in making use of those type specific advantages (like language specific field analyzing/tokenizing) without manipulating the '''<tt>schema.xml</tt>''' of Solr, you have to define a type attribute for those fields at least. The value of the attribute '''<tt>type</tt>''' can be any name of each '''<tt><dynamicField></tt>''' configured in the '''<tt>schema.xml</tt>''' that starts with a '''<tt>*_</tt>'''. The resulting field inside the Solr document is then named '''<tt><luceneFieldName>_<dynamicFieldSuffix></tt>'''.

* '''Fallback:''' If you don't have defined a type attribute and there does not exist an explicit field in the '''<tt>schema.xml</tt>''' named according to the Lucene field name OpenCms uses '''<tt>text_general</tt>''' as fallback. E.g. a Lucene field '''<tt><field name="title" index="true"> ... </field></tt>''' will be stored as a dynamic field named '''<tt>title_txt</tt>''' in the Solr index.

An originally field configuration like:

<source lang="xml">
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
</source>

Could look after the conversion like this:

<source lang="xml">
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
</source>

==Indexed data==

The following sections will show what data is indexed by default and what possibilities are offered by OpenCms to configure / implement additional field configurations / mappings.

===The Solr index schema (schema.xml)===

Have a look at the Solr '''<tt>schema.xml</tt>''' first. In the file '''<tt><CATALINA_HOME>/webapps/<OPENCMS>/WEB-INF/solr/conf/schema.xml</tt>''' you will find the field definitions that will be used by OpenCms that were briefly summarized before.

<source lang="xml">
 <fields>
    <!-- Unique indetifier. -->
   <field name="id"                  type="uuid"         indexed="true"  stored="true"  default="NEW" /> 
   <field name="path"                type="string"       indexed="true"  stored="true"  required="true" />
   <!-- is copied -->
   <field name="path_hierarchy"      type="text_path"    indexed="true"  stored="false" required="true" />
   <field name="parent-folders"      type="string"       indexed="true"  stored="false" required="true" multiValued="true" />
   <field name="type"                type="string"       indexed="true"  stored="true"  required="true" />
   <field name="suffix"              type="string"       indexed="true"  stored="true" />
   <field name="res_locales"         type="string"       indexed="true"  stored="true"  required="true" multiValued="true" />
   <field name="con_locales"         type="string"       indexed="true"  stored="true"  required="true" multiValued="true" />
   <field name="contentdate"         type="date"         indexed="true"  stored="true"  required="true" />
   <field name="created"             type="date"         indexed="true"  stored="true"  required="true" />
   <field name="lastmodified"        type="date"         indexed="true"  stored="true"  required="true" />
   <field name="expired"             type="date"         indexed="true"  stored="true"  />
   <field name="relased"             type="date"         indexed="true"  stored="true"  />
   <field name="content"             type="text_general" indexed="true"  stored="false" multiValued="true" compressed="true" />
   <field name="contentblob"         type="binary"       indexed="false" stored="true"  compressed="true" />
   <field name="category"            type="text_general" indexed="true"  stored="true"  multiValued="true" />
   <!-- is copied -->
   <field name="category_exact"      type="string"       indexed="true"  stored="false" multiValued="true" termVectors="true" />
   <field name="dependencyType"      type="string"       indexed="true"  stored="true" />
   <!-- Catchall for general text fields -->
   <field name="text"                type="text_general" indexed="true"  stored="false" multiValued="true"/>
   <!-- Catchall for English text fields -->
   <field name="text_en"             type="text_en"      indexed="true"  stored="false" multiValued="true"/>
   <!-- Catchall for German  text fields -->
   <field name="text_de"             type="text_de"      indexed="true"  stored="false" multiValued="true"/>
   <!-- Catchall for Greek  text fields -->
   <field name="text_el"             type="text_el"      indexed="true"  stored="false" multiValued="true"/>
   <!-- Catchall for Spanish  text fields -->
   <field name="text_es"             type="text_es"      indexed="true"  stored="false" multiValued="true"/>
   <!-- Catchall for French  text fields -->
   <field name="text_fr"             type="text_fr"      indexed="true"  stored="false" multiValued="true"/>
   <!-- Catchall for Hungarian text fields -->
   <field name="text_hu"             type="text_hu"      indexed="true"  stored="false" multiValued="true"/>
   <!-- Catchall for Italian text fields -->
   <field name="text_it"             type="text_it"      indexed="true"  stored="false" multiValued="true"/>
   
   <field name="timestamp" type="date" indexed="true" stored="true" default="NOW" />

   <dynamicField name="*_exact"      type="string"       indexed="true"  stored="false"/>
   <dynamicField name="*_prop"       type="text_general" indexed="true"  stored="true"/>
   <dynamicField name="*_i"          type="int"          indexed="true"  stored="true"/>
   <dynamicField name="*_s"          type="string"       indexed="true"  stored="true"/>
   <dynamicField name="*_l"          type="long"         indexed="true"  stored="true"/>
   <dynamicField name="*_t"          type="text_general" indexed="true"  stored="true"/>
   <dynamicField name="*_txt"        type="text_general" indexed="true"  stored="true" multiValued="true"/>
   <dynamicField name="*_en"         type="text_en"      indexed="true"  stored="true" multiValued="true" />
   <dynamicField name="*_de"         type="text_de"      indexed="true"  stored="true" multiValued="true" />
   <dynamicField name="*_el"         type="text_el"      indexed="true"  stored="true" multiValued="true" />
   <dynamicField name="*_es"         type="text_es"      indexed="true"  stored="true" multiValued="true" />
   <dynamicField name="*_fi"         type="text_fi"      indexed="true"  stored="true" multiValued="true" />
   <dynamicField name="*_fr"         type="text_fr"      indexed="true"  stored="true" multiValued="true" />
   <dynamicField name="*_hu"         type="text_hu"      indexed="true"  stored="true" multiValued="true" />
   <dynamicField name="*_it"         type="text_it"      indexed="true"  stored="true" multiValued="true" />
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
   <dynamicField name="attr_*"       type="text_general" indexed="true"  stored="true" multiValued="true"/>
   <dynamicField name="random_*"     type="random" />
   <dynamicField name="text_*"       type="text_general" indexed="true"  stored="true" termVectors="true" 
termPositions="true" termOffsets="true"/>
   <dynamicField name="dep_*"        type="string"       indexed="false" stored="true" multiValued="true"/>
 </fields>

 <uniqueKey>id</uniqueKey>

 <copyField source="path"      dest="path_hierarchy" />
 <copyField source="*_prop"    dest="*_exact" />

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
</source>

===Default index fields===

OpenCms indexes several information for each resource by default:

* '''id''' - Structure id used as unique identifier for an document (The structure id of the resource)
* '''path''' - Full root path (The root path of the resource e.g. /sites/default/flower_en/.content/article.html)
* '''path_hierarchy''' - The full path as (path tokenized field type: text_path)
* '''parent-folders''' - Parent folders (multi-valued field containing an entry for each parent path)
* '''type''' - Type name (the resource type name)
* '''res_locales''' - Existing locale nodes for XML content and all available locales in case of binary files
* '''created''' - The creation date (The date when the resource itself has being created)
* '''lastmodified''' - The date last modified (The last modification date of the resource itself)
* '''contentdate''' - The content date (The date when the resource's content has been modified)
* '''released''' - The release and expiration date of the resource
* '''content''' A general content field that holds all extracted resource data (all languages, type text_general)
* '''contentblob''' - The serialized extraction result (content_blob) to improve the extraction performance while indexing
* '''category''' - All categories as general text
* '''category_exact''' - All categories as exact string for faceting reasons
* '''text_<locale>''' - Extracted textual content optimized for the language specific search (Default languages: en, de, el, es, fr, hu, it)
* '''timestamp''' - The time when the document was indexed last time
* '''*_prop''' - All properties of a resource as searchable and stored text (field name: <Property_Definition_Name>_prop as text_general)
* '''*_exact''' - All properties of a resource as exact not stored string (field name: <Property_Definition_Name>_exact as string)

===Custom fields for XML contents===

Declarative field configuration with field mappings can also be bone via the '''<tt>XSD-Content-Definition</tt>''' of an XML resource type as defined in the '''<tt>DefaultAppinfoTypes.xsd</tt>'''

<source lang="xml">
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
</source>

You are able to declare search field mappings for XML content elements directly in the XSD Content Definition. A XSD using this feature can then look like:

<source lang="xml">
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
</source>

===Dynamic field mappings===

If the requirements for the field mapping are more "dynamic" than just: '''<tt>'static piece of content' -> 'specified field defined in the Solr schema'</tt>''', you are able to implement the the interface '''<tt>org.opencms.search.fields.I_CmsSearchFieldMapping</tt>'''.

===Custom field configuration===

Declarative field configurations with field mappings can be defined in the file '''<tt>opencms-search.xml</tt>'''. You can use exactly the same features as already known for OpenCms Lucene field configurations.
* '''Please see [[#Migrating a Lucene index to a Solr index]]'''

===Extend the CmsSolrFieldConfiguration===

If the standard configuration options are still not flexible enough you are able to extends from the class: '''<tt>org.opencms.search.solr.CmsSolrFieldConfiguration</tt>''' and define a custom Solr field configuration in the '''<tt>opencms-search.xml</tt>''':

<source lang="xml">
  <fieldconfiguration class="your.package.YourSolrFieldConfiguration">
    <name>solr_fields</name>
    <description>The Solr search index field configuration.</description>
    <fields/>
  </fieldconfiguration>
</source>

=Behind the walls=
==The request handler==
The documentation will follow soon.

==Permission check==
The documentation will follow soon.

==Configurable post processor==

OpenCms offers the capability for post search processing Solr documents after the document has been checked for permissions. This capability allows you to add fields to the found document before the search result is returned. In order to make use of the post processor you have to add an optional parameter for the search index as follows:

<source lang="xml">
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
</source>

The specified class for the parameter '''<tt>org.opencms.search.solr.CmsSolrIndex.postProcessor</tt>''' must be an implementation of '''<tt>org.opencms.search.solr.I_CmsSolrPostSearchProcessor</tt>'''.

==Multilingual support==
The documentation will follow soon.

==Multilingual dependency resolving==
The documentation will follow soon.

==Extraction result cache==
The documentation will follow soon.


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
