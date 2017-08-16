package chat;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class MACFrame implements Serializable {
	private static final long serialVersionUID = -4546155266440558579L;

	byte[] header = {0x12, 0x34, 0x56, 0x78};
	Byte[] frameControl = new Byte[] {0x01, 0x02};
	Byte sequenceNumber = 1;
	Byte destinationPANID = 2;
	Byte destinationAddress = 3;
	Byte sourcePANID = 4;
	Byte sourceAddress = 5;
	Byte framePayload = 6;
	Byte fcs = 7;			

	public MACFrame(final byte seq) {
		sequenceNumber = seq;
	}

	public byte[] getHeader() {
		return(header);
	}

	public ByteArrayOutputStream getData() {
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		ba.write(header, 0, header.length);
		ba.write(sequenceNumber);
		ba.write(destinationPANID);
		ba.write(destinationAddress);
		ba.write(sourcePANID);
		ba.write(sourceAddress);
		ba.write(framePayload);
		ba.write(fcs);			
		return(ba);
	}
}


