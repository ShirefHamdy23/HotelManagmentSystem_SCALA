import akka.actor.{Actor, Props}
import java.sql.{Connection, ResultSet}

case class Report()

class ReportDAO extends Actor {

  override def receive: Receive = {
    case Report() => report()
  }

  private def report(): Unit = {
    var connection: Connection = null
    connection = DatabaseConfig.getConnection

    if (connection != null) {
      try {
        val statement = connection.createStatement()
        println("------------------Report Generator----------------")

        val selectQuery1 = "SELECT * FROM room WHERE is_available=true"
        val resultSet1: ResultSet = statement.executeQuery(selectQuery1)
        println("-----------Available Rooms--------------------")

        var availableRoomCount = 0
        var unavailableRoomCount = 0
        var totalRoomCount = 0
        while (resultSet1.next()) {
          val roomId = resultSet1.getInt("room_number")
          val roomType = resultSet1.getString("room_type")
          val isAvailable = resultSet1.getBoolean("is_available")

          println(s"Room ID: $roomId, Room Type: $roomType, Available: $isAvailable")

          if (isAvailable) {
            availableRoomCount += 1
          }
        }
        println(s"Total available rooms: $availableRoomCount")

        val selectQuery2 = "SELECT * FROM room WHERE is_available=false"
        val resultSet2: ResultSet = statement.executeQuery(selectQuery2)

        println("-------------Unavailable Rooms------------------")


        while (resultSet2.next()) {
          val roomId = resultSet2.getInt("room_number")
          val roomType = resultSet2.getString("room_type")
          val isAvailable = resultSet2.getBoolean("is_available")

          println(s"Room ID: $roomId, Room Type: $roomType, Available: $isAvailable")

          if (!isAvailable) {
            unavailableRoomCount += 1
          }

        }
        println(s"Total unavailable rooms: $unavailableRoomCount")

        val selectQuery3 = "SELECT * FROM room"
        val resultSet3: ResultSet = statement.executeQuery(selectQuery3)
        println("------------End Of Rooms-------------------")

        val selectQuery4 = "SELECT * FROM bookings"
        val resultSet4: ResultSet = statement.executeQuery(selectQuery4)
        println(" -----------All Bookings--------------------")

        while (resultSet4.next()) {
          val booking_id = resultSet4.getInt("booking_id")
          val room_id = resultSet4.getInt("room_id")
          val check_in_date = resultSet4.getDate("check_in_date")
          println(s"booking_id: $booking_id, room_id: $room_id, check_in_date: $check_in_date")
        }

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
}

object ReportDAO {
  def props: Props = Props[ReportDAO]
}
