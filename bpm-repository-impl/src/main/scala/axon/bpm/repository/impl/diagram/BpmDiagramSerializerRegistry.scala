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

package axon.bpm.repository.impl.diagram

import axon.bpm.repository.api.model.BpmDiagram
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object BpmDiagramSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[BpmDiagram],
    JsonSerializer[CreateBpmDiagram],
    JsonSerializer[UpdateBpmDiagram],
    JsonSerializer[DeleteBpmDiagram],
    JsonSerializer[FindBpmDiagramById],
    JsonSerializer[BpmDiagramCreated],
    JsonSerializer[BpmDiagramUpdated],
    JsonSerializer[BpmDiagramDeleted],
  )
}
