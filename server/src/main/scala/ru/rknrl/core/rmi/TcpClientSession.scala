//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core.rmi

import java.io.{DataOutputStream, InputStream}

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp.Event
import akka.util.ByteString
import ru.rknrl.Supervisor._
import ru.rknrl.logging.ActorLog

import scala.annotation.tailrec

case object CloseConnection

case object Ack extends Event

abstract class Msg(id: Byte)

abstract class TcpClientSession(var tcp: ActorRef) extends Actor with ActorLog {

  import akka.io.Tcp._

  override def supervisorStrategy = EscalateStrategy

  val headerSize = 4
  val commandIdSize = 1
  val maxSize = 512 * 1024
  implicit val byteOrder = java.nio.ByteOrder.LITTLE_ENDIAN

  protected def writeCommand(msg: Any, os: DataOutputStream): Unit

  protected def parseCommand(commandId: Byte, is: InputStream): Unit

  private var receiveBuffer = ByteString.empty

  private val sendBuffer = ByteString.newBuilder

  private var waitForAck = false

  def receive: Receive = {
    case Received(receivedData) ⇒
      val data = receiveBuffer ++ receivedData
      val (newBuffer, frames) = extractFrames(data, Nil)
      for (frame ← frames) processFrame(frame)
      receiveBuffer = newBuffer

    case _: ConnectionClosed ⇒
      log.debug("connection closed")
      context stop self

    case CloseConnection ⇒
      log.debug("close connection command")
      context stop self

    case msg: Msg ⇒ sendMessages(List(msg))

    case messages: Iterable[Msg] ⇒ sendMessages(messages)

    case CommandFailed(e) ⇒
      log.error("command failed " + e)
      context stop self

    case Ack ⇒
      waitForAck = false
      flush()
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

  private def processFrame(frame: ByteString): Unit = {
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

  private def send(byteString: ByteString): Unit = {
    val length = byteString.size + headerSize
    if (length > maxSize) throw new IllegalArgumentException(s"send too large frame of size $length")
    sendBuffer.putInt(length)
    sendBuffer.append(byteString)
    flush()
  }

  private def flush(): Unit =
    if (!waitForAck && sendBuffer.length > 0) {
      waitForAck = true
      tcp ! Write(sendBuffer.result.compact, Ack)
      sendBuffer.clear()
    }
}


