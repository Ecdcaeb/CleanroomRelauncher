package com.cleanroommc.relauncher.wrapper;

import java.util.Optional;

public class RelaunchMainWrapperV2 {

    public static void main(String[] args) throws ReflectiveOperationException {
        String mainClassName = null;
        for (int i = args.length - 1; i > 0; i--) {
            if ("--mainClass".equals(args[i])) {
                mainClassName = args[i + 1];
                break;
            }
        }
        Optional<ProcessHandle> optional = ProcessHandle.current().parent();
        if (optional.isEmpty()) {
            throw new RuntimeException("Unable to grab parent process!");
        }
        Thread thread = new Thread("Relauncher Parent Watcher") {

            private final ProcessHandle handle = optional.get();

            @Override
            public void run() {
                while (this.handle.isAlive()) { }
                System.exit(0);
            }
        };
        thread.setDaemon(true);
        thread.start();
        Class.forName(mainClassName).getMethod("main", String[].class).invoke(null, (Object) args);
    }

}
