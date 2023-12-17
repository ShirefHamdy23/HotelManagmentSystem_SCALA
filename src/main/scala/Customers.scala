import java.sql.{Connection, SQLException, Statement}
import java.time.LocalDate
import akka.actor.{Actor, Props}

//messages for interacting with CustomersDAO actor
case class BookRoom(roomId: Int, name: String, email: String, phone_number: String)

class CustomersDAO extends Actor {

  override def receive: Receive = {
    case BookRoom(roomId, name, email, phone_number) => bookRoom(roomId, name, email, phone_number)
  }

  private def bookRoom(roomId: Int, name: String, email: String, phone_number: String): Unit = {
    var connection: Connection = null
    try {
      connection = DatabaseConfig.getConnection
      if (connection != null) {
        val statement = connection.createStatement()

        try {
          // Check if the room exists before booking
          val checkRoomQuery = "SELECT COUNT(*) AS room_count FROM room WHERE room_number = ?"
          val checkRoomStatement = connection.prepareStatement(checkRoomQuery)
          checkRoomStatement.setInt(1, roomId)
          val roomResult = checkRoomStatement.executeQuery()

          if (roomResult.next() && roomResult.getInt("room_count") > 0) {
            // Room exists, proceed with booking logic

            // Debug prints
            println(s"Booking room with ID $roomId")

            // Insert into Customers table
            val insertQuery = "INSERT INTO Customers (name, email, phone_number) VALUES (?, ?, ?)"
            val preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)
            preparedStatement.setString(1, name)
            preparedStatement.setString(2, email)
            preparedStatement.setString(3, phone_number)
            preparedStatement.executeUpdate()

            // Retrieve customer_id after the insert using getGeneratedKeys
            val generatedKeys = preparedStatement.getGeneratedKeys
            val customerId = if (generatedKeys.next()) generatedKeys.getInt(1) else -1

            // Debug prints
            println(s"Customer ID: $customerId")

            // Retrieve the corresponding room ID based on the room number
            val getRoomIdQuery = "SELECT room_id, is_available FROM room WHERE room_number = ?"
            val getRoomIdStatement = connection.prepareStatement(getRoomIdQuery)
            getRoomIdStatement.setInt(1, roomId)
            val roomResult = getRoomIdStatement.executeQuery()

            if (roomResult.next()) {
              val roomDbId = roomResult.getInt("room_id")
              val isAvailable = roomResult.getBoolean("is_available")

              if (isAvailable) {
                // Update Room availability to false
                val updateQuery = "UPDATE room SET is_available = false WHERE room_id = ?"
                val roomUpdateStatement = connection.prepareStatement(updateQuery)
                roomUpdateStatement.setInt(1, roomDbId)
                roomUpdateStatement.executeUpdate()

                println(s"Booking successful for room with ID $roomDbId")
              } else {
                println(s"Room with ID $roomId is not available.")
              }

              // Insert into Bookings table
              val bookingQuery = "INSERT INTO Bookings (room_id, customer_id, check_in_date) VALUES (?, ?, NOW())"
              val bookingStatement = connection.prepareStatement(bookingQuery)
              bookingStatement.setInt(1, roomDbId) // Use the retrieved room ID
              bookingStatement.setInt(2, customerId)
              bookingStatement.executeUpdate()
            } else {
              println(s"Failed to retrieve room ID for room number $roomId")
            }
          } else {
            println(s"Room with ID $roomId does not exist.")
          }
        } catch {
          case e: SQLException =>
            println(e.getMessage)
        }
      }
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    } finally {
      if (connection != null) {
        DatabaseConfig.closeConnection(connection)
      }
    }
  }

}

  object CustomersDAO {
  def props: Props = Props[CustomersDAO]
}
