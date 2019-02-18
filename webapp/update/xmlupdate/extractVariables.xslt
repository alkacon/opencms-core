<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" 
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-variables.dtd"
                indent="yes" />
                
<!-- 

Copies login message configuration from opencms-system to opencms-variables.

-->
                

<xsl:param name="configDir" />
<xsl:param name="opencmsSystem" select="document(concat($configDir, '/opencms-system.xml'))" />


    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/opencms[not(variables)]">
        <opencms>
            <variables>
                <xsl:copy-of select="$opencmsSystem//loginmessage" />
            </variables>
        </opencms>
    </xsl:template>
</xsl:stylesheet>
