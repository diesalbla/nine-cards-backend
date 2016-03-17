package com.fortysevendeg.ninecards.processes.utils

import com.fortysevendeg.ninecards.processes.utils.EncryptionAlgorithm._
import com.fortysevendeg.ninecards.services.common.NineCardsConfig
import com.roundeights.hasher.Hasher

object EncryptionAlgorithm {

  sealed trait Algorithm

  case object Md5 extends Algorithm

  case object Sha256 extends Algorithm

  case object Sha512 extends Algorithm

  case object HMacMd5 extends Algorithm

  case object HMacSha256 extends Algorithm

  case object HMacSha512 extends Algorithm

}

class HashUtils(implicit config: NineCardsConfig) {

  lazy val salt: Option[String] = config.getOptionalString("ninecards.salt")
  lazy val secretKey: String = config.getString("ninecards.secretKey")

  def hashValue(text: String, algorithm: Algorithm = HMacSha512) = {

    val hasher = salt.fold(Hasher(text))(Hasher(text).salt(_))

    val digest = algorithm match {
      case Md5 => hasher.md5
      case Sha256 => hasher.sha256
      case Sha512 => hasher.sha512
      case HMacMd5 => hasher.hmac(secretKey).md5
      case HMacSha256 => hasher.hmac(secretKey).sha256
      case HMacSha512 => hasher.hmac(secretKey).sha512
    }

    digest.hex
  }
}

object HashUtils {

  implicit def hashUtils(implicit config: NineCardsConfig) = new HashUtils
}