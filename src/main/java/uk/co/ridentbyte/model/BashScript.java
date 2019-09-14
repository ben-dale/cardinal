package uk.co.ridentbyte.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BashScript {

    private List<CardinalRequest> requests;
    private Config config;
    private long throttle;

    public BashScript(List<CardinalRequest> requests, Config config, int throttle) {
        this.requests = requests;
        this.config = config;
        this.throttle = throttle;
    }

    @Override
    public String toString() {
        String header = "#!/usr/bin/env bash";
        String date = "#Auto-generated " + new Date().toString();
        String delay = "";
        if (throttle > 0) {
            delay = "sleep " + throttle / 1000.0 + "\n";
        }
        String content = requests.stream().map(r -> r.toCurl(config)).collect(Collectors.joining("\necho\n" + delay)) + "\necho";
        return header + "\n\n" + date + "\n" + content;
    }

    public void writeTo(File file) throws IOException {
        try (var fileWriter = new FileWriter(file)) {
            fileWriter.write(toString());
            Set<PosixFilePermission> posix = new HashSet<>();
            posix.add(PosixFilePermission.OWNER_WRITE);
            posix.add(PosixFilePermission.OWNER_READ);
            posix.add(PosixFilePermission.OWNER_EXECUTE);
            posix.add(PosixFilePermission.GROUP_READ);
            posix.add(PosixFilePermission.GROUP_EXECUTE);
            posix.add(PosixFilePermission.OTHERS_READ);
            posix.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(file.toPath(), posix);
        }
    }
}
