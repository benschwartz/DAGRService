
public class DAGR {
	private String GUID;
	public void setGUID(String gUID) {
		GUID = gUID;
	}
	public void setParentGUID(String parentGUID) {
		this.parentGUID = parentGUID;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public void setModifiedTime(long modifiedTime) {
		this.modifiedTime = modifiedTime;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public void setType(String type) {
		this.type = type;
	}
	private String parentGUID;
	private String name;
	private String location;
	private String author;
	private Long createTime;
	private Long modifiedTime;
	private Long size;
	private String type;
	
	public DAGR(){
		
	}
	public DAGR(String gUID, String parentGUID, String name, String location,
			String author, Long createTime, Long modifiedTime, Long size,
			String type) {
		GUID = gUID;
		this.parentGUID = parentGUID;
		this.name = name;
		this.location = location;
		this.author = author;
		this.createTime = createTime;
		this.modifiedTime = modifiedTime;
		this.size = size;
		this.type = type;
	}
	public String getGUID() {
		return GUID;
	}
	public String getParentGUID() {
		return parentGUID;
	}
	public String getName() {
		return name;
	}
	public String getLocation() {
		return location;
	}
	public String getAuthor() {
		return author;
	}
	public long getCreateTime() {
		return createTime;
	}
	public long getModifiedTime() {
		return modifiedTime;
	}
	public long getSize() {
		return size;
	}
	public String getType() {
		return type;
	}
	
}
