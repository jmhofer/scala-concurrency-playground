package org.zalando.benchmarks

import akka.actor._
import akka.pattern.{ask, pipe}
import akka.routing.BalancingPool
import akka.util.Timeout
import org.zalando.benchmarks.ComputationFollowedByAsyncPublishing._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class Actors(system: ActorSystem) {
  def benchmark(coreFactor: Int): Unit = {
    import system.dispatcher
    implicit val timeout = Timeout(1 hour)

    // Route computations through a balanced pool of (cpu bound) computation workers.
    val router = system actorOf BalancingPool(numWorkers(coreFactor)).props(Props[ComputeActor])

    try {
      // Collect the results, sum them up and print the sum.
      printResult(Await.result(Future.traverse(1 to numTasks map Job) { job =>
        (router ? job).mapTo[PublishResult]
      }, 1 hour))

    } finally {
      // Shut down the actors.
      router ! PoisonPill
    }
  }
}

// Actor responsible for the computation, and for delegating to the publishing actor(s).
class ComputeActor extends Actor {
  val publisher = context actorOf Props[PublishActor]

  def receive = {
    case job: Job =>
      // tell the publisher about who sent us the job, and the job results
      val s = sender()
      publisher ! (s, Computer compute job)
  }
}

// Actor responsible for publishing, and for sending the response back.
class PublishActor extends Actor {
  import context.dispatcher

  def receive = {
    case (s: ActorRef, r: JobResult) =>
      // just pipe the result back to the original sender
      Publisher.publish(r, context.system) pipeTo s
  }
}
