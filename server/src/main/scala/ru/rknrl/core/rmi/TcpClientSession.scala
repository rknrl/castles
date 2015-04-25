//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core.rmi

import java.io.{DataOutputStream, InputStream}

import akka.actor.ActorRef
import akka.util.ByteString
import org.slf4j.LoggerFactory
import ru.rknrl.EscalateStrategyActor

import scala.annotation.tailrec

case class ReceivedCommand(commandId: Byte, byteString: ByteString)

case object CloseConnection

abstract class Msg(id: Byte)

abstract class TcpClientSession(tcpSender: ActorRef, name: String) extends EscalateStrategyActor {

  import akka.io.Tcp._

  val log = LoggerFactory.getLogger(getClass)

  val headerSize = 4
  val commandIdSize = 1
  val maxSize = 512 * 1024
  implicit val byteOrder = java.nio.ByteOrder.LITTLE_ENDIAN

  protected def writeCommand(msg: Any, os: DataOutputStream)

  protected def parseCommand(commandId: Byte, is: InputStream): Unit

  private var buffer: ByteString = ByteString.empty

  override final def receive: Receive = {
    case Received(receivedData) ⇒
      val data = buffer ++ receivedData
      val (newBuffer, frames) = extractFrames(data, Nil)
      for (frame ← frames) processFrame(frame)
      buffer = newBuffer

    case PeerClosed ⇒
      log.debug("peer closed " + name)
      context stop self

    case CloseConnection ⇒
      log.debug("close connection command " + name)
      context stop self

    case msg: Msg ⇒ sendMessages(List(msg))

    case messages: Iterable[Msg] ⇒ sendMessages(messages)
  }

  @tailrec
  private def extractFrames(data: ByteString, frames: List[ByteString]): (ByteString, Seq[ByteString]) =
    if (data.length < headerSize)
      (data.compact, frames)
    else {
      val length = data.iterator.getInt

      if (length < 0 || length > maxSize)
        throw new IllegalArgumentException(s"received too large frame of size $length (max = $maxSize)")

      if (data.length >= length)
        extractFrames(data drop length, data.slice(headerSize, length) :: frames)
      else
        (data.compact, frames)
    }

  private def processFrame(frame: ByteString) = {
    val commandId = frame.iterator.getByte
    val byteString = frame.drop(commandIdSize)
    val is = byteString.iterator.asInputStream
    parseCommand(commandId, is)
  }

  private def sendMessages(messages: Iterable[Msg]): Unit = {
    val builder = ByteString.newBuilder
    val os = new DataOutputStream(builder.asOutputStream)
    for (msg ← messages) writeCommand(msg, os)
    send(builder.result)
  }

  private def send(byteString: ByteString) = {
    val length = byteString.size + headerSize
    if (length > maxSize) throw new IllegalArgumentException(s"send too large frame of size $length")
    tcpSender ! Write(
      ByteString.newBuilder
        .putInt(length)
        .append(byteString)
        .result
    )
  }

}

