<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="us-ascii"
doctype-system="http://www.opencms.org/dtd/6.0/opencms-importexport.dtd"
                indent="yes" />

<!--

Replace import versions list in opencms-importexport.

-->


<xsl:param name="configDir" />
<xsl:param name="defaultImportexport" select="document(concat($configDir, '/defaults/opencms-importexport.xml'))" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="importversions">
        <xsl:copy-of select="$defaultImportexport//importversions" />
    </xsl:template>

</xsl:stylesheet>