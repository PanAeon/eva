package foo.bar.example;

import javaslang.Tuple2;
import javaslang.Tuple6;
import foo.bar.sql;
import foo.bar.queries.Query1;

public interface SimpleSql {
	@sql("select * from users join organizations on users.organizationId=organizations.id where users.id = ${userId};")
	Query1<Tuple2<String, String>> getUserWithOrg(int userId);
	
	
	@sql("select * from users where users.id = ${userId}")
	Query1<Tuple6<Integer, Integer, String, String, String, String>> userQ(int userId); // overloading works, good.
	
	
	@sql("select firstName, lastName from users ")
	Query1<Tuple2<String, String>> userQ();
	
	// unfortunately @Param("userId") int userId ... fuck
	
	void foo()/*{
	  yeah!
	}*/; 
}
