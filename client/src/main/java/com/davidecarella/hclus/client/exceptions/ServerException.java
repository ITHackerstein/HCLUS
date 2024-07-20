package com.davidecarella.hclus.client.exceptions;

/**
 * Eccezione lanciata quando il server risponde con esito negativo a una richiesta.
 */
public class ServerException extends Exception {
    /**
     * I dettagli che specificano ulteriormente l'eccezione.
     */
    private final String details;

    /**
     * Costruisce l'eccezione con il messaggio, {@code message}, specificato come parametro.
     *
     * @param message il messaggio d'errore dell'eccezione
     */
    public ServerException(String message) {
        this(message, "");
    }

    /**
     * Costruice l'eccezione con il messaggio, {@code message}, specificato come parametro e i dettagli, {@code details},
     * specificati come parametro.
     *
     * @param message il messaggio dell'eccezione
     * @param details i dettagli dell'eccezione
     */
    public ServerException(String message, String details) {
        super(message);
        this.details = details;
    }

    /**
     * Restituisce i dettagli dell'eccezione.
     *
     * @return i dettagli dell'eccezione
     */
    public String getDetails() {
        return this.details;
    }
}
