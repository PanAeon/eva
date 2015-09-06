package foo.bar;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


public class JdbcMetadataInferer {

	static DataSource ds;
	
	static {
		Properties props = new Properties();
		props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
		props.setProperty("dataSource.user", "postgres");
		
		props.setProperty("dataSource.password", "postgres");
		props.setProperty("dataSource.databaseName", "postgres");
		
//		props.setProperty("dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
//		props.setProperty("dataSource.user", "root");
//		props.setProperty("dataSource.password", "root");
//		props.setProperty("dataSource.databaseName", "mydb");
//		props.setProperty("dataSource.url", "jdbc:mysql://localhost/mydb?generateSimpleParameterMetadata=true");
		

		HikariConfig config = new HikariConfig(props);
		HikariDataSource dataSource = new HikariDataSource(config);
		ds = dataSource;
	}
	
	public static QueryMetadata infereMetadata(String sql) throws Exception {
		QueryMetadata meta = new QueryMetadata();
		Connection connection = ds.getConnection();
		PreparedStatement st = connection.prepareStatement(sql);
		//st.setInt(1, 23);
		
		ParameterMetaData parameterMetadata = st.getParameterMetaData();
		meta.nParams = parameterMetadata.getParameterCount();
		int[] types = new int[meta.nParams];
		for (int i = 1; i <= meta.nParams; i++) { 
			types[i-1] = parameterMetadata.getParameterType(i);
		}
		meta.paramTypes = types;
		
		// now result types....
		ResultSetMetaData resultMetadata = st.getMetaData();
		meta.nResults = resultMetadata.getColumnCount();
		meta.resultNames = new String[meta.nResults];
		meta.resultTypes = new int[meta.nResults];
		meta.tableNames = new String[meta.nResults];
		
		for (int i = 1; i <= meta.nResults; i++) {
			 meta.resultNames[i-1] = resultMetadata.getColumnName(i);
			 meta.resultTypes[i-1] = resultMetadata.getColumnType(i);
			 meta.tableNames[i-1]  = resultMetadata.getTableName(i);
		}
		
		return meta;
	}
}

class QueryMetadata {
	int nParams; // java.sql.Types
	int[] paramTypes;
	
	int nResults;
	int[] resultTypes;
	String[] resultNames;
	String[] tableNames;
	
	@Override 
	public String toString() {
		return "QueryMetadata(" 
	    + "\nnParams: " + nParams
	    + "\nparamTypes: " + Arrays.toString(paramTypes)
	    + "\nnResults: " + nResults
	    + "\nresultTypes: " + Arrays.toString(resultTypes)
	    + "\nresultNames: " + Arrays.toString(resultNames)
	    + "\ntableNames: " + Arrays.toString(tableNames)
	    + "\n)";
	}
	
}
