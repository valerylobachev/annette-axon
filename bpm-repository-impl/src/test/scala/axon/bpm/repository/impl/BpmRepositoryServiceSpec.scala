package axon.bpm.repository.impl

import akka.Done
import annette.shared.exceptions.AnnetteException
import axon.bpm.repository.api.{BpmDiagram, BpmDiagramAlreadyExist, BpmDiagramNotFound, BpmRepositoryService}
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.Random

class BpmRepositoryServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new BpmRepositoryApplication(ctx) with LocalServiceLocator {
      override def additionalConfiguration: AdditionalConfiguration = {
        super.additionalConfiguration ++ Configuration.from(
          Map(
            "cassandra-query-journal.eventual-consistency-delay" -> "0"
          ))
      }
    }
  }

  val client = server.serviceClient.implement[BpmRepositoryService]

  override protected def afterAll() = server.stop()

  "bpm repository service" should {

    "create bpmDiagram & find it by id" in {
      val xmlBpmDiagram = TestData.xmlBpmDiagram()
      for {
        created <- client.createBpmDiagram.invoke(xmlBpmDiagram)
        found <- client.findBpmDiagramById(TestData.id).invoke()
      } yield {
        created shouldBe BpmDiagram(TestData.id, TestData.name, Some(TestData.description), "BPMN", xmlBpmDiagram)
        found shouldBe BpmDiagram(TestData.id, TestData.name, Some(TestData.description), "BPMN", xmlBpmDiagram)
      }
    }

    "create bpmDiagram with existing id" in {
      for {
        created <- client.createBpmDiagram.invoke(TestData.xmlBpmDiagram("id3"))
        created2 <- client.createBpmDiagram
          .invoke(TestData.xmlBpmDiagram("id3"))
          .recover {
            case th: Throwable => th
          }
      } yield {
        created shouldBe a[BpmDiagram]
        created2 shouldBe a[AnnetteException]
        created2.asInstanceOf[AnnetteException].code shouldBe BpmDiagramAlreadyExist.MessageCode
      }

    }

    "update bpmDiagram" in {
      val sch = TestData.xmlBpmDiagram("id4", name = "name1", description = "description1")
      for {
        created <- client.createBpmDiagram.invoke(TestData.xmlBpmDiagram("id4"))
        updated <- client.updateBpmDiagram
          .invoke(sch)
          .recover { case th: Throwable => th }
        bpmDiagram <- client.findBpmDiagramById("id4").invoke()
      } yield {
        created shouldBe a[BpmDiagram]
        updated shouldBe a[BpmDiagram]
        bpmDiagram.xml shouldBe sch
        bpmDiagram.name shouldBe "name1"
        bpmDiagram.description shouldBe Some("description1")
      }

    }

    "update bpmDiagram with non-existing id" in {
      val sch = TestData.xmlBpmDiagram("id5", name = "name1", description = "description1")
      for {
        updated <- client.updateBpmDiagram
          .invoke(sch)
          .recover { case th: Throwable => th }
      } yield {
        updated shouldBe a[AnnetteException]
        updated.asInstanceOf[AnnetteException].code shouldBe BpmDiagramNotFound.MessageCode
      }

    }

    "delete bpmDiagram" in {
      val id = s"id${Random.nextInt()}"
      val sch = TestData.xmlBpmDiagram(id)
      for {
        created <- client.createBpmDiagram.invoke(sch)
        found1 <- client
          .findBpmDiagramById(id)
          .invoke()
          .recover { case th: Throwable => th }
        deleted <- client.deleteBpmDiagram(id).invoke()
        found2 <- client
          .findBpmDiagramById(id)
          .invoke()
          .recover {
            case th: Throwable =>
              th
          }
      } yield {
        created shouldBe a[BpmDiagram]
        found1 shouldBe BpmDiagram(id, TestData.name, Some(TestData.description), "BPMN", sch)
        deleted shouldBe Done
        found2 shouldBe a[AnnetteException]
        found2.asInstanceOf[AnnetteException].code shouldBe BpmDiagramNotFound.MessageCode
      }

    }

    "delete bpmDiagram with nonexisting id" in {
      val id = s"id${Random.nextInt()}"
      for {
        deleted <- client
          .deleteBpmDiagram(id)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        deleted shouldBe a[AnnetteException]
        deleted.asInstanceOf[AnnetteException].code shouldBe BpmDiagramNotFound.MessageCode
      }

    }

    "find bpmDiagram by non-existing id" in {
      val xmlBpmDiagram = TestData.xmlBpmDiagram()
      for {
        found <- client
          .findBpmDiagramById(Random.nextInt().toString)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        found shouldBe a[AnnetteException]
        found.asInstanceOf[AnnetteException].code shouldBe BpmDiagramNotFound.MessageCode
      }
    }

    "find bpmDiagrams" in {
      val ids = Seq(
        s"id-${Random.nextInt()}",
        s"id-${Random.nextInt()}",
        s"id-${Random.nextInt()}"
      )
      val names = Seq(
        s"name-${Random.nextInt()}",
        s"name-${Random.nextInt()}",
        s"name-${Random.nextInt()}"
      )
      val descriptions = Seq(
        s"description-${Random.nextInt()}",
        s"description-${Random.nextInt()}",
        s"description-${Random.nextInt()}"
      )
      val bpmDiagrams = for (i <- 0 until ids.length) yield TestData.xmlBpmDiagram(ids(i), names(i), descriptions(i))

      val createFuture = Future.traverse(bpmDiagrams)(bpmDiagram => client.createBpmDiagram.invoke(bpmDiagram))

      (for {
        created <- createFuture.recover { case th: Throwable => th }
      } yield {
        awaitSuccess() {
          for {
            found0 <- client.findBpmDiagrams.invoke("")
            found1 <- client.findBpmDiagrams.invoke(ids(0))
            found2 <- client.findBpmDiagrams.invoke(names(1))
            found3 <- client.findBpmDiagrams.invoke(descriptions(2))
            found4 <- client.findBpmDiagrams.invoke(Random.nextInt().toString)
          } yield {

            found0.length shouldBe >=(ids.length)

            found1.length shouldBe 1
            found1.head.id shouldBe ids(0)
            found1.head.name shouldBe names(0)
            found1.head.description shouldBe Some(descriptions(0))

            found2.length shouldBe 1
            found2.head.id shouldBe ids(1)
            found2.head.name shouldBe names(1)
            found2.head.description shouldBe Some(descriptions(1))

            found3.length shouldBe 1
            found3.head.id shouldBe ids(2)
            found3.head.name shouldBe names(2)
            found3.head.description shouldBe Some(descriptions(2))

            found4.length shouldBe 0

          }
        }
      }).flatMap(identity)
    }

  }

  def awaitSuccess[T](maxDuration: FiniteDuration = 10.seconds, checkEvery: FiniteDuration = 100.milliseconds)(block: => Future[T]): Future[T] = {
    val checkUntil = System.currentTimeMillis() + maxDuration.toMillis

    def doCheck(): Future[T] = {
      block.recoverWith {
        case recheck if checkUntil > System.currentTimeMillis() =>
          val timeout = Promise[T]()
          server.application.actorSystem.scheduler.scheduleOnce(checkEvery) {
            timeout.completeWith(doCheck())
          }(server.executionContext)
          timeout.future
      }
    }

    doCheck()
  }

}
