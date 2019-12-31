import it.unisa.auctionmechanism.AuctionImplementation;
import it.unisa.auctionmechanism.MessageListener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionMechanismTest {

    private static AuctionImplementation peer0;
    private static AuctionImplementation peer1;
    private static AuctionImplementation peer2;
    private static AuctionImplementation peer3;

    @BeforeAll
    static void setup() throws Exception{

        class MessageListenerImpl implements MessageListener {
            int peerid;

            public MessageListenerImpl(int peerid) {
                this.peerid = peerid;
            }

            public Object parseMessage(Object obj) {
                System.out.println(peerid + "] (Direct Message Received) " + obj);
                return "success";
            }
        }

        peer0 = new AuctionImplementation(0, "127.0.0.1", new MessageListenerImpl(0));
        peer1 = new AuctionImplementation(1, "127.0.0.1", new MessageListenerImpl(1));
        peer2 = new AuctionImplementation(2, "127.0.0.1", new MessageListenerImpl(2));
        peer3 = new AuctionImplementation(3, "127.0.0.1", new MessageListenerImpl(3));
    }

    @AfterAll
    static void tearDown() {
        peer0.exit();
        peer1.exit();
        peer2.exit();
        peer3.exit();
    }

    //CHANGE
    @Test
    void DuplicateAuctionError() {
        try {
            //CREATE A DATE
            Date date = null;
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            date = formatter.parse("22/12/2030");
            date.setHours(11);
            date.setMinutes(30);
            assertTrue(peer0.createAuction("Iphone 11", date, 800, "New Apple Smartphone"));
            assertFalse(peer0.createAuction("Iphone 11", date, 800, "New Apple Smartphone"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    //placing a bid for an iphone and checking his status.
    @Test
    void placeABid(){
        try {
            Date date = null;
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            date = formatter.parse("22/12/2030");
            date.setHours(11);
            date.setMinutes(30);

            peer0.createAuction("Iphone", date, 800, "New Apple Smartphone 2019");
            assertEquals("The auction is active until Sun Dec 22 11:30:00 CET 2030 and the highest offer is yours with: 1000.0", peer1.placeAbid("Iphone", 1000));
            Thread.sleep(1500);
            assertEquals("The auction is active until Sun Dec 22 11:30:00 CET 2030 and the highest offer is: 1000.0",peer0.checkAuction("Iphone"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    //placing a bid for a OnePlus, but the auction is outdated.
    @Test
    void placeAnOutdatedBid(){
        try {
            peer0.createAuction("OnePlus", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 800, "New Android Smartphone");
            Thread.sleep(1500);
            assertEquals("You can't do a bid! The Auction is ended with no winner!", peer1.placeAbid("OnePlus", 1000));
            Thread.sleep(1500);
            assertEquals("The Auction is ended with no winner!", peer0.checkAuction("OnePlus"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void placeABidAsCreator(){
        try {
            Date date = new Date();
            peer0.createAuction("Notebook MSI", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 1500, "A notebook is a small, portable personal computer (PC)");
            assertEquals("The creator can't do a bid!", peer0.placeAbid("Notebook MSI", 1500));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    void placeABidAsBestOfferer(){
        try {
            Date date = new Date();
            peer0.createAuction("Notebook MSI", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 1500, "A notebook is a small, portable personal computer (PC)");
            peer1.placeAbid("Notebook MSI", 1600);
            assertEquals("You have already offered the highest bid!", peer1.placeAbid("Notebook MSI", 1800));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void removeABid(){
        try {
            Date date = new Date();
            peer0.createAuction("Notebook HP", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 1300, "A notebook is a small, portable personal computer (PC)");
            assertEquals(true, peer0.removeAnAuction("Notebook HP"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void removeABidAsANonCreator(){
        try {
            Date date = new Date();
            peer0.createAuction("Notebook HP", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 1300, "A notebook is a small, portable personal computer (PC)");
            assertEquals(false, peer1.removeAnAuction("Notebook HP"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    void auctionWithNoWinner(){
        try {
            Date date = new Date();
            peer0.createAuction("Nintendo Switch", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 300, "Nintendo Switch is a hybrid console between a portable and a home gaming system");
            Thread.sleep(2000);
            assertEquals("The Auction is ended with no winner!", peer0.checkAuction("Nintendo Switch"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    void auctionWithOneBidder(){
        try {
            Date date = new Date();
            peer0.createAuction("Logitech G431", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 70, "Logitech G430 7.1 surround sound gaming headset with lightweight, performance ear cups, and digital USB balances performance and comfort.");
            peer1.placeAbid("Logitech G431",100);
            Thread.sleep(3000);
            assertEquals("The Auction is ended and the winner is 1 with this bid: 100.0 and the price is 70.0", peer0.checkAuction("Logitech G431"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    void multipleBids(){
        try {
            Date date = new Date();
            peer0.createAuction("HUAWEI Mediapad T5 Tablet", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 170, "Tablet, Display: 10.1\", Memory: 32 GB, RAM: 3 GB, OS: Android 8.0, Wi-Fi, Black");
            peer1.placeAbid("HUAWEI Mediapad T5 Tablet",190);
            peer3.placeAbid("HUAWEI Mediapad T5 Tablet",200);
            peer2.placeAbid("HUAWEI Mediapad T5 Tablet",220);

            Thread.sleep(2000);
            assertEquals("The Auction is ended and the winner is 2 with this bid: 220.0 and the price is 200.0", peer0.checkAuction("HUAWEI Mediapad T5 Tablet"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void auctionWithAWinner(){
        try {
            Date date = new Date();
            peer0.createAuction("Logitech G430", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 70, "Logitech G430 7.1 surround sound gaming headset with lightweight, performance ear cups, and digital USB balances performance and comfort.");
            peer1.placeAbid("Logitech G430",100);
            peer2.placeAbid("Logitech G430",200);
            Thread.sleep(2000);
            assertEquals("The Auction is ended and the winner is 2 with this bid: 200.0 and the price is 100.0", peer0.checkAuction("Logitech G430"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void leaveTheNetworkAsCreator(){
        try {
            Date date = new Date();
            peer0.createAuction("Proiettore APEMAN Portatile", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 100, "Il proiettore APEMAN LC550 viene utilizzato principalmente per l'home cinema e i videogiochi, NON consigliato per Powerpoint o presentazioni aziendali.");

            Thread.sleep(2000);
            assertEquals(true, peer0.exit());
            assertEquals(null, peer0.checkAuction("Proiettore APEMAN Portatile"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void leaveTheNetworkAsBestBidder(){
        try {
            Date date = new Date();
            peer0.createAuction("Play Station 4", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 200, "Console Sony!");
            peer1.placeAbid("Play Station 4", 300);
            peer2.placeAbid("Play Station 4", 350);
            Thread.sleep(2000);
            assertEquals(true, peer2.exit());
            assertEquals("The Auction is ended with no winner!", peer0.checkAuction("Play Station 4"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    void checkAllAuctions(){
        try {
            Date date = new Date();
            peer0.createAuction("The Witcher 3: Wild Hunt", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 50, "Videogame for PC and Play Station 4");
            peer1.createAuction("Mountain Bike", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 500, "The mountain bike is a bicycle structured so that it can also move off asphalt roads, both uphill and downhill.");
            Thread.sleep(2000);
            assertEquals("Name: Logitech G431, Best Bid: 100.0, Status: ENDED, Description: Logitech G430 7.1 surround sound gaming headset with lightweight, performance ear cups, and digital USB balances performance and comfort.\n" +
                    "Name: Notebook HP, Reserved Price: 1300.0, Status: ENDED, Description: A notebook is a small, portable personal computer (PC)\n" +
                    "Name: Iphone 11, Reserved Price: 800.0, Status: ACTIVE, Description: New Apple Smartphone\n" +
                    "Name: Logitech G430, Best Bid: 200.0, Status: ENDED, Description: Logitech G430 7.1 surround sound gaming headset with lightweight, performance ear cups, and digital USB balances performance and comfort.\n" +
                    "Name: The Witcher 3: Wild Hunt, Reserved Price: 50.0, Status: ENDED, Description: Videogame for PC and Play Station 4\n" +
                    "Name: Mountain Bike, Reserved Price: 500.0, Status: ENDED, Description: The mountain bike is a bicycle structured so that it can also move off asphalt roads, both uphill and downhill.\n", peer2.checkAllAuctions());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


}
