/*
 * Copyright (c) 2019 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.sample.util;

import org.apache.sshd.common.util.io.IoUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.StaticPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Helper class
 *
 * @author Symphony Dev Team<br> Created on May 8, 2019
 */
public class CommunicatorUtils {

    /**
     * Start simple ssh server on given port
     *
     * @param port ssh port
     * @return resource holder
     */
    public static Closeable startSshServer(int port) {
        try {
            Path keyStorage = Files.createTempDirectory("sshd").resolve("hostkey.ser");
            SshServer sshd = SshServer.setUpDefaultServer();
            sshd.setPort(port);
            sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(keyStorage));
            sshd.setPasswordAuthenticator(new StaticPasswordAuthenticator(true));
            sshd.setCommandFactory(ProcessShellCommandFactory.INSTANCE);
            sshd.setShellFactory(InteractiveProcessShellFactory.INSTANCE);

            sshd.start();
            return sshd::stop;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Start simple telnet server on given port
     *
     * @param port telnet port
     * @return resource holder
     */
    public static Closeable startTelnetServer(int port) {
        try {
            final ServerSocket serverSocket = new ServerSocket(port);
            runAsync(() -> processCommand(serverSocket));
            return serverSocket;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void processCommand(ServerSocket serverSocket) {
        try (Socket socket = serverSocket.accept()) {
            PrintStream out = new PrintStream(socket.getOutputStream());
            out.println("Welcome!");
            try (Scanner scanner = new Scanner(socket.getInputStream())){
                while (scanner.hasNext()) {
                    final Process exec = Runtime.getRuntime().exec(scanner.nextLine().split(" "));
                    exec.waitFor();
                    IoUtils.copy(exec.getInputStream(), out);
                }
            } catch (Exception e) {
                e.printStackTrace(out);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
