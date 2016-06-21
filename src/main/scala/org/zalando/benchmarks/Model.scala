package org.zalando.benchmarks

import akka.actor.ActorSystem
import akka.pattern.after
import org.openjdk.jmh.infra.Blackhole

import scala.concurrent.Future
import scala.util.Random

case class Job(id: Int) {
  val payload = Array.fill(16000)(Random.nextInt())
}
case class JobResult(job: Job, result: Int)
case class PublishResult(result: JobResult)

object Computer {
  import ComputationFollowedByAsyncPublishing._

  def compute(job: Job): JobResult = {
    // jmh ensures that this really consumes CPU
    Blackhole consumeCPU numTokensToConsume
    JobResult(job, job.id)
  }
}

object Publisher {
  import ComputationFollowedByAsyncPublishing._

  // we use the scheduler and the dispatcher of the actor system here because it's so very convenient
  def publish(result: JobResult, system: ActorSystem): Future[PublishResult] =
    after(publishDuration, system.scheduler) {
      Future(PublishResult(result))(system.dispatcher)
    } (system.dispatcher)
}
