/*
 * Copyright 2018 Valery Lobachev
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

package annette.shared.exceptions
import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, TransportErrorCode, TransportException}
import play.api.libs.json.{Format, Json}

final class AnnetteTransportException(errorCode: TransportErrorCode, annetteException: AnnetteException)
    extends TransportException(errorCode, new ExceptionMessage(annetteException.code, annetteException.toDetails), annetteException)

/*object AnnetteTransportException {
  implicit val format: Format[AnnetteTransportException] = Json.format
}*/
