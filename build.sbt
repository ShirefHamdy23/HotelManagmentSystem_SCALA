ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.23"
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-typed" % "2.8.0"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.32"

lazy val root = (project in file("."))
  .settings(
    name := "HotelProject"
  )
