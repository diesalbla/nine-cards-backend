package cards.nine.api.converters

import cards.nine.api.messages.{ rankings ⇒ Api }
import cards.nine.processes.messages.{ rankings ⇒ Proc }
import cards.nine.services.free.domain.Category
import cards.nine.services.free.domain.{ rankings ⇒ Domain }
import cards.nine.services.common.NineCardsConfig._
import org.joda.time.DateTime

object rankings {

  import Domain.{ AuthParams, DateRange, RankingParams }

  def toApiRanking(resp: Proc.Get.Response): Api.Ranking = {

    def toApiCatRanking(cat: Category, rank: Domain.CategoryRanking): Api.CategoryRanking =
      Api.CategoryRanking(cat, rank.ranking map (_.name))

    Api.Ranking(resp.ranking.categories.toList map ((toApiCatRanking _).tupled))
  }

  object reload {

    def toRankingParams(token: String, request: Api.Reload.Request): RankingParams = {
      val length = request.rankingLength
      val dateRange = DateRange(request.startDate, request.endDate)
      RankingParams(dateRange, length, AuthParams(token))
    }

    def toXorResponse(proc: Proc.Reload.XorResponse): Api.Reload.XorResponse =
      proc.bimap(
        err ⇒ Api.Reload.Error(err.code, err.message, err.status),
        res ⇒ Api.Reload.Response()
      )
  }

}