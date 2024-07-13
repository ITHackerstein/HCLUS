package com.davidecarella.hclus.common.serialization;

import com.davidecarella.hclus.common.ClusterDistanceMethod;
import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.ClusteringStep;
import com.davidecarella.hclus.common.Example;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DataSerializer implements AutoCloseable {
    private final DataOutputStream dataOutputStream;

    public DataSerializer(OutputStream inputStream) {
        this.dataOutputStream = new DataOutputStream(inputStream);
    }

    public void serializeInt(int v) throws IOException {
        this.dataOutputStream.writeInt(v);
    }

    public void serializeDouble(double v) throws IOException {
        this.dataOutputStream.writeDouble(v);
    }

    public void serializeChar(char c) throws IOException {
        this.dataOutputStream.writeChar(c);
    }

    public void serializeString(String v) throws IOException {
        this.dataOutputStream.writeInt(v.length());
        this.dataOutputStream.writeChars(v);
    }

    public void serializeExample(Example example) throws IOException {
        this.dataOutputStream.writeInt(example.getSize());
        for (var value : example) {
            this.dataOutputStream.writeDouble(value);
        }
    }

    public void serializeClusterDistance(ClusterDistanceMethod clusterDistanceMethod) throws IOException {
        this.dataOutputStream.writeInt(clusterDistanceMethod.id());
        this.serializeString(clusterDistanceMethod.displayName());
    }

    public void serializeClusteringStep(ClusteringStep clusteringStep) throws IOException {
        this.dataOutputStream.writeInt(clusteringStep.firstClusterIndex());
        this.dataOutputStream.writeInt(clusteringStep.secondClusterIndex());
        this.dataOutputStream.writeInt(clusteringStep.newClusterSize());
    }

    public void serializeClustering(Clustering clustering) throws IOException {
        this.dataOutputStream.writeInt(clustering.exampleCount());
        this.dataOutputStream.writeInt(clustering.steps().length + 1);
        for (var step : clustering.steps()) {
            this.serializeClusteringStep(step);
        }
    }

    @Override
    public void close() throws IOException {
        this.dataOutputStream.close();
    }
}
