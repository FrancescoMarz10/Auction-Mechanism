# Auction Mechanism [![Build Status](https://travis-ci.org/FrancescoMarz10/Auction-Mechanism.svg?branch=master)](https://travis-ci.org/FrancescoMarz10/Auction-Mechanism)
Un meccanismo di aste basato su una rete P2P. Ogni peer può vendere e acquistare beni utilizzando un meccanismo di Second-Price Auctions (EBay). Il miglior offerente ottiene il primo slot, ma paga il prezzo offerto dal secondo miglior offerente. Il sistema consente agli utenti di creare una nuova asta (con un orario di fine, un prezzo di vendita riservato e una descrizione), controllare lo stato di un'asta e infine fare una nuova offerta per un'asta. Come descritto nell'API Java di AuctionMechanism.

```
Autore: Marzullo Francesco - Matricola: 0522500679
```

# Tecnologie Utilizzate
- Java 8
- Tom P2P
- JUnit
- Apache Maven
- Docker
- IntelliJ

# Struttura del Progetto 
Grazie all'utilizzo di Maven è possibile inserire all'interno del file pome le dipendenze a Tom P2P coem segue:
```
<repositories>
    <repository>
        <id>tomp2p.net</id>
         <url>http://tomp2p.net/dev/mvn/</url>
     </repository>
</repositories>
<dependencies>
   <dependency>
     <groupId>net.tomp2p</groupId>
     <artifactId>tomp2p-all</artifactId>
      <version>5.0-Beta8</version>
   </dependency>
</dependencies>
```
Il package ```src/main/java/it/unisa/auctionmechanism``` fornisce le seguenti classi Java:

 - MessageListener, un'interfaccia per il listener dei messaggi ricevuti dai peer.
 - AuctionMechanism, un'interfaccia che definisce i principali metodi del paradigma AuctionMechanism.
 - Auction, la classe rappresentante l'oggetto asta.
 - AuctionImplementation, un'implementazione dell'interfaccia AuctionMechanism che sfrutta la libreria Tom P2P.
 - Example, un esempio di applicazione della rete di peer in grado di utilizzare il meccanismo di aste sviluppato. 

# Sviluppo
La classe *Auction* è costituita dalle seguenti variabili di istanza:

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


## Interfaccia AuctionMechanism
L'interfaccia fornita per lo sviluppo del progetto AuctionMechanism è costituita dai seguenti metodi:

1. createAuction: per creare un'asta
2. checkAuction: per verificare lo stato dell'asta
3. placeAbid: per fare un'offerta

#### Metodo createAuction
Il metodo *createAuction* prende in input i seguenti valori:

    - _auction_name, nome dell'asta
    - _end_time, tempo di terminazione dell'asta
    - _reserved_price, prezzo di partenza dell'asta
    - _description, descrizione dell'oggetto in asta
    
Tale funzione si sviluppa attraverso i seguenti step:
1. Controlla che non sia già esistente un asta con il medesimo nome 
2. Crea una nuova asta con tutti parametri ricevuti.
3. Ricerca la presenza della lista dei nomi delle aste all'interno della dht.
4. In caso affermativo ottiene tale lista ed aggiunge il nome dell'asta ad essa e la ricarica nella dht. 
5. Infine, carica l'intera asta nella dht.
    
##### Implementazione 
```
public boolean createAuction(String _auction_name, Date _end_time, double _reserved_price, String _description) throws IOException, ClassNotFoundException {

        if(checkAuction(_auction_name) == null){
            Date actual_date = new Date();
            if (actual_date.after(_end_time)){
                return false;
            }
            Auction auction = new Auction(_auction_name,  peer_id,_end_time, _reserved_price,_description);
            FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
            
            auctions_names.add(_auction_name);
            dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
            dht.put(Number160.createHash(_auction_name)).data(new Data(auction)).start().awaitUninterruptibly();
            return true;
        }
        return false;
    }
```

#### Metodo checkAuction
Il metodo *checkAuction* prende in input solo il nome dell'asta da ricercare con l'obiettivo di verificarne la presenza all'interno della lista delle aste ed il suo eventuale stato.

Tale funzione si sviluppa attraverso i seguenti step:
1. Ricerca la presenza della lista dei nomi delle aste all'interno della dht
2. Se la ricerca ottiene un risultato affermativo scarica l'intera lista, altrimenti ne crea una nuova da caricare successivamente nella dht
3. Una volta ottenuta la lista, controlla la presenza di un asta che abbia il nome ottenuto come parametro
4. Nel caso in cui l'esito della ricerca abbia esito affermativo scarica l'oggetto 'Auction' corrispondente dalla dht
5. La funzione, poi, controlla se l'asta è attiva o terminata, in base alla data e all'ora di scadenza
5. In entrambi i casi restituisce lo stato dell'asta, mostrando l'eventuale vincitore o l'offerta maggiore e tutte le relative informazioni

##### Implementazione
```
public String checkAuction(String _auction_name) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {
            if (futureGet.isEmpty()) {
                dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
                return null;
            }
            
            auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
            
            if(auctions_names.contains(_auction_name)) {
                futureGet = dht.get(Number160.createHash(_auction_name)).start();
                futureGet.awaitUninterruptibly();

                if (futureGet.isSuccess()) {
                        Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();
                        Date actual_date = new Date();
                        if (actual_date.after(auction.get_end_time())) {
                            if (auction.get_reserved_price().toString().equals(auction.getMax_bid().toString())) {
                                return "The Auction is ended with no winner!";
                            } else {
                                if (auction.getBid_id() == peer_id) {
                                    return "The Auction is ended and the winner is you, " + auction.getBid_id() + ", with this bid: " + auction.getMax_bid() + " and the price is " + auction.getSecond_max_bid();
                                } else
                                    return "The Auction is ended and the winner is " + auction.getBid_id() + " with this bid: " + auction.getMax_bid() + " and the price is " + auction.getSecond_max_bid();
                            }

                        } else {
                            if (auction.getUsers().isEmpty()) {
                                return "The auction is active until " + auction.get_end_time() + " and the reserved price is: " + auction.get_reserved_price();
                            } else {
                                if (auction.getBid_id() == peer_id) {
                                    return "The auction is active until " + auction.get_end_time() + " and the highest offer is yours with: " + auction.getMax_bid();
                                } else
                                    return "The auction is active until " + auction.get_end_time() + " and the highest offer is: " + auction.getMax_bid();
                            }
                        }
                    }
            }
            return null;
        }
        return null;
    }

```
#### Metodo placeABid
Il metodo *placeABid* prende in input il nome dell'asta ed il valore dell'offerta che si desidera presentare.

Tale funzione si sviluppa attraverso i seguenti step:
1. Ricerca la presenza della lista dei nomi delle aste all'interno della dht
2. Se la ricerca ottiene un risultato affermativo scarica l'intera lista
3. Una volta ottenuta la lista, controlla la presenza di un asta che abbia il nome ottenuto come parametro
4. Nel caso in cui l'esito della ricerca abbia esito affermativo, scarica l'asta corrispondente e controlla se essa è attiva o terminata, in base alla data e all'ora di scadenza
5. Nel caso in cui l'asta fosse ancora attiva, chi la propone non è il creatore dell'asta e la nuova offerta supera quella attuale aggiorna tutte le informazioni relative alla nuova proposta ed al suo autore.
6. Aggiorna l'asta all'interno della dht così da rendere le modifiche visibili a tutti i peer.

##### Implementazione

```
 public String placeAbid(String _auction_name, double _bid_amount) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
        futureGet.awaitUninterruptibly();
        
        if (futureGet.isSuccess()) {
            Collection<Data> dataMapValues = futureGet.dataMap().values();

            if(dataMapValues.isEmpty()){
                return null;
            }
            else{
                auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
            }
            if (auctions_names.contains(_auction_name)) {
                futureGet = dht.get(Number160.createHash(_auction_name)).start();
                futureGet.awaitUninterruptibly();

                if (futureGet.isSuccess()) {
                    Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();
                    Date actual_date = new Date();
                    if (auction.get_creator() == peer_id) {
                        return "The creator can't do a bid!";
                    }
                    if (auction.getBid_id() == peer_id) {
                        return "You have already offered the highest bid!";
                    }
                    if (actual_date.after(auction.get_end_time())) {
                         if(Double.compare(auction.get_reserved_price(),auction.getMax_bid())==0){
                            return "You can't do a bid! The Auction is ended with no winner!";
                        } else {
                            return "You can't do a bid! The Auction is ended, the winner is " + auction.getBid_id() + " with this bid: " + auction.getMax_bid() + " and the price is " + auction.getSecond_max_bid();
                        }

                     //Checking if the new bid is better than the old one and updating all the variables of the auction
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
                        dht.put(Number160.createHash(_auction_name)).data(new Data(auction)).start().awaitUninterruptibly();
                        sendMessage("The new best bid on the " + _auction_name + " auction is " + auction.getMax_bid() + " by " + auction.getBid_id(), _auction_name);

                        return "The auction is active until " + auction.get_end_time() + " and the highest offer is yours with: " + auction.getMax_bid();
                    } else {
                        return "You can't do a bid lower then the biggest bid!";
                    }
                }
            }
        }
        return null;
    }
```

## Altri metodi implementati

#### Metodo sendMessage
Il metodo *sendMessage* viene utilizzato per notificare un peer che la sua offerta è stata appena superata da una nuova proposta di valore maggiore, oppure per avvisare i partecipanti ad un asta dell'improvviso abbandono della rete da parte del creatore dell'asta e dell'eliminazione di quest'ultima.
Questo metodo prende in input il messaggio da recapitare, il nome dell'asta a cui fa riferimento ed il tipo di messaggio da inviare.

#### Metodo  checkAllAuctions
Il metodo *checkAllAuctions* viene utilizzato per ottenere la completa lista delle aste attualmente presenti e alcune informazioni fondamentali ad esse relate, come lo stato e l'attuale migliore offerta.

#### Metodo removeAnAuction
Il metodo *removeAnAuction* permette al creatore di un asta, utilizzandone il nome ottenuto come parametro, di eliminarla.

#### Metodo exit
Il metodo *exit* viene utilizzato per permettere ad un nodo di uscire dal sistema. Inoltre, quando un nodo lascia la rete, vengono eliminate tutte le aste da esso create, resettate tutte le aste in cui è il miglior offerente ed i partecipanti a queste vengono avvisati tramite un messaggio, grazie al metodo removeMyAuctionsAndOffers().

#### Metodo removeMyAuctionsAndOffers
Il metodo *removeMyAuctionsAndOffers* viene richiamato dal metodo exit quando un peer lascia la rete e si preoccupa di eliminare tutte le aste e le offerte realizzate dal peer in questione.


# Testing
 I casi di test analizzati sono i seguenti:
 
 1. Offerta realizzata dal creatore dell'asta
 2. Offerta realizzata dall'attuale maggior offerente
 3. Offerta realizzata dopo la scadenza dell'asta
 4. Rimozione di un'asta da parte del creatore
 5. Rimozione di un'asta da parte di un semplice partecipante
 6. Asta terminata con vincitore
 7. Asta termianta senza vincitore
 8. Creazione di un asta già presente nella dht
 9. Abbandonare la rete da creatore di almeno un'asta
 10. Abbandonare la rete da miglior offerente su un'asta
 11. Controlla tutte le aste presenti nella dht
 12. Asta terminata con offerte multiple 
 13. Asta terminata con una singola offerta
 14. Controllare lo stato di un'asta non esistente 
 15. Controllare lo stato di un'asta senza offerenti
 16. Controllare lo stato di un'asta attiva con offerenti
 17. Effettuare un'offerta ad un'asta già terminata con un vincitore
 18. Effettuare un'offerta minore del minimo prezzo
 

# Come Buildare Auction Mechanism

### In un Container Docker
La prima operazione da eseguire nel terminale consiste nell'effettuare la build del docker container grazie alla seguente istruzione:
```
docker build --no-cache -t auctionmechanism .
```
### Avviare il Master Peer
Come seconda operazione dopo la build del container, bisogna avviare il master peer tramite la seguente riga di codice all'interno della linea di comando in modalità interactive (-i) e con due (-e) variabili di ambiente:
```
docker run -i --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 auctionmechanism
```
La variabile d'ambienbte MASTERIP è l'indirizzo ip del master peer e la variabile d'ambiente ID è l'id unico del peer. Ricorda che è necessario avviare il master peer con ID=0. 

### Avviare un Peer Generico
Una volta avviato il Master Peer è possibile avviare altri Peer grazie all'istruzione seguente e selezionando un ID univoco:
```
docker run -i --name PEER-1 -e MASTERIP="172.17.0.2" -e ID=1 auctionmechanism
```
