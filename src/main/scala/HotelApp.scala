import akka.actor.{ ActorSystem}
import scala.io.StdIn.{readInt, readLine}
import scala.util.Random
import scala.util.control.Breaks.{break, breakable}

object HotelApp {
  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem("HotelActorSystem")

    val customersActor = system.actorOf(CustomersDAO.props, "customersActor")
    val roomActor = system.actorOf(RoomDAO.props, "roomActor")
    val billingActor = system.actorOf(BillingDAO.props, "billingActor")
    val reportActor = system.actorOf(ReportDAO.props, "reportActor")

    var flag: Int = 1
    var roomId: Int = 1

    println("WELCOME IN OUR HOTEL, PLEASE ENTER WHAT YOU WANT TO DO: ")

    breakable {
      while (flag == 1) {
        println("1 To Book a room \n" +
          "2 To make a bill and check out \n" + "3 To make a report\n" + "4 To Add a room\n" + "5 To Delete a room\n" + "0 To EXIT\n")

        var operation = readInt()

        operation match {
          case 1 =>
            roomActor ! ListRoom()
            print("Enter room ID: ")
            roomId = readInt()

            println("Enter name : ")
            val name = readLine()
            println("Email Address : ")
            val email = readLine()
            println("phone number : ")
            val phone_number = readLine()
            try {
              customersActor ! BookRoom(roomId, name, email, phone_number)
              println("Room booked successfully.")
            } catch {
              case e: Throwable => println(e.getMessage)
            }
          case 2 =>
            //MAKE BILL AND CHECK OUT
            billingActor ! ListBookings()
            print("enter the Booking_id to make a Bill: ")
            val booking_id = readInt()
            print("enter room id to check out: ")
            roomId = readInt()
            val billValue: Int = Math.abs(100+ Random.nextInt(9901))
            billingActor ! MakeBill(booking_id,roomId,billValue)
            println(s"Bill was made successfully, and it's: $billValue USD" )
            println("CHECKED OUT!")

          case 3 =>
            reportActor ! Report()
            billingActor ! ListBookings()


          case 4 =>
            roomActor ! ListRoom()
            // Get user input for room details
            print("Enter room ID: ")
            val roomId = readInt()
            print("Enter room type: ")
            val roomType = readLine()
            print("Is room available (true/false): ")
            val isAvailableStr = readLine()
            val isAvailable = isAvailableStr.toBoolean
            // Send the AddRoom message with the provided room details
            roomActor ! AddRoom(roomId, roomType, isAvailable)
            println("Room added successfully.")

          case 5 =>
            roomActor ! ListRoom()
            // Get user input for room ID to delete
            print("Enter room ID to delete: ")
            val roomIdToDelete = readInt()

            // Send the DeleteRoom message with the provided room ID
            roomActor ! DeleteRoom(roomIdToDelete)
            println("Room deleted successfully.")

          case 0 => break() // This will break out of the while loop
          case _ => println("invalid operation")
        }
      }
    }

    // Terminate the ActorSystem outside the loop
    system.terminate()
  }
}
