# Protocollo HCLUS

## Tabella dei contenuti

* [1. Codifica dei dati](#1-codifica-dei-dati)
  * [1.1. `int`](#11-int)
  * [1.2. `double`](#12-double)
  * [1.3. `char`](#13-char)
  * [1.4. Array](#14-array)
  * [1.5. `String`](#15-string)
  * [1.6. `Example`](#16-example)
  * [1.7. `ClusterDistanceMethod`](#17-clusterdistancemethod)
  * [1.8. `ClusteringStep`](#18-clusteringstep)
  * [1.9. `Clustering`](#19-clustering)
* [2. Richieste e risposte](#2-richieste-e-risposte)
  * [2.1. `LoadDataset`](#21-loaddataset)
  * [2.2. `NewClustering`](#22-newclustering)
  * [2.3. `LoadClustering`](#23-loadclustering)
  * [2.4. `GetExamples`](#24-getexamples)
  * [2.5. `GetClusterDistanceMethods`](#25-getclusterdistancemethods)
  * [2.6. `GetSavedClusterings`](#26-getsavedclusterings)
  * [2.7. `CloseConnection`](#27-closeconnection)

Il seguente documento specifica il protocollo HCLUS utilizzato dal server e il client per comunicare fra loro.

## 1. Codifica dei dati

Il seguente paragrafo si occupa di descrivere le codifiche per i dati utilizzati dal protocollo.

### 1.1. `int`

Il dato `int`, rappresentante un intero (con segno), viene codificato con il complemento a due a 32 bit. 
L'ordinamento dei byte è Big-Endian.

### 1.2. `double`

Il dato `double`, rappresentante un numero reale, viene codificato secondo lo standard IEEE-754 a doppia precisione 
(64 bit). L'ordinamento dei byte è Big-Endian.

### 1.3. `char`

Il dato `char`, rappresentante un carattere, viene codificato secondo lo standard UTF-16. L'ordinamento dei byte è 
Big-Endian.

### 1.4. Array

Indicheremo un array con il nome del tipo seguito dalla sua lunghezza racchiusa in delle parentesi quadre (es.: `int[5]`).

### 1.5. `String`

Il dato `String`, rappresentante una stringa (un array di caratteri), è l'aggregazione dei seguenti dati:
* `length`: `int`
* `chars`: `char[length]`

### 1.6. `Example`

Il dato `Example`, rappresentante un esempio, è l'aggregazione dei seguenti dati:
* `size`: `int`
* `values`: `double[size]`

### 1.7. `ClusterDistanceMethod`

Il dato `ClusterDistanceMethod`, rappresentante un metodo per il calcolo della distanza fra cluster, è 
l'aggregazione dei seguenti dati:
  * `id`: `int`
  * `displayName`: `String`

### 1.8. `ClusteringStep`

Il dato `ClusteringStep`, rappresentante un passo del clustering, è l'aggregazione dei seguenti dati:
  * `firstClusterIndex`: `int`
  * `secondClusterIndex`: `int`
  * `newClusterSize`: `int`

### 1.9. `Clustering`

Il dato `Clustering`, rappresentate un clustering, è l'aggregazione dei seguenti dati:
  * `exampleCount`: `int`
  * `stepCount`: `int`
  * `steps`: `stepCount` `ClusteringStep[stepCount]`

## 2. Richieste e risposte

Il client comunica con il server inviando delle **richieste** a cui il server risponde inviando delle **risposte**.

Ogni richiesta seguirà il seguente formato:
* `requestType`: `int`
* `arguments` (cambiano in base alla richiesta e possono essere opzionali)

Le risposte del server seguiranno invece il seguente formato:
* `responseType`: `int`, può essere `0` per indicare che non ci sono stati errori o `1` per indicare la presenza di 
  errori
* `data` (cambia in base alla risposta e può essere opzionale)

Vediamole ora nello specifico.

### 2.1. `LoadDataset`

La seguente richiesta viene inviata dal client quando desidera caricare sul server un dataset.

Il formato è il seguente:
* `requestType = 0`
* `tableName`: `String`

Il server proverà a caricare il dataset dalla tabella del DBMS il cui nome è `tableName`. 

Se il tutto avviene con successo il server risponde con la seguente risposta:
* `requestType = 0`
* `exampleCount`: `int`, il numero di esempi contenuti nel dataset caricato
 
altrimenti risponde con:
* `responseType = 1`
* `errorMessage`: `String`

### 2.2. `NewClustering`

Viene inviata dal client quando desidera estrarre un nuovo clustering.

Il formato è il seguente:
* `requestType = 1`
* `depth`: `int`
* `distanceId`: `int`
* `name`: `String`

> **NOTA**: `name` **DEVE** contenere solo lettere maiuscole o minuscole, numeri e `_`.

Il server proverà a estrarre un nuovo clustering di profondità `depth` e nome `name` con il metodo per il calcolo 
della distanza fra cluster identificato da `distanceId` e lo salva.

> **NOTA**: Come il clustering viene salvato dipende dal server.

Se il tutto avviene con successo il server risponde con la seguente risposta:
* `requestType = 0`
* `clustering`: `Clustering`, il clustering estratto
 
altrimenti risponde con:
* `responseType = 1`
* `errorMessage`: `String`

### 2.3. `LoadClustering`

Viene inviata dal client quando desidera caricare un clustering salvato sul server.

Il formato è il seguente:
* `requestType = 2`
* `name`: `String`

Il server proverà a caricare il clustering di nome `name`.

Se il tutto avviene con successo il server risponde con la seguente risposta:
* `requestType = 0`
* `clustering`: `Clustering`, il clustering caricato

altrimenti risponde con:
* `responseType = 1`
* `errorMessage`: `String`

### 2.4. `GetExamples`

Viene inviata quando il client desidera ricevere degli esempi memorizzati nel dataset.

Il formato è il seguente:
* `requestType = 3`
* `indexCount`: `int`
* `indices`: `int[indexCount]`

Il server proverà a creare un array di `Example` di dimensione `indexCount`, sia questo `examples`, tale che per ogni 
posizione `i` si ha che `examples[i] = esempio numero indices[i]`.

Se il tutto avviene con successo il server risponde con la seguente risposta:
* `responseType = 0`
* `examples`

altrimenti risponde con:
* `repsonseType = 1`
* `errorMessage`: `String`

### 2.5. `GetClusterDistanceMethods`

Viene inviata quando il client desidera ricevere i metodi per il calcolo della distanza tra cluster che il server 
mette a disposizione per l'estrazione di nuovi clustering.

Il formato è il seguente:
* `requestType = 4`

> **NOTA**: Questa richiesta non dovrebbe generare errori.

Il server risponde con:
* `responseType = 0`
* `count`: `int`
* `clusterDistanceMethods`: `ClusterDistanceMethod[count]`

### 2.6. `GetSavedClusterings`

Viene inviata quando il client desidera ricevere la lista dei nomi dei clustering salvati sul server.

Il formato è il seguente:
* `requestType = 5`

Il server proverà a elencare i nomi dei clustering salvati e, se non si dovessero verificare errori, risponde con:
* `responseType = 0`
* `count`: `int`
* `clusteringNames`: `String[count]`, l'elenco dei nomi dei clustering salvati

altrimenti risponde con:
* `responseType = 1`
* `errorMessage`: `String`

### 2.7. `CloseConnection`

Viene inviata quando il client desidera chiudere la connessione.

Il formato è il seguente:
* `requestType = 6`

> **NOTA**: Questa richiesta non dovrebbe generare errori.

Il server risponderà con:
* `responseType = 0`
e chiuderà la connessione.
