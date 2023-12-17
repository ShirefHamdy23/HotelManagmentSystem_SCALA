import java.sql.{Connection, DriverManager, SQLException}

object DatabaseConfig {

  private val url = "jdbc:mysql://localhost:3306/hoteldb"
  private val driver = "com.mysql.cj.jdbc.Driver"
  private val username = "root"
  private val password = ""
  // Load the database driver
  Class.forName(driver)

  def getConnection: Connection = {
    try {
      DriverManager.getConnection(url, username, password)
    } catch {
      case e: SQLException =>
        println(s"Error connecting to the database: ${e.getMessage}")
        throw e
    }
  }

  def closeConnection(connection: Connection): Unit = {
    try {
      connection.close()
    } catch {
      case e: SQLException =>
        println(s"Error closing the database connection: ${e.getMessage}")
    }
  }
}
