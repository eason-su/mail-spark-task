package com.chongdianleme.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL 基本操作 通过它,可以很轻松的使用 JDBC 来操纵数据库
 *
 * @author Null
 */
public class SQLHelper {

	private static org.apache.log4j.Logger logger4j = org.apache.log4j.Logger.getLogger(SQLHelper.class);
	public static void main(String[] args)
	{

	}
	public static Connection getRecmConnection(String ip,int port,String database,String user,String password) {
		try {
			// 获取驱动,不同版本的驱动,语句有所不同
			Class.forName("org.gjt.mm.mysql.Driver");
		} catch (ClassNotFoundException ex) {
			logger4j.error("getRecmConnection:", ex);
		}
		try {
			return DriverManager.getConnection("jdbc:mysql://"+ip+":"+port+"/"+database+"?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true", user, password);
		} catch (SQLException ex) {
			logger4j.error("getRecmConnection:", ex);
			return null;
		}
	}
	//365waiwang
	public static Connection getConnection()
	{
		return SQLHelper.getRecmConnection("172.16.5.159", 3306, "cdlm", "ruite",
				"ruite");
	}
	//sqoop
	/*public static Connection getConnection()
	{
		return SQLHelper.getRecmConnection("106.12.200.196", 3306, "chongdianleme", "root",
				"chongdianleme888");
	}*/
	public static void insert(String tableName,String columns,
							  String value) {
		Connection conn = null;
		String sql = "insert into " + tableName + "(" + columns
				+ ") values(";
		String[] cols = columns.split(",");
		for (int i = 1; i < cols.length + 1; i++) {

			sql += i == 1 ? "?" : ",?";
		}
		sql += ");";
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(sql);
			int rsOrder = 1;
			//for (String item : values)
			{
				String[] arr = value.split(",");
				for (int i = 0; i < arr.length; i++) {
					pstmt.setObject(i + 1, arr[i]);
				}
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			conn.commit();
		} catch (Exception ex) {
			try {
				ex.printStackTrace();
				conn.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static JSONArray resultSetToJson(ResultSet rs) throws SQLException,JSONException
	{
		// json数组
		JSONArray array = new JSONArray();

		// 获取列数
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		// 遍历ResultSet中的每条数据
		while (rs.next()) {
			JSONObject jsonObj = new JSONObject();
			// 遍历每一列
			for (int i = 1; i <= columnCount; i++) {
				String columnName =metaData.getColumnLabel(i);
				String value = rs.getString(columnName);
				jsonObj.put(columnName, value);
			}
			array.add(jsonObj);
		}
		return array;
	}
	/**
	 * 返回一个 ResultSet的json
	 * @param sql SQL 语句
	 * @return
	 */
	public static JSONArray query(String sql)  {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			//System.out.println(sql);
			resultSet = pstmt.executeQuery(sql);
			return resultSetToJson(resultSet);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (resultSet!=null)
			{
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	public static void insert(String tableName,String columns,
							  String value,String splitStr) {
		Connection conn = null;
		String sql = "insert into " + tableName + "(" + columns
				+ ") values(";
		String[] cols = columns.split(",");
		for (int i = 1; i < cols.length + 1; i++) {

			sql += i == 1 ? "?" : ",?";
		}
		sql += ");";
		//System.out.println(sql);
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(sql);
			int rsOrder = 1;
			//for (String item : values)
			{
				String[] arr = value.split(splitStr);
				for (int i=0;i<arr.length;i++) {
					pstmt.setObject(i+1, arr[i]);
				}
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			conn.commit();
		} catch (Exception ex) {
			try {
				ex.printStackTrace();
				conn.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static void insertBatch(String tableName,String columns,
								   List<String> valueList,String splitStr) {
		Connection conn = null;
		String sql = "insert into " + tableName + "(" + columns
				+ ") values(";
		String[] cols = columns.split(",");
		for (int i = 1; i < cols.length + 1; i++) {

			sql += i == 1 ? "?" : ",?";
		}
		sql += ");";
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(sql);
			for (String value : valueList)
			{
				String[] arr = value.split(splitStr);
				for (int i=0;i<arr.length;i++) {
					pstmt.setObject(i+1, arr[i]);
				}
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			conn.commit();
		} catch (Exception ex) {
			try {
				ex.printStackTrace();
				conn.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static void update(String sql,List<String> list) {
		//sql:UPDATE USER SET device=?,email=? WHERE phone=?
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < list.size(); i++) {
				pstmt.setObject(i + 1, list.get(i));
			}
			pstmt.addBatch();
			pstmt.executeBatch();
			conn.commit();
		} catch (Exception ex) {
			try {
				ex.printStackTrace();
				conn.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}
