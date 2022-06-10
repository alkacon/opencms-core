<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
 <xsl:output indent="yes"/>

 <xsl:template match="node()|@*">
     <xsl:copy>
       <xsl:apply-templates select="node()|@*" />
     </xsl:copy>
 </xsl:template>

<xsl:template match="Title">
<Heading>
    <xsl:value-of select="." />
</Heading>
</xsl:template>

<xsl:template match="Intro">
<Intro>
    <xsl:value-of select="concat('modified ', .)" />
</Intro>
</xsl:template>

</xsl:stylesheet>

