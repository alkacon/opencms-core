<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="us-ascii"
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-importexport.dtd"
                indent="yes" />

<!-- 

Replace immutables list in opencms-importexport.

-->


<xsl:param name="configDir" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="immutables">
        <immutables>
                <resource uri="/" />
                <resource uri="/shared/" />
                <resource uri="/sites/" />
                <resource uri="/system/" />
                <resource uri="/system/categories/" />
                <resource uri="/system/config/" />
                <resource uri="/system/handler/" />
                <resource uri="/system/login/" />
                <resource uri="/system/modules/" />
                <resource uri="/system/orgunits/" />
                <resource uri="/system/shared/" />
                <resource uri="/system/userimages/" />
                <resource uri="/system/workplace/" />
                <resource uri="/system/workplace/commons/" />
                <resource uri="/system/workplace/editors/" />
                <resource uri="/system/workplace/resources/" />
        </immutables>
    </xsl:template>
    
</xsl:stylesheet>
