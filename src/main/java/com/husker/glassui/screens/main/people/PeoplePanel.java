package com.husker.glassui.screens.main.people;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.*;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.managers.ProfileApiMethod;
import com.husker.launcher.ui.components.LabelButton;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.components.skin.SkinViewer;
import com.husker.launcher.ui.utils.ImageUtils;
import li.flor.nativejfilechooser.NativeJFileChooser;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class PeoplePanel extends TransparentPanel {

    private BlurList<Player> playerList;
    private BlurTextField search;
    private Screen screen;

    private Player selectedPlayer;
    private TransparentPanel infoPanel;
    private WebLabel name, id, email, creationTime;
    private SkinViewer skinViewer;
    private BlurButtonLineChooser status;
    private BlurButton resetPassword, resetEmail;

    private final String[] statusList = {"Гость", "Игрок", "Модератор", "Администратор"};

    private boolean eventEnabled = true;

    public PeoplePanel(Screen screen){
        try {
            this.screen = screen;
            setMargin(10, 10, 10, 10);
            setLayout(new BorderLayout());

            add(new TransparentPanel() {{
                setLayout(new BorderLayout(0, 7));
                add(search = new BlurTextField(screen) {{
                    addTextListener(text -> updateData());
                }}, BorderLayout.NORTH);
                add(playerList = new BlurList<>(screen));
                playerList.addSelectedListener(player -> setSelectedPlayer(player));
                playerList.setPreferredWidth(190);
                for (int i = 0; i < 10; i++)
                    playerList.addContentPanel(new PlayerPanel(screen));
            }}, BorderLayout.WEST);
            add(infoPanel = new TransparentPanel(){{
                setMargin(0, 10, 0, 0);
                setLayout(new VerticalFlowLayout(0, 0));
                add(name = GlassUI.createTitleLabel(""));
                //name.setPreferredHeight(30);

                add(status = new BlurButtonLineChooser(screen){{
                    addButton("Гость");
                    addButton("Игрок");
                    addButton("Модер");
                    addButton("Админ");
                    addSelectedListener(index -> {
                        if(!eventEnabled)
                            return;
                        try {
                            API.getJSON(ProfileApiMethod.create("profiles.setStatus", screen.getLauncher().User.getToken())
                                    .set("status", statusList[index])
                                    .set("id", selectedPlayer.id)
                            );
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    });
                }});

                add(GlassUI.createParameterLine("ID", id = GlassUI.createParameterLineValueLabel()));
                add(GlassUI.createParameterLine("Email", email = GlassUI.createParameterLineValueLabel()));
                add(GlassUI.createParameterLine("Создан", creationTime = GlassUI.createParameterLineValueLabel()));

                add(new TransparentPanel(){{
                    setLayout(new BorderLayout(10, 0));
                    setMargin(10, 0, 0, 0);
                    add(new TransparentPanel(){{
                        setLayout(new OverlayLayout(this));

                        add(new TransparentPanel(){{
                            setLayout(new BorderLayout());
                            add(new TransparentPanel(){
                                {
                                    setLayout(new BorderLayout());
                                    add(new LabelButton(){{
                                        setIcon(createIcon(Resources.Icon_Edit));
                                        setSelectedIcon(createIcon(Resources.Icon_Edit_Selected));
                                        addActionListener(() -> {
                                            File file = chooseSkin();
                                            if(file == null)
                                                return;

                                            try {
                                                BufferedImage skin = ImageIO.read(file);
                                                API.getJSON(ProfileApiMethod.create("profile.setSkin", screen.getLauncher().User.getToken())
                                                        .set("skin", API.toBase64(skin))
                                                        .set("id", selectedPlayer.id)
                                                );
                                                setSelectedPlayer(selectedPlayer);
                                            }catch (Exception ex){
                                                ex.printStackTrace();
                                            }
                                        });
                                    }}, BorderLayout.WEST);
                                    add(new LabelButton(){{
                                        setIcon(createIcon(Resources.Icon_Reply));
                                        setSelectedIcon(createIcon(Resources.Icon_Reply_Selected));
                                        addActionListener(() -> {
                                            NetManager.openLink(API.getMethodUrl(ApiMethod.create("skins.getSkin").set("name", selectedPlayer.name)));
                                        });
                                    }}, BorderLayout.EAST);
                                }
                                Icon createIcon(BufferedImage image){
                                    int size = 25;
                                    return new ImageIcon(ImageUtils.getScaledInstance(image, size, size, BufferedImage.SCALE_SMOOTH));
                                }
                            }, BorderLayout.SOUTH);
                        }}, 0);
                        add(skinViewer = new SkinViewer(){{
                            setPreferredSize(new Dimension(110, 140));
                        }}, 1);
                    }}, BorderLayout.WEST);

                    add(new TransparentPanel(){{
                        setLayout(new VerticalFlowLayout(0, 7));
                        add(resetEmail = new BlurButton(screen, "Сбросить почту"){{
                            addActionListener(e -> {
                                new Thread(() -> {
                                    final Player player = selectedPlayer;
                                    try {
                                        API.getJSON(ProfileApiMethod.create("profiles.resetEmail", screen.getLauncher().User.getToken())
                                                .set("id", selectedPlayer.id)
                                        );
                                    }catch (Exception ex){
                                        ex.printStackTrace();
                                    }
                                    if(player == selectedPlayer)
                                        setSelectedPlayer(selectedPlayer);
                                }).start();
                            });
                        }});
                        add(resetPassword = new BlurButton(screen, "Сбросить пароль"){{
                            addActionListener(e -> {
                                new Thread(() -> {
                                    try {
                                        API.getJSON(ProfileApiMethod.create("profiles.resetPassword", screen.getLauncher().User.getToken())
                                                .set("id", selectedPlayer.id)
                                        );
                                    }catch (Exception ex){
                                        ex.printStackTrace();
                                    }
                                }).start();
                            });
                        }});

                        add(new TransparentPanel(){{
                            setLayout(new BorderLayout(7, 0));
                            WebLabel text = GlassUI.createSimpleLabel("Shift  + ");

                            text.setForeground(GlassUI.Colors.labelLightText);
                            text.setHorizontalAlignment(SwingConstants.RIGHT);
                            text.setVerticalAlignment(SwingConstants.CENTER);
                            add(text);
                            add(new BlurButton.Flat(screen, "Удалить"){
                                {
                                    setPadding(20, 20);
                                    addActionListener(e -> {
                                        if((e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK) {
                                            final Player player = selectedPlayer;
                                            try {
                                                API.getJSON(ProfileApiMethod.create("profiles.removeProfile", screen.getLauncher().User.getToken())
                                                        .set("id", selectedPlayer.id)
                                                );
                                            }catch (Exception ex){
                                                ex.printStackTrace();
                                            }
                                            updateData();
                                        }
                                    });
                                }
                            }, BorderLayout.EAST);
                        }});
                    }});
                }});
            }});
        }catch (Exception ex){
            ex.printStackTrace();
        }
        setSelectedPlayer(null);
    }

    public void setSelectedPlayer(Player player){
        eventEnabled = false;
        selectedPlayer = player;
        infoPanel.setVisible(player != null);
        if(player != null) {
            try {
                JSONObject object = API.getJSON(ProfileApiMethod.create("profiles.getProfileInfo", screen.getLauncher().User.getToken()).set("id", player.id).set("fields", "login,email,status,creation_time"));
                JSONObject info = object.getJSONObject("info");

                id.setText(player.id + "");
                name.setText(info.getString("login"));
                email.setText(info.getString("email"));
                status.setSelected(Arrays.asList(statusList).indexOf(info.getString("status")));
                skinViewer.setPlayerTexture(API.Skins.getSkin(info.getString("login")));
                resetPassword.setEnabled(!info.getString("email").equals("null"));
                resetEmail.setEnabled(!info.getString("email").equals("null"));

                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd");
                creationTime.setText(dateFormat.format(new Date(Long.parseLong(info.getString("creation_time")))));
            }catch (Exception ex){
                ex.printStackTrace();
                name.setText("[Error]");
                infoPanel.setVisible(false);
            }
        }
        eventEnabled = true;
    }

    public void onShow(){
        updateData();
    }

    public void updateData(){
        new Thread(() -> {
            try {
                JSONObject object = API.getJSON(ProfileApiMethod.create("profiles.searchProfiles", screen.getLauncher().User.getToken()).set("search", search.getText()));
                JSONArray info = object.getJSONArray("info");

                ArrayList<Player> players = new ArrayList<>();
                for(int i = 0; i < info.length(); i++)
                    players.add(new Player(info.getJSONObject(i).getInt("id"), info.getJSONObject(i).getString("name")));
                playerList.setContent(players.toArray(new Player[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static File chooseSkin(){
        JFileChooser chooser = new NativeJFileChooser(){{
            setDialogTitle("Выбор скина");
            setFileFilter(new FileFilter() {
                public String getDescription() {
                    return "PNG Images (*.png)";
                }

                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
                }
            });
        }};

        if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile();
        else
            return null;
    }

    public static class Player{
        public final int id;
        public final String name;
        public Player(int id, String name){
            this.id = id;
            this.name = name;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Player player = (Player) o;
            return id == player.id &&
                    Objects.equals(name, player.name);
        }

        public int hashCode() {
            return Objects.hash(id, name);
        }
    }

    public static class PlayerPanel extends BlurList.ContentPanel<Player> {

        private final WebLabel name, id;

        public PlayerPanel(Screen screen) {
            super(screen);
            add(name = GlassUI.createSimpleLabel(""), BorderLayout.WEST);
            add(id = GlassUI.createSimpleLabel(""), BorderLayout.EAST);
            id.setForeground(GlassUI.Colors.labelLightText);
        }

        public void applyContent(Player player) {
            if(player != null) {
                name.setText(player.name);
                id.setText(player.id + "");
            }else{
                name.setText("");
                id.setText("");
            }
        }
    }
}
