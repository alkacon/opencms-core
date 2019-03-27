<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="UTF-8"
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-workplace.dtd"
                indent="yes" />

<!-- 

Moves startView preference from hidden tab to advanced tab.

-->


<xsl:param name="configDir" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
 
    
    <xsl:template match="preference-tab[@name='hidden']/preference[@name='startView']" />
    
    <xsl:template match="preference-tab[@name='extended'][not(preference[@name='startView'])]">
        <preference-tab name="extended">
            <xsl:apply-templates />
            <preference name="startView" value="explorer" />
        </preference-tab>
    </xsl:template>

</xsl:stylesheet>
