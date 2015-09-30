package foo.bar;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hsqldb.Server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class JdbcMetadataInferer {

	public static DataSource ds;

	static {
		try {
			Properties props = new Properties();
			// props.setProperty("dataSourceClassName",
			// "org.postgresql.ds.PGSimpleDataSource");
			// props.setProperty("dataSource.user", "postgres");
			//
			// props.setProperty("dataSource.password", "postgres");
			// props.setProperty("dataSource.databaseName", "postgres");

			// props.setProperty("dataSourceClassName",
			// "oracle.jdbc.pool.OracleDataSource");
			// props.setProperty("dataSource.url",
			// "jdbc:oracle:thin:@172.20.0.66:49161:xe");
			// props.setProperty("dataSource.user", "");
			// props.setProperty("dataSource.password", "");
			// props.setProperty("dataSource.implicitCachingEnabled", "true");

			Class.forName("org.hsqldb.jdbc.JDBCDataSource");

			props.setProperty("dataSourceClassName", "org.hsqldb.jdbc.JDBCDataSource");
			props.setProperty("dataSource.user", "sa");
			props.setProperty("dataSource.password", "");
			props.setProperty("dataSource.url", "jdbc:hsqldb:mem:redfoo");

			// props.setProperty("dataSourceClassName",
			// "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
			// props.setProperty("dataSource.user", "root");
			// props.setProperty("dataSource.password", "root");
			// props.setProperty("dataSource.databaseName", "mydb");
			// props.setProperty("dataSource.url",
			// "jdbc:mysql://localhost/mydb?generateSimpleParameterMetadata=true");

			HikariConfig config = new HikariConfig(props);
			HikariDataSource dataSource = new HikariDataSource(config);
			ds = dataSource;
			
			setUpHsql();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static void setUpHsql() throws Exception {
		Connection connection = ds.getConnection();
		connection.prepareStatement("create table organizations (id integer, name varchar(255), city varchar(255), address varchar(255), PRIMARY KEY (id));").execute();
		connection.prepareStatement("insert into organizations (id, name, city, address)" + "values (1, 'org1', 'horlovka', 'pobedy 13');").execute();
		connection.prepareStatement("create table users (id integer, organizationId integer, firstName varchar(255), lastName varchar(255), city varchar(255), address varchar(255), PRIMARY KEY (id), FOREIGN KEY (organizationId) REFERENCES Organizations(id));").execute();
		connection.prepareStatement("insert into users (id, organizationId, firstName, lastName, city, address)" + "values (1, 1, 'vasia', 'pupkin', 'horlovka', 'lenina 25');").execute();
		
		connection.close();
	}
	
	// if (sqlTypeCode == java.sql.Types.BLOB)

	public static QueryMetadata infereMetadata(String sql) throws Exception {
		QueryMetadata meta = new QueryMetadata();
		Connection connection = ds.getConnection();
		PreparedStatement st = connection.prepareStatement(sql);
		// st.setInt(1, 23);

		ParameterMetaData parameterMetadata = st.getParameterMetaData();
		meta.nParams = parameterMetadata.getParameterCount();
		int[] types = new int[meta.nParams];
		String[] classNames = new String[meta.nParams];
		for (int i = 1; i <= meta.nParams; i++) {
			types[i - 1] = parameterMetadata.getParameterType(i);
			classNames[i - 1] = parameterMetadata.getParameterClassName(i); // isNullable
		}
		meta.paramTypes = types;
		meta.paramClassNames = classNames;

		// now result types....
		ResultSetMetaData resultMetadata = st.getMetaData();
		meta.nResults = resultMetadata.getColumnCount();
		meta.resultNames = new String[meta.nResults];
		meta.resultTypes = new int[meta.nResults];
		meta.tableNames = new String[meta.nResults];
		meta.resultClassNames = new String[meta.nResults];

		for (int i = 1; i <= meta.nResults; i++) {
			meta.resultNames[i - 1] = resultMetadata.getColumnName(i);
			meta.resultTypes[i - 1] = resultMetadata.getColumnType(i);
			meta.tableNames[i - 1] = resultMetadata.getTableName(i);
			meta.resultClassNames[i - 1] = resultMetadata.getColumnClassName(i); 
		}

		return meta;
	}
}

class QueryMetadata {
	int nParams; // java.sql.Types
	int[] paramTypes;
	String[] paramClassNames;

	int nResults;
	int[] resultTypes;
	String[] resultClassNames;
	String[] resultNames;
	String[] tableNames;

	@Override
	public String toString() {
		return  ToStringBuilder.reflectionToString(this);
//		return "QueryMetadata(" + "\nnParams: " + nParams + "\nparamTypes: " + Arrays.toString(paramTypes)
//		        + "\nparamClasses: " + Arrays.toString(paramClassNames)
//				+ "\nnResults: " + nResults + "\nresultTypes: " + Arrays.toString(resultTypes) + "\nresultClassNames: "
//				+ Arrays.toString(resultClassNames) + "\nresultNames: "
//				+ Arrays.toString(resultNames) + "\ntableNames: " + Arrays.toString(tableNames) + "\n)";
	}

}
