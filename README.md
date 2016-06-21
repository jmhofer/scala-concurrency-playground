# Playground for Scala concurrency concepts

This is the companion repository for an upcoming [Zalando Tech Blog](https://tech.zalando.de/blog/) post.

It provides a playground for playing around with the various ways of doing Scala concurrency.
It can be used to familiarize yourself with the different concurrency concepts available to you in Scala.

The playground is based on an extremely simplified use case of having a cpu-bound computation first,
and then publishing the result of it in an asynchronous way.

It currently explores the behavior of

* good-old blocking threads
* [Scala Futures](http://docs.scala-lang.org/overviews/core/futures.html)
* [Actors](http://doc.akka.io/docs/akka/2.4.7/scala/actors.html)
* [Akka Streams](http://doc.akka.io/docs/akka/2.4.7/scala/stream/stream-introduction.html)
* [RxScala](http://reactivex.io/rxscala/)

Of course, there are many more ways of doing the same things differently! I invite you to explore.

It uses [JMH](http://openjdk.java.net/projects/code-tools/jmh/) for benchmarking.

## Getting Started

As a prerequisite for running this, you need to have [SBT](http://scala-sbt.org) installed.
Then, clone the repo, run `sbt`, and then for example:
```
jmh:run -wi 1 -i 5 .*Computation.*
```

See also the docs for the [SBT JMH plugin](https://github.com/ktoso/sbt-jmh) and for [JMH itself](http://openjdk.java.net/projects/code-tools/jmh/) for more details about the options.

Explore, fiddle and most of all, have fun!

## Contributing

If you want to add another way of doing the same thing, please feel free to put up a pull request.

## License

Copyright (c) 2016 Zalando SE

Licensed under the MIT License, see [here](https://github.com/zalando/benchmarks-scala-nonblocking/blob/master/LICENSE).
