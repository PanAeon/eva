package foo.bar;

import foo.bar.queries.Query1;
import javaslang.Tuple2;
import javaslang.Tuple5;


public interface Genesys {
//	@sql("select * from users join organizations on users.id=organizations.id where users.id = ?;")
	
	
//	@sql("select * from users where users.id = {userId}")
//	Query1<Tuple5<Integer, String, String, String, String>> userQ(/*int userId*/);
	
	
	@sql("select firstName, lastName from users ")
	Query1<Tuple2<String, String>> userQ();
}
