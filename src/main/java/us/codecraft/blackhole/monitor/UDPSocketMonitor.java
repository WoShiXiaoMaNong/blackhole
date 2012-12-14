package us.codecraft.blackhole.monitor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import us.codecraft.blackhole.connection.UDPConnection;

public class UDPSocketMonitor extends Thread {

	private Logger log = Logger.getLogger(this.getClass());

	private final InetAddress addr;
	private final int port;
	private static final short udpLength = 512;
	private final DatagramSocket socket;
	private ExecutorService executorService = Executors.newFixedThreadPool(100);

	public UDPSocketMonitor(final InetAddress addr, final int port)
			throws SocketException {
		super();
		this.addr = addr;
		this.port = port;

		socket = new DatagramSocket(port, addr);

		this.setDaemon(true);
	}

	@Override
	public void run() {

		log.info("Starting UDP socket monitor on address "
				+ this.getAddressAndPort());

		while (true) {
			try {

				byte[] in = new byte[udpLength];
				DatagramPacket indp = new DatagramPacket(in, in.length);

				indp.setLength(in.length);
				socket.receive(indp);
				executorService.execute(new UDPConnection(socket, indp));

				log.debug("UDP connection from " + indp.getSocketAddress());

			} catch (SocketException e) {

				// This is usally thrown on shutdown
				log.debug("SocketException thrown from UDP socket on address "
						+ this.getAddressAndPort() + ", " + e);
				break;
			} catch (IOException e) {

				log.info("IOException thrown by UDP socket on address "
						+ this.getAddressAndPort() + ", " + e);
			}
		}
		log.info("UDP socket monitor on address " + getAddressAndPort()
				+ " shutdown");
	}

	public void closeSocket() throws IOException {

		log.info("Closing TCP socket monitor on address " + getAddressAndPort()
				+ "...");

		this.socket.close();
	}

	public String getAddressAndPort() {

		return addr.getHostAddress() + ":" + port;
	}
}