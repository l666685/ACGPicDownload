package xyz.zcraft.ACGPicDownload.Commands;

import xyz.zcraft.ACGPicDownload.Main;
import xyz.zcraft.ACGPicDownload.Util.FetchUtil.FetchUtil;
import xyz.zcraft.ACGPicDownload.Util.FetchUtil.Result;
import xyz.zcraft.ACGPicDownload.Util.Logger;
import xyz.zcraft.ACGPicDownload.Util.SourceUtil.Source;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Fetch {

    private final HashMap<String, String> arguments = new HashMap<>();
    public boolean enableConsoleProgressBar = false;
    private String sourceName;
    private String outputDir = new File("").getAbsolutePath();
    private boolean multiThread = false;
    private Logger logger;
    private int times = 1;
    private boolean saveFullResult = false;


    public void main(ArrayList<String> args, Logger logger) {
        this.logger = logger;
        for (int i = 0; i < args.size(); i++) {
            switch (args.get(i)) {
                case "-s", "--source" -> {
                    if (args.size() > i + 1 && !args.get(i + 1).startsWith("-")) {
                        sourceName = args.get(i + 1);
                        i += 1;
                    } else {
                        logger.err("Please provide a source name.");
                    }
                }
                case "-o", "--output" -> {
                    if (args.size() > i + 1 && !args.get(i + 1).startsWith("-")) {
                        outputDir = args.get(i + 1);
                        i += 1;
                    } else {
                        logger.err("Please provide a output path.");
                    }
                }
                case "--arg", "-a", "--args" -> {
                    if (args.size() > i + 1 && !args.get(i + 1).startsWith("-")) {
                        String[] t = args.get(i + 1).split(",");
                        for (String s : t) {
                            String key = s.substring(0, s.indexOf("="));
                            String value = s.substring(s.indexOf("=") + 1);
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            }
                            arguments.put(key, value);
                        }
                        i += 1;
                    } else {
                        logger.err("Please provide arguments.");
                    }
                }
                case "--multi-thread" -> multiThread = true;
                case "-f", "--full" -> saveFullResult = true;
                case "--debug" -> Main.debugOn();
                case "--list-sources" -> {
                    try {
                        FetchUtil.listSources(logger);
                    } catch (IOException e) {
                        //TODO Auto-generated catch block
                        throw new RuntimeException(e);
                    }
                    return;
                }
                case "-h", "--help" -> {
                    usage();
                    return;
                }
                case "-t", "--times" -> {
                    if (args.size() > (i + 1)) {
                        try {
                            times = Integer.parseInt(args.get(i + 1));
                            i++;
                            break;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    logger.err("Please enter a number");
                }
                default -> {
                    logger.err("Unknown argument " + args.get(i) + " . Please use -h to see usage.");
                    return;
                }
            }
        }

        if (sourceName == null || sourceName.trim().equals("")) {
            List<Source> sources;
            try {
                sources = FetchUtil.getSourcesConfig();
            } catch (IOException e) {
                //TODO Auto-generated catch block
                throw new RuntimeException(e);
            }
            if (sources == null || sources.size() == 0) {
                logger.err("No available sources");
                return;
            } else {
                sourceName = sources.get(0).getName();
            }
        }
        if (outputDir == null || outputDir.trim().equals("")) {
            outputDir = "";
        }
        Source s;
        try {
            s = FetchUtil.getSourceByName(sourceName);
        } catch (Exception e) {
            logger.err("Could not find source " + sourceName);
            return;
        }
        if (s == null) {
            logger.err("Could not find source named " + sourceName
                    + ". Please check your sources.json file. To get all sources, use \"--list-sources\"");
            return;
        }
        FetchUtil.replaceArgument(s, arguments);
        ArrayList<Result> r;
        r = FetchUtil.fetch(s,times,logger,enableConsoleProgressBar);
        if (r.size() == 0) {
            logger.info("No pictures were found!");
            return;
        }

        logger.info("Got " + r.size() + " pictures!");

        FetchUtil.startDownload(r, outputDir, logger, multiThread, saveFullResult, enableConsoleProgressBar);
    }

    private void usage() {
        logger.info(
                """
                                Available arguments:
                                   --list-sources : List all the sources
                                   -s, --source <source name> : Set the source to use. Required.
                                   -o, --output <output dictionary> : Set the output dictionary. Required.
                                   --arg key1=value1,key2=value2,... : custom the argument in the url.
                                           Example:If the url is "https://www.someurl.com/pic?num=${num}", then with
                                                    "--arg num=1", the exact url will be "https://www.someurl.com/pic?num=1"
                                   --multi-thread : (Experimental) Enable multi thread download. May improve download speed.
                        """);
    }
}
