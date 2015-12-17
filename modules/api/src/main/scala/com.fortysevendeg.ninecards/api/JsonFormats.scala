package com.fortysevendeg.ninecards.api

import com.fortysevendeg.ninecards.processes.domain._
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonFormats
  extends DefaultJsonProtocol
  with SprayJsonSupport {

  implicit val googlePlayAppFormat = jsonFormat7(GooglePlayApp)

  implicit val googleOAuth2DataFormat = jsonFormat3(GoogleOAuth2Data)

  implicit val googleAuthDataDeviceInfoFormat = jsonFormat4(GoogleAuthDataDeviceInfo)

  implicit val googleAuthDataFormat = jsonFormat2(GoogleAuthData)

  implicit val anonymousAuthDataFormat = jsonFormat1(AnonymousAuthData)

  implicit val facebookAuthDataFormat = jsonFormat3(FacebookAuthData)

  implicit val twitterAuthDataFormat = jsonFormat6(TwitterAuthData)

  implicit val authDataFormat = jsonFormat5(AuthData)

  implicit val userFormat = jsonFormat6(User)
}