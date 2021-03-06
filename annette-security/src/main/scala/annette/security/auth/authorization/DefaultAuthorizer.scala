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

package annette.security.auth.authorization

import annette.authorization.api.AuthorizationService
import annette.authorization.api.model.{AuthorizationPrincipal, CheckPermissions, FindPermissions, Permission, RoleId}
import annette.security.auth.SessionData
import javax.inject._
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefaultAuthorizer @Inject()(authorizationService: AuthorizationService) extends Authorizer {

  override def authorize[A](request: Request[A], sessionData: SessionData, authorizationQuery: AuthorizationQuery)(
      implicit ec: ExecutionContext): Future[SessionData] = {
    println(s"DefaultAuthorizer.authorize: $authorizationQuery")
    val principals = Set(AuthorizationPrincipal("user", sessionData.principal.userId))
    authorizationQuery match {
      case AuthorizationQuery(DontCheck, _, DontFind) =>
        println("throw AuthorizationFailedException")
        throw new AuthorizationFailedException()
      case AuthorizationQuery(_, OR, _) =>
        processOr(sessionData, principals, authorizationQuery)
      case AuthorizationQuery(_, AND, _) =>
        processAnd(sessionData, principals, authorizationQuery)
    }
  }

  def processOr(sessionData: SessionData, principals: Set[AuthorizationPrincipal], authorizationQuery: AuthorizationQuery)(
      implicit ec: ExecutionContext): Future[SessionData] = {
    for {
      sessionDataAfterCheck <- checkPermissions(sessionData, principals, authorizationQuery)
      _ = if (!sessionDataAfterCheck.authorizationResult.checked &&
              authorizationQuery.findRule == DontFind &&
              !sessionDataAfterCheck.principal.superUser) {
        authorizationFailed(authorizationQuery)
      }
      resultSessionData <- findPermissions(sessionDataAfterCheck, principals, authorizationQuery)
    } yield resultSessionData
  }

  def processAnd(sessionData: SessionData, principals: Set[AuthorizationPrincipal], authorizationQuery: AuthorizationQuery)(
      implicit ec: ExecutionContext): Future[SessionData] = {
    for {
      sessionDataAfterCheck <- checkPermissions(sessionData, principals, authorizationQuery)
      _ = if (!sessionDataAfterCheck.authorizationResult.checked) {
        authorizationFailed(authorizationQuery)
      }
      resultSessionData <- findPermissions(sessionDataAfterCheck, principals, authorizationQuery)
    } yield resultSessionData
  }

  def checkPermissions(sessionData: SessionData, principals: Set[AuthorizationPrincipal], authorizationQuery: AuthorizationQuery)(
      implicit ec: ExecutionContext): Future[SessionData] = {
    for {
      checked <- if (sessionData.principal.superUser) {
        Future.successful(true)
      } else {
        authorizationQuery.checkRule match {
          case DontCheck =>
            Future.successful(false)
          case CheckAllRule(permissions) =>
            authorizationService.checkAllPermissions.invoke(CheckPermissions(principals, permissions))
          case CheckAnyRule(permissions) =>
            authorizationService.checkAnyPermissions.invoke(CheckPermissions(principals, permissions))
        }
      }
    } yield {
      val authResult = sessionData.authorizationResult.copy(
        checkRule = authorizationQuery.checkRule,
        checked = checked
      )
      sessionData.copy(authorizationResult = authResult)
    }
  }

  def findPermissions(sessionData: SessionData, principals: Set[AuthorizationPrincipal], authorizationQuery: AuthorizationQuery)(
      implicit ec: ExecutionContext): Future[SessionData] = {
    for {
      found <- authorizationQuery.findRule match {
        case DontFind =>
          Future.successful(Set.empty[Permission])
        case FindPermissionsRule(permissionIds) =>
          authorizationService.findPermissions.invoke(FindPermissions(principals, permissionIds))
      }
    } yield {
      val authResult = sessionData.authorizationResult.copy(
        findRule = authorizationQuery.findRule,
        found = found
      )
      sessionData.copy(authorizationResult = authResult)
    }

  }

  private def authorizationFailed(authorizationQuery: AuthorizationQuery) = {
    authorizationQuery.checkRule match {
      case DontCheck =>
        throw new AuthorizationFailedException()
      case CheckAllRule(permissions) =>
        throw new RequiredAllPermissionException(permissions)
      case CheckAnyRule(permissions) =>
        throw new RequiredAnyPermissionException(permissions)
    }
  }

}
