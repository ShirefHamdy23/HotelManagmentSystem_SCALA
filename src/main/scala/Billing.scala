import java.sql.{Connection, ResultSet}
import akka.actor.{Actor, Props}
import java.sql.PreparedStatement
import java.time.LocalDate

// Define messages for interacting with BillingDAO actor
case class MakeBill(booking_id: Int, roomId: Int, billValue: Int)
case class ListBookings()

class BillingDAO extends Actor {

  override def receive: Receive = {
    case MakeBill(booking_id, roomId, billValue) => makeBill(booking_id, roomId, billValue)
    case ListBookings() => listBookings()
  }

  private def hasBookings: Boolean = {
    var connection: Connection = null
    connection = DatabaseConfig.getConnection
    val statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
    if (connection != null) {
      try {
        val selectQuery = "SELECT * FROM bookings where check_out_date is null"
        val resultSet: ResultSet = statement.executeQuery(selectQuery)
        resultSet.isBeforeFirst()
      } catch {
        case e: Throwable => false
      } finally {
        if (connection != null) {
          DatabaseConfig.closeConnection(connection)
        }
      }
    } else {
      false
    }
  }

  private def makeBill(booking_id: Int, roomId: Int, billValue: Int): Unit = {
    if (bookingExists(booking_id) && roomExists(roomId)) {
      var connection: Connection = null
      connection = DatabaseConfig.getConnection
      val statement = connection.createStatement()
      if (connection != null) {
        try {

          val bookingQuery = "INSERT INTO Billing (booking_id, total_amount) VALUES (?, ?)"
          val bookingStatement: PreparedStatement = connection.prepareStatement(bookingQuery)
          bookingStatement.setInt(1, booking_id)
          bookingStatement.setInt(2, billValue)
          bookingStatement.executeUpdate()
          val updateQuery = "UPDATE Room SET is_available = ? WHERE room_id = ?"
          val updateQueryDate = "UPDATE Bookings SET check_out_date = ? WHERE room_id = ?"

          val roomUpdateStatement = connection.prepareStatement(updateQuery)
          val bookingUpdateStatement = connection.prepareStatement(updateQueryDate)

          roomUpdateStatement.setBoolean(1, true)
          roomUpdateStatement.setInt(2, roomId)

          bookingUpdateStatement.setDate(1, java.sql.Date.valueOf(LocalDate.now()))
          bookingUpdateStatement.setInt(2, roomId)

          roomUpdateStatement.executeUpdate()
          bookingUpdateStatement.executeUpdate()

        } catch {
          case e: Throwable => println(s"Error making bill: ${e.getMessage}")
        } finally {
          if (connection != null) {
            DatabaseConfig.closeConnection(connection)
          }
        }
      }
    } else {
      println("Invalid booking_id or roomId. Please check your input.")
    }
  }

  private def listBookings(): Unit = {
    var connection: Connection = null
    connection = DatabaseConfig.getConnection
    val statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
    if (connection != null) {
      try {
        println("Bookings:")

        val selectQuery = "SELECT * FROM bookings where check_out_date is null"
        val resultSet: ResultSet = statement.executeQuery(selectQuery)

        if (!resultSet.isBeforeFirst()) {
          println("No bookings available.")
        } else {
          while (resultSet.next()) {
            val booking_id = resultSet.getInt("booking_id")
            val room_id = resultSet.getInt("room_id")
            val check_in_date = resultSet.getDate("check_in_date")
            println(s"booking_id: $booking_id, room_id: $room_id, check_in_date: $check_in_date")
          }
        }

      } catch {
        case e: Throwable => println(s"Error listing bookings: ${e.getMessage}")
      } finally {
        if (connection != null) {
          DatabaseConfig.closeConnection(connection)
        }
      }
    }
  }

  private def bookingExists(booking_id: Int): Boolean = {
    var connection: Connection = null
    connection = DatabaseConfig.getConnection
    val statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
    if (connection != null) {
      try {
        val selectQuery = s"SELECT * FROM bookings WHERE booking_id = $booking_id"
        val resultSet: ResultSet = statement.executeQuery(selectQuery)
        resultSet.isBeforeFirst()
      } catch {
        case e: Throwable => false
      } finally {
        if (connection != null) {
          DatabaseConfig.closeConnection(connection)
        }
      }
    } else {
      false
    }
  }

  private def roomExists(roomId: Int): Boolean = {
    var connection: Connection = null
    connection = DatabaseConfig.getConnection
    val statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
    if (connection != null) {
      try {
        val selectQuery = s"SELECT * FROM room WHERE room_id = $roomId"
        val resultSet: ResultSet = statement.executeQuery(selectQuery)
        resultSet.isBeforeFirst()
      } catch {
        case e: Throwable => false
      } finally {
        if (connection != null) {
          DatabaseConfig.closeConnection(connection)
        }
      }
    } else {
      false
    }
  }

}

object BillingDAO {

  def props: Props = Props[BillingDAO]
}
