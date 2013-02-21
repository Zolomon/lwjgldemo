package game;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import utils.PNGDecoder;
import utils.PNGDecoder.Format;

public class Texture {
	private PNGDecoder decoder;
	private ByteBuffer buf;

	public Texture(String filename) {
		try {
			InputStream in = getClass().getResourceAsStream(filename);
			decoder = new PNGDecoder(in);

			ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth()
					* decoder.getHeight());
			decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			buf.flip();

			this.buf = buf;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getWidth() {
		return decoder.getWidth();
	}

	public int getHeight() {
		return decoder.getHeight();
	}

	public ByteBuffer getBuffer() {
		return buf;
	}

}
