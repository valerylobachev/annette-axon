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

package axon.rest.bpm.permission

import annette.authorization.api.model.Permission

object BpmPermissions {

  final val BPM_REPOSITORY_CONTROL = Permission("axon.bpm.repository.control")
  final val BPM_DIAGRAM_VIEW =       Permission("axon.bpm.repository.bpmDiagram.view")
  final val BUSINESS_PROCESS_VIEW =  Permission("axon.bpm.repository.businessProcess.view")

  final val PROCESS_DEF_VIEW =       Permission("axon.bpm.deployment.view")


}
