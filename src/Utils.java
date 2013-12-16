import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.MediaType;

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
			conn = null;
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
			conn = null;
			LOGGER.log(Level.SEVERE, "could not close connection", e);
		}
	}

	public static void executeStatement() {

	}

	public static void executeQuery() {

	}

	public static String insertDAGR(DAGR dagr, String parentGUID) {
		return insertDAGR(dagr.getGUID(), dagr.getName(), dagr.getCreateTime(),
				dagr.getModifiedTime(), dagr.getLocation(), parentGUID,
				dagr.getAuthor(), dagr.getType(), dagr.getSize());
	}

	public static String insertDAGR(String GUID, String name, long create_date,
			long modify_date, String location, String parentGUID,
			String author, String type, long size) {
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
			return null;
		}
		if (existingGUID != null) {
			try {
				insertParentChild(parentGUID, existingGUID);
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE,
						"could not insert parent child relationship: {parent:"
								+ parentGUID + ", child:" + existingGUID + "}",
						e);
				disconnnect();
				connect();
			}
			LOGGER.warning(name + " is already present in the DAGR with GUID "
					+ existingGUID);

			return existingGUID;
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
				// LOGGER.info(insert);
				stmt.execute(insert);
				rs = stmt
						.executeQuery("Select author_id from author where name='"
								+ author + "';");
				if (!rs.next()) {
					rs.close();
					stmt.close();
					LOGGER.log(Level.SEVERE, "could not insert DAGR with GUID "
							+ GUID);
					return GUID;
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
			sql.append("INSERT INTO dagr (GUID,name,date_created,date_modified,location,author_id,type,size) ");
			sql.append("VALUES (");
			sql.append("'" + GUID + "',");
			sql.append("'" + name + "',");
			sql.append(create_date + ",");
			sql.append(modify_date + ",");
			sql.append("'" + location + "',");
			sql.append(authorID + ",");
			sql.append("'" + type + "',");
			sql.append(size);
			sql.append(");");
			// LOGGER.info(sql.toString());
			stmt.execute(sql.toString());
			stmt.close();
			insertParentChild(parentGUID, GUID);
			LOGGER.info("INSERT " + location + " " + name + " " + GUID);
			return GUID;
		} catch (SQLException e) {
			String DAGR = location + " " + name + " " + GUID;
			LOGGER.log(Level.SEVERE, "Could not insert DAGR\n" + DAGR, e);
			disconnnect();
			connect();
			return null;
		}
	}

	private static void insertParentChild(String parentGUID, String GUID)
			throws SQLException {
		Statement stmt = conn.createStatement();
		StringBuilder sql = new StringBuilder();
		sql.append("Select count(*) COUNT from dagr_parent_child where ");
		sql.append("parent = '" + parentGUID + "'");
		sql.append(" AND child = '" + GUID + "';");
		ResultSet rs = stmt.executeQuery(sql.toString());
		rs.next();
		int count = rs.getInt("COUNT");
		if (count == 1) {
			LOGGER.warning("parent child relationship already exists {parent: "
					+ parentGUID + ", child: " + GUID + "}");
			rs.close();
			stmt.close();
			return;
		}
		rs.close();
		stmt.close();
		stmt = conn.createStatement();
		sql = new StringBuilder();
		sql.append("INSERT INTO dagr_parent_child (parent,child) ");
		sql.append("VALUES (");
		sql.append("'" + parentGUID + "',");
		sql.append("'" + GUID + "'");
		sql.append(");");
		stmt.execute(sql.toString());
		stmt.close();

	}

	private static String containsDAGR(String name, String location)
			throws SQLException {
		String existingGUID = null;
		Statement stmt = conn.createStatement();
		String sql = "SELECT guid FROM dagr where name='" + name
				+ "' AND location='" + location + "';";
		// LOGGER.info("Checking for: " + sql);
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

	public static List<DAGR> getDAGRs(String GUID, String location,
			String type, String author, String name, String size,
			String c_start, String c_stop, String m_start, String m_stop) {
		String sql = getSQL(GUID, location, type, author, name, size, c_start,
				c_stop, m_start, m_stop);
		return queryDAGRs(sql);
	}

	private static List<DAGR> queryDAGRs(String sql) {
		List<DAGR> DAGRs = new ArrayList<DAGR>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String guid = rs.getString("guid");
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
				DAGR dagr = new DAGR(guid, nm, loc, auth, cT, mT, siz, ty);
				DAGRs.add(dagr);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not execute query: " + sql, e);
			disconnnect();
			connect();
			return null;
		}
		return DAGRs;
	}

	private static String getSQL(String GUID, String location, String type,
			String author, String name, String size, String c_start,
			String c_stop, String m_start, String m_stop) {
		List<String> filters = new ArrayList<String>();
		if (GUID != null) {
			filters.add("guid='" + GUID + "'");
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
		if (c_start != null && c_stop != null) {
			filters.add("date_created between " + Long.parseLong(c_start)
					+ " and " + Long.parseLong(c_stop));
		}
		if (m_start != null && m_stop != null) {
			filters.add("date_modified between " + Long.parseLong(m_start)
					+ " and " + Long.parseLong(m_stop));
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

	public static List<String> getParents(String GUID) {
		List<String> parents = new ArrayList<String>();
		StringBuilder sql = new StringBuilder();
		sql.append("Select * FROM dagr_parent_child where ");
		sql.append("child = '" + GUID + "';");
		List<String> results;
		try {
			results = getChildParents(sql.toString());
			for (String parent : results) {
				parents.add(parent);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not get parents of " + GUID, e);
			disconnnect();
			connect();
		}
		return Collections.unmodifiableList(parents);
	}

	public static List<String> getChildren(String GUID) {
		List<String> children = new ArrayList<String>();
		StringBuilder sql = new StringBuilder();
		sql.append("Select * FROM dagr_parent_child where ");
		sql.append("parent = '" + GUID + "';");
		List<String> results;
		try {
			results = getParentChildren(sql.toString());
			for (String child : results) {
				children.add(child);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not get parents of " + GUID, e);
			disconnnect();
			connect();
		}
		return Collections.unmodifiableList(children);
	}

	private static List<String> getParentChildren(String sql)
			throws SQLException {
		List<String> children = new ArrayList<String>();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		while (rs.next()) {
			children.add(rs.getString("child"));
		}
		rs.close();
		stmt.close();
		return Collections.unmodifiableList(children);
	}

	private static List<String> getChildParents(String sql) throws SQLException {
		ArrayList<String> parents = new ArrayList<String>();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		while (rs.next()) {
			parents.add(rs.getString("parent"));
		}
		rs.close();
		stmt.close();
		return Collections.unmodifiableList(parents);
	}

	public static void main(String args[]) {
		System.out.println(MediaType.APPLICATION_JSON.getName());
	}

	/**
	 * Get all the DAGRs in the List of GUIDs
	 * 
	 * @param parentGUIDs
	 * @return
	 */
	public static List<DAGR> getDAGRs(List<String> GUIDs) {
		if (GUIDs.isEmpty()) {
			return Collections.emptyList();
		}
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM dagr left outer join author on author.author_id=dagr.author_id");
		sql.append(" WHERE dagr.guid in (");
		for (String guid : GUIDs) {
			sql.append("'" + guid + "',");
		}
		sql.replace(sql.length() - 1, sql.length(), "");
		sql.append(") ORDER BY type,dagr.name;");
		// LOGGER.info(sql.toString());
		return queryDAGRs(sql.toString());
	}

	public static List<String> getTypes() {
		List<String> types = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			String sql = "SELECT type from type;";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				types.add(rs.getString("type"));
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not get types", e);
			disconnnect();
			connect();
		}
		return types;
	}

	public static List<String> getAuthors() {
		List<String> authors = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			String sql = "SELECT name from author;";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				authors.add(rs.getString("name"));
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not get authors", e);
			disconnnect();
			connect();
		}
		return authors;
	}

	public static Set<DAGR> deleteDAGR(String GUID, boolean cascade) {
		if (!cascade) {
			return new HashSet<DAGR>(batman(GUID));
		} else {
			Set<DAGR> deleted = new HashSet<DAGR>();
			recursiveDeleteDAGR(GUID, deleted);
			return deleted;
		}
	}

	private static void recursiveDeleteDAGR(String GUID, Set<DAGR> deleted) {
		Set<DAGR> orphans = new HashSet<DAGR>(batman(GUID));
		try {
			Statement stmt = conn.createStatement();
			String sql = "Delete from dagr where GUID='" + GUID + "';";
			stmt.execute(sql);
			stmt.close();
			deleted.addAll(getDAGRs(Arrays.asList(GUID)));
			if (!orphans.isEmpty()) {
				deleted.addAll(orphans);
				for (DAGR dagr : orphans) {
					recursiveDeleteDAGR(dagr.getGUID(), deleted);
				}
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not delete DAGR " + GUID, e);
			disconnnect();
			connect();
		}
	}

	/**
	 * Orphans children
	 * 
	 * @return list of orphaned DAGRs
	 */
	private static List<DAGR> batman(String GUID) {
		List<String> orphanGUIDs = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			String sql = "Select child from dagr_parent_child where parent='"
					+ GUID + "';";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				orphanGUIDs.add(rs.getString("child"));
			}
			rs.close();
			stmt.close();
			if (!orphanGUIDs.isEmpty()) {
				stmt = conn.createStatement();
				sql = "DELETE from dagr_parent_child where parent='" + GUID
						+ "';";
				stmt.execute(sql);
				stmt.close();
			}
			stmt = conn.createStatement();
			sql = "DELETE from dagr_parent_child where child='" + GUID + "';";
			stmt.execute(sql);
			stmt.close();
			stmt = conn.createStatement();
			sql = "Delete from dagr where GUID='" + GUID + "';";
			stmt.execute(sql);
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not get children of DAGR " + GUID,
					e);
			disconnnect();
			connect();
		}
		if (orphanGUIDs.isEmpty()) {
			return Collections.emptyList();
		}
		return getDAGRs(orphanGUIDs);
	}

	public static List<String> getKeywords(String GUID) {
		List<String> keywords = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			String sql = "Select keyword FROM dagr_keyword_tag where GUID='"
					+ GUID + "';";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				keywords.add(rs.getString("keyword"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not get keywords for DAGR " + GUID,
					e);
			disconnnect();
			connect();
		}
		return keywords;
	}

	public static List<String> addKeyword(String GUID, String keyword) {
		try {
			Boolean addKW = false;
			Statement stmt = conn.createStatement();
			String sql = "Select count(*) COUNT FROM keyword where keyword='"
					+ keyword + "';";
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			if (rs.getInt("COUNT") == 0) {
				addKW = true;
			}
			rs.close();
			stmt.close();
			if (addKW) {
				stmt = conn.createStatement();
				sql = "INSERT INTO keyword (keyword) VALUES ('" + keyword
						+ "');";
				stmt.execute(sql);
				stmt.close();
			}
			stmt = conn.createStatement();
			sql = "INSERT INTO dagr_keyword_tag (GUID,keyword) VALUES ";
			sql = sql + "('" + GUID + "','" + keyword + "');";
			stmt.execute(sql);
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not add keyword " + keyword
					+ "for DAGR " + GUID, e);
			disconnnect();
			connect();
		}
		return getKeywords(GUID);
	}

	public static List<String> deleteKeyword(String GUID, String keyword) {
		try {
			Boolean delKW = false;
			Statement stmt = conn.createStatement();
			String sql = "Select count(*) COUNT FROM dagr_keyword_tag where keyword='"
					+ keyword + "';";
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			if (rs.getInt("COUNT") == 1) {
				delKW = true;
			}
			rs.close();
			stmt.close();
			stmt = conn.createStatement();
			sql = "DELETE FROM dagr_keyword_tag where ";
			sql = sql + "GUID ='" + GUID + "' AND keyword = '" + keyword + "';";
			stmt.execute(sql);
			stmt.close();
			if (delKW) {
				stmt = conn.createStatement();
				sql = "DELETE FROM keyword where keyword ='" + keyword + "';";
				stmt.execute(sql);
				stmt.close();
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not delete keyword " + keyword
					+ "for DAGR " + GUID, e);
			disconnnect();
			connect();
		}
		return getKeywords(GUID);
	}

	public static List<DAGR> getOrphans() {
		List<String> orphans = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			String sql = "select GUID from dagr where GUID not in "
					+ "(select distinct child from dagr_parent_child);";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				orphans.add(rs.getString("GUID"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not get orphans", e);
			disconnnect();
			connect();
		}
		return getDAGRs(orphans);
	}

	public static List<DAGR> getSterile() {
		List<String> sterile = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			String sql = "select GUID from dagr where GUID not in "
					+ "(select distinct parent from dagr_parent_child);";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				sterile.add(rs.getString("GUID"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not get sterile DAGRs", e);
			disconnnect();
			connect();
		}
		return getDAGRs(sterile);
	}

	public static List<String> getKeywords() {
		List<String> keywords = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			String sql = "Select keyword from keyword order by keyword;";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				keywords.add(rs.getString("keyword"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not get keyword list", e);
			disconnnect();
			connect();
		}
		return keywords;
	}

	public static List<DAGR> getCreateTimeRange(long start, long stop) {
		List<String> GUIDs = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			String sql = "Select GUID from dagr where date_created between "
					+ start + " and " + stop + ";";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				GUIDs.add(rs.getString("GUID"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE,
					"Could not get DAGR list for create timerange [" + start
							+ "," + stop + "]", e);
			disconnnect();
			connect();
		}
		return getDAGRs(GUIDs);
	}

	public static List<DAGR> getModifiedTimeRange(long start, long stop) {
		List<String> GUIDs = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			String sql = "Select GUID from dagr where date_modified between "
					+ start + " and " + stop + ";";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				GUIDs.add(rs.getString("GUID"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE,
					"Could not get DAGR list for modified timerange [" + start
							+ "," + stop + "]", e);
			disconnnect();
			connect();
		}
		return getDAGRs(GUIDs);
	}

	public static List<DAGR> getReach(String GUID) {
		Set<String> GUIDs = new HashSet<String>();
		boolean firstTime = true;
		GUIDs.add(GUID);
		int size = GUIDs.size();
		while (size != GUIDs.size() || firstTime) {
			size = GUIDs.size();
			firstTime = false;
			try {
				Statement stmt = conn.createStatement();
				StringBuilder sql = new StringBuilder();
				sql.append("Select parent,child from dagr_parent_child where ");
				sql.append("parent in (");
				for (String guid : GUIDs) {
					sql.append("'" + guid + "',");
				}
				sql.replace(sql.length() - 1, sql.length(), "");
				sql.append(") OR child in (");
				for (String guid : GUIDs) {
					sql.append("'" + guid + "',");
				}
				sql.replace(sql.length() - 1, sql.length(), "");
				sql.append(");");
				ResultSet rs = stmt.executeQuery(sql.toString());
				while (rs.next()) {
					GUIDs.add(rs.getString("parent"));
					GUIDs.add(rs.getString("child"));
				}
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, "Could not finish reach query for "
						+ GUID, e);
				disconnnect();
				connect();
			}
		}
		return getDAGRs(new ArrayList<String>(GUIDs));
	}

	public static String getWebUUID() {
		return "WEB_ROOT";
	}

	public static void cleanUpAuthors() {
		try {
			List<Integer> authors = new ArrayList<Integer>();
			Statement stmt = conn.createStatement();
			String sql = "Select distinct author_id from dagr;";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				authors.add(rs.getInt("author_id"));
			}
			rs.close();
			stmt.close();
			stmt = conn.createStatement();
			StringBuilder sql2 = new StringBuilder();
			sql2.append("Delete from author where author_id not in (");
			for (int author : authors) {
				sql2.append("" + author + ",");
			}
			if (authors.size() > 0) {
				sql2.replace(sql2.length() - 1, sql2.length(), "");
			}
			sql2.append(");");
			stmt.execute(sql2.toString());
			stmt.close();
		} catch (SQLException e) {
			disconnnect();
			connect();
		}
	}

	public static void cleanUpTypes() {
		try {
			List<String> types = new ArrayList<String>();
			Statement stmt = conn.createStatement();
			String sql = "Select distinct type from dagr;";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				types.add(rs.getString("type"));
			}
			rs.close();
			stmt.close();
			stmt = conn.createStatement();
			StringBuilder sql2 = new StringBuilder();
			sql2.append("Delete from type where type not in (");
			for (String type : types) {
				sql2.append("'" + type + "',");
			}
			if (types.size() > 0) {
				sql2.replace(sql2.length() - 1, sql2.length(), "");
			}
			sql2.append(");");
			stmt.execute(sql2.toString());
			stmt.close();
		} catch (SQLException e) {
			disconnnect();
			connect();
		}
	}
}
