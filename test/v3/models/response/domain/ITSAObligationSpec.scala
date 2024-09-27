/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.models.response.domain

import api.models.domain.business.DesBusiness
import support.UnitSpec
import v3.models.response.downstream.DownstreamObligationsFixture

class ITSAObligationSpec extends UnitSpec with DownstreamObligationsFixture with ObligationsFixture {

  "fromDownstream" should {
    "map fields correctly" in {
      ITSAObligation.fromDownstream(
        downstreamObligation(
          Some(
            downstreamIdentification(incomeSourceType = Some(DesBusiness.ITSA), referenceNumber = "ignoredRefNo", referenceType = "ignoredRefType")),
          Seq(downstreamObligationDetail())
        )) shouldBe
        Some(ITSAObligation(Seq(obligationDetail())))
    }

    behave like ignoreBusinessType(DesBusiness.ITSB)
    behave like ignoreBusinessType(DesBusiness.ITSF)
    behave like ignoreBusinessType(DesBusiness.ITSP)

    def ignoreBusinessType(incomeSourceType: DesBusiness): Unit =
      s"return None for income source type $incomeSourceType" in {
        ITSAObligation.fromDownstream(
          downstreamObligation(
            Some(downstreamIdentification(incomeSourceType = Some(incomeSourceType))),
            Seq(downstreamObligationDetail())
          )) shouldBe None
      }

    "return None if there is no identification in the downstream obligation" in {
      ITSAObligation.fromDownstream(
        downstreamObligation(
          None,
          Seq(downstreamObligationDetail())
        )) shouldBe None
    }

    "return None if the identification in the downstream obligation has no incomeSourceType" in {
      ITSAObligation.fromDownstream(
        downstreamObligation(
          Some(downstreamIdentification(incomeSourceType = None)),
          Seq(downstreamObligationDetail())
        )) shouldBe None
    }
  }

}
