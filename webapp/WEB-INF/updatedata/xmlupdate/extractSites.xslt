<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" 
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-sites.dtd"
                indent="yes" />
                
<!-- 

Copies site configuration from opencms-system to opencms-sites.

-->


<xsl:param name="configDir" />
<xsl:param name="opencmsSystem" select="document(concat($configDir, '/opencms-system.xml'))" />


    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/opencms[not(sites)]">
        <opencms>
            <xsl:copy-of select="$opencmsSystem//sites" />
        </opencms>
    </xsl:template>
</xsl:stylesheet>
