val username = "markblokpoel"
val repo = "lanag-ambiguityhelps"

lazy val commonSettings = Seq(
  name := repo,
  scalaVersion := "2.12.8",
  organization := s"com.markblokpoel",
  description := "Lanag Project Ambiguity helps (Rational Speech Act)",
  crossScalaVersions := Seq("2.12.8"),
  crossVersion := CrossVersion.binary,
  resolvers ++= Seq(
    "jitpack" at "https://jitpack.io"
  ),
  libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "2.4.1" % Provided,
    "org.apache.spark" %% "spark-sql" % "2.4.1" % Provided,
    "com.typesafe" % "config" % "1.3.3",
    "com.markblokpoel" %% "lanag-core" % "0.3.8",
    "com.lihaoyi" %% "scalatags" % "0.7.0",
    "com.github.jupyter" % "jvm-repr" % "0.4.0" % Provided
  ),
  // Compile options
  compile in Compile := (compile in Compile).dependsOn(formatAll).value,
  mainClass in assembly := Some(
    "com.markblokpoel.lanag.ambiguityhelps.experiments.uniform.UniformExperiment"),
  assemblyMergeStrategy in assembly := {
    case "application.conf" => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },
  test in Test := (test in Test).dependsOn(checkFormat).value,
  formatAll := {
    (scalafmt in Compile).value
    (scalafmt in Test).value
    (scalafmtSbt in Compile).value
  },
  checkFormat := {
    (scalafmtCheck in Compile).value
    (scalafmtCheck in Test).value
    (scalafmtSbtCheck in Compile).value
  }
)

lazy val root = (project in file("."))
  .settings(name := s"$repo")
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(docSettings: _*)
  .settings(releaseSettings: _*)
  .enablePlugins(SiteScaladocPlugin)
  .enablePlugins(GhpagesPlugin)

/*
 Scaladoc settings
 Note: To compile diagrams, Graphviz must be installed in /usr/local/bin
 */
import com.typesafe.sbt.SbtGit.GitKeys._
lazy val docSettings = Seq(
  autoAPIMappings := true,
  siteSourceDirectory := target.value / "api",
  git.remoteRepo := scmInfo.value.get.connection,
  envVars in ghpagesPushSite += ("SBT_GHPAGES_COMMIT_MESSAGE" -> s"Publishing Scaladoc [CI SKIP]"),
  scalacOptions in (Compile, doc) ++= Seq(
    "-groups",
    "-diagrams",
    "-implicits",
    "-doc-root-content",
    baseDirectory.value + "/overview.txt",
    "-doc-title",
    "LANguage AGents - Project Ambiguity Helps",
    "-diagrams-dot-path",
    "/usr/local/bin/dot"
  )
)

// Enforce source formatting before submit
lazy val formatAll = taskKey[Unit](
  "Format all the source code which includes src, test, and build files")
lazy val checkFormat = taskKey[Unit](
  "Check all the source code which includes src, test, and build files")

// Maven / Scaladex release settings
import ReleaseTransformations._

lazy val releaseSettings = Seq(
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    //runClean,
    //runTest,
    setReleaseVersion,
    //commitReleaseVersion,
    //ghpagesPushSite,
    tagRelease,
    releaseStepCommandAndRemaining("publishSigned"),
    setNextVersion,
    //commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    //pushChanges
  )
)

// Github and OSS Sonatype/Maven publish settings
lazy val publishSettings = Seq(
  homepage := Some(url(s"https://github.com/$username/$repo")),
  licenses += "GPLv3" -> url(
    s"https://github.com/$username/$repo/blob/master/LICENSE"),
  scmInfo := Some(
    ScmInfo(url(s"https://github.com/$username/$repo"),
            s"git@github.com:$username/$repo.git")),
  apiURL := Some(url(s"https://$username.github.io/$repo/latest/api/")),
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  developers := List(
    Developer(
      id = username,
      name = "Mark Blokpoel",
      email = "mark.blokpoel@gmail.com",
      url = new URL(s"http://github.com/$username")
    )
  ),
  useGpg := true,
  usePgpKeyHex("15B885FCC9586C56EE4587C9993E5F170C68BA83"),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := Some(
    if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
    else Opts.resolver.sonatypeStaging),
  //  credentials ++= (for {
  //    username <- sys.env.get("SONATYPE_USERNAME")
  //    password <- sys.env.get("SONATYPE_PASSWORD")
  //  } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq,
  // Following 2 lines need to get around https://github.com/sbt/sbt/issues/4275
  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(
    true)
)
