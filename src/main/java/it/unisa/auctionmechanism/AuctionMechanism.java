package it.unisa.auctionmechanism;

import java.io.IOException;
import java.util.Date;

public interface AuctionMechanism {
    /**
     * Creates a new auction for a good.
     * @param _auction_name a String, the name identify the auction.
     * @param _end_time a Date that is the end time of an auction.
     * @param _reserved_price a double value that is the reserve minimum pricing selling.
     * @param _description a String describing the selling goods in the auction.
     * @return true if the auction is correctly created, false otherwise.
     */
    public boolean createAuction(String _auction_name, Date _end_time, double _reserved_price, String _description) throws IOException, ClassNotFoundException;

    /**
     * Checks the status of the auction.
     * @param _auction_name a String, the name of the auction.
     * @return a String value that is the status of the auction.
     */
    public String checkAuction(String _auction_name) throws IOException, ClassNotFoundException;

    /**
     * Places a bid for an auction if it is not already ended.
     * @param _auction_name a String, the name of the auction.
     * @param _bid_amount a double value, the bid for an auction.
     * @return a String value that is the status of the auction.
     */
    public String placeAbid(String _auction_name, double _bid_amount) throws IOException, ClassNotFoundException;
}
