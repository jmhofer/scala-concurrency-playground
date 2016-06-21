package org.zalando.benchmarks

import java.util.concurrent.TimeUnit

import akka.actor._
import com.softwaremill.macwire.wire
import org.openjdk.jmh.annotations._

import scala.concurrent.duration._
import scala.util.Random

object ComputationFollowedByAsyncPublishing {
  implicit val system = ActorSystem()

  val publishDuration = 15 millis
  val numTasks = 20000
  val numTokensToConsume = 5000000L // eats about 9-10 ms on my box

  val actors   = wire[Actors]
  val futures  = wire[Futures]
  val blocking = wire[Blocking]
  val streams  = wire[Streams]
  val rx       = wire[RxScala]

  def printResult(rs: Seq[PublishResult]): Unit = println(rs map computeResult sum)

  def computeResult(r: PublishResult): Int =
    r.result.result + r.result.job.payload(Random.nextInt(r.result.job.payload length))

  def numWorkers(coreFactor: Int): Int = Runtime.getRuntime.availableProcessors * coreFactor
}

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
class ComputationFollowedByAsyncPublishing {
  import ComputationFollowedByAsyncPublishing._

  @Benchmark def bmActors():          Unit =   actors benchmark  1          // <= 109 threads ("Live peak" in JVisualVM)
  @Benchmark def bmParallelFutures(): Unit =  futures benchmark  1          // <=  44 threads ("Live peak" in JVisualVM)
  @Benchmark def bmBlocking():        Unit = blocking benchmark 64          // <= 549 threads ("Live peak" in JVisualVM)
  @Benchmark def bmStreams():         Unit =  streams benchmark  1          // <=  52 threads ("Live peak" in JVisualVM)

//@Benchmark def bmRxScala():         Unit =       rx benchmark             // blows up with OutOfMemoryError :(

  @TearDown def tearDown(): Unit = system.terminate()
}
