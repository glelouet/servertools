package fr.lelouet.servertools.temperature.remote;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import fr.lelouet.servertools.temperature.ServerConnection;
import fr.lelouet.servertools.temperature.ServerConnection.SensorsEntry;

/** exports a sensors to be available on the network
 * @author guillaume */
public class SensorsExporter {

  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
      .getLogger(SensorsExporter.class);

  public static final int DEFAULT_PORT = 8765;

  public static final String PORT_ARG = "-p";

  /** export a server connection to be available on the network <br />
   * uses nio to perform low-overhead answers in one thread.
   * @param connection
   * @param localport */
  public static void export(ServerConnection connection, int localport) {
    try {
      Selector selector = SelectorProvider.provider().openSelector();
      ServerSocketChannel sChan = ServerSocketChannel.open();
      InetSocketAddress iaddr = new InetSocketAddress(localport);
      sChan.configureBlocking(false);
      sChan.socket().bind(iaddr);
      System.err.println("Running on port:" + sChan.socket().getLocalPort());
      sChan.register(selector, SelectionKey.OP_ACCEPT);
      HashMap<SelectionKey, ExternalConnection> sockets = new HashMap<SelectionKey, ExternalConnection>();
      while (selector.select() > 0) {
        for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i
            .hasNext();) {
          SelectionKey key = i.next();
          i.remove();
          if (key.isConnectable()) {
            ((SocketChannel) key.channel()).finishConnect();
          }
          if (key.isAcceptable()) {
            // accept connection
            SocketChannel client = sChan.accept();
            client.configureBlocking(false);
            client.socket().setTcpNoDelay(true);
            ExternalConnection ec = new ExternalConnection();
            ec.socketChannel = client;
            sockets.put(client.register(selector, SelectionKey.OP_READ), ec);
          }
          if (key.isReadable()) {
            handleRead(sockets.get(key), connection);
          }
        }
      }
    }
    catch (IOException e) {
      logger.warn("", e);
    }
  }

  static class ExternalConnection {
    public ByteBuffer buffer = ByteBuffer.allocate(1024);
    public SocketChannel socketChannel;
  }

  /** new data is available to read on the connecton ec. */
  public static void handleRead(ExternalConnection ec,
      ServerConnection connection) {
    try {
      ec.socketChannel.read(ec.buffer);
      String s = ec.buffer.toString();
      int idx = 0;
      while ((idx = s.indexOf("\n")) > 0) {
        idx = s.indexOf("\n", idx);
        String order = s.substring(0, idx);
        s = s.substring(idx + 1);
        ec.socketChannel.write(ByteBuffer.wrap(answer(order, connection)
            .getBytes()));
        System.err.println("after answering " + order + ", buffer=" + s);
      }
      ec.buffer.reset();
      ec.buffer.asCharBuffer().put(s);
    }
    catch (IOException e) {
      logger.warn("", e);
    }
  }

  /** send {@value #RETRIEVEORDER} to retrieve a sensorsEntry */
  public static final String RETRIEVEORDER = "retrieve";

  /** send {@value #GETORDER}[id] to retrieve the temperature of sensor id */
  public static final String GETORDER = "get ";

  /** executes an order on a serverconnection
   * @param order the String order to execute
   * @param connection the sensors to get the temperature from.
   * @return the resulting answer of the order. */
  public static String answer(String order,
      ServerConnection connection) {
    System.err.println("order : " + order);
    if (order == null || order.length() == 0) {
      return null;
    }
    if (RETRIEVEORDER.equals(order)) {
      return plainSensorsEntry(connection.retrieve().get());
    } else if (order.startsWith(GETORDER)) {
      String key = order.substring(GETORDER.length());
      return "" + connection.getSensor(key).retrieve().get();
    } else {
      logger.warn("order unknown : " + order);
    }
    return null;
  }

  /** @param plain a description of an entry produced by
   * {@link #plainSensorsEntry(SensorsEntry)}
   * @return the entry corresponding to the plain string */
  public static SensorsEntry parseSensorsEntry(String plain) {
    SensorsEntry ret = new SensorsEntry();
    String[] vals = plain.split("\n");
    ret.date = Long.parseLong(vals[0]);
    for (int i = 1; i < vals.length; i++) {
      String[] keyval = vals[i].split(" : ");
      ret.put(keyval[0], Double.parseDouble(keyval[1]));
    }
    return null;
  }

  /** @param entry the entry to convert
   * @return a plain string corresponding to the entry */
  public static String plainSensorsEntry(SensorsEntry entry) {
    StringBuilder sb = new StringBuilder("" + entry.date);
    for (Entry<String, Double> e : entry.entrySet()) {
      sb.append("\n").append(e.getKey()).append(" : ").append(e.getValue());
    }
    return sb.toString();
  }

}
