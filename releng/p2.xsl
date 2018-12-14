<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright (c) 2018 1C-Soft LLC.

   This program and the accompanying materials are made available under
   the terms of the Eclipse Public License 2.0 which is available at
   https://www.eclipse.org/legal/epl-2.0/

   SPDX-License-Identifier: EPL-2.0

   Contributors:
       Vladimir Piskarev (1C) - initial API and implementation
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
  <xsl:strip-space elements="*"/>

  <xsl:param name="mirrorsURL"/>
  <xsl:param name="statsId"/>

  <!-- add missing newline -->
  <xsl:template match="repository">
    <xsl:text>&#10;</xsl:text>
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>

  <!-- add p2.mirrorsURL and p2.statsURI properties -->
  <xsl:template match="repository/properties">
    <properties size="{@size+2}">
      <xsl:copy-of select="property"/>
      <property name="p2.mirrorsURL" value="{$mirrorsURL}"/>
      <property name="p2.statsURI" value="http://download.eclipse.org/stats/handly"/>
    </properties>
  </xsl:template>

  <!-- add download.stats property -->
  <xsl:template match="repository/artifacts/artifact[
    @classifier='osgi.bundle' and (@id='org.eclipse.handly' or
    @id='org.eclipse.handly.ui' or @id='org.eclipse.handly.xtext.ui' or
    @id='org.eclipse.handly.examples')
    ]/properties">
    <properties size="{@size+1}">
      <xsl:copy-of select="property"/>
      <property name="download.stats" value="{../@id}-{$statsId}"/>
    </properties>
  </xsl:template>

  <!-- copy everything else -->
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
