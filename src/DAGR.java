
public class DAGR {
	private String GUID;
	public void setGUID(String gUID) {
		GUID = gUID;
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
	private String name;
	private String location;
	private String author;
	private Long createTime;
	private Long modifiedTime;
	private Long size;
	private String type;
	
	public DAGR(){
		
	}
	public DAGR(String gUID, String name, String location,
			String author, Long createTime, Long modifiedTime, Long size,
			String type) {
		GUID = gUID;
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
	public String getName() {
		return name;
	}
	public String getLocation() {
		return location;
	}
	public String getAuthor() {
		return author;
	}
	public Long getCreateTime() {
		return createTime;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((GUID == null) ? 0 : GUID.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DAGR other = (DAGR) obj;
		if (GUID == null) {
			if (other.GUID != null)
				return false;
		} else if (!GUID.equals(other.GUID))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	public Long getModifiedTime() {
		return modifiedTime;
	}
	public Long getSize() {
		return size;
	}
	public String getType() {
		return type;
	}
	
}
