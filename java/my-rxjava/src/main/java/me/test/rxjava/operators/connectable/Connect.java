package me.test.rxjava.operators.connectable;

import io.reactivex.*;
import io.reactivex.flowables.*;
import me.test.rxjava.*;

import java.util.concurrent.*;

/**
 *
 */
public class Connect {


    public static void main(String[] args) throws InterruptedException {

        connect01();

    }


    // ConnectableFlowable 可以确保多次 subscribe 时，得到的消息队列都一样。
    // https://github.com/ReactiveX/RxJava/wiki/Connectable-Observable-Operators
    public static void connect01() throws InterruptedException {
        System.out.println("----------------------connect01");
        ConnectableFlowable<Integer> f = Flowable.range(1, 1000000)
                .sample(1, TimeUnit.MILLISECONDS)
                .publish();

        f.subscribe(i -> U.print("s1.subscribe", i));

//        Thread.sleep(1000);
        f.subscribe(i -> U.print("s2.subscribe ================= ", i));
        // 开始发送消息前，有两个订阅
        f.connect(i -> U.print("s1.connect", i));

        // 开始发送消息前，有一个订阅
//        Thread.sleep(1000);
        // FIXME: s3为何没有收到消息？ 若官方示例图中所示，connect() 之后应该能收到部分消息。
        f.subscribe(i -> U.print("s3.subscribe =================########### ", i));

        Thread.sleep(9000);
    }


}
