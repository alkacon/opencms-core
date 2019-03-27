<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" 
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-scheduler.dtd"
                indent="yes" />
                
<!-- 

Copies scheduler configuration from opencms-system to opencms-scheduler.

-->
                

<xsl:param name="configDir" />
<xsl:param name="opencmsSystem" select="document(concat($configDir, '/opencms-system.xml'))" />


    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/opencms[not(scheduler)]">
        <opencms>
            <xsl:copy-of select="$opencmsSystem//scheduler" />
        </opencms>
    </xsl:template>
</xsl:stylesheet>
