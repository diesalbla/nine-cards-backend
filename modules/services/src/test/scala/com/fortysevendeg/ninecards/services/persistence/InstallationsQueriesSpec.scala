package com.fortysevendeg.ninecards.services.persistence

import com.fortysevendeg.ninecards.services.free.domain.Installation
import com.fortysevendeg.ninecards.services.free.domain.Installation.Queries._
import doobie.contrib.specs2.analysisspec.AnalysisSpec
import doobie.imports._
import org.specs2.mutable.Specification

class InstallationsQueriesSpec
  extends Specification
    with AnalysisSpec
    with DomainDatabaseContext {

  val getByIdQuery = installationPersistenceImpl.generateQuery(
    sql = getById,
    values = 1l)
  check(getByIdQuery)

  val getByUserAndAndroidIdQuery = installationPersistenceImpl.generateQuery(
    sql = getByUserAndAndroidId,
    values = (1l, "111a-222b-33c-444d13"))
  check(getByUserAndAndroidIdQuery)

  val insertInstallationQuery = installationPersistenceImpl.generateUpdateWithGeneratedKeys(
    sql = insert,
    values = (1l, Option("111a-222b-4d13"), "35a4df64a31adf3"))
  check(insertInstallationQuery)

  val updateDeviceTokenQuery = installationPersistenceImpl.generateUpdateWithGeneratedKeys(
    sql = updateDeviceToken,
    values = (Option("111a-222b-4d13"), 1l, "35a4df64a31adf3"))
  check(updateDeviceTokenQuery)

}