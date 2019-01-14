package io.github.psokovykh.divin.util;

import io.github.psokovykh.divin.core.Threadlike;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FlexibleJFXThread extends Thread implements Threadlike {
	private static boolean isRunned = false;

	private static BlockingQueue<Runnable> actionsToForeground;
	private static Consumer<Stage> showPrimaryStageLambda;

	private static int checkStageQueueCdSecs = 2;
	private static int pollStageTimeoutMs = 100;

	public static class JfxApp extends  Application{
		@Override
		public void start(Stage primaryStage) throws Exception {
			showPrimaryStageLambda.accept(primaryStage);
			Timeline timeSchedule = new Timeline(
					new KeyFrame(Duration.seconds(checkStageQueueCdSecs),(event)->{
						boolean tryAgain = true;
						while(tryAgain){
							tryAgain = false;
							try {
								Objects.requireNonNull(//TODO useless here
										actionsToForeground.poll(pollStageTimeoutMs, TimeUnit.MILLISECONDS)
								).run();
								tryAgain = true;
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (NullPointerException err){
								//TODO (it's ok)
							}
						}
					})
			);
			timeSchedule.setCycleCount(Timeline.INDEFINITE);
			timeSchedule.play();
		}
	}

	public FlexibleJFXThread(Consumer<Stage> showPrimaryStageLambda) {
		if(isRunned){
			throw new IllegalStateException("You can't start more than one JFX threads");
		}
		isRunned = true;
		actionsToForeground = new LinkedBlockingQueue<>();
		FlexibleJFXThread.showPrimaryStageLambda = showPrimaryStageLambda;
	}


	public FlexibleJFXThread() {
		this((stage)->{});
	}

	@Override
	public void run() {
		super.run();
		Application.launch(JfxApp.class);
	}

	@Override
	public void interrupt(){
		super.interrupt();
		//Again, wanna priority queue and max priority for that
		FlexibleJFXThread.actionsToForeground.offer(Platform::exit);
	}

	public void performJfxForeground(Runnable r){
		FlexibleJFXThread.actionsToForeground.offer(r);
	}
}
