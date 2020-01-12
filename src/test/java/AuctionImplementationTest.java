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

    /**
     * Test for an auction with only one bidder. Peer0 creates an auction while peer1 places a bid.
     * Finally, thanks to the test on checkauction we are able to observe if the best offer is the one just made.
     */
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

    /**
     * Peer0 creates an auction while peer1, a simple bidder, tries to eliminate it.
     * Of course, the auction is only removable by its creator.
     */
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


    /**
     *The peer0 creates an auction and immediately afterwards tries to create a second identical one,
     *this is not possible because the name of the auction is unique.
     */
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

    /**
     * Peer0 creates an auction. Peer1 makes an offer, which is exceeded by the offer from peer2. Finally the auction expires.
     * The method checks that the actual winner is the highest bidder at the auction ended with his bid
     * and that he must pay the price offered by the second highest bidder.
     */
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

    /**
     * Two new auctions are created from peer0 and peer1.
     * Subsequently the CheckAllAuctions method is invoked which prints all the auctions present in the DHT and their most important information.
     */
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

    /**
     * The peer0 creates an auction, but subsequently leaves the network.
     * The method verifies that the exit is successful and that the auction created by that peer is deleted.
     */
    @Test
    public void F_leaveTheNetworkAsCreator(){
        try {
            Date date = null;
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            date = formatter.parse("22/12/2030");
            date.setHours(11);
            date.setMinutes(30);
            peer0.createAuction("Proiettore APEMAN Portatile", date, 100, "Il proiettore APEMAN LC550 viene utilizzato principalmente per l'home cinema e i videogiochi, NON consigliato per Powerpoint o presentazioni aziendali.");

            Thread.sleep(2000);
            assertTrue(peer0.exit());
            assertEquals(null, peer0.checkAuction("Proiettore APEMAN Portatile"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Peer0 creates an auction, and peer1, peer3 and peer2 make a bid in sequence.
     * Subsequently the auction expires and the method checks that there is the correct winner
     * and checks the correctness of the strings returned to both the winner and the creator.
     */
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

    /**
     * Peer0 creates an auction, and peer1 places a bid.
     * Subsequently, the same peer1 tries to make a second offer, but cannot already be the best bidder.
     */
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

    /**
     * Peer0 creates an auction but no bidding takes place. So the auction ends without winners.
     */
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

    /**
     * Peer0 creates an auction and then tries to remove it. The method checks that removal is successful.
     */
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

    /**
     * Peer0 creates an auction and then tries to bid on its own auction.
     * Of course this is not possible and is signaled by a special string returned.
     */
    @Test
    public void M_placeABidAsCreator(){
        try {
           Date date = new Date();
            peer0.createAuction("Notebook Razer", new Date(Calendar.getInstance().getTimeInMillis() + 8000), 1500, "A notebook is a small, portable personal computer (PC)");
            assertEquals("The creator can't do a bid!", peer0.placeAbid("Notebook Razer", 1501));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Peer0 creates an auction and then peer1 and peer2 make bids in sequence.
     * Peer2, or the highest bidder, leaves the network.
     * So the auction resets the current best offer at the starting price and warns the other participants with a message.
     * Subsequently the auction expires without bids and therefore without having a winner.
     */
    @Test
    public void N_leaveTheNetworkAsBestBidder(){
        try {
            peer0.createAuction("Play Station 4", new Date(Calendar.getInstance().getTimeInMillis() + 8000), 200, "Console Sony!");
            peer1.placeAbid("Play Station 4", 300);
            peer2.placeAbid("Play Station 4", 350);
            assertTrue( peer2.exit());
            Thread.sleep(8000);
            assertEquals("The Auction is ended with no winner!", peer0.checkAuction("Play Station 4"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The peer0 creates an auction, but it expires without having received bids.
     * After the auction has expired the peer1 tries to place a bid, but is warned via the return string that it is no longer possible to bid.
     * Finally by checking the status of the auction it is possible to verify the absence of a winner.
     */
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

    /**
     * The peer0 tries to check the status of a non-existent auction. The return value is null.
     */
    @Test
    public void P_CheckingANonExistentAuction(){
        try {
            assertEquals(null, peer0.checkAuction("Joystick PS4"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The peer0 tries to check the status of an auction that has not received any bids.
     * A string is returned with the information relating to it and indicating the presence of the reserved price as the maximum offer value.
     */
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

    /**
     * Peer0 tries to check the status of an auction that has received a bid from peer1.
     * A string is returned with the information relating to it and indicating the presence of the best offer.
     * The method is tested by both the creator and the highest bidder to show the different strings returned.
     */
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

    /**
     * Peer0 creates an auction. Subsequently, peer2 makes an offer after which the auction expires.
     * Then peer1 tries to bid but the returned string shows that the auction has ended and that the winner is peer2 with its prices.
     */
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

    /**
     * Peer0 creates an auction. Subsequently, peer1 makes a lower offer than the reserved price.
     * The returned string shows that this is not possible.
     */
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



