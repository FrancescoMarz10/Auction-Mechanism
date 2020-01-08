import it.unisa.auctionmechanism.AuctionImplementation;
import it.unisa.auctionmechanism.MessageListener;
import org.junit.*;
import org.junit.runners.MethodSorters;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuctionImplementationTest {

    private static AuctionImplementation peer0;
    private static AuctionImplementation peer1;
    private static AuctionImplementation peer2;
    private static AuctionImplementation peer3;

    public AuctionImplementationTest(){

    }

    @BeforeClass
    public static void setup() throws Exception{

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

    @AfterClass
    public static void tearDown() {
        peer1.exit();
        peer2.exit();
        peer3.exit();
        peer0.exit();
    }

    @Test
    public void A_auctionWithOneBidder(){
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
    public void B_removeAnAuctionAsANonCreator(){
        try {
            Date date = new Date();
            peer0.createAuction("Notebook HP", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 1300, "A notebook is a small, portable personal computer (PC)");
            assertFalse(peer1.removeAnAuction("Notebook HP"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }



    @Test
    public void C_DuplicateAuctionError() {
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

    @Test
    public void D_auctionWithAWinner(){
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
    public void E_checkAllAuctions() {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void F_leaveTheNetworkAsCreator(){
        try {
            Date date = new Date();
            peer0.createAuction("Proiettore APEMAN Portatile", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 100, "Il proiettore APEMAN LC550 viene utilizzato principalmente per l'home cinema e i videogiochi, NON consigliato per Powerpoint o presentazioni aziendali.");

            Thread.sleep(2000);
            assertTrue( peer0.exit());
            assertEquals(null, peer0.checkAuction("Proiettore APEMAN Portatile"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void G_multipleBids(){
        try {
            Date date = new Date();
            peer0.createAuction("HUAWEI Mediapad T5 Tablet", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 170, "Tablet, Display: 10.1\", Memory: 32 GB, RAM: 3 GB, OS: Android 8.0, Wi-Fi, Black");
            peer1.placeAbid("HUAWEI Mediapad T5 Tablet",190);
            peer3.placeAbid("HUAWEI Mediapad T5 Tablet",200);
            peer2.placeAbid("HUAWEI Mediapad T5 Tablet",220);

            Thread.sleep(2000);
            assertEquals("The Auction is ended and the winner is 2 with this bid: 220.0 and the price is 200.0", peer0.checkAuction("HUAWEI Mediapad T5 Tablet"));
            assertEquals("The Auction is ended and the winner is you, 2, with this bid: 220.0 and the price is 200.0", peer2.checkAuction("HUAWEI Mediapad T5 Tablet"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void H_placeABidAsBestOfferer(){
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
    public void I_auctionWithNoWinner(){
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
    public void L_removeAnAuction(){
        try {
            Date date = new Date();
            peer0.createAuction("Notebook HP", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 1300, "A notebook is a small, portable personal computer (PC)");
            assertTrue( peer0.removeAnAuction("Notebook HP"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void M_placeABidAsCreator(){
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
    public void N_leaveTheNetworkAsBestBidder(){
        try {
            Date date = new Date();
            peer0.createAuction("Play Station 4", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 200, "Console Sony!");
            peer1.placeAbid("Play Station 4", 300);
            peer2.placeAbid("Play Station 4", 350);
            Thread.sleep(2000);
            assertTrue( peer2.exit());
            assertEquals("The Auction is ended with no winner!", peer0.checkAuction("Play Station 4"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    //placing a bid for a OnePlus, but the auction is outdated.
    @Test
    public void O_placeAnOutdatedBid(){
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
    public void P_CheckingANonExistentAuction(){
        try {
            assertEquals(null, peer0.checkAuction("Joystick PS4"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void Q_CheckingAnAuctionWithNoBidders(){
        try {
            Date date = null;
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            date = formatter.parse("22/12/2030");
            date.setHours(11);
            date.setMinutes(30);
            peer0.createAuction("Bottiglia Termica", date, 22, "È un contenitore progettato per mantenere a lungo la temperatura del liquido all’interno");
            assertEquals("The auction is active until "+date+" and the reserved price is: 22.0", peer0.checkAuction("Bottiglia Termica"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void R_CheckingAnAuction(){
        try {
            Date date = null;
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            date = formatter.parse("22/12/2030");
            date.setHours(11);
            date.setMinutes(30);
            peer0.createAuction("Bottiglia Termica", date, 22, "È un contenitore progettato per mantenere a lungo la temperatura del liquido all’interno");
            peer1.placeAbid("Bottiglia Termica",30);
            assertEquals("The auction is active until "+date+" and the highest offer is: 30.0", peer0.checkAuction("Bottiglia Termica"));
            assertEquals("The auction is active until "+date+" and the highest offer is yours with: 30.0", peer1.checkAuction("Bottiglia Termica"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void S_placeAnOutdatedBidWithAWinner(){
        try {
            peer0.createAuction("Amazon Echo", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 50, "Assistente Vocale di Amazon");
            peer2.placeAbid("Amazon Echo",60);
            Thread.sleep(1500);
            assertEquals("You can't do a bid! The Auction is ended, the winner is 2 with this bid: 60.0 and the price is 50.0", peer1.placeAbid("Amazon Echo", 80));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void T_placeABidLowerThenThePrice(){
        try {
            peer0.createAuction("Google Home", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 50, "Assistente Vocale di Google");
            assertEquals("You can't do a bid lower then the biggest bid!", peer1.placeAbid("Google Home", 40));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}



