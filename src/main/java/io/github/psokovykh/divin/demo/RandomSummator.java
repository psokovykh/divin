package io.github.psokovykh.divin.demo;

import io.github.psokovykh.divin.model.LambdaDoJob;
import io.github.psokovykh.divin.vc.ViewController;
import io.github.psokovykh.divin.vc.BasicRequest;
import io.github.psokovykh.divin.vc.BlockingReadVCLambdas;
import io.github.psokovykh.divin.core.DivinBasedApp;
import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.model.Model;
import io.github.psokovykh.divin.model.BasicModelLambdas;
import io.github.psokovykh.divin.model.BasicResponse;
import io.github.psokovykh.divin.util.BlockingCooldown;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class RandomSummator extends DivinBasedApp {


	public static void main(String[] args){
		new RandomSummator().run();
	}

	@Override
	protected @NotNull List<ViewController> getViewControllers(BlockingQueue<RequestMessage> outQ) {
		Random rnd = new Random();
		BlockingCooldown readCd = new BlockingCooldown(3000);

		return List.of(
			new BlockingReadVCLambdas(
				outQ,
				()->readCd.waitAndProduce(()->
						new BasicRequest(
								"TESTMODULE1",
								rnd.nextInt(100)+"+"+rnd.nextInt(100)
						)
				),
				System.out::println
			)
		);
	}

	@SuppressWarnings("Duplicates")
	@Override
	protected @NotNull List<Model> getModels() {
		BlockingCooldown doJobCd = new BlockingCooldown(6000);

		LambdaDoJob add = (req) -> {
			String[] ops = req.getText().split("\\+");
			int result = Integer.parseInt(ops[0])+Integer.parseInt(ops[1]);
			return doJobCd.waitAndProduce(()->new BasicResponse(req.getText()+"="+result));
		};

		var pm = new BasicModelLambdas(2, add, new String[]{"TESTMODULE1"});

		return List.of(pm);
	}

	@Override
	protected boolean doMainLoop() {
		//just terminates on interruption
		var cd = new BlockingCooldown(30*1000);
		cd.tryDo(()->{}); cd.tryDo(()->{});
		return false;
	}
}
