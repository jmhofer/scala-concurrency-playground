package org.zalando.benchmarks

import akka.actor.ActorSystem
import rx.lang.scala.Observable
import rx.lang.scala.schedulers.{ComputationScheduler, ExecutionContextScheduler}

class RxScala(system: ActorSystem) {
  import ComputationFollowedByAsyncPublishing._

  def benchmark: Unit = {
    // looks nice, not sure if correct, blows up the heap
    Observable
      .from(1 to numTasks map Job)
      .subscribeOn(ComputationScheduler())
      .map(Computer compute)
      .subscribeOn(ExecutionContextScheduler(system dispatcher))
      .flatMap(1024, r => Observable.from(Publisher publish (r, system))(system dispatcher))
      .foldLeft(0) { case (s, r) => s + computeResult(r) }
      .foreach(println)
  }
}
