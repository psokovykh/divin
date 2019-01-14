package io.github.psokovykh.divin.demo;

import io.github.psokovykh.divin.model.BasicModelLambdas;
import io.github.psokovykh.divin.model.Model;
import io.github.psokovykh.divin.vc.ViewController;
import io.github.psokovykh.divin.vc.BasicRequest;
import io.github.psokovykh.divin.vc.BlockingReadVCLambdas;
import io.github.psokovykh.divin.vc.ErrorWhileReading;
import io.github.psokovykh.divin.restvc.DispatcherLambda;
import io.github.psokovykh.divin.restvc.RestVC;
import io.github.psokovykh.divin.restvc.UriRequest;
import io.github.psokovykh.divin.core.DivinBasedApp;
import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.core.ResponseMessage;
import io.github.psokovykh.divin.model.BasicResponse;
import io.github.psokovykh.divin.util.BlockingCooldown;
import io.github.psokovykh.divin.util.FlexibleJFXThread;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class RestAndVk extends DivinBasedApp{

	private final Object eventClose = new Object();
	private static FlexibleJFXThread jfxThread;
	private static String token;

	public static void main(String[] args){
		//IF we want to use GUI features, we must start JFX Application first
		jfxThread = new FlexibleJFXThread();
		jfxThread.start();

		token = VkDummy.getTokenUrl(jfxThread, "5639519");

		new RestAndVk().run();
	}

	@Override
	protected @NotNull List<ViewController> getViewControllers(BlockingQueue<RequestMessage> outQ) {
		List<ViewController> ret = new LinkedList<>();

		//TODO move that queue to deafult constructor param
		var qq = new PriorityBlockingQueue<ResponseMessage>();
		ret.add(new RestVC(outQ, qq, 1899, 8,
				new DispatcherLambda("/add/*/*", r->
						new UriRequest("add", r.splat()[0]+"+"+r.splat()[1])
				),
				new DispatcherLambda("/echo/*", r->
						new UriRequest("echo", r.splat()[0])
				),
				new DispatcherLambda("/exit", r->
						new UriRequest("exit", "")
				)
		));

		BlockingCooldown cdVK = new BlockingCooldown(10000);
		final String[] lastresponse = new String[1]; lastresponse[0]="";
		ret.add(new BlockingReadVCLambdas(outQ,
				()->{
					while(true) {
						cdVK.waitUntilRefreshes();
						String response = "ыы"; //!=""
						try {
							response = VkDummy.invokeMethod("messages.getImportantMessages",
									"count=1", token);
						} catch (IOException e) {
							throw new ErrorWhileReading("Something went wrong", e);
						}
						try {
							if (!response.equals(lastresponse[0])) {
								lastresponse[0] = response;

								var text = response.split("\"text\":\"")[1].split("\"")[0]
										.split(" ",2);
								return new BasicRequest(text[0], text[1]);
							}
						}catch (Exception e){}
					}
				},
				(res)->{
					System.out.println(res.getText());
				}
		));

		return ret;
	}

	@SuppressWarnings("Duplicates")
	@Override
	protected @NotNull List<Model> getModels() {
		List<Model> ret = new LinkedList<>();

		Model pm;

		pm = new BasicModelLambdas(
				1,
				(req) -> {
					String[] ops = req.getText().split("\\+");
					int result = Integer.parseInt(ops[0])+Integer.parseInt(ops[1]);
					return new BasicResponse(req.getText()+"="+result);
				},
				new String[]{"add"}
		);
		ret.add(pm);
		pm = new BasicModelLambdas(
				1,
				(req) ->{
					StringBuilder stringBuilder = new StringBuilder();
					req.getText().codePoints().forEach((c)->{
						stringBuilder.append(Character.toChars(c));
						stringBuilder.append(Character.toChars(c));
					});
					return new BasicResponse(stringBuilder.toString());
				},
				new String[]{"echo"}
		);
		ret.add(pm);
		pm = new BasicModelLambdas(
				1,
				(req) ->{
					synchronized (this.eventClose) {
						this.eventClose.notify();
					}
					//User won't see the message
					return new BasicResponse("Yeah, gonna stop");
				},
				new String[]{"exit"}
		);
		ret.add(pm);
		return ret;
	}

	@Override
	protected boolean doMainLoop() {
		synchronized (eventClose){
			try {
				eventClose.wait();
			} catch (InterruptedException e) {/*noop*/}
		}
		return false;
	}
}

