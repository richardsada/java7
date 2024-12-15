import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class CombinedVehicleArrayFiller {
    private static final int ARRAY_SIZE = 20; // Размер массива
    private static final int THREAD_COUNT = 8; // Количество потоков

    public static void main(String[] args) throws InterruptedException {
        // Многопоточное заполнение с ручной синхронизацией
        System.out.println("=== Многопоточное заполнение (синхронизация вручную) ===");
        List<Vehicle> synchronizedVehicles = new ArrayList<>();
        long startTime = System.nanoTime();
        runSynchronizedFiller(synchronizedVehicles);
        long endTime = System.nanoTime();
        System.out.printf("Время выполнения многопоточного заполнения: %.2f ms\n",
                (endTime - startTime) / 1_000_000.0);

        // Конкурентное заполнение с потокобезопасной коллекцией
        System.out.println("\n=== Конкурентное заполнение (CopyOnWriteArrayList) ===");
        CopyOnWriteArrayList<Vehicle> concurrentVehicles = new CopyOnWriteArrayList<>();
        startTime = System.nanoTime();
        runConcurrentFiller(concurrentVehicles);
        endTime = System.nanoTime();
        System.out.printf("Время выполнения конкурентного заполнения: %.2f ms\n",
                (endTime - startTime) / 1_000_000.0);
    }

    // Метод для выполнения многопоточного заполнения с ручной синхронизацией
    private static void runSynchronizedFiller(List<Vehicle> vehicles) throws InterruptedException {
        final Object lock = new Object();
        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(new SynchronizedVehicleAdder(i, vehicles, lock));
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Массив заполнен. Общее количество: " + vehicles.size());
        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle);
        }
    }

    // Метод для выполнения конкурентного заполнения с CopyOnWriteArrayList
    private static void runConcurrentFiller(CopyOnWriteArrayList<Vehicle> vehicles) throws InterruptedException {
        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(new ConcurrentVehicleAdder(i, vehicles));
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Массив заполнен. Общее количество: " + vehicles.size());
        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle);
        }
    }

    // Класс для добавления объектов в список с ручной синхронизацией
    static class SynchronizedVehicleAdder implements Runnable {
        private final int threadId;
        private final List<Vehicle> vehicles;
        private final Object lock;
        private final Random random = new Random();

        public SynchronizedVehicleAdder(int threadId, List<Vehicle> vehicles, Object lock) {
            this.threadId = threadId;
            this.vehicles = vehicles;
            this.lock = lock;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    if (vehicles.size() >= ARRAY_SIZE) {
                        break;
                    }
                    Vehicle vehicle = createRandomVehicle();
                    vehicles.add(vehicle);
                    System.out.println("Поток " + threadId + " добавил: " + vehicle);
                }
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private Vehicle createRandomVehicle() {
            return createVehicle();
        }
    }

    // Класс для добавления объектов в CopyOnWriteArrayList
    static class ConcurrentVehicleAdder implements Runnable {
        private final int threadId;
        private final CopyOnWriteArrayList<Vehicle> vehicles;
        private final Random random = new Random();

        public ConcurrentVehicleAdder(int threadId, CopyOnWriteArrayList<Vehicle> vehicles) {
            this.threadId = threadId;
            this.vehicles = vehicles;
        }

        @Override
        public void run() {
            while (vehicles.size() < ARRAY_SIZE) {
                Vehicle vehicle = createRandomVehicle();
                vehicles.add(vehicle);
                System.out.println("Поток " + threadId + " добавил: " + vehicle);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private Vehicle createRandomVehicle() {
            return createVehicle();
        }
    }

    // Метод для создания случайного транспортного средства
    private static Vehicle createVehicle() {
        Random random = new Random();
        int type = random.nextInt(5) + 1;
        double width = 1.5 + random.nextDouble();
        double height = 2.0 + random.nextDouble();
        double length = 4.0 + random.nextDouble();
        double weight = 1000 + random.nextDouble() * 10000;
        double speed = 30 + random.nextDouble() * 50;

        switch (type) {
            case 1:
                return new Car(width, height, length, weight, speed);
            case 2:
                return new Truck(width, height, length, weight, speed);
            case 3:
                return new Bus(width, height, length, weight, speed);
            case 4:
                return new Trailer(width, height, length, weight, speed);
            case 5:
                return new CarWithTrailer(width, height, length, weight, speed);
            default:
                throw new IllegalArgumentException("Неизвестный тип транспорта.");
        }
    }
}
