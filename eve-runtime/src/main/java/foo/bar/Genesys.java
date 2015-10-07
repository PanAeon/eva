package foo.bar;

import foo.bar.queries.Query1;
import foo.bar.annotations.*;
import javaslang.*;

// TODO: unit tests from scratch ....
// but there's mapping && includes ....
// let's complete POC by the deadline

// TODO: ok, doesn't need to actually type-check sql queries, make
// sqlt and sql operators, similar to slick ones

// see http://jdbi.org/

/* ouch!
 * 
 * @sql({
	"select * from",
	"table a as A join",
	"table b as B join",
	"table c as C",
	"where",
	"a.id > 3"})
 */

public interface Genesys {
	
	
	@sql({
		"select * from",
		"users",
		"join organizations",
		"on users.organizationId=organizations.id", 
		"where users.id = ${userId}"})
	Query1<Tuple2<String, String>> userWithOrgQ(int userId);
	
	
	@sql("select * from users where users.id = ${userId}")
	Query1<Tuple6<Integer, Integer, String, String, String, String>> userQ(int userId); // overloading works, good.
	
	
	@sql("select firstName, lastName from users ")
	Query1<Tuple2<String, String>> userQ();
	
	// support?  @Param("userId") int userId ...
	
	void foo()/*{
	  doesn't work without explict src path, then can use javac api to parse src
	  yeah!
	}*/; 
}
