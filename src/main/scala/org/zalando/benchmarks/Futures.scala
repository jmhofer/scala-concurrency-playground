package org.zalando.benchmarks

import java.util.concurrent.Executors

import akka.actor.ActorSystem

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class Futures(system: ActorSystem) {
  import ComputationFollowedByAsyncPublishing._

  def benchmark(coreFactor: Int): Unit = {
    import system.dispatcher

    // execution context only for the (cpu-bound) computation
    val ec = ExecutionContext fromExecutorService Executors.newFixedThreadPool(numWorkers(coreFactor))

    try {
      // `traverse` will distribute the tasks to the thread pool, the rest happens fully async
      printResult(Await.result(Future.traverse(1 to numTasks map Job) { job =>
        Future(Computer compute job)(ec) flatMap (Publisher.publish(_, system))
      }, 1 hour))

    } finally {
      // the execution context has to be shut down explicitly
      ec.shutdown()
    }
  }
}
