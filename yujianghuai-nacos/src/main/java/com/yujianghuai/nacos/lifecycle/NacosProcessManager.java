package com.yujianghuai.nacos.lifecycle;

import com.yujianghuai.nacos.config.NacosConfigFileWriter;
import com.yujianghuai.nacos.config.NacosManagerProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class NacosProcessManager implements ApplicationRunner, SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(NacosProcessManager.class);

    private final NacosManagerProperties properties;
    private final NacosConfigFileWriter configFileWriter;
    private volatile Process process;
    private volatile boolean running;
    private volatile String lastError;

    public NacosProcessManager(NacosManagerProperties properties, NacosConfigFileWriter configFileWriter) {
        this.properties = properties;
        this.configFileWriter = configFileWriter;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.isAutoStart()) {
            try {
                startNacos();
            } catch (RuntimeException exception) {
                lastError = exception.getMessage();
                running = false;
                log.warn("Nacos server auto startup skipped: {}", exception.getMessage());
            }
        }
    }

    public synchronized void startNacos() {
        if (isNacosProcessAlive()) {
            return;
        }

        configFileWriter.prepareApplicationProperties();
        Path startupScript = resolveStartupScript();
        List<String> command = buildCommand(startupScript);
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(startupScript.getParent().toFile());
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        try {
            process = processBuilder.start();
            running = true;
            lastError = null;
            log.info("Nacos server startup command executed: {}", String.join(" ", command));
        } catch (IOException exception) {
            lastError = exception.getMessage();
            throw new IllegalStateException("failed to start nacos: " + exception.getMessage(), exception);
        }
    }

    public synchronized void stopNacos() {
        if (!isNacosProcessAlive()) {
            running = false;
            return;
        }
        process.destroy();
        running = false;
        log.info("Nacos server process stopped");
    }

    public boolean isNacosProcessAlive() {
        return process != null && process.isAlive();
    }

    public String status() {
        if (isNacosProcessAlive()) {
            return "RUNNING";
        }
        return running ? "STARTED_BY_SCRIPT" : "STOPPED";
    }

    public String lastError() {
        return lastError;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        stopNacos();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private Path resolveStartupScript() {
        String home = properties.getHome();
        if (home == null || home.isBlank()) {
            throw new IllegalStateException("NACOS_HOME is missing, please set nacos.manager.home or NACOS_HOME");
        }

        Path homePath = Path.of(home);
        Path script = isWindows()
                ? homePath.resolve("bin").resolve("startup.cmd")
                : homePath.resolve("bin").resolve("startup.sh");
        if (!Files.exists(script)) {
            throw new IllegalStateException("nacos startup script not found: " + script);
        }
        return script;
    }

    private List<String> buildCommand(Path startupScript) {
        List<String> command = new ArrayList<>();
        if (isWindows()) {
            command.add("cmd");
            command.add("/c");
            command.add(startupScript.toAbsolutePath().toString());
        } else {
            command.add("sh");
            command.add(startupScript.toAbsolutePath().toString());
        }
        command.add("-m");
        command.add(properties.getMode());

        return command;
    }

    private boolean isWindows() {
        return File.separatorChar == '\\'
                || System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }
}
