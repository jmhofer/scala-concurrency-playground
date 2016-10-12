package org.zalando.benchmarks

import java.util.concurrent.{ExecutorService, Executors}

import akka.actor.ActorSystem
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}

class RxScala(system: ActorSystem) {
  import ComputationFollowedByAsyncPublishing._

  def benchmark(coreFactor: Int): Unit = {
    val executor: ExecutorService = Executors.newCachedThreadPool()
    implicit val ec = ExecutionContext.fromExecutor(executor)
    try {
      Observable
        .from(1 to numTasks)
        .map(Job)
        .flatMap(numWorkers(coreFactor), job => Observable.from(Future(Computer compute job)))
        .flatMap(r => Observable.from(Publisher publish (r, system))(system dispatcher))
        .foldLeft(0) { case (s, r) => s + computeResult(r) }
        .toBlocking
        .foreach(println)
    } finally {
      executor.shutdown()
    }
  }
}
