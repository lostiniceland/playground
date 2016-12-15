import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}
import playground.akka.Reaper
import playground.akka.Reaper.WatchMe

// Our test reaper.  Sends the snooper a message when all
// the souls have been reaped
class TestReaper(snooper: ActorRef) extends Reaper {
  def allSoulsReaped(): Unit = snooper ! "Dead"
}

class ReaperSpec extends TestKit(ActorSystem("ReaperSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
  with MustMatchers {

  override def afterAll() {
    system.terminate()
  }

  "Reaper" should {
    "work" in {
      // Set up some dummy Actors
      val a = TestProbe()
      val b = TestProbe()
      val c = TestProbe()
      val d = TestProbe()

      // Build our reaper
      val reaper = system.actorOf(Props(new TestReaper(testActor)))

      // Watch a couple 
      reaper ! WatchMe(a.ref)
      reaper ! WatchMe(d.ref)

      // Stop them
      system.stop(a.ref)
      system.stop(d.ref)

      // Make sure we've been called
      expectMsg("Dead")
    }
  }
}