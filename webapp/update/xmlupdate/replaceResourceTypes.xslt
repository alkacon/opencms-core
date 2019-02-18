<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="UTF-8"
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-vfs.dtd"
                indent="yes" />

<!-- 

Replace resource types with the default ones in opencms-vfs.

-->


<xsl:param name="configDir" />
<xsl:param name="opencmsVfs" select="document(concat($configDir, '/defaults/opencms-vfs.xml'))" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="resourcetypes">
        <xsl:copy-of select="$opencmsVfs//resourcetypes" />
    </xsl:template>
</xsl:stylesheet>
