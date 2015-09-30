package foo.bar;

import foo.bar.queries.Query1;
import javaslang.*;

// TODO: unit tests from scratch ....
// but there's mapping && includes ....
// let's complete POC by the deadline

// TODO: ok, doesn't need to actually type-check sql queries, make
// sqlt and sql operators, similar to slick ones

// see http://jdbi.org/

public interface Genesys {
	
	
	@sql("select * from users join organizations on users.organizationId=organizations.id where users.id = ${userId};")
	Query1<Tuple2<String, String>> getUserWithOrg(int userId);
	
	
	@sql("select * from users where users.id = ${userId}")
	Query1<Tuple6<Integer, Integer, String, String, String, String>> userQ(int userId); // overloading works, good.
	
	
	@sql("select firstName, lastName from users ")
	Query1<Tuple2<String, String>> userQ();
	
	// unfortunately @Param("userId") int userId ... fuck
	
	void foo()/*{
	   now what? 
	   can I haz source files?
	}*/; 
}
