name := "s3-ls"

version := "1.0"

scalaVersion := "2.11.5"

incOptions := incOptions.value.withNameHashing(false)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  "com.amazonaws" %	"aws-java-sdk" %	"1.9.20.1",
  "com.chuusai" %% "shapeless" % "2.1.0"
)
    