//package org.k3a;
//
//import com.lmax.disruptor.*;
//import com.lmax.disruptor.dsl.Disruptor;
//import org.mole.tracer.consumer.ValueEvent;
//
///**
// * Created by k3a
// * on 19-1-8  上午10:45
// */
//public class VarArgTest {
//
//    public static void main(String[] args) throws InterruptedException {
//        final EventHandler<ValueEvent> handler = new EventHandler<ValueEvent>()
//        {
//            public void onEvent(final ValueEvent event, final long sequence, final boolean endOfBatch) throws Exception
//            {
//                // process a new event.
//            }
//        };
//
//        RingBuffer<ValueEvent> ringBuffer =
//                new RingBuffer<ValueEvent>(ValueEvent.EVENT_FACTORY,
//                        new SingleThreadedClaimStrategy(RING_SIZE),
//                        new SleepingWaitStrategy());
//
//        SequenceBarrier<ValueEvent> barrier = ringBuffer.newBarrier();
//        BatchEventProcessor<ValueEvent> eventProcessor = new BatchEventProcessor<ValueEvent>(barrier, handler);
//        ringBuffer.setGatingSequences(eventProcessor.getSequence());
//
//// Each EventProcessor can run on a separate thread
//        EXECUTOR.submit(eventProcessor);
//
//        Disruptor<ValueEvent> disruptor =
//                new Disruptor<ValueEvent>(ValueEvent.EVENT_FACTORY, EXECUTOR,
//                        new SingleThreadedClaimStrategy(RING_SIZE),
//                        new SleepingWaitStrategy());
//        disruptor.handleEventsWith(handler);
//        RingBuffer<ValueEvent> ringBuffer = disruptor.start();
//
//    }
//
//
//}
