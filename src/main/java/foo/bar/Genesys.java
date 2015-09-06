package foo.bar;

import javaslang.Tuple5;


public interface Genesys {
	@sql("select * from users join organizations on users.id=organizations.id where users.id = ?;")
	Query<Tuple5<Integer, String, String, String, String>> userQ(int userId);
}
