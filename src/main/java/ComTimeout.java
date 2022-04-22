public class ComTimeout extends Thread {

    private static volatile boolean running = true;
    private static volatile boolean paused = false;
    private static volatile Object pauseLock;
    private static volatile long timestamp;
    private static volatile int maxTimeout;

    private Server sv;

    ComTimeout(Server sv, int maxTimeout){
        this.sv = sv;
        this.pauseLock = new Object();
        this.maxTimeout = maxTimeout;
    }

    @Override
    public void run() {

        this.timestamp = System.currentTimeMillis();

        synchronized (pauseLock) {

            while (running) {

                if (!running) {
                    break;
                }

                if (paused) {

                    try {
                        synchronized (pauseLock) {
                            pauseLock.wait();
                        }
                    } catch (InterruptedException ex) {
                        break;
                    }

                    if (!running) {
                        break;
                    }
                }

                if (System.currentTimeMillis() - timestamp > maxTimeout) {
                    sv.IniciaEleicao();

                    try {
                        synchronized (pauseLock) {
                            pauseLock.wait();
                        }
                    } catch (InterruptedException ex) {
                        break;
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    // reseta o temporizador
    public void Reset(){
        this.timestamp = System.currentTimeMillis();
    }

    public void Stop() {
        running = false;
        Resume();
    }

    public void Pause() {
        paused = true;
    }

    public void Resume() {
        synchronized (pauseLock) {
            paused = false;
            Reset();
            pauseLock.notifyAll();
        }
    }
};
