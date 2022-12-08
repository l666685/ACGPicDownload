package xyz.zcraft.acgpicdownload.gui;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import xyz.zcraft.acgpicdownload.gui.controllers.*;
import xyz.zcraft.acgpicdownload.util.ResourceBundleUtil;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class GUI extends Application {
    public FetchPaneController fetchPaneController;
    public WelcomePaneController welcomePaneController;
    public MainPaneController mainPaneController;
    public PixivMenuPaneController pixivMenuPaneController;
    public SettingsPaneController settingsPaneController;
    public PixivDownloadPaneController pixivDownloadPaneController;
    public PixivDiscoveryPaneController pixivDiscoveryPaneController;
    public PixivUserPaneController pixivUserPaneController;
    public PixivRelatedPaneController pixivRelatedPaneController;
    public PixivRankingPaneController pixivRankingPaneController;

    public Stage mainStage;
    public Pane mainPane;
    public Pane stagePane;
    public Pane welcomePane;

    public GUI gui;

    public static void start(String[] args) {
        launch(args);
    }

    public static String conductException(Exception e) {
        if (e instanceof org.jsoup.HttpStatusException ex) {
            switch (ex.getStatusCode()) {
                case 400, 401 -> {
                    return ResourceBundleUtil.getString("err.status.401");
                }
                case 403 -> {
                    return ResourceBundleUtil.getString("err.status.403");
                }
                case 404 -> {
                    return ResourceBundleUtil.getString("err.status.404");
                }
                case 427 -> {
                    return ResourceBundleUtil.getString("err.status.427");
                }
            }
        } else if (e instanceof java.net.SocketTimeoutException) {
            return ResourceBundleUtil.getString("err.status.timeout");
        } else if ((e instanceof java.net.ConnectException ex && ex.getMessage().contains("Connection refused"))
                || (e instanceof java.net.SocketException ex1
                && ex1.getMessage().contains("Network is unreachable: no further information"))
                || (e instanceof java.net.UnknownHostException)) {
            String h = ConfigManager.getConfig().getString("proxyHost");
            Integer p = ConfigManager.getConfig().getInteger("proxyPort");
            String s = "";
            if (h != null && p != null) {
                s = h + ":" + p;
            }
            return String.format(Objects.requireNonNull(ResourceBundleUtil.getString("err.status.invalidProxy")), s);
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            return ResourceBundleUtil.getString("err.status.ssl");
        }

        return null;
    }

    public void openPixivMenuPane() {
        pixivMenuPaneController.show();
    }

    public void fill(Node node) {
        AnchorPane.setTopAnchor(node, 0d);
        AnchorPane.setBottomAnchor(node, 0d);
        AnchorPane.setLeftAnchor(node, 0d);
        AnchorPane.setRightAnchor(node, 0d);
    }

    public void fill(Node... node) {
        for (Node node1 : node) {
            fill(node1);
        }
    }

    public void openFetchPane() {
        fetchPaneController.show();
    }

    public void openPixivDiscPane() {
        pixivDiscoveryPaneController.show();
    }

    public void openPixivDownloadPane() {
        pixivDownloadPaneController.show();
    }

    @Override
    public void start(Stage stage) throws Exception {
        ConfigManager.readConfig();
        if (ConfigManager.getConfig().containsKey("lang"))
            ResourceBundleUtil.load(ConfigManager.getConfig().getString("lang"));
        gui = this;
        mainStage = stage;

        stage.setTitle("ACGPicDownload");

        FXMLLoader mainLoader = new FXMLLoader(ResourceLoader.loadURL("fxml/MainPane.fxml"),
                ResourceBundleUtil.getResource());
        stagePane = mainLoader.load();
        mainPaneController = mainLoader.getController();
        mainPane = mainPaneController.getMainPane();
        mainPaneController.setGui(gui);

        readBackground();

        stage.setOnCloseRequest(windowEvent -> {
            stage.hide();
            System.exit(0);
        });

        stage.show();

        Thread initThread = new Thread(() -> {
            try {
                FXMLLoader loader;
                loader = new FXMLLoader(ResourceLoader.loadURL("fxml/WelcomePane.fxml"), ResourceBundleUtil.getResource());
                welcomePane = loader.load();
                welcomePaneController = loader.getController();
                welcomePaneController.setGui(gui);
                fill(welcomePane);
                Platform.runLater(() -> mainPane.getChildren().add(welcomePane));
                mainPaneController.setProgress(0.1);

                loader = new FXMLLoader(ResourceLoader.loadURL("fxml/FetchPane.fxml"), ResourceBundleUtil.getResource());
                loader.load();
                fetchPaneController = loader.getController();
                fetchPaneController.setGui(gui);
                fill(fetchPaneController.getMainPane());
                Platform.runLater(() -> mainPane.getChildren().add(fetchPaneController.getMainPane()));
                mainPaneController.setProgress(0.2);

                loader = new FXMLLoader(ResourceLoader.loadURL("fxml/PixivMenuPane.fxml"), ResourceBundleUtil.getResource());
                loader.load();
                pixivMenuPaneController = loader.getController();
                pixivMenuPaneController.setGui(gui);
                fill(pixivMenuPaneController.getMainPane());
                Platform.runLater(() -> mainPane.getChildren().add(pixivMenuPaneController.getMainPane()));
                mainPaneController.setProgress(0.3);

                loader = new FXMLLoader(ResourceLoader.loadURL("fxml/PixivUserPane.fxml"), ResourceBundleUtil.getResource());
                loader.load();
                pixivUserPaneController = loader.getController();
                pixivUserPaneController.setGui(gui);
                fill(pixivUserPaneController.getMainPane());
                Platform.runLater(() -> mainPane.getChildren().add(pixivUserPaneController.getMainPane()));
                mainPaneController.setProgress(0.4);

                loader = new FXMLLoader(ResourceLoader.loadURL("fxml/PixivRelatedPane.fxml"), ResourceBundleUtil.getResource());
                loader.load();
                pixivRelatedPaneController = loader.getController();
                pixivRelatedPaneController.setGui(gui);
                fill(pixivUserPaneController.getMainPane());
                Platform.runLater(() -> mainPane.getChildren().add(pixivRelatedPaneController.getMainPane()));
                mainPaneController.setProgress(0.5);

                loader = new FXMLLoader(ResourceLoader.loadURL("fxml/PixivDownloadPane.fxml"), ResourceBundleUtil.getResource());
                loader.load();
                pixivDownloadPaneController = loader.getController();
                pixivDownloadPaneController.setGui(gui);
                fill(pixivDownloadPaneController.getMainPane());
                Platform.runLater(() -> mainPane.getChildren().add(pixivDownloadPaneController.getMainPane()));
                mainPaneController.setProgress(0.6);

                loader = new FXMLLoader(ResourceLoader.loadURL("fxml/PixivDiscoveryPane.fxml"), ResourceBundleUtil.getResource());
                loader.load();
                pixivDiscoveryPaneController = loader.getController();
                pixivDiscoveryPaneController.setGui(gui);
                fill(pixivDiscoveryPaneController.getMainPane());
                Platform.runLater(() -> mainPane.getChildren().add(pixivDiscoveryPaneController.getMainPane()));
                mainPaneController.setProgress(0.7);

                loader = new FXMLLoader(ResourceLoader.loadURL("fxml/SettingsPane.fxml"), ResourceBundleUtil.getResource());
                loader.load();
                settingsPaneController = loader.getController();
                settingsPaneController.setGui(gui);
                fill(settingsPaneController.getMainPane());
                Platform.runLater(() -> mainPane.getChildren().add(settingsPaneController.getMainPane()));
                mainPaneController.setProgress(0.8);

                loader = new FXMLLoader(ResourceLoader.loadURL("fxml/PixivRankingPane.fxml"), ResourceBundleUtil.getResource());
                loader.load();
                pixivRankingPaneController = loader.getController();
                pixivRankingPaneController.setGui(gui);
                fill(pixivRankingPaneController.getMainPane());
                Platform.runLater(() -> mainPane.getChildren().add(pixivRankingPaneController.getMainPane()));
                mainPaneController.setProgress(0.9);

                // Load done
                welcomePane.setVisible(false);
                mainPaneController.setProgress(1);

                Thread.sleep(1000);

                Platform.runLater(() -> {
                    mainPaneController.initDone();
                    welcomePane.setVisible(true);
                    welcomePaneController.showMain();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    MFXGenericDialog content = MFXGenericDialogBuilder.build()
                            .setContentText(ResourceBundleUtil.getString("gui.seriousERR") + "\n" + sw)
                            .setShowClose(true)
                            .setHeaderText(ResourceBundleUtil.getString("cli.fetch.err")).get();
                    content.addActions(Map.entry(new MFXButton("OK"), event -> System.exit(1)));
                    MFXGenericDialogBuilder.build(content)
                            .toStageDialogBuilder()
                            .initOwner(stage)
                            .initModality(Modality.APPLICATION_MODAL)
                            .setDraggable(true)
                            .setTitle("Dialogs Preview")
                            .setScrimPriority(ScrimPriority.WINDOW)
                            .setScrimOwner(true)
                            .get()
                            .showDialog();

                    pw.close();
                    try {
                        sw.close();
                    } catch (IOException ignored) {
                    }
                });
            }
        });

        initThread.setPriority(1);
        initThread.start();
    }

    private void readBackground() throws IOException {
        Scene s = new Scene(stagePane);
        String bg = ConfigManager.getConfig().getString("bg");
        InputStream imgMain;
        mainStage.setScene(s);
        if (bg != null && bg.equals("transparent")) {
            s.setFill(null);
            mainStage.initStyle(StageStyle.TRANSPARENT);
            mainPane.setStyle("-fx-background: rgba(255,255,255,0.5);");
            mainPaneController.getTitlePane().setStyle("-fx-background: rgba(255,255,255,0.5);");
            stagePane.setStyle("-fx-background: rgba(255,255,255,0.5);");
            mainStage.setWidth(800);
            mainStage.setHeight(500);
            mainStage.setResizable(true);
            mainPaneController.setTransparent();
        } else {
            mainStage.initStyle(StageStyle.UNDECORATED);
            if (bg != null && !bg.isEmpty()) {
                File bgFolder = new File(bg);
                if (!bgFolder.exists())
                    bgFolder.mkdirs();
                List<File> fl = new ArrayList<>(Stream.of(Objects.requireNonNull(bgFolder.listFiles()))
                        .filter((f) -> f.getName().endsWith(".png") || f.getName().endsWith(".jpg"))
                        .toList());
                if (fl.size() > 0) {
                    File file = fl.get(new Random().nextInt(fl.size()));
                    imgMain = new FileInputStream(file);
                } else {
                    imgMain = ResourceLoader.loadStream("bg.png");
                }
            } else {
                imgMain = ResourceLoader.loadStream("bg.png");
            }

            mainStage.setWidth(800);
            mainStage.setHeight(500);
            mainPaneController.setBackground(imgMain);
            mainStage.setResizable(false);
        }
    }

    public void openSettingsPane() {
        settingsPaneController.show();
    }

    public void openPixivUserPane() {
        pixivUserPaneController.show();
    }

    public void openPixivRelatedPane() {
        pixivRelatedPaneController.show();
    }

    public void showError(Exception e) {
        ErrorPaneController epc = ErrorPaneController.getInstance(mainPane);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        Platform.runLater(() -> {
            StringBuilder sb = new StringBuilder();
            String msg = conductException(e);
            if (msg != null) {
                sb.append(ResourceBundleUtil.getString("err.conduct"));
                sb.append(msg);
                sb.append("\n");
            }
            sb.append(sw);
            gui.fill(epc.getErrorPane());
            gui.mainPane.getChildren().addAll(epc.getErrorPane());
            epc.setErrorMessage(sb.toString());
            epc.setBlur(mainPaneController.isTransparent() ? null : mainPane.snapshot(new SnapshotParameters(), null));
            epc.show();
            pw.close();
            try {
                sw.close();
            } catch (IOException ignored) {
            }
        });
    }

    public void openPixivRankingPane() {
        pixivRankingPaneController.show();
    }
}
