package it.unisa.auctionmechanism;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Example {

    @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
    private static String master;

    @Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
    private static int id;

    public static void main(String[] args) throws Exception {
        class MessageListenerImpl implements MessageListener{
            int peerid;

            public MessageListenerImpl(int peerid)
            {
                this.peerid=peerid;

            }
            public Object parseMessage(Object obj) {

                TextIO textIO = TextIoFactory.getTextIO();
                TextTerminal terminal = textIO.getTextTerminal();
                terminal.printf("\n"+peerid+"] (Direct Message Received) "+obj+"\n\n");
                return "success";
            }

        }


        Example example = new Example();

        final CmdLineParser parser = new CmdLineParser(example);

        try {
            parser.parseArgument(args);
            TextIO textIO = TextIoFactory.getTextIO();
            TextTerminal terminal = textIO.getTextTerminal();

            AuctionImplementation impl = new AuctionImplementation(id,master,new MessageListenerImpl(id));

            terminal.printf("\nStaring peer id: %d on master node: 127.0.0.1\n", id );
            String auction_name="";

            while(true) {
                printMenu(terminal);

                int option = textIO.newIntInputReader()
                        .withMaxVal(6)
                        .withMinVal(1)
                        .read("\nOption");

                switch (option) {
                    case 1: terminal.printf("\nCHECK ALL AUCTIONS\n");

                            String allAuctions = impl.checkAllAuctions();
                            if(allAuctions!=null) {
                                terminal.printf("\n%s\n", allAuctions);
                            }
                            else{
                                terminal.printf("\nTHERE'S NO ACTIVE AUCTION!\n");
                            }
                            break;

                    case 2: terminal.printf("\nCHECK AN AUCTION\n");
                            auction_name = textIO.newStringInputReader()
                                .read(" Auction Name:");

                            String checkedAuction = impl.checkAuction(auction_name);

                            if(checkedAuction!= null)
                                terminal.printf("\n%s\n",checkedAuction);
                            else
                                terminal.printf("\nERROR OCCURED CHECKING THIS AUCTION\n");

                            break;

                    case 3: terminal.printf("\nPLACE A BID\n");
                            auction_name = textIO.newStringInputReader()
                                    .read(" Auction Name:");

                            String amount = textIO.newStringInputReader()
                                    .read(" Insert a Bid Amount:");

                            Double bid_amount = 0.0;
                            try{
                                 bid_amount= Double.parseDouble(amount);

                            }catch(NumberFormatException e){
                                terminal.printf("\nERROR OCCURED PLACING THIS BID\n");
                                break;
                            }


                            String method = impl.placeAbid(auction_name,bid_amount );
                            if(method!=null){
                                terminal.printf("\n%s\n",method);
                            }
                            else{
                                terminal.printf("\nERROR OCCURED PLACING THIS BID\n");
                            }
                            break;

                    case 4: terminal.printf("\nCREATE AN AUCTION\n");
                            auction_name = textIO.newStringInputReader()
                                .read(" Auction Name: ");

                            String complete_date  = textIO.newStringInputReader()
                                .read(" Insert Day of the End of Auction (GG/MM/AAAA): ");

                            if(complete_date.split("/").length != 3){
                                terminal.printf("\nDATE NOT INSERTED CORRECTLY\n");
                                break;
                            }

                            if(complete_date.split("/")[0].length()>2){
                                terminal.printf("\nDATE NOT INSERTED CORRECTLY\n");
                                break;
                             }
                            if(complete_date.split("/")[1].length()>2){
                                terminal.printf("\nMONTH NOT INSERTED CORRECTLY\n");
                                break;
                            }
                            if(complete_date.split("/")[2].length()>4){
                                terminal.printf("\nYEAR NOT INSERTED CORRECTLY\n");
                                break;
                            }

                            int day=0;
                            int month=0;
                            int year=0;

                            try{
                                day = Integer.parseInt(complete_date.split("/")[0]);
                                month = Integer.parseInt(complete_date.split("/")[1]);
                                year = Integer.parseInt(complete_date.split("/")[2]);
                            }catch(NumberFormatException e){
                                terminal.printf("\nDATE NOT INSERTED CORRECTLY\n");
                                break;
                            }


                            if(day>31) {
                                terminal.printf("\nDATE NOT INSERTED CORRECTLY\n");
                                break;
                            }
                            if(month == 2 && day >29) {
                                terminal.printf("\nDATE NOT INSERTED CORRECTLY\n");
                                break;
                            }
                            if((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
                                terminal.printf("\nDATE NOT INSERTED CORRECTLY\n");
                                break;
                            }

                           // Date actual = new Date();


                            String complete_hour  = textIO.newStringInputReader()
                                .read(" Insert the Hour of the End of Auction (hh:mm): ");

                            if(complete_hour.split(":").length != 2){
                                 terminal.printf("\nHOUR NOT INSERTED CORRECTLY\n");
                                 break;
                            }

                            if((complete_hour.split(":")[0].length()>2) || (complete_hour.split(":")[1].length()>2)){
                                terminal.printf("\nHOUR NOT INSERTED CORRECTLY\n");
                                break;
                            }

                            int hour = 0;
                            int minutes = 0;

                            try{
                                hour = Integer.parseInt(complete_hour.split(":")[0]);
                                minutes = Integer.parseInt(complete_hour.split(":")[1]);
                            }catch(NumberFormatException e){
                                terminal.printf("\nHOUR NOT INSERTED CORRECTLY\n");
                                break;
                            }



                             if((hour > 24) || (minutes > 59)){
                                 terminal.printf("\nHOUR NOT INSERTED CORRECTLY\n");
                                 break;
                             }

                             String dateInString =  day+ "/" + month  + "/" +year;
                             java.util.Date date = null;

                            try {
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                date = formatter.parse(dateInString);
                                date.setHours(hour);
                                date.setMinutes(minutes);


                            } catch (ParseException e) {
                                System.out.println(e.toString());
                                e.printStackTrace();
                            }


                            String r_price = textIO.newStringInputReader()
                                    .read(" Insert Reserved Price: ");

                            if(r_price.length()>10){
                                terminal.printf("\nThe Bid is too big!\n");
                                break;
                            }

                            Double reserved_price = 0.0;
                            try{
                                reserved_price = Double.parseDouble(r_price);

                            }catch(NumberFormatException e){
                                terminal.printf("\nERROR OCCURED CREATING THIS AUCTION\n");
                                break;
                            }

                            String description = textIO.newStringInputReader()
                                    .read(" Insert Description: ");

                            boolean createdAuction = impl.createAuction(auction_name,date,reserved_price,description);

                            if(createdAuction==true){
                                terminal.printf("\nAUCTION CREATED CORRECTLY\n");
                            }
                            else{
                                terminal.printf("\nERROR OCCURED CREATING THIS AUCTION\n");
                            }
                            break;

                    case 5: terminal.printf("\nDELETE AN AUCTION\n");
                            auction_name = textIO.newStringInputReader()
                                .read(" Auction Name: ");

                            if(impl.removeAnAuction(auction_name)==true){
                                terminal.printf("\nAUCTION REMOVED CORRECTLY\n");
                            }
                            else{
                                terminal.printf("\nYOU CAN'T REMOVE THIS AUCTION BECAUSE YOU'RE NOT THE CREATOR OR THIS AUCTION DON'T EXIST.\n");
                            }

                            break;

                    case 6: terminal.printf("\nARE YOU SURE TO LEAVE THE NETWORK?\n");
                            boolean exit = textIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
                            if(exit) {
                                impl.exit();
                                System.exit(0);
                            }
                            break;

                    default: break;
                }

            }
        }
        catch (Exception e){

        }

    }


    public static void printMenu(TextTerminal terminal) {
        terminal.printf("\n1 - CHECK ALL AUCTIONS\n");
        terminal.printf("\n2 - CHECK AN AUCTION\n");
        terminal.printf("\n3 - PLACE A BID\n");
        terminal.printf("\n4 - CREATE AN AUCTION\n");
        terminal.printf("\n5 - DELETE AN AUCTION\n");
        terminal.printf("\n6 - EXIT\n");

    }


}
