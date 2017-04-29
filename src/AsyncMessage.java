

public class AsyncMessage extends Thread {
    MafiaSThread thread = null;
    String message = null;
    public AsyncMessage(MafiaSThread thread, String message) {
        super("AsyncMessage");
        this.thread = thread;
        this.message = message;
    }

    public void run() {
        thread.socketOutput(message);
    }
}