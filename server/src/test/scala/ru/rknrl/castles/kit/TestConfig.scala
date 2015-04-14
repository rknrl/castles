//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.kit

import com.typesafe.config.ConfigFactory

object TestConfig {
  val config = ConfigFactory.parseString(
    """
      |akka {
      |  test {
      |    # factor by which to scale timeouts during tests, e.g. to account for shared
      |    # build system load
      |    timefactor =  1.0
      |
      |    # duration of EventFilter.intercept waits after the block is finished until
      |    # all required messages are received
      |    filter-leeway = 50ms
      |
      |    # duration to wait in expectMsg and friends outside of within() block
      |    # by default
      |    single-expect-default = 50ms
      |
      |    # The timeout that is added as an implicit by DefaultTimeout trait
      |    default-timeout = 50ms
      |
      |    calling-thread-dispatcher {
      |      type = akka.testkit.CallingThreadDispatcherConfigurator
      |    }
      |  }
      |}
    """.stripMargin
  )
}

