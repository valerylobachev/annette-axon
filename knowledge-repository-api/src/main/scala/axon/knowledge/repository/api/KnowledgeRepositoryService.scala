package axon.knowledge.repository.api

import akka.{Done, NotUsed}
import annette.shared.exceptions.AnnetteExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.Environment

import scala.collection._

trait KnowledgeRepositoryService extends Service {

//  def createSchema: ServiceCall[Schema, Schema]
//  def updateSchema: ServiceCall[Schema, Schema]
//  def deleteSchema(id: SchemaId): ServiceCall[NotUsed, Done]
//  def findSchemaById(id: String): ServiceCall[NotUsed, Schema]
//  def findSchemas: ServiceCall[String, immutable.Seq[SchemaSummary]]

  final override def descriptor = {
    import Service._
    // @formatter:off
    named("knowledge-repository")
      .withCalls(
//        restCall(Method.POST, "/api/bpm/repository/schema", createSchema),
//        restCall(Method.PUT, "/api/bpm/repository/schema", updateSchema),
//        restCall(Method.DELETE, "/api/bpm/repository/schema/:id", deleteSchema _),
//        restCall(Method.GET, "/api/bpm/repository/schema/:id", findSchemaById _),
//        restCall(Method.POST, "/api/bpm/repository/findSchema", findSchemas _)
      )
      .withExceptionSerializer(new AnnetteExceptionSerializer())
      .withAutoAcl(true)
    // @formatter:on
  }
}
