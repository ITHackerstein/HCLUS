# HCLUS

Un sistema client-server per la scoperta di un dendrogramma di cluster con un algoritmo di [clustering agglomerativo](https://it.wikipedia.org/wiki/Clustering_gerarchico) scritto completamente in Java.

## Compilazione

### Prerequisiti

Per poter compilare il progetto c'è bisogno di un [JDK](https://www.oracle.com/java/), di seguito alcune possibilità:
* [GraalVM](https://www.graalvm.org/): consigliata per migliori performance
* [OpenJDK](https://adoptium.net/)

Il server avrà poi bisogno del DBMS [MySQL](https://mysql.com/) per memorizzare i dataset.

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

> TODO

#### Client

Per poter avviare il client possiamo eseguire il seguente comando:
```
$ java -jar client/build/libs/client-1.0.jar <address> <port>
```
dove `address` è l'indirizzo del server e `port` è la porta dove questo è in ascolto.

> TODO
