package foo.bar

import java.util.Properties
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig
import java.sql.Connection

case class JdbcQueryMetadata(
    params: List[JdbcQueryParameter],
    resultCols: List[JdbcQueryResultCol])
case class JdbcQueryParameter(jdbcType : Int, className : String)
case class JdbcQueryResultCol(
    jdbcType : Int,
    className : String,
    columnName : String,
    tableName : String
    )

// just a prototype instance, with everything hardcoded
object JdbcMetadataInferer {
  val props = new Properties()
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
  val config = new HikariConfig(props);
  val dataSource = new HikariDataSource(config);
  setUpHsql()

  def setUpHsql() {
    withConnection { connection =>
      connection.prepareStatement("create table organizations (id integer, name varchar(255), city varchar(255), address varchar(255), PRIMARY KEY (id));").execute();
      connection.prepareStatement("insert into organizations (id, name, city, address)" + "values (1, 'org1', 'horlovka', 'pobedy 13');").execute();
      connection.prepareStatement("create table users (id integer, organizationId integer, firstName varchar(255), lastName varchar(255), city varchar(255), address varchar(255), PRIMARY KEY (id), FOREIGN KEY (organizationId) REFERENCES Organizations(id));").execute();
      connection.prepareStatement("insert into users (id, organizationId, firstName, lastName, city, address)" + "values (1, 1, 'vasia', 'pupkin', 'horlovka', 'lenina 25');").execute();
    }
  }

  def infereMetadata(sql: String): JdbcQueryMetadata = withConnection { connection =>
    val statement = connection.prepareStatement(sql);
    val parameterMetadata = statement.getParameterMetaData();
    val resultMetadata = statement.getMetaData();
   
    val params = for ( i <- 1 to parameterMetadata.getParameterCount) yield {
      JdbcQueryParameter(
          parameterMetadata.getParameterType(i),
          parameterMetadata.getParameterClassName(i))
    }
    val cols = for ( i <- 1 to resultMetadata.getColumnCount ) yield {
      JdbcQueryResultCol(
          resultMetadata.getColumnType(i),
          resultMetadata.getColumnClassName(i),
          resultMetadata.getColumnName(i),
          resultMetadata.getTableName(i)
      )
    }
    JdbcQueryMetadata(params.toList, cols.toList)
  }

  def withConnection[T](f: Connection => T): T = {
    val connection = dataSource.getConnection
    try {
      f(connection)
    } finally {
      connection.close()
    }
  }
}