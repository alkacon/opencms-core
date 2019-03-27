<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" 
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-system.dtd"
                indent="yes" />

<!-- 

Removes the sections from opencms-system.xml which now have their own config files.

-->


<xsl:param name="configDir" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="loginmessage">
    </xsl:template>
    
    <xsl:template match="scheduler">
    </xsl:template>
    
    <xsl:template match="sites">
    </xsl:template>
    
    
</xsl:stylesheet>
