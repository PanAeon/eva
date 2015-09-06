package foo.bar;

import java.io.StringReader;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

public class TypeInferer {
	static void infereTypes(String sql) throws Exception {
		 CCJSqlParserManager parserManager = new CCJSqlParserManager();
		Statement stmt = parserManager.parse(new StringReader(sql));
		System.out.println(stmt.toString());
	}
}
