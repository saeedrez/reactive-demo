package reactive.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MultiThreadApplication {

		private static final ThreadLocal<Integer> threadLocalValue = ThreadLocal.withInitial(() -> 0);

		public static void main(String[] args) {
			Runnable task = () -> {
				int value = threadLocalValue.get();
				System.out.println(Thread.currentThread().getName() + " initial value: " + value);
				for (int i = 0; i < 5; i++) {
					if (Thread.currentThread().getName().equalsIgnoreCase("Thread-1")) {
						threadLocalValue.set(value + 2);
					} else
						threadLocalValue.set(value + 1);
					System.out.println(Thread.currentThread().getName() + " updated value: " + threadLocalValue.get());
					value = threadLocalValue.get();
				};
			};

			Thread thread1 = new Thread(task, "Thread-1");
			Thread thread2 = new Thread(task, "Thread-2");

			thread1.start();
			thread2.start();
		}
	}
