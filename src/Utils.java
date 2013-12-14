import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {
	private static Logger LOGGER = Logger.getLogger("DAGR");
	private static final String URL;
	private static final String user;
	private static final String password;
	private static Connection conn = null;

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			throw new ExceptionInInitializerError(e);
		}
		Properties props = new Properties();
		InputStream iStream = Utils.class
				.getResourceAsStream("creds.properties");
		try {
			if (iStream == null) {
				throw new IOException(
						"resource creds.properties does not exist on the classpath");
			}
			props.load(iStream);
			iStream.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Could not load properties", e);
			throw new ExceptionInInitializerError(e);
		}
		URL = props.getProperty("URL");
		user = props.getProperty("user");
		password = props.getProperty("password");
		connect();
	}
	

	public static boolean connect() {
		if (conn != null) {
			LOGGER.log(Level.WARNING, "connection already open");
			return false;
		}
		try {
			conn = DriverManager.getConnection("jdbc:mysql://" + URL, user,
					password);
			return true;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "could not get connection", e);
			return false;
		}
	}

	public static void disconnnect() {
		if (conn == null) {
			LOGGER.log(Level.WARNING, "close called on null connection");
			return;
		}
		try {
			conn.close();
			conn = null;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "could not close connection", e);
		}
	}

	public static void executeStatement() {

	}

	public static void executeQuery() {

	}

	public static boolean insertDAGR(String GUID, String name,
			long create_date, long modify_date, String location,
			String parentGUID, String author, String type, long size) {
		GUID = encodeString(GUID);
		name = encodeString(name);
		location = encodeString(location);
		parentGUID = encodeString(parentGUID);
		author = encodeString(author);
		type = encodeString(type);
		String existingGUID = null;
		try {
			existingGUID = containsDAGR(name, location);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE,
					"Could not determine if the object is in the DAGR", e);
			disconnnect();
			connect();
			return false;
		}
		if (existingGUID != null) {
			LOGGER.warning(name + " is already present in the DAGR with GUID "
					+ existingGUID);
			return false;
		}
		try {
			Statement stmt = conn.createStatement();
			// AuthorID
			int authorID = -1;
			ResultSet rs = stmt
					.executeQuery("Select author_id from author where name='"
							+ author + "';");
			if (rs.next()) {
				authorID = rs.getInt("author_id");
			} else {
				rs.close();
				String insert = "INSERT INTO author (name) VALUES ('" + author
						+ "');";
				LOGGER.info(insert);
				stmt.execute(insert);
				rs = stmt
						.executeQuery("Select author_id from author where name='"
								+ author + "';");
				if (!rs.next()) {
					rs.close();
					stmt.close();
					LOGGER.log(Level.SEVERE, "could not insert DAGR with GUID "
							+ GUID);
					return false;
				}
				authorID = rs.getInt("author_id");
				rs.close();

			}
			// Type
			rs = stmt.executeQuery("Select type from type where type = '"
					+ type + "'");
			if (!rs.next()) {
				stmt.execute("INSERT INTO type (type) VALUES ('" + type + "');");
			}
			rs.close();
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO dagr (GUID,name,date_created,date_modified,location,parent_GUID,author_id,type,size) ");
			sql.append("VALUES (");
			sql.append("'" + GUID + "',");
			sql.append("'" + name + "',");
			sql.append(create_date + ",");
			sql.append(modify_date + ",");
			sql.append("'" + location + "',");
			sql.append("'" + parentGUID + "',");
			sql.append(authorID + ",");
			sql.append("'" + type + "',");
			sql.append(size);
			sql.append(");");
			LOGGER.info(sql.toString());
			stmt.execute(sql.toString());
			stmt.close();
			return true;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not insert DAGR with GUID " + GUID,
					e);
			disconnnect();
			connect();
			return false;
		}
	}

	public static boolean insertDAGR(DAGR dagr) {
		return insertDAGR(dagr.getGUID(), dagr.getName(), dagr.getCreateTime(),
				dagr.getModifiedTime(), dagr.getLocation(),
				dagr.getParentGUID(), dagr.getAuthor(), dagr.getType(),
				dagr.getSize());
	}

	private static String containsDAGR(String name, String location)
			throws SQLException {
		String existingGUID = null;
		Statement stmt = conn.createStatement();
		String sql = "SELECT guid FROM dagr where name='" + name
				+ "' AND location='" + location + "';";
		LOGGER.info("Checking for: " + sql);
		ResultSet rs = stmt.executeQuery(sql);
		if (rs.next()) {
			existingGUID = rs.getString("guid");
		}
		rs.close();
		stmt.close();
		return existingGUID;
	}

	public static String getFileSystemUUID() {
		return "FS_ROOT";
	}

	private static String encodeString(String str) {
		return str.replace("\\", "\\\\").replace("'", "\'")
				.replace("\"", "\\\"");
	}

	public static List<DAGR> getDAGRs(String GUID, String parentGUID,
			String location, String type, String author, String name,
			String size, String createTime, String modifiedTime) {
		List<DAGR> DAGRs = new ArrayList<DAGR>();
		String sql = getSQL(GUID, parentGUID, location, type, author, name,
				size, createTime, modifiedTime);
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String guid = rs.getString("guid");
				String parent = rs.getString("parent_guid");
				String loc = rs.getString("location");
				String ty = rs.getString("type");
				String nm = rs.getString("name");
				String auth = rs.getString("author.name");
				BigDecimal cTime = rs.getBigDecimal("date_created");
				BigDecimal mTime = rs.getBigDecimal("date_modified");
				BigDecimal sz = rs.getBigDecimal("size");
				Long cT = null;
				if (cTime != null) {
					cT = cTime.longValue();
				}
				Long mT = null;
				if (mTime != null) {
					mT = mTime.longValue();
				}
				Long siz = null;
				if (sz != null) {
					siz = sz.longValue();
				}
				DAGR dagr = new DAGR(guid, parent, nm, loc, auth, cT, mT, siz,
						ty);
				DAGRs.add(dagr);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not execute query: " + sql, e);
			return null;
		}
		return DAGRs;
	}

	private static String getSQL(String GUID, String parentGUID,
			String location, String type, String author, String name,
			String size, String createTime, String modifiedTime) {
		List<String> filters = new ArrayList<String>();
		if (GUID != null) {
			filters.add("guid='" + GUID + "'");
		}
		if (parentGUID != null) {
			filters.add("parent_guid='" + parentGUID + "'");
		}
		if (location != null) {
			filters.add("location='" + location + "'");
		}
		if (type != null) {
			filters.add("type='" + type + "'");
		}
		if (author != null) {
			filters.add("author.name='" + author + "'");
		}
		if (name != null) {
			filters.add("dagr.name='" + name + "'");
		}
		if (size != null) {
			filters.add("size=" + Long.parseLong(size));
		}
		if (createTime != null) {
			filters.add("date_created=" + Long.parseLong(createTime));
		}
		if (modifiedTime != null) {
			filters.add("date_modified=" + Long.parseLong(modifiedTime));
		}
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM dagr left outer join author on author.author_id=dagr.author_id");
		if (filters.size() > 0) {
			sql.append(" where ");
			sql.append(filters.get(0));
			for (int i = 1; i < filters.size(); i++) {
				sql.append(" AND " + filters.get(i));
			}
		}
		sql.append(";");
		return sql.toString();
	}
	
	public static void main(String args[]){
		
	}
}
