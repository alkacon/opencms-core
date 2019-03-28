<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="UTF-8"
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-search.dtd"
                indent="yes" />

<!--

Removes the sections from opencms-workplace.xml which are no longer used.

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

    <xsl:template match="documenttype[name='xmlcontent-galleries']">
    </xsl:template>

    <xsl:template match="documenttype[name='xmlpage-galleries']">
    </xsl:template>

    <xsl:template match="analyzer[class='org.opencms.search.galleries.CmsGallerySearchAnalyzer']">
    </xsl:template>

    <xsl:template match="indexsource[name='gallery_source']">
    </xsl:template>

    <xsl:template match="indexsource[name='gallery_modules_source']">
    </xsl:template>

    <xsl:template match="indexsource[name='gallery_source_all']">
    </xsl:template>

   <xsl:template match="@boost[parent::field]">
    </xsl:template>

   <xsl:param name="opencmsSearch" select="document(concat($configDir, '/defaults/opencms-search.xml'))" />
	<xsl:template match="analyzers">
        <xsl:variable name="ws" select="." />
        <analyzers>
            <xsl:apply-templates />
            <xsl:for-each select="$opencmsSearch//analyzer">
                <xsl:variable name="cls" select="class" />
                <xsl:if test="not($ws//analyzer//class=$cls)">
                    <xsl:copy-of select="." />
                </xsl:if>
            </xsl:for-each>
        </analyzers>
    </xsl:template>

</xsl:stylesheet>
