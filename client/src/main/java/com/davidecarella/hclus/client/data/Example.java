package com.davidecarella.hclus.client.data;

import java.util.List;

/**
 * Record che rappresenta un esempio ricevuto dal server
 *
 * @param values la lista dei valori memorizzati nell'esempio
 */
public record Example(List<Double> values) {
}
