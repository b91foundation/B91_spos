package vsys.transaction.contract

import com.google.common.primitives.{Bytes, Longs, Shorts}
import com.wavesplatform.state2.ByteStr
import com.wavesplatform.utils.base58Length
import play.api.libs.json.{JsObject, Json}
import scorex.account.{PrivateKeyAccount, PublicKeyAccount}
import vsys.contract.DataEntry
import scorex.serialization.{BytesSerializable, Deser}
import scorex.transaction.TransactionParser._
import scorex.transaction.{AssetId, ValidationError}
import scorex.crypto.encode.Base58
import vsys.account.ContractAccount
import vsys.transaction.proof._
import vsys.transaction.ProvenTransaction

import scala.util.{Failure, Success, Try}

case class ExecuteContractFunctionTransaction private(contractId: ContractAccount,
                                                      funcIdx: Short,
                                                      data: Seq[DataEntry],
                                                      description: Array[Byte],
                                                      fee: Long,
                                                      feeScale: Short,
                                                      timestamp: Long,
                                                      proofs: Proofs)
  extends ProvenTransaction {

  override val transactionType: TransactionType.Value = TransactionType.ExecuteContractFunctionTransaction

  lazy val toSign: Array[Byte] = Bytes.concat(
    Array(transactionType.id.toByte),
    contractId.bytes.arr,
    Shorts.toByteArray(funcIdx),
    //Deser.serializeArray(data.flatMap(_.bytes).toArray),
    Deser.serializeArray(DataEntry.serializeArrays(data)),
    BytesSerializable.arrayWithSize(description),
    Longs.toByteArray(fee),
    Shorts.toByteArray(feeScale),
    Longs.toByteArray(timestamp)
  )

  override lazy val json: JsObject = jsonBase() ++ Json.obj(
    "contractId" -> contractId.address,
    "funcIdx" -> funcIdx,
    "data" -> Base58.encode(data.flatMap(_.bytes).toArray),
    "description" -> Base58.encode(description),
    "fee" -> fee,
    "feeScale" -> feeScale,
    "timestamp" -> timestamp
  )

  override val assetFee: (Option[AssetId], Long, Short) = (None, fee, feeScale)
  override lazy val bytes: Array[Byte] = Bytes.concat(toSign, proofs.bytes)

}

object ExecuteContractFunctionTransaction {

  val MaxDescriptionSize = 140
  val maxDescriptionStringSize: Int = base58Length(MaxDescriptionSize)

  def parseTail(bytes: Array[Byte]): Try[ExecuteContractFunctionTransaction] = Try {
    val contractId = ContractAccount.fromBytes(bytes.slice(0, ContractAccount.AddressLength)).right.get
    val funcIdx = Shorts.fromByteArray(bytes.slice(ContractAccount.AddressLength, ContractAccount.AddressLength + 2))
    val (dataBytes, dataEnd) = Deser.parseArraySize(bytes, ContractAccount.AddressLength + 2)
    //val data = DataEntry.fromArrayBytes(dataBytes).right.get
    val data = DataEntry.parseArrays(dataBytes)
    val (description, descriptionEnd) = Deser.parseArraySize(bytes, dataEnd)
    val fee = Longs.fromByteArray(bytes.slice(descriptionEnd, descriptionEnd + 8))
    val feeScale = Shorts.fromByteArray(bytes.slice(descriptionEnd + 8, descriptionEnd + 10))
    val timestamp = Longs.fromByteArray(bytes.slice(descriptionEnd + 10, descriptionEnd + 18))
    (for {
      proofs <- Proofs.fromBytes(bytes.slice(descriptionEnd + 18, bytes.length))
      tx <- ExecuteContractFunctionTransaction.createWithProof(contractId, funcIdx,
        data, description, fee, feeScale, timestamp, proofs)
    } yield tx).fold(left => Failure(new Exception(left.toString)), right => Success(right))
  }.flatten

  def createWithProof(contractId: ContractAccount,
                      funcIdx: Short,
                      data: Seq[DataEntry],
                      description: Array[Byte],
                      fee: Long,
                      feeScale: Short,
                      timestamp: Long,
                      proofs: Proofs): Either[ValidationError, ExecuteContractFunctionTransaction] =
    if (description.length > MaxDescriptionSize) {
      Left(ValidationError.TooBigArray)
    } else if (fee <= 0) {
      Left(ValidationError.InsufficientFee)
    } else if (feeScale != 100) {
      Left(ValidationError.WrongFeeScale(feeScale))
    } else {
      Right(ExecuteContractFunctionTransaction(contractId, funcIdx, data, description, fee, feeScale, timestamp, proofs))
    }

  def create(sender: PrivateKeyAccount,
             contractId: ContractAccount,
             funcIdx: Short,
             data: Seq[DataEntry],
             description: Array[Byte],
             fee: Long,
             feeScale: Short,
             timestamp: Long): Either[ValidationError, ExecuteContractFunctionTransaction] = for {
    unsigned <- createWithProof(contractId, funcIdx, data, description, fee, feeScale, timestamp, Proofs.empty)
    proofs <- Proofs.create(List(EllipticCurve25519Proof.createProof(unsigned.toSign, sender).bytes))
    tx <- createWithProof(contractId, funcIdx, data, description, fee, feeScale, timestamp, proofs)
  } yield tx

  def create(sender: PublicKeyAccount,
             contractId: ContractAccount,
             funcIdx: Short,
             data: Seq[DataEntry],
             description: Array[Byte],
             fee: Long,
             feeScale: Short,
             timestamp: Long,
             signature: ByteStr): Either[ValidationError, ExecuteContractFunctionTransaction] = for {
    proofs <- Proofs.create(List(EllipticCurve25519Proof.buildProof(sender, signature).bytes))
    tx <- createWithProof(contractId, funcIdx, data, description, fee, feeScale, timestamp, proofs)
  } yield tx
}