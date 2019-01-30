package axon.bpm.engine.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import axon.bpm.engine.api._
import axon.bpm.repository.api.BpmDiagram
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.camunda.bpm.engine.ProcessEngine
import play.api.libs.json.{JsObject, Json}

import scala.collection.immutable
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementation of the BpmService.
  */
class BpmEngineServiceImpl(registry: PersistentEntityRegistry, system: ActorSystem, processEngine: ProcessEngine)(
    implicit ec: ExecutionContext,
    mat: Materializer)
    extends BpmEngineService {

  val repositoryService = processEngine.getRepositoryService

  override def findProcessDef: ServiceCall[FindProcessDefOptions, immutable.Seq[ProcessDef]] = ServiceCall { findOptions =>
    Future.successful {
      var q = repositoryService.createProcessDefinitionQuery()

      q = findOptions.key.filter(_.trim.length != 0).map(k => q.processDefinitionKeyLike(k.trim)).getOrElse(q)
      q = findOptions.name.filter(_.trim.length != 0).map(n => q.processDefinitionNameLike(n.trim)).getOrElse(q)
      q = if (findOptions.latest) { q.latestVersion() } else { q }

      val list = q
        .withoutTenantId()
        .orderByProcessDefinitionName()
        .asc()
        .list()
        .asScala
        .map(pd => ProcessDef.apply(pd))
      immutable.Seq(list: _*)
    }
  }

  override def findCaseDef: ServiceCall[FindCaseDefOptions, immutable.Seq[CaseDef]] = ServiceCall { findOptions =>
    Future.successful {
      var q = repositoryService.createCaseDefinitionQuery()

      q = findOptions.key.filter(_.trim.length != 0).map(k => q.caseDefinitionKeyLike(k.trim)).getOrElse(q)
      q = findOptions.name.filter(_.trim.length != 0).map(n => q.caseDefinitionNameLike(n.trim)).getOrElse(q)
      q = if (findOptions.latest) { q.latestVersion() } else { q }

      val list = q
        .withoutTenantId()
        .orderByCaseDefinitionName()
        .asc()
        .list()
        .asScala
        .map(pd => CaseDef.apply(pd))
      immutable.Seq(list: _*)
    }
  }

  override def findDecisionDef: ServiceCall[FindDecisionDefOptions, immutable.Seq[DecisionDef]] = ServiceCall { findOptions =>
    Future.successful {
      var q = repositoryService.createDecisionDefinitionQuery()

      q = findOptions.key.filter(_.trim.length != 0).map(k => q.decisionDefinitionKeyLike(k.trim)).getOrElse(q)
      q = findOptions.name.filter(_.trim.length != 0).map(n => q.decisionDefinitionNameLike(n.trim)).getOrElse(q)
      q = if (findOptions.latest) { q.latestVersion() } else { q }

      val list = q
        .withoutTenantId()
        .orderByDecisionDefinitionName()
        .asc()
        .list()
        .asScala
        .map(pd => DecisionDef.apply(pd))
      immutable.Seq(list: _*)
    }
  }

  override def deploy: ServiceCall[BpmDiagram, DeploymentWithDefs] = ServiceCall { bpmDiagram =>
    Future.successful {
      val result = repositoryService
        .createDeployment()
        .name(bpmDiagram.name)
        .source(bpmDiagram.id)
        .addString(s"${bpmDiagram.name}.${bpmDiagram.notation.toLowerCase}", bpmDiagram.xml)
        .deployWithResult()
      DeploymentWithDefs(result)
    }
  }

  override def test: ServiceCall[JsObject, String] = ServiceCall { jsObject =>
    Future.successful(Json.prettyPrint(jsObject))
  }
}
