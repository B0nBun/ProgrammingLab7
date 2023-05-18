package itmo.app.client;

import itmo.app.shared.ClientRequest;
import itmo.app.shared.ServerResponse;
import itmo.app.shared.Utils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class BlockingChannelWrapper implements AutoCloseable {

    SocketChannel channel;
    Selector selector;

    public BlockingChannelWrapper(SocketChannel channel) throws IOException {
        this.selector = Selector.open();
        channel.register(selector, channel.validOps());
        this.channel = channel;
    }

    public ServerResponse readResponse() throws IOException, ClassNotFoundException {
        while (true) {
            selector.select();
            var keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                var key = keys.next();
                keys.remove();

                if (key.isReadable()) {
                    ServerResponse response = responseFromChannel(channel);
                    return response;
                }
            }
        }
    }

    public void writeRequest(ClientRequest<Serializable, Serializable> request)
        throws IOException {
        while (true) {
            selector.select();
            var keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                var key = keys.next();
                keys.remove();

                if (key.isWritable()) {
                    ByteBuffer buffer = Utils.objectToBuffer(request);
                    buffer.flip();
                    this.channel.write(buffer);
                    return;
                }
            }
        }
    }

    private static ServerResponse responseFromChannel(ReadableByteChannel channel)
        throws IOException, ClassNotFoundException {
        var objectSizeBuffer = ByteBuffer.allocate(Integer.BYTES);
        channel.read(objectSizeBuffer);
        objectSizeBuffer.flip();
        int objectSize = objectSizeBuffer.getInt();

        var objectBuffer = ByteBuffer.allocate(Integer.BYTES + objectSize);
        channel.read(objectBuffer);
        try (
            var byteArrayStream = new ByteArrayInputStream(
                objectBuffer.slice(Integer.BYTES, objectSize).array()
            );
            var objectInputStream = new ObjectInputStream(byteArrayStream);
        ) {
            return (ServerResponse) objectInputStream.readObject();
        }
    }

    @Override
    public void close() throws IOException {
        this.selector.close();
        if (this.channel.isOpen()) this.channel.close();
    }
}
