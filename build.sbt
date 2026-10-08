ThisBuild / scalaVersion := "3.9.0"
ThisBuild / organization := "pe.utp"
ThisBuild / version := "1.0.0"

Compile / run / fork := true
Compile / run / connectInput := true

lazy val root = (project in file("."))
  .settings(
    name := "sistema-experto-amenazas",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
  )
