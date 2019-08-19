package vsys.settings

import vsys.Version
import vsys.utils.ScorexLogging

/**
  * System constants here.
  */

object Constants extends ScorexLogging {
  val ApplicationName = "B91 SYSTEMS"
  val AgentName = s"B91 Core v${Version.VersionString}"

  val UnitsInVsys = 100000000L
  val TotalVsys = 10000000000L // unuse in mainnet
}
