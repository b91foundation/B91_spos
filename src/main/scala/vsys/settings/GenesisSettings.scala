package vsys.settings

import vsys.blockchain.state.ByteStr

import scala.concurrent.duration._

case class GenesisTransactionSettings(recipient: String, amount: Long, slotId: Int)

case class GenesisSettings(
  blockTimestamp: Long,
  timestamp: Long,
  initialBalance: Long,
  signature: Option[ByteStr],
  transactions: Seq[GenesisTransactionSettings],
  initialMintTime: Long,
  averageBlockDelay: FiniteDuration)

object GenesisSettings {
  val MAINNET = GenesisSettings(1567152000000000666L, 1567152000000000666L, 51000000000000000L,
    ByteStr.decodeBase58("53a4RuzK6EeZZUavcvBNGCPBzMCdnzqhMDA2eisKTdSsXPdavJFJ4DRMSynYHsEUqc6inwZXgtdHyZ1Btt2sUevX").toOption,
    List(
        GenesisTransactionSettings("b9QcPZ3pgAK14VrR9tR7oidue6qKgWJPmjf",51000000000000000L,-1),
        GenesisTransactionSettings("b9QMKkYrhS9XTJWmB1HAkdFUhrEXYvbD3y5",0L,0),
        GenesisTransactionSettings("b9CnJo9DTpqSBSgsSVx3Nen98wPCLBgy5MU",0L,4),
        GenesisTransactionSettings("b9WHTRcFQrSqGpHFUQtNByukK5aMQN41P3z",0L,8),
        GenesisTransactionSettings("b9LdPaSwUF4gs7JT7GfQdTeFD9RjaGKXRy4",0L,12),
        GenesisTransactionSettings("b9TySojwiiGq76SXyaNPmJDzyvCYLQ6RhjK",0L,16),
        GenesisTransactionSettings("b9JGcDURyvBtQZreEk5HX8fqPdRXKWccNHp",0L,20),
        GenesisTransactionSettings("b9Tqo9EDwwSXLnzDFCTscYp6j3Wx2CgsuLn",0L,24),
        GenesisTransactionSettings("b9CQMdeaNSXDZtLasqxAaMExQRnyp2V95DS",0L,28)),
    1567152000000000000L, 60.seconds)

  val TESTNET = GenesisSettings(1566118800000000666L, 1566118800000000666L, Constants.UnitsInVsys * Constants.TotalVsys,
    ByteStr.decodeBase58("3QRCgwvGBjDRWsqUGvimzJ9gGWeCnw6XiEtEwbDcQ2Up8YzVtRwBehbYEMxKhNaeeNNgpYiZgPALrini29E8bd1L").toOption,
    List(
      GenesisTransactionSettings("bLBViKY7dswNw2PfVKWW5ZEjdB736gJ5Cso", (Constants.UnitsInVsys * Constants.TotalVsys * 0.30).toLong, 0)),
    1566118800000000000L, 60.seconds)
}
