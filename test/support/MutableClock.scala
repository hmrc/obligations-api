/*
 * Copyright 2026 HM Revenue & Customs
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

package support

import java.time.{Clock, Instant, ZoneId}
import java.util.concurrent.atomic.AtomicReference

class MutableClock(private val zone: ZoneId, initialInstant: Instant) extends Clock {
  private val currentInstant: AtomicReference[Instant] = new AtomicReference(initialInstant)

  override def getZone: ZoneId = zone

  override def withZone(zone: ZoneId): Clock = new MutableClock(zone, currentInstant.get())

  override def instant(): Instant = currentInstant.get()

  def setInstant(newInstant: Instant): Unit = {
    currentInstant.set(newInstant)
  }

}

object MutableClock {

  def of(instant: Instant, zone: ZoneId): MutableClock =
    new MutableClock(zone, instant)

}
