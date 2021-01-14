/*
 * Copyright (C) 2009-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.dispatch

import java.util.concurrent.{Executors, ExecutorService, ThreadFactory }

import com.typesafe.config.Config

class VirtualPoolExecutorServiceFactory(builder: Thread.Builder, underlying: ExecutorServiceFactory) extends ExecutorServiceFactory {
  final def createExecutorService: ExecutorService = Executors.newThreadExecutor(builder.virtual(underlying.createExecutorService).factory())
}

class VirtualPoolConfigurator(config: Config, prerequisites: DispatcherPrerequisites, underlying: ExecutorServiceFactoryProvider)
  extends ExecutorServiceConfigurator(config, prerequisites) {

  final def createExecutorServiceFactory(id: String, threadFactory: ThreadFactory): ExecutorServiceFactory =
    new VirtualPoolExecutorServiceFactory(
      threadFactory match {
        case m: MonitorableThreadFactory =>
          // Unforunately it is not possible to provide a ContextClassloader here
          Thread.
            builder().
            daemon(m.daemonic).
            uncaughtExceptionHandler(m.exceptionHandler).
            name(m.name + "-" + id + "-", 0)
        case _ =>
          // Open question whether we should throw an exception in this case or not
          Thread.
            builder().
            name(id + "-", 0)
      },
      underlying.createExecutorServiceFactory(id, threadFactory)
    )
}
