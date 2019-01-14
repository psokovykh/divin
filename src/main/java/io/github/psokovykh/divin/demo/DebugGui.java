package io.github.psokovykh.divin.demo;

import io.github.psokovykh.divin.model.BasicModelLambdas;
import io.github.psokovykh.divin.model.Model;
import io.github.psokovykh.divin.vc.ViewController;
import io.github.psokovykh.divin.vc.BasicVC;
import io.github.psokovykh.divin.vc.BasicRequest;
import io.github.psokovykh.divin.vc.BlockingReadVCLambdas;
import io.github.psokovykh.divin.core.DivinBasedApp;
import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.debug.DebugVcGui;
import io.github.psokovykh.divin.model.BasicResponse;
import io.github.psokovykh.divin.util.BlockingCooldown;
import io.github.psokovykh.divin.util.FlexibleJFXThread;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class DebugGui extends DivinBasedApp {
	private DebugVcGui dag;
	private BasicVC amForDebug;
	private static FlexibleJFXThread jfxThread;

	public static void main(String[] args){
		//IF we want to use GUI features, we must start JFX Application first
		jfxThread = new FlexibleJFXThread();
		jfxThread.start();
		new DebugGui().run();
	}

	@Override
	protected @NotNull List<ViewController> getViewControllers(BlockingQueue<RequestMessage> outQ) {
		List<ViewController> ret = new LinkedList<>();

		Random rnd = new Random();
		BlockingCooldown cdRead = new BlockingCooldown(1000);
		BlockingCooldown cdWrite = new BlockingCooldown(3000);

		this.amForDebug = new BlockingReadVCLambdas(
			outQ,
			()->
				cdRead.waitAndProduce(()-> new BasicRequest(
					"tst", rnd.nextInt(100) + "*" + rnd.nextInt(100),
						33+rnd.nextInt(50)
				)),
			(res)->
				cdWrite.tryDoWait(()->System.out.println(res))
		);

		ret.add(amForDebug);
		return ret;
	}

	@Override
	protected boolean doMainLoop(){
		this.dag = new DebugVcGui(jfxThread, amForDebug);
		this.dag.start();
		try {
			this.dag.join();
		} catch (InterruptedException e) {
			//It's perfectly normal
		}
		return false;
	}

	@Override
	protected @NotNull List<Model> getModels() {
		List<Model> ret = new LinkedList<>();

		Random rnd = new Random();
		BlockingCooldown cd = new BlockingCooldown(2000);

		var pm = new BasicModelLambdas(
			1,
			(req) ->{
					String[] ops = req.getText().split("\\*");
					int result = Integer.parseInt(ops[0])*Integer.parseInt(ops[1]);
					return new BasicResponse(req.getText()+"="+result);
			},
			new String[]{"tst"}
		){
			@Override
			public void addJob(RequestMessage req) {
				try {
					cd.waitUntilRefreshes();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				super.addJob(req);
			}
		};

		ret.add(pm);
		return ret;
	}


}
