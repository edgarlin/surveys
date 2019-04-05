package com.survey;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/proc")
@SuppressWarnings("unused")
public class SurveyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection conn = null;

	private Gson gson = new Gson();

	private class PARMJSON {
		public boolean init(ResultSet rs) {
			try {
				this.SURVEYID = rs.getBigDecimal("surveyid");
				this.MODE = rs.getString("mode");
				this.FORMID = rs.getString("formid");
				this.DATAID = rs.getString("dataid");
				this.DATAJSON = rs.getString("datajson");
				this.MEMO = rs.getString("memo");
				this.SAVETIME = rs.getTimestamp("savetime");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;

		}

		public BigDecimal SURVEYID;
		public String MODE;
		public String FORMID;
		public String DATAID;
		public String DATAJSON;
		public String MEMO;
		public Timestamp SAVETIME;
	}

	private class PARMFORM {
		public String name;
		public String value;
	}

	public SurveyServlet() {
		super();
		try {
			Class.forName("org.postgresql.Driver");
			this.conn = DriverManager.getConnection("jdbc:postgresql://192.168.0.107:5432/admin", "admin", "admin");
			// this.connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFormList() {
		List<PARMJSON> lists = new ArrayList<PARMJSON>();
		try (Statement stmt = this.conn.createStatement()) {
			ResultSet rs = stmt.executeQuery("select * from surveys where mode = 'FORM' order by surveyid");
			while (rs.next()) {
				PARMJSON parm = new PARMJSON();
				parm.init(rs);
				lists.add(parm);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (gson.toJson(lists));
	}

	public String getForm(String FORMID) {
		PARMJSON parm = new PARMJSON();
		String sql = "select * from surveys where mode = 'FORM' and formid = ? ";
		try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
			ps.setString(1, FORMID);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				parm.init(rs);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (gson.toJson(parm));
	}

	public String getLatestData(String FORMID) {
		PARMJSON parm = new PARMJSON();
		String sql = "select * from surveys where surveyid = "
				+ " (select max(surveyid) from surveys where mode = 'DATA' and formid = ? group by formid)";
		try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
			ps.setString(1, FORMID);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				parm.init(rs);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (gson.toJson(parm));
	}

	public String getPrevData(String FORMID, BigDecimal SURVEYID) {
		PARMJSON parm = new PARMJSON();
		String sql = "select * from surveys where mode = 'DATA' and formid = ? and surveyid <= ? order by surveyid desc ";
		try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
			ps.setString(1, FORMID);
			ps.setBigDecimal(2, SURVEYID);
			ResultSet rs = ps.executeQuery();
			int rownum = 0;
			while (rs.next()) {
				if (rownum > 1) {
					break;
				}
				parm.init(rs);
				rownum++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (gson.toJson(parm));
	}

	public String getNextData(String FORMID, BigDecimal SURVEYID) {
		PARMJSON parm = new PARMJSON();
		String sql = "select * from surveys where mode = 'DATA' and formid = ? and surveyid >= ? order by surveyid ";
		try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
			ps.setString(1, FORMID);
			ps.setBigDecimal(2, SURVEYID);
			ResultSet rs = ps.executeQuery();
			int rownum = 0;
			while (rs.next()) {
				if (rownum > 1) {
					break;
				}
				parm.init(rs);
				rownum++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (gson.toJson(parm));
	}

	public String saveData(String FORMID, String DATAJSON) {
		PARMJSON parm = new PARMJSON();
		String sql = "insert into surveys (surveyid,mode,formid,dataid,datajson,memo,savetime) "
				+ " values (nextval('surveyseq'),'DATA',?,null,?::JSON,null,current_timestamp) ";
		try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
			ps.setString(1, FORMID);
			ps.setObject(2, DATAJSON);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (gson.toJson(parm));
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		if (this.conn == null) {
			response.getWriter().write("{\"errMsg\":\"connection failed\"}");
			return;
		}

		String action = request.getParameter("action");
		String json = request.getParameter("json");
		String outJson = "";

		PARMFORM[] parmForms = null;
		String parmFORMID = "";
		String parmSURVEYID = "";
		String parmDATAJSON = "";

		try {
			parmForms = gson.fromJson(json, PARMFORM[].class);
			parmFORMID = Arrays.stream(parmForms).filter(parmForm -> "FORMID".equals(parmForm.name))
					.map(parmForm -> parmForm.value).findFirst().orElse("");
			parmSURVEYID = Arrays.stream(parmForms).filter(parmForm -> "SURVEYID".equals(parmForm.name))
					.map(parmForm -> parmForm.value).findFirst().orElse("0");
			parmDATAJSON = Arrays.stream(parmForms).filter(parmForm -> "DATAJSON".equals(parmForm.name))
					.map(parmForm -> parmForm.value).findFirst().orElse("{}");
		} catch (Exception e) {
			System.out.println("some error");
		}

		if ("FORM".equals(action)) {
			PARMJSON parmJson = gson.fromJson(json, PARMJSON.class);
			outJson = getForm(parmJson.FORMID);
		} else if ("DATA".equals(action)) {
			outJson = getLatestData(parmFORMID);
		} else if ("SAVE".equals(action)) {
			outJson = saveData(parmFORMID, parmDATAJSON);
		} else if ("PREV".equals(action)) {
			outJson = getPrevData(parmFORMID, new BigDecimal(parmSURVEYID));
		} else if ("NEXT".equals(action)) {
			outJson = getNextData(parmFORMID, new BigDecimal(parmSURVEYID));
		} else {
			outJson = getFormList();
		}

		response.getWriter().write(outJson);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
