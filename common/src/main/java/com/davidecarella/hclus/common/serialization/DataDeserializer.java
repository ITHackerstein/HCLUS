package com.davidecarella.hclus.common.serialization;

import com.davidecarella.hclus.common.ClusterDistanceMethod;
import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.ClusteringStep;
import com.davidecarella.hclus.common.Example;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DataDeserializer implements AutoCloseable {
    private final DataInputStream dataInputStream;

    public DataDeserializer(InputStream inputStream) {
        this.dataInputStream = new DataInputStream(inputStream);
    }

    public int deserializeInt() throws IOException {
        return this.dataInputStream.readInt();
    }

    public double deserializeDouble() throws IOException {
        return this.dataInputStream.readDouble();
    }

    public char deserializeChar() throws IOException {
        return this.dataInputStream.readChar();
    }

    public String deserializeString() throws IOException {
        var size = this.dataInputStream.readInt();
        var stringBuilder = new StringBuilder();
        while (size-- > 0) {
            stringBuilder.append(this.dataInputStream.readChar());
        }
        return stringBuilder.toString();
    }

    public Example deserializeExample() throws IOException {
        var size = this.dataInputStream.readInt();
        var example = new Example();
        while (size-- > 0) {
            example.add(this.dataInputStream.readDouble());
        }
        return example;
    }

    public ClusterDistanceMethod deserializeClusterDistanceMethod() throws IOException {
        return new ClusterDistanceMethod(
            this.dataInputStream.readInt(),
            this.deserializeString()
        );
    }

    public ClusteringStep deserializeClusteringStep() throws IOException {
        return new ClusteringStep(
            this.dataInputStream.readInt(),
            this.dataInputStream.readInt(),
            this.dataInputStream.readInt()
        );
    }

    public Clustering deserializeClustering() throws IOException {
        var exampleCount = this.dataInputStream.readInt();
        var depth = this.dataInputStream.readInt();
        var steps = new ClusteringStep[depth - 1];
        for (int i = 0; i < depth - 1; ++i) {
            steps[i] = this.deserializeClusteringStep();
        }
        return new Clustering(exampleCount, steps);
    }

    @Override
    public void close() throws IOException {
        this.dataInputStream.close();
    }
}
