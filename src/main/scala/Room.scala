import java.sql.{Connection, DriverManager, ResultSet}
import akka.actor.{Actor, Props}

case class AddRoom(roomId: Int, roomType: String, isAvailable: Boolean)
case class DeleteRoom(roomId: Int)
case class ListRoom()

class RoomDAO extends Actor {

  override def receive: Receive = {
    case ListRoom() => listRoom()
    case AddRoom(roomId, roomType, isAvailable) => addRoom(roomId, roomType, isAvailable)
    case DeleteRoom(roomId) => deleteRoom(roomId)
  }

  def listRoom(): Unit = {
    var connection: Connection = null
    connection = DatabaseConfig.getConnection

    if (connection != null) {
      try {
        val statement = connection.createStatement()
        println("Rooms:")

        val selectQuery = "SELECT * FROM room WHERE is_available=true"
        val resultSet: ResultSet = statement.executeQuery(selectQuery)

        while (resultSet.next()) {
          val roomId = resultSet.getInt("room_number")
          val roomType = resultSet.getString("room_type")
          val isAvailable = resultSet.getBoolean("is_available")

          println(s"Room ID: $roomId, Room Type: $roomType, Available: $isAvailable")
        }
        print("Enter room ID: ")
      } catch {
        case e: Throwable =>
          println(e.getMessage)
        case _ =>
          println("ALL ROOMS ARE BOOKED!!!")
      } finally {
        connection.close()
      }
    }
  }

  def addRoom(roomId: Int, roomType: String, isAvailable: Boolean): Unit = {
    var connection: Connection = null
    connection = DatabaseConfig.getConnection

    if (connection != null) {
      try {
        val statement = connection.prepareStatement("INSERT INTO room (room_number, room_type, is_available) VALUES (?, ?, ?)")
        statement.setInt(1, roomId)
        statement.setString(2, roomType)
        statement.setBoolean(3, isAvailable)
        statement.executeUpdate()
        println(s"Room with ID $roomId added successfully.")
      } catch {
        case e: Throwable =>
          println(e.getMessage)
      } finally {
        connection.close()
      }
    }
  }

  private def deleteRoom(roomId: Int): Unit = {
    var connection: Connection = null
    connection = DatabaseConfig.getConnection

    if (connection != null) {
      try {
        val statement = connection.createStatement()

        // Check if the room exists before deleting
        val checkRoomQuery = "SELECT COUNT(*) AS room_count FROM room WHERE room_number = ?"
        val checkRoomStatement = connection.prepareStatement(checkRoomQuery)
        checkRoomStatement.setInt(1, roomId)
        val roomResult = checkRoomStatement.executeQuery()

        if (roomResult.next() && roomResult.getInt("room_count") > 0) {
          // Room exists, proceed with deletion
          val deleteQuery = "DELETE FROM room WHERE room_number = ?"
          val deleteStatement = connection.prepareStatement(deleteQuery)
          deleteStatement.setInt(1, roomId)
          deleteStatement.executeUpdate()
        } else {
          println(s"Room with ID $roomId does not exist. No deletion performed.")
        }
      } catch {
        case e: Throwable =>
          println("An error occurred while deleting the room.")
          println(e.getMessage)
      } finally {
        connection.close()
      }
    }
  }

}

object RoomDAO {
  def props: Props = Props[RoomDAO]
}
