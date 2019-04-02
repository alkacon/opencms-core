<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" 
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-workplace.dtd"
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
    
    <xsl:template match="explorertypes/multicontextmenu">
    </xsl:template>
    
    <xsl:template match="explorertypes/menurules">
    </xsl:template>
    
    <xsl:template match="workplace-customfoot">
    </xsl:template>
    
    <xsl:template match="tool-manager/roots/root[uri/text()='/system/workplace/explorer/']">
    </xsl:template>
    
    
</xsl:stylesheet>
