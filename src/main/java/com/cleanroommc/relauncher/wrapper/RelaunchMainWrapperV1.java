package com.cleanroommc.relauncher.wrapper;

public class RelaunchMainWrapperV1 {

    public static void main(String[] args) throws ReflectiveOperationException {
        String mainClassName = null;
        for (int i = args.length - 1; i > 0; i--) {
            if ("--mainClass".equals(args[i])) {
                mainClassName = args[i + 1];
                break;
            }
        }
        Thread thread = new Thread("Relauncher Parent Watcher") {
            @Override
            public void run() {
                try {
                    while (System.in.read() != -1) {
                        Thread.sleep(1000);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                System.exit(0);
            }
        };
        thread.setDaemon(true);
        thread.start();
        Class.forName(mainClassName).getMethod("main", String[].class).invoke(null, (Object) args);
    }

}
