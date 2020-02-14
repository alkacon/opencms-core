<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="UTF-8"
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-search.dtd"
                indent="yes" />

<!--

Adjusts document type configurations for xmlcontent and containerpage.

In OpenCms 12, multiple document-types for the same resource type can be configured. To be used in different indexes.

Hence the document type "xmlcontent" and "xmlcontent-solr" are now both configured for resourcetype "xmlcontent".

Note that a configured document factory in an index source is ignored if globally there's a factory that matches more
precisely. Therefor, it is necessary that both factories "xmlcontent" and "xmlcontent-solr" match for exactly the
same resourcetype/mimetype configuration - in our case for *all* mimetypes and resourcetype xmlcontent.

The same adjustment is made for types "containerpage" and "containerpage-solr".

-->


<xsl:param name="configDir" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="index[@class='org.opencms.search.galleries.CmsGallerySearchIndex']">
    </xsl:template>

    <xsl:template match="fieldconfiguration[@class='org.opencms.search.solr.CmsGallerySolrFieldConfiguration']">
    </xsl:template>

    <xsl:template match="fieldconfiguration[@class='org.opencms.search.galleries.CmsGallerySearchFieldConfiguration']">
    </xsl:template>

    <xsl:template match="documenttype[name='xmlcontent']">
        <documenttype>
            <name>xmlcontent</name>
            <class>org.opencms.search.documents.CmsDocumentXmlContent</class>
            <mimetypes/>
            <resourcetypes>
                <resourcetype>xmlcontent</resourcetype>
            </resourcetypes>
        </documenttype>
    </xsl:template>

    <xsl:template match="documenttype[name='containerpage']">
        <documenttype>
            <name>containerpage</name>
            <class>org.opencms.search.documents.CmsDocumentContainerPage</class>
            <mimetypes/>
            <resourcetypes>
                <resourcetype>containerpage</resourcetype>
            </resourcetypes>
        </documenttype>
    </xsl:template>

    <xsl:template match="documenttype[name='xmlcontent-solr']">
        <documenttype>
            <name>xmlcontent-solr</name>
            <class>org.opencms.search.solr.CmsSolrDocumentXmlContent</class>
            <mimetypes/>
            <resourcetypes>
                <resourcetype>xmlcontent</resourcetype>
            </resourcetypes>
        </documenttype>
    </xsl:template>

    <xsl:template match="documenttype[name='containerpage-solr']">
        <documenttype>
            <name>containerpage-solr</name>
            <class>org.opencms.search.solr.CmsSolrDocumentContainerPage</class>
            <mimetypes/>
            <resourcetypes>
                <resourcetype>containerpage</resourcetype>
            </resourcetypes>
        </documenttype>
    </xsl:template>

</xsl:stylesheet>
