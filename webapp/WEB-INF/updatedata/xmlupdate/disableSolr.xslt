<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="UTF-8"
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-search.dtd"
                indent="yes" />

<!--

Disables Solr for the module update step.

-->


<xsl:param name="configDir" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

     <xsl:template match="@enabled[parent::solr]">
       <xsl:attribute name="enabled">false</xsl:attribute>
    </xsl:template>
</xsl:stylesheet>
