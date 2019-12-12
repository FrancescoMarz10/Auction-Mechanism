# Auction Mechanism
Un meccanismo di aste basato su una rete P2P. Ogni peer può vendere e acquistare beni utilizzando un meccanismo di Second-Price Auctions (EBay). Quest'ultimo è un meccanismo di asta non veritiero per più oggetti. Ogni offerente effettua un'offerta. Il miglior offerente ottiene il primo slot, il secondo più alto, il secondo slot e così via, ma il miglior offerente paga il prezzo offerto dal secondo miglior offerente, il secondo più alto paga il prezzo offerto dal terzo più alto, e presto. Il sistema consente agli utenti di creare una nuova asta (con un orario di fine, un prezzo di vendita riservato e una descrizione), controllare lo stato di un'asta e infine fare una nuova offerta per un'asta. Come descritto nell'API Java di AuctionMechanism.

# Implementazione
La classe Auction è costituita dalle seguenti variabili di istanza:

- _auction_name, il nome dell'asta
- _creator, l'id del peer creatore dell'asta
- _end_time, data e ora di scadenza dell'asta
- _reserved_price, il tempo di partenza
- _description, descrizione del prodotto messo all'asta
- max_bid, la migliore offerta ricevuta
- second_max_bid, la seconda migliore offerta ricevuta
- bid_id, l'id del miglior offerente
- peerAddress_bid, il peer address del miglior offerente
- old_bid_Address, il peer address del precedente miglior offerente
- users, lista di peer partecipanti all'asta

Di seguito è riportata l'implementazione di tale classe:

```
public class Auction implements Serializable {

    String _auction_name;
    int _creator;
    Date _end_time;
    Double _reserved_price;
    Double max_bid;
    Double second_max_bid;
    int bid_id;
    PeerAddress peerAddress_bid;
    PeerAddress old_bid_Address;
    String _description;
    HashSet<PeerAddress> users;

    //constructor
    public Auction(String name, int id_creator, Date et, double res_price, String desc){
        _auction_name = name;
        _creator = id_creator;
        _end_time = et;
        _reserved_price = res_price;
        _description = desc;
        max_bid = res_price;
        second_max_bid = res_price;
        bid_id=0;
        users = new HashSet<PeerAddress>();
    }

    public Auction(){
        //void constructor
    }

    //get and set methods
    
    //toString() method
    @Override
    public String toString() {
        return "Auction{" +
                "_auction_name='" + _auction_name + '\'' +
                ", _creator=" + _creator +
                ", _end_time=" + _end_time +
                ", _reserved_price=" + _reserved_price +
                ", max_bid=" + max_bid +
                ", second_max_bid=" + second_max_bid +
                ", bid_id=" + bid_id +
                ", peerAddress_bid=" + peerAddress_bid +
                ", old_bid_Address=" + old_bid_Address +
                ", _description='" + _description + '\'' +
                ", users=" + users +
                '}';
    }
}

```

## Interfaccia AuctionMechanism
L'interfaccia fornita per lo sviluppo del progetto AuctionMechanism è costituita dai seguenti metodi:

1. createAuction: per creare un'asta
2. checkAuction: per verificare lo stato dell'asta
3. placeAbid: per fare un'offerta

#### Metodo createAuction
Il metodo createAuction prende in input i seguenti valori:
    - _auction_name, nome dell'asta
    - _end_time, tempo di terminazione dell'asta
    - _reserved_price, prezzo di partenza dell'asta
    - _description, descrizione dell'oggetto in asta
    
Tale funzione si sviluppa attraverso i seguenti step:
1. Controlla che non sia già esistente un asta con il medesimo nome 
2. Crea una nuova asta con tutti parametri ricevuti.
3. Ricerca la presenza della lista di aste all'interno della dht.
4. In caso affermativo ottiene tale lista, aggiunge l'asta ad essa e la ricarica nella dht. 
    
##### Implementazione 
```
public boolean createAuction(String _auction_name, Date _end_time, double _reserved_price, String _description) throws IOException, ClassNotFoundException {

        if(checkAuction(_auction_name) == null){

            Auction auction = new Auction(_auction_name,  peer_id,_end_time, _reserved_price,_description);
            FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();

            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
               auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
            }
            
            auctions.put(_auction_name,auction);
            dht.put(Number160.createHash("auctions")).data(new Data(auctions)).start().awaitUninterruptibly();
            return true;
        }
        return false;
    }
```

#### Metodo checkAuction
Il metodo checkAuction prende in input solo il nome dell'asta da ricercare con l'obiettivo di verificarne la presenza all'interno della lista delle aste ed il suo eventuale stato.

Tale funzione si sviluppa attraverso i seguenti step:
1. Ricerca la presenza della lista di aste all'interno della dht
2. Se la ricerca ottiene un risultato affermativo scarica l'intera lista, altrimenti ne crea una nuova da caricare successivamente nella dht
3. Una volta ottenuta la lista, controlla la presenza di un asta che abbia il nome ottenuto come parametro
4. Nel caso in cui l'esito della ricerca abbia esito affermativo la funzione controlla se essa è attiva o terminata, in base alla data e all'ora di scadenza
5. In entrambi i casi restituisce lo stato dell'asta, mostrando l'eventuale vincitore o l'offerta maggiore e tutte le relative informazioni

##### Implementazione
```
 public String checkAuction(String _auction_name) throws IOException, ClassNotFoundException {

        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {
            if (futureGet.isEmpty()) {
                dht.put(Number160.createHash("auctions")).data(new Data(auctions)).start().awaitUninterruptibly();
                return null;
            }
            
            HashMap<String, Auction> auctions;
            auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
            
            if(auctions.containsKey(_auction_name)) {
                Auction auction = auctions.get(_auction_name);
                Date actual_date = new Date();

                if (actual_date.after(auction.get_end_time())) {
                    if(auction.get_reserved_price().toString().equals(auction.getMax_bid().toString())){
                        return "The Auction is ended with no winner!";
                    }
                    else{
                        if(auction.getBid_id()==peer_id){
                            return "The Auction is ended and the winner is you, " + auction.getBid_id() + ", with this bid: " + auction.getMax_bid() +" and the price is " + auction.getSecond_max_bid();
                        }
                        else return "The Auction is ended and the winner is " + auction.getBid_id() + " with this bid: " + auction.getMax_bid()+" and the price is " + auction.getSecond_max_bid();
                    }

                } else {
                    if(auction.getUsers().isEmpty()){
                        return "The auction is active until "+ auction.get_end_time()+" and the reserved price is: " + auction.get_reserved_price();
                    }
                    else {
                        if(auction.getBid_id()==peer_id){
                            return "The auction is active until "+ auction.get_end_time()+" and the highest offer is yours with: " + auction.getMax_bid();
                        }
                        else return "The auction is active until "+ auction.get_end_time()+" and the highest offer is: " + auction.getMax_bid();
                    }
                }
            }
            return null;
        }
        return null;
    }
```
#### Metodo placeABid
Il metodo placeABid prende in input il nome dell'asta ed il valore dell'offerta che si desidera presentare.

Tale funzione si sviluppa attraverso i seguenti step:
1. Ricerca la presenza della lista di aste all'interno della dht
2. Se la ricerca ottiene un risultato affermativo scarica l'intera lista
3. Una volta ottenuta la lista, controlla la presenza di un asta che abbia il nome ottenuto come parametro
4. Nel caso in cui l'esito della ricerca abbia esito affermativo la funzione controlla se essa è attiva o terminata, in base alla data e all'ora di scadenza
5. Nel caso in cui l'asta fosse ancora attiva, chi la propone non è il creatore dell'asta e la nuova offerta supera quella attuale aggiorna tutte le informazioni relative alla nuova proposta ed al suo autore.
6. Aggiorna l'asta all'interno della dht così da rendere le modifiche visibili a tutti i peer.

##### Implementazione

```
public String placeAbid(String _auction_name, double _bid_amount) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {

            Collection<Data> dataMapValues = futureGet.dataMap().values();

            HashMap<String, Auction> auctions;
            if(dataMapValues.isEmpty()){
                return null;
            }
            else{
                auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
            }

            if (auctions.containsKey(_auction_name)) {
                Auction auction = auctions.get(_auction_name);
                Date actual_date = new Date();

                if(auction.get_creator()== peer_id){
                    return "The creator can't do a bid!";
                }
                if(auction.getBid_id() == peer_id){
                    return "You have already offered the highest bid!";
                }

                if (actual_date.after(auction.get_end_time())) {
                    if(auction.get_reserved_price().toString().equals(auction.getMax_bid().toString())){
                        return "You can't do a bid! The Auction is ended with no winner!";
                    }
                    else{
                        return "You can't do a bid! The Auction is ended, the winner is " + auction.getBid_id() + " with this bid: " + auction.getMax_bid()+" and the price is " + auction.getSecond_max_bid();
                    }

                } else if (_bid_amount > auction.getMax_bid()) {

                    auction.setSecond_max_bid(auction.getMax_bid());
                    auction.setMax_bid(_bid_amount);

                    auction.setOld_bid_Address(auction.getPeerAddress_bid());
                    auction.setPeerAddress_bid(peer.peerAddress());

                    auction.setBid_id(peer_id);

                    if (!auction.getUsers().contains(peer_id)) {
                        auction.getUsers().add(peer.peerAddress());
                        auction.setPeerAddress_bid(peer.peerAddress());
                    }

                    auctions.put(_auction_name, auction);

                    dht.put(Number160.createHash("auctions")).data(new Data(auctions)).start().awaitUninterruptibly();
                    sendMessage("The new best bid on the "+ _auction_name+" auction is "+auction.getMax_bid()+" by "+ auction.getBid_id(),_auction_name);

                    return "The auction is active until "+ auction.get_end_time()+" and the highest offer is yours with: " + auction.getMax_bid();
                }
                else {
                    return "You can't do a bid lesser then the biggest bid!";
                }
            }
        }
        return null;
    }
```

## Altri metodi implementati

#### Metodo sendMessage
Il metodo sendMessage viene utilizzato per notificare un peer che la sua offerta è stata appena superata da una nuova proposta di valore maggiore.
Questo metodo prende in input il messaggio da recapitare e il nome dell'asta a cui fa riferimento.

Tale funzione si sviluppa attraverso i seguenti step:
1. Ricerca la presenza della lista di aste all'interno della dht
2. Se la ricerca ottiene un risultato affermativo scarica l'intera lista
3. Una volta ottenuta la lista, controlla la presenza di un asta che abbia il nome ottenuto come parametro
4. Scorre la lista dei peer che hanno partecipato all'asta e invia il messaggio al precedente vincitore momentaneo se tale lista ha più di un elemento al suo interno.


##### Implementazione
```
public boolean sendMessage(Object _obj,String _auction_name) throws IOException, ClassNotFoundException {

        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {
            Collection<Data> dataMapValues = futureGet.dataMap().values();
            HashMap<String, Auction> auctions;
            if (dataMapValues.isEmpty()) {
                return false;
            } else {
                auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
            }
            if (auctions.containsKey(_auction_name)) {
                Auction auction = auctions.get(_auction_name);
                HashSet<PeerAddress> users = auction.getUsers();
                for (PeerAddress  mypeer : users) {
                    if(mypeer.equals(auction.getOld_bid_Address()) && users.size()>1) {
                        FutureDirect futureDirect = dht.peer().sendDirect(mypeer).object(_obj).start();
                        futureDirect.awaitUninterruptibly();
                    }

                }
            }
        }
        return true;
    }
    
```

#### Metodo  checkAllAuctions
Il metodo checkAllAuctions viene utilizzato per ottenere la completa lista delle aste attualmente presenti e alcune informazioni fondamentali ad esse relate, come lo stato e l'attuale migliore offerta.

Tale funzione si sviluppa attraverso i seguenti step:
1. Ricerca la presenza della lista di aste all'interno della dht
2. Se la ricerca ottiene un risultato affermativo scarica l'intera lista
3. Scorrendo l'intera lista delle aste presenti en controlla la scadenza e setta di conseguenza la variabile status relativa ad ognuna come 'ENDED' o 'ACTIVE'
4. Costrusice la stringa contenente tutte le aste presenti con le relative informazioni di base e la restituisce

##### Implementazione
```
 public String checkAllAuctions() throws IOException, ClassNotFoundException {

        String all_auctions = "";
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();
        String status = "";

        if (futureGet.isSuccess()) {
            if(!futureGet.dataMap().values().isEmpty()) {
                HashMap<String, Auction> auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
                if (!auctions.isEmpty()) {
                    for (String name : auctions.keySet()) {

                        Date actual_date = new Date();

                        if (actual_date.after(auctions.get(name).get_end_time())) {
                            status = "ENDED";
                        }
                        else{
                            status = "ACTIVE";
                        }
                        if(auctions.get(name).getUsers().isEmpty()){
                            all_auctions += "Name: " + name + ", Reserved Price: " + auctions.get(name).getMax_bid() +", Status: "+status+ ", Description: " + auctions.get(name).get_description()+ "\n";

                        }
                        else{
                            all_auctions += "Name: " + name + ", Best Bid: " + auctions.get(name).getMax_bid() +", Status: "+status+ ", Description: " + auctions.get(name).get_description()+ "\n";
                        }

                    }
                    return all_auctions;
                }
            }
        }
        return null;
    }
```

#### Metodo removeAnAuction
Il metodo removeAnAuction permette al creatore di un asta, utilizzandone il nome ottenuto come parametro, di eliminarla.

Tale funzione si sviluppa attraverso i seguenti step:
1. Ricerca la presenza della lista di aste all'interno della dht
2. Se la ricerca ottiene un risultato affermativo scarica l'intera lista
3. Scorrendo l'intera lista delle aste si ricerca quella con il nome corrispondente al parametro ricevuto e, dopo aver controllato se è il creatore a richiamare il metodo, si elimina dalla lista
4. La lista modificata viene ricaricata nella dht


##### Implementazione
```
 public boolean removeAnAuction(String _auction_name) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {
            Collection<Data> dataMapValues = futureGet.dataMap().values();

            HashMap<String, Auction> auctions;
            if(dataMapValues.isEmpty()){
                return false;
            }
            else{
                auctions = (HashMap<String, Auction>) futureGet.dataMap().values().iterator().next().object();
            }

            for (String name : auctions.keySet()) {
                if (name.equals(_auction_name) && auctions.get(name).get_creator()==peer_id ) {
                    auctions.remove(name);
                    dht.put(Number160.createHash("auctions")).data(new Data(auctions)).start().awaitUninterruptibly();
                    return true;
                }
            }
        }
        return false;
    }   
 ```
 
 #### Metodo exit
 Il metodo exit viene utilizzato per permettere ad un nodo di uscire dal sistema.
 
 #### Implementazione
 ```
 public boolean exit(){
        try {
            dht.peer().announceShutdown().start().awaitUninterruptibly();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
 ```
 
 # Testing
 I casi di test analizzati sono i seguenti:
 1. Offerta realizzata dal creatore dell'asta
 2. Offerta realizzata dall'attuale maggior offerente
 3. Offerta realizzata dopo la scadenza dell'asta
 4. Rimozione di un asta (diversi casi)
 5. Asta terminata senza offerte
 6. Asta con e senza vincitore
 7. Creazione di un asta già presente
 
 ### 1.placeABidAsCreator()
  ```
  void placeABidAsCreator(){
        try {
            Date date = new Date();
            peer0.createAuction("Notebook MSI", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 1500, "A notebook is a small, portable personal computer (PC)");
            assertEquals(peer0.placeAbid("Notebook MSI", 1500), "The creator can't do a bid!");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
 ```
 ### 2. placeABidAsBestOfferer()
```
    void placeABidAsBestOfferer(){
        try {
            Date date = new Date();
            peer0.createAuction("Notebook MSI", new Date(Calendar.getInstance().getTimeInMillis() + 1000), 1500, "A notebook is a small, portable personal computer (PC)");
            peer1.placeAbid("Notebook MSI", 1600);
            assertEquals(peer1.placeAbid("Notebook MSI", 1800), "You have already offered the highest bid!");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
```
 
 

    //place a bid (all cases) -
    //bid by a creator -
    //bid by the big offerer -
    //remove an auction -
    //bid when auction is ended -
    //no winner and winner -
    //messages?
    //aucton without offers -
    //leave auction???
    //multiple bids -
    //see all auctions -
