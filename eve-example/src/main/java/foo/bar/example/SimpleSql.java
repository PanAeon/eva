package foo.bar.example;


import foo.bar.annotations.*;

public interface SimpleSql {
	
	
	
	/**
	 * well this, is doc comment
	 */
	@sql("select * from users where users.id = ${userId}")
	void userQ(int userId) /* and this is another type of comments */; // overloading works, good.
	
	

	

}
