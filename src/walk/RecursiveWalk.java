package ru.ifmo.ctddev.titova.walk;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
//TODO: out of memory

public class RecursiveWalk {

    private static final long FNV_PRIME = 16777619;
    private static final long FNV_START = 2166136261L;
    private static final long MOD = 4294967296L;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Wrong amount of args: " + args.length);
        } else {
            new RecursiveWalk().myWalk(args, StandardCharsets.UTF_8);
        }
    }


    private void myWalk(String[] args, Charset charset) {
        try {
            Path p = Paths.get(args[0]);
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(args[0]), charset));
                if (!Files.isReadable(Paths.get(args[0]))) {
                    //System.out.println("Input file is not available");
                } else {


                    if (Files.notExists(Paths.get(args[1]))) {

                        if (Paths.get(args[1]).getParent() != null) {
                            Files.createDirectories(Paths.get(args[1]).getParent());
                        }
                        Files.createFile(Paths.get(args[1]));
                    }
                    PrintWriter writer = new PrintWriter(
                            new OutputStreamWriter(new FileOutputStream(args[1]), charset));
                    if (!Files.isReadable(Paths.get(args[1]))) {
                        // System.out.println("Output file is not available");
                    } else {
                        //try {

                        String pathStr;
                        while ((pathStr = reader.readLine()) != null) {
                            try {
                                Path path = Paths.get(pathStr);
                                if (Files.isReadable(path)) {
                                    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                                        @Override
                                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                                            print(getHash(file), file);
                                            return FileVisitResult.CONTINUE;
                                        }

                                        @Override
                                        public FileVisitResult visitFileFailed(Path file, IOException exc) {
                                            // print(0, file);
                                            writer.printf("%08x %s\n", 0, path.toString());
                                            //System.out.println("Error during visiting file: " + file.toString());
                                            return FileVisitResult.CONTINUE;

                                        }

                                        private void print(long hash, Path path) {
                                            writer.printf("%08x %s\n", hash, path.toString());
                                            if (!Files.isReadable(path)) {
                                                //System.out.println("File: " + path.toString() + " is not readable ");
                                            } else if (writer.checkError()) {
                                                //System.out.println("Error during writing " + hash + " in file: " + path);
                                            }

                                        }
                                    });
                                } else {
                                    writer.printf("%08x %s\n",0, path.toString());
                                    //System.out.println("File is not readable");
                                }
                            } catch (InvalidPathException ppp) {
                                writer.printf("%08x %s\n",0, pathStr);
                                //System.out.println(ppp.getMessage());
                            }
                        }
                        writer.close();
                    }
                }
//}
            } catch (FileNotFoundException fee) {
                if (!Files.isRegularFile(Paths.get(args[0]))) {
                    System.out.println("Input file wasn't found");
                } else {
                    System.out.println("Incorrect output file, it is a directory");
                }
            } catch (IOException e) {
                System.out.printf(e.getMessage(), StandardCharsets.UTF_8);
            }
        } catch (InvalidPathException pp) {
            System.out.println(pp.getMessage());
        }
    }

    private static long getHash(Path file) {
        long hash = FNV_START;
        try (InputStream inputStream = new FileInputStream(file.toString())) {
            int sz;
            byte[] buffer = new byte[1024];
            while ((sz = inputStream.read(buffer, 0, 1024)) != -1) {
                for (int i = 0; i < sz; i++) {
                    hash = hash * (FNV_PRIME) % (MOD) ^ (buffer[i] & ((1 << 8) - 1));
                }
            }
        } catch (IOException e) {
            hash = 0;
            //System.out.println(e.getMessage() + " in file " + file.toString());
        }
        return hash;
    }

}