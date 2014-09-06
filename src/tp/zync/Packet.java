package tp.zync;

import java.io.Serializable;
import java.util.HashMap;

public class Packet implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String pcktType;
	public String fileName;
//	public String fileType;
	public int size;
	public long fileSize;
	public byte bArray[];
	public HashMap<String, Long> fileMap;

}
