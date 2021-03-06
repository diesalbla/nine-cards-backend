/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import sbt.Build

trait ApiSettings extends Settings {

  import CustomSettings._
  import org.flywaydb.sbt.FlywayPlugin._
  import org.flywaydb.sbt.FlywayPlugin.autoImport._
  import sbt.Keys._
  import sbt._
  import sbtassembly.AssemblyPlugin._
  import sbtassembly.AssemblyPlugin.autoImport._
  import sbtassembly.MergeStrategy._
  import spray.revolver.RevolverPlugin
  import com.typesafe.sbt.SbtNativePackager._
  import com.typesafe.sbt.packager.archetypes.JavaAppPackaging.autoImport._

  private[this] lazy val assemblySettings9C = {
    import sbtassembly.AssemblyPlugin._
    import sbtassembly.AssemblyPlugin.autoImport._
    import sbtassembly.MergeStrategy._

    assemblySettings ++ Seq(
      assemblyJarName in assembly := s"9cards-${Versions.buildVersion}.jar",
      assembleArtifact in assemblyPackageScala := true,
      Keys.test in assembly := {},
      assemblyMergeStrategy in assembly := {
        case "application.conf" => concat
        case "reference.conf" => concat
        case entry =>
          val oldStrategy = (assemblyMergeStrategy in assembly).value
          val mergeStrategy = oldStrategy(entry)
          mergeStrategy == deduplicate match {
            case true => first
            case _ => mergeStrategy
          }
      },
      publishArtifact in(Test, packageBin) := false,
      mappings in Universal <<= (mappings in Universal, assembly in Compile) map { (mappings, fatJar) =>
        val filtered = mappings filter { case (file, fileName) => !fileName.endsWith(".jar") }
        filtered :+ (fatJar -> ("lib/" + fatJar.getName))
      },
      scriptClasspath := Seq((assemblyJarName in assembly).value)
    )
  }

  private[this] lazy val flywaySettings9C = {
    flywayBaseSettings(Runtime) ++ Seq(
      flywayDriver := databaseConfig.value.driver,
      flywayUrl := databaseConfig.value.url,
      flywayUser := databaseConfig.value.user,
      flywayPassword := databaseConfig.value.password,
      flywayLocations := Seq("filesystem:" + apiResourcesFolder.value.getPath + "/db/migration")
    )
  }

  lazy val apiSettings = projectSettings ++ Seq(
      name := "9cards-backend",
      databaseConfig := databaseConfigDef.value,
      apiResourcesFolder := apiResourcesFolderDef.value,
      run <<= run in Runtime dependsOn flywayMigrate
    ) ++
    RevolverPlugin.settings ++
    flywaySettings9C ++
    assemblySettings9C

}

