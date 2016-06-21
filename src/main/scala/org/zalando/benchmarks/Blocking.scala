package org.zalando.benchmarks

import java.util.concurrent.{Callable, Executors}

import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration._

class Blocking(system: ActorSystem) {
  import ComputationFollowedByAsyncPublishing._

  def benchmark(coreFactor: Int): Unit = {
    // let's do this Ye Olde Schoole Way
    val exec = Executors newFixedThreadPool numWorkers(coreFactor)

    try {
      val futures = 1 to numTasks map Job map { job =>
        exec.submit(new Callable[PublishResult] {
          // explicitly turn async publishing operation into a blocking operation
          override def call(): PublishResult = Await.result(Publisher publish (Computer compute job, system), 1 hour)
        })
      }
      printResult(futures map (_.get))

    } finally {
      // never forget
      exec.shutdown()
    }
  }
}
