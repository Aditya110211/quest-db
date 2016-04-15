/*******************************************************************************
 *    ___                  _   ____  ____
 *   / _ \ _   _  ___  ___| |_|  _ \| __ )
 *  | | | | | | |/ _ \/ __| __| | | |  _ \
 *  | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *   \__\_\\__,_|\___||___/\__|____/|____/
 *
 * Copyright (C) 2014-2016 Appsicle
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * As a special exception, the copyright holders give permission to link the
 * code of portions of this program with the OpenSSL library under certain
 * conditions as described in each individual source file and distribute
 * linked combinations including the program with the OpenSSL library. You
 * must comply with the GNU Affero General Public License in all respects for
 * all of the code used other than as permitted herein. If you modify file(s)
 * with this exception, you may extend this exception to your version of the
 * file(s), but you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version. If you delete this
 * exception statement from all source files in the program, then also delete
 * it in the license file.
 *
 ******************************************************************************/

package com.nfsdb.mp;

import com.nfsdb.std.ObjectFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class ConcurrentTest {

    /**
     * <pre>
     *                    +--------+
     *               +--->| worker |
     *     +-----+   |    +--------+
     *     | pub |-->|
     *     +-----+   |    +--------+
     *               +--->| worker |
     *                    +--------+
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testOneToManyBusy() throws Exception {
        System.out.println("testOneToManyBusy");
        int cycle = 1024;
        int size = 1024 * cycle;
        RingQueue<Event> queue = new RingQueue<>(Event.FACTORY, cycle);
        SPSequence pubSeq = new SPSequence(cycle);
        MCSequence subSeq = new MCSequence(cycle, null);
        pubSeq.followedBy(subSeq);
        subSeq.followedBy(pubSeq);

        CyclicBarrier barrier = new CyclicBarrier(3);
        CountDownLatch latch = new CountDownLatch(2);

        BusyConsumer consumers[] = new BusyConsumer[2];
        consumers[0] = new BusyConsumer(size, subSeq, queue, barrier, latch);
        consumers[1] = new BusyConsumer(size, subSeq, queue, barrier, latch);

        consumers[0].start();
        consumers[1].start();

        barrier.await();
        int i = 0;
        while (true) {
            long cursor = pubSeq.next();
            if (cursor < 0) {
                continue;
            }
            queue.get(cursor).value = i++;
            pubSeq.done(cursor);

            if (i == size) {
                break;
            }
        }

        publishEOE(queue, pubSeq);
        publishEOE(queue, pubSeq);

        latch.await();

        int buf[] = new int[size];
        System.arraycopy(consumers[0].buf, 0, buf, 0, consumers[0].finalIndex);
        System.arraycopy(consumers[1].buf, 0, buf, consumers[0].finalIndex, consumers[1].finalIndex);
        Arrays.sort(buf);
        for (i = 0; i < buf.length; i++) {
            Assert.assertEquals(i, buf[i]);
        }
    }

    @Test
    public void testOneToManyWaiting() throws Exception {
        System.out.println("testOneToManyWaiting");
        int cycle = 1024;
        int size = 1024 * cycle;
        RingQueue<Event> queue = new RingQueue<>(Event.FACTORY, cycle);
        SPSequence pubSeq = new SPSequence(cycle);
        MCSequence subSeq = new MCSequence(cycle, new BlockingWaitStrategy());
        pubSeq.followedBy(subSeq);
        subSeq.followedBy(pubSeq);

        CyclicBarrier barrier = new CyclicBarrier(3);
        CountDownLatch latch = new CountDownLatch(2);

        WaitingConsumer consumers[] = new WaitingConsumer[2];
        consumers[0] = new WaitingConsumer(size, subSeq, queue, barrier, latch);
        consumers[1] = new WaitingConsumer(size, subSeq, queue, barrier, latch);

        consumers[0].start();
        consumers[1].start();

        barrier.await();
        int i = 0;
        while (true) {
            long cursor = pubSeq.nextBully();
            queue.get(cursor).value = i++;
            pubSeq.done(cursor);
            if (i == size) {
                break;
            }
        }

        publishEOE(queue, pubSeq);
        publishEOE(queue, pubSeq);

        latch.await();

        int buf[] = new int[size];
        System.arraycopy(consumers[0].buf, 0, buf, 0, consumers[0].finalIndex);
        System.arraycopy(consumers[1].buf, 0, buf, consumers[0].finalIndex, consumers[1].finalIndex);
        Arrays.sort(buf);
        for (i = 0; i < buf.length; i++) {
            Assert.assertEquals(i, buf[i]);
        }
    }

    @Test
    public void testOneToOneBusy() throws Exception {
        System.out.println("testOneToOneBusy");
        int cycle = 1024;
        int size = 1024 * cycle;
        RingQueue<Event> queue = new RingQueue<>(Event.FACTORY, cycle);
        Sequence pubSeq = new SPSequence(cycle);
        Sequence subSeq = new SCSequence(new BlockingWaitStrategy());
        pubSeq.followedBy(subSeq);
        subSeq.followedBy(pubSeq);

        CyclicBarrier barrier = new CyclicBarrier(2);
        CountDownLatch latch = new CountDownLatch(1);

        BusyConsumer consumer = new BusyConsumer(size, subSeq, queue, barrier, latch);
        consumer.start();

        barrier.await();
        int i = 0;
        while (true) {
            long cursor = pubSeq.next();
            if (cursor < 0) {
                continue;
            }
            queue.get(cursor).value = i++;
            pubSeq.done(cursor);
            if (i == size) {
                break;
            }
        }

        publishEOE(queue, pubSeq);

        latch.await();

        int buf[] = consumer.buf;
        for (i = 0; i < buf.length; i++) {
            Assert.assertEquals(i, buf[i]);
        }
    }

    @Test
    public void testOneToOneWaiting() throws Exception {
        System.out.println("testOneToOneWaiting");
        int cycle = 1024;
        int size = 1024 * cycle;
        RingQueue<Event> queue = new RingQueue<>(Event.FACTORY, cycle);
        Sequence pubSeq = new SPSequence(cycle);
        Sequence subSeq = new SCSequence(new BlockingWaitStrategy());
        pubSeq.followedBy(subSeq);
        subSeq.followedBy(pubSeq);

        CyclicBarrier barrier = new CyclicBarrier(2);
        CountDownLatch latch = new CountDownLatch(1);

        WaitingConsumer consumer = new WaitingConsumer(size, subSeq, queue, barrier, latch);
        consumer.start();

        barrier.await();
        int i = 0;
        while (true) {
            long cursor = pubSeq.nextBully();
            queue.get(cursor).value = i++;
            pubSeq.done(cursor);
            if (i == size) {
                break;
            }
        }

        publishEOE(queue, pubSeq);

        latch.await();

        int buf[] = consumer.buf;
        for (i = 0; i < buf.length; i++) {
            Assert.assertEquals(i, buf[i]);
        }
    }

    @Test
    public void testOneToParallelMany() throws Exception {
        System.out.println("testOneToParallelMany");
        int cycle = 1024;
        int size = 1024 * cycle;
        RingQueue<Event> queue = new RingQueue<>(Event.FACTORY, cycle);
        SPSequence pubSeq = new SPSequence(cycle);
        Sequence sub1 = new SCSequence();
        Sequence sub2 = new SCSequence();
        pubSeq.followedBy(new FanOut(sub1, sub2));
        sub1.followedBy(pubSeq);
        sub2.followedBy(pubSeq);

        CyclicBarrier barrier = new CyclicBarrier(3);
        CountDownLatch latch = new CountDownLatch(2);

        BusyConsumer consumers[] = new BusyConsumer[2];
        consumers[0] = new BusyConsumer(size, sub1, queue, barrier, latch);
        consumers[1] = new BusyConsumer(size, sub2, queue, barrier, latch);

        consumers[0].start();
        consumers[1].start();

        barrier.await();
        int i = 0;
        while (true) {
            long cursor = pubSeq.next();
            if (cursor < 0) {
                continue;
            }
            queue.get(cursor).value = i++;
            pubSeq.done(cursor);

            if (i == size) {
                break;
            }
        }

        publishEOE(queue, pubSeq);
        publishEOE(queue, pubSeq);

        latch.await();

        for (int k = 0; k < 2; k++) {
            for (i = 0; i < consumers[k].buf.length; i++) {
                Assert.assertEquals(i, consumers[k].buf[i]);
            }
        }
    }

    @Test
    public void testOneToParallelSubscriber() throws Exception {
        System.out.println("testOneToParallelSubscriber");
        int cycle = 1024;
        int size = 1024 * cycle;
        RingQueue<Event> queue = new RingQueue<>(Event.FACTORY, cycle);
        SPSequence pubSeq = new SPSequence(cycle);
        Sequence sub1 = new SCSequence();
        Sequence sub2 = new SCSequence();
        FanOut fanOut;
        pubSeq.followedBy(fanOut = new FanOut(sub1, sub2));
        sub1.followedBy(pubSeq);
        sub2.followedBy(pubSeq);

        CyclicBarrier barrier = new CyclicBarrier(4);
        CountDownLatch latch = new CountDownLatch(3);

        BusyConsumer consumers[] = new BusyConsumer[2];
        consumers[0] = new BusyConsumer(size, sub1, queue, barrier, latch);
        consumers[1] = new BusyConsumer(size, sub2, queue, barrier, latch);

        BusySubscriber subscriber = new BusySubscriber(queue, barrier, latch, fanOut, pubSeq);
        subscriber.start();

        consumers[0].start();
        consumers[1].start();

        barrier.await();
        int i = 0;
        while (true) {
            long cursor = pubSeq.next();
            if (cursor < 0) {
                continue;
            }
            queue.get(cursor).value = i++;
            pubSeq.done(cursor);

            if (i == size) {
                break;
            }
        }

        publishEOE(queue, pubSeq);
        publishEOE(queue, pubSeq);

        latch.await();

        for (int k = 0; k < 2; k++) {
            for (i = 0; i < consumers[k].buf.length; i++) {
                Assert.assertEquals(i, consumers[k].buf[i]);
            }
        }

        for (i = 0; i < subscriber.buf.length; i++) {
            Assert.assertTrue(subscriber.buf[i] > 0);
        }
    }

    private static void publishEOE(RingQueue<Event> queue, Sequence sequence) {
        long cursor = sequence.nextBully();
        queue.get(cursor).value = Integer.MIN_VALUE;
        sequence.done(cursor);
    }

    private static class BusyConsumer extends Thread {
        private final Sequence sequence;
        private final int[] buf;
        private final RingQueue<Event> queue;
        private final CyclicBarrier barrier;
        private final CountDownLatch latch;
        private volatile int finalIndex = 0;

        BusyConsumer(int cycle, Sequence sequence, RingQueue<Event> queue, CyclicBarrier barrier, CountDownLatch latch) {
            this.sequence = sequence;
            this.buf = new int[cycle];
            this.queue = queue;
            this.barrier = barrier;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                barrier.await();
                int p = 0;
                while (true) {
                    long cursor = sequence.next();
                    if (cursor < 0) {
                        continue;
                    }
                    int v = queue.get(cursor).value;
                    sequence.done(cursor);

                    if (v == Integer.MIN_VALUE) {
                        break;
                    }
                    buf[p++] = v;
                }

                finalIndex = p;
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class BusySubscriber extends Thread {
        private final int[] buf;
        private final RingQueue<Event> queue;
        private final CyclicBarrier barrier;
        private final CountDownLatch latch;
        private final FanOut fanOut;
        private final Sequence publisher;

        BusySubscriber(RingQueue<Event> queue, CyclicBarrier barrier, CountDownLatch latch, FanOut fanOut, Sequence publisher) {
            this.buf = new int[20];
            this.queue = queue;
            this.barrier = barrier;
            this.latch = latch;
            this.fanOut = fanOut;
            this.publisher = publisher;
        }

        @Override
        public void run() {
            try {
                barrier.await();
                Thread.sleep(10);

                // subscribe
                Sequence sequence = new SCSequence(publisher.current());
                sequence.followedBy(publisher);
                fanOut.add(sequence);
                int p = 0;
                while (p < buf.length) {
                    long cursor = sequence.next();
                    if (cursor < 0) {
                        continue;
                    }
                    int v = queue.get(cursor).value;
                    sequence.done(cursor);

                    if (v == Integer.MIN_VALUE) {
                        break;
                    }
                    buf[p++] = v;
                }

                fanOut.remove(sequence);

                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class WaitingConsumer extends Thread {
        private final Sequence sequence;
        private final int[] buf;
        private final RingQueue<Event> queue;
        private final CyclicBarrier barrier;
        private final CountDownLatch latch;
        private volatile int finalIndex = 0;

        WaitingConsumer(int cycle, Sequence sequence, RingQueue<Event> queue, CyclicBarrier barrier, CountDownLatch latch) {
            this.sequence = sequence;
            this.buf = new int[cycle];
            this.queue = queue;
            this.barrier = barrier;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                barrier.await();
                int p = 0;
                while (true) {
                    long cursor = sequence.waitForNext();
                    int v = queue.get(cursor).value;
                    sequence.done(cursor);

                    if (v == Integer.MIN_VALUE) {
                        break;
                    }
                    buf[p++] = v;
                }

                finalIndex = p;
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class Event {
        private static final ObjectFactory<Event> FACTORY = new ObjectFactory<Event>() {
            @Override
            public Event newInstance() {
                return new Event();
            }
        };
        private int value;
    }
}