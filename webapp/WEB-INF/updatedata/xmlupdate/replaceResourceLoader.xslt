<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="us-ascii"
doctype-system="http://www.opencms.org/dtd/6.0/opencms-vfs.dtd"
                indent="yes" />

<!--

Replace import versions list in opencms-importexport.

-->


<xsl:param name="configDir" />
<xsl:param name="defaultVfs" select="document(concat($configDir, '/defaults/opencms-vfs.xml'))" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="resourceloaders">
        <xsl:copy-of select="$defaultVfs//resourceloaders" />
    </xsl:template>

</xsl:stylesheet>