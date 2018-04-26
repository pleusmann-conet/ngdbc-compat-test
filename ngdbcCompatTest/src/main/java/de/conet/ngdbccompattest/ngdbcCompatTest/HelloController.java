package de.conet.ngdbccompattest.ngdbcCompatTest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sap.db.jdbcext.DataSourceSAP;
import com.sap.xs.env.Credentials;
import com.sap.xs.env.Service;
import com.sap.xs.env.VcapServices;

@Controller
@EnableAutoConfiguration
public class HelloController {

  @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/plain")
  @ResponseBody
  String home() {
    StringBuilder builder = new StringBuilder();
    builder.append("Hello World !!");

    builder.append("\n\nJDBC connection available: ");
    try {
      Connection conn = getConnection();
      if (conn != null) {
        builder.append("yes");
        builder.append("\n\nCurrent Hana DB user:\n");
        String userName = getCurrentUser(conn);
        builder.append(userName);
        builder.append("\n\nCurrent Hana schema:\n");
        builder.append(getCurrentSchema(conn));
      } else {
        builder.append("no");
      }
    } catch (SQLException e) {
      builder.append("no");
    }

    return builder.toString();
  }

  private String getCurrentUser(Connection conn) throws SQLException {
    String currentUser = "";
    PreparedStatement prepareStatement = conn.prepareStatement("SELECT CURRENT_USER \"current_user\" FROM DUMMY;");
    ResultSet resultSet = prepareStatement.executeQuery();
    int column = resultSet.findColumn("current_user");
    while (resultSet.next()) {
      currentUser += resultSet.getString(column);
    }
    return currentUser;
  }

  private String getCurrentSchema(Connection conn) throws SQLException {
    String currentSchema = "";
    PreparedStatement prepareStatement = conn.prepareStatement("SELECT CURRENT_SCHEMA \"current_schema\" FROM DUMMY;");
    ResultSet resultSet = prepareStatement.executeQuery();
    int column = resultSet.findColumn("current_schema");
    while (resultSet.next()) {
      currentSchema += resultSet.getString(column);
    }
    return currentSchema;
  }

  private Connection getConnection() throws SQLException {
    VcapServices services = VcapServices.fromEnvironment();

    // find HDI service by label "hana"
    Service service = services.findService("hana", "", "");
    if (service == null) {
      return null;
    }

    Credentials credentials = service.getCredentials();
    DataSourceSAP dataSource = new DataSourceSAP();

    dataSource.setUser(credentials.getUser());
    dataSource.setPassword(credentials.getPassword());
    dataSource.setPort(Integer.parseInt(credentials.getPort()));
    dataSource.setServerName(credentials.getHost());

    return dataSource.getConnection();
  }

  public static void main(String[] args) throws Exception {
    SpringApplication.run(HelloController.class, args);
  }
}
