package com.nikitatomilov.yaprofiiotbackends.communication

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.nikitatomilov.yaprofiiotbackends.communication.Message.Companion.PING
import com.nikitatomilov.yaprofiiotbackends.services.PingService
import com.nikitatomilov.yaprofiiotbackends.util.Utils
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.system.exitProcess

@Service
class UDPGateway(
  @Value("#{environment.NRF_GATEWAY_ADDRESS}") private val nrfGatewayAddress: String,
  private val pingService: PingService
) {

  private lateinit var socket: DatagramSocket
  private val responseMap = ConcurrentHashMap<Int, Message>()

  @PostConstruct
  fun startGateway() {
    logger.warn { "Starting UDP Gateway" }
    logger.warn { "Outbound connection to $nrfGatewayAddress:$NRF_GATEWAY_PORT" }
    logger.warn { "Inbound connection to port $LISTEN_PORT" }

    try {
      socket = DatagramSocket(LISTEN_PORT)
      socket.soTimeout = 5000
    } catch (ex: Exception) {
      ex.printStackTrace()
      exitProcess(-1)
    }
    setupListenerThread()
  }

  private fun setupListenerThread() {
    val executor = Executors.newSingleThreadExecutor(
        ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("udp-listener")
            .build())
    val receiveData = ByteArray(11)
    val receivePacket = DatagramPacket(receiveData, receiveData.size)
    executor.submit {
      while (true) {
        try {
          socket.receive(receivePacket)
          val msg = Message.fromBytes(receivePacket.data)
          if (msg.type == PING) {
            pingService.registerPing(msg.nodeID, msg.timestamp)
          } else {
            responseMap[msg.nodeID] = msg
          }
        } catch (stex: SocketTimeoutException) {
          //do nothing on socket timeout
        } catch (ex: Exception) {
          ex.printStackTrace()
        }
      }
    }
  }

  fun clearMessage(nodeID: Int) {
    responseMap.remove(nodeID)
  }

  @Throws(IOException::class)
  fun retrieveMessageBlocking(nodeID: Int): Message {
    var ans: Message? = null
    val timeout = System.currentTimeMillis() + 1000
    while (System.currentTimeMillis() < timeout) {
      ans = responseMap[nodeID]
      if (ans != null) {
        break
      }
      Utils.delay(10)
    }
    if (ans == null) {
      throw IOException("Timeout")
    }
    return ans
  }

  fun retrieveMessage(nodeID: Int): Message? {
    return responseMap[nodeID]
  }

  @Synchronized
  @Throws(IOException::class)
  fun sendMessage(msg: Message) {
    val sendData = msg.toBytes()
    val sendPacket = DatagramPacket(
        sendData,
        sendData.size,
        InetAddress.getByName(nrfGatewayAddress),
        NRF_GATEWAY_PORT)
    clearMessage(msg.nodeID)
    socket.send(sendPacket)
  }

  companion object : KLogging() {
    private const val NRF_GATEWAY_PORT = 1337
    private const val LISTEN_PORT = 1338
  }
}