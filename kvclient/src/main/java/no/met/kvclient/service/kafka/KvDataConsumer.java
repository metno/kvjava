package no.met.kvclient.service.kafka;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

public class KvDataConsumer  implements Runnable{
    private KafkaStream<byte[],byte[]> stream;
    private String id;
    KafkaDataSubscriber subscribers;

    public KvDataConsumer(KafkaStream<byte[],byte[]> stream, KafkaDataSubscriber subscribers, String id) {
        this.stream= stream;
        this.id=id;
        this.subscribers=subscribers;
    }

    public void run() {
        ConsumerIterator<byte[], byte[]> it = stream.iterator();
        while (it.hasNext()){
        	String msg=new String(it.next().message());
            System.out.println("Thread " +id + ": " + msg);
            System.out.println("--------------------------------------");
        }
        System.out.println("Shutting down Thread: " + id);
    }
}
