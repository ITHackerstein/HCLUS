# HCLUS

Un sistema client-server per la scoperta di un dendrogramma di cluster con un algoritmo di [clustering agglomerativo](https://it.wikipedia.org/wiki/Clustering_gerarchico) scritto completamente in Java.

## Compilazione

### Prerequisiti

Per poter compilare il progetto c'è bisogno di un [JDK](https://www.oracle.com/java/), di seguito alcune possibilità:
* [GraalVM](https://www.graalvm.org/): consigliata per migliori performance
* [OpenJDK](https://adoptium.net/)

Il server avrà poi bisogno del DBMS [MySQL](https://mysql.com/) per memorizzare i dataset.

> **NOTA**: È richiesto che l'eseguibile `mysql` sia disponibile nella variabile d'ambiente `PATH`.

### Compilare il progetto

Prima di iniziare a compilare il progetto va creato il file `dmbs_root_password` nella cartella `assets` che deve contere
la password dell'utente `root` del DBMS.

> **NOTA**: Se non si ha intenzione di utilizzare il server si può lasciare il file vuoto.

Per compilare il progetto bisogna eseguire il seguente comando:
* **Linux**
```
$ ./gradlew build
```
* **Windows**
```
$ .\gradlew.bat build
```

Se si ha intenzione di compilare solo il client o il server si può invece eseguire il seguente comando:
* **Linux**
```
$ ./gradlew :<server-client>:build
```
* **Windows**
```
$ .\gradlew.bat :<server-client>:build
```
sostituendo a `<server-client>`, `server` o `client` in base a ciò che si desidera compilare.

## Documentazione

### Protocollo

Si veda: [docs/Protocollo.md](docs/Protocollo.md).

### Generazione del JavaDoc

È possibile generare la documentazione del sorgente con il seguente comando:
* **Linux**
```
$ ./gradlew javadocAll
```
* **Windows**
```
$ .\gradlew.bat javadocAll
```
A questo punto la documentazione si troverà in `docs/javadoc` e sarà possibile visualizzarla aprendo il file `index.html` 
in un browser.

### Istruzioni per l'uso

#### Server

Per poter avviare il server bisognerà prima eseguire il comando:
* **Linux**
```
$ ./gradlew installDatabase
```
* **Windows**
```
$ .\gradlew.bat installDatabase
```
che configurerà il DBMS per poter essere utilizzato dal server.

Se, in futuro, si desidera portare il DBMS al suo stato iniziale si potrà eseguire il comando:
* **Linux**
```
$ ./gradlew uninstallDatabase
```
* **Windows**
```
$ .\gradlew.bat uninstallDatabase
```

Dopodiché possiamo eseguire il server con il seguente comando:
```
$ java -jar server/build/libs/server-1.0.jar <port>
```
dove `port` è il numero della porta dove si metterà in ascolto il server.

Una volta avviato il server nella console si riceveranno vari messaggi di informazioni/errore in base alle 
connessioni da parte di client e delle loro richieste.

#### Client

Per poter avviare il client possiamo eseguire il seguente comando:
```
$ java -jar client/build/libs/client-1.0.jar
```

All'esecuzione del client verrà mostrata la seguente finestra

![Finestra del client](docs/imgs/client_inizio.png)

in cui possiamo notare la presenza di tre elementi distinti:
* un menu che contiene una sola voce (`Aiuto`) la quale mostrerà altre due voci: `Comandi dendrogramma` e `Informazioni 
  sull'applicazione`. La prima, in particolare, mostrerà i comandi che è possibile utilizzare nella sezione di 
  visualizzazione del dendrogramma (che verrà approfondita dopo);
* una sezione divisa in tre schede, di cui solo la prima (`Connessione`) è accessibile;
* una sezione con titolo `Dendrogramma`.

##### Connessione

Una volta inseriti i campi allora premendo il pulsante `Connetti` allora un tentativo di connessione al server avrà 
inizio e, se tutto va a buon fine, l'icona di stato della connessione (a sinistra del pulsante) diventerà verde e la 
scheda `Dataset` diventerà accessibile.

![Client connesso](docs/imgs/client_connesso.png)

> **NOTA**: Nel caso in cui la connessione non dovesse andare a buon fine allora verrà mostrata una finestra di dialogo 
> che descrive cosa non è andata a buon fine.
>
> ![Errore durante la connessione](docs/imgs/client_errore_connessione.png)

> **NOTA**: Nel caso in cui si decida di connettersi ad un altro server (e una connessione è già aperta) la 
> connessione precedente verrà chiusa.

##### Dataset

![Scheda dataset](docs/imgs/client_scheda_dataset.png)

Nella sezione `Dataset` possiamo scegliere un dataset da caricare sul server. Nel caso in cui ne 
conosciamo già il nome possiamo inserirlo nel campo di testo altrimenti possiamo utilizzare il pulsante alla destra di esso 
che visualizzerà una finestra di dialogo che contiene un elenco di quelli disponibili sul server.

![Finestra suggerimento dataset](docs/imgs/client_finestra_suggerimento_dataset.png)

> **NOTA**: Nel caso in cui la lettura dei dataset disponibili sul server non dovesse andare a buon fine verrà 
> mostrata una finestra di dialogo che descrive cosa non è andato a buon fine.
> 
> ![Errore lettura dataset disponibili](docs/imgs/client_errore_lettura_dataset_disponibili.png)

> **NOTA**: Nel caso in cui non ci siano dei dataset disponibili sul server allora viene mostrata una finestra di 
> dialogo che lo comunica.
> 
> ![Nessun dataset disponibile](docs/imgs/client_nessun_dataset_disponibile.png)

A questo punto possiamo selezionare uno degli elementi della lista (con un doppio click del mouse o con un singolo 
click del mouse seguito dall pressione del pulsante di conferma) e il suo nome comparirà nel campo di testo.
A quel punto premendo il pulsante `Carica` avrà inizio un tentativo di caricamento del dataset selezionato e, se 
tutto va a buon fine, allora verrà mostrate le informazioni sul dataset caricato e la scheda `Clustering` diventerà 
accessibile.

![Dataset caricato](docs/imgs/client_dataset_caricato.png)

> **NOTA**: Nel caso in cui il caricamento del dataset non dovesse andare a buon fine allora verrà mostrata una 
> finestra di dialogo che descrive cosa non è andato a buon fine.
> 
> ![Errore durante il caricamento del dataset](docs/imgs/client_errore_caricamento_dataset.png)

> **NOTA**: Nel caso in cui si decida di caricare un altro dataset (e un dataset è già stato caricato) si perde 
> quello precedente.

##### Clustering

![Scheda clustering](docs/imgs/client_scheda_clustering.png)

Nella sezione `Clustering` possiamo estrarre un clustering sul server o caricarne uno estratto in precendenza.
Per prima cosa possiamo selezionare, tramite una casella di controllo, se vogliamo estrarre un nuovo clustering o 
caricarne uno, inserire i campi richiesti e premere il pulsante per estrarre il clustering

Nel caso in cui si stia caricando un clustering e non si conosca il nome di quello che si vuole caricare lo si può 
selezionare con il pulsante alla destra del campo di testo che mostrerà una finestra di dialogo con la lista dei 
clustering disponibili sul server.

![Finestra suggerimento clustering](docs/imgs/client_finestra_suggerimento_clustering.png)

> **NOTA**: Nel caso in cui la lettura dei clustering disponibili sul server non dovesse andare a buon fine verrà 
> mostrata una finestra di dialogo che lo comunica.
> 
> ![Errore durante lettura clustering disponibili](docs/imgs/client_errore_lettura_clustering_disponibili.png)

> **NOTA**: Nel caso in cui non ci siano clustering disponibili verrà mostrata una finestra di dialogo che lo comunica.
>
> ![Nessun clustering disponibile](docs/imgs/client_nessun_clustering_disponibile.png)

A questo punto possiamo selezionare uno degli elementi della lista (con un doppio click del mouse o con un singolo 
click del mouse seguito dalla pressione del pulsante di conferma) e il suo nome comparirà nel campo di testo.

Premuto il pulsante per estrarre, un tentativo di estrazione del clustering avrà inizio e, se tutto va a buon fine, 
allora verrà visualizzato il dendrogramma associato al clustering.

> **NOTA**: Nel caso in cui l'estrazione del clustering non dovesse andare a buon fine allora verrà mostrata una 
> finestra di dialogo che descrive cosa non è andato a buon fine.
> 
> ![Errore durante il clustering](docs/imgs/client_errore_clustering.png)
> 

> **NOTA**: Nel caso in cui si decida di estrarre un altro clustering (e un clustering era già stato estratto) si 
> perde quello estratto in precedenza.

##### Dendrogramma

![Scheda clustering](docs/imgs/client_demo_clustering.mp4)