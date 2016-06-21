package org.zalando.benchmarks

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl._
import akka.stream._

import scala.concurrent.Await
import scala.concurrent.duration._

class Streams(implicit system: ActorSystem) {
  import ComputationFollowedByAsyncPublishing._

  def benchmark(coreFactor: Int)(implicit system: ActorSystem): Unit = {

    // a sink that computes the sum
    val sink = Sink.fold[Int, PublishResult](0) { case (sum, job) => sum + computeResult(job) }

    // wiring up the graph of streams
    val g = RunnableGraph fromGraph GraphDSL.create(sink) { implicit b => sink =>
      // preparations...
      val source   = b add Source(1 to numTasks map Job)
      val compute  = Flow.fromFunction(Computer compute).withAttributes(ActorAttributes dispatcher "compute-dispatcher")
      val balanced = b add balancer(compute, numWorkers(coreFactor)).async
      val publish  = b add Flow[JobResult].mapAsyncUnordered(1024) { Publisher.publish(_ , system) }

      // finally, here's the graph
      source ~> balanced ~> publish ~> sink.in

      ClosedShape
    }

    // Running the graph will materialize it into a future int. We wait for it and print it.
    println(Await.result(g.run()(ActorMaterializer()), 1 hour))
  }

  // Deals with balancing workload over the given number of workers.
  private def balancer[In, Out](worker: Flow[In, Out, Any], workerCount: Int): Flow[In, Out, NotUsed] = {
    Flow fromGraph GraphDSL.create() { implicit b =>
      val balancer = b add Balance[In](workerCount, waitForAllDownstreams = false)
      val merge    = b add Merge[Out](workerCount)

      1 to workerCount foreach { _ => balancer ~> worker.async ~> merge }

      FlowShape(balancer.in, merge.out)
    }
  }
}
