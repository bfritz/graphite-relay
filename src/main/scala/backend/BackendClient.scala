package graphite.relay.backend

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import org.apache.log4j.Logger
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.group.ChannelGroup
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender
import org.jboss.netty.util.HashedWheelTimer

import graphite.relay.Update
import graphite.relay.backend.codec.PickleEncoder
import graphite.relay.overflow.OverflowHandler


class BackendClient(channels: ChannelGroup, backend: Backend, reconnect: Int,
                    overflow: OverflowHandler) {

  private val log = Logger.getLogger(toString)
  private val timer = new HashedWheelTimer()
  timer.start()

  private val bootstrap = newBootstrap
  private val address = new InetSocketAddress(backend.host, backend.port)
  private val handler = new BackendClientHandler(channels, bootstrap, timer,
                              reconnect, log)
  
  bootstrap.setPipelineFactory(newPipleFactory)
  bootstrap.setOption("remoteAddress", address)

  handler.connect()

  def apply(update: Update) {
    if(handler.isAvailable) {
      handler(update)
    } else {
      overflow(update)
    }
  }

  def shutdown() = {
    timer.stop()
    bootstrap.releaseExternalResources()
  }

  private def newBootstrap = new ClientBootstrap(
    new NioClientSocketChannelFactory(
      Executors.newFixedThreadPool(numCores),
      Executors.newFixedThreadPool(numCores)))

  private def numCores = Runtime.getRuntime.availableProcessors

  private def newPipleFactory = new ChannelPipelineFactory {

    def getPipeline = {
      val pipeline = Channels.pipeline
    
      val prepender = new LengthFieldPrepender(4)
      val encoder = new PickleEncoder()

      pipeline.addLast("encoder", prepender)
      pipeline.addLast("pickleEncoder", encoder) 
      pipeline.addLast("handler", handler)

      pipeline
    }
  }

  override def toString =
    "Backend[%s:%s]".format(backend.host, backend.port)
}
