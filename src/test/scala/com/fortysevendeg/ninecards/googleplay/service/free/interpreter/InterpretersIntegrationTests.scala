package com.fortysevendeg.ninecards.googleplay.service.free.interpreter

import cats.data.Xor
import com.fortysevendeg.ninecards.config.NineCardsConfig
import com.fortysevendeg.ninecards.googleplay.TestConfig._
import com.fortysevendeg.ninecards.googleplay.domain.Domain._
import com.fortysevendeg.ninecards.googleplay.service.GooglePlayDomain._
import com.fortysevendeg.ninecards.googleplay.service.free.algebra.GooglePlay._
import org.specs2.matcher.TaskMatchers
import org.specs2.mutable.Specification
import org.http4s.client.blaze.PooledHttp1Client
import scala.concurrent.duration._
import scalaz.concurrent.Task

class InterpretersIntegrationTests extends Specification with TaskMatchers {

  private val fisherprice = "air.fisherprice.com.shapesAndColors"
  private val nonexisting = "com.package.does.not.exist"
  private val localization = Some(Localization("es-ES"))

  // Most of this should be moved to a wiring module, with the cache.
  private val client = PooledHttp1Client()
  private val apiEndpoint = NineCardsConfig.getConfigValue("googleplay.api.endpoint")
  private val apiClient = new Http4sGooglePlayApiClient(apiEndpoint, client)
  private val webEndpoint = NineCardsConfig.getConfigValue("googleplay.web.endpoint")
  private val webClient = new Http4sGooglePlayWebScraper(webEndpoint, client)
  private val interpreter = TaskInterpreter(apiClient, webClient)

  "Making an API request" should {

    "result in an Item for packages that exist" in {
      val request = apiClient(Package(fisherprice), (token, androidId, localization))
      val fetchedDocId = request.map(xor => xor.map(item => item.docV2.docid))
      fetchedDocId must returnValue(Xor.right(fisherprice))
      // todo should this be more comprehensive? check all other tests too
    }

    "result in an error state for packages that do not exist" in {
      val request = apiClient(Package(nonexisting), (token, androidId, localization))
      request must returnValue(Xor.left(nonexisting))
    }
  }

  "Making a Web scrape request against the Play Store" should {

    val expectedCategories = List("EDUCATION", "FAMILY_EDUCATION")
    val expectedDocId = fisherprice
    val expectedTitle = "Shapes & Colors Music Show"
    val auth: GoogleAuthParams = (Token(""), AndroidId(""), localization)

    "result in an Item for packages that exist" in {
      val request: Task[QueryResult] = webClient(Package(fisherprice), auth)
      val relevantDetails = request.map { xor =>
        xor.map { i: Item =>
          (i.docV2.docid, i.docV2.details.appDetails.appCategory, i.docV2.title)
        }
      }
      relevantDetails must returnValue(Xor.right((expectedDocId, expectedCategories, expectedTitle)))
    }

    "result in an error state for packages that do not exist" in {
      val request = webClient(Package(nonexisting), auth)
      request must returnValue(Xor.left(nonexisting))
    }
  }

  "Making requests to the Google Play store" should {

    "result in a correctly parsed response for a single package" in {

      val result = interpreter(RequestPackage(params, Package(fisherprice)))

      val retrievedCategory = result.map { optionalItem =>
        optionalItem.flatMap(_.docV2.details.appDetails.appCategory.headOption)
      }

      retrievedCategory must returnValue(Some("EDUCATION"))
    }

    "result in a correctly parsed response for multiple packages" in {

      val successfulCategories = List(
        (fisherprice, "EDUCATION"),
        ("com.google.android.googlequicksearchbox", "TOOLS")
      )

      val invalidPackages = List(nonexisting, "com.another.invalid.package")

      val packages = successfulCategories.map(_._1) ++ invalidPackages

      val response = interpreter(BulkRequestPackage(params, PackageListRequest(packages)))

      val result = response.map { case PackageDetails(errors, items) =>
        val itemCategories = items.flatMap(_.docV2.details.appDetails.appCategory)

        (errors.sorted, itemCategories.sorted)
      }

      result must returnValue((invalidPackages.sorted, successfulCategories.map(_._2).sorted))
    }
  }

  "Making requests when the Google Play API is not successful" should {
    "fail over to the web scraping approach" in {

      val badApiRequest: QueryService = ( _ => Task.fail(new RuntimeException("Failed request")) )

      val badApiClient = new Http4sGooglePlayApiClient("http://unknown.host.com", client)
      val interpreter = TaskInterpreter(badApiClient, webClient)

      val result = interpreter(RequestPackage(params, Package(fisherprice)))

      val retrievedCategory = result.map { optionalItem =>
        optionalItem.flatMap(_.docV2.details.appDetails.appCategory.headOption)
      }

      retrievedCategory.runFor(10.seconds) must_=== Some("EDUCATION")
    }
  }
}
