package axon.bpm.repository.impl

import axon.bpm.repository.api.BpmRepositoryService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable

class BpmRepositoryLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new BpmRepositoryApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new BpmRepositoryApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[BpmRepositoryService])
}

abstract class BpmRepositoryApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[BpmRepositoryService](wire[BpmRepositoryServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = BpmRepositorySerializerRegistry

  persistentEntityRegistry.register(wire[SchemaEntity])

}

object BpmRepositorySerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = SchemaSerializerRegistry.serializers
}