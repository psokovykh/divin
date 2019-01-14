package io.github.psokovykh.divin.vc;

import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.core.ResponseMessage;
import io.github.psokovykh.divin.model.BasicResponse;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class BasicAmTests {

	final Duration TOO_LONG_OP = Duration.of(1000, MILLIS);
	final Duration TOO_LONG_ASYNC = Duration.of(100, MILLIS);

	@Test
	public void constructorGarbage(){
		var aReqQueue = new PriorityBlockingQueue<RequestMessage>();
		var aResQueue = new PriorityBlockingQueue<ResponseMessage>();
		assertThrows(IllegalArgumentException.class, ()->provideFreshTestee(null, null));
		assertThrows(IllegalArgumentException.class, ()->provideFreshTestee(aReqQueue, null));
		assertThrows(IllegalArgumentException.class, ()->provideFreshTestee(null, aResQueue));
	}

	@Test(timeout = 5*1000)
	public void doubleStart(){
		var aReqQueue = new PriorityBlockingQueue<RequestMessage>();
		var aResQueue = new PriorityBlockingQueue<ResponseMessage>();
		BasicVC testee;

		testee = provideFreshTestee(aReqQueue, aResQueue);

		assertTimeout(TOO_LONG_ASYNC, testee::start);
		assertTimeout(TOO_LONG_ASYNC, testee::interrupt);
		try {
			testee.join();
		} catch (InterruptedException e) {/*noop*/}
		assertThrows(IllegalThreadStateException.class, testee::start);
	}

	@Test
	public void isHandleResponseCalled() throws InterruptedException {
		var testResponses = new ResponseMessage[]{
				new BasicResponse("a", BasicResponse.NORM_PRIORITY),
				new BasicResponse("bb", BasicResponse.NORM_PRIORITY),
				new BasicResponse("ccc", BasicResponse.NORM_PRIORITY)
		};

		var aReqQueue = new PriorityBlockingQueue<RequestMessage>();
		var aResQueue = new PriorityBlockingQueue<>(
				List.of(testResponses)
		);
		var expectedList = new ArrayList<String>();
		for(var msg : testResponses){
			expectedList.add(msg.toString() + msg.toString());
		}
		var gotList = new PriorityBlockingQueue<String>();
		BasicVC testee;

		testee = provideFreshTestee(aReqQueue, aResQueue, (res)->{
			gotList.add(res.toString() + res.toString());
		});
		testee.start();

		System.out.println(":)3");

		assertTimeout(TOO_LONG_OP.multipliedBy(testResponses.length), ()-> {
					int count = 0;
					while (count < testResponses.length) {
						var got = gotList.take();
						assertEquals(got, expectedList.get(count));
						++count;
					}
		});


		testee.interrupt();
		testee.join();
	}

	private BasicVC provideFreshTestee(
			BlockingQueue<RequestMessage> req, BlockingQueue<ResponseMessage> res,
			LambdaHandleResponse lmbd
	){
		return new BasicVC(req, res) {
			@Override
			protected void handleResponse(ResponseMessage response) throws ErrorWhileSending, InterruptedException {
				lmbd.accept(response);
			}
		};
	}

	private BasicVC provideFreshTestee(
			BlockingQueue<RequestMessage> req, BlockingQueue<ResponseMessage> res
	){
		return provideFreshTestee(req, res, r->{});
	}
}
