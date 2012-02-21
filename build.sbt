import AssemblyKeys._

name := "graphite-relay"

version := "0.2.5"

organization := "com.invitemedia"

scalaVersion := "2.8.1"

resolvers ++= Seq(
    "jboss-repo" at "http://repository.jboss.org/nexus/content/groups/public/"
)

libraryDependencies ++= Seq(
    "com.google.inject" % "guice" % "3.0",
    "joda-time" % "joda-time" % "1.6.2",
    "log4j" % "log4j" % "1.2.16",
    "org.jboss.netty" % "netty" % "3.2.4.Final",
    "org.scalatest" % "scalatest" % "1.3" % "test",
    "com.github.scopt" %% "scopt" % "1.1.1"
)

seq(assemblySettings: _*)
